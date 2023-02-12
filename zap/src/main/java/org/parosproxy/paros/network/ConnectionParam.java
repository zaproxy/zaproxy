/*
 * Created on Jun 6, 2004
 *
 * Paros and its related class files.
 *
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2004 Chinotec Technologies Company
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Clarified Artistic License
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Clarified Artistic License for more details.
 *
 * You should have received a copy of the Clarified Artistic License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
// ZAP: 2011/08/03 Cope with unexpected values in config file
// ZAP: 2012/04/23 Added @Override annotation to the appropriate method and removed
// unnecessary cast.
// ZAP: 2012/11/15 Issue 416: Normalise how multiple related options are managed
// throughout ZAP and enhance the usability of some options.
// ZAP: 2013/01/04 Added portsForSslTunneling parameter with method
// isPortDemandingSslTunnel() to indicate HTTP CONNECT behavior.
// ZAP: 2013/01/30 Issue 478: Allow to choose to send ZAP's managed cookies on
// a single Cookie request header and set it as the default
// ZAP: 2013/12/13 Issue 939: ZAP should accept SSL connections on non-standard ports automatically
// ZAP: 2014/03/23 Issue 416: Normalise how multiple related options are managed throughout ZAP
// and enhance the usability of some options
// ZAP: 2014/03/23 Issue 968: Allow to choose the enabled SSL/TLS protocols
// ZAP: 2014/03/23 Issue 1100: Annotate option methods that shouldn't be exposed in the ZAP API
// ZAP: 2041/08/14 Issue 1305: Outgoing proxy is disabled when updating from old versions
// ZAP: 2016/08/08 Issue 2742: Allow for override/customization of Java's "networkaddress.cache.ttl"
// value
// ZAP: 2017/01/11 Exclude some options from the API (manually handled to return correct values).
// ZAP: 2017/04/14 Validate that the SSL/TLS versions persisted can be set/used.
// ZAP: 2017/05/02 Added option key to enable / disable HTTP State
// ZAP: 2017/05/15 Ensure HttpState is non-null when HTTP State is enabled.
// ZAP: 2017/06/19 Do not allow to set negative timeout values and expose the default value.
// ZAP: 2017/09/26 Use helper methods to read the configurations.
// ZAP: 2018/02/14 Remove unnecessary boxing / unboxing
// ZAP: 2018/08/10 Set the default user agent to HttpRequestHeader (Issue 4846).
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2020/01/02 Updated default user agent
// ZAP: 2020/04/20 Allow to configure the SOCKS proxy (Issue 29).
// ZAP: 2020/11/26 Use Log4j 2 classes for logging.
// ZAP: 2021/10/06 Updated default user agent
// ZAP: 2022/02/02 Removed getProxyChainSkipName() and setProxyChainSkipName(String)
// ZAP: 2022/02/08 Use isEmpty where applicable.
// ZAP: 2022/05/04 Deprecate single cookie request header option.
// ZAP: 2022/05/20 Deprecate the class.
// ZAP: 2022/09/21 Use format specifiers instead of concatenation when logging.
package org.parosproxy.paros.network;

import java.net.PasswordAuthentication;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.httpclient.HttpState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.common.AbstractParam;
import org.zaproxy.zap.extension.api.ZapApiIgnore;
import org.zaproxy.zap.network.DomainMatcher;

/** @deprecated (2.12.0) Use the network add-on instead. */
@Deprecated
public class ConnectionParam extends AbstractParam {

    // ZAP: Added logger
    private static Logger log = LogManager.getLogger(ConnectionParam.class);

    private static final String CONNECTION_BASE_KEY = "connection";

    private static final String USE_PROXY_CHAIN_KEY = CONNECTION_BASE_KEY + ".proxyChain.enabled";
    private static final String PROXY_CHAIN_NAME = CONNECTION_BASE_KEY + ".proxyChain.hostName";
    private static final String PROXY_CHAIN_PORT = CONNECTION_BASE_KEY + ".proxyChain.port";
    private static final String USE_PROXY_CHAIN_AUTH_KEY =
            CONNECTION_BASE_KEY + ".proxyChain.authEnabled";
    private static final String PROXY_CHAIN_REALM = CONNECTION_BASE_KEY + ".proxyChain.realm";
    private static final String PROXY_CHAIN_USER_NAME =
            CONNECTION_BASE_KEY + ".proxyChain.userName";
    private static final String PROXY_CHAIN_PASSWORD = CONNECTION_BASE_KEY + ".proxyChain.password";

    private static final String PROXY_EXCLUDED_DOMAIN_KEY =
            CONNECTION_BASE_KEY + ".proxyChain.exclusions";
    private static final String ALL_PROXY_EXCLUDED_DOMAINS_KEY =
            PROXY_EXCLUDED_DOMAIN_KEY + ".exclusion";
    private static final String PROXY_EXCLUDED_DOMAIN_VALUE_KEY = "name";
    private static final String PROXY_EXCLUDED_DOMAIN_REGEX_KEY = "regex";
    private static final String PROXY_EXCLUDED_DOMAIN_ENABLED_KEY = "enabled";
    private static final String CONFIRM_REMOVE_EXCLUDED_DOMAIN =
            CONNECTION_BASE_KEY + ".proxyChain.confirmRemoveExcludedDomain";

