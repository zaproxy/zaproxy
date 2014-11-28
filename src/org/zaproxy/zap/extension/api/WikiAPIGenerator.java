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
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.parosproxy.paros.Constant;

public class WikiAPIGenerator {
	/*
	 * Note that this currently only generates English wiki pages, although the API itself can be internationalized
	 */
	private String base = "ApiGen_";
	private String title = "= ZAP 2.0.0 API =\n";
	private File dir; 
	private int methods = 0;
	private boolean optional = false;

	private ResourceBundle msgs = ResourceBundle.getBundle("lang." + Constant.MESSAGES_PREFIX, Locale.ENGLISH,
		ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_PROPERTIES));

    public WikiAPIGenerator() {
    	dir = new File("../zaproxy-wiki"); 
    }

    public WikiAPIGenerator(String path, boolean optional) {
    	dir = new File(path); 
    	this.optional = optional;
    }

	private void generateWikiIndex() throws IOException {
		File f = new File(this.dir, base + "Index.wiki");
		System.out.println("Generating " + f.getAbsolutePath());
		FileWriter out = new FileWriter(f);
		out.write(title);
		out.write("== Components ==\n");
		for (ApiImplementor imp : ApiGeneratorUtils.getAllImplementors()) {
			out.write("  * [" + base + imp.getPrefix() + " " + imp.getPrefix() + "]\n");
		}
		out.write("\n\n[" + base + "Full" + " Full list.]\n\n");
		//out.write("Generated on " + new Date() + "\n");
		out.close();
	}

	private void generateWikiFull() throws IOException {
		File f = new File(this.dir, base + "Full.wiki");
		System.out.println("Generating " + f.getAbsolutePath());
		FileWriter out = new FileWriter(f);
		out.write(title);
		out.write("== Full List ==\n");
		out.write("|| _Component_ || _Name_ || _Type_ || _Parameters_ || _Description_ ||\n");
		for (ApiImplementor imp : ApiGeneratorUtils.getAllImplementors()) {
			for (ApiElement view : imp.getApiViews()) {
				this.generateWikiElement(view, imp.getPrefix(), "view", out, true);
			}
			for (ApiElement action : imp.getApiActions()) {
				this.generateWikiElement(action, imp.getPrefix(), "action", out, true);
			}
			for (ApiElement other : imp.getApiOthers()) {
				this.generateWikiElement(other, imp.getPrefix(), "other", out, true);
			}
		}
		out.write("\n");
		out.write("Starred parameters are mandatory.\n\n");
		if (optional) {
			out.write("This component is optional and therefore the API will only work if it is installed.\n\n");
		}
		out.write("Back to [" + base + "Index index]\n\n");
		//out.write("\nGenerated on " + new Date() + "\n");
		out.close();
	}

	public void generateWikiFiles(List<ApiImplementor> implementors) throws IOException {
		// Generate index first
		this.generateWikiIndex();
		for (ApiImplementor imp : implementors) {
			this.generateWikiComponent(imp);
		}
		this.methods = 0;
		this.generateWikiFull();
		System.out.println("Generated a total of " + methods + " methods");
	}
	
	private void generateWikiElement(ApiElement element, String component, String type, Writer out) throws IOException {
		this.generateWikiElement(element, component, type, out, false);
		
	}
	private void generateWikiElement(ApiElement element, String component, String type, Writer out, boolean incComponentCol) throws IOException {
		if (incComponentCol) {
			out.write("|| " + component);
		}
		out.write("|| " + element.getName() + "|| " + type + " || ");
		if (element.getMandatoryParamNames() != null) {
			for (String param : element.getMandatoryParamNames()) {
				out.write(param + "`*` ");
			}
		}
		if (element.getOptionalParamNames() != null) {
			for (String param : element.getOptionalParamNames()) {
				out.write(param + " ");
			}
		}
		out.write(" || ");
		// Add description if defined
		String descTag = element.getDescriptionTag();
		if (descTag == null) {
			// This is the default, but it can be overriden by the getDescriptionTag method if required
			descTag = component + ".api." + type + "." + element.getName();
		}
		try {
			out.write(msgs.getString(descTag));
		} catch (Exception e) {
			// Might not be set, so just print out the ones that are missing
			System.out.println("No i18n for: " + descTag);
		}
		
		out.write(" ||\n");
		methods++;
		
	}

	private void generateWikiComponent(ApiImplementor imp) throws IOException {
		File f = new File(this.dir, base + imp.getPrefix() + ".wiki");
		System.out.println("Generating " + f.getAbsolutePath());
		FileWriter out = new FileWriter(f);
		out.write(title);
		out.write("== Component: " + imp.getPrefix() + " ==\n");
		out.write("|| _Name_ || _Type_ || _Parameters_ || _Description_ ||\n");
		for (ApiElement view : imp.getApiViews()) {
			this.generateWikiElement(view, imp.getPrefix(), "view", out);
		}
		for (ApiElement action : imp.getApiActions()) {
			this.generateWikiElement(action, imp.getPrefix(), "action", out);
		}
		for (ApiElement other : imp.getApiOthers()) {
			this.generateWikiElement(other, imp.getPrefix(), "other", out);
		}
		out.write("\n");
		out.write("Starred parameters are mandatory\n\n");
		out.write("Back to [" + base + "Index index]\n\n");
		//out.write("\nGenerated on " + new Date() + "\n");
		out.close();
	}

	public static void main(String[] args) throws Exception {
		// Command for generating a wiki version of the ZAP API
		
		WikiAPIGenerator wapi = new WikiAPIGenerator();
		wapi.generateWikiFiles(ApiGeneratorUtils.getAllImplementors());
		
	}

}
