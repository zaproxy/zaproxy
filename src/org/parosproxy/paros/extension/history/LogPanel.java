/*
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
// ZAP: 2012/01/12 Changed the method valueChanged of the ListSelectionListener.
// ZAP: 2012/03/15 Changed to allow clear the displayQueue. 
// ZAP: 2012/04/25 Added @Override annotation to all appropriate methods.
// ZAP: 2012/04/28 Added logger and log of exception.
// ZAP: 2012/05/02 Changed to use the class literal, instead of getting the
// class at runtime, to get the resource.
// ZAP: 2012/07/29 Issue 43: added Scope support
// ZAP: 2013/02/26 Issue 538: Allow non sequential lines to be selected in the history log
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2013/03/03 Issue 547: Deprecate unused classes and methods
// ZAP: 2013/11/16 Issue 898: Replace all toggle buttons that set a tool tip text based on button's state with ZapToggleButton
// ZAP: 2013/11/16 Issue 899: Remove "manual" update of toggle buttons' icon based on button's state
// ZAP: 2013/11/16 Issue 886: Main pop up menu invoked twice on some components
// ZAP: 2013/12/02 Issue 915: Dynamically filter history based on selection in the sites window
// ZAP: 2014/01/28 Issue 207: Support keyboard shortcuts 
// ZAP: 2014/02/07 Issue 207: Give the most suitable component focus on tab switch
// ZAP: 2014/03/23 Issue 609: Provide a common interface to query the state and 
// access the data (HttpMessage and HistoryReference) displayed in the tabs
// ZAP: 2014/03/23 Issue 503: Change the footer tabs to display the data
// with tables instead of lists
// ZAP: 2015/02/10 Issue 1528: Support user defined font size
// ZAP: 2016/04/14 Use View to display the HTTP messages

package org.parosproxy.paros.extension.history;

import java.awt.BorderLayout;
import java.awt.Event;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.extension.ViewDelegate;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.httppanel.HttpPanel;
import org.zaproxy.zap.utils.DisplayUtils;
import org.zaproxy.zap.view.DeselectableButtonGroup;
import org.zaproxy.zap.view.ZapToggleButton;
import org.zaproxy.zap.view.table.DefaultHistoryReferencesTableEntry;
import org.zaproxy.zap.view.table.HistoryReferencesTable;
import org.zaproxy.zap.view.table.HistoryReferencesTableModel;

public class LogPanel extends AbstractPanel implements Runnable {
	private static final long serialVersionUID = 1L;
	// ZAP: Added logger.
	private static final Logger logger = Logger.getLogger(LogPanel.class);
	private javax.swing.JScrollPane scrollLog = null;
	private HistoryReferencesTable historyReferencesTable = null;
	// ZAP: Added history (filter) toolbar
	private javax.swing.JPanel historyPanel = null;
	private javax.swing.JToolBar panelToolbar = null;
	private JButton filterButton = null;
	private JLabel filterStatus = null;
	private ZapToggleButton scopeButton = null;
	
    private ExtensionHistory extension = null;

	private ZapToggleButton linkWithSitesTreeButton;

	private LinkWithSitesTreeSelectionListener linkWithSitesTreeSelectionListener;

	private DeselectableButtonGroup historyListFiltersButtonGroup;

	private final ViewDelegate view;
	
	/**
	 * @deprecated (2.5.0) Use {@link #LogPanel(ViewDelegate)} instead.
	 */
	@Deprecated
	public LogPanel() {
		this(View.getSingleton());
	}

	public LogPanel(ViewDelegate view) {
		super();
		this.view = view;
		initialize();
	}

	/**
	 * This method initializes this
	 */
	private  void initialize() {
		historyListFiltersButtonGroup = new DeselectableButtonGroup();

		this.setLayout(new BorderLayout());
	    if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
	    	this.setSize(600, 200);
	    }
		this.add(getHistoryPanel(), java.awt.BorderLayout.CENTER);
		
		this.setDefaultAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | Event.SHIFT_MASK, false));
		this.setMnemonic(Constant.messages.getChar("history.panel.mnemonic"));
		
	}

	@Override
	public void tabSelected() {
		// Give the history list focus so that the user can immediatelly use the arrow keys to navigate
	    getHistoryReferenceTable().requestFocusInWindow();
	}

    void setExtension(ExtensionHistory extension) {
        this.extension = extension;
    }
    
	/**

	 * This method initializes scrollLog	

	 * 	

	 * @return javax.swing.JScrollPane	

	 */    
	private javax.swing.JScrollPane getScrollLog() {
		if (scrollLog == null) {
			scrollLog = new javax.swing.JScrollPane();
			scrollLog.setViewportView(getHistoryReferenceTable());
			scrollLog.setName("scrollLog");
		}
		return scrollLog;
	}

	private javax.swing.JPanel getHistoryPanel() {
		if (historyPanel == null) {

			historyPanel = new javax.swing.JPanel();
			historyPanel.setLayout(new java.awt.GridBagLayout());
			historyPanel.setName("History Panel");
			
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();

			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.gridy = 0;
			gridBagConstraints1.weightx = 1.0D;
			gridBagConstraints1.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
			
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.gridy = 1;
			gridBagConstraints2.weightx = 1.0;
			gridBagConstraints2.weighty = 1.0;
			gridBagConstraints2.insets = new java.awt.Insets(0,0,0,0);
			gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTHWEST;
			
			historyPanel.add(this.getPanelToolbar(), gridBagConstraints1);
			historyPanel.add(getScrollLog(), gridBagConstraints2);

		}
		return historyPanel;
	}

	private javax.swing.JToolBar getPanelToolbar() {
		if (panelToolbar == null) {
			
			panelToolbar = new javax.swing.JToolBar();
			panelToolbar.setLayout(new java.awt.GridBagLayout());
			panelToolbar.setEnabled(true);
			panelToolbar.setFloatable(false);
			panelToolbar.setRollover(true);
			panelToolbar.setPreferredSize(new java.awt.Dimension(800,30));
			panelToolbar.setName("History Toolbar");
			
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.insets = new java.awt.Insets(0,0,0,0);
			gbc.anchor = java.awt.GridBagConstraints.WEST;

			panelToolbar.add(getScopeButton(), gbc);

			++gbc.gridx;
			panelToolbar.add(getLinkWithSitesTreeButton(), gbc);

			++gbc.gridx;
			panelToolbar.add(getFilterButton(), gbc);

			filterStatus = new JLabel(Constant.messages.getString("history.filter.label.filter") + 
					Constant.messages.getString("history.filter.label.off"));

			++gbc.gridx;
			panelToolbar.add(filterStatus, gbc);

			++gbc.gridx;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
			gbc.anchor = java.awt.GridBagConstraints.EAST;
			gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
			panelToolbar.add(new JLabel(), gbc);
		}
		return panelToolbar;
	}

	private JButton getFilterButton() {
		if (filterButton == null) {
			filterButton = new JButton();
			// ZAP: Changed to use the class literal.
			filterButton.setIcon(new ImageIcon(LogPanel.class.getResource("/resource/icon/16/054.png")));	// 'filter' icon
			filterButton.setToolTipText(Constant.messages.getString("history.filter.button.filter"));
			DisplayUtils.scaleIcon(filterButton);

			filterButton.addActionListener(new java.awt.event.ActionListener() { 

				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					extension.showFilterPlusDialog();
				}
			});
		}
		return filterButton;
	}

	private JToggleButton getScopeButton() {
		if (scopeButton == null) {
			scopeButton = new ZapToggleButton();
			scopeButton.setIcon(new ImageIcon(LogPanel.class.getResource("/resource/icon/fugue/target-grey.png")));
			scopeButton.setToolTipText(Constant.messages.getString("history.scope.button.unselected"));
			scopeButton.setSelectedIcon(new ImageIcon(LogPanel.class.getResource("/resource/icon/fugue/target.png")));
			scopeButton.setSelectedToolTipText(Constant.messages.getString("history.scope.button.selected"));
			DisplayUtils.scaleIcon(scopeButton);

			scopeButton.addActionListener(new java.awt.event.ActionListener() { 

				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					extension.setShowJustInScope(scopeButton.isSelected());
				}
			});
			historyListFiltersButtonGroup.add(scopeButton);
		}
		return scopeButton;
	}

	private JToggleButton getLinkWithSitesTreeButton() {
		if (linkWithSitesTreeButton == null) {
			linkWithSitesTreeButton = new ZapToggleButton();
			linkWithSitesTreeButton.setIcon(new ImageIcon(LogPanel.class.getResource("/resource/icon/16/earth-grey.png")));
			linkWithSitesTreeButton.setToolTipText(Constant.messages.getString("history.linkWithSitesSelection.unselected.button.tooltip"));
			linkWithSitesTreeButton.setSelectedIcon(new ImageIcon(LogPanel.class.getResource("/resource/icon/16/094.png")));
			linkWithSitesTreeButton.setSelectedToolTipText(Constant.messages.getString("history.linkWithSitesSelection.selected.button.tooltip"));
			DisplayUtils.scaleIcon(linkWithSitesTreeButton);

			linkWithSitesTreeButton.addActionListener(new java.awt.event.ActionListener() {

				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					setLinkWithSitesTreeSelection(linkWithSitesTreeButton.isSelected());
				}
			});
			historyListFiltersButtonGroup.add(linkWithSitesTreeButton);
		}
		return linkWithSitesTreeButton;
	}

	public void setLinkWithSitesTreeSelection(boolean enabled) {
		linkWithSitesTreeButton.setSelected(enabled);
		final JTree sitesTree = view.getSiteTreePanel().getTreeSite();
		String baseUri = null;
		if (enabled) {
			final TreePath selectionPath = sitesTree.getSelectionPath();
			if (selectionPath != null) {
				baseUri = getLinkWithSitesTreeBaseUri((SiteNode) selectionPath.getLastPathComponent());
			}
			sitesTree.addTreeSelectionListener(getLinkWithSitesTreeSelectionListener());
		} else {
			sitesTree.removeTreeSelectionListener(getLinkWithSitesTreeSelectionListener());
		}
		extension.setLinkWithSitesTree(enabled, baseUri);
	}

	private static String getLinkWithSitesTreeBaseUri(SiteNode siteNode) {
		if (!siteNode.isRoot()) {
			HistoryReference historyReference = siteNode.getHistoryReference();
			if (historyReference != null) {
				return historyReference.getURI().toString();
			}
		}
		return null;
	}

	private HistoryReferencesTable getHistoryReferenceTable() {
		if (historyReferencesTable == null) {
			historyReferencesTable = new HistoryReferencesTable();
			historyReferencesTable.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			historyReferencesTable.setName("History Table");
			historyReferencesTable.addMouseListener(new java.awt.event.MouseAdapter() {

				@Override
				public void mouseClicked(java.awt.event.MouseEvent e) {
					if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() > 1) {  // double click
						view.getRequestPanel().setTabFocus();
						return;
				    }
				}
			});
			
			historyReferencesTable.getSelectionModel().addListSelectionListener(new javax.swing.event.ListSelectionListener() {

				@Override
				public void valueChanged(javax.swing.event.ListSelectionEvent e) {
					// ZAP: Changed to only display the message when there are no more selection changes.
					if (!e.getValueIsAdjusting()) {
						HistoryReference historyRef = getHistoryReferenceTable().getSelectedHistoryReference();
						if (historyRef == null) {
					        return;
					    }
	
	                    readAndDisplay(historyRef);
					}

				}


			});

		}
		return historyReferencesTable;
	}
	
