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

import java.util.ArrayList;
import java.util.List;

public class FoundComponent {
    private final Object component;
    private final List<Object> parents;

    public FoundComponent(Object component) {
        this.component = component;
        this.parents = new ArrayList<>();
    }

    public Object getComponent() {
        return component;
    }

    public <T> T getComponentCasted() {
        return Generic.uncheckedCast(component);
    }

    public void addParent(Object parent) {
        parents.add(parent);
    }

    public List<Object> getParents() {
        return new ArrayList<>(parents);
    }

    public <T> T getParentAsCasted(int index) {
        return Generic.uncheckedCast(parents.get(index));
    }
}
