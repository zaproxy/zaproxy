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
// ZAP: 2012/01/02 Separate param and attack
// ZAP: 2012/03/03 Added getLevel(boolean incDefault)
// ZAP: 2102/03/15 Changed the type of the parameter "sb" of the method matchBodyPattern to
// StringBuilder.
// ZAP: 2012/04/25 Added @Override annotation to all appropriate methods.
// ZAP: 2012/08/07 Renamed Level to AlertThreshold and added support for AttackStrength
// ZAP: 2012/08/31 Enabled control of AttackStrength
// ZAP: 2012/10/03 Issue 388 Added enabling support for technologies
// ZAP: 2013/01/19 Issue 460 Add support for a scan progress dialog
// ZAP: 2013/01/25 Removed the "(non-Javadoc)" comments.
// ZAP: 2013/02/19 Issue 528 Scan progress dialog can show negative progress times
// ZAP: 2013/04/14 Issue 611: Log the exceptions thrown by active scanners as error
// ZAP: 2013/05/02 Re-arranged all modifiers into Java coding standard order
// ZAP: 2013/07/12 Issue 713: Add CWE and WASC numbers to issues
// ZAP: 2013/09/08 Issue 691: Handle old plugins
// ZAP: 2013/11/16 Issue 842: NullPointerException while active scanning with ExtensionAntiCSRF
// disabled
// ZAP: 2014/01/16 Add support to plugin skipping
// ZAP: 2014/02/12 Issue 1030: Load and save scan policies
// ZAP: 2014/02/21 Issue 1043: Custom active scan dialog
// ZAP: 2014/05/15 Issue 1196: AbstractPlugin.bingo incorrectly sets evidence to attack
// ZAP: 2014/05/23 Issue 1209: Reliability becomes Confidence and add levels
// ZAP: 2014/07/07 Issue 389: Enable technology scope for scanners
// ZAP: 2014/10/25 Issue 1062: Made plugins that calls sendandrecieve also invoke scanner
// hook before and after message update
// ZAP: 2014/11/19 Issue 1412: Init scan rule status (quality) from add-on
// ZAP: 2015/03/26 Issue 1573: Add option to inject plugin ID in header for all ascan requests
// ZAP: 2015/07/26 Issue 1618: Target Technology Not Honored
// ZAP: 2015/08/19 Issue 1785: Plugin enabled even if dependencies are not, "hangs" active scan
// ZAP: 2016/03/22 Implement init() and getDependency() by default, most plugins do not use them
// ZAP: 2016/04/21 Include Plugin itself when notifying of a new message sent
// ZAP: 2016/05/03 Remove exceptions' stack trace prints
// ZAP: 2016/06/10 Honour scan's scope when following redirections
// ZAP: 2016/07/12 Do not allow techSet to be null
// ZAP: 2017/03/27 Use HttpRequestConfig.
// ZAP: 2017/05/31 Remove re-declaration of methods.
// ZAP: 2017/10/31 Use ExtensionLoader.getExtension(Class).
// ZAP: 2017/11/14 Notify completion in a finally block.
// ZAP: 2017/12/29 Rely on HostProcess to validate the redirections.
// ZAP: 2018/02/02 Add helper method to check if any of several techs is in scope.
// ZAP: 2018/08/15 Implemented hashCode
// ZAP: 2019/03/22 Add bingo with references.
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2019/10/21 Use and expose Alert builder.
// ZAP: 2020/01/27 Extracted code from sendAndReceive method into regenerateAntiCsrfToken method in
// ExtensionAntiCSRF.
// ZAP: 2020/09/23 Add functionality for custom error pages handling (Issue 9).
// ZAP: 2020/11/17 Use new TechSet#getAllTech().
// ZAP: 2020/11/26 Use Log4j2 getLogger() and deprecate Log4j1.x.
// ZAP: 2021/07/20 Correct message updated with the scan rule ID header (Issue 6689).
// ZAP: 2022/06/11 Add functionality for custom pages AUTHN/AUTHZ handling.
// ZAP: 2022/06/05 Remove usage of HttpException.
// ZAP: 2022/08/03 Keep enabled state when setting default alert threshold (Issue 7400).
// ZAP: 2022/09/08 Use format specifiers instead of concatenation when logging.
// ZAP: 2022/09/28 Do not set the Content-Length header when the method does not require one.
package org.parosproxy.paros.core.scanner;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.InvalidParameterException;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.scanner.Alert.Source;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.parosproxy.paros.network.HttpStatusCode;
import org.zaproxy.zap.control.AddOn;
import org.zaproxy.zap.extension.anticsrf.ExtensionAntiCSRF;
import org.zaproxy.zap.extension.custompages.CustomPage;
import org.zaproxy.zap.model.Tech;
import org.zaproxy.zap.model.TechSet;

public abstract class AbstractPlugin implements Plugin, Comparable<Object> {

    private static final String[] NO_DEPENDENCIES = {};

    /** Default pattern used in pattern check for most plugins. */
    protected static final int PATTERN_PARAM = Pattern.CASE_INSENSITIVE | Pattern.MULTILINE;
    /** CRLF string. */
    protected static final String CRLF = "\r\n";

    private HostProcess parent = null;
    private HttpMessage msg = null;
    private boolean enabled = true;
    private Logger logger = LogManager.getLogger(this.getClass());
    private Configuration config = null;
    // ZAP Added delayInMs
    private int delayInMs;
    private ExtensionAntiCSRF extAntiCSRF = null;
    private AlertThreshold defaultAttackThreshold = AlertThreshold.MEDIUM;
    private static final AlertThreshold[] alertThresholdsSupported =
            new AlertThreshold[] {AlertThreshold.MEDIUM};
    private AttackStrength defaultAttackStrength = AttackStrength.MEDIUM;
    private static final AttackStrength[] attackStrengthsSupported =
            new AttackStrength[] {AttackStrength.MEDIUM};
    private TechSet techSet;
    private Date started = null;
    private Date finished = null;
    private AddOn.Status status = AddOn.Status.unknown;

