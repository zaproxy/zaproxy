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
// ZAP: 2014/06/23 Issue 1241: Active scanner might not report finished state when using host scanners
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
// ZAP: 2016/04/21 Allow scanners to notify of messages sent (and tweak the progress and request count of each plugin)
// ZAP: 2016/06/29 Allow to specify and obtain the reason why a scanner was skipped
// ZAP: 2016/07/12 Do not allow techSet to be null
// ZAP: 2016/07/01 Issue 2647 Support a/pscan rule configuration 
// ZAP: 2016/09/20 - Reorder statements to prevent (potential) NullPointerException in scanSingleNode
//                 - JavaDoc tweaks

package org.parosproxy.paros.core.scanner;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.common.ThreadPool;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.ConnectionParam;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpSender;
import org.zaproxy.zap.extension.ascan.ScanPolicy;
import org.zaproxy.zap.extension.ruleconfig.RuleConfig;
import org.zaproxy.zap.extension.ruleconfig.RuleConfigParam;
import org.zaproxy.zap.model.SessionStructure;
import org.zaproxy.zap.model.StructuralNode;
import org.zaproxy.zap.model.TechSet;
import org.zaproxy.zap.users.User;

public class HostProcess implements Runnable {

    private static final Logger log = Logger.getLogger(HostProcess.class);
    private static final DecimalFormat decimalFormat = new java.text.DecimalFormat("###0.###");
    
    private List<StructuralNode> startNodes = null;
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
    
    /**
     * Constructs a {@code HostProcess}.
     * 
     * @param hostAndPort the host:port value of the site that need to be processed
     * @param parentScanner the scanner instance which instantiated this process
     * @param scannerParam the session scanner parameters
     * @param connectionParam the connection parameters
     * @param scanPolicy the scan policy
     * @param ruleConfigParam the rules' configurations
     */
    public HostProcess(String hostAndPort, Scanner parentScanner, 
    		ScannerParam scannerParam, ConnectionParam connectionParam, 
    		ScanPolicy scanPolicy, RuleConfigParam ruleConfigParam) {
        
        super();
        this.hostAndPort = hostAndPort;
        this.parentScanner = parentScanner;
        this.scannerParam = scannerParam;
		this.pluginFactory = scanPolicy.getPluginFactory().clone();
		this.ruleConfigParam = ruleConfigParam;
		
        httpSender = new HttpSender(connectionParam, true, HttpSender.ACTIVE_SCANNER_INITIATOR);
        httpSender.setUser(this.user);
        httpSender.setRemoveUserDefinedAuthHeaders(true);
        
        int maxNumberOfThreads;
        if (scannerParam.getHandleAntiCSRFTokens()) {
            // Single thread if handling anti CSRF tokens, otherwise token requests might get out of step
            maxNumberOfThreads = 1;
        
        } else {
            maxNumberOfThreads = scannerParam.getThreadPerHost();
        }
        
        threadPool = new ThreadPool(maxNumberOfThreads, "ZAP-ActiveScanner-");
        this.techSet = TechSet.AllTech;
    }

    /**
     * Set the initial starting node.
     * Should be set after the HostProcess initialization
     * @param startNode the start node we should start from
     */
    public void setStartNode(StructuralNode startNode) {
        this.startNodes = new ArrayList<StructuralNode>(); 
        this.startNodes.add(startNode);
    }

    public void addStartNode(StructuralNode startNode) {
    	if (this.startNodes == null) {
            this.startNodes = new ArrayList<StructuralNode>(); 
    	}
        this.startNodes.add(startNode);
    }

    /**
     * Stop the current scanning process
     */
    public void stop() {
        isStop = true;
        getAnalyser().stop();
    }

