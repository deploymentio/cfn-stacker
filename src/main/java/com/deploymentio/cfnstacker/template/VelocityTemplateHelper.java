package com.deploymentio.cfnstacker.template;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class VelocityTemplateHelper {

	protected Logger logger = Logger.getLogger("velocity");
	protected VelocityEngine ve = null;
	private VelocityContext context;
	
	public VelocityTemplateHelper() {
		
		ve = new VelocityEngine() ;
		ve.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.Log4JLogChute");
		ve.setProperty("runtime.log.logsystem.log4j.logger", "velocity");
		ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath"); 
		ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
		ve.init();
		
		context = new VelocityContext();
		context.put("Util", VelocityUtil.class);
		context.put("HASH_HASH", "##");
		context.put("DOLR", "$");
	}

	public VelocityContext createContext(Map<String, JsonNode> baseMap, Map<String, JsonNode> map) throws Exception {
		HashMap<String, JsonNode> values = new HashMap<>();
		if (baseMap != null) {
			values.putAll(baseMap);
		}
		if (map != null) {
			values.putAll(map);
		}
		
		// replace all JsonNodes with velocity friendly objects
		ObjectMapper mapper = new ObjectMapper();
		VelocityContext thisContext = new VelocityContext(context);
		for (Map.Entry<String, JsonNode> entry : values.entrySet()) {
			JsonNode value = entry.getValue();
			if (value.isContainerNode()) {
				if (value.isArray()) {
					thisContext.put(entry.getKey(), mapper.treeToValue(value, VelocityObjectList.class));
				} else {
					thisContext.put(entry.getKey(), mapper.treeToValue(value, VelocityObjectMap.class));
				}
			} else {
				thisContext.put(entry.getKey(), value.textValue());
			}
		}
		
		return thisContext;
	}
	
	public String evaluate (String inputId, String input, VelocityContext context) {
		StringWriter writer = new StringWriter();
		boolean evaluated = ve.evaluate(context, writer, inputId, input);
		return evaluated ? writer.toString() : null ;
	}
}
