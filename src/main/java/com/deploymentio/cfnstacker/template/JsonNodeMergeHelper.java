package com.deploymentio.cfnstacker.template;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonNodeMergeHelper {

	/**
	 * Merges the source's JSON tree into the sink JSON tree. Json leaf nodes
	 * with conflicts are not merged.
	 * 
	 * @param sink the source JSON tree will be merges here
	 * @param source this JSON tree will be merged in the sing
	 * @return a list of fully qualified keys that were not merged because of
	 *         conflicts
	 */
	public List<String> merge(JsonNode sink, JsonNode source) {
		return mergeJsonNodes(sink, source, new Stack<String>()) ;
	}
	
	/**
	 * Merges the source's JSON tree into the sink JSON tree. Json leaf nodes
	 * with conflicts are not merged.
	 * 
	 * @param sink the source JSON tree will be merges here
	 * @param source this JSON tree will be merged in the sing
	 * @param stack a stack of current sink node's parents, this stack is used
	 *        to construct a fully qualified key name when a merge conflict is
	 *        found
	 * @return a list of fully qualified keys that were not merged because of
	 *         conflicts
	 */
	private List<String> mergeJsonNodes(JsonNode sink, JsonNode source, Stack<String> stack) {
		
		ArrayList<String> errors = new ArrayList<String>() ;
		Iterator<String> fieldNames = source.fieldNames();
		while (fieldNames.hasNext()) {
	
			String fieldName = fieldNames.next();
			JsonNode sinkValue = sink.get(fieldName);
			// if field doesn't exist or is an embedded object
			if (sinkValue != null && sinkValue.isObject()) {
				stack.push(fieldName) ;
				errors.addAll(mergeJsonNodes(sinkValue, source.get(fieldName), stack));
				stack.pop() ;
			} else {
				if (sink instanceof ObjectNode) {
					JsonNode sourceValue = source.get(fieldName);
					if (sinkValue == null) {
						// the value doesn't exist in sink so all is OK
						((ObjectNode) sink).set(fieldName, sourceValue);
					} else if (sinkValue.isArray() && sourceValue.isArray()) {
						// if the values are arrays, merge them
						((ArrayNode)sinkValue).addAll((ArrayNode)sourceValue) ;
					} else if (sinkValue.hashCode() != sourceValue.hashCode()) {
						// will not overwrite fields
						errors.add(toJsonPath(stack, fieldName)) ;
					}
				}
			}
		}
		return errors ;
	}

	/**
	 * Creates a fully qualified JSON key for the given key and its parent key's
	 * stack
	 * 
	 * @param stack stack of current key's parents
	 * @param currentKey the current key
	 * @return full qualified JSON key
	 */
	private String toJsonPath(Stack<String> stack, String currentKey) {
		StringBuilder pathBuilder = new StringBuilder() ;
		for (String key : stack)
			pathBuilder.append("/").append(key) ;
		pathBuilder.append("/").append(currentKey) ;
		return pathBuilder.toString() ;
	}
}
