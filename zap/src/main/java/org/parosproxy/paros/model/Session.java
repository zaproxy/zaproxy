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
// ZAP: 2011/05/15 Support for exclusions
// ZAP: 2012/02/11 Re-ordered icons, added spider icon and notify via SiteMap
// ZAP: 2012/02/18 Rationalised session handling
// ZAP: 2012/04/23 Added @Override annotation to all appropriate methods and
// removed unnecessary casts.
// ZAP: 2012/05/15 Changed the method parse() to get the session description.
// ZAP: 2012/06/11 Changed the JavaDoc of the method isNewState().
// ZAP: 2012/07/29 Issue 43: Added support for Scope
// ZAP: 2012/08/01 Issue 332: added support for Modes
// ZAP: 2012/08/07 Added method for getting all Nodes in Scope
// ZAP: 2012/08/29 Issue 250 Support for authentication management
// ZAP: 2012/10/02 Issue 385: Added support for Contexts
// ZAP: 2012/10/03 Issue 388: Added support for technologies
// ZAP: 2012/10/08 Issue 391: Performance improvements
// ZAP: 2012/12/14 Issue 438: Validate regexs as part of API enhancements
// ZAP: 2013/04/16 Issue 638: Persist and snapshot sessions instead of saving them
// ZAP: 2013/08/27 Issue 772: Restructuring of Saving/Loading Context Data
// ZAP: 2013/09/26 Issue 747: Error opening session files on directories with special characters
// ZAP: 2013/11/16 Issue 869: Differentiate proxied requests from (ZAP) user requests
// ZAP: 2014/01/06 Issue 965: Support 'single page' apps and 'non standard' parameter separators
// ZAP: 2014/01/31 Issue 979: Sites and Alerts trees can get corrupted - load session on
// EventDispatchThread
// ZAP: 2014-02-04 Added GlobalExcludeURL functionality:  Issue: TODO - insert bug/issue list here.
// ZAP: 2014/03/23 Issue 997: Session.open complains about improper use of addPath
// ZAP: 2014/03/23 Issue 999: History loaded in wrong order
// ZAP: 2014/05/26 Added listeners for contexts changed events.
// ZAP: 2014/06/10 Added helper method for removing data for context and type
// ZAP: 2014/07/15 Issue 1265: Context import and export
// ZAP: 2014/11/18 Issue 1408: Extend the structural parameter handling to forms param
// ZAP: 2014/12/22 Issue 1476: Display contexts in the Sites tree
// ZAP: 2015/01/30 Set default context name
// ZAP: 2015/02/09 Issue 1525: Introduce a database interface layer to allow for alternative
// implementations
// ZAP: 2015/04/02 Issue 321: Support multiple databases and Issue 1582: Low memory option
// ZAP: 2015/08/19 Change to use ZapXmlConfiguration instead of extending FileXML
// ZAP: 2015/08/19 Issue 1789: Forced Browse/AJAX Spider messages not restored to Sites tab
// ZAP: 2015/10/21 Issue 1576: Support data driven content
// ZAP: 2015/12/14 Issue 2119: Context's description not imported
// ZAP: 2016/02/26 Issue 2273: Clear stats on session init
// ZAP: 2016/05/02 Issue 2451: Only a single Data Driven Node can be saved in a context
// ZAP: 2016/05/04 Changes to address issues related to ParameterParser
// ZAP: 2016/05/10 Use empty String for (URL) parameters with no value
// ZAP: 2016/05/24 Call Database.discardSession(long) in Session.discard()
// ZAP: 2016/06/10 Do not clean up the database if the current session does not require it
// ZAP: 2016/07/05 Issue 2218: Persisted Sessions don't save unconfigured Default Context
// ZAP: 2016/08/25 Detach sites tree model when loading the session
// ZAP: 2016/08/29 Issue 2736: Can't generate reports from saved Session data
// ZAP: 2016/10/24 Delay addition of imported context until it's known that it has no errors
// ZAP: 2016/10/26 Issue 1952: Do not allow Contexts with same name
// ZAP: 2016/12/06 Remove contexts before refreshing the UI when discarding the contexts
// ZAP: 2017/01/04 Remove dependency on ExtensionSpider
// ZAP: 2017/01/26 Remove dependency on ExtensionActiveScan
// ZAP: 2017/03/13 Remove global excluded URLs from Session's state.
// ZAP: 2017/06/07 Allow to persist the session properties (e.g. name, description).
// ZAP: 2017/09/03 Cope with Java 9 change to TreeNode.children().
// ZAP: 2017/10/11 Make contextsChangedListeners static.
// ZAP: 2018/01/25 Do not save session file if not file based.
// ZAP: 2018/02/14 Remove unnecessary boxing / unboxing
// ZAP: 2018/07/09 No longer need cast on SiteMap.getRoot
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2019/07/10 Update to use Context.getId following deprecation of Context.getIndex
// ZAP: 2020/07/31 Tidy up parameter methods
// ZAP: 2020/08/17 Changed to use the VariantFactory
// ZAP: 2020/09/04 Added getContextDataString and getContextDataInteger
// ZAP: 2020/10/01 Remove use of org.jfree.util.Log use normal log4j infrastructure.
// ZAP: 2020/10/14 Require just the name when importing context.
// ZAP: 2020/11/02 Validate parameters in getLeafName(...)
// ZAP: 2020/11/26 Use Log4j 2 classes for logging.
// ZAP: 2022/02/09 Remove code no longer needed and deprecate a method.
// ZAP: 2022/02/28 Remove code deprecated in 2.6.0
// ZAP: 2022/08/23 Use SiteMap#createTree to create a new Sites Tree when loading a session.
// ZAP: 2022/09/21 Use format specifiers instead of concatenation when logging.
package org.parosproxy.paros.model;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Pattern;
import javax.swing.tree.TreeNode;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.db.Database;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.RecordContext;
import org.parosproxy.paros.db.RecordSessionUrl;
import org.parosproxy.paros.network.HtmlParameter;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.control.ExtensionFactory;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.IllegalContextNameException;
import org.zaproxy.zap.model.NameValuePair;
import org.zaproxy.zap.model.ParameterParser;
import org.zaproxy.zap.model.SessionStructure;
import org.zaproxy.zap.model.StandardParameterParser;
import org.zaproxy.zap.model.StructuralNodeModifier;
import org.zaproxy.zap.model.Tech;
import org.zaproxy.zap.model.TechSet;
import org.zaproxy.zap.utils.Stats;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

public class Session {

    // ZAP: Added logger
    private static Logger log = LogManager.getLogger(Session.class);

    private static final String ROOT = "session";

    private static final String SESSION_DESC = "sessionDesc";
    private static final String SESSION_ID = "sessionId";
    private static final String SESSION_NAME = "sessionName";

    private ZapXmlConfiguration configuration;

    // other runtime members
    private Model model = null;
    private String fileName = "";
    private String sessionDesc = "";
    private List<String> excludeFromProxyRegexs = new ArrayList<>();
    private List<String> excludeFromScanRegexs = new ArrayList<>();
    private List<String> excludeFromSpiderRegexs = new ArrayList<>();

    private List<Context> contexts = new ArrayList<>();
    private int nextContextId = 1;