    /** Default Constructor */
    public AbstractPlugin() {
        this.techSet = TechSet.getAllTech();
    }

    @Override
    public String getCodeName() {
        String result = getClass().getName();
        int pos = getClass().getName().lastIndexOf(".");
        if (pos > -1) {
            result = result.substring(pos + 1);
        }
        return result;
    }

    /**
     * Returns no dependencies by default.
     *
     * @since 2.5.0
     * @return an empty array (that is, no dependencies)
     */
    @Override
    public String[] getDependency() {
        return NO_DEPENDENCIES;
    }

    @Override
    public void init(HttpMessage msg, HostProcess parent) {
        this.msg = msg.cloneAll();
        this.parent = parent;
        init();
    }

    /**
     * Finishes the initialisation of the plugin, subclasses should add any initialisation
     * logic/code to this method.
     *
     * <p>Called after the plugin has been initialised with the message being scanned. By default it
     * does nothing.
     *
     * <p>Since 2.5.0 it is no longer abstract.
     *
     * @see #init(HttpMessage, HostProcess)
     */
    public void init() {}

    /**
     * Obtain a new HttpMessage with the same request as the base. The response is empty. This is
     * used by plugin to build/craft a new message to send/receive. It does not affect the base
     * message.
     *
     * @return A new HttpMessage with cloned request. Response is empty.
     */
    protected HttpMessage getNewMsg() {
        return msg.cloneRequest();
    }

    /**
     * Get the base reference HttpMessage for this check. Both request and response is present. It
     * should not be modified during when the plugin runs.
     *
     * @return The base HttpMessage with request/response.
     */
    protected HttpMessage getBaseMsg() {
        return msg;
    }

    /**
     * Sends and receives the given {@code message}, always following redirections.
     *
     * <p>The following changes are made to the request before being sent:
     *
     * <ul>
     *   <li>The anti-CSRF token contained in the message will be handled/regenerated, if any;
     *   <li>The request headers {@link HttpHeader#IF_MODIFIED_SINCE} and {@link
     *       HttpHeader#IF_NONE_MATCH} are removed, to always obtain a fresh response;
     *   <li>The header {@link HttpHeader#CONTENT_LENGTH} is updated, to match the length of the
     *       request body.
     *   <li>Changes done by {@link org.zaproxy.zap.network.HttpSenderListener HttpSenderListener}
     *       (for example, scripts).
     * </ul>
     *
     * @param message the message to be sent and received
     * @throws IOException if an I/O error occurred (for example, read time out)
     * @see #sendAndReceive(HttpMessage, boolean)
     * @see #sendAndReceive(HttpMessage, boolean, boolean)
     */
    protected void sendAndReceive(HttpMessage message) throws IOException {
        sendAndReceive(message, true);
    }

    /**
     * Sends and receives the given {@code message}, optionally following redirections.
     *
     * <p>The following changes are made to the request before being sent:
     *
     * <ul>
     *   <li>The anti-CSRF token contained in the message will be handled/regenerated, if any;
     *   <li>The request headers {@link HttpHeader#IF_MODIFIED_SINCE} and {@link
     *       HttpHeader#IF_NONE_MATCH} are removed, to always obtain a fresh response;
     *   <li>The header {@link HttpHeader#CONTENT_LENGTH} is updated, to match the length of the
     *       request body.
     *   <li>Changes done by {@link org.zaproxy.zap.network.HttpSenderListener HttpSenderListener}
     *       (for example, scripts).
     * </ul>
     *
     * @param message the message to be sent and received
     * @param isFollowRedirect {@code true} if redirections should be followed, {@code false}
     *     otherwise
     * @throws IOException if an I/O error occurred (for example, read time out)
     * @see #sendAndReceive(HttpMessage)
     * @see #sendAndReceive(HttpMessage, boolean, boolean)
     */
    protected void sendAndReceive(HttpMessage message, boolean isFollowRedirect)
            throws IOException {
        sendAndReceive(message, isFollowRedirect, true);
    }

    /**
     * Sends and receives the given {@code message}, optionally following redirections and
     * optionally regenerating anti-CSRF token, if any.
     *
     * <p>The following changes are made to the request before being sent:
     *
     * <ul>
     *   <li>The request headers {@link HttpHeader#IF_MODIFIED_SINCE} and {@link
     *       HttpHeader#IF_NONE_MATCH} are removed, to always obtain a fresh response;
     *   <li>The header {@link HttpHeader#CONTENT_LENGTH} is updated, to match the length of the
     *       request body.
     *   <li>Changes done by {@link org.zaproxy.zap.network.HttpSenderListener HttpSenderListener}
     *       (for example, scripts).
     * </ul>
     *
     * @param message the message to be sent and received
     * @param isFollowRedirect {@code true} if redirections should be followed, {@code false}
     *     otherwise
     * @param handleAntiCSRF {@code true} if the anti-CSRF token present in the request should be
     *     handled/regenerated, {@code false} otherwise
     * @throws IOException if an I/O error occurred (for example, read time out)
     * @see #sendAndReceive(HttpMessage)
     * @see #sendAndReceive(HttpMessage, boolean)
     */
    protected void sendAndReceive(
            HttpMessage message, boolean isFollowRedirect, boolean handleAntiCSRF)
            throws IOException {

        if (parent.handleAntiCsrfTokens() && handleAntiCSRF) {
            if (extAntiCSRF == null) {
                extAntiCSRF =
                        Control.getSingleton()
                                .getExtensionLoader()
                                .getExtension(ExtensionAntiCSRF.class);
            }
            if (extAntiCSRF != null) {
                extAntiCSRF.regenerateAntiCsrfToken(
                        message, tokenMsg -> sendAndReceive(tokenMsg, true, false));
            }
        }

        if (this.parent.getScannerParam().isInjectPluginIdInHeader()) {
            message.getRequestHeader()
                    .setHeader(HttpHeader.X_ZAP_SCAN_ID, Integer.toString(getId()));
        }

        // always get the fresh copy
        message.getRequestHeader().setHeader(HttpHeader.IF_MODIFIED_SINCE, null);
        message.getRequestHeader().setHeader(HttpHeader.IF_NONE_MATCH, null);

        updateRequestContentLength(message);

        if (this.getDelayInMs() > 0) {
            try {
                Thread.sleep(this.getDelayInMs());
            } catch (InterruptedException e) {
                // Ignore
            }
        }

        // ZAP: Runs the "beforeScan" methods of any ScannerHooks
        parent.performScannerHookBeforeScan(message, this);

        if (isFollowRedirect) {
            parent.getHttpSender().sendAndReceive(message, getParent().getRedirectRequestConfig());
        } else {
            parent.getHttpSender().sendAndReceive(message, false);
        }

        // ZAP: Notify parent
        parent.notifyNewMessage(this, message);

        // ZAP: Set the history reference back and run the "afterScan" methods of any ScannerHooks
        parent.performScannerHookAfterScan(message, this);
    }

