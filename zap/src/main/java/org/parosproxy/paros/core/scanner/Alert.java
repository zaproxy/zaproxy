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
// ZAP: 2011/12/04 Support deleting alerts
// ZAP: 2012/01/02 Separate param and attack
// ZAP: 2012/01/23 Changed the method compareTo to compare the fields correctly
// with each other.
// ZAP: 2012/03/15 Changed the methods toPluginXML and getUrlParamXML to use the class
// StringBuilder instead of StringBuffer and replaced some string concatenations with
// calls to the method append of the class StringBuilder.
// ZAP: 2012/04/25 Added @Override annotation to all appropriate methods.
// ZAP: 2012/05/02 Changed to not create a new String in the setters.
// ZAP: 2012/07/10 Issue 323: Added getIconUrl()
// ZAP: 2012/10/08 Issue 391: Performance improvements
// ZAP: 2012/12/19 Code Cleanup: Moved array brackets from variable name to type
// ZAP: 2013/07/12 Issue 713: Add CWE and WASC numbers to issues
// ZAP: 2013/09/08 Issue 691: Handle old plugins
// ZAP: 2013/11/16 Issue 866: Alert keeps HttpMessage longer than needed when HistoryReference is
// set/available
// ZAP: 2014/04/10 Issue 1042: Having significant issues opening a previous session
// ZAP: 2014/05/23 Issue 1209: Reliability becomes Confidence and add levels
// ZAP: 2015/01/04 Issue 1419: Include alert's evidence in HTML report
// ZAP: 2014/01/04 Issue 1475: Alerts with different name from same scanner might not be shown in
// report
// ZAP: 2015/02/09 Issue 1525: Introduce a database interface layer to allow for alternative
// implementations
// ZAP: 2015/08/24 Issue 1849: Option to merge related issues in reports
// ZAP: 2015/11/16 Issue 1555: Rework inclusion of HTML tags in reports
// ZAP: 2016/02/26 Deprecate alert as an element of Alert in favour of name
// ZAP: 2016/05/25 Normalise equals/hashCode/compareTo
// ZAP: 2016/08/10 Issue 2757: Alerts with different request method are considered the same
// ZAP: 2016/08/25 Initialise the method to an empty string
// ZAP: 2016/09/20 JavaDoc tweaks
// ZAP: 2016/10/11 Issue 2592: Differentiate the source of alerts
// ZAP: 2017/02/22 Issue 3224: Use TreeCellRenderers to prevent HTML injection issues
// ZAP: 2017/08/30: Issue 1984: Ensure element setters set empty string if passed a null value
// ZAP: 2017/09/15 Initialise the source from the RecordAlert always.
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2019/07/10 Add utility methods isValidRisk(int) and isValidConfidence(int)
// ZAP: 2019/10/21 Add Alert builder.
// ZAP: 2020/11/03 Add alertRef field.
// ZAP: 2020/11/26 Use Log4j 2 classes for logging.
// ZAP: 2021/04/30 Add input vector to Alert
// ZAP: 2021/06/22 Moved the ReportGenerator.entityEncode method to this class.
// ZAP: 2022/02/02 Deleted a deprecated setDetails method.
// ZAP: 2022/02/03 Removed SUSPICIOUS, WARNING, MSG_RELIABILITY, setRiskReliability(int, int) and
// getReliability()
// ZAP: 2022/02/25 Remove code deprecated in 2.5.0
// ZAP: 2022/05/26 Add addTag and removeTag methods
package org.parosproxy.paros.core.scanner;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;
import org.apache.commons.httpclient.URI;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.RecordAlert;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.utils.DisplayUtils;
import org.zaproxy.zap.utils.XMLStringUtil;

public class Alert implements Comparable<Alert> {

    /**
     * The source of the alerts.
     *
     * @since 2.6.0
     */
    public enum Source {
        /**
         * An alert raised by unknown tool/functionality, mostly for old alerts which source is not
         * (well) known.
         */
        UNKNOWN(0, "alert.source.unknown"),
        /** An alert raised by an active scanner. */
        ACTIVE(1, "alert.source.active"),
        /** An alert raised manually (by the user). */
        MANUAL(2, "alert.source.manual"),
        /** An alert raised by a passive scanner. */
        PASSIVE(3, "alert.source.passive"),
        /**
         * An alert raised by other tools/functionalities in ZAP (for example, fuzzer, HTTPS Info
         * add-on, custom scripts...).
         */
        TOOL(4, "alert.source.tool");

