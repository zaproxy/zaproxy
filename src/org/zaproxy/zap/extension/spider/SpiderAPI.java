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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.SiteNode;
import org.zaproxy.zap.extension.api.ApiAction;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiImplementor;
import org.zaproxy.zap.extension.api.ApiView;
import org.zaproxy.zap.model.ScanListenner;

public class SpiderAPI extends ApiImplementor implements ScanListenner {

    private static Log log = LogFactory.getLog(SpiderAPI.class);

	private static final String PREFIX = "spider";
	private static final String ACTION_SCAN = "scan";
	private static final String VIEW_STATUS = "status";
	private static final String ACTION_SCANSITE_PARAM_URL = "url";
	
	private ExtensionSpider extension;
	private SpiderThread spiderThread = null;
	private int progress = 0;
	
	public SpiderAPI (ExtensionSpider extension) {
		this.extension = extension;
		List<String> scanParams = new ArrayList<String>();
		scanParams.add(ACTION_SCANSITE_PARAM_URL);
		this.addApiAction(new ApiAction(ACTION_SCAN, scanParams));
		this.addApiView(new ApiView(VIEW_STATUS));

	}
	
	@Override
	public String getPrefix() {
		return PREFIX;
	}

	@Override
	public JSON handleApiAction(String name, JSONObject params) throws ApiException {
		log.debug("handleApiAction " + name + " " + params.toString());
		if (ACTION_SCAN.equals(name)) {
			String url = params.getString(ACTION_SCANSITE_PARAM_URL);
			if (url == null || url.length() == 0) {
				throw new ApiException(ApiException.Type.MISSING_PARAMETER, ACTION_SCANSITE_PARAM_URL);
			}
			scanURL(url);

		} else {
			throw new ApiException(ApiException.Type.BAD_ACTION);
		}
		JSONArray result = new JSONArray();
		result.add("OK");
		return result;
	}

	private void scanURL(String url) throws ApiException {
		
		if (spiderThread != null && ! spiderThread.isStopped()) {
			throw new ApiException(ApiException.Type.SCAN_IN_PROGRESS);
		}
		
		// Try to find node
		SiteNode startNode;
		try {
			startNode = Model.getSingleton().getSession().getSiteTree().findNode(new URI(url, true));
			if (startNode == null) {
				throw new ApiException(ApiException.Type.URL_NOT_FOUND);
			}
		} catch (URIException e) {
			throw new ApiException(ApiException.Type.URL_NOT_FOUND);
		}

		spiderThread = new SpiderThread(extension, "API", this, extension.getSpiderParam());
		spiderThread.setStartNode(startNode);
		spiderThread.start();
		
	}

	@Override
	public JSON handleApiView(String name, JSONObject params)
			throws ApiException {
		JSONArray result = new JSONArray();
		if (VIEW_STATUS.equals(name)) {
			result.add("" + progress);
		} else {
			throw new ApiException(ApiException.Type.BAD_VIEW);
		}
		return result;
	}

	@Override
	public String viewResultToXML (String name, JSON result) {
		XMLSerializer serializer = new XMLSerializer();
		if (VIEW_STATUS.equals(name)) {
			serializer.setArrayName("status");
			serializer.setElementName("percent");
		}
		return serializer.write(result);
	}

	@Override
	public String actionResultToXML (String name, JSON result) {
		XMLSerializer serializer = new XMLSerializer();
		serializer.setArrayName("result");
		return serializer.write(result);
	}

	@Override
	public void scanFinshed(String host) {
	}

	@Override
	public void scanProgress(String host, int progress, int maximum) {
		this.progress = (progress * 100) / maximum; 
	}

}
