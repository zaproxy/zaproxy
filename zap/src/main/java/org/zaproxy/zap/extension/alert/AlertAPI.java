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
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.function.Consumer;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.RecordAlert;
import org.parosproxy.paros.db.TableAlert;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.zaproxy.zap.db.TableAlertTag;
import org.zaproxy.zap.extension.api.ApiAction;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiImplementor;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.extension.api.ApiResponseElement;
import org.zaproxy.zap.extension.api.ApiResponseList;
import org.zaproxy.zap.extension.api.ApiResponseSet;
import org.zaproxy.zap.extension.api.ApiView;
import org.zaproxy.zap.utils.ApiUtils;
import org.zaproxy.zap.utils.XMLStringUtil;

public class AlertAPI extends ApiImplementor {

    public static final String PREFIX = "alert";

    private static final String ACTION_DELETE_ALL_ALERTS = "deleteAllAlerts";
    private static final String ACTION_DELETE_ALERT = "deleteAlert";
    private static final String ACTION_UPDATE_ALERT = "updateAlert";
    private static final String ACTION_ADD_ALERT = "addAlert";
    private static final String ACTION_UPDATE_ALERTS_CONFIDENCE = "updateAlertsConfidence";
    private static final String ACTION_UPDATE_ALERTS_RISK = "updateAlertsRisk";

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

    private static final String PARAM_MESSAGE_ID = "messageId";
    private static final String PARAM_ALERT_ID = "id";
    private static final String PARAM_ALERT_IDS = "ids";
    private static final String PARAM_ALERT_NAME = "name";
    private static final String PARAM_CONFIDENCE = "confidenceId";
    private static final String PARAM_ALERT_DESCRIPTION = "description";
    private static final String PARAM_ALERT_PARAM = "param";
    private static final String PARAM_ALERT_ATTACK = "attack";
    private static final String PARAM_ALERT_OTHERINFO = "otherInfo";
    private static final String PARAM_ALERT_SOLUTION = "solution";
    private static final String PARAM_ALERT_REFS = "references";
    private static final String PARAM_ALERT_EVIDENCE = "evidence";
    private static final String PARAM_CWEID = "cweId";
    private static final String PARAM_WASCID = "wascId";

    /**
     * The constant that indicates that no risk ID is being provided.
     *
     * @see #getRiskId(JSONObject)
     * @see #processAlerts(String, int, int, int, Processor)
     */
    private static final int NO_RISK_ID = -1;
    /**
     * The constant that indicates that no confidence ID is being provided.
     *
     * @see #getConfidenceId(JSONObject)
     */
    private static final int NO_CONFIDENCE_ID = -1;

    private ExtensionAlert extension = null;
    private static final Logger logger = LogManager.getLogger(AlertAPI.class);

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

