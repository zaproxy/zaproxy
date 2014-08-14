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
 */
package org.zaproxy.zap.extension.spider;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.sf.json.JSONObject;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.parosproxy.paros.network.HttpResponseHeader;
import org.zaproxy.zap.extension.api.ApiAction;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiException.Type;
import org.zaproxy.zap.extension.api.ApiImplementor;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.extension.api.ApiResponseElement;
import org.zaproxy.zap.extension.api.ApiResponseList;
import org.zaproxy.zap.extension.api.ApiResponseSet;
import org.zaproxy.zap.extension.api.ApiView;
import org.zaproxy.zap.extension.users.ExtensionUserManagement;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.ScanListenner;
import org.zaproxy.zap.spider.SpiderListener;
import org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus;
import org.zaproxy.zap.users.User;
import org.zaproxy.zap.utils.ApiUtils;

public class SpiderAPI extends ApiImplementor {

	private static final Logger log = Logger.getLogger(SpiderAPI.class);

	/** The Constant PREFIX defining the name/prefix of the api. */
	private static final String PREFIX = "spider";
	/** The Constant ACTION_START_SCAN that defines the action of starting a new scan. */
	private static final String ACTION_START_SCAN = "scan";
	private static final String ACTION_START_SCAN_AS_USER = "scanAsUser";

	private static final String ACTION_PAUSE_SCAN = "pause";
	private static final String ACTION_RESUME_SCAN = "resume";
	/** The Constant ACTION_STOP_SCAN that defines the action of stopping a pending scan. */
	private static final String ACTION_STOP_SCAN = "stop";
	private static final String ACTION_PAUSE_ALL_SCANS = "pauseAllScans";
	private static final String ACTION_RESUME_ALL_SCANS = "resumeAllScans";
	private static final String ACTION_STOP_ALL_SCANS = "stopAllScans";
	private static final String ACTION_REMOVE_SCAN = "removeScan";
	private static final String ACTION_REMOVE_ALL_SCANS = "removeAllScans";

	/**
	 * The Constant VIEW_STATUS that defines the view which describes the current status of the
	 * scan.
	 */
	private static final String VIEW_STATUS = "status";

	/**
	 * The Constant VIEW_RESULTS that defines the view which describes the urls found during the
	 * scan.
	 */
	private static final String VIEW_RESULTS = "results";
	private static final String VIEW_FULL_RESULTS = "fullResults";
	private static final String VIEW_SCANS = "scans";

	/**
	 * The Constant PARAM_URL that defines the parameter defining the url of the scan.
	 */
	private static final String PARAM_URL = "url";
	private static final String PARAM_USER_ID = "userId";
	private static final String PARAM_CONTEXT_ID = "contextId";
	private static final String PARAM_REGEX = "regex";
	private static final String PARAM_SCAN_ID = "scanId";

	private static final String ACTION_EXCLUDE_FROM_SCAN = "excludeFromScan";
	private static final String ACTION_CLEAR_EXCLUDED_FROM_SCAN = "clearExcludedFromScan";

	private static final String VIEW_EXCLUDED_FROM_SCAN = "excludedFromScan";

	/** The spider extension. */
	private ExtensionSpider extension;

	/**
	 * The {@code Lock} for exclusive access of instance variables related to multiple spider scans.
	 * 
	 * @see #spiderScans
	 * @see #scanIdCounter
	 * @see #lastSpiderScanAvailable
	 */
	private final Lock spiderScansLock;

	/**
	 * The counter used to give an unique ID to spider scans.
	 * <p>
	 * <strong>Note:</strong> All accesses (both write and read) should be done while holding the {@code Lock}
	 * {@code spiderScansLock}.
	 * </p>
	 * 
	 * @see #spiderScansLock
	 * @see #scanURL(String, User)
	 */
	private int scanIdCounter;

	/**
	 * A map that contains all {@code SpiderScan}s created (and not yet removed). Used to control (i.e. pause/resume and stop)
	 * the multiple spider scans and get its results. The instance variable is never {@code null}. The map key is the ID of the
	 * scan.
	 * <p>
	 * <strong>Note:</strong> All accesses (both write and read) should be done while holding the {@code Lock}
	 * {@code spiderScansLock}.
	 * </p>
	 * 
	 * @see #spiderScansLock
	 * @see #scanURL(String, User)
	 * @see #scanIdCounter
	 */
	private Map<Integer, SpiderScan> spiderScans;

