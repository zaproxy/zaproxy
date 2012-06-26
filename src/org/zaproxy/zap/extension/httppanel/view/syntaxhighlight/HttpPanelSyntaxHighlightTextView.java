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
package org.zaproxy.zap.extension.httppanel.view.syntaxhighlight;

import java.awt.BorderLayout;
import java.awt.event.InputEvent;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.commons.configuration.FileConfiguration;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelView;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelViewModelEvent;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelViewModelListener;
import org.zaproxy.zap.extension.httppanel.view.models.AbstractStringHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.text.HttpPanelTextView;
import org.zaproxy.zap.extension.search.SearchMatch;
import org.zaproxy.zap.extension.search.SearchableHttpPanelView;

public abstract class HttpPanelSyntaxHighlightTextView implements HttpPanelView, HttpPanelViewModelListener, SearchableHttpPanelView {

	public static final String CONFIG_NAME = HttpPanelTextView.CONFIG_NAME;
	
	private static final String CAPTION_NAME = Constant.messages.getString("http.panel.view.text.name");
	
	
	private HttpPanelSyntaxHighlightTextArea httpPanelTextArea;
	private JPanel mainPanel;
	
	private AbstractStringHttpPanelViewModel model;
	
	private String configurationKey;
	
	public HttpPanelSyntaxHighlightTextView(AbstractStringHttpPanelViewModel model) {
		this.model = model;
		this.configurationKey = "";
		
		init();
		
		this.model.addHttpPanelViewModelListener(this);
	}
	
	private void init() {
		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		
		httpPanelTextArea = createHttpPanelTextArea();
		httpPanelTextArea.setEditable(false);
		httpPanelTextArea.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseReleased(java.awt.event.MouseEvent e) {
				// right mouse button action
				if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0 || e.isPopupTrigger()) { 
					if (!httpPanelTextArea.isFocusOwner()) {
						httpPanelTextArea.requestFocusInWindow();
					}

					View.getSingleton().getPopupMenu().show(httpPanelTextArea, e.getX(), e.getY());
				}
			}
		});
		
		JScrollPane scrollPane = new RTextScrollPane(httpPanelTextArea, false);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		mainPanel.add(scrollPane, BorderLayout.CENTER);
	}
	
	/**
	 * Classes that what to extend the functionalities of a HttpPanelTextArea
	 * should override this method and return the appropriate extended HttpPanelTextArea
	 * 
	 * @return a HttpPanelTextArea
	 */
	protected abstract HttpPanelSyntaxHighlightTextArea createHttpPanelTextArea();
	
	@Override
	public void setSelected(boolean selected) {
		if (selected) {
			httpPanelTextArea.requestFocusInWindow();
		}
	}
	
	@Override
	public String getName() {
		return CAPTION_NAME;
	}

	@Override
	public String getConfigName() {
		return CONFIG_NAME;
	}
	
	@Override
	public int getPosition() {
		return Integer.MIN_VALUE;
	}

	@Override
	public boolean isEnabled(HttpMessage msg) {
		return true;
	}

	@Override
	public boolean hasChanged() {
		return true;
	}
	
	@Override
	public JComponent getPane() {
		return mainPanel;
	}

	@Override
	public boolean isEditable() {
		return httpPanelTextArea.isEditable();
	}

	@Override
	public void setEditable(boolean editable) {
		httpPanelTextArea.setEditable(editable);
	}
	
	@Override
	public HttpPanelViewModel getModel() {
		return model;
	}
	
	@Override
	public void dataChanged(HttpPanelViewModelEvent e) {
		httpPanelTextArea.setHttpMessage(model.getHttpMessage());
		
		final boolean isEditable = httpPanelTextArea.isEditable();
		final boolean empty = model.getData().isEmpty();
		
		if (empty || !isEditable) {
			httpPanelTextArea.discardAllEdits();
		}
		
		httpPanelTextArea.setText(model.getData());
		httpPanelTextArea.setCaretPosition(0);
		
		if (empty || !isEditable) {
			httpPanelTextArea.discardAllEdits();
		}
	}
	
	@Override
	public void save() {
		model.setData(httpPanelTextArea.getText());
	}
	
	@Override
	public void search(Pattern p, List<SearchMatch> matches) {
		httpPanelTextArea.search(p, matches);
	}

	@Override
	public void highlight(SearchMatch sm) {
		httpPanelTextArea.highlight(sm);
	}
	
	@Override
	public void setParentConfigurationKey(String configurationKey) {
		this.configurationKey = configurationKey + CONFIG_NAME + ".";
	}
	
	@Override
	public void loadConfiguration(FileConfiguration fileConfiguration) {
		httpPanelTextArea.loadConfiguration(configurationKey , fileConfiguration);
	}
	
	@Override
	public void saveConfiguration(FileConfiguration fileConfiguration) {
		httpPanelTextArea.saveConfiguration(configurationKey, fileConfiguration);
	}
}
