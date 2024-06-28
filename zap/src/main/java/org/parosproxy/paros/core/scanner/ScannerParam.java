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
// ZAP: 2014/03/23 Issue 1076: Change active scanner to not delete the temporary messages generated
// ZAP: 2014/05/13 Issue 1193: Scan URL path elements - turn off by default
// ZAP: 2014/09/22 Issue 1345: Support Attack mode
// ZAP: 2014/10/24 Issue 1378: Revamp active scan panel
// ZAP: 2014/11/19 Issue 1412: Manage scan policies
// ZAP: 2015/03/04 Issue 1345: Added 'attack on start' option
// ZAP: 2015/03/25 Issue 1573: Add option to inject plugin ID in header for all ascan requests
// ZAP: 2015/10/01 Issue 1944:  Chart responses per second in ascan progress
// ZAP: 2016/01/20 Issue 1959: Allow to active scan headers of all requests
// ZAP: 2016/10/24 Issue 2951:  Support active scan rule and scan max duration
// ZAP: 2017/01/13 Exclude getExcludedParamList from the ZAP API
// ZAP: 2017/09/26 Use helper methods to read the configurations.
// ZAP: 2018/02/14 Remove unnecessary boxing / unboxing
// ZAP: 2018/09/12 Make the addition of a query parameter optional.
// ZAP: 2019/05/10 Enable custom (script) input vectors by default.
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2020/11/26 Use Log4j 2 classes for logging.
// ZAP: 2021/04/13 Issue 6469: Add option to scan null JSON values.
// ZAP: 2021/09/14 Enable Anti CSRF handling by default.
// ZAP: 2022/09/21 Use format specifiers instead of concatenation when logging.
// ZAP: 2022/11/04 Prevent invalid number of hosts/threads.
// ZAP: 2022/12/22 Issue 7663: Default thread to number of processors.
// ZAP: 2023/01/10 Tidy up logger.
// ZAP: 2023/05/17 Add option for the maximum number of alerts per rule.
// ZAP: 2023/07/06 Deprecate delayInMs.
package org.parosproxy.paros.core.scanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.common.AbstractParam;
import org.zaproxy.zap.extension.api.ZapApiIgnore;

public class ScannerParam extends AbstractParam {

    // Base path for the Scanner Param tree
    private static final String ACTIVE_SCAN_BASE_KEY = "scanner";

    private static final String HOST_PER_SCAN = ACTIVE_SCAN_BASE_KEY + ".hostPerScan";
    private static final String THREAD_PER_HOST = ACTIVE_SCAN_BASE_KEY + ".threadPerHost";
    // ZAP: Added support for delayInMs
    private static final String DELAY_IN_MS = ACTIVE_SCAN_BASE_KEY + ".delayInMs";
    private static final String INJECT_PLUGIN_ID_IN_HEADER = ACTIVE_SCAN_BASE_KEY + ".pluginHeader";
    private static final String HANDLE_ANTI_CSRF_TOKENS = ACTIVE_SCAN_BASE_KEY + ".antiCSRF";
    private static final String PROMPT_IN_ATTACK_MODE = ACTIVE_SCAN_BASE_KEY + ".attackPrompt";
    private static final String RESCAN_IN_ATTACK_MODE = ACTIVE_SCAN_BASE_KEY + ".attackRescan";
    private static final String PROMPT_TO_CLEAR_FINISHED = ACTIVE_SCAN_BASE_KEY + ".clearFinished";
    private static final String MAX_RESULTS_LIST = ACTIVE_SCAN_BASE_KEY + ".maxResults";
    private static final String MAX_SCANS_IN_UI = ACTIVE_SCAN_BASE_KEY + ".maxScansInUI";
    private static final String SHOW_ADV_DIALOG = ACTIVE_SCAN_BASE_KEY + ".advDialog";
    private static final String DEFAULT_POLICY = ACTIVE_SCAN_BASE_KEY + ".defaultPolicy";
    private static final String ATTACK_POLICY = ACTIVE_SCAN_BASE_KEY + ".attackPolicy";
    private static final String ALLOW_ATTACK_ON_START = ACTIVE_SCAN_BASE_KEY + ".attackOnStart";
    private static final String MAX_CHART_TIME_IN_MINS = ACTIVE_SCAN_BASE_KEY + ".chartTimeInMins";

