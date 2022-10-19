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
// ZAP: 2011/07/23 Added TYPE_FUZZER
// ZAP: 2011/12/04 Support deleting alerts
// ZAP: 2012/03/15 Changed the method getDisplay to use the class StringBuilder
//      instead of StringBuffer.
// ZAP: 2012/04/23 Added @Override annotation to the appropriate method.
// ZAP: 2012/05/28 Added some JavaDoc
// ZAP: 2012/06/13 Optimized alerts related code
// ZAP: 2012/08/07 Deleted some not used Spider Related constants
// ZAP: 2012/10/08 Issue 391: Performance improvements
// ZAP: 2012/02/26 Cache the response body length as part of Issue 539
// ZAP: 2013/08/07 Added TYPE_AUTHENTICATION
// ZAP: 2013/11/16 Issue 869: Differentiate proxied requests from (ZAP) user requests
// ZAP: 2013/11/16 Issue 892: Cache of response body length in HistoryReference might not be correct
// ZAP: 2014/04/10 Changed to use HttpMessageCachedData and expose the cached data
// ZAP: 2014/04/10 Issue 1042: Having significant issues opening a previous session
// ZAP: 2014/05/23 Issue 1209: Reliability becomes Confidence and add levels
// ZAP: 2014/06/10 Added TYPE_ACCESS_CONTROL
// ZAP: 2014/06/16 Issue 990: Allow to delete alerts through the API
// ZAP: 2014/08/14 Issue 1311: Differentiate temporary internal messages from temporary scanner
// messages
// ZAP: 2014/12/11 Update the flag webSocketUpgrade sooner to avoid re-reading the message from
// database
// ZAP: 2015/02/09 Issue 1525: Introduce a database interface layer to allow for alternative
// implementations
// ZAP: 2016/04/12 Update the SiteNode when deleting alerts
// ZAP: 2016/05/27 Moved the temporary types to this class
// ZAP: 2016/05/30 Add new type for CONNECT requests received by the proxy
// ZAP: 2016/06/15 Add TYPE_SEQUENCE_TEMPORARY
// ZAP: 2016/06/20 Add TYPE_ZEST_SCRIPT and deprecate TYPE_RESERVED_11
// ZAP: 2016/08/30 Use a Set instead of a List for the alerts
// ZAP: 2017/02/07 Add TYPE_SPIDER_AJAX_TEMPORARY.
// ZAP: 2017/03/19 Add TYPE_SPIDER_TEMPORARY.
// ZAP: 2017/05/03 Notify tag changes.
// ZAP: 2017/05/17 Allow to obtain the tags of a message.
// ZAP: 2017/05/31 Add a multi-catch for a specific handler.
// ZAP: 2017/06/08 Allow to keep the HttpMessage in memory for immediate reuse.
// ZAP: 2017/06/13 Notify when a note is set.
// ZAP: 2017/07/04 Notify when a HistoryReference is deleted.
// ZAP: 2017/08/18 Add TYPE_FUZZER_TEMPORARY.
// ZAP: 2018/02/14 Remove unnecessary boxing / unboxing
// ZAP: 2019/01/09 Add TYPE_CALLBACK.
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2020/11/26 Use Log4j 2 classes for logging.
// ZAP: 2021/07/07 Add TYPE_OAST.
// ZAP: 2022/02/28 Remove code deprecated in 2.6.0
// ZAP: 2022/06/27 Add TYPE_PARAM_DIGGER.
// ZAP: 2022/09/21 Use format specifiers instead of concatenation when logging.
package org.parosproxy.paros.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import org.apache.commons.httpclient.URI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.RecordAlert;
import org.parosproxy.paros.db.RecordHistory;
import org.parosproxy.paros.db.RecordTag;
import org.parosproxy.paros.db.TableAlert;
import org.parosproxy.paros.db.TableHistory;
import org.parosproxy.paros.db.TableTag;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.ZAP;
import org.zaproxy.zap.eventBus.Event;
import org.zaproxy.zap.model.Target;

/**
 * This class abstracts a reference to a http message stored in database. It reads the whole http
 * message from database when getHttpMessage() is called.
 */
public class HistoryReference {

    /** Temporary type = not retrieved from history. To be deleted. */
    public static final int TYPE_TEMPORARY = 0;