//    private void readAndDisplay(HistoryReference historyRef) {
//
//        HttpMessage msg = null;
//        try {
//            msg = historyRef.getHttpMessage();
//            if (msg.getRequestHeader().isEmpty()) {
//                requestPanel.setMessage(null, true);
//            } else {
//                requestPanel.setMessage(msg, true);
//            }
//            
//            if (msg.getResponseHeader().isEmpty()) {
//                responsePanel.setMessage(null, false);
//            } else {
//                responsePanel.setMessage(msg, false);
//            }
//        } catch (Exception e1) {
//            e1.printStackTrace();
//        }
//        
//    }

    

    
    private Vector<HistoryReference> displayQueue = new Vector<>();
    private Thread thread = null;
    
    protected void display(final HistoryReference historyRef) {
    	this.readAndDisplay(historyRef);
    	getHistoryReferenceTable().selectHistoryReference(historyRef.getHistoryId());
    }

    public void clearDisplayQueue() {
    	synchronized(displayQueue) {
    		displayQueue.clear();
    	}
    }
    
    private void readAndDisplay(final HistoryReference historyRef) {

        synchronized(displayQueue) {
            if (displayQueue.size() > 0) {
                displayQueue.clear();
            }
            
            displayQueue.add(historyRef);

        }
        
        if (thread != null && thread.isAlive()) {
            return;
        }
        
        thread = new Thread(this);

        thread.setPriority(Thread.NORM_PRIORITY);
        thread.start();
    }
    
    
    /**
     * @deprecated (2.5.0) No longer used/needed.
     */
    @Deprecated
    @SuppressWarnings("javadoc")
    public void setDisplayPanel(HttpPanel requestPanel, HttpPanel responsePanel) {
    }

    @Override
    public void run() {
        HistoryReference ref = null;
        int count = 0;
        
        do {
            synchronized(displayQueue) {
                count = displayQueue.size();
                if (count == 0) {
                    break;
                }
                
                ref = displayQueue.get(0);
                displayQueue.remove(0);
            }
            
            try {
                final HttpMessage msg = ref.getHttpMessage();
                EventQueue.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        view.displayMessage(msg);
                        getHistoryReferenceTable().requestFocus();

                    }
                });
                
            } catch (Exception e) {
                // ZAP: Added logging.
                logger.error(e.getMessage(), e);
            }
            
            // wait some time to allow another selection event to be triggered
            try {
                Thread.sleep(200);
            } catch (Exception e) {}
        } while (true);
        
        
    }

    public void setFilterStatus (HistoryFilter filter) {
    	filterStatus.setText(filter.toShortString());
    	filterStatus.setToolTipText(filter.toLongString());
    }

	private LinkWithSitesTreeSelectionListener getLinkWithSitesTreeSelectionListener() {
		if (linkWithSitesTreeSelectionListener == null) {
			linkWithSitesTreeSelectionListener = new LinkWithSitesTreeSelectionListener();
		}
		return linkWithSitesTreeSelectionListener;
	}

	private class LinkWithSitesTreeSelectionListener implements TreeSelectionListener {

		@Override
		public void valueChanged(TreeSelectionEvent e) {
			extension.updateLinkWithSitesTreeBaseUri(getLinkWithSitesTreeBaseUri((SiteNode) e.getPath().getLastPathComponent()));
		}
	}

	public HistoryReference getSelectedHistoryReference() {
		return getHistoryReferenceTable().getSelectedHistoryReference();
	}

	public List<HistoryReference> getSelectedHistoryReferences() {
		return getHistoryReferenceTable().getSelectedHistoryReferences();
	}

	public void setModel(HistoryReferencesTableModel<DefaultHistoryReferencesTableEntry> historyTableModel) {
		getHistoryReferenceTable().setModel(historyTableModel);
	}
}
