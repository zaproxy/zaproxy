/*
* Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright the ZAP Development Team
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
package org.zaproxy.zap.extension.ascan;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

public class PolicyManager {
	
	public static final String POLICY_EXTENSION = ".policy";
	
	private static final String DEFAULT_POLICY_NAME = Constant.messages.getString("ascan.policymgr.default.name");
    public static final String ILLEGAL_POLICY_NAME_CHRS = "/`?*\\<>|\":\t\n\r";

	private List<String> allPolicyNames = null;
	private ExtensionActiveScan extension;

    private static final Logger logger = Logger.getLogger(PolicyManager.class);

    public PolicyManager(ExtensionActiveScan extension) {
    	this.extension = extension;
    }
    
    public void init() {
    	// Force load
    	getAllPolicyNames();
    }
    
	public synchronized List<String> getAllPolicyNames() {
		if (allPolicyNames == null) {
			allPolicyNames = new ArrayList<String>();
			String[] files = Constant.getPoliciesDir().list();
			if (files != null) {
				for (String file : files) {
					if (file.endsWith(POLICY_EXTENSION)) {
						logger.debug("Found policy file " + file);
						allPolicyNames.add(file.substring(0, file.lastIndexOf(POLICY_EXTENSION)));
					}
				}
			}
			if (allPolicyNames.size() == 0) {
				// No policies :( Create a default one
				ScanPolicy defaultPolicy = new ScanPolicy();
				defaultPolicy.setName(DEFAULT_POLICY_NAME);
				// Load from the 'old' configs
				defaultPolicy.getPluginFactory().loadAllPlugin(extension.getScannerParam().getConfig());
				try {
					// Note this will add the name to allPolicyNames
					this.savePolicy(defaultPolicy);
				} catch (ConfigurationException e) {
					logger.debug("Failed to create default scan policy in " + Constant.getPoliciesDir().getAbsolutePath(), e);
				}
			}
			
			Collections.sort(allPolicyNames);
		}
		return allPolicyNames;
	}

	public void savePolicy (ScanPolicy policy) throws ConfigurationException {
		this.savePolicy(policy, null);
	}

	public void savePolicy (ScanPolicy policy, String previousName) throws ConfigurationException {
		logger.debug("Save policy " + policy.getName());

		File file = new File(Constant.getPoliciesDir(), policy.getName() + POLICY_EXTENSION);

		ZapXmlConfiguration conf = new ZapXmlConfiguration();
    	conf.setProperty("policy", policy.getName());
        conf.setProperty("scanner.level", policy.getDefaultThreshold().name());
        conf.setProperty("scanner.strength", policy.getDefaultStrength().name());

		policy.getPluginFactory().saveTo(conf);

    	conf.save(file);
    	
    	if (previousName != null && previousName.length() > 0) {
    		allPolicyNames.remove(previousName);
    	}
    	if (!allPolicyNames.contains(policy.getName())) {
    		allPolicyNames.add(policy.getName());
    		Collections.sort(allPolicyNames);
    	}
	}
	
	/**
	 * Tells whether or not a scan policy with the given {@code name} exists.
	 *
	 * @param name the name of the scan policy
	 * @return {@code true} if the scan policy exists, {@code false} otherwise
	 * @since 2.4.3
	 */
	public static boolean policyExists(String name) {
		return (new File(Constant.getPoliciesDir(), name + POLICY_EXTENSION)).exists();
		
	}

	public ScanPolicy getPolicy (String name) throws ConfigurationException {
		return this.loadPolicy(new File(Constant.getPoliciesDir(), name + POLICY_EXTENSION));
	}

	public ScanPolicy loadPolicy (String name) throws ConfigurationException {
		return this.loadPolicy(new File(Constant.getPoliciesDir(), name + POLICY_EXTENSION));
	}
	
	private ScanPolicy loadPolicy (File file) throws ConfigurationException {
		File policyFile;
		try {
			// Obtain the name of the file in correct case, for DEFAULT_POLICY_NAME it might not be exactly the same if the file
			// system is case insensitive, thus not matching with the name read directly from the file system (method
			// getAllPolicyNames()).
			policyFile = file.toPath().toRealPath().toFile();
		} catch (IOException e) {
			throw new ConfigurationException("Failed to obtain the real path of the policy file:", e);
		}
		ScanPolicy policy = new ScanPolicy(new ZapXmlConfiguration(policyFile));
		if (! policyFile.getName().equals(policy.getName() + POLICY_EXTENSION)) {
			// The file name takes precedence in case theres another policy with the same name
			policy.setName(policyFile.getName().substring(0, policyFile.getName().indexOf(POLICY_EXTENSION)));
		}
		
		return policy;
	}
	
	public void importPolicy (File file) throws ConfigurationException, IOException {
		logger.debug("Import policy from " + file.getAbsolutePath());
		ScanPolicy policy = new ScanPolicy(new ZapXmlConfiguration(file));
    	String baseName = file.getName();
    	if (baseName.endsWith(POLICY_EXTENSION)) {
    		// Stip off the extension for the 'friendly name' and if we need to prevent overwriting an existing one
    		baseName = baseName.substring(0, baseName.indexOf(POLICY_EXTENSION));
    	}
    	String finalName = baseName;
    	File newFile = new File(Constant.getPoliciesDir(), finalName + POLICY_EXTENSION);
		int i=2;
		while (newFile.exists()) {
			finalName = baseName + i; 
	    	newFile = new File(Constant.getPoliciesDir(), finalName + POLICY_EXTENSION);
	    	i++;
		}
		policy.setName(finalName);
		this.savePolicy(policy);
	}

	public void exportPolicy (ScanPolicy policy, File file) throws ConfigurationException {
		logger.debug("Export policy to " + file.getAbsolutePath());
		ZapXmlConfiguration conf = new ZapXmlConfiguration();
    	conf.setProperty("policy", policy.getName());
        conf.setProperty("scanner.level", policy.getDefaultThreshold().name());
        conf.setProperty("scanner.strength", policy.getDefaultStrength().name());
		policy.getPluginFactory().saveTo(conf);
    	conf.save(file);
	}

	public ScanPolicy getTemplatePolicy() throws ConfigurationException {
		return new ScanPolicy();
	}

	public void deletePolicy(String name) {
		logger.debug("Delete policy " + name);
		File file = new File(Constant.getPoliciesDir(), name + POLICY_EXTENSION);
		if (file.exists()) {
			file.delete();
		}
		this.allPolicyNames.remove(name);
	}

	public ScanPolicy getDefaultScanPolicy() {
		try {
			String policyName = extension.getScannerParam().getDefaultPolicy();
			if (policyExists(policyName)) {
				logger.debug("getDefaultScanPolicy: " + policyName);
				return this.loadPolicy(policyName);
			}
			// No good, try the default name
			policyName = DEFAULT_POLICY_NAME;
			if (policyExists(policyName)) {
				logger.debug("getDefaultScanPolicy (default name): " + policyName);
				return this.loadPolicy(policyName);
			}
			if (this.allPolicyNames.size() > 0) {
				// Still no joy, try the first
				logger.debug("getDefaultScanPolicy (first one): " + policyName);
				return this.loadPolicy(this.allPolicyNames.get(0));
			}
			
		} catch (ConfigurationException e) {
			logger.error(e.getMessage(), e);
		}
		// Return a new 'blank' one
		logger.debug("getDefaultScanPolicy (new blank)");
		return new ScanPolicy();
	}

	public ScanPolicy getAttackScanPolicy() {
		try {
			String policyName = extension.getScannerParam().getAttackPolicy();
			if (policyExists(policyName)) {
				return this.loadPolicy(policyName);
			}
			// No good, try the default name
			policyName = DEFAULT_POLICY_NAME;
			if (policyExists(policyName)) {
				return this.loadPolicy(policyName);
			}
			if (this.allPolicyNames.size() > 0) {
				// Still no joy, try the first
				return this.loadPolicy(this.allPolicyNames.get(0));
			}
		} catch (ConfigurationException e) {
			logger.error(e.getMessage(), e);
		}
		// Return a new 'blank' one
		return new ScanPolicy();
	}

    public boolean isLegalPolicyName(String str) {
		for (int i=0; i < str.length(); i++) {
			if (ILLEGAL_POLICY_NAME_CHRS.indexOf(str.charAt(i)) >= 0) {
				return false;
			}
		}
		return true;
	}

}
