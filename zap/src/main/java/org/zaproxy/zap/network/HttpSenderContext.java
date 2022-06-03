/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2022 The ZAP Development Team
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
package org.zaproxy.zap.network;

import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.users.User;

/** <strong>Note:</strong> Not part of the public API. */
public interface HttpSenderContext {

    void setUseGlobalState(boolean use);

    void setUseCookies(boolean use);

    void setFollowRedirects(boolean followRedirects);

    void setMaxRedirects(int max);

    void setMaxRetriesOnIoError(int max);

    void setRemoveUserDefinedAuthHeaders(boolean remove);

    void setUser(User user);

    User getUser(HttpMessage msg);
}
