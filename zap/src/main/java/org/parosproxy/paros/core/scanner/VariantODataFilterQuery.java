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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.network.HttpMessage;

/**
 * Specialized variant able to handle the filter parameters of OData URIs (focused on OData v2)
 *
 * <p>Example of query:<br>
 * http://services.odata.org/OData/OData.svc/Product?$filter=startswith(name,'Foo') and price lt 10
 *
 * <p>References:
 *
 * <ul>
 *   <li>http://www.odata.org/documentation/uri-conventions
 *   <li>http://msdn.microsoft.com/en-us/library/gg309461.aspx#BKMK_filter
 * </ul>
 *
 * TODO:<br>
 * - Properly handle escaped vs. unescaped parameters<br>
 * - Handle OData functions (startwith, substringof, ...)<br>
 */
public class VariantODataFilterQuery implements Variant {

    private static final Logger LOG = LogManager.getLogger(VariantODataFilterQuery.class);

    // Extract the content of the $filter parameter
    private static final Pattern patternFilterParameters =
            Pattern.compile("\\$filter[ ]*=[ ]*([\\w\\s()',./\\-:]*)");

    // Extract the effective parameters from the $filter string
    // TODO: Support complex expressions
    private static final Pattern patternParameters =
            Pattern.compile("([\\w]+)\\s+(eq|ne|gt|ge|lt|le|and|or|not)\\s+([\\w'/]+)");

    // Store the URI parts located before and after the filter expression
    private String beforeFilterExpression = null;
    private String afterFilterExpression = null;

    /** Storage for the operation parameters */
    private Map<String, OperationParameter> mapParameters = Collections.emptyMap();

    private static final String SHORT_NAME = "odatafilter";

    @Override
    public String getShortName() {
        return SHORT_NAME;
    }

    @Override
    public void setMessage(HttpMessage msg) {
        URI uri = msg.getRequestHeader().getURI();
        parse(uri);
    }

    private void parse(URI uri) {
        try {
            String query = uri.getQuery();

            // Detection of a filter statement if any
            if (query != null) {

                Matcher matcher = patternFilterParameters.matcher(query);
                if (matcher.find()) {
                    String filterExpression = "";

                    filterExpression = matcher.group(1);

                    int begin = query.indexOf(filterExpression);
                    int end = begin + filterExpression.length();

                    beforeFilterExpression = query.substring(0, begin);
                    afterFilterExpression = query.substring(end);

                    // Now scan the expression in order to identify all parameters
                    mapParameters = new HashMap<>();

                    Matcher matcherParameters = patternParameters.matcher(filterExpression);
                    while (matcherParameters.find()) {

                        String nameOpAndValue = matcherParameters.group(0);
                        String paramName = matcherParameters.group(1);
                        String operator = matcherParameters.group(2);
                        String paramValue = matcherParameters.group(3);

                        begin = filterExpression.indexOf(nameOpAndValue);
                        end = begin + nameOpAndValue.length();

                        String before = filterExpression.substring(0, begin);
                        String after = filterExpression.substring(end);

                        OperationParameter opParam =
                                new OperationParameter(
                                        paramName, operator, paramValue, before, after);
                        mapParameters.put(opParam.getParameterName(), opParam);
                    }

                } else {
                    beforeFilterExpression = null;
                    afterFilterExpression = null;
                    mapParameters = Collections.emptyMap();
                }

            } else {
                beforeFilterExpression = null;
                afterFilterExpression = null;
                mapParameters = Collections.emptyMap();
            }

        } catch (URIException e) {
            LOG.error("{} {}", e.getMessage(), uri, e);
        }
    }

    @Override
    public Vector<NameValuePair> getParamList() {
        Vector<NameValuePair> out = new Vector<>(mapParameters.values().size());

        int i = 1;
        for (OperationParameter opParam : mapParameters.values()) {
            out.add(
                    new NameValuePair(
                            NameValuePair.TYPE_QUERY_STRING,
                            opParam.getParameterName(),
                            opParam.getValue(),
                            i++));
        }

        return out;
    }

    @Override
    public String setParameter(
            HttpMessage msg, NameValuePair originalPair, String param, String value) {
        // TODO: Implement correctly escaped / non-escaped params

        OperationParameter opParam = mapParameters.get(param);
        if (opParam != null) {
            String newfilter = opParam.getModifiedFilter(value);
            String modifiedQuery = beforeFilterExpression + newfilter + afterFilterExpression;

            try {
                msg.getRequestHeader().getURI().setQuery(modifiedQuery);

            } catch (URIException | NullPointerException e) {
                LOG.error("Exception with the modified query {}", modifiedQuery, e);
            }

            return newfilter;
        }

        return null;
    }

    @Override
    public String setEscapedParameter(
            HttpMessage msg, NameValuePair originalPair, String param, String value) {
        // TODO: Implement correctly escaped / non-escaped params
        return setParameter(msg, originalPair, param, value);
    }

    /** Store a parameter and related data */
    static class OperationParameter {

        private String paramName;
        private String operator;
        private String originalValue;
        private String stringBeforeOperation;
        private String stringAfterOperation;

        /**
         * Constructs an {@code OperationParameter} with the given name, operator, value and
         * surrounding strings.
         *
         * @param name the name
         * @param operator the operator
         * @param value the value
         * @param stringBeforeOperation the string before the operation (parameter + operator +
         *     value)
         * @param stringAfterOperation the string after the operation (parameter + operator + value)
         */
        public OperationParameter(
                String name,
                String operator,
                String value,
                String stringBeforeOperation,
                String stringAfterOperation) {
            super();

            this.paramName = name;
            this.operator = operator;
            this.originalValue = value;
            this.stringBeforeOperation = stringBeforeOperation;
            this.stringAfterOperation = stringAfterOperation;
        }

        /**
         * Gets the value of the parameter.
         *
         * @return the value of the parameter
         */
        public String getValue() {
            return this.originalValue;
        }

        public String getParameterName() {
            return this.paramName;
        }

        public String getModifiedFilter(String newIdValue) {
            StringBuilder builder = new StringBuilder();
            builder.append(this.stringBeforeOperation)
                    .append(this.paramName)
                    .append(' ')
                    .append(this.operator)
                    .append(' ')
                    .append(newIdValue)
                    .append(this.stringAfterOperation);
            return builder.toString();
        }
    }
}
