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

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.io.File;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.PatternSyntaxException;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.UIManager;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.AbstractHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.CompoundHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.IconHighlighter;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.IconAware;
import org.jdesktop.swingx.renderer.IconValues;
import org.jdesktop.swingx.renderer.MappedValue;
import org.jdesktop.swingx.renderer.StringValues;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.Extension;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.view.AbstractFrame;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.control.AddOn;
import org.zaproxy.zap.control.AddOnCollection;
import org.zaproxy.zap.utils.DesktopUtils;
import org.zaproxy.zap.utils.FontUtils;
import org.zaproxy.zap.view.LayoutHelper;

public class ManageAddOnsDialog extends AbstractFrame implements CheckForUpdateCallback {

	protected enum State {IDLE, DOWNLOADING_ZAP, DOWNLOADED_ZAP, DOWNLOADING_UPDATES, DOWNLOADED_UPDATES}

	static final Icon ICON_ADD_ON_ISSUES = new ImageIcon(
			InstalledAddOnsTableModel.class.getResource("/resource/icon/16/050.png"));
	static final Icon ICON_ADD_ON_EXTENSION_ISSUES = new ImageIcon(
			InstalledAddOnsTableModel.class.getResource("/resource/icon/fugue/information-white.png"));
	
	private static final Logger logger = Logger.getLogger(ManageAddOnsDialog.class);
	private static final long serialVersionUID = 1L;
	private JTabbedPane jTabbed = null;
	private JPanel topPanel = null;
	private JPanel installedPanel = null;
	private JPanel browsePanel = null;
	private JPanel corePanel = null;
	private JPanel installedAddOnsPanel = null;
	private JPanel installedAddOnsFilterPanel = null;
	private JPanel uninstalledAddOnsPanel = null;
	private JPanel uninstalledAddOnsFilterPanel = null;
	private JPanel retrievePanel = null;
	private JScrollPane marketPlaceScrollPane = null;

	private JButton addOnInfoButton = null;
	private JButton coreNotesButton = null;
	private JButton downloadZapButton = null;
	private JButton checkForUpdatesButton = null;
	private JButton updateButton = null;
	private JButton updateAllButton = null;
	private JButton uninstallButton = null;
	private JButton installButton = null;
	private JButton close1Button = null;
	private JButton close2Button = null;
	
	private JLabel downloadProgress = null;
	private JLabel updatesMessage = null;
	
	private JXTable installedAddOnsTable = null;
	private JXTable uninstalledAddOnsTable = null;

	//private ZapRelease latestRelease = null;
	private String currentVersion = null;
	private AddOnCollection latestInfo = null;
	private AddOnCollection prevInfo = null;
	private ExtensionAutoUpdate extension = null;
	private AddOnCollection installedAddOns;
	private final InstalledAddOnsTableModel installedAddOnsModel;
	private final UninstalledAddOnsTableModel uninstalledAddOnsModel;

	private State state = null;
	
    /**
     * @throws HeadlessException
     */
    public ManageAddOnsDialog(ExtensionAutoUpdate ext, String currentVersion, AddOnCollection installedAddOns) throws HeadlessException {
        super();
        this.extension = ext;
        this.currentVersion = currentVersion;
        this.installedAddOns = installedAddOns;

        installedAddOnsModel = new InstalledAddOnsTableModel(installedAddOns);
        uninstalledAddOnsModel = new UninstalledAddOnsTableModel(installedAddOns);

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
	
	protected void selectMarketplaceTab() {
		getJTabbed().setSelectedIndex(1);
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
							FontUtils.getFont(FontUtils.Size.standard),
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
			installedPanel.validate();
		}
		
		
		return corePanel;
	}

