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
package org.zaproxy.zap.authentication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.parosproxy.paros.network.HttpSender;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.authentication.AuthenticationMethod.AuthCheckingStrategy;
import org.zaproxy.zap.authentication.AuthenticationMethod.AuthPollFrequencyUnits;
import org.zaproxy.zap.model.SessionStructure;
import org.zaproxy.zap.users.AuthenticationState;
import org.zaproxy.zap.users.User;
import org.zaproxy.zap.utils.Stats;

/**
 * Handles verification of whether a user is authenticated, independently of the authentication
 * method used to log in.
 *
 * <p>Verification can be done by checking each request/response for indicator patterns, or by
 * polling a dedicated URL at a configurable frequency.
 *
 * @since 2.18.0
 */
public class VerificationMethod {

    private static final Logger LOGGER = LogManager.getLogger(VerificationMethod.class);

    public static final String AUTH_STATE_ASSUMED_IN_STATS = "stats.auth.state.assumedin";
    public static final String AUTH_STATE_LOGGED_IN_STATS = "stats.auth.state.loggedin";
    public static final String AUTH_STATE_LOGGED_OUT_STATS = "stats.auth.state.loggedout";
    public static final String AUTH_STATE_NO_INDICATOR_STATS = "stats.auth.state.noindicator";
    public static final String AUTH_STATE_UNKNOWN_STATS = "stats.auth.state.unknown";

    public static final int DEFAULT_POLL_FREQUENCY = 60;

    private AuthCheckingStrategy authCheckingStrategy = AuthCheckingStrategy.POLL_URL;

    private String pollUrl;
    private String pollData;
    private String pollHeaders;
    private int pollFrequency = DEFAULT_POLL_FREQUENCY;
    private AuthPollFrequencyUnits pollFrequencyUnits = AuthPollFrequencyUnits.SECONDS;

    private Pattern loggedInIndicatorPattern = null;
    private Pattern loggedOutIndicatorPattern = null;

    private HttpSender httpSender;

    private BiConsumer<HttpMessage, User> userDataReplacer;

    private HttpSender getHttpSender() {
        if (this.httpSender == null) {
            this.httpSender = new HttpSender(HttpSender.AUTHENTICATION_POLL_INITIATOR);
        }
        return httpSender;
    }

    /**
     * Checks if the response received by the HTTP message corresponds to an authenticated web
     * session.
     *
     * <p>If none of the indicators are set up, the method defaults to returning {@code true}, so
     * that no authentications are attempted when there is no way to check authentication.
     *
     * @param msg the http message
     * @param user the user being checked
     * @return {@code true} if authenticated or no indicators have been set, {@code false} otherwise
     */
    public boolean isAuthenticated(HttpMessage msg, User user) {
        return this.isAuthenticated(msg, user, false);
    }