    // parameters in XML
    private long sessionId = 0;
    private String sessionName = "";
    private SiteMap siteTree = null;

    private ParameterParser defaultParamParser = new StandardParameterParser();

    /**
     * Constructor for the current session. The current system time will be used as the session ID.
     *
     * @param model
     */
    public Session(Model model) {
        configuration = new ZapXmlConfiguration();
        configuration.setRootElementName(ROOT);

        // add session variable here
        setSessionId(System.currentTimeMillis());
        setSessionName(Constant.messages.getString("session.untitled"));
        setSessionDesc("");

        if (!Constant.isLowMemoryOptionSet()) {
            // create default object
            this.siteTree = SiteMap.createTree(model);
        }

        this.model = model;

        discardContexts();

        Stats.clearAll();
    }

    private void discardContexts() {
        this.contexts.clear();
        if (View.isInitialised()) {
            View.getSingleton().discardContexts();
        }
        for (OnContextsChangedListener l : contextsChangedListeners) l.contextsChanged();
        nextContextId = 1;
    }

    protected void discard() {
        try {
            model.getDb().discardSession(getSessionId());
        } catch (DatabaseException e) {
            // ZAP: Log exceptions
            log.warn(e.getMessage(), e);
        }
        discardContexts();
    }

    protected void close() {
        discardContexts();
    }

    /** @return Returns the sessionDesc. */
    public String getSessionDesc() {
        return sessionDesc;
    }

    /** @return Returns the sessionId. */
    public long getSessionId() {
        return sessionId;
    }
    /** @return Returns the name. */
    public String getSessionName() {
        return sessionName;
    }
    /** @return Returns the siteTree. */
    public SiteMap getSiteTree() {
        return siteTree;
    }

    /**
     * Tells whether this session is in a new state or not. A session is in a new state if it was
     * never saved or it was not loaded from an existing session.
     *
     * @return {@code true} if this session is in a new state, {@code false} otherwise.
     */
    // ZAP: Changed the JavaDoc.
    public boolean isNewState() {
        return fileName.equals("");
    }

