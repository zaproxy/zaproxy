/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2013 The ZAP Development Team
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
package org.zaproxy.zap.extension.pscan.scanner;

import java.io.StringWriter;
import java.util.List;

import net.htmlparser.jericho.Source;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.pscan.ExtensionPassiveScan;
import org.zaproxy.zap.extension.pscan.PassiveScanThread;
import org.zaproxy.zap.extension.pscan.PassiveScript;
import org.zaproxy.zap.extension.pscan.PluginPassiveScanner;
import org.zaproxy.zap.extension.script.ExtensionScript;
import org.zaproxy.zap.extension.script.ScriptWrapper;

public class ScriptsPassiveScanner extends PluginPassiveScanner {
	
	private ExtensionScript extension = null;
	private PassiveScanThread parent = null;
	private String name = null;
	
	// private Logger logger = Logger.getLogger(ScriptsPassiveScanner.class);

	private int currentHRefId;

	public ScriptsPassiveScanner() {
	}
	
	@Override
	public String getName() {
		if (name == null) {
			// Cache to prevent an NPE when unloaded
	    	if (Constant.messages.containsKey("pscan.scripts.passivescanner.title")) {
	    		name = Constant.messages.getString("pscan.scripts.passivescanner.title");
	    	}
	    	name = "Script passive scan rules";
		}
		return name;
	}

	private ExtensionScript getExtension() {
		if (extension == null) {
			extension = (ExtensionScript) Control.getSingleton().getExtensionLoader().getExtension(ExtensionScript.NAME);
		}
		return extension;
	}
	
	@Override
	public int getPluginId () {
		return 50001;
	}
	
	@Override
	public void scanHttpRequestSend(HttpMessage msg, int id) {
		// Ignore
	}

	@Override
	public void scanHttpResponseReceive(HttpMessage msg, int id, Source source) {
		if (this.getExtension() != null) {
			currentHRefId = id;
			List<ScriptWrapper> scripts = extension.getScripts(ExtensionPassiveScan.SCRIPT_TYPE_PASSIVE);
			for (ScriptWrapper script : scripts) {
				StringWriter writer = new StringWriter();
				try {
					if (script.isEnabled()) {
						PassiveScript s = extension.getInterface(script, PassiveScript.class);
						
						if (s != null) {
							s.scan(this, msg, source);
							
						} else {
							writer.append(Constant.messages.getString("scripts.interface.active.error"));
							extension.setError(script, writer.toString());
							extension.setEnabled(script, false);
						}
					}
					
				} catch (Exception e) {
					writer.append(e.toString());
					extension.setError(script, e);
					extension.setEnabled(script, false);
				}
			}
		}
		
	}
	
	public void raiseAlert(int risk, int confidence, String name, String description, String uri, 
			String param, String attack, String otherInfo, String solution, String evidence, 
			int cweId, int wascId, HttpMessage msg) {
		
		Alert alert = new Alert(getPluginId(), risk, confidence, name);
		     
		alert.setDetail(description, msg.getRequestHeader().getURI().toString(), 
				param, attack, otherInfo, solution, null, evidence, cweId, wascId, msg);		// Left out reference to match ScriptsActiveScanner

		this.parent.raiseAlert(currentHRefId, alert);
	}

	@Override
	public void setParent(PassiveScanThread parent) {
		this.parent = parent;
	}

}
