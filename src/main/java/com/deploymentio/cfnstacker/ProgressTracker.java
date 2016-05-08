package com.deploymentio.cfnstacker;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProgressTracker {
	
	private final static Logger logger = LoggerFactory.getLogger(ProgressTracker.class);
	
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
	public ProgressTracker track(CloudFormationClient client, String stackName, String stackId, int checkIntervalSeconds) {
		
		if (stackNames.add(stackName)) {
			logger.info("Tracking Stack: Name=" + stackName + " ID=" + stackId);
			Future<String> future = executor.submit(new ProgressTrackerTask(this, client, stackName, stackId, checkIntervalSeconds));
			trackerRecords.put(stackId, new ProgressTrackerRecord(stackName, future));
		} else {
			logger.debug("Ignoring Stack: Name=" + stackName);
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
			logger.warn("Events generated but tracking record not found: StackId=" + stackId);
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
				logger.debug("Tracker: Done=" + done + " Name=" + trackerRecord.getStackName() + " ID=" + stackId);
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
