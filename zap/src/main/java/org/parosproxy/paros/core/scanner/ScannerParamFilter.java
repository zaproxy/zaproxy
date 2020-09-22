/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpMessage;

/**
 * Module for parameter filtering according to URL, type and parameter name regexes
 *
 * @author yhawke (2014)
 * @see NameValuePair
 */
public class ScannerParamFilter implements Cloneable {
    private String wildcardedUrl;
    private String paramNameRegex;
    private int paramType;

    private Pattern paramNamePattern;
    private Pattern urlPattern;

    private static final Map<Integer, String> typeMap = new LinkedHashMap<>();

    static {
        typeMap.put(
                NameValuePair.TYPE_UNDEFINED,
                Constant.messages.getString("variant.param.type.all"));
        typeMap.put(
                NameValuePair.TYPE_QUERY_STRING,
                Constant.messages.getString("variant.param.type.query"));
        typeMap.put(
                NameValuePair.TYPE_POST_DATA,
                Constant.messages.getString("variant.param.type.postdata"));
        typeMap.put(
                NameValuePair.TYPE_URL_PATH,
                Constant.messages.getString("variant.param.type.path"));
        typeMap.put(
                NameValuePair.TYPE_HEADER,
                Constant.messages.getString("variant.param.type.header"));
        typeMap.put(
                NameValuePair.TYPE_COOKIE,
                Constant.messages.getString("variant.param.type.cookie"));
        typeMap.put(
                NameValuePair.TYPE_JSON, Constant.messages.getString("variant.param.type.json"));
        typeMap.put(
                NameValuePair.TYPE_GRAPHQL_INLINE,
                Constant.messages.getString("variant.param.type.graphql.inline"));
        typeMap.put(
                NameValuePair.TYPE_MULTIPART_DATA_PARAM,
                Constant.messages.getString("variant.param.type.multipart.dataparam"));
        typeMap.put(
                NameValuePair.TYPE_MULTIPART_DATA_FILE_NAME,
                Constant.messages.getString("variant.param.type.multipart.filename"));
        typeMap.put(
                NameValuePair.TYPE_MULTIPART_DATA_FILE_PARAM,
                Constant.messages.getString("variant.param.type.multipart.fileparam"));
        typeMap.put(
                NameValuePair.TYPE_MULTIPART_DATA_FILE_CONTENTTYPE,
                Constant.messages.getString("variant.param.type.multipart.contenttype"));
    }

    /** Default constructor to initialize default values */
    public ScannerParamFilter() {
        this.wildcardedUrl = "*";
        this.paramType = NameValuePair.TYPE_UNDEFINED;
        this.paramNamePattern = null;
        this.urlPattern = null;
    }

    public ScannerParamFilter(String paramName, int paramType, String urlPattern) {
        this.paramType = paramType;
        this.setParamName(paramName);
        this.setWildcardedUrl(urlPattern);
    }

    public int getType() {
        return paramType;
    }

    public void setType(int paramType) {
        this.paramType = paramType;
    }

    public String getParamName() {
        return paramNameRegex;
    }

    public final void setParamName(String paramNameRegex) {
        this.paramNameRegex = paramNameRegex;
        this.paramNamePattern = Pattern.compile(paramNameRegex);
    }

    public static boolean isValidParamNameRegex(String paramNameRegex) {
        try {
            Pattern.compile(paramNameRegex);
            return true;
        } catch (PatternSyntaxException e) {
            return false;
        }
    }

    public String getWildcardedUrl() {
        return wildcardedUrl;
    }

    public final void setWildcardedUrl(String wildcardedUrl) {
        this.wildcardedUrl = wildcardedUrl;
        if ((wildcardedUrl == null) || wildcardedUrl.equals("*")) {
            this.urlPattern = null;

        } else {
            String wname = wildcardedUrl.toUpperCase(Locale.ROOT);
            wname = Pattern.quote(wname);
            wname = wname.replaceAll("\\?", "\\\\E.\\\\Q");
            wname = wname.replaceAll("\\*", "\\\\E.*\\\\Q");
            wname = wname.replaceAll("\\\\Q\\\\E", "");

            this.urlPattern = Pattern.compile(wname);
        }
    }

    /**
     * Check if the parameter should be excluded by the scanner
     *
     * @param msg the message that is currently under scanning
     * @param param the Value/Name param object that is currently under scanning
     * @return true if the parameter should be excluded
     */
    public boolean isToExclude(HttpMessage msg, NameValuePair param) {
        return ((paramType == NameValuePair.TYPE_UNDEFINED) || (param.getType() == paramType))
                && ((urlPattern == null)
                        || urlPattern
                                .matcher(
                                        msg.getRequestHeader()
                                                .getURI()
                                                .toString()
                                                .toUpperCase(Locale.ROOT))
                                .matches())
                && (paramNamePattern.matcher(param.getName()).matches());
    }

    /**
     * Clone this filter
     *
     * @return a new filter with replicated contents
     */
    @Override
    public ScannerParamFilter clone() {
        return new ScannerParamFilter(paramNameRegex, paramType, wildcardedUrl);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((paramNameRegex == null) ? 0 : paramNameRegex.hashCode());
        result = prime * result + paramType;
        result = prime * result + ((wildcardedUrl == null) ? 0 : wildcardedUrl.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ScannerParamFilter) {
            ScannerParamFilter p = (ScannerParamFilter) obj;
            return ((p.getType() == getType())
                    && p.getWildcardedUrl().equals(getWildcardedUrl())
                    && p.getParamName().equals(getParamName()));

        } else return false;
    }

    public String getTypeString() {
        return typeMap.get(paramType);
    }

    public void setType(String value) {
        int type = NameValuePair.TYPE_UNDEFINED;
        for (Map.Entry<Integer, String> entry : typeMap.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(value)) {
                type = entry.getKey();
                break;
            }
        }

        setType(type);
    }

    public static final Collection<String> getListTypes() {
        return typeMap.values();
    }

    public static final String getStringType(int paramType) {
        return typeMap.get(paramType);
    }

    /**
     * Gets the parameter types (IDs and corresponding name).
     *
     * @return the parameter types.
     * @since 2.6.0
     */
    public static Map<Integer, String> getTypes() {
        return Collections.unmodifiableMap(typeMap);
    }
}
