package com.deploymentio.cfnstacker.config;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.deploymentio.cfnstacker.JsonNodeParseResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonFileStackConfigCreator implements StackConfigCreator {

	private final static Logger logger = LoggerFactory.getLogger(JsonFileStackConfigCreator.class);

	private ObjectMapper mapper = new ObjectMapper();
	private File baseDir;
	private StackConfig config;
	
	public JsonFileStackConfigCreator(String path) throws Exception {
		File file = new File(path);
		baseDir = file.getParentFile();
		
		JsonNode node = mapper.readTree(file);
		config = mapper.convertValue(node.get("Stacker"), StackConfig.class);
		config.setConfigCreator(this);
		config.setTemplate(node);
		((ObjectNode)node).remove("Stacker");
		
		for(String subStackName: config.getSubStacks().keySet()) {
			SubStackConfig subStack = config.getSubStacks().get(subStackName);
			subStack.setName(subStackName);
			
			JsonNodeParseResult result = loadStackTemplate(subStack.getPath());
			if(!result.hasError()) {
				subStack.setTemplate(result.getNode());
			} else {
				logger.error("Error parsing sub-stack: Name=" + subStackName + " " + result.getError());
			}
		}
	}
	
	@Override
	public StackConfig getStackConfig() throws Exception {
		return config;
	}

	@Override
	public JsonNodeParseResult loadStackTemplate(String templatePathName) {
		File file = new File(baseDir, templatePathName);
		if (!file.exists()) {
			String error = "File=" + templatePathName + " Error=MissingTemplate FullPath=" + file.getAbsolutePath();
			return new JsonNodeParseResult(error);
		} else {
			try {
				JsonNode node = mapper.readTree(file);
				return new JsonNodeParseResult(node); 
			} catch (Exception e) {
				return new JsonNodeParseResult("File=" + templatePathName + " Error=" + e.getMessage());
			}
		}
	}
}
