/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2011 The Zed Attack Proxy team
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
package org.zaproxy.zap.extension.pscan;

import net.htmlparser.jericho.Source;

import org.parosproxy.paros.core.scanner.Plugin.AlertThreshold;
import org.parosproxy.paros.network.HttpMessage;

public interface PassiveScanner {

	void scanHttpRequestSend(HttpMessage msg, int id);
	
	void scanHttpResponseReceive(HttpMessage msg, int id, Source source);
	
	void setParent (PassiveScanThread parent);

	String getName();
	
	void setEnabled (boolean enabled);
	
	boolean isEnabled();
	
	AlertThreshold getLevel();
	
	void setLevel (AlertThreshold level);
	
	boolean appliesToHistoryType (int historyType);
}
