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

// ZAP: 2011/08/04 Changed for cleanup
// ZAP: 2011/11/20 Set order
// ZAP: 2012/03/15 Changed to reset the message of the ManualRequestEditorDialog
// when a new session is created. Added the key configuration to the 
// ManualRequestEditorDialog.
// ZAP: 2012/03/17 Issue 282 Added getAuthor()
// ZAP: 2012/04/25 Added @Override annotation to all appropriate methods.
// ZAP: 2012/07/02 ManualRequestEditorDialog changed to receive Message instead
// of HttpMessage. Changed logger to static.
// ZAP: 2012/07/29 Issue 43: added sessionScopeChanged event

package org.parosproxy.paros.extension.manualrequest;

import javax.swing.JMenuItem;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.zaproxy.zap.extension.httppanel.Message;


public class ExtensionManualRequestEditor extends ExtensionAdaptor implements SessionChangedListener {

	private ManualRequestEditorDialog manualRequestEditorDialog = null;
	private JMenuItem menuManualRequestEditor = null;
	// ZAP Added logger
	private static final Logger logger = Logger.getLogger(ExtensionManualRequestEditor.class);

	
	public ExtensionManualRequestEditor() {
		super();
		initialize();
	}

	
	public ExtensionManualRequestEditor(String name) {
		super(name);
	}

	
	private void initialize() {
		this.setName("ExtensionManualRequest");
        this.setOrder(36);
	}

	@Override
	public void hook(ExtensionHook extensionHook) {
		super.hook(extensionHook);
		if (getView() != null) {
			extensionHook.getHookMenu().addToolsMenuItem(getMenuManualRequestEditor());
			
			extensionHook.addSessionListener(this);
		}
	}


	    
	private JMenuItem getMenuManualRequestEditor() {
		if (menuManualRequestEditor == null) {
			menuManualRequestEditor = new JMenuItem();
			menuManualRequestEditor.setText(Constant.messages.getString("menu.tools.manReq"));	// ZAP: i18n
			menuManualRequestEditor.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					ManualRequestEditorDialog dialog = getManualRequestEditorDialog();
					// ZAP: method was refactored from getHttpMessage() to getMessage()
					Message message = dialog.getMessage();
					if (message == null) {
					    setDefaultMessageToManualRequestEditor();
					} else if (message instanceof HttpMessage && ((HttpMessage)message).getRequestHeader().isEmpty()) {
					    setDefaultMessageToManualRequestEditor();
				    }
					dialog.setVisible(true);
				}
			});
		}
		return menuManualRequestEditor;
	}

	    
	ManualRequestEditorDialog getManualRequestEditorDialog() {
		if (manualRequestEditorDialog == null) {
			manualRequestEditorDialog = new ManualRequestEditorDialog(getView().getMainFrame(), false, true, this, "manual");
			manualRequestEditorDialog.setTitle(Constant.messages.getString("manReq.dialog.title"));	// ZAP: i18n
		}
		return manualRequestEditorDialog;
	}

	@Override
	public String getAuthor() {
		return Constant.PAROS_TEAM;
	}

	@Override
	public void sessionChanged(Session session) {
		if (manualRequestEditorDialog != null) {
			manualRequestEditorDialog.clear();
			setDefaultMessageToManualRequestEditor();
		}
	}

	@Override
	public void sessionAboutToChange(Session session) {
	}
	
	private void setDefaultMessageToManualRequestEditor() {
		HttpMessage msg = new HttpMessage();
		try {
			URI uri = new URI("http://www.any_domain_name.org/path", true);
			msg.setRequestHeader(new HttpRequestHeader(HttpRequestHeader.GET, uri, HttpHeader.HTTP10));
			// ZAP: method was refactored from setHttpMessage() to setMessage()
			manualRequestEditorDialog.setMessage(msg);
		} catch (HttpMalformedHeaderException e) {
			logger.error(e.getMessage(), e);
		} catch (URIException e) {
		    logger.error(e.getMessage(), e);
        }
	}
	
	@Override
	public void sessionScopeChanged(Session session) {
	}
}