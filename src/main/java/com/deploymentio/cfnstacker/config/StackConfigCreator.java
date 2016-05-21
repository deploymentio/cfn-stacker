package com.deploymentio.cfnstacker.config;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

public interface StackConfigCreator {

	public StackConfig getStackConfig() throws Exception;
	
	public JsonParseResult loadStackTemplate(Fragment fragment, Map<String, JsonNode> baseParmeters);
}
