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
// ZAP: 2013/09/26 Issue 716: ZAP flags its own HTTP responses
// ZAP: 2013/09/26 Issue 656: Content-length: 0 in GET requests
// ZAP: 2013/09/29 Deprecating configuring HTTP Authentication through Options
// ZAP: 2013/11/16 Issue 837: Update, always, the HTTP request sent/forward by ZAP's proxy
// ZAP: 2013/12/11 Corrected log.info calls to use debug
// ZAP: 2014/03/04 Issue 1043: Custom active scan dialog
// ZAP: 2014/03/23 Issue 412: Enable unsafe SSL/TLS renegotiation option not saved
// ZAP: 2014/03/23 Issue 416: Normalise how multiple related options are managed throughout ZAP
// and enhance the usability of some options
// ZAP: 2014/03/29 Issue 1132: HttpSender ignores the "Send single cookie request header" option
// ZAP: 2014/08/14 Issue 1291: 407 Proxy Authentication Required while active scanning
// ZAP: 2014/10/25 Issue 1062: Added a getter for the HttpClient.
// ZAP: 2014/10/28 Issue 1390: Force https on cfu call
// ZAP: 2014/11/25 Issue 1411: Changed getUser() visibility
// ZAP: 2014/12/11 Added JavaDoc to constructor and removed the instance variable allowState.
// ZAP: 2015/04/09 Allow to specify the maximum number of retries on I/O error.
// ZAP: 2015/04/09 Allow to specify the maximum number of redirects.
// ZAP: 2015/04/09 Allow to specify if circular redirects are allowed.
// ZAP: 2015/06/12 Issue 1459: Add an HTTP sender listener script
// ZAP: 2016/05/24 Issue 2463: Websocket not proxied when outgoing proxy is set
// ZAP: 2016/05/27 Issue 2484: Circular Redirects
// ZAP: 2016/06/08 Set User-Agent header defined in options as default for (internal) CONNECT requests
// ZAP: 2016/06/10 Allow to validate the URI of the redirections before being followed
// ZAP: 2016/08/04 Added removeListener(..)

package org.parosproxy.paros.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpHost;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodDirector;
import org.apache.commons.httpclient.HttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.InvalidRedirectLocationException;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.ProxyHost;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.log4j.Logger;
import org.zaproxy.zap.ZapGetMethod;
import org.zaproxy.zap.ZapHttpConnectionManager;
import org.zaproxy.zap.network.HttpSenderListener;
import org.zaproxy.zap.network.ZapNTLMScheme;
import org.zaproxy.zap.users.User;

public class HttpSender {
	public static final int PROXY_INITIATOR = 1;
	public static final int ACTIVE_SCANNER_INITIATOR = 2;
	public static final int SPIDER_INITIATOR = 3;
	public static final int FUZZER_INITIATOR = 4;
	public static final int AUTHENTICATION_INITIATOR = 5;
	public static final int MANUAL_REQUEST_INITIATOR = 6;
	public static final int CHECK_FOR_UPDATES_INITIATOR = 7;
	public static final int BEAN_SHELL_INITIATOR = 8;
	public static final int ACCESS_CONTROL_SCANNER_INITIATOR = 9;

	private static Logger log = Logger.getLogger(HttpSender.class);

	private static ProtocolSocketFactory sslFactory = null;
	private static Protocol protocol = null;

	private static List<HttpSenderListener> listeners = new ArrayList<>();
	private static Comparator<HttpSenderListener> listenersComparator = null;;

	private User user = null;

	static {
		try {
			protocol = Protocol.getProtocol("https");
			sslFactory = protocol.getSocketFactory();
		} catch (Exception e) {
		}
		// avoid init again if already initialized
		if (sslFactory == null || !(sslFactory instanceof SSLConnector)) {
			Protocol.registerProtocol("https", new Protocol("https",
					(ProtocolSocketFactory) new SSLConnector(true), 443));
		}

		AuthPolicy.registerAuthScheme(AuthPolicy.NTLM, ZapNTLMScheme.class);
	}

