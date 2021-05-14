/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2019 The ZAP Development Team
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
package org.zaproxy.zap.extension.custompages;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.utils.Enableable;

public class DefaultCustomPage extends Enableable implements CustomPage {

    /**
     * The Constant FIELD_SEPARATOR used for separating DefaultCustomPage's fields during
     * encode/decode operations.
     */
    private static final String FIELD_SEPARATOR = ";";

    private static final Logger LOGGER = LogManager.getLogger(DefaultCustomPage.class);

    private int contextId;
    private String pageMatcher;
    private CustomPageMatcherLocation pageMatcherLocation;
    private boolean regex;
    private Type type;
    private Pattern pattern;

    /**
     * Constructs a {@code DefaultCustomPage} with the given details.
     *
     * @param contextId the context ID for which the {@code DefaultCustomPage} is being created
     * @param pageMatcher the page matcher of the {@code DefaultCustomPage}
     * @param pageMatcherLocation the {@link CustomPageMatcherLocation} of the {@code
     *     DefaultCustomPage}
     * @param regex a boolean specifying whether or not the {@code DefaultCustomPage} page matcher
     *     is represented by a Regex pattern
     * @param type the {@link CustomPage.Type} of the {@code DefaultCustomPage}
     * @param enabled a boolean specifying whether or not the {@code DefaultCustomPage} is enabled
     *     or not
     */
    public DefaultCustomPage(
            int contextId,
            String pageMatcher,
            CustomPageMatcherLocation pageMatcherLocation,
            boolean regex,
            Type type,
            boolean enabled) {
        super();
        this.contextId = contextId;
        this.pageMatcher = pageMatcher;
        this.pageMatcherLocation = pageMatcherLocation;
        this.regex = regex;
        this.type = type;
        this.setEnabled(enabled);
    }

    /**
     * Constructs a {@code DefaultCustomPage} with the given details. {@link CustomPage.Type} and
     * {@link CustomPageMatcherLocation} are specified by {@code int} ID instead of {@code enum}
     * literal.
     *
     * @param contextId the context ID for which the {@code DefaultCustomPage} is being created
     * @param pageMatcher the page matcher of the {@code DefaultCustomPage}
     * @param pageMatcherLocationID the ID of the {@link CustomPageMatcherLocation} of the {@code
     *     DefaultCustomPage}
     * @param regex a boolean specifying whether or not the {@code DefaultCustomPage} page matcher
     *     is represented by a Regex pattern
     * @param type the {@code CustomPage.Type} of the {@code DefaultCustomPage}
     * @param enabled a boolean specifying whether or not the {@code DefaultCustomPage} is enabled
     *     or not
     */
    public DefaultCustomPage(
            int contextId,
            String pageMatcher,
            int pageMatcherLocationID,
            boolean regex,
            Type type,
            boolean enabled) {
        super();
        this.contextId = contextId;
        this.pageMatcher = pageMatcher;
        this.pageMatcherLocation =
                CustomPageMatcherLocation.getCustomPagePageMatcherLocationWithId(
                        pageMatcherLocationID);
        this.regex = regex;
        this.type = type;
        this.setEnabled(enabled);
    }

    @Override
    public int getContextId() {
        return contextId;
    }

    @Override
    public void setContextId(int contextId) {
        this.contextId = contextId;
    }

    @Override
    public String getPageMatcher() {
        return pageMatcher;
    }

    @Override
    public void setPageMatcher(String pageMatcher) {
        this.pageMatcher = pageMatcher;
    }

    @Override
    public CustomPageMatcherLocation getPageMatcherLocation() {
        return pageMatcherLocation;
    }

    @Override
    public void setPageMatcherLocation(CustomPageMatcherLocation cppmt) {
        this.pageMatcherLocation = cppmt;
    }

    @Override
    public boolean isRegex() {
        return regex;
    }

