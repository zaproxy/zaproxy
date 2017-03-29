/*
 * HeadURL: https://svn.apache.org/repos/asf/httpcomponents/oac.hc3x/trunk/src/java/org/apache/commons/httpclient/HttpMethodDirector.java 
 * Revision: 915934 
 * Date: 2010-02-24 19:17:53 +0000 (Wed, 24 Feb 2010) 
 *
 * ====================================================================
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.commons.httpclient;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.httpclient.auth.AuthChallengeException;
import org.apache.commons.httpclient.auth.AuthChallengeParser;
import org.apache.commons.httpclient.auth.AuthChallengeProcessor;
import org.apache.commons.httpclient.auth.AuthScheme;
import org.apache.commons.httpclient.auth.AuthState;
import org.apache.commons.httpclient.auth.AuthenticationException;
import org.apache.commons.httpclient.auth.CredentialsProvider;
import org.apache.commons.httpclient.auth.CredentialsNotAvailableException;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.auth.MalformedChallengeException;
import org.apache.commons.httpclient.params.HostParams;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.params.HttpParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 * Forked class...
 * 
 * It was forked because ZAP depends (and uses) Commons HttpClient which is not compatible with, the newer version, 
 * HttpComponents Client.
 * 
 * Changes:
 *  - Removed the characters "$" from the previous SVN keywords (HeadURL, Revision and Date) to avoid accidental expansions;
 *  - Allow to remove user defined authentication headers (Issue 1291), in the methods #authenticateHost(HttpMethod) and 
 *  #authenticateProxy(HttpMethod). The (user defined) authentications headers need to be removed (and thus force the 
 *  authentication) because some messages previously sent that already contain authentication headers (which might no longer be
 *  valid) are reused/resent in some ZAP components (e.g. active scanner, fuzzer, ...);
 *  - Added constant PARAM_REMOVE_USER_DEFINED_AUTH_HEADERS;
 *  - Added the public modifier to the class.
 *  - Establish a tunnel if the request has a connection upgrade.
 */
/**
 * Handles the process of executing a method including authentication, redirection and retries.
 * 
 * @since 3.0
 */
@SuppressWarnings("deprecation")
public class HttpMethodDirector {

    /**
     * Parameter to remove user defined authentication headers.
     */
    public static final String PARAM_REMOVE_USER_DEFINED_AUTH_HEADERS = "remove.user.defined.auth.headers";

    /**
     * Parameter to set/obtain the default {@code User-Agent} of internal CONNECT requests (if {@code null} no
     * {@code User-Agent} is set).
     */
    public static final String PARAM_DEFAULT_USER_AGENT_CONNECT_REQUESTS = "method.connect.default.user.agent";

    /** The www authenticate challange header. */
    public static final String WWW_AUTH_CHALLENGE = "WWW-Authenticate";

    /** The www authenticate response header. */
    public static final String WWW_AUTH_RESP = "Authorization";

    /** The proxy authenticate challange header. */
    public static final String PROXY_AUTH_CHALLENGE = "Proxy-Authenticate";

    /** The proxy authenticate response header. */
    public static final String PROXY_AUTH_RESP = "Proxy-Authorization";

    private static final Log LOG = LogFactory.getLog(HttpMethodDirector.class);

    private ConnectMethod connectMethod;
    
    private HttpState state;
	
    private HostConfiguration hostConfiguration;
    
    private HttpConnectionManager connectionManager;
    
    private HttpClientParams params;
    
    private HttpConnection conn;
    
    /** A flag to indicate if the connection should be released after the method is executed. */
    private boolean releaseConnection = false;

    /** Authentication processor */
    private AuthChallengeProcessor authProcessor = null;

    private Set<URI> redirectLocations = null; 
    
