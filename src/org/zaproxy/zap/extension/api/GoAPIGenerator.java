package org.zaproxy.zap.extension.api;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;
import org.parosproxy.paros.Constant;

public class GoAPIGenerator {

	private File dir;
	private boolean optional = false;
	private boolean addImports = false;

	private final String HEADER = "// Zed Attack Proxy (ZAP) and its related class files.\n" + "//\n"
			+ "// ZAP is an HTTP/HTTPS proxy for assessing web application security.\n" + "//\n"
			+ "// Copyright 2015 the ZAP development team\n" + "//\n"
			+ "// Licensed under the Apache License, Version 2.0 (the \"License\");\n"
			+ "// you may not use this file except in compliance with the License.\n"
			+ "// You may obtain a copy of the License at\n" + "//\n"
			+ "//   http://www.apache.org/licenses/LICENSE-2.0\n" + "//\n"
			+ "// Unless required by applicable law or agreed to in writing, software\n"
			+ "// distributed under the License is distributed on an \"AS IS\" BASIS,\n"
			+ "// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n"
			+ "// See the License for the specific language governing permissions and\n"
			+ "// limitations under the License.\n";

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

	public GoAPIGenerator() {
		dir = new File("../zap-api-go/zap/");
	}

	public GoAPIGenerator(String path, boolean optional) {
		dir = new File(path);
		this.optional = optional;
	}

	public void generateGoFiles(List<ApiImplementor> implementors) throws IOException {
		for (ApiImplementor imp : implementors) {
			this.generateGoComponent(imp);
		}
	}

	private void generateGoComponent(ApiImplementor imp) throws IOException {
		String className = imp.getPrefix().substring(0, 1).toUpperCase() + imp.getPrefix().substring(1);
		String pkgName = safeName(camelCaseToLowerCaseDash(className));

		File f = new File(dir, pkgName + ".go");
		System.out.println("Generating " + f.getAbsolutePath());
		FileWriter out = new FileWriter(f);

		out.write(HEADER);
		out.write("//\n");
		out.write("// *** This file was automatically generated. ***\n");
		out.write("//\n\n");
		out.write("package zap\n\n");
		out.write("_imports_");
		out.write("type " + className + " struct {}" + "\n\n");

		for (ApiElement view : imp.getApiViews()) {
			this.generateGoElement(view, className, imp.getPrefix(), "view", out);
		}
		for (ApiElement action : imp.getApiActions()) {
			this.generateGoElement(action, className, imp.getPrefix(), "action", out);
		}
		for (ApiElement other : imp.getApiOthers()) {
			this.generateGoElement(other, className, imp.getPrefix(), "other", out);
		}

		out.close();
		addImports(f, addImports);
		addImports = false;
	}

	private void generateGoElement(ApiElement element, String className, String component, String type, Writer out)
			throws IOException {

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
			out.write("// " + desc + "\n");
			if (optional) {
				out.write("//\n");
				out.write("// " + OPTIONAL_MASSAGE + "\n");
			}
		} catch (Exception e) {
			// Might not be set, so just print out the ones that are missing
			System.out.println("No i18n for: " + descTag);
			if (optional) {
				out.write("// " + OPTIONAL_MASSAGE + "\n");
			}
		}

		// Function declaration
		out.write("func (" + className.substring(0, 1).toLowerCase() + " " + className + ") ");
		out.write(toTitleCase(createMethodName(element.getName())));
		out.write("(");

		// Iterate through and write out mandatory function arguments
		writeOutArgs(element.getMandatoryParamNames(), out, hasParams);

		if (element.getMandatoryParamNames() != null && 
			element.getMandatoryParamNames().size() > 0 &&
			element.getOptionalParamNames() != null && 
			element.getOptionalParamNames().size() > 0) {
			out.write(", ");
		}

		// Iterate through and write out optional function arguments
		writeOutArgs(element.getOptionalParamNames(), out, hasParams);

