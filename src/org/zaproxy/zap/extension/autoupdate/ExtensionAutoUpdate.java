/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2011 The Zed Attack Proxy Project
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
package org.zaproxy.zap.extension.autoupdate;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.text.MessageFormat;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.view.WaitMessageDialog;
import org.zaproxy.zap.extension.option.OptionsParamCheckForUpdates;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ExtensionAutoUpdate extends ExtensionAdaptor implements ComponentListener{

	private JMenuItem menuItemCheckUpdate = null;
    
    private Logger logger = Logger.getLogger(ExtensionAutoUpdate.class);
    
    private WaitMessageDialog waitDialog = null;
    private boolean isManual = false;
    private boolean cancelled = false;

    private CheckForUpdates checkForUpdates = null;
    
    /**
     * 
     */
    public ExtensionAutoUpdate() {
        super();
 		initialize();
   }   

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setName("ExtensionAutoUpdate");
			
	}

	/**
	 * This method initializes menuItemEncoder	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */    
	private JMenuItem getMenuItemCheckUpdate() {
		if (menuItemCheckUpdate == null) {
			menuItemCheckUpdate = new JMenuItem();
			menuItemCheckUpdate.setText(Constant.messages.getString("cfu.help.menu.check"));
			menuItemCheckUpdate.addActionListener(new java.awt.event.ActionListener() { 

				public void actionPerformed(java.awt.event.ActionEvent e) {    

					checkForUpdates(true);
				}

			});

		}
		return menuItemCheckUpdate;
	}
	
	public void checkComplete (String latestVersionName) {
		checkForUpdates = null;

        if (waitDialog != null) {
            waitDialog.setVisible(false);
            waitDialog = null;
        }
        // based on simple String compare, works in many cases
    	// breaks, in cases of 1.2.13 vs. 1.2.1
        if (cancelled) {
        	// Dont report anything to the user
        } else if (latestVersionName.equals("")) {
        	if (isManual) {
        		getView().showWarningDialog(
    				Constant.messages.getString("cfu.check.failed"));
        	}
        } else if (Constant.PROGRAM_VERSION.compareTo(latestVersionName) >= 0) {
        	if (isManual) {
        		getView().showMessageDialog(
        			Constant.messages.getString("cfu.check.latest"));
        	}
        } else {
        	getView().showMessageDialog(MessageFormat.format(
        			Constant.messages.getString("cfu.check.newer"),
        			latestVersionName));
        }
	}
	
	public void checkForUpdates(boolean manual) {
		
		if (checkForUpdates != null) {
			// Currently checking
			return;
		}
		
		isManual = manual;
		cancelled = false;
		if (! manual) {
			if (getModel().getOptionsParam().getCheckForUpdatesParam().isCheckOnStartUnset()) {
				// First time in
				OptionsParamCheckForUpdates param = getModel().getOptionsParam().getCheckForUpdatesParam();
                int result = getView().showConfirmDialog(
                		Constant.messages.getString("cfu.confirm.startCheck"));
                if (result == JOptionPane.OK_OPTION) {
                	param.setChckOnStart(1);
                } else {
                	param.setChckOnStart(0);
                }
                // Save
			    try {
			    	this.getModel().getOptionsParam().getConfig().save();
	            } catch (ConfigurationException ce) {
	            	logger.error(ce.getMessage(), ce);
	                getView().showWarningDialog(
	                		Constant.messages.getString("cfu.confirm.error"));
	                return;
	            }
			}
			if (! getModel().getOptionsParam().getCheckForUpdatesParam().isCheckOnStart()) {
				return;
			}
			
		}
		
        if (manual) {
        	waitDialog = getView().getWaitMessageDialog(Constant.messages.getString("cfu.check.checking"));
        	// Allow user to close the dialog
        	waitDialog.setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
        	waitDialog.addComponentListener(this);
        	waitDialog.setVisible(true);
        }

		checkForUpdates = new CheckForUpdates(this);
		checkForUpdates.execute();
        
	}


	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);
	    if (getView() != null) {
	        extensionHook.getHookMenu().addHelpMenuItem(getMenuItemCheckUpdate());
	    }
	}
    

	@Override
	public void componentHidden(ComponentEvent e) {
		cancelled = true;
		if (checkForUpdates != null) {
			checkForUpdates.cancel(true);
		}
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentResized(ComponentEvent e) {
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}
}
