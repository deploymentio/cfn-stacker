package com.deploymentio.cfnstacker.templatelang.directives;

import com.deploymentio.cfnstacker.templatelang.BaseDirective;
import com.deploymentio.cfnstacker.templatelang.Context;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class JoinDirective extends BaseDirective {

	@Override
	public String getDirectiveName() {
		return "Cfntl::Join";
	}

	@Override
	public JsonNode getReplacementNode(Context ctxt, JsonNode arguments) {
		StringBuilder builder = new StringBuilder();
		ArrayNode arrayNode = (ArrayNode) arguments;
		for (int i = 0; i < arrayNode.size(); i++) {
			JsonNode node = arrayNode.get(i);
			builder.append(node.asText(""));
		}
		return new TextNode(builder.toString());
	}
}
