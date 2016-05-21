package com.deploymentio.cfnstacker.template;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;

public class VelocityTemplateHelperTest {

	private VelocityTemplateHelper helper;

	@Before
	public void setup() {
		helper = new VelocityTemplateHelper();
	}
	
	@Test
	public void testCreateContextNullMaps() throws Exception {
		VelocityContext context = helper.createContext(null, null);
		assertNotNull(context);
	}
	
	@Test
	public void testCreateContextMergedMapValues() throws Exception {
		
		HashMap<String, JsonNode> map1 = new HashMap<>();
		map1.put("k1", new TextNode("v1"));
		
		HashMap<String, JsonNode> map2 = new HashMap<>();
		map1.put("k2", new TextNode("v2"));

		VelocityContext context = helper.createContext(map1, map2);
		assertTrue(context.containsKey("k1"));
		assertTrue(context.containsKey("k2"));
	}

	@Test
	public void testCreateContextMergedMapValuesOverriden() throws Exception {
		
		HashMap<String, JsonNode> map1 = new HashMap<>();
		map1.put("k1", new TextNode("v1"));
		
		HashMap<String, JsonNode> map2 = new HashMap<>();
		map1.put("k1", new TextNode("v2"));

		VelocityContext context = helper.createContext(map1, map2);
		assertEquals("v2", context.get("k1"));
	}

	@Test
	public void testCreateContextWithListValue() throws Exception {
		
		HashMap<String, JsonNode> map = new HashMap<>();
		map.put("key", new ObjectMapper().createArrayNode().add("1").add("2"));
		
		VelocityContext context = helper.createContext(map, null);
		assertTrue(context.get("key") instanceof List);
		
		List converted = (List)context.get("key");
		assertEquals(2, converted.size());
		assertEquals("1", converted.get(0).toString());
		assertEquals("2", converted.get(1).toString());
	}
	
	@Test
	public void testCreateContextWithMapValue() throws Exception {
		
		HashMap<String, JsonNode> map = new HashMap<>();
		map.put("key", new ObjectMapper().createObjectNode().put("k1", "1").put("k2", "2"));
		
		VelocityContext context = helper.createContext(map, null);
		assertTrue(context.get("key") instanceof Map);
		
		Map converted = (Map)context.get("key");
		assertEquals(2, converted.size());
		assertEquals("1", converted.get("k1").toString());
		assertEquals("2", converted.get("k2").toString());
	}
}
