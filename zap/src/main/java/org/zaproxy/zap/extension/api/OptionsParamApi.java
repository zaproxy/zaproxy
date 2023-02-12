/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2011 The ZAP Development Team
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
package org.zaproxy.zap.extension.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.common.AbstractParam;
import org.zaproxy.zap.network.DomainMatcher;

public class OptionsParamApi extends AbstractParam {

    private static final Logger LOGGER = LogManager.getLogger(OptionsParamApi.class);

    public static final String ENABLED = "api.enabled";
    public static final String UI_ENABLED = "api.uienabled";
    public static final String SECURE_ONLY = "api.secure";
    public static final String API_KEY = "api.key";
    private static final String DISABLE_KEY = "api.disablekey";
    private static final String INC_ERROR_DETAILS = "api.incerrordetails";
    private static final String AUTOFILL_KEY = "api.autofillkey";
    private static final String ENABLE_JSONP = "api.enablejsonp";
    private static final String NO_KEY_FOR_SAFE_OPS = "api.nokeyforsafeops";
    private static final String REPORT_PERM_ERRORS = "api.reportpermerrors";
    private static final String NONCE_TTL_IN_SECS = "api.noncettlsecs";

    private static final String PROXY_PERMITTED_ADDRS_KEY = "api.addrs";
    private static final String ADDRESS_KEY = PROXY_PERMITTED_ADDRS_KEY + ".addr";
    private static final String ADDRESS_VALUE_KEY = "name";
    private static final String ADDRESS_REGEX_KEY = "regex";
    private static final String ADDRESS_ENABLED_KEY = "enabled";
    private static final String CONFIRM_REMOVE_ADDRESS = "api.addrs.confirmRemoveAddr";
    protected static final String CALLBACK_KEY = "api.callbacks.callback";
    private static final String CALLBACK_URL_KEY = "url";
    private static final String CALLBACK_PREFIX_KEY = "prefix";

    private static final int DEFAULT_NONCE_TTL_IN_SECS = 5 * 60; // 5 mins

    private static final String IPV6_LOOPBACK_ADDRS = "0:0:0:0:0:0:0:1";

    private boolean enabled = true;
    private boolean uiEnabled = true;
    private boolean secureOnly;
    private boolean disableKey;
    private boolean incErrorDetails;
    private boolean autofillKey;
    private boolean enableJSONP;
    private boolean noKeyForSafeOps;
    private boolean reportPermErrors;
    private boolean confirmRemovePermittedAddress = true;
    private List<DomainMatcher> permittedAddresses = new ArrayList<>(0);
    private List<DomainMatcher> permittedAddressesEnabled = new ArrayList<>(0);
    private int nonceTimeToLiveInSecs = DEFAULT_NONCE_TTL_IN_SECS;
    private Map<String, String> persistentCallBacks = new HashMap<>();

    private String key = "";

    public OptionsParamApi() {}

    @Override
    protected void parse() {

        enabled = getBoolean(ENABLED, true);
        uiEnabled = getBoolean(UI_ENABLED, true);
        secureOnly = getBoolean(SECURE_ONLY, false);
        disableKey = getBoolean(DISABLE_KEY, false);
        incErrorDetails = getBoolean(INC_ERROR_DETAILS, false);
        autofillKey = getBoolean(AUTOFILL_KEY, false);
        enableJSONP = getBoolean(ENABLE_JSONP, false);
        noKeyForSafeOps = getBoolean(NO_KEY_FOR_SAFE_OPS, false);
        reportPermErrors = getBoolean(REPORT_PERM_ERRORS, false);
        nonceTimeToLiveInSecs = getInt(NONCE_TTL_IN_SECS, DEFAULT_NONCE_TTL_IN_SECS);
        key = getString(API_KEY, "");
        loadPermittedAddresses();
        this.confirmRemovePermittedAddress = getBoolean(CONFIRM_REMOVE_ADDRESS, true);
        loadPersistentCallBacks();
    }

    @Override
    public OptionsParamApi clone() {
        return (OptionsParamApi) super.clone();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        getConfig().setProperty(ENABLED, enabled);
    }

    public boolean isUiEnabled() {
        return uiEnabled;
    }

    public void setUiEnabled(boolean uiEnabled) {
        this.uiEnabled = uiEnabled;
        getConfig().setProperty(UI_ENABLED, uiEnabled);
    }

    public boolean isSecureOnly() {
        return secureOnly;
    }

    public void setSecureOnly(boolean secureOnly) {
        this.secureOnly = secureOnly;
        getConfig().setProperty(SECURE_ONLY, secureOnly);
    }

    public boolean isDisableKey() {
        return disableKey;
    }

    public void setDisableKey(boolean disableKey) {
        this.disableKey = disableKey;
        getConfig().setProperty(DISABLE_KEY, disableKey);
    }

    public boolean isIncErrorDetails() {
        return incErrorDetails;
    }

    public void setIncErrorDetails(boolean incErrorDetails) {
        this.incErrorDetails = incErrorDetails;
        getConfig().setProperty(INC_ERROR_DETAILS, incErrorDetails);
    }

