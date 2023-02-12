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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.common.AbstractParam;
import org.parosproxy.paros.model.Model;

/** @deprecated (2.12.0) No longer used/needed. It will be removed in a future release. */
@Deprecated
public class ProxiesParam extends AbstractParam {

    private static final Logger logger = LogManager.getLogger(ProxiesParam.class);

    private static final String PROXIES_BASE_KEY = "proxies";

    private static final String ALL_PROXIES_KEY = PROXIES_BASE_KEY + ".all";

    private static final String PROXY_ADDRESS_KEY = "address";
    private static final String PROXY_ENABLED_KEY = "enabled";
    private static final String PROXY_PORT_KEY = "port";
    private static final String PROXY_ANY_LOCAL_KEY = "anylocal";
    private static final String PROXY_REM_UNSUPPORTED = "remunsupported";
    private static final String PROXY_DECODE_KEY = "decode";
    private static final String PROXY_BEHIND_NAT_KEY = "behindnat";

    private static final String CONFIRM_REMOVE_PROXY_KEY = PROXIES_BASE_KEY + ".confirmRemoveProxy";

    private List<ProxiesParamProxy> proxies = null;
    private boolean confirmRemoveProxy = true;

    public ProxiesParam() {}

    @Override
    protected void parse() {
        try {
            List<HierarchicalConfiguration> fields =
                    ((HierarchicalConfiguration) getConfig()).configurationsAt(ALL_PROXIES_KEY);
            this.proxies = new ArrayList<>(fields.size() + 1);

            for (HierarchicalConfiguration sub : fields) {
                this.proxies.add(
                        new ProxiesParamProxy(
                                sub.getString(PROXY_ADDRESS_KEY),
                                sub.getInt(PROXY_PORT_KEY),
                                sub.getBoolean(PROXY_ENABLED_KEY, true),
                                sub.getBoolean(PROXY_ANY_LOCAL_KEY),
                                sub.getBoolean(PROXY_REM_UNSUPPORTED, true),
                                sub.getBoolean(PROXY_DECODE_KEY, true),
                                sub.getBoolean(PROXY_BEHIND_NAT_KEY, false)));
            }
        } catch (ConversionException e) {
            logger.error("Error while loading proxies: {}", e.getMessage(), e);
        }

        this.confirmRemoveProxy = getBoolean(CONFIRM_REMOVE_PROXY_KEY, true);
    }

    public ProxiesParamProxy getMainProxy() {
        org.parosproxy.paros.core.proxy.ProxyParam mainProxyParam =
                Model.getSingleton().getOptionsParam().getProxyParam();
        ProxiesParamProxy mainProxy =
                new ProxiesParamProxy(
                        mainProxyParam.getRawProxyIP(), mainProxyParam.getProxyPort(), true);
        mainProxy.setAlwaysDecodeGzip(mainProxyParam.isAlwaysDecodeGzip());
        mainProxy.setBehindNat(mainProxyParam.isBehindNat());
        mainProxy.setProxyIpAnyLocalAddress(mainProxyParam.isProxyIpAnyLocalAddress());
        mainProxy.setRemoveUnsupportedEncodings(mainProxyParam.isRemoveUnsupportedEncodings());
        return mainProxy;
    }

    public String[] getSecurityProtocolsEnabled() {
        return Model.getSingleton().getOptionsParam().getProxyParam().getSecurityProtocolsEnabled();
    }

    public void setSecurityProtocolsEnabled(String[] protocols) {
        Model.getSingleton()
                .getOptionsParam()
                .getProxyParam()
                .setSecurityProtocolsEnabled(protocols);
    }

    public List<ProxiesParamProxy> getProxies() {
        List<ProxiesParamProxy> list = new ArrayList<>(proxies.size() + 1);
        for (ProxiesParamProxy proxy : this.proxies) {
            list.add(new ProxiesParamProxy(proxy));
        }
        return list;
    }

    public void setMainProxy(ProxiesParamProxy proxy) {
        org.parosproxy.paros.core.proxy.ProxyParam proxyParam =
                Model.getSingleton().getOptionsParam().getProxyParam();
        proxyParam.setProxyIp(proxy.getAddress());
        proxyParam.setProxyPort(proxy.getPort());
        proxyParam.setAlwaysDecodeGzip(proxy.isAlwaysDecodeGzip());
        proxyParam.setBehindNat(proxy.isBehindNat());
        proxyParam.setRemoveUnsupportedEncodings(proxy.isRemoveUnsupportedEncodings());
    }

    public void setProxies(List<ProxiesParamProxy> proxies) {
        this.proxies = new ArrayList<>(proxies);

        ((HierarchicalConfiguration) getConfig()).clearTree(ALL_PROXIES_KEY);

        for (int i = 0, size = proxies.size(); i < size; ++i) {
            String elementBaseKey = ALL_PROXIES_KEY + "(" + i + ").";
            ProxiesParamProxy proxy = proxies.get(i);

            getConfig().setProperty(elementBaseKey + PROXY_ADDRESS_KEY, proxy.getAddress());
            getConfig().setProperty(elementBaseKey + PROXY_PORT_KEY, proxy.getPort());
            getConfig().setProperty(elementBaseKey + PROXY_ENABLED_KEY, proxy.isEnabled());
            getConfig()
                    .setProperty(
                            elementBaseKey + PROXY_ANY_LOCAL_KEY, proxy.isProxyIpAnyLocalAddress());
            getConfig()
                    .setProperty(
                            elementBaseKey + PROXY_REM_UNSUPPORTED,
                            proxy.isRemoveUnsupportedEncodings());
            getConfig().setProperty(elementBaseKey + PROXY_DECODE_KEY, proxy.isAlwaysDecodeGzip());
            getConfig().setProperty(elementBaseKey + PROXY_BEHIND_NAT_KEY, proxy.isBehindNat());
        }
    }

    public void addProxy(ProxiesParamProxy proxy) {
        this.proxies.add(proxy);
        // Save the configs
        setProxies(this.proxies);
    }

    public void removeProxy(String address, int port) {
        if (address == null || address.isEmpty()) {
            return;
        }

        for (Iterator<ProxiesParamProxy> it = proxies.iterator(); it.hasNext(); ) {
            ProxiesParamProxy proxy = it.next();
            if (address.equals(proxy.getAddress()) && proxy.getPort() == port) {
                it.remove();
                break;
            }
        }
        // Save the configs
        setProxies(this.proxies);
    }

    public boolean isConfirmRemoveProxy() {
        return this.confirmRemoveProxy;
    }

    public void setConfirmRemoveProxy(boolean confirmRemove) {
        this.confirmRemoveProxy = confirmRemove;
        getConfig().setProperty(CONFIRM_REMOVE_PROXY_KEY, confirmRemoveProxy);
    }
}