	/**
	 * The last {@code SpiderScan} available. Might be {@code null}, when no scan was created or all scans were removed.
	 * <p>
	 * The multiple spider scans are accessed/controlled using its ID but to keep backward compatibility we keep a reference to
	 * the last scan so it's still possible to get the status/results and stop the (last) scan without using a scan ID.
	 * </p>
	 * <p>
	 * <strong>Note:</strong> All accesses (both write and read) should be done while holding the {@code Lock}
	 * {@code spiderScansLock}.
	 * </p>
	 * 
	 * @see #spiderScansLock
	 * @see #scanURL(String, User)
	 */
	private SpiderScan lastSpiderScanAvailable;

	/**
	 * Instantiates a new spider API.
	 * 
	 * @param extension the extension
	 */
	public SpiderAPI(ExtensionSpider extension) {
		this.spiderScansLock = new ReentrantLock();
		this.extension = extension;
		this.spiderScans = new HashMap<>();
		// Register the actions
		this.addApiAction(new ApiAction(ACTION_START_SCAN, new String[] { PARAM_URL }));
		this.addApiAction(new ApiAction(ACTION_START_SCAN_AS_USER, new String[] { PARAM_URL,
				PARAM_CONTEXT_ID, PARAM_USER_ID }));
		this.addApiAction(new ApiAction(ACTION_PAUSE_SCAN, new String[] { PARAM_SCAN_ID }));
		this.addApiAction(new ApiAction(ACTION_RESUME_SCAN, new String[] { PARAM_SCAN_ID }));
		this.addApiAction(new ApiAction(ACTION_STOP_SCAN, null, new String[] { PARAM_SCAN_ID }));
		this.addApiAction(new ApiAction(ACTION_REMOVE_SCAN, new String[] { PARAM_SCAN_ID }));
		this.addApiAction(new ApiAction(ACTION_PAUSE_ALL_SCANS));
		this.addApiAction(new ApiAction(ACTION_RESUME_ALL_SCANS));
		this.addApiAction(new ApiAction(ACTION_STOP_ALL_SCANS));
		this.addApiAction(new ApiAction(ACTION_REMOVE_ALL_SCANS));
		this.addApiAction(new ApiAction(ACTION_CLEAR_EXCLUDED_FROM_SCAN));
		this.addApiAction(new ApiAction(ACTION_EXCLUDE_FROM_SCAN, new String[] { PARAM_REGEX }));

		// Register the views
		this.addApiView(new ApiView(VIEW_STATUS, null, new String[] { PARAM_SCAN_ID }));
		this.addApiView(new ApiView(VIEW_RESULTS, null, new String[] { PARAM_SCAN_ID }));
		this.addApiView(new ApiView(VIEW_FULL_RESULTS, new String[] { PARAM_SCAN_ID }));
		this.addApiView(new ApiView(VIEW_SCANS));
		this.addApiView(new ApiView(VIEW_EXCLUDED_FROM_SCAN));

	}

	@Override
	public String getPrefix() {
		return PREFIX;
	}

