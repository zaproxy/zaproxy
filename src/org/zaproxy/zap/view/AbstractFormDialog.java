/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2012 The ZAP Development Team
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

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.parosproxy.paros.Constant;

public abstract class AbstractFormDialog extends JDialog {

	private static final long serialVersionUID = -3345423228612477780L;

	private static final String CANCEL_BUTTON_LABEL = Constant.messages
			.getString("form.dialog.button.cancel");

	private JButton confirmButton;
	private JButton cancelButton;

	private boolean firstTime;

	public AbstractFormDialog(Dialog owner, String title) {
		super(owner, title, true);

		firstTime = true;
		initView();
	}

	public AbstractFormDialog(Dialog owner, String title, boolean initView) {
		super(owner, title, true);

		firstTime = true;
		if (initView)
			initView();
	}

	protected void initView() {
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.LINE_AXIS));
		buttonsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		buttonsPanel.add(Box.createHorizontalGlue());
		buttonsPanel.add(getCancelButton());
		buttonsPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		buttonsPanel.add(getConfirmButton());

		JPanel panel = new JPanel(new BorderLayout());

		panel.add(getFieldsPanel(), BorderLayout.CENTER);
		panel.add(buttonsPanel, BorderLayout.PAGE_END);

		this.setContentPane(panel);
	}

	private JButton getConfirmButton() {
		if (confirmButton == null) {
			confirmButton = new JButton(getConfirmButtonLabel());
			confirmButton.setEnabled(false);

			confirmButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (validateFields()) {
						performAction();
						clearAndHide();
					}
				}
			});
		}
		return confirmButton;
	}

	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton(getCancelButtonLabel());
			cancelButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					clearAndHide();
				}
			});
		}
		return cancelButton;
	}

	protected abstract JPanel getFieldsPanel();

	@Override
	public void setVisible(boolean b) {
		init();

		if (firstTime) {
			centreOnOwner();
			firstTime = false;
		}

		super.setVisible(b);
	}

	private void clearAndHide() {
		clearFields();
		dispose();
	}

	protected abstract String getConfirmButtonLabel();

	protected String getCancelButtonLabel() {
		return CANCEL_BUTTON_LABEL;
	}

	protected void setConfirmButtonEnabled(boolean enabled) {
		getConfirmButton().setEnabled(enabled);
	}

	protected void init() {
	}

	protected boolean validateFields() {
		return true;
	}

	protected void performAction() {
	}

	protected void clearFields() {
	}

	private void centreOnOwner() {
		Dimension frameSize = this.getSize();
		Rectangle mainrect = getOwner().getBounds();
		int x = mainrect.x + (mainrect.width - frameSize.width) / 2;
		int y = mainrect.y + (mainrect.height - frameSize.height) / 2;
		this.setLocation(x, y);
	}

}
