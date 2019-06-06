/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2018 The ZAP Development Team
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
package org.zaproxy.zap.extension.alert;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.RecordAlert;
import org.parosproxy.paros.db.TableAlert;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.SiteNode;
import org.zaproxy.zap.extension.api.ApiAction;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiImplementor;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.extension.api.ApiResponseElement;
import org.zaproxy.zap.extension.api.ApiResponseList;
import org.zaproxy.zap.extension.api.ApiResponseSet;
import org.zaproxy.zap.extension.api.ApiView;
import org.zaproxy.zap.utils.ApiUtils;

public class AlertAPI extends ApiImplementor {

    public static final String PREFIX = "alert";

    private static final String ACTION_DELETE_ALL_ALERTS = "deleteAllAlerts";
    private static final String ACTION_DELETE_ALERT = "deleteAlert";

    private static final String VIEW_ALERT = "alert";
    private static final String VIEW_ALERTS = "alerts";
    private static final String VIEW_ALERTS_SUMMARY = "alertsSummary";
    private static final String VIEW_NUMBER_OF_ALERTS = "numberOfAlerts";
    private static final String VIEW_ALERTS_BY_RISK = "alertsByRisk";
    private static final String VIEW_ALERT_COUNTS_BY_RISK = "alertCountsByRisk";

    private static final String PARAM_BASE_URL = "baseurl";
    private static final String PARAM_COUNT = "count";
    private static final String PARAM_URL = "url";
    private static final String PARAM_ID = "id";
    private static final String PARAM_RECURSE = "recurse";
    private static final String PARAM_RISK = "riskId";
    private static final String PARAM_START = "start";

    /**
     * The constant that indicates that no risk ID is being provided.
     *
     * @see #getRiskId(JSONObject)
     * @see #processAlerts(String, int, int, int, Processor)
     */
    private static final int NO_RISK_ID = -1;

    private ExtensionAlert extension = null;
    private static final Logger logger = Logger.getLogger(AlertAPI.class);