        private final int id;
        private final String i18nKey;

        private Source(int id, String i18nKey) {
            this.id = id;
            this.i18nKey = i18nKey;
        }

        /**
         * Gets the identifier of this {@code Source}.
         *
         * <p>Should be used for persistence.
         *
         * @return the identifier.
         * @see #getSource(int)
         */
        public int getId() {
            return id;
        }

        /**
         * Gets the key for the internationalised name.
         *
         * @return the key for the internationalised name.
         */
        public String getI18nKey() {
            return i18nKey;
        }

        /**
         * Gets the {@code Source} with the given identifier.
         *
         * @param id the identifier of the {@code Source}
         * @return the {@code Source} with the given identifier, or {@link #UNKNOWN} if not a
         *     recognised identifier.
         * @see #getId()
         */
        public static Source getSource(int id) {
            switch (id) {
                case 0:
                    return UNKNOWN;
                case 1:
                    return ACTIVE;
                case 2:
                    return MANUAL;
                case 3:
                    return PASSIVE;
                case 4:
                    return TOOL;
                default:
                    return UNKNOWN;
            }
        }
    }

    public static final int RISK_INFO = 0;
    public static final int RISK_LOW = 1;
    public static final int RISK_MEDIUM = 2;
    public static final int RISK_HIGH = 3;

    // ZAP: Added FALSE_POSITIVE
    public static final int CONFIDENCE_FALSE_POSITIVE = 0;

    public static final int CONFIDENCE_LOW = 1;

    public static final int CONFIDENCE_MEDIUM = 2;
    public static final int CONFIDENCE_HIGH = 3;
    public static final int CONFIDENCE_USER_CONFIRMED = 4;

    public static final String[] MSG_RISK = {"Informational", "Low", "Medium", "High"};

    public static final String[] MSG_CONFIDENCE = {
        "False Positive", "Low", "Medium", "High", "Confirmed"
    };

    private int alertId = -1; // ZAP: Changed default alertId
    private int pluginId = -1;
    private String name = "";
    private int risk = RISK_INFO;
    private int confidence = CONFIDENCE_MEDIUM;
    private String description = "";
    private String uri = "";
    private String param = "";
    private String attack = "";
    private String otherInfo = "";
    private String solution = "";
    private String reference = "";
    private String evidence = "";
    private String inputVector = "";
    private int cweId = -1;
    private int wascId = -1;
    // Temporary ref - should be cleared asap after use
    private HttpMessage message = null;
    // ZAP: Added sourceHistoryId to Alert
    private int sourceHistoryId = 0;
    private HistoryReference historyRef = null;
    // ZAP: Added logger
    private static final Logger logger = LogManager.getLogger(Alert.class);
    // Cache this info so that we dont have to keep a ref to the HttpMessage
    private String method = "";
    private String postData;
    private URI msgUri = null;
    private Source source = Source.UNKNOWN;
    private String alertRef = "";
    private Map<String, String> tags = Collections.emptyMap();

    public Alert(int pluginId) {
        this.pluginId = pluginId;
        if (pluginId > -1) {
            // By default the alertRef is the plugin ID but rules should set this if they raise > 1
            // type of alert
            this.alertRef = Integer.toString(pluginId);
        }
    }

    public Alert(int pluginId, int risk, int confidence, String name) {
        this(pluginId);
        setRiskConfidence(risk, confidence);
        setName(name);
    }

    public Alert(RecordAlert recordAlert) {
        this(
                recordAlert.getPluginId(),
                recordAlert.getRisk(),
                recordAlert.getConfidence(),
                recordAlert.getAlert());

        HistoryReference hRef = null;
        try {
            hRef = new HistoryReference(recordAlert.getHistoryId());

        } catch (HttpMalformedHeaderException e) {
            // ZAP: Just an indication the history record doesn't exist
            logger.debug(e.getMessage(), e);
        } catch (Exception e) {
            // ZAP: Log the exception
            logger.error(e.getMessage(), e);
        }

        init(recordAlert, hRef);
    }