	private static HttpMethodHelper helper = new HttpMethodHelper();
	private static String userAgent = "";
	private static final ThreadLocal<Boolean> IN_LISTENER = new ThreadLocal<Boolean>();

	private HttpClient client = null;
	private HttpClient clientViaProxy = null;
	private ConnectionParam param = null;
	private MultiThreadedHttpConnectionManager httpConnManager = null;
	private MultiThreadedHttpConnectionManager httpConnManagerProxy = null;
	private boolean followRedirect = false;
	private int initiator = -1;

	/*
	 * public HttpSender(ConnectionParam connectionParam, boolean allowState) { this
	 * (connectionParam, allowState, -1); }
	 */

	/**
	 * Constructs an {@code HttpSender}.
	 * <p>
	 * If {@code useGlobalState} is {@code true} the HttpSender will use the HTTP state given by
	 * {@code ConnectionParam#getHttpState()} iff {@code ConnectionParam#isHttpStateEnabled()} returns {@code true} otherwise it
	 * doesn't have any state (i.e. cookies are disabled). If {@code useGlobalState} is {@code false} it uses a non shared HTTP
	 * state. The actual state used is overridden, per message, when {@code HttpMessage#getRequestingUser()} returns non
	 * {@code null}.
	 * <p>
	 * The {@code initiator} is used to indicate the component that is sending the messages when the {@code HttpSenderListener}s
	 * are notified of messages sent and received.
	 *
	 * @param connectionParam the parameters used to setup the connections to target hosts
	 * @param useGlobalState {@code true} if the messages sent/received should use the global HTTP state, {@code false} if
	 *			should use a non shared HTTP state
	 * @param initiator the ID of the initiator of the HTTP messages sent
	 * @see ConnectionParam#getHttpState()
	 * @see HttpSenderListener
	 * @see HttpMessage#getRequestingUser()
	 */
	public HttpSender(ConnectionParam connectionParam, boolean useGlobalState, int initiator) {
		this.param = connectionParam;
		this.initiator = initiator;

		client = createHttpClient();
		clientViaProxy = createHttpClientViaProxy();
		setAllowCircularRedirects(true);
		
		// Set how cookie headers are sent no matter of the "allowState", in case a state is forced by
		// other extensions (e.g. Authentication)
		final boolean singleCookieRequestHeader = param.isSingleCookieRequestHeader();
		client.getParams().setBooleanParameter(HttpMethodParams.SINGLE_COOKIE_HEADER,
				singleCookieRequestHeader);
		clientViaProxy.getParams().setBooleanParameter(HttpMethodParams.SINGLE_COOKIE_HEADER,
				singleCookieRequestHeader);
		String defaultUserAgent = param.getDefaultUserAgent();
		client.getParams().setParameter(HttpMethodDirector.PARAM_DEFAULT_USER_AGENT_CONNECT_REQUESTS, defaultUserAgent);
		clientViaProxy.getParams().setParameter(HttpMethodDirector.PARAM_DEFAULT_USER_AGENT_CONNECT_REQUESTS, defaultUserAgent);

		if (useGlobalState) {
			checkState();
		}
	}

	public static SSLConnector getSSLConnector() {
		return (SSLConnector) protocol.getSocketFactory();
	}

