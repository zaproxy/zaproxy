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
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.parosproxy.paros.common.ThreadPool;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.ConnectionParam;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpSender;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class HostProcess implements Runnable {

    private static Log log = LogFactory.getLog(HostProcess.class);
	private static DecimalFormat decimalFormat = new java.text.DecimalFormat("###0.###");

    private SiteNode startNode = null;
    private boolean isStop = false;
    private PluginFactory pluginFactory = null;
    private ScannerParam scannerParam = null;
    private HttpSender httpSender = null;
    private ThreadPool threadPool = null;
    private Scanner parentScanner = null;
    private String hostAndPort = "";
    private Analyser analyser = null;
    private Kb kb = null;
    
    // time related 
    private HashMap mapPluginStartTime = new HashMap();
    private long hostProcessStartTime = 0;

    

    /**
     * 
     */
    public HostProcess(String hostAndPort, Scanner parentScanner, ScannerParam scannerParam, ConnectionParam connectionParam) {
        super();
        this.hostAndPort = hostAndPort;
        this.parentScanner = parentScanner;
        this.scannerParam = scannerParam;
        httpSender = new HttpSender(connectionParam, true);
        threadPool = new ThreadPool(scannerParam.getThreadPerHost());
    }
    
    public void setStartNode(SiteNode startNode) {
        this.startNode = startNode;
    }
    
    public void stop() {
        isStop = true;
        getAnalyser().stop();
    }
    
    public void run() {

        hostProcessStartTime = System.currentTimeMillis();
        getAnalyser().start(startNode);
        
        Plugin plugin = null;
        while (!isStop && getPluginFactory().existPluginToRun()) {
            plugin = getPluginFactory().nextPlugin();
            if (plugin != null) {
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
        log.info("start host " + hostAndPort + " | " + plugin.getCodeName());
        mapPluginStartTime.put(new Long(plugin.getId()), new Long(System.currentTimeMillis()));
        if (plugin instanceof AbstractHostPlugin) {
            scanSingleNode(plugin, startNode);
        } else if (plugin instanceof AbstractAppPlugin) {
            traverse(plugin, startNode);
            threadPool.waitAllThreadComplete(600000);
        	pluginCompleted(plugin);

        }
            
    }
    
    private PluginFactory getPluginFactory() {
        if (pluginFactory == null) {
            pluginFactory = new PluginFactory();
        }
        return pluginFactory;
    }
    
    private void traverse(Plugin plugin, SiteNode node) {
        
        if (node == null || plugin == null) {
            return;
        }

        scanSingleNode(plugin, node);

        for (int i=0; i<node.getChildCount() && !isStop(); i++) {
            try {
                traverse(plugin, (SiteNode) node.getChildAt(i));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    /**
     * Create new plugin instance and run against a node
     * @param plugin
     * @param node.  If node == null, run for server level plugin
     */
    private void scanSingleNode(Plugin plugin, SiteNode node) {
        Thread thread = null;
        Plugin test = null;
        HttpMessage msg = null;

        // do not poll for isStop here to allow every plugin to run but terminate immediately.
        //if (isStop()) return;

        try {
            if (node == null) {
                return;
            }
            msg = node.getHistoryReference().getHttpMessage();

            test = (Plugin) plugin.getClass().newInstance();
            test.setConfig(plugin.getConfig());
            test.init(msg, this);
            notifyHostProgress(plugin.getName() + ": " + msg.getRequestHeader().getURI().toString());

        } catch (Exception e ) {
            e.printStackTrace();
            return;
        }
        
        do {
            thread = threadPool.getFreeThreadAndRun(test);
            if (thread == null) {
                Util.sleep(200);
            }
        } while (thread == null);
        
    }
    
    
    /**
     * @return Returns the httpSender.
     */
    public HttpSender getHttpSender() {
        return httpSender;
    }
    
    public boolean isStop() {
        return (isStop || parentScanner.isStop());
    }
 
    private void notifyHostProgress(String msg) {
        int percentage = 0;
        if (getPluginFactory().totalPluginToRun() == 0) {
            percentage = 100;
        } else {
            percentage = (100 * getPluginFactory().totalPluginCompleted() / getPluginFactory().totalPluginToRun());
        }
        parentScanner.notifyHostProgress(hostAndPort, msg, percentage);
    }
    
    private void notifyHostComplete() {
        long diffTimeMillis = System.currentTimeMillis() - hostProcessStartTime;
		String diffTimeString = decimalFormat.format((double) (diffTimeMillis/1000.0)) + "s";
        log.info("completed host " + hostAndPort + " in " + diffTimeString);
        parentScanner.notifyHostComplete(hostAndPort);
    }
    
    public void alertFound(Alert alert) {
        parentScanner.notifyAlertFound(alert);
    }
    
	public Analyser getAnalyser() {
	    if (analyser == null) {
	        analyser = new Analyser(getHttpSender());
	    }
	    return analyser;
	}
	
	void pluginCompleted(Plugin plugin) {
	    Object obj = mapPluginStartTime.get(new Long(plugin.getId()));
	    StringBuffer sb = new StringBuffer();
	    if (isStop) {
	        sb.append("stopped host/plugin ");
	    } else {
	        sb.append("completed host/plugin "); 
	    }
	    sb.append(hostAndPort + " | " + plugin.getCodeName());
	    if (obj != null) {
	        long startTimeMillis = ((Long) obj).longValue(); 
	        long diffTimeMillis = System.currentTimeMillis() - startTimeMillis;
	        String diffTimeString = decimalFormat.format((double) (diffTimeMillis/1000.0)) + "s";
		    sb.append(" in " + diffTimeString);
	    }
	    log.info(sb.toString());
	    getPluginFactory().setRunningPluginCompleted(plugin);
        notifyHostProgress(null);
	    
	}
	
	Kb getKb() {
	    if (kb == null) {
	        kb = new Kb();
	    }
	    return kb;
	}
    
}
