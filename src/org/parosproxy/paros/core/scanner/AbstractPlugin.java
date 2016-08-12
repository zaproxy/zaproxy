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
// ZAP: 2013/11/16 Issue 842: NullPointerException while active scanning with ExtensionAntiCSRF disabled
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

package org.parosproxy.paros.core.scanner;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.InvalidParameterException;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.URI;
import org.apache.log4j.Logger;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.encoder.Encoder;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpSender;
import org.zaproxy.zap.control.AddOn;
import org.zaproxy.zap.extension.anticsrf.AntiCsrfToken;
import org.zaproxy.zap.extension.anticsrf.ExtensionAntiCSRF;
import org.zaproxy.zap.model.Tech;
import org.zaproxy.zap.model.TechSet;

public abstract class AbstractPlugin implements Plugin, Comparable<Object> {

    private static final String[] NO_DEPENDENCIES = {};

    /**
     * Default pattern used in pattern check for most plugins.
     */
    protected static final int PATTERN_PARAM = Pattern.CASE_INSENSITIVE | Pattern.MULTILINE;
    /**
     * CRLF string.
     */
    protected static final String CRLF = "\r\n";
    private HostProcess parent = null;
    private HttpMessage msg = null;
    private boolean enabled = true;
    private Logger log = Logger.getLogger(this.getClass());
    private Configuration config = null;
    // ZAP Added delayInMs
    private int delayInMs;
    private ExtensionAntiCSRF extAntiCSRF = null;
    private Encoder encoder = new Encoder();
    private AlertThreshold defaultAttackThreshold = AlertThreshold.MEDIUM;
    private static final AlertThreshold[] alertThresholdsSupported = new AlertThreshold[]{AlertThreshold.MEDIUM};
    private AttackStrength defaultAttackStrength = AttackStrength.MEDIUM;
    private static final AttackStrength[] attackStrengthsSupported = new AttackStrength[]{AttackStrength.MEDIUM};
    private TechSet techSet;
    private Date started = null;
    private Date finished = null;
    private AddOn.Status status = AddOn.Status.unknown;

    /**
     * The redirection validator that ensures the followed redirections are in scan's scope.
     * <p>
     * Lazily initialised.
     * 
     * @see #getRedirectionValidator()
     */
    private HttpSender.RedirectionValidator redirectionValidator;

    /**
     * Default Constructor
     */
    public AbstractPlugin() {
        this.techSet = TechSet.AllTech;
    }

    @Override
    public abstract int getId();

    @Override
    public abstract String getName();

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
    public abstract String getDescription();

    @Override
    public abstract int getCategory();

    @Override
    public abstract String getSolution();

    @Override
    public abstract String getReference();

    @Override
    public void init(HttpMessage msg, HostProcess parent) {
        this.msg = msg.cloneAll();
        this.parent = parent;
        if (this.parent.getScannerParam().isInjectPluginIdInHeader()) {
    		this.msg.getRequestHeader().setHeader(HttpHeader.X_ZAP_SCAN_ID, Integer.toString(getId()));
    	}
        init();
    }

    /**
     * Finishes the initialisation of the plugin, subclasses should add any initialisation logic/code to this method.
     * <p>
     * Called after the plugin has been initialised with the message being scanned. By default it does nothing.
     * <p>
     * Since 2.5.0 it is no longer abstract.
     * 
     * @see #init(HttpMessage, HostProcess)
     */
    public void init() {
    }

    /**
     * Obtain a new HttpMessage with the same request as the base. The response
     * is empty. This is used by plugin to build/craft a new message to
     * send/receive. It does not affect the base message.
     *
     * @return A new HttpMessage with cloned request. Response is empty.
     */
    protected HttpMessage getNewMsg() {
        return msg.cloneRequest();
    }

    /**
     * Get the base reference HttpMessage for this check. Both request and
     * response is present. It should not be modified during when the plugin
     * runs.
     *
     * @return The base HttpMessage with request/response.
     */
    protected HttpMessage getBaseMsg() {
        return msg;
    }

