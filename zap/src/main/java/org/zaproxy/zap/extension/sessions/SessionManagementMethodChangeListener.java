/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2023 The ZAP Development Team
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
package org.zaproxy.zap.extension.sessions;

import java.util.List;
import org.zaproxy.zap.session.SessionManagementMethodType;

/** Interface used for listening to changes made on {@link SessionManagementMethodType}s. */
public interface SessionManagementMethodChangeListener {

    /**
     * Method to be invoked when one or more {@link SessionManagementMethodType}s were added or
     * removed.
     *
     * @param sessionManagementMethodTypes the new, updated list of {@link
     *     SessionManagementMethodType}s.
     */
    void onStateChanged(List<SessionManagementMethodType> sessionManagementMethodTypes);
}
