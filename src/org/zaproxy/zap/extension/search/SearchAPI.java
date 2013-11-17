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

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.parosproxy.paros.db.RecordHistory;
import org.parosproxy.paros.db.TableHistory;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiImplementor;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.extension.api.ApiResponseList;
import org.zaproxy.zap.extension.api.ApiResponseSet;
import org.zaproxy.zap.extension.api.ApiView;

public class SearchAPI extends ApiImplementor {

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
	public ApiResponse handleApiView(String name, JSONObject params)
			throws ApiException {
		ApiResponseList result = new ApiResponseList(name);
		try {
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

			ApiSearchListener searchListener = new ApiSearchListener();
			// The search kicks off a background thread
			extension.search(
					params.getString(PARAM_REGEX), 
					searchListener, type, false, false, 
					this.getParam(params, PARAM_BASE_URL, (String)null),
					this.getParam(params, PARAM_START, -1),
					this.getParam(params, PARAM_COUNT, -1));
			
			while(!searchListener.isSearchComplete()) {
				Thread.sleep(100);
			}

			TableHistory tableHistory = Model.getSingleton().getDb().getTableHistory();
			for (Integer hRefId : searchListener.getHistoryReferencesIds()) {
				try {
					final RecordHistory recHistory = tableHistory.read(hRefId.intValue());
					final HttpMessage msg = recHistory.getHttpMessage();
					Map<String, String> map = new HashMap<>();
					map.put("id", String.valueOf(recHistory.getHistoryId()));
					map.put("type", String.valueOf(recHistory.getHistoryType()));
					map.put("method", msg.getRequestHeader().getMethod());
					map.put("url", msg.getRequestHeader().getURI().toString());
					map.put("code", String.valueOf(msg.getResponseHeader().getStatusCode()));
					map.put("time", String.valueOf(msg.getTimeElapsedMillis()));
					result.addItem(new ApiResponseSet(name, map));
				} catch (SQLException | HttpMalformedHeaderException e) {
					log.error(e.getMessage(), e);
				}
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

	private static class ApiSearchListener implements SearchListenner {

		private boolean searchComplete;

		private List<Integer> historyReferencesIds;

		public ApiSearchListener() {
			super();
			searchComplete = false;
			historyReferencesIds = new LinkedList<>();
		}

		@Override
		public void addSearchResult(SearchResult sr) {
			historyReferencesIds.add(Integer.valueOf(sr.getMessage().getHistoryRef().getHistoryId()));
		}

		@Override
		public void searchComplete() {
			this.searchComplete = true;
		}

		public boolean isSearchComplete() {
			return searchComplete;
		}

		public List<Integer> getHistoryReferencesIds() {
			return historyReferencesIds;
		}
	}

}
