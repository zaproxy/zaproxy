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
// ZAP: 2011/11/20 Set order
// ZAP: 2012/02/18 Rationalised session handling
// ZAP: 2012/03/17 Issue 282 Added getAuthor()
// ZAP: 2012/04/25 Added @Override annotation to all appropriate methods.
// ZAP: 2012/05/03 Changed the method hook(ExtensionHook) to check if there is
// a view. 
// ZAP: 2012/07/29 Issue 43: added sessionScopeChanged event
// ZAP: 2012/08/01 Issue 332: added support for Modes
// ZAP: 2013/01/25 Removed the "(non-Javadoc)" comments.
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2014/01/28 Issue 207: Support keyboard shortcuts 
// ZAP: 2015/03/16 Issue 1525: Further database independence changes
// ZAP: 2016/04/05 Issue 2458: Fix xlint warning messages 
// ZAP: 2016/06/20 Removed unnecessary/unused constructor

package org.parosproxy.paros.extension.state;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JOptionPane;

import org.apache.commons.httpclient.HttpState;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.model.Session;
import org.zaproxy.zap.view.ZapMenuItem;

public class ExtensionState extends ExtensionAdaptor implements SessionChangedListener {

	private JCheckBoxMenuItem menuSessionTrackingEnable = null;

	private ZapMenuItem menuResetSessionState = null;

    public ExtensionState() {
        super("ExtensionState");
        this.setOrder(12);
	}
	

	@SuppressWarnings("deprecation")
	@Override
	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);
	    
	    // ZAP: Changed to check if there is a view.
	    if (getView() != null) {
		    extensionHook.getHookMenu().addEditMenuItem(extensionHook.getHookMenu().getMenuSeparator());
		    extensionHook.getHookMenu().addEditMenuItem(getMenuSessionTrackingEnable());
		    extensionHook.getHookMenu().addEditMenuItem(getMenuResetSessionState());
	    }
	}

	
	/**
	 * This method initializes menuViewImage	
	 * 	
	 * @return javax.swing.JCheckBoxMenuItem	
	 */    
	private JCheckBoxMenuItem getMenuSessionTrackingEnable() {
		if (menuSessionTrackingEnable == null) {
		    menuSessionTrackingEnable = new JCheckBoxMenuItem();
		    menuSessionTrackingEnable.setText(Constant.messages.getString("menu.edit.enableTracking"));	// ZAP: i18n
			getMenuResetSessionState().setEnabled(menuSessionTrackingEnable.isSelected());

			menuSessionTrackingEnable.addItemListener(new java.awt.event.ItemListener() { 

				@Override
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
	private ZapMenuItem getMenuResetSessionState() {
		if (menuResetSessionState == null) {
			menuResetSessionState = new ZapMenuItem("menu.edit.resetState");
			menuResetSessionState.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
				    if (getView().showConfirmDialog(Constant.messages.getString("state.reset.warning")) == JOptionPane.OK_OPTION) {
				        resetSessionState();
				    }

				}
			});
		}
		return menuResetSessionState;
	}

    @Override
    public void sessionChanged(Session session) {
        getModel().getOptionsParam().getConnectionParam().setHttpState(new HttpState());        
    }

    private void resetSessionState() {
        getModel().getOptionsParam().getConnectionParam().setHttpState(new HttpState());
    }

	@Override
	public void sessionAboutToChange(Session session) {
	}
	@Override
	public String getAuthor() {
		return Constant.PAROS_TEAM;
	}

	@Override
	public void sessionScopeChanged(Session session) {
	}
	
	@Override
	public void sessionModeChanged(Mode mode) {
		// Ignore
	}

	/**
	 * No database tables used, so all supported
	 */
	@Override
	public boolean supportsDb(String type) {
    	return true;
    }
}