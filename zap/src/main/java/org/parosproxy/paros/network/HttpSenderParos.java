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
// ZAP: 2022/06/03 Move implementation from HttpSender.
// ZAP: 2022/06/07 Deprecate the class.
// ZAP: 2022/09/21 Use format specifiers instead of concatenation when logging.
package org.parosproxy.paros.network;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.net.SocketFactory;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpHost;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.InvalidRedirectLocationException;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.model.Model;
import org.zaproxy.zap.network.HttpRedirectionValidator;
import org.zaproxy.zap.network.HttpRequestConfig;
import org.zaproxy.zap.network.HttpSenderImpl;
import org.zaproxy.zap.network.HttpSenderListener;
import org.zaproxy.zap.users.User;

/** @deprecated (2.12.0) Implementation details, do not use. */
@Deprecated
public class HttpSenderParos implements HttpSenderImpl<HttpSenderContextParos> {

    private static final Logger log = LogManager.getLogger(HttpSenderParos.class);

    private static SSLConnector sslConnector;

    private static List<HttpSenderListener> listeners = new ArrayList<>();
    private static final Comparator<HttpSenderListener> LISTENERS_COMPARATOR =
            (o1, o2) -> Integer.compare(o1.getListenerOrder(), o2.getListenerOrder());

    static {
        sslConnector = new SSLConnector(true);

        Protocol.registerProtocol(
                "https", new Protocol("https", (ProtocolSocketFactory) sslConnector, 443));

        Protocol.registerProtocol(
                "http", new Protocol("http", new ProtocolSocketFactoryImpl(), 80));

        AuthPolicy.registerAuthScheme(AuthPolicy.NTLM, org.zaproxy.zap.network.ZapNTLMScheme.class);
        CookiePolicy.registerCookieSpec(
                CookiePolicy.DEFAULT, org.zaproxy.zap.network.ZapCookieSpec.class);
        CookiePolicy.registerCookieSpec(
                CookiePolicy.BROWSER_COMPATIBILITY, org.zaproxy.zap.network.ZapCookieSpec.class);
    }

    private static HttpMethodHelper helper = new HttpMethodHelper();
    private static final ThreadLocal<Boolean> IN_LISTENER = new ThreadLocal<>();
    private static final HttpRequestConfig NO_REDIRECTS = HttpRequestConfig.builder().build();
    private static final HttpRequestConfig FOLLOW_REDIRECTS =
            HttpRequestConfig.builder().setFollowRedirects(true).build();

    private static final ResponseBodyConsumer DEFAULT_BODY_CONSUMER =
            (msg, method) -> {
                if (msg.isEventStream()) {
                    msg.getResponseBody().setCharset(msg.getResponseHeader().getCharset());
                    msg.getResponseBody().setLength(0);
                    return;
                }

                msg.setResponseBody(method.getResponseBody());
            };

    private ConnectionParam param = null;

    private static MultiThreadedHttpConnectionManager httpConnManager;

    private ConnectionParam getParam() {
        if (param == null) {
            param = Model.getSingleton().getOptionsParam().getConnectionParam();
        }
        return param;
    }

    @Override
    public boolean isGlobalStateEnabled() {
        return getParam().isHttpStateEnabled();
    }

    private static MultiThreadedHttpConnectionManager getConnectionManager() {
        if (httpConnManager == null) {
            createConnectionManager();
        }
        return httpConnManager;
    }

    private static synchronized void createConnectionManager() {
        if (httpConnManager == null) {
            httpConnManager = new MultiThreadedHttpConnectionManager();
            httpConnManager.getParams().setStaleCheckingEnabled(true);
            // Set to arbitrary large values to prevent locking
            httpConnManager.getParams().setDefaultMaxConnectionsPerHost(10000);
            httpConnManager.getParams().setMaxTotalConnections(200000);
        }
    }

    SSLConnector getSslConnector() {
        return sslConnector;
    }

    HttpClient getClient() {
        return new HttpClient(getConnectionManager());
    }

