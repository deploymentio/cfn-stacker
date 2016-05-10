package com.deploymentio.cfnstacker.templatelang;

import com.fasterxml.jackson.databind.JsonNode;

public class DirectiveExecutionResult {
	
	private boolean replaced;
	private JsonNode node;
	
	public DirectiveExecutionResult(boolean replaced, JsonNode node) {
		this.replaced = replaced;
		this.node = node;
	}
	
	public boolean isReplaced() {
		return replaced;
	}
	
	public JsonNode getNode() {
		return node;
	}
}
