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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.history.LogPanelCellRenderer;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.view.AbstractFrame;
import org.parosproxy.paros.view.View;
import org.parosproxy.paros.view.WaitMessageDialog;
import org.zaproxy.zap.control.AddOn;
import org.zaproxy.zap.control.AddOnCollection;
import org.zaproxy.zap.view.LayoutHelper;

public class ManageAddOnsDialog extends AbstractFrame implements CheckForUpdateCallback {

	protected enum State {IDLE, DOWNLOADING_ZAP, DOWNLOADED_ZAP, DOWNLOADING_UPDATES, DOWNLOADED_UPDATES}
	
	private Logger logger = Logger.getLogger(ManageAddOnsDialog.class);
	private static final long serialVersionUID = 1L;
	private JTabbedPane jTabbed = null;
	private JPanel topPanel = null;
	private JPanel installedPanel = null;
	private JPanel browsePanel = null;
	private JPanel corePanel = null;
	private JPanel installedAddOnsPanel = null;
	private JPanel uninstalledAddOnsPanel = null;
	private JPanel retrievePanel = null;
	private JScrollPane marketPlaceScrollPane = null;
    private WaitMessageDialog waitDialog = null;

	//private JButton addOnInfoButton = null;
	private JButton coreNotesButton = null;
	private JButton downloadZapButton = null;
	private JButton checkForUpdatesButton = null;
	private JButton updateButton = null;
	private JButton uninstallButton = null;
	private JButton installButton = null;
	private JButton close1Button = null;
	private JButton close2Button = null;
	
	private JLabel downloadProgress = null;
	private JLabel updatesMessage = null;
	
	private JTable installedAddOnsTable = null;
	private JTable uninstalledAddOnsTable = null;

	//private ZapRelease latestRelease = null;
	private String currentVersion = null;
	private AddOnCollection latestInfo = null;
	private List<AddOnWrapper> installedAddOns = null;
	private List<AddOnWrapper> uninstalledAddOns = null;
	private ExtensionAutoUpdate extension = null;
	private InstalledAddOnsTableModel installedAddOnsModel = null;
	private UninstalledAddOnsTableModel uninstalledAddOnsModel = null;
	
	private State state = null;
	
