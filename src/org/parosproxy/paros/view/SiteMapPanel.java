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
package org.parosproxy.paros.view;

import java.awt.CardLayout;
import java.awt.EventQueue;
import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.parosproxy.paros.model.SiteMap;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.view.SiteMapListener;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class SiteMapPanel extends JPanel {

    // ZAP: Added logger
    private static Log log = LogFactory.getLog(SiteMapPanel.class);

	private JScrollPane jScrollPane = null;
	private JTree treeSite = null;
	private TreePath rootTreePath = null;
	private View view = null;
	
	// ZAP: Added SiteMapListenners
	private List<SiteMapListener> listenners = new ArrayList<SiteMapListener>();
	
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
	 * 
	 * @return void
	 */
	private  void initialize() {
		this.setLayout(new CardLayout());
		this.setSize(300,200);
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
			treeSite = new JTree();
			treeSite.setShowsRootHandles(true);
			treeSite.setName("treeSite");
			treeSite.setToggleClickCount(1);
			treeSite.addMouseListener(new java.awt.event.MouseAdapter() { 

				public void mousePressed(java.awt.event.MouseEvent e) {    
	          		if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0) {  // right mouse button

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

				public void valueChanged(javax.swing.event.TreeSelectionEvent e) {    

				    HttpMessage msg = null;
				    SiteNode node = (SiteNode) treeSite.getLastSelectedPathComponent();
				    if (node == null) {
				        return;
				    }
				    if (!node.isRoot()) {
                        try {
                            msg = node.getHistoryReference().getHttpMessage();
                        } catch (Exception e1) {
                        	// ZAP: Log exceptions
                        	log.warn(e1.getMessage(), e1);
                            return;
                            
                        }

                        HttpPanel reqPanel = getView().getRequestPanel();
				        HttpPanel resPanel = getView().getResponsePanel();
				        reqPanel.setMessage(msg, true);
			            resPanel.setMessage(msg, false);

			        	// ZAP: Call SiteMapListenners
			            for (SiteMapListener listener : listenners) {
			            	listener.nodeSelected(node);
			            }
				    }
	
				}
			});

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
	public void addSiteMapListenner (SiteMapListener listenner) {
		this.listenners.add(listenner);
	}
}
