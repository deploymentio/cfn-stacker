package com.deploymentio.cfnstacker.config;

import java.io.File;
import java.io.FileReader;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.velocity.VelocityContext;

import com.deploymentio.cfnstacker.template.VelocityTemplateHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonFileStackConfigCreator implements StackConfigCreator {

	private ObjectMapper mapper = new ObjectMapper();
	private File baseDir;
	private StackConfig config;
	private VelocityTemplateHelper templateHelper;
	
	public JsonFileStackConfigCreator(String path) throws Exception {
		File file = new File(path);
		baseDir = file.getParentFile();
		config = mapper.readValue(file, StackConfig.class);
		config.setConfigCreator(this);
		templateHelper = new VelocityTemplateHelper();
	}
	
	@Override
	public StackConfig getStackConfig() throws Exception {
		return config;
	}

	@Override
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
