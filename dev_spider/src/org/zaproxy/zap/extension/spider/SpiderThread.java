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
import java.util.List;

import javax.swing.DefaultListModel;

import org.apache.log4j.Logger;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.SiteMap;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.zaproxy.zap.model.ScanListenner;
import org.zaproxy.zap.model.ScanThread;
import org.zaproxy.zap.spider.Spider;
import org.zaproxy.zap.spider.SpiderListener;
import org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus;

/**
 * The Class SpiderThread that controls the spidering process on a particular site. Being a
 * ScanThread, it also handles the update of the graphical UI and any other "extension-level"
 * required actions.
 */
public class SpiderThread extends ScanThread implements SpiderListener {

	/** Whether the scanning process is stopped. */
	private boolean stopScan = false;

	/** Whether the scanning process is paused. */
	private boolean isPaused = false;

	/** The related extension. */
	private ExtensionSpider extension;

	/** The spider. */
	private Spider spider = null;

	/** The pending spider listeners which will be added to the Spider as soon at is initialized. */
	private List<SpiderListener> pendingSpiderListeners;

	/** The spider done. */
	private int spiderDone = 0;

	/** The spider todo. */
	private int spiderTodo = 100; // Will get updated ;)

	/** The Constant log used for logging. */
	private static final Logger log = Logger.getLogger(SpiderThread.class);

	/** The just scan in scope. */
	private boolean justScanInScope = false;

	/** The scan children. */
	private boolean scanChildren = false;

	/**
	 * Instantiates a new spider thread.
	 * 
	 * @param extension the extension
	 * @param site the site
	 * @param listenner the scan listener
	 */
	public SpiderThread(ExtensionSpider extension, String site, ScanListenner listenner) {
		super(site, listenner);
		this.extension = extension;
		this.site = site;
		this.pendingSpiderListeners = new LinkedList<SpiderListener>();
		log.debug("Initializing spider thread for site: " + site);
	}

	/* (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run() */
	@Override
	public void run() {
		runScan();
	}

	/**
	 * Runs the scan.
	 */
	private void runScan() {
		// Do the scan
		spiderDone = 0;
		Date start = new Date();
		log.info("Starting spidering scan on " + site + " at " + start);
		startSpider();
	}

	/* (non-Javadoc)
	 * 
	 * @see org.zaproxy.zap.model.ScanThread#stopScan() */
	@Override
	public void stopScan() {
		if (spider != null) {
			spider.stop();
		}
		stopScan = true;
		this.listenner.scanFinshed(site);
	}

	/* (non-Javadoc)
	 * 
	 * @see org.zaproxy.zap.model.ScanThread#isStopped() */
	@Override
	public boolean isStopped() {
		return stopScan;
	}

	/* (non-Javadoc)
	 * 
	 * @see org.zaproxy.zap.model.GenericScanner#getList() */
	@Override
	public DefaultListModel getList() {
		// Not used, as the SpiderPanel is relying on a TableModel
		return null;
	}

	/* (non-Javadoc)
	 * 
	 * @see org.zaproxy.zap.model.ScanThread#pauseScan() */
	@Override
	public void pauseScan() {
		if (spider != null) {
			spider.pause();
		}
		this.isPaused = true;
	}

	/* (non-Javadoc)
	 * 
	 * @see org.zaproxy.zap.model.ScanThread#resumeScan() */
	@Override
	public void resumeScan() {
		if (spider != null) {
			spider.resume();
		}
		this.isPaused = false;
	}

	/* (non-Javadoc)
	 * 
	 * @see org.zaproxy.zap.model.ScanThread#isPaused() */
	@Override
	public boolean isPaused() {
		return this.isPaused;
	}

	/* (non-Javadoc)
	 * 
	 * @see org.zaproxy.zap.model.ScanThread#getMaximum() */
	@Override
	public int getMaximum() {
		return this.spiderDone + this.spiderTodo;
	}