        this.addApiAction(
                new ApiAction(
                        ACTION_UPDATE_ALERTS_CONFIDENCE,
                        new String[] {PARAM_ALERT_IDS, PARAM_CONFIDENCE}));
        this.addApiAction(
                new ApiAction(
                        ACTION_UPDATE_ALERTS_RISK, new String[] {PARAM_ALERT_IDS, PARAM_RISK}));
        this.addApiAction(
                new ApiAction(
                        ACTION_UPDATE_ALERT,
                        new String[] {
                            PARAM_ALERT_ID,
                            PARAM_ALERT_NAME,
                            PARAM_RISK,
                            PARAM_CONFIDENCE,
                            PARAM_ALERT_DESCRIPTION
                        },
                        new String[] {
                            PARAM_ALERT_PARAM,
                            PARAM_ALERT_ATTACK,
                            PARAM_ALERT_OTHERINFO,
                            PARAM_ALERT_SOLUTION,
                            PARAM_ALERT_REFS,
                            PARAM_ALERT_EVIDENCE,
                            PARAM_CWEID,
                            PARAM_WASCID
                        }));
        this.addApiAction(
                new ApiAction(
                        ACTION_ADD_ALERT,
                        new String[] {
                            PARAM_MESSAGE_ID,
                            PARAM_ALERT_NAME,
                            PARAM_RISK,
                            PARAM_CONFIDENCE,
                            PARAM_ALERT_DESCRIPTION
                        },
                        new String[] {
                            PARAM_ALERT_PARAM,
                            PARAM_ALERT_ATTACK,
                            PARAM_ALERT_OTHERINFO,
                            PARAM_ALERT_SOLUTION,
                            PARAM_ALERT_REFS,
                            PARAM_ALERT_EVIDENCE,
                            PARAM_CWEID,
                            PARAM_WASCID
                        }));
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
            TableAlertTag tableAlertTag = Model.getSingleton().getDb().getTableAlertTag();
            RecordAlert recordAlert;
            Map<String, String> alertTags;
            try {
                recordAlert = tableAlert.read(this.getParam(params, PARAM_ID, -1));
                alertTags = tableAlertTag.getTagsByAlertId(this.getParam(params, PARAM_ID, -1));
            } catch (DatabaseException e) {
                logger.error("Failed to read the alert from the session:", e);
                throw new ApiException(ApiException.Type.INTERNAL_ERROR);
            }
            if (recordAlert == null) {
                throw new ApiException(ApiException.Type.DOES_NOT_EXIST);
            }
            Alert alert = new Alert(recordAlert);
            alert.setTags(alertTags);
            result = new ApiResponseElement(alertToSet(alert));
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
            Arrays.stream(list).forEach(resultList::addItem);
        } else if (VIEW_ALERT_COUNTS_BY_RISK.equals(name)) {
            String url = this.getParam(params, PARAM_URL, "");
            boolean recurse = this.getParam(params, PARAM_RECURSE, false);

            // 0 (RISK_INFO) -> 3 (RISK_HIGH)
            int[] riskCounts = new int[] {0, 0, 0, 0};
            int falsePositiveCount = 0;

            AlertTreeModel model = extension.getTreeModel();
            AlertNode root = (AlertNode) model.getRoot();
            Enumeration<?> enumAllAlerts = root.children();
            while (enumAllAlerts.hasMoreElements()) {
                AlertNode child = (AlertNode) enumAllAlerts.nextElement();
                Alert alert = child.getUserObject();

                ApiResponseList alertList = filterAlertInstances(child, url, recurse);
                if (alertList.getItems().size() > 0) {
                    if (alert.getConfidence() == Alert.CONFIDENCE_FALSE_POSITIVE) {
                        falsePositiveCount += 1;
                    } else {
                        riskCounts[alert.getRisk()] += 1;
                    }
                }
            }
            Map<String, Integer> map = new HashMap<>();
            map.put(Alert.MSG_RISK[Alert.RISK_HIGH], riskCounts[Alert.RISK_HIGH]);
            map.put(Alert.MSG_RISK[Alert.RISK_MEDIUM], riskCounts[Alert.RISK_MEDIUM]);
            map.put(Alert.MSG_RISK[Alert.RISK_LOW], riskCounts[Alert.RISK_LOW]);
            map.put(Alert.MSG_RISK[Alert.RISK_INFO], riskCounts[Alert.RISK_INFO]);
            map.put(Alert.MSG_CONFIDENCE[Alert.CONFIDENCE_FALSE_POSITIVE], falsePositiveCount);
            result = new ApiResponseSet<>(name, map);
        } else {
            throw new ApiException(ApiException.Type.BAD_VIEW);
        }
        return result;
    }

    @Override
    public ApiResponse handleApiAction(String name, JSONObject params) throws ApiException {

        if (ACTION_DELETE_ALERT.equals(name)) {
            int alertId = ApiUtils.getIntParam(params, PARAM_ID);

            extension.deleteAlert(getAlertFromDb(alertId));
        } else if (ACTION_DELETE_ALL_ALERTS.equals(name)) {
            extension.deleteAllAlerts();
        } else if (ACTION_UPDATE_ALERT.equals(name)) {
            int alertId = ApiUtils.getIntParam(params, PARAM_ALERT_ID);
            String alertName = params.getString(PARAM_ALERT_NAME);
            int riskId = getRiskId(params);
            int confidenceId = getConfidenceId(params);
            String desc = params.getString(PARAM_ALERT_DESCRIPTION);
            String param = ApiUtils.getOptionalStringParam(params, PARAM_ALERT_PARAM);
            String attack = ApiUtils.getOptionalStringParam(params, PARAM_ALERT_ATTACK);
            String otherInfo = ApiUtils.getOptionalStringParam(params, PARAM_ALERT_OTHERINFO);
            String solution = ApiUtils.getOptionalStringParam(params, PARAM_ALERT_SOLUTION);
            String refs = ApiUtils.getOptionalStringParam(params, PARAM_ALERT_REFS);
            String evidence = ApiUtils.getOptionalStringParam(params, PARAM_ALERT_EVIDENCE);
            int cweId = getParam(params, PARAM_CWEID, 0);
            int wascId = getParam(params, PARAM_WASCID, 0);

            Alert updatedAlert = getAlertFromDb(alertId);
            updatedAlert.setName(alertName);
            updatedAlert.setRisk(riskId);
            updatedAlert.setConfidence(confidenceId);
            updatedAlert.setDescription(desc);
            updatedAlert.setParam(param);
            updatedAlert.setAttack(attack);
            updatedAlert.setOtherInfo(otherInfo);
            updatedAlert.setSolution(solution);
            updatedAlert.setReference(refs);
            updatedAlert.setEvidence(evidence);
            updatedAlert.setCweId(cweId);
            updatedAlert.setWascId(wascId);

            processAlertUpdate(updatedAlert);
        } else if (ACTION_ADD_ALERT.equals(name)) {
            int messageId = ApiUtils.getIntParam(params, PARAM_MESSAGE_ID);
            String alertName = ApiUtils.getNonEmptyStringParam(params, PARAM_ALERT_NAME);
            int riskId = getRiskId(params);
            int confidenceId = getConfidenceId(params);
            String desc = params.getString(PARAM_ALERT_DESCRIPTION);
            String param = ApiUtils.getOptionalStringParam(params, PARAM_ALERT_PARAM);
            String attack = ApiUtils.getOptionalStringParam(params, PARAM_ALERT_ATTACK);
            String otherInfo = ApiUtils.getOptionalStringParam(params, PARAM_ALERT_OTHERINFO);
            String solution = ApiUtils.getOptionalStringParam(params, PARAM_ALERT_SOLUTION);
            String refs = ApiUtils.getOptionalStringParam(params, PARAM_ALERT_REFS);
            String evidence = ApiUtils.getOptionalStringParam(params, PARAM_ALERT_EVIDENCE);
            int cweId = getParam(params, PARAM_CWEID, 0);
            int wascId = getParam(params, PARAM_WASCID, 0);

            HttpMessage msg = getHttpMessage(messageId);

            Alert newAlert = new Alert(-1, riskId, confidenceId, alertName);
            newAlert.setSource(Alert.Source.MANUAL);
            newAlert.setMessage(msg);
            newAlert.setUri(msg.getRequestHeader().getURI().toString());
            newAlert.setName(alertName);
            newAlert.setRisk(riskId);
            newAlert.setConfidence(confidenceId);
            newAlert.setDescription(desc);
            newAlert.setParam(param);
            newAlert.setAttack(attack);
            newAlert.setOtherInfo(otherInfo);
            newAlert.setSolution(solution);
            newAlert.setReference(refs);
            newAlert.setEvidence(evidence);
            newAlert.setCweId(cweId);
            newAlert.setWascId(wascId);
            extension.alertFound(newAlert, msg.getHistoryRef());
            return new ApiResponseElement(name, Integer.toString(newAlert.getAlertId()));
        } else if (ACTION_UPDATE_ALERTS_CONFIDENCE.equals(name)) {
            int confidenceId = getConfidenceId(params);

            updateAlerts(params, alert -> alert.setConfidence(confidenceId));
        } else if (ACTION_UPDATE_ALERTS_RISK.equals(name)) {
            int riskId = getRiskId(params);

            updateAlerts(params, alert -> alert.setRisk(riskId));
        } else {
            throw new ApiException(ApiException.Type.BAD_ACTION);
        }
        return ApiResponseElement.OK;
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
            TableAlertTag tableAlertTag = Model.getSingleton().getDb().getTableAlertTag();
            // TODO this doesn't work, but should be used when its fixed :/
            // Vector<Integer> v =
            // tableAlert.getAlertListBySession(Model.getSingleton().getSession().getSessionId());
            Vector<Integer> v = tableAlert.getAlertList();

            PaginationConstraintsChecker pcc = new PaginationConstraintsChecker(start, count);
            for (int alertId : v) {
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
                    alert.setTags(tableAlertTag.getTagsByAlertId(alertId));
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

    private ApiResponseSet<Object> alertToSet(Alert alert) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", String.valueOf(alert.getAlertId()));
        map.put("pluginId", String.valueOf(alert.getPluginId()));
        map.put("alertRef", alert.getAlertRef());
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
        map.put("inputVector", alert.getInputVector());
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
        map.put("tags", alert.getTags());
        return new CustomApiResponseSet<>("alert", map);
    }

    private ApiResponseSet<String> alertSummaryToSet(Alert alert) {
        Map<String, String> map = new HashMap<>();
        map.put("id", String.valueOf(alert.getAlertId()));
        map.put("name", alert.getName());
        map.put("risk", Alert.MSG_RISK[alert.getRisk()]);
        map.put("confidence", Alert.MSG_CONFIDENCE[alert.getConfidence()]);
        map.put("url", alert.getUri());
        map.put("param", alert.getParam());

        return new ApiResponseSet<>("alertsummary", map);
    }

    private static void throwInvalidRiskId() throws ApiException {
        throw new ApiException(
                ApiException.Type.ILLEGAL_PARAMETER,
                Constant.messages.getString("alert.api.param.riskId.illegal", PARAM_RISK));
    }

    private static void throwInvalidConfidenceId() throws ApiException {
        throw new ApiException(
                ApiException.Type.ILLEGAL_PARAMETER,
                Constant.messages.getString("alert.api.param.confidenceId.illegal", PARAM_RISK));
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

    /**
     * Gets the confidence ID from the given {@code parameters}, using {@link #PARAM_CONFIDENCE} as
     * parameter name.
     *
     * @param parameters the parameters of the API request.
     * @return the ID of the confidence, or {@link #NO_CONFIDENCE_ID} if none provided.
     * @throws ApiException if the provided confidence ID is not valid (not an integer nor a valid
     *     confidence ID).
     */
    private int getConfidenceId(JSONObject parameters) throws ApiException {
        String confidenceIdParam = getParam(parameters, PARAM_CONFIDENCE, "").trim();
        if (confidenceIdParam.isEmpty()) {
            return NO_CONFIDENCE_ID;
        }

        int confidenceId = NO_CONFIDENCE_ID;
        try {
            confidenceId = Integer.parseInt(confidenceIdParam);
        } catch (NumberFormatException e) {
            throwInvalidConfidenceId();
        }

        if (confidenceId < Alert.CONFIDENCE_FALSE_POSITIVE
                || confidenceId > Alert.CONFIDENCE_USER_CONFIRMED) {
            throwInvalidConfidenceId();
        }
        return confidenceId;
    }

    private List<Integer> getAlertIds(String alertIds) throws ApiException {
        List<Integer> idsList = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(alertIds, ",");
        while (tokenizer.hasMoreTokens()) {
            String alertIdStr = tokenizer.nextToken().trim();
            try {
                idsList.add(Integer.parseInt(alertIdStr));
            } catch (NumberFormatException nfe) {
                throw new ApiException(
                        ApiException.Type.ILLEGAL_PARAMETER,
                        Constant.messages.getString(
                                "alert.api.param.alertIds.illegal", alertIdStr, alertIds));
            }
        }
        return idsList;
    }

    private Alert getAlertFromDb(int alertId) throws ApiException {
        RecordAlert recAlert;
        try {
            recAlert = Model.getSingleton().getDb().getTableAlert().read(alertId);
        } catch (DatabaseException e) {
            logger.error(e.getMessage(), e);
            throw new ApiException(ApiException.Type.INTERNAL_ERROR, e);
        }

        if (recAlert == null) {
            throw new ApiException(ApiException.Type.DOES_NOT_EXIST, String.valueOf(alertId));
        }
        return new Alert(recAlert);
    }

    private void processAlertUpdate(Alert updatedAlert) throws ApiException {
        try {
            extension.updateAlert(updatedAlert);
        } catch (HttpMalformedHeaderException | DatabaseException e) {
            logger.error(e.getMessage(), e);
            throw new ApiException(ApiException.Type.INTERNAL_ERROR, e);
        }
    }

    private void updateAlerts(JSONObject params, Consumer<Alert> consumer) throws ApiException {
        String alertIds = params.getString(PARAM_ALERT_IDS);

        for (int id : getAlertIds(alertIds)) {
            Alert updatedAlert = getAlertFromDb(id);
            consumer.accept(updatedAlert);
            processAlertUpdate(updatedAlert);
        }
    }

    private static HttpMessage getHttpMessage(int id) throws ApiException {
        try {
            return new HistoryReference(id, true).getHttpMessage();
        } catch (HttpMalformedHeaderException | DatabaseException e) {
            if (e.getMessage() == null) {
                throw new ApiException(ApiException.Type.DOES_NOT_EXIST, PARAM_MESSAGE_ID);
            }
            logger.error("Failed to read the history record:", e);
            throw new ApiException(ApiException.Type.INTERNAL_ERROR, e);
        }
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

    @SuppressWarnings("unchecked")
    private static class CustomApiResponseSet<T> extends ApiResponseSet<T> {
        public CustomApiResponseSet(String name, Map<String, T> values) {
            super(name, values);
        }

        @Override
        public void toXML(Document doc, Element parent) {
            parent.setAttribute("type", "set");
            for (Map.Entry<String, T> val : getValues().entrySet()) {
                Element el = doc.createElement(val.getKey());
                if ("tags".equals(val.getKey())) {
                    el.setAttribute("type", "list");
                    Map<String, String> tags = (Map<String, String>) val.getValue();
                    for (Map.Entry<String, String> tag : tags.entrySet()) {
                        Element elTag = doc.createElement("tag");
                        elTag.setAttribute("type", "set");

                        Element elKey = doc.createElement("key");
                        elKey.appendChild(
                                doc.createTextNode(XMLStringUtil.escapeControlChrs(tag.getKey())));
                        elTag.appendChild(elKey);

                        Element elValue = doc.createElement("value");
                        elValue.appendChild(
                                doc.createTextNode(
                                        XMLStringUtil.escapeControlChrs(tag.getValue())));
                        elTag.appendChild(elValue);

                        el.appendChild(elTag);
                    }
                } else {
                    String textValue = val.getValue() == null ? "" : val.getValue().toString();
                    Text text = doc.createTextNode(XMLStringUtil.escapeControlChrs(textValue));
                    el.appendChild(text);
                }
                parent.appendChild(el);
            }
        }
    }
}
