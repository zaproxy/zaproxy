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
package org.zaproxy.zap.extension.httppanelcomp.all.request;

import java.awt.BorderLayout;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.apache.commons.configuration.FileConfiguration;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.component.HttpPanelComponentInterface;
import org.zaproxy.zap.extension.httppanel.component.HttpPanelComponentViewsManager;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelDefaultViewSelector;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelView;
import org.zaproxy.zap.extension.httppanel.view.models.request.RequestStringHttpPanelViewModel;
import org.zaproxy.zap.extension.search.SearchMatch;
import org.zaproxy.zap.extension.search.SearchableHttpPanelComponent;

public class RequestAllComponent implements HttpPanelComponentInterface, SearchableHttpPanelComponent {
	
	public static final String NAME = "RequestAll";

	private static final String BUTTON_TOOL_TIP = Constant.messages.getString("http.panel.component.all.tooltip");
	
	protected JToggleButton buttonShowView;
	
	protected JPanel panelOptions;
	protected JPanel panelMain;

	protected HttpMessage httpMessage;
	
	protected HttpPanelComponentViewsManager views;

	public RequestAllComponent() {
		this.httpMessage = null;
		
		views = new HttpPanelComponentViewsManager("all");
		
		initUi();
	}
	
	protected void initUi() {
		// Common
		buttonShowView = new JToggleButton(new ImageIcon(RequestAllComponent.class.getResource("/resource/icon/view_all.png")));
		buttonShowView.setToolTipText(BUTTON_TOOL_TIP);

		panelOptions = new JPanel();
		panelOptions.add(views.getComboBox());
		
		initViews();

		// All
		panelMain = new JPanel(new BorderLayout());
		panelMain.add(views.getViewsPanel());

		setSelected(false);
	}
	
	@Override
	public void setParentConfigurationKey(String configurationKey) {
		views.setConfigurationKey(configurationKey);
	}
	
	@Override
	public JToggleButton getButton() {
		return buttonShowView;
	}

	@Override
	public JPanel getOptionsPanel() {
		return panelOptions;
	}

	@Override
	public JPanel getMainPanel() {
		return panelMain;
	}
	
	@Override
	public void setSelected(boolean selected) {
		buttonShowView.setSelected(selected);

		views.setSelected(selected);
	}
	
	protected void initViews() {
		views.addView(new HttpRequestAllPanelTextView(new RequestStringHttpPanelViewModel()));
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void setHttpMessage(HttpMessage httpMessage) {
		this.httpMessage = httpMessage;
		
		views.setHttpMessage(httpMessage);
	}
	
	@Override
	public void save() {
		if (httpMessage == null) {
			return;
		}
		
		views.save();
	}
	
	@Override
	public void addView(HttpPanelView view, Object options, FileConfiguration fileConfiguration) {
		views.addView(view, fileConfiguration);
	}

	@Override
	public void removeView(String viewName, Object options) {
		views.removeView(viewName);
	}
	
	@Override
	public void clearView() {
		views.clearView();
	}

	@Override
	public void clearView(boolean enableViewSelect) {
		views.clearView();
		
		setEnableViewSelect(enableViewSelect);
	}
	
	@Override
	public void setEnableViewSelect(boolean enableViewSelect) {
		views.setEnableViewSelect(enableViewSelect);
	}

	@Override
	public void addDefaultViewSelector(HttpPanelDefaultViewSelector defaultViewSelector, Object options) {
		views.addDefaultViewSelector(defaultViewSelector);
	}

	@Override
	public void removeDefaultViewSelector(String defaultViewSelectorName, Object options) {
		views.removeDefaultViewSelector(defaultViewSelectorName);
	}

	@Override
	public void loadConfig(FileConfiguration fileConfiguration) {
		views.loadConfig(fileConfiguration);
	}

	@Override
	public void saveConfig(FileConfiguration fileConfiguration) {
		views.saveConfig(fileConfiguration);
	}

	@Override
	public void setEditable(boolean editable) {
		views.setEditable(editable);
	}

	@Override
	public void highlightHeader(SearchMatch sm) {
		views.highlight(sm);
	}

	@Override
	public void highlightBody(SearchMatch sm) {
		views.highlight(sm);
	}

	@Override
	public void searchHeader(Pattern p, List<SearchMatch> matches) {
		views.search(p, matches);
	}

	@Override
	public void searchBody(Pattern p, List<SearchMatch> matches) {
		views.search(p, matches);
	}
}
