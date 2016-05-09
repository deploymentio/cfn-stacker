package com.deploymentio.cfnstacker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.deploymentio.cfnstacker.config.JsonFileStackConfigCreator;
import com.deploymentio.cfnstacker.config.StackConfig;
import com.deploymentio.cfnstacker.config.StackConfigCreator;

public class Stacker {
	
	private final static Logger logger = LoggerFactory.getLogger(Stacker.class);

	public static void main(String[] args) {

		boolean success = false;
		Stacker stacker = new Stacker();
		try {
			success = stacker.run(args[0], Action.valueOf(args[1]));
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.exit(success ? 0 : 1);
	}
	
	public boolean run(String stackConfigFile, Action action) throws Exception {

		StackConfigCreator configCreator = new JsonFileStackConfigCreator(stackConfigFile);
		StackConfig stackConfig = configCreator.getStackConfig();
		CloudFormationClient client = new CloudFormationClient().withConfig(stackConfig);
		JsonNodeHelper jsonNodeHelper = new JsonNodeHelper().withConfig(stackConfig);
		ProgressTracker tracker = new ProgressTracker();
		
		// see what state the stack is in
		Status status = client.getStackExecutionStatus(stackConfig.getName());
		if (action.isAllowed(status)) {
			
			logger.info("About to take action: Action=" + action.name() + " Status=" + status.name());
			switch(action) {
				
				case CREATE:
					return new CloudFormationClientExecutor(client, jsonNodeHelper, tracker) {
						@Override protected String executeStack(String stackName, String templateBody, CloudFormationClient client) throws Exception {
							return client.createStack(templateBody, false);
						}
					}.validateAndExecuteStack(stackConfig, "create", false);

				case CREATE_DRY_RUN:
					return new CloudFormationClientExecutor(client, jsonNodeHelper, tracker) {
						@Override protected String executeStack(String stackName, String templateBody, CloudFormationClient client) throws Exception {
							return client.createStack(templateBody, false);
						}
					}.validateAndExecuteStack(stackConfig, "create", true);

				case DELETE:
					return new CloudFormationClientExecutor(client, jsonNodeHelper, tracker) {
						@Override protected String executeStack(String stackName, String templateBody, CloudFormationClient client) throws Exception {
							client.deleteStack(stackName);
							return null;
						}
					}.validateAndExecuteStack(stackConfig, "delete", false);
					
				case UPDATE:
					return new CloudFormationClientExecutor(client, jsonNodeHelper, tracker) {
						@Override protected String executeStack(String stackName, String templateBody, CloudFormationClient client) throws Exception {
							return client.updateStack(templateBody);
						}
					}.validateAndExecuteStack(stackConfig, "update", false);

				case UPDATE_DRY_RUN:
					return new CloudFormationClientExecutor(client, jsonNodeHelper, tracker) {
						@Override protected String executeStack(String stackName, String templateBody, CloudFormationClient client) throws Exception {
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