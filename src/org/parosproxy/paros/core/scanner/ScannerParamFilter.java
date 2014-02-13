/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2012 The ZAP Development Team
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

import java.util.regex.Pattern;
import org.parosproxy.paros.network.HttpMessage;

/**
 * Module for parameter filtering according to URL, 
 * type and parameter name regexes
 * @author yhawke (2014)
 */
public class ScannerParamFilter {
    private String wildcardedUrl;
    private String paramNameRegex;
    private int paramType;
    
    private Pattern paramNamePattern;
    private Pattern urlPattern;

    /**
     * Default constructor to initialize default values 
     */
    public ScannerParamFilter() {
        this.wildcardedUrl = "*";
        this.paramType = -1;
        this.paramNamePattern = null;
        this.urlPattern = null;
    }
    
    public ScannerParamFilter(String paramName, int paramType, String urlPattern) {
        this.paramType = paramType;
        this.setParamName(paramName);
        this.setWildcardedUrl(urlPattern);
    }
    
    public int getType() {
        return paramType;
    }

    public void setType(int paramType) {
        this.paramType = paramType;
    }

    public String getParamName() {
        return paramNameRegex;
    }

    public final void setParamName(String paramNameRegex) {
        this.paramNameRegex = paramNameRegex;
        this.paramNamePattern = Pattern.compile(paramNameRegex);
    }

    public String getWildcardedUrl() {
        return wildcardedUrl;
    }

    public final void setWildcardedUrl(String wildcardedUrl) {
        this.wildcardedUrl = wildcardedUrl;
        if ((wildcardedUrl == null) || wildcardedUrl.equals("*")) {
            this.urlPattern = null;
            
        } else {        
            String wname = wildcardedUrl.toUpperCase();
            wname = wname.replaceAll("\\?", ".");
            wname = wname.replaceAll("\\*", ".*");

            this.urlPattern = Pattern.compile(wname);    
        }
    }

    /**
     * Check if the parameter should be excluded by the scanner
     * @param msg the message that is currently under scanning
     * @param param the Value/Name param object thta is currently under scanning 
     * @return true if the parameter should be excluded
     */
    public boolean isToExclude(HttpMessage msg, NameValuePair param) {
        // Verify if check for the paramType should be maintained because
        // It's currently optimized using a Map in the container
        return ((paramType < 0) || (param.getType() == paramType)) &&
                ((urlPattern == null) || urlPattern.matcher(msg.getRequestHeader().getURI().toString()).matches()) && 
                (paramNamePattern.matcher(param.getName()).matches());
    }
}
