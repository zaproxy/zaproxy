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
package org.parosproxy.paros.core.scanner;

import java.text.DecimalFormat;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.parosproxy.paros.common.ThreadPool;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.ConnectionParam;



/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Scanner implements Runnable {

    private static Log log = LogFactory.getLog(Scanner.class);
	private static DecimalFormat decimalFormat = new java.text.DecimalFormat("###0.###");

	private Vector listenerList = new Vector();
	private ScannerParam scannerParam = null;
	private ConnectionParam connectionParam = null;
	//private HttpSender httpSender = null;
	private boolean isStop = false;
	private ThreadPool pool = null;
	private SiteNode startNode = null;
	private Analyser analyser = null;
	private long startTimeMillis = 0;
	
    /**
     * 
     */
    public Scanner(ScannerParam scannerParam, ConnectionParam param) {
	    this.connectionParam = param;
	    this.scannerParam = scannerParam;
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
	            hostProcess = new HostProcess(hostAndPort, this, scannerParam, connectionParam);
	            hostProcess.setStartNode(child);
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

            hostProcess = new HostProcess(hostAndPort, this, scannerParam, connectionParam);
            hostProcess.setStartNode(node);
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
	        result = curNode.toString();
	    }
	    return result;
	}
	
	void notifyHostComplete(String hostAndPort) {
	    for (int i=0; i<listenerList.size(); i++) {
	        ScannerListener listener = (ScannerListener) listenerList.get(i);
	        listener.hostComplete(hostAndPort);
	    }
	}
	
	void notifyHostProgress(String hostAndPort, String msg, int percentage) {
	    for (int i=0; i<listenerList.size(); i++) {
	        ScannerListener listener = (ScannerListener) listenerList.get(i);
	        listener.hostProgress(hostAndPort, msg, percentage);
	    }
	    
	}
	
	void notifyScannerComplete() {

	    for (int i=0; i<listenerList.size(); i++) {
	        ScannerListener listener = (ScannerListener) listenerList.get(i);
	        listener.scannerComplete();
	    }
	    long diffTimeMillis = System.currentTimeMillis() - startTimeMillis;
		String diffTimeString = decimalFormat.format((double) (diffTimeMillis/1000.0)) + "s";
	    log.info("scanner completed in " + diffTimeString);
	    isStop = true;
	}
	
	void notifyAlertFound(Alert alert) {
	    for (int i=0; i<listenerList.size(); i++) {
	        ScannerListener listener = (ScannerListener) listenerList.get(i);
	        listener.alertFound(alert);
	    }

	}

	void notifyHostNewScan(String hostAndPort, HostProcess hostThread) {
	    for (int i=0; i<listenerList.size(); i++) {
	        ScannerListener listener = (ScannerListener) listenerList.get(i);
	        listener.hostNewScan(hostAndPort, hostThread);
	    }
	    
	}
	

}
