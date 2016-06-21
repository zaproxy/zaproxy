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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.network.HttpHeaderField;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;

/**
 *
 * @author andy
 */
public class VariantHeader implements Variant {
	
	//might still be publicly used.
	@Deprecated 
	public static final String[] injectableHeaders = {
		  HttpRequestHeader.USER_AGENT,
		  HttpRequestHeader.REFERER,
		  HttpRequestHeader.HOST
	};

	//headers converted to lowercase to make comparison easier later.    
    private static final String [] injectablesTempArray = {    	
        HttpRequestHeader.CONTENT_LENGTH.toLowerCase(Locale.ROOT),			//scanning this would likely break the entire request 
        HttpRequestHeader.PRAGMA.toLowerCase(Locale.ROOT),					//unlikely to be picked up/used by the app itself.
        HttpRequestHeader.CACHE_CONTROL.toLowerCase(Locale.ROOT),			//unlikely to be picked up/used by the app itself.          
        HttpRequestHeader.COOKIE.toLowerCase(Locale.ROOT),					//The Cookie header has its own variant that controls whether it is scanned. Better not to scan it as a header.
        HttpRequestHeader.AUTHORIZATION.toLowerCase(Locale.ROOT),			//scanning this would break authorisation
        HttpRequestHeader.PROXY_AUTHORIZATION.toLowerCase(Locale.ROOT),		//scanning this would break authorisation
        HttpRequestHeader.CONNECTION.toLowerCase(Locale.ROOT),				//scanning this would likely break the entire request
        HttpRequestHeader.PROXY_CONNECTION.toLowerCase(Locale.ROOT),		//scanning this would likely break the entire request
        HttpRequestHeader.IF_MODIFIED_SINCE.toLowerCase(Locale.ROOT),		//unlikely to be picked up/used by the app itself.
        HttpRequestHeader.IF_NONE_MATCH.toLowerCase(Locale.ROOT),			//unlikely to be picked up/used by the app itself.
        HttpRequestHeader.X_CSRF_TOKEN.toLowerCase(Locale.ROOT),			//scanning this would break authorisation
        HttpRequestHeader.X_CSRFTOKEN.toLowerCase(Locale.ROOT),				//scanning this would break authorisation
        HttpRequestHeader.X_XSRF_TOKEN.toLowerCase(Locale.ROOT),			//scanning this would break authorisation
        HttpRequestHeader.X_ZAP_SCAN_ID.toLowerCase(Locale.ROOT),			//inserted by ZAP, so no need to scan it.
        HttpRequestHeader.X_ZAP_REQUESTID.toLowerCase(Locale.ROOT),			//inserted by ZAP, so no need to scan it.
        HttpRequestHeader.X_SECURITY_PROXY.toLowerCase(Locale.ROOT),		//unlikely to be picked up/used by the app itself.
    };
    //a hashset of (lowercase) headers that we can look up quickly and easily
    private static final HashSet <String> NON_INJECTABLE_HEADERS = new HashSet<String>(Arrays.asList(injectablesTempArray));

    
    private final List<NameValuePair> params = new ArrayList<>();
    private static final Logger log = Logger.getLogger(VariantHeader.class);

    private static ScannerParam scannerOptions;

    /**
     * 
     * @param msg 
     */
    @Override
    public void setMessage(HttpMessage msg) {
        if (!isValidMessageToScan(msg)) {
            return;
        }
        
        //httpHeaders is never null, so no need to check for null
        List<HttpHeaderField> httpHeaders = msg.getRequestHeader().getHeaders();
        int headerPos=0;
        for (HttpHeaderField header : httpHeaders) {
	        if (! NON_INJECTABLE_HEADERS.contains(header.getName().toLowerCase(Locale.ROOT))) {
	        	params.add(new NameValuePair(NameValuePair.TYPE_HEADER, header.getName(), header.getValue(), headerPos++));             
	        }
        }
    }

    private boolean isValidMessageToScan(HttpMessage msg) {
        if (getScannerOptions().isScanHeadersAllRequests()) {
            return true;
        }

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
                return false;
            }
        }        
        return true;
    }

    private static ScannerParam getScannerOptions() {
        if (scannerOptions == null) {
            getScannerOptionsSync();
        }
        return scannerOptions;
    }

    private static synchronized void getScannerOptionsSync() {
        if (scannerOptions == null) {
            scannerOptions = Model.getSingleton().getOptionsParam().getParamSet(ScannerParam.class);
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
