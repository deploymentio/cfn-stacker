package com.deploymentio.cfnstacker.config;

import java.util.HashMap;
import java.util.Map;

public class StackConfig extends BaseStackConfig {
	
	private String snsTopic;
	private Map<String, String> properties = new HashMap<>();
	private Map<String, SubStackConfig> suStacks = new HashMap<>();
	
	public String getSnsTopic() {
		return snsTopic;
	}
	public void setSnsTopic(String snsTopic) {
		this.snsTopic = snsTopic;
	}
	public Map<String, String> getProperties() {
		return properties;
	}
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}
	public Map<String, SubStackConfig> getSuStacks() {
		return suStacks;
	}
	public void setSuStacks(Map<String, SubStackConfig> suStacks) {
		this.suStacks = suStacks;
	}
}
