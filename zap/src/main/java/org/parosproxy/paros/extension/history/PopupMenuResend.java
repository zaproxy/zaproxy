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
// ZAP: 2012/01/12 Reflected the rename of the class ExtensionPopupMenu to
// ExtensionPopupMenuItem.
// ZAP: 2012/04/25 Added @Override annotation to all appropriate methods.
// ZAP: 2012/07/29 Issue 43: Cleaned up access to ExtensionHistory UI
// ZAP: 2013/01/23 Clean up of exception handling/logging.
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2013/03/03 Issue 547: Deprecate unused classes and methods
// ZAP: 2015/02/09 Issue 1525: Introduce a database interface layer to allow for alternative implementations

package org.parosproxy.paros.extension.history;

import java.awt.Component;

import javax.swing.JList;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;
import org.parosproxy.paros.extension.manualrequest.ManualRequestEditorDialog;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;


/**
 * @deprecated No longer used/needed. It will be removed in a future release.
 */
@Deprecated
public class PopupMenuResend extends ExtensionPopupMenuItem {

	private static final long serialVersionUID = 2598282233227430069L;
	
	private static final Logger logger = Logger.getLogger(PopupMenuResend.class);
	
	private ExtensionHistory extension = null;
    
    public PopupMenuResend() {
        super();
 		initialize();
    }

    /**
     * @param label
     */
    public PopupMenuResend(String label) {
        super(label);
    }
    
	private void initialize() {
        this.setText(Constant.messages.getString("history.resend.popup"));	// ZAP: i18n

        this.addActionListener(new java.awt.event.ActionListener() { 

        	@Override
        	public void actionPerformed(java.awt.event.ActionEvent evt) {
        	    ManualRequestEditorDialog dialog = extension.getResendDialog();
        	    
        	    HistoryReference ref = extension.getSelectedHistoryReference();
        	    HttpMessage msg = null;
        	    try {
                    msg = ref.getHttpMessage().cloneRequest();
                    dialog.setMessage(msg);
                    dialog.setVisible(true);
                } catch (HttpMalformedHeaderException | DatabaseException e) {
                    logger.error(e.getMessage(), e);
                }
        	}
        });
	}
	
    @Override
    public boolean isEnableForComponent(Component invoker) {
        if (invoker.getName() != null && invoker.getName().equals("ListLog")) {
            try {
                JList<?> list = (JList<?>) invoker;
                if (list.getSelectedIndex() >= 0) {
                    this.setEnabled(true);
                } else {
                    this.setEnabled(false);
                }
            } catch (Exception e) {}
            return true;
        }
        return false;
    }
    
    void setExtension(ExtensionHistory extension) {
        this.extension = extension;
    }
	
}