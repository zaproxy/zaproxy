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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.zaproxy.zap.extension.httppanel.component.split.request.RequestSplitComponent;
import org.zaproxy.zap.view.HttpPanelManager;

public class HttpPanelRequest extends HttpPanel {
	private static final long serialVersionUID = 1L;
	
	protected JComboBox<String> comboChangeMethod;
	
	private static final String REQUEST_KEY = "request.";
	
	public HttpPanelRequest(boolean isEditable, String configurationKey) {
        super(isEditable, configurationKey + REQUEST_KEY);

        HttpPanelManager.getInstance().addRequestPanel(this);
        this.setHideable(false);
	}

	@Override
	protected void initComponents() {
		addComponent(new RequestSplitComponent<HttpMessage>(), Model.getSingleton().getOptionsParam().getConfig());
	}

	@Override
	protected void initSpecial() {
		if (isEditable()) {
			initComboChangeMethod();
		}
	}

	@Override
	public void setEnableViewSelect(boolean enableViewSelect) {
		super.setEnableViewSelect(enableViewSelect);
		
		if (isEditable()) {
			initComboChangeMethod();
			comboChangeMethod.setEnabled(enableViewSelect);
		}
	}
	
	protected void initComboChangeMethod() {
		if (comboChangeMethod == null) {
			comboChangeMethod = new JComboBox<>();
			comboChangeMethod.setEditable(false);
			comboChangeMethod.setMaximumRowCount(9);	// Prevents scrollbar
			comboChangeMethod.addItem(Constant.messages.getString("manReq.pullDown.method"));
			comboChangeMethod.addItem(HttpRequestHeader.CONNECT);
			comboChangeMethod.addItem(HttpRequestHeader.DELETE);
			comboChangeMethod.addItem(HttpRequestHeader.GET);
			comboChangeMethod.addItem(HttpRequestHeader.HEAD);
			comboChangeMethod.addItem(HttpRequestHeader.OPTIONS);
			comboChangeMethod.addItem(HttpRequestHeader.POST);
			comboChangeMethod.addItem(HttpRequestHeader.PUT);
			comboChangeMethod.addItem(HttpRequestHeader.TRACE);
			comboChangeMethod.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (message == null) {
						comboChangeMethod.setSelectedIndex(0);
						return;
					}
					if (comboChangeMethod.getSelectedIndex() > 0) {

                        if (message instanceof HttpMessage) {
    						saveData();
    						((HttpMessage)message).mutateHttpMethod((String) comboChangeMethod.getSelectedItem());
    						comboChangeMethod.setSelectedIndex(0);
    						updateContent();
						}
					}
				}});
	
			addOptions(comboChangeMethod, OptionsLocation.BEGIN);
			comboChangeMethod.setEnabled(false);
		}
	}
	
	@Override
	public void setEditable(boolean editable) {
		super.setEditable(editable);
		
		if (editable) {
			initComboChangeMethod();
			comboChangeMethod.setEnabled(true);
		}
	}

}