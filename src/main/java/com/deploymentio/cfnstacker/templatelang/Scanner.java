package com.deploymentio.cfnstacker.templatelang;

import java.util.Iterator;

import com.deploymentio.cfnstacker.templatelang.directives.Registry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Scanner {

	private Registry registry = new Registry();
	public Scanner withRegistry(Registry registry) {
		this.registry = registry;
		return this;
	}
	
	public JsonNode scanAndExecute(Context context, JsonNode template) {
		
		if (template.getNodeType() == JsonNodeType.ARRAY) {

			for (int i = 0; i < template.size(); i++) {
				
				JsonNode node = template.get(i);
				DirectiveExecutionResult result = registry.execute(this, context, node);
				if (result.isReplaced()) {
					((ArrayNode)template).set(i, result.getNode());
				} else {
					scanAndExecute(new Context(context), node);
				}
			}
			
		} else if (template.getNodeType() == JsonNodeType.OBJECT) {
		
			DirectiveExecutionResult result = registry.execute(this, context, template);
			if (!result.isReplaced()) {
				for (Iterator<String> fieldIter = template.fieldNames(); fieldIter.hasNext(); ) {
					String name = fieldIter.next();
					JsonNode node = template.get(name);
					
					result = registry.execute(this, context, node);
					if (result.isReplaced()) {
						((ObjectNode)template).set(name, result.getNode());
					} else {
						scanAndExecute(new Context(context), node);
					}
				}
			} else {
				return result.getNode();
			}
		}
		
		return template;
	}
}