		out.write(")");

		// Function return types
		if (typeOther) {
			out.write(" ([]byte, error) ");
		} else {
			out.write(" (interface{}, error) ");
		}
		out.write("{\n");

		// Function content
		if (hasParams) {
			out.write("\tm := map[string]string{");

			// Iterate through and write out mandatory request parameters
			writeOutRequestParams(element.getMandatoryParamNames(), out);

			// Iterate through and write out optional request parameters
			writeOutRequestParams(element.getOptionalParamNames(), out);

			out.write("\n\t}\n");
		}

		String method = "Request";
		if (typeOther) {
			method += "Other";
		}
		out.write("\treturn " + method + "(\"" + component + "/" + type + "/" + element.getName() + "/\"");

		if (hasParams) {
			out.write(", m");
		}
		out.write(")\n");
		out.write("}\n\n");
	}

	// Writes the function arguments
	private void writeOutArgs(List<String> elements, Writer out, boolean hasParams) throws IOException {
		if (elements != null && elements.size() > 0) {
			ArrayList<String> args = new ArrayList<String>();
			for (String param : elements) {
				if (param.toLowerCase().equals("boolean")) {
					args.add("boolean bool");
				} else if (param.toLowerCase().equals("integer")) {
					args.add("i int");
				} else if (param.toLowerCase().equals("string")) {
					args.add("str string");
				} else if (param.toLowerCase().equals("type")) {
					args.add("t string");
				} else {
					args.add(param.toLowerCase() + " string");
				}
			}
			out.write(StringUtils.join(args, ", "));
		}
	}

	// Writes the request parameters
	private void writeOutRequestParams(List<String> elements, Writer out) throws IOException {
		if (elements != null && elements.size() > 0) {
			for (String param : elements) {
				out.write("\n\t\t\"" + param + "\": ");
				if (param.toLowerCase().equals("boolean")) {
					addImports = true;
					out.write("strconv.FormatBool(boolean)");
				} else if (param.toLowerCase().equals("integer")) {
					addImports = true;
					out.write("strconv.Itoa(i)");
				} else if (param.toLowerCase().equals("string")) {
					out.write("str");
				} else if (param.toLowerCase().equals("type")) {
					out.write("t");
				} else {
					out.write(param.toLowerCase());
				}
				out.write(",");
			}
		}
	}

	private static String safeName(String name) {
		if (nameMap.containsKey(name)) {
			return nameMap.get(name);
		}
		return name;
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

	public static String camelCaseToLowerCaseDash(String s) {
		// Ripped off / inspired by
		// http://stackoverflow.com/questions/2559759/how-do-i-convert-camelcase-into-human-readable-names-in-java
		return safeName(s).replaceAll(
				String.format("%s|%s|%s", 
							  "(?<=[A-Z])(?=[A-Z][a-z])", 
							  "(?<=[^A-Z])(?=[A-Z])",
							  "(?<=[A-Za-z])(?=[^A-Za-z])"), 
							  "-").toLowerCase();
	}

	private static String toTitleCase(String input) {
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

	// It replaces the placeholder _imports_ with the proper import packages,
	// or removes it in case there isn't any packages to import
	private static void addImports(File file, boolean addImports) throws IOException {
		Path path = Paths.get(file.getPath());
		Charset charset = StandardCharsets.UTF_8;
		String content = new String(Files.readAllBytes(path), charset);
		if (addImports) {
			content = content.replaceAll("_imports_", "import \"strconv\"\n\n");
		} else {
			content = content.replaceAll("_imports_", "");
		}
		Files.write(path, content.getBytes(charset));
	}

	public static void main(String[] args) throws Exception {
		// Command for generating a java version of the ZAP API
		GoAPIGenerator gapi = new GoAPIGenerator();
		gapi.generateGoFiles(ApiGeneratorUtils.getAllImplementors());
	}

}
