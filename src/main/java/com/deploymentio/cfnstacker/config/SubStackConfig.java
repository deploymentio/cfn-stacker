package com.deploymentio.cfnstacker.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonSetter;

public class SubStackConfig {
	
	private String name;
	private Map<String, String> parameters = new HashMap<>();
	private List<String> fragments = new ArrayList<>();
	
	public String getName() {
		return name;
	}
	@JsonSetter("Name")
	public void setName(String name) {
		this.name = name;
	}
	public Map<String, String> getParameters() {
		return parameters;
	}
	@JsonSetter("Parameters")
	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}
	public List<String> getFragments() {
		return fragments;
	}
	@JsonSetter("Fragments")
	public void setFragments(List<String> fragments) {
		this.fragments = fragments;
	}
}
