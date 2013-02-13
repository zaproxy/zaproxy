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
package org.zaproxy.zap.extension.auth;

import java.util.regex.Pattern;

import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpMessage;

public class ContextAuth {

	private int contextId;
	private SiteNode loginSiteNode = null;
	private HttpMessage loginMsg = null;
	private SiteNode logoutSiteNode = null;
	private HttpMessage logoutMsg = null;
	private Pattern loggedInIndicationPattern = null;
	private Pattern loggedOutIndicationPattern = null;
	
	public ContextAuth (int contextId) {
		this.contextId = contextId;
	}
	
	public boolean canAuthenticate() {
		return  getLoginMsg() != null && (getLoggedInIndicationPattern() != null || getLoggedOutIndicationPattern() != null);
	}
	
	public SiteNode getLoginSiteNode() {
		return loginSiteNode;
	}
	public void setLoginSiteNode(SiteNode loginSiteNode) {
		this.loginSiteNode = loginSiteNode;
	}
	public HttpMessage getLoginMsg() {
		return loginMsg;
	}
	public void setLoginMsg(HttpMessage loginMsg) {
		this.loginMsg = loginMsg;
	}
	public SiteNode getLogoutSiteNode() {
		return logoutSiteNode;
	}
	public void setLogoutSiteNode(SiteNode logoutSiteNode) {
		this.logoutSiteNode = logoutSiteNode;
	}
	public HttpMessage getLogoutMsg() {
		return logoutMsg;
	}
	public void setLogoutMsg(HttpMessage logoutMsg) {
		this.logoutMsg = logoutMsg;
	}
	public Pattern getLoggedInIndicationPattern() {
		return loggedInIndicationPattern;
	}
	public void setLoggedInIndicationPattern(Pattern loggedInIndicationPattern) {
		this.loggedInIndicationPattern = loggedInIndicationPattern;
	}
	public Pattern getLoggedOutIndicationPattern() {
		return loggedOutIndicationPattern;
	}
	public void setLoggedOutIndicationPattern(Pattern loggedOutIndicationPattern) {
		this.loggedOutIndicationPattern = loggedOutIndicationPattern;
	}
	public int getContextId() {
		return contextId;
	}
	
}
