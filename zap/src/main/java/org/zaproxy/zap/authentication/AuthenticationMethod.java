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
package org.zaproxy.zap.authentication;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.jfree.util.Log;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.parosproxy.paros.network.HttpSender;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.extension.api.ApiResponseSet;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.SessionStructure;
import org.zaproxy.zap.session.SessionManagementMethod;
import org.zaproxy.zap.session.WebSession;
import org.zaproxy.zap.users.User;
import org.zaproxy.zap.utils.Stats;

/**
 * The {@code AuthenticationMethod} represents an authentication method that can be used to
 * authenticate an entity in a particular web application.
 */
public abstract class AuthenticationMethod {

    public static final String CONTEXT_CONFIG_AUTH = Context.CONTEXT_CONFIG + ".authentication";
    public static final String CONTEXT_CONFIG_AUTH_TYPE = CONTEXT_CONFIG_AUTH + ".type";
    public static final String CONTEXT_CONFIG_AUTH_STRATEGY = CONTEXT_CONFIG_AUTH + ".strategy";
    public static final String CONTEXT_CONFIG_AUTH_POLL_URL = CONTEXT_CONFIG_AUTH + ".pollurl";
    public static final String CONTEXT_CONFIG_AUTH_POLL_DATA = CONTEXT_CONFIG_AUTH + ".polldata";
    public static final String CONTEXT_CONFIG_AUTH_POLL_FREQ = CONTEXT_CONFIG_AUTH + ".pollfreq";
    public static final String CONTEXT_CONFIG_AUTH_POLL_UNITS = CONTEXT_CONFIG_AUTH + ".pollunits";
    public static final String CONTEXT_CONFIG_AUTH_LOGGEDIN = CONTEXT_CONFIG_AUTH + ".loggedin";
    public static final String CONTEXT_CONFIG_AUTH_LOGGEDOUT = CONTEXT_CONFIG_AUTH + ".loggedout";

    public static final String AUTH_STATE_ASSUMED_IN_STATS = "stats.auth.state.assumedin";
    public static final String AUTH_STATE_LOGGED_IN_STATS = "stats.auth.state.loggedin";
    public static final String AUTH_STATE_LOGGED_OUT_STATS = "stats.auth.state.loggedout";
    public static final String AUTH_STATE_NO_INDICATOR_STATS = "stats.auth.state.noindicator";
    public static final String AUTH_STATE_UNKNOWN_STATS = "stats.auth.state.unknown";

    public static final String TOKEN_PREFIX = "{%";
    public static final String TOKEN_POSTFIX = "%}";

    public static final int DEFAULT_POLL_FREQUENCY = 60;

    public static enum AuthCheckingStrategy {
        EACH_RESP,
        EACH_REQ,
        EACH_REQ_RESP,
        POLL_URL
    };

    public static enum AuthPollFrequencyUnits {
        REQUESTS,
        SECONDS
    };

    private AuthCheckingStrategy authCheckingStrategy = AuthCheckingStrategy.EACH_RESP;

    private String pollUrl;

    private String pollData;

    private int pollFrequency = DEFAULT_POLL_FREQUENCY;

    private AuthPollFrequencyUnits pollFrequencyUnits = AuthPollFrequencyUnits.REQUESTS;

    private Date lastPollTime;

    private Boolean lastPollResult;

    private int requestsSincePoll = 0;

    /**
     * Checks if the authentication method is fully configured.
     *
     * @return true, if is configured
     */
    public abstract boolean isConfigured();

    /**
     * Clones the current authentication method, creating a deep-copy of it.
     *
     * @return a deep copy of the authentication method
     */
    @Override
    public AuthenticationMethod clone() {
        AuthenticationMethod method = duplicate();
        method.authCheckingStrategy = this.authCheckingStrategy;
        method.pollUrl = this.pollUrl;
        method.pollData = this.pollData;
        method.pollFrequency = this.pollFrequency;
        method.pollFrequencyUnits = this.pollFrequencyUnits;
        method.loggedInIndicatorPattern = this.loggedInIndicatorPattern;
        method.loggedOutIndicatorPattern = this.loggedOutIndicatorPattern;
        return method;
    }

    /**
     * Internal method for cloning the current authentication method, creating a deep-copy of it.
     *
     * @return a deep copy of the authentication method
     */
    protected abstract AuthenticationMethod duplicate();

    /**
     * Validates that the creation of authentication credentials is possible, returning {@code true}
     * if it is, {@code false} otherwise.
     *
     * <p>If view is enabled the user should be informed that it's not possible to create
     * authentication credentials.
     *
     * <p>Default implementation returns, always, {@code true}.
     *
     * @return {@code true} if the creation of authentication credentials is possible, {@code false}
     *     otherwise
     * @see #createAuthenticationCredentials()
     * @since 2.4.3
     */
    public boolean validateCreationOfAuthenticationCredentials() {
        return true;
    }

