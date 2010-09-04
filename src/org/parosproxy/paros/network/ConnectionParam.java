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
package org.parosproxy.paros.network;

import java.util.Vector;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpState;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.parosproxy.paros.common.AbstractParam;




/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ConnectionParam extends AbstractParam {

    // ZAP: Added logger
    private static Log log = LogFactory.getLog(ConnectionParam.class);

//	private static final String CONNECTION = "connection";

	private static final String PROXY_CHAIN_NAME = "connection.proxyChain.hostName";
	private static final String PROXY_CHAIN_PORT = "connection.proxyChain.port";
	private static final String PROXY_CHAIN_SKIP_NAME = "connection.proxyChain.skipName";
	private static final String PROXY_CHAIN_REALM = "connection.proxyChain.realm";
	private static final String PROXY_CHAIN_USER_NAME = "connection.proxyChain.userName";
	private static final String PROXY_CHAIN_PASSWORD = "connection.proxyChain.password";
	// ZAP: Added prompt option
	private static final String PROXY_CHAIN_PROMPT = "connection.proxyChain.prompt";

	
	private String proxyChainName = "";
	private int proxyChainPort = 8080;
	private String proxyChainSkipName = "";
	private String proxyChainRealm = "";
	private String proxyChainUserName = "";
	private String proxyChainPassword = "";
	private HttpState httpState = null;
	private boolean httpStateEnabled = false;
	private Vector listAuth = new Vector();
	// ZAP: Added prompt option
	private boolean proxyChainPrompt = false;

	private Pattern					patternSkip = null;

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
	
	protected void parse() {

		setProxyChainName(getConfig().getString(PROXY_CHAIN_NAME, ""));
		try {
			setProxyChainPort(getConfig().getInt(PROXY_CHAIN_PORT, 8080));
		} catch (Exception e) {
        	// ZAP: Log exceptions
        	log.warn(e.getMessage(), e);
		}
		setProxyChainSkipName(getConfig().getString(PROXY_CHAIN_SKIP_NAME, ""));
		setProxyChainRealm(getConfig().getString(PROXY_CHAIN_REALM, ""));
		setProxyChainUserName(getConfig().getString(PROXY_CHAIN_USER_NAME, ""));
		// ZAP: Added prompt option
		if (getConfig().getBoolean(PROXY_CHAIN_PROMPT, false)) {
			setProxyChainPrompt(true);
		} else {
			setProxyChainPrompt(false);
			setProxyChainPassword(getConfig().getString(PROXY_CHAIN_PASSWORD, ""));
		}
		
		parseAuthentication();
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
    public Vector getListAuth() {
        return listAuth;
    }
    /**
     * @param listAuth The listAuth to set.
     */
    public void setListAuth(Vector listAuth) {
        this.listAuth = listAuth;
        HostAuthentication auth = null;
        
        for (int i=0; i<((listAuth.size() > 100)? listAuth.size(): 100); i++) {
            // clearProperty doesn't work.  So set all host name to blank as a workaround.
            getConfig().clearProperty(getAuth(i, AUTH_HOST_NAME));            
            getConfig().clearProperty(getAuth(i, AUTH_PORT));            
            getConfig().clearProperty(getAuth(i, AUTH_USER_NAME));            
            getConfig().clearProperty(getAuth(i, AUTH_PASSWORD));            
            getConfig().clearProperty(getAuth(i, AUTH_REALM));
            getConfig().clearProperty(AUTH + ".A"+i);
        }
        for (int i=0; i<listAuth.size(); i++) {
            auth = (HostAuthentication) listAuth.get(i);            
            getConfig().setProperty(getAuth(i, AUTH_HOST_NAME), auth.getHostName());
            getConfig().setProperty(getAuth(i, AUTH_PORT), Integer.toString(auth.getPort()));
            getConfig().setProperty(getAuth(i, AUTH_USER_NAME), auth.getUserName());
            getConfig().setProperty(getAuth(i, AUTH_PASSWORD), auth.getPassword());
            getConfig().setProperty(getAuth(i, AUTH_REALM), auth.getRealm());
            
        }
        
    }

    private static final String AUTH = "connection.auth";
    private static final String AUTH_HOST_NAME = "hostName";
    private static final String AUTH_PORT = "port";
    private static final String AUTH_USER_NAME = "userName";
    private static final String AUTH_PASSWORD = "password";
    private static final String AUTH_REALM = "realm";
    
    
    private String getAuth(int i, String name) {
        return AUTH + ".A" + i + "." + name;
    }
    
    private void parseAuthentication() {
        listAuth.clear();

        String host = "";
        for (int i=0; host != null; i++) {

            host = getConfig().getString(getAuth(i, AUTH_HOST_NAME));
            if (host == null) {
                   break;
            }
            
            if (host.equals("")) {
                break;
            }
            
            HostAuthentication auth = new HostAuthentication(
                    host,
                    getConfig().getInt(getAuth(i, AUTH_PORT)),
                    getConfig().getString(getAuth(i, AUTH_USER_NAME)),
                    getConfig().getString(getAuth(i, AUTH_PASSWORD)),
                    getConfig().getString(getAuth(i, AUTH_REALM))
                    );
            listAuth.add(auth);
            
            
        }

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
}
