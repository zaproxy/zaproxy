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
package org.zaproxy.zap.extension.autoupdate;
import java.awt.EventQueue;
import java.io.IOException;
import java.text.MessageFormat;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.httpclient.URI;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpSender;
import org.parosproxy.paros.network.HttpStatusCode;
import org.parosproxy.paros.view.WaitMessageDialog;
import org.zaproxy.zap.extension.option.OptionsParamCheckForUpdates;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ExtensionAutoUpdate extends ExtensionAdaptor{

	private JMenuItem menuItemCheckUpdate = null;
    //private static final String GF_ZAP_LATEST_OLD = "http://zaproxy.googlecode.com/svn/wiki/LatestVersion.wiki";
	// The short URL means that the number of checkForUpdates can be tracked - see http://goo.gl/info/EhdIA
    private static final String GF_ZAP_LATEST_XML_SHORT = "http://goo.gl/EhdIA";
    // The long URL is a failsafe ;)
    private static final String GF_ZAP_LATEST_XML_FULL = "http://code.google.com/p/zaproxy/wiki/LatestVersionXml";
    
    private static final String ZAP_START_TAG = "&lt;ZAP&gt;";
    private static final String ZAP_END_TAG = "&lt;/ZAP&gt;";
	private HttpSender httpSender = null;
    
    private String latestVersionName = null;
    private Logger logger = Logger.getLogger(ExtensionAutoUpdate.class);
    
    private WaitMessageDialog waitDialog = null;
    public static boolean manual = false;
    
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
	
	public void checkForUpdates(boolean manual) {
		
        // check 1 in 30 cases to avoid too frequent check.
        //if (! manual && getRandom(CHECK_FREQUENCY) != 1) {
        //    return;
        //}
		ExtensionAutoUpdate.manual = manual;
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
		
        Thread t = new Thread(new Runnable() {
            public void run() {
                latestVersionName = getLatestVersionName();
                
                if (waitDialog != null) {
                    waitDialog.setVisible(false);
                    waitDialog = null;
                }
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        
                        if (Constant.PROGRAM_VERSION.equals(latestVersionName)) {
                        	if (ExtensionAutoUpdate.manual) {
                        		getView().showMessageDialog(
                        			Constant.messages.getString("cfu.check.latest"));
                        	}
                        } else if (latestVersionName.equals("")) {
                        	if (ExtensionAutoUpdate.manual) {
                        		getView().showWarningDialog(
                    				Constant.messages.getString("cfu.check.failed"));
                        	}
                            
                        } else {
                            getView().showMessageDialog(MessageFormat.format(
                        			Constant.messages.getString("cfu.check.newer"),
                        			latestVersionName));
                        }
                    }
                });
            }
        });
        
        if (manual) {
        	waitDialog = getView().getWaitMessageDialog(
				Constant.messages.getString("cfu.check.checking"));
        }

        t.start();
        if (manual) {
        	waitDialog.setVisible(true);
        }
	}


	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);
	    if (getView() != null) {
	        extensionHook.getHookMenu().addHelpMenuItem(getMenuItemCheckUpdate());
	    }
	}
    
    private String getLatestVersionName() {
        String newVersionName = this.getLatestVersionNameFromUrl(GF_ZAP_LATEST_XML_SHORT);
        if (newVersionName.length() == 0) {
        	// Shortened version failed, try going direct
            newVersionName = this.getLatestVersionNameFromUrl(GF_ZAP_LATEST_XML_FULL);
        }

        httpSender.shutdown();
        httpSender = null;
        
        return newVersionName;
    }
    
    private String getLatestVersionNameFromUrl(String url) {
        String newVersionName = "";
        HttpMessage msg = null;
        String resBody = null;
        
        try {
            msg = new HttpMessage(new URI(url, true));
            getHttpSender().sendAndReceive(msg,true);
            if (msg.getResponseHeader().getStatusCode() != HttpStatusCode.OK) {
            	logger.error("Failed to access " + url +
            			" response " + msg.getResponseHeader().getStatusCode());
                throw new IOException();
            }
            resBody = msg.getResponseBody().toString();
            
            int startIndex = resBody.indexOf(ZAP_START_TAG);
            if (startIndex > 0) {
            	startIndex += ZAP_START_TAG.length();
                int endIndex = resBody.indexOf(ZAP_END_TAG, startIndex);
            	newVersionName = resBody.substring(startIndex, endIndex ); 
            }
            
        } catch (Exception e) {
        	logger.error("Failed to access " + url, e);
            newVersionName = "";
        }
        
        return newVersionName;
    	
    }
    
    private HttpSender getHttpSender() {
        if (httpSender == null) {
            httpSender = new HttpSender(getModel().getOptionsParam().getConnectionParam(), true);
        }
        return httpSender;
    }
}