    private void init(RecordAlert recordAlert, HistoryReference ref) {
        this.alertId = recordAlert.getAlertId();
        this.source = Source.getSource(recordAlert.getSourceId());
        setDetail(
                recordAlert.getDescription(),
                recordAlert.getUri(),
                recordAlert.getParam(),
                recordAlert.getAttack(),
                recordAlert.getOtherInfo(),
                recordAlert.getSolution(),
                recordAlert.getReference(),
                recordAlert.getEvidence(),
                recordAlert.getCweId(),
                recordAlert.getWascId(),
                null);
        setInputVector(recordAlert.getInputVector());
        setHistoryRef(ref);
        String alertRef = recordAlert.getAlertRef();
        if (alertRef != null) {
            this.setAlertRef(alertRef);
        }
    }

    public Alert(RecordAlert recordAlert, HistoryReference ref) {
        this(
                recordAlert.getPluginId(),
                recordAlert.getRisk(),
                recordAlert.getConfidence(),
                recordAlert.getAlert());

        init(recordAlert, ref);
    }

    public void setRiskConfidence(int risk, int confidence) {
        setRisk(risk);
        setConfidence(confidence);
    }

    /**
     * Sets the risk of the alert.
     *
     * @param risk the new risk.
     * @since 2.9.0
     */
    public void setRisk(int risk) {
        this.risk = risk;
    }

    /**
     * Sets the confidence of the alert.
     *
     * @param confidence the new confidence.
     * @since 2.9.0
     */
    public void setConfidence(int confidence) {
        this.confidence = confidence;
    }

    /**
     * Sets the name of the alert to name
     *
     * @param name the name to set for the alert
     * @since 2.5.0
     */
    public void setName(String name) {
        this.name = (name == null) ? "" : name;
    }

    /**
     * Sets the details of the alert.
     *
     * @param description the description of the alert
     * @param uri the URI that has the issue
     * @param param the parameter that has the issue
     * @param attack the attack that triggers the issue
     * @param otherInfo other information about the issue
     * @param solution the solution for the issue
     * @param reference references about the issue
     * @param evidence the evidence (in the HTTP response) that the issue exists
     * @param cweId the CWE ID of the issue
     * @param wascId the WASC ID of the issue
     * @param msg the HTTP message that triggers/triggered the issue
     * @since 2.2.0
     * @see Builder
     */
    public void setDetail(
            String description,
            String uri,
            String param,
            String attack,
            String otherInfo,
            String solution,
            String reference,
            String evidence,
            int cweId,
            int wascId,
            HttpMessage msg) {
        setDescription(description);
        setUri(uri);
        setParam(param);
        setAttack(attack);
        setOtherInfo(otherInfo);
        setSolution(solution);
        setReference(reference);
        setMessage(msg);
        setEvidence(evidence);
        setCweId(cweId);
        setWascId(wascId);
        if (msg != null) {
            setHistoryRef(msg.getHistoryRef());
        }
    }

    private void setDetail(
            String description,
            String uri,
            String param,
            String attack,
            String otherInfo,
            String solution,
            String reference,
            HistoryReference href) {
        setDescription(description);
        setUri(uri);
        setParam(param);
        setAttack(attack);
        setOtherInfo(otherInfo);
        setSolution(solution);
        setReference(reference);
        setHistoryRef(href);
    }

    public void setUri(String uri) {
        this.uri = (uri == null) ? "" : uri;
    }

    public void setDescription(String description) {
        this.description = (description == null) ? "" : description;
    }

    public void setParam(String param) {
        this.param = (param == null) ? "" : param;
    }

    public void setOtherInfo(String otherInfo) {
        this.otherInfo = (otherInfo == null) ? "" : otherInfo;
    }

    public void setSolution(String solution) {
        this.solution = (solution == null) ? "" : solution;
    }

    public void setReference(String reference) {
        this.reference = (reference == null) ? "" : reference;
    }

    public void setMessage(HttpMessage message) {
        if (message != null) {
            this.message = message;
            this.method = message.getRequestHeader().getMethod();
            this.postData = message.getRequestBody().toString();
            this.msgUri = message.getRequestHeader().getURI();
        } else {
            // Used to clear the ref so we dont hold onto it
            this.message = null;
        }
    }