    /**
     * @deprecated Use {@link #TYPE_PROXIED} instead.
     * @see #TYPE_ZAP_USER
     */
    @Deprecated public static final int TYPE_MANUAL = 1;

    /** A HTTP message that was proxied through ZAP. */
    public static final int TYPE_PROXIED = 1;

    /**
     * A HTTP message sent by the user from within ZAP, for example, using "Manual Request Editor"
     * or "Resend" dialogues.
     */
    public static final int TYPE_ZAP_USER = 15;

    public static final int TYPE_SPIDER = 2;
    public static final int TYPE_SCANNER = 3;
    public static final int TYPE_HIDDEN = 6;
    // ZAP: Added TYPE_BRUTE_FORCE
    public static final int TYPE_BRUTE_FORCE = 7;
    /**
     * A HTTP message sent by the fuzzer.
     *
     * <p>Used for fuzzer messages that have an {@link Alert}, which outlive the fuzzing sessions.
     *
     * @since 1.4.0
     * @see #TYPE_FUZZER_TEMPORARY
     */
    public static final int TYPE_FUZZER = 8;
    /**
     * A (temporary) HTTP message of the spider.
     *
     * <p>The type is used to off-load the messages (of resources found but not yet fetched) from
     * the memory.
     *
     * @since 2.0.0
     * @see #TYPE_SPIDER
     * @see #TYPE_SPIDER_TEMPORARY
     * @see #DEFAULT_TEMPORARY_HISTORY_TYPES
     */
    public static final int TYPE_SPIDER_TASK = 9;

    /**
     * A HTTP message sent by the AJAX Spider.
     *
     * @since 2.0.0
     * @see #TYPE_SPIDER_AJAX_TEMPORARY
     */
    public static final int TYPE_SPIDER_AJAX = 10;
    /**
     * A (temporary) HTTP message that (attempts to) authenticates a {@link
     * org.zaproxy.zap.users.User User}.
     *
     * @since 2.4.0
     * @see #DEFAULT_TEMPORARY_HISTORY_TYPES
     */
    public static final int TYPE_AUTHENTICATION = 11;
    // ZAP: Added TYPE_ACCESS_CONTROL for use in access control testing methods
    public static final int TYPE_ACCESS_CONTROL = 13;

    /**
     * A HTTP message sent by a Zest script.
     *
     * <p>Not all HTTP messages sent by Zest scripts will have this type, some might use the type(s)
     * of the underlying component (for example, Zest Active Rules will use the types of the active
     * scanner, {@link #TYPE_SCANNER_TEMPORARY} or {@link #TYPE_SCANNER}).
     *
     * @since 2.6.0
     */
    public static final int TYPE_ZEST_SCRIPT = 12;

    /**
     * A (temporary) HTTP message sent by the (active) scanner.
     *
     * @since 2.4.0
     * @see #DEFAULT_TEMPORARY_HISTORY_TYPES
     */
    public static final int TYPE_SCANNER_TEMPORARY = 14;

    /**
     * The {@code Set} of temporary history types.
     *
     * @see #addTemporaryType(int)
     * @see #getTemporaryTypes()
     * @see #DEFAULT_TEMPORARY_HISTORY_TYPES
     */
    private static final Set<Integer> TEMPORARY_HISTORY_TYPES = new HashSet<>();

    /**
     * The {@code Set} with default temporary history types:
     *
     * <ul>
     *   <li>{@link #TYPE_TEMPORARY};
     *   <li>{@link #TYPE_SCANNER_TEMPORARY};
     *   <li>{@link #TYPE_AUTHENTICATION};
     *   <li>{@link #TYPE_SPIDER_TASK};
     *   <li>{@link #TYPE_SEQUENCE_TEMPORARY};
     *   <li>{@link #TYPE_SPIDER_AJAX_TEMPORARY};
     *   <li>{@link #TYPE_SPIDER_TEMPORARY};
     *   <li>{@link #TYPE_FUZZER_TEMPORARY};
     * </ul>
     *
     * <p>Persisted messages with temporary types are deleted when the session is closed.
     *
     * <p><strong>Note:</strong> This set does not allow modifications, any attempt to modify it
     * will result in an {@code UnsupportedOperationException}.
     *
     * @since 2.5.0
     * @see #getTemporaryTypes()
     */
    public static final Set<Integer> DEFAULT_TEMPORARY_HISTORY_TYPES;

