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

package com.deploymentio.cfnstacker.template;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.cloudformation.model.Parameter;
import com.deploymentio.cfnstacker.config.StackConfig;
import com.fasterxml.jackson.databind.JsonNode;

public class TemplateParameters {

	private StackConfig config;

	public TemplateParameters(StackConfig config) {
		this.config = config;
	}

	public List<Parameter> getApplicableParameters(JsonNode templateBodyJson) throws Exception {
		
		JsonNode parametersNode = templateBodyJson.get("Parameters");
		
		List<Parameter> params = new ArrayList<>();
		for (String key: config.getParameters().keySet()) {
			if (parametersNode != null && parametersNode.has(key)) {
				params.add(new Parameter()
					.withParameterKey(key)
					.withParameterValue(config.getParameters().get(key).textValue()));
			}
		}
		return params;
	}
	
}
