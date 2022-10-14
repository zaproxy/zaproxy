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

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.network.HttpMessage;

/**
 * Specialized variant able to handles OData URIs for the resource ID part
 *
 * <p>It's focused on OData v2
 *
 * <p>Example of query having a single unnamed id:<br>
 * {@literal http://services.odata.org/OData/OData.svc/Category(1)/Products?$top=2&$orderby=name}
 *
 * <p>Example of query having a composite (named) id:<br>
 * http://services.odata.org/OData/OData.svc/DisplayItem(key1=2L,key2='B0EB1CA')
 *
 * <p>Reference: <br>
 * http://www.odata.org/documentation/uri-conventions
 */
public class VariantODataIdQuery implements Variant {

    private static final Logger LOG = LogManager.getLogger(VariantODataIdQuery.class);

    /** In order to identify the unnamed id we add this prefix to the resource name * */
    public static final String RESOURCE_ID_PREFIX = "__ID__";

    /**
     * It's optional to have a resource parameter Set it to null of there is no such parameter in
     * the URI
     */
    private ResourceParameter resourceParameter = null;

    // Extract the ID of a resource including the surrounding quote
    // First group is the resource_name
    // Second group is the ID (quote will be taken as part of the value)
    private static final Pattern patternResourceIdentifierUnquoted =
            Pattern.compile("/(\\w*)\\(([\\w']*)\\)");

    // Detect a section containing a composite IDs
    private static final Pattern patternResourceMultipleIdentifier =
            Pattern.compile("/\\w*\\((.*)\\)");

    // Extract the detail of the multiples IDs
    private static final Pattern patternResourceMultipleIdentifierDetail =
            Pattern.compile("(\\w*)=([\\w']*)");

    // Not very clean, should be improved.
    // Save part of the URI before and after the section containing the composite IDs
    private String beforeMultipleIDs = null;
    private String afterMultipleIDs = null;

    // Store the composite IDs if any
    private List<NameValuePair> listParams = null;

    private static final String SHORT_NAME = "odataid";

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
            resourceParameter = null;

            beforeMultipleIDs = null;
            afterMultipleIDs = null;
            listParams = null;

            String path = uri.getPath();

            if (path != null) {

                // Detection of the resource and resource id (if any)
                String resourceName = "";
                String resourceID;

                // check for single ID (unnamed)
                Matcher matcher = patternResourceIdentifierUnquoted.matcher(path);
                if (matcher.find()) {
                    resourceName = matcher.group(1);
                    resourceID = matcher.group(2);

                    String subString = resourceName + "(" + resourceID + ")";
                    int begin = path.indexOf(subString);
                    int end = begin + subString.length();

                    String beforeSubstring = path.substring(0, begin);
                    String afterSubstring = path.substring(end);

                    resourceParameter =
                            new ResourceParameter(
                                    resourceName, resourceID, beforeSubstring, afterSubstring);

                } else {

                    matcher = patternResourceMultipleIdentifier.matcher(path);
                    if (matcher.find()) {
                        // We've found a composite identifier. i.e: /Resource(field1=a,field2=3)

                        String multipleIdentifierSection = matcher.group(1);

                        int begin = path.indexOf(multipleIdentifierSection);
                        int end = begin + multipleIdentifierSection.length();

                        beforeMultipleIDs = path.substring(0, begin);
                        afterMultipleIDs = path.substring(end);

                        listParams = new ArrayList<>();

                        matcher =
                                patternResourceMultipleIdentifierDetail.matcher(
                                        multipleIdentifierSection);
                        int i = 1;
                        while (matcher.find()) {

                            String paramName = matcher.group(1);
                            String value = matcher.group(2);

                            NameValuePair vp =
                                    new NameValuePair(
                                            NameValuePair.TYPE_QUERY_STRING, paramName, value, i++);
                            listParams.add(vp);
                        }
                    }
                }
            }

        } catch (URIException e) {
            LOG.error("{} {}", e.getMessage(), uri, e);
        }
    }

    @Override
    public Vector<NameValuePair> getParamList() {
        Vector<NameValuePair> params = new Vector<>();

        if (resourceParameter != null) {
            params.add(
                    new NameValuePair(
                            NameValuePair.TYPE_QUERY_STRING,
                            resourceParameter.getParameterName(),
                            resourceParameter.getValue(),
                            1));
        }

        if (listParams != null) {
            params.addAll(listParams);
        }

        return params;
    }

    @Override
    public String setParameter(
            HttpMessage msg, NameValuePair originalPair, String param, String value) {
        // TODO: Implement correctly escaped vs. non-escaped params

        // Check if the parameter is a resource parameter
        if (resourceParameter != null && resourceParameter.getParameterName().equals(param)) {

            String query = value;
            String modifiedPath = resourceParameter.getModifiedPath(value);

            try {
                msg.getRequestHeader().getURI().setPath(modifiedPath);

            } catch (URIException e) {
                throw new RuntimeException("Error with uri " + modifiedPath, e);

            } catch (NullPointerException e) {
                throw new RuntimeException("Error with uri " + modifiedPath, e);
            }

            return query;

        } else if (listParams != null) {
            // Check for composite ID
            StringBuilder sb = new StringBuilder();
            StringBuilder sbQuery = new StringBuilder();

            sb.append(beforeMultipleIDs);

            boolean firstPass = true;
            for (NameValuePair nv : listParams) {
                if (firstPass) {
                    firstPass = false;

                } else {
                    sbQuery.append(",");
                }

                sbQuery.append(nv.getName()).append("=");

                if (nv.getName().equals(param)) {
                    sbQuery.append(value);

                } else {
                    sbQuery.append(nv.getValue());
                }
            }

            sb.append(sbQuery);
            sb.append(afterMultipleIDs);

            String path = sb.toString();
            String query = sbQuery.toString();

            try {
                msg.getRequestHeader().getURI().setPath(path);

            } catch (URIException | NullPointerException e) {
                throw new RuntimeException("Error with uri " + path, e);
            }

            return query;
        }

        return "";
    }

    @Override
    public String setEscapedParameter(
            HttpMessage msg, NameValuePair originalPair, String param, String value) {
        // TODO: Implement correctly escaped vs. non-escaped params
        return setParameter(msg, originalPair, param, value);
    }

    /** Store the ID of a resource and related data */
    static class ResourceParameter {

        private String parameterName;
        private String resourceName;
        private String originalValue;
        private String pathBeforeParameter;
        private String pathAfterParameter;

        /**
         * @param resourceName
         * @param originalValue
         * @param pathBeforeParameter
         * @param pathAfterParameter
         */
        public ResourceParameter(
                String resourceName,
                String originalValue,
                String pathBeforeParameter,
                String pathAfterParameter) {
            super();

            this.resourceName = resourceName;
            this.parameterName = RESOURCE_ID_PREFIX + resourceName;
            this.originalValue = originalValue;
            this.pathBeforeParameter = pathBeforeParameter;
            this.pathAfterParameter = pathAfterParameter;
        }

        /** @return */
        public String getValue() {
            return this.originalValue;
        }

        public String getParameterName() {
            return this.parameterName;
        }

        public String getModifiedPath(String newIdValue) {
            StringBuilder builder = new StringBuilder();
            builder.append(this.pathBeforeParameter)
                    .append(this.resourceName)
                    .append("(")
                    .append(newIdValue)
                    .append(")")
                    .append(this.pathAfterParameter);

            return builder.toString();
        }
    }
}
