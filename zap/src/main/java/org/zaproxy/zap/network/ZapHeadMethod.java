/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2013 The ZAP Development Team
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
package org.zaproxy.zap.network;

import java.io.IOException;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.ProtocolException;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An HTTP HEAD method implementation that ignores malformed HTTP response header lines.
 *
 * @see HeadMethod
 * @deprecated (2.12.0) Implementation details, do not use.
 */
@Deprecated
public class ZapHeadMethod extends EntityEnclosingMethod {

    /** Log object for this class. */
    private static final Log LOG = LogFactory.getLog(HeadMethod.class);

    public ZapHeadMethod() {
        super();
    }

    public ZapHeadMethod(String uri) {
        super(uri);
    }

    @Override
    public String getName() {
        return "HEAD";
    }

    /**
     * Overrides {@code HttpMethodBase} method to <i>not</i> read a response body, despite the
     * presence of a {@code Content-Length} or {@code Transfer-Encoding} header.
     *
     * @param state the {@link HttpState state} information associated with this method
     * @param conn the {@code HttpConnection connection} used to execute this HTTP method
     * @throws IOException if an I/O (transport) error occurs. Some transport exceptions can be
     *     recovered from.
     * @see #readResponse
     * @see #processResponseBody
     * @since 2.0
     */
    // Implementation copied from HeadMethod.
    @Override
    protected void readResponseBody(
            HttpState state, org.apache.commons.httpclient.HttpConnection conn) throws IOException {
        LOG.trace("enter HeadMethod.readResponseBody(HttpState, HttpConnection)");

        int bodyCheckTimeout =
                getParams().getIntParameter(HttpMethodParams.HEAD_BODY_CHECK_TIMEOUT, -1);

        if (bodyCheckTimeout < 0) {
            responseBodyConsumed();
        } else {
            LOG.debug(
                    "Check for non-compliant response body. Timeout in "
                            + bodyCheckTimeout
                            + " ms");
            boolean responseAvailable = false;
            try {
                responseAvailable = conn.isResponseAvailable(bodyCheckTimeout);
            } catch (IOException e) {
                LOG.debug(
                        "An IOException occurred while testing if a response was available,"
                                + " we will assume one is not.",
                        e);
                responseAvailable = false;
            }
            if (responseAvailable) {
                if (getParams().isParameterTrue(HttpMethodParams.REJECT_HEAD_BODY)) {
                    throw new ProtocolException(
                            "Body content may not be sent in response to HTTP HEAD request");
                } else {
                    LOG.warn("Body content returned in response to HTTP HEAD");
                }
                super.readResponseBody(state, conn);
            }
        }
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
                ZapHttpParser.parseHeaders(
                        conn.getResponseInputStream(), getParams().getHttpElementCharset());
        // Wire logging moved to HttpParser
        getResponseHeaderGroup().setHeaders(headers);
    }
}
