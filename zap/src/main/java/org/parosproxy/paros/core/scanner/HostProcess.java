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
// ZAP: 2011/08/30 Support for scanner levels
// ZAP: 2012/02/18 Dont log errors for temporary hrefs
// ZAP: 2012/03/15 Changed the method getPathRegex to use the class StringBuilder
// instead of StringBuffer and replaced some string concatenations with calls
// to the method append of the class StringBuilder. Removed unnecessary castings
// in the methods scanSingleNode, notifyHostComplete and pluginCompleted. Changed
// the methods processPlugin and pluginCompleted to use Long.valueOf instead of
// creating a new Long.
// ZAP: 2012/04/25 Added @Override annotation to the appropriate method.
// ZAP: 2012/07/30 Issue 43: Added support for Scope
// ZAP: 2012/08/07 Issue 342 Support the HttpSenderListener
// ZAP: 2012/08/07 Renamed Level to AlertThreshold and added support for AttackStrength
// ZAP: 2012/08/31 Enabled control of AttackStrength
// ZAP: 2012/11/22 Issue 421: Cleanly shut down any active scan threads on shutdown
// ZAP: 2013/01/19 Issue 460 Add support for a scan progress dialog
// ZAP: 2013/03/08 Added some debug logging
// ZAP: 2014/01/16 Add support to plugin skipping
// ZAP: 2014/02/21 Issue 1043: Custom active scan dialog
// ZAP: 2014/03/23 Issue 1084: NullPointerException while selecting a node in the "Sites" tab
// ZAP: 2014/04/01 Changed to set a name to created threads.
// ZAP: 2014/06/23 Issue 1241: Active scanner might not report finished state when using host
// scanners
// ZAP: 2014/06/26 Added the possibility to evaluate the current plugin/process progress
// ZAP: 2014/07/07 Issue 389: Enable technology scope for scanners
// ZAP: 2014/08/14 Issue 1291: 407 Proxy Authentication Required while active scanning
// ZAP: 2014/10/24 Issue 1378: Revamp active scan panel
// ZAP: 2014/10/25 Issue 1062: Made it possible to hook into the active scanner from extensions
// ZAP: 2014/11/19 Issue 1412: Manage scan policies
// ZAP: 2015/02/18 Issue 1062: Tidied up extension hooks
// ZAP: 2015/04/02 Issue 321: Support multiple databases and Issue 1582: Low memory option
// ZAP: 2015/04/17 A problem occur when a single node should be scanned because count start from -1
// ZAP: 2015/05/04 Issue 1566: Improve active scan's reported progress
// ZAP: 2015/07/26 Issue 1618: Target Technology Not Honored
// ZAP: 2015/10/29 Issue 2005: Active scanning incorrectly performed on sibling nodes
// ZAP: 2015/11/27 Issue 2086: Report request counts per plugin
// ZAP: 2015/12/16 Prevent HostProcess (and plugins run) from becoming in undefined state
// ZAP: 2016/01/27 Prevent HostProcess from reporting progress higher than 100%
// ZAP: 2016/04/21 Allow scanners to notify of messages sent (and tweak the progress and request
// count of each plugin)
// ZAP: 2016/06/29 Allow to specify and obtain the reason why a scanner was skipped
// ZAP: 2016/07/12 Do not allow techSet to be null
// ZAP: 2016/07/01 Issue 2647 Support a/pscan rule configuration
// ZAP: 2016/09/20 - Reorder statements to prevent (potential) NullPointerException in
// scanSingleNode
//                 - JavaDoc tweaks
// ZAP: 2016/11/14 Restore and deprecate old constructor, to keep binary compatibility
// ZAP: 2016/12/13 Issue 2951:  Support active scan rule and scan max duration
// ZAP: 2016/12/20 Include the name of the user when logging the scan info
// ZAP: 2017/03/20 Improve node enumeration in pre-scan phase.
// ZAP: 2017/03/20 Log the number of messages sent by the scanners, when finished.
// ZAP: 2017/03/25 Ensure messages to be scanned have a response.
// ZAP: 2017/06/07 Scan just one node with AbstractHostPlugin (they apply to the whole host not
// individual messages).
// ZAP: 2017/06/08 Collect messages to be scanned.
// ZAP: 2017/06/15 Initialise the plugin factory immediately after starting the scan.
// ZAP: 2017/06/15 Do not start following plugin if the scanner is paused.
// ZAP: 2017/06/20 Log number of alerts raised by each scanner.
// ZAP: 2017/07/06 Expose plugin stats.
// ZAP: 2017/07/12 Tweak the method used when initialising the PluginFactory.
// ZAP: 2017/07/13 Automatically skip dependent scanners (Issue 3784)
// ZAP: 2017/07/18 Allow to obtain the (total) alert count.
// ZAP: 2017/09/27 Allow to skip scanners by ID and don't allow to skip scanners already
// finished/skipped.
// ZAP: 2017/10/05 Replace usage of Class.newInstance (deprecated in Java 9).
// ZAP: 2017/11/29 Skip plugins if there's nothing to scan.
// ZAP: 2017/12/29 Provide means to validate the redirections.
// ZAP: 2018/01/01 Update initialisation of PluginStats.
// ZAP: 2018/11/14 Log alert count when completed.
// ZAP: 2019/01/19 Handle counting alerts raised by scan (Issue 3929).
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2019/11/09 Ability to filter to active scan (Issue 5278).
// ZAP: 2020/09/23 Add functionality for custom error pages handling (Issue 9).
// ZAP: 2020/10/19 Tweak JavaDoc and init startNodes in the constructor.
// ZAP: 2020/06/30 Fix bug that makes zap test same request twice (Issue 6043).
// ZAP: 2020/11/17 Use new TechSet#getAllTech().
// ZAP: 2020/11/23 Expose getScannerParam() for tests.
// ZAP: 2020/11/26 Use Log4j 2 classes for logging.
// ZAP: 2021/09/14 No longer force single threading if Anti CSRF handling turned on.
// ZAP: 2021/09/30 Pass plugin to PluginStats instead of just the name.
// ZAP: 2022/02/25 Remove code deprecated in 2.5.0
// ZAP: 2022/04/23 Use new HttpSender constructor.
// ZAP: 2022/05/20 Address deprecation warnings with ConnectionParam.
// ZAP: 2022/05/30 Remove deprecation usage.
// ZAP: 2022/09/21 Use format specifiers instead of concatenation when logging.
package org.parosproxy.paros.core.scanner;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.common.ThreadPool;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.parosproxy.paros.network.HttpSender;
import org.zaproxy.zap.extension.alert.ExtensionAlert;
import org.zaproxy.zap.extension.ascan.ScanPolicy;
import org.zaproxy.zap.extension.ascan.filters.FilterResult;
import org.zaproxy.zap.extension.ascan.filters.ScanFilter;
import org.zaproxy.zap.extension.custompages.CustomPage;
import org.zaproxy.zap.extension.ruleconfig.RuleConfig;
import org.zaproxy.zap.extension.ruleconfig.RuleConfigParam;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.SessionStructure;
import org.zaproxy.zap.model.StructuralNode;
import org.zaproxy.zap.model.TechSet;
import org.zaproxy.zap.network.HttpRedirectionValidator;
import org.zaproxy.zap.network.HttpRequestConfig;
import org.zaproxy.zap.users.User;

