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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.FormField;
import net.htmlparser.jericho.FormFields;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import org.apache.commons.httpclient.URI;
import org.parosproxy.paros.network.HtmlParameter;
import org.parosproxy.paros.network.HtmlParameter.Type;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.model.DefaultValueGenerator;
import org.zaproxy.zap.model.ValueGenerator;
import org.zaproxy.zap.spider.SpiderParam;
import org.zaproxy.zap.spider.URLCanonicalizer;

/**
 * The Class SpiderHtmlFormParser is used for parsing HTML files for processing forms.
 */
public class SpiderHtmlFormParser extends SpiderParser {

	private static final String ENCODING_TYPE = "UTF-8";
	private static final String DEFAULT_EMPTY_VALUE = "";
	private static final String METHOD_POST = "POST";
	private URI uri;
	private String url;

	/** The form attributes*/
	private Map<String, String> envAttributes = new HashMap<String, String>();

	/** The spider parameters. */
	private final SpiderParam param;

	/**Create new Value Generator field*/
	private final ValueGenerator valueGenerator;

	/**
	 * Instantiates a new spider html form parser.
	 *
	 * @param param the parameters for the spider
	 * @throws IllegalArgumentException if {@code param} is null.
	 */
	public SpiderHtmlFormParser(SpiderParam param) {
		this(param, new DefaultValueGenerator());
	}

	/**
	 * Instantiates a new spider html form parser.
	 *
	 * @param param the parameters for the spider
	 * @param valueGenerator the ValueGenerator
	 * @throws IllegalArgumentException if {@code param} or {@code valueGenerator} is null.
	 */
	public SpiderHtmlFormParser(SpiderParam param, ValueGenerator valueGenerator) {
		super();
		if (param == null) {
			throw new IllegalArgumentException("Parameter param must not be null.");
		}
		if (valueGenerator == null){
			throw new IllegalArgumentException("Parameter valueGenerator must not be null.");
		}
		this.param = param;
		this.valueGenerator = valueGenerator;
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
		String baseURL = message.getRequestHeader().getURI().toString();
		uri = message.getRequestHeader().getURI();

		// Try to see if there's any BASE tag that could change the base URL
		Element base = source.getFirstElement(HTMLElementName.BASE);
		if (base != null) {
			if (log.isDebugEnabled()) {
				log.debug("Base tag was found in HTML: " + base.getDebugInfo());
			}
			String href = base.getAttributeValue("href");
			if (href != null && !href.isEmpty()) {
				baseURL = URLCanonicalizer.getCanonicalURL(href, baseURL);
			}
		}

		// Go through the forms
		List<Element> forms = source.getAllElements(HTMLElementName.FORM);

		for (Element form : forms) {
			//Clear the attributes for each form and store their key and values
			envAttributes.clear();
			for (Attribute att : form.getAttributes()){
				envAttributes.put(att.getKey(), att.getValue());
			}
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

			// Clear the fragment, if any, as it does not have any relevance for the server
			if (action.contains("#")) {
				int fs = action.lastIndexOf("#");
				action = action.substring(0, fs);
			}

			url = URLCanonicalizer.getCanonicalURL(action, baseURL);
			FormData formData = prepareFormDataSet(form.getFormFields());

			// Process the case of a POST method
			if (method != null && method.trim().equalsIgnoreCase(METHOD_POST)) {
				// Build the absolute canonical URL
				String fullURL = URLCanonicalizer.getCanonicalURL(action, baseURL);
				if (fullURL == null) {
					return false;
				}
				log.debug("Canonical URL constructed using '" + action + "': " + fullURL);

				/*
				 * Ignore encoding, as we will not POST files anyway, so using
				 * "application/x-www-form-urlencoded" is adequate
				 */
				// String encoding = form.getAttributeValue("enctype");
				// if (encoding != null && encoding.equals("multipart/form-data"))
				String baseRequestBody = buildEncodedUrlQuery(formData.getFields());
				if (formData.getSubmitFields().isEmpty()) {
					notifyPostResourceFound(message, depth, fullURL, baseRequestBody);
					continue;
				}

				for (HtmlParameter submitField : formData.getSubmitFields()) {
					notifyPostResourceFound(
							message,
							depth,
							fullURL,
							appendEncodedUrlQueryParameter(baseRequestBody, submitField));
				}

			} // Process anything else as a GET method
			else {

				// Process the final URL
				if (action.contains("?")) {
					if (action.endsWith("?")) {
						processGetForm(message, depth, action, baseURL, formData);
					} else {
						processGetForm(message, depth, action + "&", baseURL, formData);
					}
				} else {
					processGetForm(message, depth, action + "?", baseURL, formData);
				}
			}

		}

		return false;
	}

