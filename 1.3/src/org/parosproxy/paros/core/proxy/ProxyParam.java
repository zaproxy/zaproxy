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
package org.parosproxy.paros.core.proxy;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.parosproxy.paros.common.AbstractParam;



/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ProxyParam extends AbstractParam {

//	private static final String PROXY = "proxy";
	
	private static final String PROXY_IP = "proxy.ip";
	private static final String PROXY_PORT = "proxy.port";
//	private static final String PROXY_SSL_IP = "proxy.SSLIp";
//	private static final String PROXY_SSL_PORT = "proxy.SSLPort";
	
	private static final String USE_REVERSE_PROXY = "proxy.reverseProxy.use";
	private static final String REVERSE_PROXY_IP = "proxy.reverseProxy.ip";
	private static final String REVERSE_PROXY_HTTP_PORT = "proxy.reverseProxy.httpPort";
	private static final String REVERSE_PROXY_HTTPS_PORT = "proxy.reverseProxy.httpsPort";	
		
	private String proxyIp = "localhost";
	private int proxyPort = 8080;
	private int proxySSLPort = 8443;
	private int useReverseProxy = 0;
	private String reverseProxyIp = "localhost";
	private int reverseProxyHttpPort = 80;
	private int reverseProxyHttpsPort = 443;
		
	public ProxyParam() {
	}
	
	protected void parse() {
		proxyIp = getConfig().getString(PROXY_IP, "localhost");
		try {
			proxyPort = getConfig().getInt(PROXY_PORT, 8080);
		} catch (Exception e) {}

		try {
			proxySSLPort = 8443;	//getConfig().getInt(PROXY_SSL_PORT, 8443);
		} catch (Exception e) {}

		reverseProxyIp = getConfig().getString(REVERSE_PROXY_IP);
		if (reverseProxyIp.equalsIgnoreCase("localhost") || reverseProxyIp.equalsIgnoreCase("127.0.0.1")) {
		    try {
                reverseProxyIp = InetAddress.getLocalHost().getHostAddress();

            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            }
		}
		
		reverseProxyHttpPort = getConfig().getInt(REVERSE_PROXY_HTTP_PORT, 80);
		reverseProxyHttpsPort = getConfig().getInt(REVERSE_PROXY_HTTPS_PORT, 443);
		useReverseProxy = getConfig().getInt(USE_REVERSE_PROXY, 0);

	}
	
	public String getProxyIp() {
		return proxyIp;
	}
	
	public void setProxyIp(String proxyIp) {
		this.proxyIp = proxyIp.trim();
		getConfig().setProperty(PROXY_IP, this.proxyIp);
		
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

}
