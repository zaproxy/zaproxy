/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
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
package org.zaproxy.zap.extension.keyboard;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import org.zaproxy.zap.view.ZapMenuItem;

class KeyboardMapping {

    private ZapMenuItem menuItem;
    private String i18nKey;

    public KeyboardMapping() {
    }

    public KeyboardMapping(String i18nKey) {
    	this.i18nKey = i18nKey;
    }

    public KeyboardMapping(ZapMenuItem menuItem) {
        this.menuItem = menuItem;
    }

    public String getName() {
    	if (this.menuItem != null) {
    		return this.menuItem.getText();
    	}
    	return null;
    }
    
    public String getIdentifier() {
    	if (this.menuItem != null) {
    		return this.menuItem.getIdenfifier();
    	}
    	return this.i18nKey;
    	
    }
    
    public KeyStroke getKeyStroke() {
    	if (this.menuItem != null) {
    		return this.menuItem.getAccelerator();
    	}
    	return null;
    }

    public String getKeyStrokeKeyCodeString() {
    	if (this.menuItem == null || this.menuItem.getAccelerator() == null) {
    		return "";
    	}
		int keyCode = this.menuItem.getAccelerator().getKeyCode();
		if (keyCode >= KeyEvent.VK_F1 && keyCode <= KeyEvent.VK_F12) {
			// Function key
			return "F" + (keyCode - KeyEvent.VK_F1 + 1 ); 
		} else {
			// A 'normal' key
			return String.valueOf((char)keyCode).toUpperCase();
		}
    }

    public String getKeyStrokeModifiersString() {
    	if (this.menuItem == null || this.menuItem.getAccelerator() == null) {
    		return "";
    	}
    	KeyStroke ks = this.menuItem.getAccelerator();
    	StringBuilder sb = new StringBuilder();
    	
    	if ((ks.getModifiers() & InputEvent.CTRL_DOWN_MASK) > 0) {
    		sb.append("Control ");
    	}
    	if ((ks.getModifiers() & InputEvent.ALT_DOWN_MASK) > 0) {
    		sb.append("Alt ");
    	}
    	if ((ks.getModifiers() & InputEvent.SHIFT_DOWN_MASK) > 0) {
    		sb.append("Shift ");
    	}
		return sb.toString();
    }

    public String getKeyStrokeString() {
    	if (this.menuItem == null || this.menuItem.getAccelerator() == null) {
    		return "";
    	}
		return getKeyStrokeModifiersString() + " " + getKeyStrokeKeyCodeString();
    }
    
    public void setKeyStroke(KeyStroke keyStroke) {
    	if (this.menuItem != null) {
    		this.menuItem.setAccelerator(keyStroke);
    	}
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + ((menuItem == null) ? 0 : menuItem.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
    	if (obj == null) {
    		return false;
    	}
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        KeyboardMapping other = (KeyboardMapping) obj;
        if (menuItem == null) {
            if (other.menuItem != null) {
                return false;
            }
        } else if (!menuItem.equals(other.menuItem)) {
            return false;
        }
        return true;
    }

}