    /**
     * @throws HeadlessException
     */
    public ManageAddOnsDialog(ExtensionAutoUpdate ext, String currentVersion, List<AddOnWrapper> installedAddOns) throws HeadlessException {
        super();
        this.extension = ext;
        this.currentVersion = currentVersion;
        this.installedAddOns = this.sortAddOns(installedAddOns, false);

 		initialize();
    }

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
		this.setTitle(Constant.messages.getString("cfu.manage.title"));
        //this.setContentPane(getJTabbed());
        this.setContentPane(getTopPanel());
        this.pack();
        if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
        	this.setSize(700, 500);
        }
        state = State.IDLE;
	}

	private JPanel getTopPanel() {
		if (topPanel == null) {
			topPanel = new JPanel();
			topPanel.setLayout(new GridBagLayout());
			topPanel.add(getJTabbed(), LayoutHelper.getGBC(0, 0, 1, 1.0D, 1.0D));
			topPanel.add(this.getUpdatesMessage(), LayoutHelper.getGBC(0, 2, 1, 1.0D));
		}
		return topPanel;
	}

	private JTabbedPane getJTabbed() {
		if (jTabbed == null) {
			jTabbed = new JTabbedPane();
			jTabbed.addTab(Constant.messages.getString("cfu.tab.installed"), this.getInstalledPanel());
			jTabbed.addTab(Constant.messages.getString("cfu.tab.browse"), this.getBrowsePanel());
		}
		return jTabbed;
	}
	
	private JPanel getInstalledPanel() {
		if (installedPanel == null) {
			installedPanel = new JPanel();
			installedPanel.setLayout(new GridBagLayout());
			installedPanel.add(getCorePanel(true), LayoutHelper.getGBC(0, 0, 1, 1.0D, 0.0D));
			installedPanel.add(getInstalledAddOnsPanel(), LayoutHelper.getGBC(0, 1, 1, 1.0D, 1.0D));
		}
		return installedPanel;
	}

	private JPanel getBrowsePanel() {
		if (browsePanel == null) {
			browsePanel = new JPanel();
			browsePanel.setLayout(new GridBagLayout());
			browsePanel.add(getUninstalledAddOnsPanel(), LayoutHelper.getGBC(0, 0, 1, 1.0D, 1.0D));
		}
		return browsePanel;
	}


	private JPanel getCorePanel(boolean update) {
		if (corePanel == null) {
			corePanel = new JPanel();
			corePanel.setLayout(new GridBagLayout());
			corePanel.setBorder(
					BorderFactory.createTitledBorder(
							null, Constant.messages.getString("cfu.label.zap.border"), TitledBorder.DEFAULT_JUSTIFICATION,
							javax.swing.border.TitledBorder.DEFAULT_POSITION,
							new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11),
							java.awt.Color.black));

			if (latestInfo == null || this.latestInfo.getZapRelease() == null) {
				// Havnt checked for updatees yet
				corePanel.add(new JLabel(this.currentVersion), LayoutHelper.getGBC(0, 0, 1, 0.0D));
				corePanel.add(new JLabel(""), LayoutHelper.getGBC(1, 0, 1, 1.0D));
				corePanel.add(this.getCheckForUpdatesButton(), LayoutHelper.getGBC(2, 0, 1, 0.0D));
				
			} else if (this.latestInfo.getZapRelease().isNewerThan(this.currentVersion)) {
				corePanel.add(new JLabel(Constant.messages.getString("cfu.check.zap.newer")), LayoutHelper.getGBC(0, 0, 1, 0.0D));
				corePanel.add(new JLabel(this.latestInfo.getZapRelease().getVersion()), LayoutHelper.getGBC(1, 0, 1, 0.1D));
				corePanel.add(new JLabel(""), LayoutHelper.getGBC(2, 0, 1, 0.8D));
				corePanel.add(this.getDownloadProgress(), LayoutHelper.getGBC(3, 0, 1, 0.2D));
				corePanel.add(this.getCoreNotesButton(), LayoutHelper.getGBC(4, 0, 1, 0.0D));
				corePanel.add(this.getDownloadZapButton(), LayoutHelper.getGBC(5, 0, 1, 0.0D));
				
			} else {
				corePanel.add(new JLabel(this.currentVersion + " : " + Constant.messages.getString("cfu.check.zap.latest")), LayoutHelper.getGBC(0, 0, 1, 1.0D));
			}
			
		} else if (update && latestInfo != null && this.latestInfo.getZapRelease() != null) {
			corePanel.removeAll();

			if (this.latestInfo.getZapRelease().isNewerThan(this.currentVersion)) {
				corePanel.add(new JLabel(Constant.messages.getString("cfu.check.zap.newer")), LayoutHelper.getGBC(0, 0, 1, 0.0D));
				corePanel.add(new JLabel(this.latestInfo.getZapRelease().getVersion()), LayoutHelper.getGBC(1, 0, 1, 0.1D));
				corePanel.add(new JLabel(""), LayoutHelper.getGBC(2, 0, 1, 0.8D));
				corePanel.add(this.getDownloadProgress(), LayoutHelper.getGBC(3, 0, 1, 0.2D));
				corePanel.add(this.getCoreNotesButton(), LayoutHelper.getGBC(4, 0, 1, 0.0D));
				corePanel.add(this.getDownloadZapButton(), LayoutHelper.getGBC(5, 0, 1, 0.0D));
			} else {
				corePanel.add(new JLabel(this.currentVersion + " : " + Constant.messages.getString("cfu.check.zap.latest")), LayoutHelper.getGBC(0, 0, 1, 1.0D));
			}
		}
		
		
		return corePanel;
	}
	
	private List<AddOnWrapper> sortAddOns(List<AddOnWrapper> addons, final boolean statusFirst) {
		if (addons != null) {
	        Collections.sort(addons, new Comparator<AddOnWrapper>() {
				@Override
				public int compare(AddOnWrapper ao1, AddOnWrapper ao2) {
					if (statusFirst && ! ao1.getAddOn().getStatus().equals(ao2.getAddOn().getStatus())) {
						// Reverse order - we want the most stable ones first
						return ao2.getAddOn().getStatus().compareTo(ao1.getAddOn().getStatus());
					}
					return ao1.getAddOn().getName().toLowerCase().compareTo(ao2.getAddOn().getName().toLowerCase());
				};
	        });
		}
        return addons;
	}

	private JPanel getInstalledAddOnsPanel() {
		if (installedAddOnsPanel == null) {

			installedAddOnsPanel = new JPanel();
			installedAddOnsPanel.setLayout(new GridBagLayout());
			installedAddOnsPanel.setBorder(
					BorderFactory.createTitledBorder(
							null, Constant.messages.getString("cfu.label.addons.border"), TitledBorder.DEFAULT_JUSTIFICATION,
							javax.swing.border.TitledBorder.DEFAULT_POSITION,
							new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11),
							java.awt.Color.black));

			getInstalledAddOnsTable();
			
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
			scrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			scrollPane.setViewportView(getInstalledAddOnsTable());

			int row = 0;
			installedAddOnsPanel.add(scrollPane, LayoutHelper.getGBC(0, row++, 4, 1.0D, 1.0D));
			installedAddOnsPanel.add(new JLabel(""), LayoutHelper.getGBC(0, row, 1, 1.0D));
			installedAddOnsPanel.add(getUninstallButton(), LayoutHelper.getGBC(1, row, 1, 0.0D));
			installedAddOnsPanel.add(getUpdateButton(), LayoutHelper.getGBC(2, row, 1, 0.0D));
			installedAddOnsPanel.add(getClose1Button(), LayoutHelper.getGBC(3, row, 1, 0.0D));

		}
		return installedAddOnsPanel;
	}

	private JPanel getUninstalledAddOnsPanel() {
		if (uninstalledAddOnsPanel == null) {

			uninstalledAddOnsPanel = new JPanel();
			uninstalledAddOnsPanel.setLayout(new GridBagLayout());
			uninstalledAddOnsPanel.setBorder(
					BorderFactory.createTitledBorder(
							null, Constant.messages.getString("cfu.label.addons.border"), TitledBorder.DEFAULT_JUSTIFICATION,
							javax.swing.border.TitledBorder.DEFAULT_POSITION,
							new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11),
							java.awt.Color.black));

			if (this.uninstalledAddOns == null) {
				// Not checked yet
				getUninstalledAddOnsTable();	// To initialise the table and model
				getMarketPlaceScrollPane().setViewportView(getRetrievePanel());
			} else {
				getMarketPlaceScrollPane().setViewportView(getUninstalledAddOnsTable());
			}

			int row = 0;
			uninstalledAddOnsPanel.add(getMarketPlaceScrollPane(), LayoutHelper.getGBC(0, row++, 4, 1.0D, 1.0D));
			uninstalledAddOnsPanel.add(new JLabel(""), LayoutHelper.getGBC(0, row, 1, 1.0D));
			uninstalledAddOnsPanel.add(getInstallButton(), LayoutHelper.getGBC(1, row, 1, 0.0D));
			//uninstalledAddOnsPanel.add(getAddOnInfoButton(), LayoutHelper.getGBC(2, row, 1, 0.0D));
			uninstalledAddOnsPanel.add(getClose2Button(), LayoutHelper.getGBC(3, row, 1, 0.0D));

		}
		return uninstalledAddOnsPanel;
	}
	
	private JScrollPane getMarketPlaceScrollPane () {
		if (marketPlaceScrollPane == null) {
			marketPlaceScrollPane = new JScrollPane();
			marketPlaceScrollPane.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
			marketPlaceScrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		}
		return marketPlaceScrollPane;
	}
	
	private JPanel getRetrievePanel() {
		if (retrievePanel == null) {
			retrievePanel = new JPanel();
			retrievePanel.setLayout(new GridBagLayout());
			
			JButton retrieveButton = new JButton();
			retrieveButton.setText(Constant.messages.getString("cfu.button.checkForUpdates"));

			retrieveButton.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					checkForUpdates();
				}
			});
			retrievePanel.add(new JLabel(""), LayoutHelper.getGBC(0, 0, 1, 1.0D));
			retrievePanel.add(retrieveButton, LayoutHelper.getGBC(1, 0, 1, 0.0D));
			retrievePanel.add(new JLabel(""), LayoutHelper.getGBC(2, 0, 1, 1.0D));
			retrievePanel.add(new JLabel(""), LayoutHelper.getGBC(0, 1, 3, 1.0D, 1.0D));
			
		}
		return retrievePanel;
	}
	

	public void setInstalledAddOns(List<AddOnWrapper> installedAddOns) {
		this.installedAddOns = installedAddOns;
		installedAddOnsModel.setAddOns(this.sortAddOns(this.installedAddOns, false));
		installedAddOnsModel.fireTableDataChanged();
		if (this.latestInfo != null) {
			// Flag updates
			setLatestVersionInfo(latestInfo);
		}
	}
	
	protected void setLatestVersionInfo(AddOnCollection latestInfo) {
		this.latestInfo = latestInfo;
		getCorePanel(true);
		
		if (latestInfo != null) {
			this.uninstalledAddOns = new ArrayList<AddOnWrapper>();

			for (AddOn addOn : latestInfo.getAddOns()) {
				boolean found = false;
				for (AddOnWrapper aow : this.installedAddOns) {
					if (addOn.isSameAddOn(aow.getAddOn())) {
						// Found it
						try {
							if (addOn.isUpdateTo(aow.getAddOn())) {
								aow.setStatus(AddOnWrapper.Status.newVersion);
								aow.setAddOn(addOn);
							} else {
								aow.setStatus(AddOnWrapper.Status.latest);
							}
							found = true;
							break;
						} catch (Exception e) {
							logger.error(e.getMessage(), e);
						}
					}
				}
				if (! found) {
					this.uninstalledAddOns.add(new AddOnWrapper(addOn, AddOnWrapper.Status.uninstalled));
				}
			}
			installedAddOnsModel.setAddOns(this.sortAddOns(this.installedAddOns, false));
			uninstalledAddOnsModel.setAddOns(this.sortAddOns(this.uninstalledAddOns, true));
		}
	}
	
	private JTable getInstalledAddOnsTable () {
		if (installedAddOnsTable == null) {
			installedAddOnsTable = new JTable() {
				private static final long serialVersionUID = 1L;
				@Override
				public String getToolTipText(MouseEvent e) {
			        java.awt.Point p = e.getPoint();
			        int rowIndex = rowAtPoint(p);
			        AddOn ao = ((InstalledAddOnsTableModel)getModel()).getElement(rowIndex).getAddOn();
			        return addOnToHtml(ao);
				}
			};
			installedAddOnsModel = new InstalledAddOnsTableModel(this.installedAddOns);
			installedAddOnsModel.addTableModelListener(new TableModelListener() {
				@Override
				public void tableChanged(TableModelEvent e) {
					getUpdateButton().setEnabled(installedAddOnsModel.canUpdateSelected());
					getUninstallButton().setEnabled(installedAddOnsModel.canUninstallSelected());
					
				}});

			installedAddOnsTable.setModel(installedAddOnsModel);
			installedAddOnsTable.getColumnModel().getColumn(0).setPreferredWidth(200);
			installedAddOnsTable.getColumnModel().getColumn(1).setPreferredWidth(400);
			installedAddOnsTable.getColumnModel().getColumn(2).setPreferredWidth(60);
			installedAddOnsTable.getColumnModel().getColumn(3).setPreferredWidth(40);
			
		}
		
		
		return installedAddOnsTable;
	}

	private JTable getUninstalledAddOnsTable () {
		if (uninstalledAddOnsTable == null) {
			uninstalledAddOnsTable = new JTable() {
				private static final long serialVersionUID = 1L;
				@Override
				public String getToolTipText(MouseEvent e) {
			        java.awt.Point p = e.getPoint();
			        int rowIndex = rowAtPoint(p);
			        AddOn ao = ((UninstalledAddOnsTableModel)getModel()).getElement(rowIndex).getAddOn();
			        return addOnToHtml(ao);
				}
			};
			
			uninstalledAddOnsModel = new UninstalledAddOnsTableModel(this.sortAddOns(this.uninstalledAddOns, true));
			uninstalledAddOnsModel.addTableModelListener(new TableModelListener() {
				@Override
				public void tableChanged(TableModelEvent e) {
					getInstallButton().setEnabled(uninstalledAddOnsModel.canIinstallSelected());
				}});
			
			uninstalledAddOnsTable.setModel(uninstalledAddOnsModel);
			uninstalledAddOnsTable.getColumnModel().getColumn(0).setPreferredWidth(50);
			uninstalledAddOnsTable.getColumnModel().getColumn(1).setPreferredWidth(150);
			uninstalledAddOnsTable.getColumnModel().getColumn(2).setPreferredWidth(300);
			uninstalledAddOnsTable.getColumnModel().getColumn(3).setPreferredWidth(60);
			uninstalledAddOnsTable.getColumnModel().getColumn(4).setPreferredWidth(40);
		}
		return uninstalledAddOnsTable;
	}
	
	private String addOnToHtml(AddOn ao) {
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		sb.append("<table>");
		
		sb.append("<tr><td><i>");
		sb.append(Constant.messages.getString("cfu.table.header.name"));
		sb.append("</i></td><td>");
		sb.append(ao.getName());
		sb.append("</td></tr>");
		
		sb.append("<tr><td><i>");
		sb.append(Constant.messages.getString("cfu.table.header.status"));
		sb.append("</i></td><td>");
		sb.append(Constant.messages.getString("cfu.status." + ao.getStatus().name()));
		sb.append("</td></tr>");
		
		sb.append("<tr><td><i>");
		sb.append(Constant.messages.getString("cfu.table.header.id"));
		sb.append("</i></td><td>");
		sb.append(ao.getId());
		sb.append("</td></tr>");
		
		sb.append("<tr><td><i>");
		sb.append(Constant.messages.getString("cfu.table.header.desc"));
		sb.append("</i></td><td>");
		sb.append(ao.getDescription());
		sb.append("</td></tr>");
		
		sb.append("<tr><td><i>");
		sb.append(Constant.messages.getString("cfu.table.header.author"));
		sb.append("</i></td><td>");
		sb.append(ao.getAuthor());
		sb.append("</td></tr>");
		
		sb.append("<tr><td><i>");
		sb.append(Constant.messages.getString("cfu.table.header.changes"));
		sb.append("</i></td><td>");
		sb.append(ao.getChanges());
		sb.append("</td></tr>");

		sb.append("<tr><td><i>");
		sb.append(Constant.messages.getString("cfu.table.header.version"));
		sb.append("</i></td><td>");
		sb.append(ao.getVersion());
		sb.append("</td></tr>");

		sb.append("</table>");
		sb.append("</html>");
		
		return sb.toString();

	}

	private JLabel getUpdatesMessage() {
		if (this.updatesMessage == null) {
			this.updatesMessage = new JLabel(" ");
		}
		return this.updatesMessage;
	}

	private JButton getCoreNotesButton() {
		if (coreNotesButton == null) {
			coreNotesButton = new JButton();
			coreNotesButton.setIcon(new ImageIcon(LogPanelCellRenderer.class.getResource("/resource/icon/16/022.png")));	// 'Text file' icon
			coreNotesButton.setToolTipText(Constant.messages.getString("cfu.button.zap.relnotes"));
			final ManageAddOnsDialog dialog = this;
			coreNotesButton.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					StringBuffer sb = new StringBuffer();
					sb.append("<html>");
					sb.append(MessageFormat.format(
							Constant.messages.getString("cfu.title.relnotes"), latestInfo.getZapRelease().getVersion()));
					
					// Reformat the notes into html - the leading and trailing whitespace does need to be removed for some reason
					String []strs = latestInfo.getZapRelease().getReleaseNotes().split("\n");
					for (String s : strs) {
						sb.append(s.replace("&lt;", "<").trim());
					}
					sb.append("</html>");
					
					View.getSingleton().showMessageDialog(dialog, sb.toString());

				}
			});
			
		}
		return coreNotesButton;
	}
	
	private JButton getClose1Button() {
		if (close1Button == null) {
			close1Button = new JButton();
			close1Button.setText(Constant.messages.getString("all.button.close"));
			close1Button.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {    
				    ManageAddOnsDialog.this.dispose();
				}
			});

		}
		return close1Button;
	}
	
	private JButton getClose2Button() {
		if (close2Button == null) {
			close2Button = new JButton();
			close2Button.setText(Constant.messages.getString("all.button.close"));
			close2Button.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {    
				    ManageAddOnsDialog.this.dispose();
				}
			});

		}
		return close2Button;
	}
	
	private JLabel getDownloadProgress() {
		if (downloadProgress == null) {
			downloadProgress = new JLabel("");
		}
		return downloadProgress;
	}

	private JButton getCheckForUpdatesButton() {
		if (checkForUpdatesButton == null) {
			checkForUpdatesButton = new JButton();
			checkForUpdatesButton.setText(Constant.messages.getString("cfu.button.checkForUpdates"));
			checkForUpdatesButton.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					checkForUpdates();
				}
			});

		}
		return checkForUpdatesButton;
	}
	
	protected void checkForUpdates() {
    	waitDialog = View.getSingleton().getWaitMessageDialog(this, Constant.messages.getString("cfu.check.checking"));
    	// Allow user to close the dialog
    	waitDialog.setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);

		extension.getLatestVersionInfo(this);
		waitDialog.setVisible(true);
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
		for (AddOnWrapper aoi : this.installedAddOns) {
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
		for (AddOnWrapper aoi : this.installedAddOns) {
			if (aoi.isEnabled() && aoi.getProgress() == 0) {
				aoi.setStatus(AddOnWrapper.Status.downloading);
				extension.downloadFile(aoi.getAddOn().getUrl(), aoi.getAddOn().getFile(), aoi.getAddOn().getSize());
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
			updateButton.setEnabled(false);	// Nothing will be selected initially
			updateButton.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					downloadUpdates();
				}
			});

		}
		return updateButton;
	}

	private JButton getUninstallButton() {
		if (uninstallButton == null) {
			uninstallButton = new JButton();
			uninstallButton.setText(Constant.messages.getString("cfu.button.addons.uninstall"));
			uninstallButton.setEnabled(false);	// Nothing will be selected initially

			final ManageAddOnsDialog dialog = this;
			uninstallButton.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
			    	if (View.getSingleton().showConfirmDialog(dialog, 
			    			Constant.messages.getString("cfu.uninstall.confirm")) == JOptionPane.OK_OPTION) {
						
						boolean addOnUninstalled = false;
						for (AddOnWrapper aoi : installedAddOns) {
							if (aoi.isEnabled()) {
								logger.debug("Uninstalling " + aoi.getAddOn().getName());
								if (extension.uninstall(aoi.getAddOn())) {
									logger.debug("Uninstalling " + aoi.getAddOn().getName() + " worked");
									addOnUninstalled = true;
								} else {
									logger.debug("Uninstalling " + aoi.getAddOn().getName() + " failed");
								}
							}
						}
						if (addOnUninstalled) {
							extension.reloadAddOnData();
						}
			    	}
				}
			});

		}
		return uninstallButton;
	}

	private JButton getInstallButton() {
		if (installButton == null) {
			installButton = new JButton();
			installButton.setText(Constant.messages.getString("cfu.button.addons.install"));
			installButton.setEnabled(false);	// Nothing will be selected initially
			installButton.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					if (uninstalledAddOns != null && uninstalledAddOns.size() > 0) {
						for (AddOnWrapper aoi : uninstalledAddOns) {
							if (aoi.isEnabled()) {
								state = State.DOWNLOADING_UPDATES;
								aoi.setStatus(AddOnWrapper.Status.downloading);
								extension.downloadFile(aoi.getAddOn().getUrl(), aoi.getAddOn().getFile(), aoi.getAddOn().getSize());
							}
						}
					}
				}
			});

		}
		return installButton;
	}

	/*
	// Change to View Online or equiv?
	private JButton getAddOnInfoButton() {
		if (addOnInfoButton == null) {
			addOnInfoButton = new JButton();
			addOnInfoButton.setText(Constant.messages.getString("cfu.button.addons.info"));
			//addOnInfoButton.setEnabled(false);	// Nothing will be selected initially
			final ManageAddOnsDialog dialog = this;
			addOnInfoButton.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					if (uninstalledAddOns != null && uninstalledAddOns.size() > 0) {
						for (AddOnWrapper aoi : uninstalledAddOns) {
							if (aoi.isEnabled()) {
								break;
							}
						}
					}
				}
			});

		}
		return addOnInfoButton;
	}
	*/

	public void showProgress() {
		if (this.state.equals(State.DOWNLOADING_UPDATES)) {
			// Updates
			boolean updatesProgressed = false;
			for (AddOnWrapper ao : this.installedAddOns) {
				if (ao.getStatus().equals(AddOnWrapper.Status.downloading)) {
					try {
						int progress = extension.getDownloadProgressPercent(ao.getAddOn().getUrl());
						if (progress > 0) {
							ao.setProgress(progress);
							updatesProgressed = true;
						}
					} catch (Exception e) {
						logger.debug("Error on " + ao.getAddOn().getUrl(), e);
						ao.setFailed(true);
					}
				}
			}
			if (this.installedAddOnsModel != null && updatesProgressed) {
				this.installedAddOnsModel.fireTableDataChanged();
			}
			// New addons
			boolean installsProgressed = false;
			for (AddOnWrapper ao : this.uninstalledAddOns) {
				if (ao.getStatus().equals(AddOnWrapper.Status.downloading)) {
					try {
						int progress = extension.getDownloadProgressPercent(ao.getAddOn().getUrl());
						if (progress > 0) {
							ao.setProgress(progress);
							installsProgressed = true;
						}
					} catch (Exception e) {
						logger.debug("Error on " + ao.getAddOn().getUrl(), e);
						ao.setFailed(true);
					}
				}
			}
			if (this.uninstalledAddOnsModel != null && installsProgressed) {
				this.uninstalledAddOnsModel.fireTableDataChanged();
			}
			
			if (extension.getCurrentDownloadCount() == 0) {
				this.state = State.DOWNLOADED_UPDATES;
				this.getDownloadZapButton().setEnabled(true);
				this.getUpdateButton().setEnabled(true);	
				this.getUpdatesMessage().setText(Constant.messages.getString("cfu.check.upd.downloaded"));
			}
		} else if (this.state.equals(State.DOWNLOADING_ZAP)) {
			try {
				int progress = extension.getDownloadProgressPercent(this.latestInfo.getZapRelease().getUrl());
				if (progress > 0) {
					this.getDownloadProgress().setText(progress + "%");
					if (progress >= 100) {
						this.zapDownloadComplete();
					}
				}
			} catch (Exception e) {
				logger.debug("Error on " + this.latestInfo.getZapRelease().getUrl(), e);
				this.getDownloadProgress().setText(Constant.messages.getString("cfu.table.label.failed"));
			}
		}
	}
	
	private void zapDownloadComplete () throws IOException {
		if (this.state.equals(State.DOWNLOADED_ZAP)) {
			// Prevent re-entry
			return;
		}
		this.state = State.DOWNLOADED_ZAP;
		File f = new File(Constant.FOLDER_LOCAL_PLUGIN, latestInfo.getZapRelease().getFileName());

		if (Desktop.isDesktopSupported()) {
			extension.promptToLaunchReleaseAndClose(this.latestInfo.getZapRelease().getVersion(), f);
		} else {
			View.getSingleton().showWarningDialog(this, MessageFormat.format(
					Constant.messages.getString("cfu.warn.nolaunch"), 
					this.latestInfo.getZapRelease().getVersion(),
					f.getAbsolutePath()));
		}
		// Let people download updates now
		this.getUpdateButton().setEnabled(true);
		this.getUpdatesMessage().setText(MessageFormat.format(
				Constant.messages.getString("cfu.check.zap.downloaded"), 
				f.getAbsolutePath()));
	}

	@Override
	public void gotLatestData(AddOnCollection aoc) {
		// Callback
		logger.debug("gotLatestData(AddOnCollection " + aoc);
		if (waitDialog != null) {
			waitDialog.setVisible(false);
			waitDialog.dispose();
		}
		if (aoc != null) {
			setLatestVersionInfo(aoc);
			getMarketPlaceScrollPane().setViewportView(getUninstalledAddOnsTable());
		} else {
			View.getSingleton().showWarningDialog(this, Constant.messages.getString("cfu.check.failed"));
		}
	}
}
