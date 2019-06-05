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
package org.zaproxy.zap.extension.script;

import javax.script.ScriptException;
import org.parosproxy.paros.network.HttpMessage;

/**
 * A script that will be executed when a proxied request is ready to be forwarded to the server and
 * when the response is ready to be forwarded to the client.
 *
 * <p><strong>Note:</strong> In the presence of more than one script or listener (for example,
 * {@link org.parosproxy.paros.core.proxy.ProxyListener ProxyListener} and {@link
 * org.zaproxy.zap.network.HttpSenderListener HttpSenderListener}) there are <i>no</i> guarantees
 * that the (final) request/response is exactly the same as the one crafted by this script, or, that
 * the request/response is actually going to be forwarded, as the following scripts/listeners may
 * modify it or drop it.
 *
 * @since 2.2.0
 */
public interface ProxyScript {

    /**
     * Called when a request is received from the client.
     *
     * <p>Both the request and response of the HTTP message may be modified. If a response is set
     * the request is <i>not</i> forwarded to the server, thus preventing the request and overriding
     * the response. If the return value is {@code true} the request <i>may be</i> forwarded and the
     * following scripts/listeners will be called, if the value is {@code false} the request is
     * dropped and no more scripts/listeners will be called.
     *
     * @param msg the HTTP message (request) being proxied/sent.
     * @return {@code true} if the request should be forwarded to the server, {@code false}
     *     otherwise.
     * @throws ScriptException if an error occurred while executing the script.
     */
    boolean proxyRequest(HttpMessage msg) throws ScriptException;

    /**
     * Called after receiving the response from a proxied request.
     *
     * <p>If the return value is {@code true} the response <i>may be</i> forwarded and the following
     * scripts/listeners will be called, if the value is {@code false} the response <i>will not</i>
     * be forwarded and no more scripts/listeners will be called.
     *
     * @param msg the HTTP message (response) received (or overridden by a script/listener).
     * @return {@code true} if the response should be forwarded to the client, {@code false}
     *     otherwise.
     * @throws ScriptException if an error occurred while executing the script.
     */
    boolean proxyResponse(HttpMessage msg) throws ScriptException;
}
