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
package org.zaproxy.zap.extension.httppanel;

import java.util.Collections;
import java.util.Map;
import org.zaproxy.zap.eventBus.Event;

public interface Message {
    boolean isInScope();

    boolean isForceIntercept();

    /**
     * Returns a map of data suitable for including in an {@link Event}
     *
     * @since 2.8.0
     */
    default Map<String, String> toEventData() {
        return Collections.emptyMap();
    }

    /**
     * Returns the type of the message. By default this is the implementing class name, but ideally
     * this should be replaced with a more readable string.
     *
     * @since 2.8.0
     * @return the type of the message
     */
    default String getType() {
        return this.getClass().getCanonicalName();
    }
}
