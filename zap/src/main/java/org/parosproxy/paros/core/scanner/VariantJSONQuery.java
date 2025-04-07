/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2013 The ZAP Development Team
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
package org.parosproxy.paros.core.scanner;

import org.apache.commons.text.StringEscapeUtils;

/**
 * @author andy
 */
public class VariantJSONQuery extends VariantAbstractRPCQuery {

    public static final String JSON_RPC_CONTENT_TYPE = "application/json";

    public static final int NAME_SEPARATOR = ':';
    public static final int VALUE_SEPARATOR = ',';
    public static final int BEGIN_ARRAY = '[';
    public static final int QUOTATION_MARK = '"';
    public static final int BEGIN_OBJECT = '{';
    public static final int END_OBJECT = '}';
    public static final int END_ARRAY = ']';
    public static final int BACKSLASH = '\\';

    private boolean scanNullValues;

    private static final String SHORT_NAME = "json";

    @Override
    public String getShortName() {
        return SHORT_NAME;
    }

    public VariantJSONQuery() {
        super(NameValuePair.TYPE_JSON);
    }

    /**
     * Sets whether or not to scan null values.
     *
     * <p>The null values are handled as if they were strings, that is, the payload injected is a
     * string.
     *
     * @param scan {@code true} if null values should be scanned, {@code false} otherwise.
     * @since 2.11.0
     * @see #isScanNullValues()
     */
    public void setScanNullValues(boolean scan) {
        scanNullValues = scan;
    }

    /**
     * Tells whether or not to scan null values.
     *
     * @return {@code true} if null values should be scanned, {@code false} otherwise.
     * @see #setScanNullValues(boolean)
     */
    public boolean isScanNullValues() {
        return scanNullValues;
    }

    @Override
    public boolean isValidContentType(String contentType) {
        return contentType.startsWith(JSON_RPC_CONTENT_TYPE);
    }

    @Override
    public void parseContent(String content) {
        JsonParamParser jpp = new JsonParamParser(content, scanNullValues);
        jpp.getParameters().forEach(p -> addParameter(p));
    }

    @Override
    public String getEscapedValue(String value, boolean toQuote) {
        String result = StringEscapeUtils.escapeJava(value);
        return (toQuote) ? (char) QUOTATION_MARK + result + (char) QUOTATION_MARK : result;
    }

    @Override
    public String getUnescapedValue(String value) {
        return StringEscapeUtils.unescapeJava(value);
    }
}