    @Override
    public int compareTo(Alert alert2) {
        if (risk < alert2.risk) {
            return -1;
        } else if (risk > alert2.risk) {
            return 1;
        }

        if (confidence < alert2.confidence) {
            return -1;
        } else if (confidence > alert2.confidence) {
            return 1;
        }

        if (pluginId < alert2.pluginId) {
            return -1;
        } else if (pluginId > alert2.pluginId) {
            return 1;
        }

        int result = compareStrings(alertRef, alert2.alertRef);
        if (result != 0) {
            return result;
        }

        result = name.compareToIgnoreCase(alert2.name);
        if (result != 0) {
            return result;
        }

        result = method.compareToIgnoreCase(alert2.method);
        if (result != 0) {
            return result;
        }

        // ZAP: changed to compare the field uri with alert2.uri
        result = uri.compareToIgnoreCase(alert2.uri);
        if (result != 0) {
            return result;
        }

        // ZAP: changed to compare the field param with alert2.param
        result = param.compareToIgnoreCase(alert2.param);
        if (result != 0) {
            return result;
        }

        result = otherInfo.compareToIgnoreCase(alert2.otherInfo);
        if (result != 0) {
            return result;
        }

        result = compareStrings(evidence, alert2.evidence);
        if (result != 0) {
            return result;
        }

        result = inputVector.compareTo(alert2.inputVector);
        if (result != 0) {
            return result;
        }

        return compareStrings(attack, alert2.attack);
    }

    private int compareStrings(String string, String otherString) {
        if (string == null) {
            if (otherString == null) {
                return 0;
            }
            return -1;
        } else if (otherString == null) {
            return 1;
        }
        return string.compareTo(otherString);
    }

    /**
     * Override equals. Alerts are equal if the plugin id, alert, other info, uri and param is the
     * same.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        Alert item = (Alert) obj;
        if (risk != item.risk) {
            return false;
        }
        if (confidence != item.confidence) {
            return false;
        }
        if (pluginId != item.pluginId) {
            return false;
        }
        if (!alertRef.equals(item.alertRef)) {
            return false;
        }
        if (!name.equals(item.name)) {
            return false;
        }
        if (!method.equalsIgnoreCase(item.method)) {
            return false;
        }
        if (!uri.equalsIgnoreCase(item.uri)) {
            return false;
        }
        if (!param.equalsIgnoreCase(item.param)) {
            return false;
        }
        if (!otherInfo.equalsIgnoreCase(item.otherInfo)) {
            return false;
        }
        if (evidence == null) {
            if (item.evidence != null) {
                return false;
            }
        } else if (!evidence.equals(item.evidence)) {
            return false;
        }
        if (!inputVector.equals(item.inputVector)) {
            return false;
        }
        if (attack == null) {
            if (item.attack != null) {
                return false;
            }
        } else if (!attack.equals(item.attack)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + risk;
        result = prime * result + confidence;
        result = prime * result + ((evidence == null) ? 0 : evidence.hashCode());
        result = prime * result + inputVector.hashCode();
        result = prime * result + name.hashCode();
        result = prime * result + otherInfo.hashCode();
        result = prime * result + param.hashCode();
        result = prime * result + pluginId;
        result = prime * result + alertRef.hashCode();
        result = prime * result + method.hashCode();
        result = prime * result + uri.hashCode();
        result = prime * result + ((attack == null) ? 0 : attack.hashCode());
        return result;
    }

    /**
     * Creates a new instance of {@code Alert} with same members.
     *
     * @return a new {@code Alert} instance
     */
    public Alert newInstance() {
        Alert item = new Alert(this.pluginId);
        item.setRiskConfidence(this.risk, this.confidence);
        item.setName(this.name);
        item.setDetail(
                this.description,
                this.uri,
                this.param,
                this.attack,
                this.otherInfo,
                this.solution,
                this.reference,
                this.historyRef);
        item.setEvidence(this.evidence);
        item.setInputVector(inputVector);
        item.setCweId(this.cweId);
        item.setWascId(this.wascId);
        item.setSource(this.source);
        item.setTags(this.tags);
        return item;
    }

