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
package org.zaproxy.zap.extension.log4j;

import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.view.ScanStatus;
import org.zaproxy.zap.view.ZapMenuItem;

/**
 * This class adds a count of the number of log4j errors encountered and outputs the details
 * to the Output tab.
 * It will only be enabled in a developer build.
 * @author Psiinon
 *
 */
public class ExtensionLog4j extends ExtensionAdaptor {

    private ZapMenuItem menuGarbageCollect = null;

	private ScanStatus scanStatus;
	
    public ExtensionLog4j() {
        super("ExtensionLog4j");
        this.setOrder(56);

		if (Constant.isDevBuild()) {
			// Only enable if this is a developer build, ie build from source
        
	        scanStatus = new ScanStatus(
					new ImageIcon(
							ExtensionLog4j.class.getResource("/resource/icon/fugue/bug.png")),
						Constant.messages.getString("log4j.icon.title"));
	
	        Logger.getRootLogger().addAppender(new ZapOutputWriter(scanStatus));
	
			if (View.isInitialised()) {
				View.getSingleton().getMainFrame().getMainFooterPanel().addFooterToolbarRightLabel(scanStatus.getCountLabel());
			}
		}
	}
	
	@Override
	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);

	    if (getView() != null) {	        
	        extensionHook.getHookMenu().addToolsMenuItem(getMenuGarbageCollect());
	    }

	}

	private ZapMenuItem getMenuGarbageCollect() {
        if (menuGarbageCollect == null) {
        	menuGarbageCollect = new ZapMenuItem("log4j.tools.menu.gc");

        	menuGarbageCollect.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                	Runtime.getRuntime().gc();
                }
            });
        }
        return menuGarbageCollect;
    }

	@Override
	public String getAuthor() {
		return Constant.ZAP_TEAM;
	}

	@Override
	public String getDescription() {
		return Constant.messages.getString("log4j.desc");
	}

	@Override
	public URL getURL() {
		try {
			return new URL(Constant.ZAP_HOMEPAGE);
		} catch (MalformedURLException e) {
			return null;
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
