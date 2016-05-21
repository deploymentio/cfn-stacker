package com.deploymentio.cfnstacker;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.cloudformation.model.StackResource;
import com.deploymentio.cfnstacker.config.StackConfig;
import com.deploymentio.cfnstacker.config.SubStackConfig;
import com.deploymentio.cfnstacker.template.JsonFormatter;
import com.deploymentio.cfnstacker.template.JsonNodeHelper;
import com.fasterxml.jackson.databind.JsonNode;

public abstract class Operation {
	
	private final static Logger logger = LoggerFactory.getLogger(Operation.class);

	private CloudFormationClient client;
	private JsonNodeHelper jsonNodeHelper;
	private OperationTracker tracker;
	
	public Operation(CloudFormationClient client, JsonNodeHelper jsonNodeHelper, OperationTracker tracker) {
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
		
		JsonFormatter formatter = new JsonFormatter();
		String stackName = options.getName() ;
		JsonNode templateBody = null ;
		boolean validated = false ;
		
		try {
			
			if (!"create".equals(action)) {
				// print the existing values
				formatter.writeFormattedJSONString(client.getTemplateValue(stackName), new File(".template-existing.json")) ;
				for (StackResource res: client.getStackResources(stackName)) {
					String subStackName = res.getLogicalResourceId();
					formatter.writeFormattedJSONString(client.getTemplateValue(res.getPhysicalResourceId()), new File(".template-existing-" + subStackName + ".json")) ;
				}
			}
			
			// validate any sub-stacks
			Map<String, String> subStackTemplateUrls = new HashMap<>();
			boolean allSubStacksValid = true;
			for(SubStackConfig subStackConfig: options.getSubStacks()) {
				String subStackName = subStackConfig.getName();
				JsonNode subStackBody = jsonNodeHelper.getSubStackMergedJson(subStackConfig);
				String templateUrl = client.uploadCfnTemplateToS3(options.getName(), subStackName, subStackBody);
				if (client.validateSubStackTemplate(templateUrl)) {
					logger.info("SubStack template with name '" + subStackName + "' is valid") ;
					subStackTemplateUrls.put(subStackName, templateUrl);
					formatter.writeFormattedJSONString(subStackBody, new File(".template-new-" + subStackName + ".json")) ;
				} else {
					logger.error("SubStack template with name '" + subStackName + "' is NOT valid") ;
					allSubStacksValid = false;
					break;
				}
			}
			
			// validate the main stack
			if (allSubStacksValid) {
				
				templateBody = jsonNodeHelper.getStackMergedJson(subStackTemplateUrls);
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
		
		if (validated && !validateOnly) {
			
			// execute the code
			logger.info("Attempting to " + action + " stack with name '" + stackName + "'") ;
			String stackId = execute(stackName, templateBody, client) ;

			// track its progress
			tracker.track(client, stackName, stackId, 30).waitUntilCompletion();
			
			if (!"delete".equals(action)) {
                client.printStackOutputs(client.findStack(stackName)) ;
            }
		}

		if(validated && validateOnly) {
			formatter.writeFormattedJSONString(templateBody, new File(".template-new.json")) ;
		}

		return validated;
	}

	protected boolean validateTemplateBody(JsonNode templateBody) throws Exception {
		if (templateBody != null) {
			return client.validateTemplate(templateBody);
		}
		return false ;
	}
	
	/**
	 * Executes a stack operation. This method is called from
	 * {@link Operation#validateAndExecuteStack(String)}
	 * after the stack operation has been validated.
	 * 
	 * @param stackName stack name
	 * @param templateBody the stack's JSON template
	 * @param client the AWS CloudFormation client
	 * @return the stack ID of the stack on which the operation was just
	 *         executed
	 */
	protected abstract String execute(String stackName, JsonNode templateBody, CloudFormationClient client) throws Exception ;
}
