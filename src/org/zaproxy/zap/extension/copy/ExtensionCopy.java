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
package org.zaproxy.zap.extension.copy;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.net.MalformedURLException;
import java.net.URL;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ExtensionCopy extends ExtensionAdaptor implements ClipboardOwner {

    private PopupCopyMenu popupCopyMenu = null;

    /**
     * 
     */
    public ExtensionCopy() {
        super();
 		initialize();
    }

    /**
     * @param name
     */
    public ExtensionCopy(String name) {
        super(name);
    }

	/**
	 * This method initializes this
	 */
	private void initialize() {
        this.setName("ExtensionCopy");
        this.setOrder(6);
	}
	

	@Override
	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);

	    if (getView() != null) {	        
	        extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuCopy());
	        
	    }

	}
    
    /**
     * This method initializes popupMenuFind	
     * 	
     * @return org.parosproxy.paros.extension.ExtensionPopupMenu	
     */
    private PopupCopyMenu getPopupMenuCopy() {
        if (popupCopyMenu== null) {
            popupCopyMenu = new PopupCopyMenu();
            popupCopyMenu.setText(Constant.messages.getString("copy.copy.popup"));
            popupCopyMenu.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    setClipboardContents(popupCopyMenu.getLastInvoker().getSelectedText());
                }
            });
        }
        return popupCopyMenu;
    }
    
	private void setClipboardContents (String str) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents( new StringSelection(str), this );
	}

	@Override
	public void lostOwnership(Clipboard arg0, Transferable arg1) {
		// Ignore
	}
	@Override
	public String getAuthor() {
		return Constant.ZAP_TEAM;
	}

	@Override
	public String getDescription() {
		return Constant.messages.getString("copy.desc");
	}

	@Override
	public URL getURL() {
		try {
			return new URL(Constant.ZAP_HOMEPAGE);
		} catch (MalformedURLException e) {
			return null;
		}
	}

}
