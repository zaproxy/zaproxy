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
package org.zaproxy.zap.users;

import java.util.List;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.authentication.AuthenticationCredentials;
import org.zaproxy.zap.authentication.AuthenticationMethod;
import org.zaproxy.zap.authentication.AuthenticationMethod.UnsupportedAuthenticationCredentialsException;
import org.zaproxy.zap.extension.authentication.ExtensionAuthentication;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.session.SessionManagementMethod;
import org.zaproxy.zap.session.WebSession;
import org.zaproxy.zap.utils.Enableable;

/** ZAP representation of a web application user. */
public class User extends Enableable {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(User.class);

    /** The id source. */
    private static int ID_SOURCE = 0;

    /** The Constant FIELD_SEPARATOR used for separating Users's field during serialization. */
    private static final String FIELD_SEPARATOR = ";";

    /** The id. */
    private int id;

    /** The name. */
    private String name;

    /** The corresponding context id. */
    private int contextId;

    /** The roles corresponding to this user. */
    // TODO: Here for future use
    @SuppressWarnings("unused")
    private List<Role> roles;

    /** The authenticated session. */
    private WebSession authenticatedSession;

    /** The authentication credentials that can be used for configuring the user. */
    private AuthenticationCredentials authenticationCredentials;

    /** The extension auth. */
    private static ExtensionAuthentication extensionAuth;

    /** The context. */
    private Context context;

    private AuthenticationState authenticationState = new AuthenticationState();

    /**
     * Instantiates a new user.
     *
     * @param contextId the context id
     * @param name the name
     */
    public User(int contextId, String name) {
        super();
        this.id = ID_SOURCE++;
        this.contextId = contextId;
        this.name = name;
    }

    /**
     * Instantiates a new user.
     *
     * @param contextId the context id
     * @param name the name
     * @param id the id
     */
    public User(int contextId, String name, int id) {
        super();
        this.id = id;
        if (this.id >= ID_SOURCE) ID_SOURCE = this.id + 1;
        this.contextId = contextId;
        this.name = name;
    }

    /**
     * Gets the name of the user.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the context id.
     *
     * @return the context id
     */
    public int getContextId() {
        return contextId;
    }

    @Override
    public String toString() {
        return "User [id="
                + id
                + ", name="
                + name
                + ", contextId="
                + contextId
                + ", enabled="
                + isEnabled()
                + "]";
    }

