/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2011 The ZAP Development Team
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
package org.zaproxy.zap.extension.anticsrf;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.extension.history.HistoryFilter;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.network.HtmlParameter;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.pscan.ExtensionPassiveScan;

/**
 * An {@code Extension} that handles anti-csrf tokens.
 *
 * <p>Extracts and tracks anti-csrf tokens, allowing to refresh and send them in new requests.
 *
 * @since 1.3.0
 */
public class ExtensionAntiCSRF extends ExtensionAdaptor implements SessionChangedListener {

    public static final String NAME = "ExtensionAntiCSRF";
    public static final String TAG = "AntiCSRF";

    private Map<String, AntiCsrfToken> valueToToken = new HashMap<>();

    private OptionsAntiCsrfPanel optionsAntiCsrfPanel = null;
    private PopupMenuGenerateForm popupMenuGenerateForm = null;

    private static Logger log = LogManager.getLogger(ExtensionAntiCSRF.class);

    private AntiCsrfParam antiCsrfParam;
    private AntiCsrfDetectScanner antiCsrfDetectScanner;

    private HistoryReferenceFactory historyReferenceFactory;

    public ExtensionAntiCSRF() {
        super();
        initialize();
    }

    private void initialize() {
        this.setName(NAME);
        this.setOrder(50);
    }

    @Override
    public boolean supportsDb(String type) {
        return true;
    }

    @Override
    public void init() {
        antiCsrfParam = new AntiCsrfParam();
        antiCsrfDetectScanner = new AntiCsrfDetectScanner(this);
    }

    @Override
    public String getUIName() {
        return Constant.messages.getString("acsrf.name");
    }

    @Override
    public void hook(ExtensionHook extensionHook) {
        super.hook(extensionHook);

        extensionHook.addOptionsParamSet(antiCsrfParam);

        final ExtensionHistory extensionHistory =
                Control.getSingleton().getExtensionLoader().getExtension(ExtensionHistory.class);
        if (extensionHistory != null) {
            historyReferenceFactory =
                    new HistoryReferenceFactory() {

                        @Override
                        public HistoryReference createHistoryReference(int id) {
                            return extensionHistory.getHistoryReference(id);
                        }
                    };
        } else {
            historyReferenceFactory =
                    new HistoryReferenceFactory() {

                        @Override
                        public HistoryReference createHistoryReference(int id)
                                throws HttpMalformedHeaderException, DatabaseException {
                            return new HistoryReference(id);
                        }
                    };
        }
        AntiCsrfToken.setHistoryReferenceFactory(historyReferenceFactory);

        extensionHook.addSessionListener(this);

        if (getView() != null) {
            extensionHook.getHookView().addOptionPanel(getOptionsAntiCsrfPanel());
            extensionHook.getHookMenu().addPopupMenuItem(this.getPopupMenuGenerateForm());
        }

        ExtensionPassiveScan extensionPassiveScan =
                Control.getSingleton()
                        .getExtensionLoader()
                        .getExtension(ExtensionPassiveScan.class);
        if (extensionPassiveScan != null) {
            extensionPassiveScan.addPassiveScanner(antiCsrfDetectScanner);
        }

        AntiCsrfAPI api = new AntiCsrfAPI(this);
        api.addApiOptions(getParam());
        extensionHook.addApiImplementor(api);
    }

    @Override
    public void unload() {
        ExtensionPassiveScan extensionPassiveScan =
                Control.getSingleton()
                        .getExtensionLoader()
                        .getExtension(ExtensionPassiveScan.class);
        if (extensionPassiveScan != null) {
            extensionPassiveScan.removePassiveScanner(antiCsrfDetectScanner);
        }

        super.unload();
    }

    private PopupMenuGenerateForm getPopupMenuGenerateForm() {
        if (popupMenuGenerateForm == null) {
            this.popupMenuGenerateForm =
                    new PopupMenuGenerateForm(
                            Constant.messages.getString("anticsrf.genForm.popup"));
        }
        return popupMenuGenerateForm;
    }

    private OptionsAntiCsrfPanel getOptionsAntiCsrfPanel() {
        if (optionsAntiCsrfPanel == null) {
            optionsAntiCsrfPanel = new OptionsAntiCsrfPanel();
        }
        return optionsAntiCsrfPanel;
    }

    protected AntiCsrfParam getParam() {
        return antiCsrfParam;
    }

    void setParam(AntiCsrfParam antiCsrfParam) {
        this.antiCsrfParam = antiCsrfParam;
    }