    protected void open(final File file, final SessionListener callback) {
        Thread t =
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                Exception thrownException = null;
                                try {
                                    open(file.getAbsolutePath());
                                } catch (Exception e) {
                                    thrownException = e;
                                }
                                if (callback != null) {
                                    callback.sessionOpened(file, thrownException);
                                }
                            }
                        });
        t.setPriority(Thread.NORM_PRIORITY - 2);
        t.start();
    }

    protected void open(final String sessionFile, final SessionListener callback) {
        Thread t =
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                Exception thrownException = null;
                                try {
                                    open(sessionFile);
                                } catch (Exception e) {
                                    thrownException = e;
                                }
                                if (callback != null) {
                                    callback.sessionOpened(null, thrownException);
                                }
                            }
                        });
        t.setPriority(Thread.NORM_PRIORITY - 2);
        t.start();
    }

    protected void open(String fileName) throws DatabaseException, IOException, Exception {

        // TODO extract into db specific classes??
        if (Database.DB_TYPE_HSQLDB.equals(model.getDb().getType())) {
            configuration = new ZapXmlConfiguration(new File(fileName));
            sessionId = configuration.getLong(SESSION_ID);
            sessionName = configuration.getString(SESSION_NAME, "");
            sessionDesc = configuration.getString(SESSION_DESC, "");
        } else {
            this.setSessionId(Long.parseLong(fileName));
        }
        model.getDb().close(false, isCleanUpRequired());
        model.getDb().open(fileName);
        this.fileName = fileName;

        // historyList.removeAllElements();

        if (View.isInitialised()) {
            // Detach the siteTree model from the Sites tree, to reduce notification changes to the
            // UI while loading
            View.getSingleton().getSiteTreePanel().getTreeSite().setModel(new SiteMap(null, null));
        }

        if (!Constant.isLowMemoryOptionSet()) {
            siteTree = SiteMap.createTree(model);
        }

        // update history reference
        List<Integer> list =
                model.getDb()
                        .getTableHistory()
                        .getHistoryIdsOfHistType(
                                getSessionId(),
                                HistoryReference.TYPE_PROXIED,
                                HistoryReference.TYPE_ZAP_USER);

        HistoryReference historyRef = null;

        discardContexts();

        // Load the session urls
        this.setExcludeFromProxyRegexs(
                sessionUrlListToStingList(
                        model.getDb()
                                .getTableSessionUrl()
                                .getUrlsForType(RecordSessionUrl.TYPE_EXCLUDE_FROM_PROXY)));

        this.setExcludeFromScanRegexs(
                sessionUrlListToStingList(
                        model.getDb()
                                .getTableSessionUrl()
                                .getUrlsForType(RecordSessionUrl.TYPE_EXCLUDE_FROM_SCAN)));

        this.setExcludeFromSpiderRegexs(
                sessionUrlListToStingList(
                        model.getDb()
                                .getTableSessionUrl()
                                .getUrlsForType(RecordSessionUrl.TYPE_EXCLUDE_FROM_SPIDER)));

        for (int i = 0; i < list.size(); i++) {
            // ZAP: Removed unnecessary cast.
            int historyId = list.get(i);

            try {
                historyRef = new HistoryReference(historyId);

                if (View.isInitialised()) {
                    final HistoryReference hRef = historyRef;
                    final HttpMessage msg = historyRef.getHttpMessage();
                    EventQueue.invokeAndWait(
                            new Runnable() {

                                @Override
                                public void run() {
                                    SiteNode sn = getSiteTree().addPath(hRef, msg);
                                    if (sn != null) {
                                        sn.setIncludedInScope(isIncludedInScope(sn), false);
                                        sn.setExcludedFromScope(isExcludedFromScope(sn), false);
                                    }
                                }
                            });
                } else {
                    SiteNode sn = getSiteTree().addPath(historyRef);
                    if (sn != null) {
                        sn.setIncludedInScope(this.isIncludedInScope(sn), false);
                        sn.setExcludedFromScope(this.isExcludedFromScope(sn), false);
                    }
                }
                // ZAP: Load alerts from db
                historyRef.loadAlerts();

                if (i % 100 == 99) Thread.yield();
            } catch (Exception e) {
                // ZAP: Log exceptions
                log.warn(e.getMessage(), e);
            }
        }

        // update siteTree reference
        list =
                model.getDb()
                        .getTableHistory()
                        .getHistoryIdsOfHistType(
                                getSessionId(),
                                HistoryReference.TYPE_SPIDER,
                                HistoryReference.TYPE_BRUTE_FORCE,
                                HistoryReference.TYPE_SPIDER_AJAX,
                                HistoryReference.TYPE_SCANNER);

        for (int i = 0; i < list.size(); i++) {
            // ZAP: Removed unnecessary cast.
            int historyId = list.get(i);

            try {
                historyRef = new HistoryReference(historyId);

                if (View.isInitialised()) {
                    final HistoryReference hRef = historyRef;
                    final HttpMessage msg = historyRef.getHttpMessage();
                    EventQueue.invokeAndWait(
                            new Runnable() {

                                @Override
                                public void run() {
                                    getSiteTree().addPath(hRef, msg);
                                }
                            });
                } else {
                    getSiteTree().addPath(historyRef);
                }

                historyRef.loadAlerts();

                if (i % 100 == 99) Thread.yield();

            } catch (Exception e) {
                // ZAP: Log exceptions
                log.warn(e.getMessage(), e);
            }
        }
        List<RecordContext> contextData = model.getDb().getTableContext().getAllData();
        for (RecordContext data : contextData) {
            Context ctx = this.getContext(data.getContextId());
            if (ctx == null) {
                ctx = new Context(this, data.getContextId());
                this.addContext(ctx);
                if (nextContextId <= data.getContextId()) {
                    nextContextId = data.getContextId() + 1;
                }
            }
            switch (data.getType()) {
                case RecordContext.TYPE_NAME:
                    ctx.setName(data.getData());
                    if (View.isInitialised()
                            && !ctx.getName().equals(String.valueOf(ctx.getId()))) {
                        View.getSingleton().renameContext(ctx);
                    }
                    break;
                case RecordContext.TYPE_DESCRIPTION:
                    ctx.setDescription(data.getData());
                    break;
                case RecordContext.TYPE_INCLUDE:
                    ctx.addIncludeInContextRegex(data.getData());
                    break;
                case RecordContext.TYPE_EXCLUDE:
                    ctx.addExcludeFromContextRegex(data.getData());
                    break;
                case RecordContext.TYPE_IN_SCOPE:
                    ctx.setInScope(Boolean.parseBoolean(data.getData()));
                    break;
                case RecordContext.TYPE_INCLUDE_TECH:
                    ctx.getTechSet().include(new Tech(data.getData()));
                    break;
                case RecordContext.TYPE_EXCLUDE_TECH:
                    ctx.getTechSet().exclude(new Tech(data.getData()));
                    break;
            }
        }
        for (Context ctx : contexts) {
            try {
                // Set up the URL parameter parser
                List<String> strs =
                        this.getContextDataStrings(
                                ctx.getId(), RecordContext.TYPE_URL_PARSER_CLASSNAME);
                if (strs.size() == 1) {
                    Class<?> c = ExtensionFactory.getAddOnLoader().loadClass(strs.get(0));
                    if (c == null) {
                        log.error(
                                "Failed to load URL parser for context {} : {}",
                                ctx.getId(),
                                strs.get(0));
                    } else {
                        ParameterParser parser = (ParameterParser) c.getConstructor().newInstance();
                        strs =
                                this.getContextDataStrings(
                                        ctx.getId(), RecordContext.TYPE_URL_PARSER_CONFIG);
                        if (strs.size() == 1) {
                            parser.init(strs.get(0));
                        }
                        parser.setContext(ctx);
                        ctx.setUrlParamParser(parser);
                    }
                }
            } catch (Exception e) {
                log.error("Failed to load URL parser for context {}", ctx.getId(), e);
            }
            try {
                // Set up the URL parameter parser
                List<String> strs =
                        this.getContextDataStrings(
                                ctx.getId(), RecordContext.TYPE_POST_PARSER_CLASSNAME);
                if (strs.size() == 1) {
                    Class<?> c = ExtensionFactory.getAddOnLoader().loadClass(strs.get(0));
                    if (c == null) {
                        log.error(
                                "Failed to load POST parser for context {} : {}",
                                ctx.getId(),
                                strs.get(0));
                    } else {
                        ParameterParser parser = (ParameterParser) c.getConstructor().newInstance();
                        strs =
                                this.getContextDataStrings(
                                        ctx.getId(), RecordContext.TYPE_POST_PARSER_CONFIG);
                        if (strs.size() == 1) {
                            parser.init(strs.get(0));
                        }
                        parser.setContext(ctx);
                        ctx.setPostParamParser(parser);
                    }
                }
            } catch (Exception e) {
                log.error("Failed to load POST parser for context {}", ctx.getId(), e);
            }

            try {
                // Set up the Data Driven Nodes
                List<String> strs =
                        this.getContextDataStrings(
                                ctx.getId(), RecordContext.TYPE_DATA_DRIVEN_NODES);
                for (String str : strs) {
                    ctx.addDataDrivenNodes(new StructuralNodeModifier(str));
                }
            } catch (Exception e) {
                log.error("Failed to load data driven nodes for context {}", ctx.getId(), e);
            }

            ctx.restructureSiteTree();
        }

        if (View.isInitialised()) {
            View.getSingleton().getSiteTreePanel().getTreeSite().setModel(siteTree);
            View.getSingleton().getSiteTreePanel().expandRoot();
        }
        this.refreshScope();
        Stats.clearAll();

        System.gc();
    }

    /**
     * Tells whether or not the session requires a clean up (for example, to remove temporary
     * messages).
     *
     * <p>The session requires a clean up if it's not a new session or, if it is, the database used
     * is not HSQLDB (file based).
     *
     * @return {@code true} if a clean up is required, {@code false} otherwise.
     */
    boolean isCleanUpRequired() {
        if (!isNewState()) {
            return true;
        }

        if (Database.DB_TYPE_HSQLDB.equals(model.getDb().getType())) {
            return false;
        }

        return true;
    }

    private List<String> sessionUrlListToStingList(List<RecordSessionUrl> rsuList) {
        List<String> urlList = new ArrayList<>(rsuList.size());
        for (RecordSessionUrl url : rsuList) {
            urlList.add(url.getUrl());
        }
        return urlList;
    }

    /**
     * Asynchronous call to save a session.
     *
     * @param fileName
     * @param callback
     */
    protected void save(final String fileName, final SessionListener callback) {
        Thread t =
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                Exception thrownException = null;
                                try {
                                    save(fileName);
                                } catch (Exception e) {
                                    // ZAP: Log exceptions
                                    log.warn(e.getMessage(), e);
                                    thrownException = e;
                                }
                                if (callback != null) {
                                    callback.sessionSaved(thrownException);
                                }
                            }
                        });
        t.setPriority(Thread.NORM_PRIORITY - 2);
        t.start();
    }

    /**
     * Synchronous call to save a session.
     *
     * @param fileName
     * @throws Exception
     */
    protected void save(String fileName) throws Exception {
        configuration.save(new File(fileName));
        if (isNewState()) {
            model.moveSessionDb(fileName);
        } else {
            if (!this.fileName.equals(fileName)) {
                // copy file to new fileName
                model.copySessionDb(this.fileName, fileName);
            }
        }
        this.fileName = fileName;

        if (!Constant.isLowMemoryOptionSet()) {
            synchronized (siteTree) {
                saveSiteTree(siteTree.getRoot());
            }
        }

        model.getDb().getTableSession().update(getSessionId(), getSessionName());
    }

    /**
     * Persists the properties (e.g. name, description) of the session.
     *
     * <p>Should be called only by "core" classes.
     *
     * @throws Exception if an error occurred while persisting the properties.
     * @since 2.7.0
     * @see #setSessionName(String)
     * @see #setSessionDesc(String)
     */
    protected void persistProperties() throws Exception {
        if (isNewState()) {
            return;
        }

        if (Database.DB_TYPE_HSQLDB.equals(model.getDb().getType())) {
            configuration.save(new File(fileName));
        }
        model.getDb().getTableSession().update(getSessionId(), getSessionName());
    }

    /**
     * Asynchronous call to snapshot a session.
     *
     * @param fileName
     * @param callback
     */
    protected void snapshot(final String fileName, final SessionListener callback) {
        Thread t =
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                Exception thrownException = null;
                                try {
                                    snapshot(fileName);
                                } catch (Exception e) {
                                    // ZAP: Log exceptions
                                    log.warn(e.getMessage(), e);
                                    thrownException = e;
                                }
                                if (callback != null) {
                                    callback.sessionSnapshot(thrownException);
                                }
                            }
                        });
        t.setPriority(Thread.NORM_PRIORITY - 2);
        t.start();
    }

    /**
     * Synchronous call to snapshot a session.
     *
     * @param fileName
     * @throws Exception
     */
    protected void snapshot(String fileName) throws Exception {
        configuration.save(new File(fileName));
        model.snapshotSessionDb(this.fileName, fileName);
    }

    /** @param sessionDesc The sessionDesc to set. */
    public void setSessionDesc(String sessionDesc) {
        this.sessionDesc = sessionDesc;
        configuration.setProperty(SESSION_DESC, sessionDesc);
    }

    /** @param sessionId The sessionId to set. */
    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
        // setText(SESSION_ID, Long.toString(sessionId));
        configuration.setProperty(SESSION_ID, Long.toString(sessionId));
    }
    /** @param name The name to set. */
    public void setSessionName(String name) {
        this.sessionName = name;
        // setText(SESSION_NAME, name);
        configuration.setProperty(SESSION_NAME, name);
    }

    public String getFileName() {
        return fileName;
    }

    private void saveSiteTree(SiteNode node) {
        HttpMessage msg = null;

        if (!node.isRoot()) {
            if (node.getHistoryReference().getHistoryType() < 0) {
                // -ve means to be saved
                saveNodeMsg(msg);
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            try {
                saveSiteTree((SiteNode) node.getChildAt(i));
            } catch (Exception e) {
                // ZAP: Log exceptions
                log.warn(e.getMessage(), e);
            }
        }
    }

    private void saveNodeMsg(HttpMessage msg) {
        // nothing need to be done
    }

    public String getSessionFolder() {
        String result = "";
        if (fileName.equals("")) {
            //            result = Constant.FOLDER_SESSION;
            result = Constant.getInstance().FOLDER_SESSION;
        } else {
            File file = new File(fileName);
            result = file.getParent();
        }
        return result;
    }

    public List<String> getExcludeFromProxyRegexs() {
        return excludeFromProxyRegexs;
    }

    private List<String> stripEmptyLines(List<String> list) {
        List<String> slist = new ArrayList<>();
        for (String str : list) {
            if (str.length() > 0) {
                slist.add(str);
            }
        }
        return slist;
    }

    private void refreshScope(SiteNode node) {
        if (node == null) {
            return;
        }
        if (node.isIncludedInScope() == !this.isIncludedInScope(node)) {
            // Its 'scope' state has changed, so switch it!
            node.setIncludedInScope(!node.isIncludedInScope(), false);
        }
        if (node.isExcludedFromScope() == !this.isExcludedFromScope(node)) {
            // Its 'scope' state has changed, so switch it!
            node.setExcludedFromScope(!node.isExcludedFromScope(), false);
        }
        // Recurse down
        if (node.getChildCount() > 0) {
            SiteNode c = (SiteNode) node.getFirstChild();
            while (c != null) {
                refreshScope(c);
                c = (SiteNode) node.getChildAfter(c);
            }
        }
    }

    private void refreshScope() {
        // log.debug("refreshScope");
        if (Constant.isLowMemoryOptionSet()) {
            // Nothing to do
            return;
        }

        if (EventQueue.isDispatchThread()) {
            refreshScope(siteTree.getRoot());
            Control.getSingleton().sessionScopeChanged();
        } else {
            try {
                EventQueue.invokeLater(
                        new Runnable() {
                            @Override
                            public void run() {
                                refreshScope(siteTree.getRoot());
                                Control.getSingleton().sessionScopeChanged();
                            }
                        });
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    protected boolean isIncludedInScope(SiteNode sn) {
        if (sn == null) {
            return false;
        }
        return isIncludedInScope(sn.getHierarchicNodeName());
    }

    private boolean isIncludedInScope(String url) {
        if (url == null) {
            return false;
        }
        if (url.indexOf("?") > 0) {
            // Strip off any parameters
            url = url.substring(0, url.indexOf("?"));
        }
        for (Context context : contexts) {
            if (context.isInScope() && context.isIncluded(url)) {
                return true;
            }
        }
        return false;
    }

    protected boolean isExcludedFromScope(SiteNode sn) {
        if (sn == null) {
            return false;
        }
        return isExcludedFromScope(sn.getHierarchicNodeName());
    }

    private boolean isExcludedFromScope(String url) {
        if (url == null) {
            return false;
        }
        if (url.indexOf("?") > 0) {
            // Strip off any parameters
            url = url.substring(0, url.indexOf("?"));
        }
        for (Context context : contexts) {
            if (context.isInScope() && context.isExcluded(url)) {
                return true;
            }
        }
        return false;
    }

    public boolean isInScope(HistoryReference href) {
        if (href == null) {
            return false;
        }
        if (href.getSiteNode() != null) {
            return this.isInScope(href.getSiteNode());
        }
        try {
            return this.isInScope(href.getURI().toString());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    public boolean isInScope(SiteNode sn) {
        if (sn == null) {
            return false;
        }
        return isInScope(sn.getHierarchicNodeName());
    }

    public boolean isInScope(String url) {
        if (url.indexOf("?") > 0) {
            // String off any parameters
            url = url.substring(0, url.indexOf("?"));
        }
        if (!this.isIncludedInScope(url)) {
            // Not explicitly included
            return false;
        }
        // Check to see if its explicitly excluded
        return !this.isExcludedFromScope(url);
    }

    /**
     * Gets the nodes from the site tree which are "In Scope". Searches recursively starting from
     * the root node. Should be used with care, as it is time-consuming, querying the database for
     * every node in the Site Tree.
     *
     * @return the nodes in scope from site tree
     */
    public List<SiteNode> getNodesInScopeFromSiteTree() {
        List<SiteNode> nodes = new LinkedList<>();
        SiteNode rootNode = getSiteTree().getRoot();
        fillNodesInScope(rootNode, nodes);
        return nodes;
    }

    /**
     * Gets the top nodes from the site tree which contain nodes that are "In Scope". Searches
     * recursively starting from the root node. Should be used with care, as it is time-consuming,
     * querying the database for every node in the Site Tree.
     *
     * @return the nodes in scope from site tree
     */
    public List<SiteNode> getTopNodesInScopeFromSiteTree() {
        List<SiteNode> nodes = new LinkedList<>();
        SiteNode rootNode = getSiteTree().getRoot();
        @SuppressWarnings("unchecked")
        Enumeration<TreeNode> en = rootNode.children();
        while (en.hasMoreElements()) {
            SiteNode sn = (SiteNode) en.nextElement();
            if (isContainsNodesInScope(sn)) {
                nodes.add(sn);
            }
        }
        return nodes;
    }

    private boolean isContainsNodesInScope(SiteNode node) {
        if (node.isIncludedInScope()) {
            return true;
        }
        @SuppressWarnings("unchecked")
        Enumeration<TreeNode> en = node.children();
        while (en.hasMoreElements()) {
            SiteNode sn = (SiteNode) en.nextElement();
            if (isContainsNodesInScope(sn)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Fills a given list with nodes in scope, searching recursively.
     *
     * @param rootNode the root node
     * @param nodesList the nodes list
     */
    private void fillNodesInScope(SiteNode rootNode, List<SiteNode> nodesList) {
        @SuppressWarnings("unchecked")
        Enumeration<TreeNode> en = rootNode.children();
        while (en.hasMoreElements()) {
            SiteNode sn = (SiteNode) en.nextElement();
            if (isInScope(sn)) nodesList.add(sn);
            fillNodesInScope(sn, nodesList);
        }
    }

    /**
     * Gets the nodes from the site tree which are "In Scope" in a given context. Searches
     * recursively starting from the root node. Should be used with care, as it is time-consuming,
     * querying the database for every node in the Site Tree.
     *
     * @param context the context
     * @return the nodes in scope from site tree
     */
    public List<SiteNode> getNodesInContextFromSiteTree(Context context) {
        List<SiteNode> nodes = new LinkedList<>();
        SiteNode rootNode = getSiteTree().getRoot();
        fillNodesInContext(rootNode, nodes, context);
        return nodes;
    }

    /**
     * Fills a given list with nodes in context, searching recursively.
     *
     * @param rootNode the root node
     * @param nodesList the nodes list
     * @param context the context
     */
    private void fillNodesInContext(SiteNode rootNode, List<SiteNode> nodesList, Context context) {
        @SuppressWarnings("unchecked")
        Enumeration<TreeNode> en = rootNode.children();
        while (en.hasMoreElements()) {
            SiteNode sn = (SiteNode) en.nextElement();
            if (context.isInContext(sn)) nodesList.add(sn);
            fillNodesInContext(sn, nodesList, context);
        }
    }

    public void setExcludeFromProxyRegexs(List<String> ignoredRegexs) throws DatabaseException {
        // Validate its a valid regex first
        for (String url : ignoredRegexs) {
            Pattern.compile(url, Pattern.CASE_INSENSITIVE);
        }

        this.excludeFromProxyRegexs = stripEmptyLines(ignoredRegexs);

        model.getDb()
                .getTableSessionUrl()
                .setUrls(RecordSessionUrl.TYPE_EXCLUDE_FROM_PROXY, this.excludeFromProxyRegexs);
    }

    public void addExcludeFromProxyRegex(String ignoredRegex) throws DatabaseException {
        // Validate its a valid regex first
        Pattern.compile(ignoredRegex, Pattern.CASE_INSENSITIVE);

        this.excludeFromProxyRegexs.add(ignoredRegex);
        model.getDb()
                .getTableSessionUrl()
                .setUrls(RecordSessionUrl.TYPE_EXCLUDE_FROM_PROXY, this.excludeFromProxyRegexs);
    }

    public List<String> getExcludeFromScanRegexs() {
        return excludeFromScanRegexs;
    }

    public void addExcludeFromScanRegexs(String ignoredRegex) throws DatabaseException {
        // Validate its a valid regex first
        Pattern.compile(ignoredRegex, Pattern.CASE_INSENSITIVE);

        this.excludeFromScanRegexs.add(ignoredRegex);
        model.getDb()
                .getTableSessionUrl()
                .setUrls(RecordSessionUrl.TYPE_EXCLUDE_FROM_SCAN, this.excludeFromScanRegexs);
    }

    public void setExcludeFromScanRegexs(List<String> ignoredRegexs) throws DatabaseException {
        // Validate its a valid regex first
        for (String url : ignoredRegexs) {
            Pattern.compile(url, Pattern.CASE_INSENSITIVE);
        }

        this.excludeFromScanRegexs = stripEmptyLines(ignoredRegexs);
        model.getDb()
                .getTableSessionUrl()
                .setUrls(RecordSessionUrl.TYPE_EXCLUDE_FROM_SCAN, this.excludeFromScanRegexs);
    }

    /**
     * Gets the regular expressions used to exclude URLs from the spiders (e.g. traditional, AJAX).
     *
     * @return a {@code List} containing the regular expressions, never {@code null}.
     */
    public List<String> getExcludeFromSpiderRegexs() {
        return excludeFromSpiderRegexs;
    }

    /**
     * Adds the given regular expression to the list of regular expressions used to exclude URLs
     * from the spiders (e.g. traditional, AJAX).
     *
     * @param ignoredRegex the regular expression to be added
     * @throws IllegalArgumentException if the regular expression is not valid.
     * @throws DatabaseException if an error occurred while persisting the list.
     */
    public void addExcludeFromSpiderRegex(String ignoredRegex) throws DatabaseException {
        // Validate its a valid regex first
        Pattern.compile(ignoredRegex, Pattern.CASE_INSENSITIVE);

        this.excludeFromSpiderRegexs.add(ignoredRegex);
        model.getDb()
                .getTableSessionUrl()
                .setUrls(RecordSessionUrl.TYPE_EXCLUDE_FROM_SPIDER, this.excludeFromSpiderRegexs);
    }

    /**
     * Sets the given regular expressions as the list of regular expressions used to exclude URLs
     * from the spiders (e.g. traditional, AJAX).
     *
     * @param ignoredRegexs the regular expressions to be set
     * @throws IllegalArgumentException if any of the regular expressions is not valid.
     * @throws DatabaseException if an error occurred while persisting the list.
     */
    public void setExcludeFromSpiderRegexs(List<String> ignoredRegexs) throws DatabaseException {
        // Validate its a valid regex first
        for (String url : ignoredRegexs) {
            Pattern.compile(url, Pattern.CASE_INSENSITIVE);
        }

        this.excludeFromSpiderRegexs = stripEmptyLines(ignoredRegexs);
        model.getDb()
                .getTableSessionUrl()
                .setUrls(RecordSessionUrl.TYPE_EXCLUDE_FROM_SPIDER, this.excludeFromSpiderRegexs);
    }

    /**
     * Resets the global exclude URLs of the Local Proxy.
     *
     * <p>This should be considered an internal method, to be called only by core code.
     *
     * @since 2.3.0
     * @deprecated (2.12.0) No longer used/needed. It will be removed in a future release.
     */
    @Deprecated
    public void forceGlobalExcludeURLRefresh() {}

    /**
     * Gets the global exclude URLs.
     *
     * <p><strong>Note:</strong> This method is only provided as a convenience, the global exclude
     * URLs are not saved in the session.
     *
     * @return an unmodifiable {@code List} containing the URLs that should be excluded globally.
     * @since 2.3.0
     */
    public List<String> getGlobalExcludeURLRegexs() {
        return Collections.unmodifiableList(
                model.getOptionsParam().getGlobalExcludeURLParam().getTokensNames());
    }

    public void setSessionUrls(int type, List<String> urls) throws DatabaseException {
        model.getDb().getTableSessionUrl().setUrls(type, urls);
    }

    public void setSessionUrl(int type, String url) throws DatabaseException {
        List<String> list = new ArrayList<>(1);
        list.add(url);
        this.setSessionUrls(type, list);
    }

    public List<String> getSessionUrls(int type) throws DatabaseException {
        List<RecordSessionUrl> urls = model.getDb().getTableSessionUrl().getUrlsForType(type);
        List<String> list = new ArrayList<>(urls.size());
        for (RecordSessionUrl url : urls) {
            list.add(url.getUrl());
        }
        return list;
    }

    public List<String> getContextDataStrings(int contextId, int type) throws DatabaseException {
        List<RecordContext> dataList =
                model.getDb().getTableContext().getDataForContextAndType(contextId, type);
        List<String> list = new ArrayList<>();
        for (RecordContext data : dataList) {
            list.add(data.getData());
        }
        return list;
    }

    /**
     * Returns a context data string of the given type and using the given default value if the
     * value is not present
     *
     * @param contextId the context Id
     * @param type the data type required
     * @param defaultValue the default value to use if the type is not present
     * @return the context data string
     * @throws DatabaseException
     * @since 2.10.0
     */
    public String getContextDataString(int contextId, int type, String defaultValue)
            throws DatabaseException {
        List<RecordContext> dataList =
                model.getDb().getTableContext().getDataForContextAndType(contextId, type);
        if (dataList.size() > 0) {
            return dataList.get(0).getData();
        }
        return defaultValue;
    }

    /**
     * Returns a context data integer of the given type and using the given default value if the
     * value is not present
     *
     * @param contextId the context Id
     * @param type the data type required
     * @param defaultValue the default value to use if the type is not present
     * @return the context data integer
     * @throws DatabaseException
     * @since 2.10.0
     */
    public int getContextDataInteger(int contextId, int type, int defaultValue)
            throws DatabaseException {
        List<RecordContext> dataList =
                model.getDb().getTableContext().getDataForContextAndType(contextId, type);
        if (dataList.size() > 0) {
            try {
                return Integer.parseInt(dataList.get(0).getData());
            } catch (NumberFormatException e) {
                log.error("Failed to parse context value type {}", type, e);
            }
        }
        return defaultValue;
    }

    public void setContextData(int contextId, int type, String data) throws DatabaseException {
        List<String> list = new ArrayList<>();
        list.add(data);
        this.setContextData(contextId, type, list);
    }

    public void setContextData(int contextId, int type, List<String> dataList)
            throws DatabaseException {
        model.getDb().getTableContext().setData(contextId, type, dataList);
    }

    public void clearContextDataForType(int contextId, int type) throws DatabaseException {
        model.getDb().getTableContext().deleteAllDataForContextAndType(contextId, type);
    }

    public void clearContextData(int contextId) throws DatabaseException {
        model.getDb().getTableContext().deleteAllDataForContext(contextId);
    }

    private List<String> techListToStringList(TreeSet<Tech> techList) {
        List<String> strList = new ArrayList<>();
        Iterator<Tech> iter = techList.iterator();
        while (iter.hasNext()) {
            strList.add(iter.next().toString());
        }
        return strList;
    }

    private List<String> snmListToStringList(List<StructuralNodeModifier> list) {
        List<String> strList = new ArrayList<>();
        for (StructuralNodeModifier snm : list) {
            strList.add(snm.getConfig());
        }
        return strList;
    }

    public void saveContext(Context c) {
        try {
            this.setContextData(c.getId(), RecordContext.TYPE_NAME, c.getName());
            this.setContextData(c.getId(), RecordContext.TYPE_DESCRIPTION, c.getDescription());
            this.setContextData(
                    c.getId(), RecordContext.TYPE_IN_SCOPE, Boolean.toString(c.isInScope()));
            this.setContextData(
                    c.getId(), RecordContext.TYPE_INCLUDE, c.getIncludeInContextRegexs());
            this.setContextData(
                    c.getId(), RecordContext.TYPE_EXCLUDE, c.getExcludeFromContextRegexs());
            this.setContextData(
                    c.getId(),
                    RecordContext.TYPE_INCLUDE_TECH,
                    techListToStringList(c.getTechSet().getIncludeTech()));
            this.setContextData(
                    c.getId(),
                    RecordContext.TYPE_EXCLUDE_TECH,
                    techListToStringList(c.getTechSet().getExcludeTech()));
            this.setContextData(
                    c.getId(),
                    RecordContext.TYPE_URL_PARSER_CLASSNAME,
                    c.getUrlParamParser().getClass().getCanonicalName());
            this.setContextData(
                    c.getId(),
                    RecordContext.TYPE_URL_PARSER_CONFIG,
                    c.getUrlParamParser().getConfig());
            this.setContextData(
                    c.getId(),
                    RecordContext.TYPE_POST_PARSER_CLASSNAME,
                    c.getPostParamParser().getClass().getCanonicalName());
            this.setContextData(
                    c.getId(),
                    RecordContext.TYPE_POST_PARSER_CONFIG,
                    c.getPostParamParser().getConfig());
            this.setContextData(
                    c.getId(),
                    RecordContext.TYPE_DATA_DRIVEN_NODES,
                    snmListToStringList(c.getDataDrivenNodes()));

            model.saveContext(c);
        } catch (DatabaseException e) {
            log.error(e.getMessage(), e);
        }

        if (View.isInitialised()) {
            View.getSingleton().changeContext(c);
            refreshScope();
        }
    }

    public void saveAllContexts() {
        for (Context c : contexts) {
            this.saveContext(c);
        }
    }

    /**
     * Gets a newly created context with the given name.
     *
     * <p>The context is automatically added to the session.
     *
     * @param name the name of the context
     * @return the new {@code Context}.
     * @throws IllegalContextNameException (since 2.6.0) if the given name is {@code null} or empty
     *     or if a context with the given name already exists.
     */
    public Context getNewContext(String name) {
        validateContextName(name);
        Context c = createContext(name);
        this.addContext(c);
        return c;
    }

    /**
     * Creates a new context with the given name.
     *
     * @param name the name of the context
     * @return the new {@code Context}.
     * @see #getNewContext(String)
     */
    private Context createContext(String name) {
        Context context = new Context(this, this.nextContextId++);
        context.setName(name);
        return context;
    }

    /**
     * Validates the given name is not {@code null} nor empty and that no context already exists
     * with the given name.
     *
     * @param name the name to be validated
     * @throws IllegalContextNameException if the given name is {@code null} or empty or if a
     *     context with the given name already exists.
     */
    private void validateContextName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalContextNameException(
                    IllegalContextNameException.Reason.EMPTY_NAME,
                    "The context name must not be null nor empty.");
        }

        if (getContext(name) != null) {
            throw new IllegalContextNameException(
                    IllegalContextNameException.Reason.DUPLICATED_NAME,
                    "A context with the given name [" + name + "] already exists.");
        }
    }

    /**
     * Adds the given context.
     *
     * @param c the context to be added
     * @throws IllegalArgumentException (since 2.6.0) if the given context is {@code null}.
     * @throws IllegalContextNameException (since 2.6.0) if context's name is {@code null} or empty
     *     or if a context with the same name already exists.
     */
    public void addContext(Context c) {
        if (c == null) {
            throw new IllegalArgumentException("The context must not be null. ");
        }
        validateContextName(c.getName());

        this.contexts.add(c);
        this.model.loadContext(c);

        for (OnContextsChangedListener l : contextsChangedListeners) {
            l.contextAdded(c);
        }

        if (View.isInitialised()) {
            View.getSingleton().addContext(c);
        }
    }

    public void deleteContext(Context c) {
        this.contexts.remove(c);
        try {
            this.clearContextData(c.getId());
        } catch (DatabaseException e) {
            log.error(e.getMessage(), e);
        }

        for (OnContextsChangedListener l : contextsChangedListeners) {
            l.contextDeleted(c);
        }

        if (View.isInitialised()) {
            View.getSingleton().deleteContext(c);
            refreshScope();
        }
    }

    public Context getContext(int id) {
        for (Context context : contexts) {
            if (context.getId() == id) {
                return context;
            }
        }
        return null;
    }

    public Context getContext(String name) {
        for (Context context : contexts) {
            if (context.getName().equals(name)) {
                return context;
            }
        }
        return null;
    }

    public List<Context> getContexts() {
        return contexts;
    }

    public List<Context> getContextsForNode(SiteNode sn) {
        if (sn == null) {
            return new ArrayList<>();
        }
        return getContextsForUrl(sn.getHierarchicNodeName());
    }

    public List<Context> getContextsForUrl(String url) {
        List<Context> ctxList = new ArrayList<>();
        if (url.indexOf("?") > 0) {
            // String off any parameters
            url = url.substring(0, url.indexOf("?"));
        }
        for (Context context : contexts) {
            if (context.isInContext(url)) {
                ctxList.add(context);
            }
        }
        return ctxList;
    }

    /**
     * Export the specified context to a file
     *
     * @param contextId the ID of the Context to be exported
     * @param file the File to which the Context should be exported
     * @throws ConfigurationException
     */
    public void exportContext(int contextId, File file) throws ConfigurationException {
        this.exportContext(this.getContext(contextId), file);
    }

    /**
     * Export the specified context to a file
     *
     * @param c
     * @param file
     * @throws ConfigurationException
     */
    public void exportContext(Context c, File file) throws ConfigurationException {
        ZapXmlConfiguration config = new ZapXmlConfiguration();

        config.setProperty(Context.CONTEXT_CONFIG_NAME, c.getName());
        config.setProperty(Context.CONTEXT_CONFIG_DESC, c.getDescription());
        config.setProperty(Context.CONTEXT_CONFIG_INSCOPE, c.isInScope());
        config.setProperty(Context.CONTEXT_CONFIG_INC_REGEXES, c.getIncludeInContextRegexs());
        config.setProperty(Context.CONTEXT_CONFIG_EXC_REGEXES, c.getExcludeFromContextRegexs());
        config.setProperty(
                Context.CONTEXT_CONFIG_TECH_INCLUDE,
                techListToStringList(c.getTechSet().getIncludeTech()));
        config.setProperty(
                Context.CONTEXT_CONFIG_TECH_EXCLUDE,
                techListToStringList(c.getTechSet().getExcludeTech()));
        config.setProperty(
                Context.CONTEXT_CONFIG_URLPARSER_CLASS,
                c.getUrlParamParser().getClass().getCanonicalName());
        config.setProperty(
                Context.CONTEXT_CONFIG_URLPARSER_CONFIG, c.getUrlParamParser().getConfig());
        config.setProperty(
                Context.CONTEXT_CONFIG_POSTPARSER_CLASS,
                c.getPostParamParser().getClass().getCanonicalName());
        config.setProperty(
                Context.CONTEXT_CONFIG_POSTPARSER_CONFIG, c.getPostParamParser().getConfig());
        for (StructuralNodeModifier snm : c.getDataDrivenNodes()) {
            config.addProperty(Context.CONTEXT_CONFIG_DATA_DRIVEN_NODES, snm.getConfig());
        }

        model.exportContext(c, config);
        config.save(file);
    }

    /**
     * Imports a context from the specified (XML) file.
     *
     * @param file the (XML) file that contains the context data
     * @return the imported {@code Context}, already added to the session.
     * @throws ConfigurationException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws IllegalContextNameException (since 2.6.0) if context's name is not provided or it's
     *     empty or if a context with the same name already exists.
     */
    public Context importContext(File file)
            throws ConfigurationException, ClassNotFoundException, InstantiationException,
                    IllegalAccessException, IllegalArgumentException, InvocationTargetException,
                    NoSuchMethodException, SecurityException {
        ZapXmlConfiguration config = new ZapXmlConfiguration(file);

        String name = config.getString(Context.CONTEXT_CONFIG_NAME);
        validateContextName(name);

        Context c = createContext(name);

        c.setDescription(config.getString(Context.CONTEXT_CONFIG_DESC));
        c.setInScope(config.getBoolean(Context.CONTEXT_CONFIG_INSCOPE, false));
        for (Object obj : config.getList(Context.CONTEXT_CONFIG_INC_REGEXES)) {
            c.addIncludeInContextRegex(obj.toString());
        }
        for (Object obj : config.getList(Context.CONTEXT_CONFIG_EXC_REGEXES)) {
            c.addExcludeFromContextRegex(obj.toString());
        }

        TechSet techSet = new TechSet();
        for (Object obj : config.getList(Context.CONTEXT_CONFIG_TECH_INCLUDE)) {
            techSet.include(new Tech(obj.toString()));
        }
        for (Object obj : config.getList(Context.CONTEXT_CONFIG_TECH_EXCLUDE)) {
            techSet.exclude(new Tech(obj.toString()));
        }
        c.setTechSet(techSet);

        String urlParserClass = config.getString(Context.CONTEXT_CONFIG_URLPARSER_CLASS);
        if (urlParserClass == null) {
            // Can happen due to a bug in 2.4.0 where is was saved using the wrong name :(
            urlParserClass = config.getString(Context.CONTEXT_CONFIG_URLPARSER);
        }
        if (urlParserClass == null) {
            urlParserClass = StandardParameterParser.class.getCanonicalName();
        }
        Class<?> cl = ExtensionFactory.getAddOnLoader().loadClass(urlParserClass);
        if (cl == null) {
            throw new ConfigurationException(
                    "Failed to load URL parser for context " + urlParserClass);
        } else {
            ParameterParser parser = (ParameterParser) cl.getConstructor().newInstance();
            parser.init(config.getString(Context.CONTEXT_CONFIG_URLPARSER_CONFIG));
            parser.setContext(c);
            c.setUrlParamParser(parser);
        }

        String postParserClass = config.getString(Context.CONTEXT_CONFIG_POSTPARSER_CLASS);
        String postParserConfig = config.getString(Context.CONTEXT_CONFIG_POSTPARSER_CONFIG);
        if (postParserClass == null) {
            // Can happen due to a bug in 2.4.0 where is was saved using the wrong name :(
            postParserClass = config.getString(urlParserClass);
            postParserConfig = config.getString(Context.CONTEXT_CONFIG_URLPARSER_CONFIG);
        }
        if (postParserClass == null) {
            postParserClass = StandardParameterParser.class.getCanonicalName();
        }
        cl = ExtensionFactory.getAddOnLoader().loadClass(postParserClass);
        if (cl == null) {
            throw new ConfigurationException(
                    "Failed to load POST parser for context " + postParserClass);
        } else {
            ParameterParser parser = (ParameterParser) cl.getConstructor().newInstance();
            parser.init(postParserConfig);
            parser.setContext(c);
            c.setPostParamParser(parser);
        }
        for (Object obj : config.getList(Context.CONTEXT_CONFIG_DATA_DRIVEN_NODES)) {
            c.addDataDrivenNodes(new StructuralNodeModifier(obj.toString()));
        }

        model.importContext(c, config);

        c.restructureSiteTree();

        addContext(c);
        saveContext(c);
        return c;
    }

    /**
     * Returns the url parameter parser associated with the first context found that includes the
     * URL, or the default parser if it is not in a context
     *
     * @param url
     * @return
     */
    public ParameterParser getUrlParamParser(String url) {
        List<Context> contexts = getContextsForUrl(url);
        if (contexts.size() > 0) {
            return contexts.get(0).getUrlParamParser();
        }
        return this.defaultParamParser;
    }

    /**
     * Returns the form parameter parser associated with the first context found that includes the
     * URL, or the default parser if it is not in a context
     *
     * @param url
     * @return
     */
    public ParameterParser getFormParamParser(String url) {
        List<Context> contexts = getContextsForUrl(url);
        if (contexts.size() > 0) {
            return contexts.get(0).getPostParamParser();
        }
        return this.defaultParamParser;
    }

    /**
     * Returns the specified parameters for the given message based on the parser associated with
     * the first context found that includes the URL for the message, or the default parser if it is
     * not in a context
     *
     * @param msg
     * @param type
     * @return
     * @deprecated 2.10.0 use #getParameters(String) This method will lose duplicated parameter
     *     names
     */
    @Deprecated
    public Map<String, String> getParams(HttpMessage msg, HtmlParameter.Type type) {
        switch (type) {
            case form:
                return this.getFormParamParser(msg.getRequestHeader().getURI().toString())
                        .getParams(msg, type);
            case url:
                return this.getUrlParamParser(msg.getRequestHeader().getURI().toString())
                        .getParams(msg, type);
            default:
                throw new InvalidParameterException("Type not supported: " + type);
        }
    }

    /**
     * Gets the parameters of the given {@code type} from the given {@code message}.
     *
     * <p>Parameters' names and values are in decoded form.
     *
     * @param msg the message whose parameters will be extracted from
     * @param type the type of parameters to extract
     * @return a {@code List} containing the parameters
     * @throws IllegalArgumentException if any of the parameters is {@code null} or if the given
     *     {@code type} is not {@link org.parosproxy.paros.network.HtmlParameter.Type#url url} or
     *     {@link org.parosproxy.paros.network.HtmlParameter.Type#form form}.
     * @since 2.5.0
     * @see StandardParameterParser#getParameters(HttpMessage,
     *     org.parosproxy.paros.network.HtmlParameter.Type)
     */
    public List<NameValuePair> getParameters(HttpMessage msg, HtmlParameter.Type type) {
        if (msg == null) {
            throw new IllegalArgumentException("Parameter msg must not be null.");
        }
        if (type == null) {
            throw new IllegalArgumentException("Parameter type must not be null.");
        }

        switch (type) {
            case form:
                return this.getFormParamParser(msg.getRequestHeader().getURI().toString())
                        .getParameters(msg, type);
            case url:
                return this.getUrlParamParser(msg.getRequestHeader().getURI().toString())
                        .getParameters(msg, type);
            default:
                throw new IllegalArgumentException("The provided type is not supported: " + type);
        }
    }

    /**
     * Returns the URL parameters for the given URL based on the parser associated with the first
     * context found that includes the URL, or the default parser if it is not in a context
     *
     * @param uri
     * @return
     * @throws URIException
     * @deprecated 2.10.0 use #getUrlParameters(String)
     */
    @Deprecated
    public Map<String, String> getUrlParams(URI uri) throws URIException {
        Map<String, String> map = new HashMap<>();
        for (NameValuePair parameter :
                getUrlParamParser(uri.toString()).parseParameters(uri.getEscapedQuery())) {
            String value = parameter.getValue();
            if (value == null) {
                value = "";
            }
            map.put(parameter.getName(), value);
        }
        return map;
    }

    /**
     * Returns the URL parameters for the given URL based on the parser associated with the first
     * context found that includes the URL, or the default parser if it is not in a context
     *
     * @param uri
     * @return the URL parameters for the given URL
     * @throws URIException
     * @since 2.10.0
     */
    public List<NameValuePair> getUrlParameters(URI uri) throws URIException {
        return getUrlParamParser(uri.toString()).parseParameters(uri.getEscapedQuery());
    }

    /**
     * Returns the FORM parameters for the given URL based on the parser associated with the first
     * context found that includes the URL, or the default parser if it is not in a context
     *
     * @param uri
     * @param formData
     * @return the FORM parameters for the given URL
     * @throws URIException
     * @deprecated 2.10.0 use #getFormParameters(String)
     */
    @Deprecated
    public Map<String, String> getFormParams(URI uri, String formData) throws URIException {
        return this.getFormParamParser(uri.toString()).parse(formData);
    }

    /**
     * Returns the FORM parameters for the given URL based on the parser associated with the first
     * context found that includes the URL, or the default parser if it is not in a context
     *
     * @param uri
     * @param formData
     * @return the FORM parameters for the given URL
     * @throws URIException
     * @since 2.10.0
     */
    public List<NameValuePair> getFormParameters(URI uri, String formData) throws URIException {
        return this.getFormParamParser(uri.toString()).parseParameters(formData);
    }

    /** @deprecated use {@link SessionStructure#getTreePath(Model, URI)} */
    @Deprecated
    public List<String> getTreePath(URI uri) throws URIException {
        return SessionStructure.getTreePath(Model.getSingleton(), uri);
    }

    /** @deprecated use {@link SessionStructure#getTreePath(Model, HttpMessage)} */
    @Deprecated
    public List<String> getTreePath(HttpMessage msg) throws URIException {
        return SessionStructure.getTreePath(Model.getSingleton(), msg);
    }

    // ZAP: Added listeners for contexts changed events.
    // TODO: Might be better structured elsewhere, so maybe just a temporary solution.
    private static List<OnContextsChangedListener> contextsChangedListeners = new LinkedList<>();

    public void addOnContextsChangedListener(OnContextsChangedListener l) {
        contextsChangedListeners.add(l);
    }

    public void removeOnContextsChangedListener(OnContextsChangedListener l) {
        contextsChangedListeners.remove(l);
    }

    /** Listener notified whenever the registered list of contexts changes. */
    public interface OnContextsChangedListener {

        /** Called whenever a new context is created and added. */
        public void contextAdded(Context context);

        /** Called whenever a new context is deleted. */
        public void contextDeleted(Context context);

        /** Called whenever the whole contexts list was changed. */
        public void contextsChanged();
    }
}
