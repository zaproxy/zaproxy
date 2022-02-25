/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The ZAP Development Team
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
package org.zaproxy.zap.extension.httppanel.view.largeresponse;

import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.Message;

/** @deprecated (2.12.0) No longer in use. */
@Deprecated
public class LargeResponseUtil {

    public static final int DEFAULT_MIN_CONTENT_LENGTH = 100000;

    protected static int minContentLength = DEFAULT_MIN_CONTENT_LENGTH;

    public static int getMinContentLength() {
        return minContentLength;
    }

    public static void setMinContentLength(int aMinContentLength) {
        minContentLength = aMinContentLength;
    }

    public static void restoreDefaultMinContentLength() {
        minContentLength = DEFAULT_MIN_CONTENT_LENGTH;
    }

    public static boolean isLargeResponse(Message aMessage) {
        if (aMessage instanceof HttpMessage) {
            HttpMessage httpMessage = (HttpMessage) aMessage;
            if (minContentLength <= 0) {
                return false;
            }

            return httpMessage.getResponseBody().length() > minContentLength;
        }

        return false;
    }
}