    private void setProxyAuth(HttpState state) {
        if (getParam().isUseProxyChain() && getParam().isUseProxyChainAuth()) {
            String realm = getParam().getProxyChainRealm();
            state.setProxyCredentials(
                    new AuthScope(
                            getParam().getProxyChainName(),
                            getParam().getProxyChainPort(),
                            realm.isEmpty() ? AuthScope.ANY_REALM : realm),
                    new NTCredentials(
                            getParam().getProxyChainUserName(),
                            getParam().getProxyChainPassword(),
                            "",
                            realm));
        } else {
            state.clearProxyCredentials();
        }
    }

    @Override
    public HttpSenderContextParos createContext(HttpSender parent, int initiator) {
        HttpClient client = new HttpClient(getConnectionManager());
        client.getParams().setBooleanParameter(HttpClientParams.ALLOW_CIRCULAR_REDIRECTS, true);

        // Set how cookie headers are sent no matter of the "allowState", in case a state is forced
        // by other extensions (e.g. Authentication)
        client.getParams().setBooleanParameter(HttpMethodParams.SINGLE_COOKIE_HEADER, true);
        return new HttpSenderContextParos(parent, initiator, getParam(), client);
    }

    int executeMethodImpl(HttpSenderContextParos ctx, HttpMethod method, HttpState state)
            throws IOException {
        int responseCode = -1;

        String hostName;
        hostName = method.getURI().getHost();
        method.setDoAuthentication(true);
        HostConfiguration hc = null;

        HttpClient requestClient;
        if (isConnectionUpgrade(method)) {
            requestClient = new HttpClient(new org.zaproxy.zap.ZapHttpConnectionManager());
        } else {
            requestClient = ctx.getHttpClient();
        }

        if (ctx.getInitiator() == HttpSender.CHECK_FOR_UPDATES_INITIATOR) {
            // Use the 'strict' SSLConnector, i.e. one that performs all the usual cert checks
            // The 'standard' one 'trusts' everything
            // This is to ensure that all 'check-for update' calls are made to the expected https
            // urls
            // without this is would be possible to intercept and change the response which could
            // result
            // in the user downloading and installing a malicious add-on
            hc =
                    new HostConfiguration() {
                        @Override
                        public synchronized void setHost(URI uri) {
                            try {
                                setHost(new HttpHost(uri.getHost(), uri.getPort(), getProtocol()));
                            } catch (URIException e) {
                                throw new IllegalArgumentException(e.toString());
                            }
                        }
                    };

            hc.setHost(
                    hostName,
                    method.getURI().getPort(),
                    new Protocol("https", (ProtocolSocketFactory) new SSLConnector(false), 443));
        }

        method.getParams()
                .setBooleanParameter(
                        org.apache.commons.httpclient.HttpMethodDirector.PARAM_RESOLVE_HOSTNAME,
                        getParam().shouldResolveRemoteHostname(hostName));
        method.getParams()
                .setParameter(
                        org.apache.commons.httpclient.HttpMethodDirector
                                .PARAM_DEFAULT_USER_AGENT_CONNECT_REQUESTS,
                        getParam().getDefaultUserAgent());

        int timeout = (int) TimeUnit.SECONDS.toMillis(getParam().getTimeoutInSecs());
        method.getParams().setSoTimeout(timeout);
        httpConnManager.getParams().setConnectionTimeout(timeout);

        // ZAP: Check if a custom state is being used
        if (state != null) {
            // Make sure cookies are enabled
            method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            setProxyAuth(state);
        } else {
            setProxyAuth(requestClient.getState());
        }

        if (getParam().isUseProxy(hostName)) {
            if (hc == null) {
                hc = new HostConfiguration();
                hc.setHost(hostName, method.getURI().getPort(), method.getURI().getScheme());
            }
            hc.setProxy(getParam().getProxyChainName(), getParam().getProxyChainPort());
        }

        responseCode = requestClient.executeMethod(hc, method, state);

        return responseCode;
    }

