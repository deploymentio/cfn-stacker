package com.deploymentio.cfnstacker.templatelang.directives;

import java.util.ArrayList;
import java.util.List;

import com.deploymentio.cfnstacker.templatelang.BaseDirective;
import com.deploymentio.cfnstacker.templatelang.Context;
import com.deploymentio.cfnstacker.templatelang.DirectiveExecutionResult;
import com.fasterxml.jackson.databind.JsonNode;

public class Registry {

	private List<BaseDirective> directives = new ArrayList<>();
	
	public Registry() {
		directives.add(new ReferenceDirective());
	}
	
	public DirectiveExecutionResult execute(Context ctxt, JsonNode node) {
		
		for (BaseDirective directive: directives) {
			if (directive.isMatch(ctxt, node)) {
				JsonNode argumentNode = node.get(directive.getDirectiveName());
				JsonNode replacementNode = directive.getReplacementNode(ctxt, argumentNode);
				return new DirectiveExecutionResult(true, replacementNode);
			}
		}
		
		return new DirectiveExecutionResult(false, node);
	}
}