public class HostProcess implements Runnable {

    private static final Logger log = LogManager.getLogger(HostProcess.class);
    private static final DecimalFormat decimalFormat = new java.text.DecimalFormat("###0.###");

    private List<StructuralNode> startNodes;
    private boolean isStop = false;
    private PluginFactory pluginFactory;
    private ScannerParam scannerParam = null;
    private HttpSender httpSender = null;
    private ThreadPool threadPool = null;
    private Scanner parentScanner = null;
    private String hostAndPort = "";
    private Analyser analyser = null;
    private Kb kb = null;
    private User user = null;
    private TechSet techSet;
    private RuleConfigParam ruleConfigParam;
    private String stopReason = null;
    private Context context;

    /**
     * A {@code Map} from plugin IDs to corresponding {@link PluginStats}.
     *
     * @see #processPlugin(Plugin)
     */
    private final Map<Integer, PluginStats> mapPluginStats = new HashMap<>();

    private long hostProcessStartTime = 0;

    // ZAP: progress related
    private int nodeInScopeCount = 0;
    private int percentage = 0;

    /** The count of requests sent by the {@code HostProcess} itself. */
    private int requestCount;

    /** The count of alerts raised during the scan. */
    private int alertCount;

    /** New alerts found during the scan in the current session. */
    private int newAlertCount = 0;

    /**
     * The ID of the message to be scanned by {@link AbstractHostPlugin}s.
     *
     * <p>As opposed to {@link AbstractAppPlugin}s, {@code AbstractHostPlugin}s just require one
     * message to scan as they run against the host (not individual messages/endpoints).
     *
     * @see #messagesIdsToAppScan
     */
    private int messageIdToHostScan;

    /**
     * The IDs of the messages to be scanned by {@link AbstractAppPlugin}s.
     *
     * @see #messageIdToHostScan
     */
    private List<Integer> messagesIdsToAppScan;

    /**
     * The HTTP request configuration, uses a {@link HttpRedirectionValidator} that ensures the
     * followed redirections are in scan's scope.
     *
     * <p>Lazily initialised.
     *
     * @see #getRedirectRequestConfig()
     * @see #redirectionValidator
     */
    private HttpRequestConfig redirectRequestConfig;

    /**
     * The redirection validator that ensures the followed redirections are in scan's scope.
     *
     * <p>Lazily initialised.
     *
     * @see #getRedirectionValidator()
     * @see #redirectRequestConfig
     */
    private HttpRedirectionValidator redirectionValidator;

    /**
     * Constructs a {@code HostProcess}, with no rules' configurations.
     *
     * @param hostAndPort the host:port value of the site that need to be processed
     * @param parentScanner the scanner instance which instantiated this process
     * @param scannerParam the session scanner parameters
     * @param connectionParam the connection parameters
     * @param scanPolicy the scan policy
     * @deprecated Use {@link #HostProcess(String, Scanner, ScannerParam, ScanPolicy,
     *     RuleConfigParam)} instead. It will be removed in a future version.
     */
    @Deprecated
    public HostProcess(
            String hostAndPort,
            Scanner parentScanner,
            ScannerParam scannerParam,
            org.parosproxy.paros.network.ConnectionParam connectionParam,
            ScanPolicy scanPolicy) {
        this(hostAndPort, parentScanner, scannerParam, connectionParam, scanPolicy, null);
    }

