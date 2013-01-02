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
package org.zaproxy.zap.extension.websocket.ui;

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.zaproxy.zap.extension.websocket.ExtensionWebSocket;
import org.zaproxy.zap.extension.websocket.WebSocketException;
import org.zaproxy.zap.view.SingleColumnTableModel;

public class SessionExcludeFromWebSocket extends AbstractParamPanel {
	public static final String PANEL_NAME = Constant.messages.getString("websocket.session.exclude.title");
	private static final long serialVersionUID = -1000465438379563850L;

	private JPanel panelSession = null;
	private JTable tableIgnore = null;
	private JScrollPane jScrollPane = null;
	private SingleColumnTableModel model = null;
	
	private ExtensionWebSocket extWs;

	public SessionExcludeFromWebSocket(ExtensionWebSocket extWs) {
		super();
		this.extWs = extWs;
		initialize();
	}
	
	private void initialize() {
		setLayout(new CardLayout());
		setName(PANEL_NAME);
		add(getPanelSession(), getPanelSession().getName());
	}

	private JPanel getPanelSession() {
		if (panelSession == null) {
			panelSession = new JPanel();
			panelSession.setLayout(new GridBagLayout());
			panelSession.setName("Ignorewebsocket");

			java.awt.GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			java.awt.GridBagConstraints gridBagConstraints1 = new GridBagConstraints();

			javax.swing.JLabel jLabel = new JLabel();

			jLabel.setText(Constant.messages.getString("websocket.session.label.ignore"));
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.gridy = 0;
			gridBagConstraints1.gridheight = 1;
			gridBagConstraints1.insets = new java.awt.Insets(10, 0, 5, 0);
			gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints1.weightx = 0.0D;

			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.gridy = 1;
			gridBagConstraints2.weightx = 1.0;
			gridBagConstraints2.weighty = 1.0;
			gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints2.ipadx = 0;
			gridBagConstraints2.insets = new java.awt.Insets(0, 0, 0, 0);
			gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTHWEST;
			panelSession.add(jLabel, gridBagConstraints1);
			panelSession.add(getJScrollPane(), gridBagConstraints2);
		}
		return panelSession;
	}

	@Override
	public void initParam(Object obj) {
		getModel().setLines(extWs.getChannelIgnoreList());
	}

	@Override
	public void validateParam(Object obj) throws PatternSyntaxException {
		// Check for valid regexs
		for (String regex : getModel().getLines()) {
			if (regex.trim().length() > 0) {
				Pattern.compile(regex.trim(), Pattern.CASE_INSENSITIVE);
			}
		}
	}

	@Override
	public void saveParam(Object obj) throws WebSocketException {
		extWs.setChannelIgnoreList(getModel().getLines());
	}

	private JTable getTableIgnore() {
		if (tableIgnore == null) {
			tableIgnore = new JTable();
			tableIgnore.setModel(getModel());
			tableIgnore.setRowHeight(18);
		}
		return tableIgnore;
	}

	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getTableIgnore());
			jScrollPane.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
		}
		return jScrollPane;
	}

	private SingleColumnTableModel getModel() {
		if (model == null) {
			model = new SingleColumnTableModel(Constant.messages.getString("websocket.session.table.header.ignore"));
		}
		return model;
	}

	@Override
	public String getHelpIndex() {
		return "ui.dialogs.sessprop";
	}
}
