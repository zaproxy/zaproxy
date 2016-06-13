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
// ZAP: 2012/04/25 Added @Override annotation to the appropriate method.
// ZAP: 2012/11/04 Issue 408: Add support to encoding transformations, added an
// option to control whether the "Accept-Encoding" request-header field is 
// modified/removed or not.
// ZAP: 2013/01/23 Clean up of exception handling/logging.
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2014/01/22 Add the possibility to bound the proxy to all interfaces if null IP address has been set
// ZAP: 2014/03/06 Issue 1063: Add option to decode all gzipped content
// ZAP: 2014/03/23 Issue 968: Allow to choose the enabled SSL/TLS protocols
// ZAP: 2014/03/23 Issue 1017: Proxy set to 0.0.0.0 causes incorrect PAC file to be generated
// ZAP: 2015/11/04 Issue 1920: Report the host:port ZAP is listening on in daemon mode, or exit if it cant
// ZAP: 2016/06/13 Change option "Accept-Encoding" request-header to Remove Unsupported Encodings

package org.parosproxy.paros.core.proxy;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.log4j.Logger;
import org.parosproxy.paros.common.AbstractParam;
import org.parosproxy.paros.network.SSLConnector;

/**
 * @author simon
 *
 */
public class ProxyParam extends AbstractParam {

    //	private static final String PROXY = "proxy";
    private static final Logger logger = Logger.getLogger(ProxyParam.class);

    private static final String PROXY_BASE_KEY = "proxy";

    private static final String PROXY_IP = "proxy.ip";
    private static final String PROXY_PORT = "proxy.port";
    //	private static final String PROXY_SSL_IP = "proxy.SSLIp";
    //	private static final String PROXY_SSL_PORT = "proxy.SSLPort";

    private static final String USE_REVERSE_PROXY = "proxy.reverseProxy.use";
    private static final String REVERSE_PROXY_IP = "proxy.reverseProxy.ip";
    private static final String REVERSE_PROXY_HTTP_PORT = "proxy.reverseProxy.httpPort";
    private static final String REVERSE_PROXY_HTTPS_PORT = "proxy.reverseProxy.httpsPort";


    private static final String SECURITY_PROTOCOLS_ENABLED = PROXY_BASE_KEY + ".securityProtocolsEnabled";
    private static final String ALL_SECURITY_PROTOCOLS_ENABLED_KEY = SECURITY_PROTOCOLS_ENABLED + ".protocol";

    /**
     * The configuration key to save/load the option {@link #removeUnsupportedEncodings}.
     */
    private static final String REMOVE_UNSUPPORTED_ENCODINGS = "proxy.removeUnsupportedEncodings";

    /**
     * The configuration key for the option that controls whether the proxy
     * should always decode gzipped content or not.
     */
    private static final String ALWAYS_DECODE_GZIP = "proxy.decodeGzip";

    private String proxyIp = "localhost";
    private int proxyPort = 8080;
    private int proxySSLPort = 8443;
    private int useReverseProxy = 0;
    private String reverseProxyIp = "localhost";
    private int reverseProxyHttpPort = 80;
    private int reverseProxyHttpsPort = 443;

    /**
     * Flag that indicates if the proxy ip is any local address.
     * 
     * @see #parse()
     */
    private boolean proxyIpAnyLocalAddress;

    /**
     * Flag that controls whether or not the local proxy should remove unsupported encodings from the "Accept-Encoding"
     * request-header field.
     * <p>
     * Default is {@code true}.
     * 
     * @see #REMOVE_UNSUPPORTED_ENCODINGS
     * @see #setRemoveUnsupportedEncodings(boolean)
     */
    private boolean removeUnsupportedEncodings = true;
    /**
     * The option that controls whether the proxy should always decode gzipped content or not.
     */
    private boolean alwaysDecodeGzip = true;

    private String[] securityProtocolsEnabled;

    public ProxyParam() {
    }

    @Override
    protected void parse() {
        proxyIp = getConfig().getString(PROXY_IP, "localhost");
        determineProxyIpAnyLocalAddress();
        
        try {
            proxyPort = getConfig().getInt(PROXY_PORT, 8080);

        } catch (Exception e) {
        }

        try {
            proxySSLPort = 8443;	//getConfig().getInt(PROXY_SSL_PORT, 8443);
        } catch (Exception e) {
        }

        reverseProxyIp = getConfig().getString(REVERSE_PROXY_IP);
        if (reverseProxyIp.equalsIgnoreCase("localhost") || reverseProxyIp.equalsIgnoreCase("127.0.0.1")) {
            try {
                reverseProxyIp = InetAddress.getLocalHost().getHostAddress();

            } catch (UnknownHostException e1) {
                logger.error(e1.getMessage(), e1);
            }
        }

        reverseProxyHttpPort = getConfig().getInt(REVERSE_PROXY_HTTP_PORT, 80);
        reverseProxyHttpsPort = getConfig().getInt(REVERSE_PROXY_HTTPS_PORT, 443);
        useReverseProxy = getConfig().getInt(USE_REVERSE_PROXY, 0);

        removeUnsupportedEncodings = getConfig().getBoolean(REMOVE_UNSUPPORTED_ENCODINGS, true);
        alwaysDecodeGzip = getConfig().getBoolean(ALWAYS_DECODE_GZIP, true);

        loadSecurityProtocolsEnabled();
    }