    public HttpMethodDirector(
        final HttpConnectionManager connectionManager,
        final HostConfiguration hostConfiguration,
        final HttpClientParams params,
        final HttpState state
    ) {
        super();
        this.connectionManager = connectionManager;
        this.hostConfiguration = hostConfiguration;
        this.params = params;
        this.state = state;
        this.authProcessor = new AuthChallengeProcessor(this.params);
    }
    
	
    /**
     * Executes the method associated with this method director.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void executeMethod(final HttpMethod method) throws IOException, HttpException {
        if (method == null) {
            throw new IllegalArgumentException("Method may not be null");
        }
        // Link all parameter collections to form the hierarchy:
        // Global -> HttpClient -> HostConfiguration -> HttpMethod
        this.hostConfiguration.getParams().setDefaults(this.params);
        method.getParams().setDefaults(this.hostConfiguration.getParams());
        
        // Generate default request headers
        Collection<?> defaults = (Collection<?>)this.hostConfiguration.getParams().
			getParameter(HostParams.DEFAULT_HEADERS);
        if (defaults != null) {
        	Iterator<?> i = defaults.iterator();
        	while (i.hasNext()) {
        		method.addRequestHeader((Header)i.next());
        	}
        }
        
        try {
            int maxRedirects = this.params.getIntParameter(HttpClientParams.MAX_REDIRECTS, 100);

            for (int redirectCount = 0;;) {

                // make sure the connection we have is appropriate
                if (this.conn != null && !hostConfiguration.hostEquals(this.conn)) {
                    this.conn.setLocked(false);
                    this.conn.releaseConnection();
                    this.conn = null;
                }
        
                // get a connection, if we need one
                if (this.conn == null) {
                    this.conn = connectionManager.getConnectionWithTimeout(
                        hostConfiguration,
                        this.params.getConnectionManagerTimeout() 
                    );
                    this.conn.setLocked(true);
                    if (this.params.isAuthenticationPreemptive()
                     || this.state.isAuthenticationPreemptive()) 
                    {
                        LOG.debug("Preemptively sending default basic credentials");
                        method.getHostAuthState().setPreemptive();
                        method.getHostAuthState().setAuthAttempted(true);
                        if (this.conn.isProxied() && !this.conn.isSecure()) {
                            method.getProxyAuthState().setPreemptive();
                            method.getProxyAuthState().setAuthAttempted(true);
                        }
                    }
                }
                authenticate(method);
                executeWithRetry(method);
                if (this.connectMethod != null) {
                    fakeResponse(method);
                    break;
                }
                
                boolean retry = false;
                if (isRedirectNeeded(method)) {
                    if (processRedirectResponse(method)) {
                        retry = true;
                        ++redirectCount;
                        if (redirectCount >= maxRedirects) {
                            LOG.error("Narrowly avoided an infinite loop in execute");
                            throw new RedirectException("Maximum redirects ("
                                + maxRedirects + ") exceeded");
                        }
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Execute redirect " + redirectCount + " of " + maxRedirects);
                        }
                    }
                }
                if (isAuthenticationNeeded(method)) {
                    if (processAuthenticationResponse(method)) {
                        LOG.debug("Retry authentication");
                        retry = true;
                    }
                }
                if (!retry) {
                    break;
                }
                // retry - close previous stream.  Caution - this causes
                // responseBodyConsumed to be called, which may also close the
                // connection.
                if (method.getResponseBodyAsStream() != null) {
                    method.getResponseBodyAsStream().close();
                }

            } //end of retry loop
        } finally {
            if (this.conn != null) {
                this.conn.setLocked(false);
            }
            // If the response has been fully processed, return the connection
            // to the pool.  Use this flag, rather than other tests (like
            // responseStream == null), as subclasses, might reset the stream,
            // for example, reading the entire response into a file and then
            // setting the file as the stream.
            if (
                (releaseConnection || method.getResponseBodyAsStream() == null) 
                && this.conn != null
            ) {
                this.conn.releaseConnection();
            }
        }

    }

    
    private void authenticate(final HttpMethod method) {
        try {
            if (this.conn.isProxied() && !this.conn.isSecure()) {
                authenticateProxy(method);
            }
            authenticateHost(method);
        } catch (AuthenticationException e) {
            LOG.error(e.getMessage(), e);
        }
    }


    private boolean cleanAuthHeaders(final HttpMethod method, final String name) {
        Header[] authheaders = method.getRequestHeaders(name);
        boolean clean = true;
        for (int i = 0; i < authheaders.length; i++) {
            Header authheader = authheaders[i];
            if (authheader.isAutogenerated()) {
                method.removeRequestHeader(authheader);
            } else {
                clean = false;
            }
        }
        return clean;
    }
    

    private void authenticateHost(final HttpMethod method) throws AuthenticationException {
        // Clean up existing authentication headers
        boolean userDefinedAuthenticationHeaders = !cleanAuthHeaders(method, WWW_AUTH_RESP);
        if (userDefinedAuthenticationHeaders) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("User defined '" + WWW_AUTH_RESP + "' headers present in the request.");
            }
        }
        AuthState authstate = method.getHostAuthState();
        AuthScheme authscheme = authstate.getAuthScheme();
        if (authscheme == null) {
            return;
        }
        if (authstate.isAuthRequested() || !authscheme.isConnectionBased()) {
            String host = method.getParams().getVirtualHost();
            if (host == null) {
                host = conn.getHost();
            }
            int port = conn.getPort();
            AuthScope authscope = new AuthScope(
                host, port, 
                authscheme.getRealm(), 
                authscheme.getSchemeName());  
            if (LOG.isDebugEnabled()) {
                LOG.debug("Authenticating with " + authscope);
            }
            Credentials credentials = this.state.getCredentials(authscope);
            if (credentials != null) {
                if (userDefinedAuthenticationHeaders) {
                    if (!method.getParams().getBooleanParameter(PARAM_REMOVE_USER_DEFINED_AUTH_HEADERS, false)) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Ignoring authentication, user defined '" + WWW_AUTH_RESP
                                    + "' headers present in the request.");
                        } 
                        return;
                    }
                    method.removeRequestHeader(WWW_AUTH_RESP);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Removed user defined '" + WWW_AUTH_RESP + "' headers.");
                    }
                }

                String authstring = authscheme.authenticate(credentials, method);
                if (authstring != null) {
                    method.addRequestHeader(new Header(WWW_AUTH_RESP, authstring, true));
                }
            } else {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Required credentials not available for " + authscope);
                    if (method.getHostAuthState().isPreemptive()) {
                        LOG.warn("Preemptive authentication requested but no default " +
                            "credentials available"); 
                    }
                }
            }
        }
    }


    private void authenticateProxy(final HttpMethod method) throws AuthenticationException {
        // Clean up existing authentication headers
        boolean userDefinedAuthenticationHeaders = !cleanAuthHeaders(method, PROXY_AUTH_RESP);
        if (userDefinedAuthenticationHeaders) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("User defined '" + PROXY_AUTH_RESP + "' headers present in the request.");
            }
        }
        AuthState authstate = method.getProxyAuthState();
        AuthScheme authscheme = authstate.getAuthScheme();
        if (authscheme == null) {
            return;
        }
        if (authstate.isAuthRequested() || !authscheme.isConnectionBased()) {
            AuthScope authscope = new AuthScope(
                conn.getProxyHost(), conn.getProxyPort(), 
                authscheme.getRealm(), 
                authscheme.getSchemeName());  
            if (LOG.isDebugEnabled()) {
                LOG.debug("Authenticating with " + authscope);
            }
            Credentials credentials = this.state.getProxyCredentials(authscope);
            if (credentials != null) {
                if (userDefinedAuthenticationHeaders) {
                    if (!method.getParams().getBooleanParameter(PARAM_REMOVE_USER_DEFINED_AUTH_HEADERS, false)) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Ignoring proxy authentication, user defined '" + PROXY_AUTH_RESP
                                    + "' headers present in the request.");
                        }
                        return;
                    }
                    method.removeRequestHeader(PROXY_AUTH_RESP);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Removed user defined '" + PROXY_AUTH_RESP + "' headers.");
                    }
                }

                String authstring = authscheme.authenticate(credentials, method);
                if (authstring != null) {
                    method.addRequestHeader(new Header(PROXY_AUTH_RESP, authstring, true));
                }
            } else {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Required proxy credentials not available for " + authscope);
                    if (method.getProxyAuthState().isPreemptive()) {
                        LOG.warn("Preemptive authentication requested but no default " +
                            "proxy credentials available"); 
                    }
                }
            }
        }
    }
    
    
    /**
     * Applies connection parameters specified for a given method
     * 
     * @param method HTTP method
     * 
     * @throws IOException if an I/O occurs setting connection parameters 
     */
    private void applyConnectionParams(final HttpMethod method) throws IOException {
        int timeout = 0;
        // see if a timeout is given for this method
        Object param = method.getParams().getParameter(HttpMethodParams.SO_TIMEOUT);
        if (param == null) {
            // if not, use the default value
            param = this.conn.getParams().getParameter(HttpConnectionParams.SO_TIMEOUT);
        }
        if (param != null) {
            timeout = ((Integer)param).intValue();
        }
        this.conn.setSocketTimeout(timeout);                    
    }
    
