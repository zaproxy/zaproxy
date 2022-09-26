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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.extension.pscan;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Plugin;
import org.zaproxy.zap.extension.api.ApiAction;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiImplementor;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.extension.api.ApiResponseElement;
import org.zaproxy.zap.extension.api.ApiResponseList;
import org.zaproxy.zap.extension.api.ApiResponseSet;
import org.zaproxy.zap.extension.api.ApiView;
import org.zaproxy.zap.utils.ApiUtils;

public class PassiveScanAPI extends ApiImplementor {

    private static final Logger logger = LogManager.getLogger(PassiveScanAPI.class);

    private static final String PREFIX = "pscan";

    private static final String VIEW_SCAN_ONLY_IN_SCOPE = "scanOnlyInScope";
    private static final String VIEW_RECORDS_TO_SCAN = "recordsToScan";
    private static final String VIEW_SCANNERS = "scanners";
    private static final String VIEW_CURRENT_RULE = "currentRule";
    private static final String VIEW_CURRENT_TASKS = "currentTasks";
    private static final String VIEW_MAX_ALERTS_PER_RULE = "maxAlertsPerRule";

    private static final String ACTION_SET_ENABLED = "setEnabled";
    private static final String ACTION_SET_SCAN_ONLY_IN_SCOPE = "setScanOnlyInScope";
    private static final String ACTION_ENABLE_ALL_SCANNERS = "enableAllScanners";
    private static final String ACTION_DISABLE_ALL_SCANNERS = "disableAllScanners";
    private static final String ACTION_ENABLE_SCANNERS = "enableScanners";
    private static final String ACTION_DISABLE_SCANNERS = "disableScanners";
    private static final String ACTION_SET_SCANNER_ALERT_THRESHOLD = "setScannerAlertThreshold";
    private static final String ACTION_SET_MAX_ALERTS_PER_RULE = "setMaxAlertsPerRule";
    private static final String ACTION_DISABLE_ALL_TAGS = "disableAllTags";
    private static final String ACTION_ENABLE_ALL_TAGS = "enableAllTags";
    private static final String ACTION_CLEAR_QUEUE = "clearQueue";

    private static final String PARAM_ENABLED = "enabled";
    private static final String PARAM_ONLY_IN_SCOPE = "onlyInScope";
    private static final String PARAM_IDS = "ids";
    private static final String PARAM_ID = "id";
    private static final String PARAM_ALERT_THRESHOLD = "alertThreshold";
    private static final String PARAM_MAX_ALERTS = "maxAlerts";

    private ExtensionPassiveScan extension;

    public PassiveScanAPI(ExtensionPassiveScan extension) {
        this.extension = extension;

        this.addApiAction(new ApiAction(ACTION_SET_ENABLED, new String[] {PARAM_ENABLED}));
        this.addApiAction(
                new ApiAction(ACTION_SET_SCAN_ONLY_IN_SCOPE, new String[] {PARAM_ONLY_IN_SCOPE}));
        this.addApiAction(new ApiAction(ACTION_ENABLE_ALL_SCANNERS));
        this.addApiAction(new ApiAction(ACTION_DISABLE_ALL_SCANNERS));
        this.addApiAction(new ApiAction(ACTION_ENABLE_SCANNERS, new String[] {PARAM_IDS}));
        this.addApiAction(new ApiAction(ACTION_DISABLE_SCANNERS, new String[] {PARAM_IDS}));
        this.addApiAction(
                new ApiAction(
                        ACTION_SET_SCANNER_ALERT_THRESHOLD,
                        new String[] {PARAM_ID, PARAM_ALERT_THRESHOLD}));
        this.addApiAction(
                new ApiAction(ACTION_SET_MAX_ALERTS_PER_RULE, new String[] {PARAM_MAX_ALERTS}));
        this.addApiAction(new ApiAction(ACTION_DISABLE_ALL_TAGS));
        this.addApiAction(new ApiAction(ACTION_ENABLE_ALL_TAGS));
        this.addApiAction(new ApiAction(ACTION_CLEAR_QUEUE));

        this.addApiView(new ApiView(VIEW_SCAN_ONLY_IN_SCOPE));
        this.addApiView(new ApiView(VIEW_RECORDS_TO_SCAN));
        this.addApiView(new ApiView(VIEW_SCANNERS));
        ApiView currentRule = new ApiView(VIEW_CURRENT_RULE);
        currentRule.setDeprecated(true);
        currentRule.setDeprecatedDescription(
                Constant.messages.getString("pscan.api.view.currentRule.deprecated"));
        this.addApiView(currentRule);
        this.addApiView(new ApiView(VIEW_CURRENT_TASKS));
        this.addApiView(new ApiView(VIEW_MAX_ALERTS_PER_RULE));
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public ApiResponse handleApiAction(String name, JSONObject params) throws ApiException {
        switch (name) {
            case ACTION_SET_ENABLED:
                boolean enabled = getParam(params, PARAM_ENABLED, false);

                extension.setPassiveScanEnabled(enabled);
                break;
            case ACTION_SET_SCAN_ONLY_IN_SCOPE:
                extension
                        .getPassiveScanParam()
                        .setScanOnlyInScope(params.getBoolean(PARAM_ONLY_IN_SCOPE));
                break;
            case ACTION_ENABLE_ALL_SCANNERS:
                extension.setAllPluginPassiveScannersEnabled(true);
                break;
            case ACTION_DISABLE_ALL_SCANNERS:
                extension.setAllPluginPassiveScannersEnabled(false);
                break;
            case ACTION_ENABLE_SCANNERS:
                setPluginPassiveScannersEnabled(params, true);
                break;
            case ACTION_DISABLE_SCANNERS:
                setPluginPassiveScannersEnabled(params, false);
                break;
            case ACTION_SET_SCANNER_ALERT_THRESHOLD:
                String paramId = params.getString(PARAM_ID);
                int pluginId;
                try {
                    pluginId = Integer.valueOf(paramId.trim());
                } catch (NumberFormatException e) {
                    throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, PARAM_ID);
                }
                if (!extension.hasPluginPassiveScanner(pluginId)) {
                    throw new ApiException(ApiException.Type.DOES_NOT_EXIST, PARAM_ID);
                }

                Plugin.AlertThreshold alertThreshold =
                        getAlertThresholdFromParamAlertThreshold(params);
                extension.setPluginPassiveScannerAlertThreshold(pluginId, alertThreshold);
                break;
            case ACTION_SET_MAX_ALERTS_PER_RULE:
                extension
                        .getPassiveScanParam()
                        .setMaxAlertsPerRule(ApiUtils.getIntParam(params, PARAM_MAX_ALERTS));
                break;
            case ACTION_DISABLE_ALL_TAGS:
                extension
                        .getPassiveScanParam()
                        .getAutoTagScanners()
                        .forEach(tagScanner -> tagScanner.setEnabled(false));
                break;
            case ACTION_ENABLE_ALL_TAGS:
                extension
                        .getPassiveScanParam()
                        .getAutoTagScanners()
                        .forEach(tagScanner -> tagScanner.setEnabled(true));
                break;
            case ACTION_CLEAR_QUEUE:
                extension.clearQueue();
                break;
            default:
                throw new ApiException(ApiException.Type.BAD_ACTION);
        }

        return ApiResponseElement.OK;
    }