    public String getProxyIp() {
        if (isProxyIpAnyLocalAddress()) {
            try {
                return InetAddress.getLocalHost().getHostAddress();

            } catch (UnknownHostException ex) {
                return "localhost";
            }

        } else {
            return proxyIp;
        }
    }
    
    // ZAP: added a method for nullable proxy...
    public String getRawProxyIP() {
    	if (proxyIp == null || proxyIp.length() == 0) {
    		return "0.0.0.0";
    	}
        return proxyIp;
    }

    public void setProxyIp(String proxyIp) {
        if (this.proxyIp.equals(proxyIp)) {
            return;
        }
        this.proxyIp = proxyIp.trim();
        getConfig().setProperty(PROXY_IP, this.proxyIp);
        determineProxyIpAnyLocalAddress();

    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
        getConfig().setProperty(PROXY_PORT, Integer.toString(this.proxyPort));
    }

    public int getProxySSLPort() {
        return proxySSLPort;
    }

    //	public void setProxySSLPort(int proxySSLPort) {
    //		this.proxySSLPort = proxySSLPort;
    //		getConfig().setProperty(PROXY_SSL_PORT, Integer.toString(this.proxySSLPort));
    //	}
    public String getReverseProxyIp() {
        return reverseProxyIp;
    }

    public void setReverseProxyIp(String reverseProxyIp) {
        this.reverseProxyIp = reverseProxyIp.trim();
        getConfig().setProperty(REVERSE_PROXY_IP, this.reverseProxyIp);

    }

    public int getReverseProxyHttpPort() {
        return reverseProxyHttpPort;
    }

    public void setReverseProxyHttpPort(int reverseProxyHttpPort) {
        this.reverseProxyHttpPort = reverseProxyHttpPort;
        getConfig().setProperty(REVERSE_PROXY_HTTP_PORT, Integer.toString(this.reverseProxyHttpPort));
    }

    public int getReverseProxyHttpsPort() {
        return reverseProxyHttpsPort;
    }

    public void setReverseProxyHttpsPort(int reverseProxyHttpsPort) {
        this.reverseProxyHttpsPort = reverseProxyHttpsPort;
        getConfig().setProperty(REVERSE_PROXY_HTTPS_PORT, Integer.toString(this.reverseProxyHttpsPort));
    }

    public boolean isUseReverseProxy() {
        return (useReverseProxy != 0);
    }

    public void setUseReverseProxy(boolean isUse) {
        if (isUse) {
            useReverseProxy = 1;
            getConfig().setProperty(USE_REVERSE_PROXY, Integer.toString(useReverseProxy));
            return;
        }
        
        useReverseProxy = 0;
        getConfig().setProperty(USE_REVERSE_PROXY, Integer.toString(useReverseProxy));

    }

    /**
     * Sets whether the proxy should modify/remove the "Accept-Encoding"
     * request-header field or not.
     *
     * @param modifyAcceptEncodingHeader {@code true} if the proxy should
     * modify/remove the "Accept-Encoding" request-header field, {@code false}
     * otherwise
     * @deprecated (TODO add version) Use {@link #setRemoveUnsupportedEncodings(boolean)} instead.
     * @since 2.0.0
     */
    @Deprecated
    public void setModifyAcceptEncodingHeader(boolean modifyAcceptEncodingHeader) {
        setRemoveUnsupportedEncodings(modifyAcceptEncodingHeader);
    }

    /**
     * Tells whether the proxy should modify/remove the "Accept-Encoding"
     * request-header field or not.
     *
     * @return {@code true} if the proxy should modify/remove the
     * "Accept-Encoding" request-header field, {@code false} otherwise
     * @deprecated (TODO add version) Use {@link #isRemoveUnsupportedEncodings()} instead.
     * @since 2.0.0
     */
    @Deprecated
    public boolean isModifyAcceptEncodingHeader() {
        return isRemoveUnsupportedEncodings();
    }

    /**
     * Sets whether or not the local proxy should remove unsupported encodings from the "Accept-Encoding" request-header field.
     * <p>
     * The whole header is removed if empty after the removal of unsupported encodings.
     *
     * @param remove {@code true} if the local proxy should remove unsupported encodings, {@code false} otherwise
     * @since TODO add version
     * @see #isRemoveUnsupportedEncodings()
     */
    public void setRemoveUnsupportedEncodings(boolean remove) {
        if (removeUnsupportedEncodings != remove) {
            this.removeUnsupportedEncodings = remove;
            getConfig().setProperty(REMOVE_UNSUPPORTED_ENCODINGS, Boolean.valueOf(removeUnsupportedEncodings));
        }
    }