    private static final String MAX_RULE_DURATION_IN_MINS =
            ACTIVE_SCAN_BASE_KEY + ".maxRuleDurationInMins";
    private static final String MAX_SCAN_DURATION_IN_MINS =
            ACTIVE_SCAN_BASE_KEY + ".maxScanDurationInMins";

    // ZAP: Excluded Parameters
    private static final String EXCLUDED_PARAMS_KEY = ACTIVE_SCAN_BASE_KEY + ".excludedParameters";
    private static final String EXCLUDED_PARAM_NAME = "name";
    private static final String EXCLUDED_PARAM_TYPE = "type";
    private static final String EXCLUDED_PARAM_URL = "url";

    // ZAP: TARGET CONFIGURATION
    private static final String TARGET_INJECTABLE = ACTIVE_SCAN_BASE_KEY + ".injectable";
    private static final String TARGET_ENABLED_RPC = ACTIVE_SCAN_BASE_KEY + ".enabledRPC";

    /**
     * Configuration key to write/read the {@code scanHeadersAllRequests} flag.
     *
     * @since 2.5.0
     * @see #scanHeadersAllRequests
     */
    private static final String SCAN_HEADERS_ALL_REQUESTS =
            ACTIVE_SCAN_BASE_KEY + ".scanHeadersAllRequests";

    /**
     * Configuration key to write/read the {@code addQueryParam} flag.
     *
     * @since 2.8.0
     * @see #addQueryParam
     */
    private static final String SCAN_ADD_QUERY_PARAM = ACTIVE_SCAN_BASE_KEY + ".addQueryParam";

    /**
     * Configuration key to write/read the {@link #scanNullJsonValues} flag.
     *
     * @since 2.11.0
     */
    static final String SCAN_NULL_JSON_VALUES = ACTIVE_SCAN_BASE_KEY + ".scanNullJsonValues";

    private static final String MAX_ALERTS_PER_RULE = ACTIVE_SCAN_BASE_KEY + ".maxAlertsPerRule";

    // ZAP: Configuration constants
    public static final int TARGET_QUERYSTRING = 1;
    public static final int TARGET_POSTDATA = 1 << 1;
    public static final int TARGET_COOKIE = 1 << 2;
    public static final int TARGET_HTTPHEADERS = 1 << 3;
    public static final int TARGET_URLPATH = 1 << 4;

    public static final int RPC_MULTIPART = 1;
    public static final int RPC_XML = 1 << 1;
    public static final int RPC_JSON = 1 << 2;
    public static final int RPC_GWT = 1 << 3;
    public static final int RPC_ODATA = 1 << 4;
    public static final int RPC_DWR = 1 << 5;
    public static final int RPC_CUSTOM = 1 << 7;
    public static final int RPC_USERDEF = 1 << 8;

    // Defaults for initial configuration
    public static final int TARGET_INJECTABLE_DEFAULT = TARGET_QUERYSTRING | TARGET_POSTDATA;
    public static final int TARGET_ENABLED_RPC_DEFAULT =
            RPC_MULTIPART | RPC_XML | RPC_JSON | RPC_GWT | RPC_ODATA | RPC_DWR | RPC_CUSTOM;
    private static final int DEFAULT_MAX_CHART_TIME_IN_MINS = 10;

    // Internal variables
    private int hostPerScan = 2;
    private int threadPerHost;
    private int delayInMs = 0;
    private int maxResultsToList = 1000;
    private int maxScansInUI = 5;
    private boolean injectPluginIdInHeader = false;
    private boolean handleAntiCSRFTokens = true;
    private boolean promptInAttackMode = true;
    private boolean rescanInAttackMode = true;
    private boolean promptToClearFinishedScans = true;
    private boolean showAdvancedDialog = false;
    private boolean allowAttackOnStart = false;
    private String defaultPolicy;
    private String attackPolicy;
    private int maxChartTimeInMins = DEFAULT_MAX_CHART_TIME_IN_MINS;
    private int maxRuleDurationInMins = 0;
    private int maxScanDurationInMins = 0;

    // ZAP: Variants Configuration
    private int targetParamsInjectable = TARGET_INJECTABLE_DEFAULT;
    private int targetParamsEnabledRPC = TARGET_ENABLED_RPC_DEFAULT;

