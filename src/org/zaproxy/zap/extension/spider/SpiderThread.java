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

import java.awt.EventQueue;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javax.swing.DefaultListModel;

import org.apache.commons.httpclient.URI;
import org.apache.log4j.Logger;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.ScanListenner;
import org.zaproxy.zap.model.ScanThread;
import org.zaproxy.zap.model.SessionStructure;
import org.zaproxy.zap.model.TechSet;
import org.zaproxy.zap.spider.Spider;
import org.zaproxy.zap.spider.SpiderListener;
import org.zaproxy.zap.spider.SpiderParam;
import org.zaproxy.zap.spider.filters.FetchFilter;
import org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus;
import org.zaproxy.zap.spider.filters.ParseFilter;
import org.zaproxy.zap.spider.parser.SpiderParser;
import org.zaproxy.zap.users.User;

/**
 * The Class SpiderThread that controls the spidering process on a particular site. Being a
 * ScanThread, it also handles the update of the graphical UI and any other "extension-level"
 * required actions.
 */
public class SpiderThread extends ScanThread implements SpiderListener {

	/** Whether the scanning process has stopped (either completed, either by user request). */
	private boolean stopScan = false;

	/** Whether the scanning process is paused. */
	private boolean isPaused = false;

	/** Whether the scanning process is running. */
	private boolean isAlive = false;

	/** The related extension. */
	private ExtensionSpider extension;

	/** The spider. */
	private Spider spider = null;

	/** The pending spider listeners which will be added to the Spider as soon at is initialized. */
	private List<SpiderListener> pendingSpiderListeners;

	/** The spider done. */
	private int spiderDone = 0;

	/** The spider todo. It will be updated by the "spiderProgress()" method. */
	private int spiderTodo = 1;

	/** The Constant log used for logging. */
	private static final Logger log = Logger.getLogger(SpiderThread.class);

	/** The just scan in scope. */
	private boolean justScanInScope = false;

	/** The scan children. */
	private boolean scanChildren = false;

	/** The scan context. */
	private Context scanContext = null;

	/** The scan user. */
	private User scanUser = null;

	/** The results model. */
	private SpiderPanelTableModel resultsModel;

	/** The start uri. */
	private URI startURI = null;
	
	private SpiderParam spiderParams;
	
	private List<SpiderParser> customSpiderParsers = null;

	private List<FetchFilter> customFetchFilters = null;

	private List<ParseFilter> customParseFilters = null;