    /**
     * Main execution method
     */
    @Override
    public void run() {
        log.debug("HostProcess.run");

        try {
            TraverseCounter counter = new TraverseCounter();
            hostProcessStartTime = System.currentTimeMillis();
            for (StructuralNode node : startNodes) {
    	        // ZAP: before all get back the size of this scan
    	        traverse(node, true, counter);
    	        // ZAP: begin to analyze the scope
    	        getAnalyser().start(node);
            }
            nodeInScopeCount = counter.getCount();

            log.info("Scanning " + nodeInScopeCount + " node(s) from " + hostAndPort);
            
            Plugin plugin;
            
            while (!isStop() && pluginFactory.existPluginToRun()) {
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
            getHttpSender().shutdown();
        }
    }

    private void processPlugin(final Plugin plugin) {
        synchronized (mapPluginStats) {
            mapPluginStats.put(plugin.getId(), new PluginStats());
        }

        if (!plugin.targets(techSet)) {
            pluginSkipped(plugin, Constant.messages.getString("ascan.progress.label.skipped.reason.techs"));
            pluginCompleted(plugin);
            return;
        }

        log.info("start host " + hostAndPort + " | " + plugin.getCodeName()
                + " strength " + plugin.getAttackStrength() + " threshold " + plugin.getAlertThreshold());
        
        for (StructuralNode startNode : startNodes) {
	        if (plugin instanceof AbstractHostPlugin) {
	            if (!scanSingleNode(plugin, startNode)) {
	                // Mark the plugin as as completed if it was not run so the scan process can continue as expected.
	                // The plugin might not be run if the startNode: is not in scope, is explicitly excluded, ...
	                pluginCompleted(plugin);
	            }
	            
	        } else if (plugin instanceof AbstractAppPlugin) {
	            try {
	                traverse(startNode, true, new TraverseAction() {

                        @Override
                        public void apply(StructuralNode node) {
                            log.debug("traverse: plugin=" + plugin.getName() + " url=" + node.getName());
                            scanSingleNode(plugin, node);
                        }

                        @Override
                        public boolean isStopTraversing() {
                            return isSkipped(plugin);
                        }
                    });
	                threadPool.waitAllThreadComplete(600000);
	            } finally {
	                pluginCompleted(plugin);
	            }
	        }
        }
    }

    private void traverse(StructuralNode node, TraverseAction action) {
        this.traverse(node, false, action);
    }

    private void traverse(StructuralNode node, boolean incRelatedSiblings, TraverseAction action) {
        if (node == null || isStop()) {
            return;
        }

        Set<StructuralNode> parentNodes = new HashSet<>();
        parentNodes.add(node);

        action.apply(node);

        if (!action.isStopTraversing() && parentScanner.scanChildren()) {
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
    					if (! node.isSameAs(sibling) && 
    							nodeName.equals(
    									SessionStructure.getCleanRelativeName(sibling, false))) {
    				        log.debug("traverse: including related sibling " + sibling.getName());
    				        parentNodes.add(sibling);
    					}
    				}
    			} catch (DatabaseException e) {
    				// Ignore - if we cant connect to the db there will be plenty of other errors logged ;)
    			}
            }
        	
