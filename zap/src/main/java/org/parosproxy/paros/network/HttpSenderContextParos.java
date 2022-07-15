/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2022 The ZAP Development Team
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
package org.parosproxy.paros.network;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.zaproxy.zap.network.HttpSenderContext;
import org.zaproxy.zap.users.User;

/** @deprecated (2.12.0) Implementation details, do not use. */
@Deprecated
public class HttpSenderContextParos implements HttpSenderContext {

    private final HttpSender parent;
    private final int initiator;

    private final ConnectionParam param;

    private final HttpClient client;

    private boolean followRedirects;
    private User user;
    private boolean useCookies;
    private boolean useGlobalState;

    HttpSenderContextParos(
            HttpSender parent, int initiator, ConnectionParam param, HttpClient client) {
        this.parent = parent;
        this.initiator = initiator;
        this.param = param;
        this.client = client;
    }

    HttpSender getParent() {
        return parent;
    }

    int getInitiator() {
        return initiator;
    }

    HttpClient getHttpClient() {
        return client;
    }

    @Override
    public void setUseGlobalState(boolean use) {
        this.useGlobalState = use;
        checkState();
    }

    @Override
    public void setUseCookies(boolean use) {
        this.useCookies = use;
        checkState();
    }

    @Override
    public void setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
    }

    boolean isFollowRedirects() {
        return followRedirects;
    }

    @Override
    public void setMaxRedirects(int max) {
        if (max < 0) {
            throw new IllegalArgumentException(
                    "Parameter maxRedirects must be greater or equal to zero.");
        }
        client.getParams().setIntParameter(HttpClientParams.MAX_REDIRECTS, max);
    }

    @Override
    public void setMaxRetriesOnIoError(int max) {
        if (max < 0) {
            throw new IllegalArgumentException(
                    "Parameter retries must be greater or equal to zero.");
        }

        HttpMethodRetryHandler retryHandler = new DefaultHttpMethodRetryHandler(max, false);
        client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, retryHandler);
    }

    @Override
    public void setRemoveUserDefinedAuthHeaders(boolean remove) {
        client.getParams()
                .setBooleanParameter(
                        org.apache.commons.httpclient.HttpMethodDirector
                                .PARAM_REMOVE_USER_DEFINED_AUTH_HEADERS,
                        remove);
    }

    @Override
    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public User getUser(HttpMessage msg) {
        if (user != null) {
            return user;
        }
        return msg.getRequestingUser();
    }

    private void checkState() {
        if (!useCookies) {
            resetState();
            setClientsCookiePolicy(CookiePolicy.IGNORE_COOKIES);
        } else if (useGlobalState) {
            if (param.isHttpStateEnabled()) {
                client.setState(param.getHttpState());
                setClientsCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            } else {
                setClientsCookiePolicy(CookiePolicy.IGNORE_COOKIES);
            }
        } else {
            resetState();

            setClientsCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        }
    }

    private void setClientsCookiePolicy(String policy) {
        client.getParams().setCookiePolicy(policy);
    }

    private void resetState() {
        HttpState state = new HttpState();
        client.setState(state);
    }
}
