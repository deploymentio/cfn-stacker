package com.deploymentio.cfnstacker.template;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.cloudformation.model.Parameter;
import com.deploymentio.cfnstacker.config.StackConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TemplateParameters {

	private StackConfig config;

	public TemplateParameters(StackConfig config) {
		this.config = config;
	}

	public List<Parameter> getApplicableParameters(JsonNode templateBodyJson) throws Exception {
		
		ObjectMapper mapper = new ObjectMapper();
		JsonNode parametersNode = templateBodyJson.get("Parameters");
		
		List<Parameter> params = new ArrayList<>();
		for (String key: config.getParameters().keySet()) {
			if (parametersNode != null && parametersNode.has(key)) {
				params.add(new Parameter()
					.withParameterKey(key)
					.withParameterValue(config.getParameters().get(key)));
			}
		}
		return params;
	}
	
}
