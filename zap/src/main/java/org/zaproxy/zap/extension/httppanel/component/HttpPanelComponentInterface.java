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
package org.zaproxy.zap.extension.httppanel.component;

import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.apache.commons.configuration.FileConfiguration;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelDefaultViewSelector;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelView;

public interface HttpPanelComponentInterface {
	
	/**
	 * 
	 * @return 
	 */
	// Name of the component for internal reference.
	public String getName();
	
	// Component has to provide the button which is displayed in the HttpPanel to select this view
	public JToggleButton getButton();
	
	// Component needs to provide a panel with main content which is displayed in HttpPanel
	public JPanel getMainPanel();
	
	// Component can provide an additional panel which is displayed in the HttpPanel header when this view is selected
	public JPanel getOptionsPanel();
	
	// Set a new HttpMessage for this Component
	// For example, the user selects a new message in the history tab. 
	// The component should update it's models accordingly.
	public void setHttpMessage(HttpMessage httpMessage);
	
	// The component is requested to save data from the UI into the current HttpMessage.
	// For example, the user selects a new message in the history tab. Or in break mode, want to send the modified message. 
	public void save();
	
	public void addView(HttpPanelView view, Object options, FileConfiguration fileConfiguration);
	
	public void removeView(String viewName, Object options);
	
	public void clearView();
	
	public void clearView(boolean enableViewSelect);
	
	public void setEnableViewSelect(boolean enableViewSelect);
	
	public void addDefaultViewSelector(HttpPanelDefaultViewSelector defaultViewSelector, Object options);
	
	public void removeDefaultViewSelector(String defaultViewSelectorName, Object options);
	
	public void setParentConfigurationKey(String configurationKey);
	
	public void loadConfig(FileConfiguration fileConfiguration);
	
	public void saveConfig(FileConfiguration fileConfiguration);
	
	public void setEditable(boolean editable);
	
	// Used to inform the view if it was selected/unselected
	public void setSelected(boolean selected);


}
