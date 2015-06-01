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

public class PhpAPIGenerator {
	private File dir;
	private boolean optional = false;

	private final String HEADER =
			"<?php\n" +
			"/**\n" +
			" * Zed Attack Proxy (ZAP) and its related class files.\n" +
			" *\n" +
			" * ZAP is an HTTP/HTTPS proxy for assessing web application security.\n" +
			" *\n" +
			" * Copyright 2015 the ZAP development team\n" +
			" *\n" +
			" * Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
			" * you may not use this file except in compliance with the License.\n" +
			" * You may obtain a copy of the License at\n" +
			" *\n" +
			" *   http://www.apache.org/licenses/LICENSE-2.0\n" +
			" *\n" +
			" * Unless required by applicable law or agreed to in writing, software\n" +
			" * distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
			" * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
			" * See the License for the specific language governing permissions and\n" +
			" * limitations under the License.\n" +
			" */\n" +
			"\n\n";

	private final String OPTIONAL_MASSAGE = "This component is optional and therefore the API will only work if it is installed"; 

	private ResourceBundle msgs = ResourceBundle.getBundle("lang." + Constant.MESSAGES_PREFIX, Locale.ENGLISH,
		ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_PROPERTIES));

	/**
	 * Map any names which are reserved in java to something legal
	 */
	private static final Map<String, String> nameMap;
    static {
        Map<String, String> initMap = new HashMap<>();
        initMap.put("Break", "Brk");
        initMap.put("break", "brk");
        nameMap = Collections.unmodifiableMap(initMap);
    }

    public PhpAPIGenerator() {
    	dir = new File("php/api/zapv2/src/Zap"); 
    }

    public PhpAPIGenerator(String path, boolean optional) {
    	dir = new File(path); 
    	this.optional = optional;
    }

	public void generatePhpFiles(List<ApiImplementor> implementors) throws IOException {
		for (ApiImplementor imp : implementors) {
			this.generatePhpComponent(imp);
		}
	}

	private void generatePhpElement(ApiElement element, String component, 
			String type, Writer out) throws IOException {

		boolean hasParams = (element.getMandatoryParamNames() != null && 
								element.getMandatoryParamNames().size() > 0) ||
							(element.getOptionalParamNames() != null &&
								element.getOptionalParamNames().size() > 0);
		boolean hasApiParam = (type.equals("action") || type.equals("other"));
		boolean returnFirstValue = !type.equals("other");

		out.write(generateCommentString(element, component, type));

		out.write(generateFunctionOpeningString(element, hasParams, hasApiParam));	

		out.write(generateFunctionBodyString(element, component, type, hasParams, hasApiParam, returnFirstValue));
	}

	private String generateCommentString(ApiElement element, String component, String type) {

		StringBuffer comment = new StringBuffer();

		// Add description if defined
		String descTag = element.getDescriptionTag();
		if (descTag == null) {
			// This is the default, but it can be overriden by the getDescriptionTag method if required
			descTag = component + ".api." + type + "." + element.getName();
		}

		try {
			String desc = msgs.getString(descTag);
			comment.append("\t/**\n");
			comment.append("\t * " + desc + "\n");
			if (optional) {
				comment.append("\t * " + OPTIONAL_MASSAGE + "\n");
			}
			comment.append("\t */\n");
		} catch (Exception e) {
			// Might not be set, so just print out the ones that are missing
			System.out.println("No i18n for: " + descTag);
			if (optional) {
				comment.append("\t/**\n");
				comment.append("\t * " + OPTIONAL_MASSAGE + "\n");
				comment.append("\t */\n");
			}
		}

		return comment.toString();
	}

	private String generateFunctionOpeningString(ApiElement element, Boolean hasParams, Boolean hasApiParam) {

		StringBuffer code = new StringBuffer();
		
		code.append("\tpublic function " + createMethodName(element.getName()) + "(");

		String paramMan = "";
		if (element.getMandatoryParamNames() != null) {
			for (String param : element.getMandatoryParamNames()) {
			    if (paramMan != "") {
			        paramMan += ", ";
			    }
				paramMan += "$" + param.toLowerCase();
			}
			code.append(paramMan);
		}
		String paramOpt = "";
		if (element.getOptionalParamNames() != null) {
			for (String param : element.getOptionalParamNames()) {
			    if (paramMan != "" || paramOpt != "") {
			        paramOpt += ", ";
			    }
				paramOpt += "$" + param.toLowerCase() + "=''";
			}
			code.append(paramOpt);
		}

		if (hasApiParam) {
		    if (hasParams) {
		        code.append(", ");
		    }
			// Always add the API key - we've no way of knowing if it will be required or not
			code.append("$" + API.API_KEY_PARAM + "=''");
		}

		code.append(") {\n");

		return code.toString();
	}
	
	private String generateFunctionBodyString(ApiElement element, String component, String type,
					Boolean hasParams, Boolean hasApiParam, Boolean returnFirstValue) {

		String reqString = generateRequestString(element, component, type, hasParams, hasApiParam);
		StringBuffer code = new StringBuffer();

		if (returnFirstValue) {
			code.append(String.format("\t\t$res = %s;\n", reqString));
			code.append("\t\treturn reset($res);\n");
		} else {
			code.append(String.format("\t\treturn %s;\n", reqString));
		}

		code.append("\t}\n\n");

		return code.toString();
	}

	private String generateRequestString(ApiElement element, String component,
										 String type, Boolean hasParams, Boolean hasApiParam) {

		StringBuffer code = new StringBuffer();

		String method = "request";
		String baseUrl = "base";
		if (type.equals("other")) {
			method += "other";
			baseUrl += "other";
		}

		code.append("$this->zap->" + method + "($this->zap->" + baseUrl + " . '" +
				component + "/" + type + "/" + element.getName() + "/'");

		if (hasParams || hasApiParam) {
			code.append(", array(");
			boolean first = true;
			if (element.getMandatoryParamNames() != null) {
				for (String param : element.getMandatoryParamNames()) {
					if (first) {
						first = false;
					} else {
						code.append(", ");
					}
					code.append("'" + param + "' => $" + param.toLowerCase());
				}
			}
			if (element.getOptionalParamNames() != null) {
				for (String param : element.getOptionalParamNames()) {
					if (first) {
						first = false;
					} else {
						code.append(", ");
					}
					code.append("'" + param + "' => $" + param.toLowerCase());
				}
			}
			if (hasApiParam) {
					// Always add the API key - we've no way of knowing if it will be required or not
					if (first) {
						first = false;
					} else {
						code.append(", ");
					}
					code.append("'" + API.API_KEY_PARAM + "' => $" + API.API_KEY_PARAM);
			}

			code.append(")");
		}

		code.append(")");

		return code.toString();
	}

	private static String createMethodName(String name) {
		if (nameMap.containsKey(name)) {
			name = nameMap.get(name);
		}
		return removeAllFullStopCharacters(name);
	}

	private static String removeAllFullStopCharacters(String string) {
		return string.replaceAll("\\.", "");
	}

	private void generatePhpComponent(ApiImplementor imp) throws IOException {
		String className = safeName(imp.getPrefix().substring(0, 1).toUpperCase() + imp.getPrefix().substring(1));

		File f = new File(this.dir, className + ".php");
		System.out.println("Generating " + f.getAbsolutePath());
		FileWriter out = new FileWriter(f);
		out.write(HEADER);
		out.write("namespace Zap;\n\n");

		out.write("\n");
		out.write("/**\n");
		out.write(" * This file was automatically generated.\n");
		out.write(" */\n");
		out.write("class " + className + " {\n\n");

		out.write("\tpublic function __construct ($zap) {\n");
		out.write("\t\t$this->zap = $zap;\n");
		out.write("\t}\n\n");

		for (ApiElement view : imp.getApiViews()) {
			this.generatePhpElement(view, imp.getPrefix(), "view", out);
		}
		for (ApiElement action : imp.getApiActions()) {
			this.generatePhpElement(action, imp.getPrefix(), "action", out);
		}
		for (ApiElement other : imp.getApiOthers()) {
			this.generatePhpElement(other, imp.getPrefix(), "other", out);
		}
		out.write("}\n");
		out.close();
	}

	private static String safeName (String name) {
		if (nameMap.containsKey(name)) {
			return nameMap.get(name);
		}
		return name;
	}

	public static void main(String[] args) throws Exception {
		PhpAPIGenerator wapi = new PhpAPIGenerator();
		wapi.generatePhpFiles(ApiGeneratorUtils.getAllImplementors());
	}

}
