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
package org.zaproxy.zap.extension.httpsessions;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/** A set of Http Session tokens, that is valid for a particular Site, Context etc. */
public class HttpSessionTokensSet {

    /** The session tokens. */
    private LinkedHashSet<String> sessionTokens = new LinkedHashSet<>();

    public HttpSessionTokensSet() {}

    /**
     * Adds the token.
     *
     * @param token the token
     */
    protected void addToken(String token) {
        synchronized (sessionTokens) {
            sessionTokens.add(token);
        }
    }

    public boolean isEmpty() {
        synchronized (sessionTokens) {
            return sessionTokens.isEmpty();
        }
    }

    /**
     * Removes the token.
     *
     * @param token the token
     */
    protected void removeToken(String token) {
        synchronized (sessionTokens) {
            sessionTokens.remove(token);
        }
    }

    /**
     * Checks if is session token.
     *
     * @param token the token
     * @return true, if successful
     */
    public boolean isSessionToken(String token) {
        synchronized (sessionTokens) {
            return sessionTokens.contains(token);
        }
    }

    /**
     * Returns an unmodifiable view of the tokens in this set. This method provides a "read-only"
     * access to the internal set. Query operations on the returned set "read through" to the
     * internal set, and attempts to modify the returned set, whether direct or via its iterator,
     * result in an {@link UnsupportedOperationException}.
     *
     * @return the tokens set
     */
    public Set<String> getTokensSet() {
        synchronized (sessionTokens) {
            return Collections.unmodifiableSet(sessionTokens);
        }
    }
}
