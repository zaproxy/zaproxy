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

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.zaproxy.clientapi.core.Alert.Reliability;
import org.zaproxy.clientapi.core.Alert.Risk;
import org.zaproxy.clientapi.gen.Acsrf;
import org.zaproxy.clientapi.gen.Ascan;
import org.zaproxy.clientapi.gen.Auth;
import org.zaproxy.clientapi.gen.Autoupdate;
import org.zaproxy.clientapi.gen.Break;
import org.zaproxy.clientapi.gen.Context;
import org.zaproxy.clientapi.gen.Core;
import org.zaproxy.clientapi.gen.HttpSessions;
import org.zaproxy.clientapi.gen.Params;
import org.zaproxy.clientapi.gen.Pscan;
import org.zaproxy.clientapi.gen.Search;
import org.zaproxy.clientapi.gen.Spider;

public class ClientApi {
	private Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8090));
	private boolean debug = false;
	private PrintStream debugStream = System.out;

	// Note that any new API implementations added have to be added here manually
	public Acsrf acsrf = new Acsrf(this);
	public Ascan ascan = new Ascan(this);
	public Auth auth = new Auth(this);
	public Autoupdate autoupdate = new Autoupdate(this);
	public Core core = new Core(this);
	public HttpSessions httpSessions = new HttpSessions(this);
	public Params params = new Params(this);
	public Pscan pscan = new Pscan(this);
	public Search search = new Search(this);
	public Spider spider = new Spider(this);
    public Context context = new Context(this);
    public Break brk = new Break(this);

	public ClientApi (String zapAddress, int zapPort) {
		this(zapAddress, zapPort, false);
	}
	
	public ClientApi (String zapAddress, int zapPort, boolean debug) {
		proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(zapAddress, zapPort));
		this.debug = debug;
	}
	
	public void setDebugStream(PrintStream debugStream) {
		this.debugStream = debugStream;
	}

	public void accessUrl (String url) throws ClientApiException {
		accessUrlViaProxy(proxy, url);
	}


	/*
	 * These methods are retained for some backwards compatibility
	 */

	/**
	 * @deprecated  As of release 2.0.0, replaced by core.shutdown()
	 */
	@Deprecated
	public void stopZap() throws ClientApiException {
		core.shutdown();
	}

	/**
	 * @deprecated  As of release 2.0.0, replaced by spider.scan(url) and polling spider.status()
	 */
	@Deprecated
	public void spiderUrl (String url) throws ClientApiException {
		spider.scan(url);
		// Poll until spider finished
		while ( statusToInt(spider.status()) < 100) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// Ignore
			}
		}
	}

	private int statusToInt(ApiResponse response) {
		return Integer.parseInt(((ApiResponseElement)response).getValue());
	}

	/**
	 * @deprecated  As of release 2.0.0, replaced by ascan.scan(url, recurse) and polling ascan.status()
	 */
	@Deprecated
	public void activeScanUrl (String url) throws ClientApiException {
		ascan.scan(url, "true", "false");
		// Poll until spider finished
		while ( statusToInt(ascan.status()) < 100) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// Ignore
			}
		}
	}

	/**
	 * @deprecated  As of release 2.0.0, replaced by ascan.scan(url, recurse) and polling ascan.status()
	 */
	@Deprecated
    public void activeScanSite (String url) throws ClientApiException {
		ascan.scan(url, "true", "false");
		// Poll until spider finished
		while ( statusToInt(ascan.status()) < 100) {
            try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// Ignore
			}
        }
    }
	
	/**
	 * @deprecated  As of release 2.0.0, replaced by core.newSession("");
	 */
	@Deprecated
	public void newSession () throws ClientApiException {
		core.newSession("", "true");
	}

	/**
	 * @deprecated  As of release 2.0.0, replaced by core.newSession(name);
	 */
	@Deprecated
	public void newSession (String name) throws ClientApiException {
		core.newSession(name, "true");
	}

	/**
	 * @deprecated  As of release 2.0.0, replaced by core.loadSession(name);
	 */
	@Deprecated
	public void loadSession (String name) throws ClientApiException {
		core.loadSession(name);
	}

	/**
	 * @deprecated  As of release 2.0.0, replaced by core.saveSession(name);
	 */
	@Deprecated
	public void saveSession (String name) throws ClientApiException {
		core.saveSession(name, "true");
	}

	public void checkAlerts (List<Alert> ignoreAlerts, List<Alert> requireAlerts) throws ClientApiException {
        HashMap<String,List<Alert>> results = checkForAlerts(ignoreAlerts, requireAlerts);
        verifyAlerts(results.get("requireAlerts"), results.get("reportAlerts"));
	}

    private void verifyAlerts(List<Alert> requireAlerts, List<Alert> reportAlerts) throws ClientApiException {
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
                debugStream.println("Failed: " + sb.toString());
            }
            throw new ClientApiException (sb.toString());
        }
    }

    public void checkAlerts(List<Alert> ignoreAlerts, List<Alert> requireAlerts, File outputFile) throws ClientApiException {
        HashMap<String,List<Alert>> results = checkForAlerts(ignoreAlerts, requireAlerts);
        int alertsFound = results.get("reportAlerts").size();
        int alertsNotFound = results.get("requireAlerts").size();
        int alertsIgnored = results.get("ignoredAlerts").size();
        String resultsString = String.format("Alerts Found: %d, Alerts required but not found: %d, Alerts ignored: %d", alertsFound, alertsNotFound, alertsIgnored);
        try {
			AlertsFile.saveAlertsToFile(results.get("requireAlerts"), results.get("reportAlerts"), results.get("ignoredAlerts"), outputFile);
		} catch (Exception e) {
            throw new ClientApiException (e);
		}
        if (alertsFound>0 || alertsNotFound>0){
            fail("Check Alerts Failed!\n"+resultsString);
        }else{
        	if (debug) {
        		debugStream.println("Check Alerts Passed!\n" + resultsString);
        	}
        }
    }

    public List<Alert> getAlerts(String baseUrl, int start, int count) throws ClientApiException {
    	List<Alert> alerts = new ArrayList<Alert>();
        ApiResponse response = core.alerts(baseUrl, String.valueOf(start), String.valueOf(count));
        if (response != null && response instanceof ApiResponseList) {
            ApiResponseList alertList = (ApiResponseList)response;
            for (ApiResponse resp : alertList.getItems()) {
            	ApiResponseSet alertSet = (ApiResponseSet)resp;
                alerts.add(new Alert(
                		alertSet.getAttribute("alert"),
                        alertSet.getAttribute("url"),
                        Risk.valueOf(alertSet.getAttribute("risk")),
                        Reliability.valueOf(alertSet.getAttribute("reliability")),
                        alertSet.getAttribute("param"),
                        alertSet.getAttribute("other"),
                        alertSet.getAttribute("attack"),
                        alertSet.getAttribute("description"),
                        alertSet.getAttribute("reference"),
                        alertSet.getAttribute("solution"),
                        alertSet.getAttribute("evidence"),
                        Integer.parseInt(alertSet.getAttribute("cweid")),
                        Integer.parseInt(alertSet.getAttribute("wascid"))));
            }
        }
    	return alerts;
    }

    private HashMap<String, List<Alert>> checkForAlerts(List<Alert> ignoreAlerts, List<Alert> requireAlerts) throws ClientApiException {
        List<Alert> reportAlerts = new ArrayList<>();
        List<Alert> ignoredAlerts = new ArrayList<>();
        List<Alert> alerts = getAlerts(null, -1, -1);
        for (Alert alert : alerts) {
            boolean ignore = false;
            if (ignoreAlerts != null) {
                for (Alert ignoreAlert : ignoreAlerts) {
                    if (alert.matches(ignoreAlert)) {
                        if (debug) {
                            debugStream.println("Ignoring alert " + ignoreAlert);
                        }
                        ignoredAlerts.add(alert);
                        ignore = true;
                        break;
                    }
                }
            }
            if (! ignore) {
                reportAlerts.add(alert);
            }
            if (requireAlerts != null) {
                for (Alert requireAlert : requireAlerts) {
                    if (alert.matches(requireAlert)) {
                        if (debug) {
                            debugStream.println("Found alert " + alert);
                        }
                        requireAlerts.remove(requireAlert);
                        // Remove it from the not-ignored list as well
                        reportAlerts.remove(alert);
                        break;
                    }
                }
            }
        }
        HashMap<String, List<Alert>> results = new HashMap<>();
        results.put("reportAlerts", reportAlerts);
        results.put("requireAlerts", requireAlerts);
        results.put("ignoredAlerts", ignoredAlerts);
        return results;
    }

	private void accessUrlViaProxy (Proxy proxy, String apiurl) throws ClientApiException {
		try {
			URL url = new URL(apiurl);
			if (debug) {
				debugStream.println("Open URL: " + apiurl);
			}
			HttpURLConnection uc = (HttpURLConnection)url.openConnection(proxy);
			uc.connect();

			BufferedReader in;
			try {
				in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
				String inputLine;

				while ((inputLine = in.readLine()) != null) {
					if (debug) {
						debugStream.println(inputLine);
					}
				}
				in.close();

			} catch (IOException e) {
				// Ignore
				if (debug) {
					debugStream.println("Ignoring exception " + e);
				}
			}
		} catch (Exception e) {
			throw new ClientApiException (e);
		}
	}

	public ApiResponse callApi (String component, String type, String method,
			Map<String, String> params) throws ClientApiException {
		Document dom;
		try {
			dom = this.callApiDom(component, type, method, params);
		} catch (Exception e) {
			throw new ClientApiException(e);
		}
		return ApiResponseFactory.getResponse(dom.getFirstChild());
	}

	private Document callApiDom (String component, String type, String method,
			Map<String, String> params) throws ClientApiException {
		try {
			URL url = buildZapRequestUrl("xml", component, type, method, params);
			if (debug) {
				debugStream.println("Open URL: " + url);
			}
			HttpURLConnection uc = (HttpURLConnection)url.openConnection(proxy);
			//get the factory
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			//Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();
			//parse using builder to get DOM representation of the XML file
			return db.parse(uc.getInputStream());
		} catch (Exception e) {
			throw new ClientApiException(e);
		}
	}

	public byte[] callApiOther (String component, String type, String method,
			Map<String, String> params) throws ClientApiException {
		try {
			URL url = buildZapRequestUrl("other", component, type, method, params);
			if (debug) {
				debugStream.println("Open URL: " + url);
			}
			HttpURLConnection uc = (HttpURLConnection)url.openConnection(proxy);
			InputStream in = uc.getInputStream();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buffer = new byte[8 * 1024];
			try {
				int bytesRead;
			    while ((bytesRead = in.read(buffer)) != -1) {
			    	out.write(buffer, 0, bytesRead);
			    }
			} catch (IOException e) {
				throw new ClientApiException(e);
			} finally {
				out.close();
				in.close();
			}
			return out.toByteArray();
			
		} catch (Exception e) {
			throw new ClientApiException(e);
		}
	}

    private static URL buildZapRequestUrl(
            String format,
            String component,
            String type,
            String method,
            Map<String, String> params) throws MalformedURLException {
        StringBuilder sb = new StringBuilder();
        sb.append("http://zap/");
        sb.append(format);
        sb.append('/');
        sb.append(component);
        sb.append('/');
        sb.append(type);
        sb.append('/');
        sb.append(method);
        sb.append('/');
        if (params != null) {
            sb.append('?');
            for (Map.Entry<String, String> p : params.entrySet()) {
                sb.append(p.getKey());
                sb.append('=');
                if (p.getValue() != null) {
                    sb.append(p.getValue());
                }
                sb.append('&');
            }
        }

        return new URL(sb.toString());
    }

    public void addExcludeFromContext(String contextName, String regex) throws Exception {
        context.excludeFromContext(contextName, regex);
    }

    public void addIncludeInContext(String contextName, String regex) throws Exception {
        context.includeInContext(contextName, regex);
    }

    public void includeOneMatchingNodeInContext(String contextName, String regex) throws Exception {
        List<String> sessionUrls = getSessionUrls();
        boolean foundOneMatch = false;
        for (String sessionUrl : sessionUrls){
            if (sessionUrl.matches(regex)){
                if (foundOneMatch){
                    addExcludeFromContext(contextName, sessionUrl);
                } else {
                    foundOneMatch = true;
                }
            }
        }
        if(!foundOneMatch){
            throw new Exception("Unexpected result: No url found in site tree matching regex " + regex);
        }

    }

    private List<String> getSessionUrls() throws Exception {
        List<String> sessionUrls = new ArrayList<>();
        ApiResponse response = core.urls();
        if (response != null && response instanceof ApiResponseList) {
            ApiResponseElement urlList = (ApiResponseElement) ((ApiResponseList) response).getItems().get(0);
            for (ApiResponse element: ((ApiResponseList) response).getItems()){
                URL url = new URL(((ApiResponseElement)element).getValue());
                sessionUrls.add(url.getProtocol()+"://"+url.getHost()+url.getPath());
            }
            System.out.println(urlList);
        }
        return sessionUrls;
    }

    public void activeScanSiteInScope(String url) throws Exception {
        ascan.scan(url, "true", "true");
        // Poll until spider finished
        int status = 0;
        while ( status < 100) {
            status = statusToInt(ascan.status());
            if(debug){
                String format = "Scanning %s Progress: %d%%";
                System.out.println(String.format(format, url, status));
            }try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // Ignore
            }
        }
    }
}
