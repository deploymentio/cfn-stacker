package com.deploymentio.cfnstacker.template;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.deploymentio.cfnstacker.config.Fragment;
import com.deploymentio.cfnstacker.config.JsonParseResult;
import com.deploymentio.cfnstacker.config.StackConfig;
import com.deploymentio.cfnstacker.config.SubStackConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonNodeHelper {

	private final static Logger logger = LoggerFactory.getLogger(JsonNodeHelper.class);
	private StackConfig config;

	public JsonNodeHelper(StackConfig config) {
		this.config = config;
	}

	protected FragmentMergeResult getMergedFragments(List<Fragment> fragments, String errorMessage) throws Exception {

		boolean hasErrors = false;
		JsonNode combinedTemplateNode = null;
		JsonNodeMergeHelper mergeHelper = new JsonNodeMergeHelper();
		
		for (Fragment f : fragments) {
			
			JsonParseResult nodeParseResult = config.getConfigCreator().loadStackTemplate(f, config.getParameters());
			if (nodeParseResult.hasError()) {
				logger.error(errorMessage + " " + nodeParseResult.getError());
				hasErrors = true;
				continue;
			}
			
			JsonNode node = nodeParseResult.getNode();
			if (combinedTemplateNode == null) {
				combinedTemplateNode = node ;
			} else {
				List<String> errors = mergeHelper.merge(combinedTemplateNode, node) ;
				for (String err : errors) {
					logger.error("Duplicate keys while merging template fragments: File=" + f.getPath() + " Key=" + err) ;
					hasErrors = true ;
				}
			}
		}
		
		return new FragmentMergeResult(combinedTemplateNode, hasErrors);
	}
	
	
	/**
	 * Gets complete CloudFormation sub-stack template as a JSON string. The template is
	 * created by merging several JSON fragments stored in files.
	 * 
	 * @return merged JSON CloudFormation template
	 */
	public JsonNode getSubStackMergedJson(SubStackConfig subStackConfig) throws Exception {
		FragmentMergeResult result = getMergedFragments(subStackConfig.getFragments(), "Error loading sub-stack template fragment: SubStack=" + subStackConfig.getName());
		return result.errors ? null : result.combinedTemplateNode;
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
	public JsonNode getStackMergedJson(Map<String, String> subStackTemplateUrls) throws Exception {
		FragmentMergeResult result = getMergedFragments(config.getFragments(), "Error loading stack template fragment:");
		if (!result.errors) {
			ObjectNode parameters = (ObjectNode) result.combinedTemplateNode.get("Parameters");
			for (String key: subStackTemplateUrls.keySet()) {
				String value = subStackTemplateUrls.get(key);
				parameters.putObject(key + "TemplateURL")
				.put("Description", "S3 Template URL for " + key + " sub-stack")
				.put("Type", "String")
				.put("Default", value);
			}
			return result.combinedTemplateNode;
		} else {
			return null;
		}
	}
	
	static class FragmentMergeResult {
		JsonNode combinedTemplateNode;
		boolean errors;
		public FragmentMergeResult(JsonNode combinedTemplateNode, boolean errors) {
			this.combinedTemplateNode = combinedTemplateNode;
			this.errors = errors;
		}
	}
}
