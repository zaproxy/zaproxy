/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
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
 * 
 * Note that this extension and the other classes in this package are heavily 
 * based on the original Paros ExtensionSpider! 
 */
package org.zaproxy.zap.extension.spider;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.table.TableModel;

import org.apache.commons.httpclient.URI;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.parosproxy.paros.network.HttpResponseHeader;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.model.GenericScanner2;
import org.zaproxy.zap.model.ScanListenner;
import org.zaproxy.zap.model.ScanListenner2;
import org.zaproxy.zap.model.Target;
import org.zaproxy.zap.spider.SpiderListener;
import org.zaproxy.zap.spider.SpiderParam;
import org.zaproxy.zap.spider.filters.FetchFilter;
import org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus;
import org.zaproxy.zap.spider.filters.ParseFilter;
import org.zaproxy.zap.spider.parser.SpiderParser;
import org.zaproxy.zap.users.User;

public class SpiderScan implements ScanListenner, SpiderListener, GenericScanner2 {

	private static enum State {
		NOT_STARTED,
		RUNNING,
		PAUSED,
		FINISHED
	};

	private static final EnumSet<FetchStatus> FETCH_STATUS_IN_SCOPE = EnumSet.of(FetchStatus.VALID, FetchStatus.SEED);

	private static final EnumSet<FetchStatus> FETCH_STATUS_OUT_OF_SCOPE = EnumSet.of(
			FetchStatus.OUT_OF_SCOPE,
			FetchStatus.OUT_OF_CONTEXT,
			FetchStatus.USER_RULES);

	private final Lock lock;

	private int scanId;
	
	private String displayName = "";

	/**
	 * Counter for number of URIs, in and out of scope, found during the scan.
	 * <p>
	 * The counter is incremented when a new URI is found.
	 * 
	 * @see #foundURI(String, String, FetchStatus)
	 * @see #getNumberOfURIsFound()
	 */
	private AtomicInteger numberOfURIsFound;

	private Set<String> foundURIs;

	private List<SpiderResource> resourcesFound;

	private Set<String> foundURIsOutOfScope;

	private SpiderThread spiderThread = null;

	private State state;

	private int progress;
	
	private ScanListenner2 listener = null;

	/**
	 * The table model of the messages sent.
	 * <p>
	 * Lazily initialised.
	 * 
	 * @see #getMessagesTableModel()
	 * @see #readURI(HttpMessage)
	 */
	private SpiderMessagesTableModel messagesTableModel;

	public SpiderScan(ExtensionSpider extension, SpiderParam spiderParams, Target target, URI spiderURI, User scanUser, int scanId) {
		lock = new ReentrantLock();
		this.scanId = scanId;

		numberOfURIsFound = new AtomicInteger();
		foundURIs = Collections.synchronizedSet(new HashSet<String>());
		resourcesFound = Collections.synchronizedList(new ArrayList<SpiderResource>());
		foundURIsOutOfScope = Collections.synchronizedSet(new HashSet<String>());

		state = State.NOT_STARTED;

		spiderThread = new SpiderThread(extension, spiderParams, "SpiderApi-" + scanId, this);

		spiderThread.setStartURI(spiderURI);
		spiderThread.setStartNode(target.getStartNode());
		spiderThread.setScanContext(target.getContext());
		spiderThread.setScanAsUser(scanUser);
		spiderThread.setJustScanInScope(target.isInScopeOnly());
		spiderThread.setScanChildren(target.isRecurse());
	}

	/**
	 * Returns the ID of the scan.
	 *
	 * @return the ID of the scan
	 */
	@Override
	public int getScanId() {
		return scanId;
	}