    public AlertAPI(ExtensionAlert ext) {
        this.extension = ext;
        this.addApiView(new ApiView(VIEW_ALERT, new String[] {PARAM_ID}));
        this.addApiView(
                new ApiView(
                        VIEW_ALERTS,
                        null,
                        new String[] {PARAM_BASE_URL, PARAM_START, PARAM_COUNT, PARAM_RISK}));
        this.addApiView(new ApiView(VIEW_ALERTS_SUMMARY, null, new String[] {PARAM_BASE_URL}));
        this.addApiView(
                new ApiView(
                        VIEW_NUMBER_OF_ALERTS, null, new String[] {PARAM_BASE_URL, PARAM_RISK}));
        this.addApiView(
                new ApiView(VIEW_ALERTS_BY_RISK, null, new String[] {PARAM_URL, PARAM_RECURSE}));
        this.addApiView(
                new ApiView(
                        VIEW_ALERT_COUNTS_BY_RISK, null, new String[] {PARAM_URL, PARAM_RECURSE}));

        this.addApiAction(new ApiAction(ACTION_DELETE_ALL_ALERTS));
        this.addApiAction(new ApiAction(ACTION_DELETE_ALERT, new String[] {PARAM_ID}));
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public ApiResponse handleApiView(String name, JSONObject params) throws ApiException {
        ApiResponse result = null;
        if (VIEW_ALERT.equals(name)) {
            TableAlert tableAlert = Model.getSingleton().getDb().getTableAlert();
            RecordAlert recordAlert;
            try {
                recordAlert = tableAlert.read(this.getParam(params, PARAM_ID, -1));
            } catch (DatabaseException e) {
                logger.error("Failed to read the alert from the session:", e);
                throw new ApiException(ApiException.Type.INTERNAL_ERROR);
            }
            if (recordAlert == null) {
                throw new ApiException(ApiException.Type.DOES_NOT_EXIST);
            }
            result = new ApiResponseElement(alertToSet(new Alert(recordAlert)));
        } else if (VIEW_ALERTS.equals(name)) {
            final ApiResponseList resultList = new ApiResponseList(name);

            processAlerts(
                    this.getParam(params, PARAM_BASE_URL, (String) null),
                    this.getParam(params, PARAM_START, -1),
                    this.getParam(params, PARAM_COUNT, -1),
                    getRiskId(params),
                    new Processor<Alert>() {

                        @Override
                        public void process(Alert alert) {
                            resultList.addItem(alertToSet(alert));
                        }
                    });
            result = resultList;
        } else if (VIEW_NUMBER_OF_ALERTS.equals(name)) {
            CounterProcessor<Alert> counter = new CounterProcessor<>();
            processAlerts(
                    this.getParam(params, PARAM_BASE_URL, (String) null),
                    this.getParam(params, PARAM_START, -1),
                    this.getParam(params, PARAM_COUNT, -1),
                    getRiskId(params),
                    counter);

            result = new ApiResponseElement(name, Integer.toString(counter.getCount()));
        } else if (VIEW_ALERTS_SUMMARY.equals(name)) {
            final int[] riskSummary = {0, 0, 0, 0};
            Processor<Alert> counter =
                    new Processor<Alert>() {

                        @Override
                        public void process(Alert alert) {
                            riskSummary[alert.getRisk()]++;
                        }
                    };
            processAlerts(
                    this.getParam(params, PARAM_BASE_URL, (String) null),
                    -1,
                    -1,
                    NO_RISK_ID,
                    counter);

            Map<String, Object> alertData = new HashMap<>();
            for (int i = 0; i < riskSummary.length; i++) {
                alertData.put(Alert.MSG_RISK[i], riskSummary[i]);
            }
            result =
                    new ApiResponseSet<Object>("risk", alertData) {

                        @Override
                        public JSON toJSON() {
                            JSONObject response = new JSONObject();
                            response.put(name, super.toJSON());
                            return response;
                        }
                    };
        } else if (VIEW_ALERTS_BY_RISK.equals(name)) {
            String url = this.getParam(params, PARAM_URL, "");
            boolean recurse = this.getParam(params, PARAM_RECURSE, false);
            ApiResponseList resultList = new ApiResponseList(name);
            result = resultList;

            // 0 (RISK_INFO) -> 3 (RISK_HIGH)
            ApiResponseList[] list = new ApiResponseList[4];
            for (int i = 0; i < list.length; i++) {
                list[i] = new ApiResponseList(Alert.MSG_RISK[i]);
            }

            AlertTreeModel model = extension.getTreeModel();
            AlertNode root = (AlertNode) model.getRoot();
            Enumeration<?> enumAllAlerts = root.children();
            while (enumAllAlerts.hasMoreElements()) {
                AlertNode child = (AlertNode) enumAllAlerts.nextElement();
                Alert alert = child.getUserObject();

                ApiResponseList alertList = filterAlertInstances(child, url, recurse);
                if (alertList.getItems().size() > 0) {
                    list[alert.getRisk()].addItem(alertList);
                }
            }
            for (int i = 0; i < list.length; i++) {
                resultList.addItem(list[i]);
            }
        } else if (VIEW_ALERT_COUNTS_BY_RISK.equals(name)) {
            String url = this.getParam(params, PARAM_URL, "");
            boolean recurse = this.getParam(params, PARAM_RECURSE, false);

            // 0 (RISK_INFO) -> 3 (RISK_HIGH)
            int[] counts = new int[] {0, 0, 0, 0};

            AlertTreeModel model = extension.getTreeModel();
            AlertNode root = (AlertNode) model.getRoot();
            Enumeration<?> enumAllAlerts = root.children();
            while (enumAllAlerts.hasMoreElements()) {
                AlertNode child = (AlertNode) enumAllAlerts.nextElement();
                Alert alert = child.getUserObject();

                ApiResponseList alertList = filterAlertInstances(child, url, recurse);
                if (alertList.getItems().size() > 0) {
                    counts[alert.getRisk()] += 1;
                }
            }
            Map<String, Integer> map = new HashMap<String, Integer>();
            map.put(Alert.MSG_RISK[Alert.RISK_HIGH], counts[Alert.RISK_HIGH]);
            map.put(Alert.MSG_RISK[Alert.RISK_MEDIUM], counts[Alert.RISK_MEDIUM]);
            map.put(Alert.MSG_RISK[Alert.RISK_LOW], counts[Alert.RISK_LOW]);
            map.put(Alert.MSG_RISK[Alert.RISK_INFO], counts[Alert.RISK_INFO]);
            result = new ApiResponseSet<Integer>(name, map);
        } else {
            throw new ApiException(ApiException.Type.BAD_VIEW);
        }
        return result;
    }

    @Override
    public ApiResponse handleApiAction(String name, JSONObject params) throws ApiException {

        if (ACTION_DELETE_ALERT.equals(name)) {
            int alertId = ApiUtils.getIntParam(params, PARAM_ID);

            RecordAlert recAlert;
            try {
                recAlert = Model.getSingleton().getDb().getTableAlert().read(alertId);
            } catch (DatabaseException e) {
                logger.error(e.getMessage(), e);
                throw new ApiException(ApiException.Type.INTERNAL_ERROR, e);
            }

            if (recAlert == null) {
                throw new ApiException(ApiException.Type.DOES_NOT_EXIST, PARAM_ID);
            }

            final ExtensionAlert extAlert =
                    Control.getSingleton().getExtensionLoader().getExtension(ExtensionAlert.class);
            if (extAlert != null) {
                extAlert.deleteAlert(new Alert(recAlert));
            } else {
                try {
                    Model.getSingleton().getDb().getTableAlert().deleteAlert(alertId);
                } catch (DatabaseException e) {
                    logger.error(e.getMessage(), e);
                    throw new ApiException(ApiException.Type.INTERNAL_ERROR, e);
                }
            }
        } else if (ACTION_DELETE_ALL_ALERTS.equals(name)) {
            final ExtensionAlert extAlert =
                    Control.getSingleton().getExtensionLoader().getExtension(ExtensionAlert.class);
            if (extAlert != null) {
                extAlert.deleteAllAlerts();
            } else {
                try {
                    Model.getSingleton().getDb().getTableAlert().deleteAllAlerts();
                } catch (DatabaseException e) {
                    logger.error(e.getMessage(), e);
                }

                SiteNode rootNode = Model.getSingleton().getSession().getSiteTree().getRoot();
                rootNode.deleteAllAlerts();

                removeHistoryReferenceAlerts(rootNode);
            }
        } else {
            throw new ApiException(ApiException.Type.BAD_ACTION);
        }
        return ApiResponseElement.OK;
    }

    private static void removeHistoryReferenceAlerts(SiteNode siteNode) {
        for (int i = 0; i < siteNode.getChildCount(); i++) {
            removeHistoryReferenceAlerts((SiteNode) siteNode.getChildAt(i));
        }
        if (siteNode.getHistoryReference() != null) {
            siteNode.getHistoryReference().deleteAllAlerts();
        }
        for (HistoryReference hRef : siteNode.getPastHistoryReference()) {
            hRef.deleteAllAlerts();
        }
    }

    private ApiResponseList filterAlertInstances(AlertNode alertNode, String url, boolean recurse) {
        ApiResponseList alertList = new ApiResponseList(alertNode.getUserObject().getName());
        Enumeration<?> enumAlertInsts = alertNode.children();
        while (enumAlertInsts.hasMoreElements()) {
            AlertNode childAlert = (AlertNode) enumAlertInsts.nextElement();
            if (!url.isEmpty()) {
                String alertUrl = childAlert.getUserObject().getUri();
                if (!alertUrl.startsWith(url)) {
                    continue;
                }
                if (!recurse) {
                    // Exact match, excluding url params
                    if (alertUrl.indexOf('?') > 0) {
                        alertUrl = alertUrl.substring(0, alertUrl.indexOf('?'));
                    }
                    if (!alertUrl.equals(url)) {
                        continue;
                    }
                }
            }
            alertList.addItem(alertSummaryToSet(childAlert.getUserObject()));
        }
        return alertList;
    }

    private void processAlerts(
            String baseUrl, int start, int count, int riskId, Processor<Alert> processor)
            throws ApiException {
        List<Alert> alerts = new ArrayList<>();
        try {
            TableAlert tableAlert = Model.getSingleton().getDb().getTableAlert();
            // TODO this doesn't work, but should be used when its fixed :/
            // Vector<Integer> v =
            // tableAlert.getAlertListBySession(Model.getSingleton().getSession().getSessionId());
            Vector<Integer> v = tableAlert.getAlertList();

            PaginationConstraintsChecker pcc = new PaginationConstraintsChecker(start, count);
            for (int i = 0; i < v.size(); i++) {
                int alertId = v.get(i);
                RecordAlert recAlert = tableAlert.read(alertId);
                Alert alert = new Alert(recAlert);

                if (alert.getConfidence() != Alert.CONFIDENCE_FALSE_POSITIVE
                        && !alerts.contains(alert)) {
                    if (baseUrl != null && !alert.getUri().startsWith(baseUrl)) {
                        // Not subordinate to the specified URL
                        continue;
                    }
                    if (riskId != NO_RISK_ID && alert.getRisk() != riskId) {
                        continue;
                    }

                    pcc.recordProcessed();
                    alerts.add(alert);

                    if (!pcc.hasPageStarted()) {
                        continue;
                    }
                    processor.process(alert);

                    if (pcc.hasPageEnded()) {
                        break;
                    }
                }
            }
        } catch (DatabaseException e) {
            logger.error(e.getMessage(), e);
            throw new ApiException(ApiException.Type.INTERNAL_ERROR);
        }
    }

    private ApiResponseSet<String> alertToSet(Alert alert) {
        Map<String, String> map = new HashMap<>();
        map.put("id", String.valueOf(alert.getAlertId()));
        map.put("pluginId", String.valueOf(alert.getPluginId()));
        map.put(
                "alert",
                alert.getName()); // Deprecated in 2.5.0, maintain for compatibility with custom
        // code
        map.put("name", alert.getName());
        map.put("description", alert.getDescription());
        map.put("risk", Alert.MSG_RISK[alert.getRisk()]);
        map.put("confidence", Alert.MSG_CONFIDENCE[alert.getConfidence()]);
        map.put("url", alert.getUri());
        map.put("method", alert.getMethod());
        map.put("other", alert.getOtherInfo());
        map.put("param", alert.getParam());
        map.put("attack", alert.getAttack());
        map.put("evidence", alert.getEvidence());
        map.put("reference", alert.getReference());
        map.put("cweid", String.valueOf(alert.getCweId()));
        map.put("wascid", String.valueOf(alert.getWascId()));
        map.put("sourceid", String.valueOf(alert.getSource().getId()));
        map.put("solution", alert.getSolution());
        map.put(
                "messageId",
                (alert.getHistoryRef() != null)
                        ? String.valueOf(alert.getHistoryRef().getHistoryId())
                        : "");

        return new ApiResponseSet<String>("alert", map);
    }

    private ApiResponseSet<String> alertSummaryToSet(Alert alert) {
        Map<String, String> map = new HashMap<>();
        map.put("id", String.valueOf(alert.getAlertId()));
        map.put("name", alert.getName());
        map.put("risk", Alert.MSG_RISK[alert.getRisk()]);
        map.put("confidence", Alert.MSG_CONFIDENCE[alert.getConfidence()]);
        map.put("url", alert.getUri());
        map.put("param", alert.getParam());

        return new ApiResponseSet<String>("alertsummary", map);
    }

    private static void throwInvalidRiskId() throws ApiException {
        throw new ApiException(
                ApiException.Type.ILLEGAL_PARAMETER,
                "Parameter "
                        + PARAM_RISK
                        + " is not a valid risk ID (integer in interval [0, 3]).");
    }

    /**
     * Gets the risk ID from the given {@code parameters}, using {@link #PARAM_RISK} as parameter
     * name.
     *
     * @param parameters the parameters of the API request.
     * @return the ID of the risk, or {@link #NO_RISK_ID} if none provided.
     * @throws ApiException if the provided risk ID is not valid (not an integer nor a valid risk
     *     ID).
     */
    private int getRiskId(JSONObject parameters) throws ApiException {
        String riskIdParam = getParam(parameters, PARAM_RISK, "").trim();
        if (riskIdParam.isEmpty()) {
            return NO_RISK_ID;
        }

        int riskId = NO_RISK_ID;
        try {
            riskId = Integer.parseInt(riskIdParam);
        } catch (NumberFormatException e) {
            throwInvalidRiskId();
        }

        if (riskId < Alert.RISK_INFO || riskId > Alert.RISK_HIGH) {
            throwInvalidRiskId();
        }
        return riskId;
    }

    private interface Processor<T> {

        void process(T object);
    }

    private static class CounterProcessor<T> implements Processor<T> {

        private int count;

        public CounterProcessor() {
            count = 0;
        }

        @Override
        public void process(T object) {
            ++count;
        }

        public int getCount() {
            return count;
        }
    }

    private static class PaginationConstraintsChecker {

        private boolean pageStarted;
        private boolean pageEnded;
        private final int startRecord;
        private final boolean hasEnd;
        private final int finalRecord;
        private int recordsProcessed;

        public PaginationConstraintsChecker(int start, int count) {
            recordsProcessed = 0;

            if (start > 0) {
                pageStarted = false;
                startRecord = start;
            } else {
                pageStarted = true;
                startRecord = 0;
            }

            if (count > 0) {
                hasEnd = true;
                finalRecord = !pageStarted ? start + count - 1 : count;
            } else {
                hasEnd = false;
                finalRecord = 0;
            }
            pageEnded = false;
        }

        public void recordProcessed() {
            ++recordsProcessed;

            if (!pageStarted) {
                pageStarted = recordsProcessed >= startRecord;
            }

            if (hasEnd && !pageEnded) {
                pageEnded = recordsProcessed >= finalRecord;
            }
        }

        public boolean hasPageStarted() {
            return pageStarted;
        }

        public boolean hasPageEnded() {
            return pageEnded;
        }
    }
}
