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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.ScannerParam;
import org.zaproxy.zap.extension.anticsrf.AntiCsrfAPI;
import org.zaproxy.zap.extension.ascan.ActiveScanAPI;
import org.zaproxy.zap.extension.auth.AuthAPI;
import org.zaproxy.zap.extension.autoupdate.AutoUpdateAPI;
import org.zaproxy.zap.extension.autoupdate.OptionsParamCheckForUpdates;
import org.zaproxy.zap.extension.params.ParamsAPI;
import org.zaproxy.zap.extension.search.SearchAPI;
import org.zaproxy.zap.extension.spider.SpiderAPI;
import org.zaproxy.zap.spider.SpiderParam;

public class JavaAPIGenerator {
	private List<ApiImplementor> implementors = new ArrayList<ApiImplementor> ();
	private File dir = new File("src/org/zaproxy/clientapi/gen"); 
	
	private final String HEADER = 
			"/* Zed Attack Proxy (ZAP) and its related class files.\n" +
			" *\n" +
			" * ZAP is an HTTP/HTTPS proxy for assessing web application security.\n" +
			" *\n" +
			" * Copyright 2012 ZAP development team\n" +
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

	private ResourceBundle msgs = ResourceBundle.getBundle("lang." + Constant.MESSAGES_PREFIX, Locale.ENGLISH);

	public void addImplementor(ApiImplementor imp) {
		this.implementors.add(imp);
	}
	

	public void generateJavaFiles() throws IOException {
		for (ApiImplementor imp : this.implementors) {
			this.generateJavaComponent(imp);
		}
	}
	
	private void generateJavaElement(ApiElement element, String component, 
			String type, Writer out) throws IOException {
		boolean hasParams = false;

		// Add description if defined
		String descTag = element.getDescriptionTag();
		if (descTag == null) {
			// This is the default, but it can be overriden by the getDescriptionTag method if required
			descTag = component + ".api." + type + "." + element.getName();
		}
		try {
			String desc = msgs.getString(descTag);
			out.write("\t/**\n");
			out.write("\t * " + desc + "\n");
			out.write("\t */\n");
		} catch (Exception e) {
			// Might not be set, so just print out the ones that are missing
			System.out.println("No i18n for: " + descTag);
		}

		out.write("\tpublic ApiResponse " + element.getName() + "(");
		//out.write("\t\t\t");

		if (element.getMandatoryParamNames() != null) {
			for (String param : element.getMandatoryParamNames()) {
				if (! hasParams) {
					hasParams = true;
				} else {
					out.write(", ");
				}
				if (param.toLowerCase().equals("boolean")) {
					out.write("boolean bool");
				} else if (param.toLowerCase().equals("integer")) {
					out.write("int i");
				} else {
					out.write("String ");
					out.write(param.toLowerCase());
				}
			}
		}
		if (element.getOptionalParamNames() != null) {
			for (String param : element.getOptionalParamNames()) {
				if (! hasParams) {
					hasParams = true;
				} else {
					out.write(", ");
				}
				if (param.toLowerCase().equals("boolean")) {
					out.write("boolean bool");
				} else if (param.toLowerCase().equals("integer")) {
					out.write("int i");
				} else {
					out.write("String ");
					out.write(param.toLowerCase());
				}
			}
		}
		out.write(") throws ClientApiException {\n");


		out.write("\t\tMap<String, String> map = null;\n"); 
		
		if (hasParams) {
			out.write("\t\tmap = new HashMap<String, String>();\n"); 
			if (element.getMandatoryParamNames() != null) {
				for (String param : element.getMandatoryParamNames()) {
					out.write("\t\tmap.put(\"" + param + "\", ");
					if (param.toLowerCase().equals("boolean")) {
						out.write("Boolean.toString(bool)");
					} else if (param.toLowerCase().equals("integer")) {
						out.write("Integer.toString(i)");
					} else {
						out.write(param.toLowerCase());
					}
					out.write(");\n");
				}
			}
			if (element.getOptionalParamNames() != null) {
				for (String param : element.getOptionalParamNames()) {
					out.write("\t\tmap.put(\"" + param + "\", ");
					if (param.toLowerCase().equals("boolean")) {
						out.write("Boolean.toString(bool)");
					} else if (param.toLowerCase().equals("integer")) {
						out.write("Integer.toString(i)");
					} else {
						out.write(param.toLowerCase());
					}
					out.write(");\n");
				}
			}
		}
		
		out.write("\t\treturn api.callApi(\"" + 
				component + "\", \"" + type + "\", \"" + element.getName() + "\", map);\n"); 
		
		out.write("\t}\n\n");
		
	}

	private void generateJavaComponent(ApiImplementor imp) throws IOException {
		String className = imp.getPrefix().substring(0, 1).toUpperCase() + imp.getPrefix().substring(1);
	
		File f = new File(this.dir, className + ".java");
		System.out.println("Generating " + f.getAbsolutePath());
		FileWriter out = new FileWriter(f);
		out.write(HEADER);
		out.write("package org.zaproxy.clientapi.gen;\n\n");
		
		out.write("import java.util.HashMap;\n");
		out.write("import java.util.Map;\n");
		out.write("import org.zaproxy.clientapi.core.ApiResponse;\n");
		out.write("import org.zaproxy.clientapi.core.ClientApi;\n");
		out.write("import org.zaproxy.clientapi.core.ClientApiException;\n");
		out.write("\n");
		
		out.write("\n");
		out.write("/**\n");
		out.write(" * This file was automatically generated.\n");
		out.write(" */\n");
		out.write("public class " + className + " {\n\n");
		
		out.write("\tprivate ClientApi api = null;\n\n");
		out.write("\tpublic " + className + "(ClientApi api) {\n");
		out.write("\t\tthis.api = api;\n");
		out.write("\t}\n\n");

		for (ApiElement view : imp.getApiViews()) {
			this.generateJavaElement(view, imp.getPrefix(), "view", out);
		}
		for (ApiElement action : imp.getApiActions()) {
			this.generateJavaElement(action, imp.getPrefix(), "action", out);
		}
		for (ApiElement other : imp.getApiOthers()) {
			this.generateJavaElement(other, imp.getPrefix(), "other", out);
		}
		out.write("}\n");
		out.close();
	}

	public static void main(String[] args) throws Exception {
		// Command for generating a python version of the ZAP API
		
		JavaAPIGenerator wapi = new JavaAPIGenerator();
		ApiImplementor api;

		wapi.addImplementor(new AntiCsrfAPI(null));
		wapi.addImplementor(new SearchAPI(null));

		api = new AutoUpdateAPI(null);
		api.addApiOptions(new OptionsParamCheckForUpdates());
		wapi.addImplementor(api);

		api = new SpiderAPI(null);
		api.addApiOptions(new SpiderParam());
		wapi.addImplementor(api);

		api = new CoreAPI();
        //api.addApiOptions(new ConnectionParam());
		wapi.addImplementor(api);

		wapi.addImplementor(new ParamsAPI(null));
		
		api = new ActiveScanAPI(null);
		api.addApiOptions(new ScannerParam());
		wapi.addImplementor(api);
		
		wapi.addImplementor(new AuthAPI(null));

		wapi.generateJavaFiles();
		
	}

}
