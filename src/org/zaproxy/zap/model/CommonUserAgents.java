/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2015 The Zed Attack Proxy Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.zaproxy.zap.model;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;

public class CommonUserAgents {

	private static Map<String, String> nameToString = null;
	private static Map<String, String> stringToName = null;

    private static final Logger logger = Logger.getLogger(CommonUserAgents.class);

	static {

		nameToString = new HashMap<String, String>();
		stringToName = new HashMap<String, String>();

		String fileName = Constant.getZapInstall() + File.separator + "xml" + File.separator + "common-user-agents.txt"; 
		File f = new File(fileName);
		if (f.exists()) {
			try {
				for (String line : Files.readAllLines(f.toPath(), Charset.forName("US-ASCII"))) {
					if (line.trim().length() == 0 || line.startsWith("#")) {
						// Skip blank lines or ones that start with a #
						continue;
					}
					String[] array = line.split("\t");
					if (array.length != 3) {
						logger.error("Unexpected line in " + f.getAbsolutePath() + " : " + line);
					} else {
						nameToString.put(array[2], array[1]);
						stringToName.put(array[1], array[2]);
					}
				}
				
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		} else {
			logger.error("Failed to read common user agents from " + f.getAbsolutePath());
		}
	}
	
	public static String getStringFromName(String name) {
		return nameToString.get(name);
	}
	
	public static String getNameFromString(String str) {
		return stringToName.get(str);
	}
	
	public static String[] getNames() {
		Set<String> keys = nameToString.keySet();
		String[] names = new String[keys.size()];
		int i=0;
		for (String key : keys) {
			names[i] = key;
			i++;
		}
		Arrays.sort(names);
		return names;
	}
}
