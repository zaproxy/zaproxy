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
package org.zaproxy.zap.extension.httppanel;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import org.apache.commons.configuration.FileConfiguration;
import org.apache.log4j.Logger;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.extension.Extension;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.component.HttpPanelComponentInterface;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelDefaultViewSelector;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelView;
import org.zaproxy.zap.extension.search.SearchMatch;
import org.zaproxy.zap.extension.search.SearchableHttpPanelComponent;
import org.zaproxy.zap.extension.tab.Tab;

/**
*
* Panel to display HTTP request/response with header and body.
* 
* This creates:
* +---------------------+
* | panelHeader         |
* +---------------------+
* |                     |
* | panelContent        |
* |                     |
* +---------------------+
* 
* 
*/
abstract public class HttpPanel extends AbstractPanel implements Tab, ActionListener {
	private static final long serialVersionUID = 1L;
	
	private static Logger log = Logger.getLogger(HttpPanel.class);

	private JPanel panelHeader;
	private JPanel panelContent;

	private Extension extension = null;
	protected boolean isEditable = false;
	protected boolean isEnableViewSelect = false;
	protected HttpMessage httpMessage;
	
	private String baseConfigurationKey;
	private String componentsConfigurationKey;
	
	private static final String HTTP_PANEL_KEY = "httppanel.";
	private static final String COMPONENTS_KEY = "components.";
	private static final String DEFAULT_COMPONENT_KEY = "defaultcomponent";
	
	private Hashtable<String, HttpPanelComponentInterface> components = new Hashtable<String, HttpPanelComponentInterface>();
	private Hashtable<JToggleButton, String> componentsButtons = new Hashtable<JToggleButton, String>();
	
	private HttpPanelComponentInterface currentComponent;
	protected String savedSelectedComponentName;

	public enum OptionsLocation {BEGIN, AFTER_COMPONENTS, END};
	
	private JPanel allOptions;
	private JPanel componentOptions;
	private JToolBar toolBarComponents;
	private JToolBar toolBarMoreOptions;
	private JPanel endAllOptions;
	
	/*** Constructors ***/

	public HttpPanel(boolean isEditable, HttpMessage httpMessage, String configurationKey) {
		this.isEditable = isEditable;
		this.httpMessage = httpMessage;

		setConfigurationKey(configurationKey);
		
		initialize();
		initUi();
		initSpecial();
	}

	public HttpPanel(boolean isEditable, Extension extension, HttpMessage httpMessage, String configurationKey) {
		this.isEditable = isEditable;
		this.httpMessage = httpMessage;
		this.extension = extension;
		
		setConfigurationKey(configurationKey);
		
		initialize();
		initUi();
		initSpecial();
	}
	
	private void setConfigurationKey(String key) {
		baseConfigurationKey = key + HTTP_PANEL_KEY;
		componentsConfigurationKey = baseConfigurationKey + COMPONENTS_KEY;
	}
	
	abstract protected void initSpecial();
	
	private  void initialize() {
		this.setLayout(new BorderLayout());
		
		this.add(getPanelHeader(), BorderLayout.NORTH);
		this.add(getPanelContent(), BorderLayout.CENTER);
	}
	
	private void initUi() {

		allOptions = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
		componentOptions = new JPanel(new BorderLayout(0, 0));
		
		toolBarComponents = new JToolBar();
		toolBarComponents.setFloatable(false);
		toolBarComponents.setBorder(BorderFactory.createEmptyBorder());
		toolBarComponents.setRollover(true);
		
		toolBarComponents.addSeparator();
		toolBarComponents.addSeparator();
		
		toolBarMoreOptions = new JToolBar();
		toolBarMoreOptions.setFloatable(false);
		toolBarMoreOptions.setBorder(BorderFactory.createEmptyBorder());
		toolBarMoreOptions.setRollover(true);
		
		endAllOptions = new JPanel();
		
		JPanel panel1 = new JPanel(new BorderLayout(0, 0));
		
		JPanel panelFlow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
		panelFlow.add(allOptions);
		panelFlow.add(componentOptions);
		panelFlow.add(toolBarComponents);
		panelFlow.add(toolBarMoreOptions);
		panel1.add(panelFlow, BorderLayout.WEST);
		
		panelFlow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		panelFlow.add(endAllOptions);
		
		panel1.add(panelFlow, BorderLayout.EAST);
		
		panelHeader.add(panel1, BorderLayout.NORTH);
		
		initComponents();
		
		if (!components.isEmpty()) {
			String componentName;
			componentName = components.values().iterator().next().getName();
			switchComponent(componentName);
		}
	}

	
	/**
	 * This method initializes the content panel
	 */    
	protected JPanel getPanelContent() {
		if (panelContent == null) {
			panelContent = new JPanel(new CardLayout());
		}
		
		return panelContent;
	}
	

