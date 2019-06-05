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

import java.nio.file.Path;
import java.nio.file.Paths;
import org.parosproxy.paros.Constant;

/**
 * Helper class with utility methods for ZAP {@code Session}s.
 *
 * @see org.parosproxy.paros.model.Session
 */
public final class SessionUtils {

    public static final String SESSION_EXTENSION = ".session";

    private SessionUtils() {}

    public static Path getSessionPath(String session) {
        String normalisedSession = getNormalisedSessionName(session);
        Path sessionPath = Paths.get(normalisedSession);
        if (!sessionPath.isAbsolute()) {
            sessionPath =
                    Paths.get(Constant.getZapHome(), Constant.FOLDER_SESSION_DEFAULT)
                            .resolve(sessionPath);
        }
        return sessionPath;
    }

    private static String getNormalisedSessionName(String session) {
        if (!session.endsWith(SESSION_EXTENSION)) {
            return session + SESSION_EXTENSION;
        }
        return session;
    }
}
