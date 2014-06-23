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

package org.parosproxy.paros.core.scanner;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.parosproxy.paros.common.ThreadPool;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.ConnectionParam;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.users.User;



public class Scanner implements Runnable {

    private static Logger log = Logger.getLogger(Scanner.class);
	private static DecimalFormat decimalFormat = new java.text.DecimalFormat("###0.###");

	private Vector<ScannerListener> listenerList = new Vector<>();
	private ScannerParam scannerParam = null;
	private ConnectionParam connectionParam = null;
	private PluginFactory pluginFactory = null;
	//private HttpSender httpSender = null;
	private boolean isStop = false;
	private ThreadPool pool = null;
	private SiteNode startNode = null;
	private long startTimeMillis = 0;
    private List<Pattern> excludeUrls = null;
	private boolean justScanInScope = false;
	private boolean scanChildren = true;
	private User user = null;

	// ZAP: Added scanner pause option
	private boolean pause = false;
	
	private List<HostProcess> hostProcesses = new ArrayList<>();

    public Scanner(ScannerParam scannerParam, ConnectionParam param, PluginFactory pluginFactory) {
	    this.connectionParam = param;
	    this.scannerParam = scannerParam;
	    this.pluginFactory = pluginFactory;
	    //httpSender = new HttpSender(param);
	    pool = new ThreadPool(scannerParam.getHostPerScan());
    }
    
    
    public void start(SiteNode startNode) {
        isStop = false;
        log.info("scanner started");
        startTimeMillis = System.currentTimeMillis();
        this.startNode = startNode;
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

	@Override
	public void run() {
	    scan(startNode);
	    
//	    while (pool.isAllThreadComplete()) {
//	        Util.sleep(4000);
//	    }
	    pool.waitAllThreadComplete(0);
	    notifyScannerComplete();
	}
	
	public void scan(SiteNode node) {

	    HostProcess hostProcess = null;
	    Thread thread = null;
	    
	    if (node.isRoot()) {
	        for (int i=0; i<node.getChildCount() && !isStop(); i++) {
	            SiteNode child = (SiteNode) node.getChildAt(i);
	            String hostAndPort = getHostAndPort(child);
	            hostProcess = new HostProcess(hostAndPort, this, scannerParam, connectionParam, pluginFactory.clone());
	            hostProcess.setStartNode(child);
	            hostProcess.setUser(this.user);
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
            String hostAndPort = getHostAndPort(node);

            hostProcess = new HostProcess(hostAndPort, this, scannerParam, connectionParam, pluginFactory);
            hostProcess.setStartNode(node);
            hostProcess.setUser(this.user);
            this.hostProcesses.add(hostProcess);
            thread = pool.getFreeThreadAndRun(hostProcess);
            notifyHostNewScan(hostAndPort, hostProcess);
	        
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
	        parent = (SiteNode) node.getParent();
	        while (!parent.isRoot()) {
	            curNode = parent;
	            parent = (SiteNode) curNode.getParent();
	        }
	        result = curNode.getNodeName();
	    }
	    return result;
	}
	
	void notifyHostComplete(String hostAndPort) {
	    for (int i=0; i<listenerList.size(); i++) {
	        // ZAP: Removed unnecessary cast.
	        ScannerListener listener = listenerList.get(i);
	        listener.hostComplete(hostAndPort);
	    }
	}
	
	void notifyHostProgress(String hostAndPort, String msg, int percentage) {
	    for (int i=0; i<listenerList.size(); i++) {
	        // ZAP: Removed unnecessary cast.
	        ScannerListener listener = listenerList.get(i);
	        listener.hostProgress(hostAndPort, msg, percentage);
	    }
	    
	}
	
	void notifyScannerComplete() {

	    for (int i=0; i<listenerList.size(); i++) {
	        // ZAP: Removed unnecessary cast.
	        ScannerListener listener = listenerList.get(i);
	        listener.scannerComplete();
	    }
	    long diffTimeMillis = System.currentTimeMillis() - startTimeMillis;
		// ZAP: Removed unnecessary cast.
		String diffTimeString = decimalFormat.format(diffTimeMillis/1000.0) + "s";
	    log.info("scanner completed in " + diffTimeString);
	    isStop = true;
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
	        listener.hostNewScan(hostAndPort, hostThread);
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
	
	public boolean urlInScope(String uri) {
		if (this.justScanInScope && ! Model.getSingleton().getSession().isInScope(uri)) {
			// Restricted to urls in scope, and this isnt
			return false;
		}
		
		if (excludeUrls != null) {
			for (Pattern p : excludeUrls) {
				if (p.matcher(uri).matches()) {
					if (log.isDebugEnabled()) {
						log.debug("URL excluded: " + uri + " Regex: " + p.pattern());
					}
					// Explicitly excluded
					return false;
				}
			}
		}
		return true;
	}

	public void setStartNode(SiteNode startNode) {
		this.startNode = startNode;
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

	public void setPluginFactory(PluginFactory pluginFactory) {
		this.pluginFactory = pluginFactory;
	}

	/**
	 * Set the user to scan as. If null then the current session will be used.
	 * @param user
	 */
	public void setUser(User user) {
		this.user = user;
	}
	
}
