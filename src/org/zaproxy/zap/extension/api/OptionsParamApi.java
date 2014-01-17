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

import org.parosproxy.paros.common.AbstractParam;

public class OptionsParamApi extends AbstractParam {

	public static final String ENABLED = "api.enabled";
	public static final String POST_ACTIONS = "api.postactions";
	public static final String API_KEY = "api.key";
	
	private boolean enabled = false;
	private String key = "";
	//private boolean postActions = false;
	
	
    public OptionsParamApi() {
    }

    @Override
    protected void parse() {
        
	    enabled = getConfig().getBoolean(ENABLED, true);
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

	public String getKey() {
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
