package com.deploymentio.cfnstacker.config;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonSetter;

public abstract class BaseStackConfig {

	private String name;
	private List<String> templates = new ArrayList<>();
	
	public String getName() {
		return name;
	}
	@JsonSetter("Name")
	public void setName(String name) {
		this.name = name;
	}
	public List<String> getTemplates() {
		return templates;
	}
	public void setTemplates(List<String> templates) {
		this.templates = templates;
	}
}
