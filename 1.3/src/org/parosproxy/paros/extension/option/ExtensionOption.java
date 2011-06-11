/*
 *
 * Paros and its related class files.
 * 
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2004 Chinotec Technologies Company
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Clarified Artistic License
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Clarified Artistic License for more details.
 * 
 * You should have received a copy of the Clarified Artistic License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.parosproxy.paros.extension.option;

import javax.swing.JCheckBoxMenuItem;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.ExtensionHookView;
import org.zaproxy.zap.extension.option.OptionsCheckForUpdatesPanel;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ExtensionOption extends ExtensionAdaptor {

	private JCheckBoxMenuItem menuViewImage = null;
	private OptionsConnectionPanel optionsConnectionPanel = null;
	private OptionsAuthenticationPanel optionsAuthenticationPanel = null;
	private OptionsCertificatePanel optionsCertificatePanel = null;
	private OptionsLocalProxyPanel optionsLocalProxyPanel = null;
	private OptionsViewPanel optionsViewPanel = null;
	private OptionsCheckForUpdatesPanel optionsCheckForUpdatesPanel = null;
    /**
     * 
     */
    public ExtensionOption() {
        super();
 		initialize();
    }

    /**
     * @param name
     */
    public ExtensionOption(String name) {
        super(name);
    }

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setName("ExtensionViewOption");
			
	}
	

	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);
	    if (getView() != null) {
	        ExtensionHookView pv = extensionHook.getHookView();
	        extensionHook.getHookMenu().addViewMenuItem(getMenuViewImage());
	        
	        extensionHook.getHookView().addOptionPanel(getOptionsConnectionPanel());
	        extensionHook.getHookView().addOptionPanel(getOptionsLocalProxyPanel());
	        extensionHook.getHookView().addOptionPanel(getOptionsAuthenticationPanel());
	        extensionHook.getHookView().addOptionPanel(getOptionsCertificatePanel());
	        extensionHook.getHookView().addOptionPanel(getOptionsViewPanel());
	        // ZAP: Added Check for Updates options panel
	        extensionHook.getHookView().addOptionPanel(getOptionsCheckForUpdatesPanel());
	    }
	}

	
	/**
	 * This method initializes menuViewImage	
	 * 	
	 * @return javax.swing.JCheckBoxMenuItem	
	 */    
	private JCheckBoxMenuItem getMenuViewImage() {
		if (menuViewImage == null) {
			menuViewImage = new JCheckBoxMenuItem();
			menuViewImage.setText(Constant.messages.getString("menu.view.enableImage"));	// ZAP: i18n
			menuViewImage.addItemListener(new java.awt.event.ItemListener() { 

				public void itemStateChanged(java.awt.event.ItemEvent e) {    

					getModel().getOptionsParam().getViewParam().setProcessImages(getMenuViewImage().getState() ? 1 : 0);
					
				}
			});

		}
		return menuViewImage;
	}
	/**
	 * This method initializes optionsConnectionPanel	
	 * 	
	 * @return org.parosproxy.paros.extension.viewOption.OptionsConnectionPanel	
	 */    
	private OptionsConnectionPanel getOptionsConnectionPanel() {
		if (optionsConnectionPanel == null) {
			optionsConnectionPanel = new OptionsConnectionPanel();
		}
		return optionsConnectionPanel;
	}
	/**
	 * This method initializes optionsAuthenticationPanel	
	 * 	
	 * @return org.parosproxy.paros.extension.viewOption.OptionsAuthenticationPanel	
	 */    
	private OptionsAuthenticationPanel getOptionsAuthenticationPanel() {
		if (optionsAuthenticationPanel == null) {
			optionsAuthenticationPanel = new OptionsAuthenticationPanel();
		}
		return optionsAuthenticationPanel;
	}
	/**
	 * This method initializes optionsCertificatePanel	
	 * 	
	 * @return org.parosproxy.paros.extension.viewOption.OptionsCertificatePanel	
	 */    
	private OptionsCertificatePanel getOptionsCertificatePanel() {
		if (optionsCertificatePanel == null) {
			optionsCertificatePanel = new OptionsCertificatePanel();
		}
		return optionsCertificatePanel;
	}
	/**
	 * This method initializes optionsLocalProxyPanel	
	 * 	
	 * @return org.parosproxy.paros.extension.viewOption.OptionsLocalProxyPanel	
	 */    
	private OptionsLocalProxyPanel getOptionsLocalProxyPanel() {
		if (optionsLocalProxyPanel == null) {
			optionsLocalProxyPanel = new OptionsLocalProxyPanel();
		}
		return optionsLocalProxyPanel;
	}
	/**
	 * This method initializes optionsViewPanel	
	 * 	
	 * @return org.parosproxy.paros.extension.viewOption.OptionsViewPanel	
	 */    
	private OptionsViewPanel getOptionsViewPanel() {
		if (optionsViewPanel == null) {
			optionsViewPanel = new OptionsViewPanel();
		}
		return optionsViewPanel;
	}
	private OptionsCheckForUpdatesPanel getOptionsCheckForUpdatesPanel() {
		if (optionsCheckForUpdatesPanel == null) {
			optionsCheckForUpdatesPanel = new OptionsCheckForUpdatesPanel();
		}
		return optionsCheckForUpdatesPanel;
	}
}
