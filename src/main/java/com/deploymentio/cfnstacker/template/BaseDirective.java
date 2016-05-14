package com.deploymentio.cfnstacker.template;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

public abstract class BaseDirective {

	public abstract String getDirectiveName();

	public boolean isMatch(Context ctxt, JsonNode node) {
		return node.getNodeType() == JsonNodeType.OBJECT && node.size() == 1 && node.has(getDirectiveName());		
	}
	
	public abstract JsonNode getReplacementNode(Context ctxt, JsonNode arguments);	
}
