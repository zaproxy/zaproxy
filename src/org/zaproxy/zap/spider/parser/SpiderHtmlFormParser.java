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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.FormControl;
import net.htmlparser.jericho.FormControlType;
import net.htmlparser.jericho.FormField;
import net.htmlparser.jericho.FormFields;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import org.parosproxy.paros.network.HtmlParameter;
import org.parosproxy.paros.network.HtmlParameter.Type;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.spider.SpiderParam;
import org.zaproxy.zap.spider.URLCanonicalizer;

/**
 * The Class SpiderHtmlFormParser is used for parsing HTML files for processing forms.
 */
public class SpiderHtmlFormParser extends SpiderParser {

	private static final String ENCODING_TYPE = "UTF-8";
	private static final String DEFAULT_NUMBER_VALUE = "1";
	private static final String DEFAULT_FILE_VALUE = "test_file.txt";
	private static final String DEFAULT_TEXT_VALUE = org.parosproxy.paros.Constant.PROGRAM_NAME_SHORT;

	private static final String METHOD_POST = "POST";
	private static final String ATTR_TYPE = "type";
	private static final String DEFAULT_EMPTY_VALUE = "";
	private static final String DEFAULT_PASS_VALUE = DEFAULT_TEXT_VALUE;

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

	@Override
	public boolean parseResource(HttpMessage message, Source source, int depth) {
		log.debug("Parsing an HTML message for forms...");
		// If form processing is disabled, don't parse anything
		if (!param.isProcessForm()) {
			return false;
		}

		// Prepare the source, if not provided
		if (source == null) {
			source = new Source(message.getResponseBody().toString());
		}

		// Get the context (base url)
		String baseURL;
		if (message == null) {
			baseURL = "";
		} else {
			baseURL = message.getRequestHeader().getURI().toString();
		}

		// Try to see if there's any BASE tag that could change the base URL
		Element base = source.getFirstElement(HTMLElementName.BASE);
		if (base != null) {
			if (log.isDebugEnabled()) {
				log.debug("Base tag was found in HTML: " + base.getDebugInfo());
			}
			if (base.getAttributeValue("href") != null) {
				baseURL = base.getAttributeValue("href");
			}
		}

		// Go through the forms
		List<Element> forms = source.getAllElements(HTMLElementName.FORM);

		for (Element form : forms) {
			// Get method and action
			String method = form.getAttributeValue("method");
			String action = form.getAttributeValue("action");
			log.debug("Found new form with method: '" + method + "' and action: " + action);

			// If no action, skip the form
			if (action == null) {
				log.debug("No form 'action' defined. Using base URL: " + baseURL);
				action = baseURL;
			}

			// If POSTing forms is not enabled, skip processing of forms with POST method
			if (!param.isPostForm() && method != null && method.trim().equalsIgnoreCase(METHOD_POST)) {
				log.debug("Skipping form with POST method because of user settings.");
				continue;
			}

			// Prepare data set
			List<HtmlParameter> formDataSet = prepareFormDataSet(form.getFormFields());

			// Process the case of a POST method
			if (method != null && method.trim().equalsIgnoreCase(METHOD_POST)) {
				String query = "";

				/*
				 * Ignore encoding, as we will not POST files anyway, so using
				 * "application/x-www-form-urlencoded" is adequate
				 */
				// String encoding = form.getAttributeValue("enctype");
				// if (encoding != null && encoding.equals("multipart/form-data"))
				query = buildEncodedUrlQuery(formDataSet);
				log.debug("Submiting form with POST method and message body with form parameters (normal encoding): "
						+ query);

				// Build the absolute canonical URL
				String fullURL = URLCanonicalizer.getCanonicalURL(action, baseURL);
				if (fullURL == null) {
					return false;
				}

				log.debug("Canonical URL constructed using '" + action + "': " + fullURL);
				notifyListenersPostResourceFound(message, depth + 1, fullURL, query);

			} // Process anything else as a GET method
			else {
				String query = buildEncodedUrlQuery(formDataSet);
				log.debug("Submiting form with GET method and query with form parameters: " + query);

				// Clear the fragment, if any, as it does not have any relevance for the server
				if (action.contains("#")) {
					int fs = action.lastIndexOf("#");
					action = action.substring(0, fs);
				}

				// Process the final URL
				if (action.contains("?")) {
					if (action.endsWith("?")) {
						processURL(message, depth, action + query, baseURL);
					} else {
						processURL(message, depth, action + "&" + query, baseURL);
					}
				} else {
					processURL(message, depth, action + "?" + query, baseURL);
				}
			}

		}

		return false;
	}

