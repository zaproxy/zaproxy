/*
 *
 * Paros and its related class files.
 * 
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2004 Chinotec Technologies Company
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Clarified Artistic License
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Clarified Artistic License for more details.
 * 
 * You should have received a copy of the Clarified Artistic License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
// ZAP: 2011/08/30 Support for scanner levels
// ZAP: 2012/04/25 Added @Override annotation to the appropriate method.
// ZAP: 2012/06/07 Added targetParam options
// ZAP: 2012/07/30 Issue 43: Added support for Scope
// ZAP: 2012/08/07 Renamed Level to AlertThreshold and added support for AttackStrength
// ZAP: 2012/08/31 Enabled control of AttackStrength
// ZAP: 2013/01/25 Removed the "(non-Javadoc)" comments.
// ZAP: 2013/04/26 Issue 652: Added option to not delete records on shutdown
// ZAP: 2013/09/23 Issue 795: Allow param types scanned to be configured via UI
// ZAP: 2013/09/24 Issue 797: Limit number of ascan results listed to speed up scans
// ZAP: 2013/09/26 Reviewed Variant Panel configuration
// ZAP: 2014/01/10 Issue 974: Scan URL path elements
// ZAP: 2014/02/08 Added Custom Script management settings
// ZAP: 2014/02/13 Added HTTP parameter exclusion configuration on Active Scanning
//
package org.parosproxy.paros.core.scanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.log4j.Logger;
import org.parosproxy.paros.common.AbstractParam;
import org.parosproxy.paros.core.scanner.Plugin.AlertThreshold;
import org.parosproxy.paros.core.scanner.Plugin.AttackStrength;

public class ScannerParam extends AbstractParam {

    private static final String HOST_PER_SCAN = "scanner.hostPerScan";
    private static final String THREAD_PER_HOST = "scanner.threadPerHost";
    // ZAP: Added support for delayInMs
    private static final String DELAY_IN_MS = "scanner.delayInMs";
    private static final String HANDLE_ANTI_CSRF_TOKENS = "scanner.antiCSFR";
    private static final String DELETE_RECORDS_ON_SHUTDOWN = "scanner.deleteOnShutdown";
    private static final String LEVEL = "scanner.level";
    private static final String STRENGTH = "scanner.strength";
    private static final String MAX_RESULTS_LIST = "scanner.maxResults";

    // ZAP: Excluded Parameters
    private static final String ACTIVE_SCAN_BASE_KEY = "scanner";
    private static final String EXCLUDED_PARAMS_KEY = ACTIVE_SCAN_BASE_KEY + ".excludedParameters";
    private static final String EXCLUDED_PARAM_NAME = "name";
    private static final String EXCLUDED_PARAM_TYPE = "type";
    private static final String EXCLUDED_PARAM_URL = "url";
    
    // ZAP: TARGET CONFIGURATION
    private static final String TARGET_INJECTABLE = "scanner.injectable";
    private static final String TARGET_ENABLED_RPC = "scanner.enabledRPC";    
    
    // ZAP: Configuration constants
    public static final int TARGET_QUERYSTRING  = 1;
    public static final int TARGET_POSTDATA     = 1<<1;
    public static final int TARGET_COOKIE       = 1<<2;
    public static final int TARGET_HTTPHEADERS  = 1<<3;
    public static final int TARGET_URLPATH   	= 1<<4;
    
    public static final int RPC_MULTIPART   = 1;
    public static final int RPC_XML         = 1<<1;
    public static final int RPC_JSON        = 1<<2;
    public static final int RPC_GWT         = 1<<3;
    public static final int RPC_ODATA       = 1<<4;
    public static final int RPC_CUSTOM      = 1<<7;
    public static final int RPC_USERDEF     = 1<<8;
    
    // Defaults for initial configuration
    public static final int TARGET_INJECTABLE_DEFAULT = TARGET_QUERYSTRING | TARGET_POSTDATA | TARGET_URLPATH;
    public static final int TARGET_ENABLED_RPC_DEFAULT = RPC_MULTIPART | RPC_XML | RPC_JSON | RPC_GWT | RPC_ODATA;
        
    // Internal variables
    private int hostPerScan = 2;
    private int threadPerHost = 1;
    private int delayInMs = 0;
    private int maxResultsToList = 1000;
    private boolean handleAntiCSRFTokens = false;
    private boolean deleteRequestsOnShutdown = true;
    private Plugin.AlertThreshold alertThreshold = AlertThreshold.MEDIUM;
    private Plugin.AttackStrength attackStrength = AttackStrength.MEDIUM;
    
    // ZAP: Variants Configuration
    private int targetParamsInjectable = TARGET_INJECTABLE_DEFAULT;
    private int targetParamsEnabledRPC = TARGET_ENABLED_RPC_DEFAULT;
            
    // ZAP: Excluded Parameters
    private final List<ScannerParamFilter> excludedParams = new ArrayList<>();
    private final Map<Integer, List<ScannerParamFilter>> excludedParamsMap = new HashMap<>();

    // ZAP: internal Logger
    private static final Logger logger = Logger.getLogger(ScannerParam.class);    
    
    public ScannerParam() {
    }

    @Override
    protected void parse() {
        try {
            this.threadPerHost = getConfig().getInt(THREAD_PER_HOST, 1);
        } catch (Exception e) {}
        
        try {
            this.hostPerScan = getConfig().getInt(HOST_PER_SCAN, 2);
        } catch (Exception e) {}
        
        try {
            this.delayInMs = getConfig().getInt(DELAY_IN_MS, 0);
        } catch (Exception e) {}
        
        try {
            this.maxResultsToList = getConfig().getInt(MAX_RESULTS_LIST, 1000);
        } catch (Exception e) {}
        
        try {
            this.handleAntiCSRFTokens = getConfig().getBoolean(HANDLE_ANTI_CSRF_TOKENS, false);
        } catch (Exception e) {}
        
        try {
            this.deleteRequestsOnShutdown = getConfig().getBoolean(DELETE_RECORDS_ON_SHUTDOWN, true);
        } catch (Exception e) {}
        
        try {
            this.alertThreshold = AlertThreshold.valueOf(getConfig().getString(LEVEL, AlertThreshold.MEDIUM.name()));
        } catch (Exception e) {}
        
        try {
            this.attackStrength = AttackStrength.valueOf(getConfig().getString(STRENGTH, AttackStrength.MEDIUM.name()));
        } catch (Exception e) {}
        
        try {
            this.targetParamsInjectable = getConfig().getInt(TARGET_INJECTABLE, TARGET_INJECTABLE_DEFAULT);
        } catch (Exception e) {}
        
        try {
            this.targetParamsEnabledRPC = getConfig().getInt(TARGET_ENABLED_RPC, TARGET_ENABLED_RPC_DEFAULT);
        } catch (Exception e) {}
        
        // Parse the parameters that need to be excluded
        // ------------------------------------------------
        try {
            List<HierarchicalConfiguration> fields = 
                    ((HierarchicalConfiguration)getConfig()).configurationsAt(EXCLUDED_PARAMS_KEY);
            
            this.excludedParams.clear();
            this.excludedParamsMap.clear();
            List<String> tempParamNames = new ArrayList<>(fields.size());
            
            for (HierarchicalConfiguration sub : fields) {
                String name = sub.getString(EXCLUDED_PARAM_NAME, "");
                if (!name.isEmpty() && !tempParamNames.contains(name)) {
                    tempParamNames.add(name);
                    
                    addScannerParamFilter(
                            name, 
                            sub.getInt(EXCLUDED_PARAM_TYPE, NameValuePair.TYPE_UNDEFINED), 
                            sub.getString(EXCLUDED_PARAM_URL)
                    );                                        
                }
            }
            
        } catch (ConversionException e) {
            logger.error("Error while loading the exluded parameter list: " + e.getMessage(), e);
        }
        
        // If the list is null probably we've to use defaults!!!
        if (this.excludedParams.isEmpty()) {
            // OK let's set the Default parameter exclusion list
            // Evaluate the possibility to load it from an external file...
            addScannerParamFilter("(?i)ASP.NET_SessionId", NameValuePair.TYPE_UNDEFINED, "*");
            addScannerParamFilter("(?i)ASPSESSIONID.*", NameValuePair.TYPE_UNDEFINED, "*");
            addScannerParamFilter("(?i)PHPSESSID", NameValuePair.TYPE_UNDEFINED, "*");
            addScannerParamFilter("(?i)SITESERVER", NameValuePair.TYPE_UNDEFINED, "*");
            addScannerParamFilter("(?i)sessid", NameValuePair.TYPE_UNDEFINED, "*");
            addScannerParamFilter("__VIEWSTATE", NameValuePair.TYPE_POST_DATA, "*");
            addScannerParamFilter("__EVENTVALIDATION", NameValuePair.TYPE_POST_DATA, "*");
            addScannerParamFilter("__EVENTTARGET", NameValuePair.TYPE_POST_DATA, "*");
            addScannerParamFilter("__EVENTARGUMENT", NameValuePair.TYPE_POST_DATA, "*");
            addScannerParamFilter("(?i)jsessionid", NameValuePair.TYPE_UNDEFINED, "*");
            addScannerParamFilter("cfid", NameValuePair.TYPE_COOKIE, "*");
            addScannerParamFilter("cftoken", NameValuePair.TYPE_COOKIE, "*");
        }
    }

    private void addScannerParamFilter(String paramName, int paramType, String url) {
        ScannerParamFilter filter = new ScannerParamFilter();
        filter.setParamName(paramName);
        filter.setType(paramType);
        filter.setWildcardedUrl(url);

        List<ScannerParamFilter> subList = excludedParamsMap.get(filter.getType());
        if (subList == null) {
            subList = new ArrayList<>();
            excludedParamsMap.put(filter.getType(), subList);
        }

        excludedParams.add(filter);
        subList.add(filter);        
    }
    
    public List<ScannerParamFilter> getExcludedParamList() {
        return excludedParams;
    }
    
    public List<ScannerParamFilter> getExcludedParamList(int paramType) {
        return excludedParamsMap.get(paramType);
    }

    /**
     * 
     * @param filters 
     */
    public void setExcludedParamList(List<ScannerParamFilter> filters) {

        ((HierarchicalConfiguration) getConfig()).clearTree(EXCLUDED_PARAMS_KEY);
        this.excludedParams.clear();
        this.excludedParamsMap.clear();

        for (int i = 0, size = filters.size(); i < size; ++i) {
            String elementBaseKey = EXCLUDED_PARAMS_KEY + "(" + i + ").";
            ScannerParamFilter filter = filters.get(i);

            getConfig().setProperty(elementBaseKey + EXCLUDED_PARAM_NAME, filter.getParamName());
            getConfig().setProperty(elementBaseKey + EXCLUDED_PARAM_TYPE, Integer.valueOf(filter.getType()));
            getConfig().setProperty(elementBaseKey + EXCLUDED_PARAM_URL, filter.getWildcardedUrl());
            
            // And now populate again all parameter list
            addScannerParamFilter(
                    filter.getParamName(),
                    filter.getType(),
                    filter.getWildcardedUrl()
            );
        }
    }
    
    /**
     * 
     * @return 
     */
    public int getThreadPerHost() {
        return threadPerHost;
    }

    /**
     * 
     * @param threadPerHost 
     */
    public void setThreadPerHost(int threadPerHost) {
        this.threadPerHost = threadPerHost;
        getConfig().setProperty(THREAD_PER_HOST, Integer.toString(this.threadPerHost));

    }

    /**
     * @return Returns the thread.
     */
    public int getHostPerScan() {
        return hostPerScan;
    }

    /**
     * @param hostPerScan The thread to set.
     */
    public void setHostPerScan(int hostPerScan) {
        this.hostPerScan = hostPerScan;
        getConfig().setProperty(HOST_PER_SCAN, Integer.toString(this.hostPerScan));
    }

    /**
     * 
     * @return 
     */
    public int getMaxResultsToList() {
        return maxResultsToList;
    }

    /**
     * 
     * @param maxResultsToList 
     */
    public void setMaxResultsToList(int maxResultsToList) {
        this.maxResultsToList = maxResultsToList;
        getConfig().setProperty(MAX_RESULTS_LIST, Integer.toString(this.maxResultsToList));
    }

    /**
     * 
     * @param delayInMs 
     */
    public void setDelayInMs(int delayInMs) {
        this.delayInMs = delayInMs;
        getConfig().setProperty(DELAY_IN_MS, Integer.toString(this.delayInMs));
    }

    /**
     * 
     * @return 
     */
    public int getDelayInMs() {
        return delayInMs;
    }

    /**
     * 
     * @return 
     */
    public boolean getHandleAntiCSRFTokens() {
        return handleAntiCSRFTokens;
    }

    /**
     * 
     * @param handleAntiCSRFTokens 
     */
    public void setHandleAntiCSRFTokens(boolean handleAntiCSRFTokens) {
        this.handleAntiCSRFTokens = handleAntiCSRFTokens;
        getConfig().setProperty(HANDLE_ANTI_CSRF_TOKENS, handleAntiCSRFTokens);
    }

    /**
     * 
     * @return 
     */
    public boolean isDeleteRequestsOnShutdown() {
        return deleteRequestsOnShutdown;
    }

    /**
     * 
     * @param deleteRequestsOnShutdown 
     */
    public void setDeleteRequestsOnShutdown(boolean deleteRequestsOnShutdown) {
        this.deleteRequestsOnShutdown = deleteRequestsOnShutdown;
        getConfig().setProperty(DELETE_RECORDS_ON_SHUTDOWN, deleteRequestsOnShutdown);
    }

    /**
     * 
     * @return 
     */
    public Plugin.AlertThreshold getAlertThreshold() {
        return alertThreshold;
    }

    /**
     * 
     * @param level 
     */
    public void setAlertThreshold(Plugin.AlertThreshold level) {
        this.alertThreshold = level;
        getConfig().setProperty(LEVEL, level.name());
    }

    /**
     * 
     * @param level 
     */
    public void setAlertThreshold(String level) {
        this.setAlertThreshold(AlertThreshold.valueOf(level));
    }

    /**
     * 
     * @return 
     */
    public Plugin.AttackStrength getAttackStrength() {
        return attackStrength;
    }

    /**
     * 
     * @param strength 
     */
    public void setAttackStrength(Plugin.AttackStrength strength) {
        this.attackStrength = strength;
        getConfig().setProperty(STRENGTH, strength.name());
    }

    /**
     * 
     * @param strength 
     */
    public void setAttackStrength(String strength) {
        this.setAttackStrength(AttackStrength.valueOf(strength));
    }

    /**
     * 
     * @return 
     */
    public int getTargetParamsInjectable() {
        return targetParamsInjectable;
    }

    /**
     * 
     * @param targetParamsInjectable 
     */
    public void setTargetParamsInjectable(int targetParamsInjectable) {
        this.targetParamsInjectable = targetParamsInjectable;
        getConfig().setProperty(TARGET_INJECTABLE, this.targetParamsInjectable);
    }

    /**
     * 
     * @return 
     */
    public int getTargetParamsEnabledRPC() {
        return targetParamsEnabledRPC;
    }

    /**
     * 
     * @param targetParamsEnabledRPC 
     */
    public void setTargetParamsEnabledRPC(int targetParamsEnabledRPC) {
        this.targetParamsEnabledRPC = targetParamsEnabledRPC;
        getConfig().setProperty(TARGET_ENABLED_RPC, this.targetParamsEnabledRPC);
    }
}