    private void setPluginPassiveScannersEnabled(JSONObject params, boolean enabled)
            throws ApiException {
        try {
            String[] ids = getParam(params, PARAM_IDS, "").split(",");
            for (String id : ids) {
                int pluginId = Integer.parseInt(id.trim());
                if (!extension.hasPluginPassiveScanner(pluginId)) {
                    throw new ApiException(ApiException.Type.DOES_NOT_EXIST, id.trim());
                }
                extension.setPluginPassiveScannerEnabled(pluginId, enabled);
            }
        } catch (NumberFormatException e) {
            logger.error("Failed to parse scanner ID: {}", e.getMessage(), e);
            throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, e.getMessage(), e);
        }
    }

    private static Plugin.AlertThreshold getAlertThresholdFromParamAlertThreshold(JSONObject params)
            throws ApiException {
        final String paramAlertThreshold =
                params.getString(PARAM_ALERT_THRESHOLD).trim().toUpperCase();
        try {
            return Plugin.AlertThreshold.valueOf(paramAlertThreshold);
        } catch (IllegalArgumentException e) {
            throw new ApiException(ApiException.Type.DOES_NOT_EXIST, PARAM_ALERT_THRESHOLD);
        }
    }

    @Override
    public ApiResponse handleApiView(String name, JSONObject params) throws ApiException {
        ApiResponse result;

        switch (name) {
            case VIEW_SCAN_ONLY_IN_SCOPE:
                result =
                        new ApiResponseElement(
                                name,
                                Boolean.toString(
                                        extension.getPassiveScanParam().isScanOnlyInScope()));
                break;
            case VIEW_RECORDS_TO_SCAN:
                result = new ApiResponseElement(name, String.valueOf(extension.getRecordsToScan()));
                break;
            case VIEW_SCANNERS:
                List<PluginPassiveScanner> scanners = extension.getPluginPassiveScanners();

                ApiResponseList resultList = new ApiResponseList(name);
                for (PluginPassiveScanner scanner : scanners) {
                    Map<String, String> map = new HashMap<>();
                    map.put("id", String.valueOf(scanner.getPluginId()));
                    map.put("name", scanner.getName());
                    map.put("enabled", String.valueOf(scanner.isEnabled()));
                    map.put("alertThreshold", scanner.getAlertThreshold(true).name());
                    map.put("quality", scanner.getStatus().toString());
                    map.put("status", scanner.getStatus().toString());
                    resultList.addItem(new ApiResponseSet<>("scanner", map));
                }

                result = resultList;
                break;
            case VIEW_CURRENT_RULE:
                result = getResponseForTask(extension.getOldestRunningTask(), name);
                break;
            case VIEW_CURRENT_TASKS:
                ApiResponseList taskList = new ApiResponseList(name);
                extension.getRunningTasks().stream()
                        .forEach(t -> taskList.addItem(getResponseForTask(t, name)));
                result = taskList;
                break;
            case VIEW_MAX_ALERTS_PER_RULE:
                result =
                        new ApiResponseElement(
                                VIEW_MAX_ALERTS_PER_RULE,
                                Integer.toString(
                                        extension.getPassiveScanParam().getMaxAlertsPerRule()));
                break;
            default:
                throw new ApiException(ApiException.Type.BAD_VIEW);
        }
        return result;
    }

    private ApiResponseSet<String> getResponseForTask(PassiveScanTask task, String name) {
        Map<String, String> map = new HashMap<>();
        PassiveScanner scanner = task.getCurrentScanner();
        map.put("name", scanner == null ? "" : scanner.getName());
        map.put("url", task.getURI().toString());
        long time = task.getStartTime();
        if (time > 0) {
            time = System.currentTimeMillis() - time;
        }
        map.put("time", String.valueOf(time));
        return new ApiResponseSet<>(name, map);
    }
}
