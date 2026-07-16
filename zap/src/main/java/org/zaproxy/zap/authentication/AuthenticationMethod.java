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

import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.extension.api.ApiResponseSet;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.session.SessionManagementMethod;
import org.zaproxy.zap.session.WebSession;
import org.zaproxy.zap.users.AuthenticationState;
import org.zaproxy.zap.users.User;

/**
 * The {@code AuthenticationMethod} represents an authentication method that can be used to
 * authenticate an entity in a particular web application.
 */
public abstract class AuthenticationMethod {

    private static final Logger LOGGER = LogManager.getLogger(AuthenticationMethod.class);

    public static final String CONTEXT_CONFIG_AUTH = Context.CONTEXT_CONFIG + ".authentication";
    public static final String CONTEXT_CONFIG_AUTH_TYPE = CONTEXT_CONFIG_AUTH + ".type";
    public static final String CONTEXT_CONFIG_AUTH_STRATEGY = CONTEXT_CONFIG_AUTH + ".strategy";
    public static final String CONTEXT_CONFIG_AUTH_POLL_URL = CONTEXT_CONFIG_AUTH + ".pollurl";
    public static final String CONTEXT_CONFIG_AUTH_POLL_DATA = CONTEXT_CONFIG_AUTH + ".polldata";
    public static final String CONTEXT_CONFIG_AUTH_POLL_HEADERS =
            CONTEXT_CONFIG_AUTH + ".pollheaders";
    public static final String CONTEXT_CONFIG_AUTH_POLL_FREQ = CONTEXT_CONFIG_AUTH + ".pollfreq";
    public static final String CONTEXT_CONFIG_AUTH_POLL_UNITS = CONTEXT_CONFIG_AUTH + ".pollunits";
    public static final String CONTEXT_CONFIG_AUTH_LOGGEDIN = CONTEXT_CONFIG_AUTH + ".loggedin";
    public static final String CONTEXT_CONFIG_AUTH_LOGGEDOUT = CONTEXT_CONFIG_AUTH + ".loggedout";

    /**
     * @deprecated Use {@link VerificationMethod#AUTH_STATE_ASSUMED_IN_STATS}.
     */
    @Deprecated
    public static final String AUTH_STATE_ASSUMED_IN_STATS =
            VerificationMethod.AUTH_STATE_ASSUMED_IN_STATS;

    /**
     * @deprecated Use {@link VerificationMethod#AUTH_STATE_LOGGED_IN_STATS}.
     */
    @Deprecated
    public static final String AUTH_STATE_LOGGED_IN_STATS =
            VerificationMethod.AUTH_STATE_LOGGED_IN_STATS;

    /**
     * @deprecated Use {@link VerificationMethod#AUTH_STATE_LOGGED_OUT_STATS}.
     */
    @Deprecated
    public static final String AUTH_STATE_LOGGED_OUT_STATS =
            VerificationMethod.AUTH_STATE_LOGGED_OUT_STATS;

    /**
     * @deprecated Use {@link VerificationMethod#AUTH_STATE_NO_INDICATOR_STATS}.
     */
    @Deprecated
    public static final String AUTH_STATE_NO_INDICATOR_STATS =
            VerificationMethod.AUTH_STATE_NO_INDICATOR_STATS;

    /**
     * @deprecated Use {@link VerificationMethod#AUTH_STATE_UNKNOWN_STATS}.
     */
    @Deprecated
    public static final String AUTH_STATE_UNKNOWN_STATS =
            VerificationMethod.AUTH_STATE_UNKNOWN_STATS;

    public static final String TOKEN_PREFIX = "{%";
    public static final String TOKEN_POSTFIX = "%}";

    /**
     * @deprecated Use {@link VerificationMethod#DEFAULT_POLL_FREQUENCY}.
     */
    @Deprecated
    public static final int DEFAULT_POLL_FREQUENCY = VerificationMethod.DEFAULT_POLL_FREQUENCY;

    public static enum AuthCheckingStrategy {
        EACH_RESP,
        EACH_REQ,
        EACH_REQ_RESP,
        POLL_URL,
        AUTO_DETECT
    }

    public static enum AuthPollFrequencyUnits {
        REQUESTS,
        SECONDS
    }

    private VerificationMethod verificationMethod;

    // Kept for binary compatibility with subclasses. Use VerificationMethod for all access.
    protected Pattern loggedInIndicatorPattern;
    // Kept for binary compatibility with subclasses. Use VerificationMethod for all access.
    protected Pattern loggedOutIndicatorPattern;

    {
        verificationMethod = new VerificationMethod();
        verificationMethod.setUserDataReplacer(this::replaceUserDataInPollRequest);
    }

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
        VerificationMethod clonedVm = this.verificationMethod.clone();
        clonedVm.setUserDataReplacer(method::replaceUserDataInPollRequest);
        method.setVerificationMethod(clonedVm);
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

    public abstract void replaceUserDataInPollRequest(HttpMessage msg, User user);

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

    /**
     * Gets the {@link VerificationMethod} that handles authentication state checking for this
     * authentication method.
     *
     * @return the verification method
     * @since 2.18.0
     */
    public VerificationMethod getVerificationMethod() {
        return verificationMethod;
    }

    /**
     * Sets the {@link VerificationMethod} that handles authentication state checking for this
     * authentication method.
     *
     * @param verificationMethod the verification method to use
     * @since 2.18.0
     */
    public void setVerificationMethod(VerificationMethod verificationMethod) {
        this.verificationMethod = verificationMethod;
        verificationMethod.setUserDataReplacer(this::replaceUserDataInPollRequest);
        this.loggedInIndicatorPattern = verificationMethod.getLoggedInIndicatorPattern();
        this.loggedOutIndicatorPattern = verificationMethod.getLoggedOutIndicatorPattern();
    }

