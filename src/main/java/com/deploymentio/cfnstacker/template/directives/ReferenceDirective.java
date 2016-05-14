package com.deploymentio.cfnstacker.template.directives;

import com.deploymentio.cfnstacker.template.BaseDirective;
import com.deploymentio.cfnstacker.template.Context;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class ReferenceDirective extends BaseDirective {

	@Override
	public String getDirectiveName() {
		return "Cfntl::Ref";
	}

	@Override
	public JsonNode getReplacementNode(Context ctxt, JsonNode arguments) {
		String varName = arguments.asText();
		String val = ctxt.get(varName).toString();
		return new TextNode(val);
	}

}
