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

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpSender;
import org.zaproxy.zap.extension.script.ScriptsCache.Configuration;
import org.zaproxy.zap.network.HttpSenderListener;

class HttpSenderScriptListener implements HttpSenderListener {

    private final ScriptsCache<HttpSenderScript> scriptsCache;

    public HttpSenderScriptListener(ExtensionScript extension) {
        this.scriptsCache =
                extension.createScriptsCache(
                        Configuration.<HttpSenderScript>builder()
                                .setScriptType(ExtensionScript.TYPE_HTTP_SENDER)
                                .setTargetInterface(HttpSenderScript.class)
                                .setInterfaceErrorMessageProvider(
                                        sw ->
                                                Constant.messages.getString(
                                                        "script.interface.httpsender.error"))
                                .build());
    }

    @Override
    public int getListenerOrder() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void onHttpRequestSend(HttpMessage msg, int initiator, HttpSender sender) {
        scriptsCache.refresh();

        HttpSenderScriptHelper scriptHelper = new HttpSenderScriptHelper(sender);
        scriptsCache.execute(script -> script.sendingRequest(msg, initiator, scriptHelper));
    }

    @Override
    public void onHttpResponseReceive(HttpMessage msg, int initiator, HttpSender sender) {
        HttpSenderScriptHelper scriptHelper = new HttpSenderScriptHelper(sender);
        scriptsCache.execute(script -> script.responseReceived(msg, initiator, scriptHelper));
    }
}