	/**
	 * Prepares the form data set. A form data set is a sequence of control-name/current-value pairs
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
	private List<HtmlParameter> prepareFormDataSet(FormFields form) {
		List<HtmlParameter> formDataSet = new LinkedList<>();

		// Process each form field
		Iterator<FormField> it = form.iterator();
		while (it.hasNext()) {
			FormField field = it.next();
			if (log.isDebugEnabled()) {
				log.debug("New form field: " + field.getDebugInfo());
			}

			// Get its value(s)
			List<String> values = field.getValues();
			if (log.isDebugEnabled()) {
				log.debug("Existing values: " + values);
			}

			// If there are no values at all or only an empty value
			if (values.isEmpty() || (values.size() == 1 && values.get(0).isEmpty())) {
				String finalValue = DEFAULT_EMPTY_VALUE;

				// Check if we can use predefined values
				Collection<String> predefValues = field.getPredefinedValues();
				if (!predefValues.isEmpty()) {
					// Try first elements
					Iterator<String> iterator = predefValues.iterator();
					finalValue = iterator.next();

					// If there are more values, don't use the first, as it usually is a "No select"
					// item
					if (iterator.hasNext()) {
						finalValue = iterator.next();
					}
				} else {
					/*
					 * In all cases, according to Jericho documentation, the only left option is for
					 * it to be a TEXT field, without any predefined value. We check if it has only
					 * one userValueCount, and, if so, fill it with a default value.
					 */
					if (field.getUserValueCount() > 0) {
						finalValue = getDefaultTextValue(field);
					}
				}

				// Save the finalValue in the FormDataSet
				log.debug("No existing value for field " + field.getName() + ". Generated: " + finalValue);
				HtmlParameter p = new HtmlParameter(Type.form, field.getName(), finalValue);
				formDataSet.add(p);
			}
			// If there are preselected values for the fields, use them
			else {
				for (String v : values) {
					// Save the finalValue in the FormDataSet
					HtmlParameter p = new HtmlParameter(Type.form, field.getName(), v);
					formDataSet.add(p);
				}
			}
		}

		return formDataSet;
	}

	/**
	 * Gets the default value that the input field, including HTML5 types, should have.
	 * 
	 * <p>
	 * Generates accurate field values for following types:
	 * <ul>
	 * <li>Text/Password/Search - DEFAULT_TEXT_VALUE</li>
	 * <li>number/range - if min is defined, then use min, otherwise if max is defined use max
	 * otherwise DEFAULT_NUMBER_VALUE;</li>
	 * <li>url - http://www.example.com</li>
	 * <li>email - contact@example.com</li>
	 * <li>color - #000000</li>
	 * <li>tel - 9999999999</li>
	 * <li>date/datetime/time/month/week/datetime-local - current date in the proper format</li>
	 * <li>file - DEFAULT_FILE_VALUE</li>
	 * </ul>
	 * 
	 * @param field the field
	 * @return the default text value
	 */
	private String getDefaultTextValue(FormField field) {
		FormControl fc = field.getFormControl();
		if (fc.getFormControlType() == FormControlType.TEXT) {
			// If the control type was reduced to a TEXT type by the Jericho library, check the
			// HTML5 type and use proper values
			String type = fc.getAttributesMap().get(ATTR_TYPE);
			if (type == null || type.equalsIgnoreCase("text")) {
				return DEFAULT_TEXT_VALUE;
			}

			if (type.equalsIgnoreCase("number") || type.equalsIgnoreCase("range")) {
				String min = fc.getAttributesMap().get("min");
				if (min != null) {
					return min;
				}
				String max = fc.getAttributesMap().get("min");
				if (max != null) {
					return max;
				}
				return DEFAULT_NUMBER_VALUE;
			}

			if (type.equalsIgnoreCase("url")) {
				return "http://www.example.com";
			}

			if (type.equalsIgnoreCase("email")) {
				return "foo-bar@example.com";
			}

			if (type.equalsIgnoreCase("color")) {
				return "#ffffff";
			}

			if (type.equalsIgnoreCase("tel")) {
				return "9999999999";
			}

			if (type.equalsIgnoreCase("datetime")) {
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
				return format.format(new Date());
			}
			if (type.equalsIgnoreCase("datetime-local")) {
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
				return format.format(new Date());
			}
			if (type.equalsIgnoreCase("date")) {
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				return format.format(new Date());
			}
			if (type.equalsIgnoreCase("time")) {
				SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
				return format.format(new Date());
			}
			if (type.equalsIgnoreCase("month")) {
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
				return format.format(new Date());
			}
			if (type.equalsIgnoreCase("week")) {
				SimpleDateFormat format = new SimpleDateFormat("yyyy-'W'ww");
				return format.format(new Date());
			}
		} else if (fc.getFormControlType() == FormControlType.PASSWORD) {
			return DEFAULT_PASS_VALUE;
		} else if (fc.getFormControlType() == FormControlType.FILE) {
			return DEFAULT_FILE_VALUE;
		}
		return DEFAULT_EMPTY_VALUE;
	}

	/**
	 * Builds the query, encoded with "application/x-www-form-urlencoded".
	 * 
	 * 
	 * @see http://www.w3.org/TR/REC-html40/interact/forms.html#form-content-type
	 * @param formDataSet the form data set
	 * @return the query
	 */
	private String buildEncodedUrlQuery(List<HtmlParameter> formDataSet) {
		StringBuilder request = new StringBuilder();
		// Build the query
		for (HtmlParameter p : formDataSet) {
			String v;
			try {
				v = URLEncoder.encode(p.getName(), ENCODING_TYPE);
				request.append(v);
				request.append("=");
				v = URLEncoder.encode(p.getValue(), ENCODING_TYPE);
				request.append(v);
			} catch (UnsupportedEncodingException e) {
				log.warn("Error while encoding query for form.", e);
			}
			request.append("&");
		}
		// Delete the last ampersand
		if (request.length() > 0) {
			request.deleteCharAt(request.length() - 1);
		}

		return request.toString();
	}

	@Override
	public boolean canParseResource(HttpMessage message, String path, boolean wasAlreadyConsumed) {
		// Fallback parser - if it's a HTML message which has not already been processed		
		return !wasAlreadyConsumed && message.getResponseHeader() != null
				&& message.getResponseHeader().isHtml();
	}
}
