/*
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.zaproxy.zap.extension.spider;

import java.sql.SQLException;
import java.util.Date;

import javax.swing.DefaultListModel;

import org.apache.commons.httpclient.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.parosproxy.paros.core.spider.Spider;
import org.parosproxy.paros.core.spider.SpiderListener;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.SiteMap;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.model.ScanListenner;
import org.zaproxy.zap.model.ScanThread;
import org.zaproxy.zap.utils.SortedListModel;

public class SpiderThread extends ScanThread implements ScanListenner, SpiderListener {

	private String site;
	private SortedListModel list;
	private boolean stopScan = false;
	private boolean isPaused = false;
	private ScanListenner listenner;
	private int progress = 0;
	private ExtensionSpider extension;

	private Spider spider = null;
	private SiteMap siteTree = null;
	private SiteNode startNode = null;
	
	private int spiderDone = 0;
	private int spiderTodo = 100;	// Will get updated ;)

    private static Log log = LogFactory.getLog(SpiderThread.class);

	public SpiderThread (ExtensionSpider extension, String site, ScanListenner listenner, org.parosproxy.paros.core.spider.SpiderParam portScanParam) {
		super(site, listenner);
		this.extension = extension;
		this.site = site;
		this.listenner = listenner;

		this.list = new SortedListModel();
		log.debug("Spider : " + site);
	}
	
	@Override
	public void run() {
		runScan();
	}
	
	private void runScan() {
		// Do the scan
		Date start = new Date();
		log.debug("Starting scan on " + site + " at " + start);
		startSpider();
	}

	public void stopScan() {
		if (spider != null) {
			spider.stop();
		}
		stopScan = true;
		this.listenner.scanFinshed(site);
	}

	public boolean isStopped() {
		return stopScan;
	}

	public String getSite() {
		return site;
	}
	
	public int getProgress () {
		return this.spiderDone;
	}

	public DefaultListModel getList() {
		return list;
	}

	@Override
	public void scanFinshed(String host) {
		// Ignore
	}

	@Override
	public void scanProgress(String host, int progress, int maximum) {
		if (progress > this.progress) {
			this.progress = progress;
			this.listenner.scanProgress(site, progress, maximum);
		}
	}

	public void pauseScan() {
		if (spider != null) {
			spider.pause();
		}
		this.isPaused = true;
	}

	public void resumeScan() {
		if (spider != null) {
			spider.resume();
		}
		this.isPaused = false;
	}
	
	public boolean isPaused() {
		return this.isPaused;
	}

	@Override
	public int getMaximum() {
		return this.spiderDone + this.spiderTodo;
	}
	
	public void startSpider() {
        siteTree = extension.getModel().getSession().getSiteTree();

	    if (startNode == null) {
	        startNode = (SiteNode) siteTree.getRoot();
	    }
        startSpider(startNode);

	}
	
	private void startSpider(SiteNode startNode) {
	    if (spider == null) {
	        try {
	        	extension.getModel().getDb().getTableHistory().deleteHistoryType(
	        			extension.getModel().getSession().getSessionId(), HistoryReference.TYPE_SPIDER_SEED);
	        	extension.getModel().getDb().getTableHistory().deleteHistoryType(
	        			extension.getModel().getSession().getSessionId(), HistoryReference.TYPE_SPIDER_VISITED);

	        } catch (SQLException e) {
            	log.error(e.getMessage(), e);
            }
	        
	        spider = new Spider(extension.getSpiderParam(), extension.getModel().getOptionsParam().getConnectionParam(), extension.getModel());
	        spider.addSpiderListener(this);

	        inOrderSeed(spider, startNode);

	    }
	    
	    extension.getSpiderPanel().setTabFocus();

		try {
			spider.start();
		    
        } catch (NullPointerException e1) {
        	log.error(e1.getMessage(), e1);
        }
	}
	
	private void inOrderSeed(Spider spider, SiteNode node) {

	    try {
	        if (!node.isRoot()) {
	            HttpMessage msg = node.getHistoryReference().getHttpMessage();
	            if (msg != null) {
	                if (!msg.getResponseHeader().isImage()) {
	                    spider.addSeed(msg);
	                }
	            }
	        }
	    } catch (Exception e) {
        	log.error(e.getMessage(), e);
	    }
	    
	    if (!node.isLeaf()) {
	        for (int i=0; i<node.getChildCount(); i++) {
	            try {
	                inOrderSeed(spider, (SiteNode) node.getChildAt(i));
	            } catch (Exception e) {
                	log.error(e.getMessage(), e);
	            }
	        }
	    }
	}
	
	public void spiderComplete() {
        try {
        	extension.getModel().getDb().getTableHistory().deleteHistoryType(
        			extension.getModel().getSession().getSessionId(), HistoryReference.TYPE_SPIDER_SEED);
        	extension.getModel().getDb().getTableHistory().deleteHistoryType(
        			extension.getModel().getSession().getSessionId(), HistoryReference.TYPE_SPIDER_VISITED);

        } catch (SQLException e) {
        	log.warn(e.getMessage(), e);
        }

		if (this.listenner != null) {
			this.listenner.scanFinshed(site);
			// Nasty, but otherwise can stick on 90+%
			this.listenner.scanProgress(site, this.getMaximum(), this.getMaximum());
		}
		stopScan = true;

	}
	
	public void foundURI(HttpMessage msg, boolean isSkip) {
	    if (extension.getView() != null) {
	        if (isSkip) {
	        	extension.getSpiderPanel().appendFoundButSkip(msg.getRequestHeader().getURI().toString() + "\n");
	        } else {
	        	extension.getSpiderPanel().appendFound(msg.getRequestHeader().getURI().toString() + "\n");
	        }
        }
	}
	
	public void readURI(HttpMessage msg) {

	    SiteMap siteTree = extension.getModel().getSession().getSiteTree();

		HistoryReference historyRef = null;
        try {
            historyRef = new HistoryReference(extension.getModel().getSession(), HistoryReference.TYPE_SPIDER, msg);
        } catch (Exception e) {
        	log.warn(e.getMessage(), e);
        }
        siteTree.addPath(historyRef, msg);

        
	}

    public Spider getSpider() {
	    return spider;
	}
	
	public void spiderProgress(final URI uri, final int percentageComplete, final int numberCrawled, final int numberToCrawl) {
	    this.spiderDone = numberCrawled;
	    this.spiderTodo = numberToCrawl;
	    this.scanProgress(site, numberCrawled, numberCrawled + numberToCrawl);
	}
    /**
     * @return Returns the startNode.
     */
    public SiteNode getStartNode() {
        return startNode;
    }
    /**
     * @param startNode The startNode to set.
     */
    public void setStartNode(SiteNode startNode) {
        this.startNode = startNode;
    }


}
