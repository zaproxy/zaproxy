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

import java.awt.Desktop;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractDialog;
import org.parosproxy.paros.extension.history.LogPanelCellRenderer;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.control.AddOn;
import org.zaproxy.zap.control.ZapRelease;
import org.zaproxy.zap.view.LayoutHelper;

public class UpdateDialog extends AbstractDialog {

	protected enum State {IDLE, DOWNLOADING_ZAP, DOWNLOADED_ZAP, DOWNLOADING_UPDATES, DOWNLOADED_UPDATES}
	private Logger logger = Logger.getLogger(UpdateDialog.class);
	private static final long serialVersionUID = 1L;
	private JPanel jPanel = null;
	private JPanel corePanel = null;
	private JPanel addOnsPanel = null;

	private JButton coreNotesButton = null;
	private JButton downloadZapButton = null;
	private JButton updateButton = null;
	private JButton closeButton = null;
	
	private JLabel downloadProgress = null;
	private JLabel updatesMessage = null;
	
	private JTable updatesTable = null;

	private ZapRelease latestRelease = null;
	private List<AddOn> updatedAddOns = null;
	private ExtensionAutoUpdate extension = null;
	private AddOnUpdatesTableModel model = null;
	
	private State state = null;
	
    /**
     * @throws HeadlessException
     */
    public UpdateDialog(ExtensionAutoUpdate ext, ZapRelease latestRelease, List<AddOn> updatedAddOns) throws HeadlessException {
        super();
        this.extension = ext;
        this.latestRelease = latestRelease;
        this.updatedAddOns = updatedAddOns;

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
        	this.setSize(606, 300);
        }
        state = State.IDLE;
	}
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new JPanel();
			this.setTitle(Constant.messages.getString("cfu.title"));
			jPanel.setLayout(new GridBagLayout());
			jPanel.add(getCorePanel(), LayoutHelper.getGBC(0, 0, 1, 1.0D, 0.0D));
			jPanel.add(getAddOnsPanel(), LayoutHelper.getGBC(0, 1, 1, 1.0D, 1.0D));
			jPanel.add(this.getUpdatesMessage(), LayoutHelper.getGBC(0, 2, 1, 1.0D));

		}
		return jPanel;
	}

	private JPanel getCorePanel() {
		if (corePanel == null) {
			corePanel = new JPanel();
			corePanel.setLayout(new GridBagLayout());
			corePanel.setBorder(
					BorderFactory.createTitledBorder(
							null, Constant.messages.getString("cfu.label.zap.border"), TitledBorder.DEFAULT_JUSTIFICATION,
							javax.swing.border.TitledBorder.DEFAULT_POSITION,
							new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11),
							java.awt.Color.black));

			if (latestRelease == null) {
				corePanel.add(new JLabel(Constant.messages.getString("cfu.check.zap.latest")), LayoutHelper.getGBC(0, 0, 1, 1.0D));
			} else {
				corePanel.add(new JLabel(Constant.messages.getString("cfu.check.zap.newer")), LayoutHelper.getGBC(0, 0, 1, 0.0D));
				corePanel.add(new JLabel(this.latestRelease.getVersion()), LayoutHelper.getGBC(1, 0, 1, 0.1D));
				corePanel.add(new JLabel(""), LayoutHelper.getGBC(2, 0, 1, 0.8D));
				corePanel.add(this.getDownloadProgress(), LayoutHelper.getGBC(3, 0, 1, 0.2D));
				corePanel.add(this.getCoreNotesButton(), LayoutHelper.getGBC(4, 0, 1, 0.0D));
				corePanel.add(this.getDownloadZapButton(), LayoutHelper.getGBC(5, 0, 1, 0.0D));
			}
			
		}
		return corePanel;
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
			if (this.updatedAddOns == null || this.updatedAddOns.size() == 0) {
				addOnsPanel.add(new JLabel(Constant.messages.getString("cfu.check.addons.latest")), LayoutHelper.getGBC(0, row++, 3, 1.0D));
			} else {
				addOnsPanel.add(new JLabel(Constant.messages.getString("cfu.check.addons.newer")), LayoutHelper.getGBC(0, row++, 3, 1.0D));
				
				// Default to selected for all that havnt been downloaded already
				for (AddOn ao : this.updatedAddOns) {
					ao.setEnabled((ao.getProgress() == 0));
				}
				
				model = new AddOnUpdatesTableModel(true, this.updatedAddOns);
				getUpdatesTable().setModel(model);
				
				JScrollPane scrollPane = new JScrollPane();
				scrollPane.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
				scrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
				scrollPane.setViewportView(getUpdatesTable());

				addOnsPanel.add(scrollPane, LayoutHelper.getGBC(0, row++, 3, 1.0D, 1.0D));

				addOnsPanel.add(new JLabel(""), LayoutHelper.getGBC(0, row, 1, 1.0D));
				addOnsPanel.add(getCloseButton(), LayoutHelper.getGBC(1, row, 1, 0.0D));
				addOnsPanel.add(getUpdateButton(), LayoutHelper.getGBC(2, row, 1, 0.0D));

			}

		}
		return addOnsPanel;
	}
	
	public void setUpdatedAddOns(List<AddOn> updatedAddOns) {
		this.updatedAddOns = updatedAddOns;
		for (AddOn ao : this.updatedAddOns) {
			ao.setEnabled((ao.getProgress() == 0));
		}
		model = new AddOnUpdatesTableModel(true, this.updatedAddOns);
		getUpdatesTable().setModel(model);
	}
	
	private JTable getUpdatesTable () {
		if (updatesTable == null) {
			updatesTable = new JTable() {
				private static final long serialVersionUID = 1L;
				@Override
				public String getToolTipText(MouseEvent e) {
			        java.awt.Point p = e.getPoint();
			        int rowIndex = rowAtPoint(p);
			        AddOn ao = ((AddOnUpdatesTableModel)getModel()).getElement(rowIndex);
			        return ao.getChanges();
				}
			};
		}
		return updatesTable;
	}

	private JLabel getUpdatesMessage() {
		if (this.updatesMessage == null) {
			this.updatesMessage = new JLabel("");
		}
		return this.updatesMessage;
	}

	private JButton getCoreNotesButton() {
		if (coreNotesButton == null) {
			coreNotesButton = new JButton();
			coreNotesButton.setIcon(new ImageIcon(LogPanelCellRenderer.class.getResource("/resource/icon/16/022.png")));	// 'Text file' icon
			coreNotesButton.setToolTipText(Constant.messages.getString("cfu.button.zap.relnotes"));
			coreNotesButton.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					StringBuffer sb = new StringBuffer();
					sb.append("<html>");
					sb.append(MessageFormat.format(
							Constant.messages.getString("cfu.title.relnotes"), latestRelease.getVersion()));
					
					// Reformat the notes into html - the leading and trailing whitespace does need to be removed for some reason
					String []strs = latestRelease.getReleaseNotes().split("\n");
					for (String s : strs) {
						sb.append(s.replace("&lt;", "<").trim());
					}
					sb.append("</html>");
					View.getSingleton().showMessageDialog(sb.toString());
				}
			});
			
		}
		return coreNotesButton;
	}
	
	private JButton getCloseButton() {
		if (closeButton == null) {
			closeButton = new JButton();
			closeButton.setText(Constant.messages.getString("all.button.close"));
			closeButton.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {    
				    UpdateDialog.this.dispose();
				}
			});

		}
		return closeButton;
	}
	
	private JLabel getDownloadProgress() {
		if (downloadProgress == null) {
			downloadProgress = new JLabel("");
		}
		return downloadProgress;
	}

	private JButton getDownloadZapButton() {
		if (downloadZapButton == null) {
			downloadZapButton = new JButton();
			downloadZapButton.setText(Constant.messages.getString("cfu.button.zap.download"));
			downloadZapButton.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					extension.downloadLatestRelease();
					setDownloadingZap();
				}
			});

		}
		return downloadZapButton;
	}
	
	protected void setDownloadingZap() {
		downloadZapButton.setEnabled(false);
		getUpdateButton().setEnabled(false);	// Makes things less complicated
		state = State.DOWNLOADING_ZAP;
		getUpdatesMessage().setText(Constant.messages.getString("cfu.check.zap.downloading"));
	}

	protected void setDownloadingAllUpdates() {
		for (AddOn aoi : this.updatedAddOns) {
			aoi.setEnabled(false);
		}
		setDownloadingUpdates();
	}
	
	private void setDownloadingUpdates() {
		this.getDownloadZapButton().setEnabled(false);		// Makes things less complicated
		this.getUpdateButton().setEnabled(false);	
		this.state = State.DOWNLOADING_UPDATES;
		this.getUpdatesMessage().setText(Constant.messages.getString("cfu.check.upd.downloading"));
	}

	private void downloadUpdates() {
		boolean downloading = false;
		for (AddOn aoi : this.updatedAddOns) {
			if (aoi.isEnabled() && aoi.getProgress() == 0) {
				extension.downloadFile(aoi.getUrl(), aoi.getFile(), aoi.getSize());
				aoi.setEnabled(false);
				downloading = true;
			}
		}
		if (downloading) {
			setDownloadingUpdates();
		}
	}

	private JButton getUpdateButton() {
		if (updateButton == null) {
			updateButton = new JButton();
			updateButton.setText(Constant.messages.getString("cfu.button.addons.update"));
			updateButton.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					downloadUpdates();
				}
			});

		}
		return updateButton;
	}

	public void showUpdateProgress() {
		boolean updatesProgressed = false;
		if (this.state.equals(State.DOWNLOADING_UPDATES)) {
			for (AddOn ao : this.updatedAddOns) {
				try {
					int progress = extension.getDownloadProgressPercent(ao.getUrl());
					if (progress > 0) {
						ao.setProgress(progress);
						updatesProgressed = true;
					}
				} catch (Exception e) {
					logger.debug("Error on " + ao.getUrl(), e);
					ao.setFailed(true);
				}
			}
			if (model != null && updatesProgressed) {
				model.fireTableDataChanged();
			}
			if (extension.getCurrentDownloadCount() == 0) {
				this.state = State.DOWNLOADED_UPDATES;
				this.getDownloadZapButton().setEnabled(true);
				this.getUpdateButton().setEnabled(true);	
				this.getUpdatesMessage().setText(Constant.messages.getString("cfu.check.upd.downloaded"));
			}
		} else if (this.state.equals(State.DOWNLOADING_ZAP)) {
			try {
				int progress = extension.getDownloadProgressPercent(this.latestRelease.getUrl());
				if (progress > 0) {
					this.getDownloadProgress().setText(progress + "%");
					if (progress >= 100) {
						this.downloadComplete();
					}
				}
			} catch (Exception e) {
				logger.debug("Error on " + this.latestRelease.getUrl(), e);
				this.getDownloadProgress().setText(Constant.messages.getString("cfu.table.label.failed"));
			}
		}
	}
	
	private void downloadComplete () throws IOException {
		if (this.state.equals(State.DOWNLOADED_ZAP)) {
			// Prevent re-entry
			return;
		}
		this.state = State.DOWNLOADED_ZAP;
		File f = new File(Constant.FOLDER_LOCAL_PLUGIN, latestRelease.getFileName());

		if (Desktop.isDesktopSupported()) {
			extension.promptToLaunchReleaseAndClose(this.latestRelease.getVersion(), f);
		} else {
			View.getSingleton().showWarningDialog(MessageFormat.format(
					Constant.messages.getString("cfu.warn.nolaunch"), 
					this.latestRelease.getVersion(),
					f.getAbsolutePath()));
		}
		// Let people download updates now
		this.getUpdateButton().setEnabled(true);
		this.getUpdatesMessage().setText(MessageFormat.format(
				Constant.messages.getString("cfu.check.zap.downloaded"), 
				f.getAbsolutePath()));
	}
}
