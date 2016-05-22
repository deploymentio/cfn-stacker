package com.deploymentio.cfnstacker;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;

public class StackerOptions {

	protected PrintWriter usageWriter = new PrintWriter(System.err); 

	private Action desiredAction;
	private File configFile;
	private List<String> errors = new ArrayList<>();
	
	public StackerOptions(String[] args) {
		
		Options options = new Options();
		options.addOption(Option.builder("c").desc("Stack configuration file").longOpt("config").hasArg().argName("file").type(File.class).required().build());
		options.addOption(Option.builder("a").desc("Action to take").longOpt("action").hasArg().argName("name").required().build());
		
		String desiredActionValue = null;
		
		CommandLineParser parser = new DefaultParser();
		CommandLine commandLine = null ;
		try {
			commandLine = parser.parse(options, args);
		
			desiredActionValue = commandLine.getOptionValue("action");
			configFile = (File) commandLine.getParsedOptionValue("config");
			
		} catch (ParseException e) {
			errors.add("Invalid or missing arguments, see usage message");
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(usageWriter, 100, "java -jar cfn-stacker.jar -c <file> -a <" + StringUtils.join(Action.values(), '|') + ">", "\nCFN Stacker is used to create/update/delete an AWS CloudFormation stack\n", options, 3, 3, "") ;
			usageWriter.append('\n').flush();
		}
		
		if (!hasErrors()) {
		
			if (!configFile.exists() || !configFile.isFile()) {
				errors.add("Config file doesn't exist: Path=" + configFile.getAbsolutePath());
			}
			
			try {
				desiredAction = Action.valueOf(desiredActionValue);
			} catch (IllegalArgumentException e) {
				errors.add("Action is not valid: Found=" + desiredActionValue + " Expected=" + StringUtils.join(Action.values(), '|'));
			}
		}
	}

	public List<String> getErrors() {
		return errors;
	}
	
	public boolean hasErrors() {
		return !errors.isEmpty();
	}
	
	public Action getDesiredAction() {
		return desiredAction;
	}
	
	public File getConfigFile() {
		return configFile;
	}
}