	private JPanel getInstalledAddOnsPanel() {
		if (installedAddOnsPanel == null) {

			installedAddOnsPanel = new JPanel();
			installedAddOnsPanel.setLayout(new GridBagLayout());
			installedAddOnsPanel.setBorder(
					BorderFactory.createTitledBorder(
							null, Constant.messages.getString("cfu.label.addons.border"), TitledBorder.DEFAULT_JUSTIFICATION,
							javax.swing.border.TitledBorder.DEFAULT_POSITION,
							FontUtils.getFont(FontUtils.Size.standard),
							java.awt.Color.black));

			getInstalledAddOnsTable();
			
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			scrollPane.setViewportView(getInstalledAddOnsTable());

			installedAddOnsFilterPanel = createFilterPanel(getInstalledAddOnsTable());

			int row = 0;
			installedAddOnsPanel.add(installedAddOnsFilterPanel, LayoutHelper.getGBC(0, row++, 5, 0.0D));
			installedAddOnsPanel.add(scrollPane, LayoutHelper.getGBC(0, row++, 5, 1.0D, 1.0D));
			installedAddOnsPanel.add(new JLabel(""), LayoutHelper.getGBC(0, row, 1, 1.0D));
			installedAddOnsPanel.add(getUninstallButton(), LayoutHelper.getGBC(1, row, 1, 0.0D));
			installedAddOnsPanel.add(getUpdateButton(), LayoutHelper.getGBC(2, row, 1, 0.0D));
			installedAddOnsPanel.add(getUpdateAllButton(), LayoutHelper.getGBC(3, row, 1, 0.0D));
			installedAddOnsPanel.add(getClose1Button(), LayoutHelper.getGBC(4, row, 1, 0.0D));

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
							FontUtils.getFont(FontUtils.Size.standard),
							java.awt.Color.black));

			uninstalledAddOnsFilterPanel = createFilterPanel(getUninstalledAddOnsTable());

			if (latestInfo == null) {
				// Not checked yet
				getUninstalledAddOnsTable();	// To initialise the table and model
				getMarketPlaceScrollPane().setViewportView(getRetrievePanel());
				uninstalledAddOnsFilterPanel.setVisible(false);
			} else {
				getMarketPlaceScrollPane().setViewportView(getUninstalledAddOnsTable());
				uninstalledAddOnsFilterPanel.setVisible(true);
			}

