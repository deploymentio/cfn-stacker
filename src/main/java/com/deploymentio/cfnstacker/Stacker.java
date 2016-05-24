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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.cloudformation.model.Stack;
import com.deploymentio.cfnstacker.config.StackConfig;
import com.deploymentio.cfnstacker.template.JsonNodeHelper;
import com.fasterxml.jackson.databind.JsonNode;

public class Stacker {
	
	private final static Logger logger = LoggerFactory.getLogger(Stacker.class);

	public static void main(String[] args) {

		boolean success = false;
		
		StackerOptions options = new StackerOptions(args);
		if (options.hasErrors()) {
			for (String err : options.getErrors()) {
				logger.error(err);
			}
		} else {
			try {
				success = new Stacker().run(options);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		System.exit(success ? 0 : 1);
	}
	
	public boolean run(StackerOptions options) throws Exception {

		// fix the logging level first
		new LogbackLevelChanger(options);
		
		// initializing other helper objects
		final StackConfig stackConfig = options.getStackConfig();
		final CloudFormationClient client = new CloudFormationClient(stackConfig);
		final JsonNodeHelper jsonNodeHelper = new JsonNodeHelper(stackConfig);
		final OperationTracker tracker = new OperationTracker();
		
		// see what state the stack is in
		final Stack stack = client.findStack(stackConfig.getName()) ;
		final Status status = Status.valueOf(stack);

		// perform the operation we want
		Action action = options.getDesiredAction();
		if (action.isAllowed(status)) {
			
			logger.debug("About to take action: Action=" + action.name() + " Status=" + status.name());
			switch(action) {
				
				case CREATE:
					return new Operation(client, jsonNodeHelper, tracker) {
						@Override protected String execute(JsonNode templateBody) throws Exception {
							return client.createStack(templateBody, true);
						}
					}.validateAndExecuteStack(stackConfig, "create", false);

				case CREATE_DRY_RUN:
					return new Operation(client, jsonNodeHelper, tracker) {
						@Override protected String execute(JsonNode templateBody) throws Exception {
							return client.createStack(templateBody, true);
						}
					}.validateAndExecuteStack(stackConfig, "create", true);

				case DELETE:
					return new Operation(client, jsonNodeHelper, tracker) {
						@Override protected String execute(JsonNode templateBody) throws Exception {
							client.deleteStack();
							return stack.getStackId();
						}
					}.validateAndExecuteStack(stackConfig, "delete", false);
					
				case UPDATE:
					return new Operation(client, jsonNodeHelper, tracker) {
						@Override protected String execute(JsonNode templateBody) throws Exception {
							return client.updateStack(templateBody);
						}
					}.validateAndExecuteStack(stackConfig, "update", false);

				case UPDATE_DRY_RUN:
					return new Operation(client, jsonNodeHelper, tracker) {
						@Override protected String execute(JsonNode templateBody) throws Exception {
							return client.updateStack(templateBody);
						}
					}.validateAndExecuteStack(stackConfig, "update", true);
			}
		} else {
			logger.error("This action is not allowed: Action=" + action.name() + " Status=" + status.name());
		}
		
		return false;
	}
}