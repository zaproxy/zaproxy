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

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.parosproxy.paros.network.HtmlParameter;
import org.parosproxy.paros.network.HttpMessage;

/**
 *
 * @author andy
 */
public class VariantCookie implements Variant {

    private List<NameValuePair> params = new ArrayList<>();

    /**
     * 
     * @param msg 
     */
    @Override
    public void setMessage(HttpMessage msg) {
        Set<HtmlParameter> cp = msg.getRequestHeader().getCookieParams();
        int idx = 0;
        
        for (HtmlParameter param : cp) {
            // ZAP: the parameter could be encoded so decode it
            params.add(new NameValuePair(NameValuePair.TYPE_COOKIE, param.getName(), getUnescapedValue(param.getValue()), idx++));
        }
    }

    /**
     * Encode the parameter
     * @param value the value that need to be encoded
     * @return the Encoded value
     */
    private String getEscapedValue(String value) {
        return (value != null) ? 
                AbstractPlugin.getURLEncode(value) : "";
    }
    
    /**
     * Decode the parameter
     * @param value
     * @return 
     */
    private String getUnescapedValue(String value) {
        //return value;
        return (value != null) ? AbstractPlugin.getURLDecode(value) : "";
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
        List<HttpCookie> cookies = new ArrayList<>();
        String encodedValue = (escaped) ? value : getEscapedValue(value);
        NameValuePair param;
        
        for (int idx = 0; idx < params.size(); idx++) {
            param = params.get(idx);
            if (idx == originalPair.getPosition()) {
                cookies.add(new HttpCookie(name, encodedValue));
                
            } else {
                cookies.add(new HttpCookie(param.getName(), getEscapedValue(param.getValue())));
            }
        }
        
        msg.getRequestHeader().setCookies(cookies);
        return name + "=" + encodedValue;
    }    
}