    /**
     * Updates the Content-Length header of the request.
     *
     * <p>For methods with absent or unanticipated enclosed content, the header is removed otherwise
     * in all other cases the header is updated to match the length of the content.
     *
     * @param message the message to update.
     * @since 2.12.0
     */
    protected void updateRequestContentLength(HttpMessage message) {
        int bodyLength = message.getRequestBody().length();
        String method = message.getRequestHeader().getMethod();
        if (bodyLength == 0
                && (HttpRequestHeader.GET.equalsIgnoreCase(method)
                        || HttpRequestHeader.CONNECT.equalsIgnoreCase(method)
                        || HttpRequestHeader.DELETE.equalsIgnoreCase(method)
                        || HttpRequestHeader.HEAD.equalsIgnoreCase(method)
                        || HttpRequestHeader.TRACE.equalsIgnoreCase(method))) {
            message.getRequestHeader().setHeader(HttpHeader.CONTENT_LENGTH, null);
            return;
        }
        message.getRequestHeader().setContentLength(bodyLength);
    }

    @Override
    public void run() {
        // ZAP : set skipped to false otherwise the plugin should stop continuously
        // this.skipped = false;

        try {
            if (!isStop()) {
                this.started = new Date();
                scan();
            }

        } catch (Exception e) {
            getLog().error(e.getMessage(), e);
        } finally {
            notifyPluginCompleted(getParent());
            this.finished = new Date();
        }
    }

    /**
     * Generate an alert when a security issue (risk/info) is found. Default name, description,
     * solution of this Plugin will be used.
     *
     * @param risk the risk of the new alert
     * @param confidence the confidence of the new alert
     * @param uri the affected URI
     * @param param the name/ID of the affected parameter
     * @param attack the attack that shows the issue
     * @param otherInfo other information about the issue
     * @param msg the message that shows the issue
     * @deprecated (2.9.0) Use {@link #newAlert()} to build and {@link AlertBuilder#raise() raise}
     *     the alert.
     */
    @Deprecated
    protected void bingo(
            int risk,
            int confidence,
            String uri,
            String param,
            String attack,
            String otherInfo,
            HttpMessage msg) {

        bingo(
                risk,
                confidence,
                this.getName(),
                this.getDescription(),
                uri,
                param,
                attack,
                otherInfo,
                this.getSolution(),
                msg);
    }

    /**
     * Generate an alert when a security issue (risk/info) is found. Custom alert name, description
     * and solution will be used.
     *
     * @param risk the risk of the new alert
     * @param confidence the confidence of the new alert
     * @param name the name of the new alert
     * @param description the description of the new alert
     * @param uri the affected URI
     * @param param the name/ID of the affected parameter
     * @param attack the attack that shows the issue
     * @param otherInfo other information about the issue
     * @param solution the solution for the issue
     * @param msg the message that shows the issue
     * @deprecated (2.9.0) Use {@link #newAlert()} to build and {@link AlertBuilder#raise() raise}
     *     the alert.
     */
    @Deprecated
    protected void bingo(
            int risk,
            int confidence,
            String name,
            String description,
            String uri,
            String param,
            String attack,
            String otherInfo,
            String solution,
            HttpMessage msg) {

        newAlert()
                .setRisk(risk)
                .setConfidence(confidence)
                .setName(name)
                .setDescription(description)
                .setUri(uri)
                .setParam(param)
                .setAttack(attack)
                .setOtherInfo(otherInfo)
                .setSolution(solution)
                .setMessage(msg)
                .raise();
    }

    /**
     * Generate an alert when a security issue (risk/info) is found. Default name, description,
     * solution of this Plugin will be used.
     *
     * @param risk the risk of the new alert
     * @param confidence the confidence of the new alert
     * @param uri the affected URI
     * @param param the name/ID of the affected parameter
     * @param attack the attack that shows the issue
     * @param otherInfo other information about the issue
     * @param evidence the evidence (in the response) that shows the issue
     * @param msg the message that shows the issue
     * @deprecated (2.9.0) Use {@link #newAlert()} to build and {@link AlertBuilder#raise() raise}
     *     the alert.
     */
    @Deprecated
    protected void bingo(
            int risk,
            int confidence,
            String uri,
            String param,
            String attack,
            String otherInfo,
            String evidence,
            HttpMessage msg) {

        bingo(
                risk,
                confidence,
                this.getName(),
                this.getDescription(),
                uri,
                param,
                attack,
                otherInfo,
                this.getSolution(),
                evidence,
                msg);
    }

