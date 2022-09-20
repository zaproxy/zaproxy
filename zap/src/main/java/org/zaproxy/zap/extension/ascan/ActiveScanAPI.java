/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2011 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.extension.ascan;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.regex.PatternSyntaxException;
import net.sf.json.JSON;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.scanner.Category;
import org.parosproxy.paros.core.scanner.HostProcess;
import org.parosproxy.paros.core.scanner.NameValuePair;
import org.parosproxy.paros.core.scanner.Plugin;
import org.parosproxy.paros.core.scanner.Plugin.AlertThreshold;
import org.parosproxy.paros.core.scanner.ScannerParamFilter;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.zaproxy.zap.extension.api.ApiAction;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiException.Type;
import org.zaproxy.zap.extension.api.ApiImplementor;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.extension.api.ApiResponseElement;
import org.zaproxy.zap.extension.api.ApiResponseList;
import org.zaproxy.zap.extension.api.ApiResponseSet;
import org.zaproxy.zap.extension.api.ApiView;
import org.zaproxy.zap.extension.users.ExtensionUserManagement;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.SessionStructure;
import org.zaproxy.zap.model.StructuralNode;
import org.zaproxy.zap.model.Target;
import org.zaproxy.zap.users.User;
import org.zaproxy.zap.utils.ApiUtils;
import org.zaproxy.zap.utils.XMLStringUtil;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

public class ActiveScanAPI extends ApiImplementor {

    private static Logger log = LogManager.getLogger(ActiveScanAPI.class);

    private static final String PREFIX = "ascan";
    private static final String ACTION_SCAN = "scan";
    private static final String ACTION_SCAN_AS_USER = "scanAsUser";
    private static final String ACTION_PAUSE_SCAN = "pause";
    private static final String ACTION_RESUME_SCAN = "resume";
    private static final String ACTION_STOP_SCAN = "stop";
    private static final String ACTION_PAUSE_ALL_SCANS = "pauseAllScans";
    private static final String ACTION_RESUME_ALL_SCANS = "resumeAllScans";
    private static final String ACTION_STOP_ALL_SCANS = "stopAllScans";
    private static final String ACTION_REMOVE_SCAN = "removeScan";
    private static final String ACTION_REMOVE_ALL_SCANS = "removeAllScans";

    private static final String ACTION_EXCLUDE_FROM_SCAN = "excludeFromScan";
    private static final String ACTION_CLEAR_EXCLUDED_FROM_SCAN = "clearExcludedFromScan";
    private static final String ACTION_ENABLE_ALL_SCANNERS = "enableAllScanners";
    private static final String ACTION_DISABLE_ALL_SCANNERS = "disableAllScanners";
    private static final String ACTION_ENABLE_SCANNERS = "enableScanners";
    private static final String ACTION_DISABLE_SCANNERS = "disableScanners";
    private static final String ACTION_SET_ENABLED_POLICIES = "setEnabledPolicies";
    private static final String ACTION_SET_POLICY_ATTACK_STRENGTH = "setPolicyAttackStrength";
    private static final String ACTION_SET_POLICY_ALERT_THRESHOLD = "setPolicyAlertThreshold";
    private static final String ACTION_SET_SCANNER_ATTACK_STRENGTH = "setScannerAttackStrength";
    private static final String ACTION_SET_SCANNER_ALERT_THRESHOLD = "setScannerAlertThreshold";
    private static final String ACTION_ADD_SCAN_POLICY = "addScanPolicy";
    private static final String ACTION_REMOVE_SCAN_POLICY = "removeScanPolicy";
    private static final String ACTION_UPDATE_SCAN_POLICY = "updateScanPolicy";
    private static final String ACTION_IMPORT_SCAN_POLICY = "importScanPolicy";

    private static final String ACTION_ADD_EXCLUDED_PARAM = "addExcludedParam";
    private static final String ACTION_MODIFY_EXCLUDED_PARAM = "modifyExcludedParam";
    private static final String ACTION_REMOVE_EXCLUDED_PARAM = "removeExcludedParam";

    private static final String ACTION_SKIP_SCANNER = "skipScanner";

    private static final String VIEW_STATUS = "status";
    private static final String VIEW_SCANS = "scans";
    private static final String VIEW_MESSAGES_IDS = "messagesIds";
    private static final String VIEW_ALERTS_IDS = "alertsIds";
    private static final String VIEW_EXCLUDED_FROM_SCAN = "excludedFromScan";
    private static final String VIEW_SCANNERS = "scanners";
    // TODO rename? Note any changes like this to the existing API must be clearly documented to
    // users
    private static final String VIEW_POLICIES = "policies";
    private static final String VIEW_SCAN_POLICY_NAMES = "scanPolicyNames";
    private static final String VIEW_ATTACK_MODE_QUEUE = "attackModeQueue";
    private static final String VIEW_SCAN_PROGRESS = "scanProgress";
    private static final String VIEW_EXCLUDED_PARAMS = "excludedParams";
    private static final String VIEW_OPTION_EXCLUDED_PARAM_LIST = "optionExcludedParamList";
    private static final String VIEW_EXCLUDED_PARAM_TYPES = "excludedParamTypes";

    private static final String PARAM_URL = "url";
    private static final String PARAM_CONTEXT_ID = "contextId";
    private static final String PARAM_USER_ID = "userId";
    private static final String PARAM_REGEX = "regex";
    private static final String PARAM_RECURSE = "recurse";
    private static final String PARAM_JUST_IN_SCOPE = "inScopeOnly";
    private static final String PARAM_IDS = "ids";
    private static final String PARAM_ID = "id";
    private static final String PARAM_ATTACK_STRENGTH = "attackStrength";
    private static final String PARAM_ALERT_THRESHOLD = "alertThreshold";
    private static final String PARAM_SCAN_POLICY_NAME = "scanPolicyName";
    private static final String PARAM_PATH = "path";
    // TODO rename to categoryId? Note any changes like this to the existing API must be clearly
    // documented to users
    private static final String PARAM_CATEGORY_ID = "policyId";
    private static final String PARAM_SCAN_ID = "scanId";
    private static final String PARAM_SCANNER_ID = "scannerId";
    private static final String PARAM_METHOD = "method";
    private static final String PARAM_POST_DATA = "postData";
    private static final String PARAM_IDX = "idx";
    private static final String PARAM_TYPE = "type";
    private static final String PARAM_NAME = "name";

    private ExtensionActiveScan controller = null;

