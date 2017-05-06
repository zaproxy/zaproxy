package org.zaproxy.zap.extension.api;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;
import org.parosproxy.paros.Constant;

public class RubyAPIGenerator {

	private File dir;
	private boolean optional = false;

	private final String HEADER = "# Zed Attack Proxy (ZAP) and its related class files.\n" + "#\n"
			+ "# ZAP is an HTTP/HTTPS proxy for assessing web application security.\n" + "#\n"
			+ "# Copyright 2015 the ZAP development team\n" + "#\n"
			+ "# Licensed under the Apache License, Version 2.0 (the \"License\");\n"
			+ "# you may not use this file except in compliance with the License.\n"
			+ "# You may obtain a copy of the License at\n" + "#\n" + "#   http://www.apache.org/licenses/LICENSE-2.0\n"
			+ "#\n" + "# Unless required by applicable law or agreed to in writing, software\n"
			+ "# distributed under the License is distributed on an \"AS IS\" BASIS,\n"
			+ "# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n"
			+ "# See the License for the specific language governing permissions and\n"
			+ "# limitations under the License.\n\n";

	private final String OPTIONAL_MASSAGE = "This component is optional and therefore the API will only work if it is installed";

	private ResourceBundle msgs = ResourceBundle.getBundle("lang." + Constant.MESSAGES_PREFIX, Locale.ENGLISH,
			ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_PROPERTIES));

	/**
	 * Map any names which are reserved in Go to something legal
	 */
	private static final Map<String, String> nameMap;

	static {
		Map<String, String> initMap = new HashMap<>();
		initMap.put("break", "brk");
		nameMap = Collections.unmodifiableMap(initMap);
	}

	public RubyAPIGenerator() {
		dir = new File("../zap-api-ruby/lib/zap/");
	}

	public RubyAPIGenerator(String path, boolean optional) {
		dir = new File(path);
		this.optional = optional;
	}

	public void generateRubyFiles(List<ApiImplementor> implementors) throws IOException {
		for (ApiImplementor imp : implementors) {
			this.generateRubyComponent(imp);
		}
	}

	private void generateRubyComponent(ApiImplementor imp) throws IOException {
		File f = new File(this.dir, createFileName(imp.getPrefix()));
		System.out.println("Generating " + f.getAbsolutePath());
		try (FileWriter out = new FileWriter(f)) {
			out.write(HEADER);
			out.write("# *** This file was automatically generated. ***\n\n");
			out.write("module ZAP\n");
			out.write("  class " + toTitleCase(safeName(imp.getPrefix())) + "\n\n");
			out.write("    # @param zap [ZAP::Client]\n");
			out.write("    def initialize(zap)\n");
			out.write("      @zap = zap\n");
			out.write("    end\n");
			out.write("\n");

			for (ApiElement view : imp.getApiViews()) {
				this.generateRubyElement(view, imp.getPrefix(), "view", out);
			}
			for (ApiElement action : imp.getApiActions()) {
				this.generateRubyElement(action, imp.getPrefix(), "action", out);
			}
			for (ApiElement other : imp.getApiOthers()) {
				this.generateRubyElement(other, imp.getPrefix(), "other", out);
			}
			out.write("  end\n");
			out.write("end");
			out.write("\n");
		}
	}

	private void generateRubyElement(ApiElement element, String component, String type, Writer out) throws IOException {

		boolean typeOther = type.equals("other");
		boolean hasParams = (element.getMandatoryParamNames() != null && element.getMandatoryParamNames().size() > 0)
				|| (element.getOptionalParamNames() != null && element.getOptionalParamNames().size() > 0);

		// Add description if defined
		String descTag = element.getDescriptionTag();
		if (descTag == null) {
			// This is the default, but it can be overridden by the getDescriptionTag method if required
			descTag = component + ".api." + type + "." + element.getName();
		}
		try {
			String desc = msgs.getString(descTag);
			out.write("    # " + desc + "\n");
			if (optional) {
				out.write("    #\n");
				out.write("    # " + OPTIONAL_MASSAGE + "\n");
			}
		} catch (Exception e) {
			// Might not be set, so just print out the ones that are missing
			System.out.println("No i18n for: " + descTag);
			if (optional) {
				out.write("    # " + OPTIONAL_MASSAGE + "\n");
			}
		}

		// Method declaration
		out.write("    def " + createMethodName(element.getName()));
		out.write("(");
		
		// Iterate through and write out method arguments
		ArrayList<String> args = new ArrayList<String>();
		if (element.getMandatoryParamNames() != null) {
			for (String param : element.getMandatoryParamNames()) {
				args.add(param.toLowerCase());
			}
		}
		if (element.getOptionalParamNames() != null) {
			for (String param : element.getOptionalParamNames()) {
				args.add(param.toLowerCase() + " = ''");
			}
		}
		out.write(StringUtils.join(args, ", "));
		out.write(")\n");

		String method = "request";
		if (typeOther) { method += "_other"; }

		// Return method
		out.write("      @zap." + method + "('" + component + "/" + type + "/" + element.getName() + "/'");

		// Request parameters
		if (hasParams) {
			out.write(", {");
			ArrayList<String> paramArgs = new ArrayList<String>();
			
			// Iterate through and write out mandatory request parameters
			writeOutRequestParams(element.getMandatoryParamNames(), paramArgs, out);

			// Iterate through and write out optional request parameters
			writeOutRequestParams(element.getOptionalParamNames(), paramArgs, out);
			
			out.write(StringUtils.join(paramArgs, ", "));
			out.write("}");
		}

		out.write(")");
		out.write("\n");
		out.write("    end\n");
		out.write("\n");
	}

	// Writes the request parameters
	private void writeOutRequestParams(List<String> elements, ArrayList<String> args, Writer out) throws IOException {
		if (elements != null && elements.size() > 0) {
			for (String param : elements) {
				args.add("'" + param + "' => " + param.toLowerCase());
			}
		}
	}

	private static String safeName(String name) {
		if (nameMap.containsKey(name)) {
			return nameMap.get(name);
		}
		return name;
	}

	private static String createFileName(String name) {
		return camelCaseToLcUnderscores(safeName(name)) + ".rb";
	}

	private static String createMethodName(String name) {
		return removeAllFullStopCharacters(camelCaseToLcUnderscores(safeName(name)));
	}

	private static String removeAllFullStopCharacters(String string) {
		return string.replaceAll("\\.", "");
	}

	public static String camelCaseToLcUnderscores(String s) {
		// Ripped off / inspired by
		// http://stackoverflow.com/questions/2559759/how-do-i-convert-camelcase-into-human-readable-names-in-java
		return safeName(s).replaceAll(
				String.format("%s|%s|%s", 
							  "(?<=[A-Z])(?=[A-Z][a-z])", 
							  "(?<=[^A-Z])(?=[A-Z])",
							  "(?<=[A-Za-z])(?=[^A-Za-z])"), 
							  "_").toLowerCase();
	}

	public static String toTitleCase(String input) {
		StringBuilder titleCase = new StringBuilder();
		boolean nextTitleCase = true;

		for (char c : input.toCharArray()) {
			if (Character.isSpaceChar(c)) {
				nextTitleCase = true;
			} else if (nextTitleCase) {
				c = Character.toTitleCase(c);
				nextTitleCase = false;
			}
			titleCase.append(c);
		}
		return titleCase.toString();
	}

	public static void main(String[] args) throws Exception {
		RubyAPIGenerator wapi = new RubyAPIGenerator();
		wapi.generateRubyFiles(ApiGeneratorUtils.getAllImplementors());
	}
}