    /**
     * Generate an alert when a security issue (risk/info) is found. Custom alert name, description
     * and solution will be used.
     *
     * @param risk the risk of the new alert
     * @param confidence the confidence of the new alert
     * @param name the name of the new alert
     * @param description the description of the new alert
     * @param uri the affected URI
     * @param param the name/ID of the affected parameter
     * @param attack the attack that shows the issue
     * @param otherInfo other information about the issue
     * @param solution the solution for the issue
     * @param evidence the evidence (in the response) that shows the issue
     * @param msg the message that shows the issue
     * @deprecated (2.9.0) Use {@link #newAlert()} to build and {@link AlertBuilder#raise() raise}
     *     the alert.
     */
    @Deprecated
    protected void bingo(
            int risk,
            int confidence,
            String name,
            String description,
            String uri,
            String param,
            String attack,
            String otherInfo,
            String solution,
            String evidence,
            HttpMessage msg) {

        newAlert()
                .setRisk(risk)
                .setConfidence(confidence)
                .setName(name)
                .setDescription(description)
                .setUri(uri)
                .setParam(param)
                .setAttack(attack)
                .setOtherInfo(otherInfo)
                .setSolution(solution)
                .setEvidence(evidence)
                .setMessage(msg)
                .raise();
    }

    /**
     * @deprecated (2.9.0) Use {@link #newAlert()} to build and {@link AlertBuilder#raise() raise}
     *     the alert.
     */
    @Deprecated
    protected void bingo(
            int risk,
            int confidence,
            String name,
            String description,
            String uri,
            String param,
            String attack,
            String otherInfo,
            String solution,
            String evidence,
            int cweId,
            int wascId,
            HttpMessage msg) {
        bingo(
                risk,
                confidence,
                name,
                description,
                uri,
                param,
                attack,
                otherInfo,
                solution,
                evidence,
                this.getReference(),
                cweId,
                wascId,
                msg);
    }

    /**
     * @deprecated (2.9.0) Use {@link #newAlert()} to build and {@link AlertBuilder#raise() raise}
     *     the alert.
     */
    @Deprecated
    protected void bingo(
            int risk,
            int confidence,
            String name,
            String description,
            String uri,
            String param,
            String attack,
            String otherInfo,
            String solution,
            String evidence,
            String reference,
            int cweId,
            int wascId,
            HttpMessage msg) {

        newAlert()
                .setRisk(risk)
                .setConfidence(confidence)
                .setName(name)
                .setDescription(description)
                .setUri(uri)
                .setParam(param)
                .setAttack(attack)
                .setOtherInfo(otherInfo)
                .setSolution(solution)
                .setEvidence(evidence)
                .setReference(reference)
                .setCweId(cweId)
                .setWascId(wascId)
                .setMessage(msg)
                .raise();
    }

    /**
     * Tells whether or not the file exists, based on {@code CustomPage} definition or previous
     * analysis. Falls back to use {@code Analyser} which analyzes specific behavior and status
     * codes.
     *
     * @param msg the message that will be checked
     * @return {@code true} if the file exists, {@code false} otherwise
     */
    protected boolean isFileExist(HttpMessage msg) {
        return isPage200(msg);
    }

    /**
     * Tells whether or not the message matches the specific {@code CustomPage.Type}.
     *
     * @param msg the message that will be checked
     * @param cpType the custom page type to be checked
     * @return {@code true} if the message matches, {@code false} otherwise
     * @since 2.10.0
     */
    private boolean isCustomPage(HttpMessage msg, CustomPage.Type cpType) {
        return parent.isCustomPage(msg, cpType);
    }

    /**
     * Tells whether or not the message matches {@code CustomPage.Type.OK_200} definitions. Falls
     * back to use {@code Analyser} which analyzes specific behavior and status codes. Checks if the
     * message matches {@code CustomPage.Type.ERROR_500} or {@code CusotmPage.Type.NOTFOUND_404}
     * first, in case the user is trying to override something.
     *
     * @param msg the message that will be checked
     * @return {@code true} if the message matches, {@code false} otherwise
     * @since 2.10.0
     */
    protected boolean isPage200(HttpMessage msg) {
        if (isCustomPage(msg, CustomPage.Type.NOTFOUND_404)
                || isCustomPage(msg, CustomPage.Type.ERROR_500)) {
            return false;
        }
        if (isCustomPage(msg, CustomPage.Type.OK_200)) {
            return true;
        }
        return parent.getAnalyser().isFileExist(msg);
    }

    /**
     * Tells whether or not the message matches {@code CustomPage.Type.ERROR_500} definitions. Falls
     * back to simply checking the response status code for "500 - Internal Server Error". Checks if
     * the message matches {@code CustomPage.Type.OK_200} or {@code CusotmPage.Type.NOTFOUND_404}
     * first, in case the user is trying to override something.
     *
     * @param msg the message that will be checked
     * @return {@code true} if the message matches, {@code false} otherwise
     * @since 2.10.0
     */
    protected boolean isPage500(HttpMessage msg) {
        if (isCustomPage(msg, CustomPage.Type.OK_200)
                || isCustomPage(msg, CustomPage.Type.NOTFOUND_404)) {
            return false;
        }
        if (isCustomPage(msg, CustomPage.Type.ERROR_500)) {
            return true;
        }
        return msg.getResponseHeader().getStatusCode() == HttpStatusCode.INTERNAL_SERVER_ERROR;
    }

    /**
     * Tells whether or not the message matches a {@code CustomPage.Type.NOTFOUND_404} definition.
     * Falls back to {@code Analyser}. Checks if the message matches {@code CustomPage.Type.OK_200}
     * or {@code CustomPage.Type.ERROR_500} first, in case the user is trying to override something.
     *
     * @param msg the message that will be checked
     * @return {@code true} if the message matches, {@code false} otherwise
     * @since 2.10.0
     */
    protected boolean isPage404(HttpMessage msg) {
        if (isCustomPage(msg, CustomPage.Type.OK_200)
                || isCustomPage(msg, CustomPage.Type.ERROR_500)) {
            return false;
        }
        if (isCustomPage(msg, CustomPage.Type.NOTFOUND_404)) {
            return true;
        }
        return !parent.getAnalyser().isFileExist(msg);
    }

