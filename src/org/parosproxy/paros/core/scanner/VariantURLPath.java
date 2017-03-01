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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.parosproxy.paros.network.HttpMessage;

/**
 * Variant class used for URL path elements. For a URL like:
 * http://www.example.com/aaa/bbb/ccc?ddd=eee&fff=ggg it will handle: aaa, bbb
 * and ccc
 *
 * @author psiinon
 */
public class VariantURLPath implements Variant {

    private final Logger logger = Logger.getLogger(this.getClass());
    private final List<NameValuePair> stringParam = new ArrayList<>();

    /**
     *
     * @param msg
     */
    @Override
    public void setMessage(HttpMessage msg) {
        /*
         * For a URL like: http://www.example.com/aaa/bbb/ccc?ddd=eee&fff=ggg
         * Add the following:
         * parameter	position
         *      aaa     1
         *      bbb     2
         *      ccc     3
         */
        try {
            if (msg.getRequestHeader().getURI().getPath() != null) {
                String[] paths = msg.getRequestHeader().getURI().getPath().toString().split("/");
                int i = 0;
                for (String path : paths) {
                    if (path.length() > 0) {
                        stringParam.add(new NameValuePair(NameValuePair.TYPE_URL_PATH, path, path, i));
                    }
                    
                    i++;
                }
            }
        } catch (URIException e) {
            // Ignore
        }
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
     * Encode the parameter value for a correct URL introduction
     * @param value the value that need to be encoded
     * @return the Encoded value
     */
    private String getEscapedValue(String value) {
        if (value != null) {
            try {
                return URLEncoder.encode(value, "UTF-8");
                
            } catch ( UnsupportedEncodingException ex) { }            
        }
        
        return "";
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
        try {
            URI uri = msg.getRequestHeader().getURI();
            String[] paths = msg.getRequestHeader().getURI().getPath().toString().split("/");

            if (originalPair.getPosition() < paths.length) {
                
                String encodedValue = (escaped) ? value : getEscapedValue(value);
                
                paths[originalPair.getPosition()] = encodedValue;
                String path = StringUtils.join(paths, "/");
                
                try {
                    uri.setEscapedPath(path);

                } catch (URIException e) {
                    // Looks like it wasnt escaped after all
                    uri.setPath(path);                    
                }
            }
            
        } catch (URIException e) {
            logger.error(e.getMessage(), e);
        }
        
        return value;
    }
    
    /*
    public static void main(String[] args) {
        VariantURLPath var = new VariantURLPath();
        String value = var.getEscapedValue("prova +codifica+ strana");
        System.out.println(value);
        String res = var.getUnescapedValue(value);
        System.out.println(res);
    }
    */
}
