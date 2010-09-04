/*
 *
 * Paros and its related class files.
 * 
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2005 Chinotec Technologies Company
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
package org.parosproxy.paros.extension.state;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.apache.commons.httpclient.HttpState;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.ExtensionHookView;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.model.Session;
/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ExtensionState extends ExtensionAdaptor implements SessionChangedListener {

	private JCheckBoxMenuItem menuSessionTrackingEnable = null;

	private JMenuItem menuResetSessionState = null;
    /**
     * 
     */
    public ExtensionState() {
        super();
 		initialize();
    }

    /**
     * @param name
     */
    public ExtensionState(String name) {
        super(name);
    }

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setName("ExtensionState");
			
	}
	

	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);
	    ExtensionHookView pv = extensionHook.getHookView();
	    extensionHook.getHookMenu().addEditMenuItem(extensionHook.getHookMenu().getMenuSeparator());
	    extensionHook.getHookMenu().addEditMenuItem(getMenuSessionTrackingEnable());
	    extensionHook.getHookMenu().addEditMenuItem(getMenuResetSessionState());
        
        
	}

	
	/**
	 * This method initializes menuViewImage	
	 * 	
	 * @return javax.swing.JCheckBoxMenuItem	
	 */    
	private JCheckBoxMenuItem getMenuSessionTrackingEnable() {
		if (menuSessionTrackingEnable == null) {
		    menuSessionTrackingEnable = new JCheckBoxMenuItem();
		    menuSessionTrackingEnable.setText("Enable Session Tracking (Cookie)");
			getMenuResetSessionState().setEnabled(menuSessionTrackingEnable.isSelected());

			menuSessionTrackingEnable.addItemListener(new java.awt.event.ItemListener() { 

				public void itemStateChanged(java.awt.event.ItemEvent e) {    

					getModel().getOptionsParam().getConnectionParam().setHttpStateEnabled(menuSessionTrackingEnable.isEnabled());
					getMenuResetSessionState().setEnabled(menuSessionTrackingEnable.isSelected());
			        resetSessionState();
				}
			});

		}
		return menuSessionTrackingEnable;
	}
	

          //  @jve:decl-index=0:}  //  @jve:decl-index=0:
	/**
	 * This method initializes jMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */    
	private JMenuItem getMenuResetSessionState() {
		if (menuResetSessionState == null) {
			menuResetSessionState = new JMenuItem();
			menuResetSessionState.setText("Reset Session State");
			menuResetSessionState.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {
				    if (getView().showConfirmDialog("Session state will be reset.  Continue?") == JOptionPane.OK_OPTION) {
				        resetSessionState();
				    }

				}
			});
		}
		return menuResetSessionState;
	}

    /* (non-Javadoc)
     * @see org.parosproxy.paros.extension.SessionChangedListener#sessionChanged(org.parosproxy.paros.model.Session)
     */
    public void sessionChanged(Session session) {
        getModel().getOptionsParam().getConnectionParam().setHttpState(new HttpState());        
    }

    private void resetSessionState() {
        getModel().getOptionsParam().getConnectionParam().setHttpState(new HttpState());
    }


}