    /**
     * Creates a new, empty, Authentication Credentials object corresponding to this type of
     * Authentication method.
     *
     * @return the authentication credentials
     * @see #validateCreationOfAuthenticationCredentials()
     */
    public abstract AuthenticationCredentials createAuthenticationCredentials();

    /**
     * Gets the {@link AuthenticationMethodType} corresponding to this authentication method.
     *
     * <p>Implementations may return new instantiations at every call, so performance considerations
     * should be taken by users.
     *
     * @return the type
     */
    public abstract AuthenticationMethodType getType();

    /**
     * Performs an authentication in a web application, returning an authenticated.
     *
     * @param sessionManagementMethod the set up session management method is provided so it can be
     *     used, if needed, to automatically extract session information from Http Messages.
     * @param credentials the credentials
     * @param user the user to authenticate
     * @return an authenticated web session
     * @throws UnsupportedAuthenticationCredentialsException the unsupported authentication
     *     credentials exception {@link WebSession}.
     */
    public abstract WebSession authenticate(
            SessionManagementMethod sessionManagementMethod,
            AuthenticationCredentials credentials,
            User user)
            throws UnsupportedAuthenticationCredentialsException;

    /**
     * Gets an api response that represents the Authentication Method.
     *
     * @return the api response representation
     */
    public abstract ApiResponse getApiResponseRepresentation();

    /**
     * Called when the Authentication Method is persisted/saved in a Context. For example, in this
     * method, UI elements can be marked accordingly.Description
     */
    public void onMethodPersisted() {}

    /**
     * Called when the Authentication Method is discarded from/not used in a Context. For example,
     * in this method, UI elements can be (un)marked accordingly.
     */
    public void onMethodDiscarded() {}

    /** The logged in indicator pattern. */
    protected Pattern loggedInIndicatorPattern = null;

    /** The logged out indicator pattern. */
    protected Pattern loggedOutIndicatorPattern = null;

    private HttpSender httpSender;

    private HttpSender getHttpSender() {
        if (this.httpSender == null) {
            this.httpSender =
                    new HttpSender(
                            Model.getSingleton().getOptionsParam().getConnectionParam(),
                            true,
                            HttpSender.AUTHENTICATION_POLL_INITIATOR);
        }
        return httpSender;
    }

    /** Deprecated TODO add version. */
    @Deprecated
    public boolean isAuthenticated(HttpMessage msg) {
        return this.isAuthenticated(msg, null, false);
    }

    /**
     * Checks if the response received by the Http Message corresponds to an authenticated Web
     * Session.
     *
     * <p>If none of the indicators are set up, the method defaults to returning true, so that no
     * authentications are tried when there is no way to check authentication. A message is also
     * shown on the output console in this case.
     *
     * @param msg the http message
     * @return true, if is authenticated or no indicators have been set, and false otherwise
     */
    public boolean isAuthenticated(HttpMessage msg, User user) {
        return this.isAuthenticated(msg, user, false);
    }

