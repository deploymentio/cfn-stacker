package com.deploymentio.cfnstacker;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.DeleteStackRequest;
import com.amazonaws.services.cloudformation.model.DescribeStackEventsRequest;
import com.amazonaws.services.cloudformation.model.DescribeStackEventsResult;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksResult;
import com.amazonaws.services.cloudformation.model.GetTemplateRequest;
import com.amazonaws.services.cloudformation.model.ListStackResourcesRequest;
import com.amazonaws.services.cloudformation.model.ListStackResourcesResult;
import com.amazonaws.services.cloudformation.model.Output;
import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.cloudformation.model.StackEvent;
import com.amazonaws.services.cloudformation.model.StackResource;
import com.amazonaws.services.cloudformation.model.StackResourceSummary;
import com.amazonaws.services.cloudformation.model.Tag;
import com.amazonaws.services.cloudformation.model.TemplateParameter;
import com.amazonaws.services.cloudformation.model.UpdateStackRequest;
import com.amazonaws.services.cloudformation.model.ValidateTemplateRequest;
import com.amazonaws.services.cloudformation.model.ValidateTemplateResult;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.util.DateUtils;
import com.deploymentio.cfnstacker.config.StackConfig;
import com.deploymentio.cfnstacker.template.JsonFormatter;
import com.deploymentio.cfnstacker.template.TemplateParameters;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CloudFormationClient {
	
	private final static Logger logger = LoggerFactory.getLogger(CloudFormationClient.class);

	protected AmazonS3 s3Client = new AmazonS3Client();
	protected AmazonCloudFormation client = new AmazonCloudFormationClient();
	protected JsonFormatter formatter = new JsonFormatter();

	private StackConfig config;
	private TemplateParameters templateParameters;

	public CloudFormationClient(StackConfig config) {
		this.config = config;
		this.templateParameters = new TemplateParameters(config);
	}
	
	/**
	 * Finds the stack with the given name. Will only find stack still
	 * running
	 * 
	 * @param name name of the stack
	 * @return the stack object or <code>null</code> if no running stack with
	 *         this name was found
	 */
	public Stack findStack(String name) {
		DescribeStacksResult describeStacksResult = null ;
		String nextToken = null ;
		
		do {
			describeStacksResult = client.describeStacks(new DescribeStacksRequest().withNextToken(nextToken)) ;
			nextToken = describeStacksResult.getNextToken() ;
			
			for (Stack stack : describeStacksResult.getStacks()) {
				if (stack.getStackName().equals(name)) {
					return stack ;
				}
			}
		} while (!StringUtils.isEmpty(nextToken)) ;

		return null ;
	}

	/**
	 * Gets a list of all stack resources
	 * 
	 * @param name the stack's name
	 * @param filter an optional filter that can be used to just get selected
	 *        resources
	 */
	public List<StackResource> getStackResources(String name) {
		return getStackResources(name, null) ;
	}
	
	protected List<StackResource> getStackResources(String name, String nextToken) {
		
		ArrayList<StackResource> resources = new ArrayList<StackResource>() ;
		
		ListStackResourcesResult result = client.listStackResources(new ListStackResourcesRequest().withStackName(name).withNextToken(nextToken)) ;
		for (StackResourceSummary summary : result.getStackResourceSummaries()) {
			StackResource resource = new StackResource() ;
			resource.setLogicalResourceId(summary.getLogicalResourceId()) ;
			resource.setPhysicalResourceId(summary.getPhysicalResourceId()) ;
			resource.setResourceType(summary.getResourceType()) ;
			resource.setResourceStatus(summary.getResourceStatus()) ;
			resource.setResourceStatusReason(summary.getResourceStatusReason()) ;
		
			if ("AWS::CloudFormation::Stack".equals(resource.getResourceType())) {
				resources.add(resource) ;
			}
		}
		
		// get more if results were truncated
		if (!StringUtils.isEmpty(result.getNextToken()))
			resources.addAll(getStackResources(name, result.getNextToken())) ;
		
		return resources;
	}

	/**
	 * Gets all non-progress events for stack that were generated after a
	 * certain time. This method will ignore any "throttling" error from AWS and
	 * return empty results.
	 * 
	 * @param stackId unique ID for the stack
	 * @param startDate only events after this time are considered
	 * @return a list of stack events
	 */
	public List<StackEvent> getStackEvents(String stackId, Date startDate) {
		return getStackEvents(stackId, startDate, null, 0);
	}
	
	/**
	 * Gets all non-progress events for stack that were generated after a
	 * certain time. This method will ignore any "throttling" error from AWS and
	 * return empty results.
	 * 
	 * @param stackId unique ID for the stack
	 * @param startDate only events after this time are considered
	 * @return a list of stack events
	 */
	public List<StackEvent> getStackEvents(String stackId, Date startDate, OperationTracker tracker, int checkIntervalSeconds) {

		ArrayList<StackEvent> events = new ArrayList<StackEvent>() ;
		DescribeStackEventsResult result = null ;
		String nextToken = null ;
		
		doLoop: do {
			try {
				result = client.describeStackEvents(new DescribeStackEventsRequest().withStackName(stackId)) ;
			} catch (AmazonServiceException ase) {
				if ("Throttling".equals(ase.getErrorCode())) {
					logger.warn("Got a throttling error from AWS while calling describeStackEvents()") ;
					break ;
				} else {
					throw ase ;
				}
			}
			nextToken = result.getNextToken() ;
			
			for (StackEvent evt : result.getStackEvents()) {

				// break out if we start seeing events older than our start date
				if (!evt.getTimestamp().after(startDate)) {
					if (logger.isTraceEnabled()) {
						logger.trace(createStackEventLogMessage(evt, startDate, "Saw event older than startdate"));
					}
					break doLoop ;
				}

				// mark that an event was generated
				if (tracker != null) {
					tracker.markEventsGenerated(stackId);
				}
				
				// ignore IN_PROGRESS events
				if (!evt.getResourceStatus().endsWith("_IN_PROGRESS")) {
					if (logger.isTraceEnabled()) {
						logger.trace(createStackEventLogMessage(evt, startDate, "Adding event"));
					}
					events.add(evt) ;
				} else {
					if (logger.isTraceEnabled()) {
						logger.trace(createStackEventLogMessage(evt, startDate, "Ignorning event"));
					}
				}
				
				// start tracking a sub-stack if we come across one
				if (tracker != null && evt.getResourceType().equals("AWS::CloudFormation::Stack") && !evt.getPhysicalResourceId().equals(stackId)) {
					tracker.track(this, evt.getLogicalResourceId(), evt.getPhysicalResourceId(), checkIntervalSeconds);
				}
			}
			
		} while (!StringUtils.isEmpty(nextToken)) ;

		// sort the events
		Collections.sort(events, new Comparator<StackEvent>() {
			@Override public int compare(StackEvent e1, StackEvent e2) {
				return e1.getTimestamp().compareTo(e2.getTimestamp());
			}
		});
		
		return events ;
	}

	private String createStackEventLogMessage(StackEvent evt, Date startDate, String message) {
		return message + ": StartDate=" + DateUtils.formatISO8601Date(startDate) + " EventStatus=" + evt.getResourceStatus() + " EventDate=" + DateUtils.formatISO8601Date(evt.getTimestamp()) + " EventId=" + evt.getEventId() + " EventResourceId=" + evt.getLogicalResourceId() + " EventResourceType=" + evt.getResourceType();
	}
	
	/**
	 * Looks up a stack's template from Cloud formation
	 */
	public JsonNode getTemplateValue(String stackName) throws Exception {
		String templateBody = client.getTemplate(new GetTemplateRequest().withStackName(stackName)).getTemplateBody();
		return new ObjectMapper().readTree(templateBody);
	}
	
	/**
	 * Validates the stack template with CloudFormation and ensure that values
	 * were provided for all required parameters
	 * 
	 * @param templateBody ClouadFormation JSON template
	 * @param options options needed to validate the stack template
	 * @return <code>true</code> if the stack is valid, <code>false</code>
	 *         otherwise
	 */
	public boolean validateTemplate(JsonNode templateBody) throws Exception {

		boolean allOK = true ;
		Map<String, String> stackProperties = config.getParameters();
		ValidateTemplateResult validationResult = client.validateTemplate(new ValidateTemplateRequest()
			.withTemplateURL(uploadCfnTemplateToS3(config.getName(), "validate", templateBody))) ;
		
		// check if the template has any parameters without defaults for which no stack properties were provided
		for (TemplateParameter param : validationResult.getParameters()) {
			String key = param.getParameterKey() ;
			if (StringUtils.isEmpty(param.getDefaultValue()) && !stackProperties.containsKey(key)) {
				logger.error("Missing template parameter value: Key=" + key) ;
				allOK = false ;
			}
		}
		
		return allOK ;
	}

	/**
	 * Validates a sub-stack template with CloudFormation.
	 * 
	 * @param templateBody ClouadFormation JSON template
	 */
	public boolean validateSubStackTemplate(String templateBodyUrl) throws Exception {
		client.validateTemplate(new ValidateTemplateRequest().withTemplateURL(templateBodyUrl));
		return true;
	}
	
	/**
	 * Uploads the template to a file in S3 and returns the URL to the s3
	 * resource
	 * 
	 * @param templateBody the template body (JSON)
	 * @return the URL to the s3 resource
	 */
	protected String uploadCfnTemplateToS3(String name, String type, JsonNode templateBody) throws Exception {

		File file = File.createTempFile(name  + "-" + type + "-", ".json");
		formatter.writeFormattedJSONString(templateBody, file);
		
		String key = config.getS3Prefix() + file.getName() ;
		s3Client.putObject(config.getS3Bucket(), key, file) ;
		file.delete() ;

		String url = "https://s3.amazonaws.com/" + config.getS3Bucket() + "/" + key ;
		logger.info("Uploded CFN template to S3: Url=" + url) ;
		return url ;
	}
	
	
	/**
	 * Initiates creation of a new CloudFormation stack with the given options
	 * and template
	 * 
	 * @param templateBody ClouadFormation JSON template to create the stack
	 *        from
	 * @return ID of the new stack
	 */
	public String createStack(JsonNode templateBody, boolean disableRollback) throws Exception {
		List<Tag> tags = new ArrayList<>();
		for (String key : config.getTags().keySet()) {
			tags.add(new Tag().withKey(key).withValue(config.getTags().get(key)));
		}
		return client.createStack(new CreateStackRequest().withStackName(config.getName())
				.withTemplateURL(uploadCfnTemplateToS3(config.getName(), "create", templateBody))
				.withNotificationARNs(config.getSnsTopic()).withCapabilities("CAPABILITY_IAM")
				.withTags(tags).withDisableRollback(disableRollback)
				.withParameters(templateParameters.getApplicableParameters(templateBody))).getStackId();
	}

	/**
	 * Initiate updates to an existing stack with the given options and template
	 * 
	 * @param templateBody updated ClouadFormation JSON template
	 * @return ID of the updated stack
	 */
	public String updateStack(JsonNode templateBody) throws Exception  {
		return client.updateStack(new UpdateStackRequest().withStackName(config.getName())
				.withTemplateURL(uploadCfnTemplateToS3(config.getName(), "update", templateBody))
				.withCapabilities("CAPABILITY_IAM")
				.withParameters(templateParameters.getApplicableParameters(templateBody))).getStackId();
	}

	/**
	 * Initiates deletion of a running stack
	 * 
	 * @param stackName name of the stack that needs to be deleted
	 */
	public void deleteStack(String stackName) {
		client.deleteStack(new DeleteStackRequest().withStackName(stackName));
	}
	
    /**
	 * Prints the output variables for the given stack
	 * 
	 * @param stack the stack
	 */
	public void printStackOutputs(Stack stack) {
		for (Output outputs : stack.getOutputs()) {
			logger.info("Output Variable: Key=" + outputs.getOutputKey() + " Value=" + outputs.getOutputValue());
		}
	}
}
