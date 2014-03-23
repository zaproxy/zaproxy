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

package org.zaproxy.zap.extension;

import java.awt.Component;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.zaproxy.zap.view.PopupMenuHistoryReference;
import org.zaproxy.zap.view.popup.ExtensionPopupMenuComponent;

public class ExtensionPopupMenu extends JMenu implements ExtensionPopupMenuComponent {

	private static final long serialVersionUID = 1925623776527543421L;

	public ExtensionPopupMenu() {
		super();
	}

	public ExtensionPopupMenu(String label) {
		super(label);
	}

	/**
	 * By default, the pop up menu button is enabled and the pop up menu is only enable for the given {@code invoker} if at
	 * least one of the child menu items is enable for the given {@code invoker}.
	 * <p>
	 * Although the pop up menu is allowed to contain child menus and menu items of any type of {@code JMenu} or
	 * {@code JMenuItem} the only children considered as enablers are the ones of the type of {@code PopupMenuHistoryReference}.
	 * </p>
	 * <p>
	 * The {@code PopupMenuHistoryReference}s are considered enable if the corresponding method
	 * {@code isEnableForComponent(Component)}, with {@code invoker} as parameter, returns {@code true}.
	 * </p>
	 * <p>
	 * <strong>Implementation Note:</strong> The method {@code isEnableForComponent(Component)} is called on all child
	 * {@code PopupMenuHistoryReference}s, even if a previous child has returned {@code true}, as it allows to notify all the
	 * children that the pop up menu in which they are, is being invoked. Subclasses should take it into account when overriding
	 * this the method.
	 * </p>
	 */
	@Override
	public boolean isEnableForComponent(Component invoker) {
		boolean retV = false;
		for (int index = 0; index < this.getItemCount(); index++) {
			JMenuItem item = this.getItem(index);
			if (item instanceof PopupMenuHistoryReference) {
				PopupMenuHistoryReference itemRef=(PopupMenuHistoryReference) item;
				if (itemRef.isEnableForComponent(invoker))
				{
					retV = true;
				}
			}
		}
		return retV;
	}

	public String getParentMenuName() {
		return null;
	}

	@Override
	public int getMenuIndex() {
		return -1;
	}

	public int getParentMenuIndex() {
		return -1;
	}

	public boolean isSubMenu() {
		return false;
	}

	@Override
	public boolean precedeWithSeparator() {
		return false;
	}

	@Override
	public boolean succeedWithSeparator() {
		return false;
	}

	@Override
	public boolean isSafe() {
		return true;
	}

	public void prepareShow() {
		return;
	}
}
