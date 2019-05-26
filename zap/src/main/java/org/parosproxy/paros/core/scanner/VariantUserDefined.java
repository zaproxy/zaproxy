/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright The ZAP Development team
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;


public class VariantUserDefined implements Variant {

	private static Map<String, int[][]> injectionPointMap = new HashMap<String, int[][]>();
    
	private int headerLength;
	private int bodyLength;

	private int[][] injectionPoints = null;

    private static final Logger logger = Logger.getLogger(VariantUserDefined.class);    

    public VariantUserDefined() {
        super();
    }
    
    public static void setInjectionPoints(String url, int[][] injectionPoints) {
    	/* 
    	 * Variants are initialised at a fairly low level in the code, which means the higher levels
    	 * cant access them directly.
    	 * However this sort of variant only makes sense under the direct control of the user, hence
    	 * this rather nasty static method which associates user defined injection points with URLs
    	 */
    	injectionPointMap.put(url, injectionPoints);
    }
    
    @Override
    public void setMessage(HttpMessage msg) {
    	headerLength = msg.getRequestHeader().toString().length();
    	bodyLength = msg.getRequestBody().toString().length();
    	String url = msg.getRequestHeader().getURI().toString();
    	this.injectionPoints = injectionPointMap.get(url);
    }

    @Override
    public List<NameValuePair> getParamList() {
    	List<NameValuePair> list = new ArrayList<NameValuePair>();
    	if (this.injectionPoints!= null) {
	    	for (int i=0; i < this.injectionPoints.length; i++) {
	    		if (isInHeader(this.injectionPoints[i]) || isInBody(this.injectionPoints[i]) ) {
	    			list.add(new NameValuePair(NameValuePair.TYPE_UNDEFINED, "", "", i));
	    		} else {
	    			logger.warn("Invalid injection point: " + java.util.Arrays.toString(this.injectionPoints[i]));
	    		}
	    	}
    	}
    	return list;
    }
    @Override
    public String setParameter(HttpMessage msg, NameValuePair originalPair, String param, String value) {
    	int[] injPoint = this.injectionPoints[originalPair.getPosition()];
		if (isInHeader(injPoint)) {
	    	String header = msg.getRequestHeader().toString();
	    	StringBuilder sb = new StringBuilder(header.length());
    		sb.append(header.substring(0, injPoint[0]));
    		sb.append(value);
    		sb.append(header.substring(injPoint[1]));
    		try {
				msg.getRequestHeader().setMessage(sb.toString());
			} catch (HttpMalformedHeaderException e) {
				logger.error(e.getMessage(), e);
			}
		} else {
	    	String body = msg.getRequestBody().toString();
	    	StringBuilder sb = new StringBuilder(body.length());
    		sb.append(body.substring(0, injPoint[0] - headerLength));
    		sb.append(value);
    		sb.append(body.substring(injPoint[1] - headerLength));
			msg.getRequestBody().setBody(sb.toString());
		}
    	return "value";
    }
    
    @Override
    public String setEscapedParameter(HttpMessage msg, NameValuePair originalPair, String param, String value) {
    	return this.setParameter(msg, originalPair, param, value);
    }
    
    private boolean isInHeader(int[] injPoint) {
    	return injPoint[0] < headerLength && injPoint[1] < headerLength;
    }
    
    private boolean isInBody(int[] injPoint) {
    	return injPoint[0] > headerLength && injPoint[1] > headerLength &&
    			injPoint[0] - headerLength < bodyLength && injPoint[1] - headerLength < bodyLength;
    }
    
}
