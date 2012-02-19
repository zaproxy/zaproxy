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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.FileConfiguration;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.common.DynamicLoader;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.model.Session;
import org.zaproxy.zap.extension.alert.ExtensionAlert;
import org.zaproxy.zap.extension.anticsrf.AntiCsrfDetectScanner;
import org.zaproxy.zap.extension.params.ParamScanner;
import org.zaproxy.zap.extension.pscan.scanner.RegexAutoTagScanner;
import org.zaproxy.zap.extension.pscan.scanner.RegexAutoTagScanner.TYPE;

public class ExtensionPassiveScan extends ExtensionAdaptor implements SessionChangedListener {

	public static final String NAME = "ExtensionPassiveScan"; 
	private static final String PSCAN_NAMES = "pscans.names";

	private PassiveScannerList scannerList;
	private OptionsPassiveScan optionsPassiveScan = null;
	private PolicyPassiveScanPanel policyPanel = null;
	private PassiveScanThread pst = null;
	
	public ExtensionPassiveScan() {
		super();
		initialize();
	}

	private void initialize() {
        this.setOrder(26);
        this.setName(NAME);
	}

	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);

        extensionHook.addProxyListener(getPassiveScanThread());
        extensionHook.addSessionListener(this);
        //extensionHook.addSessionListener(getPassiveScanThread());
        extensionHook.getHookView().addOptionPanel(
        		getOptionsPassiveScan(getPassiveScanThread()));

	}
	
	private void addPassiveScanner (PluginPassiveScanner scanner) {
		FileConfiguration config = this.getModel().getOptionsParam().getConfig();
		scanner.setConfig(config);

		scannerList.add(scanner);
		getPolicyPanel().getPassiveScanTableModel().addScanner(scanner);
	}
	
	@SuppressWarnings("unchecked")
	private PassiveScannerList getPassiveScannerList() {
		if (scannerList == null) {
			scannerList = new PassiveScannerList();
	        
            // Read from the configs
            FileConfiguration config = this.getModel().getOptionsParam().getConfig();
            List<String> pscanList = config.getList(PSCAN_NAMES);
            for (String pscanName : pscanList) {
            	scannerList.add(
                	new RegexAutoTagScanner(pscanName, 
                		TYPE.valueOf(config.getString("pscans." + pscanName + ".type")),
                		config.getString("pscans." + pscanName + ".config"),
                		config.getString("pscans." + pscanName + ".reqUrlRegex"),
                		config.getString("pscans." + pscanName + ".reqHeadRegex"),
                		config.getString("pscans." + pscanName + ".resHeadRegex"),
                		config.getString("pscans." + pscanName + ".resBodyRegex"),
                		config.getBoolean("pscans." + pscanName + ".enabled")));
            }
    		scannerList.add(new AntiCsrfDetectScanner());
    		scannerList.add(new ParamScanner());
            
            // Dynamically load 'switchable' plugins
           	DynamicLoader zapLoader = new DynamicLoader(Constant.FOLDER_PLUGIN, "org.zaproxy.zap.extension.pscan.scanner");
            List<Object> listTest = zapLoader.getFilteredObject(PluginPassiveScanner.class);
            for (Object obj : listTest) {
        		addPassiveScanner((PluginPassiveScanner)obj);
            }
		}
		return scannerList;
	}
	
	public PolicyPassiveScanPanel getPolicyPanel() {
		if (policyPanel == null) {
    		policyPanel = new PolicyPassiveScanPanel();
    		//policyModel = new PolicyPassiveScanTableModel();

    		//policyPanel.setPassiveScanTableModel(policyModel);
		}
		return policyPanel;
	}

	private PassiveScanThread getPassiveScanThread() {
		if (pst == null) {
	        pst = new PassiveScanThread(getPassiveScannerList());
	        
	        pst.start();
		}
		return pst;
	}

	protected void save (RegexAutoTagScanner defn) {
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
	protected void add (RegexAutoTagScanner defn) {
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

	private OptionsPassiveScan getOptionsPassiveScan(PassiveScanThread passiveScanThread) {
		if (optionsPassiveScan == null) {
			optionsPassiveScan = new OptionsPassiveScan(this, scannerList);
		}
		return optionsPassiveScan;
	}

	@Override
	public void sessionChanged(Session session) {
		// Will create a new thread if one doesnt exist
		getPassiveScanThread();
	}
	
	@Override
	public List<Class<?>> getDependencies() {
		List<Class<?>> deps = new ArrayList<Class<?>>();
		deps.add(ExtensionAlert.class);
		
		return deps;
	}

	@Override
	public void sessionAboutToChange(Session session) {
		getPassiveScanThread().shutdown();
		this.pst = null;
	}
}
