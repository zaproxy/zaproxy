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
package org.zaproxy.zap.extension.help;

import java.awt.Component;
import java.net.URL;

import javax.help.CSH;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.swing.JButton;
import javax.swing.JMenuItem;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ExtensionHelp extends ExtensionAdaptor {

	private static final String helpHS = "ZapHelp";

	private JMenuItem menuHelpZap = null;
	private JButton helpButton = null;

	private static HelpSet hs = null;
	private static HelpBroker hb = null;

    /**
     * 
     */
    public ExtensionHelp() {
        super();
 		initialize();
    }

    /**
     * @param name
     */
    public ExtensionHelp(String name) {
        super(name);
    }

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setName("ExtensionHelp");
	}
	
	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);

	    if (getView() != null) {	        
	        extensionHook.getHookMenu().addHelpMenuItem(getMenuHelpZapUserGuide());

	        //View.getSingleton().addMainToolbarButton(this.getHelpButton());

	    }

	}
	
	private static HelpBroker getHelpBroker() {
		if (hb == null) {
			try {
				ClassLoader cl = HelpBroker.class.getClassLoader();  
				URL hsUrl = HelpSet.findHelpSet(
						cl, helpHS);
				if (hsUrl != null) {
					hs = new HelpSet(cl, hsUrl);
					hb = hs.createHelpBroker();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		return hb;
	}
	
	public static void enableHelpKey (Component component, String key) {
		if (getHelpBroker() != null) {
			hb.enableHelp(component, key, hs);
		}
	}

	/*
	public static void enablePopupHelpKey (Component component, String key) {
		if (getHelpBroker() != null) {
			hb.enableHelpKey(component, 
					"zap.intro", hs, "javax.help.SecondaryWindow", null);
			hb.enableHelp(component, key, hs);

		}
	}
	*/

	public static void showHelp () {
		// TODO this breaks the context sensitive help :((
		//new CSH.DisplayHelpFromSource(hb);
		/*
		getHelpBroker();
		try {
			Robot robot = new Robot();
			robot.keyPress(java.awt.event.KeyEvent.VK_F1);
			robot.keyRelease(java.awt.event.KeyEvent.VK_F1);
		} catch (AWTException e1) {
			e1.printStackTrace();
		}
		*/
		try {
			getHelpBroker().setCurrentID("zap.intro");
		} catch (Exception e) {
System.out.println("Failed " + e);
		}
	}

	private JMenuItem getMenuHelpZapUserGuide() {
		if (menuHelpZap == null) {
			menuHelpZap = new JMenuItem();
			menuHelpZap.setText(Constant.messages.getString("help.menu.guide"));
			
			if (getHelpBroker() != null) {

				// Set up the help menu item
				menuHelpZap.addActionListener(
						new CSH.DisplayHelpFromFocus(hb));

				// Enable the top level F1 help kay
				hb.enableHelpKey(this.getView().getMainFrame().getRootPane(), 
						"zap.intro", hs, "javax.help.SecondaryWindow", null);

				// Register all of the main tabs

				hb.enableHelp(this.getView().getSiteTreePanel(), 
						"ui.tabs.sites", hs);
				
				hb.enableHelp(this.getView().getRequestPanel(), 
						"ui.tabs.request", hs);
				hb.enableHelp(this.getView().getResponsePanel(), 
						"ui.tabs.response", hs);

			} else {
				System.out.println("Failed to get helpset url:(");
			}
				
			menuHelpZap.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
					java.awt.event.KeyEvent.VK_F1, 0, false));
			
		}
		return menuHelpZap;
	}
	
	@SuppressWarnings("unused")
	private JButton getHelpButton() {
		if (helpButton == null) {
			helpButton = new JButton();
			helpButton.setText("Help");
			helpButton.setToolTipText("Help");
			//helpButton.setIcon(new ImageIcon("/resource/icon/help.png"));
			helpButton.setToolTipText(Constant.messages.getString("help.button.tooltip"));
			helpButton.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {
					showHelp();
				}
			});
		}
		return helpButton;
	}

}
