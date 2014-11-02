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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;

/**
 * Variant class used for Multipart Form-Data POST request handling.
 * It takes all parameters passed inside the form-data structure
 * and set them for the injection
 * 
 * @author andy
 */
public class VariantMultipartFormQuery implements Variant {

    private final List<NameValuePair> stringParam = new ArrayList<>();
    private final List<MultipartParam> fileParam = new ArrayList<>();
    private String boundary = null;

    /**
     * 
     * @param msg 
     */
    @Override
    public void setMessage(HttpMessage msg) {
        // First check if it's a multipart form-data request
        // Otherwise give back an empty param list
        String contentType = msg.getRequestHeader().getHeader(HttpHeader.CONTENT_TYPE);        
        if (contentType == null || !contentType.startsWith(MultipartFormParser.WWW_MULTIPART_FORM_DATA)) {
            return;
        }
            
        try {
            MultipartFormParser parser = new MultipartFormParser(msg);
            MultipartParam param;
            int index = 0;
            
            boundary = parser.getBoundary();
            
            while ((param = parser.getNextParam()) != null) {
                if (param.getFileName() == null) {
                    // This is a parameter, add it to the vector of values
                    stringParam.add(new NameValuePair(NameValuePair.TYPE_POST_DATA, 
                    		param.getName(), param.getContent(), index++));

                } else {
                    // This is a file
                    fileParam.add(param);
                }                
            }
            
        } catch (IOException ex) {}
    }

    /**
     *
     * @return
     */
    @Override
    public List<NameValuePair> getParamList() {
        return stringParam;
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
    	StringBuilder sb = new StringBuilder();
        NameValuePair pair;
        
        // First set parameters
        for (int i = 0; i < stringParam.size(); i++) {
            pair = stringParam.get(i);

            // First write the boundary --boundary
            sb.append(boundary);
            sb.append(HttpHeader.CRLF);
            sb.append("Content-Disposition: form-data; name=\"");
            
            if (i == originalPair.getPosition()) {
                sb.append(name);
                sb.append("\"" + HttpHeader.CRLF + HttpHeader.CRLF);
                sb.append(value);

            } else {
                sb.append(pair.getName());
                sb.append("\"" + HttpHeader.CRLF + HttpHeader.CRLF);
                sb.append(pair.getValue());
            }

            // Write a newline for the next boundary
            sb.append(HttpHeader.CRLF);
        }
        
        for (MultipartParam part: fileParam) {
            // First write the boundary --boundary
            sb.append(boundary);
            sb.append(HttpHeader.CRLF);

            // Then write the content
            sb.append(part.toString());
        }
        
        // Set the last boundary --boundary--
        sb.append(boundary);
        // RFC821 (an extra -- should be added at last)
        sb.append("--");
        sb.append(HttpHeader.CRLF);
        
        String query = sb.toString();
        msg.getRequestBody().setBody(query);
        return query;
    }
}
