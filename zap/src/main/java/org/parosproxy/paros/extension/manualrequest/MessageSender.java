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
package org.parosproxy.paros.extension.manualrequest;

import org.zaproxy.zap.extension.httppanel.Message;

/**
 * A message sender knows how to send a given message object.
 *
 * @deprecated (2.12.0) Replaced by Requester add-on.
 */
@Deprecated
public interface MessageSender {

    void handleSendMessage(Message aMessage) throws Exception;

    void cleanup();
}
