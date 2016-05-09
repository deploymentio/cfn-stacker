package com.deploymentio.cfnstacker.config;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;

public class StackConfig {
	
	private String name;
	private StackConfigCreator configCreator;
	private String snsTopic;
	private String s3Prefix;
	private String s3Bucket;
	private Map<String, String> parameters = new HashMap<>();
	private Map<String, SubStackConfig> subStacks = new HashMap<>();
	private Map<String, String> tags = new HashMap<>();
	private JsonNode template;
	
	public String getName() {
		return name;
	}
	@JsonSetter("Name")
	public void setName(String name) {
		this.name = name;
	}
	public JsonNode getTemplate() {
		return template;
	}
	public void setTemplate(JsonNode template) {
		this.template = template;
	}
	public String getS3Prefix() {
		return s3Prefix;
	}
	@JsonSetter("S3Prefix")
	public void setS3Prefix(String s3Prefix) {
		if (s3Prefix.startsWith("/")) {
			s3Prefix = s3Prefix.substring(1);
		}
		if (!s3Prefix.endsWith("/")) {
			s3Prefix = s3Prefix + "/";
		}
		this.s3Prefix = s3Prefix;
	}
	public String getS3Bucket() {
		return s3Bucket;
	}
	@JsonSetter("S3Bucket")
	public void setS3Bucket(String s3Bucket) {
		this.s3Bucket = s3Bucket;
	}
	public StackConfigCreator getConfigCreator() {
		return configCreator;
	}
	public void setConfigCreator(StackConfigCreator configCreator) {
		this.configCreator = configCreator;
	}
	public String getSnsTopic() {
		return snsTopic;
	}
	@JsonSetter("SnsTopic")
	public void setSnsTopic(String snsTopic) {
		this.snsTopic = snsTopic;
	}
	public Map<String, String> getParameters() {
		return parameters;
	}
	@JsonSetter("Parameters")
	public void setParameters(Map<String, String> paramaters) {
		this.parameters = paramaters;
	}
	public Map<String, SubStackConfig> getSubStacks() {
		return subStacks;
	}
	@JsonSetter("SubStacks")
	public void setSubStacks(Map<String, SubStackConfig> subStacks) {
		this.subStacks = subStacks;
	}
	public Map<String, String> getTags() {
		return tags;
	}
	@JsonSetter("Tags")
	public void setTags(Map<String, String> tags) {
		this.tags = tags;
	}
}
