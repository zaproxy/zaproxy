/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2018 The ZAP Development Team
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

/**
 * A listener for scripts' output.
 *
 * @since 2.8.0
 * @see ExtensionScript#addScriptOutputListener(ScriptOutputListener)
 */
@FunctionalInterface
public interface ScriptOutputListener {

    /**
     * Called each time a script writes some output.
     *
     * <p>Can be called concurrently.
     *
     * @param script the source of the output.
     * @param output the new output.
     */
    void output(ScriptWrapper script, String output);
}
