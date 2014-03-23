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
package org.zaproxy.zap.extension.stdmenus;

import javax.swing.ImageIcon;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.model.SiteNode;
import org.zaproxy.zap.extension.spider.ExtensionSpider;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.users.User;

/**
 * The Class PopupMenuSpiderURLAsUser.
 */
public class PopupMenuSpiderURLAsUser extends PopupUserMenuItemHolder {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 7007308539824098245L;

	/** The extension. */
	private ExtensionSpider extension = null;

	/**
	 * Instantiates a new popup menu spider url as user.
	 * 
	 * @param label the label
	 */
	public PopupMenuSpiderURLAsUser(String label) {
		super(label, Constant.messages.getString("attack.site.popup"));
		this.setIcon(new ImageIcon(PopupMenuSpiderURLAsUser.class.getResource("/resource/icon/16/spider.png")));
	}

	/**
	 * Gets the extension spider.
	 * 
	 * @return the extension spider
	 */
	private ExtensionSpider getExtensionSpider() {
		if (extension == null) {
			extension = (ExtensionSpider) Control.getSingleton().getExtensionLoader()
					.getExtension(ExtensionSpider.NAME);
		}
		return extension;
	}

	@Override
	public PopupUserMenu getPopupUserMenu(Context context, User user, String parentName) {
		return new PopupUserMenu(context, user, parentName) {

			/** The Constant serialVersionUID. */
			private static final long serialVersionUID = 3914042295666599416L;

			@Override
			public void performAction(SiteNode sn) {
				if (sn != null) {
					getExtensionSpider().startScanNode(sn, getUser());
				}
			}
		};
	}

}
