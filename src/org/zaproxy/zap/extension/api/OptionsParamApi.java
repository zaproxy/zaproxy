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
import org.parosproxy.paros.common.AbstractParam;

public class OptionsParamApi extends AbstractParam {

	public static final String ENABLED = "api.enabled";
	public static final String SECURE_ONLY = "api.secure";
	public static final String POST_ACTIONS = "api.postactions";
	public static final String API_KEY = "api.key";
	private static final String DISABLE_KEY = "api.disablekey";
	private static final String INC_ERROR_DETAILS = "api.incerrordetails";
	private static final String AUTOFILL_KEY = "api.autofillkey";
	private static final String ENABLE_JSONP = "api.enablejsonp";
	
	private boolean enabled = false;
	private boolean secureOnly = false;
	private boolean disableKey = false;
	private boolean incErrorDetails = false;
	private boolean autofillKey = false;
	private boolean enableJSONP = false;

	private String key = "";
	//private boolean postActions = false;
	
	
    public OptionsParamApi() {
    }

    @Override
    protected void parse() {
        
	    enabled = getConfig().getBoolean(ENABLED, true);
	    secureOnly = getConfig().getBoolean(SECURE_ONLY, false);
		disableKey = getConfig().getBoolean(DISABLE_KEY, false);
		incErrorDetails = getConfig().getBoolean(INC_ERROR_DETAILS, false);
		autofillKey = getConfig().getBoolean(AUTOFILL_KEY, false);
		enableJSONP = getConfig().getBoolean(ENABLE_JSONP, false);
	    key = getConfig().getString(API_KEY, "");
	    //postActions = getConfig().getBoolean(POST_ACTIONS, false);
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
			getConfig().setProperty(API_KEY, key);
			try {
				getConfig().save();
			} catch (ConfigurationException e) {
				// Ignore
			}
		}
		return key;
	}

	public void setKey(String key) {
		this.key = key;
		getConfig().setProperty(API_KEY, key);
	}

	/*
	public boolean isPostActions() {
		return postActions;
	}

	public void setPostActions(boolean postActions) {
		this.postActions = postActions;
		getConfig().setProperty(POST_ACTIONS, postActions);
	}
	*/
    
}
