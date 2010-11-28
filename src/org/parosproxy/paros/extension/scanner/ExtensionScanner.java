/*
 *
 * Paros and its related class files.
 * 
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2004 Chinotec Technologies Company
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Clarified Artistic License
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Clarified Artistic License for more details.
 * 
 * You should have received a copy of the Clarified Artistic License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.parosproxy.paros.extension.scanner;

import java.awt.EventQueue;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Vector;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import org.apache.commons.configuration.ConfigurationException;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.core.scanner.HostProcess;
import org.parosproxy.paros.core.scanner.Scanner;
import org.parosproxy.paros.core.scanner.ScannerListener;
import org.parosproxy.paros.core.scanner.ScannerParam;
import org.parosproxy.paros.db.RecordAlert;
import org.parosproxy.paros.db.RecordScan;
import org.parosproxy.paros.db.TableAlert;
import org.parosproxy.paros.extension.CommandLineArgument;
import org.parosproxy.paros.extension.CommandLineListener;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.ExtensionHookMenu;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.extension.history.ManualRequestEditorDialog;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteMap;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ExtensionScanner extends ExtensionAdaptor implements ScannerListener, SessionChangedListener, CommandLineListener {
    
    private static final int ARG_SCAN_IDX = 0;
    
	private JMenuItem menuItemScanAll = null;
	private ExtensionHookMenu pluginMenu = null;
	private Scanner scanner = null;
	private SiteMap siteTree = null;
	private SiteNode startNode = null;	
	private AlertTreeModel treeAlert = null;
	
	private JMenu menuScanner = null;
	private JMenuItem menuItemPolicy = null;
	private ProgressDialog progressDialog = null;  //  @jve:decl-index=0:visual-constraint="10,10"
	private JMenuItem menuItemScan = null;
	private AlertPanel alertPanel = null;  //  @jve:decl-index=0:visual-constraint="61,102"
	private RecordScan recordScan = null;
	
	private ManualRequestEditorDialog manualRequestEditorDialog = null;
	private PopupMenuResend popupMenuResend = null;
	// ZAP: Added popup menu alert edit
	private PopupMenuAlertEdit popupMenuAlertEdit = null;
	private OptionsScannerPanel optionsScannerPanel = null;
	private ScannerParam scannerParam = null;   //  @jve:decl-index=0:
	private CommandLineArgument[] arguments = new CommandLineArgument[1];
	private long startTime = 0;

    private PopupMenuScanHistory popupMenuScanHistory = null;
    
    /**
     * 
     */
    public ExtensionScanner() {
        super();
 		initialize();
    }

    /**
     * @param name
     */
    public ExtensionScanner(String name) {
        super(name);
    }

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setName("ExtensionScanner");
			
	}
	/**
	 * This method initializes menuItemScanAll	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */    
	private JMenuItem getMenuItemScanAll() {
		if (menuItemScanAll == null) {
			menuItemScanAll = new JMenuItem();
			menuItemScanAll.setText(Constant.messages.getString("menu.analyse.scanAll"));	// ZAP: i18n
			menuItemScanAll.addActionListener(new java.awt.event.ActionListener() { 

				public void actionPerformed(java.awt.event.ActionEvent e) {
				    menuItemScan.setEnabled(false);
				    menuItemScanAll.setEnabled(false);
				    getAlertPanel().setTabFocus();
				    startScan();
				    
				}
			});

		}
		return menuItemScanAll;
	}
		
	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);
	    if (getView() != null) {
	        //extensionHook.getHookMenu().addNewMenu(getMenuScanner());
            extensionHook.getHookMenu().addAnalyseMenuItem(getMenuItemScanAll());
            extensionHook.getHookMenu().addAnalyseMenuItem(getMenuItemScan());
            extensionHook.getHookMenu().addAnalyseMenuItem(getMenuItemPolicy());

            extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuResend());
            extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuScanHistory());

        	// ZAP: Added popup menu alert edit
            extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuAlertEdit());

            extensionHook.getHookView().addStatusPanel(getAlertPanel());
	        extensionHook.getHookView().addOptionPanel(getOptionsScannerPanel());
	    }
        extensionHook.addSessionListener(this);
        extensionHook.addOptionsParamSet(getScannerParam());
        extensionHook.addCommandLine(getCommandLineArguments());


	}
	
	
	void startScan() {
        siteTree = getModel().getSession().getSiteTree();

	    if (startNode == null) {
	        startNode = (SiteNode) siteTree.getRoot();
	    }
	    
        startScan(startNode);
	}
	
	void startScan(SiteNode startNode) {

	    scanner = new Scanner(getScannerParam(), getModel().getOptionsParam().getConnectionParam());
	    scanner.addScannerListener(this);

	    if (getView() != null) {
	        getProgressDialog().setVisible(true);
		    getProgressDialog().setPluginScanner(this);
	        menuItemScanAll.setEnabled(false);
	        menuItemScan.setEnabled(false);
	        getMenuItemPolicy().setEnabled(false);
            getPopupMenuScanHistory().setEnabled(false);
            getAlertPanel().setTabFocus();

	    }
	    
	    try {
	        recordScan = getModel().getDb().getTableScan().insert(getModel().getSession().getSessionId(), getModel().getSession().getSessionName());
	    } catch (SQLException e) {
        	// ZAP: Print stack trace to Output tab
        	getView().getOutputPanel().append(e);
	    }
        startTime = System.currentTimeMillis();
	    scanner.start(startNode);
        
	}
	

	


    /**
     * @return Returns the startNode.
     */
    public SiteNode getStartNode() {
        return startNode;
    }

    
	/**
	 * This method initializes menuScanner	
	 * 	
	 * @return javax.swing.JMenu	
	 */    
