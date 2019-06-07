/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
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
package org.zaproxy.zap.extension.ascan;

import org.parosproxy.paros.network.HttpMessage;

/**
 * Abstract interface for plugins that should be managed by a Challenge based Callback API
 *
 * @author yhawke (2014)
 * @deprecated (2.8.0) Superseded by {@link org.zaproxy.zap.extension.callback.CallbackImplementor}.
 */
@Deprecated
public interface ChallengeCallbackPlugin {

    public void notifyCallback(String challenge, HttpMessage msg);
}
