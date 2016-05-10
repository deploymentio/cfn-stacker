package com.deploymentio.cfnstacker.templatelang;

import java.util.HashMap;
import java.util.Map;

public class Context {

	private Context parent;
	private Map<String, Object> map = new HashMap<>();
	
	public Context(Context parent) {
		this.parent = parent;
	}
	
	public Context(Map<String, String> map) {
		this.map.putAll(map);
	}
	
	
	public Object get(String key) {
		if (map.containsKey(key)) {
			return map.get(key);
		}
		
		if (parent != null) {
			return parent.get(key);
		}
		
		return null;
	}
}