    /**
     * Gets the names of the anti-csrf tokens handled by this extension.
     *
     * @return the names of the anti-csrf tokens.
     * @see #addAntiCsrfTokenName(String)
     * @since 1.4.0
     */
    public List<String> getAntiCsrfTokenNames() {
        return this.getParam().getTokensNames();
    }

    /**
     * Adds the given token name, enabled by default.
     *
     * <p>The call to this method has no effect if the given name is null or empty, or a token with
     * the given name already exist.
     *
     * @param token the token name to add.
     * @see #removeAntiCsrfTokenName(String)
     * @see #getAntiCsrfTokenNames()
     * @since 1.4.0
     */
    public void addAntiCsrfTokenName(String token) {
        this.getParam().addToken(token);
    }

    /**
     * Removes the given token name.
     *
     * <p>The call to this method has no effect if the given name is null or empty, or if the token
     * with the given name does not exist.
     *
     * @param token the token name to remove.
     * @see #addAntiCsrfTokenName(String)
     * @since 1.4.0
     */
    public void removeAntiCsrfTokenName(String token) {
        this.getParam().removeToken(token);
    }

    public void registerAntiCsrfToken(AntiCsrfToken token) {
        log.debug(
                "registerAntiCsrfToken {} {}",
                token.getMsg().getRequestHeader().getURI(),
                token.getValue());
        synchronized (valueToToken) {
            try {
                HistoryReference hRef = token.getMsg().getHistoryRef();
                if (hRef == null) {
                    hRef =
                            new HistoryReference(
                                    getModel().getSession(),
                                    HistoryReference.TYPE_TEMPORARY,
                                    token.getMsg());
                    token.getMsg().setHistoryRef(null);
                }

                token.setHistoryReferenceId(hRef.getHistoryId());
                valueToToken.put(getURLEncode(token.getValue()), token);
            } catch (HttpMalformedHeaderException | DatabaseException e) {
                log.error("Failed to persist the message: ", e);
            }
        }
    }

    public boolean requestHasToken(HttpMessage msg) {
        return this.requestHasToken(msg.getRequestBody().toString());
    }

    public boolean requestHasToken(String reqBody) {
        Set<String> values;
        synchronized (valueToToken) {
            values = Collections.unmodifiableSet(new HashSet<>(valueToToken.keySet()));
        }
        for (String token : values) {
            if (reqBody.indexOf(token) >= 0) {
                return true;
            }
        }
        return false;
    }

    public List<AntiCsrfToken> getTokens(HttpMessage msg) {
        return this.getTokens(
                msg.getRequestBody().toString(), msg.getRequestHeader().getURI().toString());
    }

    private List<AntiCsrfToken> getTokens(String reqBody, String targetUrl) {
        List<AntiCsrfToken> tokens = new ArrayList<>();
        Set<String> values;
        synchronized (valueToToken) {
            values = Collections.unmodifiableSet(new HashSet<>(valueToToken.keySet()));
        }

        for (String value : values) {
            if (reqBody.indexOf(value) >= 0) {
                AntiCsrfToken token = valueToToken.get(value).clone();
                token.setTargetURL(targetUrl);
                tokens.add(token);
            }
        }

        return tokens;
    }

