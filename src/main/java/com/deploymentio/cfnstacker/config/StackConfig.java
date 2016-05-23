/*
 * Copyright 2016 - Deployment IO
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.deploymentio.cfnstacker.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

public class StackConfig extends BaseStackConfig {
	
	private StackConfigCreator configCreator;
	private String snsTopic;
	private String s3Prefix;
	private String s3Bucket;
	private Map<String, JsonNode> parameters = new HashMap<>();
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
	public Map<String, JsonNode> getParameters() {
		return parameters;
	}
	public void setParameters(Map<String, JsonNode> paramaters) {
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
