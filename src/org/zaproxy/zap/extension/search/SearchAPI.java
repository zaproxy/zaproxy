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
package org.zaproxy.zap.extension.search;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiImplementor;
import org.zaproxy.zap.extension.api.ApiView;

public class SearchAPI extends ApiImplementor implements SearchListenner {

    private static Logger log = Logger.getLogger(SearchAPI.class);

	private static final String PREFIX = "search";
	
	private static final String VIEW_URLS_BY_URL_REGEX = "urlsByUrlRegex";
	private static final String VIEW_URLS_BY_REQUEST_REGEX = "urlsByRequestRegex";
	private static final String VIEW_URLS_BY_RESPONSE_REGEX = "urlsByResponseRegex";
	private static final String VIEW_URLS_BY_HEADER_REGEX = "urlsByHeaderRegex";

	private static final String PARAM_BASE_URL = "baseurl";
	//private static final String PARAM_CONTEXT_ID = "contextId";	TODO: implement 
	private static final String PARAM_COUNT = "count";
	private static final String PARAM_REGEX = "regex";
	private static final String PARAM_START = "start";

	private ExtensionSearch extension;

	private List<SearchResult> results = null;
	private boolean searchInProgress = false;
	
	public SearchAPI (ExtensionSearch extension) {
		this.extension = extension;
		
		this.addApiView(new ApiView(VIEW_URLS_BY_URL_REGEX, 
				new String[] {PARAM_REGEX}, new String[] {PARAM_BASE_URL, PARAM_START, PARAM_COUNT}));
		this.addApiView(new ApiView(VIEW_URLS_BY_REQUEST_REGEX, 
				new String[] {PARAM_REGEX}, new String[] {PARAM_BASE_URL, PARAM_START, PARAM_COUNT}));
		this.addApiView(new ApiView(VIEW_URLS_BY_RESPONSE_REGEX, 
				new String[] {PARAM_REGEX}, new String[] {PARAM_BASE_URL, PARAM_START, PARAM_COUNT}));
		this.addApiView(new ApiView(VIEW_URLS_BY_HEADER_REGEX, 
				new String[] {PARAM_REGEX}, new String[] {PARAM_BASE_URL, PARAM_START, PARAM_COUNT}));
		
	}
	
	@Override
	public String getPrefix() {
		return PREFIX;
	}

	@Override
	public JSON handleApiView(String name, JSONObject params)
			throws ApiException {
		JSONArray result = new JSONArray();
		try {
			if (this.searchInProgress) {
				// TODO better exception
				throw new ApiException(ApiException.Type.BAD_VIEW);
			}
			this.searchInProgress = true;
			this.results = new ArrayList<SearchResult>();
			ExtensionSearch.Type type;
			
			if (VIEW_URLS_BY_URL_REGEX.equals(name)) {
				type = ExtensionSearch.Type.URL;
			} else if (VIEW_URLS_BY_REQUEST_REGEX.equals(name)) {
				type = ExtensionSearch.Type.Request;
			} else if (VIEW_URLS_BY_RESPONSE_REGEX.equals(name)) {
				type = ExtensionSearch.Type.Response;
			} else if (VIEW_URLS_BY_HEADER_REGEX.equals(name)) {
				type = ExtensionSearch.Type.Header;
			} else {
				throw new ApiException(ApiException.Type.BAD_VIEW);
			}

			// The search kicks off a background thread
			extension.search(
					params.getString(PARAM_REGEX), 
					this, type, false, false, 
					this.getParam(params, PARAM_BASE_URL, (String)null),
					this.getParam(params, PARAM_START, -1),
					this.getParam(params, PARAM_COUNT, -1));
			
			while(this.searchInProgress) {
				Thread.sleep(100);
			}

			for (SearchResult sr : this.results) {
				JSONObject ja = new JSONObject();
				ja.put("id", sr.getMessage().getHistoryRef().getHistoryId());
				ja.put("type", sr.getMessage().getHistoryRef().getHistoryType());
				ja.put("method", sr.getMessage().getRequestHeader().getMethod());
				ja.put("url", sr.getMessage().getRequestHeader().getURI().toString());
				ja.put("code", sr.getMessage().getResponseHeader().getStatusCode());
				ja.put("time", sr.getMessage().getTimeElapsedMillis());
				result.add(ja);
			}
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new ApiException(ApiException.Type.INTERNAL_ERROR,
					e.getMessage());
		}
		return result;
	}
	/*
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
	*/

	@Override
	public void addSearchResult(SearchResult res) {
		this.results.add(res);
	}

	@Override
	public void searchComplete() {
		this.searchInProgress = false;
	}

}
