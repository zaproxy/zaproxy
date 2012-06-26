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
package org.zaproxy.zap.extension.httppanel.view.image;

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.commons.configuration.FileConfiguration;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelView;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelViewModelEvent;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelViewModelListener;

public class ResponseImageView implements HttpPanelView, HttpPanelViewModelListener {

	public static final String CONFIG_NAME = "image";
	
	public static final String CAPTION_NAME = Constant.messages.getString("http.panel.view.image.name");
	
	private JPanel mainPanel;
	private JLabel imageLabel;
	
	private HttpPanelViewModel model;
	
	public ResponseImageView(HttpPanelViewModel model) {
		this.model = model;
		
		imageLabel = new JLabel();
		imageLabel.setVerticalAlignment(JLabel.TOP);
		
		mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(new JScrollPane(imageLabel));
		
		this.model.addHttpPanelViewModelListener(this);
	}
	
	@Override
	public void save() {
	}

	@Override
	public void setSelected(boolean selected) {
		if (selected) {
			imageLabel.requestFocusInWindow();
		}
	}

	@Override
	public String getName() {
		return CAPTION_NAME;
	}
	
	@Override
	public int getPosition() {
		return 1;
	}

	@Override
	public String getConfigName() {
		return CONFIG_NAME;
	}

	@Override
	public boolean isEnabled(HttpMessage httpMessage) {
		return isImage(httpMessage);
	}

	@Override
	public boolean hasChanged() {
		return false;
	}

	@Override
	public JComponent getPane() {
		return mainPanel;
	}

	@Override
	public boolean isEditable() {
		return false;
	}

	@Override
	public void setEditable(boolean editable) {
	}

	private boolean isImage(final HttpMessage httpMessage) {
		if (httpMessage == null || httpMessage.getResponseBody() == null) {
			return false;
		}
		
		return httpMessage.getResponseHeader().isImage();
	}
	
	@Override
	public void setParentConfigurationKey(String configurationKey) {
	}
	
	@Override
	public void loadConfiguration(FileConfiguration fileConfiguration) {
	}
	
	@Override
	public void saveConfiguration(FileConfiguration fileConfiguration) {
	}

	@Override
	public HttpPanelViewModel getModel() {
		return model;
	}
	
	@Override
	public void dataChanged(HttpPanelViewModelEvent e) {
		HttpMessage httpMessage = model.getHttpMessage();
		
		if (isImage(httpMessage)) {
			imageLabel.setIcon(new ImageIcon(httpMessage.getResponseBody().getBytes()));
		} else {
			imageLabel.setIcon(null);
		}
	}
}
