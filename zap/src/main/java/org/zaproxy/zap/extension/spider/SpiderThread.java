/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2010 The ZAP Development Team
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
package org.zaproxy.zap.extension.spider;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.tree.TreeNode;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.ScanListenner;
import org.zaproxy.zap.model.ScanThread;
import org.zaproxy.zap.model.SessionStructure;
import org.zaproxy.zap.model.StructuralNode;
import org.zaproxy.zap.model.TechSet;
import org.zaproxy.zap.users.User;

/**
 * The Class SpiderThread that controls the spidering process on a particular site. Being a
 * ScanThread, it also handles the update of the graphical UI and any other "extension-level"
 * required actions.
 *
 * @deprecated (2.12.0) See the spider add-on in zap-extensions instead.
 */
@Deprecated
public class SpiderThread extends ScanThread implements org.zaproxy.zap.spider.SpiderListener {

    /** Whether the scanning process has stopped (either completed, either by user request). */
    private boolean stopScan = false;

    /** Whether the scanning process is paused. */
    private boolean isPaused = false;

    /** Whether the scanning process is running. */
    private boolean isAlive = false;

    /** The related extension. */
    private ExtensionSpider extension;

    /** The spider. */
    private org.zaproxy.zap.spider.Spider spider = null;

    /** The pending spider listeners which will be added to the Spider as soon at is initialized. */
    private List<org.zaproxy.zap.spider.SpiderListener> pendingSpiderListeners;

    /** The spider done. */
    private int spiderDone = 0;

    /** The spider todo. It will be updated by the "spiderProgress()" method. */
    private int spiderTodo = 1;

    /** The Constant log used for logging. */
    private static final Logger log = LogManager.getLogger(SpiderThread.class);

    /** The just scan in scope. */
    private boolean justScanInScope = false;

    /** The scan children. */
    private boolean scanChildren = false;

    /** The scan context. */
    private Context scanContext = null;

    /** The scan user. */
    private User scanUser = null;

    /** The results model. */
    private SpiderPanelTableModel resultsModel;

    /** The added nodes model, i.e. the names of the nodes that were added to the sites tree. */
    private SpiderPanelTableModel addedNodesModel;

    /** The start uri. */
    private URI startURI = null;

    private org.zaproxy.zap.spider.SpiderParam spiderParams;

    private List<org.zaproxy.zap.spider.parser.SpiderParser> customSpiderParsers = null;

    private List<org.zaproxy.zap.spider.filters.FetchFilter> customFetchFilters = null;

    private List<org.zaproxy.zap.spider.filters.ParseFilter> customParseFilters = null;

    private final String id;

    private Date started;

    private long timeTakenInMs;

    /**
     * Constructs a {@code SpiderThread} with the given data.
     *
     * @param id the ID of the spider, usually a unique integer
     * @param extension the extension to obtain configurations and notify the view
     * @param spiderParams the spider options
     * @param site the name that identifies the target site
     * @param listenner the scan listener
     * @since 2.6.0
     */
    public SpiderThread(
            String id,
            ExtensionSpider extension,
            org.zaproxy.zap.spider.SpiderParam spiderParams,
            String site,
            ScanListenner listenner) {
        super(site, listenner);
        log.debug("Initializing spider thread for site: {}", site);
        this.id = id;
        this.extension = extension;
        this.site = site;
        this.pendingSpiderListeners = new LinkedList<>();
        this.resultsModel = extension.getView() != null ? new SpiderPanelTableModel() : null;
        // This can be used in daemon mode via the API
        this.addedNodesModel = new SpiderPanelTableModel(false);
        this.spiderParams = spiderParams;

        setName("ZAP-SpiderInitThread-" + id);
    }

    @Override
    public void run() {
        try {
            runScan();
        } catch (Exception e) {
            log.error("An error occurred while starting the spider:", e);
            stopScan();
        }
    }

    /** Runs the scan. */
    private void runScan() {
        // Do the scan
        spiderDone = 0;
        started = new Date();
        log.info("Starting spidering scan on {} at {}", site, started);
        startSpider();
        this.isAlive = true;
    }

    @Override
    public void stopScan() {
        if (spider != null) {
            spider.stop();
        }
        stopScan = true;
        isAlive = false;
        this.listenner.scanFinshed(site);
    }

    @Override
    public boolean isStopped() {
        return stopScan;
    }

