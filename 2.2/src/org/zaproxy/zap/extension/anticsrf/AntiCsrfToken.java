/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
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
package org.zaproxy.zap.extension.anticsrf;

import org.parosproxy.paros.network.HttpMessage;

public class AntiCsrfToken implements Cloneable {

	private HttpMessage msg; 
	private String name;
	private String value;
	private String targetURL;
	private int formIndex;
	
	public AntiCsrfToken(HttpMessage msg, String name, String value, int formIndex) {
		super();
		this.msg = msg;
		this.name = name;
		this.value = value;
	}
	
	public HttpMessage getMsg() {
		return msg;
	}
	public void setMsg(HttpMessage msg) {
		this.msg = msg;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}

	public void setTargetURL(String targetUrl) {
		this.targetURL = targetUrl;
	}

	public String getTargetURL() {
		return targetURL;
	}

	public int getFormIndex() {
		return formIndex;
	}

	public void setFormIndex(int formIndex) {
		this.formIndex = formIndex;
	}

	@Override	
	public AntiCsrfToken clone () {
		return new AntiCsrfToken(msg, name, value, formIndex);
	}
	
}
