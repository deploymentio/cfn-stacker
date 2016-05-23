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

import java.util.Date;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.cloudformation.model.StackEvent;
import com.amazonaws.util.DateUtils;

public class OperationTracker {
	
	private final static Logger logger = LoggerFactory.getLogger(OperationTracker.class);
	
	private Map<String, ProgressTrackerRecord> trackerRecords = new ConcurrentHashMap<>();
	private ConcurrentSkipListSet<String> stackNames = new ConcurrentSkipListSet<>();
	private ExecutorService executor = Executors.newCachedThreadPool();
	
	/**
	 * Adds a new ongoing stack operation to track. If a task (based on
	 * taskName) is already tracked, it is silently ignored.
	 * 
	 * @param client
	 *            the CFN client
	 * @param stackName
	 *            stack name
	 * @param stackId
	 *            stack ID
	 * @param checkIntervalSeconds
	 *            how often to check the stack operation's progress
	 */
	public OperationTracker track(final CloudFormationClient client, final String stackName, final String stackId, final int checkIntervalSeconds) {
		
		if (stackNames.add(stackName)) {
			logger.debug("Tracking Stack: Name=" + stackName + " ID=" + stackId);
			Future<String> future = executor.submit(new Callable<String>() {
				@Override public String call() throws Exception {
					// set the time to be a minute in the past - this is to account for any
					// time differences between the local clock and clock on CFN servers
					Date startTime = new Date(System.currentTimeMillis()-60000) ;
					while(true) {
						
						if (logger.isTraceEnabled()) {
							logger.trace("Waiting for completion: StartDate=" + DateUtils.formatISO8601Date(startTime)) ;
						}
						
						// display all events and look for the final event for the stack itself
						boolean getOutLater = false ;
						for (StackEvent evt : client.getStackEvents(stackId, startTime, OperationTracker.this, checkIntervalSeconds)) {
							
							if (evt.getResourceType().equals("AWS::CloudFormation::Stack")) {
								
								if (evt.getPhysicalResourceId().equals(stackId)) {
									getOutLater = true ;
								}
							}
								
							// record the latest timestamp
							if (evt.getTimestamp().after(startTime))
								startTime = evt.getTimestamp() ;
					
							if (logger.isDebugEnabled()) {
								logger.info("EventDate=" + DateUtils.formatISO8601Date(evt.getTimestamp()) + " Stack=" + stackName + " Type=" + evt.getResourceType() + " ID=" + evt.getLogicalResourceId() + " Status=" + evt.getResourceStatus());
							} else {
								logger.info("Stack=" + stackName + " Type=" + evt.getResourceType() + " ID=" + evt.getLogicalResourceId() + " Status=" + evt.getResourceStatus());
							}
						}
								
						if (getOutLater) {
							break ;
						} else {
							Thread.sleep(checkIntervalSeconds*1000);
						}
					}

					return stackId;
				}
			});
			trackerRecords.put(stackId, new ProgressTrackerRecord(stackName, future));
		} else {
			logger.trace("Ignoring Stack: Name=" + stackName);
		}
		
		return this;
	}
	
	/**
	 * Called to notify the tracker when there are any events generated for a particular stacker ID
	 */
	public void markEventsGenerated(String stackId) {
		ProgressTrackerRecord record = trackerRecords.get(stackId);
		if (record != null) {
			record.markEventsGenerated();
		} else {
			logger.trace("Events generated but tracking record not found: StackId=" + stackId);
		}
	}
	
	/**
	 * Waits until all tracked tasks are done
	 */
	public void waitUntilCompletion() {
		
		while(true) {
			
			boolean allDone = true;
			for (String stackId : trackerRecords.keySet()) {
				ProgressTrackerRecord trackerRecord = trackerRecords.get(stackId);
				boolean done = trackerRecord.isDone(60000);
				logger.trace("Done=" + done + " Name=" + trackerRecord.getStackName() + " ID=" + stackId);
				allDone = allDone && done ;
			}
	
			if (allDone) {
				break;
			} else {
				try {
					Thread.sleep(15 * 1000);
				} catch (Exception e) {
					logger.warn("Could not wait until completion", e);
					break;
				}
			}
		}
	}
	
	class ProgressTrackerRecord {
		
		private String stackName;
		private Future<String> future;
		private boolean eventsGenerated;
		private Date added;
		
		public ProgressTrackerRecord(String stackName, Future<String> future) {
			this.stackName = stackName;
			this.future = future;
			this.added = new Date();
		}

		public void markEventsGenerated() {
			this.eventsGenerated = true;
		}
		
		public boolean isEventsGenerated() {
			return eventsGenerated;
		}
		
		public Future<String> getFuture() {
			return future;
		}
		
		public String getStackName() {
			return stackName;
		}
		
		public boolean isDone(long firstEventTimeoutMillis) {
			boolean done = future.isDone() || future.isCancelled();
			if (!done) {
				long msecsSinceAdded = System.currentTimeMillis()-added.getTime();
				done = !eventsGenerated && msecsSinceAdded > firstEventTimeoutMillis;
			}
			return done;
		}
	}
}
