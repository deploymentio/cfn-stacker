package com.deploymentio.cfnstacker.config;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseStackConfig {

	private String name;
	private List<String> fragements = new ArrayList<>();
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<String> getFragements() {
		return fragements;
	}
	public void setFragements(List<String> fragements) {
		this.fragements = fragements;
	}
}
