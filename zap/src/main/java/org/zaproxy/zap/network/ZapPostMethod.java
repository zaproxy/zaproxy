/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2013 ZAP development team
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
package org.zaproxy.zap.network;

import java.io.IOException;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.methods.PostMethod;

/**
 * An HTTP POST method implementation that ignores malformed HTTP response header lines.
 * 
 * @see PostMethod
 */
public class ZapPostMethod extends PostMethod {

    public ZapPostMethod() {
        super();
    }

    public ZapPostMethod(String uri) {
        super(uri);
    }

    /**
     * {@inheritDoc}
     * 
     * <strong>Note:</strong> Malformed HTTP header lines are ignored (instead of throwing an exception).
     */
    /*
     * Implementation copied from HttpMethodBase#readResponseHeaders(HttpState, HttpConnection) but changed to use a custom
     * header parser (ZapHttpParser#parseHeaders(InputStream, String)).
     */
    @Override
    protected void readResponseHeaders(HttpState state, HttpConnection conn) throws IOException, HttpException {
        getResponseHeaderGroup().clear();

        Header[] headers = ZapHttpParser.parseHeaders(conn.getResponseInputStream(), getParams().getHttpElementCharset());
        // Wire logging moved to HttpParser
        getResponseHeaderGroup().setHeaders(headers);
    }
}
