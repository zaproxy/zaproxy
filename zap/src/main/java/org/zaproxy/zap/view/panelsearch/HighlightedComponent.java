/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2017 The ZAP Development Team
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
package org.zaproxy.zap.view.panelsearch;

import java.util.HashMap;
import java.util.Map;

public class HighlightedComponent {
    private final Object component;
    Map<String, Object> store = new HashMap<>();

    public HighlightedComponent(Object component) {
        this.component = component;
    }

    public Object getComponent() {
        return component;
    }

    public <T> void put(String key, T value) {
        store.put(key, value);
    }

    public <T> T get(String key) {
        return Generic.uncheckedCast(store.get(key));
    }
}
