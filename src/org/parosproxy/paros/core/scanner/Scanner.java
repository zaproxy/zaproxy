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
// ZAP: 2012/04/25 Added @Override annotation to the appropriate method and removed
// unnecessary casts.
// ZAP: 2012/05/04 Catch CloneNotSupportedException whenever an Uri is cloned,
// 		as introduced with version 3.1 of HttpClient
// ZAP: 2012/07/30 Issue 43: Added support for Scope
// ZAP: 2013/01/19 Issue 460 Add support for a scan progress dialog
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2014/02/21 Issue 1043: Custom active scan dialog
// ZAP: 2014/06/23 Issue 1151: Active Scan in Scope finishes before scanning all
// messages in scope if multiple domains available
// ZAP: 2014/06/23 Issue 1242: Active scanner might use outdated policy settings
// ZAP: 2014/07/07 Issue 389: Enable technology scope for scanners
// ZAP: 2014/10/24 Issue 1378: Revamp active scan panel
// ZAP: 2014/10/25 Issue 1062: Made the scanner load all scannerhooks from the extensionloader
// ZAP: 2014/11/19 Issue 1412: Manage scan policies
// ZAP: 2015/02/18 Issue 1062: Tidied up extension hooks
// ZAP: 2015/04/02 Issue 1582: Low memory option
// ZAP: 2015/10/21 Issue 1576: Removed SiteNode cast no longer needed
// ZAP: 2015/12/14 Prevent scans from becoming in undefined state
// ZAP: 2016/07/12 Do not allow techSet to be null
// ZAP: 2016/07/01 Issue 2647 Support a/pscan rule configuration 

package org.parosproxy.paros.core.scanner;

import java.security.InvalidParameterException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.common.ThreadPool;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.ConnectionParam;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.ascan.ScanPolicy;
import org.zaproxy.zap.extension.ruleconfig.RuleConfigParam;
import org.zaproxy.zap.extension.script.ScriptCollection;
import org.zaproxy.zap.model.StructuralNode;
import org.zaproxy.zap.model.StructuralSiteNode;
import org.zaproxy.zap.model.Target;
import org.zaproxy.zap.model.TechSet;
import org.zaproxy.zap.users.User;



public class Scanner implements Runnable {

    private static Logger log = Logger.getLogger(Scanner.class);
	private static DecimalFormat decimalFormat = new java.text.DecimalFormat("###0.###");

	private Vector<ScannerListener> listenerList = new Vector<>();
	
	//ZAP: Added a list of scannerhooks
	private Vector<ScannerHook> hookList = new Vector<>();
	private ScannerParam scannerParam = null;
	private ConnectionParam connectionParam = null;
	private ScanPolicy scanPolicy;
	private RuleConfigParam ruleConfigParam;
	private boolean isStop = false;
	private ThreadPool pool = null;
	private Target target = null;
	private long startTimeMillis = 0;
    private List<Pattern> excludeUrls = null;
	private boolean justScanInScope = false;
	private boolean scanChildren = true;
	private User user = null;
    private TechSet techSet;
    private Set<ScriptCollection> scriptCollections = new HashSet<ScriptCollection>();
	private int id;

	// ZAP: Added scanner pause option
	private boolean pause = false;
	
	private List<HostProcess> hostProcesses = new ArrayList<>();

    public Scanner(ScannerParam scannerParam, ConnectionParam param, 
    		ScanPolicy scanPolicy, RuleConfigParam ruleConfigParam) {
	    this.connectionParam = param;
	    this.scannerParam = scannerParam;
	    this.scanPolicy = scanPolicy;
	    this.ruleConfigParam = ruleConfigParam;
	    pool = new ThreadPool(scannerParam.getHostPerScan());
	    
	  //ZAP: Load all scanner hooks from extensionloader. 
	    Control.getSingleton().getExtensionLoader().hookScannerHook(this);

		techSet = TechSet.AllTech;
    }
    
    
    public void start(SiteNode startNode) {
    	this.start(new Target(startNode));
    }

