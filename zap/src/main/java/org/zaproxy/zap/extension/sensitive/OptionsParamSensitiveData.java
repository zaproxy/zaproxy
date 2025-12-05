/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2024 The ZAP Development Team
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
package org.zaproxy.zap.extension.sensitive;

import org.parosproxy.paros.common.AbstractParam;

/**
 * Manages the sensitive data masking configurations saved in the configuration file.
 *
 * <p>It allows to change, programmatically, the following sensitive data options:
 *
 * <ul>
 *   <li>Enabled - whether sensitive data masking is enabled.
 *   <li>Headers to Mask - comma-separated list of HTTP header names to mask.
 *   <li>Body Fields to Mask - comma-separated list of body field names to mask.
 * </ul>
 */
public class OptionsParamSensitiveData extends AbstractParam {

    /** The base configuration key for all sensitive data configurations. */
    private static final String PARAM_BASE_KEY = "sensitivedata";

    /** The configuration key for the enabled option. */
    private static final String PARAM_ENABLED = PARAM_BASE_KEY + ".enabled";

    /** The configuration key for the headers to mask. */
    private static final String PARAM_HEADERS_TO_MASK = PARAM_BASE_KEY + ".headers";

    /** The configuration key for the body fields to mask. */
    private static final String PARAM_BODY_FIELDS_TO_MASK = PARAM_BASE_KEY + ".bodyfields";

    private static final boolean DEFAULT_ENABLED = false;
    private static final String DEFAULT_HEADERS_TO_MASK = "";
    private static final String DEFAULT_BODY_FIELDS_TO_MASK = "";

    /**
     * Whether sensitive data masking is enabled. Default is {@code false}.
     */
    private boolean enabled;

    /** The comma-separated list of HTTP header names to mask. */
    private String headersToMask;

    /** The comma-separated list of body field names to mask. */
    private String bodyFieldsToMask;

    public OptionsParamSensitiveData() {
        super();
        enabled = DEFAULT_ENABLED;
        headersToMask = DEFAULT_HEADERS_TO_MASK;
        bodyFieldsToMask = DEFAULT_BODY_FIELDS_TO_MASK;
    }

    /**
     * Parses the sensitive data options.
     *
     * <p>The following sensitive data options are parsed:
     *
     * <ul>
     *   <li>Enabled - whether sensitive data masking is enabled.
     *   <li>Headers to Mask - comma-separated list of HTTP header names to mask.
     *   <li>Body Fields to Mask - comma-separated list of body field names to mask.
     * </ul>
     */
    @Override
    protected void parse() {
        enabled = getBoolean(PARAM_ENABLED, DEFAULT_ENABLED);
        headersToMask = getString(PARAM_HEADERS_TO_MASK, DEFAULT_HEADERS_TO_MASK);
        bodyFieldsToMask = getString(PARAM_BODY_FIELDS_TO_MASK, DEFAULT_BODY_FIELDS_TO_MASK);
    }

    /**
     * Tells whether sensitive data masking is enabled or not.
     *
     * @return {@code true} if sensitive data masking is enabled, {@code false} otherwise
     * @see #setEnabled(boolean)
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether sensitive data masking is enabled or not.
     *
     * @param enabled {@code true} if sensitive data masking should be enabled, {@code false}
     *     otherwise
     * @see #isEnabled()
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        getConfig().setProperty(PARAM_ENABLED, enabled);
    }

    /**
     * Gets the comma-separated list of HTTP header names to mask.
     *
     * @return the comma-separated list of HTTP header names to mask
     */
    public String getHeadersToMask() {
        return headersToMask;
    }

    /**
     * Sets the comma-separated list of HTTP header names to mask.
     *
     * @param headersToMask the comma-separated list of HTTP header names to mask
     */
    public void setHeadersToMask(String headersToMask) {
        this.headersToMask = headersToMask;
        getConfig().setProperty(PARAM_HEADERS_TO_MASK, headersToMask);
    }

    /**
     * Gets the comma-separated list of body field names to mask.
     *
     * @return the comma-separated list of body field names to mask
     */
    public String getBodyFieldsToMask() {
        return bodyFieldsToMask;
    }

    /**
     * Sets the comma-separated list of body field names to mask.
     *
     * @param bodyFieldsToMask the comma-separated list of body field names to mask
     */
    public void setBodyFieldsToMask(String bodyFieldsToMask) {
        this.bodyFieldsToMask = bodyFieldsToMask;
        getConfig().setProperty(PARAM_BODY_FIELDS_TO_MASK, bodyFieldsToMask);
    }
}