    @Override
    public boolean isRunning() {
        return isAlive;
    }

    @Override
    public DefaultListModel<?> getList() {
        // Not used, as the SpiderPanel is relying on a TableModel
        return null;
    }

    @Override
    public void pauseScan() {
        if (spider != null) {
            spider.pause();
        }
        this.isPaused = true;
    }

    @Override
    public void resumeScan() {
        if (spider != null) {
            spider.resume();
        }
        this.isPaused = false;
    }

    @Override
    public boolean isPaused() {
        return this.isPaused;
    }

    @Override
    public int getMaximum() {
        return this.spiderDone + this.spiderTodo;
    }

    /** Start spider. */
    private void startSpider() {

        spider =
                new org.zaproxy.zap.spider.Spider(
                        id, extension, spiderParams, extension.getModel(), this.scanContext);

        // Register this thread as a Spider Listener, so it gets notified of events and is able
        // to manipulate the UI accordingly
        spider.addSpiderListener(this);

        // Add the pending listeners
        for (org.zaproxy.zap.spider.SpiderListener l : pendingSpiderListeners) {
            spider.addSpiderListener(l);
        }

        // Add the list of (regex) URIs that should be excluded
        List<String> excludeList = new ArrayList<>();
        excludeList.addAll(extension.getExcludeList());
        excludeList.addAll(extension.getModel().getSession().getExcludeFromSpiderRegexs());
        excludeList.addAll(extension.getModel().getSession().getGlobalExcludeURLRegexs());
        spider.setExcludeList(excludeList);

        // Add seeds accordingly
        addSeeds();

        spider.setScanAsUser(scanUser);

        // Add any custom parsers and filters specified
        if (this.customSpiderParsers != null) {
            for (org.zaproxy.zap.spider.parser.SpiderParser sp : this.customSpiderParsers) {
                spider.addCustomParser(sp);
            }
        }
        if (this.customFetchFilters != null) {
            for (org.zaproxy.zap.spider.filters.FetchFilter ff : this.customFetchFilters) {
                spider.addFetchFilter(ff);
            }
        }
        if (this.customParseFilters != null) {
            for (org.zaproxy.zap.spider.filters.ParseFilter pf : this.customParseFilters) {
                spider.addParseFilter(pf);
            }
        }

        // Start the spider
        spider.start();
    }

    /**
     * Adds the initial seeds, with following constraints:
     *
     * <ul>
     *   <li>If a {@link #scanContext context} is provided:
     *       <ul>
     *         <li>{@link #startURI Start URI}, if in context;
     *         <li>{@link #startNode Start node}, if in context;
     *         <li>All nodes in the context;
     *       </ul>
     *   <li>If spidering just in {@link #justScanInScope scope}:
     *       <ul>
     *         <li>Start URI, if in scope;
     *         <li>Start node, if in scope;
     *         <li>All nodes in scope;
     *       </ul>
     *   <li>If there's no context/scope restriction:
     *       <ul>
     *         <li>Start URI;
     *         <li>Start node, also:
     *             <ul>
     *               <li>Child nodes, if {@link #scanChildren spidering "recursively"}.
     *             </ul>
     *       </ul>
     * </ul>
     *
     * @see #addStartSeeds()
     */
    private void addSeeds() {
        addStartSeeds();

        List<SiteNode> nodesInScope = Collections.emptyList();
        if (this.scanContext != null) {
            log.debug("Adding seed for Scan of all in context {}", scanContext.getName());
            nodesInScope = this.scanContext.getNodesInContextFromSiteTree();
        } else if (justScanInScope) {
            log.debug("Adding seed for Scan of all in scope.");
            nodesInScope = Model.getSingleton().getSession().getNodesInScopeFromSiteTree();
        }

        if (!nodesInScope.isEmpty()) {
            for (SiteNode node : nodesInScope) {
                addSeed(node);
            }
        }
    }