	/**
	 * This method initializes the header, aka toolbar
	 */    
	protected JPanel getPanelHeader() {
		if (panelHeader == null) {
			panelHeader = new JPanel(new BorderLayout());
		}

		return panelHeader;
	}
	
	/* Set new HttpMessage
	 * Update UI accordingly.
	 */
	public void setMessage(HttpMessage msg) {
		if (msg == null) {
			return;
		}
		
		this.httpMessage = msg;
		
		updateContent();
	}
	
	public void setMessage(HttpMessage msg, boolean enableViewSelect) {
		setMessage(msg);
		setEnableViewSelect(enableViewSelect);
	}

	/* Get HttpMessage
	 * External code needs to modify or view saved HttpMessage
	 * save data first so it's current
	 */
	public HttpMessage getHttpMessage() {
//		saveData();
		return httpMessage;
	}

	// Obsolete?
	public void setExtension(Extension extension) {
		this.extension = extension;
	}

	public Extension getExtension() {
		return extension;
	}

	public boolean isEditable() {
		return isEditable;
	}
	
	public void setEditable(boolean editable) {
		if (isEditable != editable) {
			isEditable = editable;

			synchronized (components) {
				Iterator<HttpPanelComponentInterface> it = components.values().iterator();
				while (it.hasNext()) {
					it.next().setEditable(editable);
				}
			}
		}
	}

	abstract protected void initComponents();
	
	// Button listener
	@Override
	public void actionPerformed(ActionEvent e) {
		if (isEditable) {
			saveData();
		}
		
		String name = componentsButtons.get(e.getSource());
		switchComponent(name);
	}
	
	// New HttpMessage was set
	public void updateContent() {
		if (currentComponent == null) {
			log.error("Could not find current component.");
			return;
		}
		currentComponent.setHttpMessage(httpMessage);
	}
	
	public void saveData() {
		if (httpMessage == null || currentComponent == null) {
			return;
		}
		
		currentComponent.save();
	}
	
	private void switchComponent(String name) {
		if (this.currentComponent != null && currentComponent.getName().equals(name)) {
			currentComponent.setSelected(true);
			return ;
		}
		
		HttpPanelComponentInterface newComponent = components.get(name);
		
		if (newComponent == null) {
			log.error("HttpPanel: could not find component with name: " + name);
			return;
		}
		
		if (this.currentComponent != null) {
			currentComponent.setSelected(false);
			currentComponent.clearView();
			
			componentOptions.remove(0);
		}
		
		this.currentComponent = newComponent;
		
		updateContent();
		
		((CardLayout)getPanelContent().getLayout()).show(panelContent, name);
		
		componentOptions.add(currentComponent.getOptionsPanel());
		componentOptions.validate();
		
		currentComponent.setSelected(true);
	}

	public void addOptions(Component comp, OptionsLocation location) {
		
		switch (location) {
		case BEGIN:
			allOptions.add(comp);
			break;
		case AFTER_COMPONENTS:
			toolBarMoreOptions.add(comp);
			break;
		case END:
			endAllOptions.add(comp);
			break;
		default:
			break;
		}
	}
	
	public void addOptionsSeparator() {
		toolBarMoreOptions.addSeparator();
	}
	
	protected void addComponent(HttpPanelComponentInterface component) {
		synchronized (components) {
			final String name = component.getName();
			final JToggleButton button = component.getButton();
			
			components.put(name, component);
			componentsButtons.put(button, name);
			button.addActionListener(this);
			
			toolBarComponents.add(component.getButton(), toolBarComponents.getComponentCount()-1);
			
			panelContent.add(component.getMainPanel(), name);
			
			if (savedSelectedComponentName != null) {
				if (savedSelectedComponentName.equals(component.getName())) {
					switchComponent(component.getName());
					savedSelectedComponentName = null;
				}
			}
			
			if (currentComponent == null) {
				switchComponent(component.getName());
			}
		}
		
		component.setEditable(isEditable);
		component.setEnableViewSelect(isEnableViewSelect);
		component.getButton().setEnabled(isEnableViewSelect);
	}
	
	public void addComponent(HttpPanelComponentInterface component, FileConfiguration fileConfiguration) {
		addComponent(component);
		
		component.setParentConfigurationKey(componentsConfigurationKey);
		component.loadConfig(fileConfiguration);
	}
	
	public void removeComponent(String componentName) {
		//synchronized (components) {
			//TODO remove component
		//}
	}
	
	public void addView(String componentName, HttpPanelView view, Object options, FileConfiguration fileConfiguration) {
		synchronized (components) {
			HttpPanelComponentInterface component = components.get(componentName);
			if (component != null) {
				component.addView(view, options, fileConfiguration);
			}
		}
	}
	
