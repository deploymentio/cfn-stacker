package com.deploymentio.cfnstacker.config;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseStackConfig {

	private String name;
	private List<Fragment> fragments = new ArrayList<>();
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Fragment> getFragments() {
		return fragments;
	}
	public void setFragments(List<Fragment> fragments) {
		this.fragments = fragments;
	}
}