	/**
	 * Processes the given GET form data into, possibly, several URLs.
	 * <p>
	 * For each submit field present in the form data is processed one URL, which includes remaining normal fields.
	 *
	 * @param message the source message
	 * @param depth the current depth
	 * @param action the action
	 * @param baseURL the base URL
	 * @param formData the GET form data
	 * @see #processURL(HttpMessage, int, String, String)
	 */
	private void processGetForm(HttpMessage message, int depth, String action, String baseURL, FormData formData) {
		String baseQuery = buildEncodedUrlQuery(formData.getFields());
		if (formData.getSubmitFields().isEmpty()) {
			log.debug("Submiting form with GET method and query with form parameters: " + baseQuery);
			processURL(message, depth, action + baseQuery, baseURL);
		} else {
			for (HtmlParameter submitField : formData.getSubmitFields()) {
				String query = appendEncodedUrlQueryParameter(baseQuery, submitField);
				log.debug("Submiting form with GET method and query with form parameters: " + query);
				processURL(message, depth, action + query, baseURL);
			}
		}
	}

	/**
	 * Prepares the form data set. A form data set is a sequence of control-name/current-value pairs
	 * constructed from successful controls, which will be sent with a GET/POST request for a form.
	 * 
	 * @see <a href="https://www.w3.org/TR/REC-html40/interact/forms.html#form-data-set">HTML 4.01 Specification - 17.13.3
	 *      Processing form data</a>
	 * @see <a href="https://html.spec.whatwg.org/multipage/forms.html#association-of-controls-and-forms">HTML 5 - 4.10.18.3
	 *      Association of controls and forms</a>
	 * @param form the form
	 * @return the list
	 */
	private FormData prepareFormDataSet(FormFields form) {
		List<HtmlParameter> formDataSet = new LinkedList<>();
		List<HtmlParameter> submitFields = new ArrayList<>();

		// Process each form field
		Iterator<FormField> it = form.iterator();
		while (it.hasNext()) {
			FormField field = it.next();
			if (log.isDebugEnabled()) {
				log.debug("New form field: " + field.getDebugInfo());
			}

			List<HtmlParameter> currentList = formDataSet;
			if (field.getFormControl().getFormControlType().isSubmit()) {
				currentList = submitFields;
			}
			for (String value : getDefaultTextValue(field)) {
				currentList.add(new HtmlParameter(Type.form, field.getName(), value));
			}
		}

		return new FormData(formDataSet, submitFields);
	}

