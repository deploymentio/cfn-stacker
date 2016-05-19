package com.deploymentio.cfnstacker.config;

import java.util.Map;

import com.deploymentio.cfnstacker.JsonNodeParseResult;

public interface StackConfigCreator {

	public StackConfig getStackConfig() throws Exception;
	
	public JsonNodeParseResult loadStackTemplate(Fragment fragment, Map<String, String> baseParmeters);
}