//	private JMenu getMenuScanner() {
//		if (menuScanner == null) {
//			menuScanner = new JMenu();
//			menuScanner.setText("Scanner");
//			menuScanner.add(getMenuItemScanAll());
//			menuScanner.add(getMenuItemScan());
//			menuScanner.addSeparator();
//			menuScanner.add(getMenuItemPolicy());
//		}
//		return menuScanner;
//	}


    public void scannerComplete() {
	    try {
	        Thread.sleep(1000);
	    } catch (Exception e) {}

        final long scanTime = System.currentTimeMillis() - startTime;
        
	    if (getView() != null) {
	        getMenuItemScanAll().setEnabled(true);
	        getMenuItemScan().setEnabled(true);
	        getMenuItemPolicy().setEnabled(true);
            popupMenuScanHistory.setEnabled(true);

	    }

	    if (getView() != null && progressDialog != null) {
	        if (EventQueue.isDispatchThread()) {
	            progressDialog.dispose();
                progressDialog = null;
                getView().showMessageDialog(MessageFormat.format(Constant.messages.getString("scanner.completed.label"), 
                		new Object []{ Long.valueOf(scanTime/1000)}));	// ZAP: i18n
	            return;
	        }
	        try {
	            EventQueue.invokeAndWait(new Runnable() {
	                public void run() {
	                    progressDialog.dispose();
	                    progressDialog = null;
	                    getView().showMessageDialog(MessageFormat.format(Constant.messages.getString("scanner.completed.label"), 
	                    		new Object []{ Long.valueOf(scanTime/1000)}));	// ZAP: i18n
	                }
	            });
	        } catch (Exception e) {
            	// ZAP: Print stack trace to Output tab
            	getView().getOutputPanel().append(e);
	        }
	    }

    }
	
	/**
	 * This method initializes menuItemPolicy	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */    
	private JMenuItem getMenuItemPolicy() {
		if (menuItemPolicy == null) {
			menuItemPolicy = new JMenuItem();
			menuItemPolicy.setText(Constant.messages.getString("menu.analyse.scanPolicy"));	// ZAP: i18n
			menuItemPolicy.addActionListener(new java.awt.event.ActionListener() { 

				public void actionPerformed(java.awt.event.ActionEvent e) {    

					PolicyDialog dialog = new PolicyDialog(getView().getMainFrame());
				    dialog.initParam(getModel().getOptionsParam());
					int result = dialog.showDialog(false);
					if (result == JOptionPane.OK_OPTION) {
					    try {
			                getModel().getOptionsParam().getConfig().save();
			            } catch (ConfigurationException ce) {
			                ce.printStackTrace();
			                getView().showWarningDialog(Constant.messages.getString("scanner.save.warning"));	// ZAP: i18n
			                return;
			            }
					}					
				}
			});

		}
		return menuItemPolicy;
	}

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.ScannerListener#ScannerProgress(java.lang.String, com.proofsecure.paros.network.HttpMessage, int)
     */
    public void hostProgress(String hostAndPort, String msg, int percentage) {
        if (getView() != null) {
            getProgressDialog().updateHostProgress(hostAndPort, msg, percentage);
        }
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.ScannerListener#HostComplete(java.lang.String)
     */
    public void hostComplete(String hostAndPort) {
        if (getView() != null) {
            getProgressDialog().removeHostProgress(hostAndPort);
        }
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.ScannerListener#hostNewScan(java.lang.String)
     */
    public void hostNewScan(String hostAndPort, HostProcess hostThread) {
        if (getView() != null) {
            getProgressDialog().addHostProgress(hostAndPort, hostThread);
        }
    }
    
    public void alertFound(Alert alert) {

        try {
            writeAlertToDB(alert);
            addAlertToDisplay(alert);
        } catch (Exception e) {
        	// ZAP: Print stack trace to Output tab
        	getView().getOutputPanel().append(e);
        }
    }

    private void addAlertToDisplay(Alert alert) {

        treeAlert.addPath(alert);
        if (getView() != null) {
            getAlertPanel().expandRoot();
        }
        
    }
	/**
	 * This method initializes progressDialog	
	 * 	
	 * @return com.proofsecure.paros.extension.scanner.ProgressDialog	
	 */    
	private ProgressDialog getProgressDialog() {
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(getView().getMainFrame(), false);
			progressDialog.setSize(500, 460);
		}
		return progressDialog;
	}
	
	
    /**
     * @return Returns the scanner.
     */
    public Scanner getScanner() {
        return scanner;
    }
	/**
	 * This method initializes menuItemScan	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */    
	private JMenuItem getMenuItemScan() {
		if (menuItemScan == null) {
			menuItemScan = new JMenuItem();
			menuItemScan.setText(Constant.messages.getString("menu.analyse.scan"));	// ZAP: i18n
			menuItemScan.addActionListener(new java.awt.event.ActionListener() { 

				public void actionPerformed(java.awt.event.ActionEvent e) {    

				    JTree siteTree = getView().getSiteTreePanel().getTreeSite();
		            SiteNode node = (SiteNode) siteTree.getLastSelectedPathComponent();
		            if (node == null) {
		                getView().showWarningDialog(Constant.messages.getString("scanner.select.warning"));	// ZAP: i18n
		                return;
		            }
				    menuItemScan.setEnabled(false);
				    menuItemScanAll.setEnabled(false);
	                startScan(node);
	                
				}
			});

		}
		return menuItemScan;
	}
	/**
	 * This method initializes alertPanel	
	 * 	
	 * @return com.proofsecure.paros.extension.scanner.AlertPanel	
	 */    
	AlertPanel getAlertPanel() {
		if (alertPanel == null) {
			alertPanel = new AlertPanel();
			alertPanel.setView(getView());
			alertPanel.setSize(345, 122);
			alertPanel.getTreeAlert().setModel(getTreeModel());
		}
		
		return alertPanel;
	}
	
	// ZAP: Changed return type for getTreeModel
	private AlertTreeModel getTreeModel() {
	    if (treeAlert == null) {
	        treeAlert = new AlertTreeModel();
	    }
	    return treeAlert;
	}
	
	private void writeAlertToDB(Alert alert) throws HttpMalformedHeaderException, SQLException {

	    TableAlert tableAlert = getModel().getDb().getTableAlert();
        HistoryReference ref = new HistoryReference(getModel().getSession(), HistoryReference.TYPE_SCANNER, alert.getMessage());
        // ZAP: cope with recordScan being null
        int scanId = 0;
        if (recordScan != null) {
        	scanId = recordScan.getScanId();
        }
        RecordAlert recordAlert = tableAlert.write(
                scanId, alert.getPluginId(), alert.getAlert(), alert.getRisk(), alert.getReliability(),
                alert.getDescription(), alert.getUri(), alert.getParam(), alert.getOtherInfo(), alert.getSolution(), alert.getReference(),
        		ref.getHistoryId(), alert.getSourceHistoryId()
                );
        
        alert.setAlertId(recordAlert.getAlertId());
        
	}

    // ZAP: Added updateAlertInDB
	public void updateAlertInDB(Alert alert) throws HttpMalformedHeaderException, SQLException {

	    TableAlert tableAlert = getModel().getDb().getTableAlert();
	    tableAlert.update(alert.getAlertId(), alert.getAlert(), alert.getRisk(), 
	    		alert.getReliability(), alert.getDescription(), alert.getUri(),
	    		alert.getParam(), alert.getOtherInfo(), alert.getSolution(), 
	    		alert.getReference(), alert.getSourceHistoryId());
	}
	
	// ZAP: Added displayAlert 
	public void displayAlert (Alert alert) {
		this.alertPanel.getAlertViewPanel().displayAlert(alert);
	}
	
	// ZAP: Added updateAlertInTree
	public void updateAlertInTree(Alert originalAlert, Alert alert) {
		this.getTreeModel().updatePath(originalAlert, alert);
	}

	public void sessionChanged(Session session) {
	    AlertTreeModel tree = (AlertTreeModel) getAlertPanel().getTreeAlert().getModel();

	    DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getRoot();
	    
        while (root.getChildCount() > 0) {
            tree.removeNodeFromParent((MutableTreeNode) root.getChildAt(0));
        }
        // ZAP: Reset the alert counts
	    tree.resetAlertCounts();
	    
	    try {
            refreshAlert(session);
            // ZAP: this prevent the UI getting corruted
            tree.nodeStructureChanged(root);
        } catch (SQLException e) {
        	// ZAP: Print stack trace to Output tab
        	getView().getOutputPanel().append(e);
        }
	}
	
	private void refreshAlert(Session session) throws SQLException {

	    TableAlert tableAlert = getModel().getDb().getTableAlert();
	    Vector v = tableAlert.getAlertListBySession(session.getSessionId());
	    
	    for (int i=0; i<v.size(); i++) {
	        int alertId = ((Integer) v.get(i)).intValue();
	        RecordAlert recAlert = tableAlert.read(alertId);
	        Alert alert = new Alert(recAlert);
	        addAlertToDisplay(alert);
	    }
	}

	/**
	 * This method initializes manualRequestEditorDialog	
	 * 	
	 * @return org.parosproxy.paros.extension.history.ManualRequestEditorDialog	
	 */    
	ManualRequestEditorDialog getManualRequestEditorDialog() {
		if (manualRequestEditorDialog == null) {
			manualRequestEditorDialog = new ManualRequestEditorDialog(getView().getMainFrame(), false, false, this);
			manualRequestEditorDialog.setTitle(Constant.messages.getString("manReq.resend.popup"));	// ZAP: i18n
			manualRequestEditorDialog.setSize(500, 600);
		}
		return manualRequestEditorDialog;
	}
	/**
	 * This method initializes popupMenuResend	
	 * 	
	 * @return org.parosproxy.paros.extension.scanner.PopupMenuResend	
	 */    
	private PopupMenuResend getPopupMenuResend() {
		if (popupMenuResend == null) {
			popupMenuResend = new PopupMenuResend();
			popupMenuResend.setExtension(this);
		}
		return popupMenuResend;
	}
	
	// ZAP: Added popup menu alert edit
	private PopupMenuAlertEdit getPopupMenuAlertEdit() {
		if (popupMenuAlertEdit == null) {
			popupMenuAlertEdit = new PopupMenuAlertEdit();
			popupMenuAlertEdit.setExtension(this);
		}
		return popupMenuAlertEdit;
	}

	/**
	 * This method initializes optionsScannerPanel	
	 * 	
	 * @return org.parosproxy.paros.extension.scanner.OptionsScannerPanel	
	 */    
	private OptionsScannerPanel getOptionsScannerPanel() {
		if (optionsScannerPanel == null) {
			optionsScannerPanel = new OptionsScannerPanel();
		}
		return optionsScannerPanel;
	}
	/**
	 * This method initializes scannerParam	
	 * 	
	 * @return org.parosproxy.paros.core.scanner.ScannerParam	
	 */    
	private ScannerParam getScannerParam() {
		if (scannerParam == null) {
			scannerParam = new ScannerParam();
		}
		return scannerParam;
	}
	
    /* (non-Javadoc)
     * @see org.parosproxy.paros.extension.CommandLineListener#execute(org.parosproxy.paros.extension.CommandLineArgument[])
     */
    public void execute(CommandLineArgument[] args) {

        if (arguments[ARG_SCAN_IDX].isEnabled()) {
            System.out.println("Scanner started...");
            startScan();
        } else {
            return;
        }

        while (!getScanner().isStop()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
        System.out.println("Scanner completed.");

    }

    private CommandLineArgument[] getCommandLineArguments() {
        arguments[ARG_SCAN_IDX] = new CommandLineArgument("-scan", 0, null, "", "-scan : Run vulnerability scan depending on previously saved policy.");
        return arguments;
    }

    /**
     * This method initializes popupMenuScanHistory	
     * 	
     * @return org.parosproxy.paros.extension.scanner.PopupMenuScanHistory	
     */
    private PopupMenuScanHistory getPopupMenuScanHistory() {
        if (popupMenuScanHistory == null) {
            popupMenuScanHistory = new PopupMenuScanHistory();
            popupMenuScanHistory.setExtension(this);
        }
        return popupMenuScanHistory;
    }

	@Override
	public void notifyNewMessage(HttpMessage msg) {
		// Ignore
	}
	
}
