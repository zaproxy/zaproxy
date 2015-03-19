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
// ZAP: 2012/03/03 Moved popups to stdmenus extension
// ZAP: 2012/04/23 Added @Override annotation to all appropriate methods.
// ZAP: 2012/10/17 Issue 393: Added more online links from menu
// ZAP: 2013/01/23 Clean up of exception handling/logging.
// ZAP: 2013/03/03 Issue 547: Deprecate unused classes and methods
// ZAP: 2013/04/16 Issue 638: Persist and snapshot sessions instead of saving them
// ZAP: 2013/09/11 Issue 786: Snapshot session menu item not working
// ZAP: 2014/01/28 Issue 207: Support keyboard shortcuts 
// ZAP: 2014/11/11 Issue 1406: Move online menu items to an add-on
// ZAP: 2014/12/22 Issue 1476: Display contexts in the Sites tree
// ZAP: 2015/02/05 Issue 1524: New Persist Session dialog

package org.parosproxy.paros.view;

import java.awt.Event;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.control.MenuFileControl;
import org.parosproxy.paros.control.MenuToolsControl;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.zaproxy.zap.view.AboutDialog;
import org.zaproxy.zap.view.ZapMenuItem;

public class MainMenuBar extends JMenuBar {

	private static final long serialVersionUID = 8580116506279095244L;
	
	private static final Logger logger = Logger.getLogger(MainMenuBar.class);

	private javax.swing.JMenu menuEdit = null;
	private javax.swing.JMenu menuTools = null;
	private javax.swing.JMenu menuView = null;
	private ZapMenuItem menuToolsOptions = null;
	private javax.swing.JMenu menuFile = null;
	private ZapMenuItem menuFileNewSession = null;
	private ZapMenuItem menuFileOpen = null;
	private ZapMenuItem menuFileSaveAs = null;
	private ZapMenuItem menuFileSnapshot = null;
	private ZapMenuItem menuFileContextExport = null;
	private ZapMenuItem menuFileContextImport = null;
	private ZapMenuItem menuFileExit = null;
	private ZapMenuItem menuFileExitAndDelete = null;
	private ZapMenuItem menuFileProperties = null;
	private JMenu menuHelp = null;
	private ZapMenuItem menuHelpAbout = null;
    private JMenu menuAnalyse = null;
    // ZAP: Added standard report menu
	private JMenu menuReport = null;
	private JMenu menuOnline = null;
	/**
	 * This method initializes 
	 * 
	 */
	public MainMenuBar() {
		super();
		initialize();
	}
	/**
	 * This method initializes this
	 */
	private void initialize() {
        this.add(getMenuFile());
        this.add(getMenuEdit());
        this.add(getMenuView());
        this.add(getMenuAnalyse());
        this.add(getMenuReport());
        this.add(getMenuTools());
        this.add(getMenuOnline());
        this.add(getMenuHelp());
			
	}
	/**

	 * This method initializes menuEdit	

	 * 	

	 * @return javax.swing.JMenu	

	 */    
	public javax.swing.JMenu getMenuEdit() {
		if (menuEdit == null) {
			menuEdit = new javax.swing.JMenu();
			menuEdit.setText(Constant.messages.getString("menu.edit")); // ZAP: i18n
			menuEdit.setMnemonic(Constant.messages.getChar("menu.edit.mnemonic"));
		}
		return menuEdit;
	}

	/**

	 * This method initializes menuTools	

	 * 	

	 * @return javax.swing.JMenu	

	 */    
	public javax.swing.JMenu getMenuTools() {
		if (menuTools == null) {
			menuTools = new javax.swing.JMenu();
			menuTools.setText(Constant.messages.getString("menu.tools")); // ZAP: i18n
			menuTools.setMnemonic(Constant.messages.getChar("menu.tools.mnemonic"));
			menuTools.addSeparator();
			menuTools.add(getMenuToolsOptions());
		}
		return menuTools;
	}

	/**

	 * This method initializes menuView	

	 * 	

	 * @return javax.swing.JMenu	

	 */    
	public javax.swing.JMenu getMenuView() {
		if (menuView == null) {
			menuView = new javax.swing.JMenu();
			menuView.setText(Constant.messages.getString("menu.view")); // ZAP: i18n
			menuView.setMnemonic(Constant.messages.getChar("menu.view.mnemonic"));
		}
		return menuView;
	}

