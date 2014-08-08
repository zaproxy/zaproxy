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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import org.parosproxy.paros.Constant;
import org.zaproxy.zap.extension.anticsrf.AntiCsrfToken;

public class HttpFuzzerDialogTokenPane {
	private JScrollPane pane;
	private JPanel pane2;
	private JLabel sourceURL = new JLabel();
	private JLabel targetURL = new JLabel();
	private JLabel tokenName = new JLabel();
	private JLabel prevValue = new JLabel();
	private JCheckBox enableCheck = new JCheckBox();
	private AntiCsrfToken token;

	public HttpFuzzerDialogTokenPane(boolean enable, AntiCsrfToken token,
			String targetURL) {
		this(enable, token.getMsg().getRequestHeader().getURI().toString(),
				targetURL, token.getName(), token.getValue());
		this.token = token;
	}

	public HttpFuzzerDialogTokenPane() {
		this(false, null, null, null, null);
		this.enableCheck.setEnabled(false);
	}

	private HttpFuzzerDialogTokenPane(boolean enable, String sourceURL,
			String targetURL, String tokenName, String prevValue) {
		super();
		this.sourceURL.setText(sourceURL);
		this.targetURL.setText(targetURL);
		this.tokenName.setText(tokenName);
		this.prevValue.setText(prevValue);

		pane = new JScrollPane();
		String tmpName = null;
		pane.setBorder(javax.swing.BorderFactory.createTitledBorder(
				null,
				tmpName, // "TBI Anti CRSF Tokens", //
							// Constant.messages.getString("invoke.options.edit"),
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
				javax.swing.border.TitledBorder.DEFAULT_POSITION,
				new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11),
				java.awt.Color.black));
		pane.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
		pane.setMinimumSize(new Dimension(50, 120));

		pane2 = new JPanel();
		pane2.setLayout(new GridBagLayout());

		enableCheck.setSelected(enable);

		pane2.add(
				new JLabel(Constant.messages.getString("fuzz.acsrf.label.name")),
				getGBC(0, 1, 1, 0.25D));
		pane2.add(this.tokenName, getGBC(1, 1, 1, 0.75D));

		pane2.add(
				new JLabel(Constant.messages
						.getString("fuzz.acsrf.label.source")),
				getGBC(0, 2, 1, 0.25D));
		pane2.add(this.sourceURL, getGBC(1, 2, 1, 0.75D));

		pane2.add(
				new JLabel(Constant.messages
						.getString("fuzz.acsrf.label.target")),
				getGBC(0, 3, 1, 0.25D));
		pane2.add(this.targetURL, getGBC(1, 3, 1, 0.75D));

		pane2.add(
				new JLabel(Constant.messages.getString("fuzz.acsrf.label.prev")),
				getGBC(0, 4, 1, 0.25D));
		pane2.add(this.prevValue, getGBC(1, 4, 1, 0.75D));

		pane.setViewportView(pane2);
		pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
	}

	private GridBagConstraints getGBC(int x, int y, int width, double weightx) {
		return this.getGBC(x, y, width, weightx, 0.0);
	}

	private GridBagConstraints getGBC(int x, int y, int width, double weightx,
			double weighty) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.insets = new java.awt.Insets(1, 5, 1, 5);
		gbc.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gbc.fill = java.awt.GridBagConstraints.BOTH;
		gbc.weightx = weightx;
		gbc.weighty = weighty;
		gbc.gridwidth = width;
		return gbc;
	}

	public JComponent getPane() {
		return this.pane;
	}

	public void setAll(boolean enable, AntiCsrfToken token, String targetURL) {
		this.enableCheck.setSelected(enable);
		this.setPrevValue(token.getValue());
		this.setSourceURL(token.getMsg().getRequestHeader().getURI().toString());
		this.setTargetURL(targetURL);
		this.setTokenName(token.getName());
		this.token = token;
	}

	public void reset() {
		this.enableCheck.setSelected(false);
		this.setPrevValue(null);
		this.setSourceURL(null);
		this.setTargetURL(null);
		this.setTokenName(null);
		this.token = null;
	}

	public boolean isEnable() {
		return enableCheck.isSelected();
	}

	public String getSourceURL() {
		return sourceURL.getText();
	}

	public void setSourceURL(String sourceURL) {
		this.sourceURL.setText(sourceURL);
	}

	public String getTargetURL() {
		return targetURL.getText();
	}

	public void setTargetURL(String targetURL) {
		this.targetURL.setText(targetURL);
	}

	public String getTokenName() {
		return tokenName.getText();
	}

	public void setTokenName(String tokenName) {
		this.tokenName.setText(tokenName);
	}

	public String getPrevValue() {
		return prevValue.getText();
	}

	public void setPrevValue(String prevValue) {
		this.prevValue.setText(prevValue);
	}

	public AntiCsrfToken getToken() {
		return token;
	}

	public void setEnabled(boolean enabled) {
		pane.setEnabled(enabled);
		pane2.setEnabled(enabled);

	}

}
