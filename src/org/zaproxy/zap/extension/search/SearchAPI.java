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

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.RecordHistory;
import org.parosproxy.paros.db.TableHistory;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.api.API;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiImplementor;
import org.zaproxy.zap.extension.api.ApiOther;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.extension.api.ApiResponseConversionUtils;
import org.zaproxy.zap.extension.api.ApiResponseList;
import org.zaproxy.zap.extension.api.ApiResponseSet;
import org.zaproxy.zap.extension.api.ApiView;
import org.zaproxy.zap.utils.HarUtils;

import edu.umass.cs.benchlab.har.HarEntries;
import edu.umass.cs.benchlab.har.HarLog;

public class SearchAPI extends ApiImplementor {

    private static Logger log = Logger.getLogger(SearchAPI.class);

	private static final String PREFIX = "search";
	
	private static final String VIEW_URLS_BY_URL_REGEX = "urlsByUrlRegex";
	private static final String VIEW_URLS_BY_REQUEST_REGEX = "urlsByRequestRegex";
	private static final String VIEW_URLS_BY_RESPONSE_REGEX = "urlsByResponseRegex";
	private static final String VIEW_URLS_BY_HEADER_REGEX = "urlsByHeaderRegex";

	private static final String VIEW_MESSAGES_BY_URL_REGEX = "messagesByUrlRegex";
	private static final String VIEW_MESSAGES_BY_REQUEST_REGEX = "messagesByRequestRegex";
	private static final String VIEW_MESSAGES_BY_RESPONSE_REGEX = "messagesByResponseRegex";
	private static final String VIEW_MESSAGES_BY_HEADER_REGEX = "messagesByHeaderRegex";
	
	private static final String OTHER_HAR_BY_URL_REGEX = "harByUrlRegex";
	private static final String OTHER_HAR_BY_REQUEST_REGEX = "harByRequestRegex";
	private static final String OTHER_HAR_BY_RESPONSE_REGEX = "harByResponseRegex";
	private static final String OTHER_HAR_BY_HEADER_REGEX = "harByHeaderRegex";

	private static final String PARAM_BASE_URL = "baseurl";
	//private static final String PARAM_CONTEXT_ID = "contextId";	TODO: implement 
	private static final String PARAM_COUNT = "count";
	private static final String PARAM_REGEX = "regex";
	private static final String PARAM_START = "start";

	private enum SearchViewResponseType {
		URL,
		MESSAGE
	}

	private ExtensionSearch extension;

	public SearchAPI (ExtensionSearch extension) {
		this.extension = extension;
		
		final String[] searchMandatoryParams = new String[] { PARAM_REGEX };
		final String[] searchOptionalParams = new String[] { PARAM_BASE_URL, PARAM_START, PARAM_COUNT };

		this.addApiView(new ApiView(VIEW_URLS_BY_URL_REGEX, searchMandatoryParams, searchOptionalParams));
		this.addApiView(new ApiView(VIEW_URLS_BY_REQUEST_REGEX, searchMandatoryParams, searchOptionalParams));
		this.addApiView(new ApiView(VIEW_URLS_BY_RESPONSE_REGEX, searchMandatoryParams, searchOptionalParams));
		this.addApiView(new ApiView(VIEW_URLS_BY_HEADER_REGEX, searchMandatoryParams, searchOptionalParams));

		this.addApiView(new ApiView(VIEW_MESSAGES_BY_URL_REGEX, searchMandatoryParams, searchOptionalParams));
		this.addApiView(new ApiView(VIEW_MESSAGES_BY_REQUEST_REGEX, searchMandatoryParams, searchOptionalParams));
		this.addApiView(new ApiView(VIEW_MESSAGES_BY_RESPONSE_REGEX, searchMandatoryParams, searchOptionalParams));
		this.addApiView(new ApiView(VIEW_MESSAGES_BY_HEADER_REGEX, searchMandatoryParams, searchOptionalParams));

		this.addApiOthers(new ApiOther(OTHER_HAR_BY_URL_REGEX, searchMandatoryParams, searchOptionalParams));
		this.addApiOthers(new ApiOther(OTHER_HAR_BY_REQUEST_REGEX, searchMandatoryParams, searchOptionalParams));
		this.addApiOthers(new ApiOther(OTHER_HAR_BY_RESPONSE_REGEX, searchMandatoryParams, searchOptionalParams));
		this.addApiOthers(new ApiOther(OTHER_HAR_BY_HEADER_REGEX, searchMandatoryParams, searchOptionalParams));
	}
	
	@Override
	public String getPrefix() {
		return PREFIX;
	}

