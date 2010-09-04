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

import java.awt.Component;
import java.awt.Container;

import javax.swing.JFrame;
import javax.swing.text.JTextComponent;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionPopupMenu;

public class PopupEncoder2Menu extends ExtensionPopupMenu {

	private static final long serialVersionUID = 1L;
	private JTextComponent lastInvoker = null;
    private JFrame parentFrame = null;
    
	/**
     * @return Returns the lastInvoker.
     */
    public JTextComponent getLastInvoker() {
        return lastInvoker;
    }
    
    /**
	 * This method initializes 
	 * 
	 */
	public PopupEncoder2Menu() {
		super();
		initialize();
	}
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setText(Constant.messages.getString("enc2.tools.menu.encdec"));


			
	}
    public boolean isEnableForComponent(Component invoker) {
        if (invoker instanceof JTextComponent) {
        	JTextComponent txt = (JTextComponent) invoker;
        	String sel = txt.getSelectedText();
        	if (sel == null || sel.length() == 0) {
        		this.setEnabled(false);
        	} else {
        		this.setEnabled(true);
        	}
        	
            setLastInvoker((JTextComponent) invoker);
            Container c = getLastInvoker().getParent();
            while (!(c instanceof JFrame)) {
                c = c.getParent();
            }
            setParentFrame((JFrame) c);
            return true;
        } else {
            setLastInvoker(null);
            return false;
        }

    }

    /**
     * @return Returns the parentFrame.
     */
    public JFrame getParentFrame() {
        return parentFrame;
    }

    /**
     * @param parentFrame The parentFrame to set.
     */
    public void setParentFrame(JFrame parentFrame) {
        this.parentFrame = parentFrame;
    }

    /**
     * @param lastInvoker The lastInvoker to set.
     */
    public void setLastInvoker(JTextComponent lastInvoker) {
        this.lastInvoker = lastInvoker;
    }

    
}
