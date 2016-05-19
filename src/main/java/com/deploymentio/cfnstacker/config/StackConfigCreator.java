package com.deploymentio.cfnstacker.config;

import java.util.Map;

public interface StackConfigCreator {

	public StackConfig getStackConfig() throws Exception;
	
	public JsonParseResult loadStackTemplate(Fragment fragment, Map<String, String> baseParmeters);
}