			int row = 0;
			uninstalledAddOnsPanel.add(uninstalledAddOnsFilterPanel, LayoutHelper.getGBC(0, row++, 4, 0.0D));
			uninstalledAddOnsPanel.add(getMarketPlaceScrollPane(), LayoutHelper.getGBC(0, row++, 4, 1.0D, 1.0D));
			uninstalledAddOnsPanel.add(new JLabel(""), LayoutHelper.getGBC(0, row, 1, 1.0D));
			uninstalledAddOnsPanel.add(getInstallButton(), LayoutHelper.getGBC(1, row, 1, 0.0D));
			uninstalledAddOnsPanel.add(getAddOnInfoButton(), LayoutHelper.getGBC(2, row, 1, 0.0D));
			uninstalledAddOnsPanel.add(getClose2Button(), LayoutHelper.getGBC(3, row, 1, 0.0D));

		}
		return uninstalledAddOnsPanel;
	}
	
	private static JPanel createFilterPanel(final JXTable table) {
		JPanel filterPanel = new JPanel();
		filterPanel.setLayout(new GridBagLayout());

		JLabel filterLabel = new JLabel(Constant.messages.getString("cfu.label.addons.filter"));
		final JTextField filterTextField = new JTextField();

		filterLabel.setLabelFor(filterTextField);
		filterPanel.add(filterLabel, LayoutHelper.getGBC(0, 0, 1, 0.0D));
		filterPanel.add(filterTextField, LayoutHelper.getGBC(1, 0, 1, 1.0D));

		String tooltipText = Constant.messages.getString("cfu.label.addons.filter.tooltip");
		filterLabel.setToolTipText(tooltipText);
		filterTextField.setToolTipText(tooltipText);

		// Set filter listener
		filterTextField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				updateFilter();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				updateFilter();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				updateFilter();
			}

			public void updateFilter() {
				String filterText = filterTextField.getText();
				if (filterText.isEmpty()) {
					table.setRowFilter(null);
					filterTextField.setForeground(UIManager.getColor("TextField.foreground"));
				} else {
					try {
						table.setRowFilter(RowFilter.regexFilter("(?i)" + filterText));
						filterTextField.setForeground(UIManager.getColor("TextField.foreground"));
					} catch (PatternSyntaxException e) {
						filterTextField.setForeground(Color.RED);
					}
				}
			}
		});
		return filterPanel;
	}

	private JScrollPane getMarketPlaceScrollPane () {
		if (marketPlaceScrollPane == null) {
			marketPlaceScrollPane = new JScrollPane();
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

	protected void setPreviousVersionInfo(AddOnCollection prevInfo) {
		this.prevInfo = prevInfo;
	}

	protected void setLatestVersionInfo(AddOnCollection latestInfo) {
		this.latestInfo = latestInfo;
		getCorePanel(true);
		
		if (latestInfo != null) {
			installedAddOnsModel.setAvailableAddOns(latestInfo);
			uninstalledAddOnsModel.setAddOnCollection(latestInfo);

			List<AddOn> addOnsNotInstalled = installedAddOnsModel.updateEntries();
			uninstalledAddOnsModel.setAddOns(addOnsNotInstalled, prevInfo);
		}
		getMarketPlaceScrollPane().setViewportView(getUninstalledAddOnsTable());
		uninstalledAddOnsFilterPanel.setVisible(true);

	}
	
	private JXTable getInstalledAddOnsTable () {
		if (installedAddOnsTable == null) {
			installedAddOnsTable = new JXTable();
			installedAddOnsModel.addTableModelListener(new TableModelListener() {
				@Override
				public void tableChanged(TableModelEvent e) {
					getUpdateButton().setEnabled(installedAddOnsModel.canUpdateSelected());
					getUpdateAllButton().setEnabled(installedAddOnsModel.getAllUpdates().size() > 0);
					getUninstallButton().setEnabled(installedAddOnsModel.canUninstallSelected());
					
				}});

			installedAddOnsTable.setModel(installedAddOnsModel);
			installedAddOnsTable.getColumnModel().getColumn(0).setMaxWidth(20);//icon
			installedAddOnsTable.getColumnExt(0).setSortable(false);//icon doesn't need to be sortable
			installedAddOnsTable.getColumnModel().getColumn(1).setPreferredWidth(200);//name
			installedAddOnsTable.getColumnModel().getColumn(2).setPreferredWidth(400);//description
			installedAddOnsTable.getColumnExt(2).setSortable(false);//description doesn't need to be sortable
			installedAddOnsTable.getColumnModel().getColumn(3).setPreferredWidth(60);//update
			installedAddOnsTable.getColumnExt(3).setSortable(false);//update doesn't need to be sortable
			installedAddOnsTable.getColumnModel().getColumn(4).setPreferredWidth(40);
			installedAddOnsTable.getColumnExt(4).setSortable(false);//checkbox doesn't need to be sortable
          
            //Default sort by name (column 1)
            List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>(1);
            sortKeys.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
            installedAddOnsTable.getRowSorter().setSortKeys(sortKeys);
			
			DefaultAddOnToolTipHighlighter toolTipHighlighter = new DefaultAddOnToolTipHighlighter(
					AddOnsTableModel.COLUMN_ADD_ON_WRAPPER);
			for (int i = 1; i < installedAddOnsTable.getColumnCount(); i++) {
				installedAddOnsTable.getColumnExt(i).addHighlighter(toolTipHighlighter);
			}

			installedAddOnsTable.getColumnExt(0).setCellRenderer(
					new DefaultTableRenderer(new MappedValue(StringValues.EMPTY, IconValues.NONE), JLabel.CENTER));
			installedAddOnsTable.getColumnExt(0).setHighlighters(
					new CompoundHighlighter(
							new WarningRunningIssuesHighlighter(AddOnsTableModel.COLUMN_ADD_ON_WRAPPER),
							new WarningRunningIssuesToolTipHighlighter(AddOnsTableModel.COLUMN_ADD_ON_WRAPPER)));
			installedAddOnsTable.getColumnExt(3).setHighlighters(
					new CompoundHighlighter(
							new WarningUpdateIssuesHighlighter(AddOnsTableModel.COLUMN_ADD_ON_WRAPPER),
							new WarningUpdateIssuesToolTipHighlighter(AddOnsTableModel.COLUMN_ADD_ON_WRAPPER)));
			installedAddOnsTable.getColumnExt(4).addHighlighter(
					new DisableSelectionHighlighter(AddOnsTableModel.COLUMN_ADD_ON_WRAPPER));
		}
		
		
		return installedAddOnsTable;
	}

	private JXTable getUninstalledAddOnsTable () {
		if (uninstalledAddOnsTable == null) {
			uninstalledAddOnsTable = new JXTable();

			uninstalledAddOnsModel.addTableModelListener(new TableModelListener() {
				@Override
				public void tableChanged(TableModelEvent e) {
					getInstallButton().setEnabled(uninstalledAddOnsModel.canIinstallSelected());
				}});
			
			uninstalledAddOnsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
				@Override
				public void valueChanged(ListSelectionEvent e) {
					getAddOnInfoButton().setEnabled(false);
					if (DesktopUtils.canOpenUrlInBrowser() && getUninstalledAddOnsTable ().getSelectedRowCount() == 1) {
						//convertRowIndexToModel in-case they sorted
						AddOnWrapper ao = uninstalledAddOnsModel.getAddOnWrapper(getUninstalledAddOnsTable().convertRowIndexToModel(getUninstalledAddOnsTable().getSelectedRow()));
						if (ao != null && ao.getAddOn().getInfo() != null) {
							getAddOnInfoButton().setEnabled(true);
						}
					}
				}});
			
			uninstalledAddOnsTable.setModel(uninstalledAddOnsModel);

			uninstalledAddOnsTable.getColumnModel().getColumn(0).setMaxWidth(20);//Icon
			uninstalledAddOnsTable.getColumnExt(0).setSortable(false); //Icon doesn't need sorting
			uninstalledAddOnsTable.getColumnModel().getColumn(1).setPreferredWidth(50);//Status
			uninstalledAddOnsTable.getColumnModel().getColumn(2).setPreferredWidth(150);//Name
			uninstalledAddOnsTable.getColumnModel().getColumn(3).setPreferredWidth(300);//Description
			uninstalledAddOnsTable.getColumnExt(3).setSortable(false);//Description doesn't need sorting
			uninstalledAddOnsTable.getColumnModel().getColumn(4).setPreferredWidth(60);//Update (version number)
			uninstalledAddOnsTable.getColumnExt(4).setSortable(false);//Update doesn't need sorting
			uninstalledAddOnsTable.getColumnModel().getColumn(5).setPreferredWidth(40);//Checkbox
			uninstalledAddOnsTable.getColumnExt(5).setSortable(false);//Checkbox doesn't need sorting
         
            //Default sort by status (column 1) descending (Release, Beta, Alpha), and name (column 2) ascending 
            List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>(2);
            sortKeys.add(new RowSorter.SortKey(1, SortOrder.DESCENDING));
            sortKeys.add(new RowSorter.SortKey(2, SortOrder.ASCENDING));
            uninstalledAddOnsTable.getRowSorter().setSortKeys(sortKeys);
                        
			DefaultAddOnToolTipHighlighter toolTipHighlighter = new DefaultAddOnToolTipHighlighter(
					UninstalledAddOnsTableModel.COLUMN_ADD_ON_WRAPPER);
			for (int i = 1; i < uninstalledAddOnsTable.getColumnCount(); i++) {
				uninstalledAddOnsTable.getColumnExt(i).addHighlighter(toolTipHighlighter);
			}

			uninstalledAddOnsTable.getColumnExt(0).setCellRenderer(
					new DefaultTableRenderer(new MappedValue(StringValues.EMPTY, IconValues.NONE), JLabel.CENTER));
			uninstalledAddOnsTable.getColumnExt(0).setHighlighters(
					new CompoundHighlighter(
							new WarningRunningIssuesHighlighter(AddOnsTableModel.COLUMN_ADD_ON_WRAPPER),
							new WarningRunningIssuesToolTipHighlighter(UninstalledAddOnsTableModel.COLUMN_ADD_ON_WRAPPER)));
			uninstalledAddOnsTable.getColumnExt(5).addHighlighter(
					new DisableSelectionHighlighter(UninstalledAddOnsTableModel.COLUMN_ADD_ON_WRAPPER));
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
		sb.append(ao.getFileVersion());
		sb.append("</td></tr>");

		sb.append("<tr><td><i>");
		sb.append(Constant.messages.getString("cfu.table.header.notbefore"));
		sb.append("</i></td><td>");
		sb.append(ao.getNotBeforeVersion());
		sb.append("</td></tr>");

		sb.append("<tr><td><i>");
		sb.append(Constant.messages.getString("cfu.table.header.notfrom"));
		sb.append("</i></td><td>");
		sb.append(ao.getNotFromVersion());
		sb.append("</td></tr>");

		if (!ao.getIdsAddOnDependencies().isEmpty()) {
			sb.append("<tr><td><i>");
			sb.append(Constant.messages.getString("cfu.table.header.dependencies"));
			sb.append("</i></td><td>");
			for (String addOnId : ao.getIdsAddOnDependencies()) {
				AddOn dep = installedAddOns.getAddOn(addOnId);
				if (dep == null && latestInfo != null) {
					dep = latestInfo.getAddOn(addOnId);
				}

				if (dep != null) {
					sb.append(dep.getName());
				} else {
					sb.append(addOnId);
				}
				sb.append("<br>");
			}
			sb.append("</td></tr>");
		}

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
			coreNotesButton.setIcon(new ImageIcon(ManageAddOnsDialog.class.getResource("/resource/icon/16/022.png")));	// 'Text file' icon
			coreNotesButton.setToolTipText(Constant.messages.getString("cfu.button.zap.relnotes"));
			final ManageAddOnsDialog dialog = this;
			coreNotesButton.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					
					URL url = latestInfo.getZapRelease().getReleaseNotesUrl();
					if (url != null && DesktopUtils.canOpenUrlInBrowser()) {
						if (DesktopUtils.openUrlInBrowser(url.toString())) {
							// It worked :)
							return;
						}
					}
					
					StringBuilder sb = new StringBuilder();
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
    	this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		extension.getLatestVersionInfo(this);
    	this.setCursor(Cursor.getDefaultCursor());
	}

	private JButton getDownloadZapButton() {
		if (downloadZapButton == null) {
			downloadZapButton = new JButton();
			if (Constant.isKali()) {
				getDownloadZapButton().setText(Constant.messages.getString("cfu.button.zap.options"));
			} else {
				downloadZapButton.setText(Constant.messages.getString("cfu.button.zap.download"));
			}
			downloadZapButton.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (extension.downloadLatestRelease()) {
						setDownloadingZap();
					}
				}
			});

		}
		return downloadZapButton;
	}

	protected void setDownloadingZap() {
		downloadZapButton.setEnabled(false);
		getUpdateButton().setEnabled(false);	// Makes things less complicated
		getUpdateAllButton().setEnabled(false);
		state = State.DOWNLOADING_ZAP;
		getUpdatesMessage().setText(Constant.messages.getString("cfu.check.zap.downloading"));
	}

	protected void setDownloadingUpdates() {
		if (EventQueue.isDispatchThread()) {
			this.getDownloadZapButton().setEnabled(false); // Makes things less complicated
			this.getUpdateButton().setEnabled(false);
			this.getUpdateAllButton().setEnabled(false);
			this.state = State.DOWNLOADING_UPDATES;
			this.getUpdatesMessage().setText(Constant.messages.getString("cfu.check.upd.downloading"));
		} else {
			EventQueue.invokeLater(new Runnable() {

				@Override
				public void run() {
					setDownloadingUpdates();
				}
			});
		}
	}

	/**
	 * Notifies that the given {@code addOn} is being downloaded.
	 *
	 * @param addOn the add-on that is being downloaded
	 * @since 2.4.0
	 */
	public void notifyAddOnDownloading(AddOn addOn) {
		if (installedAddOnsModel.notifyAddOnDownloading(addOn)) {
			// It's an update...
			return;
		}

		uninstalledAddOnsModel.notifyAddOnDownloading(addOn);
	}

	/**
	 * Notifies that the download of the add-on with the given {@code url} as failed.
	 * <p>
	 * The entry of the add-on is updated to report that the download failed.
	 *
	 * @param url the URL of the add-on that was being downloaded
	 * @since 2.4.0
	 */
	public void notifyAddOnDownloadFailed(String url) {
		if (installedAddOnsModel.notifyAddOnDownloadFailed(url)) {
			// It's an update...
			return;
		}

		uninstalledAddOnsModel.notifyAddOnDownloadFailed(url);
	}

    /**
     * Notifies that the given {@code addOn} was installed. The add-on is added to the table of installed add-ons, or if an
     * update, set it as updated, and, if available in marketplace, removed from the table of available add-ons.
     *
     * @param addOn the add-on that was installed
     * @since 2.4.0
     */
    public void notifyAddOnInstalled(final AddOn addOn) {
        if (EventQueue.isDispatchThread()) {
            if (latestInfo != null && latestInfo.getAddOn(addOn.getId()) != null) {
                uninstalledAddOnsModel.removeAddOn(addOn);
            }
            installedAddOnsModel.addOrRefreshAddOn(addOn);
        } else {
            EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    notifyAddOnInstalled(addOn);
                }
            });
        }
    }

    /**
     * Notifies that the given {@code addOn} as not successfully uninstalled. Add-ons that were not successfully uninstalled are
     * not re-selectable.
     *
     * @param addOn the add-on that was not successfully uninstalled
     * @since 2.4.0
     */
	public void notifyAddOnFailedUninstallation(final AddOn addOn) {
		if (EventQueue.isDispatchThread()) {
			installedAddOnsModel.notifyAddOnFailedUninstallation(addOn);
		} else {
			EventQueue.invokeLater(new Runnable() {

				@Override
				public void run() {
					notifyAddOnFailedUninstallation(addOn);
				}
			});
		}
	}

    /**
     * Notifies that the given {@code addOn} as uninstalled. The add-on is removed from the table of installed add-ons and, if
     * available in marketplace, added to the table of available add-ons.
     *
     * @param addOn the add-on that was uninstalled
     * @since 2.4.0
     */
	public void notifyAddOnUninstalled(final AddOn addOn) {
		if (EventQueue.isDispatchThread()) {
			installedAddOnsModel.removeAddOn(addOn);
			if (latestInfo != null) {
				AddOn availableAddOn = latestInfo.getAddOn(addOn.getId());
				if (availableAddOn != null) {
					uninstalledAddOnsModel.addAddOn(latestInfo.getAddOn(addOn.getId()));
				}
			}
		} else {
			EventQueue.invokeLater(new Runnable() {

				@Override
				public void run() {
					notifyAddOnUninstalled(addOn);
				}
			});
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
					processUpdates(installedAddOnsModel.getSelectedUpdates());
				}
			});

		}
		return updateButton;
	}

	private JButton getUpdateAllButton() {
		if (updateAllButton == null) {
			updateAllButton = new JButton();
			updateAllButton.setText(Constant.messages.getString("cfu.button.addons.updateAll"));
			updateAllButton.setEnabled(false);	// Nothing will be selected initially
			updateAllButton.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					processUpdates(installedAddOnsModel.getAllUpdates());
				}
			});

		}
		return updateAllButton;
	}
	
	private void processUpdates(Set<AddOn> updatedAddOns) {
		if (updatedAddOns.isEmpty()) {
			return;
		}

		AddOnDependencyChecker calc = new AddOnDependencyChecker(installedAddOns, latestInfo);

		AddOnDependencyChecker.AddOnChangesResult result = calc.calculateUpdateChanges(updatedAddOns);
		if (!calc.confirmUpdateChanges(ManageAddOnsDialog.this, result)) {
			return;
		}

		extension.processAddOnChanges(ManageAddOnsDialog.this, result);
		
	}

	private JButton getUninstallButton() {
		if (uninstallButton == null) {
			uninstallButton = new JButton();
			uninstallButton.setText(Constant.messages.getString("cfu.button.addons.uninstall"));
			uninstallButton.setEnabled(false);	// Nothing will be selected initially
			uninstallButton.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Set<AddOn> selectedAddOns = installedAddOnsModel.getSelectedAddOns();
					if (selectedAddOns.isEmpty()) {
						return;
					}

					Set<AddOn> addOnsBeingDownloaded = installedAddOnsModel.getDownloadingAddOns();
					addOnsBeingDownloaded.addAll(uninstalledAddOnsModel.getDownloadingAddOns());

					AddOnDependencyChecker calc = new AddOnDependencyChecker(installedAddOns, latestInfo);
					AddOnDependencyChecker.UninstallationResult changes = calc.calculateUninstallChanges(selectedAddOns);

					if (!calc.confirmUninstallChanges(ManageAddOnsDialog.this, changes, addOnsBeingDownloaded)) {
						return;
					}

					Set<AddOn> addOns = changes.getUninstallations();
					Set<Extension> extensions = changes.getExtensions();
					if (!extension.warnUnsavedResourcesOrActiveActions(ManageAddOnsDialog.this, addOns, extensions, false)) {
						return;
					}

					extension.uninstallAddOnsWithView(ManageAddOnsDialog.this, addOns, false, new HashSet<AddOn>());
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
					Set<AddOn> selectedAddOns = uninstalledAddOnsModel.getSelectedAddOns();
					if (selectedAddOns.isEmpty()) {
						return;
					}

					AddOnDependencyChecker calc = new AddOnDependencyChecker(installedAddOns, latestInfo);

					AddOnDependencyChecker.AddOnChangesResult changes = calc.calculateInstallChanges(selectedAddOns);
					if (!calc.confirmInstallChanges(ManageAddOnsDialog.this,changes)) {
						return;
					}

					extension.processAddOnChanges(ManageAddOnsDialog.this, changes);
				}
			});

		}
		return installButton;
	}

	private JButton getAddOnInfoButton() {
		if (addOnInfoButton == null) {
			addOnInfoButton = new JButton();
			addOnInfoButton.setText(Constant.messages.getString("cfu.button.addons.info"));
			addOnInfoButton.setEnabled(false);	// Nothing will be selected initially
			addOnInfoButton.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (getUninstalledAddOnsTable().getSelectedRow() >= 0) {
						//convertRowIndexToModel in-case they sorted
						AddOnWrapper ao = uninstalledAddOnsModel.getAddOnWrapper(getUninstalledAddOnsTable().convertRowIndexToModel(getUninstalledAddOnsTable().getSelectedRow()));
						if (ao != null && ao.getAddOn().getInfo() != null) {
							DesktopUtils.openUrlInBrowser(ao.getAddOn().getInfo().toString());
						}
					}
				}
			});

		}
		return addOnInfoButton;
	}

	public void showProgress() {
		if (this.state.equals(State.DOWNLOADING_UPDATES)) {
			// Updates
			installedAddOnsModel.updateDownloadsProgresses(extension);

			// New addons
			uninstalledAddOnsModel.updateDownloadsProgresses(extension);
			
			if (extension.getCurrentDownloadCount() == 0) {
				this.state = State.DOWNLOADED_UPDATES;
				this.getDownloadZapButton().setEnabled(true);
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
	
	private void zapDownloadComplete () {
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
		this.getUpdateAllButton().setEnabled(true);
		this.getUpdatesMessage().setText(MessageFormat.format(
				Constant.messages.getString("cfu.check.zap.downloaded"), 
				f.getAbsolutePath()));
	}

	@Override
	public void gotLatestData(AddOnCollection aoc) {
		// Callback
		logger.debug("gotLatestData(AddOnCollection " + aoc);
		
		if (aoc != null) {
			setLatestVersionInfo(aoc);
		} else {
			View.getSingleton().showWarningDialog(this, Constant.messages.getString("cfu.check.failed"));
		}
	}
	
	@Override
	public void insecureUrl(String url, Exception cause) {
		logger.error("Failed to get check for updates on " + url, cause);
   		View.getSingleton().showWarningDialog(this, Constant.messages.getString("cfu.warn.badurl"));
	}

    private static class DisableSelectionHighlighter extends AbstractHighlighter {

        public DisableSelectionHighlighter(final int columnIndex) {
            setHighlightPredicate(new HighlightPredicate() {

                @Override
                public boolean isHighlighted(final Component renderer, final ComponentAdapter adapter) {
                    AddOn.InstallationStatus status = ((AddOnWrapper) adapter.getValue(columnIndex)).getInstallationStatus();

                    return AddOn.InstallationStatus.UNINSTALLATION_FAILED == status
                            || AddOn.InstallationStatus.SOFT_UNINSTALLATION_FAILED == status
                            || AddOn.InstallationStatus.DOWNLOADING == status;
                }
            });
        }

        @Override
        protected Component doHighlight(Component renderer, ComponentAdapter adapter) {
            renderer.setEnabled(false);
            return renderer;
        }
    }

    private static class WarningRunningIssuesHighlighter extends IconHighlighter {

        private final int columnIndex;

        public WarningRunningIssuesHighlighter(int columnIndex) {
            super();
            this.columnIndex = columnIndex;

            setHighlightPredicate(new HighlightPredicate.EqualsHighlightPredicate(Boolean.TRUE));
        }

        public Icon getIcon(ComponentAdapter adapter) {
            AddOnWrapper aow = (AddOnWrapper) adapter.getValue(columnIndex);
            if (aow.isAddOnRunningIssues()) {
                return ICON_ADD_ON_ISSUES;
            }
            return ICON_ADD_ON_EXTENSION_ISSUES;
        }

        @Override
        protected Component doHighlight(Component component, ComponentAdapter adapter) {
            if (component instanceof IconAware) {
                ((IconAware) component).setIcon(getIcon(adapter));
            } else if (component instanceof JLabel) {
                ((JLabel) component).setIcon(getIcon(adapter));
            }
            return component;
        }
    }

    private static class WarningUpdateIssuesHighlighter extends IconHighlighter {

        private final int columnIndex;

        public WarningUpdateIssuesHighlighter(int columnIndex) {
            super();
            this.columnIndex = columnIndex;

            setHighlightPredicate(new HighlightPredicate() {

                @Override
                public boolean isHighlighted(final Component renderer, final ComponentAdapter adapter) {
                    AddOnWrapper aow = (AddOnWrapper) adapter.getValue(WarningUpdateIssuesHighlighter.this.columnIndex);
                    if (AddOnWrapper.Status.newVersion == aow.getStatus()) {
                        return aow.hasUpdateIssues();
                    }
                    return false;
                }
            });
        }

        public Icon getIcon(ComponentAdapter adapter) {
            AddOnWrapper aow = (AddOnWrapper) adapter.getValue(columnIndex);
            if (aow.isAddOnUpdateIssues()) {
                return ICON_ADD_ON_ISSUES;
            }
            return ICON_ADD_ON_EXTENSION_ISSUES;
        }

        @Override
        protected Component doHighlight(Component component, ComponentAdapter adapter) {
            if (component instanceof IconAware) {
                ((IconAware) component).setIcon(getIcon(adapter));
            } else if (component instanceof JLabel) {
                ((JLabel) component).setIcon(getIcon(adapter));
            }
            return component;
        }
    }

    private class DefaultAddOnToolTipHighlighter extends AbstractHighlighter {

        private final int column;

        public DefaultAddOnToolTipHighlighter(int column) {
            this.column = column;
        }

        @Override
        protected Component doHighlight(Component component, ComponentAdapter adapter) {
            ((JComponent) component).setToolTipText(getToolTip((AddOnWrapper) adapter.getValue(column)));
            return component;
        }

        protected String getToolTip(AddOnWrapper aow) {
            if (AddOn.InstallationStatus.UNINSTALLATION_FAILED == aow.getInstallationStatus()
                    || AddOn.InstallationStatus.SOFT_UNINSTALLATION_FAILED == aow.getInstallationStatus()) {
                return addOnToHtml(aow.getAddOn());
            }

            AddOn addOn = (aow.getAddOnUpdate() != null) ? aow.getAddOnUpdate() : aow.getAddOn();
            return addOnToHtml(addOn);
        }
    }

    private class WarningRunningIssuesToolTipHighlighter extends DefaultAddOnToolTipHighlighter {

        public WarningRunningIssuesToolTipHighlighter(int column) {
            super(column);
        }

        @Override
        protected String getToolTip(AddOnWrapper aow) {
            if (aow.hasRunningIssues()) {
                return aow.getRunningIssues();
            }
            return super.getToolTip(aow);
        }
    }

    private class WarningUpdateIssuesToolTipHighlighter extends DefaultAddOnToolTipHighlighter {

        public WarningUpdateIssuesToolTipHighlighter(int column) {
            super(column);
        }

        @Override
        protected String getToolTip(AddOnWrapper aow) {
            if (aow.hasUpdateIssues()) {
                return aow.getUpdateIssues();
            }
            return super.getToolTip(aow);
        }
    }
}
