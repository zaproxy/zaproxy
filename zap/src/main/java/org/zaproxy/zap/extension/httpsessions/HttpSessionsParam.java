/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The ZAP Development Team
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.common.AbstractParam;
import org.zaproxy.zap.extension.api.ZapApiIgnore;

/**
 * The HttpSessionsParam is used to store the parameters (options) for the {@link
 * ExtensionHttpSessions} and related classes.
 */
public class HttpSessionsParam extends AbstractParam {

    /** The Constant defining the key for the default session tokens used in the application. */
    private static final String DEFAULT_TOKENS_KEY = "httpsessions.tokens";

    private static final String ALL_DEFAULT_TOKENS_KEY = DEFAULT_TOKENS_KEY + ".token";

    private static final String TOKEN_NAME_KEY = "name";
    private static final String TOKEN_ENABLED_KEY = "enabled";

    /** The Constant PROXY_ONLY_KEY defining the key for the enabledProxyOnly option. */
    private static final String PROXY_ONLY_KEY = "httpsessions.proxyOnly";

    private static final String CONFIRM_REMOVE_TOKEN_KEY = "httpsessions.confirmRemoveToken";

    /** The default tokens used when there are no saved tokens in the file. */
    private static final String[] DEFAULT_TOKENS = {
        "asp.net_sessionid",
        "aspsessionid",
        "siteserver",
        "cfid",
        "cftoken",
        "jsessionid",
        "phpsessid",
        "sessid",
        "sid",
        "viewstate",
        "zenid"
    };

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(HttpSessionsParam.class);

    /** The default tokens. */
    private List<HttpSessionToken> defaultTokens = null;

    /** The default enabled tokens. */
    private List<String> defaultTokensEnabled = null;

    /** Whether the HttpSessions extension is enabled for the proxy only. */
    private boolean enabledProxyOnly = false;

    private boolean confirmRemove = true;

    /** Instantiates a new http sessions param. */
    public HttpSessionsParam() {}

    @Override
    protected void parse() {
        // Parse the default token names
        try {
            List<HierarchicalConfiguration> fields =
                    ((HierarchicalConfiguration) getConfig())
                            .configurationsAt(ALL_DEFAULT_TOKENS_KEY);
            this.defaultTokens = new ArrayList<>(fields.size());
            this.defaultTokensEnabled = new ArrayList<>(fields.size());
            List<String> tempTokensNames = new ArrayList<>(fields.size());
            for (HierarchicalConfiguration sub : fields) {
                String name = sub.getString(TOKEN_NAME_KEY, "");
                if (!"".equals(name) && !tempTokensNames.contains(name)) {
                    boolean enabled = sub.getBoolean(TOKEN_ENABLED_KEY, true);
                    this.defaultTokens.add(new HttpSessionToken(name, enabled));
                    tempTokensNames.add(name);
                    if (enabled) {
                        this.defaultTokensEnabled.add(name);
                    }
                }
            }
        } catch (ConversionException e) {
            this.defaultTokens = new ArrayList<>(DEFAULT_TOKENS.length);
            log.error("Error while parsing config file: {}", e.getMessage(), e);
        }
        if (this.defaultTokens.isEmpty()) {
            for (String tokenName : DEFAULT_TOKENS) {
                this.defaultTokens.add(new HttpSessionToken(tokenName));
                this.defaultTokensEnabled.add(tokenName);
            }
        }

        this.enabledProxyOnly = getBoolean(PROXY_ONLY_KEY, false);
        this.confirmRemove = getBoolean(CONFIRM_REMOVE_TOKEN_KEY, true);
    }

    /**
     * Gets the default tokens.
     *
     * <p>The list of default session tokens returned is read-only view of the internal default
     * session tokens representation and any modifications will result in {@link
     * UnsupportedOperationException}. No change should be done to the {@code HttpSessionToken}s
     * contained in the list.
     *
     * @return the default tokens
     */
    public final List<HttpSessionToken> getDefaultTokens() {
        return Collections.unmodifiableList(defaultTokens);
    }

    /**
     * Gets the default tokens enabled.
     *
     * <p>The list of default session tokens enabled returned is read-only view of the internal
     * default session tokens representation and any modifications will result in {@link
     * UnsupportedOperationException}.
     *
     * @return the default tokens
     */
    public final List<String> getDefaultTokensEnabled() {
        return Collections.unmodifiableList(defaultTokensEnabled);
    }

    /**
     * Sets the default tokens.
     *
     * @param tokens the new default tokens
     */
    public void setDefaultTokens(final List<HttpSessionToken> tokens) {
        this.defaultTokens = tokens;

        saveDefaultTokens();
        this.defaultTokensEnabled =
                defaultTokens.stream()
                        .filter(HttpSessionToken::isEnabled)
                        .map(HttpSessionToken::getName)
                        .collect(Collectors.toList());
    }

