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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.db.RecordAlert;
import org.parosproxy.paros.db.RecordHistory;
import org.parosproxy.paros.db.TableAlert;
import org.parosproxy.paros.db.TableHistory;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.utils.XMLStringUtil;

public class CoreAPI extends ApiImplementor {

	private static Logger log = Logger.getLogger(CoreAPI.class);

	private static final String PREFIX = "core";
	private static final String ACTION_LOAD_SESSION = "loadsession";
	private static final String ACTION_NEW_SESSION = "newsession";
	private static final String ACTION_SAVE_SESSION = "savesession";
	private static final String ACTION_SHUTDOWN = "shutdown";
	private static final String VIEW_ALERTS = "alerts";
	private static final String VIEW_HOSTS = "hosts";
	private static final String VIEW_SITES = "sites";
	private static final String VIEW_URLS = "urls";
	private static final String VIEW_HTTP = "http";

	private static final String ACTION_SESSION_PARAM_NAME = "name";

	private Logger logger = Logger.getLogger(this.getClass());

	public CoreAPI() {

		List<String> params = new ArrayList<String>();
		params.add(ACTION_SESSION_PARAM_NAME);
		this.addApiAction(new ApiAction(ACTION_SHUTDOWN));
		this.addApiAction(new ApiAction(ACTION_NEW_SESSION, params));
		this.addApiAction(new ApiAction(ACTION_LOAD_SESSION, params));
		this.addApiAction(new ApiAction(ACTION_SAVE_SESSION, params));
		this.addApiView(new ApiView(VIEW_ALERTS));
		this.addApiView(new ApiView(VIEW_HOSTS));
		this.addApiView(new ApiView(VIEW_SITES));
		this.addApiView(new ApiView(VIEW_URLS));
		this.addApiView(new ApiView(VIEW_HTTP));
	}

	@Override
	public String getPrefix() {
		return PREFIX;
	}