	/**
	 * Instantiates a new spider thread.
	 * 
	 * @param extension the extension
	 * @param site the site
	 * @param listenner the scan listener
	 */
	public SpiderThread(ExtensionSpider extension, SpiderParam spiderParams, String site, ScanListenner listenner) {
		super(site, listenner);
		log.debug("Initializing spider thread for site: " + site);
		this.extension = extension;
		this.site = site;
		this.pendingSpiderListeners = new LinkedList<>();
		this.resultsModel = new SpiderPanelTableModel();
		this.spiderParams = spiderParams;
	}

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
		this.isAlive = true;
	}

	@Override
	public void stopScan() {
		if (spider != null) {
			spider.stop();
		}
		stopScan = true;
		isAlive = false;
		this.listenner.scanFinshed(site);
	}

	@Override
	public boolean isStopped() {
		return stopScan;
	}

	@Override
	public boolean isRunning() {
		return isAlive;
	}

	@Override
	public DefaultListModel<?> getList() {
		// Not used, as the SpiderPanel is relying on a TableModel
		return null;
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

	/**
	 * Start spider.
	 */
	private void startSpider() {

		spider = new Spider(extension, spiderParams, extension.getModel().getOptionsParam()
				.getConnectionParam(), extension.getModel(), this.scanContext);

		// Register this thread as a Spider Listener, so it gets notified of events and is able
		// to manipulate the UI accordingly
		spider.addSpiderListener(this);

		// Add the pending listeners
		for (SpiderListener l : pendingSpiderListeners) {
			spider.addSpiderListener(l);
		}

		// Add the list of excluded uris (added through the Exclude from Spider Popup Menu)
		spider.setExcludeList(extension.getExcludeList());

		// Add seeds accordingly
		addSeeds();

		spider.setScanAsUser(scanUser);
		
		// Add any custom parsers and filters specified
		if (this.customSpiderParsers != null) {
			for (SpiderParser sp : this.customSpiderParsers) {
				spider.addCustomParser(sp);
			}
		}
		if (this.customFetchFilters != null) {
			for (FetchFilter ff : this.customFetchFilters) {
				spider.addFetchFilter(ff);
			}
		}
		if (this.customParseFilters != null) {
			for (ParseFilter pf : this.customParseFilters) {
				spider.addParseFilter(pf);
			}
		}

		// Start the spider
		spider.start();
	}

	/**
	 * Adds the initial seeds, with following constraints:
	 * <ul>
	 * <li>If a {@link #scanContext context} is provided:
	 * <ul>
	 * <li>{@link #startURI Start URI}, if in context;</li>
	 * <li>{@link #startNode Start node}, if in context;</li>
	 * <li>All nodes in the context;</li>
	 * </ul>
	 * </li>
	 * <li>If spidering just in {@link #justScanInScope scope}:
	 * <ul>
	 * <li>Start URI, if in scope;</li>
	 * <li>Start node, if in scope;</li>
	 * <li>All nodes in scope;</li>
	 * </ul>
	 * </li>
	 * <li>If there's no context/scope restriction:
	 * <ul>
	 * <li>Start URI;</li>
	 * <li>Start node, also:
	 * <ul>
	 * <li>Child nodes, if {@link #scanChildren spidering "recursively"}.</li>
	 * </ul>
	 * </ul>
	 * </li>
	 * </ul>
	 * 
	 * @see #addStartSeeds()
	 */
	private void addSeeds() {
		addStartSeeds();

		List<SiteNode> nodesInScope = Collections.emptyList();
		if (this.scanContext != null) {
			log.debug("Adding seed for Scan of all in context " + scanContext.getName());
			nodesInScope = this.scanContext.getNodesInContextFromSiteTree();
		} else if (justScanInScope) {
			log.debug("Adding seed for Scan of all in scope.");
			nodesInScope = Model.getSingleton().getSession().getNodesInScopeFromSiteTree();
		}

		if (!nodesInScope.isEmpty()) {
			for (SiteNode node : nodesInScope) {
				addSeed(node);
			}
		}
	}

	/**
	 * Adds the start seeds ({@link #startNode start node} and {@link #startURI start URI}) to the spider.
	 * 
	 * @see #addSeeds()
	 */
	private void addStartSeeds() {
		if (scanContext != null) {
			if (startNode != null && scanContext.isInContext(startNode)) {
				addSeed(startNode);
			}
			if (startURI != null && scanContext.isInContext(startURI.toString())) {
				spider.addSeed(startURI);
			}
			return;
		}

		if (justScanInScope) {
			if (startNode != null && Model.getSingleton().getSession().isInScope(startNode)) {
				addSeed(startNode);
			}
			if (startURI != null && Model.getSingleton().getSession().isInScope(startURI.toString())) {
				spider.addSeed(startURI);
			}
			return;
		}

		if (startNode != null) {
			addSeeds(startNode);
		}
		if (startURI != null) {
			spider.addSeed(startURI);
		}
	}

	/**
	 * Adds the given node as seed, if the corresponding message is not an image.
	 *
	 * @param node the node that will be added as seed
	 */
	private void addSeed(SiteNode node) {
		try {
			if (!node.isRoot() && node.getHistoryReference() != null) {
				HttpMessage msg = node.getHistoryReference().getHttpMessage();
				if (!msg.getResponseHeader().isImage()) {
					spider.addSeed(msg);
				}
			}
		} catch (Exception e) {
			log.error("Error while adding seed for Spider scan: " + e.getMessage(), e);
		}
	}

	/**
	 * Adds as seeds the given node and, if {@link #scanChildren} is {@code true}, the children nodes.
	 * 
	 * @param node the node that will be added as seed and possible the children nodes
	 */
	private void addSeeds(SiteNode node) {
		// Add the current node
		addSeed(node);

		// If the "scanChildren" option is enabled, add them
		if (scanChildren) {
			@SuppressWarnings("unchecked")
			Enumeration<SiteNode> en = node.children();
			while (en.hasMoreElements()) {
				SiteNode sn = en.nextElement();
				addSeeds(sn);
			}
		}
	}

	@Override
	public void spiderComplete(boolean successful) {
		log.info("Spider scanning complete: " + successful);
		stopScan = true;
		this.isAlive = false;
		this.listenner.scanFinshed(site);
	}

	@Override
	public void foundURI(String uri, String method, FetchStatus status) {
		if (extension.getView() != null) {

			// Add the new result
			if (status == FetchStatus.VALID) {
				resultsModel.addScanResult(uri, method, null, false);
			} else if (status == FetchStatus.SEED) {
				resultsModel.addScanResult(uri, method, "SEED", false);
			} else {
				resultsModel.addScanResult(uri, method, status.toString(), true);
			}

			// Update the count of found URIs
			extension.getSpiderPanel().updateFoundCount();

		}
	}

	@Override
	public void readURI(final HttpMessage msg) {
		// Add the read message to the Site Map (tree or db structure)
		try {
			final HistoryReference historyRef = new HistoryReference(extension.getModel().getSession(),
					HistoryReference.TYPE_SPIDER, msg);

			addMessageToSitesTree(historyRef, msg);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * Adds the given message to the sites tree.
	 *
	 * @param historyReference the history reference of the message, must not be {@code null}
	 * @param message the actual message, must not be {@code null}
	 */
	private static void addMessageToSitesTree(final HistoryReference historyReference, final HttpMessage message) {
		if (View.isInitialised() && !EventQueue.isDispatchThread()) {
			EventQueue.invokeLater(new Runnable() {

				@Override
				public void run() {
					addMessageToSitesTree(historyReference, message);
				}
			});
			return;
		}

		SessionStructure.addPath(Model.getSingleton().getSession(), historyReference, message);
	}

	@Override
	public void spiderProgress(final int percentageComplete, final int numberCrawled, final int numberToCrawl) {
		this.spiderDone = numberCrawled;
		this.spiderTodo = numberToCrawl;
		this.scanProgress(site, numberCrawled, numberCrawled + numberToCrawl);
	}

	@Override
	public SiteNode getStartNode() {
		return startNode;
	}

	@Override
	public void setStartNode(SiteNode startNode) {
		this.startNode = startNode;
	}

	/**
	 * Sets the start uri. This will be used if no startNode is identified.
	 * 
	 * @param startURI the new start uri
	 */
	public void setStartURI(URI startURI) {
		this.startURI = startURI;
	}

	@Override
	public void reset() {
		this.resultsModel.removeAllElements();
	}

	/**
	 * Adds a new spider listener.
	 * 
	 * @param listener the listener
	 */
	public void addSpiderListener(SpiderListener listener) {
		if (spider != null) {
			this.spider.addSpiderListener(listener);
		} else {
			this.pendingSpiderListeners.add(listener);
		}
	}

	@Override
	public void setJustScanInScope(boolean scanInScope) {
		this.justScanInScope = scanInScope;
	}

	@Override
	public boolean getJustScanInScope() {
		return justScanInScope;
	}

	@Override
	public void setScanChildren(boolean scanChildren) {
		this.scanChildren = scanChildren;
	}

	@Override
	public int getProgress() {
		return this.progress;
	}

	/**
	 * Gets the results table model.
	 * 
	 * @return the results table model
	 */
	public SpiderPanelTableModel getResultsTableModel() {
		return this.resultsModel;
	}

	@Override
	public void setScanContext(Context context) {
		this.scanContext = context;
	}

	@Override
	public void setScanAsUser(User user) {
		this.scanUser = user;
	}

	@Override
	public void setTechSet(TechSet techSet) {
		// Ignore
	}

	public void setCustomSpiderParsers(List<SpiderParser> customSpiderParsers) {
		this.customSpiderParsers = customSpiderParsers;
	}

	public void setCustomFetchFilters(List<FetchFilter> customFetchFilters) {
		this.customFetchFilters  = customFetchFilters;
	}

	public void setCustomParseFilters(List<ParseFilter> customParseFilters) {
		this.customParseFilters  = customParseFilters;
	}

}
