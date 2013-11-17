/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2011 The ZAP Development team
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

package org.zaproxy.zap.extension.alert;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.extension.ViewDelegate;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.HttpPanel;
import org.zaproxy.zap.extension.search.SearchMatch;
import org.zaproxy.zap.view.LayoutHelper;

public class AlertPanel extends AbstractPanel {
	
	public static final String ALERT_TREE_PANEL_NAME = "treeAlert";

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(AlertPanel.class);
	
	private ViewDelegate view = null;
	private JTree treeAlert = null;
	private TreePath rootTreePath = null;	
	
	private JScrollPane paneScroll = null;

	private JPanel panelCommand = null;
	private JToolBar panelToolbar = null;
	private JSplitPane splitPane = null;
	private AlertViewPanel alertViewPanel = null;
	private JToggleButton scopeButton = null;

	private ExtensionAlert extension = null;
	private ExtensionHistory extHist = null; 

	
    /**
     * 
     */
    public AlertPanel(ExtensionAlert extension) {
        super();
        this.extension = extension;
 		initialize();
    }

	/**
	 * This method initializes this
	 */
	private void initialize() {
        this.setLayout(new CardLayout());
        this.setSize(274, 251);
        this.setName(Constant.messages.getString("alerts.panel.title"));
		this.setIcon(new ImageIcon(AlertPanel.class.getResource("/resource/icon/16/071.png")));	// 'flag' icon

        this.add(getPanelCommand(), getPanelCommand().getName());
			
	}
	
	private javax.swing.JPanel getPanelCommand() {
		if (panelCommand == null) {

			panelCommand = new javax.swing.JPanel();
			panelCommand.setLayout(new java.awt.GridBagLayout());
			panelCommand.setName("AlertPanel");
			
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
			
			panelCommand.add(getSplitPane(), gridBagConstraints2);

		}
		return panelCommand;
	}

	private javax.swing.JToolBar getPanelToolbar() {
		if (panelToolbar == null) {
			
			panelToolbar = new javax.swing.JToolBar();
			panelToolbar.setLayout(new java.awt.GridBagLayout());
			panelToolbar.setEnabled(true);
			panelToolbar.setFloatable(false);
			panelToolbar.setRollover(true);
			panelToolbar.setPreferredSize(new java.awt.Dimension(800,30));
			panelToolbar.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
			panelToolbar.setName("AlertToolbar");
			
			panelToolbar.add(getScopeButton(), LayoutHelper.getGBC(0, 0, 1, 0.0D));
			panelToolbar.add(new JLabel(), LayoutHelper.getGBC(20, 0, 1, 1.0D));	// Spacer
		}
		return panelToolbar;
	}

