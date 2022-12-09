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
// ZAP: 2016/06/08 Set User-Agent header defined in options as default for (internal) CONNECT
// requests
// ZAP: 2016/06/10 Allow to validate the URI of the redirections before being followed
// ZAP: 2016/08/04 Added removeListener(..)
// ZAP: 2016/12/07 Add initiator constant for AJAX spider requests
// ZAP: 2016/12/12 Add initiator constant for Forced Browse requests
// ZAP: 2017/03/27 Introduce HttpRequestConfig.
// ZAP: 2017/06/12 Allow to ignore listeners.
// ZAP: 2017/06/19 Allow to send a request with custom socket timeout.
// ZAP: 2017/11/20 Add initiator constant for Token Generator requests.
// ZAP: 2017/11/27 Use custom CookieSpec (ZapCookieSpec).
// ZAP: 2017/12/20 Apply socket connect timeout (Issue 4171).
// ZAP: 2018/02/06 Make the lower case changes locale independent (Issue 4327).
// ZAP: 2018/02/19 Added WEB_SOCKET_INITIATOR.
// ZAP: 2018/02/23 Issue 1161: Allow to override the global session tracking setting
//                 Fix Session Tracking button sync
// ZAP: 2018/08/03 Added AUTHENTICATION_HELPER_INITIATOR.
// ZAP: 2018/09/17 Set the user to messages created for redirections (Issue 2531).
// ZAP: 2018/10/12 Deprecate getClient(), it exposes implementation details.
// ZAP: 2019/03/24 Removed commented and unused sendAndReceive method.
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2019/08/19 Reinstate proxy auth credentials when HTTP state is changed.
// ZAP: 2019/09/17 Use remove() instead of set(null) on IN_LISTENER.
// ZAP: 2019/09/25 Add option to disable cookies
// ZAP: 2020/04/20 Configure if the names should be resolved or not (Issue 29).
// ZAP: 2020/09/04 Added AUTHENTICATION_POLL_INITIATOR
// ZAP: 2020/11/26 Use Log4j 2 classes for logging.
// ZAP: 2020/12/09 Set content encoding to the response body.
// ZAP: 2021/05/14 Remove redundant type arguments and empty statement.
// ZAP: 2022/01/04 Add initiator constant OAST_INITIATOR for OAST requests.
// ZAP: 2022/04/08 Deprecate getSSLConnector() and executeMethod.
// ZAP: 2022/04/10 Add support for unencoded redirects
// ZAP: 2022/04/11 Deprecate set/getUserAgent() and remove userAgent/modifyUserAgent().
// ZAP: 2022/04/11 Prevent null listeners and add JavaDoc to add/removeListener.
// ZAP: 2022/04/23 Use main connection options directly.
// ZAP: 2022/04/24 Notify listeners of all redirects followed.
// ZAP: 2022/04/24 Move network initialisations from ZAP class.
// ZAP: 2022/04/24 Allow to download to file.
// ZAP: 2022/04/27 Expose global HTTP state enabled status.
// ZAP: 2022/04/27 Use latest proxy settings always.
// ZAP: 2022/04/29 Deprecate setAllowCircularRedirects.
// ZAP: 2022/05/04 Always use single cookie request header.
// ZAP: 2022/05/04 Use latest timeout/user-agent always.
// ZAP: 2022/05/20 Address deprecation warnings with ConnectionParam.
// ZAP: 2022/05/29 Remove redundant checks and create SSLConnector always.
// ZAP: 2022/05/30 Use shared connection pool.
// ZAP: 2022/06/03 Remove commented code and make listeners comparator final.
// ZAP: 2022/06/03 Move implementation to HttpSenderParos.
// ZAP: 2022/06/05 Remove usage of HttpException.
// ZAP: 2022/06/07 Address deprecation warnings with HttpSenderParos.
// ZAP: 2022/06/13 Added param digger initiator.
// ZAP: 2022/12/09 Allow to restore HttpSenderImpl state.
package org.parosproxy.paros.network;

