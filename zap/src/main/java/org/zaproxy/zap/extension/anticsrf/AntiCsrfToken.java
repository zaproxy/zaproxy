/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2011 The ZAP Development Team
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
package org.zaproxy.zap.extension.anticsrf;

import java.lang.ref.SoftReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.anticsrf.ExtensionAntiCSRF.HistoryReferenceFactory;

public class AntiCsrfToken implements Cloneable {

    private static final Logger LOGGER = LogManager.getLogger(AntiCsrfToken.class);

    private HttpMessage msg;
    private SoftReference<HttpMessage> msgReference;
    private int historyReferenceId;
    private String name;
    private String value;
    private String targetURL;
    private int formIndex;

    private static HistoryReferenceFactory historyReferenceFactory;

    static void setHistoryReferenceFactory(HistoryReferenceFactory historyReferenceFactory) {
        AntiCsrfToken.historyReferenceFactory = historyReferenceFactory;
    }

    public AntiCsrfToken(HttpMessage msg, String name, String value, int formIndex) {
        this(msg, null, -1, name, value, formIndex);
    }

    private AntiCsrfToken(
            HttpMessage msg,
            SoftReference<HttpMessage> msgReference,
            int historyReferenceId,
            String name,
            String value,
            int formIndex) {
        super();
        this.msg = msg;
        this.msgReference = msgReference;
        this.historyReferenceId = historyReferenceId;
        this.name = name;
        this.value = value;
        this.formIndex = formIndex;
    }

    public HttpMessage getMsg() {
        if (msg != null) {
            return msg;
        }

        if (msgReference != null) {
            HttpMessage msg = msgReference.get();
            if (msg != null) {
                return msg;
            }
            msgReference.clear();
            msgReference = null;
        }

        if (historyReferenceId == -1) {
            return null;
        }

        try {
            HttpMessage msg =
                    historyReferenceFactory
                            .createHistoryReference(historyReferenceId)
                            .getHttpMessage();
            msgReference = new SoftReference<>(msg);
            return msg;
        } catch (HttpMalformedHeaderException | DatabaseException e) {
            LOGGER.error("Failed to load the persisted message: ", e);
        }
        return null;
    }

    public void setMsg(HttpMessage msg) {
        this.msg = msg;

        if (msgReference != null) {
            msgReference.clear();
            msgReference = null;
        }
        historyReferenceId = -1;
    }

    void setHistoryReferenceId(int historyReferenceId) {
        if (historyReferenceId < 0) {
            throw new IllegalArgumentException(
                    "Parameter historyReferenceId must be equal or greater than zero.");
        }

        setMsg(null);
        this.historyReferenceId = historyReferenceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setTargetURL(String targetUrl) {
        this.targetURL = targetUrl;
    }

    public String getTargetURL() {
        return targetURL;
    }

    public int getFormIndex() {
        return formIndex;
    }

    public void setFormIndex(int formIndex) {
        this.formIndex = formIndex;
    }

    @Override
    public AntiCsrfToken clone() {
        return new AntiCsrfToken(msg, msgReference, historyReferenceId, name, value, formIndex);
    }
}
