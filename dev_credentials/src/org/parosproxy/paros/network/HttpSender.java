/*
 *
 * Paros and its related class files.
 * 
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2004 Chinotec Technologies Company
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Clarified Artistic License
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Clarified Artistic License for more details.
 * 
 * You should have received a copy of the Clarified Artistic License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
// ZAP: 2011/09/19 Added debugging
// ZAP: 2012/04/23 Removed unnecessary cast.
// ZAP: 2012/05/08 Use custom http client on "Connection: Upgrade" in executeMethod().
//                 Retrieve upgraded socket and save for later use in send() method.
// ZAP: 2012/08/07 Issue 342 Support the HttpSenderListener
// ZAP: 2012/12/27 Do not read request body on Server-Sent Event streams.
// ZAP: 2013/01/03 Resolved Checkstyle issues: removed throws HttpException 
//                 declaration where IOException already appears, 
//                 introduced two helper methods for notifying listeners.
// ZAP: 2013/01/19 Issue 459: Active scanner locking
// ZAP: 2013/01/23 Clean up of exception handling/logging.
// ZAP: 2013/01/30 Issue 478: Allow to choose to send ZAP's managed cookies on 
// a single Cookie request header and set it as the default
// ZAP: 2013/07/10 Issue 720: Cannot send non standard http methods 
// ZAP: 2013/07/14 Issue 729: Update NTLM authentication code
// ZAP: 2013/07/25 Added support for sending the message from the perspective of a User
// ZAP: 2013/08/31 Reauthentication when sending a message from the perspective of a User
// ZAP: 2013/09/07 Switched to using HttpState for requesting User for cookie management

package org.parosproxy.paros.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.log4j.Logger;
import org.zaproxy.zap.ZapGetMethod;
import org.zaproxy.zap.ZapHttpConnectionManager;
import org.zaproxy.zap.network.HttpSenderListener;
import org.zaproxy.zap.network.ZapNTLMScheme;

public class HttpSender {
	public static final int PROXY_INITIATOR = 1;
	public static final int ACTIVE_SCANNER_INITIATOR = 2;
	public static final int SPIDER_INITIATOR = 3;
	public static final int FUZZER_INITIATOR = 4;
	public static final int AUTHENTICATION_INITIATOR = 5;
	public static final int MANUAL_REQUEST_INITIATOR = 6;
	public static final int CHECK_FOR_UPDATES_INITIATOR = 7;
	public static final int BEAN_SHELL_INITIATOR = 8;

	private static Logger log = Logger.getLogger(HttpSender.class);

	private static ProtocolSocketFactory sslFactory = null;
	private static Protocol protocol = null;

	// Issue 90
	private static boolean allowUnsafeSSLRenegotiation = false;

	private static List<HttpSenderListener> listeners = new ArrayList<>();
	private static Comparator<HttpSenderListener> listenersComparator = null;;

	static {
		try {
			protocol = Protocol.getProtocol("https");
			sslFactory = protocol.getSocketFactory();
		} catch (Exception e) {
		}
		// avoid init again if already initialized
		if (sslFactory == null || !(sslFactory instanceof SSLConnector)) {
			Protocol.registerProtocol("https", new Protocol("https",
					(ProtocolSocketFactory) new SSLConnector(), 443));
		}

		AuthPolicy.registerAuthScheme(AuthPolicy.NTLM, ZapNTLMScheme.class);
	}

	private static HttpMethodHelper helper = new HttpMethodHelper();
	private static String userAgent = "";

	private HttpClient client = null;
	private HttpClient clientViaProxy = null;
	private ConnectionParam param = null;
	private MultiThreadedHttpConnectionManager httpConnManager = null;
	private MultiThreadedHttpConnectionManager httpConnManagerProxy = null;
	private boolean followRedirect = false;
	private boolean allowState = false;
	private int initiator = -1;

	/*
	 * public HttpSender(ConnectionParam connectionParam, boolean allowState) { this
	 * (connectionParam, allowState, -1); }
	 */

	public HttpSender(ConnectionParam connectionParam, boolean allowState, int initiator) {
		this.param = connectionParam;
		this.allowState = allowState;
		this.initiator = initiator;

		client = createHttpClient();
		clientViaProxy = createHttpClientViaProxy();

		if (this.allowState) {
			checkState();
		}
		addAuth(client);
		addAuth(clientViaProxy);
	}

	public static SSLConnector getSSLConnector() {
		return (SSLConnector) protocol.getSocketFactory();
	}

	public static void setAllowUnsafeSSLRenegotiation(boolean enabled) {
		allowUnsafeSSLRenegotiation = enabled;

		if (allowUnsafeSSLRenegotiation) {
			log.info("Unsafe SSL renegotiation enabled.");
		} else {
			log.info("Unsafe SSL renegotiation disabled.");
		}

		String value = String.valueOf(allowUnsafeSSLRenegotiation).toLowerCase();
		System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", value);

	}

	private void checkState() {
		if (param.isHttpStateEnabled()) {
			client.setState(param.getHttpState());
			clientViaProxy.setState(param.getHttpState());
			client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
			clientViaProxy.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
			final boolean singleCookieRequestHeader = param.isSingleCookieRequestHeader();
			client.getParams().setBooleanParameter(HttpMethodParams.SINGLE_COOKIE_HEADER,
					singleCookieRequestHeader);
			clientViaProxy.getParams().setBooleanParameter(HttpMethodParams.SINGLE_COOKIE_HEADER,
					singleCookieRequestHeader);
		} else {
			client.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
			clientViaProxy.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
		}
	}

	private HttpClient createHttpClient() {

		httpConnManager = new MultiThreadedHttpConnectionManager();
		setCommonManagerParams(httpConnManager);
		return new HttpClient(httpConnManager);
	}

	private HttpClient createHttpClientViaProxy() {

		if (param.getProxyChainName().equals("")) {
			return createHttpClient();
		}

		httpConnManagerProxy = new MultiThreadedHttpConnectionManager();
		setCommonManagerParams(httpConnManagerProxy);
		HttpClient clientProxy = new HttpClient(httpConnManagerProxy);
		clientProxy.getHostConfiguration().setProxy(param.getProxyChainName(), param.getProxyChainPort());

		if (!param.getProxyChainUserName().equals("")) {
			// NTCredentials credentials = new NTCredentials(
			// param.getProxyChainUserName(), param.getProxyChainPassword(),
			// param.getProxyChainName(), param.getProxyChainName());
			NTCredentials credentials = new NTCredentials(param.getProxyChainUserName(),
					param.getProxyChainPassword(), "", param.getProxyChainRealm().equals("") ? ""
							: param.getProxyChainRealm());
			// Below is the original code, but user reported that above code works.
			// UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
			// param.getProxyChainUserName(), param.getProxyChainPassword());
			AuthScope authScope = new AuthScope(param.getProxyChainName(), param.getProxyChainPort(), param
					.getProxyChainRealm().equals("") ? AuthScope.ANY_REALM : param.getProxyChainRealm());

			clientProxy.getState().setProxyCredentials(authScope, credentials);
		}

		return clientProxy;
	}

	public int executeMethod(HttpMethod method, HttpState state) throws IOException {
		int responseCode = -1;

		String hostName;
		hostName = method.getURI().getHost();
		method.setDoAuthentication(true);

		if (param.isUseProxy(hostName)) {
			responseCode = clientViaProxy.executeMethod(method);
		} else {
			// ZAP: use custom client on upgrade connection and on event-source data type
			Header connectionHeader = method.getRequestHeader("connection");
			boolean isUpgrade = connectionHeader != null
					&& connectionHeader.getValue().toLowerCase().contains("upgrade");

			// ZAP: try to apply original handling of ParosProxy
			HttpClient requestClient=client;
			if (isUpgrade) {
				// Unless upgrade, when using another client that allows us to expose the socket connection.
				requestClient = new HttpClient(new ZapHttpConnectionManager());
			}
			
			// Check if a custom state is being used
			if (state != null)
				responseCode = requestClient.executeMethod(null, method, state);
			else
				responseCode = requestClient.executeMethod(method);
		}

		return responseCode;
	}

	public void shutdown() {
		if (httpConnManager != null) {
			httpConnManager.shutdown();
		}
		if (httpConnManagerProxy != null) {
			httpConnManagerProxy.shutdown();
		}
	}

	private void addAuth(HttpClient client) {
		List<HostAuthentication> list = param.getListAuthEnabled();
		for (int i = 0; i < list.size(); i++) {
			HostAuthentication auth = list.get(i);
			AuthScope authScope = null;
			NTCredentials credentials = null;
			try {
				authScope = new AuthScope(auth.getHostName(), auth.getPort(),
						(auth.getRealm() == null || auth.getRealm().equals("")) ? AuthScope.ANY_REALM
								: auth.getRealm());
				credentials = new NTCredentials(auth.getUserName(), auth.getPassword(), InetAddress
						.getLocalHost().getCanonicalHostName(), auth.getHostName());
				client.getState().setCredentials(authScope, credentials);
			} catch (UnknownHostException e1) {
				log.error(e1.getMessage(), e1);
			}
		}
	}

	public void sendAndReceive(HttpMessage msg) throws IOException {
		sendAndReceive(msg, followRedirect);
	}

	/**
	 * Do not use this unless sure what is doing. This method works but proxy may skip the pipe
	 * without properly handle the filter.
	 * 
	 * Made this method private as it doesnt appear to be used anywhere...
	 * 
	 * @param msg
	 * @param pipe
	 * @param buf
	 * @throws HttpException
	 * @throws IOException
	 */
	/*
	 * private void sendAndReceive(HttpMessage msg, HttpOutputStream pipe, byte[] buf) throws
	 * HttpException, IOException { sendAndReceive(msg, followRedirect, pipe, buf);
	 * 
	 * }
	 */

	/**
	 * Send and receive a HttpMessage.
	 * 
	 * @param msg
	 * @param isFollowRedirect
	 * @throws HttpException
	 * @throws IOException
	 */
	public void sendAndReceive(HttpMessage msg, boolean isFollowRedirect) throws IOException {

		log.debug("sendAndReceive " + msg.getRequestHeader().getMethod() + " "
				+ msg.getRequestHeader().getURI() + " start");
		msg.setTimeSentMillis(System.currentTimeMillis());

		try {
			notifyRequestListeners(msg);
			if (!isFollowRedirect
					|| !(msg.getRequestHeader().getMethod().equalsIgnoreCase(HttpRequestHeader.POST) || msg
							.getRequestHeader().getMethod().equalsIgnoreCase(HttpRequestHeader.PUT))) {
				// ZAP: Reauthentication when sending a message from the perspective of a User
				sendAuthenticated(msg, isFollowRedirect);
				return;
			}

			// ZAP: Reauthentication when sending a message from the perspective of a User
			sendAuthenticated(msg, false);

			HttpMessage temp = msg.cloneAll();
			// POST/PUT method cannot be redirected by library. Need to follow by code

			// loop 1 time only because httpclient can handle redirect itself after first GET.
			for (int i = 0; i < 1
					&& (HttpStatusCode.isRedirection(temp.getResponseHeader().getStatusCode()) && temp
							.getResponseHeader().getStatusCode() != HttpStatusCode.NOT_MODIFIED); i++) {
				String location = temp.getResponseHeader().getHeader(HttpHeader.LOCATION);
				URI baseUri = temp.getRequestHeader().getURI();
				URI newLocation = new URI(baseUri, location, false);
				temp.getRequestHeader().setURI(newLocation);

				temp.getRequestHeader().setMethod(HttpRequestHeader.GET);
				temp.getRequestHeader().setContentLength(0);
				// ZAP: Reauthentication when sending a message from the perspective of a User
				sendAuthenticated(temp, true);
			}

			msg.setResponseHeader(temp.getResponseHeader());
			msg.setResponseBody(temp.getResponseBody());

		} finally {
			msg.setTimeElapsedMillis((int) (System.currentTimeMillis() - msg.getTimeSentMillis()));
			log.debug("sendAndReceive " + msg.getRequestHeader().getMethod() + " "
					+ msg.getRequestHeader().getURI() + " took " + msg.getTimeElapsedMillis());

			notifyResponseListeners(msg);
		}
	}

	private void notifyRequestListeners(HttpMessage msg) {
		for (HttpSenderListener listener : listeners) {
			try {
				listener.onHttpRequestSend(msg, initiator);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	private void notifyResponseListeners(HttpMessage msg) {
		for (HttpSenderListener listener : listeners) {
			try {
				listener.onHttpResponseReceive(msg, initiator);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	// ZAP: Make sure a message that needs to be authenticated is authenticated
	private void sendAuthenticated(HttpMessage msg, boolean isFollowRedirect) throws IOException {
		// Modify the request message if a 'Requesting User' has been set
		if (initiator!=AUTHENTICATION_INITIATOR && msg.getRequestingUser() != null)
			msg.getRequestingUser().processMessageToMatchUser(msg);

		// Send the message
		send(msg, isFollowRedirect);

		// If there's a 'Requesting User', make sure the response corresponds to an authenticated
		// session and, if not, attempt a reauthentication and try again
		if (initiator != AUTHENTICATION_INITIATOR && msg.getRequestingUser() != null) {
			if (msg.getResponseBody() != null && !msg.getRequestHeader().isImage()
					&& !msg.getRequestingUser().isAuthenticated(msg)) {
				log.debug("First try to send authenticated message failed for "
						+ msg.getRequestHeader().getURI() + ". Authenticating and trying again...");
				msg.getRequestingUser().queueAuthentication(msg);
				msg.getRequestingUser().processMessageToMatchUser(msg);
				send(msg, isFollowRedirect);
			}
		}
	}

	private void send(HttpMessage msg, boolean isFollowRedirect) throws IOException {
		HttpMethod method = null;
		HttpResponseHeader resHeader = null;

		try {
			method = runMethod(msg, isFollowRedirect);
			// successfully executed;
			resHeader = HttpMethodHelper.getHttpResponseHeader(method);
			resHeader.setHeader(HttpHeader.TRANSFER_ENCODING, null); // replaceAll("Transfer-Encoding: chunked\r\n",
																		// "");
			msg.setResponseHeader(resHeader);
			msg.getResponseBody().setCharset(resHeader.getCharset());
			msg.getResponseBody().setLength(0);

			// ZAP: Do not read response body for Server-Sent Events stream
			// ZAP: Moreover do not set content length to zero
			if (!msg.isEventStream()) {
				msg.getResponseBody().append(method.getResponseBody());
			}

			// ZAP: set method to retrieve upgraded channel later
			if (method instanceof ZapGetMethod) {
				msg.setUserObject(method);
			}
		} finally {
			if (method != null) {
				method.releaseConnection();
			}
		}
	}

	private HttpMethod runMethod(HttpMessage msg, boolean isFollowRedirect) throws IOException {
		HttpMethod method = null;
		// no more retry
		modifyUserAgent(msg);
		method = helper.createRequestMethod(msg.getRequestHeader(), msg.getRequestBody());
		if (!(method instanceof GenericMethod)) {
			// cant do this for Generic methods - it will fail
			method.setFollowRedirects(isFollowRedirect);
		}
		// ZAP: Use custom HttpState if needed
		if (msg.getRequestingUser() != null)
			this.executeMethod(method, msg.getRequestingUser().getUserHttpState());
		else
			this.executeMethod(method, null);
		// ZAP: If the state is enabled or there's a requesting user (so state is automatically
		// used), update the request message according to what the HttpState contains
		if ((allowState && param.isHttpStateEnabled()) || msg.getRequestingUser() != null) {
			HttpMethodHelper.updateHttpRequestHeaderSent(msg.getRequestHeader(), method);
		}
		return method;
	}

	public void setFollowRedirect(boolean followRedirect) {
		this.followRedirect = followRedirect;
	}

	private void modifyUserAgent(HttpMessage msg) {

		try {
			// no modification to user agent if empty
			if (userAgent.equals("") || msg.getRequestHeader().isEmpty()) {
				return;
			}

			// append new user agent to existing user agent
			String currentUserAgent = msg.getRequestHeader().getHeader(HttpHeader.USER_AGENT);
			if (currentUserAgent == null) {
				currentUserAgent = "";
			}

			if (currentUserAgent.indexOf(userAgent) >= 0) {
				// user agent already in place, exit
				return;
			}

			String delimiter = "";
			if (!currentUserAgent.equals("") && !currentUserAgent.endsWith(" ")) {
				delimiter = " ";
			}

			currentUserAgent = currentUserAgent + delimiter + userAgent;
			msg.getRequestHeader().setHeader(HttpHeader.USER_AGENT, currentUserAgent);
		} catch (Exception e) {
		}
	}

	/**
	 * @return Returns the userAgent.
	 */
	public static String getUserAgent() {
		return userAgent;
	}

	/**
	 * @param userAgent The userAgent to set.
	 */
	public static void setUserAgent(String userAgent) {
		HttpSender.userAgent = userAgent;
	}

	private void setCommonManagerParams(MultiThreadedHttpConnectionManager mgr) {
		// ZAP: set timeout
		mgr.getParams().setSoTimeout(this.param.getTimeoutInSecs() * 1000);
		mgr.getParams().setStaleCheckingEnabled(true);

		// Set to arbitrary large values to prevent locking
		mgr.getParams().setDefaultMaxConnectionsPerHost(10000);
		mgr.getParams().setMaxTotalConnections(200000);

		// to use for HttpClient 3.0.1
		// mgr.getParams().setDefaultMaxConnectionsPerHost((Constant.MAX_HOST_CONNECTION > 5) ? 15 :
		// 3*Constant.MAX_HOST_CONNECTION);

		// mgr.getParams().setMaxTotalConnections(mgr.getParams().getDefaultMaxConnectionsPerHost()*10);

		// mgr.getParams().setConnectionTimeout(60000); // use default

	}

	/*
	 * Send and receive a HttpMessage.
	 * 
	 * @param msg
	 * 
	 * @param isFollowRedirect
	 * 
	 * @throws HttpException
	 * 
	 * @throws IOException
	 */
	/*
	 * private void sendAndReceive(HttpMessage msg, boolean isFollowRedirect, HttpOutputStream pipe,
	 * byte[] buf) throws HttpException, IOException { log.debug("sendAndReceive " +
	 * msg.getRequestHeader().getMethod() + " " + msg.getRequestHeader().getURI() + " start");
	 * msg.setTimeSentMillis(System.currentTimeMillis());
	 * 
	 * try { if (!isFollowRedirect || !
	 * (msg.getRequestHeader().getMethod().equalsIgnoreCase(HttpRequestHeader.POST) ||
	 * msg.getRequestHeader().getMethod().equalsIgnoreCase(HttpRequestHeader.PUT)) ) { send(msg,
	 * isFollowRedirect, pipe, buf); return; } else { send(msg, false, pipe, buf); }
	 * 
	 * HttpMessage temp = msg.cloneAll(); // POST/PUT method cannot be redirected by library. Need
	 * to follow by code
	 * 
	 * // loop 1 time only because httpclient can handle redirect itself after first GET. for (int
	 * i=0; i<1 && (HttpStatusCode.isRedirection(temp.getResponseHeader().getStatusCode()) &&
	 * temp.getResponseHeader().getStatusCode() != HttpStatusCode.NOT_MODIFIED); i++) { String
	 * location = temp.getResponseHeader().getHeader(HttpHeader.LOCATION); URI baseUri =
	 * temp.getRequestHeader().getURI(); URI newLocation = new URI(baseUri, location, false);
	 * temp.getRequestHeader().setURI(newLocation);
	 * 
	 * temp.getRequestHeader().setMethod(HttpRequestHeader.GET);
	 * temp.getRequestHeader().setContentLength(0); send(temp, true, pipe, buf); }
	 * 
	 * msg.setResponseHeader(temp.getResponseHeader()); msg.setResponseBody(temp.getResponseBody());
	 * 
	 * } finally { msg.setTimeElapsedMillis((int)
	 * (System.currentTimeMillis()-msg.getTimeSentMillis())); log.debug("sendAndReceive " +
	 * msg.getRequestHeader().getMethod() + " " + msg.getRequestHeader().getURI() + " took " +
	 * msg.getTimeElapsedMillis()); } }
	 */

	/*
	 * Do not use this unless sure what is doing. This method works but proxy may skip the pipe
	 * without properly handle the filter.
	 * 
	 * @param msg
	 * 
	 * @param isFollowRedirect
	 * 
	 * @param pipe
	 * 
	 * @param buf
	 * 
	 * @throws HttpException
	 * 
	 * @throws IOException
	 */
	/*
	 * private void send(HttpMessage msg, boolean isFollowRedirect, HttpOutputStream pipe, byte[]
	 * buf) throws HttpException, IOException { HttpMethod method = null; HttpResponseHeader
	 * resHeader = null;
	 * 
	 * try { method = runMethod(msg, isFollowRedirect); // successfully executed; resHeader =
	 * HttpMethodHelper.getHttpResponseHeader(method);
	 * resHeader.setHeader(HttpHeader.TRANSFER_ENCODING, null); //
	 * replaceAll("Transfer-Encoding: chunked\r\n", ""); msg.setResponseHeader(resHeader);
	 * msg.getResponseBody().setCharset(resHeader.getCharset()); msg.getResponseBody().setLength(0);
	 * 
	 * // process response for each listner
	 * 
	 * pipe.write(msg.getResponseHeader()); pipe.flush();
	 * 
	 * if (msg.getResponseHeader().getContentLength() >= 0 &&
	 * msg.getResponseHeader().getContentLength() < 20480) { // save time expanding buffer in
	 * HttpBody if (msg.getResponseHeader().getContentLength() > 0) {
	 * msg.getResponseBody().setBody(method.getResponseBody()); pipe.write(msg.getResponseBody());
	 * pipe.flush();
	 * 
	 * } } else { //byte[] buf = new byte[4096]; InputStream in = method.getResponseBodyAsStream();
	 * 
	 * int len = 0; while (in != null && (len = in.read(buf)) > 0) { pipe.write(buf, 0, len);
	 * pipe.flush();
	 * 
	 * msg.getResponseBody().append(buf, len); } } } finally { if (method != null) {
	 * method.releaseConnection(); } } }
	 */

	public static void addListener(HttpSenderListener listener) {
		listeners.add(listener);
		Collections.sort(listeners, getListenersComparator());
	}

	private static Comparator<HttpSenderListener> getListenersComparator() {
		if (listenersComparator == null) {
			createListenersComparator();
		}

		return listenersComparator;
	}

	private static synchronized void createListenersComparator() {
		if (listenersComparator == null) {
			listenersComparator = new Comparator<HttpSenderListener>() {

				@Override
				public int compare(HttpSenderListener o1, HttpSenderListener o2) {
					int order1 = o1.getListenerOrder();
					int order2 = o2.getListenerOrder();

					if (order1 < order2) {
						return -1;
					} else if (order1 > order2) {
						return 1;
					}

					return 0;
				}
			};
		}
	}
}
