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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.configuration.FileConfiguration;
import org.parosproxy.paros.model.Model;
import org.zaproxy.zap.extension.httppanel.HttpPanel;
import org.zaproxy.zap.extension.httppanel.HttpPanelRequest;
import org.zaproxy.zap.extension.httppanel.HttpPanelResponse;
import org.zaproxy.zap.extension.httppanel.component.HttpPanelComponentInterface;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelDefaultViewSelector;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelView;

public class HttpPanelManager {
	//private static Logger log = Logger.getLogger(HttpPanelManager.class);
	
	private static HttpPanelManager instance = null;
	
	HttpPanelManagement requestPanels;
	HttpPanelManagement responsePanels;
	
	private HttpPanelManager() {
		requestPanels = new HttpPanelManagement();
		responsePanels = new HttpPanelManagement();
	}
	
	public static HttpPanelManager getInstance() {
		if (instance == null) {
			createInstance();
		}
		return instance;
	}
	
	private static synchronized void createInstance() {
		if (instance == null) {
			instance = new HttpPanelManager();
		}
	}
	
	public void addRequestPanel (HttpPanelRequest panel) {
		requestPanels.addPanel(panel);
	}
	
	public void addRequestComponent (HttpPanelComponentFactory componentFactory) {
		requestPanels.addComponent(componentFactory);
	}

	public void addRequestView (String componentName, HttpPanelViewFactory viewFactory) {
		requestPanels.addView(componentName, viewFactory);
	}
	
	public void addRequestDefaultView(String componentName, HttpPanelDefaultViewSelectorFactory defaultViewSelectorFactory) {
		requestPanels.addDefaultView(componentName, defaultViewSelectorFactory);
	}
	
	public void addResponsePanel (HttpPanelResponse panel) {
		responsePanels.addPanel(panel);
	}
	
	public void addResponseComponent (HttpPanelComponentFactory componentFactory) {
		responsePanels.addComponent(componentFactory);
	}

	public void addResponseView (String componentName, HttpPanelViewFactory viewFactory) {
		responsePanels.addView(componentName, viewFactory);
	}
	
	public void addResponseDefaultView(String componentName, HttpPanelDefaultViewSelectorFactory defaultViewSelectorFactory) {
		responsePanels.addDefaultView(componentName, defaultViewSelectorFactory);
	}
	
	private static final class HttpPanelManagement {

		private List<HttpPanel> panels;
		private List<HttpPanelComponentFactory> components;
		private Map<String, List<HttpPanelViewFactory>> views;
		private Map<String, List<HttpPanelDefaultViewSelectorFactory>> defaultViews;
		
		public HttpPanelManagement() {
			panels = new ArrayList<HttpPanel> ();
			components = new ArrayList<HttpPanelComponentFactory> ();
			views = new HashMap<String, List<HttpPanelViewFactory>> ();
			defaultViews = new HashMap<String, List<HttpPanelDefaultViewSelectorFactory>> ();
		}
		
		public void addPanel (HttpPanel panel) {
			synchronized (this) {
				this.panels.add(panel);
				
				FileConfiguration fileConfiguration = Model.getSingleton().getOptionsParam().getConfig();
				
				for (HttpPanelComponentFactory componentFactory : components) {
					panel.addComponent(componentFactory.getNewComponent(), fileConfiguration);
				}
				
				Iterator<Entry<String, List<HttpPanelViewFactory>>> itComponentViews = views.entrySet().iterator();
				
				while (itComponentViews.hasNext()) {
					Map.Entry<String, List<HttpPanelViewFactory>> entry = itComponentViews.next();
					
					Iterator<HttpPanelViewFactory> itViews = entry.getValue().iterator();
					while (itViews.hasNext()) {
						HttpPanelViewFactory viewFactory = itViews.next();
						panel.addView(entry.getKey(), viewFactory.getNewView(), viewFactory.getOptions(), fileConfiguration);
					}
				}
				
				Iterator<Entry<String, List<HttpPanelDefaultViewSelectorFactory>>> itComponentDefaultViews = defaultViews.entrySet().iterator();
				
				while (itComponentDefaultViews.hasNext()) {
					Map.Entry<String, List<HttpPanelDefaultViewSelectorFactory>> entry = itComponentDefaultViews.next();
					
					Iterator<HttpPanelDefaultViewSelectorFactory> itDefaultViews = entry.getValue().iterator();
					while (itDefaultViews.hasNext()) {
						HttpPanelDefaultViewSelectorFactory viewFactory = itDefaultViews.next();
						panel.addDefaultViewSelector(entry.getKey(), viewFactory.getNewDefaultViewSelector(), viewFactory.getOptions());
					}
				}
			}
		}
		
