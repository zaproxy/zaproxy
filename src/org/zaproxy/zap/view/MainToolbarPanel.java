/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
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

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ToolTipManager;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.utils.DisplayUtils;

public class MainToolbarPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = Logger.getLogger(MainToolbarPanel.class);
	
	private JToolBar toolbar = null;
 	private JComboBox<String> modeSelect = null;
	private JButton btnNew = null;
	private JButton btnOpen = null;
	private JButton btnSave = null;
	private JButton btnSnapshot = null;
	private JButton btnSession = null;
	private JButton btnOptions = null;

	private ButtonGroup expandButtons;
	private JToggleButton btnExpandSites = null;
	private JToggleButton btnExpandReports = null;
	private JToggleButton btnExpandFull = null;

	private boolean showtabiconnames = false;
  	private ZapToggleButton btnShowTabIconNames = null;
  	private JButton btnShowAllTabs = null;
  	private JButton btnHideAllTabs = null;

	public MainToolbarPanel () {
		super();
		initialise();
	}
	
	public void initialise () {
		setLayout(new java.awt.GridBagLayout());
		setPreferredSize(DisplayUtils.getScaledDimension(getMaximumSize().width, 25));
		setMaximumSize(DisplayUtils.getScaledDimension(getMaximumSize().width, 25));
		this.setBorder(BorderFactory.createEtchedBorder());

		expandButtons = new ButtonGroup();

		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints2 = new GridBagConstraints();

		gridBagConstraints1.gridx = 0;
		gridBagConstraints1.gridy = 0;
		gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
		
		gridBagConstraints2.gridx = 1;
		gridBagConstraints2.gridy = 0;
		gridBagConstraints2.weightx = 1.0;
		gridBagConstraints2.weighty = 1.0;
		gridBagConstraints2.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;

		JToolBar t1 = new JToolBar();
		t1.setEnabled(true);
		t1.setPreferredSize(new java.awt.Dimension(80000,25));
		t1.setMaximumSize(new java.awt.Dimension(80000,25));
		
		add(getToolbar(), gridBagConstraints1);
		add(t1, gridBagConstraints2);

		toolbar.add(getModeSelect());
		toolbar.add(getBtnNew());
		toolbar.add(getBtnOpen());
		toolbar.add(getBtnSave());
		toolbar.add(getBtnSnapshot());
		toolbar.add(getBtnSession());
		toolbar.add(getBtnOptions());
		
		toolbar.addSeparator();
		toolbar.add(getShowAllTabs());
		toolbar.add(getHideAllTabs());
		toolbar.add(getShowTabIconNames());
		toolbar.addSeparator();

		toolbar.add(getBtnExpandSites());
		toolbar.add(getBtnExpandReports());
		toolbar.add(getBtnExpandFull());
		
		toolbar.addSeparator();
	}
	
	private JToolBar getToolbar() {
		if (toolbar == null) {
			toolbar = new JToolBar();
			toolbar.setEnabled(true);
			toolbar.setFloatable(false);
			toolbar.setRollover(true);
			toolbar.setName("Main Toolbar");
			toolbar.setBorder(BorderFactory.createEmptyBorder());
		}
		return toolbar;
	}
	
	public void addButton (JButton button) {
		DisplayUtils.scaleIcon(button);
		getToolbar().add(button);
	}

	public void removeButton(JButton button) {
		getToolbar().remove(button);
	}

	public void addButton(JToggleButton button) {
		DisplayUtils.scaleIcon(button);
		getToolbar().add(button);
	}

	public void removeButton(JToggleButton button) {
		getToolbar().remove(button);
	}

	public void addSeparator() {
		getToolbar().addSeparator();
	}

	public void addSeparator(JToolBar.Separator separator) {
		getToolbar().add(separator);
	}

	public void removeSeparator(JToolBar.Separator separator) {
		getToolbar().remove(separator);
	}
	
	private JComboBox<String> getModeSelect() {
		if (modeSelect == null) {
			modeSelect = new JComboBox<>();
			modeSelect.addItem(Constant.messages.getString("view.toolbar.mode.safe.select"));
			modeSelect.addItem(Constant.messages.getString("view.toolbar.mode.protect.select"));
			modeSelect.addItem(Constant.messages.getString("view.toolbar.mode.standard.select"));
			modeSelect.addItem(Constant.messages.getString("view.toolbar.mode.attack.select"));
			
			modeSelect.setToolTipText(Constant.messages.getString("view.toolbar.mode.tooltip"));
			// Increase the time the tooltip is displayed, to give people a chance to read it!
			ToolTipManager.sharedInstance().setDismissDelay(12000);
			ToolTipManager.sharedInstance().registerComponent(modeSelect);
			
			// Control wont have finished initialising yet, so get from the params
			Mode mode = Mode.valueOf(Model.getSingleton().getOptionsParam().getViewParam().getMode());
			switch (mode) {
				case safe:		modeSelect.setSelectedIndex(0); break;
				case protect:	modeSelect.setSelectedIndex(1); break;
				case standard:	modeSelect.setSelectedIndex(2); break;
				case attack:	modeSelect.setSelectedIndex(3); break;
			}

			modeSelect.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Mode mode = null;
					switch(modeSelect.getSelectedIndex()) {
						case 0:	mode = Mode.safe;	break;
						case 1:	mode = Mode.protect;	break;
						case 2:	mode = Mode.standard;	break;
						case 3: mode = Mode.attack;	break;
						default: return;	// Not recognised
					}
					Control.getSingleton().setMode(mode);
				}
			});
		}
		return modeSelect;
	}
	
	public void setMode (Mode mode) {
		this.getModeSelect().setSelectedItem(Constant.messages.getString("view.toolbar.mode." + mode.name() + ".select"));
	}

	private JButton getBtnNew() {
		if (btnNew == null) {
			btnNew = new JButton();
			btnNew.setIcon(DisplayUtils.getScaledIcon(
					new ImageIcon(MainToolbarPanel.class.getResource("/resource/icon/16/021.png"))));	// 'Blank file' icon
			btnNew.setToolTipText(Constant.messages.getString("menu.file.newSession"));

			btnNew.addActionListener(new java.awt.event.ActionListener() { 

				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					try {
						Control.getSingleton().getMenuFileControl().newSession(true);
					} catch (Exception ex) {
						logger.error(ex.getMessage(), ex);
						View.getSingleton().showWarningDialog(Constant.messages.getString("menu.file.newSession.error"));
					}
				}
			});
		}
		return btnNew;
	}

	private JButton getBtnOpen() {
		if (btnOpen == null) {
			btnOpen = new JButton();
			btnOpen.setIcon(DisplayUtils.getScaledIcon(
					new ImageIcon(MainToolbarPanel.class.getResource("/resource/icon/16/047.png"))));	// 'open folder' icon
			btnOpen.setToolTipText(Constant.messages.getString("menu.file.openSession"));

			btnOpen.addActionListener(new java.awt.event.ActionListener() { 

				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					try {
						Control.getSingleton().getMenuFileControl().openSession();
					} catch (Exception ex) {
						logger.error(ex.getMessage(), ex);
						View.getSingleton().showWarningDialog(Constant.messages.getString("menu.file.openSession.error"));
					}
				}
			});
		}
		return btnOpen;
	}

	private JButton getBtnSave() {
		if (btnSave == null) {
			btnSave = new JButton();
			btnSave.setIcon(DisplayUtils.getScaledIcon(
					new ImageIcon(MainToolbarPanel.class.getResource("/resource/icon/16/096.png"))));	// 'diskette' icon
			btnSave.setToolTipText(Constant.messages.getString("menu.file.persistSession"));

			btnSave.addActionListener(new java.awt.event.ActionListener() { 

				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					try {
						if (Model.getSingleton().getSession().isNewState()) {
							Control.getSingleton().getMenuFileControl().saveAsSession();
						} else {
							View.getSingleton().showWarningDialog(Constant.messages.getString("menu.file.sessionExists.error"));
						}
					} catch (Exception ex) {
						logger.error(ex.getMessage(), ex);
						View.getSingleton().showWarningDialog(Constant.messages.getString("menu.file.persistSession.error"));
					}
				}
			});
		}
		return btnSave;
	}

	private JButton getBtnSnapshot() {
		if (btnSnapshot == null) {
			btnSnapshot = new JButton();
			btnSnapshot.setIcon(DisplayUtils.getScaledIcon(
					new ImageIcon(MainToolbarPanel.class.getResource("/resource/icon/fugue/camera.png"))));
			btnSnapshot.setToolTipText(Constant.messages.getString("menu.file.snapshotSession"));

			btnSnapshot.addActionListener(new java.awt.event.ActionListener() { 

				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					try {
						if (Model.getSingleton().getSession().isNewState()) {
							View.getSingleton().showWarningDialog(Constant.messages.getString("menu.file.sessionNotExist.error"));
						} else {
							Control.getSingleton().getMenuFileControl().saveSnapshot();
						}
					} catch (Exception ex) {
						logger.error(ex.getMessage(), ex);
						View.getSingleton().showWarningDialog(Constant.messages.getString("menu.file.persistSession.error"));
					}
				}
			});
		}
		return btnSnapshot;
	}

	private JButton getBtnSession() {
		if (btnSession == null) {
			btnSession = new JButton();
			btnSession.setIcon(DisplayUtils.getScaledIcon(
					new ImageIcon(MainToolbarPanel.class.getResource("/resource/icon/16/024.png"))));	// 'spreadsheet' icon
			btnSession.setToolTipText(Constant.messages.getString("menu.file.sessionProperties"));

			btnSession.addActionListener(new java.awt.event.ActionListener() { 

				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Control.getSingleton().getMenuFileControl().properties();
				}
			});
		}
		return btnSession;
	}

	private JButton getBtnOptions() {
		if (btnOptions == null) {
			btnOptions = new JButton();
			btnOptions.setToolTipText(Constant.messages.getString("menu.tools.options"));
			btnOptions.setIcon(DisplayUtils.getScaledIcon(
					new ImageIcon(MainToolbarPanel.class.getResource("/resource/icon/16/041.png"))));
			btnOptions.addActionListener(new ActionListener () {
				@Override
				public void actionPerformed(ActionEvent e) {
					Control.getSingleton().getMenuToolsControl().options();
				}
			});
		}
		return btnOptions;
	}

	private JToggleButton getBtnExpandSites() {
		if (btnExpandSites == null) {
			btnExpandSites = new JToggleButton(new ChangeDisplayOptionAction(
					MainToolbarPanel.class.getResource("/resource/icon/expand_sites.png"),
					View.DISPLAY_OPTION_LEFT_FULL));
			btnExpandSites.setToolTipText(Constant.messages.getString("view.toolbar.expandSites"));
			
			expandButtons.add(btnExpandSites);
		}
		return btnExpandSites;
	}

	private JToggleButton getBtnExpandReports() {
		if (btnExpandReports == null) {
			btnExpandReports = new JToggleButton(new ChangeDisplayOptionAction(
					MainToolbarPanel.class.getResource("/resource/icon/expand_info.png"),
					View.DISPLAY_OPTION_BOTTOM_FULL));
			btnExpandReports.setToolTipText(Constant.messages.getString("view.toolbar.expandInfo"));

			expandButtons.add(btnExpandReports);
		}
		return btnExpandReports;
	}

	private JToggleButton getBtnExpandFull() {
		if (btnExpandFull == null) {
			btnExpandFull = new JToggleButton(new ChangeDisplayOptionAction(
					MainToolbarPanel.class.getResource("/resource/icon/expand_full.png"),
					View.DISPLAY_OPTION_TOP_FULL));
			btnExpandFull.setToolTipText(Constant.messages.getString("view.toolbar.expandFull"));

			expandButtons.add(btnExpandFull);
		}
		return btnExpandFull;
	}


	private void setShowTabIconNames(boolean showtabiconnames) {
		this.showtabiconnames = showtabiconnames; 
		btnShowTabIconNames.setSelected(showtabiconnames);
	}

	/*
	 * Button for showing/hiding names and icons in tabs.
	 */
	private JToggleButton getShowTabIconNames() {
		if (btnShowTabIconNames == null) {
			btnShowTabIconNames = new ZapToggleButton();
			btnShowTabIconNames.setIcon(new ImageIcon(MainToolbarPanel.class.getResource("/resource/icon/ui_tab_icon.png")));
			btnShowTabIconNames.setToolTipText(Constant.messages.getString("view.toolbar.showNames"));
			btnShowTabIconNames.setSelectedIcon(new ImageIcon(MainToolbarPanel.class.getResource("/resource/icon/ui_tab_text.png")));
			btnShowTabIconNames.setSelectedToolTipText(Constant.messages.getString("view.toolbar.showIcons"));
		  	setShowTabIconNames(Model.getSingleton().getOptionsParam().getViewParam().getShowTabNames());
			DisplayUtils.scaleIcon(btnShowTabIconNames);

			btnShowTabIconNames.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
				    setShowTabIconNames(getShowTabIconNames().isSelected());
					Model.getSingleton().getOptionsParam().getViewParam().setShowTabNames(showtabiconnames);
					try {
						Model.getSingleton().getOptionsParam().getViewParam().getConfig().save();
					} catch (ConfigurationException e1) {
						logger.error(e1.getMessage(), e1);
					}
				}
			});
		}
		return btnShowTabIconNames;
	}
	
	private JButton getShowAllTabs() {
		if (btnShowAllTabs == null) {
			btnShowAllTabs = new JButton();
			btnShowAllTabs.setIcon(new ImageIcon(MainToolbarPanel.class.getResource("/resource/icon/fugue/ui-tab-show.png")));
			btnShowAllTabs.setToolTipText(Constant.messages.getString("menu.view.tabs.show"));
			DisplayUtils.scaleIcon(btnShowAllTabs);

			btnShowAllTabs.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					View.getSingleton().showAllTabs();
				}
			});
		}
		return btnShowAllTabs;
	}

	private JButton getHideAllTabs() {
		if (btnHideAllTabs == null) {
			btnHideAllTabs = new JButton();
			btnHideAllTabs.setIcon(new ImageIcon(MainToolbarPanel.class.getResource("/resource/icon/fugue/ui-tab-hide.png")));
			btnHideAllTabs.setToolTipText(Constant.messages.getString("menu.view.tabs.hide"));
			DisplayUtils.scaleIcon(btnHideAllTabs);
			
			btnHideAllTabs.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					View.getSingleton().hideAllTabs();
				}
			});
		}
		return btnHideAllTabs;
	}

	public void setDisplayOption(int option) {
		if (option == View.DISPLAY_OPTION_BOTTOM_FULL) {
			btnExpandReports.setSelected(true);
    	} else if (option == View.DISPLAY_OPTION_LEFT_FULL) {
      		btnExpandSites.setSelected(true);
    	} else if (option == View.DISPLAY_OPTION_TOP_FULL) {
      		btnExpandFull.setSelected(true);
    	}
	}

	public void sessionChanged(Session session) {
		if (session != null) {
			this.getBtnSave().setEnabled(session.isNewState());
			this.getBtnSnapshot().setEnabled(!session.isNewState());
		}
	}

    private static class ChangeDisplayOptionAction extends AbstractAction {

        private static final long serialVersionUID = 8323387638733162321L;

        private final int displayOption;

        public ChangeDisplayOptionAction(URL iconURL, int displayOption) {
            super("", DisplayUtils.getScaledIcon(new ImageIcon(iconURL)));

            this.displayOption = displayOption;
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            if (Model.getSingleton().getOptionsParam().getViewParam().getDisplayOption() != displayOption) {
                View.getSingleton().getMainFrame().changeDisplayOption(displayOption);
                try {
                    Model.getSingleton().getOptionsParam().getConfig().save();
                } catch (ConfigurationException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

}