    /**
     * Checks if the response received by the HTTP message corresponds to an authenticated web
     * session.
     *
     * <p>If none of the indicators are set up, the method defaults to returning {@code true}, so
     * that no authentications are attempted when there is no way to check authentication.
     *
     * @param msg the http message
     * @param user the user being checked
     * @param force always check even when the polling strategy would otherwise skip this check
     * @return {@code true} if authenticated or no indicators have been set, {@code false} otherwise
     */
    public boolean isAuthenticated(HttpMessage msg, User user, boolean force) {
        if (msg == null
                || user == null
                || AuthCheckingStrategy.AUTO_DETECT.equals(this.authCheckingStrategy)) {
            return false;
        }
        AuthenticationState authState = user.getAuthenticationState();
        if (loggedInIndicatorPattern == null && loggedOutIndicatorPattern == null) {
            try {
                Stats.incCounter(SessionStructure.getHostName(msg), AUTH_STATE_NO_INDICATOR_STATS);
            } catch (URIException e) {
                // Ignore
            }
            if (View.isInitialised()) {
                View.getSingleton()
                        .getOutputPanel()
                        .append(
                                Constant.messages.getString(
                                                "authentication.output.indicatorsNotSet",
                                                msg.getRequestHeader().getURI())
                                        + "\n");
            }
            return true;
        }

        HttpMessage msgToTest;

        switch (this.authCheckingStrategy) {
            case EACH_REQ:
            case EACH_REQ_RESP:
            case EACH_RESP:
                msgToTest = msg;
                break;
            case POLL_URL:
                if (!force
                        && authState.getLastPollResult() != null
                        && authState.getLastPollResult()) {
                    switch (pollFrequencyUnits) {
                        case SECONDS:
                            if ((System.currentTimeMillis() - authState.getLastPollTime()) / 1000
                                    < pollFrequency) {
                                try {
                                    Stats.incCounter(
                                            SessionStructure.getHostName(msg),
                                            AUTH_STATE_ASSUMED_IN_STATS);
                                } catch (URIException e) {
                                    // Ignore
                                }
                                return true;
                            }
                            break;
                        case REQUESTS:
                        default:
                            if (authState.getRequestsSincePoll() < pollFrequency) {
                                authState.incRequestsSincePoll();
                                try {
                                    Stats.incCounter(
                                            SessionStructure.getHostName(msg),
                                            AUTH_STATE_ASSUMED_IN_STATS);
                                } catch (URIException e) {
                                    // Ignore
                                }
                                return true;
                            }
                            break;
                    }
                }
                try {
                    msgToTest = pollAsUser(user);
                } catch (Exception e) {
                    LOGGER.warn("Failed sending poll request to {}", this.getPollUrl(), e);
                    return false;
                }
                break;
            default:
                return false;
        }

        return evaluateAuthRequest(msgToTest, authState);
    }

    /**
     * Evaluates whether the given message indicates an authenticated state, updating the
     * authentication state accordingly.
     *
     * @param msg the http message to evaluate
     * @param authState the current authentication state to update
     * @return {@code true} if the message indicates an authenticated state
     */
    public boolean evaluateAuthRequest(HttpMessage msg, AuthenticationState authState) {
        List<String> contentToTest = new ArrayList<>();
        switch (authCheckingStrategy) {
            case EACH_REQ:
                contentToTest.add(msg.getRequestHeader().toString());
                contentToTest.add(msg.getRequestBody().toString());
                break;
            case EACH_REQ_RESP:
                contentToTest.add(msg.getRequestHeader().toString());
                contentToTest.add(msg.getRequestBody().toString());
                contentToTest.add(msg.getResponseHeader().toString());
                contentToTest.add(msg.getResponseBody().toString());
                break;
            case EACH_RESP:
            case POLL_URL:
                contentToTest.add(msg.getResponseHeader().toString());
                contentToTest.add(msg.getResponseBody().toString());
                break;
            case AUTO_DETECT:
                return false;
        }
        if (patternMatchesAny(loggedInIndicatorPattern, contentToTest)) {
            try {
                Stats.incCounter(SessionStructure.getHostName(msg), AUTH_STATE_LOGGED_IN_STATS);
            } catch (URIException e) {
                // Ignore
            }
            if (authCheckingStrategy.equals(AuthCheckingStrategy.POLL_URL)) {
                authState.setLastPollResult(true);
            }
            return true;
        }

        if (loggedOutIndicatorPattern != null
                && !patternMatchesAny(loggedOutIndicatorPattern, contentToTest)) {
            // Can't find the unauthenticated indicator — assume authenticated but record as unknown
            try {
                Stats.incCounter(SessionStructure.getHostName(msg), AUTH_STATE_UNKNOWN_STATS);
            } catch (URIException e) {
                // Ignore
            }
            if (this.authCheckingStrategy.equals(AuthCheckingStrategy.POLL_URL)) {
                authState.setLastPollResult(true);
            }
            return true;
        }
        try {
            Stats.incCounter(SessionStructure.getHostName(msg), AUTH_STATE_LOGGED_OUT_STATS);
        } catch (URIException e) {
            // Ignore
        }
        if (this.authCheckingStrategy.equals(AuthCheckingStrategy.POLL_URL)) {
            authState.setLastPollResult(false);
        }
        return false;
    }