    public ActiveScanAPI(ExtensionActiveScan controller) {
        this.controller = controller;
        this.addApiAction(
                new ApiAction(
                        ACTION_SCAN,
                        null,
                        new String[] {
                            PARAM_URL,
                            PARAM_RECURSE,
                            PARAM_JUST_IN_SCOPE,
                            PARAM_SCAN_POLICY_NAME,
                            PARAM_METHOD,
                            PARAM_POST_DATA,
                            PARAM_CONTEXT_ID
                        }));
        this.addApiAction(
                new ApiAction(
                        ACTION_SCAN_AS_USER,
                        null,
                        new String[] {
                            PARAM_URL,
                            PARAM_CONTEXT_ID,
                            PARAM_USER_ID,
                            PARAM_RECURSE,
                            PARAM_SCAN_POLICY_NAME,
                            PARAM_METHOD,
                            PARAM_POST_DATA
                        }));
        this.addApiAction(new ApiAction(ACTION_PAUSE_SCAN, new String[] {PARAM_SCAN_ID}));
        this.addApiAction(new ApiAction(ACTION_RESUME_SCAN, new String[] {PARAM_SCAN_ID}));
        this.addApiAction(new ApiAction(ACTION_STOP_SCAN, new String[] {PARAM_SCAN_ID}));
        this.addApiAction(new ApiAction(ACTION_REMOVE_SCAN, new String[] {PARAM_SCAN_ID}));
        this.addApiAction(new ApiAction(ACTION_PAUSE_ALL_SCANS));
        this.addApiAction(new ApiAction(ACTION_RESUME_ALL_SCANS));
        this.addApiAction(new ApiAction(ACTION_STOP_ALL_SCANS));
        this.addApiAction(new ApiAction(ACTION_REMOVE_ALL_SCANS));
        this.addApiAction(new ApiAction(ACTION_CLEAR_EXCLUDED_FROM_SCAN));
        this.addApiAction(new ApiAction(ACTION_EXCLUDE_FROM_SCAN, new String[] {PARAM_REGEX}));
        this.addApiAction(
                new ApiAction(
                        ACTION_ENABLE_ALL_SCANNERS, null, new String[] {PARAM_SCAN_POLICY_NAME}));
        this.addApiAction(
                new ApiAction(
                        ACTION_DISABLE_ALL_SCANNERS, null, new String[] {PARAM_SCAN_POLICY_NAME}));
        this.addApiAction(
                new ApiAction(
                        ACTION_ENABLE_SCANNERS,
                        new String[] {PARAM_IDS},
                        new String[] {PARAM_SCAN_POLICY_NAME}));
        this.addApiAction(
                new ApiAction(
                        ACTION_DISABLE_SCANNERS,
                        new String[] {PARAM_IDS},
                        new String[] {PARAM_SCAN_POLICY_NAME}));
        this.addApiAction(
                new ApiAction(
                        ACTION_SET_ENABLED_POLICIES,
                        new String[] {PARAM_IDS},
                        new String[] {PARAM_SCAN_POLICY_NAME}));
        this.addApiAction(
                new ApiAction(
                        ACTION_SET_POLICY_ATTACK_STRENGTH,
                        new String[] {PARAM_ID, PARAM_ATTACK_STRENGTH},
                        new String[] {PARAM_SCAN_POLICY_NAME}));
        this.addApiAction(
                new ApiAction(
                        ACTION_SET_POLICY_ALERT_THRESHOLD,
                        new String[] {PARAM_ID, PARAM_ALERT_THRESHOLD},
                        new String[] {PARAM_SCAN_POLICY_NAME}));
        this.addApiAction(
                new ApiAction(
                        ACTION_SET_SCANNER_ATTACK_STRENGTH,
                        new String[] {PARAM_ID, PARAM_ATTACK_STRENGTH},
                        new String[] {PARAM_SCAN_POLICY_NAME}));
        this.addApiAction(
                new ApiAction(
                        ACTION_SET_SCANNER_ALERT_THRESHOLD,
                        new String[] {PARAM_ID, PARAM_ALERT_THRESHOLD},
                        new String[] {PARAM_SCAN_POLICY_NAME}));
        this.addApiAction(
                new ApiAction(
                        ACTION_ADD_SCAN_POLICY,
                        new String[] {PARAM_SCAN_POLICY_NAME},
                        new String[] {PARAM_ALERT_THRESHOLD, PARAM_ATTACK_STRENGTH}));
        this.addApiAction(
                new ApiAction(ACTION_REMOVE_SCAN_POLICY, new String[] {PARAM_SCAN_POLICY_NAME}));
        this.addApiAction(
                new ApiAction(
                        ACTION_UPDATE_SCAN_POLICY,
                        new String[] {PARAM_SCAN_POLICY_NAME},
                        new String[] {PARAM_ALERT_THRESHOLD, PARAM_ATTACK_STRENGTH}));
        this.addApiAction(new ApiAction(ACTION_IMPORT_SCAN_POLICY, new String[] {PARAM_PATH}));

        this.addApiAction(
                new ApiAction(
                        ACTION_ADD_EXCLUDED_PARAM,
                        new String[] {PARAM_NAME},
                        new String[] {PARAM_TYPE, PARAM_URL}));
        this.addApiAction(
                new ApiAction(
                        ACTION_MODIFY_EXCLUDED_PARAM,
                        new String[] {PARAM_IDX},
                        new String[] {PARAM_NAME, PARAM_TYPE, PARAM_URL}));
        this.addApiAction(new ApiAction(ACTION_REMOVE_EXCLUDED_PARAM, new String[] {PARAM_IDX}));

        this.addApiAction(
                new ApiAction(ACTION_SKIP_SCANNER, new String[] {PARAM_SCAN_ID, PARAM_SCANNER_ID}));

        this.addApiView(new ApiView(VIEW_STATUS, null, new String[] {PARAM_SCAN_ID}));
        this.addApiView(new ApiView(VIEW_SCAN_PROGRESS, null, new String[] {PARAM_SCAN_ID}));
        this.addApiView(new ApiView(VIEW_MESSAGES_IDS, new String[] {PARAM_SCAN_ID}));
        this.addApiView(new ApiView(VIEW_ALERTS_IDS, new String[] {PARAM_SCAN_ID}));
        this.addApiView(new ApiView(VIEW_SCANS));
        this.addApiView(new ApiView(VIEW_SCAN_POLICY_NAMES));
        this.addApiView(new ApiView(VIEW_EXCLUDED_FROM_SCAN));
        this.addApiView(
                new ApiView(
                        VIEW_SCANNERS,
                        null,
                        new String[] {PARAM_SCAN_POLICY_NAME, PARAM_CATEGORY_ID}));
        this.addApiView(
                new ApiView(
                        VIEW_POLICIES,
                        null,
                        new String[] {PARAM_SCAN_POLICY_NAME, PARAM_CATEGORY_ID}));
        this.addApiView(new ApiView(VIEW_ATTACK_MODE_QUEUE));

        this.addApiView(new ApiView(VIEW_EXCLUDED_PARAMS));
        ApiView view = new ApiView(VIEW_OPTION_EXCLUDED_PARAM_LIST);
        view.setDeprecated(true);
        this.addApiView(view);
        this.addApiView(new ApiView(VIEW_EXCLUDED_PARAM_TYPES));
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @SuppressWarnings({"fallthrough"})
    @Override
    public ApiResponse handleApiAction(String name, JSONObject params) throws ApiException {
        log.debug("handleApiAction {} {}", name, params);
        ScanPolicy policy;
        int categoryId;

        User user = null;
        Context context = null;
        try {
            switch (name) {
                case ACTION_SCAN_AS_USER:
                    // These are not mandatory parameters on purpose, to keep the same order
                    // of the parameters while having PARAM_URL as (now) optional.
                    validateParamExists(params, PARAM_CONTEXT_ID);
                    validateParamExists(params, PARAM_USER_ID);

                    int userID = ApiUtils.getIntParam(params, PARAM_USER_ID);
                    ExtensionUserManagement usersExtension =
                            Control.getSingleton()
                                    .getExtensionLoader()
                                    .getExtension(ExtensionUserManagement.class);
                    if (usersExtension == null) {
                        throw new ApiException(Type.NO_IMPLEMENTOR, ExtensionUserManagement.NAME);
                    }
                    context = ApiUtils.getContextByParamId(params, PARAM_CONTEXT_ID);
                    user =
                            usersExtension
                                    .getContextUserAuthManager(context.getId())
                                    .getUserById(userID);
                    if (user == null) {
                        throw new ApiException(Type.USER_NOT_FOUND, PARAM_USER_ID);
                    }

                    // Same behaviour but with addition of the user to scan
                    // $FALL-THROUGH$
                case ACTION_SCAN:
                    String url = ApiUtils.getOptionalStringParam(params, PARAM_URL);

                    if (context == null
                            && params.has(PARAM_CONTEXT_ID)
                            && !params.getString(PARAM_CONTEXT_ID).isEmpty()) {
                        context = ApiUtils.getContextByParamId(params, PARAM_CONTEXT_ID);
                    }

                    boolean scanJustInScope =
                            context != null
                                    ? false
                                    : this.getParam(params, PARAM_JUST_IN_SCOPE, false);

                    String policyName = null;
                    policy = null;

                    try {
                        policyName = params.getString(PARAM_SCAN_POLICY_NAME);
                    } catch (Exception e1) {
                        // Ignore
                    }
                    try {
                        if (policyName != null && policyName.length() > 0) {
                            // Not specified, use the default one
                            log.debug("handleApiAction scan policy ={}", policyName);
                            policy = controller.getPolicyManager().getPolicy(policyName);
                        }
                    } catch (ConfigurationException e) {
                        throw new ApiException(
                                ApiException.Type.DOES_NOT_EXIST, PARAM_SCAN_POLICY_NAME);
                    }
                    String method = this.getParam(params, PARAM_METHOD, HttpRequestHeader.GET);
                    if (method.trim().length() == 0) {
                        method = HttpRequestHeader.GET;
                    }
                    if (!Arrays.asList(HttpRequestHeader.METHODS).contains(method)) {
                        throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, PARAM_METHOD);
                    }

                    int scanId =
                            scanURL(
                                    url,
                                    user,
                                    this.getParam(params, PARAM_RECURSE, true),
                                    scanJustInScope,
                                    method,
                                    this.getParam(params, PARAM_POST_DATA, ""),
                                    policy,
                                    context);

                    return new ApiResponseElement(name, Integer.toString(scanId));

                case ACTION_PAUSE_SCAN:
                    getActiveScan(params).pauseScan();
                    break;
                case ACTION_RESUME_SCAN:
                    getActiveScan(params).resumeScan();
                    break;
                case ACTION_STOP_SCAN:
                    getActiveScan(params).stopScan();
                    break;
                case ACTION_REMOVE_SCAN:
                    ActiveScan activeScan = controller.removeScan(params.getInt(PARAM_SCAN_ID));
                    if (activeScan == null) {
                        throw new ApiException(ApiException.Type.DOES_NOT_EXIST, PARAM_SCAN_ID);
                    }
                    break;
                case ACTION_PAUSE_ALL_SCANS:
                    controller.pauseAllScans();
                    break;
                case ACTION_RESUME_ALL_SCANS:
                    controller.resumeAllScans();
                    break;
                case ACTION_STOP_ALL_SCANS:
                    controller.stopAllScans();
                    break;
                case ACTION_REMOVE_ALL_SCANS:
                    controller.removeAllScans();
                    break;
                case ACTION_CLEAR_EXCLUDED_FROM_SCAN:
                    try {
                        Session session = Model.getSingleton().getSession();
                        session.setExcludeFromScanRegexs(new ArrayList<>());
                    } catch (DatabaseException e) {
                        log.error(e.getMessage(), e);
                        throw new ApiException(ApiException.Type.INTERNAL_ERROR, e.getMessage());
                    }
                    break;
                case ACTION_EXCLUDE_FROM_SCAN:
                    String regex = params.getString(PARAM_REGEX);
                    try {
                        Session session = Model.getSingleton().getSession();
                        session.addExcludeFromScanRegexs(regex);
                    } catch (DatabaseException e) {
                        log.error(e.getMessage(), e);
                        throw new ApiException(ApiException.Type.INTERNAL_ERROR, e.getMessage());
                    } catch (PatternSyntaxException e) {
                        throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, PARAM_REGEX);
                    }
                    break;
                case ACTION_ENABLE_ALL_SCANNERS:
                    policy = getScanPolicyFromParams(params);
                    policy.getPluginFactory().setAllPluginEnabled(true);
                    policy.save();
                    break;
                case ACTION_DISABLE_ALL_SCANNERS:
                    policy = getScanPolicyFromParams(params);
                    policy.getPluginFactory().setAllPluginEnabled(false);
                    policy.save();
                    break;
                case ACTION_ENABLE_SCANNERS:
                    policy = getScanPolicyFromParams(params);
                    setScannersEnabled(policy, getParam(params, PARAM_IDS, "").split(","), true);
                    policy.save();
                    break;
                case ACTION_DISABLE_SCANNERS:
                    policy = getScanPolicyFromParams(params);
                    setScannersEnabled(policy, getParam(params, PARAM_IDS, "").split(","), false);
                    policy.save();
                    break;
                case ACTION_SET_ENABLED_POLICIES:
                    policy = getScanPolicyFromParams(params);
                    setEnabledCategories(policy, getParam(params, PARAM_IDS, "").split(","));
                    policy.save();
                    break;
                case ACTION_SET_POLICY_ATTACK_STRENGTH:
                    categoryId = getParam(params, PARAM_ID, -1);
                    verifyCategoryId(categoryId, PARAM_ID);
                    policy = getScanPolicyFromParams(params);
                    Plugin.AttackStrength attackStrength = getAttackStrengthFromParamAttack(params);
                    updateRulesOfCategoryInPolicy(
                            categoryId, policy, s -> s.setAttackStrength(attackStrength));
                    break;
                case ACTION_SET_POLICY_ALERT_THRESHOLD:
                    categoryId = getParam(params, PARAM_ID, -1);
                    verifyCategoryId(categoryId, PARAM_ID);
                    policy = getScanPolicyFromParams(params);
                    Plugin.AlertThreshold alertThreshold1 =
                            getAlertThresholdFromParamAlertThreshold(params);
                    updateRulesOfCategoryInPolicy(
                            categoryId, policy, s -> s.setAlertThreshold(alertThreshold1));
                    break;
                case ACTION_SET_SCANNER_ATTACK_STRENGTH:
                    policy = getScanPolicyFromParams(params);
                    Plugin scanner =
                            getScannerFromId(policy, getParam(params, PARAM_ID, -1), PARAM_ID);
                    scanner.setAttackStrength(getAttackStrengthFromParamAttack(params));
                    policy.save();
                    break;
                case ACTION_SET_SCANNER_ALERT_THRESHOLD:
                    policy = getScanPolicyFromParams(params);
                    AlertThreshold alertThreshold2 =
                            getAlertThresholdFromParamAlertThreshold(params);
                    getScannerFromId(policy, getParam(params, PARAM_ID, -1), PARAM_ID)
                            .setAlertThreshold(alertThreshold2);
                    policy.save();
                    break;
                case ACTION_ADD_SCAN_POLICY:
                    String newPolicyName = params.getString(PARAM_SCAN_POLICY_NAME);
                    if (controller.getPolicyManager().getAllPolicyNames().contains(newPolicyName)) {
                        throw new ApiException(
                                ApiException.Type.ALREADY_EXISTS, PARAM_SCAN_POLICY_NAME);
                    }
                    if (!controller.getPolicyManager().isLegalPolicyName(newPolicyName)) {
                        throw new ApiException(
                                ApiException.Type.ILLEGAL_PARAMETER, PARAM_SCAN_POLICY_NAME);
                    }
                    policy = controller.getPolicyManager().getTemplatePolicy();
                    policy.setName(newPolicyName);
                    setAlertThreshold(policy, params);
                    setAttackStrength(policy, params);
                    controller.getPolicyManager().savePolicy(policy);
                    break;
                case ACTION_REMOVE_SCAN_POLICY:
                    // Check it exists
                    policy = getScanPolicyFromParams(params);
                    if (controller.getPolicyManager().getAllPolicyNames().size() == 1) {
                        // Dont remove the last one
                        throw new ApiException(
                                ApiException.Type.ILLEGAL_PARAMETER,
                                "You are not allowed to remove the last scan policy");
                    }
                    controller.getPolicyManager().deletePolicy(policy.getName());
                    break;
                case ACTION_UPDATE_SCAN_POLICY:
                    policy = getScanPolicyFromParams(params);
                    if (!isParamsChanged(policy, params)) {
                        break;
                    }
                    updateAlertThreshold(policy, params);
                    updateAttackStrength(policy, params);
                    controller.getPolicyManager().savePolicy(policy);
                    break;
                case ACTION_IMPORT_SCAN_POLICY:
                    File file = new File(params.getString(PARAM_PATH));
                    if (!file.exists()) {
                        throw new ApiException(ApiException.Type.DOES_NOT_EXIST, PARAM_PATH);
                    }
                    if (!file.isFile()) {
                        throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, PARAM_PATH);
                    }

                    ScanPolicy scanPolicy;
                    try {
                        scanPolicy = new ScanPolicy(new ZapXmlConfiguration(file));
                    } catch (IllegalArgumentException | ConfigurationException e) {
                        throw new ApiException(
                                ApiException.Type.BAD_EXTERNAL_DATA, file.toString(), e);
                    }

                    String scanPolicyName = scanPolicy.getName();
                    if (scanPolicyName.isEmpty()) {
                        scanPolicyName = file.getName();
                    }
                    if (controller
                            .getPolicyManager()
                            .getAllPolicyNames()
                            .contains(scanPolicyName)) {
                        throw new ApiException(ApiException.Type.ALREADY_EXISTS, scanPolicyName);
                    }
                    if (!controller.getPolicyManager().isLegalPolicyName(scanPolicyName)) {
                        throw new ApiException(ApiException.Type.BAD_EXTERNAL_DATA, scanPolicyName);
                    }

                    try {
                        controller.getPolicyManager().savePolicy(scanPolicy);
                    } catch (ConfigurationException e) {
                        throw new ApiException(ApiException.Type.INTERNAL_ERROR, e);
                    }
                    break;
                case ACTION_ADD_EXCLUDED_PARAM:
                    int type = getParam(params, PARAM_TYPE, NameValuePair.TYPE_UNDEFINED);
                    if (!ScannerParamFilter.getTypes().containsKey(type)) {
                        throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, PARAM_TYPE);
                    }