    public void start(Target target) {
        isStop = false;
        log.info("scanner started");
        startTimeMillis = System.currentTimeMillis();
        this.target = target;
        Thread thread = new Thread(this);
        thread.setPriority(Thread.NORM_PRIORITY-2);
        thread.start();
    }

    public void stop() {
        log.info("scanner stopped");

        isStop = true;
        
    }
    
	public void addScannerListener(ScannerListener listener) {
		listenerList.add(listener);		
	}

	public void removeScannerListener(ScannerListener listener) {
		listenerList.remove(listener);
	}
	
	// ZAP: Added functionality to add remove and get the attached scannerhooks to the Scanner. 
	public void addScannerHook(ScannerHook scannerHook) {
		hookList.add(scannerHook);
	}

	public void removerScannerHook(ScannerHook scannerHook) {
		hookList.remove(scannerHook);
	}

	protected Vector<ScannerHook> getScannerHooks(){
		return hookList;
	}

	@Override
	public void run() {
        try {
            scan(target);
	    
//	    while (pool.isAllThreadComplete()) {
//	        Util.sleep(4000);
//	    }
            pool.waitAllThreadComplete(0);
        } catch (Exception e) {
            log.error("An error occurred while active scanning:", e);
        } finally {
            notifyScannerComplete();
        }
	}
	
	public void scan(Target target) {

	    Thread thread = null;

        this.setScanChildren(target.isRecurse());
        this.setJustScanInScope(target.isInScopeOnly());

	    if (target.getStartNodes() != null) {
		    HostProcess hostProcess = null;
	    	List<StructuralNode> nodes = target.getStartNodes();
	    	if (nodes.size() == 1 && nodes.get(0).isRoot()) {
	    		Iterator<StructuralNode> iter = nodes.get(0).getChildIterator();
	    		while (iter.hasNext()) {
		        	StructuralNode child = iter.next();
		            String hostAndPort = getHostAndPort(child);
		            hostProcess = new HostProcess(hostAndPort, this, scannerParam, 
		            		connectionParam, scanPolicy, ruleConfigParam);
		            hostProcess.setStartNode(child);
		            hostProcess.setUser(this.user);
		            hostProcess.setTechSet(this.techSet);
		            this.hostProcesses.add(hostProcess);
		            do { 
		                thread = pool.getFreeThreadAndRun(hostProcess);
		                if (thread == null) Util.sleep(500);
		            } while (thread == null && !isStop());
		            if (thread != null) {
			            notifyHostNewScan(hostAndPort, hostProcess);
		            }
		        }
		    } else {
		    	Map<String, HostProcess> processMap = new HashMap<String, HostProcess>();
		    	for (StructuralNode node : nodes) {
		    		// Loop through the nodes creating new HostProcesss's as required
		            String hostAndPort = getHostAndPort(node);
		            hostProcess = processMap.get(hostAndPort);
		            if (hostProcess == null) {
			            hostProcess = new HostProcess(hostAndPort, this, 
			            		scannerParam, connectionParam, scanPolicy, ruleConfigParam);
			            hostProcess.setStartNode(node);
			            hostProcess.setUser(this.user);
			            hostProcess.setTechSet(this.techSet);
			            processMap.put(hostAndPort, hostProcess);
		            } else {
			            hostProcess.addStartNode(node);
		            }
		    	}
		    	
		    	// Now start them all off
		    	for (Entry<String, HostProcess> pmSet : processMap.entrySet()) {
		            this.hostProcesses.add(pmSet.getValue());
		            thread = pool.getFreeThreadAndRun(pmSet.getValue());
		            notifyHostNewScan(pmSet.getKey(), pmSet.getValue());
		    	}
		    }
	    } else if (target.getContext() != null) {
	    	// Loop through all of the top nodes containing children in this context
	    	// TODO need to change for lowmem
	    	if (Constant.isLowMemoryOptionSet()) {
	    		throw new InvalidParameterException("Not yet supported for the low memory option :(");
	    	}
	    	List<SiteNode> nodes = target.getContext().getTopNodesInContextFromSiteTree();
	    	for (SiteNode node : nodes) {
			    HostProcess hostProcess = null;
	            String hostAndPort = getHostAndPort(node);
	            hostProcess = new HostProcess(hostAndPort, this, scannerParam, 
	            		connectionParam, scanPolicy, ruleConfigParam);
	            hostProcess.setStartNode(new StructuralSiteNode(node));
	            hostProcess.setUser(this.user);
	            hostProcess.setTechSet(this.techSet);
	            this.hostProcesses.add(hostProcess);
	            do { 
	                thread = pool.getFreeThreadAndRun(hostProcess);
	                if (thread == null) Util.sleep(500);
	            } while (thread == null && !isStop());
	            if (thread != null) {
		            notifyHostNewScan(hostAndPort, hostProcess);
	            }
	    	}
	    } else if (target.isInScopeOnly()) {
	    	// TODO need to change for lowmem
	    	if (Constant.isLowMemoryOptionSet()) {
	    		throw new InvalidParameterException("Not yet supported for the low memory option :(");
	    	}
	    	this.justScanInScope = true;
	    	List<SiteNode> nodes = Model.getSingleton().getSession().getTopNodesInScopeFromSiteTree();
	    	for (SiteNode node : nodes) {
			    HostProcess hostProcess = null;
	            String hostAndPort = getHostAndPort(node);
	            hostProcess = new HostProcess(hostAndPort, this, scannerParam, 
	            		connectionParam, scanPolicy, ruleConfigParam);
	            hostProcess.setStartNode(new StructuralSiteNode(node));
	            hostProcess.setUser(this.user);
	            hostProcess.setTechSet(this.techSet);
	            this.hostProcesses.add(hostProcess);
	            do { 
	                thread = pool.getFreeThreadAndRun(hostProcess);
	                if (thread == null) Util.sleep(500);
	            } while (thread == null && !isStop());
	            if (thread != null) {
		            notifyHostNewScan(hostAndPort, hostProcess);
	            }
	    	}
	    }
	     
	}

