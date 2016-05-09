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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.zaproxy.clientapi.core.Alert.Confidence;
import org.zaproxy.clientapi.core.Alert.Risk;
import org.zaproxy.clientapi.gen.Acsrf;
import org.zaproxy.clientapi.gen.AjaxSpider;
import org.zaproxy.clientapi.gen.Ascan;
import org.zaproxy.clientapi.gen.Authentication;
import org.zaproxy.clientapi.gen.Authorization;
import org.zaproxy.clientapi.gen.Autoupdate;
import org.zaproxy.clientapi.gen.Break;
import org.zaproxy.clientapi.gen.Context;
import org.zaproxy.clientapi.gen.Core;
import org.zaproxy.clientapi.gen.ForcedUser;
import org.zaproxy.clientapi.gen.HttpSessions;
import org.zaproxy.clientapi.gen.ImportLogFiles;
import org.zaproxy.clientapi.gen.Params;
import org.zaproxy.clientapi.gen.Pnh;
import org.zaproxy.clientapi.gen.Pscan;
import org.zaproxy.clientapi.gen.Reveal;
import org.zaproxy.clientapi.gen.Script;
import org.zaproxy.clientapi.gen.Search;
import org.zaproxy.clientapi.gen.Selenium;
import org.zaproxy.clientapi.gen.SessionManagement;
import org.zaproxy.clientapi.gen.Spider;
import org.zaproxy.clientapi.gen.Stats;
import org.zaproxy.clientapi.gen.Users;

public class ClientApi {

	private static final int DEFAULT_CONNECTION_POOLING_IN_MS = 1000;

