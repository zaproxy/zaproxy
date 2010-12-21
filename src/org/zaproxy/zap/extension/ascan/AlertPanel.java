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
package org.zaproxy.zap.extension.ascan;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.InputEvent;

import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.extension.ViewDelegate;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.HttpPanel;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class AlertPanel extends AbstractPanel {

	private static final long serialVersionUID = 1L;
	private ViewDelegate view = null;
	private JTree treeAlert = null;
	private TreePath rootTreePath = null;	
	
	private JScrollPane paneScroll = null;

	private JSplitPane splitPane = null;
	private AlertViewPanel alertViewPanel = null;

	private ExtensionHistory extHist = null; 

	
    /**
     * 
     */
    public AlertPanel() {
        super();
 		initialize();
    }

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setLayout(new CardLayout());
        this.setSize(274, 251);
        this.setName(Constant.messages.getString("alerts.panel.title"));
		this.setIcon(new ImageIcon(getClass().getResource("/resource/icon/16/071.png")));	// 'flag' icon

        this.add(getSplitPane(), getSplitPane().getName());
			
	}
	
	private JSplitPane getSplitPane() {
		if (splitPane == null) {
			splitPane = new JSplitPane();
			splitPane.setName("Alert panels");
			splitPane.setDividerSize(3);
			splitPane.setDividerLocation(400);
			splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
			splitPane.setLeftComponent(getPaneScroll());
			//splitPane.setRightComponent(getJScrollPaneRight());
			splitPane.setRightComponent(getAlertViewPanel());
			//splitPane.setResizeWeight(0.5D);
			splitPane.setPreferredSize(new Dimension(100,200));
		}
		return splitPane;
	}

	public AlertViewPanel getAlertViewPanel() {
		if (alertViewPanel == null) {
			alertViewPanel = new AlertViewPanel();
		}
		return alertViewPanel;
	}
	
	/**
	 * This method initializes treeAlert	
	 * 	
	 * @return javax.swing.JTree	
	 */    
	JTree getTreeAlert() {
		if (treeAlert == null) {
			treeAlert = new JTree();
			treeAlert.setName("treeAlert");
			treeAlert.setShowsRootHandles(true);
			treeAlert.setBorder(javax.swing.BorderFactory.createEmptyBorder(0,0,0,0));
			treeAlert.addMouseListener(new java.awt.event.MouseAdapter() { 
				public void mousePressed(java.awt.event.MouseEvent e) {    
				    if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0) {  // right mouse button
				        view.getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
				    }	
				    if (e.getClickCount() > 1) {
				    	// Its a double click - edit the alert
					    DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeAlert.getLastSelectedPathComponent();
					    if (node != null && node.getUserObject() != null) {
					        Object obj = node.getUserObject();
					        if (obj instanceof Alert) {
					            Alert alert = (Alert) obj;
					            
								if (extHist == null) {
									extHist = (ExtensionHistory) Control.getSingleton().getExtensionLoader().getExtension("ExtensionHistory");
								}
								if (extHist != null) {
									extHist.showAlertAddDialog(alert);
								}
					        }
					    }
				    }
				}
			});
			treeAlert.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() { 
				public void valueChanged(javax.swing.event.TreeSelectionEvent e) {
				    DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeAlert.getLastSelectedPathComponent();
				    if (node != null && node.getUserObject() != null) {
				        Object obj = node.getUserObject();
				        if (obj instanceof Alert) {
				            Alert alert = (Alert) obj;
						    setMessage(alert.getMessage());
						    getAlertViewPanel().displayAlert(alert);
				        } else {
						    getAlertViewPanel().clearAlert();
				        }
				    } else {
					    getAlertViewPanel().clearAlert();
				    }
				}
			});
		}
		return treeAlert;
	}
	
	
	
	
	/**
	 * This method initializes paneScroll	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */    
	private JScrollPane getPaneScroll() {
		if (paneScroll == null) {
			paneScroll = new JScrollPane();
			paneScroll.setName("paneScroll");
			paneScroll.setViewportView(getTreeAlert());
		}
		return paneScroll;
	}
	
	void setView(ViewDelegate view) {
	    this.view = view;
	}
	
    /**
     * @return Returns the view.
     */
    private ViewDelegate getView() {
        return view;
    }

    
	public void expandRoot() {
        TreeNode root = (TreeNode) getTreeAlert().getModel().getRoot();
        if (rootTreePath == null || root != rootTreePath.getPathComponent(0)) {
            rootTreePath = new TreePath(root);
        }
	    
		if (EventQueue.isDispatchThread()) {
		    getTreeAlert().expandPath(rootTreePath);
		    return;
		}
		try {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
				    getTreeAlert().expandPath(rootTreePath);
				}
			});
		} catch (Exception e) {
		}
	}
	
	private void setMessage(HttpMessage msg) {
	    HttpPanel requestPanel = getView().getRequestPanel();
	    HttpPanel responsePanel = getView().getResponsePanel();
	    requestPanel.setMessage("","", true);
	    if (!msg.getRequestHeader().isEmpty()) {
	        requestPanel.setMessage(msg.getRequestHeader().toString(), msg.getRequestBody().toString(), true);
	    }

	    responsePanel.setMessage("","", false);
	    if (!msg.getResponseHeader().isEmpty()) {
	        responsePanel.setMessage(msg.getResponseHeader().toString(), msg.getResponseBody().toString(), false);
	    }

	}
}
