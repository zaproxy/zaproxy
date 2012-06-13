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
import java.util.LinkedList;

import javax.swing.DefaultListModel;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.http.HttpResponse;
import org.apache.log4j.Logger;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.SiteMap;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.parosproxy.paros.network.HttpSender;
import org.zaproxy.zap.model.ScanListenner;
import org.zaproxy.zap.model.ScanThread;
import org.zaproxy.zap.spider.Spider;
import org.zaproxy.zap.spider.SpiderListener;
import org.zaproxy.zap.spider.SpiderParam;
import org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus;
import org.zaproxy.zap.utils.SortedListModel;

import com.sun.jndi.toolkit.url.Uri;

public class SpiderThread extends ScanThread implements ScanListenner, SpiderListener {

	private String site;
	private SortedListModel list;
	private boolean stopScan = false;
	private boolean isPaused = false;
	private ScanListenner listenner;
	private int progress = 0;
	private ExtensionSpider extension;

	private Spider spider = null;
	private SiteNode startNode = null;

	LinkedList<SpiderListener> pendingSpiderListeners;

	private int spiderDone = 0;
	private int spiderTodo = 100; // Will get updated ;)

	private static Logger log = Logger.getLogger(SpiderThread.class);

	public SpiderThread(ExtensionSpider extension, String site, ScanListenner listenner, SpiderParam portScanParam) {
		super(site, listenner);
		this.extension = extension;
		this.site = site;
		this.listenner = listenner;
		this.pendingSpiderListeners = new LinkedList<SpiderListener>();

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
		list.clear();
		startSpider();
	}

	@Override
	public void stopScan() {
		if (spider != null) {
			spider.stop();
		}
		stopScan = true;
		this.listenner.scanFinshed(site);
	}

	@Override
	public boolean isStopped() {
		return stopScan;
	}

	@Override
	public String getSite() {
		return site;
	}

	@Override
	public int getProgress() {
		return this.spiderDone;
	}

	@Override
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

	public void startSpider() {
		if (startNode == null) {
			log.error("Spider: No start node set for site " + site);
			// TODO: Debugging purpose
			// return;
		}

		if (spider == null) {
			try {
				extension
						.getModel()
						.getDb()
						.getTableHistory()
						.deleteHistoryType(extension.getModel().getSession().getSessionId(),
								HistoryReference.TYPE_SPIDER_SEED);
				extension
						.getModel()
						.getDb()
						.getTableHistory()
						.deleteHistoryType(extension.getModel().getSession().getSessionId(),
								HistoryReference.TYPE_SPIDER_VISITED);

			} catch (SQLException e) {
				log.error(e.getMessage(), e);
			}

			spider = new Spider(extension.getSpiderParam(),
					extension.getModel().getOptionsParam().getConnectionParam(), extension.getModel());
			spider.addSpiderListener(this);
			// Add the pending listeners
			for (SpiderListener l : pendingSpiderListeners)
				spider.addSpiderListener(l);

			// TODO: Debugging purpose
			// inOrderSeed(spider, startNode);
			try {
				spider.addSeed(new URI("http://localhost:8080/Wavsep/spider/index-start.jsp", true));
			} catch (URIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NullPointerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		extension.getSpiderPanel().setTabFocus();
		spider.setExcludeList(extension.getExcludeList());
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			spider.start();

		} catch (NullPointerException e1) {
			log.error(e1.getMessage(), e1);
		}
	}

	private void inOrderSeed(Spider spider, SiteNode node) {

		try {
			if (!node.isRoot() && node.getHistoryReference() != null) {
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
			for (int i = 0; i < node.getChildCount(); i++) {
				try {
					inOrderSeed(spider, (SiteNode) node.getChildAt(i));
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		}
	}

	@Override
	public void spiderComplete(boolean successful) {
		try {
			extension
					.getModel()
					.getDb()
					.getTableHistory()
					.deleteHistoryType(extension.getModel().getSession().getSessionId(),
							HistoryReference.TYPE_SPIDER_SEED);
			extension
					.getModel()
					.getDb()
					.getTableHistory()
					.deleteHistoryType(extension.getModel().getSession().getSessionId(),
							HistoryReference.TYPE_SPIDER_VISITED);

		} catch (SQLException e) {
			log.warn(e.getMessage(), e);
		}
		log.warn("Spider scanning complete: " + successful);
		stopScan = true;
		this.listenner.scanFinshed(site);

	}

	@Override
	public void foundURI(String uri, FetchStatus status) {
		if (extension.getView() != null) {
			if (status != FetchStatus.VALID) {
				extension.getSpiderPanel().appendFoundButSkip(uri + " - " + status + "\n");
			} else {
				extension.getSpiderPanel().appendFound(uri + "\n");
			}
		}
	}

	@Override
	public void readURI(HttpMessage msg) {

		SiteMap siteTree = extension.getModel().getSession().getSiteTree();

		HistoryReference historyRef = null;
		try {
			historyRef = new HistoryReference(extension.getModel().getSession(), HistoryReference.TYPE_SPIDER, msg);
			siteTree.addPath(historyRef, msg);
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
	}

	public Spider getSpider() {
		return spider;
	}

	@Override
	public void spiderProgress(final int percentageComplete, final int numberCrawled, final int numberToCrawl) {
		this.spiderDone = numberCrawled;
		this.spiderTodo = numberToCrawl;
		this.scanProgress(site, numberCrawled, numberCrawled + numberToCrawl);
	}

	/**
	 * @return Returns the startNode.
	 */
	@Override
	public SiteNode getStartNode() {
		return startNode;
	}

	/**
	 * @param startNode The startNode to set.
	 */
	@Override
	public void setStartNode(SiteNode startNode) {
		this.startNode = startNode;
	}

	public void reset() {
		this.list = new SortedListModel();
	}

	/**
	 * Adds a new spider listener.
	 * 
	 * @param listener the listener
	 */
	public void addSpiderListener(SpiderListener listener) {
		this.pendingSpiderListeners.add(listener);
		if (spider != null)
			this.spider.addSpiderListener(listener);
	}

}