    @Override
    public void setRegex(boolean regex) {
        this.regex = regex;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public void setType(Type cpt) {
        this.type = cpt;
    }

    /**
     * Determines if a {@code HttpMessage} is a Custom Page of a particular {@code CustomPage.Type}.
     *
     * @param msg the HTTP message to be evaluated
     * @param cpt the CustomPage.Type of the Custom Pages against which the HTTP message should be
     *     evaluated
     * @return {@code true} if the HTTP message is a Custom Page of the type in question, {@code
     *     false} otherwise
     */
    @Override
    public boolean isCustomPage(HttpMessage msg, Type cpt) {
        if (isEnabled() && getType() == cpt) {
            String value = getPageMatcherByType(msg);
            return matchByLocation(value);
        }
        return false; // Default
    }

    private String getPageMatcherByType(HttpMessage msg) {
        if (CustomPageMatcherLocation.URL.equals(getPageMatcherLocation())) {
            return msg.getRequestHeader().getURI().toString();
        } else if (CustomPageMatcherLocation.RESPONSE_CONTENT.equals(getPageMatcherLocation())) {
            return getHttpMessageAsString(msg);
        }
        LOGGER.error(
                "Could not get page matcher for the given message, with: {}",
                getPageMatcherLocation());
        return "";
    }

    private boolean matchByLocation(String value) {
        if (isRegex()) {
            return isRegexMatch(pageMatcher, value);
        }
        return CustomPageMatcherLocation.URL.equals(getPageMatcherLocation())
                ? value.equals(getPageMatcher())
                : value.contains(getPageMatcher());
    }

    private static String getHttpMessageAsString(HttpMessage msg) {
        return msg.getResponseHeader().toString() + msg.getResponseBody().toString();
    }

    private boolean isRegexMatch(String pageMatcher, String toMatch) {
        if (pattern == null) {
            pattern = Pattern.compile(pageMatcher);
        }

        return pattern.matcher(toMatch).find();
    }

    @Override
    public String toString() {
        StringBuilder cp = new StringBuilder();
        cp.append("ContextId: ").append(this.getContextId());
        cp.append(", Content: ").append(this.getPageMatcher()).append(".");
        cp.append(", Content Type: ").append(this.getPageMatcherLocation().getName());
        cp.append(", Is RegEx: ").append(this.isRegex());
        cp.append(", Type: ").append(this.getType().getName());
        cp.append(", IsEnabled: ").append(this.isEnabled());
        return cp.toString();
    }

    /**
     * Encodes the DefaultCustomPage in a String. Fields that contain strings are Base64 encoded.
     *
     * @param cp the custom page to be encoded
     * @return the encoded string
     */
    public static String encode(CustomPage cp) {
        StringBuilder encodedCP = new StringBuilder();
        String matcherComponent = cp.getPageMatcher() != null ? cp.getPageMatcher() : "";

        encodedCP
                .append(
                        new String(
                                Base64.getEncoder()
                                        .encode(matcherComponent.getBytes(StandardCharsets.UTF_8)),
                                StandardCharsets.US_ASCII))
                .append(FIELD_SEPARATOR);
        encodedCP.append(cp.getPageMatcherLocation().getId()).append(FIELD_SEPARATOR);
        encodedCP.append(cp.isRegex()).append(FIELD_SEPARATOR);
        encodedCP.append(cp.getType().getId()).append(FIELD_SEPARATOR);
        encodedCP.append(cp.isEnabled()).append(FIELD_SEPARATOR);

        return encodedCP.toString();
    }

    /**
     * Decodes a DefaultCustomPage from an encoded string. The string provided as input should have
     * been obtained through calls to {@link #encode(CustomPage)}.
     *
     * @param contextId the ID of the context for which the encoded ({@code DefaultCustomPage})
     *     string is being decoded
     * @param encodedString the encoded string
     * @return the DefaultCustomPage
     */
    protected static DefaultCustomPage decode(int contextId, String encodedString) {
        String[] pieces = encodedString.split(FIELD_SEPARATOR, -1);
        DefaultCustomPage defaultCustomPage = null;
        try {
            defaultCustomPage =
                    new DefaultCustomPage(
                            contextId, // ContextID
                            new String(
                                    Base64.getDecoder().decode(pieces[0]),
                                    StandardCharsets.UTF_8), // Content
                            CustomPageMatcherLocation.getCustomPagePageMatcherLocationWithId(
                                    Integer.parseInt(pieces[1])), // Content
                            // Type
                            Boolean.parseBoolean(pieces[2]), // IsRegex
                            Type.getCustomPageTypeWithId(Integer.parseInt(pieces[3])), // Type
                            Boolean.parseBoolean(pieces[4])); // Enabled
        } catch (Exception ex) {
            LOGGER.error(
                    "An error occured while decoding DefaultCustomPage from: {}",
                    encodedString,
                    ex);
            return null;
        }
        return defaultCustomPage;
    }
}
