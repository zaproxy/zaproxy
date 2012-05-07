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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.configuration.FileConfiguration;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.common.DynamicLoader;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.view.View;
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
	
	private static final Logger logger = Logger.getLogger(ExtensionPassiveScan.class);
	
	private static final List<Class<?>> DEPENDENCIES;
	
	static {
		List<Class<?>> dep = new ArrayList<Class<?>>();
		dep.add(ExtensionAlert.class);
		
		DEPENDENCIES = Collections.unmodifiableList(dep);
	}
	
	public ExtensionPassiveScan() {
		super();
		initialize();
	}

	private void initialize() {
        this.setOrder(26);
        this.setName(NAME);
	}

	@Override
	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);

        extensionHook.addProxyListener(getPassiveScanThread());
        extensionHook.addSessionListener(this);
        //extensionHook.addSessionListener(getPassiveScanThread());
        if (getView() != null) {
            extensionHook.getHookView().addOptionPanel(
                    getOptionsPassiveScan(getPassiveScanThread()));
        }

	}
	
	private void addPassiveScanner (PluginPassiveScanner scanner) {
		try {
			FileConfiguration config = this.getModel().getOptionsParam().getConfig();
			scanner.setConfig(config);

			scannerList.add(scanner);

			// FIXME temporary "hack" to check if ZAP is in GUI mode (see
			// below).
			if (View.isInitialised()) {
				
				// The method getPolicyPanel() creates view elements
				// (subsequently initialising the java.awt.Toolkit) that are not
				// needed when ZAP is running in non GUI mode.
				getPolicyPanel().getPassiveScanTableModel().addScanner(scanner);
			}
			logger.info("loaded passive scan rule: " + scanner.getName());
		} catch (Exception e) {
			logger.error("Failed to load passive scanner " + scanner.getName(), e);
		}
	}
	
	private PassiveScannerList getPassiveScannerList() {
		if (scannerList == null) {
			scannerList = new PassiveScannerList();
	        
            // Read from the configs
            FileConfiguration config = this.getModel().getOptionsParam().getConfig();
            String[] pscanList = config.getStringArray(PSCAN_NAMES);
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
            List<PluginPassiveScanner> listTest = zapLoader.getFilteredObject(PluginPassiveScanner.class);
            for (PluginPassiveScanner scanner : listTest) {
        		addPassiveScanner(scanner);
            }
		}
		return scannerList;
	}
	
	public PolicyPassiveScanPanel getPolicyPanel() {
		if (policyPanel == null) {
    		policyPanel = new PolicyPassiveScanPanel();
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

	
	protected void add (RegexAutoTagScanner defn) {
        FileConfiguration config = this.getModel().getOptionsParam().getConfig();
    	String pscanName = defn.getName();

        // Add to the list
    	List<Object> names = config.getList(PSCAN_NAMES);
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
		return DEPENDENCIES;
	}

	@Override
	public void sessionAboutToChange(Session session) {
		getPassiveScanThread().shutdown();
		this.pst = null;
	}
	@Override
	public String getAuthor() {
		return Constant.ZAP_TEAM;
	}

	@Override
	public String getDescription() {
		return Constant.messages.getString("pscan.desc");
	}

	@Override
	public URL getURL() {
		try {
			return new URL(Constant.ZAP_HOMEPAGE);
		} catch (MalformedURLException e) {
			return null;
		}
	}
}
