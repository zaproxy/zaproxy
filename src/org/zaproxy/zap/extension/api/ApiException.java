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

import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;

import org.parosproxy.paros.Constant;

public class ApiException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public enum Type {BAD_FORMAT, BAD_TYPE, NO_IMPLEMENTOR, BAD_ACTION, BAD_VIEW, BAD_OTHER, INTERNAL_ERROR, MISSING_PARAMETER, 
		URL_NOT_FOUND, HREF_NOT_FOUND, SCAN_IN_PROGRESS, DISABLED};
	
	private String detail = null;

	public ApiException(Type type) {
		super(type.name().toLowerCase());
	}

	public ApiException(Type type, String detail) {
		super(type.name().toLowerCase());
		this.detail = detail;
	}

	public String toString(API.Format format) {
		switch(format) {
		case HTML:
		case UI:
			if (detail != null) {
				return Constant.messages.getString("api.error." + super.getMessage()) +
					"(" + super.getMessage() + ") : " + detail;
			}
			return Constant.messages.getString("api.error." + super.getMessage()) +
				"(" + super.getMessage() + ")";
			
		case XML:
			XMLSerializer serializer = new XMLSerializer();
			serializer.setObjectName("error");
			return serializer.write(this.toJSON());

		case JSON:
			return this.toJSON().toString();
		}
		return null;
	}
	
	private JSONObject toJSON () {
		JSONObject ja = new JSONObject();
		ja.put("code", super.getMessage());
		ja.put("message", Constant.messages.getString("api.error." + super.getMessage()));
		if (detail != null) {
			ja.put("detail", detail);
		}
		return ja;
	}
	
}
