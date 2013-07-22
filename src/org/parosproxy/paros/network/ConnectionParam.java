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

package org.parosproxy.paros.network;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.httpclient.HttpState;
import org.apache.log4j.Logger;
import org.parosproxy.paros.common.AbstractParam;

public class ConnectionParam extends AbstractParam {

    // ZAP: Added logger
    private static Logger log = Logger.getLogger(ConnectionParam.class);

	private static final String CONNECTION_BASE_KEY = "connection";

	private static final String PROXY_CHAIN_NAME = CONNECTION_BASE_KEY + ".proxyChain.hostName";
	private static final String PROXY_CHAIN_PORT = CONNECTION_BASE_KEY + ".proxyChain.port";
	private static final String PROXY_CHAIN_SKIP_NAME = CONNECTION_BASE_KEY + ".proxyChain.skipName";
	private static final String PROXY_CHAIN_REALM = CONNECTION_BASE_KEY + ".proxyChain.realm";
	private static final String PROXY_CHAIN_USER_NAME = CONNECTION_BASE_KEY + ".proxyChain.userName";
	private static final String PROXY_CHAIN_PASSWORD = CONNECTION_BASE_KEY + ".proxyChain.password";

    private static final String AUTH_KEY = CONNECTION_BASE_KEY + ".auths";
    private static final String ALL_AUTHS_KEY = AUTH_KEY + ".auth";
    private static final String AUTH_NAME_KEY = "name";
    private static final String AUTH_HOST_NAME_KEY = "hostName";
    private static final String AUTH_PORT_KEY = "port";
    private static final String AUTH_USER_NAME_KEY = "userName";
    private static final String AUTH_PASSWORD_KEY = "password";
    private static final String AUTH_REALM_KEY = "realm";
    private static final String AUTH_ENABLED_KEY = "enabled";
    
    // ZAP: Added prompt option and timeout
	private static final String PROXY_CHAIN_PROMPT = CONNECTION_BASE_KEY + ".proxyChain.prompt";
	private static final String TIMEOUT_IN_SECS = CONNECTION_BASE_KEY + ".timeoutInSecs";
	private static final String SSL_CONNECT_PORTS = CONNECTION_BASE_KEY + ".sslConnectPorts";
	private static final String SINGLE_COOKIE_REQUEST_HEADER = CONNECTION_BASE_KEY + ".singleCookieRequestHeader";
    
    private static final String CONFIRM_REMOVE_AUTH_KEY = CONNECTION_BASE_KEY + ".confirmRemoveAuth";

	private String proxyChainName = "";
	private int proxyChainPort = 8080;
	private String proxyChainSkipName = "";
	private String proxyChainRealm = "";
	private String proxyChainUserName = "";
	private String proxyChainPassword = "";
	private HttpState httpState = null;
	private boolean httpStateEnabled = false;
	private List<HostAuthentication> listAuth = new ArrayList<>(0);
    private List<HostAuthentication> listAuthEnabled = new ArrayList<>(0);
	
	// ZAP: Added prompt option and timeout
	private boolean proxyChainPrompt = false;
	private int timeoutInSecs = 120;

	private Pattern	patternSkip = null;
	
	private Set<Integer> portsForSslTunneling = new HashSet<>();
	
	private boolean singleCookieRequestHeader = true;
	
	private boolean confirmRemoveAuth = true;

	/**
     * @return Returns the httpStateEnabled.
     */
    public boolean isHttpStateEnabled() {
        return httpStateEnabled;
    }
    /**
     * @param httpStateEnabled The httpStateEnabled to set.
     */
    public void setHttpStateEnabled(boolean httpStateEnabled) {
        this.httpStateEnabled = httpStateEnabled;
        if (this.httpStateEnabled) {
    	    httpState = new HttpState();
        } else {
            httpState = null;
        }
    }
	
	public ConnectionParam() {
	}
	