    /**
     * Executes a method with the current hostConfiguration.
     *
     * @throws IOException if an I/O (transport) error occurs. Some transport exceptions 
     * can be recovered from.
     * @throws HttpException  if a protocol exception occurs. Usually protocol exceptions 
     * cannot be recovered from.
     */
    private void executeWithRetry(final HttpMethod method) 
        throws IOException, HttpException {
        
        /** How many times did this transparently handle a recoverable exception? */
        int execCount = 0;
        // loop until the method is successfully processed, the retryHandler 
        // returns false or a non-recoverable exception is thrown
        try {
            while (true) {
                execCount++;
                try {

                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Attempt number " + execCount + " to process request");
                    }
                    if (this.conn.getParams().isStaleCheckingEnabled()) {
                        this.conn.closeIfStale();
                    }
                    if (!this.conn.isOpen()) {
                        // this connection must be opened before it can be used
                        // This has nothing to do with opening a secure tunnel
                        this.conn.open();
                        boolean upgrade = isConnectionUpgrade(method);
                        if ((this.conn.isProxied() && (this.conn.isSecure() || upgrade))
                        && !(method instanceof ConnectMethod)) {
                            this.conn.setTunnelRequested(upgrade);
                            // we need to create a tunnel before we can execute the real method
                            if (!executeConnect()) {
                                // abort, the connect method failed
                                return;
                            }
                        }
                    }
                    applyConnectionParams(method);                    
                    method.execute(state, this.conn);
                    break;
                } catch (HttpException e) {
                    // filter out protocol exceptions which cannot be recovered from
                    throw e;
                } catch (IOException e) {
                    LOG.debug("Closing the connection.");
                    this.conn.close();
                    // test if this method should be retried
                    // ========================================
                    // this code is provided for backward compatibility with 2.0
                    // will be removed in the next major release
                    if (method instanceof HttpMethodBase) {
                        MethodRetryHandler handler = 
                            ((HttpMethodBase)method).getMethodRetryHandler();
                        if (handler != null) {
                            if (!handler.retryMethod(
                                    method,
                                    this.conn, 
                                    new HttpRecoverableException(e.getMessage()),
                                    execCount, 
                                    method.isRequestSent())) {
                                LOG.debug("Method retry handler returned false. "
                                        + "Automatic recovery will not be attempted");
                                throw e;
                            }
                        }
                    }
                    // ========================================
                    HttpMethodRetryHandler handler = 
                        (HttpMethodRetryHandler)method.getParams().getParameter(
                                HttpMethodParams.RETRY_HANDLER);
                    if (handler == null) {
                        handler = new DefaultHttpMethodRetryHandler();
                    }
                    if (!handler.retryMethod(method, e, execCount)) {
                        LOG.debug("Method retry handler returned false. "
                                + "Automatic recovery will not be attempted");
                        throw e;
                    }
                    if (LOG.isInfoEnabled()) {
                        LOG.info("I/O exception ("+ e.getClass().getName() +") caught when processing request: "
                                + e.getMessage());
                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(e.getMessage(), e);
                    }
                    LOG.info("Retrying request");
                }
            }
        } catch (IOException e) {
            if (this.conn.isOpen()) {
                LOG.debug("Closing the connection.");
                this.conn.close();
            }
            releaseConnection = true;
            throw e;
        } catch (RuntimeException e) {
            if (this.conn.isOpen()) {
                LOG.debug("Closing the connection.");
                this.conn.close();
            }
            releaseConnection = true;
            throw e;
        }
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