	public void removeView(String componentName, String viewName, Object options) {
		synchronized (components) {
			HttpPanelComponentInterface component = components.get(componentName);
			if (component != null) {
				component.removeView(viewName, options);
			}
		}
	}
	
	public void addDefaultViewSelector(String componentName, HttpPanelDefaultViewSelector defaultViewSelector, Object options) {
		synchronized (components) {
			HttpPanelComponentInterface component = components.get(componentName);
			if (component != null) {
				component.addDefaultViewSelector(defaultViewSelector, options);
			}
		}
	}
	
	public void removeDefaultViewSelector(String componentName, String defaultViewSelectorName, Object options) {
		synchronized (components) {
			HttpPanelComponentInterface component = components.get(componentName);
			if (component != null) {
				component.removeDefaultViewSelector(defaultViewSelectorName, options);
			}
		}
	}
	
	public void loadConfig(FileConfiguration fileConfiguration) {
		savedSelectedComponentName = fileConfiguration.getString(baseConfigurationKey + DEFAULT_COMPONENT_KEY);
		
		synchronized (components) {
			Iterator<HttpPanelComponentInterface> it = components.values().iterator();
			while (it.hasNext()) {
				it.next().loadConfig(fileConfiguration);
			}
		}
	}
	
	public void saveConfig(FileConfiguration fileConfiguration) {
		if (currentComponent != null) {
			fileConfiguration.setProperty(baseConfigurationKey + DEFAULT_COMPONENT_KEY, currentComponent.getName());
		}
		
		synchronized (components) {
			Iterator<HttpPanelComponentInterface> it = components.values().iterator();
			while (it.hasNext()) {
				it.next().saveConfig(fileConfiguration);
			}
		}
	}
	
	public void clearView() {
		httpMessage = null;
		
		currentComponent.clearView();
	}
	
	public void clearView(boolean enableViewSelect) {
		clearView();
		
		setEnableViewSelect(enableViewSelect);
	}
	
	public void setEnableViewSelect(boolean enableViewSelect) {
		if (isEnableViewSelect != enableViewSelect) {
			isEnableViewSelect = enableViewSelect;
			
			synchronized (components) {
				Iterator<HttpPanelComponentInterface> it = components.values().iterator();
				while (it.hasNext()) {
					HttpPanelComponentInterface component = it.next();
					component.setEnableViewSelect(enableViewSelect);
					component.getButton().setEnabled(enableViewSelect);
				}
			}
		}
	}
	
	/*** Search Functions - for SearchPanel and SearchResult 
	 * We'll only use the Text card for finding and displaying search results. 
	 * highlight* and *Search belong together.
	 ***/
	
	public void highlightHeader(SearchMatch sm) {
		if (currentComponent instanceof SearchableHttpPanelComponent) {
			((SearchableHttpPanelComponent)currentComponent).highlightHeader(sm);
		} else {
			HttpPanelComponentInterface component = findSearchablePanel();
			if (component != null) {
				switchComponent(component.getName());
				((SearchableHttpPanelComponent)currentComponent).highlightHeader(sm);
			}
		}
	}

	public void highlightBody(SearchMatch sm) {
		if (currentComponent instanceof SearchableHttpPanelComponent) {
			((SearchableHttpPanelComponent)currentComponent).highlightBody(sm);
		} else {
			HttpPanelComponentInterface component = findSearchablePanel();
			if (component != null) {
				switchComponent(component.getName());
				((SearchableHttpPanelComponent)currentComponent).highlightBody(sm);
			}
		}
	}

	public void headerSearch(Pattern p, List<SearchMatch> matches) {
		if (currentComponent instanceof SearchableHttpPanelComponent) {
			((SearchableHttpPanelComponent)currentComponent).searchHeader(p, matches);
		} else {
			HttpPanelComponentInterface component = findSearchablePanel();
			if (component != null) {
				((SearchableHttpPanelComponent)component).searchHeader(p, matches);
			}
		}
	}

	public void bodySearch(Pattern p, List<SearchMatch> matches) {
		if (currentComponent instanceof SearchableHttpPanelComponent) {
			((SearchableHttpPanelComponent)currentComponent).searchBody(p, matches);
		} else {
			HttpPanelComponentInterface component = findSearchablePanel();
			if (component != null) {
				((SearchableHttpPanelComponent)component).searchBody(p, matches);
			}
		}
	}
	
	private HttpPanelComponentInterface findSearchablePanel() {
		HttpPanelComponentInterface searchableComponent = null;
		
		synchronized (components) {
			Iterator<HttpPanelComponentInterface> it = components.values().iterator();
			while (it.hasNext()) {
				HttpPanelComponentInterface component = it.next();
				if (component instanceof SearchableHttpPanelComponent) {
					searchableComponent = component;
					break;
				}
			}
		}
		
		return searchableComponent;
	}

}