    private static final String SECURITY_PROTOCOLS_ENABLED =
            CONNECTION_BASE_KEY + ".securityProtocolsEnabled";
    private static final String SECURITY_PROTOCOL_ELEMENT_KEY = "protocol";
    private static final String ALL_SECURITY_PROTOCOLS_ENABLED_KEY =
            SECURITY_PROTOCOLS_ENABLED + "." + SECURITY_PROTOCOL_ELEMENT_KEY;

    // ZAP: Added prompt option and timeout
    private static final String PROXY_CHAIN_PROMPT = CONNECTION_BASE_KEY + ".proxyChain.prompt";
    private static final String TIMEOUT_IN_SECS = CONNECTION_BASE_KEY + ".timeoutInSecs";
    private static final String SINGLE_COOKIE_REQUEST_HEADER =
            CONNECTION_BASE_KEY + ".singleCookieRequestHeader";
    private static final String HTTP_STATE_ENABLED = CONNECTION_BASE_KEY + ".httpStateEnabled";
    public static final String DEFAULT_USER_AGENT = CONNECTION_BASE_KEY + ".defaultUserAgent";

    public static final String DEFAULT_DEFAULT_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:92.0) Gecko/20100101 Firefox/92.0";

    /** The security property for TTL of successful DNS queries. */
    private static final String DNS_TTL_SUCCESSFUL_QUERIES_SECURITY_PROPERTY =
            "networkaddress.cache.ttl";

    /**
     * The default TTL (in seconds) of successful DNS queries.
     *
     * @since 2.6.0
     */
    public static final int DNS_DEFAULT_TTL_SUCCESSFUL_QUERIES = 30;

    /** The configuration key for TTL of successful DNS queries. */
    private static final String DNS_TTL_SUCCESSFUL_QUERIES_KEY =
            CONNECTION_BASE_KEY + ".dnsTtlSuccessfulQueries";

    /**
     * The default connection timeout (in seconds).
     *
     * @since 2.7.0
     */
    public static final int DEFAULT_TIMEOUT = 20;

    private static final String SOCKS_PROXY_BASE_KEY = CONNECTION_BASE_KEY + ".socksProxy.";
    private static final String USE_SOCKS_PROXY_KEY = SOCKS_PROXY_BASE_KEY + "enabled";
    private static final String SOCKS_PROXY_HOST_KEY = SOCKS_PROXY_BASE_KEY + "host";
    private static final String SOCKS_PROXY_PORT_KEY = SOCKS_PROXY_BASE_KEY + "port";
    private static final String SOCKS_PROXY_VERSION_KEY = SOCKS_PROXY_BASE_KEY + "version";
    private static final String SOCKS_PROXY_DNS_KEY = SOCKS_PROXY_BASE_KEY + "dns";
    private static final String SOCKS_PROXY_USERNAME_KEY = SOCKS_PROXY_BASE_KEY + "username";
    private static final String SOCKS_PROXY_PASSWORD_KEY = SOCKS_PROXY_BASE_KEY + "password";

    /**
     * The default SOCKS proxy configuration.
     *
     * @since 2.10.0
     */
    public static final org.zaproxy.zap.network.SocksProxy DEFAULT_SOCKS_PROXY =
            new org.zaproxy.zap.network.SocksProxy("localhost", 1080);

    /**
     * Pattern with loopback names and addresses that should be always resolved (when creating the
     * {@link java.net.InetSocketAddress}).
     *
     * <p>Same pattern used by default proxy selector.
     *
     * @see #shouldResolveRemoteHostname(String)
     */
    private static final Pattern LOOPBACK_PATTERN =
            Pattern.compile("\\Qlocalhost\\E|\\Q127.\\E.*|\\Q[::1]\\E|\\Q0.0.0.0\\E|\\Q[::0]\\E");

    private boolean useProxyChain;
    private String proxyChainName = "";
    private int proxyChainPort = 8080;
    private boolean confirmRemoveProxyExcludeDomain = true;
    private boolean useProxyChainAuth;
    private String proxyChainRealm = "";
    private String proxyChainUserName = "";
    private String proxyChainPassword = "";

    private boolean useSocksProxy;
    private org.zaproxy.zap.network.SocksProxy socksProxy = DEFAULT_SOCKS_PROXY;
    private PasswordAuthentication socksProxyPasswordAuth =
            new PasswordAuthentication("", new char[0]);

    private HttpState httpState = null;
    private boolean httpStateEnabled = false;
    private List<DomainMatcher> proxyExcludedDomains = new ArrayList<>(0);
    private List<DomainMatcher> proxyExcludedDomainsEnabled = new ArrayList<>(0);

    private String[] securityProtocolsEnabled;

    // ZAP: Added prompt option and timeout
    private boolean proxyChainPrompt = false;
    private int timeoutInSecs = DEFAULT_TIMEOUT;

    private boolean singleCookieRequestHeader = true;
    private String defaultUserAgent = "";

    /** The TTL (in seconds) of successful DNS queries. */
    private int dnsTtlSuccessfulQueries = DNS_DEFAULT_TTL_SUCCESSFUL_QUERIES;

    /** @return Returns the httpStateEnabled. */
    public boolean isHttpStateEnabled() {
        return httpStateEnabled;
    }
    /** @param httpStateEnabled The httpStateEnabled to set. */
    public void setHttpStateEnabled(boolean httpStateEnabled) {
        setHttpStateEnabledImpl(httpStateEnabled);
        getConfig().setProperty(HTTP_STATE_ENABLED, this.httpStateEnabled);
    }

    private void setHttpStateEnabledImpl(boolean httpStateEnabled) {
        this.httpStateEnabled = httpStateEnabled;
        if (this.httpStateEnabled) {
            httpState = new HttpState();
        } else {
            httpState = null;
        }
    }

    public ConnectionParam() {}

    @Override
    protected void parse() {
        updateOptions();

        dnsTtlSuccessfulQueries =
                getInt(DNS_TTL_SUCCESSFUL_QUERIES_KEY, DNS_DEFAULT_TTL_SUCCESSFUL_QUERIES);
        Security.setProperty(
                DNS_TTL_SUCCESSFUL_QUERIES_SECURITY_PROPERTY,
                Integer.toString(dnsTtlSuccessfulQueries));

        useProxyChain = getBoolean(USE_PROXY_CHAIN_KEY, false);
        useProxyChainAuth = getBoolean(USE_PROXY_CHAIN_AUTH_KEY, false);

        setProxyChainName(getString(PROXY_CHAIN_NAME, ""));
        setProxyChainPort(getInt(PROXY_CHAIN_PORT, 8080));

        loadProxyExcludedDomains();
        this.confirmRemoveProxyExcludeDomain = getBoolean(CONFIRM_REMOVE_EXCLUDED_DOMAIN, true);

        setProxyChainRealm(getString(PROXY_CHAIN_REALM, ""));
        setProxyChainUserName(getString(PROXY_CHAIN_USER_NAME, ""));

        try {
            // ZAP: Added prompt option
            if (getConfig().getProperty(PROXY_CHAIN_PROMPT) instanceof String
                    && ((String) getConfig().getProperty(PROXY_CHAIN_PROMPT)).isEmpty()) {
                // In 1.2.0 the default for this field was empty, which causes a crash in 1.3.*
                setProxyChainPrompt(false);
            } else if (getBoolean(PROXY_CHAIN_PROMPT, false)) {
                setProxyChainPrompt(true);
            } else {
                setProxyChainPrompt(false);
                setProxyChainPassword(getString(PROXY_CHAIN_PASSWORD, ""));
            }
        } catch (Exception e) {
            // ZAP: Log exceptions
            log.error(e.getMessage(), e);
        }

        setTimeoutInSecsImpl(getInt(TIMEOUT_IN_SECS, DEFAULT_TIMEOUT));

        this.singleCookieRequestHeader = getBoolean(SINGLE_COOKIE_REQUEST_HEADER, true);

        setHttpStateEnabledImpl(getBoolean(HTTP_STATE_ENABLED, false));

        this.defaultUserAgent = getString(DEFAULT_USER_AGENT, DEFAULT_DEFAULT_USER_AGENT);
        HttpRequestHeader.setDefaultUserAgent(defaultUserAgent);

        loadSecurityProtocolsEnabled();

        parseSocksProxyOptions();
    }

    private void updateOptions() {
        final String oldKey = CONNECTION_BASE_KEY + "sslConnectPorts";
        if (getConfig().containsKey(oldKey)) {
            getConfig().clearProperty(oldKey);
        }

        final String oldSkipNameKey = CONNECTION_BASE_KEY + ".proxyChain.skipName";
        if (getConfig().containsKey(oldSkipNameKey)) {
            migrateOldSkipNameOption(getConfig().getString(oldSkipNameKey, ""));
            getConfig().clearProperty(oldSkipNameKey);
        }

        if (!getConfig().containsKey(USE_PROXY_CHAIN_KEY)) {
            String proxyName = getConfig().getString(PROXY_CHAIN_NAME, "");
            if (!proxyName.isEmpty()) {
                getConfig().setProperty(USE_PROXY_CHAIN_KEY, Boolean.TRUE);
            }
        }

        if (!getConfig().containsKey(USE_PROXY_CHAIN_AUTH_KEY)) {
            String proxyUserName = getConfig().getString(PROXY_CHAIN_USER_NAME, "");
            if (!proxyUserName.isEmpty()) {
                getConfig().setProperty(USE_PROXY_CHAIN_AUTH_KEY, Boolean.TRUE);
            }
        }
    }

    private void migrateOldSkipNameOption(String skipNames) {
        List<DomainMatcher> excludedDomains = convertOldSkipNameOption(skipNames);

        if (!excludedDomains.isEmpty()) {
            setProxyExcludedDomains(excludedDomains);
        }
    }

    private static List<DomainMatcher> convertOldSkipNameOption(String skipNames) {
        if (skipNames == null || skipNames.isEmpty()) {
            return Collections.emptyList();
        }

        ArrayList<DomainMatcher> excludedDomains = new ArrayList<>();
        String[] names = skipNames.split(";");
        for (String name : names) {
            String excludedDomain = name.trim();
            if (!excludedDomain.isEmpty()) {
                if (excludedDomain.contains("*")) {
                    excludedDomain = excludedDomain.replace(".", "\\.").replace("*", ".*?");
                    try {
                        Pattern pattern = Pattern.compile(excludedDomain, Pattern.CASE_INSENSITIVE);
                        excludedDomains.add(new DomainMatcher(pattern));
                    } catch (IllegalArgumentException e) {
                        log.error("Failed to migrate the excluded domain name: {}", name, e);
                    }
                } else {
                    excludedDomains.add(new DomainMatcher(excludedDomain));
                }
            }
        }
        excludedDomains.trimToSize();
        return excludedDomains;
    }

    /**
     * Tells whether or not the outgoing connections should use the proxy set.
     *
     * @return {@code true} if outgoing connections should use the proxy set, {@code false}
     *     otherwise.
     * @since 2.3.0
     * @see #setUseProxyChain(boolean)
     */
    public boolean isUseProxyChain() {
        return useProxyChain;
    }

    /**
     * Sets whether or not the outgoing connections should use the proxy set.
     *
     * <p><strong>Note:</strong> The call to this method has no effect if set to use the proxy but
     * the proxy was not previously configured.
     *
     * @param useProxyChain {@code true} if outgoing connections should use the proxy set, {@code
     *     false} otherwise.
     * @since 2.3.0
     * @see #isUseProxyChain()
     * @see #setProxyChainName(String)
     * @see #setProxyChainPort(int)
     */
    public void setUseProxyChain(boolean useProxyChain) {
        if (useProxyChain && (getProxyChainName() == null || getProxyChainName().isEmpty())) {
            return;
        }

        this.useProxyChain = useProxyChain;
        getConfig().setProperty(USE_PROXY_CHAIN_KEY, this.useProxyChain);
    }

    /**
     * Returns the name of the outgoing proxy. The returned name is never {@code null}.
     *
     * @return the name of the outgoing proxy, never {@code null}.
     * @see #isUseProxyChain()
     * @see #setProxyChainName(String)
     */
    public String getProxyChainName() {
        return proxyChainName;
    }

    /**
     * Sets the name of the outgoing proxy. If empty the use of the outgoing proxy will be disabled.
     *
     * <p><strong>Note:</strong> The call to this method has no effect if the given {@code
     * proxyChainName} is {@code null}.
     *
     * @param proxyChainName the name of the outgoing proxy
     * @see #getProxyChainName()
     * @see #setUseProxyChain(boolean)
     */
    public void setProxyChainName(String proxyChainName) {
        if (proxyChainName == null) {
            return;
        }
        this.proxyChainName = proxyChainName.trim();
        if (proxyChainName.isEmpty()) {
            setUseProxyChain(false);
        }
        getConfig().setProperty(PROXY_CHAIN_NAME, this.proxyChainName);
    }

    public int getProxyChainPort() {
        return proxyChainPort;
    }

    public void setProxyChainPort(int proxyChainPort) {
        this.proxyChainPort = proxyChainPort;
        getConfig().setProperty(PROXY_CHAIN_PORT, Integer.toString(this.proxyChainPort));
    }

    /**
     * Tells whether or not the outgoing connections should use the proxy authentication credentials
     * set.
     *
     * @return {@code true} if outgoing connections should use the proxy authentication credentials
     *     set, {@code false} otherwise.
     * @since 2.3.0
     * @see #isUseProxyChain()
     * @see #setUseProxyChainAuth(boolean)
     */
    public boolean isUseProxyChainAuth() {
        return useProxyChainAuth;
    }

    /**
     * Sets whether or not the outgoing connections should use the proxy authentication credentials
     * set.
     *
     * <p><strong>Note:</strong> The call to this method has no effect if set to use the credentials
     * but the credentials were not previously set.
     *
     * @param useProxyChainAuth {@code true} if outgoing connections should use the proxy
     *     authentication credentials set, {@code false} otherwise.
     * @since 2.3.0
     * @see #isUseProxyChainAuth()
     * @see #setUseProxyChain(boolean)
     * @see #setProxyChainUserName(String)
     * @see #setProxyChainPassword(String)
     * @see #setProxyChainRealm(String)
     */
    public void setUseProxyChainAuth(boolean useProxyChainAuth) {
        if (useProxyChainAuth
                && (getProxyChainUserName() == null || getProxyChainUserName().isEmpty())) {
            return;
        }

        this.useProxyChainAuth = useProxyChainAuth;
        getConfig().setProperty(USE_PROXY_CHAIN_AUTH_KEY, this.useProxyChainAuth);
    }

    public String getProxyChainRealm() {
        return proxyChainRealm;
    }

    public void setProxyChainRealm(String proxyChainRealm) {
        this.proxyChainRealm = proxyChainRealm.trim();
        getConfig().setProperty(PROXY_CHAIN_REALM, this.proxyChainRealm);
    }

    public String getProxyChainUserName() {
        return proxyChainUserName;
    }

    public void setProxyChainUserName(String proxyChainUserName) {
        this.proxyChainUserName = proxyChainUserName.trim();
        getConfig().setProperty(PROXY_CHAIN_USER_NAME, this.proxyChainUserName);
    }

    public String getProxyChainPassword() {
        return proxyChainPassword.trim();
    }

    public void setProxyChainPassword(String proxyChainPassword) {
        this.proxyChainPassword = proxyChainPassword;
        getConfig().setProperty(PROXY_CHAIN_PASSWORD, this.proxyChainPassword);
    }

    // ZAP: Added setProxyChainPassword(String, boolean) method
    public void setProxyChainPassword(String proxyChainPassword, boolean save) {
        if (save) {
            this.setProxyChainPassword(proxyChainPassword);
        } else {
            this.proxyChainPassword = proxyChainPassword;
        }
    }

    // ZAP: Added prompt option
    public void setProxyChainPrompt(boolean proxyPrompt) {
        this.proxyChainPrompt = proxyPrompt;
        getConfig().setProperty(PROXY_CHAIN_PROMPT, this.proxyChainPrompt);
    }

    public boolean isProxyChainPrompt() {
        return this.proxyChainPrompt;
    }

    /**
     * Tells whether or not the given {@code domainName} should be excluded from the outgoing proxy.
     *
     * @param domainName the domain to be checked
     * @return {@code true} if the given {@code domainName} should be excluded, {@code false}
     *     otherwise.
     * @since 2.3.0
     */
    private boolean isDomainExcludedFromProxy(String domainName) {
        if (domainName == null || domainName.isEmpty()) {
            return false;
        }

        for (DomainMatcher excludedDomain : proxyExcludedDomainsEnabled) {
            if (excludedDomain.matches(domainName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if given host name need to send using proxy.
     *
     * @param hostName host name to be checked.
     * @return true = need to send via proxy.
     */
    public boolean isUseProxy(String hostName) {
        if (!isUseProxyChain() || isDomainExcludedFromProxy(hostName)) {
            return false;
        } else {
            return true;
        }
    }

    /** @return Returns the httpState. */
    public HttpState getHttpState() {
        return httpState;
    }
    /** @param httpState The httpState to set. */
    public void setHttpState(HttpState httpState) {
        this.httpState = httpState;
    }

    public int getTimeoutInSecs() {
        return timeoutInSecs;
    }

    public void setTimeoutInSecs(int timeoutInSecs) {
        setTimeoutInSecsImpl(timeoutInSecs);
        getConfig().setProperty(TIMEOUT_IN_SECS, this.timeoutInSecs);
    }

    private void setTimeoutInSecsImpl(int timeoutInSecs) {
        if (timeoutInSecs < 0) {
            this.timeoutInSecs = 0;
            return;
        }

        this.timeoutInSecs = timeoutInSecs;
    }

    /**
     * Tells whether the cookies should be set on a single "Cookie" request header or multiple
     * "Cookie" request headers, when sending an HTTP request to the server.
     *
     * @return {@code true} if the cookies should be set on a single request header, {@code false}
     *     otherwise
     * @deprecated (2.12.0) No longer supported, when managing cookies they will be sent in a single
     *     header field.
     */
    @Deprecated
    public boolean isSingleCookieRequestHeader() {
        return true;
    }

    /**
     * Sets whether the cookies should be set on a single "Cookie" request header or multiple
     * "Cookie" request headers, when sending an HTTP request to the server.
     *
     * @param singleCookieRequestHeader {@code true} if the cookies should be set on a single
     *     request header, {@code false} otherwise
     * @deprecated (2.12.0) No longer supported, when managing cookies they will be sent in a single
     *     header field.
     */
    @Deprecated
    public void setSingleCookieRequestHeader(boolean singleCookieRequestHeader) {}

    /**
     * Returns the domains excluded from the outgoing proxy.
     *
     * @return the domains excluded from the outgoing proxy.
     * @since 2.3.0
     * @see #isUseProxy(String)
     * @see #getProxyExcludedDomainsEnabled()
     * @see #setProxyExcludedDomains(List)
     */
    @ZapApiIgnore
    public List<DomainMatcher> getProxyExcludedDomains() {
        return proxyExcludedDomains;
    }

    /**
     * Returns the, enabled, domains excluded from the outgoing proxy.
     *
     * @return the enabled domains excluded from the outgoing proxy.
     * @since 2.3.0
     * @see #isUseProxy(String)
     * @see #getProxyExcludedDomains()
     * @see #setProxyExcludedDomains(List)
     */
    @ZapApiIgnore
    public List<DomainMatcher> getProxyExcludedDomainsEnabled() {
        return proxyExcludedDomainsEnabled;
    }

    /**
     * Sets the domains that will be excluded from the outgoing proxy.
     *
     * @param proxyExcludedDomains the domains that will be excluded.
     * @since 2.3.0
     * @see #getProxyExcludedDomains()
     * @see #getProxyExcludedDomainsEnabled()
     */
    public void setProxyExcludedDomains(List<DomainMatcher> proxyExcludedDomains) {
        if (proxyExcludedDomains == null || proxyExcludedDomains.isEmpty()) {
            ((HierarchicalConfiguration) getConfig()).clearTree(ALL_PROXY_EXCLUDED_DOMAINS_KEY);

            this.proxyExcludedDomains = Collections.emptyList();
            this.proxyExcludedDomainsEnabled = Collections.emptyList();
            return;
        }

        this.proxyExcludedDomains = new ArrayList<>(proxyExcludedDomains);

        ((HierarchicalConfiguration) getConfig()).clearTree(ALL_PROXY_EXCLUDED_DOMAINS_KEY);

        int size = proxyExcludedDomains.size();
        ArrayList<DomainMatcher> enabledExcludedDomains = new ArrayList<>(size);
        for (int i = 0; i < size; ++i) {
            String elementBaseKey = ALL_PROXY_EXCLUDED_DOMAINS_KEY + "(" + i + ").";
            DomainMatcher excludedDomain = proxyExcludedDomains.get(i);

            getConfig()
                    .setProperty(
                            elementBaseKey + PROXY_EXCLUDED_DOMAIN_VALUE_KEY,
                            excludedDomain.getValue());
            getConfig()
                    .setProperty(
                            elementBaseKey + PROXY_EXCLUDED_DOMAIN_REGEX_KEY,
                            excludedDomain.isRegex());
            getConfig()
                    .setProperty(
                            elementBaseKey + PROXY_EXCLUDED_DOMAIN_ENABLED_KEY,
                            excludedDomain.isEnabled());

            if (excludedDomain.isEnabled()) {
                enabledExcludedDomains.add(excludedDomain);
            }
        }

        enabledExcludedDomains.trimToSize();
        this.proxyExcludedDomainsEnabled = enabledExcludedDomains;
    }

    private void loadProxyExcludedDomains() {
        List<HierarchicalConfiguration> fields =
                ((HierarchicalConfiguration) getConfig())
                        .configurationsAt(ALL_PROXY_EXCLUDED_DOMAINS_KEY);
        this.proxyExcludedDomains = new ArrayList<>(fields.size());
        ArrayList<DomainMatcher> excludedDomainsEnabled = new ArrayList<>(fields.size());
        for (HierarchicalConfiguration sub : fields) {
            String value = sub.getString(PROXY_EXCLUDED_DOMAIN_VALUE_KEY, "");
            if (value.isEmpty()) {
                log.warn(
                        "Failed to read an outgoing proxy excluded domain entry, required value is empty.");
                continue;
            }

            DomainMatcher excludedDomain = null;
            boolean regex = sub.getBoolean(PROXY_EXCLUDED_DOMAIN_REGEX_KEY, false);
            if (regex) {
                try {
                    Pattern pattern = DomainMatcher.createPattern(value);
                    excludedDomain = new DomainMatcher(pattern);
                } catch (IllegalArgumentException e) {
                    log.error(
                            "Failed to read an outgoing proxy excluded domain entry with regex: {}",
                            value,
                            e);
                }
            } else {
                excludedDomain = new DomainMatcher(value);
            }

            if (excludedDomain != null) {
                excludedDomain.setEnabled(sub.getBoolean(PROXY_EXCLUDED_DOMAIN_ENABLED_KEY, true));

                proxyExcludedDomains.add(excludedDomain);

                if (excludedDomain.isEnabled()) {
                    excludedDomainsEnabled.add(excludedDomain);
                }
            }
        }

        excludedDomainsEnabled.trimToSize();
        this.proxyExcludedDomainsEnabled = excludedDomainsEnabled;
    }

    /**
     * Tells whether or not the remotion of a proxy exclusion needs confirmation.
     *
     * @return {@code true} if the remotion needs confirmation, {@code false} otherwise.
     * @since 2.3.0
     */
    @ZapApiIgnore
    public boolean isConfirmRemoveProxyExcludedDomain() {
        return this.confirmRemoveProxyExcludeDomain;
    }

    /**
     * Sets whether or not the remotion of a proxy exclusion needs confirmation.
     *
     * @param confirmRemove {@code true} if the remotion needs confirmation, {@code false}
     *     otherwise.
     * @since 2.3.0
     */
    @ZapApiIgnore
    public void setConfirmRemoveProxyExcludedDomain(boolean confirmRemove) {
        this.confirmRemoveProxyExcludeDomain = confirmRemove;
        getConfig().setProperty(CONFIRM_REMOVE_EXCLUDED_DOMAIN, confirmRemoveProxyExcludeDomain);
    }

    /**
     * Returns the security protocols enabled (SSL/TLS) for outgoing connections.
     *
     * @return the security protocols enabled for outgoing connections.
     * @since 2.3.0
     */
    @ZapApiIgnore
    public String[] getSecurityProtocolsEnabled() {
        return Arrays.copyOf(securityProtocolsEnabled, securityProtocolsEnabled.length);
    }

    /**
     * Sets the security protocols enabled (SSL/TLS) for outgoing connections.
     *
     * <p>The call has no effect if the given array is null or empty.
     *
     * @param enabledProtocols the security protocols enabled (SSL/TLS) for outgoing connections.
     * @throws IllegalArgumentException if at least one of the {@code enabledProtocols} is {@code
     *     null} or empty.
     * @since 2.3.0
     */
    public void setSecurityProtocolsEnabled(String[] enabledProtocols) {
        if (enabledProtocols == null || enabledProtocols.length == 0) {
            return;
        }
        for (int i = 0; i < enabledProtocols.length; i++) {
            if (enabledProtocols[i] == null || enabledProtocols[i].isEmpty()) {
                throw new IllegalArgumentException(
                        "The parameter enabledProtocols must not contain null or empty elements.");
            }
        }

        ((HierarchicalConfiguration) getConfig()).clearTree(ALL_SECURITY_PROTOCOLS_ENABLED_KEY);

        for (int i = 0; i < enabledProtocols.length; ++i) {
            String elementBaseKey = ALL_SECURITY_PROTOCOLS_ENABLED_KEY + "(" + i + ")";
            getConfig().setProperty(elementBaseKey, enabledProtocols[i]);
        }

        this.securityProtocolsEnabled = Arrays.copyOf(enabledProtocols, enabledProtocols.length);
        setClientEnabledProtocols();
    }

    private void loadSecurityProtocolsEnabled() {
        List<Object> protocols = getConfig().getList(ALL_SECURITY_PROTOCOLS_ENABLED_KEY);
        if (!protocols.isEmpty()) {
            securityProtocolsEnabled = new String[protocols.size()];
            securityProtocolsEnabled = protocols.toArray(securityProtocolsEnabled);
            setClientEnabledProtocols();
        } else {
            setSecurityProtocolsEnabled(SSLConnector.getClientEnabledProtocols());
        }
    }

    private void setClientEnabledProtocols() {
        try {
            SSLConnector.setClientEnabledProtocols(securityProtocolsEnabled);
        } catch (IllegalArgumentException e) {
            log.warn(
                    "Failed to set persisted protocols {} falling back to {} caused by: {}",
                    Arrays.toString(securityProtocolsEnabled),
                    Arrays.toString(SSLConnector.getFailSafeProtocols()),
                    e.getMessage());
            securityProtocolsEnabled = SSLConnector.getFailSafeProtocols();
            SSLConnector.setClientEnabledProtocols(securityProtocolsEnabled);
        }
    }

    public String getDefaultUserAgent() {
        return this.defaultUserAgent;
    }

    public void setDefaultUserAgent(String defaultUserAgent) {
        this.defaultUserAgent = defaultUserAgent;
        HttpRequestHeader.setDefaultUserAgent(defaultUserAgent);
        getConfig().setProperty(DEFAULT_USER_AGENT, defaultUserAgent);
    }

    /**
     * Gets the TTL (in seconds) of successful DNS queries.
     *
     * @return the TTL in seconds
     * @since 2.6.0
     * @see #setDnsTtlSuccessfulQueries(int)
     */
    public int getDnsTtlSuccessfulQueries() {
        return dnsTtlSuccessfulQueries;
    }

    /**
     * Sets the TTL (in seconds) of successful DNS queries.
     *
     * <p>Some values have special meaning:
     *
     * <ul>
     *   <li>Negative number, cache forever;
     *   <li>Zero, disables caching;
     *   <li>Positive number, the number of seconds the successful DNS queries will be cached.
     * </ul>
     *
     * @param ttl the TTL in seconds
     * @since 2.6.0
     * @see #getDnsTtlSuccessfulQueries()
     */
    public void setDnsTtlSuccessfulQueries(int ttl) {
        if (dnsTtlSuccessfulQueries == ttl) {
            return;
        }

        dnsTtlSuccessfulQueries = ttl;
        getConfig().setProperty(DNS_TTL_SUCCESSFUL_QUERIES_KEY, ttl);
    }

    private void parseSocksProxyOptions() {
        String host = System.getProperty("socksProxyHost");
        int port;
        String version;
        boolean useDns = getBoolean(SOCKS_PROXY_DNS_KEY, DEFAULT_SOCKS_PROXY.isUseDns());

        if (host != null && !host.isEmpty()) {
            port = parseSocksPort(System.getProperty("socksProxyPort"));
            version = System.getProperty("socksProxyVersion");

            useSocksProxy = true;
        } else {
            host = getString(SOCKS_PROXY_HOST_KEY, DEFAULT_SOCKS_PROXY.getHost());
            port = parseSocksPort(getConfig().getString(SOCKS_PROXY_PORT_KEY));
            version =
                    getString(
                            SOCKS_PROXY_VERSION_KEY,
                            String.valueOf(DEFAULT_SOCKS_PROXY.getVersion().number()));

            useSocksProxy = getBoolean(USE_SOCKS_PROXY_KEY, false);
        }

        socksProxy =
                new org.zaproxy.zap.network.SocksProxy(
                        host,
                        port,
                        org.zaproxy.zap.network.SocksProxy.Version.from(version),
                        useDns);
        if (useSocksProxy) {
            apply(socksProxy);
        }

        socksProxyPasswordAuth =
                new PasswordAuthentication(
                        getString(SOCKS_PROXY_USERNAME_KEY, ""),
                        getString(SOCKS_PROXY_PASSWORD_KEY, "").toCharArray());
    }

    private static int parseSocksPort(String value) {
        if (value == null || value.isEmpty()) {
            return DEFAULT_SOCKS_PROXY.getPort();
        }

        int port;
        try {
            port = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("Failed to parse the SOCKS port: {}", value, e);
            return DEFAULT_SOCKS_PROXY.getPort();
        }

        if (port > 0 && port <= 65535) {
            return port;
        }

        log.warn("Invalid SOCKS port: {}", value);
        return DEFAULT_SOCKS_PROXY.getPort();
    }

    /**
     * Applies the given SOCKS proxy configuration to the SOCKS system properties.
     *
     * <p>If the SOCKS proxy is not in use (i.e. {@link #useSocksProxy} is {@code false}) the system
     * properties are cleared.
     *
     * @param socksProxy the SOCKS proxy to apply.
     */
    private void apply(org.zaproxy.zap.network.SocksProxy socksProxy) {
        String host = "";
        String port = "";
        String version = "";
        if (useSocksProxy) {
            host = socksProxy.getHost();
            port = Integer.toString(socksProxy.getPort());
            version = Integer.toString(socksProxy.getVersion().number());
        }
        System.setProperty("socksProxyHost", host);
        System.setProperty("socksProxyPort", port);
        System.setProperty("socksProxyVersion", version);
    }

    /**
     * Tells whether or not the given hostname should be resolved.
     *
     * <p>The names should not be resolved when ZAP is configured to use a SOCKSv5 proxy and rely on
     * it for resolution.
     *
     * <p><strong>Note:</strong> Not part of the public API.
     *
     * @param hostname the name to check.
     * @return {@code true} if the given {@code hostname} should be resolved, {@code false}
     *     otherwise.
     */
    @ZapApiIgnore
    public boolean shouldResolveRemoteHostname(String hostname) {
        if (!useSocksProxy
                || !socksProxy.isUseDns()
                || socksProxy.getVersion() != org.zaproxy.zap.network.SocksProxy.Version.SOCKS5) {
            return true;
        }
        return LOOPBACK_PATTERN.matcher(hostname).matches();
    }

    /**
     * Tells whether or not the outgoing connections should use the SOCKS proxy.
     *
     * @return {@code true} if outgoing connections should use the SOCKS proxy, {@code false}
     *     otherwise.
     * @since 2.10.0
     * @see #setUseSocksProxy(boolean)
     */
    public boolean isUseSocksProxy() {
        return useSocksProxy;
    }

    /**
     * Sets whether or not the outgoing connections should use the SOCKS proxy.
     *
     * @param useSocksProxy {@code true} if outgoing connections should use the SOCKS proxy, {@code
     *     false} otherwise.
     * @since 2.10.0
     * @see #isUseSocksProxy()
     * @see #setSocksProxy(SocksProxy)
     */
    public void setUseSocksProxy(boolean useSocksProxy) {
        if (this.useSocksProxy == useSocksProxy) {
            return;
        }

        this.useSocksProxy = useSocksProxy;

        getConfig().setProperty(USE_SOCKS_PROXY_KEY, useSocksProxy);

        apply(socksProxy);
    }

    /**
     * Gets the SOCKS proxy for outgoing connections.
     *
     * @return the SOCKS proxy, never {@code null}.
     * @since 2.10.0
     * @see #isUseSocksProxy()
     * @see #setSocksProxy(SocksProxy)
     */
    @ZapApiIgnore
    public org.zaproxy.zap.network.SocksProxy getSocksProxy() {
        return socksProxy;
    }

    /**
     * Sets the SOCKS proxy for outgoing connections.
     *
     * @param socksProxy the SOCKS proxy.
     * @throws NullPointerException if the given {@code socksProxy} is {@code null}.
     * @since 2.10.0
     * @see #getSocksProxy()
     * @see #setUseSocksProxy(boolean)
     */
    public void setSocksProxy(org.zaproxy.zap.network.SocksProxy socksProxy) {
        if (this.socksProxy.equals(socksProxy)) {
            return;
        }

        this.socksProxy = Objects.requireNonNull(socksProxy);

        getConfig().setProperty(SOCKS_PROXY_HOST_KEY, socksProxy.getHost());
        getConfig().setProperty(SOCKS_PROXY_PORT_KEY, socksProxy.getPort());
        getConfig().setProperty(SOCKS_PROXY_VERSION_KEY, socksProxy.getVersion().number());
        getConfig().setProperty(SOCKS_PROXY_DNS_KEY, socksProxy.isUseDns());

        if (useSocksProxy) {
            apply(socksProxy);
        }
    }

    /**
     * Gets the SOCKS proxy password authentication.
     *
     * @return the SOCKS proxy password authentication, never {@code null}.
     * @since 2.10.0
     * @see #isUseSocksProxy()
     * @see #setSocksProxyPasswordAuth(PasswordAuthentication)
     */
    @ZapApiIgnore
    public PasswordAuthentication getSocksProxyPasswordAuth() {
        return socksProxyPasswordAuth;
    }

    /**
     * Sets the SOCKS proxy password authentication.
     *
     * @param passwordAuth the password authentication.
     * @throws NullPointerException if the given {@code passwordAuth} is {@code null}.
     * @since 2.10.0
     * @see #getSocksProxyPasswordAuth()
     */
    public void setSocksProxyPasswordAuth(PasswordAuthentication passwordAuth) {
        this.socksProxyPasswordAuth = Objects.requireNonNull(passwordAuth);

        getConfig().setProperty(SOCKS_PROXY_USERNAME_KEY, passwordAuth.getUserName());
        getConfig().setProperty(SOCKS_PROXY_PASSWORD_KEY, new String(passwordAuth.getPassword()));
    }
}
