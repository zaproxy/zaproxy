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
package org.zaproxy.zap.model;

import java.io.File;

import org.parosproxy.paros.model.Model;

/**
 * Helper class with utility methods for ZAP {@code Session}s.
 *
 * @see org.parosproxy.paros.model.Session
 */
public final class SessionUtils {

    public static final String SESSION_EXTENSION = ".session";

    private SessionUtils() {
    }

    public static String getSessionPath(String session) {
        String sessionPath = getNormalisedSessionPath(session);
        File file = new File(sessionPath);
        if (!sessionPath.equals(file.getAbsolutePath())) {
            // Treat as a relative path
            sessionPath = Model.getSingleton().getOptionsParam().getUserDirectory() + File.separator + sessionPath;
        }
        return sessionPath;
    }

    private static String getNormalisedSessionPath(String sessionPath) {
        if (!sessionPath.endsWith(SESSION_EXTENSION)) {
            return sessionPath + SESSION_EXTENSION;
        }
        return sessionPath;
    }
}