    /**
     * Constructs a {@code HostProcess}.
     *
     * @param hostAndPort the host:port value of the site that need to be processed
     * @param parentScanner the scanner instance which instantiated this process
     * @param scannerParam the session scanner parameters
     * @param connectionParam the connection parameters
     * @param scanPolicy the scan policy
     * @param ruleConfigParam the rules' configurations, might be {@code null}.
     * @since 2.6.0
     * @deprecated (2.12.0) Use {@link #HostProcess(String, Scanner, ScannerParam, ScanPolicy,
     *     RuleConfigParam)} instead.
     */
    @Deprecated
    public HostProcess(
            String hostAndPort,
            Scanner parentScanner,
            ScannerParam scannerParam,
            org.parosproxy.paros.network.ConnectionParam connectionParam,
            ScanPolicy scanPolicy,
            RuleConfigParam ruleConfigParam) {
        this(hostAndPort, parentScanner, scannerParam, scanPolicy, ruleConfigParam);
    }

    /**
     * Constructs a {@code HostProcess}.
     *
     * @param hostAndPort the host:port value of the site that need to be processed
     * @param parentScanner the scanner instance which instantiated this process
     * @param scannerParam the session scanner parameters
     * @param scanPolicy the scan policy
     * @param ruleConfigParam the rules' configurations, might be {@code null}.
     * @since 2.12.0
     */
    public HostProcess(
            String hostAndPort,
            Scanner parentScanner,
            ScannerParam scannerParam,
            ScanPolicy scanPolicy,
            RuleConfigParam ruleConfigParam) {

        super();
        this.hostAndPort = hostAndPort;
        this.parentScanner = parentScanner;
        this.scannerParam = scannerParam;
        this.pluginFactory = scanPolicy.getPluginFactory().clone();
        this.ruleConfigParam = ruleConfigParam;
        this.messageIdToHostScan = -1;
        this.messagesIdsToAppScan = new ArrayList<>();
        this.startNodes = new ArrayList<>();

        httpSender = new HttpSender(HttpSender.ACTIVE_SCANNER_INITIATOR);
        httpSender.setUser(this.user);
        httpSender.setRemoveUserDefinedAuthHeaders(true);

        threadPool = new ThreadPool(scannerParam.getThreadPerHost(), "ZAP-ActiveScanner-");
        this.techSet = TechSet.getAllTech();
    }

    /**
     * Sets the initial starting node.
     *
     * <p>Nodes previously added are removed.
     *
     * @param startNode the start node we should start from
     * @see #addStartNode(StructuralNode)
     */
    public void setStartNode(StructuralNode startNode) {
        this.startNodes.clear();
        this.startNodes.add(startNode);
    }

    /**
     * Adds the given node, to start scanning from.
     *
     * @param startNode a start node.
     * @see #setStartNode(StructuralNode)
     */
    public void addStartNode(StructuralNode startNode) {
        this.startNodes.add(startNode);
    }

    /** Stop the current scanning process */
    public void stop() {
        isStop = true;
        getAnalyser().stop();
    }

    /** Main execution method */
    @Override
    public void run() {
        log.debug("HostProcess.run");

        try {
            hostProcessStartTime = System.currentTimeMillis();

            // Initialise plugin factory to report the state of the plugins ASAP.
            pluginFactory.reset();
            synchronized (mapPluginStats) {
                for (Plugin plugin : pluginFactory.getPending()) {
                    mapPluginStats.put(plugin.getId(), new PluginStats(plugin));
                }
            }

            for (StructuralNode startNode : startNodes) {
                Map<String, Integer> historyIdsToAdd = new LinkedHashMap<>();
                traverse(
                        startNode,
                        true,
                        node -> {
                            if (canScanNode(node)) {
                                int nodeHistoryId = node.getHistoryReference().getHistoryId();
                                if (node.getMethod().equals(HttpRequestHeader.GET)) {
                                    boolean nodeSeen = historyIdsToAdd.containsKey(nodeHash(node));
                                    if (!nodeSeen || !isTemporary(node)) {
                                        historyIdsToAdd.put(nodeHash(node), nodeHistoryId);
                                    }
                                } else {
                                    messagesIdsToAppScan.add(nodeHistoryId);
                                }
                            }
                        });

                messagesIdsToAppScan.addAll(historyIdsToAdd.values());
                getAnalyser().start(startNode);
            }
            nodeInScopeCount = messagesIdsToAppScan.size();

            if (!messagesIdsToAppScan.isEmpty()) {
                messageIdToHostScan = messagesIdsToAppScan.get(0);
            }

            logScanInfo();

            Plugin plugin;

            while (!isStop() && pluginFactory.existPluginToRun()) {
                checkPause();
                if (isStop()) {
                    break;
                }

                plugin = pluginFactory.nextPlugin();

                if (plugin != null) {
                    plugin.setDelayInMs(this.scannerParam.getDelayInMs());
                    plugin.setTechSet(this.techSet);
                    processPlugin(plugin);

                } else {
                    // waiting for dependency - no test ready yet
                    Util.sleep(1000);
                }
            }
            threadPool.waitAllThreadComplete(300000);
        } catch (Exception e) {
            log.error("An error occurred while active scanning:", e);
            stop();
        } finally {
            notifyHostProgress(null);
            notifyHostComplete();
        }
    }

    private String nodeHash(StructuralNode node) {
        String nodeMethod = node.getMethod();
        String nodeURI = node.getURI().getEscapedURI();
        return nodeMethod + nodeURI;
    }

