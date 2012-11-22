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
// ZAP: 2012/08/01 Issue 332: added support for Modes
// ZAP: 2012/11/21 Heavily refactored extension to support non-HTTP messages.

package org.parosproxy.paros.extension.manualrequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.extension.manualrequest.http.impl.ManualHttpRequestEditorDialog;
import org.parosproxy.paros.model.Session;
import org.zaproxy.zap.extension.httppanel.Message;


public class ExtensionManualRequestEditor extends ExtensionAdaptor implements SessionChangedListener {
	
	private Map<Class<? extends Message>, ManualRequestEditorDialog> dialogues = new HashMap<>();
	
	/**
	 * Name of this extension.
	 */
	public static final String NAME = "ExtensionManualRequest";

	
	public ExtensionManualRequestEditor() {
		super();
		initialize();
	}

	
	public ExtensionManualRequestEditor(String name) {
		super(name);
	}
	
	private void initialize() {
		this.setName(NAME);
        this.setOrder(36);
        
        // add default manual request editor
        ManualRequestEditorDialog httpSendEditorDialog = new ManualHttpRequestEditorDialog(true, "manual");
        httpSendEditorDialog.setTitle(Constant.messages.getString("manReq.dialog.title"));
        
        dialogues.put(httpSendEditorDialog.getMessageType(), httpSendEditorDialog);
	}
	
	/**
	 * Should be called before extension is initialized via its
	 * {@link #hook(ExtensionHook)} method.
	 * 
	 * @param dialogue
	 */
	public void addManualSendEditor(ManualRequestEditorDialog dialogue) {
		dialogues.put(dialogue.getMessageType(), dialogue);
	}

	@Override
	public void hook(ExtensionHook extensionHook) {
		super.hook(extensionHook);
		if (getView() != null) {
			for (Entry<Class<? extends Message>, ManualRequestEditorDialog> dialogue : dialogues.entrySet()) {
				extensionHook.getHookMenu().addToolsMenuItem(dialogue.getValue().getMenuItem());
			}
			
			extensionHook.addSessionListener(this);
		}
	}

	@Override
	public String getAuthor() {
		return Constant.PAROS_TEAM;
	}

	@Override
	public void sessionChanged(Session session) {
		for (Entry<Class<? extends Message>, ManualRequestEditorDialog> dialogue : dialogues.entrySet()) {
			dialogue.getValue().clear();
			dialogue.getValue().setDefaultMessage();
		}
	}

	@Override
	public void sessionAboutToChange(Session session) {
	}
	
	@Override
	public void sessionScopeChanged(Session session) {
	}
	
	@Override
	public void sessionModeChanged(Mode mode) {
		Boolean isEnabled = null;
		switch (mode) {
		case safe:
			isEnabled = false;
			break;
		case protect:
		case standard:
			isEnabled = true;
			break;
		}

		if (isEnabled != null) {
			for (Entry<Class<? extends Message>, ManualRequestEditorDialog> dialog : dialogues.entrySet()) {
				dialog.getValue().setEnabled(isEnabled);
			}
		}
	}
}