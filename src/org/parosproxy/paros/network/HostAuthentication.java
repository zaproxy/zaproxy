/*
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
// ZAP: 2012/11/15 Issue 416: Normalise how multiple related options are managed
// throughout ZAP and enhance the usability of some options.

// ZAP: 2012/11/27 Issue 376: Masking the passwords provided for Authentication
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
package org.parosproxy.paros.network;

import org.zaproxy.zap.utils.Enableable;

public class HostAuthentication extends Enableable {

    private String  name = "";
    private String	hostName = "";
    private int		port		= 80;
    private String	userName = "";
    private String	password = "";
    private String	realm	= "";
	private boolean maskedPassword;

    public HostAuthentication() {
        
    }
    
    public HostAuthentication(String name, String hostName, int port, String userName, String password, String realm) {
        super();
        setName(name);
        setHostName(hostName);
        setPort(port);
        setUserName(userName);
        setPassword(password);
        setRealm(realm);
    }
    
    public HostAuthentication(HostAuthentication auth) {
        this(auth.name, auth.hostName, auth.port, auth.userName, auth.password, auth.realm);
        setMasked(auth.isMasked());
        setEnabled(auth.isEnabled());
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns the hostName.
     */
    public String getHostName() {
        return hostName;
    }
    /**
     * @param hostName The hostName to set.
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
    /**
     * @return Returns the password.
     */
    public String getPassword() {
        return password;
    }
    /**
     * @param password The password to set.
     */
    public void setPassword(String password) {
        this.password = password;
    }
    /**
     * @return Returns the port.
     */
    public int getPort() {
        return port;
    }
    /**
     * @param port The port to set.
     */
    public void setPort(int port) {
        this.port = port;
    }
    /**
     * @return Returns the realm.
     */
    public String getRealm() {
        return realm;
    }
    /**
     * @param realm The realm to set.
     */
    public void setRealm(String realm) {
        this.realm = realm;
    }
    /**
     * @return Returns the userName.
     */
    public String getUserName() {
        return userName;
    }
    /**
     * @param userName The userName to set.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((hostName == null) ? 0 : hostName.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        result = prime * result + port;
        result = prime * result + ((realm == null) ? 0 : realm.hashCode());
        result = prime * result + ((userName == null) ? 0 : userName.hashCode());
        result = prime * result + (Boolean.valueOf(maskedPassword).hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        HostAuthentication other = (HostAuthentication) obj;
        if (hostName == null) {
            if (other.hostName != null) {
                return false;
            }
        } else if (!hostName.equals(other.hostName)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (password == null) {
            if (other.password != null) {
                return false;
            }
        } else if (!password.equals(other.password)) {
            return false;
        }
        if (port != other.port) {
            return false;
        }
        if (realm == null) {
            if (other.realm != null) {
                return false;
            }
        } else if (!realm.equals(other.realm)) {
            return false;
        }
        if (userName == null) {
            if (other.userName != null) {
                return false;
            }
        } else if (!userName.equals(other.userName)) {
            return false;
        } 
        if(other.maskedPassword != maskedPassword){
        	return false;
        }
        return true;
    }

	public boolean isMasked() {
		return maskedPassword;
	}

	public void setMasked(boolean selected) {
		maskedPassword = selected;
	}
}
