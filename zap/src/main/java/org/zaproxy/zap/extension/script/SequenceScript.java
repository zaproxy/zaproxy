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
package org.zaproxy.zap.extension.script;

import java.util.List;
import org.parosproxy.paros.core.scanner.AbstractPlugin;
import org.parosproxy.paros.network.HttpMessage;

public interface SequenceScript {

    /** * A method that will start an active scan for a single sequence. */
    void scanSequence();

    /**
     * A method that will be run, before an Active scan on a specific message.
     *
     * @param msg The message to be scanned.
     * @param plugin The current plugin.
     * @return An updated version of the supplied message.
     */
    HttpMessage runSequenceBefore(HttpMessage msg, AbstractPlugin plugin);

    /**
     * A method that will be run after a message in a sequence has been scanned.
     *
     * @param msg The message that was scanned.
     * @param plugin The current plugin.
     */
    void runSequenceAfter(HttpMessage msg, AbstractPlugin plugin);

    /**
     * Returns a boolean, indicating if a message is part of the Sequence.
     *
     * @param msg
     * @return
     */
    boolean isPartOfSequence(HttpMessage msg);

    /**
     * Returns a list, containing all requests in a Sequence.
     *
     * @return A list of all requests in a Sequence.
     */
    List<HttpMessage> getAllRequestsInScript();
}
