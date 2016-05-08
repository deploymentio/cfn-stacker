package com.deploymentio.cfnstacker;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.deploymentio.cfnstacker.config.StackConfig;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonNodeHelper {

	private final static Logger logger = LoggerFactory.getLogger(JsonNodeHelper.class);

	private StackConfig config;
	public JsonNodeHelper withConfig(StackConfig config) {
		this.config = config;
		return this;
	}
	
	/**
	 * Gets complete CloudFormation sub-stack template as a JSON string. The template is
	 * created by merging several JSON fragments stored in files.
	 * 
	 * @return merged JSON CloudFormation template
	 */
	public String getCombinedJsonStringForSubStack(String subStackName) throws Exception {
		
		boolean hasErrors = false ;
		JsonNode combinedTemplateNode = null ;
		
		for (String name: config.getSubStacks().get(subStackName).getTemplates()) {
			
			JsonNodeParseResult jsonNodeParseResult = config.getConfigCreator().loadStackTemplate(name);
			if (jsonNodeParseResult.hasError()) {
				logger.error("Error parsing sub-stack template fragement: SubStack=" + subStackName + " " + jsonNodeParseResult.getError()) ;
				hasErrors = true ;
				continue ;
			}
			
			JsonNode node = jsonNodeParseResult.getNode();
			if (combinedTemplateNode == null) {
				combinedTemplateNode = node ;
			} else {
				List<String> errors = mergeJsonNodes(combinedTemplateNode, node) ;
				for (String err : errors) {
					logger.error("Duplicate keys while merging template fragments: Template=" + name + " Key=" + err) ;
					hasErrors = true ;
				}
			}
		}
		
		return hasErrors ? null : combinedTemplateNode.toString();
	}

	/**
	 * Gets complete CloudFormation template as a JSON string. The template is
	 * created by merging several JSON fragments stored in files.
	 * 
	 * @param subStackTemplateUrls
	 *            sub-stack template URLs that will be added to the template as
	 *            parameters
	 * @return merged JSON CloudFormation template
	 */
	public String getCombinedJsonString(Map<String, String> subStackTemplateUrls) throws Exception {
		
		boolean hasErrors = false ;
		JsonNode combinedTemplateNode = null ;
		
		for (String name : config.getTemplates()) {
			
			JsonNodeParseResult jsonNodeParseResult = config.getConfigCreator().loadStackTemplate(name);
			if (jsonNodeParseResult.hasError()) {
				logger.error("Error parsing template fragement: " + jsonNodeParseResult.getError()) ;
				hasErrors = true ;
				continue ;
			}
			
			JsonNode node = jsonNodeParseResult.getNode();
			if (combinedTemplateNode == null) {
				combinedTemplateNode = node ;
			} else {
				List<String> errors = mergeJsonNodes(combinedTemplateNode, node) ;
				for (String err : errors) {
					logger.error("Duplicate keys while merging template fragments: Template=" + name + " Key=" + err) ;
					hasErrors = true ;
				}
			}
		}
		
		if (!hasErrors) {
			generateSecretHashKey(config.getName(), combinedTemplateNode);

			ObjectNode parameters = (ObjectNode) combinedTemplateNode.get("Parameters");
			for (String key: subStackTemplateUrls.keySet()) {
				String value = subStackTemplateUrls.get(key);
				parameters.putObject(key + "TemplateURL")
					.put("Description", "S3 Template URL for " + key + " sub-stack")
					.put("Type", "String")
					.put("Default", value);
			}
		}
		
		return hasErrors ? null : combinedTemplateNode.toString();
	}

	/**
	 * Write formatted version of the given JSON string to the given file.
	 */
	public void writeFormattedJSONString(String json, File file) throws IOException, JsonGenerationException, JsonMappingException {
		
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode node = objectMapper.readTree(json);
		objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

		FileWriter writer = new FileWriter(file) ;
		objectMapper.writeValue(writer, node);
		
		IOUtils.closeQuietly(writer) ;
		logger.info("Wrote formatted JSON: File=" + file.getAbsolutePath());
	}

	/**
	 * Generates new secret hash key that is inserted in the given JSON node as
	 * cloud-formation output variable
	 */
	protected void generateSecretHashKey(String stackName, JsonNode combinedTemplateNode) {
		ObjectNode outputs = (ObjectNode) combinedTemplateNode.get("Outputs") ;
		if (outputs == null) {
			outputs = ((ObjectNode)combinedTemplateNode).putObject("Outputs") ;
		}
		
		ObjectNode secretHashKey = outputs.putObject("SecretHashKey") ; 
		secretHashKey.put("Description", "Secret hash key used to update/delete this stack through stacker") ;
		secretHashKey.put("Value", stackName + "-" + StringUtils.substringAfterLast(UUID.randomUUID().toString(), "-")) ;
	}
	
	/**
	 * Merges the source's JSON tree into the sink JSON tree. Json leaf nodes
	 * with conflicts are not merged.
	 * 
	 * @param sink the source JSON tree will be merges here
	 * @param source this JSON tree will be merged in the sing
	 * @return a list of fully qualified keys that were not merged because of
	 *         conflicts
	 */
	protected List<String> mergeJsonNodes(JsonNode sink, JsonNode source) {
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
	protected List<String> mergeJsonNodes(JsonNode sink, JsonNode source, Stack<String> stack) {
		
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
						((ObjectNode) sink).put(fieldName, sourceValue);
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
	protected String toJsonPath(Stack<String> stack, String currentKey) {
		StringBuilder pathBuilder = new StringBuilder() ;
		for (String key : stack)
			pathBuilder.append("/").append(key) ;
		pathBuilder.append("/").append(currentKey) ;
		return pathBuilder.toString() ;
	}
}