    /**
     * @deprecated Use {@link VerificationMethod#isAuthenticated(HttpMessage, User)}.
     */
    @Deprecated
    public boolean isAuthenticated(HttpMessage msg) {
        return verificationMethod.isAuthenticated(msg, null, false);
    }

    /**
     * @deprecated Use {@link VerificationMethod#isAuthenticated(HttpMessage, User)}.
     */
    @Deprecated
    public boolean isAuthenticated(HttpMessage msg, User user) {
        return verificationMethod.isAuthenticated(msg, user);
    }

    /**
     * @deprecated Use {@link VerificationMethod#isAuthenticated(HttpMessage, User, boolean)}.
     */
    @Deprecated
    public boolean isAuthenticated(HttpMessage msg, User user, boolean force) {
        return verificationMethod.isAuthenticated(msg, user, force);
    }

    /**
     * @deprecated Use {@link VerificationMethod#evaluateAuthRequest(HttpMessage,
     *     AuthenticationState)}.
     */
    @Deprecated
    public boolean evaluateAuthRequest(HttpMessage msg, AuthenticationState authState) {
        return verificationMethod.evaluateAuthRequest(msg, authState);
    }

    /**
     * @deprecated Use {@link VerificationMethod#pollAsUser(User)}.
     */
    @Deprecated
    public HttpMessage pollAsUser(User user) throws IOException {
        return verificationMethod.pollAsUser(user);
    }

    /**
     * @deprecated Use {@link VerificationMethod#getLoggedInIndicatorPattern()}.
     */
    @Deprecated
    public Pattern getLoggedInIndicatorPattern() {
        return verificationMethod.getLoggedInIndicatorPattern();
    }

    /**
     * @deprecated Use {@link VerificationMethod#setLoggedInIndicatorPattern(String)}.
     */
    @Deprecated
    public void setLoggedInIndicatorPattern(String loggedInIndicatorPattern) {
        verificationMethod.setLoggedInIndicatorPattern(loggedInIndicatorPattern);
        this.loggedInIndicatorPattern = verificationMethod.getLoggedInIndicatorPattern();
    }

    /**
     * @deprecated Use {@link VerificationMethod#getLoggedOutIndicatorPattern()}.
     */
    @Deprecated
    public Pattern getLoggedOutIndicatorPattern() {
        return verificationMethod.getLoggedOutIndicatorPattern();
    }

    /**
     * @deprecated Use {@link VerificationMethod#setLoggedOutIndicatorPattern(String)}.
     */
    @Deprecated
    public void setLoggedOutIndicatorPattern(String loggedOutIndicatorPattern) {
        verificationMethod.setLoggedOutIndicatorPattern(loggedOutIndicatorPattern);
        this.loggedOutIndicatorPattern = verificationMethod.getLoggedOutIndicatorPattern();
    }

    /**
     * @deprecated Use {@link VerificationMethod#getAuthCheckingStrategy()}.
     */
    @Deprecated
    public AuthCheckingStrategy getAuthCheckingStrategy() {
        return verificationMethod.getAuthCheckingStrategy();
    }

    /**
     * @deprecated Use {@link VerificationMethod#setAuthCheckingStrategy(AuthCheckingStrategy)}.
     */
    @Deprecated
    public void setAuthCheckingStrategy(AuthCheckingStrategy authCheckingStrategy) {
        verificationMethod.setAuthCheckingStrategy(authCheckingStrategy);
    }

    /**
     * @deprecated Use {@link VerificationMethod#getPollUrl()}.
     */
    @Deprecated
    public String getPollUrl() {
        return verificationMethod.getPollUrl();
    }

    /**
     * @deprecated Use {@link VerificationMethod#setPollUrl(String)}.
     */
    @Deprecated
    public void setPollUrl(String pollUrl) {
        verificationMethod.setPollUrl(pollUrl);
    }

    /**
     * @deprecated Use {@link VerificationMethod#getPollData()}.
     */
    @Deprecated
    public String getPollData() {
        return verificationMethod.getPollData();
    }

    /**
     * @deprecated Use {@link VerificationMethod#setPollData(String)}.
     */
    @Deprecated
    public void setPollData(String pollData) {
        verificationMethod.setPollData(pollData);
    }

    /**
     * @deprecated Use {@link VerificationMethod#getPollHeaders()}.
     */
    @Deprecated
    public String getPollHeaders() {
        return verificationMethod.getPollHeaders();
    }

    /**
     * @deprecated Use {@link VerificationMethod#setPollHeaders(String)}.
     */
    @Deprecated
    public void setPollHeaders(String pollHeaders) {
        verificationMethod.setPollHeaders(pollHeaders);
    }

    /**
     * @deprecated Use {@link VerificationMethod#getPollFrequency()}.
     */
    @Deprecated
    public int getPollFrequency() {
        return verificationMethod.getPollFrequency();
    }

    /**
     * @deprecated Use {@link VerificationMethod#setPollFrequency(int)}.
     */
    @Deprecated
    public void setPollFrequency(int pollFrequency) {
        verificationMethod.setPollFrequency(pollFrequency);
    }

    /**
     * @deprecated Use {@link VerificationMethod#getPollFrequencyUnits()}.
     */
    @Deprecated
    public AuthPollFrequencyUnits getPollFrequencyUnits() {
        return verificationMethod.getPollFrequencyUnits();
    }

    /**
     * @deprecated Use {@link VerificationMethod#setPollFrequencyUnits(AuthPollFrequencyUnits)}.
     */
    @Deprecated
    public void setPollFrequencyUnits(AuthPollFrequencyUnits pollFrequencyUnits) {
        verificationMethod.setPollFrequencyUnits(pollFrequencyUnits);
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
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        return getClass() == obj.getClass();
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
