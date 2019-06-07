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
package org.zaproxy.zap.session;

import org.apache.commons.httpclient.HttpState;

/**
 * A WebSession is the ZAP implementation for a session maintained during the communication with a
 * webapp/website.
 */
public abstract class WebSession {

    private String name;
    private HttpState state;

    /**
     * Instantiates a new web session.
     *
     * @param name the name
     * @param state the state
     */
    public WebSession(String name, HttpState state) {
        this.name = name;
        this.state = state;
    }

    /**
     * Gets the http state that will be used to send messages corresponding to this session.
     *
     * @return the http state
     */
    public HttpState getHttpState() {
        return state;
    }

    /**
     * Gets the name of the web session, if set.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }
}
