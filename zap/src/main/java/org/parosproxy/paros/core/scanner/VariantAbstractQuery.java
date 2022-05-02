/*
 *
 * Paros and its related class files.
 *
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2004 Chinotec Technologies Company
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Clarified Artistic License
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Clarified Artistic License for more details.
 *
 * You should have received a copy of the Clarified Artistic License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
// ZAP: 2012/03/15 Changed the methods (the private) setParameter and paramAppend to
// use the class StringBuilder instead of StringBuffer. Removed unnecessary
// castings in the method setParameter. Made a change in the method parse.
// ZAP: 2012/04/25 Added @Override annotation to all appropriate methods.
// ZAP: 2013/05/02 Re-arranged all modifiers into Java coding standard order
// ZAP: 2013/07/02 Changed Vector to ArrayList because obsolete and faster
// ZAP: 2013/08/21 Added a new encoding/decoding model for a correct parameter value interpretation
// ZAP: 2014/01/06 Issue 965: Support 'single page' apps and 'non standard' parameter separators
// ZAP: 2014/02/08 Used the same constants used in ScanParam Target settings
// ZAP: 2016/05/04 Changes to address issues related to ParameterParser
// ZAP: 2016/05/26 Use non-null String for names and values of parameters, scanners might not handle
// null names/values well
// ZAP: 2016/09/13 Issue 2863: Attack query string even if not originally specified
// ZAP: 2017/11/06 Use indexed names for array parameters (Issue 2496).
// ZAP: 2018/09/12 Make the addition of a query parameter optional.
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2022/02/25 Remove code deprecated in 2.5.0
package org.parosproxy.paros.core.scanner;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.mutable.MutableInt;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.model.ParameterParser;
import org.zaproxy.zap.model.StandardParameterParser;

public abstract class VariantAbstractQuery implements Variant {

    private List<NameValuePair> listParam;
    private List<String> originalNames;
    private boolean addQueryParam;

    public VariantAbstractQuery() {
        listParam = Collections.emptyList();
        originalNames = Collections.emptyList();
    }

    /**
     * Build the message content setting the query string according to the Variant specific
     * implementation
     *
     * @param msg the message object we need to modify
     * @param query the query string we need to set inside the message
     */
    protected abstract void buildMessage(HttpMessage msg, String query);

    /**
     * Return escaped mutate of the value. To be overridden by subclass.
     *
     * @param msg
     * @param value
     * @return the escaped value
     */
    protected abstract String getEscapedValue(HttpMessage msg, String value);

    /**
     * Gets parameter's name in encoded/escaped form.
     *
     * <p>Default implementation is to URL encode the name.
     *
     * @param msg the message that contains the parameter
     * @param name the name to escape
     * @return the escaped name
     * @since 2.5.0
     * @see URLEncoder#encode(String, String)
     */
    protected String getEscapedName(HttpMessage msg, String name) {
        return name != null ? AbstractPlugin.getURLEncode(name) : "";
    }

    /**
     * Return unescaped mutate of the value. To be overridden by subclass.
     *
     * @param value
     * @return the unescaped value
     */
    protected abstract String getUnescapedValue(String value);

    /**
     * Sets the given {@code parameters} of the given {@code type} as the list of parameters handled
     * by this variant.
     *
     * <p>The names and values of the parameters are expected to be in decoded form.
     *
     * @param type the type of parameters
     * @param parameters the actual parameters to add
     * @throws IllegalArgumentException if {@code parameters} is {@code null}.
     * @since 2.5.0
     * @see #getParamList()
     * @see NameValuePair#TYPE_QUERY_STRING
     * @see NameValuePair#TYPE_POST_DATA
     */
    protected void setParameters(int type, List<org.zaproxy.zap.model.NameValuePair> parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("Parameter parameters must not be null.");
        }

        int size = parameters.isEmpty() ? 1 : parameters.size();
        listParam = new ArrayList<>(size);
        originalNames = new ArrayList<>(size);

        Map<String, MutableInt> arraysMap = new HashMap<>();
        int i = 0;
        for (org.zaproxy.zap.model.NameValuePair parameter : parameters) {
            String originalName = nonNullString(parameter.getName());
            originalNames.add(originalName);
            String name =
                    isParamArray(originalName)
                            ? getArrayName(originalName, arraysMap)
                            : originalName;
            listParam.add(new NameValuePair(type, name, nonNullString(parameter.getValue()), i));
            i++;
        }
        if (i == 0 && addQueryParam) {
            String param = "query";
            // No query params, lets add one just to make sure
            listParam.add(new NameValuePair(type, param, param, i));
            originalNames.add(param);
        }
    }

    private static String nonNullString(String string) {
        if (string == null) {
            return "";
        }
        return string;
    }

    private String getArrayName(String originalName, Map<String, MutableInt> arraysMap) {
        MutableInt count = arraysMap.get(originalName);
        if (count == null) {
            count = new MutableInt();
            arraysMap.put(originalName, count);
        }
        String arrayName = originalName.substring(0, originalName.length() - 2) + "[" + count + "]";
        count.increment();
        return arrayName;
    }

    private static boolean isParamArray(String name) {
        return name.endsWith("[]");
    }

    @Override
    public List<NameValuePair> getParamList() {
        return Collections.unmodifiableList(listParam);
    }

    /** If name and value = null, not to append entire parameter. */
    @Override
    public String setParameter(
            HttpMessage msg, NameValuePair originalPair, String name, String value) {
        return this.setParameter(msg, originalPair, name, value, false);
    }

    @Override
    public String setEscapedParameter(
            HttpMessage msg, NameValuePair originalPair, String name, String value) {
        return this.setParameter(msg, originalPair, name, value, true);
    }

    private String setParameter(
            HttpMessage msg,
            NameValuePair originalPair,
            String name,
            String value,
            boolean escaped) {
        // We need the correct parameter parser to use the right separators
        ParameterParser parser;
        if (originalPair.getType() == NameValuePair.TYPE_POST_DATA) {
            parser =
                    Model.getSingleton()
                            .getSession()
                            .getFormParamParser(msg.getRequestHeader().getURI().toString());

        } else if (originalPair.getType() == NameValuePair.TYPE_QUERY_STRING) {
            parser =
                    Model.getSingleton()
                            .getSession()
                            .getUrlParamParser(msg.getRequestHeader().getURI().toString());
        } else {
            parser = new StandardParameterParser();
        }

        StringBuilder sb = new StringBuilder();
        String encodedValue = (escaped) ? value : getEscapedValue(msg, value);
        NameValuePair pair;
        boolean isAppended;

        for (int i = 0; i < getParamList().size(); i++) {
            pair = getParamList().get(i);
            String origName = originalNames.get(i);
            if (i == originalPair.getPosition()) {
                isAppended =
                        paramAppend(
                                sb,
                                getEscapedName(msg, pair.getName() == name ? origName : name),
                                encodedValue,
                                parser);

            } else {
                isAppended =
                        paramAppend(
                                sb,
                                getEscapedName(msg, origName),
                                getEscapedValue(msg, pair.getValue()),
                                parser);
            }

            if (isAppended && i < getParamList().size() - 1) {
                sb.append(parser.getDefaultKeyValuePairSeparator());
            }
        }

        if (sb.length() == 0) {
            // No original query string
            sb.append(encodedValue);
        }
        String query = sb.toString();
        buildMessage(msg, query);
        return query;
    }

    /**
     * Set the name value pair into the StringBuilder. If both name and value is null, not to append
     * whole parameter.
     *
     * @param sb
     * @param name Null = not to append parameter.
     * @param value null = not to append parameter value.
     * @return true = parameter changed.
     */
    private boolean paramAppend(
            StringBuilder sb, String name, String value, ParameterParser parser) {
        boolean isEdited = false;

        if (name != null) {
            sb.append(name);
            isEdited = true;
        }

        if (value != null) {
            sb.append(parser.getDefaultKeyValueSeparator());
            sb.append(value);
            isEdited = true;
        }

        return isEdited;
    }

    public void setAddQueryParam(boolean addQueryParam) {
        this.addQueryParam = addQueryParam;
    }
}
