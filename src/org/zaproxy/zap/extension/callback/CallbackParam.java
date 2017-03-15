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

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.log4j.Logger;
import org.parosproxy.paros.common.AbstractParam;

/**
 * @author psiinon
 *
 */
public class CallbackParam extends AbstractParam {

    private static final Logger logger = Logger.getLogger(CallbackParam.class);

    private static final String PROXY_BASE_KEY = "callback";

    private static final String LOCAL_ADDRESS_KEY = PROXY_BASE_KEY
            + ".localaddr";
    private static final String REMOTE_ADDRESS_KEY = PROXY_BASE_KEY
            + ".remoteaddr";
    private static final String PORT_KEY = PROXY_BASE_KEY + ".port";

    private String localAddress;
    private String remoteAddress;
    private int port;

    public CallbackParam() {
    }

    @Override
    protected void parse() {
        try {
            localAddress = getConfig().getString(LOCAL_ADDRESS_KEY, "0.0.0.0");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        try {
            remoteAddress = getConfig().getString(REMOTE_ADDRESS_KEY,
                    getDefaultAddress());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        try {
            port = getConfig().getInt(PORT_KEY, 0);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private String getDefaultAddress() {
        List<String> addrs = getAvailableAddresses(false);
        for (String addr : addrs) {
            if (!addr.contains(":") && !addr.equals("localhost")
                    && !addr.equals("127.0.0.1")) {
                // Return the first IPv4 one thats not localhost
                return addr;
            }
        }
        if (addrs.size() > 0) {
            return addrs.get(0);
        }
        // Better than nothing
        return "localhost";
    }

    public List<String> getAvailableAddresses(boolean remoteOnly) {
        List<String> list = new ArrayList<String>();
        Enumeration<NetworkInterface> e;
        try {
            e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                NetworkInterface n = e.nextElement();
                if (n.isLoopback() || !n.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> ee = n.getInetAddresses();
                while (ee.hasMoreElements()) {
                    InetAddress i = ee.nextElement();
                    if (remoteOnly && i.isSiteLocalAddress()) {
                        continue;
                    }
                    String addr = i.getHostAddress();
                    if (addr.indexOf('%') > 0) {
                        addr = addr.substring(0, addr.indexOf('%'));
                    }
                    list.add(addr);
                }
            }
        } catch (SocketException e1) {
            logger.error(e1.getMessage(), e1);
        }
        return list;
    }

    public String getLocalAddress() {
        return localAddress;
    }

    public void setLocalAddress(String localAddress) {
        if (this.localAddress.equals(localAddress)) {
            return;
        }
        this.localAddress = localAddress.trim();
        getConfig().setProperty(LOCAL_ADDRESS_KEY, this.localAddress);
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        if (this.remoteAddress.equals(remoteAddress)) {
            return;
        }
        this.remoteAddress = remoteAddress.trim();
        getConfig().setProperty(REMOTE_ADDRESS_KEY, this.remoteAddress);
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        if (this.port == port) {
            return;
        }
        this.port = port;
        getConfig().setProperty(PORT_KEY, Integer.toString(this.port));
    }

}
