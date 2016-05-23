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

import java.io.File;
import java.io.FileReader;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.velocity.VelocityContext;

import com.deploymentio.cfnstacker.template.VelocityTemplateHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class StackConfigCreator {

	private ObjectMapper mapper = new ObjectMapper();
	private File baseDir;
	private StackConfig config;
	private VelocityTemplateHelper templateHelper;
	
	public StackConfigCreator(File file) throws Exception {
		baseDir = file.getParentFile();
		config = mapper.readValue(file, StackConfig.class);
		config.setConfigCreator(this);
		templateHelper = new VelocityTemplateHelper();
	}
	
	public StackConfig getStackConfig() throws Exception {
		return config;
	}

	public JsonParseResult loadStackTemplate(Fragment fragment, Map<String, JsonNode> baseParmeters) {
		String fragmentPath = fragment.getPath();
		File file = new File(baseDir, fragmentPath);
		if (!file.exists()) {
			String error = "File=" + fragmentPath + " Error=MissingTemplate FullPath=" + file.getAbsolutePath();
			return new JsonParseResult(error);
		} else {
			try(FileReader reader = new FileReader(file)) {
				String fileRawContent = IOUtils.toString(reader);
				
				VelocityContext context = templateHelper.createContext(baseParmeters, fragment.getParameters());
				String fileContent = templateHelper.evaluate(fragmentPath, fileRawContent, context);
				
				JsonNode node = mapper.readTree(fileContent);
				return new JsonParseResult(node); 
			} catch (Exception e) {
				return new JsonParseResult("File=" + fragmentPath + " Error=" + e.getMessage());
			}
		}
	}
}
