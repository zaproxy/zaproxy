/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
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
package org.zaproxy.zap.extension.api;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.parosproxy.paros.Constant;

public class PythonAPIGenerator {
	private File dir; 
	private boolean optional = false;
	
	private final String HEADER = 
			"# Zed Attack Proxy (ZAP) and its related class files.\n" +
			"#\n" +
			"# ZAP is an HTTP/HTTPS proxy for assessing web application security.\n" +
			"#\n" +
			"# Copyright 2014 the ZAP development team\n" +
			"#\n" +
			"# Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
			"# you may not use this file except in compliance with the License.\n" +
			"# You may obtain a copy of the License at\n" +
			"#\n" +
			"#   http://www.apache.org/licenses/LICENSE-2.0\n" +
			"#\n" +
			"# Unless required by applicable law or agreed to in writing, software\n" +
			"# distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
			"# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
			"# See the License for the specific language governing permissions and\n" +
			"# limitations under the License.\n" +
			"\"\"\"\n" +
			"This file was automatically generated.\n" +
			"\"\"\"\n\n";

	private final String OPTIONAL_MASSAGE = "This component is optional and therefore the API will only work if it is installed"; 

	private ResourceBundle msgs = ResourceBundle.getBundle("lang." + Constant.MESSAGES_PREFIX, Locale.ENGLISH,
		ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_PROPERTIES));
	
	/**
	 * Map any names which are reserved in python to something legal
	 */
	private static final Map<String, String> nameMap;
    static {
        Map<String, String> initMap = new HashMap<>();
        initMap.put("break", "brk");
        nameMap = Collections.unmodifiableMap(initMap);
    }

    public PythonAPIGenerator() {
    	dir = new File("python/api/src/zapv2"); 
    }

    public PythonAPIGenerator(String path, boolean optional) {
    	dir = new File(path); 
    	this.optional = optional;
    }

	public void generatePythonFiles(List<ApiImplementor> implementors) throws IOException {
		for (ApiImplementor imp : implementors) {
			this.generatePythonComponent(imp);
		}
	}
	
	private void generatePythonElement(ApiElement element, String component, 
			String type, Writer out) throws IOException {
		
		boolean hasParams = (element.getMandatoryParamNames() != null && 
								element.getMandatoryParamNames().size() > 0) ||
							(element.getOptionalParamNames() != null &&
								element.getOptionalParamNames().size() > 0);
				
		if (!(hasParams || type.equals("action"))) {
			out.write("    @property\n");
		}
		out.write("    def " + createFunctionName(element.getName()) + "(self");
		
		if (element.getMandatoryParamNames() != null) {
			for (String param : element.getMandatoryParamNames()) {
				out.write(", " + param.toLowerCase());
			}
		}
		if (element.getOptionalParamNames() != null) {
			for (String param : element.getOptionalParamNames()) {
				out.write(", " + param.toLowerCase() + "=''");
			}
		}

		if (type.equals("action") || type.equals("other")) {
			// Always add the API key - we've no way of knowing if it will be required or not
			out.write(", " + API.API_KEY_PARAM + "=''");
			hasParams = true;
		}

		out.write("):\n");

		// Add description if defined
		String descTag = element.getDescriptionTag();
		if (descTag == null) {
			// This is the default, but it can be overriden by the getDescriptionTag method if required
			descTag = component + ".api." + type + "." + element.getName();
		}
		try {
			String desc = msgs.getString(descTag);
			out.write("        \"\"\"\n");
			out.write("        " + desc + "\n");
			if (optional) {
				out.write("        " + OPTIONAL_MASSAGE + "\n");
			}
			out.write("        \"\"\"\n");
		} catch (Exception e) {
			// Might not be set, so just print out the ones that are missing
			System.out.println("No i18n for: " + descTag);
			if (optional) {
				out.write("        \"\"\"\n");
				out.write("        " + OPTIONAL_MASSAGE + "\n");
				out.write("        \"\"\"\n");
			}
		}

		String method = "_request";
		String baseUrl = "base";
		if (type.equals("other")) {
			method += "_other";
			baseUrl += "_other";
		}
		
		out.write("        return self.zap." + method + "(self.zap." + baseUrl + " + '" + 
				component + "/" + type + "/" + element.getName() + "/'");
		
		// , {'url': url}))
		if (hasParams) {
			out.write(", {");
			boolean first = true;
			if (element.getMandatoryParamNames() != null) {
				for (String param : element.getMandatoryParamNames()) {
					if (first) {
						first = false;
					} else {
						out.write(", ");
					}
					out.write("'" + param + "' : " + param.toLowerCase());
				}
			}
			if (element.getOptionalParamNames() != null) {
				for (String param : element.getOptionalParamNames()) {
					if (first) {
						first = false;
					} else {
						out.write(", ");
					}
					out.write("'" + param + "' : " + param.toLowerCase());
				}
			}
			if (type.equals("action") || type.equals("other")) {
				// Always add the API key - we've no way of knowing if it will be required or not
				if (first) {
					first = false;
				} else {
					out.write(", ");
				}
				out.write("'" + API.API_KEY_PARAM + "' : " + API.API_KEY_PARAM);
			}

			out.write("})");
			if (type.equals("view")) {
				out.write(".get('" + element.getName() + "')");
			}
		} else if (!type.equals("other")) {
			if (element.getName().startsWith("option")) {
				out.write(").get('" + element.getName().substring(6) + "')");
			} else {
				out.write(").get('" + element.getName() + "')");
			}
		} else {
			out.write(")");
		}
		out.write("\n\n");
		
	}

	private void generatePythonComponent(ApiImplementor imp) throws IOException {
		File f = new File(this.dir, createFileName(imp.getPrefix()));
		System.out.println("Generating " + f.getAbsolutePath());
		try(FileWriter out = new FileWriter(f)) {
			out.write(HEADER);
			out.write("class " + safeName(imp.getPrefix()) + "(object):\n\n");
			out.write("    def __init__(self, zap):\n");
			out.write("        self.zap = zap\n");
			out.write("\n");
			
			for (ApiElement view : imp.getApiViews()) {
				this.generatePythonElement(view, imp.getPrefix(), "view", out);
			}
			for (ApiElement action : imp.getApiActions()) {
				this.generatePythonElement(action, imp.getPrefix(), "action", out);
			}
			for (ApiElement other : imp.getApiOthers()) {
				this.generatePythonElement(other, imp.getPrefix(), "other", out);
			}
			out.write("\n");
		}
	}
	
	private static String safeName (String name) {
		if (nameMap.containsKey(name)) {
			return nameMap.get(name);
		}
		return name;
	}
	
	private static String createFileName(String name) {
		return safeName(name) + ".py";
	}
	
	private static String createFunctionName(String name) {
		return removeAllFullStopCharacters(camelCaseToLcUnderscores(safeName(name)));
	}

	private static String removeAllFullStopCharacters(String string) {
		return string.replaceAll("\\.", "");
	}

	public static String camelCaseToLcUnderscores(String s) {
		// Ripped off / inspired by http://stackoverflow.com/questions/2559759/how-do-i-convert-camelcase-into-human-readable-names-in-java
		return safeName(s).replaceAll(
			      String.format("%s|%s|%s",
			         "(?<=[A-Z])(?=[A-Z][a-z])",
			         "(?<=[^A-Z])(?=[A-Z])",
			         "(?<=[A-Za-z])(?=[^A-Za-z])"),
			      "_").toLowerCase();
	}

	public static void main(String[] args) throws Exception {
		// Command for generating a python version of the ZAP API
		
		PythonAPIGenerator wapi = new PythonAPIGenerator();
		wapi.generatePythonFiles(ApiGeneratorUtils.getAllImplementors());
		
		//System.out.println(camelCaseToLcUnderscores("TestCase"));
		
	}

}