                    url = getParam(params, PARAM_URL, "*");
                    if (url.isEmpty()) {
                        url = "*";
                    }

                    ScannerParamFilter excludedParam =
                            new ScannerParamFilter(params.getString(PARAM_NAME), type, url);

                    List<ScannerParamFilter> excludedParams =
                            new ArrayList<>(controller.getScannerParam().getExcludedParamList());
                    excludedParams.add(excludedParam);
                    controller.getScannerParam().setExcludedParamList(excludedParams);
                    break;
                case ACTION_MODIFY_EXCLUDED_PARAM:
                    try {
                        int idx = params.getInt(PARAM_IDX);
                        if (idx < 0
                                || idx
                                        >= controller
                                                .getScannerParam()
                                                .getExcludedParamList()
                                                .size()) {
                            throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, PARAM_IDX);
                        }

                        ScannerParamFilter oldExcludedParam =
                                controller.getScannerParam().getExcludedParamList().get(idx);
                        String epName =
                                getParam(params, PARAM_NAME, oldExcludedParam.getParamName());
                        if (epName.isEmpty()) {
                            epName = oldExcludedParam.getParamName();
                        }

                        type = getParam(params, PARAM_TYPE, oldExcludedParam.getType());
                        if (!ScannerParamFilter.getTypes().containsKey(type)) {
                            throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, PARAM_TYPE);
                        }

                        url = getParam(params, PARAM_URL, oldExcludedParam.getWildcardedUrl());
                        if (url.isEmpty()) {
                            url = "*";
                        }

                        ScannerParamFilter newExcludedParam =
                                new ScannerParamFilter(epName, type, url);
                        if (oldExcludedParam.equals(newExcludedParam)) {
                            break;
                        }

                        excludedParams =
                                new ArrayList<>(
                                        controller.getScannerParam().getExcludedParamList());
                        excludedParams.set(idx, newExcludedParam);
                        controller.getScannerParam().setExcludedParamList(excludedParams);
                    } catch (JSONException e) {
                        throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, PARAM_IDX, e);
                    }
                    break;
                case ACTION_REMOVE_EXCLUDED_PARAM:
                    try {
                        int idx = params.getInt(PARAM_IDX);
                        if (idx < 0
                                || idx
                                        >= controller
                                                .getScannerParam()
                                                .getExcludedParamList()
                                                .size()) {
                            throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, PARAM_IDX);
                        }

                        excludedParams =
                                new ArrayList<>(
                                        controller.getScannerParam().getExcludedParamList());
                        excludedParams.remove(idx);
                        controller.getScannerParam().setExcludedParamList(excludedParams);
                    } catch (JSONException e) {
                        throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, PARAM_IDX, e);
                    }
                    break;
                case ACTION_SKIP_SCANNER:
                    int pluginId = getParam(params, PARAM_SCANNER_ID, -1);
                    if (pluginId == -1) {
                        throw new ApiException(
                                ApiException.Type.ILLEGAL_PARAMETER, PARAM_SCANNER_ID);
                    }

                    String reason =
                            Constant.messages.getString("ascan.progress.label.skipped.reason.user");
                    getActiveScan(params)
                            .getHostProcesses()
                            .forEach(hp -> hp.pluginSkipped(pluginId, reason));
                    break;
                default:
                    throw new ApiException(ApiException.Type.BAD_ACTION);
            }
        } catch (ConfigurationException e) {
            throw new ApiException(ApiException.Type.INTERNAL_ERROR, e.getMessage());
        }
        return ApiResponseElement.OK;
    }

    private void setAlertThreshold(ScanPolicy policy, JSONObject params) throws ApiException {
        if (isParamExists(params, PARAM_ALERT_THRESHOLD)) {
            try {
                policy.setDefaultThreshold(getAlertThresholdFromParamAlertThreshold(params));
            } catch (IllegalArgumentException e) {
                throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, e.getMessage(), e);
            }
        }
    }

    private void setAttackStrength(ScanPolicy policy, JSONObject params) throws ApiException {
        if (isParamExists(params, PARAM_ATTACK_STRENGTH)) {
            try {
                policy.setDefaultStrength(getAttackStrengthFromParamAttack(params));
            } catch (IllegalArgumentException e) {
                throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, e.getMessage(), e);
            }
        }
    }

    private boolean isParamsChanged(ScanPolicy policy, JSONObject params) throws ApiException {
        return isAlertThresholdChanged(policy, params) || isAttackStrengthChanged(policy, params);
    }

    private boolean isAlertThresholdChanged(ScanPolicy policy, JSONObject params)
            throws ApiException {
        if (!isParamExists(params, PARAM_ALERT_THRESHOLD)) {
            return false;
        }

        AlertThreshold updatedAlertThreshold = getAlertThresholdFromParamAlertThreshold(params);
        AlertThreshold currentThreshold = policy.getDefaultThreshold();
        return !currentThreshold.equals(updatedAlertThreshold);
    }

    private boolean isAttackStrengthChanged(ScanPolicy policy, JSONObject params)
            throws ApiException {
        if (!isParamExists(params, PARAM_ATTACK_STRENGTH)) {
            return false;
        }

        Plugin.AttackStrength updatedAttackStrength = getAttackStrengthFromParamAttack(params);
        Plugin.AttackStrength currentAttackStrength = policy.getDefaultStrength();
        return !currentAttackStrength.equals(updatedAttackStrength);
    }

    private void updateAlertThreshold(ScanPolicy policy, JSONObject params) throws ApiException {
        if (isAlertThresholdChanged(policy, params)) {
            policy.setDefaultThreshold(getAlertThresholdFromParamAlertThreshold(params));
        }
    }

    private void updateAttackStrength(ScanPolicy policy, JSONObject params) throws ApiException {
        if (isAttackStrengthChanged(policy, params)) {
            policy.setDefaultStrength(getAttackStrengthFromParamAttack(params));
        }
    }

    private boolean isParamExists(JSONObject params, String key) {
        return params.has(key) && StringUtils.isNotBlank(params.getString(key));
    }

    private ScanPolicy getScanPolicyFromParams(JSONObject params) throws ApiException {
        String policyName = null;
        try {
            policyName = params.getString(PARAM_SCAN_POLICY_NAME);
        } catch (Exception e1) {
            // Ignore
        }
        if (policyName == null || policyName.length() == 0) {
            // Not specified, use the default one
            return controller.getPolicyManager().getDefaultScanPolicy();
        }
        try {
            return controller.getPolicyManager().getPolicy(policyName);
        } catch (ConfigurationException e) {
            throw new ApiException(ApiException.Type.DOES_NOT_EXIST, PARAM_SCAN_POLICY_NAME);
        }
    }

    /**
     * Returns a {@link ActiveScan} from the available active scans or the last active scan. If a
     * scan ID ( {@link #PARAM_SCAN_ID}) is present in the given {@code params} it will be used to
     * the get the {@code ActiveScan} from the available active scans, otherwise it's returned the
     * last active scan.
     *
     * @param params the parameters of the API call
     * @return the {@code ActiveScan} with the given scan ID or, if not present, the last active
     *     scan
     * @throws ApiException if there's no scan with the given scan ID
     */
    private ActiveScan getActiveScan(JSONObject params) throws ApiException {
        int id = getParam(params, PARAM_SCAN_ID, -1);

        ActiveScan activeScan = null;

        if (id == -1) {
            activeScan = controller.getLastScan();
        } else {
            activeScan = controller.getScan(id);
        }

        if (activeScan == null) {
            throw new ApiException(ApiException.Type.DOES_NOT_EXIST, PARAM_SCAN_ID);
        }

        return activeScan;
    }

    private void setScannersEnabled(ScanPolicy policy, String[] ids, boolean enabled)
            throws ApiException {
        List<String> unknownIds = null;
        try {
            for (String idString : ids) {
                String idTrimmed = idString.trim();
                int id = Integer.parseInt(idTrimmed);
                Plugin scanner = policy.getPluginFactory().getPlugin(id);
                if (scanner != null) {
                    scanner.setEnabled(enabled);
                } else {
                    if (unknownIds == null) {
                        unknownIds = new ArrayList<>();
                    }
                    unknownIds.add(idTrimmed);
                }
            }
        } catch (NumberFormatException e) {
            log.warn("Failed to parse scanner ID: ", e);
            throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, e.getMessage(), e);
        }

        if (unknownIds != null) {
            throw new ApiException(ApiException.Type.DOES_NOT_EXIST, "IDs: " + unknownIds);
        }
    }

    private void setEnabledCategories(ScanPolicy policy, String[] ids)
            throws ApiException, ConfigurationException {
        try {
            policy.getPluginFactory().setAllPluginEnabled(false);
            for (String id : ids) {
                int categoryId = Integer.parseInt(id.trim());
                verifyCategoryId(categoryId, id.trim());
                updateRulesOfCategoryInPolicy(categoryId, policy, s -> s.setEnabled(true));
            }
        } catch (NumberFormatException e) {
            log.warn("Failed to parse category ID: ", e);
            throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, e.getMessage(), e);
        }
    }

    private static void verifyCategoryId(int categoryId, String paramName) throws ApiException {
        if (categoryId < 0) {
            throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, paramName);
        }
        if (!Arrays.asList(Category.getAllNames()).contains(Category.getName(categoryId))) {
            throw new ApiException(ApiException.Type.DOES_NOT_EXIST, Integer.toString(categoryId));
        }
    }

    private void updateRulesOfCategoryInPolicy(
            int categoryId, ScanPolicy policy, Consumer<Plugin> consumer)
            throws ConfigurationException {
        for (Plugin scanner : policy.getPluginFactory().getAllPlugin()) {
            if (scanner.getCategory() == categoryId) {
                consumer.accept(scanner);
            }
        }
        policy.save();
    }

    private Plugin.AttackStrength getAttackStrengthFromParamAttack(JSONObject params)
            throws ApiException {
        final String paramAttackStrength =
                params.getString(PARAM_ATTACK_STRENGTH).trim().toUpperCase();
        try {
            return Plugin.AttackStrength.valueOf(paramAttackStrength);
        } catch (IllegalArgumentException e) {
            throw new ApiException(ApiException.Type.DOES_NOT_EXIST, PARAM_ATTACK_STRENGTH);
        }
    }

    private Plugin.AlertThreshold getAlertThresholdFromParamAlertThreshold(JSONObject params)
            throws ApiException {
        final String paramAlertThreshold =
                params.getString(PARAM_ALERT_THRESHOLD).trim().toUpperCase();
        try {
            return Plugin.AlertThreshold.valueOf(paramAlertThreshold);
        } catch (IllegalArgumentException e) {
            throw new ApiException(ApiException.Type.DOES_NOT_EXIST, PARAM_ALERT_THRESHOLD);
        }
    }

    private Plugin getScannerFromId(ScanPolicy policy, int id, String paramName)
            throws ApiException {
        if (id < 0) {
            throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, paramName);
        }
        Plugin scanner = policy.getPluginFactory().getPlugin(id);
        if (scanner == null) {
            throw new ApiException(ApiException.Type.DOES_NOT_EXIST, Integer.toString(id));
        }
        return scanner;
    }

    private int scanURL(
            String url,
            User user,
            boolean scanChildren,
            boolean scanJustInScope,
            String method,
            String postData,
            ScanPolicy policy,
            Context context)
            throws ApiException {

        boolean useUrl = true;
        if (url == null || url.isEmpty()) {
            if (context == null || !context.hasNodesInContextFromSiteTree()) {
                throw new ApiException(Type.MISSING_PARAMETER, PARAM_URL);
            }
            useUrl = false;
        } else if (context != null && !context.isInContext(url)) {
            throw new ApiException(Type.URL_NOT_IN_CONTEXT, PARAM_URL);
        }

        StructuralNode node = null;
        if (useUrl) {
            URI startURI;
            try {
                if (scanChildren && url.endsWith("/")) {
                    // Always choose the non leaf node if scanChildren option selected
                    url = url.substring(0, url.length() - 1);
                }
                startURI = new URI(url, true);
            } catch (URIException e) {
                throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, PARAM_URL, e);
            }
            String scheme = startURI.getScheme();
            if (scheme == null
                    || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new ApiException(
                        ApiException.Type.ILLEGAL_PARAMETER,
                        PARAM_URL + " does not have a scheme.");
            }

            try {
                Model model = Model.getSingleton();
                node = SessionStructure.find(model, startURI, method, postData);
                if (node == null && "GET".equalsIgnoreCase(method)) {
                    // Check if there's a non-leaf node that matches the URI, to scan the subtree.
                    // (GET is the default method, but non-leaf nodes do not have any method.)
                    node = SessionStructure.find(model, startURI, null, postData);
                }
            } catch (Exception e) {
                throw new ApiException(ApiException.Type.INTERNAL_ERROR, e);
            }

            if (node == null) {
                throw new ApiException(ApiException.Type.URL_NOT_FOUND);
            }
        }
        Target target;
        if (useUrl) {
            target = new Target(node);
            target.setContext(context);
        } else {
            target = new Target(context);
        }
        target.setRecurse(scanChildren);
        target.setInScopeOnly(scanJustInScope);

        switch (Control.getSingleton().getMode()) {
            case safe:
                throw new ApiException(ApiException.Type.MODE_VIOLATION);
            case protect:
                if ((useUrl && !Model.getSingleton().getSession().isInScope(url))
                        || (context != null && !context.isInScope())) {
                    throw new ApiException(ApiException.Type.MODE_VIOLATION);
                }
                // No problem
                break;
            case standard:
                // No problem
                break;
            case attack:
                // No problem
                break;
        }

        Object[] objs = new Object[] {};
        if (policy != null) {
            objs = new Object[] {policy};
        }

        return controller.startScan(null, target, user, objs);
    }

    @Override
    public ApiResponse handleApiView(String name, JSONObject params) throws ApiException {
        ApiResponse result;
        ActiveScan activeScan = null;
        ScanPolicy policy;
        int categoryId;

        switch (name) {
            case VIEW_STATUS:
                activeScan = getActiveScan(params);
                int progress = 0;
                if (activeScan.isStopped()) {
                    progress = 100;
                } else {
                    progress = activeScan.getProgress();
                }
                result = new ApiResponseElement(name, String.valueOf(progress));
                break;
            case VIEW_SCANS:
                ApiResponseList resultList = new ApiResponseList(name);
                for (ActiveScan scan : controller.getAllScans()) {
                    Map<String, String> map = new HashMap<>();
                    map.put("id", Integer.toString(scan.getScanId()));
                    map.put("progress", Integer.toString(scan.getProgress()));
                    map.put("state", scan.getState().name());
                    map.put("reqCount", Integer.toString(scan.getTotalRequests()));
                    map.put("alertCount", Integer.toString(scan.getAlertsIds().size()));
                    map.put("newAlertCount", Integer.toString(scan.getTotalNewAlerts()));
                    resultList.addItem(new ApiResponseSet<>("scan", map));
                }
                result = resultList;
                break;
            case VIEW_SCAN_PROGRESS:
                resultList = new ApiResponseList(name);
                activeScan = getActiveScan(params);
                for (HostProcess hp : activeScan.getHostProcesses()) {
                    ApiResponseList hpList = new ApiResponseList("HostProcess");
                    resultList.addItem(new ApiResponseElement("id", hp.getHostAndPort()));

                    for (Plugin plugin : hp.getCompleted()) {
                        long timeTaken =
                                plugin.getTimeFinished().getTime()
                                        - plugin.getTimeStarted().getTime();
                        int reqs = hp.getPluginRequestCount(plugin.getId());
                        int alertCount = hp.getPluginStats(plugin.getId()).getAlertCount();
                        hpList.addItem(
                                createPluginProgressEntry(
                                        plugin,
                                        getStatus(hp, plugin, "Complete"),
                                        timeTaken,
                                        reqs,
                                        alertCount));
                    }

                    for (Plugin plugin : hp.getRunning()) {
                        int pc = hp.getTestCurrentCount(plugin) * 100 / hp.getTestTotalCount();
                        // Make sure not return 100 (or more) if still running...
                        // That might happen if more nodes are being scanned that the ones
                        // enumerated at the beginning.
                        if (pc >= 100) {
                            pc = 99;
                        }
                        long timeTaken = new Date().getTime() - plugin.getTimeStarted().getTime();
                        int reqs = hp.getPluginRequestCount(plugin.getId());
                        int alertCount = hp.getPluginStats(plugin.getId()).getAlertCount();
                        hpList.addItem(
                                createPluginProgressEntry(
                                        plugin, pc + "%", timeTaken, reqs, alertCount));
                    }

                    for (Plugin plugin : hp.getPending()) {
                        hpList.addItem(
                                createPluginProgressEntry(
                                        plugin, getStatus(hp, plugin, "Pending"), 0, 0, 0));
                    }
                    resultList.addItem(hpList);
                }
                result = resultList;
                break;
            case VIEW_MESSAGES_IDS:
                resultList = new ApiResponseList(name);
                activeScan = getActiveScan(params);
                synchronized (activeScan.getMessagesIds()) {
                    for (Integer id : activeScan.getMessagesIds()) {
                        resultList.addItem(new ApiResponseElement("id", id.toString()));
                    }
                }
                result = resultList;
                break;
            case VIEW_ALERTS_IDS:
                resultList = new ApiResponseList(name);
                activeScan = getActiveScan(params);
                synchronized (activeScan.getAlertsIds()) {
                    for (Integer id : activeScan.getAlertsIds()) {
                        resultList.addItem(new ApiResponseElement("id", id.toString()));
                    }
                }
                result = resultList;
                break;
            case VIEW_EXCLUDED_FROM_SCAN:
                result = new ApiResponseList(name);
                Session session = Model.getSingleton().getSession();
                List<String> regexs = session.getExcludeFromScanRegexs();
                for (String regex : regexs) {
                    ((ApiResponseList) result).addItem(new ApiResponseElement("regex", regex));
                }
                break;
            case VIEW_SCANNERS:
                policy = getScanPolicyFromParams(params);
                List<Plugin> scanners = policy.getPluginFactory().getAllPlugin();

                categoryId = getParam(params, PARAM_CATEGORY_ID, -1);
                if (categoryId != -1) {
                    verifyCategoryId(categoryId, PARAM_CATEGORY_ID);
                }
                resultList = new ApiResponseList(name);
                for (Plugin scanner : scanners) {
                    if (categoryId == -1 || categoryId == scanner.getCategory()) {
                        resultList.addItem(new ScannerApiResponse(policy, scanner));
                    }
                }

                result = resultList;
                break;
            case VIEW_POLICIES:
                policy = getScanPolicyFromParams(params);
                String[] policies = Category.getAllNames();

                resultList = new ApiResponseList(name);
                for (String pluginName : policies) {
                    categoryId = Category.getCategory(pluginName);
                    Plugin.AttackStrength attackStrength =
                            getPolicyAttackStrength(policy, categoryId);
                    Plugin.AlertThreshold alertThreshold =
                            getPolicyAlertThreshold(policy, categoryId);
                    Map<String, String> map = new HashMap<>();
                    map.put("id", String.valueOf(categoryId));
                    map.put("name", pluginName);
                    map.put(
                            "attackStrength",
                            attackStrength == null ? "" : String.valueOf(attackStrength));
                    map.put(
                            "alertThreshold",
                            alertThreshold == null ? "" : String.valueOf(alertThreshold));
                    map.put("enabled", String.valueOf(isPolicyEnabled(policy, categoryId)));
                    resultList.addItem(new ApiResponseSet<>("policy", map));
                }

                result = resultList;
                break;
            case VIEW_SCAN_POLICY_NAMES:
                resultList = new ApiResponseList(name);
                for (String policyName : controller.getPolicyManager().getAllPolicyNames()) {
                    resultList.addItem(new ApiResponseElement("policy", policyName));
                }
                result = resultList;
                break;
            case VIEW_ATTACK_MODE_QUEUE:
                result =
                        new ApiResponseElement(
                                name, String.valueOf(controller.getAttackModeStackSize()));
                break;
            case VIEW_OPTION_EXCLUDED_PARAM_LIST:
            case VIEW_EXCLUDED_PARAMS:
                resultList = new ApiResponseList(name);
                List<ScannerParamFilter> excludedParams =
                        controller.getScannerParam().getExcludedParamList();
                for (int i = 0; i < excludedParams.size(); i++) {
                    resultList.addItem(new ExcludedParamApiResponse(excludedParams.get(i), i));
                }
                result = resultList;
                break;
            case VIEW_EXCLUDED_PARAM_TYPES:
                resultList = new ApiResponseList(name);
                for (Entry<Integer, String> type : ScannerParamFilter.getTypes().entrySet()) {
                    Map<String, String> typeData = new HashMap<>();
                    typeData.put("id", Integer.toString(type.getKey()));
                    typeData.put("name", type.getValue());
                    resultList.addItem(new ApiResponseSet<>("type", typeData));
                }
                result = resultList;
                break;
            default:
                throw new ApiException(ApiException.Type.BAD_VIEW);
        }
        return result;
    }

    private static ApiResponseList createPluginProgressEntry(
            Plugin plugin, String status, long timeTaken, int requestCount, int alertCount) {
        ApiResponseList pList = new ApiResponseList("Plugin");
        pList.addItem(new ApiResponseElement("name", plugin.getName()));
        pList.addItem(new ApiResponseElement("id", Integer.toString(plugin.getId())));
        pList.addItem(new ApiResponseElement("quality", plugin.getStatus().toString()));
        pList.addItem(new ApiResponseElement("status", status));
        pList.addItem(new ApiResponseElement("timeInMs", Long.toString(timeTaken)));
        pList.addItem(new ApiResponseElement("reqCount", Integer.toString(requestCount)));
        pList.addItem(new ApiResponseElement("alertCount", Integer.toString(alertCount)));
        return pList;
    }

    private boolean isPolicyEnabled(ScanPolicy policy, int category) {
        for (Plugin scanner : policy.getPluginFactory().getAllPlugin()) {
            if (scanner.getCategory() == category && !scanner.isEnabled()) {
                return false;
            }
        }
        return true;
    }

    private Plugin.AttackStrength getPolicyAttackStrength(ScanPolicy policy, int categoryd) {
        Plugin.AttackStrength attackStrength = null;
        for (Plugin scanner : policy.getPluginFactory().getAllPlugin()) {
            if (scanner.getCategory() == categoryd) {
                if (attackStrength == null) {
                    attackStrength = scanner.getAttackStrength(true);
                } else if (!attackStrength.equals(scanner.getAttackStrength(true))) {
                    // Not all the same
                    return null;
                }
            }
        }
        return attackStrength;
    }

    private Plugin.AlertThreshold getPolicyAlertThreshold(ScanPolicy policy, int categoryId) {
        Plugin.AlertThreshold alertThreshold = null;
        for (Plugin scanner : policy.getPluginFactory().getAllPlugin()) {
            if (scanner.getCategory() == categoryId) {
                if (alertThreshold == null) {
                    alertThreshold = scanner.getAlertThreshold(true);
                } else if (!alertThreshold.equals(scanner.getAlertThreshold(true))) {
                    // Not all the same
                    return null;
                }
            }
        }
        return alertThreshold;
    }

    private static String getStatus(HostProcess hp, Plugin plugin, String defaultStatus) {
        if (!hp.isSkipped(plugin)) {
            return defaultStatus;
        }

        String skippedReason = hp.getSkippedReason(plugin);
        if (skippedReason == null) {
            return Constant.messages.getString("ascan.progress.label.skipped");
        }
        return Constant.messages.getString("ascan.progress.label.skippedWithReason", skippedReason);
    }

    private static class ExcludedParamApiResponse extends ApiResponse {

        private final Map<String, String> excludedParamData;
        private final ApiResponseSet<String> type;
        private final Map<String, String> typeData;

        public ExcludedParamApiResponse(ScannerParamFilter param, int idx) {
            super("excludedParam");

            excludedParamData = new HashMap<>();
            excludedParamData.put("idx", Integer.toString(idx));
            excludedParamData.put("parameter", param.getParamName());
            excludedParamData.put("url", param.getWildcardedUrl());

            typeData = new HashMap<>();
            typeData.put("id", Integer.toString(param.getType()));
            typeData.put("name", param.getTypeString());
            type = new ApiResponseSet<>("type", typeData);
        }

        @Override
        public void toXML(Document doc, Element parent) {
            parent.setAttribute("type", "set");
            for (Entry<String, String> val : excludedParamData.entrySet()) {
                Element el = doc.createElement(val.getKey());
                el.appendChild(doc.createTextNode(XMLStringUtil.escapeControlChrs(val.getValue())));
                parent.appendChild(el);
            }

            Element el = doc.createElement(type.getName());
            type.toXML(doc, el);
            parent.appendChild(el);
        }

        @Override
        public JSON toJSON() {
            JSONObject jo = new JSONObject();
            for (Entry<String, String> val : excludedParamData.entrySet()) {
                jo.put(val.getKey(), val.getValue());
            }
            jo.put(type.getName(), type.toJSON());
            return jo;
        }

        @Override
        public void toHTML(StringBuilder sb) {
            sb.append("<h2>" + this.getName() + "</h2>\n");
            sb.append("<table border=\"1\">\n");
            for (Entry<String, String> val : excludedParamData.entrySet()) {
                sb.append("<tr><td>\n");
                sb.append(val.getKey());
                sb.append("</td><td>\n");
                sb.append(StringEscapeUtils.escapeHtml(val.getValue()));
                sb.append("</td></tr>\n");
            }
            sb.append("<tr><td>\n");
            sb.append(type.getName());
            sb.append("</td><td>\n");
            sb.append("<table border=\"1\">\n");
            for (Entry<String, ?> val : typeData.entrySet()) {
                sb.append("<tr><td>\n");
                sb.append(StringEscapeUtils.escapeHtml(val.getKey()));
                sb.append("</td><td>\n");
                Object value = val.getValue();
                if (value != null) {
                    sb.append(StringEscapeUtils.escapeHtml(value.toString()));
                }
                sb.append("</td></tr>\n");
            }
            sb.append("</table>\n");
            sb.append("</td></tr>\n");
            sb.append("</table>\n");
        }

        @Override
        public String toString(int indent) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < indent; i++) {
                sb.append("\t");
            }
            sb.append("ApiResponseSet ");
            sb.append(this.getName());
            sb.append(" : [\n");
            for (Entry<String, String> val : excludedParamData.entrySet()) {
                for (int i = 0; i < indent + 1; i++) {
                    sb.append("\t");
                }
                sb.append(val.getKey());
                sb.append(" = ");
                sb.append(val.getValue());
                sb.append("\n");
            }
            sb.append(type.toString(indent + 1));
            for (int i = 0; i < indent; i++) {
                sb.append("\t");
            }
            sb.append("]\n");
            return sb.toString();
        }
    }

    private class ScannerApiResponse extends ApiResponse {

        final Map<String, String> scannerData;
        final ApiResponseList dependencies;

        public ScannerApiResponse(ScanPolicy policy, Plugin scanner) {
            super("scanner");

            scannerData = new HashMap<>();
            scannerData.put("id", String.valueOf(scanner.getId()));
            scannerData.put("name", scanner.getName());
            scannerData.put("cweId", String.valueOf(scanner.getCweId()));
            scannerData.put("wascId", String.valueOf(scanner.getWascId()));
            scannerData.put("attackStrength", String.valueOf(scanner.getAttackStrength(true)));
            scannerData.put("alertThreshold", String.valueOf(scanner.getAlertThreshold(true)));
            scannerData.put("policyId", String.valueOf(scanner.getCategory()));
            scannerData.put("enabled", String.valueOf(scanner.isEnabled()));
            scannerData.put("quality", scanner.getStatus().toString());
            scannerData.put("status", scanner.getStatus().toString());

            boolean allDepsAvailable =
                    policy.getPluginFactory().hasAllDependenciesAvailable(scanner);
            scannerData.put("allDependenciesAvailable", Boolean.toString(allDepsAvailable));

            dependencies = new ApiResponseList("dependencies");
            for (Plugin dependency : policy.getPluginFactory().getDependencies(scanner)) {
                dependencies.addItem(
                        new ApiResponseElement("dependency", Integer.toString(dependency.getId())));
            }
        }

        @Override
        public void toXML(Document doc, Element parent) {
            parent.setAttribute("type", "set");
            for (Entry<String, String> val : scannerData.entrySet()) {
                Element el = doc.createElement(val.getKey());
                el.appendChild(doc.createTextNode(XMLStringUtil.escapeControlChrs(val.getValue())));
                parent.appendChild(el);
            }

            Element el = doc.createElement(dependencies.getName());
            dependencies.toXML(doc, el);
            parent.appendChild(el);
        }

        @Override
        public JSON toJSON() {
            JSONObject jo = new JSONObject();
            for (Entry<String, String> val : scannerData.entrySet()) {
                jo.put(val.getKey(), val.getValue());
            }
            jo.put(
                    dependencies.getName(),
                    ((JSONObject) dependencies.toJSON()).getJSONArray(dependencies.getName()));
            return jo;
        }

        @Override
        public void toHTML(StringBuilder sb) {
            sb.append("<h2>" + this.getName() + "</h2>\n");
            sb.append("<table border=\"1\">\n");
            for (Entry<String, String> val : scannerData.entrySet()) {
                sb.append("<tr><td>\n");
                sb.append(val.getKey());
                sb.append("</td><td>\n");
                sb.append(StringEscapeUtils.escapeHtml(val.getValue()));
                sb.append("</td></tr>\n");
            }
            sb.append("<tr><td>\n");
            sb.append(dependencies.getName());
            sb.append("</td><td>\n");
            sb.append("<table border=\"1\">\n");
            for (ApiResponse resp : this.dependencies.getItems()) {
                sb.append("<tr><td>\n");
                resp.toHTML(sb);
                sb.append("</td></tr>\n");
            }
            sb.append("</table>\n");
            sb.append("</td></tr>\n");
            sb.append("</table>\n");
        }

        @Override
        public String toString(int indent) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < indent; i++) {
                sb.append("	");
            }
            sb.append("ScannerApiResponse ");
            sb.append(this.getName());
            sb.append(" : [\n");
            for (Entry<String, String> val : scannerData.entrySet()) {
                for (int i = 0; i < indent + 1; i++) {
                    sb.append("\t");
                }
                sb.append(val.getKey());
                sb.append(" = ");
                sb.append(val.getValue());
                sb.append("\n");
            }
            dependencies.toString(indent + 1);
            for (int i = 0; i < indent; i++) {
                sb.append("\t");
            }
            sb.append("]\n");
            return sb.toString();
        }
    }
}
