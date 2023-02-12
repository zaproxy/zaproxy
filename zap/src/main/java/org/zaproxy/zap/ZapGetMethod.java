/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Locale;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Do not ignore HTTP status code of 101 and keep {@link Socket} &amp; {@link InputStream} open, as
 * 101 states a protocol switch.
 *
 * <p>Is essential for the WebSockets extension.
 *
 * <p>Malformed HTTP response header lines are ignored.
 *
 * @deprecated (2.12.0) Implementation details, do not use.
 */
@Deprecated
public class ZapGetMethod extends EntityEnclosingMethod {
    private static final Log LOG = LogFactory.getLog(ZapGetMethod.class);

    /**
     * If we have got an <em>Connection: Upgrade</em>, this will be set to the current connection.
     */
    private Socket upgradedSocket;

    /**
     * If we have got an <em>Connection: Upgrade</em>, this will be set to the input stream of the
     * upgraded socket.
     */
    private InputStream inputStream;

    private boolean followRedirects = true;

    /** Constructor. */
    public ZapGetMethod() {
        super();
    }

    /**
     * Constructor.
     *
     * @param uri the request URI
     */
    public ZapGetMethod(String uri) {
        super(uri);
    }

    @Override
    public String getName() {
        return "GET";
    }

    @Override
    public void setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
    }

    @Override
    public boolean getFollowRedirects() {
        return followRedirects;
    }

    @Override
    protected void addContentLengthRequestHeader(
            HttpState state, org.apache.commons.httpclient.HttpConnection conn) throws IOException {
        if (getRequestContentLength() == 0) {
            // Don't add the header with 0 length, not everything accepts it.
            return;
        }
        super.addContentLengthRequestHeader(state, conn);
    }

    /**
     * Allow response code 101, that is Switching Protocols.
     *
     * @see GetMethod#readResponse(HttpState, HttpConnection)
     */
    @Override
    protected void readResponse(HttpState state, org.apache.commons.httpclient.HttpConnection conn)
            throws IOException {
        LOG.trace("enter HttpMethodBase.readResponse(HttpState, HttpConnection)");

        boolean isUpgrade = false;

        while (getStatusLine() == null) {
            readStatusLine(state, conn);
            processStatusLine(state, conn);
            readResponseHeaders(state, conn);
            processResponseHeaders(state, conn);

            int status = this.statusLine.getStatusCode();
            if (status == 101) {
                LOG.debug(
                        "Retrieved HTTP status code '101 Switching Protocols'. Keep connection open!");

                // This means the requester has asked the server to switch protocols
                // and the server is acknowledging that it will do so
                // e.g.: upgrade to websocket

                if (conn instanceof ZapHttpConnection) {
                    isUpgrade = true;
                    // avoid connection release of HttpClient library
                    conn.setHttpConnectionManager(null);
                }
            } else if ((status >= 100) && (status < 200)) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Discarding unexpected response: " + this.statusLine.toString());
                }
                this.statusLine = null;
            }
        }

        // get socket and input stream out of HttpClient
        if (conn instanceof ZapHttpConnection) {
            ZapHttpConnection zapConn = (ZapHttpConnection) conn;
            upgradedSocket = zapConn.getSocket();
            inputStream = zapConn.getResponseInputStream();
        }

        if (!isUpgrade) {
            // read & process rest of response
            // only if connection should not be kept
            readResponseBody(state, conn);
            processResponseBody(state, conn);
        }
    }

    /**
     * Set the upgraded socket
     *
     * @param upgradedSocket
     */
    public void setUpgradedSocket(Socket upgradedSocket) {
        this.upgradedSocket = upgradedSocket;
    }

    /**
     * Set the upgraded input stream
     *
     * @param inputStream
     */
    public void setUpgradedInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    /**
     * If this response included the header <em>Connection: Upgrade</em>, then this method provides
     * the corresponding connection.
     *
     * @return Upgraded Socket or null
     */
    public Socket getUpgradedConnection() {
        return upgradedSocket;
    }

    /**
     * If this response included the header <em>Connection: Upgrade</em>, then this method provides
     * the corresponding input stream.
     *
     * <p>It might happen, that WebSocket frames are sent directly after the WebSocket handshake
     * response. In this case the frames are buffered in that stream.
     *
     * @return Input stream from response or null
     */
    public InputStream getUpgradedInputStream() {
        return inputStream;
    }

    /** Avoid releasing connection on event stream that is used in Server-Sent Events. */
    @Override
    public void releaseConnection() {
        Header header = getResponseHeader("content-type");
        if (header != null) {
            String contentTypeHeader = header.getValue();
            if (contentTypeHeader != null
                    && contentTypeHeader.toLowerCase(Locale.ROOT).contains("text/event-stream")) {
                return;
            }
        }

        super.releaseConnection();
    }

    /**
     * {@inheritDoc}
     *
     * <p><strong>Note:</strong> Malformed HTTP header lines are ignored (instead of throwing an
     * exception).
     */
    /*
     * Implementation copied from HttpMethodBase#readResponseHeaders(HttpState, HttpConnection) but changed to use a custom
     * header parser (ZapHttpParser#parseHeaders(InputStream, String)).
     */
    @Override
    protected void readResponseHeaders(
            HttpState state, org.apache.commons.httpclient.HttpConnection conn) throws IOException {
        getResponseHeaderGroup().clear();

        Header[] headers =
                org.zaproxy.zap.network.ZapHttpParser.parseHeaders(
                        conn.getResponseInputStream(), getParams().getHttpElementCharset());
        // Wire logging moved to HttpParser
        getResponseHeaderGroup().setHeaders(headers);
    }
}