    /**
     * Tells whether or not the message matches {@code CustomPage.Type.OTHER} definitions.
     *
     * @param msg the message that will be checked
     * @return {@code true} if the message matches, {@code false} otherwise
     * @since 2.10.0
     */
    protected boolean isPageOther(HttpMessage msg) {
        return isCustomPage(msg, CustomPage.Type.OTHER);
    }

    /**
     * Tells whether or not the message matches {@code CustomPage.Type.AUTH_4XX} definitions. Checks
     * if the message matches {@code CustomPage.Type.OK_200} first, in case the user is trying to
     * override something.
     *
     * @param msg the message that will be checked
     * @return {@code true} if the message matches, {@code false} otherwise
     * @since 2.12.0
     */
    protected boolean isPageAuthIssue(HttpMessage msg) {
        if (isCustomPage(msg, CustomPage.Type.OK_200)) {
            return false;
        }
        return isCustomPage(msg, CustomPage.Type.AUTH_4XX);
    }

    /**
     * Tells whether or not the response has a status code between 200 and 299 (inclusive), or
     * {@code CustomPage.Type.OK_200} and {@code Analyser#isFileExist(HttpMessage)}. Checks if the
     * message matches {@code CustomPage.Type.NOTFOUND_404} or {@code CustomPage.Type.ERROR_500}
     * first, in case the user is trying to override something.
     *
     * @param msg the message that will be checked
     * @return {@code true} if the message matches, {@code false} otherwise
     * @since 2.10.0
     * @see Analyser#isFileExist(HttpMessage)
     */
    public boolean isSuccess(HttpMessage msg) {
        if (isCustomPage(msg, CustomPage.Type.NOTFOUND_404)
                || isCustomPage(msg, CustomPage.Type.ERROR_500)) {
            return false;
        }
        if (isCustomPage(msg, CustomPage.Type.OK_200) || parent.getAnalyser().isFileExist(msg)) {
            return true;
        }
        return HttpStatusCode.isSuccess(msg.getResponseHeader().getStatusCode());
    }

    /**
     * Tells whether or not the response has a status code between 400 and 499 (inclusive), or
     * {@code CustomPage.Type.NOTFOUND_404} and {@code Analyser#isFileExist(HttpMessage)}. Checks if
     * the message matches {@code CustomPage.Type.OK_200} or {@code CustomPage.Type.ERROR_500}
     * first, in case the user is trying to override something.
     *
     * @param msg the message that will be checked
     * @return {@code true} if the message matches, {@code false} otherwise
     * @since 2.10.0
     * @see Analyser#isFileExist(HttpMessage)
     */
    public boolean isClientError(HttpMessage msg) {
        if (isCustomPage(msg, CustomPage.Type.OK_200)
                || isCustomPage(msg, CustomPage.Type.ERROR_500)) {
            return false;
        }
        if (isCustomPage(msg, CustomPage.Type.NOTFOUND_404)
                || !parent.getAnalyser().isFileExist(msg)) {
            return true;
        }
        return HttpStatusCode.isClientError(msg.getResponseHeader().getStatusCode());
    }

    /**
     * Tells whether or not the response has a status code between 500 and 599 (inclusive), or
     * {@code CustomPage.Type.EROOR_500}. Checks if the message matches {@code
     * CustomPage.Type.OK_200} or {@code CustomPage.Type.NOTFOUND_404} first, in case the user is
     * trying to override something.
     *
     * @param msg the message that will be checked
     * @return {@code true} if the message matches, {@code false} otherwise
     * @since 2.10.0
     */
    public boolean isServerError(HttpMessage msg) {
        if (isCustomPage(msg, CustomPage.Type.OK_200)
                || isCustomPage(msg, CustomPage.Type.NOTFOUND_404)) {
            return false;
        }
        if (isCustomPage(msg, CustomPage.Type.ERROR_500)) {
            return true;
        }
        return HttpStatusCode.isServerError(msg.getResponseHeader().getStatusCode());
    }

