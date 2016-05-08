package com.deploymentio.cfnstacker.config;

import com.deploymentio.cfnstacker.JsonNodeParseResult;

public interface StackConfigCreator {

	public StackConfig getStackConfig() throws Exception;
	
	public JsonNodeParseResult loadStackTemplate(String templatePathName);
}
