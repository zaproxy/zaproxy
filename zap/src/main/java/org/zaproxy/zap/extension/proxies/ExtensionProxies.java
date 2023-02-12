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
package org.zaproxy.zap.extension.proxies;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.httpclient.URI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.OptionsChangedListener;
import org.parosproxy.paros.model.OptionsParam;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

/** @deprecated (2.12.0) Use the capabilities provided by the network add-on. */
@Deprecated
public class ExtensionProxies extends ExtensionAdaptor implements OptionsChangedListener {

    public static final String NAME = "ExtensionProxies";
    public static final String ZAP_PROXY_THREAD_PREFIX = "ZAP-";

    private ProxiesParam proxiesParam = null;
    private OptionsProxiesPanel optionsProxiesPanel = null;
    private Map<String, org.parosproxy.paros.core.proxy.ProxyServer> proxyServers = new HashMap<>();

    private static Logger log = LogManager.getLogger(ExtensionProxies.class);

    public ExtensionProxies() {
        super();
        initialize();
    }

    private void initialize() {
        this.setName(NAME);
        this.setOrder(310);
    }

    @Override
    public boolean supportsDb(String type) {
        return true;
    }

    @Override
    public String getUIName() {
        return Constant.messages.getString("proxies.name");
    }

    @Override
    public void hook(ExtensionHook extensionHook) {
        super.hook(extensionHook);

        extensionHook.addOptionsParamSet(this.getParam());
        extensionHook.addOptionsChangedListener(this);

        if (getView() != null) {
            extensionHook.getHookView().addOptionPanel(getOptionsProxiesPanel());
        }

        extensionHook.addApiImplementor(new ProxiesAPI(this));
    }

    @Override
    public void stop() {
        super.stop();
        // Stop all of the running servers
        for (Entry<String, org.parosproxy.paros.core.proxy.ProxyServer> entry :
                proxyServers.entrySet()) {
            stopProxyServer(entry.getKey(), entry.getValue());
        }
        proxyServers.clear();
    }

    @Override
    public void start() {
        restartProxies();
    }

    @Override
    public void optionsChanged(OptionsParam optionsParam) {
        restartProxies();
    }

    private void restartProxies() {
        List<ProxiesParamProxy> proxyParams = this.getParam().getProxies();
        Map<String, ProxiesParamProxy> newProxies = new HashMap<>();
        Map<String, org.parosproxy.paros.core.proxy.ProxyServer> currentProxies = proxyServers;
        proxyServers = new HashMap<>();
        for (ProxiesParamProxy proxyParam : proxyParams) {
            if (proxyParam.isEnabled()) {
                // Treat disabled proxies as if they dont really exist
                String key = createProxyKey(proxyParam.getAddress(), proxyParam.getPort());
                org.parosproxy.paros.core.proxy.ProxyServer proxy = currentProxies.remove(key);
                if (proxy == null) {
                    // Its a new one
                    newProxies.put(key, proxyParam);
                } else {
                    applyProxyOptions(proxyParam, proxy);
                    proxyServers.put(key, proxy);
                }
            }
        }
        // Any proxies left have been removed
        for (Entry<String, org.parosproxy.paros.core.proxy.ProxyServer> entry :
                currentProxies.entrySet()) {
            stopProxyServer(entry.getKey(), entry.getValue());
        }
        for (Entry<String, ProxiesParamProxy> entry : newProxies.entrySet()) {
            org.parosproxy.paros.core.proxy.ProxyServer proxy = startProxyServer(entry.getValue());
            proxyServers.put(entry.getKey(), proxy);
        }
    }

    private static void applyProxyOptions(
            ProxiesParamProxy param, org.parosproxy.paros.core.proxy.ProxyServer proxyServer) {
        org.parosproxy.paros.core.proxy.ProxyParam proxyParam = proxyServer.getProxyParam();
        proxyParam.setAlwaysDecodeGzip(param.isAlwaysDecodeGzip());
        proxyParam.setBehindNat(param.isBehindNat());
        proxyParam.setRemoveUnsupportedEncodings(param.isRemoveUnsupportedEncodings());
    }

    /**
     * Creates a key that identifies a proxy, to be used with {@link #proxyServers}.
     *
     * @param address the address of the proxy.
     * @param port the port of the proxy.
     * @return a key that identifies the proxy.
     */
    private static String createProxyKey(String address, int port) {
        return address + ":" + port;
    }