    /**
     * Tells whether or not the given {@code method} has a {@code Connection} request header with
     * {@code Upgrade} value.
     *
     * @param method the method that will be checked
     * @return {@code true} if the {@code method} has a connection upgrade, {@code false} otherwise
     */
    private static boolean isConnectionUpgrade(HttpMethod method) {
        Header connectionHeader = method.getRequestHeader("connection");
        if (connectionHeader == null) {
            return false;
        }
        return connectionHeader.getValue().toLowerCase(Locale.ROOT).contains("upgrade");
    }

    @Override
    public void sendAndReceive(
            HttpSenderContextParos ctx, HttpRequestConfig config, HttpMessage message, Path file)
            throws IOException {
        HttpRequestConfig effectiveConfig = getEffectiveConfig(ctx, config);

        if (file != null) {
            sendAndReceive(
                    ctx,
                    message,
                    effectiveConfig,
                    (msg, method) -> {
                        if (effectiveConfig.isFollowRedirects()
                                && isRedirectionNeeded(msg.getResponseHeader().getStatusCode())) {
                            DEFAULT_BODY_CONSUMER.accept(msg, method);
                            return;
                        }

                        HttpResponseHeader header = msg.getResponseHeader();
                        try (FileChannel channel =
                                        (FileChannel)
                                                Files.newByteChannel(
                                                        file,
                                                        EnumSet.of(
                                                                StandardOpenOption.WRITE,
                                                                StandardOpenOption.CREATE,
                                                                StandardOpenOption
                                                                        .TRUNCATE_EXISTING));
                                InputStream is = method.getResponseBodyAsStream()) {
                            long totalRead = 0;
                            while ((totalRead +=
                                            channel.transferFrom(
                                                    Channels.newChannel(is), totalRead, 1 << 24))
                                    < header.getContentLength()) ;
                        }
                    });
        }

        sendAndReceive(ctx, message, effectiveConfig, DEFAULT_BODY_CONSUMER);
    }

    private HttpRequestConfig getEffectiveConfig(
            HttpSenderContextParos ctx, HttpRequestConfig config) {
        if (config != null) {
            return config;
        }
        return ctx.isFollowRedirects() ? FOLLOW_REDIRECTS : NO_REDIRECTS;
    }

