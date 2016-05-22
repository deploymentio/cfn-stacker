package com.deploymentio.cfnstacker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.cloudformation.model.Stack;
import com.deploymentio.cfnstacker.config.JsonFileStackConfigCreator;
import com.deploymentio.cfnstacker.config.StackConfig;
import com.deploymentio.cfnstacker.config.StackConfigCreator;
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
		
		// loading stack configuration
		logger.debug("Looking at configuration file: File=" + options.getConfigFile().getAbsolutePath());
		StackConfigCreator configCreator = new JsonFileStackConfigCreator(options.getConfigFile());
		StackConfig stackConfig = configCreator.getStackConfig();
		
		// initializing other helper objects
		CloudFormationClient client = new CloudFormationClient(stackConfig);
		JsonNodeHelper jsonNodeHelper = new JsonNodeHelper(stackConfig);
		OperationTracker tracker = new OperationTracker();
		
		// see what state the stack is in
		final Stack stack = client.findStack(stackConfig.getName()) ;
		final Status status = Status.valueOf(stack);

		// perform the operation we want
		Action action = options.getDesiredAction();
		logger.debug("About to take action: Action=" + action.name() + " Status=" + status.name());
		if (action.isAllowed(status)) {
			
			switch(action) {
				
				case CREATE:
					return new Operation(client, jsonNodeHelper, tracker) {
						@Override protected String execute(String stackName, JsonNode templateBody, CloudFormationClient client) throws Exception {
							return client.createStack(templateBody, true);
						}
					}.validateAndExecuteStack(stackConfig, "create", false);

				case CREATE_DRY_RUN:
					return new Operation(client, jsonNodeHelper, tracker) {
						@Override protected String execute(String stackName, JsonNode templateBody, CloudFormationClient client) throws Exception {
							return client.createStack(templateBody, true);
						}
					}.validateAndExecuteStack(stackConfig, "create", true);

				case DELETE:
					return new Operation(client, jsonNodeHelper, tracker) {
						@Override protected String execute(String stackName, JsonNode templateBody, CloudFormationClient client) throws Exception {
							client.deleteStack(stackName);
							return stack.getStackId();
						}
					}.validateAndExecuteStack(stackConfig, "delete", false);
					
				case UPDATE:
					return new Operation(client, jsonNodeHelper, tracker) {
						@Override protected String execute(String stackName, JsonNode templateBody, CloudFormationClient client) throws Exception {
							return client.updateStack(templateBody);
						}
					}.validateAndExecuteStack(stackConfig, "update", false);

				case UPDATE_DRY_RUN:
					return new Operation(client, jsonNodeHelper, tracker) {
						@Override protected String execute(String stackName, JsonNode templateBody, CloudFormationClient client) throws Exception {
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