    public String toPluginXML(String urls) {
        StringBuilder sb = new StringBuilder(150); // ZAP: Changed the type to StringBuilder.
        sb.append("<alertitem>\r\n");
        sb.append("  <pluginid>").append(pluginId).append("</pluginid>\r\n");
        sb.append("  <alertRef>").append(alertRef).append("</alertRef>\r\n");
        sb.append("  <alert>")
                .append(replaceEntity(name))
                .append("</alert>\r\n"); // Deprecated in 2.5.0, maintain for compatibility with
        // custom code
        sb.append("  <name>").append(replaceEntity(name)).append("</name>\r\n");
        sb.append("  <riskcode>").append(risk).append("</riskcode>\r\n");
        sb.append("  <confidence>").append(confidence).append("</confidence>\r\n");
        sb.append("  <riskdesc>")
                .append(replaceEntity(MSG_RISK[risk] + " (" + MSG_CONFIDENCE[confidence] + ")"))
                .append("</riskdesc>\r\n");
        sb.append("  <desc>").append(replaceEntity(paragraph(description))).append("</desc>\r\n");

        sb.append(urls);

        sb.append("  <solution>")
                .append(replaceEntity(paragraph(solution)))
                .append("</solution>\r\n");
        // ZAP: Added otherInfo to the report
        if (otherInfo != null && otherInfo.length() > 0) {
            sb.append("  <otherinfo>")
                    .append(replaceEntity(paragraph(otherInfo)))
                    .append("</otherinfo>\r\n");
        }
        sb.append("  <reference>")
                .append(replaceEntity(paragraph(reference)))
                .append("</reference>\r\n");
        if (cweId > 0) {
            sb.append("  <cweid>").append(cweId).append("</cweid>\r\n");
        }
        if (wascId > 0) {
            sb.append("  <wascid>").append(wascId).append("</wascid>\r\n");
        }
        sb.append("  <sourceid>").append(source.getId()).append("</sourceid>\r\n");

        sb.append("</alertitem>\r\n");
        return sb.toString();
    }

    public String replaceEntity(String text) {
        String result = null;
        if (text != null) {
            result = entityEncode(text);
        }
        return result;
    }

    /** Encode entity for HTML or XML output. */
    private static String entityEncode(String text) {
        String result = text;

        if (result == null) {
            return result;
        }

        // The escapeXml function doesn't cope with some 'special' chrs

        return StringEscapeUtils.escapeXml10(XMLStringUtil.escapeControlChrs(result));
    }

    public String paragraph(String text) {
        return "<p>" + text.replaceAll("\\r\\n", "</p><p>").replaceAll("\\n", "</p><p>") + "</p>";
    }