    /**
     * Sends a poll request as the given user and returns the response message.
     *
     * @param user the user on whose behalf to poll
     * @return the poll response message
     * @throws IOException if the poll request could not be sent
     * @throws IllegalArgumentException if the checking strategy is not {@link
     *     AuthCheckingStrategy#POLL_URL}
     */
    public HttpMessage pollAsUser(User user) throws IOException {
        if (!this.authCheckingStrategy.equals(AuthCheckingStrategy.POLL_URL)) {
            throw new IllegalArgumentException("Authentication checking strategy is not POLL_URL");
        }
        HttpMessage pollMsg = new HttpMessage(new URI(this.getPollUrl(), true));
        if (this.getPollData() != null && this.getPollData().length() > 0) {
            pollMsg.getRequestHeader().setMethod(HttpRequestHeader.POST);
            pollMsg.getRequestBody().setBody(this.getPollData());
            pollMsg.getRequestHeader().setContentLength(pollMsg.getRequestBody().length());
        }
        if (this.getPollHeaders() != null && this.getPollHeaders().length() > 0) {
            for (String header : this.getPollHeaders().split("\n")) {
                String[] headerValue = header.split(":", 2);
                if (headerValue.length == 2) {
                    pollMsg.getRequestHeader()
                            .addHeader(headerValue[0].trim(), headerValue[1].trim());
                } else {
                    LOGGER.error(
                            "Invalid header '{}' for poll request to {}",
                            header,
                            this.getPollUrl());
                }
            }
        }
        pollMsg.setRequestingUser(user);
        if (userDataReplacer != null) {
            userDataReplacer.accept(pollMsg, user);
        }

        getHttpSender().sendAndReceive(pollMsg);
        AuthenticationHelper.addAuthMessageToHistory(
                pollMsg, List.of(AuthenticationHelper.HISTORY_TAG_VERIFICATION));

        AuthenticationState authState = user.getAuthenticationState();
        authState.setLastPollTime(System.currentTimeMillis());
        authState.setRequestsSincePoll(0);

        return pollMsg;
    }