    public boolean isAutofillKey() {
        return autofillKey;
    }

    public void setAutofillKey(boolean autofillKey) {
        this.autofillKey = autofillKey;
        getConfig().setProperty(AUTOFILL_KEY, autofillKey);
    }

    public boolean isEnableJSONP() {
        return enableJSONP;
    }

    public void setEnableJSONP(boolean enableJSONP) {
        this.enableJSONP = enableJSONP;
        getConfig().setProperty(ENABLE_JSONP, enableJSONP);
    }

    public boolean isNoKeyForSafeOps() {
        return noKeyForSafeOps;
    }

    public void setNoKeyForSafeOps(boolean noKeyForSafeOps) {
        this.noKeyForSafeOps = noKeyForSafeOps;
        getConfig().setProperty(NO_KEY_FOR_SAFE_OPS, noKeyForSafeOps);
    }

    public boolean isReportPermErrors() {
        return reportPermErrors;
    }

    public void setReportPermErrors(boolean reportErrors) {
        this.reportPermErrors = reportErrors;
        getConfig().setProperty(REPORT_PERM_ERRORS, reportErrors);
    }

    /**
     * Gets the time to live for API nonces. This should not be accessible via the API.
     *
     * @return the time to live for API nonces
     * @since 2.6.0
     */
    @ZapApiIgnore
    public int getNonceTimeToLiveInSecs() {
        return nonceTimeToLiveInSecs;
    }

    protected String getRealKey() {
        return key;
    }

    protected String getKey() {
        if (this.isDisableKey()) {
            return "";
        } else if (key == null || key.length() == 0) {
            key = ExtensionAPI.generateApiKey();
            if (getConfig() != null) {
                getConfig().setProperty(API_KEY, key);
                try {
                    getConfig().save();
                } catch (ConfigurationException e) {
                    // Ignore
                }
            }
        }
        return key;
    }

    public void setKey(String key) {
        this.key = key;
        getConfig().setProperty(API_KEY, key);
    }