	@Override
	public ApiResponse handleApiAction(String name, JSONObject params) throws ApiException {
		log.debug("Request for handleApiAction: " + name + " (params: " + params.toString() + ")");

		switch (name) {
		case ACTION_START_SCAN:
			// The action is to start a new Scan
			String url = ApiUtils.getNonEmptyStringParam(params, PARAM_URL);
			int scanId = scanURL(url, null);
			return new ApiResponseElement(name, Integer.toString(scanId));

		case ACTION_START_SCAN_AS_USER:
			// The action is to start a new Scan from the perspective of a user
			String urlUserScan = ApiUtils.getNonEmptyStringParam(params, PARAM_URL);
			int userID = ApiUtils.getIntParam(params, PARAM_USER_ID);
			ExtensionUserManagement usersExtension = (ExtensionUserManagement) Control.getSingleton()
					.getExtensionLoader().getExtension(ExtensionUserManagement.NAME);
			if (usersExtension == null)
				throw new ApiException(Type.NO_IMPLEMENTOR, ExtensionUserManagement.NAME);
			Context context = ApiUtils.getContextByParamId(params, PARAM_CONTEXT_ID);
			if (!context.isIncluded(urlUserScan))
				throw new ApiException(Type.URL_NOT_IN_CONTEXT, PARAM_CONTEXT_ID);
			User user = usersExtension.getContextUserAuthManager(context.getIndex()).getUserById(userID);
			if (user == null)
				throw new ApiException(Type.USER_NOT_FOUND, PARAM_USER_ID);
			scanId = scanURL(urlUserScan, user);

			return new ApiResponseElement(name, Integer.toString(scanId));

		case ACTION_PAUSE_SCAN:
			getSpiderScan(params).pause();
			break;
		case ACTION_RESUME_SCAN:
			getSpiderScan(params).resume();
			break;
		case ACTION_STOP_SCAN:
			// The action is to stop a pending scan
			SpiderScan spiderScan = getSpiderScan(params);
			if (spiderScan != null) {
				spiderScan.stop();
			}
			break;
		case ACTION_REMOVE_SCAN:
			spiderScansLock.lock();
			try {
				spiderScan = spiderScans.remove(Integer.valueOf(params.getInt(PARAM_SCAN_ID)));
				if (spiderScan == null) {
					throw new ApiException(ApiException.Type.DOES_NOT_EXIST, PARAM_SCAN_ID);
				}
				spiderScan.stop();

				if (lastSpiderScanAvailable == spiderScan) {
					if (spiderScans.isEmpty()) {
						lastSpiderScanAvailable = null;
					} else {
						SpiderScan[] scans = new SpiderScan[spiderScans.size()];
						scans = spiderScans.values().toArray(scans);
						lastSpiderScanAvailable = scans[scans.length - 1];
					}
				}
			} finally {
				spiderScansLock.unlock();
			}
			break;
		case ACTION_PAUSE_ALL_SCANS:
			spiderScansLock.lock();
			try {
				for (SpiderScan scan : spiderScans.values()) {
					scan.pause();
				}
			} finally {
				spiderScansLock.unlock();
			}
			break;
		case ACTION_RESUME_ALL_SCANS:
			spiderScansLock.lock();
			try {
				for (SpiderScan scan : spiderScans.values()) {
					scan.resume();
				}
			} finally {
				spiderScansLock.unlock();
			}
			break;
		case ACTION_STOP_ALL_SCANS:
			spiderScansLock.lock();
			try {
				for (SpiderScan scan : spiderScans.values()) {
					scan.stop();
				}
			} finally {
				spiderScansLock.unlock();
			}
			break;
		case ACTION_REMOVE_ALL_SCANS:
			removeAllScans();
			break;
		case ACTION_CLEAR_EXCLUDED_FROM_SCAN:
			try {
				Session session = Model.getSingleton().getSession();
				session.setExcludeFromSpiderRegexs(new ArrayList<String>());
			} catch (SQLException e) {
				throw new ApiException(ApiException.Type.INTERNAL_ERROR, e.getMessage());
			}
			break;
		case ACTION_EXCLUDE_FROM_SCAN:
			String regex = params.getString(PARAM_REGEX);
			try {
				Session session = Model.getSingleton().getSession();
				session.addExcludeFromSpiderRegex(regex);
			} catch (Exception e) {
				throw new ApiException(ApiException.Type.BAD_FORMAT, PARAM_REGEX);
			}
			break;
		default:
			throw new ApiException(ApiException.Type.BAD_ACTION);
		}
		return ApiResponseElement.OK;
	}

	/**
	 * Returns a {@code SpiderScan} from the available {@code spiderScans} or the {@code lastSpiderScanAvailable}. If a scan ID
	 * ({@code PARAM_SCAN_ID}) is present in the given {@code params} it will be used to the get the {@code SpiderScan} from the
	 * available {@code spiderScans}, otherwise it's returned the {@code lastSpiderScanAvailable}.
	 *
	 * @param params the parameters of the API call
	 * @return the {@code SpiderScan} with the given scan ID or, if not present, the {@code lastSpiderScanAvailable}
	 * @throws ApiException if there's no scan with the given scan ID
	 * @see #PARAM_SCAN_ID
	 * @see #spiderScans
	 * @see #lastSpiderScanAvailable
	 */
	private SpiderScan getSpiderScan(JSONObject params) throws ApiException {
		spiderScansLock.lock();
		try {
			int id = getParam(params, PARAM_SCAN_ID, -1);

			if (id == -1) {
				return lastSpiderScanAvailable;
			}

			SpiderScan spiderScan = spiderScans.get(Integer.valueOf(id));
			if (spiderScan == null) {
				throw new ApiException(ApiException.Type.DOES_NOT_EXIST, PARAM_SCAN_ID);
			}

			return spiderScan;
		} finally {
			spiderScansLock.unlock();
		}
	}

