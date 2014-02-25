/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2013 ZAP development team
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

import java.awt.Dimension;
import java.awt.Frame;

import javax.swing.JButton;

import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.scanner.Category;
import org.parosproxy.paros.core.scanner.PluginFactory;
import org.parosproxy.paros.core.scanner.ScannerParam;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.view.AbstractParamContainerPanel;
import org.zaproxy.zap.view.StandardFieldsDialog;

public class CustomScanDialog extends StandardFieldsDialog {

	private static final String FIELD_START = "ascan.custom.label.start";
	private static final String FIELD_RECURSE = "ascan.custom.label.recurse";
	private static final String FIELD_INSCOPE = "ascan.custom.label.inscope";
	
	private static final String FIELD_VARIENT_URL_QUERY = "variant.options.injectable.querystring.label";
	private static final String FIELD_VARIENT_URL_PATH = "variant.options.injectable.urlpath.label";
	private static final String FIELD_VARIENT_POST_DATA = "variant.options.injectable.postdata.label";
	private static final String FIELD_VARIENT_HEADERS = "variant.options.injectable.headers.label";
	private static final String FIELD_VARIENT_COOKIE = "variant.options.injectable.cookie.label";
	private static final String FIELD_VARIENT_MULTIPART = "variant.options.rpc.multipart.label";
	private static final String FIELD_VARIENT_XML = "variant.options.rpc.xml.label";
	private static final String FIELD_VARIENT_JSON = "variant.options.rpc.json.label";
	private static final String FIELD_VARIENT_GWT = "variant.options.rpc.gwt.label";
	private static final String FIELD_VARIENT_ODATA = "variant.options.rpc.odata.label";
	private static final String FIELD_VARIENT_CUSTOM = "variant.options.rpc.custom.label";

    private static Logger logger = Logger.getLogger(CustomScanDialog.class);

	private static final long serialVersionUID = 1L;

	private JButton[] extraButtons = null;
	
	private ExtensionActiveScan extension = null;
	private SiteNode node = null;

	private ScannerParam scannerParam = null;
	private OptionsParam optionsParam = null;
	private PluginFactory pluginFactory = null;

	public CustomScanDialog(ExtensionActiveScan ext, Frame owner, Dimension dim) {
		super(owner, "ascan.custom.title", dim, new String [] {
				"ascan.custom.tab.scope",
				"ascan.custom.tab.input",
				"ascan.custom.tab.policy"
			});
		this.extension = ext;

		// The first time init to the default options set, after that keep own copies
		reset(false);
	}

	public void init (SiteNode node) {
		logger.debug("init " + node);
		this.node = node;

		this.removeAllFields();
		
		this.addNodeSelectField(0, FIELD_START, node, false, false);
		this.addCheckBoxField(0, FIELD_RECURSE, true);
		this.addCheckBoxField(0, FIELD_INSCOPE, false);
		this.addPadding(0);

        int targets = scannerParam.getTargetParamsInjectable();
        this.addReadOnlyField(1, "variant.options.injectable.label", 
        		Constant.messages.getString("variant.options.injectable.label"), true);
		this.addCheckBoxField(1, FIELD_VARIENT_URL_QUERY, (targets & ScannerParam.TARGET_QUERYSTRING) != 0);
		this.addCheckBoxField(1, FIELD_VARIENT_URL_PATH, (targets & ScannerParam.TARGET_URLPATH) != 0);
		this.addCheckBoxField(1, FIELD_VARIENT_POST_DATA, (targets & ScannerParam.TARGET_POSTDATA) != 0);
		this.addCheckBoxField(1, FIELD_VARIENT_HEADERS, (targets & ScannerParam.TARGET_HTTPHEADERS) != 0);
		this.addCheckBoxField(1, FIELD_VARIENT_COOKIE, (targets & ScannerParam.TARGET_COOKIE) != 0);

        int rpcEnabled = scannerParam.getTargetParamsEnabledRPC();
        this.addReadOnlyField(1, "variant.options.rpc.label", 
        		Constant.messages.getString("variant.options.rpc.label"), true);
        this.addCheckBoxField(1, FIELD_VARIENT_MULTIPART, (rpcEnabled & ScannerParam.RPC_MULTIPART) != 0);
        this.addCheckBoxField(1, FIELD_VARIENT_XML, (rpcEnabled & ScannerParam.RPC_XML) != 0);
        this.addCheckBoxField(1, FIELD_VARIENT_JSON, (rpcEnabled & ScannerParam.RPC_JSON) != 0);
        this.addCheckBoxField(1, FIELD_VARIENT_GWT, (rpcEnabled & ScannerParam.RPC_GWT) != 0);
        this.addCheckBoxField(1, FIELD_VARIENT_ODATA, (rpcEnabled & ScannerParam.RPC_ODATA) != 0);
        this.addCheckBoxField(1, FIELD_VARIENT_CUSTOM, (rpcEnabled & ScannerParam.RPC_CUSTOM) != 0);
		this.addPadding(1);

		AbstractParamContainerPanel policyPanel = 
				new AbstractParamContainerPanel(Constant.messages.getString("ascan.custom.tab.policy"));
		String[] ROOT = {};

        PolicyAllCategoryPanel policyAllCategoryPanel = 
        		new PolicyAllCategoryPanel(optionsParam, scannerParam, this.pluginFactory, null);
        policyAllCategoryPanel.setName(Constant.messages.getString("ascan.custom.tab.policy"));
        
        policyPanel.addParamPanel(null, policyAllCategoryPanel, false);
		
        for (int i = 0; i < Category.getAllNames().length; i++) {
        	policyPanel.addParamPanel(ROOT, Category.getName(i), 
        			new PolicyCategoryPanel(i, this.pluginFactory.getAllPlugin()), true);
        }
        policyPanel.showDialog(true);
		
		this.setCustomTabPanel(2, policyPanel);
	}

