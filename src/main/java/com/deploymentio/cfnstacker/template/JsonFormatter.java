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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonFormatter {
	
	private final static Logger logger = LoggerFactory.getLogger(JsonFormatter.class);
	private ObjectMapper mapper;
	
	public JsonFormatter() {
		mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
	}
	
	/**
	 * Write formatted version of the given JSON string to the given file.
	 */
	public void writeFormattedJSONString(JsonNode node, File file) throws IOException, JsonGenerationException, JsonMappingException {
		
		FileWriter writer = new FileWriter(file) ;
		mapper.writeValue(writer, node);
		
		IOUtils.closeQuietly(writer) ;
		logger.debug("Wrote formatted JSON: File=" + file.getAbsolutePath());
	}
}