	public boolean isStop() {
	    
	    return isStop;
	}
	
	private String getHostAndPort(SiteNode node) {
	    String result = "";
	    SiteNode parent = null;
	    if (node == null || node.isRoot()) {
	        result = "";
	    } else {
	        SiteNode curNode = node;
	        parent = node.getParent();
	        while (!parent.isRoot()) {
	            curNode = parent;
	            parent = curNode.getParent();
	        }
	        result = curNode.getNodeName();
	    }
	    return result;
	}
	
	private String getHostAndPort(StructuralNode node) {
	    String result = "";
	    if (node == null || node.isRoot()) {
	        result = "";
	    } else {
	    	String url = node.getName();
	    	int idx = url.indexOf("/", url.indexOf("//")+2);
	    	if (idx > 0) {
	    		result = url.substring(0, idx);
	    	} else {
	    		result = url;
	    	}
	    }
	    return result;
	}
	
	void notifyHostComplete(String hostAndPort) {
	    for (int i=0; i<listenerList.size(); i++) {
	        // ZAP: Removed unnecessary cast.
	        ScannerListener listener = listenerList.get(i);
	        listener.hostComplete(this.id, hostAndPort);
	    }
	}
	
	void notifyHostProgress(String hostAndPort, String msg, int percentage) {
	    for (int i=0; i<listenerList.size(); i++) {
	        // ZAP: Removed unnecessary cast.
	        ScannerListener listener = listenerList.get(i);
	        listener.hostProgress(id, hostAndPort, msg, percentage);
	    }
	    
	}
	
	void notifyScannerComplete() {
	    long diffTimeMillis = System.currentTimeMillis() - startTimeMillis;
		String diffTimeString = decimalFormat.format(diffTimeMillis/1000.0) + "s";
	    log.info("scanner completed in " + diffTimeString);
	    isStop = true;

	    for (int i=0; i<listenerList.size(); i++) {
	        // ZAP: Removed unnecessary cast.
	        ScannerListener listener = listenerList.get(i);
	        listener.scannerComplete(this.id);
	    }
	    
	    // ZAP: Invokes scannerhooks with the scannercomplete method.
	    for (int i=0; i<hookList.size(); i++) {
	        try {
				ScannerHook hook = hookList.get(i);
				hook.scannerComplete();
			} catch (Exception e) {
				log.info("An exception occurred while notifying a ScannerHook about scanner completion: " + e.getMessage(), e);
			}
	    }
	}
	
