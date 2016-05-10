package com.deploymentio.cfnstacker.templatelang.directives;

import com.deploymentio.cfnstacker.templatelang.BaseDirective;
import com.deploymentio.cfnstacker.templatelang.Context;
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
