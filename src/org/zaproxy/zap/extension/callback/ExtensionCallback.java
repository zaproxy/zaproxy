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
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.zaproxy.zap.extension.callback;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.proxy.OverrideMessageProxyListener;
import org.parosproxy.paros.core.proxy.ProxyServer;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.OptionsChangedListener;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.View;

public class ExtensionCallback extends ExtensionAdaptor implements
        OptionsChangedListener {

    private static final String TEST_PREFIX = "ZapTest";
    private static final String NAME = "ExtensionCallback";

    private ProxyServer proxyServer;
    private CallbackParam callbackParam;
    private OptionsCallbackPanel optionsCallbackPanel;

    private Map<String, CallbackImplementor> callbacks = new HashMap<String, CallbackImplementor>();
    private int actualPort;
    private String currentConfigLocalAddress;
    private int currentConfigPort;

    private static final Logger LOGGER = Logger
            .getLogger(ExtensionCallback.class);

    public ExtensionCallback() {
        proxyServer = new ProxyServer("ZAP-CallbackServer");
        proxyServer
                .addOverrideMessageProxyListener(new CallbackProxyListener());
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
        if (View.isInitialised()) {
            extensionHook.getHookView().addOptionPanel(
                    getOptionsCallbackPanel());
        }
    }

    @Override
    public void optionsLoaded() {
        proxyServer.setConnectionParam(getModel().getOptionsParam()
                .getConnectionParam());
        currentConfigLocalAddress = this.getCallbackParam().getLocalAddress();
        currentConfigPort = this.getCallbackParam().getPort();
    }

    @Override
    public void postInit() {
        this.restartServer(this.getCallbackParam().getPort());
    }

    private void restartServer(int port) {
        // this will close the previous listener (if there was one)
        actualPort = proxyServer.startServer(this.getCallbackParam()
                .getLocalAddress(), port, true);
        LOGGER.info("Started callback server on "
                + this.getCallbackParam().getLocalAddress() + ":" + actualPort);
    }

    public String getCallbackAddress() {
        String addr = this.getCallbackParam().getRemoteAddress();
        if (addr.contains(":")) {
            // Looks like its IPv6
            return "http://[" + addr + "]:" + actualPort + "/";
        }
        // Looks like IPv4
        return "http://" + addr + ":" + actualPort + "/";
    }

    public String getTestUrl() {
        return getCallbackAddress() + TEST_PREFIX;
    }

    protected int getPort() {
        return actualPort;
    }

    public void registerCallbackImplementor(CallbackImplementor impl) {
        for (String prefix : impl.getCallbackPrefixes()) {
            LOGGER.debug("Registering callback prefix: " + prefix);
            if (this.callbacks.containsKey(prefix)) {
                LOGGER.error("Duplicate callback prefix: " + prefix);
            }
            this.callbacks.put("/" + prefix, impl);
        }
    }

    public void removeCallbackImplementor(CallbackImplementor impl) {
        for (String shortcut : impl.getCallbackPrefixes()) {
            String key = "/" + shortcut;
            if (this.callbacks.containsKey(key)) {
                LOGGER.debug("Removing registered callback prefix: " + shortcut);
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
        if (!currentConfigLocalAddress.equals(this.getCallbackParam()
                .getLocalAddress())
                || currentConfigPort != this.getCallbackParam().getPort()) {
            // Somethings changed, reuse the port if its still a random one
            int port = actualPort;
            if (currentConfigPort != this.getCallbackParam().getPort()) {
                port = this.getCallbackParam().getPort();
            }
            this.restartServer(port);

            // Save the new ones for next time
            currentConfigLocalAddress = this.getCallbackParam()
                    .getLocalAddress();
            currentConfigPort = this.getCallbackParam().getPort();
        }
    }

    private class CallbackProxyListener implements OverrideMessageProxyListener {

        @Override
        public int getArrangeableListenerOrder() {
            return 0;
        }

        @Override
        public boolean onHttpRequestSend(HttpMessage msg) {
            try {
                String url = msg.getRequestHeader().getURI().toString();
                String path = msg.getRequestHeader().getURI().getPath();
                LOGGER.debug("Callback received for URL : " + url + " path : "
                        + path + " from "
                        + msg.getRequestHeader().getSenderAddress());

                if (path.startsWith("/" + TEST_PREFIX)) {
                    String str = Constant.messages.getString(
                            "callback.test.msg", url, msg.getRequestHeader()
                                    .getSenderAddress().toString());
                    if (View.isInitialised()) {
                        View.getSingleton().getOutputPanel()
                                .appendAsync(str + "\n");
                    }
                    LOGGER.info(str);
                    return true;
                } else if (path.startsWith("/favicon.ico")) {
                    // Just ignore - its automatically requested by browsers
                    // e.g. when trying the test URL
                    return true;
                }

                for (Entry<String, CallbackImplementor> callback : callbacks
                        .entrySet()) {
                    if (path.startsWith(callback.getKey())) {
                        // Copy the message so that CallbackImplementors cant
                        // return anything to the sender
                        callback.getValue().handleCallBack(msg.cloneAll());
                        return true;
                    }
                }
                LOGGER.error("No callback handler for URL : " + url + " from "
                        + msg.getRequestHeader().getSenderAddress());
            } catch (URIException e) {
                LOGGER.error(e.getMessage(), e);
            }
            return true;
        }

        @Override
        public boolean onHttpResponseReceived(HttpMessage msg) {
            return true;
        }

    }
}