    /**
     * A HTTP CONNECT request received (and processed) by the local proxy.
     *
     * @since 2.5.0
     */
    public static final int TYPE_PROXY_CONNECT = 16;

    /**
     * A (temporary) HTTP message created/used when active scanning sequences.
     *
     * @since 2.6.0
     * @see #DEFAULT_TEMPORARY_HISTORY_TYPES
     */
    public static final int TYPE_SEQUENCE_TEMPORARY = 17;

    /**
     * A (temporary) HTTP message of the AJAX spider.
     *
     * <p>Normally a message that was not processed (i.e. not on spider scope).
     *
     * @since 2.6.0
     * @see #TYPE_SPIDER_AJAX
     * @see #DEFAULT_TEMPORARY_HISTORY_TYPES
     */
    public static final int TYPE_SPIDER_AJAX_TEMPORARY = 18;

    /**
     * A (temporary) HTTP message of the spider.
     *
     * <p>Normally a message that was not processed (i.e. not successfully sent to the server).
     *
     * @since 2.6.0
     * @see #TYPE_SPIDER
     * @see #TYPE_SPIDER_TASK
     * @see #DEFAULT_TEMPORARY_HISTORY_TYPES
     */
    public static final int TYPE_SPIDER_TEMPORARY = 19;

    /**
     * A (temporary) HTTP message sent by the fuzzer.
     *
     * @since 2.7.0
     * @see #TYPE_FUZZER
     * @see #DEFAULT_TEMPORARY_HISTORY_TYPES
     */
    public static final int TYPE_FUZZER_TEMPORARY = 20;

    /**
     * A HTTP message received by the callback handler.
     *
     * @since 2.8.0
     */
    public static final int TYPE_CALLBACK = 21;

    /**
     * An HTTP message received by an OAST server that is relayed to ZAP.
     *
     * @since 2.11.0
     */
    public static final int TYPE_OAST = 22;

    /**
     * An HTTP message sent by the param digger.
     *
     * @since 2.12.0
     */
    public static final int TYPE_PARAM_DIGGER = 23;

    private static java.text.DecimalFormat decimalFormat = new java.text.DecimalFormat("##0.###");
    private static TableHistory staticTableHistory = null;
    // ZAP: Support for multiple tags
    private static TableTag staticTableTag = null;
    // ZAP: Support for loading alerts from db
    private static TableAlert staticTableAlert = null;

    static {
        Set<Integer> defaultHistoryTypes = new HashSet<>();
        defaultHistoryTypes.add(HistoryReference.TYPE_TEMPORARY);
        defaultHistoryTypes.add(HistoryReference.TYPE_SCANNER_TEMPORARY);
        defaultHistoryTypes.add(HistoryReference.TYPE_AUTHENTICATION);
        defaultHistoryTypes.add(HistoryReference.TYPE_SPIDER_TASK);
        defaultHistoryTypes.add(HistoryReference.TYPE_SEQUENCE_TEMPORARY);
        defaultHistoryTypes.add(HistoryReference.TYPE_SPIDER_AJAX_TEMPORARY);
        defaultHistoryTypes.add(HistoryReference.TYPE_SPIDER_TEMPORARY);
        defaultHistoryTypes.add(HistoryReference.TYPE_FUZZER_TEMPORARY);
        DEFAULT_TEMPORARY_HISTORY_TYPES = Collections.unmodifiableSet(defaultHistoryTypes);

        TEMPORARY_HISTORY_TYPES.addAll(DEFAULT_TEMPORARY_HISTORY_TYPES);
    }

    private int historyId = 0;
    private int historyType = TYPE_PROXIED;
    private SiteNode siteNode = null;
    private String display = null;
    private long sessionId = 0;

    // ZAP: Support for specific icons
    private ArrayList<String> icons = null;
    private ArrayList<Boolean> clearIfManual = null;

    // ZAP: Support for linking Alerts to Hrefs
    private Set<Alert> alerts;

    private List<String> tags = new ArrayList<>();
    private boolean webSocketUpgrade;

    private static Logger log = LogManager.getLogger(HistoryReference.class);

    private HttpMessage httpMessage;
    private HttpMessageCachedData httpMessageCachedData;

    /** @return Returns the sessionId. */
    public long getSessionId() {
        return sessionId;
    }

