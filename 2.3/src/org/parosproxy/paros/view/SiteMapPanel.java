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

package org.parosproxy.paros.view;

import java.awt.CardLayout;
import java.awt.Event;
import java.awt.EventQueue;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
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
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.HttpPanel;
import org.zaproxy.zap.view.SiteMapListener;
import org.zaproxy.zap.view.SiteMapTreeCellRenderer;


public class SiteMapPanel extends AbstractPanel {

	private static final long serialVersionUID = -3161729504065679088L;

	// ZAP: Added logger
    private static Logger log = Logger.getLogger(SiteMapPanel.class);

	private JScrollPane jScrollPane = null;
	private JTree treeSite = null;
	private TreePath rootTreePath = null;
	private View view = null;
	
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
	    this.setIcon(new ImageIcon(View.class.getResource("/resource/icon/16/094.png")));
	    this.setName(Constant.messages.getString("sites.panel.title"));
		this.setDefaultAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK | Event.SHIFT_MASK, false));
		this.setMnemonic(Constant.messages.getChar("sites.panel.mnemonic"));

		this.setLayout(new CardLayout());
	    if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
	    	this.setSize(300,200);
	    }
		this.add(getJScrollPane(), getJScrollPane().getName());
        expandRoot();
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

	          			View.getSingleton().getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
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
        if (rootTreePath == null || root != rootTreePath.getPathComponent(0)) {
            rootTreePath = new TreePath(root);
        }
	    
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