	/**
	 * Starts a spider scan at the given {@code url} and, optionally, with the perspective of the given {@code user}.
	 * <p>
	 * The {@code scanIdCounter} is used to generate the ID of the started spider scan. The started {@code SpiderScan} is saved
	 * in {@code spiderScans} for later access/control, accessible with the returned ID.
	 * </p>
	 * 
	 * @param url the url to start the spider scan
	 * @param user the user to scan as, or null if the scan is done without the perspective of any user
	 * @return the ID of the newly started scan
	 * @throws ApiException if the {@code url} is not valid
	 * @see #scanIdCounter
	 * @see #spiderScans
	 */
	private int scanURL(String url, User user) throws ApiException {
		log.debug("API Spider scanning url: " + url);

		URI startURI;
		try {
			// Try to build uri
			startURI = new URI(url, true);
		} catch (URIException e) {
			throw new ApiException(ApiException.Type.BAD_FORMAT);
		}

		SiteNode startNode = Model.getSingleton().getSession().getSiteTree().findNode(startURI);
		String scheme = startURI.getScheme();
		if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
			throw new ApiException(ApiException.Type.BAD_FORMAT);
		}

		spiderScansLock.lock();
		try {
			int scanId = scanIdCounter++;
			SpiderScan spiderApiScan = new SpiderScan(extension, startURI, startNode, user, scanId);
			spiderScans.put(Integer.valueOf(scanId), spiderApiScan);
			spiderApiScan.start();
			lastSpiderScanAvailable = spiderApiScan;

			return scanId;
		} finally {
			spiderScansLock.unlock();
		}
	}

	@Override
	public ApiResponse handleApiView(String name, JSONObject params) throws ApiException {
		ApiResponse result;
		if (VIEW_STATUS.equals(name)) {
			SpiderScan spiderScan = getSpiderScan(params);
			int progress = 0;
			if (spiderScan != null) {
				progress = spiderScan.getProgress();
			}
			result = new ApiResponseElement(name, Integer.toString(progress));
		} else if (VIEW_RESULTS.equals(name)) {
			result = new ApiResponseList(name);
			SpiderScan spiderScan = getSpiderScan(params);
			if (spiderScan != null) {
				synchronized (spiderScan.getResults()) {
					for (String s : spiderScan.getResults()) {
						((ApiResponseList) result).addItem(new ApiResponseElement("url", s));
					}
				}
			}
		} else if (VIEW_FULL_RESULTS.equals(name)) {
			ApiResponseList resultUrls = new ApiResponseList(name);
			SpiderScan spiderScan = getSpiderScan(params);
			ApiResponseList resultList = new ApiResponseList("urlsInScope");
			synchronized (spiderScan.getResults()) {
				for (SpiderResource sr : spiderScan.getResourcesFound()) {
					Map<String, String> map = new HashMap<>();
					map.put("messageId", Integer.toString(sr.getHistoryId()));
					map.put("method", sr.getMethod());
					map.put("url", sr.getUri());
					map.put("statusCode", Integer.toString(sr.getStatusCode()));
					map.put("statusReason", sr.getStatusReason());
					resultList.addItem(new ApiResponseSet("resource", map));
				}
			}
			resultUrls.addItem(resultList);

			resultList = new ApiResponseList("urlsOutOfScope");
			synchronized (spiderScan.getResultsOutOfScope()) {
				for (String url : spiderScan.getResultsOutOfScope()) {
					resultList.addItem(new ApiResponseElement("url", url));
				}
			}
			resultUrls.addItem(resultList);
			result = resultUrls;
		} else if (VIEW_EXCLUDED_FROM_SCAN.equals(name)) {
			result = new ApiResponseList(name);
			Session session = Model.getSingleton().getSession();
			List<String> regexs = session.getExcludeFromSpiderRegexs();
			for (String regex : regexs) {
				((ApiResponseList) result).addItem(new ApiResponseElement("regex", regex));
			}
		} else if (VIEW_SCANS.equals(name)) {
			ApiResponseList resultList = new ApiResponseList(name);
			spiderScansLock.lock();
			try {
				for (SpiderScan spiderScan : spiderScans.values()) {
					Map<String, String> map = new HashMap<>();
					map.put("id", Integer.toString(spiderScan.getId()));
					map.put("progress", Integer.toString(spiderScan.getProgress()));
					map.put("state", spiderScan.getState());
					resultList.addItem(new ApiResponseSet("scan", map));
				}
			} finally {
				spiderScansLock.unlock();
			}
			result = resultList;
		} else {
			throw new ApiException(ApiException.Type.BAD_VIEW);
		}
		return result;
	}

	void reset() {
		spiderScansLock.lock();
		try {
			removeAllScans();
			this.spiderScans = new HashMap<>();
		} finally {
			spiderScansLock.unlock();
		}
	}

	private void removeAllScans() {
		spiderScansLock.lock();
		try {
			for (Iterator<SpiderScan> it = spiderScans.values().iterator(); it.hasNext();) {
				it.next().stop();
				it.remove();
			}
			lastSpiderScanAvailable = null;
		} finally {
			spiderScansLock.unlock();
		}
	}

	private static class SpiderScan implements ScanListenner, SpiderListener {

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

		private final int id;

		private final Set<String> foundURIs;

		private final List<SpiderResource> resourcesFound;

		private final Set<String> foundURIsOutOfScope;

		private final SpiderThread spiderThread;

		private State state;

		private int progress;

		public SpiderScan(ExtensionSpider extension, URI startURI, SiteNode startNode, User scanUser, int scanId) {
			lock = new ReentrantLock();
			id = scanId;

			foundURIs = Collections.synchronizedSet(new HashSet<String>());
			resourcesFound = Collections.synchronizedList(new ArrayList<SpiderResource>());
			foundURIsOutOfScope = Collections.synchronizedSet(new HashSet<String>());

			state = State.NOT_STARTED;

			spiderThread = new SpiderThread(extension, "SpiderApi-" + id, this);

			if (startNode != null) {
				spiderThread.setStartNode(startNode);
			} else {
				spiderThread.setStartURI(startURI);
			}

			if (scanUser != null) {
				spiderThread.setScanAsUser(scanUser);
			}
		}

		/**
		 * Returns the ID of the scan.
		 *
		 * @return the ID of the scan
		 */
		public int getId() {
			return id;
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
		public void pause() {
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
		public void resume() {
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
		public void stop() {
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
		}

		@Override
		public void spiderComplete(boolean successful) {
			lock.lock();
			try {
				// XXX Do we really need to set the progress to 100?
				// Leave it for now just in case it's needed for backward compatibility.
				progress = 100;
				state = State.FINISHED;
			} finally {
				lock.unlock();
			}
		}

		@Override
		public void scanFinshed(String host) {
		}

		@Override
		public void scanProgress(String host, int progress, int maximum) {
		}

		@Override
		public void spiderProgress(int percentageComplete, int numberCrawled, int numberToCrawl) {
			this.progress = percentageComplete;
		}

		@Override
		public void foundURI(String uri, String method, FetchStatus status) {
			if (FETCH_STATUS_IN_SCOPE.contains(status)) {
				foundURIs.add(uri);
			} else if (FETCH_STATUS_OUT_OF_SCOPE.contains(status)) {
				foundURIsOutOfScope.add(uri);
			}
		}
	}

	/**
	 * A resource (e.g. webpage) found while spidering.
	 * <p>
	 * Contains the HTTP method used to fetch the resource, status code and reason, URI and the ID of the corresponding
	 * (persisted) HTTP message.
	 */
	private static class SpiderResource {

		private final int historyId;
		private final String method;
		private final String uri;
		private final int statusCode;
		private final String statusReason;

		public SpiderResource(int historyId, String method, String uri, int statusCode, String statusReason) {
			this.historyId = historyId;
			this.method = method;
			this.uri = uri;
			this.statusCode = statusCode;
			this.statusReason = statusReason;
		}

		public int getHistoryId() {
			return historyId;
		}

		public String getMethod() {
			return method;
		}

		public String getUri() {
			return uri;
		}

		public int getStatusCode() {
			return statusCode;
		}

		public String getStatusReason() {
			return statusReason;
		}
	}
}