	/**
	 * Gets the values for the given {@code field}.
	 * If the field is of submit type it passes the predefined values to the ValueGenerator and returns its predefined values.
	 * Gets the default value that the input field, including HTML5 types, should have.
	 *
	 * @param field the field
	 * @return a list with the values
	 */
	private List<String> getDefaultTextValue(FormField field) {

		// Get the Id
		String fieldId = field.getName();

		// Create new HashMap 'fieldAttributes' and new list 'definedValues'
		Map<String, String> fieldAttributes = new HashMap<String, String>();
		List<String> definedValues = new ArrayList<String>();

		//Store all values in the FormFiled field into the Map 'fieldAttributes'
		fieldAttributes.putAll(field.getFormControl().getAttributesMap());

		// Places a key, Control Type, for each FormControlType
		fieldAttributes.put("Control Type", field.getFormControl().getFormControlType().name());


		//Handles Submit Fields
		if (field.getFormControl().getFormControlType().isSubmit()) {
			List<String> submitFields = new ArrayList<String>();
			for (String value : field.getPredefinedValues()){
				String finalValue = this.valueGenerator.getValue(uri, url, fieldId, value, definedValues, envAttributes, fieldAttributes);
				submitFields.add(finalValue);
			}
			return submitFields;
		}

		// Get its value(s)
		List<String> values = field.getValues();
		String defaultValue;

		//If the field has a value attribute present(Predefined value)
		//Should store the value being submitted to be passed to the ValueGenerator
		if(field.getFormControl().getAttributesMap().containsKey("value")){
			defaultValue = field.getFormControl().getAttributesMap().get("value");
		}

		if (log.isDebugEnabled()) {
			log.debug("Existing values: " + values);
		}

		// If there are no values at all or only an empty value
		if (values.isEmpty() || (values.size() == 1 && values.get(0).isEmpty())) {
			defaultValue = DEFAULT_EMPTY_VALUE;

			// Check if we can use predefined values
			Collection<String> predefValues = field.getPredefinedValues();
			if (!predefValues.isEmpty()) {
				//Store those predefined values in a list for the DefaultValueGenerator
				definedValues.addAll(predefValues);
				// Try first elements
				Iterator<String> iterator = predefValues.iterator();
				defaultValue = iterator.next();

				// If there are more values, don't use the first, as it usually is a "No select"
				// item
				if (iterator.hasNext()) {
					defaultValue = iterator.next();
				}
			}

		} else {
			defaultValue = values.get(0);
		}

		//Get the default value used in DefaultValueGenerator
		String finalValue = this.valueGenerator.getValue(uri, url, fieldId, defaultValue, definedValues, envAttributes, fieldAttributes);

		log.debug("Generated: " + finalValue + "For field " + field.getName());

		values = new ArrayList<>(1);
		values.add(finalValue);

		return values;
	}

	/**
	 * Notifies listeners that a new POST resource was found.
	 *
	 * @param message the source message
	 * @param depth the current depth
	 * @param url the URL of the resource
	 * @param requestBody the request body
	 * @see #notifyListenersPostResourceFound(HttpMessage, int, String, String)
	 */
	private void notifyPostResourceFound(HttpMessage message, int depth, String url, String requestBody) {
		log.debug("Submiting form with POST method and message body with form parameters (normal encoding): " + requestBody);
		notifyListenersPostResourceFound(message, depth + 1, url, requestBody);
	}

	/**
	 * Builds the query, encoded with "application/x-www-form-urlencoded".
	 * 
	 * @see <a href="https://www.w3.org/TR/REC-html40/interact/forms.html#form-content-type">HTML 4.01 Specification - 17.13.4
	 *      Form content types</a>
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

	/**
	 * Appends the given {@code parameter} into the given {@code query}.
	 *
	 * @param query the query
	 * @param parameter the parameter to append
	 * @return the query with the parameter appended
	 */
	private static String appendEncodedUrlQueryParameter(String query, HtmlParameter parameter) {
		StringBuilder strBuilder = new StringBuilder(query);
		if (strBuilder.length() != 0) {
			strBuilder.append('&');
		}
		try {
			strBuilder.append(URLEncoder.encode(parameter.getName(), ENCODING_TYPE))
					.append('=')
					.append(URLEncoder.encode(parameter.getValue(), ENCODING_TYPE));
		} catch (UnsupportedEncodingException e) {
			log.warn("Error while encoding query for form.", e);
		}
		return strBuilder.toString();
	}

	@Override
	public boolean canParseResource(HttpMessage message, String path, boolean wasAlreadyConsumed) {
		// Fallback parser - if it's a HTML message which has not already been processed		
		return !wasAlreadyConsumed && message.getResponseHeader().isHtml();
	}

	/**
	 * The fields (and its values) of a HTML form.
	 * <p>
	 * Normal fields and submit fields are kept apart.
	 */
	private static class FormData {

		private final List<HtmlParameter> fields;
		private final List<HtmlParameter> submitFields;

		public FormData(List<HtmlParameter> fields, List<HtmlParameter> submitFields) {
			this.fields = fields;
			this.submitFields = submitFields;
		}

		public List<HtmlParameter> getFields() {
			return fields;
		}

		public List<HtmlParameter> getSubmitFields() {
			return submitFields;
		}
	}
}
