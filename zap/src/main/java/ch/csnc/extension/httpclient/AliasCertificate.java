/*
 * This file is part of WebScarab, an Open Web Application Security
 * Project utility. For details, please see http://www.owasp.org/
 *
 * Copyright (c) 2002 - 2004 Rogan Dawes
 *
 * Please note that this file was originally released under the
 * GNU General Public License  as published by the Free Software Foundation;
 * either version 2 of the License, or (at your option) any later version.
 *
 * As of October 2014 Rogan Dawes granted the OWASP ZAP Project permission to
 * redistribute this code under the Apache License, Version 2.0:
 *
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
package ch.csnc.extension.httpclient;

import java.security.cert.Certificate;

/** @deprecated (2.12.0) No longer in use. */
@Deprecated
public class AliasCertificate {

    private Certificate certificate;
    private String alias;

    AliasCertificate(Certificate certificate, String alias) {
        this.setCertificate(certificate);
        this.setAlias(alias);
    }

    public void setCertificate(Certificate certificate) {
        this.certificate = certificate;
    }

    public Certificate getCertificate() {
        return certificate;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

    public String getName() {

        String cn = getCN();

        if (cn == null || cn.length() == 0) {
            return getAlias();
        } else {
            return cn + " [" + getAlias() + "]";
        }
    }

    public String getCN() {

        String dn = getCertificate().toString();

        int i = 0;
        i = dn.indexOf("CN=");
        if (i == -1) {
            return null;
        }
        // get the remaining DN without CN=
        dn = dn.substring(i + 3);

        char[] dncs = dn.toCharArray();
        for (i = 0; i < dncs.length; i++) {
            if (dncs[i] == ',' && i > 0 && dncs[i - 1] != '\\') {
                break;
            }
        }
        return dn.substring(0, i);
    }
}
