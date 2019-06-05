/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2013 The ZAP Development Team
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
package org.zaproxy.zap.extension.script;

import javax.script.ScriptException;
import org.parosproxy.paros.network.HttpMessage;

/**
 * A script that is executed on demand by the user for selected {@link HttpMessage HTTP message}(s).
 *
 * @since 2.2.0
 */
public interface TargetedScript {

    /**
     * Called for each HTTP message selected by the user.
     *
     * @param msg the HTTP message selected, never {@code null}.
     * @throws ScriptException if an error occurred while executing the script.
     */
    void invokeWith(HttpMessage msg) throws ScriptException;
}