	@Override
	protected void parse() {

		setProxyChainName(getConfig().getString(PROXY_CHAIN_NAME, ""));
		try {
			setProxyChainPort(getConfig().getInt(PROXY_CHAIN_PORT, 8080));
		} catch (Exception e) {
        	// ZAP: Log exceptions
        	log.error(e.getMessage(), e);
		}
		try {
			setProxyChainSkipName(getConfig().getString(PROXY_CHAIN_SKIP_NAME, ""));
			setProxyChainRealm(getConfig().getString(PROXY_CHAIN_REALM, ""));
			setProxyChainUserName(getConfig().getString(PROXY_CHAIN_USER_NAME, ""));
		} catch (Exception e) {
        	// ZAP: Log exceptions
        	log.error(e.getMessage(), e);
		}
		try {
			// ZAP: Added prompt option
			if (getConfig().getProperty(PROXY_CHAIN_PROMPT) instanceof String &&
					((String)getConfig().getProperty(PROXY_CHAIN_PROMPT)).isEmpty()) {
				// In 1.2.0 the default for this field was empty, which causes a crash in 1.3.*
				setProxyChainPrompt(false);
			} else if (getConfig().getBoolean(PROXY_CHAIN_PROMPT, false)) {
				setProxyChainPrompt(true);
			} else {
				setProxyChainPrompt(false);
				setProxyChainPassword(getConfig().getString(PROXY_CHAIN_PASSWORD, ""));
			}
		} catch (Exception e) {
        	// ZAP: Log exceptions
        	log.error(e.getMessage(), e);
		}
		
		try {
			setPortsForSslTunneling(getConfig().getString(SSL_CONNECT_PORTS, "443"));
		} catch (Exception e) {
	    	// ZAP: Log exceptions
	    	log.error(e.getMessage(), e);
		}
		
		try {
			setTimeoutInSecs(getConfig().getInt(TIMEOUT_IN_SECS, 20));
		} catch (Exception e) {
        	// ZAP: Log exceptions
        	log.error(e.getMessage(), e);
		}
		
		parseAuthentication();

        try {
            this.confirmRemoveAuth = getConfig().getBoolean(CONFIRM_REMOVE_AUTH_KEY, true);
        } catch (ConversionException e) {
            log.error("Error while loading the confirm remove option: " + e.getMessage(), e);
        }
        
        try {
            this.singleCookieRequestHeader = getConfig().getBoolean(SINGLE_COOKIE_REQUEST_HEADER, true);
        } catch (ConversionException e) {
            log.error("Error while loading the option singleCookieRequestHeader: " + e.getMessage(), e);
        }
	}
	
	
	public String getProxyChainName() {
		return proxyChainName;
	}
	
	public void setProxyChainName(String proxyChainName) {
		this.proxyChainName = proxyChainName.trim();
		getConfig().setProperty(PROXY_CHAIN_NAME, this.proxyChainName);
	}
	
	public int getProxyChainPort() {
		return proxyChainPort;
	}
	
	public void setProxyChainPort(int proxyChainPort) {
		this.proxyChainPort = proxyChainPort;
		getConfig().setProperty(PROXY_CHAIN_PORT, Integer.toString(this.proxyChainPort));
	}

	public String getProxyChainSkipName() {
		return proxyChainSkipName;
	}
	
