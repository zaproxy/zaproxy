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
import org.parosproxy.paros.common.ThreadPool;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.ConnectionParam;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpSender;
import org.zaproxy.zap.extension.ascan.ScanPolicy;
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
    private TechSet techSet = null;

    // time related 
    // ZAP: changed to Integer because the pluginId is int
    private final Map<Integer, Long> mapPluginStartTime = new HashMap<>();
    private final Set<Integer> listPluginIdSkipped = new HashSet<>();
    private long hostProcessStartTime = 0;

    // ZAP: progress related
    private int nodeInScopeCount = 0;
    private final Map<Integer, Integer> mapPluginProgress = new HashMap<>();
    private int percentage = 0;
    
    /**
     * Intantiate a new HostProcess service
     * 
     * @param hostAndPort the host:port value of the site that need to be processed
     * @param parentScanner the scanner instance which instantiated this process
     * @param scannerParam the session scanner parameters
     * @param connectionParam the connection parameters
     * @param pluginFactory the Factory object for plugin management and instantiation
     */
    public HostProcess(String hostAndPort, Scanner parentScanner, 
    		ScannerParam scannerParam, ConnectionParam connectionParam, 
    		ScanPolicy scanPolicy) {
        
        super();
        this.hostAndPort = hostAndPort;
        this.parentScanner = parentScanner;
        this.scannerParam = scannerParam;
		this.pluginFactory = scanPolicy.getPluginFactory().clone();
		
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

        hostProcessStartTime = System.currentTimeMillis();
        for (StructuralNode node : startNodes) {
	        // ZAP: before all get back the size of this scan
	        nodeInScopeCount += getNodeInScopeCount(node, true);
	        // ZAP: begin to analyze the scope
	        getAnalyser().start(node);
        }
        
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
        notifyHostProgress(null);
        notifyHostComplete();
        getHttpSender().shutdown();
    }

    private void processPlugin(Plugin plugin) {
        mapPluginStartTime.put(plugin.getId(), System.currentTimeMillis());
        mapPluginProgress.put(plugin.getId(), 0);

        if (techSet != null && !plugin.targets(techSet)) {
            listPluginIdSkipped.add(plugin.getId());
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
	            traverse(plugin, startNode, true);
	            threadPool.waitAllThreadComplete(600000);
	            pluginCompleted(plugin);
	        }
        }
    }

    private void traverse(Plugin plugin, StructuralNode node) {
        this.traverse(plugin, node, false);
    }

    private void traverse(Plugin plugin, StructuralNode node, boolean incRelatedSiblings) {
        if (node == null || plugin == null || isStop()) {
            return;
        }
        log.debug("traverse: plugin=" + plugin.getName() + " url=" + node.getName());

        Set<StructuralNode> parentNodes = new HashSet<>();
        parentNodes.add(node);

        scanSingleNode(plugin, node);

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
	        	while (iter.hasNext() && !isStop() && !isSkipped(plugin)) {
	        		StructuralNode child = iter.next();
	                // ZAP: Implement pause and resume
	                while (parentScanner.isPaused() && !isStop()) {
	                    Util.sleep(500);
	                }
	
	                try {
	                    traverse(plugin, child);
	                    
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
     * @param plugin
     * @param node. If node == null, run for server level plugin
     * @return {@code true} if the {@code plugin} was run, {@code false} otherwise.
     */
    private boolean scanSingleNode(Plugin plugin, StructuralNode node) {
        Thread thread;
        Plugin test;
        HttpMessage msg;
        
        log.debug("scanSingleNode node plugin=" + plugin.getName() + " node=" + node.getName());

        // do not poll for isStop here to allow every plugin to run but terminate immediately.
        //if (isStop()) return;

        try {
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
            
            msg = node.getHistoryReference().getHttpMessage();

            if (msg == null) {
                // Likely to be a temporary node
                log.debug("scanSingleNode msg null");
                return false;
            }

            test = plugin.getClass().newInstance();
            test.setConfig(plugin.getConfig());
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
        return mapPluginProgress.get(plugin.getId());
    }

    /**
     * ZAP: method to set the current progress status for a specific plugin
     * @param plugin the plugin we're setting the progress
     * @param value the value that need to be set
     */
    public void setTestCurrentCount(Plugin plugin, int value) {        
        mapPluginProgress.put(plugin.getId(), value);
    }

    /**
     * ZAP: inner recursive method to count nodes in scope
     * @param node the starting node
     * @param incRelatedSiblings true if siblings should be included
     * @return the number of nodes
     */
    private int getNodeInScopeCount(StructuralNode node, boolean incRelatedSiblings) {
        if (node == null) {
            return 0;
        }
        
        int nodeCount = 1;
        if (parentScanner.scanChildren()) {
            
            Set<StructuralNode> parentNodes = new HashSet<>();
            parentNodes.add(node);
                        
            if (incRelatedSiblings) {
                // Also match siblings with the same hierarchic name
                // If we dont do this http://localhost/start might match the GET variant in the Sites tree and miss the hierarchic node
                // note that this is only done for the top level.
                try {
					Iterator<StructuralNode> iter = node.getParent().getChildIterator();
					while (iter.hasNext()) {
					    StructuralNode sibling = iter.next();
						if (! node.isSameAs(sibling) && ! node.getName().equals(sibling.getName())) {
					        parentNodes.add(sibling);
					        nodeCount++;
						}
					}
				} catch (DatabaseException e) {
					// Ignore - if we cant connect to the db there will be plenty of other errors logged ;)
				}
            }
            
            for (StructuralNode pNode : parentNodes) {
                Iterator<StructuralNode> iter = pNode.getChildIterator();
                while (iter.hasNext()) {
                    nodeCount += getNodeInScopeCount(iter.next(), false);
                }
            }
        }
        
        return nodeCount;
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
            float progressRunning = 0;
            for (Plugin plugin : pluginFactory.getRunning()) {
                progressRunning += (getTestCurrentCount(plugin) * 100.0) / getTestTotalCount();
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

    // ZAP: notify parent
    public void notifyNewMessage(HttpMessage msg) {
        parentScanner.notifyNewMessage(msg);
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
     * Skip the current executing plugin
     * @param plugin the plugin instance that need to be skipped
     */
    public void pluginSkipped(Plugin plugin) {
        if (pluginFactory.isRunning(plugin)) {
            listPluginIdSkipped.add(plugin.getId());
        }
    }

    /**
     * Check if a specific plugin has been explicitly skipped by the user 
     * @param plugin the plugin instance currently running
     * @return true if the user has skipped this instance
     */
    public boolean isSkipped(Plugin plugin) {
        return (!listPluginIdSkipped.isEmpty() && listPluginIdSkipped.contains(plugin.getId()));
    }
    
    /**
     * Complete the current plugin and update statistics
     * @param plugin the plugin that need to be marked as completed
     */
    void pluginCompleted(Plugin plugin) {
        Object obj = mapPluginStartTime.get(plugin.getId());
        StringBuilder sb = new StringBuilder();
        if (isStop()) {
            sb.append("stopped host/plugin ");
 
        // ZAP: added skipping notifications
        } else if (isSkipped(plugin)) {
            sb.append("skipped plugin ");
                    
        } else {
            sb.append("completed host/plugin ");
        }
        
        sb.append(hostAndPort).append(" | ").append(plugin.getCodeName());
        if (obj != null) {
            long startTimeMillis = (Long)obj;
            long diffTimeMillis = System.currentTimeMillis() - startTimeMillis;
            String diffTimeString = decimalFormat.format(diffTimeMillis / 1000.0) + "s";
            sb.append(" in ").append(diffTimeString);
        }

        // Probably too verbose evaluate 4 the future
        log.info(sb.toString());
        
        pluginFactory.setRunningPluginCompleted(plugin);
        notifyHostProgress(null);
                
        // ZAP: update progress as finished
        mapPluginProgress.put(plugin.getId(), nodeInScopeCount);
    }

    /**
     * 
     * @return 
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
	 * @param user
	 */
    public void setUser(User user) {
		this.user = user;
		if (httpSender != null) {
			httpSender.setUser(user);
		}
	}

	public TechSet getTechSet() {
		return techSet;
	}

	public void setTechSet(TechSet techSet) {
		this.techSet = techSet;
	}
	
	/** 
     * ZAP: abstract plugin will call this method in order to invoke any extensions that have hooked into the active scanner
     * @param msg
     * @param plugin
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
     * @param msg
     * @param plugin
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
}