    public String getTokenValue(HttpMessage tokenMsg, String tokenName) {
        Source source = new Source(tokenMsg.getResponseBody().toString());
        List<Element> formElements = source.getAllElements(HTMLElementName.FORM);

        if (formElements != null && formElements.size() > 0) {
            // Loop through all of the FORM tags

            for (Element formElement : formElements) {
                List<Element> inputElements = formElement.getAllElements(HTMLElementName.INPUT);

                if (inputElements != null && inputElements.size() > 0) {
                    // Loop through all of the INPUT elements
                    for (Element inputElement : inputElements) {
                        String id = inputElement.getAttributeValue("ID");
                        if (id != null && id.equalsIgnoreCase(tokenName)) {
                            return inputElement.getAttributeValue("VALUE");
                        }
                        String name = inputElement.getAttributeValue("NAME");
                        if (name != null && name.equalsIgnoreCase(tokenName)) {
                            return inputElement.getAttributeValue("VALUE");
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Convenience method that calls {@link #getTokensFromResponse(HttpMessage, Source)} with a
     * {@code Source} built from the response of the given HTTP message.
     *
     * @param msg from where the tokens should be extracted.
     * @return the extracted anti-csrf tokens.
     * @since 2.8.0
     */
    public List<AntiCsrfToken> getTokensFromResponse(HttpMessage msg) {
        return getTokensFromResponse(msg, new Source(msg.getResponseBody().toString()));
    }

    /**
     * Gets the {@link #getAntiCsrfTokenNames() known} anti-csrf tokens from the given response.
     *
     * @param msg from where the tokens should be extracted.
     * @param source the HTML source document of the response.
     * @return the extracted anti-csrf tokens.
     * @since 2.2.0
     */
    public List<AntiCsrfToken> getTokensFromResponse(HttpMessage msg, Source source) {
        List<AntiCsrfToken> list = new ArrayList<>();
        List<Element> formElements = source.getAllElements(HTMLElementName.FORM);

        if (formElements != null && formElements.size() > 0) {
            // Loop through all of the FORM tags
            log.debug("Found {} forms", formElements.size());
            int formIndex = 0;

            for (Element formElement : formElements) {
                List<Element> inputElements = formElement.getAllElements(HTMLElementName.INPUT);

                if (inputElements != null && inputElements.size() > 0) {
                    // Loop through all of the INPUT elements
                    log.debug("Found {} inputs", inputElements.size());
                    for (Element inputElement : inputElements) {
                        String value = inputElement.getAttributeValue("VALUE");
                        if (value == null) {
                            continue;
                        }

                        String attId = inputElement.getAttributeValue("ID");
                        boolean found = false;
                        if (isKnownAntiCsrfToken(attId)) {
                            list.add(new AntiCsrfToken(msg, attId, value, formIndex));
                            found = true;
                        }
                        if (!found) {
                            String name = inputElement.getAttributeValue("NAME");
                            if (isKnownAntiCsrfToken(name)) {
                                list.add(new AntiCsrfToken(msg, name, value, formIndex));
                            }
                        }
                    }
                }
                formIndex++;
            }
        }
        return list;
    }

    private boolean isKnownAntiCsrfToken(String name) {
        if (name == null) {
            return false;
        }
        for (String tokenName : this.getAntiCsrfTokenNames()) {
            if (this.getParam().isPartialMatchingEnabled()
                            && StringUtils.containsIgnoreCase(name, tokenName)
                    || tokenName.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void sessionChanged(Session session) {
        if (session == null) {
            // Closedown
            return;
        }

        synchronized (valueToToken) {
            valueToToken.clear();
        }
        // search for tokens...
        try {
            List<Integer> list =
                    getModel()
                            .getDb()
                            .getTableHistory()
                            .getHistoryIdsOfHistType(
                                    session.getSessionId(),
                                    HistoryReference.TYPE_PROXIED,
                                    HistoryReference.TYPE_ZAP_USER);
            HistoryFilter filter = new HistoryFilter();
            filter.setTags(Arrays.asList(new String[] {TAG}));

            AntiCsrfDetectScanner antiCsrfDetectScanner = new AntiCsrfDetectScanner(this);
            for (Integer i : list) {
                HistoryReference hRef = historyReferenceFactory.createHistoryReference(i);
                if (filter.matches(hRef)) {
                    HttpMessage msg = hRef.getHttpMessage();
                    Source src = new Source(msg.getResponseBody().toString());

                    if (msg.isResponseFromTargetHost()) {
                        antiCsrfDetectScanner.scanHttpResponseReceive(
                                msg, hRef.getHistoryId(), src);
                    }
                }
            }
        } catch (DatabaseException | HttpMalformedHeaderException e) {
            log.error(e.getMessage(), e);
        }
    }

    public boolean isAntiCsrfToken(String name) {
        if (name == null) {
            return false;
        }
        return this.getParam().getTokensNames().contains(name.toLowerCase());
    }

    @Override
    public void sessionAboutToChange(Session session) {}

    @Override
    public void sessionScopeChanged(Session session) {}

    @Override
    public String getAuthor() {
        return Constant.ZAP_TEAM;
    }

    @Override
    public String getDescription() {
        return Constant.messages.getString("anticsrf.desc");
    }

    @Override
    public void sessionModeChanged(Mode mode) {
        // Ignore
    }

    public String generateForm(int hrefId) throws Exception {
        ExtensionHistory extHist =
                Control.getSingleton().getExtensionLoader().getExtension(ExtensionHistory.class);
        if (extHist != null) {
            HistoryReference hr = extHist.getHistoryReference(hrefId);
            if (hr != null) {
                return generateForm(hr.getHttpMessage());
            }
        }
        return null;
    }

    /**
     * Generates a HTML form from the given message.
     *
     * @param msg the message used to generate the HTML form, must not be {@code null}.
     * @return a string containing the HTML form, never {@code null}.
     * @throws UnsupportedEncodingException if an error occurred while encoding the values of the
     *     form.
     * @since 2.7.0
     */
    public String generateForm(HttpMessage msg) throws UnsupportedEncodingException {
        return generateForm(msg, "");
    }

    /**
     * Generates a HTML form from the given message, replacing the action with the specified action
     * URL.
     *
     * @param msg the message used to generate the HTML form, must not be {@code null}.
     * @param actionUrl optional parameter
     * @return a string containing the HTML form, never {@code null}.
     * @throws UnsupportedEncodingException if an error occurred while encoding the values of the
     *     form.
     */
    String generateForm(HttpMessage msg, String actionUrl) throws UnsupportedEncodingException {

        String requestUri = msg.getRequestHeader().getURI().toString();
        if (!actionUrl.isEmpty()) {
            requestUri = actionUrl;
        }
        StringBuilder sb = new StringBuilder(300);
        sb.append("<html>\n");
        sb.append("<body>\n");
        sb.append("<h3>");
        String uriEscaped = StringEscapeUtils.escapeHtml(requestUri);
        sb.append(uriEscaped);
        sb.append("</h3>");
        sb.append("<form id=\"f1\" method=\"POST\" action=\"").append(uriEscaped).append("\">\n");
        sb.append("<table>\n");

        TreeSet<HtmlParameter> params = msg.getFormParams();
        Iterator<HtmlParameter> iter = params.iterator();
        while (iter.hasNext()) {
            HtmlParameter htmlParam = iter.next();
            String name = StringEscapeUtils.escapeHtml(urlDecode(htmlParam.getName()));
            String value = StringEscapeUtils.escapeHtml(urlDecode(htmlParam.getValue()));
            sb.append("<tr><td>\n");
            sb.append(name);
            sb.append("<td>");
            sb.append("<input name=\"");
            sb.append(name);
            sb.append("\" value=\"");
            sb.append(value);
            sb.append("\" size=\"100\">");
            sb.append("</tr>\n");
        }

        sb.append("</table>\n");
        sb.append("<input id=\"submit\" type=\"submit\" value=\"Submit\"/>\n");
        sb.append("</form>\n");
        sb.append("</body>\n");
        sb.append("</html>\n");

        return sb.toString();
    }

    private static String urlDecode(String value) {
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (UnsupportedEncodingException ignore) {
            // Shouldn't happen UTF-8 is a standard Charset (see java.nio.charset.StandardCharsets)
        }
        return value;
    }

    static interface HistoryReferenceFactory {

        HistoryReference createHistoryReference(int id)
                throws DatabaseException, HttpMalformedHeaderException;
    }

    /**
     * Regenerates the {@link AntiCsrfToken} of a {@link HttpMessage} if one exists to obtain the
     * new {@link AntiCsrfToken}.
     *
     * @param message The {@link HttpMessage} to be checked.
     * @param httpSender The {@code sendAndReceive} implementation of the caller.
     * @since 2.10.0
     */
    public void regenerateAntiCsrfToken(HttpMessage message, HttpMessageSender httpSender) {
        List<AntiCsrfToken> tokens = getTokens(message);
        AntiCsrfToken antiCsrfToken = null;
        if (tokens.size() > 0) {
            antiCsrfToken = tokens.get(0);
        }

        if (antiCsrfToken == null) {
            return;
        }
        String tokenValue = null;
        try {
            HttpMessage tokenMsg = antiCsrfToken.getMsg().cloneAll();

            httpSender.sendAndReceive(tokenMsg);

            tokenValue = getTokenValue(tokenMsg, antiCsrfToken.getName());

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        if (tokenValue != null) {
            // Replace token value - only supported in the body right now
            if (log.isDebugEnabled()) {
                log.debug(
                        "regenerateAntiCsrfToken replacing {} with {}",
                        antiCsrfToken.getValue(),
                        getURLEncode(tokenValue));
            }
            String replaced = message.getRequestBody().toString();
            replaced =
                    replaced.replace(
                            getURLEncode(antiCsrfToken.getValue()), getURLEncode(tokenValue));
            message.setRequestBody(replaced);
            registerAntiCsrfToken(
                    new AntiCsrfToken(
                            message,
                            antiCsrfToken.getName(),
                            tokenValue,
                            antiCsrfToken.getFormIndex()));
        }
    }

    private static String getURLEncode(String msg) {
        String result = "";
        try {
            result = URLEncoder.encode(msg, "UTF8");
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }
}