    /**
     * Adds the start seeds ({@link #startNode start node} and {@link #startURI start URI}) to the
     * spider.
     *
     * @see #addSeeds()
     */
    private void addStartSeeds() {
        if (scanContext != null) {
            if (startNode != null && scanContext.isInContext(startNode)) {
                addSeed(startNode);
            }
            if (startURI != null && scanContext.isInContext(startURI.toString())) {
                spider.addSeed(startURI);
            }
            return;
        }

        if (justScanInScope) {
            if (startNode != null && Model.getSingleton().getSession().isInScope(startNode)) {
                addSeed(startNode);
            }
            if (startURI != null
                    && Model.getSingleton().getSession().isInScope(startURI.toString())) {
                spider.addSeed(startURI);
            }
            return;
        }

        if (startNode != null) {
            addSeeds(startNode);
        }
        if (startURI != null) {
            spider.addSeed(startURI);
        }
    }

    /**
     * Adds the given node as seed, if the corresponding message is not an image.
     *
     * @param node the node that will be added as seed
     */
    private void addSeed(SiteNode node) {
        try {
            if (!node.isRoot() && node.getHistoryReference() != null) {
                HttpMessage msg = node.getHistoryReference().getHttpMessage();
                if (!msg.getResponseHeader().isImage()) {
                    spider.addSeed(msg);
                }
            }
        } catch (Exception e) {
            log.error("Error while adding seed for Spider scan: {}", e.getMessage(), e);
        }
    }

    /**
     * Adds as seeds the given node and, if {@link #scanChildren} is {@code true}, the children
     * nodes.
     *
     * @param node the node that will be added as seed and possible the children nodes
     */
    private void addSeeds(SiteNode node) {
        // Add the current node
        addSeed(node);

        // If the "scanChildren" option is enabled, add them
        if (scanChildren) {
            @SuppressWarnings("unchecked")
            Enumeration<TreeNode> en = node.children();
            while (en.hasMoreElements()) {
                SiteNode sn = (SiteNode) en.nextElement();
                addSeeds(sn);
            }
        }
    }

    @Override
    public void spiderComplete(boolean successful) {
        Date finished = new Date();
        log.info("Spider scanning complete: {} on {} at {}", successful, site, finished);
        this.timeTakenInMs = finished.getTime() - started.getTime();
        stopScan = true;
        this.isAlive = false;
        this.listenner.scanFinshed(site);
    }

    /**
     * Returns the time taken in milliseconds. This will be total time taken if the scan has
     * finished or the time taken so far if it is still running.
     *
     * @return the time taken in milliseconds
     * @since 2.11.0
     */
    public long getTimeTakenInMs() {
        if (this.timeTakenInMs > 0) {
            return this.timeTakenInMs;
        }
        if (this.started != null) {
            return System.currentTimeMillis() - started.getTime();
        }
        return 0;
    }

    @Override
    public void foundURI(
            String uri,
            String method,
            org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus status) {
        if (resultsModel != null) {
            addUriToResultsModel(uri, method, status);
        }
    }

    private void addUriToResultsModel(
            final String uri,
            final String method,
            final org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus status) {
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(
                    new Runnable() {

                        @Override
                        public void run() {
                            addUriToResultsModel(uri, method, status);
                        }
                    });
            return;
        }

        // Add the new result
        if (status == org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus.VALID) {
            resultsModel.addScanResult(uri, method, null, false);
        } else {
            resultsModel.addScanResult(
                    uri,
                    method,
                    getStatusLabel(status),
                    status != org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus.SEED);
        }