    private static void notifyRequestListeners(HttpSenderContextParos ctx, HttpMessage msg) {
        if (IN_LISTENER.get() != null) {
            // This is a request from one of the listeners - prevent infinite recursion
            return;
        }
        try {
            IN_LISTENER.set(true);
            for (HttpSenderListener listener : listeners) {
                try {
                    listener.onHttpRequestSend(msg, ctx.getInitiator(), ctx.getParent());
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        } finally {
            IN_LISTENER.remove();
        }
    }

    private static void notifyResponseListeners(HttpSenderContextParos ctx, HttpMessage msg) {
        if (IN_LISTENER.get() != null) {
            // This is a request from one of the listeners - prevent infinite recursion
            return;
        }
        try {
            IN_LISTENER.set(true);
            for (HttpSenderListener listener : listeners) {
                try {
                    listener.onHttpResponseReceive(msg, ctx.getInitiator(), ctx.getParent());
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        } finally {
            IN_LISTENER.remove();
        }
    }

    private void sendAuthenticated(
            HttpSenderContextParos ctx,
            HttpMessage msg,
            HttpMethodParams params,
            ResponseBodyConsumer responseBodyConsumer)
            throws IOException {
        // Modify the request message if a 'Requesting User' has been set
        User forceUser = ctx.getUser(msg);
        if (forceUser != null) {
            if (ctx.getInitiator() == HttpSender.AUTHENTICATION_POLL_INITIATOR) {
                forceUser.processMessageToMatchAuthenticatedSession(msg);
            } else if (ctx.getInitiator() != HttpSender.AUTHENTICATION_INITIATOR) {
                forceUser.processMessageToMatchUser(msg);
            }
        }

        log.debug("Sending message to: {}", msg.getRequestHeader().getURI());
        // Send the message
        send(ctx, msg, params, responseBodyConsumer);

        // If there's a 'Requesting User', make sure the response corresponds to an authenticated
        // session and, if not, attempt a reauthentication and try again
        if (ctx.getInitiator() != HttpSender.AUTHENTICATION_INITIATOR
                && ctx.getInitiator() != HttpSender.AUTHENTICATION_POLL_INITIATOR
                && forceUser != null
                && !msg.getRequestHeader().isImage()
                && !forceUser.isAuthenticated(msg)) {
            log.debug(
                    "First try to send authenticated message failed for {}. Authenticating and trying again...",
                    msg.getRequestHeader().getURI());
            forceUser.queueAuthentication(msg);
            forceUser.processMessageToMatchUser(msg);
            send(ctx, msg, params, responseBodyConsumer);
        } else log.debug("SUCCESSFUL");
    }

    private void send(
            HttpSenderContextParos ctx,
            HttpMessage msg,
            HttpMethodParams params,
            ResponseBodyConsumer responseBodyConsumer)
            throws IOException {
        HttpMethod method = null;
        HttpResponseHeader resHeader = null;

        try {
            method = runMethod(ctx, msg, params);
            // successfully executed;
            resHeader = HttpMethodHelper.getHttpResponseHeader(method);
            resHeader.setHeader(HttpHeader.TRANSFER_ENCODING, null);
            msg.setResponseHeader(resHeader);

            responseBodyConsumer.accept(msg, method);
            msg.setResponseFromTargetHost(true);

            // ZAP: set method to retrieve upgraded channel later
            if (method instanceof org.zaproxy.zap.ZapGetMethod) {
                msg.setUserObject(method);
            }
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
        }
    }

    private HttpMethod runMethod(
            HttpSenderContextParos ctx, HttpMessage msg, HttpMethodParams params)
            throws IOException {
        HttpMethod method = null;
        // no more retry
        method = helper.createRequestMethod(msg.getRequestHeader(), msg.getRequestBody(), params);
        method.setFollowRedirects(false);

        HttpState state = null;
        User forceUser = ctx.getUser(msg);
        if (forceUser != null) {
            state = forceUser.getCorrespondingHttpState();
        }
        executeMethodImpl(ctx, method, state);

        HttpMethodHelper.updateHttpRequestHeaderSent(msg.getRequestHeader(), method);

        return method;
    }

    @Override
    public void addListener(HttpSenderListener listener) {
        Objects.requireNonNull(listener);
        listeners.add(listener);
        Collections.sort(listeners, LISTENERS_COMPARATOR);
    }

    @Override
    public void removeListener(HttpSenderListener listener) {
        Objects.requireNonNull(listener);
        listeners.remove(listener);
    }

    private void sendAndReceive(
            HttpSenderContextParos ctx,
            HttpMessage message,
            HttpRequestConfig requestConfig,
            ResponseBodyConsumer responseBodyConsumer)
            throws IOException {
        if (message == null) {
            throw new IllegalArgumentException("Parameter message must not be null.");
        }
        if (requestConfig == null) {
            throw new IllegalArgumentException("Parameter requestConfig must not be null.");
        }

        sendAndReceiveImpl(ctx, message, requestConfig, responseBodyConsumer);

        if (requestConfig.isFollowRedirects()) {
            followRedirections(ctx, message, requestConfig, responseBodyConsumer);
        }
    }

    /**
     * Helper method that sends the request of the given HTTP {@code message} with the given
     * configurations.
     *
     * <p>No redirections are followed (see {@link #followRedirections(HttpMessage,
     * HttpRequestConfig)}).
     *
     * @param message the message that will be sent.
     * @param requestConfig the request configurations.
     * @throws IOException if an error occurred while sending the message or following the
     *     redirections.
     */
    private void sendAndReceiveImpl(
            HttpSenderContextParos ctx,
            HttpMessage message,
            HttpRequestConfig requestConfig,
            ResponseBodyConsumer responseBodyConsumer)
            throws IOException {
        log.debug(
                "Sending {} {}",
                message.getRequestHeader().getMethod(),
                message.getRequestHeader().getURI());
        message.setTimeSentMillis(System.currentTimeMillis());

        try {
            if (requestConfig.isNotifyListeners()) {
                notifyRequestListeners(ctx, message);
            }

            HttpMethodParams params = null;
            if (requestConfig.getSoTimeout() != HttpRequestConfig.NO_VALUE_SET) {
                params = new HttpMethodParams();
                params.setSoTimeout(requestConfig.getSoTimeout());
            }
            sendAuthenticated(ctx, message, params, responseBodyConsumer);

        } finally {
            message.setTimeElapsedMillis(
                    (int) (System.currentTimeMillis() - message.getTimeSentMillis()));

            log.debug(
                    "Received response after {}ms for {} {}",
                    message.getTimeElapsedMillis(),
                    message.getRequestHeader().getMethod(),
                    message.getRequestHeader().getURI());

            if (requestConfig.isNotifyListeners()) {
                notifyResponseListeners(ctx, message);
            }
        }
    }

    /**
     * Follows redirections using the response of the given {@code message}. The {@code validator}
     * in the given request configuration will be called for each redirection received. After the
     * call to this method the given {@code message} will have the contents of the last response
     * received (possibly the response of a redirection).
     *
     * <p>The validator is notified of each message sent and received (first message and
     * redirections followed, if any).
     *
     * @param message the message that will be sent, must not be {@code null}
     * @param requestConfig the request configuration that contains the validator responsible for
     *     validation of redirections, must not be {@code null}.
     * @throws IOException if an error occurred while sending the message or following the
     *     redirections
     * @see #isRedirectionNeeded(int)
     */
    private void followRedirections(
            HttpSenderContextParos ctx,
            HttpMessage message,
            HttpRequestConfig requestConfig,
            ResponseBodyConsumer responseBodyConsumer)
            throws IOException {
        HttpRedirectionValidator validator = requestConfig.getRedirectionValidator();
        validator.notifyMessageReceived(message);

        User requestingUser = ctx.getUser(message);
        HttpMessage redirectMessage = message;
        int maxRedirections =
                ctx.getHttpClient()
                        .getParams()
                        .getIntParameter(HttpClientParams.MAX_REDIRECTS, 100);
        for (int i = 0;
                i < maxRedirections
                        && isRedirectionNeeded(redirectMessage.getResponseHeader().getStatusCode());
                i++) {
            URI newLocation = extractRedirectLocation(redirectMessage);
            if (newLocation == null || !validator.isValid(newLocation)) {
                return;
            }

            redirectMessage = redirectMessage.cloneAll();
            redirectMessage.setRequestingUser(requestingUser);
            redirectMessage.getRequestHeader().setURI(newLocation);

            if (isRequestRewriteNeeded(redirectMessage)) {
                redirectMessage.getRequestHeader().setMethod(HttpRequestHeader.GET);
                redirectMessage.getRequestHeader().setHeader(HttpHeader.CONTENT_TYPE, null);
                redirectMessage.getRequestHeader().setHeader(HttpHeader.CONTENT_LENGTH, null);
                redirectMessage.setRequestBody("");
            }

            sendAndReceiveImpl(ctx, redirectMessage, requestConfig, responseBodyConsumer);
            validator.notifyMessageReceived(redirectMessage);

            // Update the response of the (original) message
            message.setResponseHeader(redirectMessage.getResponseHeader());
            message.setResponseBody(redirectMessage.getResponseBody());
        }
    }

    /**
     * Tells whether or not a redirection is needed based on the given status code.
     *
     * <p>A redirection is needed if the status code is 301, 302, 303, 307 or 308.
     *
     * @param statusCode the status code that will be checked
     * @return {@code true} if a redirection is needed, {@code false} otherwise
     * @see #isRequestRewriteNeeded(HttpMessage)
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
     * Tells whether or not the (original) request of the redirection, should be rewritten.
     *
     * <p>For status codes 301 and 302 the request should be changed from POST to GET when following
     * redirections, for status code 303 it should be changed to GET for all methods except GET/HEAD
     * (mimicking the behaviour of browsers, which per <a
     * href="https://tools.ietf.org/html/rfc7231#section-6.4">RFC 7231, Section 6.4</a> is now OK).
     *
     * @param message the message with the redirection.
     * @return {@code true} if the request should be rewritten, {@code false} otherwise
     * @see #isRedirectionNeeded(int)
     */
    private static boolean isRequestRewriteNeeded(HttpMessage message) {
        int statusCode = message.getResponseHeader().getStatusCode();
        String method = message.getRequestHeader().getMethod();
        if (statusCode == 301 || statusCode == 302) {
            return HttpRequestHeader.POST.equalsIgnoreCase(method);
        }
        return statusCode == 303
                && !(HttpRequestHeader.GET.equalsIgnoreCase(method)
                        || HttpRequestHeader.HEAD.equalsIgnoreCase(method));
    }

    /**
     * Extracts a {@code URI} from the {@code Location} header of the given HTTP {@code message}.
     *
     * <p>If there's no {@code Location} header this method returns {@code null}.
     *
     * @param message the HTTP message that will processed
     * @return the {@code URI} created from the value of the {@code Location} header, might be
     *     {@code null}
     * @throws InvalidRedirectLocationException if the value of {@code Location} header is not a
     *     valid {@code URI}
     */
    private static URI extractRedirectLocation(HttpMessage message)
            throws InvalidRedirectLocationException {
        String location = message.getResponseHeader().getHeader(HttpHeader.LOCATION);
        if (location == null) {
            log.debug("No Location header found: {}", message.getResponseHeader());
            return null;
        }

        try {
            return new URI(message.getRequestHeader().getURI(), location, true);
        } catch (URIException ex) {
            try {
                // Handle redirect URLs that are unencoded
                return new URI(message.getRequestHeader().getURI(), location, false);
            } catch (URIException e) {
                throw new InvalidRedirectLocationException(
                        "Invalid redirect location: " + location, location, ex);
            }
        }
    }

    private interface ResponseBodyConsumer {

        void accept(HttpMessage message, HttpMethod method) throws IOException;
    }

    /**
     * A {@link ProtocolSocketFactory} for plain sockets.
     *
     * <p>Remote hostnames are not resolved if {@link HttpMethodDirector#PARAM_RESOLVE_HOSTNAME} is
     * {@code false}.
     */
    private static class ProtocolSocketFactoryImpl implements ProtocolSocketFactory {

        @Override
        public Socket createSocket(
                String host,
                int port,
                InetAddress localAddress,
                int localPort,
                HttpConnectionParams params)
                throws IOException {
            if (params == null) {
                throw new IllegalArgumentException("Parameters may not be null");
            }
            Socket socket = SocketFactory.getDefault().createSocket();
            socket.bind(new InetSocketAddress(localAddress, localPort));
            SocketAddress remoteAddress;
            if (params.getBooleanParameter(
                    org.apache.commons.httpclient.HttpMethodDirector.PARAM_RESOLVE_HOSTNAME,
                    true)) {
                remoteAddress = new InetSocketAddress(host, port);
            } else {
                remoteAddress = InetSocketAddress.createUnresolved(host, port);
            }
            socket.connect(remoteAddress, params.getConnectionTimeout());
            return socket;
        }

        @Override
        public Socket createSocket(String host, int port, InetAddress localAddress, int localPort)
                throws IOException {
            throw new UnsupportedOperationException(
                    "Method not supported, not required/called by Commons HttpClient library (version >= 3.0).");
        }

        @Override
        public Socket createSocket(String host, int port) throws IOException {
            throw new UnsupportedOperationException(
                    "Method not supported, not required/called by Commons HttpClient library (version >= 3.0).");
        }
    }
}
