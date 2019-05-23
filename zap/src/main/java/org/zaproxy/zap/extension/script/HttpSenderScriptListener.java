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

import org.apache.log4j.Logger;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpSender;
import org.zaproxy.zap.network.HttpSenderListener;

class HttpSenderScriptListener implements HttpSenderListener {

    private static final Logger logger = Logger.getLogger(HttpSenderScriptListener.class);

    private final ExtensionScript extension;

    public HttpSenderScriptListener(ExtensionScript extension) {
        this.extension = extension;
    }

    @Override
    public int getListenerOrder() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void onHttpRequestSend(HttpMessage msg, int initiator, HttpSender sender) {
        for (ScriptWrapper script : extension.getScripts(ExtensionScript.TYPE_HTTP_SENDER)) {
            if (script.isEnabled()) {
                try {
                    extension.invokeSenderScript(script, msg, initiator, sender, true);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public void onHttpResponseReceive(HttpMessage msg, int initiator, HttpSender sender) {
        for (ScriptWrapper script : extension.getScripts(ExtensionScript.TYPE_HTTP_SENDER)) {
            if (script.isEnabled()) {
                try {
                    extension.invokeSenderScript(script, msg, initiator, sender, false);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

}
