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
package org.zaproxy.zap.extension.fuzz;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JFrame;
import javax.swing.text.JTextComponent;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionPopupMenu;
import org.parosproxy.paros.view.View;

public class PopupFuzzMenu extends ExtensionPopupMenu {

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
	public PopupFuzzMenu() {
		super();
		initialize();
	}
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setText(Constant.messages.getString("fuzz.tools.menu.fuzz"));
	}

	public boolean isEnableForComponent(Component invoker) {
		boolean visible = false;
        if (invoker instanceof JTextComponent) {
        	JTextComponent txt = (JTextComponent) invoker;
        	// Only enable for request tab
			//Component parent = invoker.getParent();
			// TODO the commented out code will probably be needed when the HttpPanel changes are reapplied
			visible = true;
			/*
			while (parent != null) {
				if (parent instanceof HttpPanelRequest) {
					visible = true;
					break;
				} else if (parent instanceof HttpPanelResponse) {
					visible = false;
					break;
				}
				parent = parent.getParent();
			}
			*/
        	visible = txt.equals(View.getSingleton().getRequestPanel().getTxtBody()) ||
            		txt.equals(View.getSingleton().getRequestPanel().getTxtHeader());

        	if (visible) {
        		
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
                visible = true;
        	} else {
        		// Its not the request tab
                setLastInvoker(null);
        	}
        } else {
            setLastInvoker(null);
        }
        return visible;
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