	/**
	 * Start spider.
	 */
	public void startSpider() {
		if (startNode == null) {
			log.error("Spider: No start node set for site " + site);
			return;
		}

		if (spider == null) {
			try {
				extension
						.getModel()
						.getDb()
						.getTableHistory()
						.deleteHistoryType(extension.getModel().getSession().getSessionId(),
								HistoryReference.TYPE_SPIDER_SEED);

			} catch (SQLException e) {
				log.error(e.getMessage(), e);
			}

			spider = new Spider(extension.getSpiderParam(),
					extension.getModel().getOptionsParam().getConnectionParam(), extension.getModel());
			spider.addSpiderListener(this);
			// Add the pending listeners
			for (SpiderListener l : pendingSpiderListeners)
				spider.addSpiderListener(l);

			inOrderSeed(spider, startNode);
			// TODO: Debugging purpose
			// try {
			// spider.addSeed(new
			// URI("http://localhost:8080/Wavsep/spider/SpiderMediumTest8NonHTMLFiles/", true));
			// } catch (URIException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// } catch (NullPointerException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
		}

		extension.getSpiderPanel().setTabFocus();
		spider.setExcludeList(extension.getExcludeList());

		try {
			spider.start();

		} catch (NullPointerException e1) {
			log.error(e1.getMessage(), e1);
		}
	}

	/**
	 * In order seed.
	 * 
	 * @param spider the spider
	 * @param node the node
	 */
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

	/* (non-Javadoc)
	 * 
	 * @see org.zaproxy.zap.spider.SpiderListener#spiderComplete(boolean) */
	@Override
	public void spiderComplete(boolean successful) {
		try {
			extension
					.getModel()
					.getDb()
					.getTableHistory()
					.deleteHistoryType(extension.getModel().getSession().getSessionId(),
							HistoryReference.TYPE_SPIDER_SEED);

		} catch (SQLException e) {
			log.warn(e.getMessage(), e);
		}
		log.warn("Spider scanning complete: " + successful);
		stopScan = true;
		this.listenner.scanFinshed(site);

	}

	/* (non-Javadoc)
	 * 
	 * @see org.zaproxy.zap.spider.SpiderListener#foundURI(java.lang.String,
	 * org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus) */
	@Override
	public void foundURI(String uri, FetchStatus status) {
		if (extension.getView() != null) {
			if (status == FetchStatus.VALID) {
				extension.getSpiderPanel().addSpiderScanResult(uri, HttpRequestHeader.GET, null, false);
			} else {
				extension.getSpiderPanel().addSpiderScanResult(uri, HttpRequestHeader.GET, status.toString(), true);
			}
		}
	}

	/* (non-Javadoc)
	 * 
	 * @see org.zaproxy.zap.spider.SpiderListener#readURI(org.parosproxy.paros.network.HttpMessage) */
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

	/* (non-Javadoc)
	 * 
	 * @see org.zaproxy.zap.spider.SpiderListener#spiderProgress(int, int, int) */
	@Override
	public void spiderProgress(final int percentageComplete, final int numberCrawled, final int numberToCrawl) {
		this.spiderDone = numberCrawled;
		this.spiderTodo = numberToCrawl;
		this.scanProgress(site, numberCrawled, numberCrawled + numberToCrawl);
	}


	/* (non-Javadoc)
	 * @see org.zaproxy.zap.model.ScanThread#getStartNode()
	 */
	@Override
	public SiteNode getStartNode() {
		return startNode;
	}


	/* (non-Javadoc)
	 * @see org.zaproxy.zap.model.ScanThread#setStartNode(org.parosproxy.paros.model.SiteNode)
	 */
	@Override
	public void setStartNode(SiteNode startNode) {
		this.startNode = startNode;
	}

	/* (non-Javadoc)
	 * 
	 * @see org.zaproxy.zap.model.GenericScanner#reset() */
	@Override
	public void reset() {
	}

	/**
	 * Adds a new spider listener.
	 * 
	 * @param listener the listener
	 */
	public void addSpiderListener(SpiderListener listener) {
		if (spider != null)
			this.spider.addSpiderListener(listener);
		else
			this.pendingSpiderListeners.add(listener);
	}

	/* (non-Javadoc)
	 * 
	 * @see org.zaproxy.zap.model.GenericScanner#setJustScanInScope(boolean) */
	@Override
	public void setJustScanInScope(boolean scanInScope) {
		this.justScanInScope = scanInScope;
	}

	/* (non-Javadoc)
	 * 
	 * @see org.zaproxy.zap.model.GenericScanner#getJustScanInScope() */
	@Override
	public boolean getJustScanInScope() {
		return justScanInScope;
	}

	/* (non-Javadoc)
	 * 
	 * @see org.zaproxy.zap.model.GenericScanner#setScanChildren(boolean) */
	@Override
	public void setScanChildren(boolean scanChildren) {
		this.scanChildren = scanChildren;
	}

	/* (non-Javadoc)
	 * 
	 * @see org.zaproxy.zap.model.ScanThread#getProgress() */
	@Override
	public int getProgress() {
		return this.progress;
	}

}
