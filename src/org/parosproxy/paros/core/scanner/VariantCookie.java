/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2013 The ZAP Development team
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

package org.parosproxy.paros.core.scanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;

/**
 * A {@code Variant} for Cookie headers, allowing to attack the names and values of the cookies.
 * 
 * @author andy
 * @see Variant
 */
public class VariantCookie implements Variant {

    private List<NameValuePair> params = Collections.emptyList();

    /**
     * @throws IllegalArgumentException if {@code message} is {@code null}.
     */
    @Override
    public void setMessage(HttpMessage message) {
        if (message == null) {
            throw new IllegalArgumentException("Parameter message must not be null.");
        }

        Vector<String> cookieLines = message.getRequestHeader().getHeaders(HttpHeader.COOKIE);
        if (cookieLines == null) {
            params = Collections.emptyList();
            return;
        }

        ArrayList<NameValuePair> extractedParameters = new ArrayList<>();
        for (String cookieLine : cookieLines) {
            if (cookieLine.trim().isEmpty()) {
                continue;
            }

            String[] cookieArray = cookieLine.split("; ?");
            for (String cookie : cookieArray) {
                String[] nameValuePair = cookie.split("=", 2);
                String name = nameValuePair[0];
                String value = null;
                if (nameValuePair.length == 2) {
                    value = getUnescapedValue(nameValuePair[1]);
                }
                extractedParameters.add(new NameValuePair(NameValuePair.TYPE_COOKIE, name, value, extractedParameters.size()));
            }
        }

        if (extractedParameters.isEmpty()) {
            params = Collections.emptyList();
        } else {
            extractedParameters.trimToSize();
            params = Collections.unmodifiableList(extractedParameters);
        }
    }

    /**
     * Encodes the given {@code value}.
     * 
     * @param value the value that needs to be encoded, must not be {@code null}.
     * @return the encoded value
     */
    private static String getEscapedValue(String value) {
        return AbstractPlugin.getURLEncode(value);
    }
    
    /**
     * Decodes the given {@code value}.
     * 
     * @param value the value that needs to be decoded, must not be {@code null}.
     * @return the decoded value
     */
    private String getUnescapedValue(String value) {
        return AbstractPlugin.getURLDecode(value);
    }
    
    /**
     * Gets the list of parameters (that is, cookies) extracted from the request header of the message.
     * 
     * @return an unmodifiable {@code List} containing the extracted parameters, never {@code null}.
     */
    @Override
    public List<NameValuePair> getParamList() {
        return params;
    }

    /**
     * 
     * @param msg
     * @param originalPair
     * @param name
     * @param value
     * @return 
     */
    @Override
    public String setParameter(HttpMessage msg, NameValuePair originalPair, String name, String value) {
    	return setParameter(msg, originalPair, name, value, false);
    }
    
    /**
     * 
     * @param msg
     * @param originalPair
     * @param name
     * @param value
     * @return 
     */
    @Override
    public String setEscapedParameter(HttpMessage msg, NameValuePair originalPair, String name, String value) {
    	return setParameter(msg, originalPair, name, value, true);
    }
    
    /**
     * 
     * @param msg
     * @param originalPair
     * @param name
     * @param value
     * @param escaped
     * @return 
     */
    private String setParameter(HttpMessage msg, NameValuePair originalPair, String name, String value, boolean escaped) {        
        String escapedValue = value == null ? null : escaped ? value : getEscapedValue(value);
        StringBuilder cookieString = new StringBuilder();
        for (int idx = 0; idx < params.size(); idx++) {
            String cookieName = null;
            String cookieValue = null;
            if (idx == originalPair.getPosition()) {
                if (!(name == null && escapedValue == null)) {
                    cookieName = name;
                    if (escapedValue != null) {
                        cookieValue = escapedValue;
                    }
                }
            } else {
                NameValuePair param = params.get(idx);
                cookieName = param.getName();
                cookieValue = param.getValue();
                if (cookieValue != null) {
                    cookieValue = getEscapedValue(cookieValue);
                }
            }

            if (cookieString.length() != 0 && !(cookieName == null && cookieValue == null)) {
                cookieString.append("; ");
            }

            if (cookieName != null) {
                cookieString.append(cookieName);
            }

            if (cookieValue != null) {
                cookieString.append('=');
                cookieString.append(cookieValue);
            }
        }

        msg.getRequestHeader().setHeader(HttpHeader.COOKIE, null);
        if (cookieString.length() != 0) {
            msg.getRequestHeader().setHeader(HttpHeader.COOKIE, cookieString.toString());
        }

        if (escapedValue == null) {
            return name;
        }

        if (name == null) {
            return "=" + escapedValue;
        }

        return name + "=" + escapedValue;
    }    
}
