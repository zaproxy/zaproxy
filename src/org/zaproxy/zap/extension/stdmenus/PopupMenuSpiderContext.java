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

import java.awt.Component;

import javax.swing.ImageIcon;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;
import org.parosproxy.paros.model.SiteNode;
import org.zaproxy.zap.extension.spider.ExtensionSpider;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.view.popup.PopupMenuItemContext;

/**
 * The Class PopupMenuSpiderContext.
 */
public class PopupMenuSpiderContext extends PopupContextMenuItemHolder {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -8413132951347511212L;

	/** The extension. */
	private ExtensionSpider extension = null;

	/**
	 * Instantiates a new popup menu spider context.
	 * 
	 * @param label the label
	 */
	public PopupMenuSpiderContext(String label) {
		super(label, Constant.messages.getString("attack.site.popup"));
		this.setIcon(new ImageIcon(PopupMenuSpiderContext.class.getResource("/resource/icon/16/spider.png")));
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
	public ExtensionPopupMenuItem getPopupContextMenu(Context context, String parentName) {
		return new PopupMenuItemContext(context, parentName, context.getName()) {

			private static final long serialVersionUID = -2524944630833835369L;

			@Override
			public void performAction(SiteNode sn) {
				getExtensionSpider().startScanAllInContext(getContext(), null);
			}
		};
	}
	
	@Override
	public boolean isEnableForComponent(Component invoker) {
		if (getExtensionSpider() == null) {
			return false;
		}

		return super.isEnableForComponent(invoker);
	}

}