    /**
     * Check if this test should be stopped. It should be checked periodically in Plugin (e.g. when
     * in loops) so the HostProcess can stop this Plugin cleanly.
     *
     * @return {@code true} if the scanner should stop, {@code false} otherwise
     */
    protected boolean isStop() {
        // ZAP: added skipping controls
        return parent.isStop() || parent.isSkipped(this);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    /** Enable this test */
    @Override
    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            setProperty("enabled", Boolean.toString(enabled));
            if (enabled && getAlertThreshold() == AlertThreshold.OFF) {
                setAlertThreshold(AlertThreshold.DEFAULT);
            }
        }
    }

    @Override
    public AlertThreshold getAlertThreshold() {
        return this.getAlertThreshold(false);
    }

    @Override
    public AlertThreshold getAlertThreshold(boolean incDefault) {
        AlertThreshold level = null;
        try {
            level = AlertThreshold.valueOf(getProperty("level"));
            // log.debug("getAlertThreshold from configs: " + level.name());

        } catch (Exception e) {
            // Ignore
        }

        if (level == null) {
            if (this.isEnabled()) {
                if (incDefault) {
                    level = AlertThreshold.DEFAULT;

                } else {
                    level = defaultAttackThreshold;
                }

                // log.debug("getAlertThreshold default: " + level.name());

            } else {
                level = AlertThreshold.OFF;
                // log.debug("getAlertThreshold not enabled: " + level.name());
            }

        } else if (level.equals(AlertThreshold.DEFAULT)) {
            if (incDefault) {
                level = AlertThreshold.DEFAULT;

            } else {
                level = defaultAttackThreshold;
            }

            // log.debug("getAlertThreshold default: " + level.name());
        }

        return level;
    }

    @Override
    public void setAlertThreshold(AlertThreshold level) {
        setProperty("level", level.name());
        setEnabledFromLevel();
    }

    @Override
    public void setDefaultAlertThreshold(AlertThreshold level) {
        AlertThreshold oldDefaultAlertThreshold = defaultAttackThreshold;
        this.defaultAttackThreshold = level;
        if ((defaultAttackThreshold == AlertThreshold.OFF
                        || oldDefaultAlertThreshold == AlertThreshold.OFF)
                && getAlertThreshold(true) == AlertThreshold.DEFAULT) {
            setEnabled(defaultAttackThreshold != AlertThreshold.OFF);
        }
    }

    private void setEnabledFromLevel() {
        AlertThreshold level = getAlertThreshold(true);
        setEnabled(
                level != AlertThreshold.OFF
                        && !(level == AlertThreshold.DEFAULT
                                && this.defaultAttackThreshold == AlertThreshold.OFF));
    }

    /** Override this if you plugin supports other levels. */
    @Override
    public AlertThreshold[] getAlertThresholdsSupported() {
        return alertThresholdsSupported;
    }

    @Override
    public AttackStrength getAttackStrength(boolean incDefault) {
        AttackStrength level = null;
        try {
            level = AttackStrength.valueOf(getProperty("strength"));
            // log.debug("getAttackStrength from configs: " + level.name());

        } catch (Exception e) {
            // Ignore
        }

        if (level == null) {
            if (incDefault) {
                level = AttackStrength.DEFAULT;

            } else {
                level = this.defaultAttackStrength;
            }

            // log.debug("getAttackStrength default: " + level.name());

        } else if (level.equals(AttackStrength.DEFAULT)) {
            if (incDefault) {
                level = AttackStrength.DEFAULT;

            } else {
                level = this.defaultAttackStrength;
            }

            // log.debug("getAttackStrength default: " + level.name());
        }

        return level;
    }

    @Override
    public AttackStrength getAttackStrength() {
        return this.getAttackStrength(false);
    }

    @Override
    public void setAttackStrength(AttackStrength level) {
        setProperty("strength", level.name());
    }

    @Override
    public void setDefaultAttackStrength(AttackStrength strength) {
        this.defaultAttackStrength = strength;
    }

    /** Override this if you plugin supports other levels. */
    @Override
    public AttackStrength[] getAttackStrengthsSupported() {
        return attackStrengthsSupported;
    }

    /** Compare if 2 plugin is the same. */
    @Override
    public int compareTo(Object obj) {
        int result = -1;
        if (obj instanceof AbstractPlugin) {
            AbstractPlugin test = (AbstractPlugin) obj;
            if (getId() < test.getId()) {
                result = -1;

            } else if (getId() > test.getId()) {
                result = 1;

            } else {
                result = 0;
            }
        }

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (compareTo(obj) == 0) {
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId());
    }

    /**
     * Check if the given pattern can be found in the header.
     *
     * @param msg the message that will be checked
     * @param header the name of the header
     * @param pattern the pattern that will be used
     * @return true if the pattern can be found.
     */
    protected boolean matchHeaderPattern(HttpMessage msg, String header, Pattern pattern) {
        if (msg.getResponseHeader().isEmpty()) {
            return false;
        }

        String val = msg.getResponseHeader().getHeader(header);
        if (val == null) {
            return false;
        }

        Matcher matcher = pattern.matcher(val);
        return matcher.find();
    }

    /**
     * Check if the given pattern can be found in the msg body. If the supplied StringBuilder is not
     * null, append the result to the StringBuilder.
     *
     * @param msg the message that will be checked
     * @param pattern the pattern that will be used
     * @param sb where the regex match should be appended
     * @return true if the pattern can be found.
     */
    protected boolean matchBodyPattern(
            HttpMessage msg,
            Pattern pattern,
            StringBuilder sb) { // ZAP: Changed the type of the parameter "sb" to StringBuilder.
        Matcher matcher = pattern.matcher(msg.getResponseBody().toString());
        boolean result = matcher.find();
        if (result) {
            if (sb != null) {
                sb.append(matcher.group());
            }
        }
        return result;
    }

    /**
     * Write a progress update message. Currently this just display in System.out
     *
     * @param msg the progress message
     */
    protected void writeProgress(String msg) {
        // System.out.println(msg);
    }

    /**
     * Get the parent HostProcess.
     *
     * @return the parent HostProcess
     */
    // ZAP: Changed from protected to public access modifier.
    public HostProcess getParent() {
        return parent;
    }

    /**
     * Replace body by stripping of pattern string. The URLencoded and URLdecoded pattern will also
     * be stripped off. This is mainly used for stripping off a testing string in HTTP response for
     * comparison against the original response. Reference: TestInjectionSQL
     *
     * @param body the body that will be used
     * @param pattern the pattern used for the removals
     * @return the body without the pattern
     */
    protected String stripOff(String body, String pattern) {
        String urlEncodePattern = getURLEncode(pattern);
        String urlDecodePattern = getURLDecode(pattern);
        String htmlEncodePattern1 = getHTMLEncode(pattern);
        String htmlEncodePattern2 = getHTMLEncode(urlEncodePattern);
        String htmlEncodePattern3 = getHTMLEncode(urlDecodePattern);
        String result =
                body.replaceAll("\\Q" + pattern + "\\E", "")
                        .replaceAll("\\Q" + urlEncodePattern + "\\E", "")
                        .replaceAll("\\Q" + urlDecodePattern + "\\E", "");
        result =
                result.replaceAll("\\Q" + htmlEncodePattern1 + "\\E", "")
                        .replaceAll("\\Q" + htmlEncodePattern2 + "\\E", "")
                        .replaceAll("\\Q" + htmlEncodePattern3 + "\\E", "");
        return result;
    }

    public static String getURLEncode(String msg) {
        String result = "";
        try {
            result = URLEncoder.encode(msg, "UTF8");

        } catch (UnsupportedEncodingException ignore) {
            // Shouldn't happen UTF-8 is a standard Charset (see java.nio.charset.StandardCharsets)
        }

        return result;
    }

    public static String getURLDecode(String msg) {
        String result = "";
        try {
            result = URLDecoder.decode(msg, "UTF8");

        } catch (UnsupportedEncodingException ignore) {
            // Shouldn't happen UTF-8 is a standard Charset (see java.nio.charset.StandardCharsets)
        }

        return result;
    }

    public static String getHTMLEncode(String msg) {
        String result = msg.replaceAll("<", "&#60;");
        result = result.replaceAll(">", "&#62;");
        return result;
    }

    protected Kb getKb() {
        return getParent().getKb();
    }

    /**
     * Gets the logger.
     *
     * @return the logger, never {@code null}.
     * @deprecated (2.10.0) Use {@link #getLogger()} instead.
     */
    @Deprecated
    protected org.apache.log4j.Logger getLog() {
        return org.apache.log4j.Logger.getLogger(getClass());
    }

    /**
     * Gets the logger.
     *
     * @return the logger, never {@code null}.
     * @since 2.10.0
     */
    protected Logger getLogger() {
        return logger;
    }

    public String getProperty(String key) {
        return this.getProperty(config, key);
    }

    private String getProperty(Configuration conf, String key) {
        return conf.getString("plugins." + "p" + getId() + "." + key);
    }

    public void setProperty(String key, String value) {
        this.setProperty(config, key, value);
    }

    private void setProperty(Configuration conf, String key, String value) {
        conf.setProperty("plugins." + "p" + getId() + "." + key, value);
    }

    @Override
    public void setConfig(Configuration config) {
        this.config = config;
    }

    @Override
    public Configuration getConfig() {
        return config;
    }

    @Override
    public void saveTo(Configuration conf) {
        setProperty(conf, "enabled", Boolean.toString(enabled));
        setProperty(conf, "level", getProperty("level"));
        setProperty(conf, "strength", getProperty("strength"));
    }

    @Override
    public void loadFrom(Configuration conf) {
        setProperty("level", getProperty(conf, "level"));
        setProperty("strength", getProperty(conf, "strength"));
        String enabledProperty = getProperty(conf, "enabled");
        if (enabledProperty != null) {
            enabled = Boolean.parseBoolean(enabledProperty);
        } else {
            enabled = getAlertThreshold() != AlertThreshold.OFF;
            enabledProperty = Boolean.toString(enabled);
        }
        setProperty("enabled", enabledProperty);
    }

    @Override
    public void cloneInto(Plugin plugin) {
        if (plugin instanceof AbstractPlugin) {
            AbstractPlugin ap = (AbstractPlugin) plugin;
            ap.setAlertThreshold(this.getAlertThreshold(true));
            ap.setEnabled(this.isEnabled());
            ap.setAttackStrength(this.getAttackStrength(true));
            ap.setDefaultAlertThreshold(this.defaultAttackThreshold);
            ap.setDefaultAttackStrength(this.defaultAttackStrength);
            ap.setTechSet(this.getTechSet());
            ap.setStatus(this.getStatus());
            ap.saveTo(plugin.getConfig());
        } else {
            throw new InvalidParameterException("Not an AbstractPlugin");
        }
    }

    /** Check and create necessary parameter in config file if not already present. */
    @Override
    public void createParamIfNotExist() {
        if (getProperty("enabled") == null) {
            setEnabled(getAlertThreshold() != AlertThreshold.OFF);
        }
    }

    // ZAP Added isDepreciated
    @Override
    public boolean isDepreciated() {
        return false;
    }

    /** @since 2.2.0 */
    @Override
    public int getRisk() {
        return Alert.RISK_MEDIUM;
    }

    @Override
    public int getDelayInMs() {
        return delayInMs;
    }

    @Override
    public void setDelayInMs(int delayInMs) {
        this.delayInMs = delayInMs;
    }

    /** @see #isAnyInScope(Tech...) */
    @Override
    public boolean inScope(Tech tech) {
        return this.techSet.includes(tech);
    }

    /**
     * Tells whether or not any of the given technologies is enabled for the scan.
     *
     * <p>Helper method to check if any of the related technologies is enabled before performing a
     * test/scan. For example:
     *
     * <pre>{@code
     * if (isAnyInScope(Tech.Linux, Tech.MacOS)) {
     *     // Perform nix test...
     * }
     * }</pre>
     *
     * @param techs the technologies that will be checked.
     * @return {@code true} if any of the technologies is enabled for the scan, {@code false}
     *     otherwise.
     * @since 2.8.0
     * @see #inScope(Tech)
     * @see #targets(TechSet)
     */
    protected boolean isAnyInScope(Tech... techs) {
        return this.techSet.includesAny(techs);
    }

    @Override
    public void setTechSet(TechSet ts) {
        if (ts == null) {
            throw new IllegalArgumentException("Parameter ts must not be null.");
        }
        this.techSet = ts;
    }

    /**
     * Returns the technologies enabled for the scan.
     *
     * @return a {@code TechSet} with the technologies enabled for the scan, never {@code null}
     *     (since 2.6.0).
     * @since 2.4.0
     * @see #inScope(Tech)
     * @see #targets(TechSet)
     */
    public TechSet getTechSet() {
        return this.techSet;
    }

    /**
     * Returns {@code true} by default.
     *
     * @since 2.4.1
     * @see #getTechSet()
     */
    @Override
    public boolean targets(TechSet technologies) {
        return true;
    }

    @Override
    public Date getTimeStarted() {
        return this.started;
    }

    @Override
    public Date getTimeFinished() {
        return this.finished;
    }

    @Override
    public void setTimeStarted() {
        this.started = new Date();
        this.finished = null;
    }

    @Override
    public void setTimeFinished() {
        this.finished = new Date();
    }

    @Override
    public int getCweId() {
        // Default 'unknown' value
        return 0;
    }

    @Override
    public int getWascId() {
        // Default 'unknown' value
        return 0;
    }

    /**
     * Gets the tags attached to the alerts raised by this plugin. Can be overridden by scan rules
     * to return the associated alert tags.
     *
     * @return the Alert Tags
     * @since 2.11.0
     */
    @Override
    public Map<String, String> getAlertTags() {
        return null;
    }

    @Override
    public AddOn.Status getStatus() {
        return status;
    }

    public void setStatus(AddOn.Status status) {
        this.status = status;
    }

    /**
     * Gets the name of the scan rule, falling back to the simple name of the class as last resort.
     *
     * @return a name representing the scan rule.
     * @since 2.12.0
     */
    @Override
    public final String getDisplayName() {
        return StringUtils.isBlank(this.getName())
                ? this.getClass().getSimpleName()
                : this.getName();
    }

    /**
     * Returns a new alert builder.
     *
     * <p>By default the alert builder sets the following fields of the alert:
     *
     * <ul>
     *   <li>Plugin ID - using {@link #getId()}
     *   <li>Name - using {@link #getName()}
     *   <li>Risk - using {@link #getRisk()}
     *   <li>Description - using {@link #getDescription()}
     *   <li>Solution - using {@link #getSolution()}
     *   <li>Reference - using {@link #getReference()}
     *   <li>CWE ID - using {@link #getCweId()}
     *   <li>WASC ID - using {@link #getWascId()}
     *   <li>URI - from the alert message
     *   <li>Alert Tags - using {@link #getAlertTags()}
     * </ul>
     *
     * @return the alert builder.
     * @since 2.9.0
     */
    protected AlertBuilder newAlert() {
        return new AlertBuilder(this);
    }

    /**
     * An alert builder to fluently build and {@link #raise() raise alerts}.
     *
     * @since 2.9.0
     */
    public static final class AlertBuilder extends Alert.Builder {

        private final AbstractPlugin plugin;
        private boolean messageSet;

        private AlertBuilder(AbstractPlugin plugin) {
            this.plugin = plugin;

            setPluginId(plugin.getId());
            setName(plugin.getName());
            setRisk(plugin.getRisk());
            setDescription(plugin.getDescription());
            setSolution(plugin.getSolution());
            setReference(plugin.getReference());
            setCweId(plugin.getCweId());
            setWascId(plugin.getWascId());
            setTags(plugin.getAlertTags());
        }

        @Override
        public AlertBuilder setAlertId(int alertId) {
            super.setAlertId(alertId);
            return this;
        }

        @Override
        public AlertBuilder setPluginId(int pluginId) {
            super.setPluginId(pluginId);
            return this;
        }

        @Override
        public AlertBuilder setName(String name) {
            super.setName(name);
            return this;
        }

        @Override
        public AlertBuilder setRisk(int risk) {
            super.setRisk(risk);
            return this;
        }

        @Override
        public AlertBuilder setConfidence(int confidence) {
            super.setConfidence(confidence);
            return this;
        }

        @Override
        public AlertBuilder setDescription(String description) {
            super.setDescription(description);
            return this;
        }

        @Override
        public AlertBuilder setUri(String uri) {
            super.setUri(uri);
            return this;
        }

        @Override
        public AlertBuilder setParam(String param) {
            super.setParam(param);
            return this;
        }

        @Override
        public AlertBuilder setAttack(String attack) {
            super.setAttack(attack);
            return this;
        }

        @Override
        public AlertBuilder setOtherInfo(String otherInfo) {
            super.setOtherInfo(otherInfo);
            return this;
        }

        @Override
        public AlertBuilder setSolution(String solution) {
            super.setSolution(solution);
            return this;
        }

        @Override
        public AlertBuilder setReference(String reference) {
            super.setReference(reference);
            return this;
        }

        @Override
        public AlertBuilder setEvidence(String evidence) {
            super.setEvidence(evidence);
            return this;
        }

        @Override
        public AlertBuilder setInputVector(String inputVector) {
            super.setInputVector(inputVector);
            return this;
        }

        @Override
        public AlertBuilder setCweId(int cweId) {
            super.setCweId(cweId);
            return this;
        }

        @Override
        public AlertBuilder setWascId(int wascId) {
            super.setWascId(wascId);
            return this;
        }

        @Override
        public AlertBuilder setMessage(HttpMessage message) {
            super.setMessage(message);
            messageSet = message != null;
            return this;
        }

        @Override
        public AlertBuilder setSourceHistoryId(int sourceHistoryId) {
            super.setSourceHistoryId(sourceHistoryId);
            return this;
        }

        @Override
        public AlertBuilder setHistoryRef(HistoryReference historyRef) {
            super.setHistoryRef(historyRef);
            return this;
        }

        @Override
        public AlertBuilder setSource(Source source) {
            super.setSource(source);
            return this;
        }

        @Override
        public AlertBuilder setAlertRef(String alertRef) {
            super.setAlertRef(alertRef);
            return this;
        }

        @Override
        public AlertBuilder setTags(Map<String, String> tags) {
            super.setTags(tags);
            return this;
        }

        @Override
        public AlertBuilder addTag(String tag) {
            super.addTag(tag);
            return this;
        }

        @Override
        public AlertBuilder addTag(String tag, String value) {
            super.addTag(tag, value);
            return this;
        }

        @Override
        public AlertBuilder removeTag(String tag) {
            super.removeTag(tag);
            return this;
        }

        @Override
        public AlertBuilder removeTag(String tag, String value) {
            super.removeTag(tag, value);
            return this;
        }

        /**
         * Raises the alert with specified data.
         *
         * @throws IllegalStateException if the HTTP message was not set.
         */
        public void raise() {
            if (!messageSet) {
                throw new IllegalStateException(
                        "A HTTP message must be set before raising the alert.");
            }

            Alert alert = build();
            plugin.logger.debug(
                    "New alert pluginid={} {} uri={}",
                    alert.getPluginId(),
                    alert.getName(),
                    alert.getUri());
            plugin.parent.alertFound(alert);
        }
    }
}
