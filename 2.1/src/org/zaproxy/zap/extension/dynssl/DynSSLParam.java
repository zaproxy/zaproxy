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

import java.security.KeyStore;

import org.apache.log4j.Logger;
import org.parosproxy.paros.common.AbstractParam;

/**
 * @author MaWoKi
 */
public class DynSSLParam extends AbstractParam {

	/*default*/ static final String PARAM_ROOT_CA = "dynssl.param.rootca";

	private KeyStore rootca = null;

	private final Logger logger = Logger.getLogger(DynSSLParam.class);

	@Override
	protected void parse() {
		try {
			String rootcastr = getConfig().getString(PARAM_ROOT_CA, null);
			if (rootcastr != null) {
				setRootca(rootcastr);
			}
		} catch (final Exception e) {
			logger.warn("Couldn't load Root CA parameter.", e);
		}
	}

	/**
	 * @param rootca
	 */
	public void setRootca(String rootca) {
		try {
			setRootca(SslCertificateUtils.string2Keystore(rootca));
		} catch (final Exception e) {
			logger.error("Couldn't save Root CA parameter.", e);
		}
	}

	public KeyStore getRootca() {
		return rootca;
	}

	/**
	 * @param rootca
	 */
	public void setRootca(KeyStore rootca) {
		this.rootca = rootca;
		if (rootca != null) {
			try {
				getConfig().setProperty(PARAM_ROOT_CA, SslCertificateUtils.keyStore2String(rootca));
			} catch (final Exception e) {
				logger.error("Couldn't save Root CA parameter.", e);
			}
		}
	}

}

