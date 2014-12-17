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
// ZAP: 2012/03/15 Changed to initiate the tree with a default model. Changed to
// clear the http panels when the root node is selected.
// ZAP: 2012/04/23 Added @Override annotation to all appropriate methods.
// ZAP: 2012/06/13 Added custom tree cell renderer to treeSite in getTreeSite().
// ZAP: 2013/01/25 Added method for removing listener.
// ZAP: 2013/11/16 Issue 886: Main pop up menu invoked twice on some components
// ZAP: 2014/01/28 Issue 207: Support keyboard shortcuts 
// ZAP: 2014/03/23 Tidy up, removed the instance variable rootTreePath, no need to
// cache the path
// ZAP: 2014/03/23 Issue 609: Provide a common interface to query the state and 
// access the data (HttpMessage and HistoryReference) displayed in the tabs
// ZAP: 2014/10/07 Issue 1357: Hide unused tabs
// ZAP: 2014/12/17 Issue 1174: Support a Site filter

package org.parosproxy.paros.view;

import java.awt.Dimension;
import java.awt.Event;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.extension.history.LogPanel;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.SiteMap;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.history.HistoryFilterPlusDialog;
import org.zaproxy.zap.extension.httppanel.HttpPanel;
import org.zaproxy.zap.view.LayoutHelper;
import org.zaproxy.zap.view.SiteMapListener;
import org.zaproxy.zap.view.SiteMapTreeCellRenderer;
import org.zaproxy.zap.view.SiteTreeFilter;
import org.zaproxy.zap.view.ZapToggleButton;
import org.zaproxy.zap.view.messagecontainer.http.DefaultSelectableHistoryReferencesContainer;
import org.zaproxy.zap.view.messagecontainer.http.SelectableHistoryReferencesContainer;


public class SiteMapPanel extends AbstractPanel {

	private static final long serialVersionUID = -3161729504065679088L;

	// ZAP: Added logger
    private static Logger log = Logger.getLogger(SiteMapPanel.class);

	private JScrollPane jScrollPane = null;
	private JTree treeSite = null;
	private View view = null;

	private javax.swing.JToolBar panelToolbar = null;
	private ZapToggleButton scopeButton = null;
	private JButton filterButton = null;
	private JLabel filterStatus = null;
	private HistoryFilterPlusDialog filterPlusDialog = null;

	// ZAP: Added SiteMapListenners
	private List<SiteMapListener> listeners = new ArrayList<>();
	
	/**
	 * This is the default constructor
	 */
	public SiteMapPanel() {
		super();
		initialize();
	}