	/**
	 * Returns the {@code String} representation of the scan state (not started, running, paused or finished).
	 *
	 * @return the {@code String} representation of the scan state.
	 */
	public String getState() {
		lock.lock();
		try {
			return state.toString();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Returns the progress of the scan, an integer between 0 and 100.
	 *
	 * @return the progress of the scan.
	 */
	@Override
	public int getProgress() {
		return progress;
	}

	/**
	 * Starts the scan.
	 * <p>
	 * The call to this method has no effect if the scan was already started.
	 * </p>
	 */
	public void start() {
		lock.lock();
		try {
			if (State.NOT_STARTED.equals(state)) {
				spiderThread.addSpiderListener(this);
				spiderThread.start();
				state = State.RUNNING;
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Pauses the scan.
	 * <p>
	 * The call to this method has no effect if the scan is not running.
	 * </p>
	 */
	@Override
	public void pauseScan() {
		lock.lock();
		try {
			if (State.RUNNING.equals(state)) {
				spiderThread.pauseScan();
				state = State.PAUSED;
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Resumes the scan.
	 * <p>
	 * The call to this method has no effect if the scan is not paused.
	 * </p>
	 */
	@Override
	public void resumeScan() {
		lock.lock();
		try {
			if (State.PAUSED.equals(state)) {
				spiderThread.resumeScan();
				state = State.RUNNING;
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Stops the scan.
	 * <p>
	 * The call to this method has no effect if the scan was not yet started or has already finished.
	 * </p>
	 */
	@Override
	public void stopScan() {
		lock.lock();
		try {
			if (!State.NOT_STARTED.equals(state) && !State.FINISHED.equals(state)) {
				spiderThread.stopScan();
				state = State.FINISHED;
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Returns the URLs found during the scan.
	 * <p>
	 * <strong>Note:</strong> Iterations must be {@code synchronized} on returned object. Failing to do so might result in
	 * {@code ConcurrentModificationException}.
	 * </p>
	 *
	 * @return the URLs found during the scan
	 * @see ConcurrentModificationException
	 */
	public Set<String> getResults() {
		return foundURIs;
	}

	/**
	 * Returns the resources found during the scan.
	 * <p>
	 * <strong>Note:</strong> Iterations must be {@code synchronized} on returned object. Failing to do so might result in
	 * {@code ConcurrentModificationException}.
	 * </p>
	 *
	 * @return the resources found during the scan
	 * @see ConcurrentModificationException
	 */
	public List<SpiderResource> getResourcesFound() {
		return resourcesFound;
	}

	/**
	 * Returns the URLs, out of scope, found during the scan.
	 * <p>
	 * <strong>Note:</strong> Iterations must be {@code synchronized} on returned object. Failing to do so might result in
	 * {@code ConcurrentModificationException}.
	 * </p>
	 *
	 * @return the URLs, out of scope, found during the scan
	 * @see ConcurrentModificationException
	 */
	public Set<String> getResultsOutOfScope() {
		return foundURIsOutOfScope;
	}

	@Override
	public void readURI(HttpMessage msg) {
		HttpRequestHeader requestHeader = msg.getRequestHeader();
		HttpResponseHeader responseHeader = msg.getResponseHeader();
		resourcesFound.add(new SpiderResource(
				msg.getHistoryRef().getHistoryId(),
				requestHeader.getMethod(),
				requestHeader.getURI().toString(),
				responseHeader.getStatusCode(),
				responseHeader.getReasonPhrase()));

		if (View.isInitialised()) {
			addMessageToMessagesTableModel(msg);
		}
	}

	private void addMessageToMessagesTableModel(final HttpMessage msg) {
		if (EventQueue.isDispatchThread()) {
			if (messagesTableModel == null) {
				messagesTableModel = new SpiderMessagesTableModel();
			}
			messagesTableModel.addHistoryReference(msg.getHistoryRef());
			return;
		}

		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				addMessageToMessagesTableModel(msg);
			}
		});
	}

	@Override
	public void spiderComplete(boolean successful) {
		lock.lock();
		try {
			state = State.FINISHED;
		} finally {
			lock.unlock();
		}
		if (listener != null) {
			listener.scanFinshed(this.getScanId(), this.getDisplayName());
		}
	}

	@Override
	public void spiderProgress(int percentageComplete, int numberCrawled, int numberToCrawl) {
		this.progress = percentageComplete;
		if (listener != null) {
			listener.scanProgress(this.getScanId(), this.getDisplayName(), percentageComplete, 100);
		}
	}

	@Override
	public void foundURI(String uri, String method, FetchStatus status) {
		numberOfURIsFound.incrementAndGet();
		if (FETCH_STATUS_IN_SCOPE.contains(status)) {
			foundURIs.add(uri);
		} else if (FETCH_STATUS_OUT_OF_SCOPE.contains(status)) {
			foundURIsOutOfScope.add(uri);
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
	}

	@Override
	public void setScanId(int id) {
		this.scanId = id;
	}

	@Override
	public void setDisplayName(String name) {
		this.displayName = name;
	}

	@Override
	public String getDisplayName() {
		return this.displayName;
	}

	@Override
	public boolean isStopped() {
		return this.spiderThread.isStopped();
	}

	@Override
	public int getMaximum() {
		return 100;
	}

	/**
	 * Gets the number of URIs, in and out of scope, found during the scan.
	 *
	 * @return the number of URIs found during the scan
	 * @since 2.4.3
	 */
	public int getNumberOfURIsFound() {
		return numberOfURIsFound.get();
	}

	@Override
	public boolean isPaused() {
		return this.spiderThread.isPaused();
	}

	@Override
	public boolean isRunning() {
		return this.spiderThread.isRunning();
	}

	@Override
	public void scanFinshed(String host) {
		this.spiderComplete(true);
	}

	@Override
	public void scanProgress(String host, int progress, int maximum) {
	}

	public TableModel getResultsTableModel() {
		return this.spiderThread.getResultsTableModel();
	}

	/**
	 * Gets the {@code TableModel} of the messages sent during the spidering process.
	 *
	 * @return a {@code TableModel} with the messages sent
	 * @since 2.5.0
	 */
	TableModel getMessagesTableModel() {
		if (messagesTableModel == null) {
			messagesTableModel = new SpiderMessagesTableModel();
		}
		return messagesTableModel;
	}

	public void setListener(ScanListenner2 listener) {
		this.listener = listener;
	}

	public void setCustomSpiderParsers(List<SpiderParser> customSpiderParsers) {
		spiderThread.setCustomSpiderParsers(customSpiderParsers);
	}

	public void setCustomFetchFilters(List<FetchFilter> customFetchFilters) {
		spiderThread.setCustomFetchFilters(customFetchFilters);
	}

	public void setCustomParseFilters(List<ParseFilter> customParseFilters) {
		spiderThread.setCustomParseFilters(customParseFilters);
	}

	/**
	 * Clears the table model of the HTTP messages sent.
	 * 
	 * @since 2.5.0
	 * @see #getMessagesTableModel()
	 */
	void clear() {
		if (messagesTableModel != null) {
			messagesTableModel.clear();
			messagesTableModel = null;
		}
	}
	
}