	private Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8090));
	private boolean debug = false;
	private PrintStream debugStream = System.out;

	private final String zapAddress;
	private final int zapPort;

	// Note that any new API implementations added have to be added here manually
	public Acsrf acsrf = new Acsrf(this);
	public AjaxSpider ajaxSpider = new AjaxSpider(this);
	public Ascan ascan = new Ascan(this);
	public Authentication authentication = new Authentication(this);
	public Authorization authorization = new Authorization(this);
	public Autoupdate autoupdate = new Autoupdate(this);
	public Break brk = new Break(this);
	public Context context = new Context(this);
	public Core core = new Core(this);
	public ForcedUser forcedUser = new ForcedUser(this);
	public HttpSessions httpSessions = new HttpSessions(this);
	public ImportLogFiles logImportFiles = new ImportLogFiles(this);
	public Params params = new Params(this);
	public Pnh pnh = new Pnh(this);
	public Pscan pscan = new Pscan(this);
	public Reveal reveal = new Reveal(this);
	public Search search = new Search(this);
	public Script script = new Script(this);
	public Selenium selenium = new Selenium(this);
	public SessionManagement sessionManagement = new SessionManagement(this);
	public Spider spider = new Spider(this);
	public Stats stats = new Stats(this);
	public Users users = new Users(this);

	public ClientApi (String zapAddress, int zapPort) {
		this(zapAddress, zapPort, false);
	}
	
	public ClientApi (String zapAddress, int zapPort, boolean debug) {
		proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(zapAddress, zapPort));
		this.debug = debug;
		this.zapAddress = zapAddress;
		this.zapPort = zapPort;
	}
	
	public void setDebugStream(PrintStream debugStream) {
		this.debugStream = debugStream;
	}

	public void accessUrl (String url) throws ClientApiException {
		accessUrlViaProxy(proxy, url);
	}

	private int statusToInt(ApiResponse response) {
		return Integer.parseInt(((ApiResponseElement)response).getValue());
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
            throw new ClientApiException("Check Alerts Failed!\n"+resultsString);
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
                        Confidence.valueOf(alertSet.getAttribute("confidence")),
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
		Document dom = this.callApiDom(component, type, method, params);
		return ApiResponseFactory.getResponse(dom.getFirstChild());
	}

	private Document callApiDom (String component, String type, String method,
			Map<String, String> params) throws ClientApiException {
		try {
			URL url = buildZapRequestUrl("xml", component, type, method, params);
			if (debug) {
				debugStream.println("Open URL: " + url);
			}
			//get the factory
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			//Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();
			//parse using builder to get DOM representation of the XML file
			return db.parse(getConnectionInputStream(url));
		} catch (Exception e) {
			throw new ClientApiException(e);
		}
	}

	private InputStream getConnectionInputStream(URL url) throws IOException {
		HttpURLConnection uc = (HttpURLConnection) url.openConnection(proxy);
		uc.connect();
		if (uc.getResponseCode() >= HttpURLConnection.HTTP_BAD_REQUEST) {
			return uc.getErrorStream();
		}
		return uc.getInputStream();
	}

	public byte[] callApiOther (String component, String type, String method,
			Map<String, String> params) throws ClientApiException {
		try {
			URL url = buildZapRequestUrl("other", component, type, method, params);
			if (debug) {
				debugStream.println("Open URL: " + url);
			}
			InputStream in = getConnectionInputStream(url);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buffer = new byte[8 * 1024];
			try {
				int bytesRead;
			    while ((bytesRead = in.read(buffer)) != -1) {
			    	out.write(buffer, 0, bytesRead);
			    }
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
                sb.append(encodeQueryParam(p.getKey()));
                sb.append('=');
                if (p.getValue() != null) {
                    sb.append(encodeQueryParam(p.getValue()));
                }
                sb.append('&');
            }
        }

        return new URL(sb.toString());
    }

    private static String encodeQueryParam(String param) {
        try {
            return URLEncoder.encode(param, "UTF-8");
        } catch (UnsupportedEncodingException ignore) {
            // UTF-8 is a standard charset.
        }
        return param;
    }

    public void addExcludeFromContext(String apikey, String contextName, String regex) throws Exception {
        context.excludeFromContext(apikey, contextName, regex);
    }

    public void addIncludeInContext(String apikey, String contextName, String regex) throws Exception {
        context.includeInContext(apikey, contextName, regex);
    }

    public void includeOneMatchingNodeInContext(String apikey, String contextName, String regex) throws Exception {
        List<String> sessionUrls = getSessionUrls();
        boolean foundOneMatch = false;
        for (String sessionUrl : sessionUrls){
            if (sessionUrl.matches(regex)){
                if (foundOneMatch){
                    addExcludeFromContext(apikey, contextName, sessionUrl);
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

    public void activeScanSiteInScope(String apikey, String url) throws Exception {
        ascan.scan(apikey, url, "true", "true", "", "", "");
        // Poll until spider finished
        int status = 0;
        while ( status < 100) {
            status = statusToInt(ascan.status(""));
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

    /**
     * Convenience method to wait for ZAP to be ready to receive API calls, when started programmatically.
     * <p>
     * It attempts to establish a connection to ZAP's proxy, in the given time, throwing an exception if the connection is not
     * successful. The connection attempts might be polled in one second interval.
     *
     * @param timeoutInSeconds the (maximum) number of seconds to wait for ZAP to start
     * @throws ClientApiException if the timeout was reached or if the thread was interrupted while waiting
     * @see #waitForSuccessfulConnectionToZap(int, int)
     */
    public void waitForSuccessfulConnectionToZap(int timeoutInSeconds) throws ClientApiException {
        waitForSuccessfulConnectionToZap(timeoutInSeconds, DEFAULT_CONNECTION_POOLING_IN_MS);
    }

    /**
     * Convenience method to wait for ZAP to be ready to receive API calls, when started programmatically.
     * <p>
     * It attempts to establish a connection to ZAP's proxy, in the given time, throwing an exception if the connection is not
     * successful. The connection attempts are done with the given polling interval.
     *
     * @param timeoutInSeconds the (maximum) number of seconds to wait for ZAP to start
     * @param pollingIntervalInMs the interval, in milliseconds, for connection polling
     * @throws ClientApiException if the timeout was reached or if the thread was interrupted while waiting.
     * @throws IllegalArgumentException if the interval for connection polling is negative.
     * @see #waitForSuccessfulConnectionToZap(int)
     */
    public void waitForSuccessfulConnectionToZap(int timeoutInSeconds, int pollingIntervalInMs) throws ClientApiException {
        int timeoutInMs = (int) TimeUnit.SECONDS.toMillis(timeoutInSeconds);
        int connectionTimeoutInMs = timeoutInMs;
        boolean connectionSuccessful = false;
        long startTime = System.currentTimeMillis();
        do {
            try (Socket socket = new Socket()) {
                try {
                    socket.connect(new InetSocketAddress(zapAddress, zapPort), connectionTimeoutInMs);
                    connectionSuccessful = true;
                } catch (SocketTimeoutException ignore) {
                    throw newTimeoutConnectionToZap(timeoutInSeconds);
                } catch (IOException ignore) {
                    // and keep trying but wait some time first...
                    try {
                        Thread.sleep(pollingIntervalInMs);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new ClientApiException(
                                "The ClientApi was interrupted while sleeping between connection polling.",
                                e);
                    }

                    long ellapsedTime = System.currentTimeMillis() - startTime;
                    if (ellapsedTime >= timeoutInMs) {
                        throw newTimeoutConnectionToZap(timeoutInSeconds);
                    }
                    connectionTimeoutInMs = (int) (timeoutInMs - ellapsedTime);
                }
            } catch (IOException ignore) {
                // the closing state doesn't matter.
            }
        } while (!connectionSuccessful);
    }

    private static ClientApiException newTimeoutConnectionToZap(int timeoutInSeconds) {
        return new ClientApiException("Unable to connect to ZAP's proxy after " + timeoutInSeconds + " seconds.");
    }
}