    private static boolean patternMatchesAny(Pattern pattern, List<String> content) {
        if (pattern != null) {
            for (String str : content) {
                if (pattern.matcher(str).find()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets the logged in indicator pattern.
     *
     * @return the logged in indicator pattern
     */
    public Pattern getLoggedInIndicatorPattern() {
        return loggedInIndicatorPattern;
    }

    /**
     * Sets the logged in indicator pattern.
     *
     * @param loggedInIndicatorPattern the new logged in indicator pattern
     */
    public void setLoggedInIndicatorPattern(String loggedInIndicatorPattern) {
        if (loggedInIndicatorPattern == null || loggedInIndicatorPattern.trim().length() == 0) {
            this.loggedInIndicatorPattern = null;
        } else {
            this.loggedInIndicatorPattern = Pattern.compile(loggedInIndicatorPattern);
        }
    }

    /**
     * Gets the logged out indicator pattern.
     *
     * @return the logged out indicator pattern
     */
    public Pattern getLoggedOutIndicatorPattern() {
        return loggedOutIndicatorPattern;
    }

    /**
     * Sets the logged out indicator pattern.
     *
     * @param loggedOutIndicatorPattern the new logged out indicator pattern
     */
    public void setLoggedOutIndicatorPattern(String loggedOutIndicatorPattern) {
        if (loggedOutIndicatorPattern == null || loggedOutIndicatorPattern.trim().length() == 0) {
            this.loggedOutIndicatorPattern = null;
        } else {
            this.loggedOutIndicatorPattern = Pattern.compile(loggedOutIndicatorPattern);
        }
    }

    public AuthCheckingStrategy getAuthCheckingStrategy() {
        return authCheckingStrategy;
    }

    public void setAuthCheckingStrategy(AuthCheckingStrategy authCheckingStrategy) {
        Objects.requireNonNull(authCheckingStrategy);
        this.authCheckingStrategy = authCheckingStrategy;
    }

    public String getPollUrl() {
        return pollUrl;
    }

    public void setPollUrl(String pollUrl) {
        this.pollUrl = pollUrl;
    }

    public String getPollData() {
        return pollData;
    }

    public void setPollData(String pollData) {
        this.pollData = pollData;
    }

    public String getPollHeaders() {
        return pollHeaders;
    }

    public void setPollHeaders(String pollHeaders) {
        this.pollHeaders = pollHeaders;
    }

    public int getPollFrequency() {
        return pollFrequency;
    }

    public void setPollFrequency(int pollFrequency) {
        this.pollFrequency = pollFrequency;
    }

    public AuthPollFrequencyUnits getPollFrequencyUnits() {
        return pollFrequencyUnits;
    }

    public void setPollFrequencyUnits(AuthPollFrequencyUnits pollFrequencyUnits) {
        this.pollFrequencyUnits = pollFrequencyUnits;
    }

    /**
     * Sets the callback used to replace user-specific data (e.g. username/password tokens) in poll
     * requests. If {@code null}, no replacement is performed.
     *
     * @param userDataReplacer the callback, or {@code null}
     */
    public void setUserDataReplacer(BiConsumer<HttpMessage, User> userDataReplacer) {
        this.userDataReplacer = userDataReplacer;
    }

    /**
     * Gets the callback used to replace user-specific data in poll requests.
     *
     * @return the callback, or {@code null} if no replacement is configured
     */
    public BiConsumer<HttpMessage, User> getUserDataReplacer() {
        return userDataReplacer;
    }

    /**
     * Creates a deep copy of this verification method. The {@link #getUserDataReplacer() user data
     * replacer} callback is copied by reference.
     *
     * @return a deep copy
     */
    @Override
    public VerificationMethod clone() {
        VerificationMethod clone = new VerificationMethod();
        clone.authCheckingStrategy = this.authCheckingStrategy;
        clone.pollUrl = this.pollUrl;
        clone.pollData = this.pollData;
        clone.pollHeaders = this.pollHeaders;
        clone.pollFrequency = this.pollFrequency;
        clone.pollFrequencyUnits = this.pollFrequencyUnits;
        clone.loggedInIndicatorPattern = this.loggedInIndicatorPattern;
        clone.loggedOutIndicatorPattern = this.loggedOutIndicatorPattern;
        clone.userDataReplacer = this.userDataReplacer;
        return clone;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result =
                prime * result
                        + ((loggedInIndicatorPattern == null)
                                ? 0
                                : loggedInIndicatorPattern.pattern().hashCode());
        result =
                prime * result
                        + ((loggedOutIndicatorPattern == null)
                                ? 0
                                : loggedOutIndicatorPattern.pattern().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        VerificationMethod other = (VerificationMethod) obj;
        if (!isSamePattern(loggedInIndicatorPattern, other.loggedInIndicatorPattern)) {
            return false;
        }
        if (!isSamePattern(loggedOutIndicatorPattern, other.loggedOutIndicatorPattern)) {
            return false;
        }
        if (!this.authCheckingStrategy.equals(other.authCheckingStrategy)) {
            return false;
        }
        if (!Objects.equals(this.pollUrl, other.pollUrl)) {
            return false;
        }
        if (!Objects.equals(this.pollData, other.pollData)) {
            return false;
        }
        if (!Objects.equals(this.pollHeaders, other.pollHeaders)) {
            return false;
        }
        if (this.pollFrequency != other.pollFrequency) {
            return false;
        }
        return this.pollFrequencyUnits.equals(other.pollFrequencyUnits);
    }

    private static boolean isSamePattern(Pattern pattern, Pattern other) {
        if (pattern == null) {
            return other == null;
        }
        return other != null && pattern.pattern().equals(other.pattern());
    }
}
