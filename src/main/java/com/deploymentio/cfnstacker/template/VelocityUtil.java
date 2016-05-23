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
