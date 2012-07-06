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

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.api.ApiAction;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiImplementor;
import org.zaproxy.zap.extension.api.ApiView;
import org.zaproxy.zap.model.ScanListenner;
import org.zaproxy.zap.spider.SpiderListener;
import org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus;

public class SpiderAPI extends ApiImplementor implements ScanListenner, SpiderListener {

	private static Logger log = Logger.getLogger(SpiderAPI.class);

	/** The Constant PREFIX defining the name/prefix of the api. */
	private static final String PREFIX = "spider";

	/** The Constant ACTION_START_SCAN that defines the action of starting a new scan. */
	private static final String ACTION_START_SCAN = "start_scan";

	/** The Constant ACTION_STOP_SCAN that defines the action of stopping a pending scan. */
	private static final String ACTION_STOP_SCAN = "stop_scan";

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

	/**
	 * The Constant ACTION_SCANSITE_PARAM_URL that defines the parameter defining the url of the
	 * scan.
	 */
	private static final String ACTION_SCANSITE_PARAM_URL = "url";

	/** The spider extension. */
	private ExtensionSpider extension;

	/** The spider thread. */
	private SpiderThread spiderThread;

	/** The current progress of the spider. */
	private int progress;

	/** The URIs found during the scan. */
	private ArrayList<String> foundURIs;

	/**
	 * Instantiates a new spider API.
	 * 
	 * @param extension the extension
	 */
	public SpiderAPI(ExtensionSpider extension) {
		this.extension = extension;
		this.foundURIs = new ArrayList<String>();
		// Register the actions
		List<String> scanParams = new ArrayList<String>(1);
		scanParams.add(ACTION_SCANSITE_PARAM_URL);
		this.addApiAction(new ApiAction(ACTION_START_SCAN, scanParams));
		this.addApiAction(new ApiAction(ACTION_STOP_SCAN));

		// Register the views
		this.addApiView(new ApiView(VIEW_STATUS));
		this.addApiView(new ApiView(VIEW_RESULTS));

	}

	@Override
	public String getPrefix() {
		return PREFIX;
	}

	@Override
	public JSON handleApiAction(String name, JSONObject params) throws ApiException {
		log.debug("Request for handleApiAction: " + name + " (params: " + params.toString() + ")");

		// The action is to start a new Scan
		if (ACTION_START_SCAN.equals(name)) {
			String url = params.getString(ACTION_SCANSITE_PARAM_URL);

			// Check for requred parameter
			if (url == null || url.length() == 0) {
				throw new ApiException(ApiException.Type.MISSING_PARAMETER, ACTION_SCANSITE_PARAM_URL);
			}

			scanURL(url);
		}
		// The action is to stop a pending scan
		else if (ACTION_STOP_SCAN.equals(name)) {
			if (spiderThread != null)
				spiderThread.stopScan();
		} else {
			throw new ApiException(ApiException.Type.BAD_ACTION);
		}
		JSONArray result = new JSONArray();
		result.add("OK");
		return result;
	}
	
	private boolean scanInProgress () {
		return spiderThread != null && ! spiderThread.isStopped();
	}

	/**
	 * Start the scan of an url.
	 * 
	 * @param url the url
	 * @throws ApiException the api exception
	 */
	private void scanURL(String url) throws ApiException {
		log.debug("API Spider scanning url: " + url);
		if (scanInProgress()) {

			throw new ApiException(ApiException.Type.SCAN_IN_PROGRESS);
		}

		// Try to find node, if any
		SiteNode startNode;
		try {
			startNode = Model.getSingleton().getSession().getSiteTree().findNode(new URI(url, true));
		} catch (URIException e) {
			throw new ApiException(ApiException.Type.URL_NOT_FOUND);
		}

		// Start the scan
		this.foundURIs.clear();
		this.progress = 0;

		spiderThread = new SpiderThread(extension, "API", this, extension.getSpiderParam());
		spiderThread.setStartNode(startNode);
		spiderThread.addSpiderListener(this);
		spiderThread.start();

	}

	@Override
	public JSON handleApiView(String name, JSONObject params) throws ApiException {
		JSONArray result = new JSONArray();
		if (VIEW_STATUS.equals(name)) {
			result.add(Integer.toString(progress));
		} else if (VIEW_RESULTS.equals(name)) {
			for (String s : foundURIs)
				result.add(s);
		} else {
			throw new ApiException(ApiException.Type.BAD_VIEW);
		}
		return result;
	}

	@Override
	public String viewResultToXML(String name, JSON result) {
		XMLSerializer serializer = new XMLSerializer();
		if (VIEW_STATUS.equals(name)) {
			serializer.setArrayName("status");
			serializer.setElementName("percent");
		} else if (VIEW_RESULTS.equals(name)) {
			serializer.setArrayName("uris_found");
			serializer.setElementName("uri");
		}
		return serializer.write(result);
	}

	@Override
	public String actionResultToXML(String name, JSON result) {
		XMLSerializer serializer = new XMLSerializer();
		serializer.setArrayName("result");
		return serializer.write(result);
	}

	@Override
	public void scanFinshed(String host) {
		// Do nothing
		// TODO: should remove scan listenner
	}

	@Override
	public void scanProgress(String host, int progress, int maximum) {
		// Do nothing
		// TODO: should remove scan listenner
	}

	@Override
	public void spiderProgress(int percentageComplete, int numberCrawled, int numberToCrawl) {
		this.progress = percentageComplete;
	}

	@Override
	public void foundURI(String uri, FetchStatus status) {
		if (status.equals(FetchStatus.VALID))
			foundURIs.add(uri);
	}

	@Override
	public void readURI(HttpMessage msg) {
		// Ignore
	}

	@Override
	public void spiderComplete(boolean successful) {
		this.progress = 100;
	}

}
