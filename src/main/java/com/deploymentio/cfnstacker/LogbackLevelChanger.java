package com.deploymentio.cfnstacker;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class LogbackLevelChanger {

	public LogbackLevelChanger(StackerOptions options) {
		
		String level = "info";
		if (options.isTraceEnabled()) {
			level = "trace";
		} else if (options.isDebugEnabled()) {
			level = "debug";
		}

		Logger rootLogger = (Logger)LoggerFactory.getLogger(getClass().getPackage().getName());
		rootLogger.setLevel(Level.toLevel(level));
	}
}