    /**
     * @return Returns the name of the alert.
     * @since 2.5.0
     */
    public String getName() {
        return name;
    }
    /** @return Returns the description. */
    public String getDescription() {
        return description;
    }
    /** @return Returns the id. */
    public int getPluginId() {
        return pluginId;
    }
    /** @return Returns the message. */
    public HttpMessage getMessage() {
        if (this.message != null) {
            return this.message;
        }
        if (this.historyRef != null) {
            try {
                return this.historyRef.getHttpMessage();
            } catch (HttpMalformedHeaderException | DatabaseException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return null;
    }
    /** @return Returns the otherInfo. */
    public String getOtherInfo() {
        return otherInfo;
    }
    /** @return Returns the param. */
    public String getParam() {
        return param;
    }
    /** @return Returns the reference. */
    public String getReference() {
        return reference;
    }

    /** @return Returns the confidence. */
    public int getConfidence() {
        return confidence;
    }

    /** @return Returns the risk. */
    public int getRisk() {
        return risk;
    }

    /**
     * Gets the correctly scaled icon for this alert.
     *
     * @return the correctly scaled icon for this alert
     * @since 2.6.0
     */
    public ImageIcon getIcon() {
        if (confidence == Alert.CONFIDENCE_FALSE_POSITIVE) {
            // Special case - theres no risk - use the green flag
            return DisplayUtils.getScaledIcon(Constant.OK_FLAG_IMAGE_URL);
        }

        switch (risk) {
            case Alert.RISK_INFO:
                return DisplayUtils.getScaledIcon(Constant.INFO_FLAG_IMAGE_URL);
            case Alert.RISK_LOW:
                return DisplayUtils.getScaledIcon(Constant.LOW_FLAG_IMAGE_URL);
            case Alert.RISK_MEDIUM:
                return DisplayUtils.getScaledIcon(Constant.MED_FLAG_IMAGE_URL);
            case Alert.RISK_HIGH:
                return DisplayUtils.getScaledIcon(Constant.HIGH_FLAG_IMAGE_URL);
        }
        return null;
    }

    @Deprecated
    public URL getIconUrl() {
        if (confidence == Alert.CONFIDENCE_FALSE_POSITIVE) {
            // Special case - theres no risk - use the green flag
            return Constant.OK_FLAG_IMAGE_URL;
        }

        switch (risk) {
            case Alert.RISK_INFO:
                return Constant.INFO_FLAG_IMAGE_URL;
            case Alert.RISK_LOW:
                return Constant.LOW_FLAG_IMAGE_URL;
            case Alert.RISK_MEDIUM:
                return Constant.MED_FLAG_IMAGE_URL;
            case Alert.RISK_HIGH:
                return Constant.HIGH_FLAG_IMAGE_URL;
        }
        return null;
    }
    /** @return Returns the solution. */
    public String getSolution() {
        return solution;
    }
    /** @return Returns the uri. */
    public String getUri() {
        return uri;
    }
    /** @return Returns the alertId. */
    public int getAlertId() {
        return alertId;
    }
    /** @param alertId The alertId to set. */
    public void setAlertId(int alertId) {
        this.alertId = alertId;
    }

    public String getUrlParamXML() {
        StringBuilder sb = new StringBuilder(200); // ZAP: Changed the type to StringBuilder.
        sb.append("  <uri>").append(replaceEntity(uri)).append("</uri>\r\n");
        sb.append("  <method>").append(replaceEntity(method)).append("</method>\r\n");
        if (param != null && param.length() > 0) {
            sb.append("  <param>").append(replaceEntity(param)).append("</param>\r\n");
        }
        if (attack != null && attack.length() > 0) {
            sb.append("  <attack>").append(replaceEntity(attack)).append("</attack>\r\n");
        }
        if (evidence != null && evidence.length() > 0) {
            sb.append("  <evidence>").append(replaceEntity(evidence)).append("</evidence>\r\n");
        }
        return sb.toString();
    }

    public int getSourceHistoryId() {
        return sourceHistoryId;
    }

    public void setSourceHistoryId(int sourceHistoryId) {
        this.sourceHistoryId = sourceHistoryId;
    }

    public HistoryReference getHistoryRef() {
        return this.historyRef;
    }

    public void setHistoryRef(HistoryReference historyRef) {
        this.historyRef = historyRef;
        if (historyRef != null) {
            this.message = null;
            this.method = historyRef.getMethod();
            this.msgUri = historyRef.getURI();
            this.postData = historyRef.getRequestBody();
            this.sourceHistoryId = historyRef.getHistoryId();
        }
    }

    public String getAttack() {
        return attack;
    }

    public void setAttack(String attack) {
        this.attack = (attack == null) ? "" : attack;
    }

    public String getMethod() {
        return method;
    }

    public String getPostData() {
        return postData;
    }

    public URI getMsgUri() {
        return msgUri;
    }

    public String getEvidence() {
        return evidence;
    }

    public void setEvidence(String evidence) {
        this.evidence = (evidence == null) ? "" : evidence;
    }

    /**
     * Gets the input vector used to find the alert.
     *
     * @return the short name of the input vector, never {@code null}.
     * @since 2.12.0
     */
    public String getInputVector() {
        return inputVector;
    }

    /**
     * Sets the input vector used to find the alert.
     *
     * @param inputVector the short name of the input vector.
     * @since 2.12.0
     */
    public void setInputVector(String inputVector) {
        this.inputVector = inputVector == null ? "" : inputVector;
    }

    public int getCweId() {
        return cweId;
    }

    public void setCweId(int cweId) {
        this.cweId = cweId;
    }

    public int getWascId() {
        return wascId;
    }

    public void setWascId(int wascId) {
        this.wascId = wascId;
    }

    /** @since 2.11.0 */
    public Map<String, String> getTags() {
        return tags;
    }

    /** @since 2.11.0 */
    public void setTags(Map<String, String> tags) {
        if (tags != null) {
            this.tags = tags;
        }
    }

    /**
     * Gets the source of the alert.
     *
     * @return the source of the alert, never {@code null}.
     * @since 2.6.0
     */
    public Source getSource() {
        return source;
    }

    /**
     * Sets the source of the alert.
     *
     * <p><strong>Note:</strong> The source should be considered immutable and should be set before
     * the alert is persisted (normally by the tool/functionality raising the alert).
     *
     * @param source the source of the alert.
     * @throws IllegalArgumentException if the given {@code source} is {@code null}.
     * @since 2.6.0
     */
    public void setSource(Source source) {
        if (source == null) {
            throw new IllegalArgumentException("Parameter source must not be null.");
        }
        this.source = source;
    }

    /**
     * Gets the alert reference.
     *
     * <p>This is a unique identifier for the type of alert raised. A scan rule may raise more that
     * one type of alert and they should all have different alert references.
     *
     * @return the alert reference
     * @since 2.10.0
     */
    public String getAlertRef() {
        return alertRef;
    }

    /**
     * Sets the alert reference.
     *
     * <p>For manually raised alerts this should be an empty string. For alerts raised by scan rules
     * it should start with the rule plugin id and optionally include a 'qualifier' (such as "-1",
     * "-2" etc). Logically different alerts should have different alert references even if they are
     * raised by the same scan rule.
     *
     * @param alertRef the alert reference
     * @since 2.10.0
     */
    public void setAlertRef(String alertRef) {
        if (alertRef == null) {
            throw new IllegalArgumentException("Alert reference must not be null");
        }
        if (alertRef.length() > 0) {
            if (alertRef.length() >= 256) {
                throw new IllegalArgumentException("Alert reference too big: " + alertRef.length());
            }
            if (!alertRef.startsWith(Integer.toString(this.pluginId))) {
                throw new IllegalArgumentException(
                        "Alert reference "
                                + alertRef
                                + " must start with the plugin id "
                                + this.pluginId);
            }
        }
        this.alertRef = alertRef;
    }

    /**
     * Returns a new alert builder.
     *
     * @return the alert builder.
     * @since 2.9.0
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * A builder of alerts.
     *
     * @since 2.9.0
     * @see #build()
     */
    public static class Builder {

        private int alertId = -1;
        private int pluginId;
        private String name;
        private int risk = RISK_INFO;
        private int confidence = CONFIDENCE_MEDIUM;
        private String description;
        private String uri;
        private String param;
        private String attack;
        private String otherInfo;
        private String solution;
        private String reference;
        private String evidence;
        private String inputVector;
        private int cweId = -1;
        private int wascId = -1;
        private HttpMessage message;
        private int sourceHistoryId;
        private HistoryReference historyRef;
        private Source source = Source.UNKNOWN;
        private String alertRef;
        private Map<String, String> tags;

        protected Builder() {}

        public Builder setAlertId(int alertId) {
            this.alertId = alertId;
            return this;
        }

        public Builder setPluginId(int pluginId) {
            this.pluginId = pluginId;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setRisk(int risk) {
            this.risk = risk;
            return this;
        }

        public Builder setConfidence(int confidence) {
            this.confidence = confidence;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setUri(String uri) {
            this.uri = uri;
            return this;
        }

        public Builder setParam(String param) {
            this.param = param;
            return this;
        }

        public Builder setAttack(String attack) {
            this.attack = attack;
            return this;
        }

        public Builder setOtherInfo(String otherInfo) {
            this.otherInfo = otherInfo;
            return this;
        }

        public Builder setSolution(String solution) {
            this.solution = solution;
            return this;
        }

        public Builder setReference(String reference) {
            this.reference = reference;
            return this;
        }

        public Builder setEvidence(String evidence) {
            this.evidence = evidence;
            return this;
        }

        /**
         * Sets the input vector used to find the alert.
         *
         * @param inputVector the short name of the input vector.
         * @return the builder for chaining.
         * @since 2.12.0
         */
        public Builder setInputVector(String inputVector) {
            this.inputVector = inputVector;
            return this;
        }

        public Builder setCweId(int cweId) {
            this.cweId = cweId;
            return this;
        }

        public Builder setWascId(int wascId) {
            this.wascId = wascId;
            return this;
        }

        public Builder setMessage(HttpMessage message) {
            this.message = message;
            return this;
        }

        public Builder setSourceHistoryId(int sourceHistoryId) {
            this.sourceHistoryId = sourceHistoryId;
            return this;
        }

        public Builder setHistoryRef(HistoryReference historyRef) {
            this.historyRef = historyRef;
            return this;
        }

        public Builder setSource(Source source) {
            this.source = source;
            return this;
        }

        public Builder setAlertRef(String alertRef) {
            this.alertRef = alertRef;
            return this;
        }

        /** @since 2.11.0 */
        public Builder setTags(Map<String, String> tags) {
            this.tags = tags;
            return this;
        }

        /**
         * Adds an Alert tag with the given key (tag/name) to the existing collection for this
         * Alert/Builder.
         *
         * @since 2.12.0
         */
        public Builder addTag(String tag) {
            addTag(tag, "");
            return this;
        }

        /**
         * Adds an Alert tag with the given key (tag/name) and value to the existing collection for
         * this Alert/Builder.
         *
         * @since 2.12.0
         */
        public Builder addTag(String tag, String value) {
            if (this.tags == null) {
                this.tags = new HashMap<>();
            }
            this.tags.put(tag, value);
            return this;
        }

        /**
         * Removes an Alert tag with the given key (tag/name) from the existing collection for this
         * Alert/Builder.
         *
         * @since 2.12.0
         */
        public Builder removeTag(String tag) {
            if (this.tags == null) {
                return this;
            }
            this.tags.remove(tag);
            return this;
        }

        /**
         * Removes an Alert tag with the given key (tag/name) and value from the existing collection
         * for this Alert/Builder.
         *
         * @since 2.12.0
         */
        public Builder removeTag(String tag, String value) {
            if (this.tags == null) {
                return this;
            }
            this.tags.remove(tag, value);
            return this;
        }

        /**
         * Builds the alert from the specified data.
         *
         * <p>The alert URI defaults to the one from the {@code HistoryReference} or {@code
         * HttpMessage} if set.
         *
         * @return the alert with specified data.
         */
        public final Alert build() {
            String alertUri = uri;
            if (alertUri == null || alertUri.isEmpty()) {
                if (historyRef != null) {
                    alertUri = historyRef.getURI().toString();
                } else if (message != null) {
                    alertUri = message.getRequestHeader().getURI().toString();
                }
            }

            Alert alert = new Alert(pluginId);
            alert.setAlertId(alertId);
            alert.setName(name);
            alert.setRisk(risk);
            alert.setConfidence(confidence);
            alert.setDescription(description);
            alert.setUri(alertUri);
            alert.setParam(param);
            alert.setAttack(attack);
            alert.setOtherInfo(otherInfo);
            alert.setSolution(solution);
            alert.setReference(reference);
            alert.setEvidence(evidence);
            alert.setInputVector(inputVector);
            alert.setCweId(cweId);
            alert.setWascId(wascId);
            alert.setMessage(message);
            alert.setSourceHistoryId(sourceHistoryId);
            alert.setHistoryRef(historyRef);
            alert.setSource(source);
            if (alertRef != null) {
                alert.setAlertRef(alertRef);
            }
            alert.setTags(tags);

            return alert;
        }
    }

    /**
     * Checks if a value {@code int} is between {@value #RISK_INFO} (RISK_INFO) and {@value
     * #RISK_HIGH} (RISK_HIGH)
     *
     * @return true if the checked risk ({@code int}) is in the range, false otherwise
     * @since 2.9.0
     * @see #RISK_INFO
     * @see #RISK_HIGH
     */
    public static boolean isValidRisk(int risk) {
        return risk >= RISK_INFO && risk <= RISK_HIGH;
    }

    /**
     * Checks if a value {@code int} is between {@value #CONFIDENCE_FALSE_POSITIVE}
     * (CONFIDENCE_FALSE_POSITIVE) and {@value #CONFIDENCE_USER_CONFIRMED}
     * (CONFIDENCE_USER_CONFIRMED)
     *
     * @return true if the checked confidence ({@code int}) is in the range, false otherwise
     * @since 2.9.0
     * @see #CONFIDENCE_FALSE_POSITIVE
     * @see #CONFIDENCE_USER_CONFIRMED
     */
    public static boolean isValidConfidence(int confidence) {
        return confidence >= CONFIDENCE_FALSE_POSITIVE && confidence <= CONFIDENCE_USER_CONFIRMED;
    }
}