	public void setProxyChainSkipName(String proxyChainSkipName) {
		this.proxyChainSkipName = proxyChainSkipName.trim();
		getConfig().setProperty(PROXY_CHAIN_SKIP_NAME, this.proxyChainSkipName);
		parseProxyChainSkip(this.proxyChainSkipName);
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
	Check if via proxy chain.
	@return	True = use proxy chain
	*/
	private boolean isUseProxyChain() {
		if (getProxyChainName().equals("")) {
			return false;
		} else {
			return true;
		}
	}
	
	/**
	Check if the given host name is in the proxy chain skip list
	@param	hostName	Host name to be checked.
	*/
	private boolean isSkipProxyChain(String hostName) {
		if (patternSkip == null || hostName == null) {
			return false;
		}
		
		return patternSkip.matcher(hostName).find();
	}
	
	/**
	Check if given host name need to send using proxy.
	@param	hostName	host name to be checked.
	@return	true = need to send via proxy.
	*/
	public boolean isUseProxy(String hostName) {
		if (!isUseProxyChain() || isSkipProxyChain(hostName)) {
			return false;
		} else {
			return true;
		}
	}
	
	/**
	Parse the proxy chain skip text string and build the regex pattern.
	*/
	private void parseProxyChainSkip(String skipName) {
		patternSkip = null;

		if (skipName == null || skipName.equals("")) {
			return;
		}
		
		skipName = skipName.replaceAll("\\.", "\\\\.");
		skipName = skipName.replaceAll("\\*",".*?").replaceAll("(;+$)|(^;+)", "");
		skipName = "(" + skipName.replaceAll(";+", "|") + ")$";
		patternSkip = Pattern.compile(skipName, Pattern.CASE_INSENSITIVE);
	}

    /**
     * @return Returns the listAuth.
     */
    public List<HostAuthentication> getListAuth() {
        return listAuth;
    }
    
    public List<HostAuthentication> getListAuthEnabled() {
        return listAuthEnabled;
    }
    
    /**
     * @param listAuth The listAuth to set.
     */
    public void setListAuth(List<HostAuthentication> listAuth) {
        this.listAuth = new ArrayList<>(listAuth);
        
        ((HierarchicalConfiguration) getConfig()).clearTree(ALL_AUTHS_KEY);
        
        ArrayList<HostAuthentication> enabledAuths = new ArrayList<>(listAuth);
        for (int i = 0, size = listAuth.size(); i < size; ++i) {
            String elementBaseKey = ALL_AUTHS_KEY + "(" + i + ").";
            HostAuthentication auth = listAuth.get(i);
            
            getConfig().setProperty(elementBaseKey + AUTH_NAME_KEY, auth.getName());
            getConfig().setProperty(elementBaseKey + AUTH_HOST_NAME_KEY, auth.getHostName());
            getConfig().setProperty(elementBaseKey + AUTH_PORT_KEY, Integer.valueOf(auth.getPort()));
            getConfig().setProperty(elementBaseKey + AUTH_USER_NAME_KEY, auth.getUserName());
            getConfig().setProperty(elementBaseKey + AUTH_PASSWORD_KEY, auth.getPassword());
            getConfig().setProperty(elementBaseKey + AUTH_REALM_KEY, auth.getRealm());
            getConfig().setProperty(elementBaseKey + AUTH_ENABLED_KEY, Boolean.valueOf(auth.isEnabled()));
            
            if (auth.isEnabled()) {
                enabledAuths.add(auth);
            }
        }
        
        enabledAuths.trimToSize();
        this.listAuthEnabled = enabledAuths;
    }
    
    private void parseAuthentication() {
        List<HierarchicalConfiguration> fields = ((HierarchicalConfiguration) getConfig()).configurationsAt(ALL_AUTHS_KEY);
        this.listAuth = new ArrayList<>(fields.size());
        ArrayList<HostAuthentication> enabledAuths = new ArrayList<>(fields.size());
        List<String> tempListNames = new ArrayList<>(fields.size());
        for (HierarchicalConfiguration sub : fields) {
            String name = sub.getString(AUTH_NAME_KEY, "");
            if (!"".equals(name) && !tempListNames.contains(name)) {
                tempListNames.add(name);
                
                String host = sub.getString(AUTH_HOST_NAME_KEY, "");
                if ("".equals(host)) {
                    break;
                }
                
                HostAuthentication auth = new HostAuthentication(
                        name,
                        host,
                        sub.getInt(AUTH_PORT_KEY),
                        sub.getString(AUTH_USER_NAME_KEY),
                        sub.getString(AUTH_PASSWORD_KEY),
                        sub.getString(AUTH_REALM_KEY));
                
                auth.setEnabled(sub.getBoolean(AUTH_ENABLED_KEY, true));
                
                listAuth.add(auth);
                
                if (auth.isEnabled()) {
                    enabledAuths.add(auth);
                }
            }
        }
        
        enabledAuths.trimToSize();
        this.listAuthEnabled = enabledAuths;
    }
    
    /**
     * @return Returns the httpState.
     */
    public HttpState getHttpState() {
        return httpState;
    }
    /**
     * @param httpState The httpState to set.
     */
    public void setHttpState(HttpState httpState) {
        this.httpState = httpState;
    }
	public int getTimeoutInSecs() {
		return timeoutInSecs;
	}
	public void setTimeoutInSecs(int timeoutInSecs) {
		this.timeoutInSecs = timeoutInSecs;
		getConfig().setProperty(TIMEOUT_IN_SECS, this.timeoutInSecs);
	}
    
    public boolean isConfirmRemoveAuth() {
        return this.confirmRemoveAuth;
    }
    
    public void setConfirmRemoveAuth(boolean confirmRemove) {
        this.confirmRemoveAuth = confirmRemove;
        getConfig().setProperty(CONFIRM_REMOVE_AUTH_KEY, Boolean.valueOf(confirmRemoveAuth));
    }
    
    /**
	 * Returns true if a SSL tunnel should be created when the following request
	 * is issued by the client: "CONNECT url:port HTTP/1.1".
	 * 
	 * @param port
	 * @return
	 */
	public boolean isPortDemandingSslTunnel(Integer port) {
		if (port == null) {
			// no port defaults to 80
			port = 80;
		}
		return portsForSslTunneling.contains(port);
	}
	
	public String getPortsForSslTunneling() {
		String ports = "";
		boolean first = true;
		for (Integer port : portsForSslTunneling) {
			if (first) {
				first = false;
			} else {
				ports += ",";
			}
			ports += port;
		}
		return ports;
	}
	
	public void setPortsForSslTunneling(String ports) {
		String[] parsedPorts = ports.split(",");
		
		portsForSslTunneling.clear();
		for (String port : parsedPorts) {
			portsForSslTunneling.add(Integer.valueOf(port));
		}
		getConfig().setProperty(SSL_CONNECT_PORTS, getPortsForSslTunneling());
	}
	
	/**
	 * Tells whether the cookies should be set on a single "Cookie" request header or multiple "Cookie" request headers, when
	 * sending an HTTP request to the server.
	 * 
	 * @return {@code true} if the cookies should be set on a single request header, {@code false} otherwise
	 */
	public boolean isSingleCookieRequestHeader() {
		return this.singleCookieRequestHeader;
	}
	
	/**
	 * Sets whether the cookies should be set on a single "Cookie" request header or multiple "Cookie" request headers, when
	 * sending an HTTP request to the server.
	 * 
	 * @param singleCookieRequestHeader {@code true} if the cookies should be set on a single request header, {@code false}
	 *            otherwise
	 */
	public void setSingleCookieRequestHeader(boolean singleCookieRequestHeader) {
		this.singleCookieRequestHeader = singleCookieRequestHeader;
		getConfig().setProperty(SINGLE_COOKIE_REQUEST_HEADER, Boolean.valueOf(singleCookieRequestHeader));
	}
}
