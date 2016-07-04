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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.parosproxy.paros.Constant;

public class WikiAPIGenerator extends AbstractAPIGenerator {

    private static final String WIKI_FILE_EXTENSION = ".md";

	/*
	 * Note that this currently only generates English wiki pages, although the API itself can be internationalized
	 */
	private String base = "ApiGen_";
	private String title = "# ZAP " + Constant.PROGRAM_VERSION + " API\n";
	private int methods = 0;

    public WikiAPIGenerator() {
    	super("../zaproxy-wiki");
    }

    public WikiAPIGenerator(String path, boolean optional) {
    	super(path, optional);
    }

	private void generateWikiIndex() throws IOException {
		Path file = getDirectory().resolve(base + "Index" + WIKI_FILE_EXTENSION);
		System.out.println("Generating " + file.toAbsolutePath());
		try (BufferedWriter out = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
			out.write(title);
			out.write("## Components\n");
			for (ApiImplementor imp : ApiGeneratorUtils.getAllImplementors()) {
				out.write("  * [" + base + imp.getPrefix() + " " + imp.getPrefix() + "]\n");
			}
			out.write("\n\n[" + base + "Full" + " Full list.]\n\n");
			//out.write("Generated on " + new Date() + "\n");
		}
	}

	private void generateWikiFull() throws IOException {
		Path file = getDirectory().resolve( base + "Full" + WIKI_FILE_EXTENSION);
		System.out.println("Generating " + file.toAbsolutePath());
		try (BufferedWriter out = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
			out.write(title);
			out.write("## Full List\n");
			out.write("| _Component_ | _Name_ | _Type_ | _Parameters_ | _Description_ |\n");
			out.write("|:------------|:-------|:-------|:-------------|:--------------|\n");
			for (ApiImplementor imp : ApiGeneratorUtils.getAllImplementors()) {
				for (ApiElement view : imp.getApiViews()) {
					this.generateWikiElement(view, imp.getPrefix(), VIEW_ENDPOINT, out, true);
				}
				for (ApiElement action : imp.getApiActions()) {
					this.generateWikiElement(action, imp.getPrefix(), ACTION_ENDPOINT, out, true);
				}
				for (ApiElement other : imp.getApiOthers()) {
					this.generateWikiElement(other, imp.getPrefix(), OTHER_ENDPOINT, out, true);
				}
			}
			out.write("\n");
			out.write("Starred parameters are mandatory.\n\n");
			if (isOptional()) {
				out.write(OPTIONAL_MESSAGE + "\n\n");
			}
			out.write("Back to [index](" + base + "Index)\n\n");
			//out.write("\nGenerated on " + new Date() + "\n");
		}
	}

	@Override
	public void generateAPIFiles(List<ApiImplementor> implementors) throws IOException {
		// Generate index first
		this.generateWikiIndex();
		for (ApiImplementor imp : implementors) {
			this.generateAPIFiles(imp);
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
			out.write("| " + component);
		}
		out.write("| " + element.getName() + "| " + type + " | ");
		if (element.getMandatoryParamNames() != null) {
			for (String param : element.getMandatoryParamNames()) {
				out.write(param + "* ");
			}
		}
		if (element.getOptionalParamNames() != null) {
			for (String param : element.getOptionalParamNames()) {
				out.write(param + " ");
			}
		}
		out.write(" | ");
		// Add description if defined
		String descTag = element.getDescriptionTag();
		if (descTag == null) {
			// This is the default, but it can be overriden by the getDescriptionTag method if required
			descTag = component + ".api." + type + "." + element.getName();
		}
		try {
			out.write(getMessages().getString(descTag));
		} catch (Exception e) {
			// Might not be set, so just print out the ones that are missing
			System.out.println("No i18n for: " + descTag);
		}
		
		out.write(" |\n");
		methods++;
		
	}

	@Override
	protected void generateAPIFiles(ApiImplementor imp) throws IOException {
		Path file = getDirectory().resolve(base + imp.getPrefix() + WIKI_FILE_EXTENSION);
		System.out.println("Generating " + file.toAbsolutePath());
		try (BufferedWriter out = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
			out.write(title);
			out.write("## Component: " + imp.getPrefix() + "\n");
			out.write("| _Name_ | _Type_ | _Parameters_ | _Description_ |\n");
			out.write("|:-------|:-------|:-------------|:--------------|\n");
			for (ApiElement view : imp.getApiViews()) {
				this.generateWikiElement(view, imp.getPrefix(), VIEW_ENDPOINT, out);
			}
			for (ApiElement action : imp.getApiActions()) {
				this.generateWikiElement(action, imp.getPrefix(), ACTION_ENDPOINT, out);
			}
			for (ApiElement other : imp.getApiOthers()) {
				this.generateWikiElement(other, imp.getPrefix(), OTHER_ENDPOINT, out);
			}
			out.write("\n");
			out.write("Starred parameters are mandatory\n\n");
			out.write("Back to [index](" + base + "Index)\n\n");
			//out.write("\nGenerated on " + new Date() + "\n");
		}
	}

	public static void main(String[] args) throws Exception {
		// Command for generating a wiki version of the ZAP API
		
		WikiAPIGenerator wapi = new WikiAPIGenerator();
		wapi.generateCoreAPIFiles();
		
	}

}
