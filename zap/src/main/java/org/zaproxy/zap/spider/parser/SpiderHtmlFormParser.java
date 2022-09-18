/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.FormControl;
import net.htmlparser.jericho.FormField;
import net.htmlparser.jericho.FormFields;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;
import org.apache.commons.httpclient.URI;
import org.apache.commons.lang3.StringUtils;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.model.DefaultValueGenerator;
import org.zaproxy.zap.model.ValueGenerator;

/**
 * The Class SpiderHtmlFormParser is used for parsing HTML files for processing forms.
 *
 * @deprecated (2.12.0) See the spider add-on in zap-extensions instead.
 */
@Deprecated
public class SpiderHtmlFormParser extends SpiderParser {

    private static final String ENCODING_TYPE = "UTF-8";
    private static final String DEFAULT_EMPTY_VALUE = "";
    private static final String METHOD_GET = "GET";
    private static final String METHOD_POST = "POST";
    private URI uri;
    private String url;

    /** The form attributes */
    private Map<String, String> envAttributes = new HashMap<>();

    /** Create new Value Generator field */
    private final ValueGenerator valueGenerator;

    /**
     * Instantiates a new spider html form parser.
     *
     * @param param the parameters for the spider
     * @throws IllegalArgumentException if {@code param} is null.
     */
    public SpiderHtmlFormParser(org.zaproxy.zap.spider.SpiderParam param) {
        this(param, new DefaultValueGenerator());
    }

    /**
     * Instantiates a new spider html form parser.
     *
     * @param param the parameters for the spider
     * @param valueGenerator the ValueGenerator
     * @throws IllegalArgumentException if {@code valueGenerator} is null.
     * @throws NullPointerException if {@code param} is null.
     */
    public SpiderHtmlFormParser(
            org.zaproxy.zap.spider.SpiderParam param, ValueGenerator valueGenerator) {
        super(param);
        if (valueGenerator == null) {
            throw new IllegalArgumentException("Parameter valueGenerator must not be null.");
        }
        this.valueGenerator = valueGenerator;
    }