    /**
     * Tells whether or not the local proxy should remove unsupported encodings from the "Accept-Encoding" request-header field.
     * <p>
     * The whole header is removed if empty after the removal of unsupported encodings.
     *
     * @return {@code true} if the local proxy should remove unsupported encodings, {@code false} otherwise
     * @since TODO add version
     * @see #setRemoveUnsupportedEncodings(boolean)
     */
    public boolean isRemoveUnsupportedEncodings() {
        return removeUnsupportedEncodings;
    }

    /**
     * Tells whether the proxy should always decode gzipped content or not.
     *
     * @return {@code true} if the proxy should always decode gzipped content, {@code false} otherwise
     */
	public boolean isAlwaysDecodeGzip() {
		return alwaysDecodeGzip;
	}

    /**
     * Sets whether the proxy should always decode gzipped content or not.
     *
     * @param alwaysDecodeGzip {@code true} if the proxy should
     * always decode gzipped content, {@code false} otherwise
     */
	public void setAlwaysDecodeGzip(boolean alwaysDecodeGzip) {
		this.alwaysDecodeGzip = alwaysDecodeGzip;
        getConfig().setProperty(ALWAYS_DECODE_GZIP, Boolean.valueOf(alwaysDecodeGzip));
	}
    
    /**
     * Returns the security protocols enabled (SSL/TLS) for outgoing connections.
     * 
     * @return the security protocols enabled for outgoing connections.
     * @since 2.3.0
     */
    public String[] getSecurityProtocolsEnabled() {
        return Arrays.copyOf(securityProtocolsEnabled, securityProtocolsEnabled.length);
    }

    /**
     * Sets the security protocols enabled (SSL/TLS) for outgoing connections.
     * <p>
     * The call has no effect if the given array is null or empty.
     * </p>
     * 
     * @param enabledProtocols the security protocols enabled (SSL/TLS) for outgoing connections.
     * @throws IllegalArgumentException if at least one of the {@code enabledProtocols} is {@code null} or empty.
     * @since 2.3.0
     */
    public void setSecurityProtocolsEnabled(String[] enabledProtocols) {
        if (enabledProtocols == null || enabledProtocols.length == 0) {
            return;
        }
        for (int i= 0; i < enabledProtocols.length; i++) {
            if (enabledProtocols[i] == null || enabledProtocols[i].isEmpty()) {
                throw new IllegalArgumentException("The parameter enabledProtocols must not contain null or empty elements.");
            }
        }

        ((HierarchicalConfiguration) getConfig()).clearTree(ALL_SECURITY_PROTOCOLS_ENABLED_KEY);

        for (int i = 0; i < enabledProtocols.length; ++i) {
            String elementBaseKey = ALL_SECURITY_PROTOCOLS_ENABLED_KEY + "(" + i + ")";
            getConfig().setProperty(elementBaseKey, enabledProtocols[i]);
        }

        this.securityProtocolsEnabled = Arrays.copyOf(enabledProtocols, enabledProtocols.length);
        SSLConnector.setServerEnabledProtocols(enabledProtocols);
    }

    private void loadSecurityProtocolsEnabled() {
        List<Object> protocols = getConfig().getList(ALL_SECURITY_PROTOCOLS_ENABLED_KEY);
        if (protocols.size() != 0) {
            securityProtocolsEnabled = new String[protocols.size()];
            securityProtocolsEnabled = protocols.toArray(securityProtocolsEnabled);
            SSLConnector.setServerEnabledProtocols(securityProtocolsEnabled);
        } else {
            setSecurityProtocolsEnabled(SSLConnector.getServerEnabledProtocols());
        }
    }

    /**
     * Tells whether or not the proxy IP is set to listen on any local address.
     * 
     * @return {@code true} if the proxy IP is set to listen on any local address, {@code false} otherwise.
     * @see #determineProxyIpAnyLocalAddress()
     * @see #getProxyIp()
     */
    public boolean isProxyIpAnyLocalAddress() {
        return proxyIpAnyLocalAddress;
    }

    /**
     * Determines if the proxy IP is set to listen on any local address and updates the corresponding flag accordingly.
     * <p>
     * The proxy IP is set to listen on any local address if it is either empty or the method
     * {@code InetAddress#isAnyLocalAddress()} called on the proxy IP returns {@code true}.
     * 
     * @see #proxyIpAnyLocalAddress
     * @see #proxyIp
     * @see #isProxyIpAnyLocalAddress()
     * @see InetAddress#isAnyLocalAddress()
     */
    private void determineProxyIpAnyLocalAddress() {
        try {
            proxyIpAnyLocalAddress = proxyIp.isEmpty() || InetAddress.getByName(proxyIp).isAnyLocalAddress();
        } catch (UnknownHostException e) {
            proxyIpAnyLocalAddress = false;
        }
    }
}