    /**
     * Checks if the response received by the Http Message corresponds to an authenticated Web
     * Session.
     *
     * <p>If none of the indicators are set up, the method defaults to returning true, so that no
     * authentications are tried when there is no way to check authentication. A message is also
     * shown on the output console in this case.
     *
     * @param msg the http message
     * @param force always check even if the polling strategy is being used
     * @return true, if is authenticated or no indicators have been set, and false otherwise
     */
    public boolean isAuthenticated(HttpMessage msg, User user, boolean force) {

        if (msg == null) {
            return false;
        }
        // Assume logged in if nothing was set up
        if (loggedInIndicatorPattern == null && loggedOutIndicatorPattern == null) {
            try {
                Stats.incCounter(SessionStructure.getHostName(msg), AUTH_STATE_NO_INDICATOR_STATS);
            } catch (URIException e) {
                // Ignore
            }
            if (View.isInitialised()) {
                // Let the user know this
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

        List<String> contentToTest = new ArrayList<>();

        switch (this.authCheckingStrategy) {
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
                contentToTest.add(msg.getResponseHeader().toString());
                contentToTest.add(msg.getResponseBody().toString());
                break;
            case POLL_URL:
                if (!force && lastPollResult != null && lastPollResult) {
                    // Check if we really need to poll the relevant URL again
                    switch (pollFrequencyUnits) {
                        case SECONDS:
                            if ((new Date().getTime() - lastPollTime.getTime()) / 1000
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
                            if (requestsSincePoll < pollFrequency) {
                                requestsSincePoll++;
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
                // Make the poll request
                try {
                    HttpMessage pollMsg = new HttpMessage(new URI(this.getPollUrl(), true));
                    if (this.getPollData() != null && this.getPollData().length() > 0) {
                        pollMsg.getRequestHeader().setMethod(HttpRequestHeader.POST);
                        pollMsg.getRequestBody().setBody(this.getPollData());
                        pollMsg.getRequestHeader()
                                .setContentLength(pollMsg.getRequestBody().length());
                    }
                    if (this.getType() != null && user != null) {
                        this.getType().replaceUserDataInPollRequest(pollMsg, user);
                    }
                    getHttpSender().sendAndReceive(pollMsg);
                    AuthenticationHelper.addAuthMessageToHistory(pollMsg);
                    contentToTest.add(pollMsg.getResponseHeader().toString());
                    contentToTest.add(pollMsg.getResponseBody().toString());
                    lastPollTime = new Date();
                    requestsSincePoll = 0;

                } catch (Exception e1) {
                    Log.error(e1.getMessage(), e1);
                    return false;
                }
                break;
            default:
                return false;
        }

        if (patternMatchesAny(loggedInIndicatorPattern, contentToTest)) {
            // Looks like we're authenticated
            try {
                Stats.incCounter(SessionStructure.getHostName(msg), AUTH_STATE_LOGGED_IN_STATS);
            } catch (URIException e) {
                // Ignore
            }
            if (this.authCheckingStrategy.equals(AuthCheckingStrategy.POLL_URL)) {
                this.lastPollResult = true;
            }
            return true;
        }

        if (loggedOutIndicatorPattern != null
                && !patternMatchesAny(loggedOutIndicatorPattern, contentToTest)) {
            // Cant find the unauthenticated indicator, assume we're authenticated but record as
            // unknown
            try {
                Stats.incCounter(SessionStructure.getHostName(msg), AUTH_STATE_UNKNOWN_STATS);
            } catch (URIException e) {
                // Ignore
            }
            if (this.authCheckingStrategy.equals(AuthCheckingStrategy.POLL_URL)) {
                this.lastPollResult = true;
            }
            return true;
        }
        // Not looking good...
        try {
            Stats.incCounter(SessionStructure.getHostName(msg), AUTH_STATE_LOGGED_OUT_STATS);
        } catch (URIException e) {
            // Ignore
        }
        if (this.authCheckingStrategy.equals(AuthCheckingStrategy.POLL_URL)) {
            this.lastPollResult = false;
        }
        return false;
    }

    private boolean patternMatchesAny(Pattern pattern, List<String> content) {
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
     * Gets the last poll result - true means that the user is authnaticated, otherwise false
     *
     * @return the last poll result
     */
    public Boolean getLastPollResult() {
        return lastPollResult;
    }

    /**
     * Sets the last poll result - this can be used by script or add-ons to change the known logged
     * in state eg if they have more accurate information
     *
     * @param lastPollResult
     */
    public void setLastPollResult(Boolean lastPollResult) {
        this.lastPollResult = lastPollResult;
    }

    public Date getLastPollTime() {
        return lastPollTime;
    }

    public int getRequestsSincePoll() {
        return requestsSincePoll;
    }

    /**
     * Checks if another method is of the same type.
     *
     * @param other the other
     * @return true, if is same type
     */
    public boolean isSameType(AuthenticationMethod other) {
        if (other == null) return false;
        return other.getClass().equals(this.getClass());
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
        AuthenticationMethod other = (AuthenticationMethod) obj;
        if (!isSamePattern(loggedInIndicatorPattern, other.loggedInIndicatorPattern)) {
            return false;
        }
        if (!isSamePattern(loggedOutIndicatorPattern, other.loggedOutIndicatorPattern)) {
            return false;
        }
        if (!this.authCheckingStrategy.equals(other.authCheckingStrategy)) {
            return false;
        }
        if (!isEqual(this.pollUrl, other.pollUrl)) {
            return false;
        }
        if (!isEqual(this.pollData, other.pollData)) {
            return false;
        }
        if (this.pollFrequency != other.pollFrequency) {
            return false;
        }
        if (!this.pollFrequencyUnits.equals(other.pollFrequencyUnits)) {
            return false;
        }
        return true;
    }

    private boolean isEqual(String str1, String str2) {
        if (str1 == null && str2 == null) {
            return true;
        }
        if (str1 == null || str2 == null) {
            return false;
        }
        return str1.equals(str2);
    }

    private static boolean isSamePattern(Pattern pattern, Pattern other) {
        if (pattern == null) {
            return other == null;
        }
        return other != null && pattern.pattern().equals(other.pattern());
    }

    /**
     * Thrown when an unsupported type of credentials is used with a {@link AuthenticationMethod} .
     */
    public static class UnsupportedAuthenticationCredentialsException extends RuntimeException {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 4802501809913124766L;

        /**
         * Instantiates a new unsupported authentication credentials exception.
         *
         * @param message the message
         */
        public UnsupportedAuthenticationCredentialsException(String message) {
            super(message);
        }
    }

    static class AuthMethodApiResponseRepresentation<T> extends ApiResponseSet<T> {

        public AuthMethodApiResponseRepresentation(Map<String, T> values) {
            super("method", values);
        }

        @Override
        public JSON toJSON() {
            JSONObject response = new JSONObject();
            response.put(getName(), super.toJSON());
            return response;
        }
    }
}
