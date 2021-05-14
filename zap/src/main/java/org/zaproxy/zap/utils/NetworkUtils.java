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
package org.zaproxy.zap.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** @since 2.7.0 */
public final class NetworkUtils {

    private NetworkUtils() {}

    private static final Logger LOG = LogManager.getLogger(NetworkUtils.class);

    public static List<String> getAvailableAddresses(boolean remoteOnly) {
        List<String> list = new ArrayList<>();
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
            LOG.error(e1.getMessage(), e1);
        }
        return list;
    }
}
