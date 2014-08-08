/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.extension.multiFuzz.impl.http;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.anticsrf.AntiCsrfToken;
import org.zaproxy.zap.extension.anticsrf.ExtensionAntiCSRF;
import org.zaproxy.zap.extension.multiFuzz.SubComponent;
import org.zaproxy.zap.extension.multiFuzz.Util;

public class AntiCSRFComponent implements SubComponent{
	
	private JCheckBox enableTokens;
	private JCheckBox showTokenRequests;
	private boolean incAcsrfToken = false;
	private HttpFuzzerDialogTokenPane tokenPane;
	
	public AntiCSRFComponent(HttpMessage msg){
		checkAntiCSRF(msg);
	}
	
	@Override
	public JComponent addOptions() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		int currentRow = 0;
		if (incAcsrfToken) { // Options for AcsrfTokens
			panel.add(
					new JLabel(Constant.messages
							.getString("fuzz.label.anticsrf")),
					Util.getGBC(0, currentRow, 6, 2.0D));
			currentRow++;
			panel.add(getEnableTokens(), Util.getGBC(0, currentRow, 6, 0.0D));
			currentRow++;
			panel.add(
					getTokensPane().getPane(),
					Util.getGBC(0, currentRow, 6, 1.0D, 0.0D,
							java.awt.GridBagConstraints.BOTH));
			currentRow++;

			panel.add(
					new JLabel(Constant.messages
							.getString("fuzz.label.showtokens")),
					Util.getGBC(0, currentRow, 2, 1.0D));
			panel.add(getShowTokenRequests(), Util.getGBC(2, currentRow, 4, 0.0D));
			currentRow++;
		}
		return panel;
	}

	private void checkAntiCSRF(HttpMessage fuzzableMessage) {
		ExtensionAntiCSRF extAntiCSRF = (ExtensionAntiCSRF) Control
				.getSingleton().getExtensionLoader()
				.getExtension(ExtensionAntiCSRF.NAME);
		List<AntiCsrfToken> tokens = null;
		if (extAntiCSRF != null) {
			tokens = extAntiCSRF.getTokens(fuzzableMessage);
		}
		if (tokens == null || tokens.size() == 0) {
			incAcsrfToken = false;
		} else {
			incAcsrfToken = true;
		}
		if (incAcsrfToken) {
			setAntiCsrfTokens(tokens);
		}
	}
	public boolean getShowTokens(){
		return getShowTokenRequests().isSelected();
	}
	public boolean getTokensEnabled(){
		return getEnableTokens().isSelected();
	}
	private HttpFuzzerDialogTokenPane getTokensPane() {
		if (tokenPane == null) {
			tokenPane = new HttpFuzzerDialogTokenPane();
		}
		return tokenPane;
	}
	private void setAntiCsrfTokens(List<AntiCsrfToken> acsrfTokens) {
		if (acsrfTokens != null && acsrfTokens.size() > 0) {
			getTokensPane().setAll(true, acsrfTokens.get(0),
					acsrfTokens.get(0).getTargetURL());
			this.getEnableTokens().setSelected(true);
			this.getEnableTokens().setEnabled(true);
			this.getTokensPane().getPane().setVisible(true);
		} else {
			getTokensPane().reset();
			this.getEnableTokens().setSelected(false);
			this.getEnableTokens().setEnabled(false);
			this.getTokensPane().getPane().setVisible(false);
		}
	}

	private JCheckBox getEnableTokens() {
		if (enableTokens == null) {
			enableTokens = new JCheckBox();
			enableTokens.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					getTokensPane().setEnabled(enableTokens.isSelected());
					getShowTokenRequests()
							.setEnabled(enableTokens.isSelected());
				}
			});
		}
		return enableTokens;
	}

	private JCheckBox getShowTokenRequests() {
		if (showTokenRequests == null) {
			showTokenRequests = new JCheckBox();
		}
		return showTokenRequests;
	}
}