    @Override
    public boolean parseResource(HttpMessage message, Source source, int depth) {
        getLogger().debug("Parsing an HTML message for forms...");
        // If form processing is disabled, don't parse anything
        if (!getSpiderParam().isProcessForm()) {
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
            getLogger().debug("Base tag was found in HTML: {}", base.getDebugInfo());
            String href = base.getAttributeValue("href");
            if (href != null && !href.isEmpty()) {
                baseURL = getCanonicalURL(href, baseURL);
            }
        }

        // Go through the forms
        List<Element> forms = source.getAllElements(HTMLElementName.FORM);

        for (Element form : forms) {
            // Clear the attributes for each form and store their key and values
            envAttributes.clear();
            for (Attribute att : form.getAttributes()) {
                envAttributes.put(att.getKey(), att.getValue());
            }
            // Get method and action
            String formMethod = form.getAttributeValue("method");

            // A single form can have multiple actions associated to it
            List<FormAction> formActions = processFormActions(form, formMethod, baseURL, source);

            for (FormAction fAction : formActions) {
                String action = fAction.action;
                String method = fAction.method;
                getLogger()
                        .debug("Found new form with method: '{}' and action: {}", method, action);

                // If POSTing forms is not enabled, skip processing of forms with POST method
                if (!getSpiderParam().isPostForm()
                        && method != null
                        && method.trim().equalsIgnoreCase(METHOD_POST)) {
                    getLogger().debug("Skipping form with POST method because of user settings.");
                    continue;
                }

                // Clear the fragment, if any, as it does not have any relevance for the server
                if (action.contains("#")) {
                    int fs = action.lastIndexOf("#");
                    action = action.substring(0, fs);
                }

                url = getCanonicalURL(action, baseURL);
                FormData formData = prepareFormDataSet(source, form);

                // Process the case of a POST method
                if (method != null && method.trim().equalsIgnoreCase(METHOD_POST)) {
                    // Build the absolute canonical URL
                    String fullURL = getCanonicalURL(action, baseURL);
                    if (fullURL == null) {
                        return false;
                    }
                    getLogger().debug("Canonical URL constructed using '{}: {}", action, fullURL);

                    /*
                     * Ignore encoding, as we will not POST files anyway, so using
                     * "application/x-www-form-urlencoded" is adequate
                     */
                    // String encoding = form.getAttributeValue("enctype");
                    // if (encoding != null && encoding.equals("multipart/form-data"))

                    for (String submitData : formData) {
                        notifyPostResourceFound(message, depth, fullURL, submitData);
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
        }

        return false;
    }

    private static class FormAction {
        final String action;
        final String method;

        FormAction(String action, String method) {
            this.action = action;
            this.method = method;
        }
    }

    /**
     * Processes the given form element into, possibly, several URLs.
     *
     * <p>For each button present in the form, we are returning the corresponding action or
     * formaction attribute defining the URL that should handle the form submission
     *
     * @param form the form to inspect
     * @param baseURL the base URL
     * @return a list of FormAction objects containing the action and associated method
     */
    private List<FormAction> processFormActions(
            Element form, String originalMethod, String baseURL, Source source) {
        List<FormAction> formActions = new ArrayList<>();

        String action = form.getAttributeValue("action");
        // If no action, use the base url
        if (action == null) {
            getLogger().debug("No form 'action' defined. Using base URL: {}", baseURL);
            action = baseURL;
        }

        // Check for possible formaction attributes in child elements with button tag.
        // formaction attributes override the action of the parent form

        // Find direct child of the form element first
        List<Element> formButtonElements =
                form.getChildElements().stream()
                        .filter(this::allowedButtonType)
                        .filter(element -> StringUtils.isEmpty(element.getAttributeValue("form")))
                        .collect(Collectors.toList());

        // The form has an id, identify possible associated buttons anywhere in the document
        if (StringUtils.isNotEmpty(form.getAttributeValue("id"))) {
            String targetId = form.getAttributeValue("id");
            formButtonElements.addAll(
                    source.getAllElements(HTMLElementName.BUTTON).stream()
                            .filter(this::allowedButtonType)
                            .filter(
                                    element ->
                                            StringUtils.equals(
                                                    element.getAttributeValue("form"), targetId))
                            .collect(Collectors.toList()));
        }

        if (!formButtonElements.isEmpty()) {
            final String defaultAction = action;
            formButtonElements.forEach(
                    button -> {
                        // A button without a formaction submits to the default action for the form
                        if (StringUtils.isEmpty(button.getAttributeValue("formaction"))) {
                            formActions.add(
                                    new FormAction(
                                            defaultAction,
                                            processFormMethodWithButton(button, originalMethod)));
                        } else {
                            formActions.add(
                                    new FormAction(
                                            button.getAttributeValue("formaction"),
                                            processFormMethodWithButton(button, originalMethod)));
                        }
                    });
        } else {
            // If no buttons, return just the default action for the form
            formActions.add(new FormAction(action, originalMethod));
        }

        return formActions;
    }

    /**
     * Function that defines the allowed button types that should be considered in a form, reset and
     * button type buttons do not trigger any action so should be ignored
     *
     * @param element an Element of type button
     * @return true if the button should be processed
     */
    private boolean allowedButtonType(Element element) {
        String type = element.getAttributeValue("type");
        return element.getStartTag().getName().equals(HTMLElementName.BUTTON)
                && !StringUtils.equalsIgnoreCase(type, "button")
                && !StringUtils.equalsIgnoreCase(type, "reset");
    }

    /**
     * Defines the correct form method to use in case a button belongs to a form. Valid form methods
     * are GET and POST, if a button has no valid form method the original form method is returned
     *
     * @param button an Element of type button
     * @param defaultMethod the default method of the parent form
     * @return the form method to be used
     */
    private String processFormMethodWithButton(Element button, String defaultMethod) {
        String buttonMethod = button.getAttributeValue("formmethod");
        return StringUtils.isEmpty(buttonMethod)
                        || (!buttonMethod.equalsIgnoreCase(METHOD_GET)
                                && !buttonMethod.equalsIgnoreCase(METHOD_POST))
                ? defaultMethod
                : buttonMethod;
    }

    /**
     * Processes the given GET form data into, possibly, several URLs.
     *
     * <p>For each submit field present in the form data is processed one URL, which includes
     * remaining normal fields.
     *
     * @param message the source message
     * @param depth the current depth
     * @param action the action
     * @param baseURL the base URL
     * @param formData the GET form data
     * @see #processURL(HttpMessage, int, String, String)
     */
    private void processGetForm(
            HttpMessage message, int depth, String action, String baseURL, FormData formData) {
        for (String submitData : formData) {
            getLogger()
                    .debug(
                            "Submitting form with GET method and query with form parameters: {}",
                            submitData);
            processURL(message, depth, action + submitData, baseURL);
        }
    }

    /**
     * Prepares the form data set. A form data set is a sequence of control-name/current-value pairs
     * constructed from successful controls, which will be sent with a GET/POST request for a form.
     *
     * @see <a href="https://www.w3.org/TR/REC-html40/interact/forms.html#form-data-set">HTML 4.01
     *     Specification - 17.13.3 Processing form data</a>
     * @see <a
     *     href="https://html.spec.whatwg.org/multipage/forms.html#association-of-controls-and-forms">HTML
     *     5 - 4.10.18.3 Association of controls and forms</a>
     * @param source the source where the form is (to obtain further input elements)
     * @param form the form
     * @return the list
     */
    private FormData prepareFormDataSet(Source source, Element form) {
        List<FormDataField> formDataFields = new LinkedList<>();

        // Process each form field
        Iterator<FormField> it = getFormFields(source, form).iterator();
        while (it.hasNext()) {
            FormField field = it.next();
            getLogger().debug("New form field: {}", field.getDebugInfo());
            for (String value : getDefaultTextValue(field)) {
                formDataFields.add(
                        new FormDataField(
                                field.getName(),
                                value,
                                field.getFormControl().getFormControlType().isSubmit()));
            }
        }
        return new FormData(formDataFields);
    }

    private static FormFields getFormFields(Source source, Element form) {
        SortedSet<FormControl> formControls = new TreeSet<>();
        addAll(formControls, form, HTMLElementName.INPUT);
        addAll(formControls, form, HTMLElementName.TEXTAREA);
        addAll(formControls, form, HTMLElementName.SELECT);
        addAll(formControls, form, HTMLElementName.BUTTON);

        String formId = form.getAttributeValue("id");
        if (formId != null && !formId.isEmpty()) {
            addAll(formControls, source.getAllElements("form", formId, true));
        }
        for (Iterator<FormControl> it = formControls.iterator(); it.hasNext(); ) {
            FormControl formControl = it.next();
            String targetForm = formControl.getAttributesMap().get("form");
            if (targetForm != null && !targetForm.equals(formId)) {
                it.remove();
            }
        }

        return new FormFields(formControls);
    }

    private static void addAll(
            SortedSet<FormControl> formControls, Segment segment, String tagName) {
        addAll(formControls, segment.getAllElements(tagName));
    }

    private static void addAll(SortedSet<FormControl> formControls, List<Element> elements) {
        for (Element element : elements) {
            FormControl formControl = element.getFormControl();
            if (formControl != null) {
                formControls.add(formControl);
            }
        }
    }

    /**
     * Gets the values for the given {@code field}. If the field is of submit type it passes the
     * predefined values to the ValueGenerator and returns its predefined values. Gets the default
     * value that the input field, including HTML5 types, should have.
     *
     * @param field the field
     * @return a list with the values
     */
    private List<String> getDefaultTextValue(FormField field) {

        // Get the Id
        String fieldId = field.getName();

        // Create new HashMap 'fieldAttributes' and new list 'definedValues'
        Map<String, String> fieldAttributes = new HashMap<>();
        List<String> definedValues = new ArrayList<>();

        // Store all values in the FormFiled field into the Map 'fieldAttributes'
        fieldAttributes.putAll(field.getFormControl().getAttributesMap());

        // Places a key, Control Type, for each FormControlType
        fieldAttributes.put("Control Type", field.getFormControl().getFormControlType().name());

        // Handles Submit Fields
        if (field.getFormControl().getFormControlType().isSubmit()) {
            List<String> submitFields = new ArrayList<>();
            for (String value : field.getPredefinedValues()) {
                String finalValue =
                        this.valueGenerator.getValue(
                                uri,
                                url,
                                fieldId,
                                value,
                                definedValues,
                                envAttributes,
                                fieldAttributes);
                submitFields.add(finalValue);
            }
            return submitFields;
        }

        // Get its value(s)
        List<String> values = field.getValues();
        String defaultValue = null;

        // If the field has a value attribute present(Predefined value)
        // Should store the value being submitted to be passed to the ValueGenerator
        if (field.getFormControl().getAttributesMap().containsKey("value")) {
            defaultValue = field.getFormControl().getAttributesMap().get("value");
        }

        getLogger().debug("Existing values: {}", values);

        // If there are no values at all or only an empty value
        if (values.isEmpty() || (values.size() == 1 && values.get(0).isEmpty())) {

            // Check if we can use predefined values
            Collection<String> predefValues = field.getPredefinedValues();
            if (!predefValues.isEmpty()) {
                // Store those predefined values in a list for the DefaultValueGenerator
                definedValues.addAll(predefValues);
                if (defaultValue == null) {
                    // Try first elements
                    Iterator<String> iterator = predefValues.iterator();
                    defaultValue = iterator.next();

                    // If there are more values, don't use the first, as it usually is a "No select"
                    // item
                    if (iterator.hasNext()) {
                        defaultValue = iterator.next();
                    }
                }
            }
            defaultValue = defaultValue == null ? DEFAULT_EMPTY_VALUE : defaultValue;

        } else if (defaultValue == null) {
            defaultValue = values.get(0);
        }

        // Get the default value used in DefaultValueGenerator
        String finalValue =
                this.valueGenerator.getValue(
                        uri,
                        url,
                        fieldId,
                        defaultValue,
                        definedValues,
                        envAttributes,
                        fieldAttributes);

        getLogger().debug("Generated: {}For field {}", finalValue, field.getName());

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
    private void notifyPostResourceFound(
            HttpMessage message, int depth, String url, String requestBody) {
        getLogger()
                .debug(
                        "Submitting form with POST method and message body with form parameters (normal encoding): {}",
                        requestBody);
        notifyListenersResourceFound(
                SpiderResourceFound.builder()
                        .setMessage(message)
                        .setDepth(depth + 1)
                        .setUri(url)
                        .setMethod(METHOD_POST)
                        .setBody(requestBody)
                        .build());
    }

    @Override
    public boolean canParseResource(HttpMessage message, String path, boolean wasAlreadyConsumed) {
        // Fallback parser - if it's a HTML message which has not already been processed
        return !wasAlreadyConsumed && message.getResponseHeader().isHtml();
    }

    /**
     * The fields (and its values) of a HTML form.
     *
     * <p>Builds the form data encoded with "application/x-www-form-urlencoded".
     *
     * @see <a href="https://www.w3.org/TR/REC-html40/interact/forms.html#form-content-type">HTML
     *     4.01 Specification - 17.13.4 Form content types</a>
     */
    private static class FormData implements Iterable<String> {

        private final List<FormDataField> fields;
        private final List<FormDataField> submitFields;

        private FormData(List<FormDataField> fields) {
            this.fields = fields;
            this.submitFields = new ArrayList<>();
            this.fields.forEach(
                    f -> {
                        if (f.isSubmit()) {
                            submitFields.add(f);
                        }
                    });
        }

        @Override
        public Iterator<String> iterator() {
            return new IteratorImpl();
        }

        private class IteratorImpl implements Iterator<String> {

            private boolean started;
            private List<FormDataField> consumedSubmitFields = new ArrayList<>();

            @Override
            public boolean hasNext() {
                return !started || consumedSubmitFields.size() < submitFields.size();
            }

            @Override
            public String next() {
                if (!started) {
                    started = true;
                } else if (consumedSubmitFields.size() >= submitFields.size()) {
                    throw new NoSuchElementException("No more form data to generate.");
                }

                boolean submitted = false;
                StringBuilder formData = new StringBuilder(100);
                for (FormDataField field : fields) {
                    if (field.isSubmit()) {
                        if (submitted || consumedSubmitFields.contains(field)) {
                            continue;
                        }
                        submitted = true;
                        consumedSubmitFields.add(field);
                    }

                    if (formData.length() > 0) {
                        formData.append('&');
                    }

                    formData.append(field.getName());
                    formData.append('=');
                    formData.append(field.getValue());
                }

                return formData.toString();
            }
        }
    }

    /** A field of a {@link FormData}. */
    private static class FormDataField {

        private String name;
        private String value;
        private boolean submit;

        public FormDataField(String name, String value, boolean submit) {
            try {
                this.name = URLEncoder.encode(name, ENCODING_TYPE);
                this.value = URLEncoder.encode(value, ENCODING_TYPE);
                this.submit = submit;
            } catch (UnsupportedEncodingException ignore) {
                // UTF-8 is one of the standard charsets of the JVM.
            }
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        public boolean isSubmit() {
            return submit;
        }
    }
}
