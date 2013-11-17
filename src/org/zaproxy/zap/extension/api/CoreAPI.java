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

import java.io.File;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.cert.Certificate;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;
import org.bouncycastle.openssl.MiscPEMGenerator;
import org.bouncycastle.util.io.pem.PemWriter;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.proxy.ProxyParam;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.db.RecordAlert;
import org.parosproxy.paros.db.RecordHistory;
import org.parosproxy.paros.db.TableAlert;
import org.parosproxy.paros.db.TableHistory;
import org.parosproxy.paros.extension.report.ReportGenerator;
import org.parosproxy.paros.extension.report.ReportLastScan;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SessionListener;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.dynssl.ExtensionDynSSL;
import org.zaproxy.zap.utils.HarUtils;

import edu.umass.cs.benchlab.har.HarEntries;
import edu.umass.cs.benchlab.har.HarLog;

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
	private static final String VIEW_NUMBER_OF_ALERTS= "numberOfAlerts";
	private static final String VIEW_HOSTS = "hosts";
	private static final String VIEW_SITES = "sites";
	private static final String VIEW_URLS = "urls";
	private static final String VIEW_MESSAGE = "message";
	private static final String VIEW_MESSAGES = "messages";
	private static final String VIEW_NUMBER_OF_MESSAGES = "numberOfMessages";
	private static final String VIEW_VERSION = "version";
	private static final String VIEW_EXCLUDED_FROM_PROXY = "excludedFromProxy";
	private static final String VIEW_HOME_DIRECTORY = "homeDirectory";

	private static final String OTHER_PROXY_PAC = "proxy.pac";
	private static final String OTHER_SET_PROXY = "setproxy";
	private static final String OTHER_ROOT_CERT = "rootcert";
	private static final String OTHER_XML_REPORT = "xmlreport";
	private static final String OTHER_MESSAGES_HAR = "messagesHar";

	private static final String PARAM_BASE_URL = "baseurl";
	private static final String PARAM_COUNT = "count";
	private static final String PARAM_DIR = "dir";
	private static final String PARAM_SESSION = "name";
	private static final String PARAM_OVERWRITE_SESSION = "overwrite";
	//private static final String PARAM_CONTEXT = "context";	// TODO need to support context methods for this!
	private static final String PARAM_REGEX = "regex";
	private static final String PARAM_START = "start";
	private static final String PARAM_PROXY_DETAILS = "proxy";
	private static final String PARAM_ID = "id";

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
	private Logger logger = Logger.getLogger(this.getClass());
	private boolean savingSession = false;

	public CoreAPI() {
		this.addApiAction(new ApiAction(ACTION_SHUTDOWN));
		this.addApiAction(new ApiAction(ACTION_NEW_SESSION, null, new String[] {PARAM_SESSION, PARAM_OVERWRITE_SESSION}));
		this.addApiAction(new ApiAction(ACTION_LOAD_SESSION, new String[] {PARAM_SESSION}));
		this.addApiAction(new ApiAction(ACTION_SAVE_SESSION, new String[] {PARAM_SESSION}, new String[] {PARAM_OVERWRITE_SESSION}));
		this.addApiAction(new ApiAction(ACTION_SNAPSHOT_SESSION));
		this.addApiAction(new ApiAction(ACTION_CLEAR_EXCLUDED_FROM_PROXY));
		this.addApiAction(new ApiAction(ACTION_EXCLUDE_FROM_PROXY, new String[] {PARAM_REGEX}));
		this.addApiAction(new ApiAction(ACTION_SET_HOME_DIRECTORY, new String[] {PARAM_DIR}));
		this.addApiAction(new ApiAction(ACTION_GENERATE_ROOT_CA));
		
		this.addApiView(new ApiView(VIEW_ALERTS, null, 
				new String[] {PARAM_BASE_URL, PARAM_START, PARAM_COUNT}));
		this.addApiView(new ApiView(VIEW_NUMBER_OF_ALERTS, null, new String[] { PARAM_BASE_URL }));
		this.addApiView(new ApiView(VIEW_HOSTS));
		this.addApiView(new ApiView(VIEW_SITES));
		this.addApiView(new ApiView(VIEW_URLS));
		this.addApiView(new ApiView(VIEW_MESSAGE, new String[] {PARAM_ID}));
		this.addApiView(new ApiView(VIEW_MESSAGES, null, 
				new String[] {PARAM_BASE_URL, PARAM_START, PARAM_COUNT}));
		this.addApiView(new ApiView(VIEW_NUMBER_OF_MESSAGES, null, new String[] { PARAM_BASE_URL }));
		this.addApiView(new ApiView(VIEW_VERSION));
		this.addApiView(new ApiView(VIEW_EXCLUDED_FROM_PROXY));
		this.addApiView(new ApiView(VIEW_HOME_DIRECTORY));
		
		this.addApiOthers(new ApiOther(OTHER_PROXY_PAC));
		this.addApiOthers(new ApiOther(OTHER_ROOT_CERT));
		this.addApiOthers(new ApiOther(OTHER_SET_PROXY, new String[] {PARAM_PROXY_DETAILS}));
		this.addApiOthers(new ApiOther(OTHER_XML_REPORT));
		this.addApiOthers(new ApiOther(OTHER_MESSAGES_HAR, null, new String[] {PARAM_BASE_URL, PARAM_START, PARAM_COUNT}));
		
		this.addApiShortcut(OTHER_PROXY_PAC);
		// this.addApiShortcut(OTHER_ROOT_CERT);
		this.addApiShortcut(OTHER_SET_PROXY);

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
			
			final boolean overwrite = getParam(params, PARAM_OVERWRITE_SESSION, false);
			
			boolean sameSession = false;
			if (!session.isNewState()) {
				final File fileCurrentSession = new File(session.getFileName());
				sameSession = fileCurrentSession.getAbsolutePath().equals(file.getAbsolutePath());
			}
			
			if (file.exists() && (!overwrite || sameSession)) {
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
			String sessionName = null;
			try {
				sessionName = params.getString(PARAM_SESSION);
			} catch (Exception e1) {
				// Ignore
			}
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
				
				final boolean overwrite = getParam(params, PARAM_OVERWRITE_SESSION, false);
				
				if (file.exists() && !overwrite) {
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
					extDyn.createNewRootCa();
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
			final ApiResponseList resultList = new ApiResponseList(name);
			processAlerts(
					this.getParam(params, PARAM_BASE_URL, (String) null), 
					this.getParam(params, PARAM_START, -1), 
					this.getParam(params, PARAM_COUNT, -1), new Processor<Alert>() {

						@Override
						public void process(Alert alert) {
							resultList.addItem(alertToSet(alert));
						}
					});
			result = resultList;
		} else if (VIEW_NUMBER_OF_ALERTS.equals(name)) {
			CounterProcessor<Alert> counter = new CounterProcessor<>();
			processAlerts(
					this.getParam(params, PARAM_BASE_URL, (String) null), 
					this.getParam(params, PARAM_START, -1), 
					this.getParam(params, PARAM_COUNT, -1), counter);
			
			result = new ApiResponseElement(name, Integer.toString(counter.getCount()));
		} else if (VIEW_MESSAGE.equals(name)) {
			TableHistory tableHistory = Model.getSingleton().getDb().getTableHistory();
			RecordHistory recordHistory;
			try {
				recordHistory = tableHistory.read(this.getParam(params, PARAM_ID, -1));
			} catch (HttpMalformedHeaderException | SQLException e) {
				throw new ApiException(ApiException.Type.INTERNAL_ERROR);
			}
			if (recordHistory == null || recordHistory.getHistoryType() == HistoryReference.TYPE_TEMPORARY) {
				throw new ApiException(ApiException.Type.DOES_NOT_EXIST);
			}
			result = ApiResponseConversionUtils.httpMessageToSet(recordHistory.getHistoryId(), recordHistory.getHttpMessage());
		} else if (VIEW_MESSAGES.equals(name)) {
			final ApiResponseList resultList = new ApiResponseList(name);
			processHttpMessages(
					this.getParam(params, PARAM_BASE_URL, (String) null),
					this.getParam(params, PARAM_START, -1),
					this.getParam(params, PARAM_COUNT, -1),
					new Processor<RecordHistory>() {

						@Override
						public void process(RecordHistory recordHistory) {
							resultList.addItem(ApiResponseConversionUtils.httpMessageToSet(
									recordHistory.getHistoryId(),
									recordHistory.getHttpMessage()));
						}
					});
			result = resultList;
		} else if (VIEW_NUMBER_OF_MESSAGES.equals(name)) {
			CounterProcessor<RecordHistory> counter = new CounterProcessor<>();
			processHttpMessages(
					this.getParam(params, PARAM_BASE_URL, (String) null),
					this.getParam(params, PARAM_START, -1),
					this.getParam(params, PARAM_COUNT, -1),
					counter);

			result = new ApiResponseElement(name, Integer.toString(counter.getCount()));
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
			final ProxyParam proxyParam = Model.getSingleton().getOptionsParam().getProxyParam();
			final String domain = proxyParam.getProxyIp();
			final int port = proxyParam.getProxyPort();
			try {
				String response = this.getPacFile(domain, port);
				msg.setResponseHeader(API.getDefaultResponseHeader("text/html", response.length()));
				
		    	msg.setResponseBody(response);
		    	
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			return msg;
		} else if (OTHER_SET_PROXY.equals(name)) {
			/* JSON string:
			 *  {"type":1,
			 *  "http":	{"host":"proxy.corp.com","port":80},
			 *  "ssl":	{"host":"proxy.corp.com","port":80},
			 *  "ftp":{"host":"proxy.corp.com","port":80},
			 *  "socks":{"host":"proxy.corp.com","port":80},
			 *  "shareSettings":true,"socksVersion":5,
			 *  "proxyExcludes":"localhost, 127.0.0.1"}
			 */
			String proxyDetails = params.getString(PARAM_PROXY_DETAILS);
			String response = "OK";

			try {
				try {
					JSONObject json = JSONObject.fromObject(proxyDetails);

					if (json.getInt("type") == 1) {
						JSONObject httpJson = JSONObject.fromObject(json.get("http"));
						String proxyHost = httpJson.getString("host");
						int proxyPort = httpJson.getInt("port");
						
						if (proxyHost != null && proxyHost.length() > 0 && proxyPort > 0) {
							Model.getSingleton().getOptionsParam().getConnectionParam().setProxyChainName(proxyHost);
							Model.getSingleton().getOptionsParam().getConnectionParam().setProxyChainPort(proxyPort);
						}
								
					}
				} catch (JSONException e) {
					throw new ApiException(ApiException.Type.BAD_FORMAT);
				}
				msg.setResponseHeader(API.getDefaultResponseHeader("text/html", response.length()));
				
		    	msg.setResponseBody(response);
		    	
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}

			return msg;
		} else if (OTHER_ROOT_CERT.equals(name)) {
			ExtensionDynSSL extDynSSL = 
					(ExtensionDynSSL) Control.getSingleton().getExtensionLoader().getExtension(ExtensionDynSSL.EXTENSION_ID);
			if (extDynSSL != null) {
				try {
					Certificate rootCA = extDynSSL.getRootCA();
					if (rootCA == null) {
						throw new ApiException(ApiException.Type.DOES_NOT_EXIST);
					}
					final StringWriter sw = new StringWriter();
					try (final PemWriter pw = new PemWriter(sw)) {
						pw.writeObject(new MiscPEMGenerator(rootCA));
						pw.flush();
					}
					String response = sw.toString();
					msg.setResponseHeader(API.getDefaultResponseHeader("application/pkix-cert;", response.length()));
					
					msg.setResponseBody(response);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					throw new ApiException(ApiException.Type.INTERNAL_ERROR);
				}
				
			} else {
				throw new ApiException(ApiException.Type.DOES_NOT_EXIST);
			}
			
			return msg;
		} else if (OTHER_XML_REPORT.equals(name)) {
			try {
				ReportLastScan rls = new ReportLastScan();
		        StringBuilder report = new StringBuilder();
				rls.generate(report, Model.getSingleton());
		        String response =  ReportGenerator.stringToHtml(report.toString(), "xml/report.xml.xsl");
				
				msg.setResponseHeader(API.getDefaultResponseHeader("text/xml; charset=UTF-8"));
				
				msg.setResponseBody(response);
				msg.getResponseHeader().setContentLength(msg.getResponseBody().length());
		        
				return msg;
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				throw new ApiException(ApiException.Type.INTERNAL_ERROR);
			}
		} else if (OTHER_MESSAGES_HAR.equals(name)) {
			byte[] responseBody;
			try {
				final HarEntries entries = new HarEntries();
				processHttpMessages(
						this.getParam(params, PARAM_BASE_URL, (String) null),
						this.getParam(params, PARAM_START, -1),
						this.getParam(params, PARAM_COUNT, -1),
						new Processor<RecordHistory>() {

							@Override
							public void process(RecordHistory recordHistory) {
								entries.addEntry(HarUtils.createHarEntry(recordHistory.getHttpMessage()));
							}
						});

				HarLog harLog = HarUtils.createZapHarLog();
				harLog.setEntries(entries);

				responseBody = HarUtils.harLogToByteArray(harLog);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);

				ApiException apiException = new ApiException(ApiException.Type.INTERNAL_ERROR, e.getMessage());
				responseBody = apiException.toString(API.Format.JSON).getBytes(StandardCharsets.UTF_8);
			}

			try {
				msg.setResponseHeader(API.getDefaultResponseHeader("application/json; charset=UTF-8", responseBody.length));
			} catch (HttpMalformedHeaderException e) {
				log.error("Failed to create response header: " + e.getMessage(), e);
			}
			msg.setResponseBody(responseBody);
			
			return msg;
		} else {
			throw new ApiException(ApiException.Type.BAD_OTHER);
		}
	}

	@Override
	public HttpMessage handleShortcut(HttpMessage msg)  throws ApiException {
		try {
			if (msg.getRequestHeader().getURI().getPath().startsWith("/" + OTHER_PROXY_PAC)) {
				return this.handleApiOther(msg, OTHER_PROXY_PAC, null);
			} else if (msg.getRequestHeader().getURI().getPath().startsWith("/" + OTHER_SET_PROXY)) {
				JSONObject params = new JSONObject();
				params.put(PARAM_PROXY_DETAILS, msg.getRequestBody());
				return this.handleApiOther(msg, OTHER_SET_PROXY, params);
			}
		} catch (URIException e) {
			logger.error(e.getMessage(), e);
			throw new ApiException(ApiException.Type.INTERNAL_ERROR);
		}
		throw new ApiException (ApiException.Type.URL_NOT_FOUND, msg.getRequestHeader().getURI().toString());
	}

	private String getPacFile(String host, int port) {
		// Could put in 'ignore urls'?
		StringBuilder sb = new StringBuilder(100);
		sb.append("function FindProxyForURL(url, host) {\n");
		sb.append("  return \"PROXY ").append(host).append(':').append(port).append("\";\n");
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
		Map<String, String> map = new HashMap<>();
		map.put("id", String.valueOf(alert.getAlertId()));
		map.put("alert", alert.getAlert());
		map.put("description", alert.getDescription());
		map.put("risk", Alert.MSG_RISK[alert.getRisk()]);
		map.put("reliability", Alert.MSG_RELIABILITY[alert.getReliability()]);
		map.put("url", alert.getUri());
		map.put("other", alert.getOtherInfo());
		map.put("param", alert.getParam());
		map.put("attack", alert.getAttack());
		map.put("evidence", alert.getEvidence());
		map.put("reference", alert.getReference());
		map.put("cweid", String.valueOf(alert.getCweId()));
		map.put("wascid", String.valueOf(alert.getWascId()));
		map.put("solution", alert.getSolution());
		if (alert.getHistoryRef() != null) {
			map.put("messageId", String.valueOf(alert.getHistoryRef().getHistoryId()));
		}
		return new ApiResponseSet("alert", map);
	}

	private void processAlerts(String baseUrl, int start, int count, Processor<Alert> processor) throws ApiException {
		List<Alert> alerts = new ArrayList<>();
		try {
			TableAlert tableAlert = Model.getSingleton().getDb()
					.getTableAlert();
			Vector<Integer> v = tableAlert.getAlertList();

			PaginationConstraintsChecker pcc = new PaginationConstraintsChecker(start, count);
			for (int i = 0; i < v.size(); i++) {
				int alertId = v.get(i).intValue();
				RecordAlert recAlert = tableAlert.read(alertId);
				Alert alert = new Alert(recAlert);

				if (alert.getReliability() != Alert.FALSE_POSITIVE
						&& !alerts.contains(alert)) {
					if (baseUrl != null && ! alert.getUri().startsWith(baseUrl)) {
						// Not subordinate to the specified URL
						continue;
					}

					pcc.recordProcessed();
					alerts.add(alert);
					
					if (!pcc.hasPageStarted()) {
						continue;
					}
					processor.process(alert);
					
					if (pcc.hasPageEnded()) {
						break;
					}
				}
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			throw new ApiException(ApiException.Type.INTERNAL_ERROR);
		}
	}

	private void processHttpMessages(String baseUrl, int start, int count, Processor<RecordHistory> processor) throws ApiException {
		try {
			TableHistory tableHistory = Model.getSingleton().getDb()
					.getTableHistory();
			Vector<Integer> v = tableHistory.getHistoryList(Model
					.getSingleton().getSession().getSessionId());

			PaginationConstraintsChecker pcc = new PaginationConstraintsChecker(start, count);
			for (int i = 0; i < v.size(); i++) {
				int historyId = v.get(i).intValue();
				RecordHistory recHistory = tableHistory.read(historyId);

				if (HistoryReference.TYPE_TEMPORARY == recHistory.getHistoryType()) {
				    continue;
				}

				HttpMessage msg = recHistory.getHttpMessage();

				if (msg.getRequestHeader().isImage() || msg.getResponseHeader().isImage()) {
					continue;
				}

				if (baseUrl != null && ! msg.getRequestHeader().getURI().toString().startsWith(baseUrl)) {
					// Not subordinate to the specified URL
					continue;
				}

				pcc.recordProcessed();
				if (!pcc.hasPageStarted()) {
					continue;
				}

				processor.process(recHistory);
				if (pcc.hasPageEnded()) {
					break;
				}
			}
		} catch (HttpMalformedHeaderException | SQLException e) {
			logger.error(e.getMessage(), e);
			throw new ApiException(ApiException.Type.INTERNAL_ERROR);
		}
	}

	private interface Processor<T> {

		void process(T object);
	}

	private static class CounterProcessor<T> implements Processor<T> {

		private int count;

		public CounterProcessor() {
			count = 0;
		}

		@Override
		public void process(T object) {
			++count;
		}

		public int getCount() {
			return count;
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

	private static class PaginationConstraintsChecker {

		private boolean pageStarted;
		private boolean pageEnded;
		private final int startRecord;
		private final boolean hasEnd;
		private final int finalRecord;
		private int recordsProcessed;

		public PaginationConstraintsChecker(int start, int count) {
			recordsProcessed = 0;

			if (start > 0) {
				pageStarted = false;
				startRecord = start;
			} else {
				pageStarted = true;
				startRecord = 0;
			}

			if (count > 0) {
				hasEnd = true;
				finalRecord = !pageStarted ? start + count - 1 : count;
			} else {
				hasEnd = false;
				finalRecord = 0;
			}
			pageEnded = false;
		}

		public void recordProcessed() {
			++recordsProcessed;

			if (!pageStarted) {
				pageStarted = recordsProcessed >= startRecord;
			}

			if (hasEnd && !pageEnded) {
				pageEnded = recordsProcessed >= finalRecord;
			}
		}

		public boolean hasPageStarted() {
			return pageStarted;
		}

		public boolean hasPageEnded() {
			return pageEnded;
		}
	}

}
