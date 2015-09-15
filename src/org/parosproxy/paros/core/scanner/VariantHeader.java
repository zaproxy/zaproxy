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
import java.util.List;

import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.network.HttpHeaderField;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;

/**
 *
 * @author andy
 */
public class VariantHeader implements Variant {

    public static final String[] nonInjectableHeaders = {
        HttpRequestHeader.CONTENT_LENGTH,
        HttpRequestHeader.PRAGMA,
        HttpRequestHeader.CACHE_CONTROL
    };

    
    private final List<NameValuePair> params = new ArrayList<>();
    private static final Logger log = Logger.getLogger(VariantHeader.class);

    /**
     * 
     * @param msg 
     */
    @Override
    public void setMessage(HttpMessage msg) {
        
        // First we check if it's a dynamic or static page
        // I'd to do this because scanning starts to be veeeeery slow
        // --
        // this is a trivial implementation, should be good to have 
        // a page dynamic check at the parent plugin level which should 
        // use or not Variants according to the behavior of the request
        // (e.g. different content or status error/redirect)
        String query = null;
        try {
            query = msg.getRequestHeader().getURI().getQuery();
            
        } catch (URIException e) {
        	log.error(e.getMessage(), e);
        }

        // If there's almost one GET parameter go ahead
        if (query == null || query.isEmpty()) {
            // If also the Request body is null maybe it's a static page oer a null parameter page
            if (msg.getRequestBody().length() == 0) {
                return;
            }
        }        
        
        List<HttpHeaderField> httpHeaders = msg.getRequestHeader().getHeaders();
        if (httpHeaders == null) return;
        int headerPos=0;
        for (HttpHeaderField header : httpHeaders) {       	
        	boolean ignoreHeader = false;
        	for (int idx = 0; idx < nonInjectableHeaders.length; idx++) {
	        	if ( header.getName().equalsIgnoreCase(nonInjectableHeaders[idx])) {
	        		ignoreHeader=true;
	        		break;
	        	}
        	}
	        if (!ignoreHeader) {
	        	params.add(new NameValuePair(NameValuePair.TYPE_HEADER, header.getName(), header.getValue(), headerPos++));	        	
	        }
        }
    }

    /**
     * 
     * @return 
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
        // Here gives null pointer exception...
        // maybe bacause the name value isn't equal to the original value one
        msg.getRequestHeader().setHeader(originalPair.getName(), value);
        return name + ":" + value;
    }    
}
