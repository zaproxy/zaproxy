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
package org.zaproxy.zap.spider.parser;

/**
 * The listener interface for receiving spiderParser events. The class that is interested in
 * processing a spiderParser event implements this interface, and the object created with that class
 * is registered with a component using the component's {@code addSpiderParserListener} method. When
 * the spiderParser event occurs, that object's appropriate method is invoked.
 *
 * @deprecated (2.12.0) See the spider add-on in zap-extensions instead.
 */
@Deprecated
public interface SpiderParserListener {

    /**
     * Event triggered when a new resource is found. The resourceFound contains all the required
     * information about the resource (source message, URI, depth, method, etc.).
     *
     * @param resourceFound definition of found spider resource
     */
    void resourceFound(SpiderResourceFound resourceFound);
}
