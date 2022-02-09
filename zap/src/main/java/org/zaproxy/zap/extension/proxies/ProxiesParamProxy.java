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

import org.zaproxy.zap.utils.Enableable;

/** @deprecated (2.12.0) No longer used/needed. It will be removed in a future release. */
@Deprecated
public class ProxiesParamProxy extends Enableable {

    private String address = "localhost";
    private int port = 8080;

    /** Flag that indicates if the proxy ip is any local address. */
    private boolean proxyIpAnyLocalAddress;

    /**
     * Flag that controls whether or not the local proxy should remove unsupported encodings from
     * the "Accept-Encoding" request-header field.
     *
     * <p>Default is {@code true}.
     *
     * @see #setRemoveUnsupportedEncodings(boolean)
     */
    private boolean removeUnsupportedEncodings = true;
    /** The option that controls whether the proxy should always decode gzipped content or not. */
    private boolean alwaysDecodeGzip = true;

    /**
     * Flag that controls whether or not the local proxy is behind NAT.
     *
     * <p>Default is {@code false}.
     */
    private boolean behindNat;

    public ProxiesParamProxy(boolean enabled) {
        super(enabled);
    }

    public ProxiesParamProxy(String address, int port, boolean enabled) {
        this(enabled);

        this.address = address;
        this.port = port;
    }

    public ProxiesParamProxy(
            String address,
            int port,
            boolean enabled,
            boolean proxyIpAnyLocalAddress,
            boolean removeUnsupportedEncodings,
            boolean alwaysDecodeGzip,
            boolean behindNat) {
        super(enabled);
        this.address = address;
        this.port = port;
        this.proxyIpAnyLocalAddress = proxyIpAnyLocalAddress;
        this.removeUnsupportedEncodings = removeUnsupportedEncodings;
        this.alwaysDecodeGzip = alwaysDecodeGzip;
        this.behindNat = behindNat;
    }

    public ProxiesParamProxy(ProxiesParamProxy proxy) {
        this(
                proxy.address,
                proxy.port,
                proxy.isEnabled(),
                proxy.proxyIpAnyLocalAddress,
                proxy.removeUnsupportedEncodings,
                proxy.alwaysDecodeGzip,
                proxy.behindNat);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isProxyIpAnyLocalAddress() {
        return proxyIpAnyLocalAddress;
    }

    public void setProxyIpAnyLocalAddress(boolean proxyIpAnyLocalAddress) {
        this.proxyIpAnyLocalAddress = proxyIpAnyLocalAddress;
    }

    public boolean isRemoveUnsupportedEncodings() {
        return removeUnsupportedEncodings;
    }

    public void setRemoveUnsupportedEncodings(boolean removeUnsupportedEncodings) {
        this.removeUnsupportedEncodings = removeUnsupportedEncodings;
    }

    public boolean isAlwaysDecodeGzip() {
        return alwaysDecodeGzip;
    }

    public void setAlwaysDecodeGzip(boolean alwaysDecodeGzip) {
        this.alwaysDecodeGzip = alwaysDecodeGzip;
    }

    public boolean isBehindNat() {
        return behindNat;
    }

    public void setBehindNat(boolean behindNat) {
        this.behindNat = behindNat;
    }
}
