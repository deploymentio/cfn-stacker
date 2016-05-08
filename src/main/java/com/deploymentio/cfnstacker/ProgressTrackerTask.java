package com.deploymentio.cfnstacker;

import java.util.Date;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.cloudformation.model.StackEvent;
import com.amazonaws.util.DateUtils;

/**
 * Waits until a stack operation is complete while printing out events
 * generated for the stack
 */

public class ProgressTrackerTask implements Callable<String> {
	
	private final static Logger logger = LoggerFactory.getLogger(ProgressTrackerTask.class);
	
	private ProgressTracker tracker;
	private CloudFormationClient client;
	private String stackName;
	private String stackId;
	private int checkIntervalSeconds;

	/**
	 * @param client the CloudFormation client
	 * @param stackName stack's name
	 * @param stackId stack's ID
	 * @param checkIntervalSeconds how often to check for new events while stack
	 *        operation is completed
	 */
	public ProgressTrackerTask(ProgressTracker tracker, CloudFormationClient client, String stackName, String stackId, int checkIntervalSeconds) {
		this.tracker = tracker;
		this.client = client;
		this.stackName = stackName;
		this.stackId = stackId;
		this.checkIntervalSeconds = checkIntervalSeconds;
	}

	@Override
	public String call() throws Exception {
		
		// set the time to be a minute in the past - this is to account for any
		// time differences between the local clock and clock on CFN servers
		Date startTime = new Date(System.currentTimeMillis()-60000) ;
		
		while(true) {
			
			if (logger.isTraceEnabled()) {
				logger.trace("Waiting for completion: StartDate=" + DateUtils.formatISO8601Date(startTime)) ;
			}
			
			// display all events and look for the final event for the stack itself
			boolean getOutLater = false ;
			for (StackEvent evt : client.getStackEvents(stackId, startTime, tracker, checkIntervalSeconds)) {
				
				if (evt.getResourceType().equals("AWS::CloudFormation::Stack")) {
					
					if (evt.getPhysicalResourceId().equals(stackId)) {
						getOutLater = true ;
					}
				}
					
				// record the latest timestamp
				if (evt.getTimestamp().after(startTime))
					startTime = evt.getTimestamp() ;
				
				logger.info("Date=" + DateUtils.formatISO8601Date(evt.getTimestamp()) + " Stack=" + stackName + " Type=" + evt.getResourceType() + " ID=" + evt.getLogicalResourceId() + " Status=" + evt.getResourceStatus());
			}
					
			if (getOutLater) {
				break ;
			} else {
				Thread.sleep(checkIntervalSeconds*1000);
			}
		}

		return stackId;
	}
}
