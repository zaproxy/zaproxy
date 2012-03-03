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
// ZAP: 2012/03/03 Added getMenuIndex()

package org.zaproxy.zap.extension;

import java.awt.Component;

import javax.swing.JMenu;


public class ExtensionPopupMenu extends JMenu {

	private static final long serialVersionUID = 1925623776527543421L;
	
	public ExtensionPopupMenu() {
        super();
    }
    
    public ExtensionPopupMenu(String label) {
        super(label);
    }
    
    public boolean isEnableForComponent(Component invoker) {
        return true;
    }
    
    public String getParentMenuName() {
    	return null;
    }
    
    public int getMenuIndex() {
    	return -1;
    }
    
    public int getParentMenuIndex() {
    	return -1;
    }
    
    public boolean isSubMenu() {
    	return false;
    }
    
    public boolean precedeWithSeparator() {
    	return false;
    }
    
    public boolean succeedWithSeparator() {
    	return false;
    }
}
