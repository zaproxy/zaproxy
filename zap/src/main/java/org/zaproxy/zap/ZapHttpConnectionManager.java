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
package org.zaproxy.zap;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;

/**
 * Custom {@link SimpleHttpConnectionManager} that uses {@link ZapHttpConnection} for connection
 * creation. Needed to expose the underlying socket.
 *
 * @deprecated (2.12.0) Implementation details, do not use.
 */
@Deprecated
public class ZapHttpConnectionManager extends SimpleHttpConnectionManager {

    /** Use custom {@link ZapHttpConnection} to allow for socket exposure. */
    @Override
    public org.apache.commons.httpclient.HttpConnection getConnectionWithTimeout(
            HostConfiguration hostConfiguration, long timeout) {

        if (httpConnection == null) {
            httpConnection = new ZapHttpConnection(hostConfiguration);
            httpConnection.setHttpConnectionManager(this);
            httpConnection.getParams().setDefaults(this.getParams());
        }

        return super.getConnectionWithTimeout(hostConfiguration, timeout);
    }
}
