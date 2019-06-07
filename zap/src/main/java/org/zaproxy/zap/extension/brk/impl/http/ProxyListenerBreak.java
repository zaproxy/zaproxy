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
package org.zaproxy.zap.extension.brk.impl.http;

import org.parosproxy.paros.core.proxy.ProxyListener;
import org.parosproxy.paros.extension.history.ProxyListenerLog;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.brk.ExtensionBreak;

public class ProxyListenerBreak implements ProxyListener {

    // Should be the last one before the listener that saves the HttpMessage to
    // the DB, this way the HttpMessage will be correctly shown to the user (to
    // edit it) because it could have been changed by other ProxyListener.
    public static final int PROXY_LISTENER_ORDER = ProxyListenerLog.PROXY_LISTENER_ORDER - 1;

    private Model model = null;
    private ExtensionBreak extension = null;

    public ProxyListenerBreak(Model model, ExtensionBreak extension) {
        this.model = model;
        this.extension = extension;
    }

    @Override
    public int getArrangeableListenerOrder() {
        return PROXY_LISTENER_ORDER;
    }

    @Override
    public boolean onHttpRequestSend(HttpMessage msg) {
        if (isSkipImage(msg.getRequestHeader())) {
            return true;
        }

        if (extension.isInScopeOnly()) {
            // Cant use msg,isInScope() as it wont have been initialised
            Session session = Model.getSingleton().getSession();
            if (!session.isInScope(msg.getRequestHeader().getURI().toString())) {
                return true;
            }
        }

        if (extension.messageReceivedFromClient(msg)) {
            return true;
        }

        return false;
    }

    @Override
    public boolean onHttpResponseReceive(HttpMessage msg) {
        if (isSkipImage(msg.getRequestHeader()) || isSkipImage(msg.getResponseHeader())) {
            return true;
        }

        if (extension.isInScopeOnly()) {
            // Cant use msg,isInScope() as it wont have been initialised
            Session session = Model.getSingleton().getSession();
            if (!session.isInScope(msg.getRequestHeader().getURI().toString())) {
                return true;
            }
        }

        if (extension.messageReceivedFromServer(msg)) {
            return true;
        }

        return false;
    }

    private boolean isSkipImage(HttpHeader header) {
        if (header.isImage() && !model.getOptionsParam().getViewParam().isProcessImages()) {
            return true;
        }

        return false;
    }
}
