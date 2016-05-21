package com.deploymentio.cfnstacker.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StackConfig extends BaseStackConfig {
	
	private StackConfigCreator configCreator;
	private String snsTopic;
	private String s3Prefix;
	private String s3Bucket;
	private Map<String, String> parameters = new HashMap<>();
	private List<SubStackConfig> subStacks = new ArrayList<>();
	private Map<String, String> tags = new HashMap<>();
	
	public String getS3Prefix() {
		return s3Prefix;
	}
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
	public void setSnsTopic(String snsTopic) {
		this.snsTopic = snsTopic;
	}
	public Map<String, String> getParameters() {
		return parameters;
	}
	public void setParameters(Map<String, String> paramaters) {
		this.parameters = paramaters;
	}
	public List<SubStackConfig> getSubStacks() {
		return subStacks;
	}
	public void setSubStacks(List<SubStackConfig> subStacks) {
		this.subStacks = subStacks;
	}
	public Map<String, String> getTags() {
		return tags;
	}
	public void setTags(Map<String, String> tags) {
		this.tags = tags;
	}
}
