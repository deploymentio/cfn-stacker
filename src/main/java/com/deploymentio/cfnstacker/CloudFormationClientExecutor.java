package com.deploymentio.cfnstacker;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.cloudformation.model.StackResource;
import com.deploymentio.cfnstacker.config.StackConfig;

public abstract class CloudFormationClientExecutor {
	
	private final static Logger logger = LoggerFactory.getLogger(CloudFormationClientExecutor.class);

	private CloudFormationClient client;
	private JsonNodeHelper jsonNodeHelper;
	private ProgressTracker tracker;
	
	public CloudFormationClientExecutor(CloudFormationClient client, JsonNodeHelper jsonNodeHelper, ProgressTracker tracker) {
		this.client = client;
		this.jsonNodeHelper = jsonNodeHelper;
		this.tracker = tracker;
	}
	
	/**
	 * Validates the stack's template for valid JSON, merge conflicts during JSON
	 * fragment merges, for required template parameters, and then executes the
	 * stack operation.
	 * 
	 * @param options the CLI and other options
	 * @param action description of the action we are performing
	 * @return <code>true</code> if everything went well
	 */
	public boolean validateAndExecuteStack (StackConfig options, String action, boolean validateOnly) throws Exception {
		
		String stackName = options.getName() ;
		String templateBody = null ;
		boolean validated = false ;
		boolean secretHashKeyMismatch = false ;
		
		try {
			
			if (!"create".equals(action)) {
				// print the existing values
				jsonNodeHelper.writeFormattedJSONString(client.getTemplateValue(stackName), new File(".template-existing.json")) ;
				for (StackResource res: client.getStackResources(stackName)) {
					String subStackName = res.getLogicalResourceId();
					jsonNodeHelper.writeFormattedJSONString(client.getTemplateValue(res.getPhysicalResourceId()), new File(".template-existing-" + subStackName + ".json")) ;
				}
			}
			
			// validate any sub-stacks
			Map<String, String> subStackTemplateUrls = new HashMap<>();
			boolean allSubStacksValid = true;
			for(String subStackName: options.getSubStacks().keySet()) {
				String subStackBody = jsonNodeHelper.getCombinedJsonStringForSubStack(subStackName);
				String templateUrl = client.uploadCfnTemplateToS3(options.getName(), subStackName, subStackBody);
				if (client.validateSubStackTemplate(templateUrl)) {
					logger.info("SubStack template with name '" + subStackName + "' is valid") ;
					subStackTemplateUrls.put(subStackName, templateUrl);
					jsonNodeHelper.writeFormattedJSONString(subStackBody, new File(".template-new-" + subStackName + ".json")) ;
				} else {
					logger.error("SubStack template with name '" + subStackName + "' is NOT valid") ;
					allSubStacksValid = false;
					break;
				}
			}
			
			// validate the main stack
			if (allSubStacksValid) {
				
				templateBody = jsonNodeHelper.getCombinedJsonString(subStackTemplateUrls);
				validated = validateTemplateBody(templateBody);
				
				if (!validated) {
					logger.error("Stack template with name '" + options.getName() + "' is NOT valid") ;
				} else {
					logger.info("Stack template with name '" + options.getName() + "' is valid") ;
				}
			}
			
		} catch (Exception e) {
			logger.error("Failure to validate stack template with name '" + stackName + "'", e) ;
		}
		
		if (validated && !validateSecretHashKey()) {
			logger.error("YOU CAN'T MODIFY AN EXISTING STACK UNLESS YOU KNOW THE SECRET!!!") ;
			secretHashKeyMismatch = true ;
		}
		
		if (validated && !secretHashKeyMismatch && !validateOnly) {
			
			// run the before-execute code 
			beforeExecuteStack(client) ;
			
			// execute the code
			logger.info("Attempting to " + action + " stack with name '" + stackName + "'") ;
			String stackId = executeStack(stackName, templateBody, client) ;

			// track its progress
			tracker.track(client, stackName, stackId, 30).waitUntilCompletion();
			
			// run the after-execute code
			afterExecuteStack(client) ;
			logger.info("Stack with name '" + stackName + "' has been " + action + "d. Stack's ID is " + stackId) ;

			if (!"delete".equals(action)) {
                client.printStackOutputs(client.findStack(stackName)) ;
            }
		}

		if(validated && !secretHashKeyMismatch && validateOnly) {
			jsonNodeHelper.writeFormattedJSONString(templateBody, new File(".template-new.json")) ;
		}

		return validated && !secretHashKeyMismatch ;
	}

	protected boolean validateTemplateBody(String templateBody) throws Exception {
		if (!StringUtils.isEmpty(templateBody))
			return client.validateTemplate(templateBody) ;
		return false ;
	}
	
	protected boolean validateSecretHashKey() throws Exception {
		return true ;
	}
	
	/**
	 * Executes a stack operation. This method is called from
	 * {@link CloudFormationClientExecutor#validateAndExecuteStack(String)}
	 * after the stack operation has been validated.
	 * 
	 * @param stackName stack name
	 * @param templateBody the stack's JSON template
	 * @param client the AWS CloudFormation client
	 * @return the stack ID of the stack on which the operation was just
	 *         executed
	 */
	protected abstract String executeStack(String stackName, String templateBody, CloudFormationClient client) throws Exception ;
	
	/**
	 * This method is called by
	 * {@link CloudFormationClientExecutor#validateAndExecuteStack(String)}
	 * after the stack operation is validated but before it is executed. By
	 * default, it does nothing but it can be extended to do something useful.
	 * 
	 * @param client the AWS CloudFormation client
	 */
	protected void beforeExecuteStack(CloudFormationClient client) throws Exception { }

	/**
	 * This method is called by
	 * {@link CloudFormationClientExecutor#validateAndExecuteStack(String)}
	 * after the stack operation has been validated and executed. By default, it
	 * does nothing but it can be extended to do something useful.
	 * 
	 * @param client the AWS CloudFormation client
	 */
	protected void afterExecuteStack(CloudFormationClient client) throws Exception { }
}