	private JSplitPane getSplitPane() {
		if (splitPane == null) {
			splitPane = new JSplitPane();
			splitPane.setName("AlertPanels");
			splitPane.setDividerSize(3);
			splitPane.setDividerLocation(400);
			splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
			// Add toolbar
			JPanel panel = new JPanel();
			panel.setLayout(new GridBagLayout());
			panel.add(this.getPanelToolbar(), LayoutHelper.getGBC(0, 0, 1, 0.0D));
			panel.add(getPaneScroll(), LayoutHelper.getGBC(0, 1, 1, 1.0D, 1.0D));
			
			splitPane.setLeftComponent(panel);
			
			splitPane.setRightComponent(getAlertViewPanel());
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
	
	private JToggleButton getScopeButton() {
		if (scopeButton == null) {
			scopeButton = new JToggleButton();
			scopeButton.setIcon(new ImageIcon(AlertPanel.class.getResource("/resource/icon/fugue/target-grey.png")));
			scopeButton.setToolTipText(Constant.messages.getString("history.scope.button.unselected"));

			scopeButton.addActionListener(new java.awt.event.ActionListener() { 

				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					extension.setShowJustInScope(scopeButton.isSelected());
					if (scopeButton.isSelected()) {
						scopeButton.setIcon(new ImageIcon(AlertPanel.class.getResource("/resource/icon/fugue/target.png")));
						scopeButton.setToolTipText(Constant.messages.getString("history.scope.button.selected"));
					} else {
						scopeButton.setIcon(new ImageIcon(AlertPanel.class.getResource("/resource/icon/fugue/target-grey.png")));
						scopeButton.setToolTipText(Constant.messages.getString("history.scope.button.unselected"));
					}
				}
			});
		}
		return scopeButton;
	}

	/**
	 * This method initializes treeAlert	
	 * 	
	 * @return javax.swing.JTree	
	 */    
	JTree getTreeAlert() {
		if (treeAlert == null) {
			treeAlert = new JTree();
			treeAlert.setName(ALERT_TREE_PANEL_NAME);
			treeAlert.setShowsRootHandles(true);
			treeAlert.setBorder(javax.swing.BorderFactory.createEmptyBorder(0,0,0,0));
			treeAlert.addMouseListener(new java.awt.event.MouseAdapter() { 
				@Override
				public void mousePressed(java.awt.event.MouseEvent e) {
				}
					
				@Override
				public void mouseReleased(java.awt.event.MouseEvent e) {
					mouseClicked(e);
				}
				
				@Override
				public void mouseClicked(java.awt.event.MouseEvent e) {
					// right mouse button action
				    if (SwingUtilities.isRightMouseButton(e)) {
						// Select site list item on right click
				    	TreePath tp = treeAlert.getPathForLocation( e.getPoint().x, e.getPoint().y );
				    	if ( tp != null ) {
				    		boolean select = true;
				    		// Only select a new item if the current item is not
				    		// already selected - this is to allow multiple items
				    		// to be selected
					    	if (treeAlert.getSelectionPaths() != null) {
					    		for (TreePath t : treeAlert.getSelectionPaths()) {
					    			if (t.equals(tp)) {
					    				select = false;
					    				break;
					    			}
					    		}
					    	}
					    	if (select) {
					    		treeAlert.getSelectionModel().setSelectionPath(tp);
					    	}
				    	}
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
									extHist = (ExtensionHistory) Control.getSingleton().getExtensionLoader().getExtension(ExtensionHistory.NAME);
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
				@Override
				public void valueChanged(javax.swing.event.TreeSelectionEvent e) {
				    DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeAlert.getLastSelectedPathComponent();
				    if (node != null && node.getUserObject() != null) {
				        Object obj = node.getUserObject();
				        if (obj instanceof Alert) {
				            Alert alert = (Alert) obj;
						    setMessage(alert.getMessage(), alert.getEvidence());
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
				@Override
				public void run() {
				    getTreeAlert().expandPath(rootTreePath);
				}
			});
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	private void setMessage(HttpMessage msg, String highlight) {
	    HttpPanel requestPanel = getView().getRequestPanel();
	    HttpPanel responsePanel = getView().getResponsePanel();
	    
	    if (msg.getRequestHeader().isEmpty()) {
	    	requestPanel.clearView(true);
	    } else {
	        requestPanel.setMessage(msg);
	    }

	    if (msg.getResponseHeader().isEmpty()) {
	    	responsePanel.clearView(false);
	    } else {
	        responsePanel.setMessage(msg, true);

	        SearchMatch sm = null;
	        int start;
	        
	        // Highlight the 'attack' / evidence
	        if (highlight == null || highlight.length() == 0) {
	        	// ignore
	        } else if ((start = msg.getResponseHeader().toString().indexOf(highlight)) >=0) {
		        sm = new SearchMatch(msg, SearchMatch.Location.RESPONSE_HEAD, start, start + highlight.length());
				responsePanel.highlightHeader(sm);
				responsePanel.setTabFocus();
		        
	        } else if ((start = msg.getResponseBody().toString().indexOf(highlight)) >=0) {
	        	sm = new SearchMatch(msg, SearchMatch.Location.RESPONSE_BODY, start, start + highlight.length());
				responsePanel.highlightBody(sm);
				responsePanel.setTabFocus();

	        } else if ((start = msg.getRequestHeader().toString().indexOf(highlight)) >=0) {
		        sm = new SearchMatch(msg, SearchMatch.Location.REQUEST_HEAD, start, start + highlight.length());
				requestPanel.highlightHeader(sm);
				requestPanel.setTabFocus();

	        } else if ((start = msg.getRequestBody().toString().indexOf(highlight)) >=0) {
	        	sm = new SearchMatch(msg, SearchMatch.Location.REQUEST_BODY, start, start + highlight.length());
				requestPanel.highlightBody(sm);
				requestPanel.setTabFocus();
	        }
	        
	    }

	}
}