    /**
     * Flag that indicates if the HTTP Headers of all requests should be scanned, not just requests
     * that send parameters, through the query or request body.
     *
     * <p>Default value is {@code false}.
     *
     * @since 2.5.0
     * @see #SCAN_HEADERS_ALL_REQUESTS
     * @see #isScanHeadersAllRequests()
     * @see #setScanHeadersAllRequests(boolean)
     */
    private boolean scanHeadersAllRequests;

    /**
     * Flag that indicates if a query param should be added to GET requests that don't have one.
     *
     * <p>Default value is {@code false}.
     *
     * @since 2.8.0
     * @see #isAddQueryParam()
     * @see #setAddQueryParam(boolean)
     */
    private boolean addQueryParam;

    /**
     * Flag that indicates if the active scanner should scan null JSON values.
     *
     * <p>Default value is {@code false}.
     *
     * @since 2.11.0
     * @see #isScanNullJsonValues()
     * @see #setScanNullJsonValues(boolean)
     */
    private boolean scanNullJsonValues;

    private int maxAlertsPerRule;

    // ZAP: Excluded Parameters
    private final List<ScannerParamFilter> excludedParams = new ArrayList<>();
    private final Map<Integer, List<ScannerParamFilter>> excludedParamsMap = new HashMap<>();

    // ZAP: internal Logger
    private static final Logger LOGGER = LogManager.getLogger(ScannerParam.class);

    public ScannerParam() {}