	private void checkState() {
		if (param.isHttpStateEnabled()) {
			client.setState(param.getHttpState());
			clientViaProxy.setState(param.getHttpState());
			client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
			clientViaProxy.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
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

		if (!param.isUseProxyChain()) {
			return createHttpClient();
		}

		httpConnManagerProxy = new MultiThreadedHttpConnectionManager();
		setCommonManagerParams(httpConnManagerProxy);
		HttpClient clientProxy = new HttpClient(httpConnManagerProxy);
		clientProxy.getHostConfiguration().setProxy(param.getProxyChainName(), param.getProxyChainPort());

		if (param.isUseProxyChainAuth()) {
			clientProxy.getState().setProxyCredentials(getAuthScope(param), getNTCredentials(param));
		}

		return clientProxy;
	}
	
	private NTCredentials getNTCredentials(ConnectionParam param) {
		// NTCredentials credentials = new NTCredentials(
		// param.getProxyChainUserName(), param.getProxyChainPassword(),
		// param.getProxyChainName(), param.getProxyChainName());
		return  new NTCredentials(param.getProxyChainUserName(),
				param.getProxyChainPassword(), "", param.getProxyChainRealm().equals("") ? ""
						: param.getProxyChainRealm());
	}

	private AuthScope getAuthScope(ConnectionParam param) {
		// Below is the original code, but user reported that above code works.
		// UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
		// param.getProxyChainUserName(), param.getProxyChainPassword());
		return new AuthScope(param.getProxyChainName(), param.getProxyChainPort(), param
				.getProxyChainRealm().equals("") ? AuthScope.ANY_REALM : param.getProxyChainRealm());
	}

	public int executeMethod(HttpMethod method, HttpState state) throws IOException {
		int responseCode = -1;

		String hostName;
		hostName = method.getURI().getHost();
		method.setDoAuthentication(true);
        HostConfiguration hc = null;

		HttpClient requestClient;
		if (isConnectionUpgrade(method)) {
			requestClient = new HttpClient(new ZapHttpConnectionManager());
			if (param.isUseProxy(hostName)) {
				requestClient.getHostConfiguration().setProxy(param.getProxyChainName(), param.getProxyChainPort());
				if (param.isUseProxyChainAuth()) {
					requestClient.getState().setProxyCredentials(getAuthScope(param), getNTCredentials(param));
				}
			}
		} else if (param.isUseProxy(hostName)) {
			requestClient = clientViaProxy;
		} else {
			requestClient = client;
		}

		if (this.initiator == CHECK_FOR_UPDATES_INITIATOR) {
			// Use the 'strict' SSLConnector, ie one that performs all the usual cert checks
			// The 'standard' one 'trusts' everything
			// This is to ensure that all 'check-for update' calls are made to the expected https urls
			// without this is would be possible to intercept and change the response which could result
			// in the user downloading and installing a malicious add-on
			hc = new HostConfiguration() {
	            @Override
	            public synchronized void setHost(URI uri) {
	                try {
	                    setHost(new HttpHost(uri.getHost(), uri.getPort(), getProtocol()));
	                } catch (URIException e) {
	                    throw new IllegalArgumentException(e.toString());
	                }
	            };
	        };
	        
	        hc.setHost(hostName, method.getURI().getPort(), new Protocol(
	                "https", (ProtocolSocketFactory) new SSLConnector(false), 443));
			if (param.isUseProxy(hostName)) {
				hc.setProxyHost(new ProxyHost(param.getProxyChainName(), param.getProxyChainPort()));
				if (param.isUseProxyChainAuth()) {
					requestClient.getState().setProxyCredentials(getAuthScope(param), getNTCredentials(param));
				}
			}
		}

		// ZAP: Check if a custom state is being used
		if (state != null) {
			// Make sure cookies are enabled
			method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
		}
		responseCode = requestClient.executeMethod(hc, method, state);

		return responseCode;
	}

	/**
	 * Tells whether or not the given {@code method} has a {@code Connection} request header with {@code Upgrade} value.
	 *
	 * @param method the method that will be checked
	 * @return {@code true} if the {@code method} has a connection upgrade, {@code false} otherwise
	 */
	private static boolean isConnectionUpgrade(HttpMethod method) {
		Header connectionHeader = method.getRequestHeader("connection");
		if (connectionHeader == null) {
			return false;
		}
		return connectionHeader.getValue().toLowerCase().contains("upgrade");
	}

	public void shutdown() {
		if (httpConnManager != null) {
			httpConnManager.shutdown();
		}
		if (httpConnManagerProxy != null) {
			httpConnManagerProxy.shutdown();
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
	 * @see #sendAndReceive(HttpMessage, RedirectionValidator)
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
				temp.getRequestHeader().setHeader(HttpHeader.CONTENT_LENGTH, null);
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
		if (IN_LISTENER.get() != null) {
			// This is a request from one of the listeners - prevent infinite recursion 
			return;
		}
		try {
			IN_LISTENER.set(true);
			for (HttpSenderListener listener : listeners) {
				try {
					listener.onHttpRequestSend(msg, initiator, this);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		} finally {
			IN_LISTENER.set(null);
		}
	}

	private void notifyResponseListeners(HttpMessage msg) {
		if (IN_LISTENER.get() != null) {
			// This is a request from one of the listeners - prevent infinite recursion 
			return;
		}
		try {
			IN_LISTENER.set(true);
			for (HttpSenderListener listener : listeners) {
				try {
					listener.onHttpResponseReceive(msg, initiator, this);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		} finally {
			IN_LISTENER.set(null);
		}
	}
	
	public User getUser (HttpMessage msg) {
		if (this.user != null) {
			// If its set for the sender it overrides the message
			return user;
		}
		return msg.getRequestingUser();
	}

	// ZAP: Make sure a message that needs to be authenticated is authenticated
	private void sendAuthenticated(HttpMessage msg, boolean isFollowRedirect) throws IOException {
		// Modify the request message if a 'Requesting User' has been set
		User forceUser = this.getUser(msg);
		if (initiator != AUTHENTICATION_INITIATOR && forceUser != null)
			forceUser.processMessageToMatchUser(msg);

		log.debug("Sending message to: " + msg.getRequestHeader().getURI().toString());
		// Send the message
		send(msg, isFollowRedirect);

		// If there's a 'Requesting User', make sure the response corresponds to an authenticated
		// session and, if not, attempt a reauthentication and try again
		if (initiator != AUTHENTICATION_INITIATOR && forceUser != null
				&& !msg.getRequestHeader().isImage()
				&& !forceUser.isAuthenticated(msg)) {
			log.debug("First try to send authenticated message failed for " + msg.getRequestHeader().getURI()
					+ ". Authenticating and trying again...");
			forceUser.queueAuthentication(msg);
			forceUser.processMessageToMatchUser(msg);
			send(msg, isFollowRedirect);
		} else
			log.debug("SUCCESSFUL");

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
			msg.setResponseFromTargetHost(true);

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
		if (!(method instanceof EntityEnclosingMethod)) {
			// cant do this for EntityEnclosingMethod methods - it will fail
			method.setFollowRedirects(isFollowRedirect);
		}
		// ZAP: Use custom HttpState if needed
		User forceUser = this.getUser(msg);
		if (forceUser != null) {
			this.executeMethod(method, forceUser.getCorrespondingHttpState());
		} else {
			this.executeMethod(method, null);
		}

		HttpMethodHelper.updateHttpRequestHeaderSent(msg.getRequestHeader(), method);

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

	public static void removeListener(HttpSenderListener listener) {
		listeners.remove(listener);
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

	/**
	 * Set the user to scan as. If null then the current session will be used.
	 * @param user
	 */
	public void setUser(User user) {
		this.user = user;
	}
	
	// ZAP: Added a getter for the client.
	public HttpClient getClient() {
		return this.client;
	}

    /**
     * Sets whether or not the authentication headers ("Authorization" and "Proxy-Authorization") already present in the request
     * should be removed if received an authentication challenge (status codes 401 and 407).
     * <p>
     * If {@code true} new authentication headers will be generated and the old ones removed otherwise the authentication
     * headers already present in the request will be used to authenticate.
     * <p>
     * Default is {@code false}, i.e. use the headers already present in the request header.
     * <p>
     * Processes that reuse messages previously sent should consider setting this to {@code true}, otherwise new authentication
     * challenges might fail.
     *
     * @param removeHeaders {@code true} if the the authentication headers already present should be removed when challenged,
     *            {@code false} otherwise
     */
    public void setRemoveUserDefinedAuthHeaders(boolean removeHeaders) {
        client.getParams().setBooleanParameter(HttpMethodDirector.PARAM_REMOVE_USER_DEFINED_AUTH_HEADERS, removeHeaders);
        clientViaProxy.getParams().setBooleanParameter(HttpMethodDirector.PARAM_REMOVE_USER_DEFINED_AUTH_HEADERS, removeHeaders);
    }

    /**
     * Sets the maximum number of retries of an unsuccessful request caused by I/O errors.
     * <p>
     * The default number of retries is 3.
     *
     * @param retries the number of retries
     * @throws IllegalArgumentException if {@code retries} is negative.
     * @since 2.4.0
     */
    public void setMaxRetriesOnIOError(int retries) {
        if (retries < 0) {
            throw new IllegalArgumentException("Parameter retries must be greater or equal to zero.");
        }

        HttpMethodRetryHandler retryHandler = new DefaultHttpMethodRetryHandler(retries, false);
        client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, retryHandler);
        clientViaProxy.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, retryHandler);
    }

    /**
     * Sets the maximum number of redirects that will be followed before failing with an exception.
     * <p>
     * The default maximum number of redirects is 100.
     *
     * @param maxRedirects the maximum number of redirects
     * @throws IllegalArgumentException if {@code maxRedirects} is negative.
     * @since 2.4.0
     */
    public void setMaxRedirects(int maxRedirects) {
        if (maxRedirects < 0) {
            throw new IllegalArgumentException("Parameter maxRedirects must be greater or equal to zero.");
        }
        client.getParams().setIntParameter(HttpClientParams.MAX_REDIRECTS, maxRedirects);
        clientViaProxy.getParams().setIntParameter(HttpClientParams.MAX_REDIRECTS, maxRedirects);
    }

    /**
     * Sets whether or not circular redirects are allowed.
     * <p>
     * Circular redirects happen when a request redirects to itself, or when a same request was already accessed in a chain of
     * redirects.
     * <p>
     * Since 2.5.0, the default is to allow circular redirects.
     *
     * @param allow {@code true} if circular redirects should be allowed, {@code false} otherwise
     * @since 2.4.0
     */
    public void setAllowCircularRedirects(boolean allow) {
        client.getParams().setBooleanParameter(HttpClientParams.ALLOW_CIRCULAR_REDIRECTS, allow);
        clientViaProxy.getParams().setBooleanParameter(HttpClientParams.ALLOW_CIRCULAR_REDIRECTS, allow);
    }

    /**
     * Sends the request of given HTTP {@code message}, following redirections per rules defined by the given {@code validator}.
     * After the call to this method the given {@code message} will have the contents of the last response received (possibly
     * the response of a redirection).
     * <p>
     * The validator is notified of each message sent and received (first message and redirections followed, if any).
     *
     * @param message the message that will be sent
     * @param validator the validator responsible for validation of redirections
     * @throws IllegalArgumentException if any of the parameters is {@code null}
     * @throws IOException if an error occurred while sending the message or following the redirections
     * @since TODO add version
     * @see #sendAndReceive(HttpMessage, boolean)
     */
    public void sendAndReceive(HttpMessage message, RedirectionValidator validator) throws IOException {
        if (message == null) {
            throw new IllegalArgumentException("Parameter message must not be null.");
        }
        if (validator == null) {
            throw new IllegalArgumentException("Parameter validator must not be null.");
        }

        sendAndReceive(message, false);
        validator.notifyMessageReceived(message);

        followRedirections(message, validator);
    }

    /**
     * Follows redirections using the response of the given {@code message}. The given {@code validator} will be called for each
     * redirection received. After the call to this method the given {@code message} will have the contents of the last response
     * received (possibly the response of a redirection).
     * <p>
     * The validator is notified of each message sent and received (first message and redirections followed, if any).
     *
     * @param message the message that will be sent, must not be {@code null}
     * @param validator the validator responsible for validation of redirections, must not be {@code null}
     * @throws IOException if an error occurred while sending the message or following the redirections
     * @see #isRedirectionNeeded(int)
     */
    private void followRedirections(HttpMessage message, RedirectionValidator validator) throws IOException {
        HttpMessage redirectMessage = message;
        int maxRedirections = client.getParams().getIntParameter(HttpClientParams.MAX_REDIRECTS, 100);
        for (int i = 0; i < maxRedirections && isRedirectionNeeded(redirectMessage.getResponseHeader().getStatusCode()); i++) {
            URI newLocation = extractRedirectLocation(redirectMessage);
            if (newLocation == null || !validator.isValid(newLocation)) {
                return;
            }

            redirectMessage = redirectMessage.cloneAll();
            redirectMessage.getRequestHeader().setURI(newLocation);

            if (isRequestRewriteNeeded(redirectMessage.getResponseHeader().getStatusCode())) {
                redirectMessage.getRequestHeader().setMethod(HttpRequestHeader.GET);
                redirectMessage.getRequestHeader().setHeader(HttpHeader.CONTENT_TYPE, null);
                redirectMessage.getRequestHeader().setHeader(HttpHeader.CONTENT_LENGTH, null);
                redirectMessage.setRequestBody("");
            }

            sendAndReceive(redirectMessage, false);
            validator.notifyMessageReceived(redirectMessage);

            // Update the response of the (original) message
            message.setResponseHeader(redirectMessage.getResponseHeader());
            message.setResponseBody(redirectMessage.getResponseBody());
        }
    }

    /**
     * Tells whether or not a redirection is needed based on the given status code.
     * <p>
     * A redirection is needed if the status code is 301, 302, 303, 307 or 308.
     *
     * @param statusCode the status code that will be checked
     * @return {@code true} if a redirection is needed, {@code false} otherwise
     * @see #isRequestRewriteNeeded(int)
     */
    private static boolean isRedirectionNeeded(int statusCode) {
        switch (statusCode) {
        case 301:
        case 302:
        case 303:
        case 307:
        case 308:
            return true;
        default:
            return false;
        }
    }

    /**
     * Tells whether or not the (original) request of the redirection with the given status code, should be rewritten.
     * <p>
     * For status codes 301, 302 and 303 the request should be changed from POST to GET when following redirections (mimicking
     * the behaviour of browsers, which per <a href="https://tools.ietf.org/html/rfc7231#section-6.4">RFC 7231, Section 6.4</a>
     * is now OK).
     *
     * @param statusCode the status code that will be checked
     * @return {@code true} if the request should be rewritten, {@code false} otherwise
     * @see #isRedirectionNeeded(int)
     */
    private static boolean isRequestRewriteNeeded(int statusCode) {
        return statusCode == 301 || statusCode == 302 || statusCode == 303;
    }

    /**
     * Extracts a {@code URI} from the {@code Location} header of the given HTTP {@code message}.
     * <p>
     * If there's no {@code Location} header this method returns {@code null}.
     * 
     * @param message the HTTP message that will processed
     * @return the {@code URI} created from the value of the {@code Location} header, might be {@code null}
     * @throws InvalidRedirectLocationException if the value of {@code Location} header is not a valid {@code URI}
     */
    private static URI extractRedirectLocation(HttpMessage message) throws InvalidRedirectLocationException {
        String location = message.getResponseHeader().getHeader(HttpHeader.LOCATION);
        if (location == null) {
            if (log.isDebugEnabled()) {
                log.debug("No Location header found: " + message.getResponseHeader());
            }
            return null;
        }

        try {
            return new URI(message.getRequestHeader().getURI(), location, true);
        } catch (URIException ex) {
            throw new InvalidRedirectLocationException("Invalid redirect location: " + location, location, ex);
        }
    }

    /**
     * A validator of redirections.
     * <p>
     * As convenience the validator will also be notified of the HTTP messages sent and received (first message and followed
     * redirections, if any).
     * 
     * @since TODO add version
     */
    public interface RedirectionValidator {

        /**
         * Tells whether or not the given {@code redirection} is valid, to be followed.
         *
         * @param redirection the redirection being checked, never {@code null}
         * @return {@code true} if the redirection is valid, {@code false} otherwise
         */
        boolean isValid(URI redirection);

        /**
         * Notifies that a new message was sent and received (called for the first message and followed redirections, if any).
         *
         * @param message the HTTP message that was received, never {@code null}
         */
        void notifyMessageReceived(HttpMessage message);
    }
}
