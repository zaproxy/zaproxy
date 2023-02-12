/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2020 The ZAP Development Team
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
package org.zaproxy.zap.network;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.model.Model;

/**
 * ZAP's {@link Authenticator}.
 *
 * <p>Authenticates to HTTP and SOCKS proxies.
 *
 * @since 2.10.0
 * @deprecated (2.12.0) No longer in use, it will be removed in a following version.
 */
@Deprecated
public class ZapAuthenticator extends Authenticator {

    private static final ZapAuthenticator SINGLETON = new ZapAuthenticator();

    private static final Logger logger = LogManager.getLogger(ZapAuthenticator.class);

    private static org.parosproxy.paros.network.ConnectionParam connectionOptions;

    private ZapAuthenticator() {}

    /**
     * Gets the singleton.
     *
     * @return the ZAP's {@code Authenticator}.
     */
    public static ZapAuthenticator getSingleton() {
        return SINGLETON;
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        PasswordAuthentication passwordAuthentication = getPasswordAuthenticationImpl();
        if (logger.isDebugEnabled()) {
            StringBuilder strBuilder = new StringBuilder();
            strBuilder.append("Getting password authentication for:").append('\n');
            strBuilder.append("Host      = ").append(getRequestingHost()).append('\n');
            strBuilder.append("Site      = ").append(getRequestingSite()).append('\n');
            strBuilder.append("Port      = ").append(getRequestingPort()).append('\n');
            strBuilder.append("Protocol  = ").append(getRequestingProtocol()).append('\n');
            strBuilder.append("Prompt    = ").append(getRequestingPrompt()).append('\n');
            strBuilder.append("Scheme    = ").append(getRequestingScheme()).append('\n');
            strBuilder.append("URL       = ").append(getRequestingURL()).append('\n');
            strBuilder.append("Auth Type = ").append(getRequestorType()).append('\n');
            strBuilder.append("Result: ");
            if (passwordAuthentication == null) {
                strBuilder.append(passwordAuthentication);
            } else {
                strBuilder.append("[Username: ").append(passwordAuthentication.getUserName());
                strBuilder.append(", Password: *****]");
            }
            logger.debug(strBuilder);
        }
        return passwordAuthentication;
    }

    private PasswordAuthentication getPasswordAuthenticationImpl() {
        if (isForSocksProxy()) {
            return getConnectionOptions().getSocksProxyPasswordAuth();
        }

        if (isForHttpProxy()) {
            return new PasswordAuthentication(
                    getConnectionOptions().getProxyChainUserName(),
                    getConnectionOptions().getProxyChainPassword().toCharArray());
        }

        return null;
    }

    /**
     * Tells whether or not the password authentication is being requested for the SOCKS proxy.
     *
     * @return {@code true} if the request is for the SOCKS proxy, {@code false} otherwise.
     */
    private boolean isForSocksProxy() {
        if (!getConnectionOptions().isUseSocksProxy()) {
            return false;
        }

        SocksProxy socksProxy = getConnectionOptions().getSocksProxy();
        return socksProxy.getVersion() == SocksProxy.Version.SOCKS5
                && getRequestorType() == RequestorType.SERVER
                && SocksProxy.Version.SOCKS5.name().equals(getRequestingProtocol())
                && socksProxy.getPort() == getRequestingPort()
                && socksProxy.getHost().equals(getRequestingHost());
    }

    /**
     * Tells whether or not the password authentication is being requested for the outgoing HTTP
     * proxy.
     *
     * @return {@code true} if the request is for the outgoing HTTP proxy, {@code false} otherwise.
     */
    private boolean isForHttpProxy() {
        return getRequestorType() == RequestorType.PROXY
                && getRequestingURL() != null
                && getConnectionOptions().isUseProxy(getRequestingURL().getHost())
                && getConnectionOptions().getProxyChainPort() == getRequestingPort()
                && getConnectionOptions().getProxyChainName().equals(getRequestingHost());
    }

    private static org.parosproxy.paros.network.ConnectionParam getConnectionOptions() {
        if (connectionOptions == null) {
            connectionOptions = Model.getSingleton().getOptionsParam().getConnectionParam();
        }
        return connectionOptions;
    }
}
