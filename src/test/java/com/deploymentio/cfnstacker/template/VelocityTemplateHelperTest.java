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
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
	public void testCreateContextWithBooleanValue() throws Exception {
		
		HashMap<String, JsonNode> map = new HashMap<>();
		map.put("key", BooleanNode.TRUE);
		
		VelocityContext context = helper.createContext(map, null);
		assertTrue(context.get("key") instanceof Boolean);
		assertTrue((Boolean)context.get("key"));
	}
	
	@Test
	public void testCreateContextWithNumberValue() throws Exception {
		
		HashMap<String, JsonNode> map = new HashMap<>();
		map.put("key", new IntNode(314));
		
		VelocityContext context = helper.createContext(map, null);
		assertTrue(context.get("key") instanceof Number);
		assertEquals(314, ((Number)context.get("key")).intValue());
	}
	
	@Test
	public void testCreateContextWithTextValue() throws Exception {
		
		HashMap<String, JsonNode> map = new HashMap<>();
		map.put("key", new TextNode("3.14"));
		
		VelocityContext context = helper.createContext(map, null);
		assertTrue(context.get("key") instanceof String);
		assertEquals("3.14", context.get("key"));
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

	@Test
	public void testCreateContextWithComplexValues() throws Exception {
		
		ObjectNode node = new ObjectMapper().createObjectNode().put("k1", "1");
		node.putArray("k2").add("1").add("2");
		
		HashMap<String, JsonNode> map = new HashMap<>();
		map.put("key", node);
		
		VelocityContext context = helper.createContext(map, null);
		assertTrue(context.get("key") instanceof Map);
		
		Map converted = (Map)context.get("key");
		assertEquals(2, converted.size());
		assertEquals("1", converted.get("k1").toString());

		assertTrue(converted.get("k2") instanceof List);
		List k2List = (List)converted.get("k2");
		assertEquals(2, k2List.size());
		assertEquals("1", k2List.get(0).toString());
		assertEquals("2", k2List.get(1).toString());
	}
}
