/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
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

package org.zaproxy.zap.utils;

import org.zaproxy.zap.extension.pscan.PluginPassiveScanner;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Plugin;

/**
 * Class to facilitate getting various details from extension plug-ins/rules
 * @author kingthorin+owaspzap@gmail.com
 */

public class ScannerUtils {

	private static final String QUALITY_RELEASE = 
			Constant.messages.getString("ascan.policy.table.qualityrelease");
	private static final String QUALITY_BETA = 
			Constant.messages.getString("ascan.policy.table.qualitybeta");
	private static final String QUALITY_ALPHA = 
			Constant.messages.getString("ascan.policy.table.qualityalpha");
	private static final String QUALITY_SCRIPT_RULES = 
			Constant.messages.getString("ascan.policy.table.qualityscriptrules");
	
	private static final String BETA_REGEX = ".*beta.*";
	private static final String ALPHA_REGEX = ".*alpha.*";
	private static final String SCRIPT_RULES_REGEX = ".*script.*rules.*";
		
    /** 
     * get the quality or status of the Passive scanner in question
     * @param scanner the scanner we want to know the quality/status of
     * @return quality 
     */
	public String getPluginQuality(PluginPassiveScanner scanner) {
        String parentExtension = scanner.getClass().getCanonicalName();
        String quality = new String();
        
        if (parentExtension.toLowerCase().matches(BETA_REGEX))
        	quality = QUALITY_BETA;
        if (parentExtension.toLowerCase().matches(ALPHA_REGEX)) 
        	quality = QUALITY_ALPHA;
        if (scanner.getName().toLowerCase().matches(SCRIPT_RULES_REGEX)) 
        	quality = QUALITY_SCRIPT_RULES;
        if (quality == null || quality.length() == 0) // We haven't matched yet so it must be Release
        	quality = QUALITY_RELEASE;
        
        return quality;
        }
	
    /** 
     * get the quality or status of the scanner in question
     * @param scanner the scanner we want to know the quality/status of
     * @return quality 
     */
	public String getPluginQuality(Plugin scanner) {
        String parentExtension = scanner.getClass().getCanonicalName();
        String quality = new String();
        
        if (parentExtension.toLowerCase().matches(BETA_REGEX))
        	quality = QUALITY_BETA;
        if (parentExtension.toLowerCase().matches(ALPHA_REGEX)) 
        	quality = QUALITY_ALPHA;
        if (scanner.getName().toLowerCase().matches(SCRIPT_RULES_REGEX)) 
        	quality = QUALITY_SCRIPT_RULES;
        if (quality == null || quality.length() == 0) // We haven't matched yet so it must be Release
        	quality = QUALITY_RELEASE;
        
        return quality;
        
        }
}

