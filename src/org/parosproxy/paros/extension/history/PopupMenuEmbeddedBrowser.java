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
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2013/03/03 Issue 547: Deprecate unused classes and methods

package org.parosproxy.paros.extension.history;

import java.awt.Component;

import javax.swing.JList;
import javax.swing.JTree;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.SiteNode;

import edu.stanford.ejalbert.BrowserLauncher;
import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;


/**
 * @deprecated No longer used/needed. It will be removed in a future release.
 */
@Deprecated
public class PopupMenuEmbeddedBrowser extends ExtensionPopupMenuItem {

	private static final long serialVersionUID = 1L;
	private ExtensionHistory extension = null;
    private Component lastInvoker = null;
    // ZAP: Changed to support BrowserLauncher
    private BrowserLauncher launcher = null;
    private boolean supported = true;

    public PopupMenuEmbeddedBrowser() {
        super();
 		initialize();
    }

    /**
     * @param label
     */
    public PopupMenuEmbeddedBrowser(String label) {
        super(label);
        initialize();
    }

	/**
	 * This method initializes this
	 */
	private void initialize() {
        this.setText(Constant.messages.getString("history.browser.popup"));

        this.setActionCommand("");
        
        this.addActionListener(new java.awt.event.ActionListener() { 

        	@Override
        	public void actionPerformed(java.awt.event.ActionEvent e) {
                HistoryReference ref = null;
                if (lastInvoker == null) {
                    return;
                }
                if (lastInvoker.getName().equalsIgnoreCase("ListLog")) {
                    ref = extension.getSelectedHistoryReference();
                    showBrowser(ref);                                   

                } else if (lastInvoker.getName().equals("treeSite")) {
                    JTree tree = (JTree) lastInvoker;
                    SiteNode node = (SiteNode) tree.getLastSelectedPathComponent();
                    ref = node.getHistoryReference();
                    showBrowser(ref);
                }
        	}
        });
			
	}
	
	private BrowserLauncher getBrowserLauncher() {
		if (! supported) {
			return null;
		}
		if (launcher == null) {
			try {
				launcher = new BrowserLauncher();
			} catch (BrowserLaunchingInitializingException e) {
				supported = false;
			} catch (UnsupportedOperatingSystemException e) {
				supported = false;
			}
		}
		return launcher;
	}
	
    private void showBrowser(HistoryReference ref) {
    	if (! supported) {
    		return;
    	}
        try {
            this.getBrowserLauncher().openURLinBrowser(ref.getURI().toString());

        } catch (Exception e) {
            extension.getView().showWarningDialog(Constant.messages.getString("history.browser.warning"));
        }
        
    }

    
    @Override
    public boolean isEnableForComponent(Component invoker) {
        lastInvoker = null;

        if ( ! supported) {
        	return false;
        }
        
        if (invoker.getName() == null) {
            return false;
        }
        
        if (invoker.getName().equalsIgnoreCase("ListLog")) {
            JList<?> list = (JList<?>) invoker;
            if (list.getSelectedIndex() >= 0) {
                this.setEnabled(true);
            } else {
                this.setEnabled(false);
            }
            lastInvoker = invoker;
            return true;
        } else if (invoker.getName().equals("treeSite")) {
        	JTree tree = (JTree) invoker;
        	lastInvoker = tree;
            SiteNode node = (SiteNode) tree.getLastSelectedPathComponent();
            this.setEnabled(node != null && node.getHistoryReference() != null);
            return true;
        }
        return false;
    }
    
    void setExtension(ExtensionHistory extension) {
        this.extension = extension;
    }
	
}