    /**
     * Executes a ConnectMethod to establish a tunneled connection.
     * 
     * @return <code>true</code> if the connect was successful
     * 
     * @throws IOException
     * @throws HttpException
     */
    private boolean executeConnect() 
        throws IOException, HttpException {

        this.connectMethod = new ConnectMethod(this.hostConfiguration);
        this.connectMethod.getParams().setDefaults(this.hostConfiguration.getParams());
        String agent = (String) getParams().getParameter(PARAM_DEFAULT_USER_AGENT_CONNECT_REQUESTS);
        if (agent != null) {
            this.connectMethod.setRequestHeader("User-Agent", agent);
        }
        
        int code;
        for (;;) {
            if (!this.conn.isOpen()) {
                this.conn.open();
            }
            if (this.params.isAuthenticationPreemptive()
                    || this.state.isAuthenticationPreemptive()) {
                LOG.debug("Preemptively sending default basic credentials");
                this.connectMethod.getProxyAuthState().setPreemptive();
                this.connectMethod.getProxyAuthState().setAuthAttempted(true);
            }
            try {
                authenticateProxy(this.connectMethod);
            } catch (AuthenticationException e) {
                LOG.error(e.getMessage(), e);
            }
            applyConnectionParams(this.connectMethod);                    
            this.connectMethod.execute(state, this.conn);
            code = this.connectMethod.getStatusCode();
            boolean retry = false;
            AuthState authstate = this.connectMethod.getProxyAuthState(); 
            authstate.setAuthRequested(code == HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED);
            if (authstate.isAuthRequested()) {
                if (processAuthenticationResponse(this.connectMethod)) {
                    retry = true;
                }
            }
            if (!retry) {
                break;
            }
            if (this.connectMethod.getResponseBodyAsStream() != null) {
                this.connectMethod.getResponseBodyAsStream().close();
            }
        }
        if ((code >= 200) && (code < 300)) {
            this.conn.tunnelCreated();
            // Drop the connect method, as it is no longer needed
            this.connectMethod = null;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Fake response
     * @param method
     */
    private void fakeResponse(final HttpMethod method)
        throws IOException, HttpException {
        // What is to follow is an ugly hack.
        // I REALLY hate having to resort to such
        // an appalling trick
        // The only feasible solution is to split monolithic
        // HttpMethod into HttpRequest/HttpResponse pair.
        // That would allow to execute CONNECT method 
        // behind the scene and return CONNECT HttpResponse 
        // object in response to the original request that 
        // contains the correct status line, headers & 
        // response body.
        LOG.debug("CONNECT failed, fake the response for the original method");
        // Pass the status, headers and response stream to the wrapped
        // method.
        // To ensure that the connection is not released more than once
        // this method is still responsible for releasing the connection. 
        // This will happen when the response body is consumed, or when
        // the wrapped method closes the response connection in 
        // releaseConnection().
        if (method instanceof HttpMethodBase) {
            ((HttpMethodBase) method).fakeResponse(
                this.connectMethod.getStatusLine(),
                this.connectMethod.getResponseHeaderGroup(),
                conn,
                this.connectMethod.getResponseBodyAsStream()
            );
            method.getProxyAuthState().setAuthScheme(
                this.connectMethod.getProxyAuthState().getAuthScheme());
            this.connectMethod = null;
        } else {
            releaseConnection = true;
            LOG.warn(
                "Unable to fake response on method as it is not derived from HttpMethodBase.");
        }
    }
    
	/**
	 * Process the redirect response.
     * 
	 * @return <code>true</code> if the redirect was successful
	 */
	private boolean processRedirectResponse(final HttpMethod method)
     throws RedirectException {
		//get the location header to find out where to redirect to
		Header locationHeader = method.getResponseHeader("location");
		if (locationHeader == null) {
			// got a redirect response, but no location header
			LOG.error("Received redirect response " + method.getStatusCode()
					+ " but no location header");
			return false;
		}
		String location = locationHeader.getValue();
		if (LOG.isDebugEnabled()) {
			LOG.debug("Redirect requested to location '" + location + "'");
		}
        
		//rfc2616 demands the location value be a complete URI
		//Location       = "Location" ":" absoluteURI
		URI redirectUri = null;
		URI currentUri = null;

		try {
			currentUri = new URI(
				this.conn.getProtocol().getScheme(),
				null,
                this.conn.getHost(), 
                this.conn.getPort(), 
				method.getPath()
			);
			
            String charset = method.getParams().getUriCharset();
            redirectUri = new URI(location, true, charset);
			
            if (redirectUri.isRelativeURI()) {
				if (this.params.isParameterTrue(HttpClientParams.REJECT_RELATIVE_REDIRECT)) {
					LOG.warn("Relative redirect location '" + location + "' not allowed");
					return false;
				} else { 
					//location is incomplete, use current values for defaults
					LOG.debug("Redirect URI is not absolute - parsing as relative");
					redirectUri = new URI(currentUri, redirectUri);
				}
			} else {
                // Reset the default params
                method.getParams().setDefaults(this.params);
            }
            method.setURI(redirectUri);
            hostConfiguration.setHost(redirectUri);
		} catch (URIException ex) {
            throw new InvalidRedirectLocationException(
                    "Invalid redirect location: " + location, location, ex);
		}

        if (this.params.isParameterFalse(HttpClientParams.ALLOW_CIRCULAR_REDIRECTS)) {
            if (this.redirectLocations == null) {
                this.redirectLocations = new HashSet<URI>();
            }
            this.redirectLocations.add(currentUri);
            try {
                if(redirectUri.hasQuery()) {
                    redirectUri.setQuery(null);
                }
            } catch (URIException e) {
                // Should never happen
                return false;
            }

            if (this.redirectLocations.contains(redirectUri)) {
                throw new CircularRedirectException("Circular redirect to '" +
                    redirectUri + "'");
            }
        }

		if (LOG.isDebugEnabled()) {
			LOG.debug("Redirecting from '" + currentUri.getEscapedURI()
				+ "' to '" + redirectUri.getEscapedURI());
		}
        //And finally invalidate the actual authentication scheme
                method.getHostAuthState().invalidate(); 
                method.getProxyAuthState().invalidate(); 
		return true;
	}

	/**
	 * Processes a response that requires authentication
	 *
	 * @param method the current {@link HttpMethod HTTP method}
	 *
	 * @return <tt>true</tt> if the authentication challenge can be responsed to,
     *   (that is, at least one of the requested authentication scheme is supported, 
     *   and matching credentials have been found), <tt>false</tt> otherwise.
	 */
	private boolean processAuthenticationResponse(final HttpMethod method) {
		LOG.trace("enter HttpMethodBase.processAuthenticationResponse("
			+ "HttpState, HttpConnection)");

		try {
            switch (method.getStatusCode()) {
                case HttpStatus.SC_UNAUTHORIZED:
                    return processWWWAuthChallenge(method);
                case HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED:
                    return processProxyAuthChallenge(method);
                default:
                    return false;
            }
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getMessage(), e);
            }
            return false;
        }
	}

