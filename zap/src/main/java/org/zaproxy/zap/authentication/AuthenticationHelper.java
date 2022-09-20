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
package org.zaproxy.zap.authentication;

import java.awt.EventQueue;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.UnaryOperator;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpSender;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.model.SessionStructure;
import org.zaproxy.zap.session.SessionManagementMethod;
import org.zaproxy.zap.users.User;
import org.zaproxy.zap.utils.Stats;

public class AuthenticationHelper {

    private HttpSender httpSender;
    private SessionManagementMethod sessionManagementMethod;
    private User user;

    public AuthenticationHelper(
            HttpSender httpSender, SessionManagementMethod sessionManagementMethod, User user) {
        super();
        this.httpSender = httpSender;
        this.sessionManagementMethod = sessionManagementMethod;
        this.user = user;
    }

    private static final Logger log = LogManager.getLogger(AuthenticationHelper.class);

    private static final String HISTORY_TAG_AUTHENTICATION = "Authentication";
    public static final String AUTH_SUCCESS_STATS = "stats.auth.success";
    public static final String AUTH_FAILURE_STATS = "stats.auth.failure";

    /** @deprecated use {@link #notifyOutputAuthSuccessful(HttpMessage)} instead. */
    @Deprecated
    public static void notifyOutputAuthSuccessful() {
        notifyOutputAuthSuccessful(null);
    }

    public static void notifyOutputAuthSuccessful(HttpMessage msg) {
        if (msg != null) {
            // Always record stats
            try {
                Stats.incCounter(SessionStructure.getHostName(msg), AUTH_SUCCESS_STATS);
            } catch (URIException e) {
                // Ignore
            }
        }
        // Let the user know it worked
        if (View.isInitialised()) {
            View.getSingleton()
                    .getOutputPanel()
                    .appendAsync(
                            Constant.messages.getString("authentication.output.success") + "\n");
        }
    }

    public static void notifyOutputAuthFailure(HttpMessage msg) {
        // Always record stats
        try {
            Stats.incCounter(SessionStructure.getHostName(msg), AUTH_FAILURE_STATS);
        } catch (URIException e) {
            // Ignore
        }
        // Let the user know it failed
        if (View.isInitialised()) {
            View.getSingleton()
                    .getOutputPanel()
                    .appendAsync(
                            Constant.messages.getString(
                                            "authentication.output.failure",
                                            msg.getRequestHeader().getURI().toString())
                                    + "\n");
        }
    }

    public HttpState getCorrespondingHttpState() {
        if (user.getAuthenticatedSession() == null)
            user.setAuthenticatedSession(sessionManagementMethod.createEmptyWebSession());
        return user.getCorrespondingHttpState();
    }

    public static void addAuthMessageToHistory(HttpMessage msg) {
        // Add message to history
        try {
            final HistoryReference ref =
                    new HistoryReference(
                            Model.getSingleton().getSession(),
                            HistoryReference.TYPE_AUTHENTICATION,
                            msg);
            ref.addTag(HISTORY_TAG_AUTHENTICATION);
            if (View.isInitialised()) {
                final ExtensionHistory extHistory =
                        Control.getSingleton()
                                .getExtensionLoader()
                                .getExtension(ExtensionHistory.class);
                if (extHistory != null) {
                    EventQueue.invokeLater(
                            new Runnable() {

                                @Override
                                public void run() {
                                    extHistory.addHistory(ref);
                                }
                            });
                }
            }
        } catch (Exception ex) {
            log.error("Cannot add authentication message to History tab.", ex);
        }
    }

    public HttpMessage prepareMessage() {
        return prepareMessage(this.sessionManagementMethod, this.user);
    }

    public static HttpMessage prepareMessage(
            SessionManagementMethod sessionManagementMethod, User user) {
        HttpMessage msg = new HttpMessage();
        // Make sure the message will be sent with a good WebSession that can record the changes
        if (user.getAuthenticatedSession() == null)
            user.setAuthenticatedSession(sessionManagementMethod.createEmptyWebSession());
        msg.setRequestingUser(user);

        return msg;
    }

    public static String replaceUserData(
            String data, Map<String, String> keyValuePairs, UnaryOperator<String> encoder) {
        for (Entry<String, String> kvp : keyValuePairs.entrySet()) {
            data = data.replace(kvp.getKey(), encoder.apply(kvp.getValue()));
        }
        return data;
    }

    public static void replaceUserDataInRequest(
            HttpMessage msg, Map<String, String> userDataMap, UnaryOperator<String> bodyEncoder) {
        try {
            Map<String, String> kvMap = new HashMap<>(userDataMap.size());
            for (Entry<String, String> userdata : userDataMap.entrySet()) {
                kvMap.put(
                        URLEncoder.encode(userdata.getKey(), StandardCharsets.UTF_8.name()),
                        userdata.getValue());
            }
            String uri =
                    AuthenticationHelper.replaceUserData(
                            msg.getRequestHeader().getURI().toString(),
                            kvMap,
                            PostBasedAuthenticationMethodType::encodeParameter);
            msg.getRequestHeader().setURI(new URI(uri, true));
        } catch (Exception e) {
            log.error(
                    "Failed to replace user data in request {}",
                    msg.getRequestHeader().getURI(),
                    e);
        }
        if (msg.getRequestBody().length() > 0) {
            msg.setRequestBody(
                    AuthenticationHelper.replaceUserData(
                            msg.getRequestBody().toString(), userDataMap, bodyEncoder));
            msg.getRequestHeader().setContentLength(msg.getRequestBody().length());
        }
    }

    public User getRequestingUser() {
        // Make sure the message will be sent with a good WebSession that can record the changes
        if (user.getAuthenticatedSession() == null)
            user.setAuthenticatedSession(sessionManagementMethod.createEmptyWebSession());
        return user;
    }

    public void sendAndReceive(HttpMessage msg) throws IOException {
        this.httpSender.sendAndReceive(msg);
    }

    public void sendAndReceive(HttpMessage msg, boolean followRedirect) throws IOException {
        this.httpSender.sendAndReceive(msg, followRedirect);
    }

    public HttpSender getHttpSender() {
        return httpSender;
    }
}