	@Override
	public ApiResponse handleApiView(final String name, JSONObject params)
			throws ApiException {
		final ApiResponseList result = new ApiResponseList(name);
		ExtensionSearch.Type searchType;
		SearchViewResponseType responseType;

		switch (name) {
		case VIEW_URLS_BY_URL_REGEX:
			searchType = ExtensionSearch.Type.URL;
			responseType = SearchViewResponseType.URL;
			break;
		case VIEW_MESSAGES_BY_URL_REGEX:
			searchType = ExtensionSearch.Type.URL;
			responseType = SearchViewResponseType.MESSAGE;
			break;
		case VIEW_URLS_BY_REQUEST_REGEX:
			searchType = ExtensionSearch.Type.Request;
			responseType = SearchViewResponseType.URL;
			break;
		case VIEW_MESSAGES_BY_REQUEST_REGEX:
			searchType = ExtensionSearch.Type.Request;
			responseType = SearchViewResponseType.MESSAGE;
			break;
		case VIEW_URLS_BY_RESPONSE_REGEX:
			searchType = ExtensionSearch.Type.Response;
			responseType = SearchViewResponseType.URL;
			break;
		case VIEW_MESSAGES_BY_RESPONSE_REGEX:
			searchType = ExtensionSearch.Type.Response;
			responseType = SearchViewResponseType.MESSAGE;
			break;
		case VIEW_URLS_BY_HEADER_REGEX:
			searchType = ExtensionSearch.Type.Header;
			responseType = SearchViewResponseType.URL;
			break;
		case VIEW_MESSAGES_BY_HEADER_REGEX:
			searchType = ExtensionSearch.Type.Header;
			responseType = SearchViewResponseType.MESSAGE;
			break;
		default:
			throw new ApiException(ApiException.Type.BAD_VIEW);
		}

		validateRegex(params);

		try {
			SearchResultsProcessor processor;

			if (SearchViewResponseType.MESSAGE == responseType) {
				processor = new SearchResultsProcessor() {

					@Override
					public void processRecordHistory(RecordHistory recordHistory) {
						result.addItem(ApiResponseConversionUtils.httpMessageToSet(
								recordHistory.getHistoryId(),
								recordHistory.getHttpMessage()));
					}
				};
			} else {
				processor = new SearchResultsProcessor() {

					@Override
					public void processRecordHistory(RecordHistory recordHistory) {
						final HttpMessage msg = recordHistory.getHttpMessage();
						Map<String, String> map = new HashMap<>();
						map.put("id", String.valueOf(recordHistory.getHistoryId()));
						map.put("type", String.valueOf(recordHistory.getHistoryType()));
						map.put("method", msg.getRequestHeader().getMethod());
						map.put("url", msg.getRequestHeader().getURI().toString());
						map.put("code", String.valueOf(msg.getResponseHeader().getStatusCode()));
						map.put("time", String.valueOf(msg.getTimeElapsedMillis()));
						result.addItem(new ApiResponseSet(name, map));
					}
				};
			}
			
			search(params, searchType, processor);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new ApiException(ApiException.Type.INTERNAL_ERROR,
					e.getMessage());
		}
		return result;
	}

	private static void validateRegex(JSONObject params) throws ApiException {
		try {
			Pattern.compile(params.getString(PARAM_REGEX));
		} catch (NullPointerException | PatternSyntaxException e) {
			throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, PARAM_REGEX, e);
		}
	}

	@Override
	public HttpMessage handleApiOther(HttpMessage msg, String name, JSONObject params) throws ApiException {
		byte responseBody[] = {};
		
		ExtensionSearch.Type searchType;

		switch (name) {
		case OTHER_HAR_BY_URL_REGEX:
			searchType = ExtensionSearch.Type.URL;
			break;
		case OTHER_HAR_BY_REQUEST_REGEX:
			searchType = ExtensionSearch.Type.Request;
			break;
		case OTHER_HAR_BY_RESPONSE_REGEX:
			searchType = ExtensionSearch.Type.Response;
			break;
		case OTHER_HAR_BY_HEADER_REGEX:
			searchType = ExtensionSearch.Type.Header;
			break;
		default:
			throw new ApiException(ApiException.Type.BAD_OTHER);
		}

		validateRegex(params);

		try {
			final HarEntries entries = new HarEntries();
			search(params, searchType, new SearchResultsProcessor() {

				@Override
				public void processRecordHistory(RecordHistory recordHistory) {
					entries.addEntry(HarUtils.createHarEntry(recordHistory.getHttpMessage()));
				}
			});

			HarLog harLog = HarUtils.createZapHarLog();
			harLog.setEntries(entries);

			responseBody = HarUtils.harLogToByteArray(harLog);

		} catch (Exception e) {
			log.error(e.getMessage(), e);

			ApiException apiException = new ApiException(ApiException.Type.INTERNAL_ERROR, e.getMessage());
			responseBody = apiException.toString(API.Format.JSON, incErrorDetails()).getBytes(StandardCharsets.UTF_8);
		}

		try {
			msg.setResponseHeader(API.getDefaultResponseHeader("application/json; charset=UTF-8", responseBody.length));
		} catch (HttpMalformedHeaderException e) {
			log.error("Failed to create response header: " + e.getMessage(), e);
		}
		msg.setResponseBody(responseBody);

		return msg;
	}

	private boolean incErrorDetails() {
		return Model.getSingleton().getOptionsParam().getApiParam().isIncErrorDetails();
	}

	private void search(JSONObject params, ExtensionSearch.Type searchType, SearchResultsProcessor processor)
			throws InterruptedException {
		ApiSearchListener searchListener = new ApiSearchListener();
		// The search kicks off a background thread
		extension.search(
				params.getString(PARAM_REGEX),
				searchListener,
				searchType,
				false,
				false,
				this.getParam(params, PARAM_BASE_URL, (String) null),
				this.getParam(params, PARAM_START, -1),
				this.getParam(params, PARAM_COUNT, -1),
				false);

		while (!searchListener.isSearchComplete()) {
			Thread.sleep(100);
		}

		TableHistory tableHistory = Model.getSingleton().getDb().getTableHistory();
		for (Integer hRefId : searchListener.getHistoryReferencesIds()) {
			try {
				processor.processRecordHistory(tableHistory.read(hRefId.intValue()));
			} catch (DatabaseException | HttpMalformedHeaderException e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	private interface SearchResultsProcessor {

		void processRecordHistory(RecordHistory recordHistory);
	}

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