    public HistoryReference(int historyId) throws HttpMalformedHeaderException, DatabaseException {
        this(historyId, false);
    }

    /**
     * Constructs a {@code HistoryReference} with the given ID and whether or not the {@code
     * HttpMessage} read from the database should be kept in memory.
     *
     * <p><strong>Note:</strong> This constructor should be used with care as the {@code
     * HttpMessage} might be kept in memory (until this instance is garbage collected or {@link
     * #clearHttpMessage() manually cleared}). It should be used only when the contents of the
     * {@code HttpMessage} are used immediately after creating the {@code HistoryReference},
     * avoiding reading the {@code HttpMessage} once again.
     *
     * @param historyId the ID of the message persisted to database
     * @param keepMessage {@code true} if the {@code HttpMessage} should be kept in memory, {@code
     *     false} otherwise.
     * @throws HttpMalformedHeaderException if an error occurred while parsing the message.
     * @throws DatabaseException if an error occurred while reading the message.
     * @since 2.7.0
     * @see #getHttpMessage()
     */
    public HistoryReference(int historyId, boolean keepMessage)
            throws HttpMalformedHeaderException, DatabaseException {
        RecordHistory history = null;
        this.icons = new ArrayList<>();
        this.clearIfManual = new ArrayList<>();
        history = staticTableHistory.read(historyId);
        if (history == null) {
            throw new HttpMalformedHeaderException();
        }
        HttpMessage msg = history.getHttpMessage();
        // ZAP: Support for multiple tags
        this.tags = getTags(historyId);

        build(history.getSessionId(), history.getHistoryId(), history.getHistoryType(), msg);

        if (keepMessage) {
            httpMessage = msg;
        }
    }

    public HistoryReference(Session session, int historyType, HttpMessage msg)
            throws HttpMalformedHeaderException, DatabaseException {

        RecordHistory history = null;
        this.icons = new ArrayList<>();
        this.clearIfManual = new ArrayList<>();
        history = staticTableHistory.write(session.getSessionId(), historyType, msg);
        build(session.getSessionId(), history.getHistoryId(), history.getHistoryType(), msg);
        // ZAP: Init HttpMessage HistoryReference field
        msg.setHistoryRef(this);
        this.tags = getTags(historyId);

        // ZAP: Support for loading the alerts from the db
        List<RecordAlert> alerts = staticTableAlert.getAlertsBySourceHistoryId(historyId);
        for (RecordAlert alert : alerts) {
            this.addAlert(new Alert(alert, this));
        }
    }

    /** @return whether the icon has to be cleaned when being manually visited or not. */
    public ArrayList<Boolean> getClearIfManual() {
        return this.clearIfManual;
    }

    /** @return The icon's string path (i.e. /resource/icon/16/xx.png) */
    public ArrayList<String> getCustomIcons() {
        return this.icons;
    }

    /**
     * @param i the icon's URL (i.e. /resource/icon/16/xx.png)
     * @param c if the icon has to be cleaned when the node is manually visited
     */
    public void setCustomIcon(String i, boolean c) {
        this.icons.add(i);
        this.clearIfManual.add(c);
    }

    private void build(long sessionId, int historyId, int historyType, HttpMessage msg) {
        this.sessionId = sessionId;
        this.historyId = historyId;
        this.historyType = historyType;
        this.webSocketUpgrade = msg.isWebSocketUpgrade();
        if (historyType == TYPE_PROXIED || historyType == TYPE_ZAP_USER) {
            this.display = getDisplay(msg);
        }
        // ZAP: Init HttpMessage HistoryReference field
        msg.setHistoryRef(this);

        // Cache info commonly used so that we dont need to keep reading the HttpMessage from the
        // db.
        httpMessageCachedData = new HttpMessageCachedData(msg);
    }

    public static void setTableHistory(TableHistory tableHistory) {
        staticTableHistory = tableHistory;
    }

    public static void setTableTag(TableTag tableTag) {
        staticTableTag = tableTag;
    }

    public static void setTableAlert(TableAlert tableAlert) {
        staticTableAlert = tableAlert;
    }
    /** @return Returns the historyId. */
    public int getHistoryId() {
        return historyId;
    }

