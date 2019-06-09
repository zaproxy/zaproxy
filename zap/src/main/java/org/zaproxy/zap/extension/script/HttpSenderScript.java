/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2015 The ZAP Development Team
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
 * A script that is executed for each {@link HttpMessage HTTP message} sent by/through ZAP.
 *
 * <p>The IDs of the initiators are defined in the {@link org.parosproxy.paros.network.HttpSender
 * HttpSender} class (for example, {@link org.parosproxy.paros.network.HttpSender#SPIDER_INITIATOR
 * SPIDER_INITIATOR}).
 *
 * <p><strong>Note:</strong> In the presence of more than one script or, internally, a {@link
 * org.zaproxy.zap.network.HttpSenderListener HttpSenderListener} there are <i>no</i> guarantees
 * that the (final) request/response is exactly the same as the one crafted by this script, as the
 * following scripts/listeners may modify it.
 *
 * @since 2.4.1
 */
public interface HttpSenderScript {

    /**
     * Called before sending the request to the server.
     *
     * <p>Only the request should be modified.
     *
     * @param msg the HTTP message (request) being sent.
     * @param initiator the ID of the initiator of the HTTP message being sent.
     * @param helper the helper class that allows to send other HTTP messages.
     * @throws ScriptException if an error occurred while executing the script.
     */
    void sendingRequest(HttpMessage msg, int initiator, HttpSenderScriptHelper helper)
            throws ScriptException;

    /**
     * Called after receiving the response from the server (if any).
     *
     * @param msg the HTTP message (response) received.
     * @param initiator the ID of the initiator of the HTTP message sent.
     * @param helper the helper class that allows to send other HTTP messages.
     * @throws ScriptException if an error occurred while executing the script.
     */
    void responseReceived(HttpMessage msg, int initiator, HttpSenderScriptHelper helper)
            throws ScriptException;
}
