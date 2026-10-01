/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2026 The ZAP Development Team
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
package org.zaproxy.zap.users;

import org.parosproxy.paros.network.HttpMessage;

/**
 * A listener that will be notified of the outcome of an authentication attempt performed by {@link
 * User#processMessageToMatchUser(HttpMessage)}.
 *
 * <p>The listener is only notified when authentication was actually required and attempted, not on
 * every message processed for an already authenticated user.
 *
 * @see User#addAuthenticationListener(AuthenticationListener)
 * @since 2.18.0
 */
public interface AuthenticationListener {

    /**
     * Notifies the listener that an authentication attempt for the given user is about to start.
     *
     * <p>Called immediately before the actual authentication request is made, only when
     * authentication is actually required.
     *
     * @param user the user that is about to be authenticated
     * @param trigger the message that triggered the authentication attempt
     */
    default void onAuthenticationRequestStart(User user, HttpMessage trigger) {}

    /**
     * Notifies the listener that an authentication attempt for the given user succeeded.
     *
     * @param user the user that was authenticated
     * @param trigger the message that triggered the authentication attempt
     */
    void onAuthenticationRequestSuccess(User user, HttpMessage trigger);

    /**
     * Notifies the listener that an authentication attempt for the given user failed.
     *
     * <p>The reason, if available, can be obtained through {@code
     * user.getAuthenticationState().getLastAuthFailure()}.
     *
     * @param user the user that failed to authenticate
     * @param trigger the message that triggered the authentication attempt
     */
    void onAuthenticationRequestFailure(User user, HttpMessage trigger);
}