	@Override
	public JSON handleApiAction(String name, JSONObject params)
			throws ApiException {

		// Session session = API.getInstance().getSession();
		Session session = Model.getSingleton().getSession();

		if (ACTION_SHUTDOWN.equals(name)) {
			Control.getSingleton().shutdown(false);
			log.info(Constant.PROGRAM_TITLE + " terminated.");
			System.exit(0);

		} else if (ACTION_SAVE_SESSION.equals(name)) {
			String sessionName = params.getString(ACTION_SESSION_PARAM_NAME);
			if (sessionName == null || sessionName.length() == 0) {
				throw new ApiException(ApiException.Type.MISSING_PARAMETER,
						ACTION_SESSION_PARAM_NAME);
			}
			session.setSessionName(name);
			if (!sessionName.endsWith(".session")) {
				sessionName = sessionName + ".session";
			}
			String filename = Model.getSingleton().getOptionsParam()
					.getUserDirectory()
					+ File.separator + sessionName;
			if (new File(filename).exists()) {
				throw new ApiException(ApiException.Type.INTERNAL_ERROR,
						filename);
			}
			try {
		    	Control.getSingleton().saveSession(filename);
			} catch (Exception e) {
				throw new ApiException(ApiException.Type.INTERNAL_ERROR,
						e.getMessage());
			}
		} else if (ACTION_LOAD_SESSION.equals(name)) {
			String sessionName = params.getString(ACTION_SESSION_PARAM_NAME);
			if (sessionName == null || sessionName.length() == 0) {
				throw new ApiException(ApiException.Type.MISSING_PARAMETER,
						ACTION_SESSION_PARAM_NAME);
			}
			if (!sessionName.endsWith(".session")) {
				sessionName = sessionName + ".session";
			}
			String filename = Model.getSingleton().getOptionsParam()
					.getUserDirectory()
					+ File.separator + sessionName;
			if (!new File(filename).exists()) {
				throw new ApiException(ApiException.Type.INTERNAL_ERROR,
						filename);
			}
			try {
				Control.getSingleton().runCommandLineOpenSession(filename);
			} catch (Exception e) {
				throw new ApiException(ApiException.Type.INTERNAL_ERROR,
						e.getMessage());
			}

		} else if (ACTION_NEW_SESSION.equals(name)) {
			String sessionName = params.getString(ACTION_SESSION_PARAM_NAME);
			if (sessionName == null || sessionName.length() == 0) {
				throw new ApiException(ApiException.Type.MISSING_PARAMETER,
						ACTION_SESSION_PARAM_NAME);
			}
			session.setSessionName(name);
			if (!sessionName.endsWith(".session")) {
				sessionName = sessionName + ".session";
			}
			String filename = Model.getSingleton().getOptionsParam()
					.getUserDirectory()
					+ File.separator + sessionName;
			if (new File(filename).exists()) {
				throw new ApiException(ApiException.Type.INTERNAL_ERROR,
						filename);
			}
			try {
				Control.getSingleton().runCommandLineNewSession(filename);
			} catch (Exception e) {
				throw new ApiException(ApiException.Type.INTERNAL_ERROR,
						e.getMessage());
			}
		} else {
			throw new ApiException(ApiException.Type.BAD_ACTION);
		}
		JSONArray result = new JSONArray();
		result.add("OK");
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSON handleApiView(String name, JSONObject params)
			throws ApiException {
		JSONArray result = new JSONArray();
		Session session = Model.getSingleton().getSession();

		if (VIEW_HOSTS.equals(name)) {
			SiteNode root = (SiteNode) session.getSiteTree().getRoot();
			Enumeration<SiteNode> en = root.children();
			while (en.hasMoreElements()) {
				String site = en.nextElement().getNodeName();
				if (site.indexOf("//") >= 0) {
					site = site.substring(site.indexOf("//") + 2);
				}
				if (site.indexOf(":") >= 0) {
					site = site.substring(0, site.indexOf(":"));
				}
				result.add(site);
			}
		} else if (VIEW_SITES.equals(name)) {
			SiteNode root = (SiteNode) session.getSiteTree().getRoot();
			Enumeration<SiteNode> en = root.children();
			while (en.hasMoreElements()) {
				result.add(en.nextElement().getNodeName());
			}
		} else if (VIEW_URLS.equals(name)) {
			SiteNode root = (SiteNode) session.getSiteTree().getRoot();
			this.getURLs(root, result);
		} else if (VIEW_ALERTS.equals(name)) {
			List<Alert> alerts = getAlerts();
			for (Alert alert : alerts) {
				result.add(this.alertToJSON(alert));
			}
		} else if (VIEW_HTTP.equals(name)) {

			ArrayList<HttpMessage> hm = null;
			try {
				hm = getHttpMessages();
				for (HttpMessage httpm : hm) {
					result.add(this.httpMessageToJSON(httpm));
				}
			} catch (HttpMalformedHeaderException e) {
				logger.error(e.getMessage(), e);
			}

		
		} else {
			throw new ApiException(ApiException.Type.BAD_VIEW);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private void getURLs(SiteNode parent, JSONArray children) {
		Enumeration<SiteNode> en = parent.children();
		while (en.hasMoreElements()) {
			SiteNode child = en.nextElement();
			String site = child.getNodeName();
			if (site.indexOf("//") >= 0) {
				site = site.substring(site.indexOf("//") + 2);
			}
			try {
				children.add(child.getHistoryReference().getHttpMessage()
						.getRequestHeader().getURI().toString());
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			getURLs(child, children);
		}
	}

	private JSONObject alertToJSON(Alert alert) {
		JSONObject ja = new JSONObject();
		ja.put("alert", alert.getAlert());
		ja.put("description", alert.getDescription());
		ja.put("risk", Alert.MSG_RISK[alert.getRisk()]);
		ja.put("reliability", Alert.MSG_RELIABILITY[alert.getReliability()]);
		ja.put("url", alert.getUri());
		ja.put("other", alert.getOtherInfo());
		ja.put("param", XMLStringUtil.escapeControlChrs(alert.getParam()));
		ja.put("attack", XMLStringUtil.escapeControlChrs(alert.getAttack()));
		ja.put("reference", alert.getReference());
		ja.put("solution", alert.getSolution());
		return ja;
	}

	/**
	 * 
	 * @param hm
	 * @return
	 */
	private JSONObject httpMessageToJSON(HttpMessage hm) {
		JSONObject ja = new JSONObject();
		ja.put("cookieParams", XMLStringUtil.escapeControlChrs(hm.getCookieParamsAsString().toString()));
		ja.put("note", hm.getNote().toString());
		ja.put("requestHeader", XMLStringUtil.escapeControlChrs(hm.getRequestHeader().toString()));
		ja.put("requestBody", XMLStringUtil.escapeControlChrs(hm.getRequestBody().toString()));
		ja.put("responseHeader", XMLStringUtil.escapeControlChrs(hm.getResponseHeader().toString()));
		
		if (HttpHeader.GZIP.equals(hm.getResponseHeader().getHeader(HttpHeader.CONTENT_ENCODING))) {
			// Uncompress gziped content
			try {
				ByteArrayInputStream bais = new ByteArrayInputStream(hm.getResponseBody().getBytes());
				GZIPInputStream gis = new GZIPInputStream(bais);
				InputStreamReader isr = new InputStreamReader(gis);
				BufferedReader br = new BufferedReader(isr);
				StringBuffer sb = new StringBuffer();
				String line = null;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				br.close();
				isr.close();
				gis.close();
				bais.close();
				ja.put("responseBody", XMLStringUtil.escapeControlChrs(sb.toString()));
			} catch (IOException e) {
				//this.log.error(e.getMessage(), e);
				System.out.println(e);
			}
		} else {
			ja.put("responseBody", XMLStringUtil.escapeControlChrs(hm.getResponseBody().toString()));
		}
		
		return ja;
	}

	@Override
	public String viewResultToXML(String name, JSON result) {
		XMLSerializer serializer = new XMLSerializer();
		if (VIEW_HOSTS.equals(name)) {
			serializer.setArrayName("hosts");
			serializer.setElementName("host");
		} else if (VIEW_SITES.equals(name)) {
			serializer.setArrayName("sites");
			serializer.setElementName("site");
		} else if (VIEW_URLS.equals(name)) {
			serializer.setArrayName("urls");
			serializer.setElementName("url");
		} else if (VIEW_ALERTS.equals(name)) {
			serializer.setArrayName("alerts");
			serializer.setElementName("alert");
		} else if (VIEW_HTTP.equals(name)) {
			serializer.setArrayName("https");
			serializer.setElementName("http");
		}
		return serializer.write(result);
	}

	@Override
	public String actionResultToXML(String name, JSON result) {
		XMLSerializer serializer = new XMLSerializer();
		serializer.setArrayName("result");
		return serializer.write(result);
	}

	private List<Alert> getAlerts() throws ApiException {
		List<Alert> alerts = new ArrayList<Alert>();
		try {
			TableAlert tableAlert = Model.getSingleton().getDb()
					.getTableAlert();
			Vector<Integer> v = tableAlert.getAlertList();

			for (int i = 0; i < v.size(); i++) {
				int alertId = ((Integer) v.get(i)).intValue();
				RecordAlert recAlert = tableAlert.read(alertId);
				Alert alert = new Alert(recAlert);

				if (alert.getReliability() != Alert.FALSE_POSITIVE
						&& !alerts.contains(alert)) {
					alerts.add(alert);
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
	private ArrayList<HttpMessage> getHttpMessages() throws ApiException,
			HttpMalformedHeaderException {
		try {
			TableHistory tableHistory = Model.getSingleton().getDb()
					.getTableHistory();
			Vector<Integer> v = tableHistory.getHistoryList(Model
					.getSingleton().getSession().getSessionId());
			ArrayList<HttpMessage> mgss = new ArrayList<HttpMessage>();
			for (int i = 0; i < v.size(); i++) {
				int sessionId = ((Integer) v.get(i)).intValue();
				RecordHistory recAlert = tableHistory.read(sessionId);
				HttpMessage msg = recAlert.getHttpMessage();
				if ( ! msg.getRequestHeader().isImage() && ! msg.getResponseHeader().isImage()) {
					mgss.add(msg);
				}
			}
			return mgss;
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			throw new ApiException(ApiException.Type.INTERNAL_ERROR);
		}
	}

}