	void notifyAlertFound(Alert alert) {
	    for (int i=0; i<listenerList.size(); i++) {
	        // ZAP: Removed unnecessary cast.
	        ScannerListener listener = listenerList.get(i);
	        listener.alertFound(alert);
	    }

	}

	void notifyHostNewScan(String hostAndPort, HostProcess hostThread) {
	    for (int i=0; i<listenerList.size(); i++) {
	        // ZAP: Removed unnecessary cast.
	        ScannerListener listener = listenerList.get(i);
	        listener.hostNewScan(this.id, hostAndPort, hostThread);
	    }
	    
	}

	// ZAP: support pause and notify parent
	public void pause() {
		this.pause = true;
	}
	
	public void resume () {
		this.pause = false;
	}
	
	public boolean isPaused() {
		return pause;
	}

	public void notifyNewMessage(HttpMessage msg) {
	    for (int i=0; i<listenerList.size(); i++) {
	        ScannerListener listener = listenerList.get(i);
	        listener.notifyNewMessage(msg);
	    }
	}
	
	public void setExcludeList(List<String> urls) {
		if (urls != null) {
		    excludeUrls = new ArrayList<>(urls.size());
		    for (String url : urls) {
				Pattern p = Pattern.compile(url, Pattern.CASE_INSENSITIVE);
				excludeUrls.add(p);
		    }
		} else {
			excludeUrls = new ArrayList<>(0);
		}
	}
	
	public boolean isInScope(String nodeName) {
		if (this.justScanInScope && ! Model.getSingleton().getSession().isInScope(nodeName)) {
			// Restricted to urls in scope, and this isnt
			return false;
		}
		if (this.target.getContext() != null) {
			if ( ! target.getContext().isIncluded(nodeName)) {
				// Restricted to nodes in the given context, and this isnt
				return false;
			}
		}
		
		if (excludeUrls != null) {
			for (Pattern p : excludeUrls) {
				if (p.matcher(nodeName).matches()) {
					if (log.isDebugEnabled()) {
						log.debug("URL excluded: " + nodeName + " Regex: " + p.pattern());
					}
					// Explicitly excluded
					return false;
				}
			}
		}
		return true;
	}

	public void setStartNode(SiteNode startNode) {
		this.target = new Target(startNode);
	}
	
	public SiteNode getStartNode() {
		if (target != null) {
			return target.getStartNode();
		}
		return null;
	}

	public void setJustScanInScope(boolean scanInScope) {
		justScanInScope = scanInScope;
	}

	public boolean getJustScanInScope() {
		return justScanInScope;
	}

	public void setScanChildren(boolean scanChildren) {
		this.scanChildren = scanChildren;
	}
	
	public boolean scanChildren() {
		return this.scanChildren;
	}
	
	public List<HostProcess> getHostProcesses() {
		return this.hostProcesses;
	}

	public void setScannerParam(ScannerParam scannerParam) {
		this.scannerParam = scannerParam;
	}

	public void setScanPolicy(ScanPolicy scanPolicy) {
		this.scanPolicy = scanPolicy;
	}

	/**
	 * Set the user to scan as. If null then the current session will be used.
	 * @param user
	 */
	public void setUser(User user) {
		this.user = user;
	}
	
	/**
	 * Gets the technologies used in the scan.
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
	 * @throws IllegalArgumentException (since TODO add version) if the given parameter is {@code null}
	 */
	public void setTechSet(TechSet techSet) {
		if (techSet == null) {
			throw new IllegalArgumentException("Parameter techSet must not be null.");
		}
		this.techSet = techSet;
	}

	public void addScriptCollection(ScriptCollection sc) {
		this.scriptCollections.add(sc);
	}

	public Set<ScriptCollection> getScriptCollections() {
		return this.scriptCollections;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
