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
package org.zaproxy.zap.spider.parser;

import java.util.LinkedList;
import java.util.List;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.parosproxy.paros.network.HtmlParameter;
import org.parosproxy.paros.network.HtmlParameter.Type;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpSender;
import org.w3c.dom.html.HTMLElement;
import org.zaproxy.zap.spider.SpiderParam;

/**
 * The Class SpiderHtmlFormParser is used for parsing HTML files and for processing forms.
 */
public class SpiderHtmlFormParser extends SpiderParser {

	private static final String METHOD_GET = "GET";
	private static final String METHOD_POST = "POST";

	/** The spider parameters. */
	SpiderParam param;

	/**
	 * Instantiates a new spider html form parser.
	 * 
	 * @param param the parameters for the spider
	 */
	public SpiderHtmlFormParser(SpiderParam param) {
		super();
		this.param = param;
	}

	/* (non-Javadoc)
	 * 
	 * @see
	 * org.zaproxy.zap.spider.parser.SpiderParser#parseResource(org.parosproxy.paros.network.HttpMessage
	 * , net.htmlparser.jericho.Source, int) */
	@Override
	public void parseResource(HttpMessage message, Source source, int depth) {
		// Prepare the source, if not provided
		if (source == null)
			source = new Source(message.getResponseBody().toString());

		// Get the context (base url)
		String baseURL;
		if (message == null)
			baseURL = "";
		else
			baseURL = message.getRequestHeader().getURI().toString();
		log.info("Base URL: " + baseURL);

		// Go through the forms
		List<Element> forms = source.getAllElements(HTMLElementName.FORM);

		for (Element form : forms) {
			// Get method and action
			String method = form.getAttributeValue("method").trim();
			String action = form.getAttributeValue("action").trim();
			log.info("Found new form with method: '" + method + "' and action: " + action);

			// Prepare data set
			List<HtmlParameter> formDataSet = buildFormDataSet(form);

			// Process the case of a GET method
			if (method.equalsIgnoreCase(METHOD_GET)) {
				String query = buildGetQuery(formDataSet);
				log.info("Query with form parameters: " + query);
				processURL(message, depth, action + "?" + query, baseURL);
			}

		}

	}

	/**
	 * Builds the form data set. A form data set is a sequence of control-name/current-value pairs
	 * constructed from successful controls, which will be sent with a GET/POST request for a form.
	 * 
	 * <br/>
	 * Also see:
	 * http://whatwg.org/specs/web-apps/current-work/multipage/association-of-controls-and-forms.
	 * html
	 * 
	 * @see http://www.w3.org/TR/REC-html40/interact/forms.html#form-data-set
	 * @param form the form
	 * @return the list
	 */
	private List<HtmlParameter> buildFormDataSet(Element form) {
		List<HtmlParameter> formDataSet = new LinkedList<HtmlParameter>();
		List<Element> elements;

		// Process INPUT elements
		elements = form.getAllElements(HTMLElementName.INPUT);
		for (Element e : elements) {

			// Get required attributes
			String type = e.getAttributeValue("type");
			String name = e.getAttributeValue("name");
			String value = e.getAttributeValue("value");
			log.debug("New element: " + type + "/" + name + "=" + value);

			// Check for name
			if (name == null)
				continue;

			// Text input
			if (type.equalsIgnoreCase("text")) {
				if (value == null)
					continue;

				HtmlParameter p = new HtmlParameter(Type.form, name, value);
				formDataSet.add(p);
				continue;
			}

		}

		return formDataSet;
	}

	/**
	 * Builds the get query.
	 * 
	 * @param formDataSet the form data set
	 * @return the query
	 */
	private String buildGetQuery(List<HtmlParameter> formDataSet) {
		StringBuilder request = new StringBuilder();
		// Build the query
		for (HtmlParameter p : formDataSet) {
			request.append(p.getName());
			request.append("=");
			request.append(p.getValue());
			request.append("&");
		}
		// Delete the last ampersand
		if (request.length() > 0)
			request.deleteCharAt(request.length() - 1);

		return request.toString();
	}
}
