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
// ZAP: 2014/03/23 Issue 412: Enable unsafe SSL/TLS renegotiation option not saved
// ZAP: 2014/08/14 Issue 1184: Improve support for IBM JDK

package org.parosproxy.paros.extension.option;

import java.io.File;

import org.apache.commons.configuration.ConversionException;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.log4j.Logger;
import org.parosproxy.paros.common.AbstractParam;
import org.parosproxy.paros.network.SSLConnector;

import ch.csnc.extension.httpclient.SSLContextManager;

public class OptionsParamCertificate extends AbstractParam {

   
    private static final Logger logger = Logger.getLogger(OptionsParamCertificate.class);

    private static final String CERTIFICATE_BASE_KEY = "certificate";

    private static final String USE_CLIENT_CERT = CERTIFICATE_BASE_KEY + ".use";
    private static final String CLIENT_CERT_LOCATION = CERTIFICATE_BASE_KEY + ".clientCertLocation";

    private static final String ALLOW_UNSAFE_SSL_RENEGOTIATION = CERTIFICATE_BASE_KEY + ".allowUnsafeSslRenegotiation";

    private int useClientCert = 0;
    private String clientCertLocation = "";
    
    private boolean allowUnsafeSslRenegotiation = false;

    public OptionsParamCertificate() {
    }

    @Override
    protected void parse() {

        // always turn off client cert
        setUseClientCert(false);
        setClientCertLocation("");
        
        try {
            allowUnsafeSslRenegotiation = getConfig().getBoolean(ALLOW_UNSAFE_SSL_RENEGOTIATION, false);
            setAllowUnsafeSslRenegotiationSystemProperty(allowUnsafeSslRenegotiation);
        } catch (ConversionException e) {
            logger.error("Error while loading the option Allow Unsafe SSL Renegotiation: " + e.getMessage(), e);
        }
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

    
    /**
     * Tells whether or not the unsafe SSL renegotiation is enabled.
     * 
     * @return {@code true} if the unsafe SSL renegotiation is enabled, {@code false} otherwise.
     * @see #setAllowUnsafeSslRenegotiation(boolean)
     */
    public boolean isAllowUnsafeSslRenegotiation() {
        return allowUnsafeSslRenegotiation;
    }

    /**
     * Sets whether or not the unsafe SSL renegotiation is enabled.
     * <p>
     * Calling this method changes the system property "sun.security.ssl.allowUnsafeRenegotiation" and
     * "com.ibm.jsse2.renegotiate". It must be set before establishing any SSL connection. Further changes after establishing a
     * SSL connection will have no effect on the renegotiation but the value will be saved and restored next time ZAP is
     * restarted.
     * </p>
     * 
     * @param allow {@code true} if the unsafe SSL renegotiation should be enabled, {@code false} otherwise.
     * @see #isAllowUnsafeSslRenegotiation()
     * @see #setAllowUnsafeSslRenegotiationSystemProperty(boolean)
     */
    public void setAllowUnsafeSslRenegotiation(boolean allow) {
        if (allowUnsafeSslRenegotiation != allow) {
            allowUnsafeSslRenegotiation = allow;

            setAllowUnsafeSslRenegotiationSystemProperty(allowUnsafeSslRenegotiation);
            getConfig().setProperty(ALLOW_UNSAFE_SSL_RENEGOTIATION, Boolean.valueOf(allowUnsafeSslRenegotiation));
        }
    }

    /**
     * Sets the given value to system property "sun.security.ssl.allowUnsafeRenegotiation" and sets the appropriate value to
     * system property "com.ibm.jsse2.renegotiate", which enables or not the unsafe SSL renegotiation.
     * <p>
     * It must be set before establishing any SSL connection. Further changes after establishing a SSL connection will have no
     * effect.
     * </p>
     * 
     * @param allow the value to set to the property
     * @see #setAllowUnsafeSslRenegotiation(boolean)
     */
    private static void setAllowUnsafeSslRenegotiationSystemProperty(boolean allow) {
        String ibmSystemPropertyValue;
        if (allow) {
            logger.info("Unsafe SSL renegotiation enabled.");
            ibmSystemPropertyValue = "ALL";
        } else {
            logger.info("Unsafe SSL renegotiation disabled.");
            ibmSystemPropertyValue = "NONE";
        }
        System.setProperty("com.ibm.jsse2.renegotiate", ibmSystemPropertyValue);
        System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", String.valueOf(allow));
    }
}