	/**

	 * This method initializes menuToolsOptions	

	 * 	

	 * @return javax.swing.JMenuItem	

	 */    
	private ZapMenuItem getMenuToolsOptions() {
		if (menuToolsOptions == null) {
			menuToolsOptions = new ZapMenuItem("menu.tools.options",
					KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | Event.ALT_MASK, false));
			menuToolsOptions.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {    

					getMenuToolsControl().options();
					
				}
			});

		}
		return menuToolsOptions;
	}

	/**

	 * This method initializes menuFile	

	 * 	

	 * @return javax.swing.JMenu	

	 */    
	public javax.swing.JMenu getMenuFile() {
		if (menuFile == null) {
			menuFile = new javax.swing.JMenu();
			menuFile.setText(Constant.messages.getString("menu.file")); // ZAP: i18n
			menuFile.setMnemonic(Constant.messages.getChar("menu.file.mnemonic"));
			menuFile.add(getMenuFileNewSession());
			menuFile.add(getMenuFileOpen());
			menuFile.addSeparator();
			menuFile.add(getMenuFileSaveAs());
			menuFile.add(getMenuFileSnapshot());
			menuFile.addSeparator();
			menuFile.add(getMenuFileProperties());
			
			menuFile.addSeparator();
			menuFile.add(getMenuContextImport());
			menuFile.add(getMenuContextExport());
			
			menuFile.addSeparator();
			menuFile.add(getMenuFileExitAndDelete());
			menuFile.add(getMenuFileExit());
		}
		return menuFile;
	}

	/**

	 * This method initializes menuFileNewSession	

	 * 	

	 * @return javax.swing.JMenuItem	

	 */    
	private javax.swing.JMenuItem getMenuFileNewSession() {
		if (menuFileNewSession == null) {
			menuFileNewSession = new ZapMenuItem("menu.file.newSession",
					KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
			menuFileNewSession.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					try {
                        getMenuFileControl().newSession(true);
                    } catch (Exception e1) {
                        View.getSingleton().showWarningDialog(Constant.messages.getString("menu.file.newSession.error")); // ZAP: i18n
                        logger.error(e1.getMessage(), e1);
                    }
				}
			});
		}
		return menuFileNewSession;
	}

	/**

	 * This method initializes menuFileOpen	

	 * 	

	 * @return javax.swing.JMenuItem	

	 */    
	private javax.swing.JMenuItem getMenuFileOpen() {
		if (menuFileOpen == null) {
			menuFileOpen = new ZapMenuItem("menu.file.openSession",
					KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
			menuFileOpen.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					getMenuFileControl().openSession();


				}
			});
		}
		return menuFileOpen;
	}

	/**

	 * This method initializes menuFileSaveAs	

	 * 	

	 * @return javax.swing.JMenuItem	

	 */    
	private javax.swing.JMenuItem getMenuFileSaveAs() {
		if (menuFileSaveAs == null) {
			menuFileSaveAs = new ZapMenuItem("menu.file.persistSession");
			menuFileSaveAs.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					if (Model.getSingleton().getSession().isNewState()) {
					    getMenuFileControl().saveAsSession();
					} else {
						View.getSingleton().showWarningDialog(Constant.messages.getString("menu.file.sessionExists.error"));
					}
				}
			});

		}
		return menuFileSaveAs;
	}

	private javax.swing.JMenuItem getMenuFileSnapshot() {
		if (menuFileSnapshot == null) {
			menuFileSnapshot = new ZapMenuItem("menu.file.snapshotSession");
			menuFileSnapshot.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					if (! Model.getSingleton().getSession().isNewState()) {
					    getMenuFileControl().saveSnapshot();
					} else {
						View.getSingleton().showWarningDialog(Constant.messages.getString("menu.file.snapshotSession.error"));
					}
				}
			});

		}
		return menuFileSnapshot;
	}

	/**

	 * This method initializes menuFileExit	

	 * 	

	 * @return javax.swing.JMenuItem	

	 */    
	private javax.swing.JMenuItem getMenuFileExit() {
		if (menuFileExit == null) {
			menuFileExit = new ZapMenuItem("menu.file.exit");
			menuFileExit.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					getMenuFileControl().exit();
				}
			});

		}
		return menuFileExit;
	}

	private javax.swing.JMenuItem getMenuFileExitAndDelete() {
		if (menuFileExitAndDelete == null) {
			menuFileExitAndDelete = new ZapMenuItem("menu.file.exit.delete");
			menuFileExitAndDelete.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					int ans = View.getSingleton().showConfirmDialog(Constant.messages.getString("menu.file.exit.delete.warning"));
					if (ans == JOptionPane.OK_OPTION) {
						Control.getSingleton().exitAndDeleteSession(Model.getSingleton().getSession().getFileName());
					}
				}
			});
		}
		return menuFileExitAndDelete;
	}

	/**
	 * This method initializes menuFileControl	
	 * 	
	 * @return org.parosproxy.paros.view.MenuFileControl	
	 */    
	public MenuFileControl getMenuFileControl() {
		return Control.getSingleton().getMenuFileControl();
	}

	/**
	 * This method initializes menuToolsControl	
	 * 	
	 * @return org.parosproxy.paros.view.MenuToolsControl	
	 */    
	private MenuToolsControl getMenuToolsControl() {
		return Control.getSingleton().getMenuToolsControl();
	}
	/**
	 * This method initializes menuFileProperties	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */    
	private ZapMenuItem getMenuFileProperties() {
		if (menuFileProperties == null) {
			menuFileProperties = new ZapMenuItem("menu.file.properties",
					KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | Event.ALT_MASK, false));
			menuFileProperties.setText(Constant.messages.getString("menu.file.properties")); // ZAP: i18n
			menuFileProperties.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
				    getMenuFileControl().properties();
				}
			});

		}
		return menuFileProperties;
	}

	private ZapMenuItem getMenuContextImport() {
		if (menuFileContextImport == null) {
			menuFileContextImport = new ZapMenuItem("menu.file.context.import");
			menuFileContextImport.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					getMenuFileControl().importContext();
				}
			});

		}
		return menuFileContextImport;
	}

	private ZapMenuItem getMenuContextExport() {
		if (menuFileContextExport == null) {
			menuFileContextExport = new ZapMenuItem("menu.file.context.export");
			menuFileContextExport.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(ActionEvent e) {
					getMenuFileControl().exportContext();
			}});
		}
		return menuFileContextExport;
	}

	/**
	 * This method initializes menuHelp	
	 * 	
	 * @return javax.swing.JMenu	
	 */    
	public JMenu getMenuHelp() {
		if (menuHelp == null) {
			menuHelp = new JMenu();
			menuHelp.setText(Constant.messages.getString("menu.help")); // ZAP: i18n
			menuHelp.setMnemonic(Constant.messages.getChar("menu.help.mnemonic"));
			menuHelp.add(getMenuHelpAbout());
		}
		return menuHelp;
	}
	
	public JMenu getMenuOnline() {
		if (menuOnline == null) {
			menuOnline = new JMenu();
			menuOnline.setText(Constant.messages.getString("menu.online"));
			menuOnline.setMnemonic(Constant.messages.getChar("menu.online.mnemonic"));
		}
		return menuOnline;
	}
	
    // ZAP: Added standard report menu
	public JMenu getMenuReport() {
		if (menuReport == null) {
			menuReport = new JMenu();
			menuReport.setText(Constant.messages.getString("menu.report")); // ZAP: i18n
			menuReport.setMnemonic(Constant.messages.getChar("menu.report.mnemonic"));
		}
		return menuReport;
	}
	/**
	 * This method initializes menuHelpAbout	
	 * 	
	 * @return javax.swing.ZapMenuItem	
	 */    
	private ZapMenuItem getMenuHelpAbout() {
		if (menuHelpAbout == null) {
			menuHelpAbout = new ZapMenuItem("menu.help.about");
			menuHelpAbout.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					AboutDialog dialog = new AboutDialog(View.getSingleton().getMainFrame(), true);
					dialog.setVisible(true);
				}
			});

		}
		return menuHelpAbout;
	}
    /**
     * This method initializes jMenu1	
     * 	
     * @return javax.swing.JMenu	
     */
    public JMenu getMenuAnalyse() {
        if (menuAnalyse == null) {
            menuAnalyse = new JMenu();
            menuAnalyse.setText(Constant.messages.getString("menu.analyse")); // ZAP: i18n
			menuAnalyse.setMnemonic(Constant.messages.getChar("menu.analyse.mnemonic"));
        }
        return menuAnalyse;
    }
    
	public void sessionChanged(Session session) {
		if (session != null) {
			this.getMenuFileSaveAs().setEnabled(session.isNewState());
			this.getMenuFileSnapshot().setEnabled(!session.isNewState());
		}
	}
}
