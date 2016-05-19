package com.deploymentio.cfnstacker.template;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;

public class VelocityUtil {

	public static String valueOrDefault(Object val, String defaultVal) {
		if (val == null || ((val instanceof String) && StringUtils.isEmpty((String)val))) {
			return defaultVal;
		}
		return val.toString();
	}

	public static String join(Collection<String> list, String delimiter) {
		return StringUtils.join(list, delimiter);
	}
	
	public static String joinLinesWithSingleQuote(String content) {
		String[] lines = StringUtils.split(content, "\r\n");
		for (int i = 0; i < lines.length; i++) {
			lines[i] = "'" + lines[i].replace("\\\"", "\\\\\"") + "\\n'";
		}
		return StringUtils.join(lines, " +\n");
	}
}
