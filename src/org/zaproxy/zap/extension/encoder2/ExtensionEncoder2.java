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

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.text.JTextComponent;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.OptionsChangedListener;
import org.parosproxy.paros.model.OptionsParam;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ExtensionEncoder2 extends ExtensionAdaptor implements OptionsChangedListener {

    private EncodeDecodeDialog encodeDecodeDialog = null;
    
    private JMenuItem menuEncode = null;
    private PopupEncoder2Menu popupEncodeMenu = null;
	private JMenuItem toolsMenuEncoder = null;

	private EncodeDecodeParamPanel optionsPanel;
	private EncodeDecodeParam params;

    /**
     * 
     */
    public ExtensionEncoder2() {
        super();
 		initialize();
    }

    /**
     * @param name
     */
    public ExtensionEncoder2(String name) {
        super(name);
    }

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setName("ExtensionEncode2");
        this.setOrder(22);
	}
	

	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);

	    if (getView() != null) {
	        extensionHook.getHookMenu().addEditMenuItem(getMenuEncode());
	        extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuEncode());
	        
	        extensionHook.getHookMenu().addToolsMenuItem(getToolsMenuItemEncoder());

	        extensionHook.getHookView().addOptionPanel(getOptionsPanel());
	        extensionHook.addOptionsParamSet(getParams());
	        
	        extensionHook.addOptionsChangedListener(this);
	    }
	}

	private JMenuItem getToolsMenuItemEncoder() {
		if (toolsMenuEncoder == null) {
			toolsMenuEncoder = new JMenuItem();
			toolsMenuEncoder.setText(Constant.messages.getString("enc2.tools.menu.encdec"));
			toolsMenuEncoder.addActionListener(new java.awt.event.ActionListener() { 

				public void actionPerformed(java.awt.event.ActionEvent e) {    
                    showEncodeDecodeDialog(getView().getMainFrame(), null);
				}
			});

		}
		return toolsMenuEncoder;
	}
	
    private void showEncodeDecodeDialog(JFrame frame, JTextComponent lastInvoker) {
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
     * This method initializes menuEncode	
     * 	
     * @return javax.swing.JMenuItem	
     */
    private JMenuItem getMenuEncode() {
        if (menuEncode == null) {
            menuEncode = new JMenuItem();
            menuEncode.setText(Constant.messages.getString("enc2.tools.menu.encdec"));

            menuEncode.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    showEncodeDecodeDialog(getView().getMainFrame(), null);
                }
            });
        }
        return menuEncode;
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
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    showEncodeDecodeDialog(popupEncodeMenu.getParentFrame(), popupEncodeMenu.getLastInvoker());
                    
                }
            });
        }
        return popupEncodeMenu;
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
}
