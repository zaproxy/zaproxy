/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2011 The Zed Attack Proxy Team
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
package org.zaproxy.clientapi.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/*
 * TODO - these are more for zap testing :)
 * checkHosts
 * checkSites
 * checkUrls
 */

public class ClientApi {
	private Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8090));
	private boolean debug = false;

	public ClientApi (String zapAddress, int zapPort) {
		this(zapAddress, zapPort, false);
	}
	
	public ClientApi (String zapAddress, int zapPort, boolean debug) {
		proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(zapAddress, zapPort));
		this.debug = debug;
	}
	
	public void stopZap() throws Exception {
		accessUrlViaProxy(proxy, "http://zap/json/core/action/shutdown");
	}
	
	public void spiderUrl (String url) throws Exception {
		String result = openUrlViaProxy(proxy, "http://zap/json/spider/action/scan/?url=" + url).toString();
		if ( ! result.equals("[[\"OK\"]]")) {
			throw new Exception("Unexpected result: " + result);
		}
		// Poll until spider finished
		while ( ! openUrlViaProxy(proxy, "http://zap/json/spider/view/status").toString().equals("[[\"100\"]]")) {
			Thread.sleep(1000);
		}
	}
	
	public void activeScanUrl (String url) throws Exception {
		String result = openUrlViaProxy(proxy, "http://zap/json/ascan/action/scan/?url=" + url).toString();
		if ( ! result.equals("[[\"OK\"]]")) {
			throw new Exception("Unexpected result: " + result);
		}
		// Poll until spider finished
		while ( ! openUrlViaProxy(proxy, "http://zap/json/ascan/view/status").toString().equals("[[\"100\"]]")) {
			Thread.sleep(1000);
		}
	}
	
	public void accessUrl (String url) throws Exception {
		accessUrlViaProxy(proxy, url);
	}

	public void newSession (String name) throws Exception {
		String result = openUrlViaProxy(proxy, "http://zap/json/core/action/newsession/?name=" + name).toString();
		if ( ! result.equals("[[\"OK\"]]")) {
			throw new Exception("Unexpected result: " + result);
		}
	}

	public void loadSession (String name) throws Exception {
		String result = openUrlViaProxy(proxy, "http://zap/json/core/action/loadsession/?name=" + name).toString();
		if ( ! result.equals("[[\"OK\"]]")) {
			throw new Exception("Unexpected result: " + result);
		}
	}

	public void saveSession (String name) throws Exception {
		String result = openUrlViaProxy(proxy, "http://zap/json/core/action/savesession/?name=" + name).toString();
		if ( ! result.equals("[[\"OK\"]]")) {
			throw new Exception("Unexpected result: " + result);
		}
	}

	public void checkAlerts (List<Alert> ignoreAlerts, List<Alert> requireAlerts) throws Exception {
		List<Alert> reportAlerts = new ArrayList<Alert>();
		JSONArray response = JSONArray.fromObject(openUrlViaProxy(proxy, "http://zap/json/core/view/alerts").toString());
		if (response != null && response.size() == 1 && response.get(0) instanceof JSONArray) {
			JSONArray alerts = (JSONArray)response.get(0);
			Object[] alertObjs = alerts.toArray();
			for (Object alertObj : alertObjs) {
				boolean ignore = false;
				Alert foundAlert = new Alert((JSONObject) alertObj);
				
				if (ignoreAlerts != null) {
					for (Alert ignoreAlert : ignoreAlerts) {
						if (foundAlert.matches(ignoreAlert)) {
							if (debug) {
								System.out.println("Ignoring alert " + ignoreAlert);
							}
							ignore = true;
							break;
						}
					}
				}
				if (! ignore) {
					reportAlerts.add(foundAlert);
				}
				if (requireAlerts != null) {
					for (Alert requireAlert : requireAlerts) {
						if (foundAlert.matches(requireAlert)) {
							if (debug) {
								System.out.println("Found alert " + foundAlert);
							}
							requireAlerts.remove(requireAlert);
							break;
						}
					}
				}
			}
		}
		StringBuilder sb = new StringBuilder();
		if (reportAlerts.size() > 0) {
			sb.append("Found ").append(reportAlerts.size()).append(" alerts\n");
			for (Alert alert: reportAlerts) {
				sb.append('\t');
				sb.append(alert.toString());
				sb.append('\n');
			}
		}
		if (requireAlerts != null && requireAlerts.size() > 0) {
			if (sb.length() > 0) {
				sb.append('\n');
			}
			sb.append("Not found ").append(requireAlerts.size()).append(" alerts\n");
			for (Alert alert: requireAlerts) {
				sb.append('\t');
				sb.append(alert.toString());
				sb.append('\n');
			}
		}
		if (sb.length() > 0) {
			if (debug) {
				System.out.println("Failed: " + sb.toString());
			}
			throw new Exception (sb.toString());
		}
	}
	
	private List<String> openUrlViaProxy (Proxy proxy, String apiurl) throws Exception {
		List<String> response = new ArrayList<String>();
		URL url = new URL(apiurl);
		if (debug) {
			System.out.println("Open URL: " + apiurl);
		}
		HttpURLConnection uc = (HttpURLConnection)url.openConnection(proxy);
		uc.connect();
		
		BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
		String inputLine;

		while ((inputLine = in.readLine()) != null) {
			response.add(inputLine);
			if (debug) {
				System.out.println(inputLine);
			}
		}

		in.close();
		return response;
	}

	private void accessUrlViaProxy (Proxy proxy, String apiurl) throws Exception {
		URL url = new URL(apiurl);
		if (debug) {
			System.out.println("Open URL: " + apiurl);
		}
		HttpURLConnection uc = (HttpURLConnection)url.openConnection(proxy);
		uc.connect();
		
		BufferedReader in;
		try {
			in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
			String inputLine;

			while ((inputLine = in.readLine()) != null) {
				if (debug) {
					System.out.println(inputLine);
				}
			}
			in.close();

		} catch (IOException e) {
			// Ignore
			if (debug) {
				System.out.println("Ignoring exception " + e);
			}
		}

	}
}
