/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2017 The ZAP Development Team
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
package org.zaproxy.zap.extension.callback;

import java.awt.EventQueue;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.httpclient.URIException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.proxy.OverrideMessageProxyListener;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.OptionsChangedListener;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpStatusCode;
import org.zaproxy.zap.extension.help.ExtensionHelp;

/** @deprecated (2.11.0) Superseded by the OAST add-on. */
@Deprecated
public class ExtensionCallback extends ExtensionAdaptor
        implements OptionsChangedListener, SessionChangedListener {

    private static final String TEST_PREFIX = "ZapTest";
    private static final String NAME = "ExtensionCallback";

    private org.parosproxy.paros.core.proxy.ProxyServer proxyServer;
    private CallbackParam callbackParam;
    private OptionsCallbackPanel optionsCallbackPanel;

    private Map<String, CallbackImplementor> callbacks = new HashMap<>();
    private int actualPort;
    private String currentConfigLocalAddress;
    private int currentConfigPort;

    private static final Logger LOGGER = LogManager.getLogger(ExtensionCallback.class);
    private org.zaproxy.zap.extension.callback.ui.CallbackPanel callbackPanel;

    public ExtensionCallback() {
        proxyServer = new org.parosproxy.paros.core.proxy.ProxyServer("ZAP-CallbackServer");
        proxyServer.addOverrideMessageProxyListener(new CallbackProxyListener());
    }

    @Override
    public boolean supportsDb(String type) {
        return true;
    }

    @Override
    public String getUIName() {
        return Constant.messages.getString("callback.name");
    }

    @Override
    public void hook(ExtensionHook extensionHook) {
        super.hook(extensionHook);
        extensionHook.addOptionsParamSet(getCallbackParam());
        extensionHook.addOptionsChangedListener(this);
        extensionHook.addSessionListener(this);
        if (hasView()) {
            extensionHook.getHookView().addStatusPanel(getCallbackPanel());
            extensionHook.getHookView().addOptionPanel(getOptionsCallbackPanel());
            ExtensionHelp.enableHelpKey(getCallbackPanel(), "ui.tabs.callbacks");
        }
    }

    @Override
    public void optionsLoaded() {
        proxyServer.setConnectionParam(getModel().getOptionsParam().getConnectionParam());
        currentConfigLocalAddress = this.getCallbackParam().getLocalAddress();
        currentConfigPort = this.getCallbackParam().getPort();
    }

    @Override
    public void postInit() {
        this.restartServer(this.getCallbackParam().getPort());
    }

    private void restartServer(int port) {
        // this will close the previous listener (if there was one)
        actualPort = proxyServer.startServer(this.getCallbackParam().getLocalAddress(), port, true);
        LOGGER.info(
                "Started callback server on {}:{}",
                this.getCallbackParam().getLocalAddress(),
                actualPort);
    }

    public String getCallbackAddress() {
        String addr = this.getCallbackParam().getRemoteAddress();
        boolean ipv6 = addr.contains(":");
        String hostname = ipv6 ? "[" + addr + "]" : addr;

        boolean isSecure = this.getCallbackParam().isSecure();
        String scheme = isSecure ? "https" : "http";

        return scheme + "://" + hostname + ":" + actualPort + "/";
    }

    private org.zaproxy.zap.extension.callback.ui.CallbackPanel getCallbackPanel() {
        if (callbackPanel == null) {
            callbackPanel = new org.zaproxy.zap.extension.callback.ui.CallbackPanel(this);
        }
        return callbackPanel;
    }

    public String getTestUrl() {
        return getCallbackAddress() + TEST_PREFIX;
    }

    protected int getPort() {
        return actualPort;
    }

    public void registerCallbackImplementor(CallbackImplementor impl) {
        for (String prefix : impl.getCallbackPrefixes()) {
            LOGGER.debug("Registering callback prefix: {}", prefix);
            if (this.callbacks.containsKey(prefix)) {
                LOGGER.error("Duplicate callback prefix: {}", prefix);
            }
            this.callbacks.put("/" + prefix, impl);
        }
    }

    public void removeCallbackImplementor(CallbackImplementor impl) {
        for (String shortcut : impl.getCallbackPrefixes()) {
            String key = "/" + shortcut;
            if (this.callbacks.containsKey(key)) {
                LOGGER.debug("Removing registered callback prefix: {}", shortcut);
                this.callbacks.remove(key);
            }
        }
    }

    private CallbackParam getCallbackParam() {
        if (this.callbackParam == null) {
            this.callbackParam = new CallbackParam();
        }
        return this.callbackParam;
    }

    private OptionsCallbackPanel getOptionsCallbackPanel() {
        if (optionsCallbackPanel == null) {
            optionsCallbackPanel = new OptionsCallbackPanel(this);
        }
        return optionsCallbackPanel;
    }

    @Override
    public String getAuthor() {
        return Constant.ZAP_TEAM;
    }

    @Override
    public String getDescription() {
        return Constant.messages.getString("callback.desc");
    }

    @Override
    public void optionsChanged(OptionsParam optionsParam) {
        if (!currentConfigLocalAddress.equals(this.getCallbackParam().getLocalAddress())
                || currentConfigPort != this.getCallbackParam().getPort()) {
            // Somethings changed, reuse the port if its still a random one
            int port = actualPort;
            if (currentConfigPort != this.getCallbackParam().getPort()) {
                port = this.getCallbackParam().getPort();
            }
            this.restartServer(port);

            // Save the new ones for next time
            currentConfigLocalAddress = this.getCallbackParam().getLocalAddress();
            currentConfigPort = this.getCallbackParam().getPort();
        }
    }

    @Override
    public void sessionChanged(Session session) {
        invokeIfRequiredAndViewIsInitialised(
                new Runnable() {
                    @Override
                    public void run() {
                        sessionChangedEventHandler(session);
                    }
                });
    }

    private void sessionChangedEventHandler(Session session) {
        getCallbackPanel().clearCallbackRequests();
        addCallbacksFromDatabaseIntoCallbackPanel(session);
    }

    private void addCallbacksFromDatabaseIntoCallbackPanel(Session session) {
        if (session == null) {
            return;
        }

        try {
            List<Integer> historyIds =
                    getModel()
                            .getDb()
                            .getTableHistory()
                            .getHistoryIdsOfHistType(
                                    session.getSessionId(), HistoryReference.TYPE_CALLBACK);

            for (int historyId : historyIds) {
                HistoryReference historyReference = new HistoryReference(historyId);
                org.zaproxy.zap.extension.callback.ui.CallbackRequest request =
                        org.zaproxy.zap.extension.callback.ui.CallbackRequest.create(
                                historyReference);
                getCallbackPanel().addCallbackRequest(request);
            }
        } catch (DatabaseException | HttpMalformedHeaderException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void deleteCallbacks() {
        deleteCallbacksFromDatabase();
        invokeIfRequiredAndViewIsInitialised(
                new Runnable() {
                    @Override
                    public void run() {
                        getCallbackPanel().clearCallbackRequests();
                    }
                });
    }

    private void deleteCallbacksFromDatabase() {
        try {
            getModel()
                    .getDb()
                    .getTableHistory()
                    .deleteHistoryType(
                            getModel().getSession().getSessionId(), HistoryReference.TYPE_CALLBACK);
        } catch (DatabaseException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void invokeIfRequiredAndViewIsInitialised(Runnable runnable) {
        if (hasView()) {
            if (!EventQueue.isDispatchThread()) {
                try {
                    EventQueue.invokeAndWait(runnable);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
                return;
            }
            runnable.run();
        }
    }

    @Override
    public void sessionAboutToChange(Session session) {}

    @Override
    public void sessionScopeChanged(Session session) {}

    @Override
    public void sessionModeChanged(Control.Mode mode) {}

    private class CallbackProxyListener implements OverrideMessageProxyListener {

        @Override
        public int getArrangeableListenerOrder() {
            return 0;
        }

        @Override
        public boolean onHttpRequestSend(HttpMessage msg) {
            try {
                msg.setTimeSentMillis(new Date().getTime());
                String url = msg.getRequestHeader().getURI().toString();
                String path = msg.getRequestHeader().getURI().getPath();
                LOGGER.debug(
                        "Callback received for URL : {} path : {} from {}",
                        url,
                        path,
                        msg.getRequestHeader().getSenderAddress());

                msg.setResponseHeader(HttpHeader.HTTP11 + " " + HttpStatusCode.OK);

                if (path.startsWith("/" + TEST_PREFIX)) {
                    String str =
                            Constant.messages.getString(
                                    "callback.test.msg",
                                    url,
                                    msg.getRequestHeader().getSenderAddress().toString());
                    if (hasView()) {
                        getView().getOutputPanel().appendAsync(str + "\n");
                    }
                    LOGGER.info(str);
                    callbackReceived(
                            Constant.messages.getString("callback.handler.test.name"), msg);
                    return true;
                } else if (path.startsWith("/favicon.ico")) {
                    // Just ignore - its automatically requested by browsers
                    // e.g. when trying the test URL
                    return true;
                }

                for (Entry<String, CallbackImplementor> callback : callbacks.entrySet()) {
                    if (path.startsWith(callback.getKey())) {
                        // Copy the message so that CallbackImplementors cant
                        // return anything to the sender
                        CallbackImplementor implementor = callback.getValue();
                        implementor.handleCallBack(msg.cloneAll());
                        callbackReceived(implementor.getClass().getSimpleName(), msg);
                        return true;
                    }
                }

                callbackReceived(Constant.messages.getString("callback.handler.none.name"), msg);
                LOGGER.error(
                        "No callback handler for URL : {} from {}",
                        url,
                        msg.getRequestHeader().getSenderAddress());
            } catch (URIException | HttpMalformedHeaderException e) {
                LOGGER.error(e.getMessage(), e);
            }
            return true;
        }

        @Override
        public boolean onHttpResponseReceived(HttpMessage msg) {
            return true;
        }
    }

    private void callbackReceived(String handler, HttpMessage httpMessage) {
        invokeIfRequiredAndViewIsInitialised(
                new Runnable() {
                    @Override
                    public void run() {
                        callbackReceivedHandler(handler, httpMessage);
                    }
                });
    }

    private void callbackReceivedHandler(String handler, HttpMessage httpMessage) {
        try {
            org.zaproxy.zap.extension.callback.ui.CallbackRequest request =
                    org.zaproxy.zap.extension.callback.ui.CallbackRequest.create(
                            handler, httpMessage);
            getCallbackPanel().addCallbackRequest(request);
        } catch (HttpMalformedHeaderException | DatabaseException e) {
            LOGGER.warn("Failed to persist received callback:", e);
        }
    }
}
