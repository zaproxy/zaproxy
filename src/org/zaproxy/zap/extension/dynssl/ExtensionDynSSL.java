/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2011 mawoki@ymail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.extension.dynssl;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.security.CachedSslCertifificateServiceImpl;
import org.parosproxy.paros.security.SslCertificateService;
import org.parosproxy.paros.view.View;

/**
 * Extension enables configuration for Root CA certificate
 *
 * @author MaWoKi
 */
public class ExtensionDynSSL extends ExtensionAdaptor {

	public static final String EXTENSION_ID = "ExtensionDynSSL";
	
	private DynSSLParam params;
	private DynamicSSLPanel optionsPanel;

	private final Logger logger = Logger.getLogger(ExtensionDynSSL.class);

	public ExtensionDynSSL() {
		super();
		this.setName(EXTENSION_ID);
        this.setOrder(54);
	}

	@Override
	public String getUIName() {
		return Constant.messages.getString("dynssl.name");
	}
	
	@Override
	public void hook(ExtensionHook extensionHook) {
		super.hook(extensionHook);
	    if (getView() != null) {
	        extensionHook.getHookView().addOptionPanel(getOptionsPanel());
	    }
        extensionHook.addOptionsParamSet(getParams());
	}
	
	@Override
	public void start() {
		final KeyStore rootca = getParams().getRootca();
		if (rootca == null) {
			try {
				createNewRootCa();
			} catch (Exception e) {
				logger.error("Failed to create new root CA certificate:", e);
			}
			return;
		}

	    try {
			setRootCa(rootca);
		} catch (final Exception e) {
			logger.error("Couldn't initialize Root CA", e);
		}
		if (isCertExpired(getRootCaCertificate())) {
			warnRooCaCertExpired();
		}
	}
	
	public void createNewRootCa() throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException {
		logger.info("Creating new root CA certificate");
		KeyStore newrootca = SslCertificateUtils.createRootCA();
		setRootCa(newrootca);
		getParams().setRootca(newrootca);
		logger.info("New root CA certificate created");
	}

	private DynamicSSLPanel getOptionsPanel() {
		if (optionsPanel == null) {
			optionsPanel = new DynamicSSLPanel(this);
		}
		return optionsPanel;
	}

	public DynSSLParam getParams() {
		if (params == null) {
			params = new DynSSLParam();
		}
		return params;
	}

	@Override
	public String getAuthor() {
		return Constant.ZAP_TEAM;
	}

	@Override
	public String getDescription() {
		return Constant.messages.getString("dynssl.desc");
	}

	@Override
	public URL getURL() {
		try {
			return new URL(Constant.ZAP_HOMEPAGE);
		} catch (MalformedURLException e) {
			return null;
		}
	}

	public void setRootCa(KeyStore rootca) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {
		CachedSslCertifificateServiceImpl.getService().initializeRootCA(rootca);
	}
	
	public Certificate getRootCA() throws KeyStoreException {
		if (this.getParams().getRootca() == null) {
			return null;
		}
		return this.getParams().getRootca().getCertificate(SslCertificateService.ZAPROXY_JKS_ALIAS);
	}

	/**
	 * No database tables used, so all supported
	 */
	@Override
	public boolean supportsDb(String type) {
    	return true;
    }
	
	@Override
	public boolean supportsLowMemory() {
		return true;
	}

	/**
	 * Gets ZAPs current Root CA Certificate in X.509 format. 
	 * Could return {@code null} if there is a problem getting the certificate.
	 * 
	 * @return The X.509 version of ZAPs current Root CA certificate.
	 * @since TODO add version
	 */
	public X509Certificate getRootCaCertificate() {
		try {
			return (X509Certificate) getRootCA();
		} catch (KeyStoreException e) {
			logger.error("Couldn't get ZAP's Root CA Certificate", e);
			return null;
		}
	}
	
	/**
	 * Returns true if the certificate expired before the current date, otherwise false.
	 * 
	 * @param cert the X.509 certificate for which expiration should be checked.
	 * @return true if the certificate has expired, otherwise false.
	 */
	private boolean isCertExpired(X509Certificate cert) {
		if (cert != null && cert.getNotAfter().before(new Date())) {
			return true;
		}
		return false;
	}
	
	/**
	 * Displays a warning dialog, and logs a warning message if ZAPs Root CA certificate has expired.
	 * 
	 * @see #isCertExpired(X509Certificate)
	 */
	private void warnRooCaCertExpired() {
		X509Certificate cert = getRootCaCertificate();
		if (cert == null) {
			return;
		}
		String warnMsg = Constant.messages.getString("dynssl.warn.cert.expired", cert.getNotAfter().toString(),
				new Date().toString());
		if (View.isInitialised()) {
			getView().showWarningDialog(warnMsg);
		}
		logger.warn(warnMsg);
	}
}
