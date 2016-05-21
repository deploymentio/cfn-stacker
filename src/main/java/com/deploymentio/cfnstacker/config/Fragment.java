package com.deploymentio.cfnstacker.config;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

public class Fragment {

	private String path;
	private Map<String, JsonNode> parameters = new HashMap<>();
	
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public Map<String, JsonNode> getParameters() {
		return parameters;
	}
	public void setParameters(Map<String, JsonNode> parameters) {
		this.parameters = parameters;
	}
}
