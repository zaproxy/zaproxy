/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2012 psiinon@gmail.com
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
package org.zaproxy.zap.extension.autoupdate;

import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractDialog;
import org.parosproxy.paros.model.Model;
import org.zaproxy.zap.control.AddOn;
import org.zaproxy.zap.view.LayoutHelper;

public class AddOnsDialog extends AbstractDialog {

	private Logger logger = Logger.getLogger(AddOnsDialog.class);
	private static final long serialVersionUID = 1L;
	private JPanel jPanel = null;
	private JPanel addOnsPanel = null;

	private JButton updateButton = null;
	private JButton closeButton = null;
	
	private JLabel updatesMessage = null;

	private List<AddOn> uninstalledAddOns = null;
	// TODO flag new addons
	//private List<AddOn> newAddOns = null;
	private ExtensionAutoUpdate extension = null;
	private AddOnUpdatesTableModel model = null;
	
    /**
     * @throws HeadlessException
     */
    public AddOnsDialog(ExtensionAutoUpdate ext, List<AddOn> uninstalledAddOns, List<AddOn> newAddOns) throws HeadlessException {
        super();
        this.extension = ext;
        this.uninstalledAddOns = uninstalledAddOns;
        //this.newAddOns = newAddOns;
 		initialize();
    }

    /**
     * @param arg0
     * @param arg1
     * @throws HeadlessException
     */
    public AddOnsDialog(Frame arg0, boolean arg1) throws HeadlessException {
        super(arg0, arg1);
 		initialize();
    }

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        this.setContentPane(getJPanel());
        this.pack();
        if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
        	this.setSize(706, 400);
        }
	}
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new JPanel();
			this.setTitle(Constant.messages.getString("cfu.browse.title"));
			jPanel.setLayout(new GridBagLayout());
			jPanel.add(getAddOnsPanel(), LayoutHelper.getGBC(0, 1, 3, 1.0D, 1.0D));
		}
		return jPanel;
	}

	private JPanel getAddOnsPanel() {
		if (addOnsPanel == null) {

			addOnsPanel = new JPanel();
			addOnsPanel.setLayout(new GridBagLayout());
			addOnsPanel.setBorder(
					BorderFactory.createTitledBorder(
							null, Constant.messages.getString("cfu.label.addons.border"), TitledBorder.DEFAULT_JUSTIFICATION,
							javax.swing.border.TitledBorder.DEFAULT_POSITION,
							new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11),
							java.awt.Color.black));

			int row = 0;
			if (this.uninstalledAddOns == null || this.uninstalledAddOns.size() == 0) {
				addOnsPanel.add(new JLabel(Constant.messages.getString("cfu.browse.addons.none")), LayoutHelper.getGBC(0, row++, 3, 1.0D));
			} else {
				addOnsPanel.add(new JLabel(Constant.messages.getString("cfu.browse.addons.new")), LayoutHelper.getGBC(0, row++, 3, 1.0D));
				
				model = new AddOnUpdatesTableModel(false, this.uninstalledAddOns);
				JTable table = new JTable(model) {
					private static final long serialVersionUID = 1L;
					@Override
					public String getToolTipText(MouseEvent e) {
				        java.awt.Point p = e.getPoint();
				        int rowIndex = rowAtPoint(p);
				        AddOn ao = ((AddOnUpdatesTableModel)getModel()).getElement(rowIndex);
				        return ao.getName();
					}
				};
				
				JScrollPane scrollPane = new JScrollPane();
				scrollPane.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
				scrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
				scrollPane.setViewportView(table);

				addOnsPanel.add(scrollPane, LayoutHelper.getGBC(0, row++, 3, 1.0D, 1.0D));
				addOnsPanel.add(this.getUpdatesMessage(), LayoutHelper.getGBC(0, row++, 3, 0.0D));

				addOnsPanel.add(new JLabel(""), LayoutHelper.getGBC(0, row, 1, 1.0D));
				addOnsPanel.add(getCloseButton(), LayoutHelper.getGBC(1, row, 1, 0.0D));
				addOnsPanel.add(getUpdateButton(), LayoutHelper.getGBC(2, row, 1, 0.0D));

			}

		}
		return addOnsPanel;
	}
	
	public void setUpdatedAddOns(List<AddOn> updatedAddOns) {
		this.uninstalledAddOns = updatedAddOns;
		model.setAddOns(this.uninstalledAddOns);
	}

	private JLabel getUpdatesMessage() {
		if (this.updatesMessage == null) {
			this.updatesMessage = new JLabel("");
		}
		return this.updatesMessage;
	}

	private JButton getCloseButton() {
		if (closeButton == null) {
			closeButton = new JButton();
			closeButton.setText(Constant.messages.getString("all.button.close"));
			closeButton.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {    
				    AddOnsDialog.this.dispose();
				}
			});

		}
		return closeButton;
	}

	private void downloadUpdates() {
		for (AddOn aoi : this.uninstalledAddOns) {
			if (aoi.isEnabled() && aoi.getProgress() == 0) {
				extension.downloadFile(aoi.getUrl(), aoi.getFile(), aoi.getSize());
				aoi.setEnabled(false);
			}
		}
		if (extension.getCurrentDownloadCount() > 0) {
			this.getUpdatesMessage().setText(Constant.messages.getString("cfu.browse.downloading"));
		}
	}

	private JButton getUpdateButton() {
		if (updateButton == null) {
			updateButton = new JButton();
			updateButton.setText(Constant.messages.getString("cfu.button.addons.download"));
			updateButton.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					downloadUpdates();
				}
			});

		}
		return updateButton;
	}

	public void showProgress() {
		for (AddOn ao : this.uninstalledAddOns) {
			try {
				int progress = extension.getDownloadProgressPercent(ao.getUrl());
				if (progress > 0) {
					ao.setProgress(progress);
				}
			} catch (Exception e) {
				logger.debug("Error on " + ao.getUrl(), e);
				ao.setFailed(true);
			}
		}
		if (model != null) {
			model.fireTableDataChanged();
		}
		if (extension.getCurrentDownloadCount() == 0) {
			this.getUpdatesMessage().setText(Constant.messages.getString("cfu.browse.downloaded"));
		}

	}
}