    private boolean isTemporary(StructuralNode node) {
        return node.getHistoryReference().getHistoryType() == HistoryReference.TYPE_TEMPORARY;
    }

    /**
     * Logs information about the scan.
     *
     * <p>It logs the {@link #nodeInScopeCount number of nodes} that will be scanned and the name of
     * the {@link #user}, if any.
     */
    private void logScanInfo() {
        StringBuilder strBuilder = new StringBuilder(150);
        if (nodeInScopeCount != 0) {
            strBuilder.append("Scanning ");
            strBuilder.append(nodeInScopeCount);
            strBuilder.append(" node(s) ");
        } else {
            strBuilder.append("No nodes to scan ");
        }
        if (parentScanner.getJustScanInScope()) {
            strBuilder.append("[just in scope] ");
        }
        strBuilder.append("from ").append(hostAndPort);
        if (user != null) {
            strBuilder.append(" as ");
            strBuilder.append(user.getName());
        }
        if (nodeInScopeCount == 0) {
            strBuilder.append(", skipping all plugins.");
        }
        log.info(strBuilder.toString());
    }

    private void processPlugin(final Plugin plugin) {
        mapPluginStats.get(plugin.getId()).start();

        if (nodeInScopeCount == 0) {
            pluginSkipped(
                    plugin,
                    Constant.messages.getString("ascan.progress.label.skipped.reason.nonodes"));
            pluginCompleted(plugin);
            return;
        } else if (!plugin.targets(techSet)) {
            pluginSkipped(
                    plugin,
                    Constant.messages.getString("ascan.progress.label.skipped.reason.techs"));
            pluginCompleted(plugin);
            return;
        }

        log.info(
                "start host {} | {} strength {} threshold {}",
                hostAndPort,
                plugin.getCodeName(),
                plugin.getAttackStrength(),
                plugin.getAlertThreshold());

        if (plugin instanceof AbstractHostPlugin) {
            checkPause();

            if (isStop() || isSkipped(plugin) || !scanMessage(plugin, messageIdToHostScan)) {
                // Mark the plugin as completed if it was not run so the scan process can continue
                // as expected.
                // The plugin might not be run, for example, if there was an error reading the
                // message form DB.
                pluginCompleted(plugin);
            }

        } else if (plugin instanceof AbstractAppPlugin) {
            try {
                for (int messageId : messagesIdsToAppScan) {
                    checkPause();

                    if (isStop() || isSkipped(plugin)) {
                        return;
                    }

                    scanMessage(plugin, messageId);
                }
                threadPool.waitAllThreadComplete(600000);
            } finally {
                pluginCompleted(plugin);
            }
        }
    }

