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
// ZAP: 2017/09/26 Use helper methods to read the configurations.
// ZAP: 2018/02/14 Remove unnecessary boxing / unboxing
// ZAP: 2018/08/01 Added support for setting and persisting client cert from CLI
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2020/11/26 Use Log4j 2 classes for logging.
package org.parosproxy.paros.extension.option;

import ch.csnc.extension.httpclient.SSLContextManager;
import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.common.AbstractParam;
import org.parosproxy.paros.network.SSLConnector;

public class OptionsParamCertificate extends AbstractParam {

    private static final Logger logger = LogManager.getLogger(OptionsParamCertificate.class);

    private static final String CERTIFICATE_BASE_KEY = "certificate";

    private static final String USE_CLIENT_CERT = CERTIFICATE_BASE_KEY + ".use";
    private static final String PERSIST_CLIENT_CERT = CERTIFICATE_BASE_KEY + ".persist";
    private static final String CLIENT_CERT_LOCATION = CERTIFICATE_BASE_KEY + ".pkcs12.path";
    private static final String CLIENT_CERT_PASSWORD = CERTIFICATE_BASE_KEY + ".pkcs12.password";
    private static final String CLIENT_CERT_INDEX = CERTIFICATE_BASE_KEY + ".pkcs12.index";

    private static final String ALLOW_UNSAFE_SSL_RENEGOTIATION =
            CERTIFICATE_BASE_KEY + ".allowUnsafeSslRenegotiation";

    private boolean useClientCert = false;
    private String clientCertLocation = "";
    private String clientCertPassword = "";
    private int clientCertIndex = 0;

    private boolean allowUnsafeSslRenegotiation = false;

    public OptionsParamCertificate() {}

    @Override
    protected void parse() {

        clientCertCheck();
        saveClientCertSettings();

        allowUnsafeSslRenegotiation = getBoolean(ALLOW_UNSAFE_SSL_RENEGOTIATION, false);
        setAllowUnsafeSslRenegotiationSystemProperty(allowUnsafeSslRenegotiation);
    }

    /**
     * Saves the client cert settings if the flag is set explicitly. Only works for the CLI
     * currently.
     */
    private void saveClientCertSettings() {

        if (getBoolean(PERSIST_CLIENT_CERT, false)) {
            logger.warn("Saving Client Certificate settings: password will be found in config");
            setUseClientCert(getBoolean(USE_CLIENT_CERT, false));
            setClientCertLocation(getString(CLIENT_CERT_LOCATION, ""));
            setClientCertPassword(getString(CLIENT_CERT_PASSWORD, ""));
            setClientCertIndex(getInt(CLIENT_CERT_INDEX, 0));

        } else {
            // Default to clear settings
            setUseClientCert(false);
            setClientCertLocation("");
            setClientCertPassword("");
            setClientCertIndex(0);
        }
    }

    /**
     * Enables ClientCertificate from -config CLI parameters Requires location, password and a flag
     * to use client certificate.
     */
    private void clientCertCheck() {

        boolean enableClientCert = getBoolean(USE_CLIENT_CERT, false);
        String certPath = getString(CLIENT_CERT_LOCATION, "");
        String certPass = getString(CLIENT_CERT_PASSWORD, "");
        int certIndex = getInt(CLIENT_CERT_INDEX, 0);

        if (enableClientCert && !certPath.isEmpty() && !certPass.isEmpty()) {
            try {

                SSLContextManager contextManager = getSSLContextManager();
                int ksIndex = contextManager.loadPKCS12Certificate(certPath, certPass);
                contextManager.unlockKey(ksIndex, certIndex, certPass);
                contextManager.setDefaultKey(ksIndex, certIndex);

                setActiveCertificate();
                setEnableCertificate(true);

                logger.info("Client Certificate enabled from CLI");
                logger.info("Use -config certificate.persist=true to save settings");

            } catch (IOException
                    | CertificateException
                    | NoSuchAlgorithmException
                    | KeyStoreException
                    | KeyManagementException ex) {
                logger.error("The certificate could not be enabled due to an error", ex);
            }
        }
    }

    public String getClientCertPassword() {
        return clientCertPassword;
    }

    public void setClientCertPassword(String clientCertPassword) {
        this.clientCertPassword = clientCertPassword;
        getConfig().setProperty(CLIENT_CERT_PASSWORD, clientCertPassword);
    }

    /** @return Returns the client cert location. */
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

    public int getClientCertIndex() {
        return clientCertIndex;
    }

    public void setClientCertIndex(int clientCertIdx) {
        this.clientCertIndex = clientCertIdx;
        getConfig().setProperty(CLIENT_CERT_INDEX, Integer.toString(clientCertIndex));
    }

    public boolean isUseClientCert() {
        return useClientCert;
    }

    private void setUseClientCert(boolean isUse) {
        useClientCert = isUse;
        getConfig().setProperty(USE_CLIENT_CERT, Boolean.toString(useClientCert));
    }

    public void setEnableCertificate(boolean enabled) {
        ProtocolSocketFactory sslFactory = Protocol.getProtocol("https").getSocketFactory();

        if (sslFactory instanceof SSLConnector) {
            SSLConnector ssl = (SSLConnector) sslFactory;
            ssl.setEnableClientCert(enabled);

            setUseClientCert(enabled);
        }
    }

    public void setActiveCertificate() {

        ProtocolSocketFactory sslFactory = Protocol.getProtocol("https").getSocketFactory();

        if (sslFactory instanceof SSLConnector) {
            SSLConnector ssl = (SSLConnector) sslFactory;
            ssl.setActiveCertificate();
        }
    }

    public SSLContextManager getSSLContextManager() {

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
     *
     * <p>Calling this method changes the system property
     * "sun.security.ssl.allowUnsafeRenegotiation" and "com.ibm.jsse2.renegotiate". It must be set
     * before establishing any SSL connection. Further changes after establishing a SSL connection
     * will have no effect on the renegotiation but the value will be saved and restored next time
     * ZAP is restarted.
     *
     * @param allow {@code true} if the unsafe SSL renegotiation should be enabled, {@code false}
     *     otherwise.
     * @see #isAllowUnsafeSslRenegotiation()
     * @see #setAllowUnsafeSslRenegotiationSystemProperty(boolean)
     */
    public void setAllowUnsafeSslRenegotiation(boolean allow) {
        if (allowUnsafeSslRenegotiation != allow) {
            allowUnsafeSslRenegotiation = allow;

            setAllowUnsafeSslRenegotiationSystemProperty(allowUnsafeSslRenegotiation);
            getConfig().setProperty(ALLOW_UNSAFE_SSL_RENEGOTIATION, allowUnsafeSslRenegotiation);
        }
    }

    /**
     * Sets the given value to system property "sun.security.ssl.allowUnsafeRenegotiation" and sets
     * the appropriate value to system property "com.ibm.jsse2.renegotiate", which enables or not
     * the unsafe SSL renegotiation.
     *
     * <p>It must be set before establishing any SSL connection. Further changes after establishing
     * a SSL connection will have no effect.
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
