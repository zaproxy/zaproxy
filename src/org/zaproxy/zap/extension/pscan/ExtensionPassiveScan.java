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
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.alert.ExtensionAlert;
import org.zaproxy.zap.extension.anticsrf.AntiCsrfDetectScanner;
import org.zaproxy.zap.extension.params.ParamScanner;

public class ExtensionPassiveScan extends ExtensionAdaptor implements SessionChangedListener {

	public static final String NAME = "ExtensionPassiveScan"; 
	
	private static final Logger logger = Logger.getLogger(ExtensionPassiveScan.class);

	private PassiveScannerList scannerList;
	private OptionsPassiveScan optionsPassiveScan = null;
	private PolicyPassiveScanPanel policyPanel = null;
	private PassiveScanThread pst = null;
	
	private PassiveScanParam passiveScanParam;
	
	private static final List<Class<?>> DEPENDENCIES;
	
	static {
		List<Class<?>> dep = new ArrayList<>();
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

        extensionHook.addOptionsParamSet(getPassiveScanParam());
        
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

			// XXX temporary "hack" to check if ZAP is in GUI mode (see
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
            scannerList.setAutoTagScanners(getPassiveScanParam().getAutoTagScanners());
            
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

    private PassiveScanParam getPassiveScanParam() {
        if (passiveScanParam == null) {
            passiveScanParam = new PassiveScanParam();
        }
        return passiveScanParam;
    }

	private OptionsPassiveScan getOptionsPassiveScan(PassiveScanThread passiveScanThread) {
		if (optionsPassiveScan == null) {
			optionsPassiveScan = new OptionsPassiveScan(scannerList);
		}
		return optionsPassiveScan;
	}

	@Override
	public void sessionAboutToChange(Session session) {
		getPassiveScanThread().shutdown();
		this.pst = null;
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
	public void sessionScopeChanged(Session session) {
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
	
	@Override
	public void sessionModeChanged(Mode mode) {
		// Ignore
	}
}