import java.io.IOException;
import java.nio.file.Path;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.zaproxy.zap.network.HttpRedirectionValidator;
import org.zaproxy.zap.network.HttpRequestConfig;
import org.zaproxy.zap.network.HttpSenderContext;
import org.zaproxy.zap.network.HttpSenderImpl;
import org.zaproxy.zap.network.HttpSenderListener;
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
    public static final int AJAX_SPIDER_INITIATOR = 10;
    public static final int FORCED_BROWSE_INITIATOR = 11;
    public static final int TOKEN_GENERATOR_INITIATOR = 12;
    public static final int WEB_SOCKET_INITIATOR = 13;
    public static final int AUTHENTICATION_HELPER_INITIATOR = 14;
    public static final int AUTHENTICATION_POLL_INITIATOR = 15;
    public static final int OAST_INITIATOR = 16;
    public static final int PARAM_DIGGER_INITIATOR = 17;

    private static final HttpRequestConfig NO_REDIRECTS = HttpRequestConfig.builder().build();
    private static final HttpRequestConfig FOLLOW_REDIRECTS =
            HttpRequestConfig.builder().setFollowRedirects(true).build();

    @SuppressWarnings("deprecation")
    private static final HttpSenderParos PAROS_IMPL = new HttpSenderParos();

    @SuppressWarnings("rawtypes")
    private static HttpSenderImpl impl = PAROS_IMPL;

    private static Object implState;

    private HttpSenderContext ctx;

    private final int initiator;

    /** <strong>Note:</strong> Not part of the public API. */
    public static <T extends HttpSenderContext> void setImpl(HttpSenderImpl<T> impl) {
        if (HttpSender.impl != PAROS_IMPL) {
            implState = HttpSender.impl.saveState();
        }

        HttpSender.impl = impl == null ? PAROS_IMPL : impl;

        if (HttpSender.impl != PAROS_IMPL) {
            HttpSender.impl.restoreState(implState);
            implState = null;
        }
    }

    /**
     * Constructs an {@code HttpSender}.
     *
     * <p>The {@code initiator} is used to indicate the component that is sending the messages when
     * the {@code HttpSenderListener}s are notified of messages sent and received.
     *
     * @param connectionParam the parameters used to setup the connections to target hosts
     * @param useGlobalState {@code true} if the messages sent/received should use the global HTTP
     *     state, {@code false} if should use a non shared HTTP state
     * @param initiator the ID of the initiator of the HTTP messages sent
     * @see ConnectionParam#getHttpState()
     * @see HttpSenderListener
     * @see HttpMessage#getRequestingUser()
     * @deprecated (2.12.0) Use {@link #HttpSender(int)} instead, refer also to {@link
     *     #setUseGlobalState(boolean)}.
     */
    @Deprecated
    public HttpSender(ConnectionParam connectionParam, boolean useGlobalState, int initiator) {
        this(useGlobalState, initiator);
    }

    /**
     * Constructs an {@code HttpSender}.
     *
     * <p>Refer to {@link #setUseGlobalState(boolean)} to know how the HTTP state is managed.
     *
     * <p>The {@code initiator} is used to indicate the component that is sending the messages when
     * the {@code HttpSenderListener}s are notified of messages sent and received.
     *
     * @param initiator the ID of the initiator of the HTTP messages sent
     * @since 2.12.0
     * @see HttpSenderListener
     */
    public HttpSender(int initiator) {
        this(true, initiator);
    }

    private HttpSender(boolean useGlobalState, int initiator) {
        this.initiator = initiator;
        HttpSenderContext createdCtx = impl.createContext(this, initiator);
        if (impl.getContext(this) == null) {
            ctx = createdCtx;
        }

        setUseGlobalState(useGlobalState);
        setUseCookies(true);
    }

    private HttpSenderContext getContext() {
        if (ctx != null) {
            return ctx;
        }
        return impl.getContext(this);
    }

    /**
     * Gets the {@code SSLConnector} of the client.
     *
     * @return the {@code SSLConnector} used by the sender.
     * @deprecated (2.12.0) It will be removed in a following version.
     */
    @Deprecated
    public static SSLConnector getSSLConnector() {
        return PAROS_IMPL.getSslConnector();
    }

    /**
     * Sets whether or not the global state should be used. Defaults to {@code true}.
     *
     * <p>If {@code enableGlobalState} is {@code true} the {@code HttpSender} will use the HTTP
     * state given by the connections options iff the HTTP state is enabled there otherwise it
     * doesn't have any state (i.e. cookies are disabled). If {@code enableGlobalState} is {@code
     * false} it uses a non shared HTTP state.
     *
     * <p><strong>Note:</strong> The actual state used is overridden when {@link
     * #getUser(HttpMessage)} returns non-{@code null}.
     *
     * @param enableGlobalState {@code true} if the global state should be used, {@code false}
     *     otherwise.
     * @since 2.8.0
     * @see #isGlobalStateEnabled()
     * @see #setUseCookies(boolean)
     */
    public void setUseGlobalState(boolean enableGlobalState) {
        getContext().setUseGlobalState(enableGlobalState);
    }

    /**
     * Tells whether or not the global HTTP state is enabled.
     *
     * @return {@code true} if the global HTTP state is enabled, {@code false} otherwise.
     * @since 2.12.0
     * @see #setUseGlobalState(boolean)
     */
    public boolean isGlobalStateEnabled() {
        return impl.isGlobalStateEnabled();
    }

    /**
     * Sets whether or not the requests sent should keep track of cookies.
     *
     * @param shouldUseCookies {@code true} if cookies should be used, {@code false} otherwise.
     * @since 2.9.0
     * @see #setUseGlobalState(boolean)
     */
    public void setUseCookies(boolean shouldUseCookies) {
        getContext().setUseCookies(shouldUseCookies);
    }

    /**
     * Executes the given method.
     *
     * @param method the method.
     * @param state the state, might be {@code null}.
     * @return the status code.
     * @throws IOException if an error occurred while executing the method.
     * @deprecated (2.12.0) Use one of the {@code sendAndReceive} methods. It will be removed in a
     *     following version.
     */
    @Deprecated
    public int executeMethod(HttpMethod method, HttpState state) throws IOException {
        HttpSenderContext ctxTemp = getContext();
        if (!(ctxTemp instanceof HttpSenderContextParos)) {
            ctxTemp = PAROS_IMPL.createContext(this, initiator);
        }
        return PAROS_IMPL.executeMethodImpl((HttpSenderContextParos) ctxTemp, method, state);
    }

    /** @deprecated (2.12.0) No longer needed. */
    @Deprecated
    public void shutdown() {}

    /**
     * Downloads the response (body) to the given file.
     *
     * <p>The body in the given {@code message} will be empty.
     *
     * @param message the message containing the request to send.
     * @param file the file where to save the response body.
     * @throws IOException if an error occurred while sending the request or while downloading.
     * @since 2.12.0
     * @see #setFollowRedirect(boolean)
     */
    public void sendAndReceive(HttpMessage message, Path file) throws IOException {
        sendImpl(null, message, file);
    }

    public void sendAndReceive(HttpMessage msg) throws IOException {
        sendImpl(null, msg, null);
    }

    /**
     * Send and receive a HttpMessage.
     *
     * @param msg
     * @param isFollowRedirect
     * @throws IOException
     * @see #sendAndReceive(HttpMessage, HttpRequestConfig)
     */
    public void sendAndReceive(HttpMessage msg, boolean isFollowRedirect) throws IOException {
        sendImpl(isFollowRedirect ? FOLLOW_REDIRECTS : NO_REDIRECTS, msg, null);
    }

    /**
     * Gets the user set in this {@code HttpSender} if any, otherwise the one in the given {@code
     * HttpMessage}.
     *
     * @param msg usually the message being sent, that might have a user.
     * @return the user set in the {@code HttpSender} or in the given {@code HttpMessage}. Might be
     *     {@code null} if no user set.
     * @throws NullPointerException if the given message is {@code null}.
     * @since 2.4.1
     * @see #setUser(User)
     * @see HttpMessage#getRequestingUser()
     */
    public User getUser(HttpMessage msg) {
        return getContext().getUser(msg);
    }

    public void setFollowRedirect(boolean followRedirect) {
        getContext().setFollowRedirects(followRedirect);
    }

    /**
     * @return Returns the userAgent.
     * @deprecated (2.12.0) No longer supported, it returns an empty string.
     * @see #setUserAgent(String)
     */
    @Deprecated
    public static String getUserAgent() {
        return "";
    }

    /**
     * @param userAgent The userAgent to set.
     * @deprecated (2.12.0) No longer supported, use a {@link HttpSenderListener} to actually set
     *     the user agent.
     */
    @Deprecated
    public static void setUserAgent(String userAgent) {}

    /**
     * Adds the given listener to be notified of each message sent/received by each {@code
     * HttpSender}.
     *
     * <p>The listener might be notified concurrently.
     *
     * @param listener the listener to add.
     * @since 2.0.0
     * @throws NullPointerException if the given listener is {@code null}.
     */
    public static void addListener(HttpSenderListener listener) {
        impl.addListener(listener);
    }

    /**
     * Removes the given listener.
     *
     * @param listener the listener to remove.
     * @since 2.0.0
     * @throws NullPointerException if the given listener is {@code null}.
     */
    public static void removeListener(HttpSenderListener listener) {
        impl.removeListener(listener);
    }

    /**
     * Set the user to scan as. If null then the current session will be used.
     *
     * @param user
     */
    public void setUser(User user) {
        getContext().setUser(user);
    }

    /**
     * @return the HTTP client implementation.
     * @deprecated (2.8.0) Do not use, this exposes implementation details which might change
     *     without warning. It will be removed in a following version.
     */
    @Deprecated
    public HttpClient getClient() {
        return PAROS_IMPL.getClient();
    }

    /**
     * Sets whether or not the authentication headers ("Authorization" and "Proxy-Authorization")
     * already present in the request should be removed if received an authentication challenge
     * (status codes 401 and 407).
     *
     * <p>If {@code true} new authentication headers will be generated and the old ones removed
     * otherwise the authentication headers already present in the request will be used to
     * authenticate.
     *
     * <p>Default is {@code false}, i.e. use the headers already present in the request header.
     *
     * <p>Processes that reuse messages previously sent should consider setting this to {@code
     * true}, otherwise new authentication challenges might fail.
     *
     * @param removeHeaders {@code true} if the the authentication headers already present should be
     *     removed when challenged, {@code false} otherwise
     */
    public void setRemoveUserDefinedAuthHeaders(boolean removeHeaders) {
        getContext().setRemoveUserDefinedAuthHeaders(removeHeaders);
    }

    /**
     * Sets the maximum number of retries of an unsuccessful request caused by I/O errors.
     *
     * <p>The default number of retries is 3.
     *
     * @param retries the number of retries
     * @throws IllegalArgumentException if {@code retries} is negative.
     * @since 2.4.0
     */
    public void setMaxRetriesOnIOError(int retries) {
        getContext().setMaxRetriesOnIoError(retries);
    }

    /**
     * Sets the maximum number of redirects that will be followed before failing with an exception.
     *
     * <p>The default maximum number of redirects is 100.
     *
     * @param maxRedirects the maximum number of redirects
     * @throws IllegalArgumentException if {@code maxRedirects} is negative.
     * @since 2.4.0
     */
    public void setMaxRedirects(int maxRedirects) {
        getContext().setMaxRedirects(maxRedirects);
    }

    /**
     * Sets whether or not circular redirects are allowed.
     *
     * <p>Circular redirects happen when a request redirects to itself, or when a same request was
     * already accessed in a chain of redirects.
     *
     * <p>Since 2.5.0, the default is to allow circular redirects.
     *
     * @param allow {@code true} if circular redirects should be allowed, {@code false} otherwise
     * @since 2.4.0
     * @deprecated (2.12.0) No longer supported, the circular redirects are allowed always. If
     *     needed they can be prevented with a custom {@link HttpRedirectionValidator}.
     */
    @Deprecated
    public void setAllowCircularRedirects(boolean allow) {}

    /**
     * Sends the request of given HTTP {@code message} with the given configurations.
     *
     * @param message the message that will be sent
     * @param requestConfig the request configurations.
     * @throws IllegalArgumentException if any of the parameters is {@code null}
     * @throws IOException if an error occurred while sending the message or following the
     *     redirections
     * @since 2.6.0
     * @see #sendAndReceive(HttpMessage, boolean)
     */
    public void sendAndReceive(HttpMessage message, HttpRequestConfig requestConfig)
            throws IOException {
        sendImpl(requestConfig, message, null);
    }

    @SuppressWarnings("unchecked")
    private void sendImpl(HttpRequestConfig requestConfig, HttpMessage message, Path file)
            throws IOException {
        if (ctx == null) {
            impl.sendAndReceive(this, requestConfig, message, file);
        } else {
            impl.sendAndReceive(ctx, requestConfig, message, file);
        }
    }
}
