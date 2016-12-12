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

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConversionException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.common.AbstractParam;

public class OptionsParamApi extends AbstractParam {

	private static final Logger LOGGER = Logger.getLogger(OptionsParamApi.class);

	public static final String ENABLED = "api.enabled";
	public static final String SECURE_ONLY = "api.secure";
	public static final String API_KEY = "api.key";
	private static final String DISABLE_KEY = "api.disablekey";
	private static final String INC_ERROR_DETAILS = "api.incerrordetails";
	private static final String AUTOFILL_KEY = "api.autofillkey";
	private static final String ENABLE_JSONP = "api.enablejsonp";
	
	private boolean enabled = true;
	private boolean secureOnly;
	private boolean disableKey;
	private boolean incErrorDetails;
	private boolean autofillKey;
	private boolean enableJSONP;

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

}
