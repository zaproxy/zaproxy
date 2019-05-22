/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2014 The ZAP Development team
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

package org.parosproxy.paros.core.scanner;

import org.parosproxy.paros.network.HttpMessage;

public interface ScannerHook {
	
	
	/**
	 * Should be run, once an active scan has completed. 
	 */
	void scannerComplete();
	
	/**
	 * Method that is run before plugins run their SendAndReceive method.
	 * @param msg The message that will be scanned, once this method has finished.
	 * @param plugin The current plugin.
	 */
	void beforeScan(HttpMessage msg, AbstractPlugin plugin, Scanner scanner);
	
	/**
	 * Method that is run after plugins run their SendAndReceive method.
	 * @param msg The message that was scanned by the plugin.
	 * @param plugin The current plugin.
	 */
	void afterScan(HttpMessage msg, AbstractPlugin plugin, Scanner scanner);

}
