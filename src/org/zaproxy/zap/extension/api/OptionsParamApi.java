/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
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
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.log4j.Logger;
import org.parosproxy.paros.common.AbstractParam;
import org.zaproxy.zap.network.DomainMatcher;

public class OptionsParamApi extends AbstractParam {

	private static final Logger LOGGER = Logger.getLogger(OptionsParamApi.class);

	public static final String ENABLED = "api.enabled";
	public static final String SECURE_ONLY = "api.secure";
	public static final String API_KEY = "api.key";
	private static final String DISABLE_KEY = "api.disablekey";
	private static final String INC_ERROR_DETAILS = "api.incerrordetails";
	private static final String AUTOFILL_KEY = "api.autofillkey";
	private static final String ENABLE_JSONP = "api.enablejsonp";
	
    private static final String PROXY_PERMITTED_ADDRS_KEY = "api.ipaddrs";
    private static final String ADDRESS_KEY = PROXY_PERMITTED_ADDRS_KEY + ".addr";
    private static final String ADDRESS_VALUE_KEY = "name";
    private static final String ADDRESS_REGEX_KEY = "regex";
    private static final String ADDRESS_ENABLED_KEY = "enabled";
    private static final String CONFIRM_REMOVE_EXCLUDED_DOMAIN = "api.ipaddrs.confirmRemoveAddr";

	private boolean enabled = true;
	private boolean secureOnly;
	private boolean disableKey;
	private boolean incErrorDetails;
	private boolean autofillKey;
	private boolean enableJSONP;
    private boolean confirmRemovePermittedAddress = true;
    private List<DomainMatcher> permittedAddresses = new ArrayList<>(0);
    private List<DomainMatcher> permittedAddressesEnabled = new ArrayList<>(0);

	private String key = "";
	
	
    public OptionsParamApi() {
    }

    @Override
    protected void parse() {
        
		enabled = getBooleanFromConfig(ENABLED, true);
		secureOnly = getBooleanFromConfig(SECURE_ONLY, false);
		disableKey = getBooleanFromConfig(DISABLE_KEY, false);
		incErrorDetails = getBooleanFromConfig(INC_ERROR_DETAILS, false);
		autofillKey = getBooleanFromConfig(AUTOFILL_KEY, false);
		enableJSONP = getBooleanFromConfig(ENABLE_JSONP, false);
		try {
			key = getConfig().getString(API_KEY, "");
		} catch (ConversionException e) {
			LOGGER.warn("Failed to load the option '" + key + "' caused by:", e);
			key = "";
		}
		loadPermittedAddresses();
        try {
            this.confirmRemovePermittedAddress = getConfig().getBoolean(CONFIRM_REMOVE_EXCLUDED_DOMAIN, true);
        } catch (ConversionException e) {
            LOGGER.error("Error while loading the confirm remove permitted address option: " + e.getMessage(), e);
        }
    }

	private boolean getBooleanFromConfig(String key, boolean defaultValue) {
		try {
			return getConfig().getBoolean(key, defaultValue);
		} catch (ConversionException e) {
			LOGGER.warn("Failed to load the option '" + key + "' caused by:", e);
			return defaultValue;
		}
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

	protected String getRealKey() {
		return key;
	}

	public String getKey() {
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
     * @return {@code true} if the given client address is allowed to access the API, {@code false} otherwise.
     * @since TODO Add Version
     */
    public boolean isPermittedIpAddress(String addr) {
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
     * @since TODO Add Version
     */
    @ZapApiIgnore
    public List<DomainMatcher> getPermittedAddresses() {
        return permittedAddresses;
    }

    /**
     * Returns the enabled client addresses that are allowed to access the API.
     *
     * @return the enabled client addresses that are allowed to access the API.
     * @since TODO Add Version
     */
    @ZapApiIgnore
    public List<DomainMatcher> getPermittedAddressesEnabled() {
        return permittedAddressesEnabled;
    }

    /**
     * Sets the client addresses that will be allowed to access the API.
     * 
     * @param addrs the client addresses that will be allowed to access the API.
     * @since TODO Add Version
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
            getConfig().setProperty(elementBaseKey + ADDRESS_REGEX_KEY, Boolean.valueOf(addr.isRegex()));
            getConfig().setProperty(
                    elementBaseKey + ADDRESS_ENABLED_KEY,
                    Boolean.valueOf(addr.isEnabled()));

            if (addr.isEnabled()) {
                enabledAddrs.add(addr);
            }
        }

        enabledAddrs.trimToSize();
        this.permittedAddressesEnabled = enabledAddrs;
    }

    private void loadPermittedAddresses() {
        List<HierarchicalConfiguration> fields = ((HierarchicalConfiguration) getConfig()).configurationsAt(ADDRESS_KEY);
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
                    LOGGER.error("Failed to read a permitted address entry with regex: " + value, e);
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
        
        if (permittedAddresses.size() == 0) {
            // None specified - always add localhost (which can then be disabled)
            DomainMatcher addr = new DomainMatcher("127.0.0.1");
            permittedAddresses.add(addr);
            addrsEnabled.add(addr);
        }
        
        this.permittedAddressesEnabled = addrsEnabled;
    }

    /**
     * Tells whether or not the removal of a permitted address needs confirmation.
     * 
     * @return {@code true} if the removal needs confirmation, {@code false} otherwise.
     * @since TODO 
     */
    @ZapApiIgnore
    public boolean isConfirmRemovePermittedAddress() {
        return this.confirmRemovePermittedAddress;
    }

    /**
     * Sets whether or not the removal of a permitted address needs confirmation.
     * 
     * @param confirmRemove {@code true} if the removal needs confirmation, {@code false} otherwise.
     * @since TODO
     */
    @ZapApiIgnore
    public void setConfirmRemovePermittedAddress(boolean confirmRemove) {
        this.confirmRemovePermittedAddress = confirmRemove;
        getConfig().setProperty(CONFIRM_REMOVE_EXCLUDED_DOMAIN, Boolean.valueOf(confirmRemovePermittedAddress));
    }

}
