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
package org.zaproxy.zap.extension.encoder2;

import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.OptionsChangedListener;
import org.parosproxy.paros.model.OptionsParam;
import org.zaproxy.zap.view.ZapMenuItem;

public class ExtensionEncoder2 extends ExtensionAdaptor implements OptionsChangedListener {

    private EncodeDecodeDialog encodeDecodeDialog = null;
    
    private PopupEncoder2Menu popupEncodeMenu = null;
	private ZapMenuItem toolsMenuEncoder = null;

	private EncodeDecodeParamPanel optionsPanel;
	private EncodeDecodeParam params;

    public ExtensionEncoder2() {
        super("ExtensionEncode2");
        this.setOrder(22);
	}
	

	@Override
	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);

	    if (getView() != null) {
	        extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuEncode());
	        
	        extensionHook.getHookMenu().addToolsMenuItem(getToolsMenuItemEncoder());

	        extensionHook.getHookView().addOptionPanel(getOptionsPanel());
	        extensionHook.addOptionsParamSet(getParams());
	        
	        extensionHook.addOptionsChangedListener(this);
	    }
	}

	private ZapMenuItem getToolsMenuItemEncoder() {
		if (toolsMenuEncoder == null) {
			toolsMenuEncoder = new ZapMenuItem("enc2.tools.menu.encdec",
					KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));

			toolsMenuEncoder.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {    
                    showEncodeDecodeDialog(null);
				}
			});

		}
		return toolsMenuEncoder;
	}
	
    private void showEncodeDecodeDialog(JTextComponent lastInvoker) {
        if (encodeDecodeDialog == null) {
            encodeDecodeDialog = new EncodeDecodeDialog();
            encodeDecodeDialog.updateOptions(getParams());
            /*
            // TODO doesnt work yet
            ExtensionHelp.enablePopupHelpKey(
            		encodeDecodeDialog, 
                    "ui.tabs.break");
            */
        } else {
        	if ((encodeDecodeDialog.getState() & Frame.ICONIFIED) == Frame.ICONIFIED ) {
        		// bring up to front if iconfied
        		encodeDecodeDialog.setState(Frame.NORMAL);
        	}
        }
        
        encodeDecodeDialog.setVisible(true);
        
        if (lastInvoker != null) {
            encodeDecodeDialog.setInputField(lastInvoker.getSelectedText());
        }
    }

    /**
     * This method initializes popupEncodeMenu	
     * 	
     * @return org.parosproxy.paros.extension.ExtensionPopupMenu	
     */
    private PopupEncoder2Menu getPopupMenuEncode() {
        if (popupEncodeMenu== null) {
            popupEncodeMenu = new PopupEncoder2Menu();
            popupEncodeMenu.setText(Constant.messages.getString("enc2.popup"));
            popupEncodeMenu.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    showEncodeDecodeDialog(popupEncodeMenu.getLastInvoker());
                    
                }
            });
        }
        return popupEncodeMenu;
    }

	@Override
	public String getAuthor() {
		return Constant.ZAP_TEAM;
	}

	@Override
	public String getDescription() {
		return Constant.messages.getString("enc2.desc");
	}

	@Override
	public URL getURL() {
		try {
			return new URL(Constant.ZAP_HOMEPAGE);
		} catch (MalformedURLException e) {
			return null;
		}
	}

	private EncodeDecodeParamPanel getOptionsPanel() {
		if (optionsPanel == null) {
			optionsPanel = new EncodeDecodeParamPanel();
		}
		return optionsPanel;
	}

	public EncodeDecodeParam getParams() {
		if (params == null) {
			params = new EncodeDecodeParam();
		}
		return params;
	}
	
	@Override
	public void optionsChanged(OptionsParam optionsParam) {
		if (encodeDecodeDialog != null) {
			encodeDecodeDialog.updateOptions(getParams());
		}
	}

	/**
	 * No database tables used, so all supported
	 */
	@Override
	public boolean supportsDb(String type) {
    	return true;
    }
}
