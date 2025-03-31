/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2025 The ZAP Development Team
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
package org.parosproxy.paros.core.scanner;

import java.util.Collections;
import java.util.List;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.utils.Stats;

public class VariantPlainBody implements Variant {

    private static final String SHORT_NAME = "bodyPlain";
    private static final String STATS_NAME = "stats.ascan.variant." + SHORT_NAME + ".content-type";

    private List<NameValuePair> paramList = Collections.emptyList();

    public VariantPlainBody() {
        super();
    }

    @Override
    public String getShortName() {
        return SHORT_NAME;
    }

    @Override
    public void setMessage(HttpMessage msg) {
        if (msg == null) {
            throw new IllegalArgumentException("Parameter message must not be null.");
        }

        if (msg.getRequestBody().length() > 0 && isApplicableContent(msg)) {
            paramList =
                    List.of(
                            new NameValuePair(
                                    NameValuePair.TYPE_PLAIN_BODY,
                                    "",
                                    msg.getRequestBody().toString(),
                                    0));
        }
    }

    @Override
    public List<NameValuePair> getParamList() {
        return paramList;
    }

    private static boolean isApplicableContent(HttpMessage msg) {
        if (msg.getRequestHeader().getHeader(HttpHeader.CONTENT_TYPE) == null) {
            Stats.incCounter(STATS_NAME + ".none");
            return true;
        }
        if (msg.getRequestHeader().hasContentType("text/plain")) {
            Stats.incCounter(STATS_NAME + ".plain");
            return true;
        }
        return false;
    }

    @Override
    public String setParameter(
            HttpMessage msg, NameValuePair originalPair, String param, String value) {
        msg.getRequestBody().setBody(value);
        return value;
    }

    @Override
    public String setEscapedParameter(
            HttpMessage msg, NameValuePair originalPair, String param, String value) {
        return setParameter(msg, originalPair, param, value);
    }
}
