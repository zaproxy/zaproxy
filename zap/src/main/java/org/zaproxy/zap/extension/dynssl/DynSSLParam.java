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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.extension.dynssl;

import java.security.KeyStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.common.AbstractParam;

/** @author MaWoKi */
@Deprecated
public class DynSSLParam extends AbstractParam {

    /*default*/ static final String PARAM_ROOT_CA = "dynssl.param.rootca";

    private KeyStore rootca = null;

    private static final Logger logger = LogManager.getLogger(DynSSLParam.class);

    @Override
    protected void parse() {
        String rootcastr = getString(PARAM_ROOT_CA, null);
        if (rootcastr != null) {
            rootca = createKeyStore(rootcastr);
        }
    }

    private static KeyStore createKeyStore(String rootcastr) {
        try {
            return SslCertificateUtils.string2Keystore(rootcastr);
        } catch (final Exception e) {
            logger.error("Couldn't create Root CA KeyStore from String: {}", rootcastr, e);
        }
        return null;
    }

    /** @param rootca */
    public void setRootca(String rootca) {
        setRootca(createKeyStore(rootca));
    }

    public KeyStore getRootca() {
        return rootca;
    }

    /** @param rootca */
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