    /**
     * Saves the {@link #defaultTokens default session tokens} to the {@link #getConfig()
     * configuration}.
     */
    private void saveDefaultTokens() {
        ((HierarchicalConfiguration) getConfig()).clearTree(ALL_DEFAULT_TOKENS_KEY);

        for (int i = 0, size = defaultTokens.size(); i < size; ++i) {
            String elementBaseKey = ALL_DEFAULT_TOKENS_KEY + "(" + i + ").";
            HttpSessionToken token = defaultTokens.get(i);

            getConfig().setProperty(elementBaseKey + TOKEN_NAME_KEY, token.getName());
            getConfig().setProperty(elementBaseKey + TOKEN_ENABLED_KEY, token.isEnabled());
        }
    }

    /**
     * Adds the default session token with the given name and enabled state.
     *
     * @param name the name of the session token.
     * @param enabled {@code true} if should be enabled, {@code false} otherwise.
     * @return {@code true} if the token did not exist, {@code false} otherwise.
     * @since 2.8.0
     */
    public boolean addDefaultToken(String name, boolean enabled) {
        String normalisedName = getNormalisedSessionTokenName(name);
        if (!getDefaultToken(normalisedName).isPresent()) {
            defaultTokens.add(new HttpSessionToken(normalisedName, enabled));
            if (enabled) {
                defaultTokensEnabled.add(normalisedName);
            }
            saveDefaultTokens();
            return true;
        }
        return false;
    }

    /**
     * Gets the default session token with the given name.
     *
     * @param name the name of the session token.
     * @return a container with the {@code HttpSessionToken}, or empty if not found.
     * @see #defaultTokens
     */
    private Optional<HttpSessionToken> getDefaultToken(String name) {
        return defaultTokens.stream().filter(e -> name.equalsIgnoreCase(e.getName())).findFirst();
    }

    /**
     * Sets whether or not the default session token with the given name is enabled.
     *
     * @param name the name of the session token.
     * @param enabled {@code true} if should be enabled, {@code false} otherwise.
     * @return {@code true} if the token's enabled state changed, {@code false} otherwise.
     * @since 2.8.0
     */
    public boolean setDefaultTokenEnabled(String name, boolean enabled) {
        Optional<HttpSessionToken> maybeToken =
                getDefaultToken(getNormalisedSessionTokenName(name));
        if (maybeToken.isPresent()) {
            HttpSessionToken token = maybeToken.get();
            if (token.isEnabled() == enabled) {
                return true;
            }
            if (token.isEnabled()) {
                defaultTokensEnabled.remove(token.getName());
            } else {
                defaultTokensEnabled.add(token.getName());
            }
            token.setEnabled(enabled);
            saveDefaultTokens();
            return true;
        }
        return false;
    }

    /**
     * Gets the normalised name of the given name.
     *
     * <p>Session token names are case insensitive thus normalised to be always lower-case.
     *
     * @param name the name of the session token.
     * @return the name normalised.
     */
    private static String getNormalisedSessionTokenName(String name) {
        return name.toLowerCase(Locale.ROOT);
    }

    /**
     * Removes the default session token with the given name.
     *
     * @param name the name of the session token.
     * @return {@code true} if the token existed, {@code false} otherwise.
     * @since 2.8.0
     */
    public boolean removeDefaultToken(String name) {
        String normalisedName = getNormalisedSessionTokenName(name);
        Optional<HttpSessionToken> maybeToken = getDefaultToken(normalisedName);
        if (maybeToken.isPresent()) {
            defaultTokens.remove(maybeToken.get());
            defaultTokensEnabled.remove(normalisedName);
            saveDefaultTokens();
            return true;
        }
        return false;
    }

    /**
     * Checks if the extension is only processing Proxy messages.
     *
     * @return true, if is enabled for proxy only
     */
    public boolean isEnabledProxyOnly() {
        return enabledProxyOnly;
    }

    /**
     * Sets if the extension is only processing Proxy messages.
     *
     * @param enabledProxyOnly the new enabled proxy only status
     */
    public void setEnabledProxyOnly(boolean enabledProxyOnly) {
        this.enabledProxyOnly = enabledProxyOnly;
        getConfig().setProperty(PROXY_ONLY_KEY, enabledProxyOnly);
    }

    @ZapApiIgnore
    public boolean isConfirmRemoveDefaultToken() {
        return this.confirmRemove;
    }

    @ZapApiIgnore
    public void setConfirmRemoveDefaultToken(boolean confirmRemove) {
        this.confirmRemove = confirmRemove;
        getConfig().setProperty(CONFIRM_REMOVE_TOKEN_KEY, confirmRemove);
    }
}