    /**
     * Gets the corresponding http message from the database. Try to minimise calls to this method
     * as much as possible. But also dont cache the HttpMessage either as this can significantly
     * increase ZAP's memory usage.
     *
     * @return the http message
     * @throws HttpMalformedHeaderException the http malformed header exception
     * @throws DatabaseException if an error occurred while reading the HTTP message.
     */
    public HttpMessage getHttpMessage() throws HttpMalformedHeaderException, DatabaseException {
        if (httpMessage != null) {
            return httpMessage;
        }

        // fetch complete message
        RecordHistory history = staticTableHistory.read(historyId);
        if (history == null) {
            throw new HttpMalformedHeaderException(
                    "No history reference for id " + historyId + " type=" + historyType);
        }
        // ZAP: Init HttpMessage HistoryReference field
        history.getHttpMessage().setHistoryRef(this);
        return history.getHttpMessage();
    }

    /**
     * Clears the {@code HttpMessage} kept in memory.
     *
     * @since 2.7.0
     * @see #HistoryReference(int, boolean)
     */
    public void clearHttpMessage() {
        httpMessage = null;
    }

    public URI getURI() {
        return httpMessageCachedData.getUri();
    }

    @Override
    public String toString() {

        if (display != null) {
            return display;
        }

        HttpMessage msg = null;
        try {
            msg = getHttpMessage();
            display = getDisplay(msg);
        } catch (HttpMalformedHeaderException | DatabaseException e1) {
            display = "";
        }
        return display;
    }

    /** @return Returns the historyType. */
    public int getHistoryType() {
        return historyType;
    }

