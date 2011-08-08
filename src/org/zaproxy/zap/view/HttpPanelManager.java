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

package org.zaproxy.zap.view;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.zaproxy.zap.extension.httppanel.HttpPanel;
import org.zaproxy.zap.extension.httppanel.HttpPanelView;

public class HttpPanelManager {
	private List<HttpPanel> panels = new ArrayList<HttpPanel> ();
	private List<HttpPanelView> views = new ArrayList<HttpPanelView> ();
	
	private static HttpPanelManager instance = null;
	private static Logger log = Logger.getLogger(HttpPanelManager.class);
	
	public static synchronized HttpPanelManager getInstance() {
		if (instance == null) {
			instance = new HttpPanelManager();
		}
		return instance;
	}
	
	public void addPanel (HttpPanel panel) {
	// Disabled because views are incorporated into httppanels
/*		synchronized (this) {
			this.panels.add(panel);
			for (HttpPanelView view : views) {
				try {
					panel.addView(view.getClass().newInstance());
				} catch (InstantiationException e) {
					log.error(e.getMessage(), e);
				} catch (IllegalAccessException e) {
					log.error(e.getMessage(), e);
				}
			}
			
		}
		*/
	}
	
	public void addPanelView (HttpPanelView view) {
		// Disabled because views are incorporated into httppanels
		/*
		synchronized (this) {
			this.views.add(view);
			for (HttpPanel panel : panels) {
				try {
					panel.addView(view.getClass().newInstance());
				} catch (InstantiationException e) {
					log.error(e.getMessage(), e);
				} catch (IllegalAccessException e) {
					log.error(e.getMessage(), e);
				}
			}
		}*/
	}
}