	@Override
	public void siteNodeSelected(String field, SiteNode node) {
		if (node != null) {
			// The user has selected a new node
			this.node = node;
		}
	}

	private void reset(boolean refreshUi) {
		FileConfiguration fileConfig = new XMLConfiguration();
		ConfigurationUtils.copy(extension.getScannerParam().getConfig(), fileConfig);
		
		scannerParam = new ScannerParam();
		scannerParam.load(fileConfig);

		optionsParam = new OptionsParam();
		optionsParam.load(fileConfig);
		
		pluginFactory = Control.getSingleton().getPluginFactory().clone();

		if (refreshUi) {
			init (node);
			repaint();
		}
	}
	
	@Override
	public String getSaveButtonText() {
		return Constant.messages.getString("ascan.custom.button.scan");
	}

	public JButton[] getExtraButtons () {
		if (extraButtons == null) {
			JButton resetButton = new JButton(Constant.messages.getString("ascan.custom.button.reset"));
			resetButton.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					reset(true);
				}
			});

			extraButtons = new JButton[] {resetButton};
		}
		return extraButtons;
	}

	public void save() {
        // Set Injectable Targets
        int targets = 0;        
        if (this.getBoolValue(FIELD_VARIENT_URL_QUERY)) {
            targets |= ScannerParam.TARGET_QUERYSTRING;
        }
        if (this.getBoolValue(FIELD_VARIENT_URL_PATH)) {
            targets |= ScannerParam.TARGET_URLPATH;
        }
        if (this.getBoolValue(FIELD_VARIENT_POST_DATA)) {
            targets |= ScannerParam.TARGET_POSTDATA;
        }
        if (this.getBoolValue(FIELD_VARIENT_HEADERS)) {
            targets |= ScannerParam.TARGET_HTTPHEADERS;
        }
        if (this.getBoolValue(FIELD_VARIENT_COOKIE)) {
            targets |= ScannerParam.TARGET_COOKIE;
        }
        this.scannerParam.setTargetParamsInjectable(targets);

        // Set Enabled RPC schemas
        int enabledRpc = 0;
        if (this.getBoolValue(FIELD_VARIENT_MULTIPART)) {
            enabledRpc |= ScannerParam.RPC_MULTIPART;
        }
        if (this.getBoolValue(FIELD_VARIENT_XML)) {
            enabledRpc |= ScannerParam.RPC_XML;
        }
        if (this.getBoolValue(FIELD_VARIENT_JSON)) {
            enabledRpc |= ScannerParam.RPC_JSON;
        }
        if (this.getBoolValue(FIELD_VARIENT_GWT)) {
            enabledRpc |= ScannerParam.RPC_GWT;
        }
        if (this.getBoolValue(FIELD_VARIENT_ODATA)) {
            enabledRpc |= ScannerParam.RPC_ODATA;
        }
        if (this.getBoolValue(FIELD_VARIENT_CUSTOM)) {
            enabledRpc |= ScannerParam.RPC_CUSTOM;
        }
        scannerParam.setTargetParamsEnabledRPC(enabledRpc);
        
		Object [] contextSpecificObjects = new Object[] {
				scannerParam,
				pluginFactory.clone()
		};
		
		this.extension.startScanCustom(
				node, 
				this.getBoolValue(FIELD_INSCOPE), 
				this.getBoolValue(FIELD_RECURSE), 
				null,	//scanContext, 
				null,	//user, 
				contextSpecificObjects);
	}

	@Override
	public String validateFields() {
		if (this.node == null) {
			return Constant.messages.getString("ascan.custom.nostart.error");
		}
		return null;
	}
}
