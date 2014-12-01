/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2011 The Zed Attack Proxy Team
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
package org.zaproxy.clientapi.ant;

import org.apache.tools.ant.Task;

public class AlertTask extends Task {
	private String alert;
	private String risk;
	/**
	 * @deprecated
	 * Use of reliability has been deprecated in favour of using confidence
	 */
	@Deprecated
	private String reliability;
	private String confidence;
	private String url;
	private String other;
	private String param;
	
	public String getAlert() {
		return alert;
	}
	public void setAlert(String alert) {
		this.alert = alert;
	}
	public String getRisk() {
		return risk;
	}
	public void setRisk(String risk) {
		this.risk = risk;
	}
	/**
	 * @deprecated (2.4.0) {@link #getConfidence()}.
	 * Use of reliability has been deprecated in favour of using confidence.
	 */
	@Deprecated
	public String getReliability() {
		return reliability;
	}
	/**
	 * @deprecated (2.4.0) Replaced by {@link #setConfidence(String)}
	 * Use of reliability has been deprecated in favour of using confidence
	 */
	@Deprecated
	public void setReliability(String reliability) {
		this.reliability = reliability;
	}
	public String getConfidence() {
		return confidence;
	}
	public void setConfidence(String confidence) {
		this.confidence = confidence;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getOther() {
		return other;
	}
	public void setOther(String other) {
		this.other = other;
	}
	public String getParam() {
		return param;
	}
	public void setParam(String param) {
		this.param = param;
	}
	
}