	private View getView() {
	    if (view == null) {
	        view = View.getSingleton();
	    }
	    
	    return view;
	}
	/**
	 * This method initializes this
	 */
	private  void initialize() {
		this.setHideable(false);
	    this.setIcon(new ImageIcon(View.class.getResource("/resource/icon/16/094.png")));
	    this.setName(Constant.messages.getString("sites.panel.title"));
		this.setDefaultAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK | Event.SHIFT_MASK, false));
		this.setMnemonic(Constant.messages.getChar("sites.panel.mnemonic"));

	    if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
	    	this.setSize(300,200);
	    }
		
		this.setLayout(new GridBagLayout());
		this.add(this.getPanelToolbar(), LayoutHelper.getGBC(0, 0, 1, 0, new Insets(2,2,2,2)));
		this.add(getJScrollPane(), 
				LayoutHelper.getGBC(0, 1, 1, 1.0, 1.0, GridBagConstraints.BOTH, new Insets(2,2,2,2)));

        expandRoot();
	}
	
	private javax.swing.JToolBar getPanelToolbar() {
		if (panelToolbar == null) {
			
			panelToolbar = new javax.swing.JToolBar();
			panelToolbar.setLayout(new GridBagLayout());
			panelToolbar.setEnabled(true);
			panelToolbar.setFloatable(false);
			panelToolbar.setRollover(true);
			panelToolbar.setPreferredSize(new Dimension(800,30));
			panelToolbar.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
			panelToolbar.setName("ScriptsListToolbar");
			
			int i = 1;
			panelToolbar.add(getScopeButton(), LayoutHelper.getGBC(i++, 0, 1, 0.0D));
			// TODO Disabled for now due to problems with scrolling with sparcely populated filtered trees
			//panelToolbar.add(getFilterButton(), LayoutHelper.getGBC(i++, 0, 1, 0.0D));
			//panelToolbar.add(getFilterStatus(), LayoutHelper.getGBC(i++, 0, 1, 0.0D));
			panelToolbar.add(new JLabel(), LayoutHelper.getGBC(20, 0, 1, 1.0D));	// spacer
		}
		return panelToolbar;
	}

	private HistoryFilterPlusDialog getFilterPlusDialog() {
		if (filterPlusDialog == null) {
			filterPlusDialog = new HistoryFilterPlusDialog(getView().getMainFrame(), true);
			// Override the title as we're reusing the history filter dialog
			filterPlusDialog.setTitle(Constant.messages.getString("sites.filter.title"));
		}
		return filterPlusDialog;
	}
	
	private JLabel getFilterStatus() {
		filterStatus = new JLabel(Constant.messages.getString("history.filter.label.filter") + 
				Constant.messages.getString("history.filter.label.off"));
		return filterStatus;
	}

	private JButton getFilterButton() {
		if (filterButton == null) {
			filterButton = new JButton();
			filterButton.setIcon(new ImageIcon(LogPanel.class.getResource("/resource/icon/16/054.png")));	// 'filter' icon
			filterButton.setToolTipText(Constant.messages.getString("history.filter.button.filter"));

			filterButton.addActionListener(new java.awt.event.ActionListener() { 

				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					showFilterPlusDialog();
				}
			});
		}
		return filterButton;
	}

	private void showFilterPlusDialog() {
		HistoryFilterPlusDialog dialog = getFilterPlusDialog();
		dialog.setModal(true);
    	try {
			dialog.setAllTags(Model.getSingleton().getDb().getTableTag().getAllTags());
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		}

		int exit = dialog.showDialog();
		SiteTreeFilter filter = new SiteTreeFilter(dialog.getFilter());
		filter.setInScope(this.getScopeButton().isSelected());
		if (exit != JOptionPane.CANCEL_OPTION) {
			setFilter();
		}
	}
	
	private void setFilter() {
		SiteTreeFilter filter = new SiteTreeFilter(getFilterPlusDialog().getFilter());
		filter.setInScope(scopeButton.isSelected());
		((SiteMap)treeSite.getModel()).setFilter(filter);
		((DefaultTreeModel)treeSite.getModel()).nodeStructureChanged((SiteNode)treeSite.getModel().getRoot());
    	getFilterStatus().setText(filter.toShortString());
    	getFilterStatus().setToolTipText(filter.toLongString());
		expandRoot();
	}

	private JToggleButton getScopeButton() {
		if (scopeButton == null) {
			scopeButton = new ZapToggleButton();
			scopeButton.setIcon(new ImageIcon(SiteMapPanel.class.getResource("/resource/icon/fugue/target-grey.png")));
			scopeButton.setToolTipText(Constant.messages.getString("history.scope.button.unselected"));
			scopeButton.setSelectedIcon(new ImageIcon(SiteMapPanel.class.getResource("/resource/icon/fugue/target.png")));
			scopeButton.setSelectedToolTipText(Constant.messages.getString("history.scope.button.selected"));

			scopeButton.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					setFilter();
				}
			});
		}
		return scopeButton;
	}

	
	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */    
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getTreeSite());
			jScrollPane.setPreferredSize(new java.awt.Dimension(200,400));
			jScrollPane.setName("jScrollPane");
		}
		return jScrollPane;
	}
	
	/**
	 * This method initializes treeSite	
	 * 	
	 * @return javax.swing.JTree	
	 */    
	public JTree getTreeSite() {
		if (treeSite == null) {
			treeSite = new JTree(new DefaultTreeModel(new DefaultMutableTreeNode()));
			treeSite.setShowsRootHandles(true);
			treeSite.setName("treeSite");
			treeSite.setToggleClickCount(1);
			treeSite.addMouseListener(new java.awt.event.MouseAdapter() { 

				@Override
				public void mousePressed(java.awt.event.MouseEvent e) {
					showPopupMenuIfTriggered(e);
				}
					
				@Override
				public void mouseReleased(java.awt.event.MouseEvent e) {
					showPopupMenuIfTriggered(e);
				}
				
				private void showPopupMenuIfTriggered(java.awt.event.MouseEvent e) {
					// right mouse button action
					if (e.isPopupTrigger()) {

						// ZAP: Select site list item on right click
				    	TreePath tp = treeSite.getPathForLocation( e.getPoint().x, e.getPoint().y );
				    	if ( tp != null ) {
				    		boolean select = true;
				    		// Only select a new item if the current item is not
				    		// already selected - this is to allow multiple items
				    		// to be selected
					    	if (treeSite.getSelectionPaths() != null) {
					    		for (TreePath t : treeSite.getSelectionPaths()) {
					    			if (t.equals(tp)) {
					    				select = false;
					    				break;
					    			}
					    		}
					    	}
					    	if (select) {
					    		treeSite.getSelectionModel().setSelectionPath(tp);
					    	}
				    	}

                        final int countSelectedNodes = treeSite.getSelectionCount();
                        final List<HistoryReference> historyReferences = new ArrayList<>(countSelectedNodes);
                        if (countSelectedNodes > 0) {
                            for (TreePath path : treeSite.getSelectionPaths()) {
                                final SiteNode node = (SiteNode) path.getLastPathComponent();
                                final HistoryReference historyReference = node.getHistoryReference();
                                if (historyReference != null) {
                                    historyReferences.add(historyReference);
                                }
                            }
                        }
                        SelectableHistoryReferencesContainer messageContainer = new DefaultSelectableHistoryReferencesContainer(
                                treeSite.getName(),
                                treeSite,
                                Collections.<HistoryReference> emptyList(),
                                historyReferences);
	          			View.getSingleton().getPopupMenu().show(messageContainer, e.getX(), e.getY());
	            	}
				} 
			});

			treeSite.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() { 

				@Override
				public void valueChanged(javax.swing.event.TreeSelectionEvent e) {    

				    SiteNode node = (SiteNode) treeSite.getLastSelectedPathComponent();
				    if (node == null) {
				        return;
				    }
				    if (!node.isRoot()) {
				    	HttpMessage msg = null;
                        try {
                            msg = node.getHistoryReference().getHttpMessage();
                        } catch (Exception e1) {
                        	// ZAP: Log exceptions
                        	log.warn(e1.getMessage(), e1);
                            return;
                            
                        }

                        HttpPanel reqPanel = getView().getRequestPanel();
				        HttpPanel resPanel = getView().getResponsePanel();
				        
				        if (msg.getRequestHeader().isEmpty()) {
				        	reqPanel.clearView(true);
				        } else {
				        	reqPanel.setMessage(msg);
				        }
				        
				        if (msg.getResponseHeader().isEmpty()) {
				        	resPanel.clearView(false);
				        } else {
				        	resPanel.setMessage(msg, true);
				        }

			        	// ZAP: Call SiteMapListenners
			            for (SiteMapListener listener : listeners) {
			            	listener.nodeSelected(node);
			            }
				    } else {
				    	// ZAP: clear the views when the root is selected
                        getView().getRequestPanel().clearView(true);
				        getView().getResponsePanel().clearView(false);
				    }
	
				}
			});

			// ZAP: Add custom tree cell renderer.
	        DefaultTreeCellRenderer renderer = new SiteMapTreeCellRenderer(listeners);
			treeSite.setCellRenderer(renderer);
		}
		return treeSite;
	}
	
	public void expandRoot() {
        TreeNode root = (TreeNode) treeSite.getModel().getRoot();
        if (root == null) {
            return;
        }
        final TreePath rootTreePath = new TreePath(root);
	    
		if (EventQueue.isDispatchThread()) {
		    getTreeSite().expandPath(rootTreePath);
		    return;
		}
		try {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
				    getTreeSite().expandPath(rootTreePath);
				}
			});
		} catch (Exception e) {
        	// ZAP: Log exceptions
        	log.warn(e.getMessage(), e);
		}
	}
	
	// ZAP: Added addSiteMapListenners
	public void addSiteMapListener(SiteMapListener listenner) {
		this.listeners.add(listenner);
	}

	public void removeSiteMapListener(SiteMapListener listener) {
		this.listeners.remove(listener);
	}
	
	public void showInSites (SiteNode node) {
		TreeNode[] path = node.getPath();
		TreePath tp = new TreePath(path);
		treeSite.setExpandsSelectedPaths(true);
		treeSite.setSelectionPath(tp);
		treeSite.scrollPathToVisible(tp);
	}
}