    /**
     * Sends and receives the given {@code message}, always following redirections.
     * <p>
     * The following changes are made to the request before being sent:
     * <ul>
     * <li>The anti-CSRF token contained in the message will be handled/regenerated, if any;</li>
     * <li>The request headers {@link HttpHeader#IF_MODIFIED_SINCE} and {@link HttpHeader#IF_NONE_MATCH} are removed, to always
     * obtain a fresh response;</li>
     * <li>The header {@link HttpHeader#CONTENT_LENGTH} is updated, to match the length of the request body.</li>
     * <li>Changes done by {@link org.zaproxy.zap.network.HttpSenderListener HttpSenderListener} (for example, scripts).</li>
     * </ul>
     *
     * @param message the message to be sent and received
     * @throws HttpException if a HTTP error occurred
     * @throws IOException if an I/O error occurred (for example, read time out)
     * @see #sendAndReceive(HttpMessage, boolean)
     * @see #sendAndReceive(HttpMessage, boolean, boolean)
     */
    protected void sendAndReceive(HttpMessage message) throws IOException {
        sendAndReceive(message, true);
    }

    /**
     * Sends and receives the given {@code message}, optionally following redirections.
     * <p>
     * The following changes are made to the request before being sent:
     * <ul>
     * <li>The anti-CSRF token contained in the message will be handled/regenerated, if any;</li>
     * <li>The request headers {@link HttpHeader#IF_MODIFIED_SINCE} and {@link HttpHeader#IF_NONE_MATCH} are removed, to always
     * obtain a fresh response;</li>
     * <li>The header {@link HttpHeader#CONTENT_LENGTH} is updated, to match the length of the request body.</li>
     * <li>Changes done by {@link org.zaproxy.zap.network.HttpSenderListener HttpSenderListener} (for example, scripts).</li>
     * </ul>
     *
     * @param message the message to be sent and received
     * @param isFollowRedirect {@code true} if redirections should be followed, {@code false} otherwise
     * @throws HttpException if a HTTP error occurred
     * @throws IOException if an I/O error occurred (for example, read time out)
     * @see #sendAndReceive(HttpMessage)
     * @see #sendAndReceive(HttpMessage, boolean, boolean)
     */
    protected void sendAndReceive(HttpMessage message, boolean isFollowRedirect) throws IOException {
        sendAndReceive(message, isFollowRedirect, true);
    }

    /**
     * Sends and receives the given {@code message}, optionally following redirections and optionally regenerating anti-CSRF
     * token, if any.
     * <p>
     * The following changes are made to the request before being sent:
     * <ul>
     * <li>The request headers {@link HttpHeader#IF_MODIFIED_SINCE} and {@link HttpHeader#IF_NONE_MATCH} are removed, to always
     * obtain a fresh response;</li>
     * <li>The header {@link HttpHeader#CONTENT_LENGTH} is updated, to match the length of the request body.</li>
     * <li>Changes done by {@link org.zaproxy.zap.network.HttpSenderListener HttpSenderListener} (for example, scripts).</li>
     * </ul>
     *
     * @param message the message to be sent and received
     * @param isFollowRedirect {@code true} if redirections should be followed, {@code false} otherwise
     * @param handleAntiCSRF {@code true} if the anti-CSRF token present in the request should be handled/regenerated,
     *            {@code false} otherwise
     * @throws HttpException if a HTTP error occurred
     * @throws IOException if an I/O error occurred (for example, read time out)
     * @see #sendAndReceive(HttpMessage)
     * @see #sendAndReceive(HttpMessage, boolean)
     */
    protected void sendAndReceive(HttpMessage message, boolean isFollowRedirect, boolean handleAntiCSRF) throws IOException {

        if (parent.handleAntiCsrfTokens() && handleAntiCSRF) {
            if (extAntiCSRF == null) {
                extAntiCSRF = (ExtensionAntiCSRF) Control.getSingleton().getExtensionLoader().getExtension(ExtensionAntiCSRF.NAME);
            }
            if (extAntiCSRF != null) {
                List<AntiCsrfToken> tokens = extAntiCSRF.getTokens(message);
                AntiCsrfToken antiCsrfToken = null;
                if (tokens.size() > 0) {
                    antiCsrfToken = tokens.get(0);
                }

                if (antiCsrfToken != null) {
                    regenerateAntiCsrfToken(message, antiCsrfToken);
                }
            }
        }

        // always get the fresh copy
        message.getRequestHeader().setHeader(HttpHeader.IF_MODIFIED_SINCE, null);
        message.getRequestHeader().setHeader(HttpHeader.IF_NONE_MATCH, null);
        message.getRequestHeader().setContentLength(message.getRequestBody().length());

        if (this.getDelayInMs() > 0) {
            try {
                Thread.sleep(this.getDelayInMs());
            } catch (InterruptedException e) {
                // Ignore
            }
        }
        
        //ZAP: Runs the "beforeScan" methods of any ScannerHooks
        parent.performScannerHookBeforeScan(message, this);

        if (isFollowRedirect) {
            parent.getHttpSender().sendAndReceive(message, getRedirectionValidator());
        } else {
            parent.getHttpSender().sendAndReceive(message, false);
        }
        
        // ZAP: Notify parent
        parent.notifyNewMessage(this, message);
        
        //ZAP: Set the history reference back and run the "afterScan" methods of any ScannerHooks
        parent.performScannerHookAfterScan(message, this);
    }

