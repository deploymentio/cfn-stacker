package com.deploymentio.cfnstacker;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonNodeParseResult {
	
	private JsonNode node;
	private String error;

	public JsonNodeParseResult(String error) {
		this.error = error;
	}
	
	public JsonNodeParseResult(JsonNode node) {
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