    /**
     * Tells whether or not the given client address is allowed to access the API.
     *
     * @param addr the client address to be checked
     * @return {@code true} if the given client address is allowed to access the API, {@code false}
     *     otherwise.
     * @since 2.6.0
     */
    public boolean isPermittedAddress(String addr) {
        if (addr == null || addr.isEmpty()) {
            return false;
        }

        for (DomainMatcher permAddr : permittedAddressesEnabled) {
            if (permAddr.matches(addr)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the client addresses that are allowed to access the API.
     *
     * @return the client addresses that are allowed to access the API.
     * @since 2.6.0
     */
    @ZapApiIgnore
    public List<DomainMatcher> getPermittedAddresses() {
        return permittedAddresses;
    }

    /**
     * Returns the enabled client addresses that are allowed to access the API.
     *
     * @return the enabled client addresses that are allowed to access the API.
     * @since 2.6.0
     */
    @ZapApiIgnore
    public List<DomainMatcher> getPermittedAddressesEnabled() {
        return permittedAddressesEnabled;
    }

    /**
     * Sets the client addresses that will be allowed to access the API.
     *
     * @param addrs the client addresses that will be allowed to access the API.
     * @since 2.6.0
     */
    public void setPermittedAddresses(List<DomainMatcher> addrs) {
        if (addrs == null || addrs.isEmpty()) {
            ((HierarchicalConfiguration) getConfig()).clearTree(ADDRESS_KEY);

            this.permittedAddresses = Collections.emptyList();
            this.permittedAddressesEnabled = Collections.emptyList();
            return;
        }

        this.permittedAddresses = new ArrayList<>(addrs);

        ((HierarchicalConfiguration) getConfig()).clearTree(ADDRESS_KEY);

        int size = addrs.size();
        ArrayList<DomainMatcher> enabledAddrs = new ArrayList<>(size);
        for (int i = 0; i < size; ++i) {
            String elementBaseKey = ADDRESS_KEY + "(" + i + ").";
            DomainMatcher addr = addrs.get(i);

            getConfig().setProperty(elementBaseKey + ADDRESS_VALUE_KEY, addr.getValue());
            getConfig().setProperty(elementBaseKey + ADDRESS_REGEX_KEY, addr.isRegex());
            getConfig().setProperty(elementBaseKey + ADDRESS_ENABLED_KEY, addr.isEnabled());

            if (addr.isEnabled()) {
                enabledAddrs.add(addr);
            }
        }

        enabledAddrs.trimToSize();
        this.permittedAddressesEnabled = enabledAddrs;
    }

    private void loadPermittedAddresses() {
        List<HierarchicalConfiguration> fields =
                ((HierarchicalConfiguration) getConfig()).configurationsAt(ADDRESS_KEY);
        this.permittedAddresses = new ArrayList<>(fields.size());
        ArrayList<DomainMatcher> addrsEnabled = new ArrayList<>(fields.size());
        for (HierarchicalConfiguration sub : fields) {
            String value = sub.getString(ADDRESS_VALUE_KEY, "");
            if (value.isEmpty()) {
                LOGGER.warn("Failed to read a permitted address entry, required value is empty.");
                continue;
            }

            DomainMatcher addr = null;
            boolean regex = sub.getBoolean(ADDRESS_REGEX_KEY, false);
            if (regex) {
                try {
                    Pattern pattern = DomainMatcher.createPattern(value);
                    addr = new DomainMatcher(pattern);
                } catch (IllegalArgumentException e) {
                    LOGGER.error(
                            "Failed to read a permitted address entry with regex: {}", value, e);
                }
            } else {
                addr = new DomainMatcher(value);
            }

            if (addr != null) {
                addr.setEnabled(sub.getBoolean(ADDRESS_ENABLED_KEY, true));

                permittedAddresses.add(addr);

                if (addr.isEnabled()) {
                    addrsEnabled.add(addr);
                }
            }
        }

        addrsEnabled.trimToSize();

        if (permittedAddresses.isEmpty()) {
            // None specified - add in the defaults (which can then be disabled)
            DomainMatcher addr = new DomainMatcher("127.0.0.1");
            permittedAddresses.add(addr);
            addrsEnabled.add(addr);

            addr = new DomainMatcher("localhost");
            permittedAddresses.add(addr);
            addrsEnabled.add(addr);

            addr = new DomainMatcher(API.API_DOMAIN);
            permittedAddresses.add(addr);
            addrsEnabled.add(addr);

            addr = new DomainMatcher(IPV6_LOOPBACK_ADDRS);
            permittedAddresses.add(addr);
            addrsEnabled.add(addr);
        }

        this.permittedAddressesEnabled = addrsEnabled;
    }

    /**
     * Tells whether or not the removal of a permitted address needs confirmation.
     *
     * @return {@code true} if the removal needs confirmation, {@code false} otherwise.
     * @since 2.6.0
     */
    @ZapApiIgnore
    public boolean isConfirmRemovePermittedAddress() {
        return this.confirmRemovePermittedAddress;
    }

    /**
     * Sets whether or not the removal of a permitted address needs confirmation.
     *
     * @param confirmRemove {@code true} if the removal needs confirmation, {@code false} otherwise.
     * @since 2.6.0
     */
    @ZapApiIgnore
    public void setConfirmRemovePermittedAddress(boolean confirmRemove) {
        this.confirmRemovePermittedAddress = confirmRemove;
        getConfig().setProperty(CONFIRM_REMOVE_ADDRESS, confirmRemovePermittedAddress);
    }

    private void loadPersistentCallBacks() {
        List<HierarchicalConfiguration> fields =
                ((HierarchicalConfiguration) getConfig()).configurationsAt(CALLBACK_KEY);

        persistentCallBacks = new HashMap<>(fields.size());

        for (HierarchicalConfiguration sub : fields) {
            String cbUrl = sub.getString(CALLBACK_URL_KEY, "");
            if (cbUrl.isEmpty()) {
                LOGGER.warn("Failed to read a callback entry, required url is empty.");
                continue;
            }
            String cbPrefix = sub.getString(CALLBACK_PREFIX_KEY, null);
            if (cbPrefix == null) {
                LOGGER.warn("Failed to read a callback entry, required prefix is empty.");
                continue;
            }
            persistentCallBacks.put(cbUrl, cbPrefix);
        }
    }

    private void savePersistentCallBacks() {
        ((HierarchicalConfiguration) getConfig()).clearTree(CALLBACK_KEY);

        int i = 0;
        for (Entry<String, String> entry : persistentCallBacks.entrySet()) {
            String elementBaseKey = CALLBACK_KEY + "(" + i + ").";
            getConfig().setProperty(elementBaseKey + CALLBACK_URL_KEY, entry.getKey());
            getConfig().setProperty(elementBaseKey + CALLBACK_PREFIX_KEY, entry.getValue());
            i++;
        }
        try {
            getConfig().save();
        } catch (ConfigurationException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * Add a callback which persists over ZAP restarts
     *
     * @param url the callback URL
     * @param prefix the prefix of the APIImplementor
     * @since 2.12.0
     */
    public void addPersistantCallBack(String url, String prefix) {
        this.persistentCallBacks.put(url, prefix);
        savePersistentCallBacks();
    }

    /**
     * Remove a callback which persists over ZAP restarts
     *
     * @param url a callback URL returned from addPersistantCallBack
     * @return the prefix associated with the callback URL
     * @since 2.12.0
     */
    public String removePersistantCallBack(String url) {
        String value = this.persistentCallBacks.remove(url);
        savePersistentCallBacks();
        return value;
    }

    /**
     * Returns a map of persistent callbacks (which persist over ZAP restarts)
     *
     * @return a Map of callback URL to implementor prefixes
     * @since 2.12.0
     */
    public Map<String, String> getPersistentCallBacks() {
        return this.persistentCallBacks;
    }
}
