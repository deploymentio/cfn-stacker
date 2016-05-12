package com.deploymentio.cfnstacker.templatelang.directives;

import java.util.ArrayList;
import java.util.List;

import com.deploymentio.cfnstacker.templatelang.BaseDirective;
import com.deploymentio.cfnstacker.templatelang.Context;
import com.deploymentio.cfnstacker.templatelang.DirectiveExecutionResult;
import com.deploymentio.cfnstacker.templatelang.Scanner;
import com.fasterxml.jackson.databind.JsonNode;

public class Registry {

	private List<BaseDirective> directives = new ArrayList<>();
	
	public Registry() {
		directives.add(new ReferenceDirective());
		directives.add(new JoinDirective());
	}
	
	public DirectiveExecutionResult execute(Scanner scanner, Context ctxt, JsonNode node) {
		
		for (BaseDirective directive: directives) {
			if (directive.isMatch(ctxt, node)) {
				JsonNode argumentNode = node.get(directive.getDirectiveName());
				argumentNode = scanner.scanAndExecute(new Context(ctxt), argumentNode);
				
				JsonNode replacementNode = directive.getReplacementNode(ctxt, argumentNode);
				return new DirectiveExecutionResult(true, replacementNode);
			}
		}
		
		return new DirectiveExecutionResult(false, node);
	}
}
