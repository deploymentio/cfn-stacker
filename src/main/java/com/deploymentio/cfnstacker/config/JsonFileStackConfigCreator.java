package com.deploymentio.cfnstacker.config;

import java.io.File;

import com.deploymentio.cfnstacker.JsonNodeParseResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonFileStackConfigCreator implements StackConfigCreator {

	private ObjectMapper mapper = new ObjectMapper();
	private File baseDir;
	private StackConfig config;
	
	public JsonFileStackConfigCreator(String path) throws Exception {
		File file = new File(path);
		baseDir = file.getParentFile();
		config = mapper.readValue(file, StackConfig.class);
		config.setConfigCreator(this);
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
