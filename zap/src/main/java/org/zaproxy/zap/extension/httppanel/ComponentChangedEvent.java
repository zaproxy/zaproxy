/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2015 The ZAP Development Team
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

import java.util.EventObject;

import org.zaproxy.zap.extension.httppanel.component.HttpPanelComponentInterface;

public class ComponentChangedEvent extends EventObject {

    private static final long serialVersionUID = 4097248911533000241L;

    private final HttpPanelComponentInterface oldComponent;
    private final HttpPanelComponentInterface newComponent;

    public ComponentChangedEvent(
            Object source,
            HttpPanelComponentInterface oldComponent,
            HttpPanelComponentInterface newComponent) {
        super(source);

        this.oldComponent = oldComponent;
        this.newComponent = newComponent;
    }

    public HttpPanelComponentInterface getOldComponent() {
        return oldComponent;
    }

    public HttpPanelComponentInterface getNewComponent() {
        return newComponent;
    }

}