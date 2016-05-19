package com.deploymentio.cfnstacker.config;

import java.util.HashMap;
import java.util.Map;

public class Fragment {

	private String path;
	private Map<String, String> parameters = new HashMap<>();
	
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public Map<String, String> getParameters() {
		return parameters;
	}
	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}
}
