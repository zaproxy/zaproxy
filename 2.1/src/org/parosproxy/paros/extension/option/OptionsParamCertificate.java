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
// ZAP: 2012/04/25 Added @Override annotation to the appropriate method.
// ZAP: 2013/01/25 Removed the "(non-Javadoc)" comments.
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments

package org.parosproxy.paros.extension.option;

import java.io.File;

import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.parosproxy.paros.common.AbstractParam;
import org.parosproxy.paros.network.SSLConnector;

import ch.csnc.extension.httpclient.SSLContextManager;

public class OptionsParamCertificate extends AbstractParam {

   

    private static final String USE_CLIENT_CERT = "certificate.use";
    private static final String CLIENT_CERT_LOCATION = "certificate.clientCertLocation";

    private int useClientCert = 0;
    private String clientCertLocation = "";
    
    public OptionsParamCertificate() {
    }

    @Override
    protected void parse() {

        // always turn off client cert
        setUseClientCert(false);
        setClientCertLocation("");
        
    }

    /**
     * @return Returns the client cert location.
     */
    public String getClientCertLocation() {
        return clientCertLocation;
    }
    
    public void setClientCertLocation(String clientCertLocation) {
        if (clientCertLocation != null && !clientCertLocation.equals("")) {
            File file = new File(clientCertLocation);
            if (!file.exists()) {
                setUseClientCert(false);
                return;
            }
        } else {
            setUseClientCert(false);   
        }
        this.clientCertLocation = clientCertLocation;   
        getConfig().setProperty(CLIENT_CERT_LOCATION, clientCertLocation);
    }

    public boolean isUseClientCert() {
        return (useClientCert != 0);
    }

    private void setUseClientCert(boolean isUse) {
    	if (isUse) {
            useClientCert = 1;
            getConfig().setProperty(USE_CLIENT_CERT, Integer.toString(useClientCert));
            return;
        }
        useClientCert = 0;
        getConfig().setProperty(USE_CLIENT_CERT, Integer.toString(useClientCert));
    }
        

    
    public void setEnableCertificate(boolean enabled) {
        ProtocolSocketFactory sslFactory = Protocol.getProtocol("https").getSocketFactory();
        
        if (sslFactory instanceof SSLConnector) { 
	    	SSLConnector ssl = (SSLConnector) sslFactory;
	        ssl.setEnableClientCert(enabled);
	        
	        setUseClientCert(enabled);
        }
    }
    
    public void setActiveCertificate(){
    	
    	ProtocolSocketFactory sslFactory = Protocol.getProtocol("https").getSocketFactory();
        
        if (sslFactory instanceof SSLConnector) { 
        	SSLConnector ssl = (SSLConnector) sslFactory;
        	ssl.setActiveCertificate();
        }
    	
    }
    
    
    public SSLContextManager getSSLContextManager(){
		
    	ProtocolSocketFactory sslFactory = Protocol.getProtocol("https").getSocketFactory();
        if (sslFactory instanceof SSLConnector) { 
        	SSLConnector ssl = (SSLConnector) sslFactory;
        
        	return ssl.getSSLContextManager();
        }
        return null;
	}

    
}
