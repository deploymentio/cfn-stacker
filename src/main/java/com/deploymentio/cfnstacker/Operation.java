/*
 * Copyright 2016 - Deployment IO
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
	 * @param config the CLI and other options
	 * @param action description of the action we are performing
	 * @return <code>true</code> if everything went well
	 */
	public boolean validateAndExecuteStack(StackConfig config, String action, boolean validateOnly) throws Exception {
		
		JsonFormatter formatter = new JsonFormatter();
		String stackName = config.getName();
		JsonNode templateBody = null;
		boolean validated = false;
		
		try {
			
			if (!"create".equals(action)) {
				// print the existing values
				formatter.writeFormattedJSONString(client.getTemplateValue(stackName), new File(".template-existing.json"));
				for (StackResource res: client.getStackResources(stackName)) {
					String subStackName = res.getLogicalResourceId();
					formatter.writeFormattedJSONString(client.getTemplateValue(res.getPhysicalResourceId()), new File(".template-existing-" + subStackName + ".json"));
				}
			}
			
			// validate any sub-stacks
			Map<String, String> subStackTemplateUrls = new HashMap<>();
			boolean allSubStacksValid = true;
			for(SubStackConfig subStackConfig: config.getSubStacks()) {
				String subStackName = subStackConfig.getName();
				JsonNode subStackBody = jsonNodeHelper.getSubStackMergedJson(subStackConfig);
				String templateUrl = client.uploadCfnTemplateToS3(config.getName(), subStackName, subStackBody);
				if (client.validateSubStackTemplate(templateUrl)) {
					logger.info("SubStack template with name '" + subStackName + "' is valid");
					subStackTemplateUrls.put(subStackName, templateUrl);
					formatter.writeFormattedJSONString(subStackBody, new File(".template-new-" + subStackName + ".json"));
				} else {
					logger.error("SubStack template with name '" + subStackName + "' is NOT valid");
					allSubStacksValid = false;
					break;
				}
			}
			
			// validate the main stack
			if (allSubStacksValid) {
				
				templateBody = jsonNodeHelper.getStackMergedJson(subStackTemplateUrls);
				validated = validateTemplateBody(templateBody);
				
				if (!validated) {
					logger.error("Stack template with name '" + config.getName() + "' is NOT valid");
				} else {
					logger.info("Stack template with name '" + config.getName() + "' is valid");
				}
			}
			
		} catch (Exception e) {
			logger.error("Failure to validate stack template with name '" + stackName + "'", e);
		}
		
		if (validated && !validateOnly) {
			
			// execute the code
			logger.info("Attempting to " + action + " stack with name '" + stackName + "'");
			String stackId = execute(templateBody);

			// track its progress
			tracker.track(client, stackName, stackId, 30).waitUntilCompletion();
			
			if (!"delete".equals(action)) {
                client.printStackOutputs(client.findStack(stackName));
            }
		}

		if(validated && validateOnly) {
			formatter.writeFormattedJSONString(templateBody, new File(".template-new.json"));
		}

		return validated;
	}

	protected boolean validateTemplateBody(JsonNode templateBody) throws Exception {
		if (templateBody != null) {
			return client.validateTemplate(templateBody);
		}
		return false;
	}
	
	/**
	 * Executes a stack operation. This method is called from
	 * {@link Operation#validateAndExecuteStack(String)}
	 * after the stack operation has been validated.
	 * 
	 * @param templateBody the stack's JSON template
	 * @return the stack ID of the stack on which the operation was just
	 *         executed
	 */
	protected abstract String execute(JsonNode templateBody) throws Exception;
}