        	for (StructuralNode pNode : parentNodes) {
	        	Iterator<StructuralNode> iter = pNode.getChildIterator();
	        	while (iter.hasNext() && !isStop() && !action.isStopTraversing()) {
	        		StructuralNode child = iter.next();
	                // ZAP: Implement pause and resume
	                while (parentScanner.isPaused() && !isStop()) {
	                    Util.sleep(500);
	                }
	
	                try {
	                    traverse(child, action);
	                    
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

    /**
     * Create new plugin instance and run against a node
     *
     * @param plugin the scanner
     * @param node the node to scan, ignored if {@code null}.
     * @return {@code true} if the {@code plugin} was run, {@code false} otherwise.
     */
    private boolean scanSingleNode(Plugin plugin, StructuralNode node) {
        Thread thread;
        Plugin test;
        HttpMessage msg;

        // do not poll for isStop here to allow every plugin to run but terminate immediately.
        //if (isStop()) return;

        if (node == null || node.getHistoryReference() == null) {
            log.debug("scanSingleNode node or href null, returning: node=" + node);
            return false;
        }
        
        if (HistoryReference.TYPE_SCANNER == node.getHistoryReference().getHistoryType()) {
            log.debug("Ignoring \"scanner\" type href");
            return false;
        }

        if (!nodeInScope(node.getName())) {
            log.debug("scanSingleNode node not in scope");
            return false;
        }

        try {
            
            msg = node.getHistoryReference().getHttpMessage();

            if (msg == null) {
                // Likely to be a temporary node
                log.debug("scanSingleNode msg null");
                return false;
            }

            log.debug("scanSingleNode node plugin=" + plugin.getName() + " node=" + node.getName());

            test = plugin.getClass().newInstance();
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
            notifyHostProgress(plugin.getName() + ": " + msg.getRequestHeader().getURI().toString());

        } catch (Exception e) {
            log.error(e.getMessage() + " " + node.getName(), e);
            return false;
        }

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

    /**
     * ZAP: method to get back the number of tests that need to be performed
     * @return the number of tests that need to be executed for this Scanner
     */
    public int getTestTotalCount() {
        return nodeInScopeCount;
    }

    /**
     * ZAP: method to get back the current progress status of a specific plugin
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

    /**
     * @deprecated (2.5.0) No longer used/needed, Plugin's progress is automatically updated/maintained by
     *             {@code HostProcess}.
     * @param plugin unused
     * @param value unused
     */
    @Deprecated
    public void setTestCurrentCount(Plugin plugin, int value) {        
        // No longer used.
    }
    
    /**
     * @return Returns the httpSender.
     */
    public HttpSender getHttpSender() {
        return httpSender;
    }

    /**
     * Check if the current host scan has been stopped
     * @return true if the process has been stopped
     */
    public boolean isStop() {
        return (isStop || parentScanner.isStop());
    }
    
    /**
     * Check if the current host scan has been paused
     * @return true if the process has been paused
     */
    public boolean isPaused() {
        return parentScanner.isPaused();
    }
    
    public int getPercentageComplete () {
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
            percentage = ((100 * pluginFactory.totalPluginCompleted()) + avgRunning) / pluginFactory.totalPluginToRun();
        }
        
        parentScanner.notifyHostProgress(hostAndPort, msg, percentage);
    }

    private void notifyHostComplete() {
        long diffTimeMillis = System.currentTimeMillis() - hostProcessStartTime;
        String diffTimeString = decimalFormat.format(diffTimeMillis / 1000.0) + "s";
        log.info("completed host " + hostAndPort + " in " + diffTimeString);
        parentScanner.notifyHostComplete(hostAndPort);
    }

    /**
     * Notifies interested parties that a new message was sent (and received).
     * <p>
     * {@link Plugin Plugins} should call {@link #notifyNewMessage(Plugin)} or {@link #notifyNewMessage(Plugin, HttpMessage)},
     * instead.
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
     * <p>
     * The call to this method has no effect if there's no {@code Plugin} with the given ID (or, it was not yet started).
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
        parentScanner.notifyAlertFound(alert);
    }

    /**
     * Give back the current process's Analyzer
     * @return the HTTP analyzer
     */
    public Analyser getAnalyser() {
        if (analyser == null) {
            analyser = new Analyser(getHttpSender(), this);
        }
        
        return analyser;
    }

    public boolean handleAntiCsrfTokens() {
        return this.scannerParam.getHandleAntiCSRFTokens();
    }

    /**
     * Skips the given plugin.
     * <p>
     * <strong>Note:</strong> Whenever possible callers should use {@link #pluginSkipped(Plugin, String)} instead.
     * 
     * @param plugin the plugin that will be skipped, must not be {@code null}
     * @since 2.4.0
     */
    public void pluginSkipped(Plugin plugin) {
        pluginSkipped(plugin, null);
    }

    /**
     * Skips the given {@code plugin} with the given {@code reason}.
     * <p>
     * Ideally the {@code reason} should be internationalised as it is shown in the GUI.
     *
     * @param plugin the plugin that will be skipped, must not be {@code null}
     * @param reason the reason why the plugin was skipped, might be {@code null}
     * @since TODO add version
     */
    public void pluginSkipped(Plugin plugin, String reason) {
        PluginStats pluginStats = mapPluginStats.get(plugin.getId());
        if (pluginStats == null) {
            return;
        }

        pluginStats.skipped();
        pluginStats.setSkippedReason(reason);
    }

    /**
     * Tells whether or not the given {@code plugin} was skipped (either programmatically or by the user).
     * 
     * @param plugin the plugin that will be checked
     * @return {@code true} if plugin was skipped, {@code false} otherwise
     * @since 2.4.0
     * @see #getSkippedReason(Plugin)
     */
    public boolean isSkipped(Plugin plugin) {
        PluginStats pluginStats = mapPluginStats.get(plugin.getId());
        if (pluginStats == null) {
            return false;
        }
        return pluginStats.isSkipped();
    }

    /**
     * Gets the reason why the given plugin was skipped.
     * 
     * @param plugin the plugin that will be checked
     * @return the reason why the given plugin was skipped, might be {@code null} if not skipped or there's no reason
     * @since TODO add version
     * @see #isSkipped(Plugin)
     */
    public String getSkippedReason(Plugin plugin) {
        PluginStats pluginStats = mapPluginStats.get(plugin.getId());
        if (pluginStats == null) {
            return null;
        }
        return pluginStats.getSkippedReason();
    }
    
    /**
     * Complete the current plugin and update statistics
     * @param plugin the plugin that need to be marked as completed
     */
    void pluginCompleted(Plugin plugin) {
        PluginStats pluginStats = mapPluginStats.get(plugin.getId());
        if (pluginStats == null) {
            // Plugin was not processed
            return;
        }

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
        long startTimeMillis = pluginStats.getStartTime();
        long diffTimeMillis = System.currentTimeMillis() - startTimeMillis;
        String diffTimeString = decimalFormat.format(diffTimeMillis / 1000.0) + "s";
        sb.append(" in ").append(diffTimeString);

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

    protected ScannerParam getScannerParam() {
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
	 * @return the technologies, never {@code null} (since TODO add version)
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
	 * @throws IllegalArgumentException (since TODO add version) if the given parameter is {@code null}.
	 */
	public void setTechSet(TechSet techSet) {
		if (techSet == null) {
			throw new IllegalArgumentException("Parameter techSet must not be null.");
		}
		this.techSet = techSet;
	}
	
	/** 
     * ZAP: abstract plugin will call this method in order to invoke any extensions that have hooked into the active scanner
     * @param msg the message being scanned
     * @param plugin the plugin being run
     */
    protected synchronized void performScannerHookBeforeScan(HttpMessage msg, AbstractPlugin plugin) {
		Iterator<ScannerHook> iter = this.parentScanner.getScannerHooks().iterator();
		while(iter.hasNext()){
			ScannerHook hook = iter.next();
			if(hook != null) {
				try {
					hook.beforeScan(msg, plugin, this.parentScanner);
				} catch (Exception e) {
					log.info("An exception occurred while trying to call beforeScan(msg, plugin) for one of the ScannerHooks: " + e.getMessage(), e); 
				} 
			}
		}
    }
    
    /** 
     * ZAP: abstract plugin will call this method in order to invoke any extensions that have hooked into the active scanner 
     * @param msg the message being scanned
     * @param plugin the plugin being run
     */
    protected synchronized void performScannerHookAfterScan(HttpMessage msg, AbstractPlugin plugin) {
		Iterator<ScannerHook> iter = this.parentScanner.getScannerHooks().iterator();
		while(iter.hasNext()){
			ScannerHook hook = iter.next();
			if(hook != null) {
				try {
					hook.afterScan(msg, plugin, this.parentScanner);
				} catch (Exception e) {
					log.info("An exception occurred while trying to call afterScan(msg, plugin) for one of the ScannerHooks: " + e.getMessage(), e);
				}
			}
		}
    }
	
	public String getHostAndPort() {
		return this.hostAndPort;
	}
	
	/**
	 * @deprecated (2.5.0) No longer used/needed, Plugin's request count is automatically updated/maintained by
	 *             {@code HostProcess}.
     * @param pluginId the ID of the plugin
     * @param reqCount the number of requests sent
	 */
	@Deprecated
	public void setPluginRequestCount(int pluginId, int reqCount) {
		// No longer used.
	}
	
	/**
	 * Gets the request count of the plugin with the give ID.
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
     * Gets the count of requests sent (and received) by all {@code Plugin}s and the {@code Analyser}.
     *
     * @return the count of request sent
     * @since 2.5.0
     * @see #getPluginRequestCount(int)
     * @see #getAnalyser()
     */
    public int getRequestCount() {
        synchronized (mapPluginStats) {
            int count = getAnalyser().getRequestCount();
            for (PluginStats stats : mapPluginStats.values()) {
                count += stats.getMessageCount();
            }
            return count;
        }
    }

    /**
     * An action to be executed for each node traversed during the scan.
     *
     * @see #apply(StructuralNode)
     */
    private interface TraverseAction {

        /**
         * Applies an action to the node traversed.
         *
         * @param node the node being traversed
         */
        void apply(StructuralNode node);

        /**
         * Called after traversing a node, to know if the traversing should be stopped.
         *
         * @return {@code true} if the traversing should be stopped, {@code false} otherwise
         */
        boolean isStopTraversing();

    }

    /**
     * A {@code TraverseAction} that counts the nodes traversed.
     * 
     * @see #getCount()
     */
    private static class TraverseCounter implements TraverseAction {

        private int count;

        /**
         * Returns the number of nodes traversed.
         *
         * @return the number of nodes traversed
         */
        public int getCount() {
            return count;
        }

        @Override
        public void apply(StructuralNode node) {
            count++;
        }

        @Override
        public boolean isStopTraversing() {
            return false;
        }
    }

    /**
     * The stats (and skip state and reason) of a {@link Plugin}, when the {@code Plugin} was started, how many messages were
     * sent and its scan progress.
     */
    private static class PluginStats {

        private final long startTime;
        private int messageCount;
        private int progress;
        private boolean skipped;
        private String skippedReason;

        /**
         * Constructs a {@code PluginStats}, initialising the starting time of the plugin.
         */
        public PluginStats() {
            startTime = System.currentTimeMillis();
        }

        /**
         * Tells whether or not the plugin was skipped.
         *
         * @return {@code true} if the plugin was skipped, {@code false} otherwise
         * @see #skipped()
         */
        public boolean isSkipped() {
            return skipped;
        }

        /**
         * Skips the plugin.
         *
         * @see #isSkipped()
         * @see #setSkippedReason(String)
         */
        public void skipped() {
            this.skipped = true;
        }

        /**
         * Gets the reason why the plugin was skipped.
         *
         * @param reason the reason why the plugin was skipped, might be {@code null}
         * @see #getSkippedReason()
         * @see #isSkipped()
         */
        public void setSkippedReason(String reason) {
            this.skippedReason = reason;
        }

        /**
         * Gets the reason why the plugin was skipped.
         *
         * @return the reason why the plugin was skipped, might be {@code null}
         * @see #setSkippedReason(String)
         * @see #isSkipped()
         */
        public String getSkippedReason() {
            return skippedReason;
        }

        /**
         * Gets the time when the plugin was started, in milliseconds.
         *
         * @return time when the plugin was started
         * @see System#currentTimeMillis()
         */
        public long getStartTime() {
            return startTime;
        }

        /**
         * Gets the count of messages sent by the plugin.
         *
         * @return the count of messages sent
         */
        public int getMessageCount() {
            return messageCount;
        }

        /**
         * Increments the count of messages sent by the plugin.
         * <p>
         * Should be called when the plugin notifies that a message was sent.
         */
        public void incMessageCount() {
            messageCount++;
        }

        /**
         * Gets the scan progress of the plugin.
         *
         * @return the scan progress
         */
        public int getProgress() {
            return progress;
        }

        /**
         * Increments the scan progress of the plugin.
         * <p>
         * Should be called after scanning a message.
         */
        public void incProgress() {
            this.progress++;
        }

        /**
         * Sets the scan progress of the plugin.
         *
         * @param progress the progress to set
         */
        public void setProgress(int progress) {
            this.progress = progress;
        }
    }

}
