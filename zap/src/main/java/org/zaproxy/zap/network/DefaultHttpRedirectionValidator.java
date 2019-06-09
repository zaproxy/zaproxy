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
package org.zaproxy.zap.network;

import org.apache.commons.httpclient.URI;

/**
 * Default implementation of {@link HttpRedirectionValidator}, all redirections are considered valid
 * and notifications of new messages are simply ignored.
 *
 * @since 2.6.0
 * @see #INSTANCE
 */
public class DefaultHttpRedirectionValidator implements HttpRedirectionValidator {

    /** The instance of {@code DefaultHttpRedirectionValidator}. */
    public static final DefaultHttpRedirectionValidator INSTANCE =
            new DefaultHttpRedirectionValidator();

    private DefaultHttpRedirectionValidator() {}

    /** Returns {@code true}, always. */
    @Override
    public boolean isValid(URI redirection) {
        return true;
    }
}
