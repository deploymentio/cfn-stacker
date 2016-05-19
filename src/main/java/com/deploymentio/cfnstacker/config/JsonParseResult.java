package com.deploymentio.cfnstacker.config;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonParseResult {
	
	private JsonNode node;
	private String error;

	public JsonParseResult(String error) {
		this.error = error;
	}
	
	public JsonParseResult(JsonNode node) {
		this.node = node;
	}
	
	public String getError() {
		return error;
	}
	
	public JsonNode getNode() {
		return node;
	}
	
	public boolean hasError() {
		return getError() != null;
	}
}
