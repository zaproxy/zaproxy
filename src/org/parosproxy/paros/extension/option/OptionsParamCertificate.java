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

package org.parosproxy.paros.extension.option;

import java.io.File;

import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.parosproxy.paros.common.AbstractParam;
import org.parosproxy.paros.network.SSLConnector;

import ch.csnc.extension.httpclient.SSLContextManager;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class OptionsParamCertificate extends AbstractParam {

   

    private static final String USE_CLIENT_CERT = "certificate.use";
    private static final String CLIENT_CERT_LOCATION = "certificate.clientCertLocation";

    private int useClientCert = 0;
    private String clientCertLocation = "";
    
    /**
     * @param rootElementName
     */
    public OptionsParamCertificate() {
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.common.FileXML#parse()
     */
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
        
    	SSLConnector ssl = (SSLConnector) sslFactory;
        ssl.setEnableClientCert(enabled);
        
        setUseClientCert(enabled);
    }
    
    public void setActiveCertificate(){
    	
    	ProtocolSocketFactory sslFactory = Protocol.getProtocol("https").getSocketFactory();
        
    	SSLConnector ssl = (SSLConnector) sslFactory;
        ssl.setActiveCertificate();
    	
    }
    
    
    public SSLContextManager getSSLContextManager(){
		
    	ProtocolSocketFactory sslFactory = Protocol.getProtocol("https").getSocketFactory();
    	SSLConnector ssl = (SSLConnector) sslFactory;
        
        return ssl.getSSLContextManager();
	}

    
}
