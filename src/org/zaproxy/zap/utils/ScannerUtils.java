/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2014 The ZAP Development Team
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.zaproxy.zap.extension.pscan.PluginPassiveScanner;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Plugin;

/**
 * Class to facilitate getting various details from extension plug-ins/rules
 * @author kingthorin+owaspzap@gmail.com
 */
public final class ScannerUtils {

	private static final String QUALITY_RELEASE = 
			Constant.messages.getString("ascan.policy.table.quality.release");
	private static final String QUALITY_BETA = 
			Constant.messages.getString("ascan.policy.table.quality.beta");
	private static final String QUALITY_ALPHA = 
			Constant.messages.getString("ascan.policy.table.quality.alpha");
	private static final String QUALITY_SCRIPT_RULES = 
			Constant.messages.getString("ascan.policy.table.quality.scriptrules");
	
	private static final Pattern BETA_REGEX = Pattern.compile("(?i).*beta.*");
	private static final Pattern ALPHA_REGEX = Pattern.compile("(?i).*alpha.*");
	private static final Pattern SCRIPT_RULES_REGEX = Pattern.compile("(?i).*script.*rules.*");
	
    /** 
     * Default constructor 
     */
	private ScannerUtils() {
	
	}
	
    /** 
     * Gets the quality or status of the given {@code scanner}, using regular expressions
     * on the scanner and package name values.
     * @param the name of the scanner
     * @param the name of the scanner package
     * @return quality 
     */
	private static String getSpecificQuality(String scannerName, String scannerPackage) {

        String quality = "";

        Matcher alphaMatcher = ALPHA_REGEX.matcher(scannerPackage);
        Matcher betaMatcher = BETA_REGEX.matcher(scannerPackage);
        Matcher scriptRulesMatcher = SCRIPT_RULES_REGEX.matcher(scannerName);
        
        if (betaMatcher.matches())
        	quality = QUALITY_BETA;
        else if (alphaMatcher.matches()) 
        	quality = QUALITY_ALPHA;
        else if (scriptRulesMatcher.matches()) 
        	quality = QUALITY_SCRIPT_RULES;
        else  // We haven't matched yet so it must be Release
        	quality = QUALITY_RELEASE;
        
        return quality;
	}
	
    /** 
     * Gets the quality or status of the given passive {@code scanner}.
     * @param the scanner we want to know the quality/status of
     * @return quality 
     */
	public static String getPluginQuality(PluginPassiveScanner scanner) {
        String scannerPackage = scanner.getClass().getCanonicalName();
        String scannerName = scanner.getName();
        
        return (getSpecificQuality(scannerName, scannerPackage));
        
        }
	
    /** 
     * Gets the quality or status of the given {@code scanner}.
     * @param the scanner we want to know the quality/status of
     * @return quality 
     */
	public static String getPluginQuality(Plugin scanner) {
        String scannerPackage = scanner.getClass().getCanonicalName();
        String scannerName = scanner.getName().toLowerCase();
        
        return (getSpecificQuality(scannerName, scannerPackage));
        
        }
}