    private void traverse(StructuralNode node, boolean incRelatedSiblings, TraverseAction action) {
        if (node == null || isStop()) {
            return;
        }

        Set<StructuralNode> parentNodes = new HashSet<>();
        parentNodes.add(node);

        action.apply(node);

        if (parentScanner.scanChildren()) {
            if (incRelatedSiblings) {
                // Also match siblings with the same hierarchic name
                // If we dont do this http://localhost/start might match the GET variant
                // in the Sites tree and miss the hierarchic node.
                // Note that this is only done for the top level
                try {
                    Iterator<StructuralNode> iter = node.getParent().getChildIterator();
                    String nodeName = SessionStructure.getCleanRelativeName(node, false);
                    while (iter.hasNext()) {
                        StructuralNode sibling = iter.next();
                        if (!node.isSameAs(sibling)
                                && nodeName.equals(
                                        SessionStructure.getCleanRelativeName(sibling, false))) {
                            log.debug("traverse: including related sibling {}", sibling.getName());
                            parentNodes.add(sibling);
                        }
                    }
                } catch (DatabaseException e) {
                    // Ignore - if we cant connect to the db there will be plenty of other errors
                    // logged ;)
                }
            }

            for (StructuralNode pNode : parentNodes) {
                Iterator<StructuralNode> iter = pNode.getChildIterator();
                while (iter.hasNext() && !isStop()) {
                    checkPause();

                    try {
                        traverse(iter.next(), false, action);

                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
        }
    }

    protected boolean nodeInScope(String nodeName) {
        return parentScanner.isInScope(nodeName);
    }

    private boolean filterNode(StructuralNode node) {
        for (ScanFilter scanFilter : parentScanner.getScanFilters()) {
            try {
                FilterResult filterResult = scanFilter.isFiltered(node);
                if (filterResult.isFiltered()) {
                    try {
                        HttpMessage msg = node.getHistoryReference().getHttpMessage();
                        parentScanner.notifyFilteredMessage(msg, filterResult.getReason());
                    } catch (HttpMalformedHeaderException | DatabaseException e) {
                        log.warn(
                                "Error while getting httpmessage from history reference: {}",
                                e.getMessage(),
                                e);
                    }

                    log.debug(
                            "Ignoring filtered node: {} Reason: {}",
                            node.getName(),
                            filterResult.getReason());
                    return true;
                }
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }
        return false;
    }

    /**
     * Scans the message with the given ID with the given plugin.
     *
     * <p>It's used a new instance of the given plugin.
     *
     * @param plugin the scanner
     * @param messageId the ID of the message.
     * @return {@code true} if the {@code plugin} was run, {@code false} otherwise.
     */
    private boolean scanMessage(Plugin plugin, int messageId) {
        Plugin test;
        HistoryReference historyReference;
        HttpMessage msg;

        try {
            historyReference = new HistoryReference(messageId, true);
            msg = historyReference.getHttpMessage();
        } catch (HttpMalformedHeaderException | DatabaseException e) {
            log.warn("Failed to read message with ID [{}], cause: {}", messageId, e.getMessage());
            return false;
        }

        try {
            // Ensure the temporary nodes, added automatically to Sites tree, have a response.
            // The scanners might base the logic/attacks on the state of the response (e.g. status
            // code).
            if (msg.getResponseHeader().isEmpty()) {
                msg = msg.cloneRequest();
                if (!obtainResponse(historyReference, msg)) {
                    return false;
                }
            }

            log.debug(
                    "scanSingleNode node plugin={} node={}",
                    plugin.getName(),
                    historyReference.getURI());

            test = plugin.getClass().getDeclaredConstructor().newInstance();
            test.setConfig(plugin.getConfig());
            if (this.ruleConfigParam != null) {
                // Set the configuration rules
                for (RuleConfig rc : this.ruleConfigParam.getAllRuleConfigs()) {
                    test.getConfig().setProperty(rc.getKey(), rc.getValue());
                }
            }
            test.setDelayInMs(plugin.getDelayInMs());
            test.setDefaultAlertThreshold(plugin.getAlertThreshold());
            test.setDefaultAttackStrength(plugin.getAttackStrength());
            test.setTechSet(getTechSet());
            test.init(msg, this);
            notifyHostProgress(
                    plugin.getName() + ": " + msg.getRequestHeader().getURI().toString());

        } catch (Exception e) {
            log.error("{} {}", e.getMessage(), historyReference.getURI(), e);
            return false;
        }

        Thread thread;
        do {
            if (this.isStop()) {
                return false;
            }
            thread = threadPool.getFreeThreadAndRun(test);
            if (thread == null) {
                Util.sleep(200);
            }

        } while (thread == null);

        mapPluginStats.get(plugin.getId()).incProgress();
        return true;
    }

    private boolean obtainResponse(HistoryReference hRef, HttpMessage message) {
        try {
            getHttpSender().sendAndReceive(message);
            notifyNewMessage(message);
            requestCount++;
            return true;
        } catch (IOException e) {
            log.warn(
                    "Failed to obtain the HTTP response for href [id={}, type={}, URL={}]: {}",
                    hRef.getHistoryId(),
                    hRef.getHistoryType(),
                    hRef.getURI(),
                    e.getMessage());
            return false;
        }
    }

    /**
     * Tells whether or not the scanner can scan the given node.
     *
     * <p>A node must not be null, must contain a valid HistoryReference and be in scope.
     *
     * @param node the node to be checked
     * @return {@code true} if the node can be scanned, {@code false} otherwise.
     */
    private boolean canScanNode(StructuralNode node) {
        if (node == null) {
            log.debug("Ignoring null node");
            return false;
        }

        HistoryReference hRef = node.getHistoryReference();
        if (hRef == null) {
            log.debug("Ignoring null history reference for node: {}", node.getName());
            return false;
        }

        if (HistoryReference.TYPE_SCANNER == hRef.getHistoryType()) {
            log.debug(
                    "Ignoring \"scanner\" type href [id={}, URL={}]",
                    hRef.getHistoryId(),
                    hRef.getURI());
            return false;
        }

        if (!nodeInScope(node.getName())) {
            log.debug("Ignoring node not in scope: {}", node.getName());
            return false;
        }

        if (filterNode(node)) {
            return false;
        }

        return true;
    }

    /**
     * Gets the number of messages that will be scanned.
     *
     * @return the number of messages that will be scanned.
     */
    public int getTestTotalCount() {
        return nodeInScopeCount;
    }

    /**
     * ZAP: method to get back the current progress status of a specific plugin
     *
     * @param plugin the plugin we're asking the progress
     * @return the current managed test count
     */
    public int getTestCurrentCount(Plugin plugin) {
        PluginStats pluginStats = mapPluginStats.get(plugin.getId());
        if (pluginStats == null) {
            return 0;
        }
        return pluginStats.getProgress();
    }

    /** @return Returns the httpSender. */
    public HttpSender getHttpSender() {
        return httpSender;
    }

    /**
     * Check if the current host scan has been stopped
     *
     * @return true if the process has been stopped
     */
    public boolean isStop() {
        if (this.scannerParam.getMaxScanDurationInMins() > 0) {
            if (System.currentTimeMillis() - this.hostProcessStartTime
                    > TimeUnit.MINUTES.toMillis(this.scannerParam.getMaxScanDurationInMins())) {
                this.stopReason =
                        Constant.messages.getString("ascan.progress.label.skipped.reason.maxScan");
                this.stop();
            }
        }
        return (isStop || parentScanner.isStop());
    }

    /**
     * Check if the current host scan has been paused
     *
     * @return true if the process has been paused
     */
    public boolean isPaused() {
        return parentScanner.isPaused();
    }

    private void checkPause() {
        while (parentScanner.isPaused() && !isStop()) {
            Util.sleep(500);
        }
    }

    public int getPercentageComplete() {
        return this.percentage;
    }

    private void notifyHostProgress(String msg) {
        if (pluginFactory.totalPluginToRun() == 0) {
            percentage = 100;
        } else {
            int numberRunning = 0;
            double progressRunning = 0;
            for (Plugin plugin : pluginFactory.getRunning()) {
                int scannedNodes = getTestCurrentCount(plugin);
                double pluginPercentage = (scannedNodes * 100.0) / getTestTotalCount();
                if (pluginPercentage >= 100) {
                    // More nodes are being scanned that the ones enumerated at the beginning...
                    // Update global count and...
                    nodeInScopeCount = scannedNodes;
                    // make sure not return 100 (or more).
                    pluginPercentage = 99;
                }
                progressRunning += pluginPercentage;
                numberRunning++;
            }

            int avgRunning = (int) (progressRunning / numberRunning);
            percentage =
                    ((100 * pluginFactory.totalPluginCompleted()) + avgRunning)
                            / pluginFactory.totalPluginToRun();
        }

        parentScanner.notifyHostProgress(hostAndPort, msg, percentage);
    }

    private void notifyHostComplete() {
        long diffTimeMillis = System.currentTimeMillis() - hostProcessStartTime;
        String diffTimeString = decimalFormat.format(diffTimeMillis / 1000.0) + "s";
        log.info(
                "completed host {} in {} with {} alert(s) raised.",
                hostAndPort,
                diffTimeString,
                getAlertCount());
        parentScanner.notifyHostComplete(hostAndPort);
    }

    /**
     * Notifies interested parties that a new message was sent (and received).
     *
     * <p>{@link Plugin Plugins} should call {@link #notifyNewMessage(Plugin)} or {@link
     * #notifyNewMessage(Plugin, HttpMessage)}, instead.
     *
     * @param msg the new HTTP message
     * @since 1.2.0
     */
    public void notifyNewMessage(HttpMessage msg) {
        parentScanner.notifyNewMessage(msg);
    }

    /**
     * Notifies that the given {@code plugin} sent (and received) the given HTTP message.
     *
     * @param plugin the plugin that sent the message
     * @param message the message sent
     * @throws IllegalArgumentException if the given {@code plugin} is {@code null}.
     * @since 2.5.0
     * @see #notifyNewMessage(Plugin)
     */
    public void notifyNewMessage(Plugin plugin, HttpMessage message) {
        parentScanner.notifyNewMessage(message);
        notifyNewMessage(plugin);
    }

    /**
     * Notifies that the given {@code plugin} sent (and received) a non-HTTP message.
     *
     * <p>The call to this method has no effect if there's no {@code Plugin} with the given ID (or,
     * it was not yet started).
     *
     * @param plugin the plugin that sent a non-HTTP message
     * @throws IllegalArgumentException if the given parameter is {@code null}.
     * @since 2.5.0
     * @see #notifyNewMessage(Plugin, HttpMessage)
     */
    public void notifyNewMessage(Plugin plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Parameter plugin must not be null.");
        }

        PluginStats pluginStats = mapPluginStats.get(plugin.getId());
        if (pluginStats != null) {
            pluginStats.incMessageCount();
        }
    }

    public void alertFound(Alert alert) {
        ExtensionAlert extensionAlertRef =
                Control.getSingleton().getExtensionLoader().getExtension(ExtensionAlert.class);
        if (extensionAlertRef.isNewAlert(alert)) {
            newAlertCount++;
        }

        parentScanner.notifyAlertFound(alert);

        PluginStats pluginStats = mapPluginStats.get(alert.getPluginId());
        if (pluginStats != null) {
            pluginStats.incAlertCount();
        }
        alertCount++;
    }

    public int getNewAlertCount() {
        return newAlertCount;
    }

    /**
     * Gets the alert count.
     *
     * @return the alert count.
     * @since 2.7.0
     */
    public int getAlertCount() {
        return alertCount;
    }

    /**
     * Give back the current process's Analyzer
     *
     * @return the HTTP analyzer
     */
    public Analyser getAnalyser() {
        if (analyser == null) {
            analyser = new Analyser(getHttpSender(), this);
        }

        return analyser;
    }

    /**
     * Gets the HTTP request configuration that ensures the followed redirections are in scan's
     * scope.
     *
     * @return the HTTP request configuration, never {@code null}.
     * @since 2.8.0
     * @see #getRedirectionValidator()
     */
    HttpRequestConfig getRedirectRequestConfig() {
        if (redirectRequestConfig == null) {
            redirectRequestConfig =
                    HttpRequestConfig.builder()
                            .setRedirectionValidator(getRedirectionValidator())
                            .build();
        }
        return redirectRequestConfig;
    }

    /**
     * Gets the redirection validator that ensures the followed redirections are in scan's scope.
     *
     * @return the redirection validator, never {@code null}.
     * @since 2.8.0
     * @see #getRedirectRequestConfig()
     */
    HttpRedirectionValidator getRedirectionValidator() {
        if (redirectionValidator == null) {
            redirectionValidator =
                    redirection -> {
                        if (!nodeInScope(redirection.getEscapedURI())) {
                            log.debug("Skipping redirection out of scan's scope: {}", redirection);
                            return false;
                        }
                        return true;
                    };
        }
        return redirectionValidator;
    }

    public boolean handleAntiCsrfTokens() {
        return this.scannerParam.getHandleAntiCSRFTokens();
    }

    /**
     * Skips the given plugin.
     *
     * <p><strong>Note:</strong> Whenever possible callers should use {@link #pluginSkipped(Plugin,
     * String)} instead.
     *
     * @param plugin the plugin that will be skipped, must not be {@code null}
     * @since 2.4.0
     */
    public void pluginSkipped(Plugin plugin) {
        pluginSkipped(plugin, null);
    }

    /**
     * Skips the plugin with the given ID with the given {@code reason}.
     *
     * <p>Ideally the {@code reason} should be internationalised as it is shown in the GUI.
     *
     * @param pluginId the ID of the plugin that will be skipped.
     * @param reason the reason why the plugin was skipped, might be {@code null}.
     * @since 2.7.0
     * @see #pluginSkipped(Plugin, String)
     */
    public void pluginSkipped(int pluginId, String reason) {
        Plugin plugin = pluginFactory.getPlugin(pluginId);
        if (plugin == null) {
            return;
        }

        pluginSkipped(plugin, reason);
    }

    /**
     * Skips the given {@code plugin} with the given {@code reason}.
     *
     * <p>Ideally the {@code reason} should be internationalised as it is shown in the GUI.
     *
     * @param plugin the plugin that will be skipped, must not be {@code null}
     * @param reason the reason why the plugin was skipped, might be {@code null}
     * @since 2.6.0
     */
    public void pluginSkipped(Plugin plugin, String reason) {
        if (isStop()) {
            return;
        }

        PluginStats pluginStats = mapPluginStats.get(plugin.getId());
        if (pluginStats == null
                || pluginStats.isSkipped()
                || pluginFactory.getCompleted().contains(plugin)) {
            return;
        }

        pluginStats.skip();
        pluginStats.setSkippedReason(reason);

        for (Plugin dependent : pluginFactory.getDependentPlugins(plugin)) {
            pluginStats = mapPluginStats.get(dependent.getId());
            if (pluginStats != null
                    && !pluginStats.isSkipped()
                    && !pluginFactory.getCompleted().contains(dependent)) {
                pluginStats.skip();
                pluginStats.setSkippedReason(
                        Constant.messages.getString(
                                "ascan.progress.label.skipped.reason.dependency"));
            }
        }
    }

    /**
     * Tells whether or not the given {@code plugin} was skipped (either programmatically or by the
     * user).
     *
     * @param plugin the plugin that will be checked
     * @return {@code true} if plugin was skipped, {@code false} otherwise
     * @since 2.4.0
     * @see #getSkippedReason(Plugin)
     */
    public boolean isSkipped(Plugin plugin) {
        PluginStats pluginStats = mapPluginStats.get(plugin.getId());

        if (pluginStats != null && pluginStats.isSkipped()) {
            return true;
        }

        if (plugin.getTimeFinished() == null && stopReason != null) {
            this.pluginSkipped(plugin, stopReason);
            return true;

        } else if (this.scannerParam.getMaxRuleDurationInMins() > 0
                && plugin.getTimeStarted() != null) {
            long endtime = System.currentTimeMillis();
            if (plugin.getTimeFinished() != null) {
                endtime = plugin.getTimeFinished().getTime();
            }
            if (endtime - plugin.getTimeStarted().getTime()
                    > TimeUnit.MINUTES.toMillis(this.scannerParam.getMaxRuleDurationInMins())) {
                this.pluginSkipped(
                        plugin,
                        Constant.messages.getString("ascan.progress.label.skipped.reason.maxRule"));
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the reason why the given plugin was skipped.
     *
     * @param plugin the plugin that will be checked
     * @return the reason why the given plugin was skipped, might be {@code null} if not skipped or
     *     there's no reason
     * @since 2.6.0
     * @see #isSkipped(Plugin)
     */
    public String getSkippedReason(Plugin plugin) {
        PluginStats pluginStats = mapPluginStats.get(plugin.getId());
        if (pluginStats == null) {
            return stopReason;
        }
        return pluginStats.getSkippedReason();
    }

    /**
     * Complete the current plugin and update statistics
     *
     * @param plugin the plugin that need to be marked as completed
     */
    void pluginCompleted(Plugin plugin) {
        PluginStats pluginStats = mapPluginStats.get(plugin.getId());
        if (pluginStats == null) {
            // Plugin was not processed
            return;
        }
        pluginStats.stopped();

        StringBuilder sb = new StringBuilder();
        if (isStop()) {
            sb.append("stopped host/plugin ");

            // ZAP: added skipping notifications
        } else if (pluginStats.isSkipped()) {
            sb.append("skipped plugin ");
            String reason = pluginStats.getSkippedReason();
            if (reason != null) {
                sb.append('[').append(reason).append("] ");
            }
        } else {
            sb.append("completed host/plugin ");
        }

        sb.append(hostAndPort).append(" | ").append(plugin.getCodeName());
        String diffTimeString = decimalFormat.format(pluginStats.getTotalTime() / 1000.0);
        sb.append(" in ").append(diffTimeString).append('s');
        sb.append(" with ").append(pluginStats.getMessageCount()).append(" message(s) sent");
        sb.append(" and ").append(pluginStats.getAlertCount()).append(" alert(s) raised.");

        // Probably too verbose evaluate 4 the future
        log.info(sb.toString());

        pluginFactory.setRunningPluginCompleted(plugin);
        notifyHostProgress(null);

        // ZAP: update progress as finished
        pluginStats.setProgress(nodeInScopeCount);
    }

    /**
     * Gets the knowledge base of the current scan.
     *
     * @return the knowledge base of the current scan, never {@code null}.
     */
    Kb getKb() {
        if (kb == null) {
            kb = new Kb();
        }

        return kb;
    }

    /**
     * Gets the scanner parameters.
     *
     * <p><strong>Note:</strong> Not part of the public API.
     *
     * @return the scanner parameters.
     */
    public ScannerParam getScannerParam() {
        return scannerParam;
    }

    public List<Plugin> getPending() {
        return this.pluginFactory.getPending();
    }

    public List<Plugin> getRunning() {
        return this.pluginFactory.getRunning();
    }

    public List<Plugin> getCompleted() {
        return this.pluginFactory.getCompleted();
    }

    /**
     * Set the user to scan as. If null then the current session will be used.
     *
     * @param user the user to scan as
     */
    public void setUser(User user) {
        this.user = user;
        if (httpSender != null) {
            httpSender.setUser(user);
        }
    }

    /**
     * Gets the technologies to be used in the scan.
     *
     * @return the technologies, never {@code null} (since 2.6.0)
     * @since 2.4.0
     */
    public TechSet getTechSet() {
        return techSet;
    }

    /**
     * Sets the technologies to be used in the scan.
     *
     * @param techSet the technologies to be used during the scan
     * @since 2.4.0
     * @throws IllegalArgumentException (since 2.6.0) if the given parameter is {@code null}.
     */
    public void setTechSet(TechSet techSet) {
        if (techSet == null) {
            throw new IllegalArgumentException("Parameter techSet must not be null.");
        }
        this.techSet = techSet;
    }

    /**
     * ZAP: abstract plugin will call this method in order to invoke any extensions that have hooked
     * into the active scanner
     *
     * @param msg the message being scanned
     * @param plugin the plugin being run
     */
    protected synchronized void performScannerHookBeforeScan(
            HttpMessage msg, AbstractPlugin plugin) {
        Iterator<ScannerHook> iter = this.parentScanner.getScannerHooks().iterator();
        while (iter.hasNext()) {
            ScannerHook hook = iter.next();
            if (hook != null) {
                try {
                    hook.beforeScan(msg, plugin, this.parentScanner);
                } catch (Exception e) {
                    log.info(
                            "An exception occurred while trying to call beforeScan(msg, plugin) for one of the ScannerHooks: {}",
                            e.getMessage(),
                            e);
                }
            }
        }
    }

    /**
     * ZAP: abstract plugin will call this method in order to invoke any extensions that have hooked
     * into the active scanner
     *
     * @param msg the message being scanned
     * @param plugin the plugin being run
     */
    protected synchronized void performScannerHookAfterScan(
            HttpMessage msg, AbstractPlugin plugin) {
        Iterator<ScannerHook> iter = this.parentScanner.getScannerHooks().iterator();
        while (iter.hasNext()) {
            ScannerHook hook = iter.next();
            if (hook != null) {
                try {
                    hook.afterScan(msg, plugin, this.parentScanner);
                } catch (Exception e) {
                    log.info(
                            "An exception occurred while trying to call afterScan(msg, plugin) for one of the ScannerHooks: {}",
                            e.getMessage(),
                            e);
                }
            }
        }
    }

    public String getHostAndPort() {
        return this.hostAndPort;
    }

    /**
     * Gets the request count of the plugin with the given ID.
     *
     * @param pluginId the ID of the plugin
     * @return the request count
     * @since 2.4.3
     * @see #getRequestCount()
     */
    public int getPluginRequestCount(int pluginId) {
        PluginStats pluginStats = mapPluginStats.get(pluginId);
        if (pluginStats != null) {
            return pluginStats.getMessageCount();
        }
        return 0;
    }

    /**
     * Gets the count of requests sent (and received) by all {@code Plugin}s and the {@code
     * Analyser}.
     *
     * @return the count of request sent
     * @since 2.5.0
     * @see #getPluginRequestCount(int)
     * @see #getAnalyser()
     */
    public int getRequestCount() {
        synchronized (mapPluginStats) {
            int count = requestCount + getAnalyser().getRequestCount();
            for (PluginStats stats : mapPluginStats.values()) {
                count += stats.getMessageCount();
            }
            return count;
        }
    }

    /**
     * Gets the stats of the {@code Plugin} with the given ID.
     *
     * @param pluginId the ID of the plugin.
     * @return the stats of the plugin, or {@code null} if not found.
     * @since 2.7.0
     */
    public PluginStats getPluginStats(int pluginId) {
        synchronized (mapPluginStats) {
            return mapPluginStats.get(pluginId);
        }
    }

    /**
     * Tells whether or not the message matches the specific {@code CustomPage.Type}. (Does not
     * leverage {@code Analyzer}).
     *
     * @param msg the message that will be checked
     * @param cpType the custom page type to be checked
     * @return {@code true} if the message matches, {@code false} otherwise
     * @since 2.10.0
     */
    protected boolean isCustomPage(HttpMessage msg, CustomPage.Type cpType) {
        if (getContext() != null) {
            return getContext().isCustomPage(msg, cpType);
        }
        return false;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * An action to be executed for each node traversed during the scan.
     *
     * @see #apply(StructuralNode)
     */
    @FunctionalInterface
    private interface TraverseAction {

        /**
         * Applies an action to the node traversed.
         *
         * @param node the node being traversed
         */
        void apply(StructuralNode node);
    }
}
