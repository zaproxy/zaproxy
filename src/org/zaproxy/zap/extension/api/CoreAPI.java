/*
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
package org.zaproxy.zap.extension.api;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.db.RecordAlert;
import org.parosproxy.paros.db.RecordHistory;
import org.parosproxy.paros.db.TableAlert;
import org.parosproxy.paros.db.TableHistory;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SessionListener;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.dynssl.ExtensionDynSSL;
import org.zaproxy.zap.extension.dynssl.SslCertificateUtils;
import org.zaproxy.zap.utils.XMLStringUtil;

public class CoreAPI extends ApiImplementor implements SessionListener {

	private static Logger log = Logger.getLogger(CoreAPI.class);

	private static final String PREFIX = "core";
	private static final String ACTION_LOAD_SESSION = "loadSession";
	private static final String ACTION_NEW_SESSION = "newSession";
	private static final String ACTION_SAVE_SESSION = "saveSession";
	private static final String ACTION_SNAPSHOT_SESSION = "snapshotSession";
	
	private static final String ACTION_SHUTDOWN = "shutdown";
	private static final String ACTION_EXCLUDE_FROM_PROXY = "excludeFromProxy";
	private static final String ACTION_CLEAR_EXCLUDED_FROM_PROXY = "clearExcludedFromProxy";
	private static final String ACTION_SET_HOME_DIRECTORY = "setHomeDirectory";
	private static final String ACTION_GENERATE_ROOT_CA = "generateRootCA";
	
	private static final String VIEW_ALERTS = "alerts";
	private static final String VIEW_HOSTS = "hosts";
	private static final String VIEW_SITES = "sites";
	private static final String VIEW_URLS = "urls";
	private static final String VIEW_MESSAGES = "messages";
	private static final String VIEW_VERSION = "version";
	private static final String VIEW_EXCLUDED_FROM_PROXY = "excludedFromProxy";
	private static final String VIEW_HOME_DIRECTORY = "homeDirectory";

	private static final String OTHER_PROXY_PAC = "proxy.pac";

	private static final String PARAM_BASE_URL = "baseurl";
	private static final String PARAM_COUNT = "count";
	private static final String PARAM_DIR = "dir";
	private static final String PARAM_SESSION = "name";
	//private static final String PARAM_CONTEXT = "context";	// TODO need to support context methods for this!
	private static final String PARAM_REGEX = "regex";
	private static final String PARAM_START = "start";

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
	private Logger logger = Logger.getLogger(this.getClass());
	private boolean savingSession = false;

	public CoreAPI() {
		this.addApiAction(new ApiAction(ACTION_SHUTDOWN));
		this.addApiAction(new ApiAction(ACTION_NEW_SESSION, null, new String[] {PARAM_SESSION}));
		this.addApiAction(new ApiAction(ACTION_LOAD_SESSION, new String[] {PARAM_SESSION}));
		this.addApiAction(new ApiAction(ACTION_SAVE_SESSION, new String[] {PARAM_SESSION}));
		this.addApiAction(new ApiAction(ACTION_SNAPSHOT_SESSION));
		this.addApiAction(new ApiAction(ACTION_CLEAR_EXCLUDED_FROM_PROXY));
		this.addApiAction(new ApiAction(ACTION_EXCLUDE_FROM_PROXY, new String[] {PARAM_REGEX}));
		this.addApiAction(new ApiAction(ACTION_SET_HOME_DIRECTORY, new String[] {PARAM_DIR}));
		this.addApiAction(new ApiAction(ACTION_GENERATE_ROOT_CA));
		
		this.addApiView(new ApiView(VIEW_ALERTS, null, 
				new String[] {PARAM_BASE_URL, PARAM_START, PARAM_COUNT}));
		this.addApiView(new ApiView(VIEW_HOSTS));
		this.addApiView(new ApiView(VIEW_SITES));
		this.addApiView(new ApiView(VIEW_URLS));
		this.addApiView(new ApiView(VIEW_MESSAGES, null, 
				new String[] {PARAM_BASE_URL, PARAM_START, PARAM_COUNT}));
		this.addApiView(new ApiView(VIEW_VERSION));
		this.addApiView(new ApiView(VIEW_EXCLUDED_FROM_PROXY));
		this.addApiView(new ApiView(VIEW_HOME_DIRECTORY));
		
		this.addApiOthers(new ApiOther(OTHER_PROXY_PAC));
	}

	@Override
	public String getPrefix() {
		return PREFIX;
	}

	@Override
	public ApiResponse handleApiAction(String name, JSONObject params)
			throws ApiException {

		Session session = Model.getSingleton().getSession();

		if (ACTION_SHUTDOWN.equals(name)) {
			Thread thread = new Thread() {
				@Override
				public void run() {
					try {
						// Give the API a chance to return
						sleep(1000);
					} catch (InterruptedException e) {
						// Ignore
					}
					Control.getSingleton().shutdown(Model.getSingleton().getOptionsParam().getDatabaseParam().isCompactDatabase());
					log.info(Constant.PROGRAM_TITLE + " terminated.");
					System.exit(0);
				}
			};
			thread.start();

		} else if (ACTION_SAVE_SESSION.equalsIgnoreCase(name)) {	// Ignore case for backwards compatibility
			String sessionName = params.getString(PARAM_SESSION);
			session.setSessionName(name);
			if (!sessionName.endsWith(".session")) {
				sessionName = sessionName + ".session";
			}
			
			File file = new File(sessionName);
			String filename = file.getAbsolutePath();
			
			if (! sessionName.equals(filename)) {
				// Treat as a relative path
				filename = Model.getSingleton().getOptionsParam()
						.getUserDirectory()
						+ File.separator + sessionName;
				file = new File(filename);
			} 
			
			if (file.exists()) {
				throw new ApiException(ApiException.Type.ALREADY_EXISTS,
						filename);
			}
			this.savingSession = true;
			try {
		    	Control.getSingleton().saveSession(filename, this);
			} catch (Exception e) {
				this.savingSession = false;
				throw new ApiException(ApiException.Type.INTERNAL_ERROR,
						e.getMessage());
			}
			// Wait for notification that its worked ok
			try {
				while (this.savingSession) {
						Thread.sleep(200);
				}
			} catch (InterruptedException e) {
				// Probably not an error
				logger.debug(e.getMessage(), e);
			}
			logger.debug("Can now return after saving session");
			
			
		} else if (ACTION_SNAPSHOT_SESSION.equalsIgnoreCase(name)) {	// Ignore case for backwards compatibility
			if (session.isNewState()) {
				throw new ApiException(ApiException.Type.DOES_NOT_EXIST);
			}
			String fileName = session.getFileName();
			
			if (fileName.endsWith(".session")) {
			    fileName = fileName.substring(0, fileName.length() - 8);
			}
			fileName += "-" + dateFormat.format(new Date()) + ".session";

			
			this.savingSession = true;
			try {
		    	Control.getSingleton().snapshotSession(fileName, this);
			} catch (Exception e) {
				this.savingSession = false;
				throw new ApiException(ApiException.Type.INTERNAL_ERROR,
						e.getMessage());
			}
			// Wait for notification that its worked ok
			try {
				while (this.savingSession) {
						Thread.sleep(200);
				}
			} catch (InterruptedException e) {
				// Probably not an error
				logger.debug(e.getMessage(), e);
			}
			logger.debug("Can now return after saving session");
			
			
		} else if (ACTION_LOAD_SESSION.equalsIgnoreCase(name)) {	// Ignore case for backwards compatibility
			String sessionName = params.getString(PARAM_SESSION);
			if (!sessionName.endsWith(".session")) {
				sessionName = sessionName + ".session";
			}
			
			File file = new File(sessionName);
			String filename = file.getAbsolutePath();

			if (! sessionName.equals(filename)) {
				// Treat as a relative path
				filename = Model.getSingleton().getOptionsParam()
						.getUserDirectory()
						+ File.separator + sessionName;
				file = new File(filename);
			} 

			if (!file.exists()) {
				throw new ApiException(ApiException.Type.DOES_NOT_EXIST, filename);
			}
			try {
				Control.getSingleton().runCommandLineOpenSession(filename);
			} catch (Exception e) {
				throw new ApiException(ApiException.Type.INTERNAL_ERROR,
						e.getMessage());
			}

		} else if (ACTION_NEW_SESSION.equalsIgnoreCase(name)) {	// Ignore case for backwards compatibility
			String sessionName = params.getString(PARAM_SESSION);
			if (sessionName == null || sessionName.length() == 0) {
				// Create a new 'unnamed' session
				Control.getSingleton().discardSession();
				try {
					Control.getSingleton().createAndOpenUntitledDb();
				} catch (Exception e) {
					throw new ApiException(ApiException.Type.INTERNAL_ERROR,
							e.getMessage());
				}
				Control.getSingleton().newSession();
			} else {
				session.setSessionName(name);
				if (!sessionName.endsWith(".session")) {
					sessionName = sessionName + ".session";
				}
				File file = new File(sessionName);
				String filename = file.getAbsolutePath();
				
				if (! sessionName.equals(filename)) {
					// Treat as a relative path
					filename = Model.getSingleton().getOptionsParam()
							.getUserDirectory()
							+ File.separator + sessionName;
					file = new File(filename);
				} 
				
				if (file.exists()) {
					throw new ApiException(ApiException.Type.ALREADY_EXISTS,
							filename);
				}
				try {
					Control.getSingleton().runCommandLineNewSession(filename);
				} catch (Exception e) {
					throw new ApiException(ApiException.Type.INTERNAL_ERROR, e.getMessage());
				}
			}
		} else if (ACTION_CLEAR_EXCLUDED_FROM_PROXY.equals(name)) {
			try {
				session.setExcludeFromProxyRegexs(new ArrayList<String>());
			} catch (SQLException e) {
				throw new ApiException(ApiException.Type.INTERNAL_ERROR, e.getMessage());
			}
		} else if (ACTION_EXCLUDE_FROM_PROXY.equals(name)) {
			String regex = params.getString(PARAM_REGEX);
			try {
				session.addExcludeFromProxyRegex(regex);
			} catch (Exception e) {
				throw new ApiException(ApiException.Type.BAD_FORMAT, PARAM_REGEX);
			}
		} else if (ACTION_SET_HOME_DIRECTORY.equals(name)) {
			File f = new File(params.getString(PARAM_DIR));
			if (f.exists() && f.isDirectory()) {
				Model.getSingleton().getOptionsParam().setUserDirectory(f);
			} else {
				throw new ApiException(ApiException.Type.BAD_FORMAT, PARAM_DIR);
			}
		} else if (ACTION_GENERATE_ROOT_CA.equals(name)) {
			ExtensionDynSSL extDyn = (ExtensionDynSSL) 
					Control.getSingleton().getExtensionLoader().getExtension(ExtensionDynSSL.EXTENSION_ID);
			if (extDyn != null) {
				try {
					final KeyStore newrootca = SslCertificateUtils.createRootCA();
					extDyn.setRootCa(newrootca);
					extDyn.getParams().setRootca(newrootca);
					
				} catch (Exception e) {
					throw new ApiException(ApiException.Type.INTERNAL_ERROR, e.getMessage());
				}
			}
		} else {
			throw new ApiException(ApiException.Type.BAD_ACTION);
		}
		return ApiResponseElement.OK;
	}

	@Override
	public ApiResponse handleApiView(String name, JSONObject params)
			throws ApiException {
		ApiResponse result = null;
		//JSONArray result = new JSONArray();
		Session session = Model.getSingleton().getSession();

		if (VIEW_HOSTS.equals(name)) {
			result = new ApiResponseList(name);
			SiteNode root = (SiteNode) session.getSiteTree().getRoot();
			@SuppressWarnings("unchecked")
			Enumeration<SiteNode> en = root.children();
			while (en.hasMoreElements()) {
				String site = en.nextElement().getNodeName();
				if (site.indexOf("//") >= 0) {
					site = site.substring(site.indexOf("//") + 2);
				}
				if (site.indexOf(":") >= 0) {
					site = site.substring(0, site.indexOf(":"));
				}
				((ApiResponseList)result).addItem(new ApiResponseElement("host", site));
			}
		} else if (VIEW_SITES.equals(name)) {
			result = new ApiResponseList(name);
			SiteNode root = (SiteNode) session.getSiteTree().getRoot();
			@SuppressWarnings("unchecked")
			Enumeration<SiteNode> en = root.children();
			while (en.hasMoreElements()) {
				((ApiResponseList)result).addItem(new ApiResponseElement("site", en.nextElement().getNodeName()));
			}
		} else if (VIEW_URLS.equals(name)) {
			result = new ApiResponseList(name);
			SiteNode root = (SiteNode) session.getSiteTree().getRoot();
			this.getURLs(root, (ApiResponseList)result);
		} else if (VIEW_ALERTS.equals(name)) {
			result = new ApiResponseList(name);
			List<Alert> alerts = getAlerts(
					this.getParam(params, PARAM_BASE_URL, (String) null), 
					this.getParam(params, PARAM_START, -1), 
					this.getParam(params, PARAM_COUNT, -1));
			for (Alert alert : alerts) {
				((ApiResponseList)result).addItem(this.alertToSet(alert));
			}
		} else if (VIEW_MESSAGES.equals(name)) {
			result = new ApiResponseList(name);

			ArrayList<HttpMessage> hm = null;
			try {
				hm = getHttpMessages(
						this.getParam(params, PARAM_BASE_URL, (String) null), 
						this.getParam(params, PARAM_START, -1), 
						this.getParam(params, PARAM_COUNT, -1));
				for (HttpMessage httpm : hm) {
					((ApiResponseList)result).addItem(this.httpMessageToSet(httpm));
				}
			} catch (HttpMalformedHeaderException e) {
				logger.error(e.getMessage(), e);
			}
		} else if (VIEW_VERSION.equals(name)) {
			result = new ApiResponseList(name);
			result = new ApiResponseElement(name, Constant.PROGRAM_VERSION);
		} else if (VIEW_EXCLUDED_FROM_PROXY.equals(name)) {
			result = new ApiResponseList(name);
			List<String> regexs = session.getExcludeFromProxyRegexs();
			for (String regex : regexs) {
				((ApiResponseList)result).addItem(new ApiResponseElement("regex", regex));
			}
		} else if (VIEW_HOME_DIRECTORY.equals(name)) {
			result = new ApiResponseElement(name, Model.getSingleton().getOptionsParam().getUserDirectory().getAbsolutePath());
		} else {
			throw new ApiException(ApiException.Type.BAD_VIEW);
		}
		return result;
	}
	
	@Override
	public HttpMessage handleApiOther(HttpMessage msg, String name,
			JSONObject params) throws ApiException {

		if (OTHER_PROXY_PAC.equals(name)) {
			String response;
			try {
				response = this.getPacFile(msg.getRequestHeader().getURI().getHost(), msg.getRequestHeader().getURI().getPort());
				msg.setResponseHeader(
						"HTTP/1.1 200 OK\r\n" +
						"Pragma: no-cache\r\n" +
						"Cache-Control: no-cache\r\n" + 
						"Access-Control-Allow-Origin: *\r\n" + 
						"Access-Control-Allow-Methods: GET,POST,OPTIONS\r\n" + 
						"Access-Control-Allow-Headers: ZAP-Header\r\n" + 
						"Content-Length: " + response.length() + 
						"\r\nContent-Type: text/html;");
				
		    	msg.setResponseBody(response);
		    	
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			return msg;
		} else {
			throw new ApiException(ApiException.Type.BAD_OTHER);
		}
	}

	private String getPacFile(String host, int port) {
		// Could put in 'ignore urls'?
		StringBuilder sb = new StringBuilder();
		sb.append("function FindProxyForURL(url, host) {\n");
		sb.append("  return \"PROXY " + host + ":" + port + "\";\n");
		sb.append("} // End of function\n");
		
		return sb.toString();
	}

	private void getURLs(SiteNode parent, ApiResponseList list) {
		@SuppressWarnings("unchecked")
		Enumeration<SiteNode> en = parent.children();
		while (en.hasMoreElements()) {
			SiteNode child = en.nextElement();
			String site = child.getNodeName();
			if (site.indexOf("//") >= 0) {
				site = site.substring(site.indexOf("//") + 2);
			}
			try {
				list.addItem(new ApiResponseElement("url", child.getHistoryReference().getURI().toString()));
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			getURLs(child, list);
		}
	}

	private ApiResponseSet alertToSet(Alert alert) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("id", String.valueOf(alert.getAlertId()));
		map.put("alert", alert.getAlert());
		map.put("description", alert.getDescription());
		map.put("risk", Alert.MSG_RISK[alert.getRisk()]);
		map.put("reliability", Alert.MSG_RELIABILITY[alert.getReliability()]);
		map.put("url", alert.getUri());
		map.put("other", alert.getOtherInfo());
		map.put("param", XMLStringUtil.escapeControlChrs(alert.getParam()));
		map.put("attack", XMLStringUtil.escapeControlChrs(alert.getAttack()));
		map.put("reference", alert.getReference());
		map.put("solution", alert.getSolution());
		if (alert.getHistoryRef() != null) {
			map.put("messageId", String.valueOf(alert.getHistoryRef().getHistoryId()));
		}
		return new ApiResponseSet("alert", map);
	}

	/**
	 * 
	 * @param msg
	 * @return
	 */
	private ApiResponseSet httpMessageToSet(HttpMessage msg) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("id", String.valueOf(msg.getHistoryRef().getHistoryId()));
		map.put("cookieParams", XMLStringUtil.escapeControlChrs(msg.getCookieParamsAsString()));
		map.put("note", msg.getNote());
		map.put("requestHeader", XMLStringUtil.escapeControlChrs(msg.getRequestHeader().toString()));
		map.put("requestBody", XMLStringUtil.escapeControlChrs(msg.getRequestBody().toString()));
		map.put("responseHeader", XMLStringUtil.escapeControlChrs(msg.getResponseHeader().toString()));
		
		if (HttpHeader.GZIP.equals(msg.getResponseHeader().getHeader(HttpHeader.CONTENT_ENCODING))) {
			// Uncompress gziped content
			try {
				ByteArrayInputStream bais = new ByteArrayInputStream(msg.getResponseBody().getBytes());
				GZIPInputStream gis = new GZIPInputStream(bais);
				InputStreamReader isr = new InputStreamReader(gis);
				BufferedReader br = new BufferedReader(isr);
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				br.close();
				isr.close();
				gis.close();
				bais.close();
				map.put("responseBody", XMLStringUtil.escapeControlChrs(sb.toString()));
			} catch (IOException e) {
				//this.log.error(e.getMessage(), e);
				System.out.println(e);
			}
		} else {
			map.put("responseBody", XMLStringUtil.escapeControlChrs(msg.getResponseBody().toString()));
		}
		
		return new ApiResponseSet("message", map);
	}

	private List<Alert> getAlerts(String baseUrl, int start, int count) throws ApiException {
		List<Alert> alerts = new ArrayList<>();
		int c = 0;
		try {
			TableAlert tableAlert = Model.getSingleton().getDb()
					.getTableAlert();
			Vector<Integer> v = tableAlert.getAlertList();

			for (int i = 0; i < v.size(); i++) {
				int alertId = v.get(i).intValue();
				if (start >= 0 && alertId < start) {
					continue;
				}
				RecordAlert recAlert = tableAlert.read(alertId);
				Alert alert = new Alert(recAlert);

				if (alert.getReliability() != Alert.FALSE_POSITIVE
						&& !alerts.contains(alert)) {
					if (baseUrl != null && ! alert.getUri().startsWith(baseUrl)) {
						// Not subordinate to the specified URL
						continue;
					}
					alerts.add(alert);
					c ++;
					if (count > 0 && c >= count) {
						break;
					}
				}
			}
			return alerts;
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			throw new ApiException(ApiException.Type.INTERNAL_ERROR);
		}
	}

	/**
	 * @throws HttpMalformedHeaderException
	 */
	private ArrayList<HttpMessage> getHttpMessages(String baseUrl, int start, int count) throws ApiException,
			HttpMalformedHeaderException {
		try {
			int c = 0;
			TableHistory tableHistory = Model.getSingleton().getDb()
					.getTableHistory();
			Vector<Integer> v = tableHistory.getHistoryList(Model
					.getSingleton().getSession().getSessionId());
			ArrayList<HttpMessage> mgss = new ArrayList<>();
			for (int i = 0; i < v.size(); i++) {
				int historyId = v.get(i).intValue();
				if (start >= 0 && historyId < start) {
					continue;
				}
				RecordHistory recHistory = tableHistory.read(historyId);
				HttpMessage msg = recHistory.getHttpMessage();
				if (baseUrl != null && ! msg.getRequestHeader().getURI().toString().startsWith(baseUrl)) {
					// Not subordinate to the specified URL
					continue;
				}
				if ( ! msg.getRequestHeader().isImage() && ! msg.getResponseHeader().isImage()) {
					
					msg.setHistoryRef(new HistoryReference(historyId));
					
					mgss.add(msg);
					c ++;
					if (count > 0 && c >= count) {
						break;
					}
				}
			}
			return mgss;
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			throw new ApiException(ApiException.Type.INTERNAL_ERROR);
		}
	}

	@Override
	public void sessionOpened(File file, Exception e) {
		// Ignore
	}

	@Override
	public void sessionSaved(Exception e) {
		logger.debug("Saved session notification");
		this.savingSession = false;
	}

	@Override
	public void sessionSnapshot(Exception e) {
		logger.debug("Snaphot session notification");
		this.savingSession = false;
	}

}
