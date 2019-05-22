/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2013 The ZAP Development Team
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

import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

/**
 * A panel that can be used to show a summary of an entity and a button to configure the entity.
 */
public class SummaryAndConfigPanel extends JPanel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 622131593103359244L;

	/** The summary area. */
	private JEditorPane summaryArea;

	/** The summary title text. */
	private String summaryTitleText;

	private JButton configButton;

	/**
	 * Instantiates a new summary and config panel.
	 * 
	 * @param summaryTitleText the summary title text
	 * @param configButtonText the config button's text
	 * @param configButtonActionListener the config button action listener
	 */
	public SummaryAndConfigPanel(String summaryTitleText, String configButtonText,
			ActionListener configButtonActionListener) {

		super(new GridBagLayout());

		this.summaryTitleText = summaryTitleText;

		configButton = new JButton(configButtonText);
		configButton.setIcon(new ImageIcon(SummaryAndConfigPanel.class
				.getResource("/resource/icon/16/041.png")));
		configButton.addActionListener(configButtonActionListener);
		this.add(configButton, LayoutHelper.getGBC(1, 0, 1, 1, 0.0D, 0.0D));

		summaryArea = new JEditorPane();
		summaryArea.setContentType("text/html");
		summaryArea.setEnabled(false);
		setSummaryContent("");
		this.add(summaryArea, LayoutHelper.getGBC(0, 0, 1, 2, 1.0D, new Insets(3, 3, 3, 3)));
	}

	/**
	 * Sets the summary content.
	 * 
	 * @param content the new summary content
	 */
	public void setSummaryContent(String content) {
		Logger.getRootLogger().info("New summary: "+content);
		summaryArea.setText("<html><b>" + summaryTitleText + "</b><br/><br/>" + content + "</html>");
	}

	/**
	 * Sets whether the config button is enabled.
	 *
	 * @param enabled true, if enabled
	 */
	public void setConfigButtonEnabled(boolean enabled) {
		this.configButton.setEnabled(enabled);
	}
}