		public void addComponent (HttpPanelComponentFactory componentFactory) {
			synchronized (this) {
				this.components.add(componentFactory);
				
				FileConfiguration fileConfiguration = Model.getSingleton().getOptionsParam().getConfig();
				
				for (HttpPanel panel : panels) {
					panel.addComponent(componentFactory.getNewComponent(), fileConfiguration);
					
					final String componentName = componentFactory.getComponentName();

					List<HttpPanelViewFactory> componentViews = views.get(componentName);
					if (componentViews != null) {
						Iterator<HttpPanelViewFactory> it = componentViews.iterator();
						while (it.hasNext()) {
							HttpPanelViewFactory viewFactory = it.next();
							panel.addView(componentName, viewFactory.getNewView(), viewFactory.getOptions(), fileConfiguration);
						}
					}

					List<HttpPanelDefaultViewSelectorFactory> defaultViewsComp = defaultViews.get(componentName);
					if (defaultViewsComp != null) {
						Iterator<HttpPanelDefaultViewSelectorFactory> it = defaultViewsComp.iterator();
						while (it.hasNext()) {
							HttpPanelDefaultViewSelectorFactory defaultViewSelector = it.next();
							panel.addDefaultViewSelector(componentName, defaultViewSelector.getNewDefaultViewSelector(), defaultViewSelector.getOptions());
						}
					}
				}
			}
		}

		public void addView (String componentName, HttpPanelViewFactory viewFactory) {
			synchronized (this) {
				
				List<HttpPanelViewFactory> componentViews = this.views.get(componentName);
				if (componentViews == null) {
					componentViews = new ArrayList<HttpPanelViewFactory> ();
					this.views.put(componentName, componentViews);
				}
				
				componentViews.add(viewFactory);
				
				FileConfiguration fileConfiguration = Model.getSingleton().getOptionsParam().getConfig();
				
				for (HttpPanel panel : panels) {
					panel.addView(componentName, viewFactory.getNewView(), viewFactory.getOptions(), fileConfiguration);
				}
			}
		}
		
		public void addDefaultView(String componentName, HttpPanelDefaultViewSelectorFactory defaultViewSelectorFactory) {
			synchronized (this) {
				
				List<HttpPanelDefaultViewSelectorFactory> defaultViews = this.defaultViews.get(componentName);
				if (defaultViews == null) {
					defaultViews = new ArrayList<HttpPanelDefaultViewSelectorFactory> ();
					this.defaultViews.put(componentName, defaultViews);
				}
				
				defaultViews.add(defaultViewSelectorFactory);
					
				for (HttpPanel panel : panels) {
					panel.addDefaultViewSelector(componentName, defaultViewSelectorFactory.getNewDefaultViewSelector(), defaultViewSelectorFactory.getOptions());
				}
			}
		}
		
	}
	
	public interface HttpPanelComponentFactory {
		public String getComponentName();
		public HttpPanelComponentInterface getNewComponent();
	}
	
	public interface HttpPanelViewFactory {
		public HttpPanelView getNewView();
		public Object getOptions();
	}
	
	public interface HttpPanelDefaultViewSelectorFactory {
		public HttpPanelDefaultViewSelector getNewDefaultViewSelector();
		public Object getOptions();
	}

}