    /**
     * Delete this HistoryReference from database This should typically only be called via the
     * ExtensionHistory.delete(href) method
     */
    public void delete() {
        if (historyId > 0) {
            try {
                // ZAP: Support for multiple tags
                staticTableTag.deleteTagsForHistoryID(historyId);
                staticTableHistory.delete(historyId);
                notifyEvent(HistoryReferenceEventPublisher.EVENT_REMOVED);
            } catch (DatabaseException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    /** @return Returns the siteNode. */
    public SiteNode getSiteNode() {
        return siteNode;
    }
    /** @param siteNode The siteNode to set. */
    public void setSiteNode(SiteNode siteNode) {
        this.siteNode = siteNode;
    }

    private String getDisplay(HttpMessage msg) {
        StringBuilder sb = new StringBuilder(Integer.toString(historyId));
        sb.append(' ');
        sb.append(msg.getRequestHeader().getPrimeHeader());
        if (!msg.getResponseHeader().isEmpty()) {
            sb.append(" \t=> ").append(msg.getResponseHeader().getPrimeHeader());
            sb.append("\t [")
                    .append(decimalFormat.format(msg.getTimeElapsedMillis() / 1000.0))
                    .append(" s]");
        }

        return sb.toString();
    }

    // ZAP: Support for multiple tags
    public void addTag(String tag) {
        if (insertTagDb(tag)) {
            this.tags.add(tag);
            notifyEvent(HistoryReferenceEventPublisher.EVENT_TAG_ADDED);
        }
    }

    private boolean insertTagDb(String tag) {
        try {
            staticTableTag.insert(historyId, tag);
            return true;
        } catch (DatabaseException e) {
            log.error("Failed to persist tag: {}", e.getMessage(), e);
        }
        return false;
    }

    private void notifyEvent(String event) {
        Map<String, String> map = new HashMap<>();
        map.put(
                HistoryReferenceEventPublisher.FIELD_HISTORY_REFERENCE_ID,
                Integer.toString(historyId));
        ZAP.getEventBus()
                .publishSyncEvent(
                        HistoryReferenceEventPublisher.getPublisher(),
                        new Event(
                                HistoryReferenceEventPublisher.getPublisher(),
                                event,
                                new Target(getSiteNode()),
                                map));
    }

    public void deleteTag(String tag) {
        if (deleteTagDb(tag)) {
            this.tags.remove(tag);
            notifyEvent(HistoryReferenceEventPublisher.EVENT_TAG_REMOVED);
        }
    }

    private boolean deleteTagDb(String tag) {
        try {
            staticTableTag.delete(historyId, tag);
            return true;
        } catch (DatabaseException e) {
            log.error("Failed to delete tag: {}", e.getMessage(), e);
        }
        return false;
    }

    public List<String> getTags() {
        return this.tags;
    }

    // ZAP: Added setNote method to HistoryReference
    public void setNote(String note) {
        try {
            staticTableHistory.updateNote(historyId, note);
            httpMessageCachedData.setNote(note != null && note.length() > 0);
            notifyEvent(HistoryReferenceEventPublisher.EVENT_NOTE_SET);
        } catch (DatabaseException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void loadAlerts() {
        // ZAP: Support for loading the alerts from the db
        List<RecordAlert> alerts;
        try {
            alerts = staticTableAlert.getAlertsBySourceHistoryId(historyId);
            for (RecordAlert alert : alerts) {
                this.addAlert(new Alert(alert, this));
            }
        } catch (DatabaseException e) {
            log.error(e.getMessage(), e);
        }
    }

    public synchronized boolean addAlert(Alert alert) {
        // If this is the first alert
        if (alerts == null) {
            alerts = new HashSet<>();
        }

        boolean added = false;
        if (alerts.add(alert)) {
            alert.setHistoryRef(this);
            added = true;
        }
        // Try to add to the SiteHNode anyway - that will also check if its already added
        if (this.siteNode != null) {
            siteNode.addAlert(alert);
        }
        return added;
    }

    public synchronized void updateAlert(Alert alert) {
        // If there are no alerts yet
        if (alerts == null) {
            return;
        }

        for (Alert a : alerts) {
            if (a.getAlertId() == alert.getAlertId()) {
                // Have to use the alertId instead of 'equals' as any of the
                // other params might have changed
                this.alerts.remove(a);
                this.alerts.add(alert);
                if (this.siteNode != null) {
                    siteNode.updateAlert(alert);
                }
                return;
            }
        }
    }

    public synchronized void deleteAlert(Alert alert) {
        if (alerts != null) {
            alerts.remove(alert);
            if (siteNode != null) {
                siteNode.deleteAlert(alert);
            }
        }
    }

    public synchronized void deleteAllAlerts() {
        if (alerts != null) {
            alerts.clear();
        }
    }

    /**
     * Tells whether or not this history reference has the given alert.
     *
     * @param alert the alert to check
     * @return {@code true} if it has the given alert, {@code false} otherwise.
     * @since 2.6.0
     * @see #hasAlerts()
     * @see #addAlert(Alert)
     */
    public synchronized boolean hasAlert(Alert alert) {
        if (alerts == null) {
            return false;
        }
        return alerts.contains(alert);
    }

    /**
     * Tells whether or not this history reference has alerts.
     *
     * @return {@code true} if it has alerts, {@code false} otherwise.
     * @since 2.6.0
     * @see #hasAlert(Alert)
     * @see #addAlert(Alert)
     */
    public synchronized boolean hasAlerts() {
        if (alerts == null) {
            return false;
        }
        return !alerts.isEmpty();
    }

    public int getHighestAlert() {
        int i = -1;
        // If there are no alerts
        if (alerts == null) return i;
        for (Alert a : alerts) {
            if (a.getConfidence() != Alert.CONFIDENCE_FALSE_POSITIVE && a.getRisk() > i) {
                i = a.getRisk();
            }
        }

        return i;
    }

    /**
     * Gets the alerts.
     *
     * <p>If alerts where never added, an unmodifiable empty list is returned, otherwise it's
     * returned a copy of the internal collection.
     *
     * @return the alerts
     * @see #addAlert(Alert)
     * @see #hasAlerts()
     * @see #hasAlert(Alert)
     */
    public synchronized List<Alert> getAlerts() {
        if (alerts == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(this.alerts);
    }

    public String getMethod() {
        return httpMessageCachedData.getMethod();
    }

    public int getStatusCode() {
        return httpMessageCachedData.getStatusCode();
    }

    public String getReason() {
        return httpMessageCachedData.getReason();
    }

    public int getRtt() {
        return httpMessageCachedData.getRtt();
    }

    /**
     * Sets the tags for the HTTP message.
     *
     * @param tags the new tags.
     * @throws IllegalArgumentException if the given parameter is {@code null}.
     * @since 2.7.0
     * @see #addTag(String)
     * @see #deleteTag(String)
     */
    public void setTags(List<String> tags) {
        if (tags == null) {
            throw new IllegalArgumentException("Parameter tags must not be null.");
        }

        for (String tag : tags) {
            if (!this.tags.contains(tag)) {
                insertTagDb(tag);
            }
        }

        for (String tag : this.tags) {
            if (!tags.contains(tag)) {
                deleteTagDb(tag);
            }
        }

        this.tags = new ArrayList<>(tags);
        notifyEvent(HistoryReferenceEventPublisher.EVENT_TAGS_SET);
    }

    /**
     * Sets the tags for the HTTP message.
     *
     * @param tags the new tags.
     * @throws IllegalArgumentException if the given parameter is {@code null}.
     * @deprecated (2.7.0) Use {@link #setTags(List)} instead.
     */
    @Deprecated
    public void setTags(Vector<String> tags) {
        setTags((List<String>) tags);
    }

    public boolean hasNote() {
        return httpMessageCachedData.hasNote();
    }

    public long getTimeSentMillis() {
        return httpMessageCachedData.getTimeSentMillis();
    }

    public long getTimeReceivedMillis() {
        return httpMessageCachedData.getTimeReceivedMillis();
    }

    public boolean isWebSocketUpgrade() {
        return webSocketUpgrade;
    }

    public int getRequestHeaderLength() {
        return httpMessageCachedData.getRequestHeaderLength();
    }

    public int getRequestBodyLength() {
        return httpMessageCachedData.getRequestBodyLength();
    }

    public int getResponseHeaderLength() {
        return httpMessageCachedData.getResponseHeaderLength();
    }

    public int getResponseBodyLength() {
        return httpMessageCachedData.getResponseBodyLength();
    }

    public String getRequestBody() {
        String requestBody = httpMessageCachedData.getRequestBody();
        if (requestBody == null) {
            try {
                requestBody = getHttpMessage().getRequestBody().toString();
                httpMessageCachedData.setRequestBody(requestBody);
            } catch (HttpMalformedHeaderException | DatabaseException e) {
                log.error(
                        "Failed to reload request body from database with history ID: {}",
                        historyId,
                        e);
                requestBody = "";
            }
        }
        return requestBody;
    }

    /**
     * Adds the given {@code type} to the set of temporary types.
     *
     * <p>Persisted messages with temporary types are deleted when the session is closed.
     *
     * @since 2.5.0
     * @param type the history type that will be added
     * @see #removeTemporaryType(int)
     * @see #getTemporaryTypes()
     */
    public static void addTemporaryType(int type) {
        synchronized (TEMPORARY_HISTORY_TYPES) {
            TEMPORARY_HISTORY_TYPES.add(type);
        }
    }

    /**
     * Removes the given {@code type} from the set of temporary types.
     *
     * <p>Attempting to remove a default temporary type has no effect.
     *
     * @since 2.5.0
     * @param type the history type that will be removed
     * @see #DEFAULT_TEMPORARY_HISTORY_TYPES
     * @see #addTemporaryType(int)
     * @see #getTemporaryTypes()
     */
    public static void removeTemporaryType(int type) {
        Integer typeInteger = type;
        if (DEFAULT_TEMPORARY_HISTORY_TYPES.contains(typeInteger)) {
            return;
        }
        synchronized (TEMPORARY_HISTORY_TYPES) {
            TEMPORARY_HISTORY_TYPES.remove(typeInteger);
        }
    }

    /**
     * Gets the temporary history types.
     *
     * <p>Persisted messages with temporary types are deleted when the session is closed.
     *
     * @return a {@code Set} with the temporary history types
     * @see #addTemporaryType(int)
     * @see #removeTemporaryType(int)
     */
    public static Set<Integer> getTemporaryTypes() {
        synchronized (TEMPORARY_HISTORY_TYPES) {
            return new HashSet<>(TEMPORARY_HISTORY_TYPES);
        }
    }

    /**
     * Gets the tags of the message with the given history ID.
     *
     * @param historyId the history ID.
     * @return a {@code List} with the tags of the message, never {@code null}.
     * @throws DatabaseException if an error occurred while obtaining the tags from the database.
     * @since 2.7.0
     */
    public static List<String> getTags(int historyId) throws DatabaseException {
        if (staticTableTag == null) {
            return new ArrayList<>();
        }

        List<String> tags = new ArrayList<>();
        List<RecordTag> rtags = staticTableTag.getTagsForHistoryID(historyId);
        for (RecordTag rtag : rtags) {
            tags.add(rtag.getTag());
        }
        return tags;
    }
}