        // Update the count of found URIs
        extension.getSpiderPanel().updateFoundCount();
    }

    private void addUriToAddedNodesModel(
            final String uri, final String method, final String params) {
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(
                    new Runnable() {

                        @Override
                        public void run() {
                            addUriToAddedNodesModel(uri, method, params);
                        }
                    });
            return;
        }

        // Add the new result
        addedNodesModel.addScanResult(uri, method, null, false);

        if (extension.getView() != null) {
            // Update the count of added nodes
            extension.getSpiderPanel().updateAddedCount();
        }
    }

    private String getStatusLabel(org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus status) {
        switch (status) {
            case SEED:
                return Constant.messages.getString("spider.table.flags.seed");
            case OUT_OF_CONTEXT:
                return Constant.messages.getString("spider.table.flags.outofcontext");
            case OUT_OF_SCOPE:
                return Constant.messages.getString("spider.table.flags.outofscope");
            case ILLEGAL_PROTOCOL:
                return Constant.messages.getString("spider.table.flags.illegalprotocol");
            case USER_RULES:
                return Constant.messages.getString("spider.table.flags.userrules");
            default:
                return status.toString();
        }
    }

    @Override
    public void notifySpiderTaskResult(org.zaproxy.zap.spider.SpiderTaskResult spiderTaskResult) {
        // Add the read message to the Site Map (tree or db structure)
        try {
            HttpMessage msg = spiderTaskResult.getHttpMessage();
            int type =
                    msg.isResponseFromTargetHost()
                            ? HistoryReference.TYPE_SPIDER
                            : HistoryReference.TYPE_SPIDER_TEMPORARY;
            HistoryReference historyRef =
                    new HistoryReference(extension.getModel().getSession(), type, msg);

            if (msg.isResponseFromTargetHost()) {
                addMessageToSitesTree(historyRef, msg);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Adds the given message to the sites tree.
     *
     * @param historyReference the history reference of the message, must not be {@code null}
     * @param message the actual message, must not be {@code null}
     */
    private void addMessageToSitesTree(
            final HistoryReference historyReference, final HttpMessage message) {
        if (View.isInitialised() && !EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(
                    new Runnable() {

                        @Override
                        public void run() {
                            addMessageToSitesTree(historyReference, message);
                        }
                    });
            return;
        }

        StructuralNode node =
                SessionStructure.addPath(Model.getSingleton(), historyReference, message, true);
        if (node != null) {
            try {
                addUriToAddedNodesModel(
                        SessionStructure.getNodeName(Model.getSingleton(), message),
                        message.getRequestHeader().getMethod(),
                        "");
            } catch (URIException e) {
                log.error("Error while adding node to added nodes model: {}", e.getMessage(), e);
            }
        }
    }

    @Override
    public void spiderProgress(
            final int percentageComplete, final int numberCrawled, final int numberToCrawl) {
        this.spiderDone = numberCrawled;
        this.spiderTodo = numberToCrawl;
        this.scanProgress(site, numberCrawled, numberCrawled + numberToCrawl);
    }

    @Override
    public SiteNode getStartNode() {
        return startNode;
    }

    @Override
    public void setStartNode(SiteNode startNode) {
        this.startNode = startNode;
    }

    /**
     * Sets the start uri. This will be used if no startNode is identified.
     *
     * @param startURI the new start uri
     */
    public void setStartURI(URI startURI) {
        this.startURI = startURI;
    }

    @Override
    public void reset() {
        if (resultsModel != null) {
            this.resultsModel.removeAllElements();
        }
        if (addedNodesModel != null) {
            this.addedNodesModel.removeAllElements();
        }
    }

    /**
     * Adds a new spider listener.
     *
     * @param listener the listener
     */
    public void addSpiderListener(org.zaproxy.zap.spider.SpiderListener listener) {
        if (spider != null) {
            this.spider.addSpiderListener(listener);
        } else {
            this.pendingSpiderListeners.add(listener);
        }
    }

    @Override
    public void setJustScanInScope(boolean scanInScope) {
        this.justScanInScope = scanInScope;
    }

    @Override
    public boolean getJustScanInScope() {
        return justScanInScope;
    }

    @Override
    public void setScanChildren(boolean scanChildren) {
        this.scanChildren = scanChildren;
    }

    @Override
    public int getProgress() {
        return this.progress;
    }

    /**
     * Gets the results table model.
     *
     * @return the results table model
     */
    public SpiderPanelTableModel getResultsTableModel() {
        return this.resultsModel;
    }

    public SpiderPanelTableModel getAddedNodesTableModel() {
        return this.addedNodesModel;
    }

    @Override
    public void setScanContext(Context context) {
        this.scanContext = context;
    }

    @Override
    public void setScanAsUser(User user) {
        this.scanUser = user;
    }

    @Override
    public void setTechSet(TechSet techSet) {
        // Ignore
    }

    public void setCustomSpiderParsers(
            List<org.zaproxy.zap.spider.parser.SpiderParser> customSpiderParsers) {
        this.customSpiderParsers = customSpiderParsers;
    }

    public void setCustomFetchFilters(
            List<org.zaproxy.zap.spider.filters.FetchFilter> customFetchFilters) {
        this.customFetchFilters = customFetchFilters;
    }

    public void setCustomParseFilters(
            List<org.zaproxy.zap.spider.filters.ParseFilter> customParseFilters) {
        this.customParseFilters = customParseFilters;
    }

    public int getNumberOfNodesAdded() {
        return getAddedNodesTableModel().getRowCount();
    }
}