    private org.parosproxy.paros.core.proxy.ProxyServer startProxyServer(ProxiesParamProxy param) {
        String address = param.getAddress();
        int port = param.getPort();
        String key = createProxyKey(address, port);
        log.info("Starting alt proxy server: {}", key);
        org.parosproxy.paros.core.proxy.ProxyServer proxyServer =
                new org.parosproxy.paros.core.proxy.ProxyServer(ZAP_PROXY_THREAD_PREFIX + key) {

                    @Override
                    public boolean excludeUrl(URI uri) {
                        String uriString = uri.toString();
                        for (String excludePattern :
                                getModel()
                                        .getOptionsParam()
                                        .getGlobalExcludeURLParam()
                                        .getTokensNames()) {
                            if (uriString.matches(excludePattern)) {
                                return true;
                            }
                        }

                        for (String excludePattern :
                                getModel().getSession().getExcludeFromProxyRegexs()) {
                            if (uriString.matches(excludePattern)) {
                                return true;
                            }
                        }
                        return false;
                    }
                };
        proxyServer.getProxyParam().load(new ZapXmlConfiguration());
        applyProxyOptions(param, proxyServer);
        proxyServer.setConnectionParam(getModel().getOptionsParam().getConnectionParam());
        // Note that if this is _not_ set then the proxy will go into a nasty loop if you point a
        // browser at it
        proxyServer.setEnableApi(true);
        Control.getSingleton().getExtensionLoader().addProxyServer(proxyServer);
        proxyServer.startServer(address, port, false);
        return proxyServer;
    }

    private void stopProxyServer(
            String proxyKey, org.parosproxy.paros.core.proxy.ProxyServer proxyServer) {
        log.info("Stopping alt proxy server: {}", proxyKey);
        proxyServer.stopServer();
        Control.getSingleton().getExtensionLoader().removeProxyServer(proxyServer);
    }

    public List<ProxiesParamProxy> getAdditionalProxies() {
        return this.getParam().getProxies();
    }

    public ProxiesParamProxy getAdditionalProxy(String address, int port) {
        for (ProxiesParamProxy p : this.getParam().getProxies()) {
            if (p.getAddress().equals(address) && p.getPort() == port) {
                return new ProxiesParamProxy(p);
            }
        }
        return null;
    }

    public void addProxy(ProxiesParamProxy proxy) {
        String key = createProxyKey(proxy.getAddress(), proxy.getPort());
        if (this.getAdditionalProxy(proxy.getAddress(), proxy.getPort()) != null) {
            throw new IllegalArgumentException("Proxy already exists: " + key);
        }
        if (!this.canListenOn(proxy.getAddress(), proxy.getPort())) {
            throw new IllegalArgumentException("Cannot listen on: " + key);
        }

        org.parosproxy.paros.core.proxy.ProxyServer proxyServer = startProxyServer(proxy);
        proxyServers.put(key, proxyServer);
        this.getParam().addProxy(proxy);
    }

    public void removeProxy(String address, int port) {
        String key = createProxyKey(address, port);
        org.parosproxy.paros.core.proxy.ProxyServer proxyServer = proxyServers.remove(key);
        if (proxyServer == null) {
            throw new IllegalArgumentException("Proxy not found: " + key);
        }
        this.stopProxyServer(key, proxyServer);
        this.getParam().removeProxy(address, port);
    }

    protected boolean canListenOn(String address, int port) {
        ServerSocket socket = null;
        try {
            socket = new ServerSocket(port, 5, InetAddress.getByName(address));
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            if (socket != null)
                try {
                    socket.close();
                } catch (IOException e) {
                    // Ignore
                }
        }
    }

    private OptionsProxiesPanel getOptionsProxiesPanel() {
        if (optionsProxiesPanel == null) {
            optionsProxiesPanel = new OptionsProxiesPanel(this);
        }
        return optionsProxiesPanel;
    }

    private ProxiesParam getParam() {
        if (this.proxiesParam == null) {
            this.proxiesParam = new ProxiesParam();
        }
        return this.proxiesParam;
    }

    @Override
    public String getAuthor() {
        return Constant.ZAP_TEAM;
    }

    @Override
    public String getDescription() {
        return Constant.messages.getString("proxies.desc");
    }

    static boolean isSameAddress(String address, String otherAddress) {
        if (address.equals(otherAddress)) {
            return true;
        }

        try {
            return InetAddress.getByName(address).equals(InetAddress.getByName(otherAddress));
        } catch (UnknownHostException e) {
            return false;
        }
    }
}
