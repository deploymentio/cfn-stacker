package com.deploymentio.cfnstacker.config;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;

public class SubStackConfig {
	
	private String name;
	private String path;
	private JsonNode template;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPath() {
		return path;
	}
	@JsonSetter("Path")
	public void setPath(String path) {
		this.path = path;
	}
	public JsonNode getTemplate() {
		return template;
	}
	public void setTemplate(JsonNode template) {
		this.template = template;
	}
}
