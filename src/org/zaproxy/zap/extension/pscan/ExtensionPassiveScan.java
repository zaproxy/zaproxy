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
package org.zaproxy.zap.extension.pscan;

import java.util.List;

import org.apache.commons.configuration.FileConfiguration;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.zaproxy.zap.extension.pscan.PassiveScanDefn.TYPE;

public class ExtensionPassiveScan extends ExtensionAdaptor {

	private static final String PSCAN_NAMES = "pscans.names";

	private ProxyListenerPassiveScan proxyListener = null;
	private OptionsPassiveScan optionsPassiveScan = null;
	
	public ExtensionPassiveScan() {
		super();
		initialize();
	}

	private void initialize() {
        this.setName("ExtensionProxyScan");
	}

	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);
        
        extensionHook.addProxyListener(getProxyListenerPassiveScan());

        extensionHook.getHookView().addOptionPanel(
        		getOptionsPassiveScan(getProxyListenerPassiveScan()));
	}

	@SuppressWarnings("unchecked")
	private ProxyListenerPassiveScan getProxyListenerPassiveScan() {
        if (proxyListener == null) {
            proxyListener = new ProxyListenerPassiveScan(this);
            
            // Read from the configs
            FileConfiguration config = this.getModel().getOptionsParam().getConfig();
            List<String> pscanList = config.getList(PSCAN_NAMES);
            for (String pscanName : pscanList) {
                proxyListener.add(
                	new PassiveScanDefn(pscanName, 
                		TYPE.valueOf(config.getString("pscans." + pscanName + ".type")),
                		config.getString("pscans." + pscanName + ".config"),
                		config.getString("pscans." + pscanName + ".reqUrlRegex"),
                		config.getString("pscans." + pscanName + ".reqHeadRegex"),
                		config.getString("pscans." + pscanName + ".resHeadRegex"),
                		config.getString("pscans." + pscanName + ".resBodyRegex"),
                		config.getBoolean("pscans." + pscanName + ".enabled")));
            }

            // TODO: can these be read automatically?
            /*
             * Disabled for now - concentrating on non automated stuff first
            
            proxyListener.add(new AutocompleteScanDefn(
            		"html_type_password_autocomplete", TYPE.ALERT, "TBA"));
            */
            
        }
        return proxyListener;
	}
	
	protected void save (PassiveScanDefn defn) {
        FileConfiguration config = this.getModel().getOptionsParam().getConfig();
    	String pscanName = defn.getName();

    	// Note that the name and type cant change on a save
		config.setProperty("pscans." + pscanName + ".config", defn.getConfig());
		config.setProperty("pscans." + pscanName + ".reqUrlRegex", defn.getRequestUrlRegex());
		config.setProperty("pscans." + pscanName + ".reqHeadRegex", defn.getRequestHeaderRegex());
		config.setProperty("pscans." + pscanName + ".resHeadRegex", defn.getResponseHeaderRegex());
		config.setProperty("pscans." + pscanName + ".resBodyRegex", defn.getResponseBodyRegex());
		config.setProperty("pscans." + pscanName + ".enabled", defn.isEnabled());
		
	}

	@SuppressWarnings("unchecked")
	protected void add (PassiveScanDefn defn) {
        FileConfiguration config = this.getModel().getOptionsParam().getConfig();
    	String pscanName = defn.getName();

        // Add to the list
    	List names = config.getList(PSCAN_NAMES);
    	names.add(pscanName);
		config.setProperty(PSCAN_NAMES, names);

    	// Add the details
		config.setProperty("pscans." + pscanName + ".type", defn.getType().toString());
		config.setProperty("pscans." + pscanName + ".config", defn.getConfig());
		config.setProperty("pscans." + pscanName + ".reqUrlRegex", defn.getRequestUrlRegex());
		config.setProperty("pscans." + pscanName + ".reqHeadRegex", defn.getRequestHeaderRegex());
		config.setProperty("pscans." + pscanName + ".resHeadRegex", defn.getResponseHeaderRegex());
		config.setProperty("pscans." + pscanName + ".resBodyRegex", defn.getResponseBodyRegex());
		config.setProperty("pscans." + pscanName + ".enabled", defn.isEnabled());
		
	}

	private OptionsPassiveScan getOptionsPassiveScan(ProxyListenerPassiveScan proxyListenerPassiveScan) {
		if (optionsPassiveScan == null) {
			optionsPassiveScan = new OptionsPassiveScan(this, proxyListenerPassiveScan);
		}
		return optionsPassiveScan;
	}
}