    /**
     * Lazy loader for getting the context to which this user corresponds.
     *
     * @return the context
     */
    public Context getContext() {
        if (context == null) {
            context = Model.getSingleton().getSession().getContext(this.contextId);
        }
        return context;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public int getId() {
        return this.id;
    }

    /**
     * Modifies a message so its Request Header/Body matches the web session corresponding to this
     * user.
     *
     * @param message the message
     */
    public void processMessageToMatchUser(HttpMessage message) {
        // If the user is not yet authenticated, authenticate now
        // Make sure there are no simultaneous authentications for the same user
        synchronized (this) {
            if (this.requiresAuthentication()) {
                this.authenticate();
                if (this.requiresAuthentication()) {
                    log.info("Authentication failed for user: {}", name);
                    return;
                }
            }
        }
        processMessageToMatchAuthenticatedSession(message);
    }

    /**
     * Modifies a message so its Request Header/Body matches the web session corresponding to this
     * user.
     *
     * @param message the message
     */
    public void processMessageToMatchAuthenticatedSession(HttpMessage message) {
        getContext()
                .getSessionManagementMethod()
                .processMessageToMatchSession(message, authenticatedSession);
    }

    /**
     * Gets the configured authentication credentials of this user.
     *
     * @return the authentication credentials
     */
    public AuthenticationCredentials getAuthenticationCredentials() {
        return authenticationCredentials;
    }

    /**
     * Sets the authentication credentials for the user. These will be used to authenticate the
     * user, if necessary.
     *
     * @param authenticationCredentials the new authentication credentials
     */
    public void setAuthenticationCredentials(AuthenticationCredentials authenticationCredentials) {
        this.authenticationCredentials = authenticationCredentials;
    }

    /**
     * Checks if an authentication is needed and will be performed at the next call to {@link
     * #processMessageToMatchUser(HttpMessage)}.
     *
     * @return true, if requires authentication
     */
    public boolean requiresAuthentication() {
        return authenticatedSession == null;
    }

    /**
     * Resets the existing authenticated session, causing subsequent calls to {@link
     * #processMessageToMatchUser(HttpMessage)} to reauthenticate.
     *
     * @param unauthenticatedMessage the unauthenticated message
     */
    public void queueAuthentication(HttpMessage unauthenticatedMessage) {
        synchronized (this) {
            if (unauthenticatedMessage.getTimeSentMillis()
                    >= this.getAuthenticationState().getLastSuccessfulAuthTime())
                authenticatedSession = null;
        }
    }

    /**
     * Gets the last successful auth time.
     *
     * @return the time of last successful authentication
     * @deprecated use #getAuthenticationState().getLastSuccessfulAuthTime()
     */
    @Deprecated
    protected long getLastSuccessfulAuthTime() {
        return this.getAuthenticationState().getLastSuccessfulAuthTime();
    }

    /**
     * Checks if the response received by the Http Message corresponds to this user.
     *
     * @param msg the msg
     * @return true, if is authenticated
     */
    public boolean isAuthenticated(HttpMessage msg) {
        return getContext().getAuthenticationMethod().isAuthenticated(msg, this);
    }

    /**
     * Authenticates the user, using its authentication credentials and the authentication method
     * corresponding to its Context.
     *
     * @see SessionManagementMethod
     * @see AuthenticationMethod
     * @see Context
     */
    public void authenticate() {
        log.info("Authenticating user: {}", this.name);
        WebSession result = null;
        try {
            result =
                    getContext()
                            .getAuthenticationMethod()
                            .authenticate(
                                    getContext().getSessionManagementMethod(),
                                    this.authenticationCredentials,
                                    this);
        } catch (UnsupportedAuthenticationCredentialsException e) {
            log.error("User does not have the expected type of credentials:", e);
        } catch (Exception e) {
            log.error("An error occurred while authenticating:", e);
            return;
        }
        // no issues appear if a simultaneous call to #queueAuthentication() is made
        synchronized (this) {
            this.getAuthenticationState().setLastSuccessfulAuthTime(System.currentTimeMillis());
            this.authenticatedSession = result;
        }
    }

    /**
     * Gets a reference to the authentication extension.
     *
     * @return the authentication extension
     */
    private static ExtensionAuthentication getAuthenticationExtension() {
        if (extensionAuth == null) {
            extensionAuth =
                    Control.getSingleton()
                            .getExtensionLoader()
                            .getExtension(ExtensionAuthentication.class);
        }
        return extensionAuth;
    }

    /**
     * Encodes the User in a String. Fields that contain strings are Base64 encoded.
     *
     * @param user the user
     * @return the string
     */
    public static String encode(User user) {
        StringBuilder out = new StringBuilder();
        out.append(user.id).append(FIELD_SEPARATOR);
        out.append(user.isEnabled()).append(FIELD_SEPARATOR);
        out.append(Base64.encodeBase64String(user.name.getBytes())).append(FIELD_SEPARATOR);
        out.append(user.getContext().getAuthenticationMethod().getType().getUniqueIdentifier())
                .append(FIELD_SEPARATOR);
        out.append(user.authenticationCredentials.encode(FIELD_SEPARATOR));
        log.debug("Encoded user: {}", out);
        return out.toString();
    }

    /**
     * Decodes an User from an encoded string. The string provided as input should have been
     * obtained through calls to {@link #encode(User)}.
     *
     * @param contextId the ID of the context the user belongs to
     * @param encodedString the encoded string
     * @return the user
     */
    public static User decode(int contextId, String encodedString) {
        // Added proxy call to help in testing
        return decode(contextId, encodedString, User.getAuthenticationExtension());
    }

    /**
     * Helper method for decoding an user from an encoded string. See {@link #decode(int, String)}.
     *
     * @param contextId the ID of the context the user belongs to
     * @param encodedString the encoded string
     * @param authenticationExtension the authentication extension
     * @return the user
     */
    protected static User decode(
            int contextId, String encodedString, ExtensionAuthentication authenticationExtension) {
        String[] pieces = encodedString.split(FIELD_SEPARATOR, -1);
        User user = null;
        try {
            int id = Integer.parseInt(pieces[0]);
            if (id >= ID_SOURCE) ID_SOURCE = id + 1;
            boolean enabled = pieces[1].equals("true");
            String name = new String(Base64.decodeBase64(pieces[2]));
            int authTypeId = Integer.parseInt(pieces[3]);
            user = new User(contextId, name, id);
            user.setEnabled(enabled);

            AuthenticationCredentials cred =
                    authenticationExtension
                            .getAuthenticationMethodTypeForIdentifier(authTypeId)
                            .createAuthenticationCredentials();
            cred.decode(pieces[4]);
            user.setAuthenticationCredentials(cred);
        } catch (Exception ex) {
            log.error("An error occured while decoding user from: {}", encodedString, ex);
            return null;
        }
        log.debug("Decoded user: {}", user);
        return user;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        User other = (User) obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }

    /**
     * Sets the name.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    public HttpState getCorrespondingHttpState() {
        if (authenticatedSession != null) return authenticatedSession.getHttpState();
        else return null;
    }

    public WebSession getAuthenticatedSession() {
        return authenticatedSession;
    }

    public void setAuthenticatedSession(WebSession session) {
        this.authenticatedSession = session;
    }

    /**
     * Returns the authentication state for this user.
     *
     * @return the authentication state
     * @since 2.10.0
     */
    public AuthenticationState getAuthenticationState() {
        return authenticationState;
    }
}