    /**
     * Gets the redirection validator, that ensures the followed redirections are in scan's scope.
     *
     * @return scan's scope redirection validator, never {@code null}
     */
    private HttpSender.RedirectionValidator getRedirectionValidator() {
        if (redirectionValidator == null) {
            redirectionValidator = new HttpSender.RedirectionValidator() {

                @Override
                public boolean isValid(URI redirection) {
                    if (!getParent().nodeInScope(redirection.getEscapedURI())) {
                        if (log.isDebugEnabled()) {
                            log.debug("Skipping redirection out of scan's scope: " + redirection);
                        }
                        return false;
                    }
                    return true;
                }

                @Override
                public void notifyMessageReceived(HttpMessage message) {
                    // Nothing to do with the message.
                }
            };
        }
        return redirectionValidator;
    }

    private void regenerateAntiCsrfToken(HttpMessage msg, AntiCsrfToken antiCsrfToken) {
        if (antiCsrfToken == null) {
            return;
        }
        
        String tokenValue = null;
        try {
            HttpMessage tokenMsg = antiCsrfToken.getMsg().cloneAll();

            // Ensure we dont loop
            sendAndReceive(tokenMsg, true, false);

            tokenValue = extAntiCSRF.getTokenValue(tokenMsg, antiCsrfToken.getName());

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        
        if (tokenValue != null) {
            // Replace token value - only supported in the body right now
            log.debug("regenerateAntiCsrfToken replacing " + antiCsrfToken.getValue() + " with " + encoder.getURLEncode(tokenValue));
            String replaced = msg.getRequestBody().toString();
            replaced = replaced.replace(encoder.getURLEncode(antiCsrfToken.getValue()), encoder.getURLEncode(tokenValue));
            msg.setRequestBody(replaced);
            extAntiCSRF.registerAntiCsrfToken(new AntiCsrfToken(msg, antiCsrfToken.getName(), tokenValue, antiCsrfToken.getFormIndex()));
        }
    }

    @Override
    public void run() {
        // ZAP : set skipped to false otherwise the plugin shoud stop continously
        //this.skipped = false;
        
        try {
            if (!isStop()) {
                this.started = new Date();
                scan();
            }
            
        } catch (Exception e) {
            getLog().error(e.getMessage(), e);
        }
        
        notifyPluginCompleted(getParent());
        this.finished = new Date();
    }

    /**
     * The core scan method to be implemented by subclass.
     */
    @Override
    public abstract void scan();

    /**
     * Generate an alert when a security issue (risk/info) is found. Default
     * name, description, solution of this Plugin will be used.
     *
     * @param risk the risk of the new alert
     * @param confidence the confidence of the new alert
     * @param uri the affected URI
     * @param param the name/ID of the affected parameter
     * @param attack the attack that shows the issue
     * @param otherInfo other information about the issue
     * @param msg the message that shows the issue
     */
    protected void bingo(int risk, int confidence, String uri, String param, String attack, String otherInfo,
            HttpMessage msg) {
        
        bingo(risk, confidence, this.getName(), this.getDescription(), uri, param, attack, otherInfo, this.getSolution(),
                msg);
    }

    /**
     * Generate an alert when a security issue (risk/info) is found. Custom
     * alert name, description and solution will be used.
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
     */
    protected void bingo(int risk, int confidence, String name, String description, String uri,
            String param, String attack, String otherInfo, String solution,
            HttpMessage msg) {
        
        log.debug("New alert pluginid=" + +this.getId() + " " + name + " uri=" + uri);
        
        Alert alert = new Alert(this.getId(), risk, confidence, name);
        if (uri == null || uri.equals("")) {
            uri = msg.getRequestHeader().getURI().toString();
        }
        
        if (param == null) {
            param = "";
        }
        
        alert.setDetail(description, uri, param, attack, otherInfo, solution, this.getReference(),
                "", this.getCweId(), this.getWascId(), msg);
        
        parent.alertFound(alert);
    }

    /**
     * Generate an alert when a security issue (risk/info) is found. Default
     * name, description, solution of this Plugin will be used.
     *
     * @param risk the risk of the new alert
     * @param confidence the confidence of the new alert
     * @param uri the affected URI
     * @param param the name/ID of the affected parameter
     * @param attack the attack that shows the issue
     * @param otherInfo other information about the issue
     * @param evidence the evidence (in the response) that shows the issue
     * @param msg the message that shows the issue
     */
    protected void bingo(int risk, int confidence, String uri, String param, String attack, String otherInfo,
            String evidence, HttpMessage msg) {
        
        bingo(risk, confidence, this.getName(), this.getDescription(), uri, param, attack, otherInfo, this.getSolution(),
                evidence, msg);
    }

    /**
     * Generate an alert when a security issue (risk/info) is found. Custom
     * alert name, description and solution will be used.
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
     */
    protected void bingo(int risk, int confidence, String name, String description, String uri,
            String param, String attack, String otherInfo, String solution,
            String evidence, HttpMessage msg) {
        
        log.debug("New alert pluginid=" + +this.getId() + " " + name + " uri=" + uri);
        Alert alert = new Alert(this.getId(), risk, confidence, name);
        if (uri == null || uri.equals("")) {
            uri = msg.getRequestHeader().getURI().toString();
        }
        
        if (param == null) {
            param = "";
        }
        
        alert.setDetail(description, uri, param, attack, otherInfo, solution, this.getReference(),
                evidence, this.getCweId(), this.getWascId(), msg);
        
        parent.alertFound(alert);
    }

    protected void bingo(int risk, int confidence, String name, String description, String uri,
            String param, String attack, String otherInfo, String solution,
            String evidence, int cweId, int wascId, HttpMessage msg) {
       
        log.debug("New alert pluginid=" + +this.getId() + " " + name + " uri=" + uri);
        Alert alert = new Alert(this.getId(), risk, confidence, name);
        
        if (uri == null || uri.equals("")) {
            uri = msg.getRequestHeader().getURI().toString();
        }
        
        if (param == null) {
            param = "";
        }
        
        alert.setDetail(description, uri, param, attack, otherInfo, solution, this.getReference(),
                evidence, cweId, wascId, msg);
        
        parent.alertFound(alert);
    }

    /**
     * Tells whether or not the file exists, based on previous analysis.
     *
     * @param msg the message that will be checked
     * @return {@code true} if the file exists, {@code false} otherwise
     */
    protected boolean isFileExist(HttpMessage msg) {
        return parent.getAnalyser().isFileExist(msg);
    }

    /**
     * Check if this test should be stopped. It should be checked periodically
     * in Plugin (eg when in loops) so the HostProcess can stop this Plugin
     * cleanly.
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

    /**
     * Enable this test
     */
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
            //log.debug("getAlertThreshold from configs: " + level.name());
            
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
                
                //log.debug("getAlertThreshold default: " + level.name());
                
            } else {
                level = AlertThreshold.OFF;
                //log.debug("getAlertThreshold not enabled: " + level.name());
            }
            
        } else if (level.equals(AlertThreshold.DEFAULT)) {
            if (incDefault) {
                level = AlertThreshold.DEFAULT;
                
            } else {
                level = defaultAttackThreshold;
            }
            
            //log.debug("getAlertThreshold default: " + level.name());
        }
        
        return level;
    }

    @Override
    public void setAlertThreshold(AlertThreshold level) {
        setProperty("level", level.name());
        setEnabled(level != AlertThreshold.OFF);
    }

    @Override
    public void setDefaultAlertThreshold(AlertThreshold level) {
        this.defaultAttackThreshold = level;
    }

    /**
     * Override this if you plugin supports other levels.
     */
    @Override
    public AlertThreshold[] getAlertThresholdsSupported() {
        return alertThresholdsSupported;
    }

    @Override
    public AttackStrength getAttackStrength(boolean incDefault) {
        AttackStrength level = null;
        try {
            level = AttackStrength.valueOf(getProperty("strength"));
            //log.debug("getAttackStrength from configs: " + level.name());
            
        } catch (Exception e) {
            // Ignore
        }
        
        if (level == null) {
            if (incDefault) {
                level = AttackStrength.DEFAULT;
            
            } else {
                level = this.defaultAttackStrength;
            }

            //log.debug("getAttackStrength default: " + level.name());
            
        } else if (level.equals(AttackStrength.DEFAULT)) {
            if (incDefault) {
                level = AttackStrength.DEFAULT;
            
            } else {
                level = this.defaultAttackStrength;
            }
            
            //log.debug("getAttackStrength default: " + level.name());
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

    /**
     * Override this if you plugin supports other levels.
     */
    @Override
    public AttackStrength[] getAttackStrengthsSupported() {
        return attackStrengthsSupported;
    }

    /**
     * Compare if 2 plugin is the same.
     */
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
     * Check if the given pattern can be found in the msg body. If the supplied
     * StringBuilder is not null, append the result to the StringBuilder.
     *
     * @param msg the message that will be checked
     * @param pattern the pattern that will be used
     * @param sb where the regex match should be appended
     * @return true if the pattern can be found.
     */
    protected boolean matchBodyPattern(HttpMessage msg, Pattern pattern, StringBuilder sb) { // ZAP: Changed the type of the parameter "sb" to StringBuilder.
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
     * Write a progress update message. Currently this just display in
     * System.out
     *
     * @param msg the progress message
     */
    protected void writeProgress(String msg) {
        //System.out.println(msg);
    }

    /**
     * Get the parent HostProcess.
     *
     * @return the parent HostProcess
     */
    //ZAP: Changed from protected to public access modifier.
    public HostProcess getParent() {
        return parent;
    }

    @Override
    public abstract void notifyPluginCompleted(HostProcess parent);

    /**
     * Replace body by stripping of pattern string. The URLencoded and
     * URLdecoded pattern will also be stripped off. This is mainly used for
     * stripping off a testing string in HTTP response for comparison against
     * the original response. Reference: TestInjectionSQL
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
        String result = body.replaceAll("\\Q" + pattern + "\\E", "").replaceAll("\\Q" + urlEncodePattern + "\\E", "").replaceAll("\\Q" + urlDecodePattern + "\\E", "");
        result = result.replaceAll("\\Q" + htmlEncodePattern1 + "\\E", "").replaceAll("\\Q" + htmlEncodePattern2 + "\\E", "").replaceAll("\\Q" + htmlEncodePattern3 + "\\E", "");
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

    protected Logger getLog() {
        return log;
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

    /**
     * Check and create necessary parameter in config file if not already
     * present.
     *
     */
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

    /**
     * @since 2.2.0
     */
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

    @Override
    public boolean inScope(Tech tech) {
        return this.techSet.includes(tech);
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
     * @return a {@code TechSet} with the technologies enabled for the scan, never {@code null} (since TODO add version).
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

	@Override
	public AddOn.Status getStatus() {
		return status;
	}

	public void setStatus(AddOn.Status status) {
		this.status = status;
	}
    
}
