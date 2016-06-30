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
package org.zaproxy.zap.extension.keyboard;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.api.API;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiImplementor;
import org.zaproxy.zap.extension.api.ApiOther;

public class KeyboardAPI extends ApiImplementor {

	private static final String PREFIX = "keyboard";
	
	private static final String OTHER_CHEETSHEET_ACTION_ORDER = "cheatsheetActionOrder";
	private static final String OTHER_CHEETSHEET_KEY_ORDER = "cheatsheetKeyOrder";
	
	private static final String PARAM_INC_UNSET = "incUnset";
	
	private ExtensionKeyboard extension;

	public KeyboardAPI(ExtensionKeyboard extension) {
		this.extension = extension;
		this.addApiOthers(new ApiOther(OTHER_CHEETSHEET_ACTION_ORDER, null, new String[] {PARAM_INC_UNSET}));
		this.addApiOthers(new ApiOther(OTHER_CHEETSHEET_KEY_ORDER, null, new String[] {PARAM_INC_UNSET}));
	}

	@Override
	public String getPrefix() {
		return PREFIX;
	}
	
	public URI getCheatSheetActionURI() throws URIException, NullPointerException {
		return new URI(API.getInstance().getBaseURL(
				API.Format.OTHER, PREFIX, API.RequestType.other, OTHER_CHEETSHEET_ACTION_ORDER, false), 
				true);
	}

	public URI getCheatSheetKeyURI() throws URIException, NullPointerException {
		return new URI(API.getInstance().getBaseURL(
				API.Format.OTHER, PREFIX, API.RequestType.other, OTHER_CHEETSHEET_KEY_ORDER, false), 
				true);
	}

	@Override
	public HttpMessage handleApiOther(HttpMessage msg, String name, JSONObject params) throws ApiException {
		if (OTHER_CHEETSHEET_ACTION_ORDER.equals(name) ||
				OTHER_CHEETSHEET_KEY_ORDER.equals(name)) {
			
			List<KeyboardShortcut> shortcuts = this.extension.getShortcuts();
			
			if (OTHER_CHEETSHEET_ACTION_ORDER.equals(name)) {
				Collections.sort(shortcuts, new Comparator<KeyboardShortcut>() {
					@Override
					public int compare(KeyboardShortcut o1, KeyboardShortcut o2) {
						return o1.getName().compareTo(o2.getName());
					}});
			} else {
				Collections.sort(shortcuts, new Comparator<KeyboardShortcut>() {
					@Override
					public int compare(KeyboardShortcut o1, KeyboardShortcut o2) {
						return o1.getKeyStrokeKeyCodeString().compareTo(o2.getKeyStrokeKeyCodeString());
					}});
			}
			
			StringBuilder response = new StringBuilder();
			response.append(Constant.messages.getString("keyboard.api.cheatsheet.header"));
			boolean incUnset = this.getParam(params, PARAM_INC_UNSET, false);
			
			for (KeyboardShortcut shortcut : shortcuts) {
				if (incUnset || shortcut.getKeyStrokeKeyCodeString().length() > 0) {
					// Only show actions with actual shortcuts
					response.append(MessageFormat.format(
							Constant.messages.getString("keyboard.api.cheatsheet.tablerow"),
							shortcut.getName(),
							shortcut.getKeyStrokeModifiersString(),
							shortcut.getKeyStrokeKeyCodeString()));
				}
			}
			response.append(Constant.messages.getString("keyboard.api.cheatsheet.footer"));
			
	    	try {
	            msg.setResponseHeader(API.getDefaultResponseHeader("text/html", response.length()));
			} catch (HttpMalformedHeaderException e) {
				throw new ApiException(ApiException.Type.INTERNAL_ERROR, name, e);
			}
	    	msg.setResponseBody(response.toString());
	    	
	    	return msg;

		} else {
			throw new ApiException(ApiException.Type.BAD_OTHER, name);
		}
	}

}