    @Override
    protected void parse() {
        migrateOldOptions();

        this.threadPerHost = Math.max(1, getInt(THREAD_PER_HOST, Constant.getDefaultThreadCount()));

        this.hostPerScan = Math.max(1, getInt(HOST_PER_SCAN, 2));

        this.delayInMs = getInt(DELAY_IN_MS, 0);

        this.maxResultsToList = getInt(MAX_RESULTS_LIST, 1000);

        this.maxRuleDurationInMins = getInt(MAX_RULE_DURATION_IN_MINS, 0);

        this.maxScanDurationInMins = getInt(MAX_SCAN_DURATION_IN_MINS, 0);

        this.maxScansInUI = getInt(MAX_SCANS_IN_UI, 5);

        this.injectPluginIdInHeader = getBoolean(INJECT_PLUGIN_ID_IN_HEADER, false);

        this.handleAntiCSRFTokens = getBoolean(HANDLE_ANTI_CSRF_TOKENS, true);

        this.promptInAttackMode = getBoolean(PROMPT_IN_ATTACK_MODE, true);

        this.rescanInAttackMode = getBoolean(RESCAN_IN_ATTACK_MODE, true);

        this.promptToClearFinishedScans = getBoolean(PROMPT_TO_CLEAR_FINISHED, true);

        this.showAdvancedDialog = getBoolean(SHOW_ADV_DIALOG, false);

        this.defaultPolicy = getString(DEFAULT_POLICY, null);

        this.attackPolicy = getString(ATTACK_POLICY, null);

        this.targetParamsInjectable = getInt(TARGET_INJECTABLE, TARGET_INJECTABLE_DEFAULT);

        this.targetParamsEnabledRPC = getInt(TARGET_ENABLED_RPC, TARGET_ENABLED_RPC_DEFAULT);

        this.allowAttackOnStart = getBoolean(ALLOW_ATTACK_ON_START, false);

        this.maxChartTimeInMins = getInt(MAX_CHART_TIME_IN_MINS, DEFAULT_MAX_CHART_TIME_IN_MINS);

        this.scanHeadersAllRequests = getBoolean(SCAN_HEADERS_ALL_REQUESTS, false);

        this.addQueryParam = getBoolean(SCAN_ADD_QUERY_PARAM, false);

        this.scanNullJsonValues = getBoolean(SCAN_NULL_JSON_VALUES, false);

        // Parse the parameters that need to be excluded
        // ------------------------------------------------
        try {
            List<HierarchicalConfiguration> fields =
                    ((HierarchicalConfiguration) getConfig()).configurationsAt(EXCLUDED_PARAMS_KEY);

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
                            sub.getString(EXCLUDED_PARAM_URL));
                }
            }

        } catch (ConversionException e) {
            LOGGER.error("Error while loading the excluded parameter list: {}", e.getMessage(), e);
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
            addScannerParamFilter("javax.faces.ViewState", NameValuePair.TYPE_POST_DATA, "*");
            addScannerParamFilter("(?i)jsessionid", NameValuePair.TYPE_UNDEFINED, "*");
            addScannerParamFilter("cfid", NameValuePair.TYPE_COOKIE, "*");
            addScannerParamFilter("cftoken", NameValuePair.TYPE_COOKIE, "*");
        }

        maxAlertsPerRule = Math.max(0, getInt(MAX_ALERTS_PER_RULE, 0));
    }

    private void migrateOldOptions() {
        String oldKey = "scanner.antiCSFR";
        if (getConfig().containsKey(oldKey)) {
            getConfig().setProperty(HANDLE_ANTI_CSRF_TOKENS, getConfig().getProperty(oldKey));
            getConfig().clearProperty(oldKey);
        }

        oldKey = "scanner.deleteOnShutdown";
        if (getConfig().containsKey(oldKey)) {
            getConfig().clearProperty(oldKey);
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

    @ZapApiIgnore
    public List<ScannerParamFilter> getExcludedParamList() {
        return excludedParams;
    }

    public List<ScannerParamFilter> getExcludedParamList(int paramType) {
        return excludedParamsMap.get(paramType);
    }

    /** @param filters */
    public void setExcludedParamList(List<ScannerParamFilter> filters) {

        ((HierarchicalConfiguration) getConfig()).clearTree(EXCLUDED_PARAMS_KEY);
        this.excludedParams.clear();
        this.excludedParamsMap.clear();

        for (int i = 0, size = filters.size(); i < size; ++i) {
            String elementBaseKey = EXCLUDED_PARAMS_KEY + "(" + i + ").";
            ScannerParamFilter filter = filters.get(i);

            getConfig().setProperty(elementBaseKey + EXCLUDED_PARAM_NAME, filter.getParamName());
            getConfig().setProperty(elementBaseKey + EXCLUDED_PARAM_TYPE, filter.getType());
            getConfig().setProperty(elementBaseKey + EXCLUDED_PARAM_URL, filter.getWildcardedUrl());

            // And now populate again all parameter list
            addScannerParamFilter(
                    filter.getParamName(), filter.getType(), filter.getWildcardedUrl());
        }
    }

    /** @return */
    public int getThreadPerHost() {
        return threadPerHost;
    }

    /** @param threadPerHost */
    public void setThreadPerHost(int threadPerHost) {
        this.threadPerHost = Math.max(1, threadPerHost);
        getConfig().setProperty(THREAD_PER_HOST, Integer.toString(this.threadPerHost));
    }

    /** @return Returns the thread. */
    public int getHostPerScan() {
        return hostPerScan;
    }

    /** @param hostPerScan The thread to set. */
    public void setHostPerScan(int hostPerScan) {
        this.hostPerScan = Math.max(1, hostPerScan);
        getConfig().setProperty(HOST_PER_SCAN, Integer.toString(this.hostPerScan));
    }

    /** @return */
    public int getMaxResultsToList() {
        return maxResultsToList;
    }

    /** @param maxResultsToList */
    public void setMaxResultsToList(int maxResultsToList) {
        this.maxResultsToList = maxResultsToList;
        getConfig().setProperty(MAX_RESULTS_LIST, Integer.toString(this.maxResultsToList));
    }

    public int getMaxRuleDurationInMins() {
        return maxRuleDurationInMins;
    }

    public void setMaxRuleDurationInMins(int maxRuleDurationInMins) {
        this.maxRuleDurationInMins = maxRuleDurationInMins;
        getConfig()
                .setProperty(
                        MAX_RULE_DURATION_IN_MINS, Integer.toString(this.maxRuleDurationInMins));
    }

    public int getMaxScanDurationInMins() {
        return maxScanDurationInMins;
    }

    public void setMaxScanDurationInMins(int maxScanDurationInMins) {
        this.maxScanDurationInMins = maxScanDurationInMins;
        getConfig()
                .setProperty(
                        MAX_SCAN_DURATION_IN_MINS, Integer.toString(this.maxScanDurationInMins));
    }

    /**
     * Gets the maximum number of alerts that a rule can raise before being skipped.
     *
     * @return the max number of alerts.
     * @since 2.13.0
     */
    public int getMaxAlertsPerRule() {
        return maxAlertsPerRule;
    }

    /**
     * Sets the maximum number of alerts that a rule can raise before being skipped.
     *
     * @param maxAlertsPerRule the max number of alerts.
     * @since 2.13.0
     */
    public void setMaxAlertsPerRule(int maxAlertsPerRule) {
        this.maxAlertsPerRule = Math.max(0, maxAlertsPerRule);

        getConfig().setProperty(MAX_ALERTS_PER_RULE, this.maxAlertsPerRule);
    }

    /**
     * @param delayInMs the delay in milliseconds.
     * @deprecated (2.13.0) This option has been superseded with the rate limiting provided by the
     *     Network add-on. It will be removed in a future release.
     */
    @Deprecated(since = "2.13.0", forRemoval = true)
    public void setDelayInMs(int delayInMs) {
        this.delayInMs = delayInMs;
        getConfig().setProperty(DELAY_IN_MS, Integer.toString(this.delayInMs));
    }

    /**
     * @return the delay in milliseconds.
     * @deprecated (2.13.0) This option has been superseded with the rate limiting provided by the
     *     Network add-on. It will be removed in a future release.
     */
    @Deprecated(since = "2.13.0", forRemoval = true)
    public int getDelayInMs() {
        return delayInMs;
    }

    /**
     * @return Returns if the option to inject plugin ID in header for ascan requests is turned on
     */
    public boolean isInjectPluginIdInHeader() {
        return injectPluginIdInHeader;
    }

    /** @param injectPluginIdInHeader */
    public void setInjectPluginIdInHeader(boolean injectPluginIdInHeader) {
        this.injectPluginIdInHeader = injectPluginIdInHeader;
        getConfig().setProperty(INJECT_PLUGIN_ID_IN_HEADER, injectPluginIdInHeader);
    }

    /** @return */
    public boolean getHandleAntiCSRFTokens() {
        return handleAntiCSRFTokens;
    }

    /** @param handleAntiCSRFTokens */
    public void setHandleAntiCSRFTokens(boolean handleAntiCSRFTokens) {
        this.handleAntiCSRFTokens = handleAntiCSRFTokens;
        getConfig().setProperty(HANDLE_ANTI_CSRF_TOKENS, handleAntiCSRFTokens);
    }

    public boolean isRescanInAttackMode() {
        return rescanInAttackMode;
    }

    public void setRescanInAttackMode(boolean rescanInAttackMode) {
        this.rescanInAttackMode = rescanInAttackMode;
        getConfig().setProperty(RESCAN_IN_ATTACK_MODE, rescanInAttackMode);
    }

    public boolean isPromptInAttackMode() {
        return promptInAttackMode;
    }

    public void setPromptInAttackMode(boolean promptInAttackMode) {
        this.promptInAttackMode = promptInAttackMode;
        getConfig().setProperty(PROMPT_IN_ATTACK_MODE, promptInAttackMode);
    }

    /** @return */
    public int getTargetParamsInjectable() {
        return targetParamsInjectable;
    }

    /** @param targetParamsInjectable */
    public void setTargetParamsInjectable(int targetParamsInjectable) {
        this.targetParamsInjectable = targetParamsInjectable;
        getConfig().setProperty(TARGET_INJECTABLE, this.targetParamsInjectable);
    }

    /** @return */
    public int getTargetParamsEnabledRPC() {
        return targetParamsEnabledRPC;
    }

    /** @param targetParamsEnabledRPC */
    public void setTargetParamsEnabledRPC(int targetParamsEnabledRPC) {
        this.targetParamsEnabledRPC = targetParamsEnabledRPC;
        getConfig().setProperty(TARGET_ENABLED_RPC, this.targetParamsEnabledRPC);
    }

    public boolean isPromptToClearFinishedScans() {
        return promptToClearFinishedScans;
    }

    public void setPromptToClearFinishedScans(boolean promptToClearFinishedScans) {
        this.promptToClearFinishedScans = promptToClearFinishedScans;
        getConfig().setProperty(PROMPT_TO_CLEAR_FINISHED, this.promptToClearFinishedScans);
    }

    public int getMaxScansInUI() {
        return maxScansInUI;
    }

    public void setMaxScansInUI(int maxScansInUI) {
        this.maxScansInUI = maxScansInUI;
        getConfig().setProperty(MAX_SCANS_IN_UI, this.maxScansInUI);
    }

    public boolean isShowAdvancedDialog() {
        return showAdvancedDialog;
    }

    public void setShowAdvancedDialog(boolean showAdvancedDialog) {
        this.showAdvancedDialog = showAdvancedDialog;
        getConfig().setProperty(SHOW_ADV_DIALOG, this.showAdvancedDialog);
    }

    public String getDefaultPolicy() {
        return defaultPolicy;
    }

    public String getAttackPolicy() {
        return attackPolicy;
    }

    public void setDefaultPolicy(String defaultPolicy) {
        this.defaultPolicy = defaultPolicy;
        getConfig().setProperty(DEFAULT_POLICY, this.defaultPolicy);
    }

    public void setAttackPolicy(String attackPolicy) {
        this.attackPolicy = attackPolicy;
        getConfig().setProperty(ATTACK_POLICY, this.attackPolicy);
    }

    public boolean isAllowAttackOnStart() {
        return allowAttackOnStart;
    }

    public void setAllowAttackOnStart(boolean allowAttackOnStart) {
        this.allowAttackOnStart = allowAttackOnStart;
        getConfig().setProperty(ALLOW_ATTACK_ON_START, this.allowAttackOnStart);
    }

    public int getMaxChartTimeInMins() {
        return maxChartTimeInMins;
    }

    public void setMaxChartTimeInMins(int maxChartTimeInMins) {
        this.maxChartTimeInMins = maxChartTimeInMins;
        getConfig().setProperty(MAX_CHART_TIME_IN_MINS, this.maxChartTimeInMins);
    }

    /**
     * Tells whether or not the HTTP Headers of all requests should be scanned, not just requests
     * that send parameters, through the query or request body.
     *
     * @return {@code true} if the HTTP Headers of all requests should be scanned, {@code false}
     *     otherwise
     * @since 2.5.0
     * @see #setScanHeadersAllRequests(boolean)
     */
    public boolean isScanHeadersAllRequests() {
        return scanHeadersAllRequests;
    }

    /**
     * Sets whether or not the HTTP Headers of all requests should be scanned, not just requests
     * that send parameters, through the query or request body.
     *
     * @param scanAllRequests {@code true} if the HTTP Headers of all requests should be scanned,
     *     {@code false} otherwise
     * @since 2.5.0
     * @see #isScanHeadersAllRequests()
     */
    public void setScanHeadersAllRequests(boolean scanAllRequests) {
        if (scanAllRequests == scanHeadersAllRequests) {
            return;
        }

        this.scanHeadersAllRequests = scanAllRequests;
        getConfig().setProperty(SCAN_HEADERS_ALL_REQUESTS, this.scanHeadersAllRequests);
    }

    /**
     * Tells whether ZAP should add a parameter to GET requests that don't have one.
     *
     * @return {@code true} if a GET parameter should be added, {@code false} otherwise
     * @since 2.8.0
     * @see #setAddQueryParam(boolean)
     */
    public boolean isAddQueryParam() {
        return addQueryParam;
    }

    /**
     * Sets whether or not ZAP should add a parameter to GET requests that don't have one.
     *
     * @param addQueryParam {@code true} if ZAP should add a parameter to GET requests that don't
     *     have one, {@code false} otherwise
     * @since 2.8.0
     * @see #isAddQueryParam()
     */
    public void setAddQueryParam(boolean addQueryParam) {
        this.addQueryParam = addQueryParam;
        getConfig().setProperty(SCAN_ADD_QUERY_PARAM, this.addQueryParam);
    }

    /**
     * Tells whether or not the active scanner should scan null JSON values.
     *
     * @return {@code true} if null values should be scanned, {@code false} otherwise.
     * @since 2.11.0
     * @see #setScanNullJsonValues(boolean)
     */
    public boolean isScanNullJsonValues() {
        return scanNullJsonValues;
    }

    /**
     * Sets whether or not the active scanner should scan null JSON values.
     *
     * @param scan {@code true} if null values should be scanned, {@code false} otherwise.
     * @since 2.11.0
     * @see #isScanNullJsonValues()
     * @see VariantJSONQuery#setScanNullValues(boolean)
     */
    public void setScanNullJsonValues(boolean scan) {
        this.scanNullJsonValues = scan;
        getConfig().setProperty(SCAN_NULL_JSON_VALUES, this.scanNullJsonValues);
    }
}