    private boolean processWWWAuthChallenge(final HttpMethod method)
        throws MalformedChallengeException, AuthenticationException  
    {
        AuthState authstate = method.getHostAuthState();
        Map<?, ?> challenges = AuthChallengeParser.parseChallenges(
            method.getResponseHeaders(WWW_AUTH_CHALLENGE));
        if (challenges.isEmpty()) {
            LOG.debug("Authentication challenge(s) not found");
            return false; 
        }
        AuthScheme authscheme = null;
        try {
            authscheme = this.authProcessor.processChallenge(authstate, challenges);
        } catch (AuthChallengeException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(e.getMessage());
            }
        }
        if (authscheme == null) {
            return false;
        }
        String host = method.getParams().getVirtualHost();
        if (host == null) {
            host = conn.getHost();
        }
        int port = conn.getPort();
        AuthScope authscope = new AuthScope(
            host, port, 
            authscheme.getRealm(), 
            authscheme.getSchemeName());
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Authentication scope: " + authscope);
        }
        if (authstate.isAuthAttempted() && authscheme.isComplete()) {
            // Already tried and failed
            Credentials credentials = promptForCredentials(
                authscheme, method.getParams(), authscope);
            if (credentials == null) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Failure authenticating with " + authscope);
                }
                return false;
            } else {
                return true;
            }
        } else {
            authstate.setAuthAttempted(true);
            Credentials credentials = this.state.getCredentials(authscope);
            if (credentials == null) {
                credentials = promptForCredentials(
                    authscheme, method.getParams(), authscope);
            }
            if (credentials == null) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("No credentials available for " + authscope); 
                }
                return false;
            } else {
                return true;
            }
        }
    }

    private boolean processProxyAuthChallenge(final HttpMethod method)
        throws MalformedChallengeException, AuthenticationException
    {  
        AuthState authstate = method.getProxyAuthState();
        Map<?, ?> proxyChallenges = AuthChallengeParser.parseChallenges(
            method.getResponseHeaders(PROXY_AUTH_CHALLENGE));
        if (proxyChallenges.isEmpty()) {
            LOG.debug("Proxy authentication challenge(s) not found");
            return false; 
        }
        AuthScheme authscheme = null;
        try {
            authscheme = this.authProcessor.processChallenge(authstate, proxyChallenges);
        } catch (AuthChallengeException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(e.getMessage());
            }
        }
        if (authscheme == null) {
            return false;
        }
        AuthScope authscope = new AuthScope(
            conn.getProxyHost(), conn.getProxyPort(), 
            authscheme.getRealm(), 
            authscheme.getSchemeName());  

        if (LOG.isDebugEnabled()) {
            LOG.debug("Proxy authentication scope: " + authscope);
        }
        if (authstate.isAuthAttempted() && authscheme.isComplete()) {
            // Already tried and failed
            Credentials credentials = promptForProxyCredentials(
                authscheme, method.getParams(), authscope);
            if (credentials == null) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Failure authenticating with " + authscope);
                }
                return false;
            } else {
                return true;
            }
        } else {
            authstate.setAuthAttempted(true);
            Credentials credentials = this.state.getProxyCredentials(authscope);
            if (credentials == null) {
                credentials = promptForProxyCredentials(
                    authscheme, method.getParams(), authscope);
            }
            if (credentials == null) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("No credentials available for " + authscope); 
                }
                return false;
            } else {
                return true;
            }
        }
    }

    /**
     * Tests if the {@link HttpMethod method} requires a redirect to another location.
     * 
     * @param method HTTP method
     * 
     * @return boolean <tt>true</tt> if a retry is needed, <tt>false</tt> otherwise.
     */
	private boolean isRedirectNeeded(final HttpMethod method) {
		switch (method.getStatusCode()) {
			case HttpStatus.SC_MOVED_TEMPORARILY:
			case HttpStatus.SC_MOVED_PERMANENTLY:
			case HttpStatus.SC_SEE_OTHER:
			case HttpStatus.SC_TEMPORARY_REDIRECT:
				LOG.debug("Redirect required");
                if (method.getFollowRedirects()) {
                    return true;
                } else {
                    return false;
                }
			default:
				return false;
		} //end of switch
	}

    /**
     * Tests if the {@link HttpMethod method} requires authentication.
     * 
     * @param method HTTP method
     * 
     * @return boolean <tt>true</tt> if a retry is needed, <tt>false</tt> otherwise.
     */
    private boolean isAuthenticationNeeded(final HttpMethod method) {
        method.getHostAuthState().setAuthRequested(
                method.getStatusCode() == HttpStatus.SC_UNAUTHORIZED);
        method.getProxyAuthState().setAuthRequested(
                method.getStatusCode() == HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED);
        if (method.getHostAuthState().isAuthRequested() || 
            method.getProxyAuthState().isAuthRequested()) {
            LOG.debug("Authorization required");
            if (method.getDoAuthentication()) { //process authentication response
                return true;
            } else { //let the client handle the authenticaiton
                LOG.info("Authentication requested but doAuthentication is "
                        + "disabled");
                return false;
            }
        } else {
            return false;
        }
    }

    private Credentials promptForCredentials(
        final AuthScheme authScheme,
        final HttpParams params, 
        final AuthScope authscope)
    {
        LOG.debug("Credentials required");
        Credentials creds = null;
        CredentialsProvider credProvider = 
            (CredentialsProvider)params.getParameter(CredentialsProvider.PROVIDER);
        if (credProvider != null) {
            try {
                creds = credProvider.getCredentials(
                    authScheme, authscope.getHost(), authscope.getPort(), false);
            } catch (CredentialsNotAvailableException e) {
                LOG.warn(e.getMessage());
            }
            if (creds != null) {
                this.state.setCredentials(authscope, creds);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(authscope + " new credentials given");
                }
            }
        } else {
            LOG.debug("Credentials provider not available");
        }
        return creds;
    }

    private Credentials promptForProxyCredentials(
        final AuthScheme authScheme,
        final HttpParams params,
        final AuthScope authscope) 
    {
        LOG.debug("Proxy credentials required");
        Credentials creds = null;
        CredentialsProvider credProvider = 
            (CredentialsProvider)params.getParameter(CredentialsProvider.PROVIDER);
        if (credProvider != null) {
            try {
                creds = credProvider.getCredentials(
                    authScheme, authscope.getHost(), authscope.getPort(), true);
            } catch (CredentialsNotAvailableException e) {
                LOG.warn(e.getMessage());
            }
            if (creds != null) {
                this.state.setProxyCredentials(authscope, creds);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(authscope + " new credentials given");
                }
            }
        } else {
            LOG.debug("Proxy credentials provider not available");
        }
        return creds;
    }

    /**
     * @return
     */
    public HostConfiguration getHostConfiguration() {
        return hostConfiguration;
    }

    /**
     * @return
     */
    public HttpState getState() {
        return state;
    }

    /**
     * @return
     */
    public HttpConnectionManager getConnectionManager() {
        return connectionManager;
    }

    /**
     * @return
     */
    public HttpParams getParams() {
        return this.params;
    }
}
