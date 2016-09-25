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
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Window;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.commons.httpclient.URI;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractDialog;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.SiteNode;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.Target;
import org.zaproxy.zap.utils.DisplayUtils;

/**
 * A modal dialog for selecting a node in the SitesTree. 
 * @author psiinon
 *
 */
public class NodeSelectDialog extends AbstractDialog {

	private static final long serialVersionUID = 1L;
	private JPanel jPanel = null;
	private JTree treeContext = null;
	private JTree treeSite = null;

	private JButton selectButton = null;
	private JButton cancelButton = null;
	
	private DefaultTreeModel contextTree = null;
	private DefaultTreeModel siteTree = null;
	
	private SiteNode selectedSiteNode = null;
	private Target selectedTarget = null;
	private boolean allowRoot = false;
	
    /**
     * Constructs a modal {@code NodeSelectDialog} with the given parent.
     * 
     * @param parent the {@code Window} from which the dialog is displayed or {@code null} if this dialog has no parent
     * @throws HeadlessException when {@code GraphicsEnvironment.isHeadless()} returns {@code true}
     */
    public NodeSelectDialog(Window parent) throws HeadlessException {
        super(parent, true);	// Modal, ie always on top
        initialize();
    }

	/**
	 * This method initializes this
	 */
	private void initialize() {
        this.setTitle(Constant.messages.getString("siteselect.dialog.title"));
        this.setContentPane(getJPanel());
        this.addWindowListener(new java.awt.event.WindowAdapter() {   
        	@Override
        	public void windowOpened(java.awt.event.WindowEvent e) {    
        	} 

        	@Override
        	public void windowClosing(java.awt.event.WindowEvent e) {    
        	    cancelButton.doClick();
        	}
        });
		pack();
	}

	public SiteNode showDialog(URI uri) {
		return this.showDialog(Model.getSingleton().getSession().getSiteTree().findNode(uri));
	}

	public SiteNode showDialog(SiteNode defaultNode) {
		// Assume Contexts cant be selected
		this.getTreeContext().setVisible(false);
		SiteNode siteRoot = (SiteNode)Model.getSingleton().getSession().getSiteTree().getRoot();
		populateNode(siteRoot, (SiteNode)this.siteTree.getRoot(), defaultNode);
		if (selectedSiteNode != null) {
			// Found the default node, select it
			TreePath path = new TreePath(this.siteTree.getPathToRoot(selectedSiteNode));
			this.getTreeSite().setExpandsSelectedPaths(true);
			this.getTreeSite().setSelectionPath(path);
			this.getTreeSite().scrollPathToVisible(path);
			this.getTreeSite().expandPath(path);
		} else {
			// no default path, just expand the top level
			TreePath path = new TreePath(this.siteTree.getPathToRoot((TreeNode)this.siteTree.getRoot()));
			this.getTreeSite().expandPath(path);
		}
		this.setVisible(true);
		// The dialog is modal so this wont return until the dialog visibility is unset
		return selectedSiteNode;
	}

	public Target showDialog(Target defaultTarget) {
		// Assume Contexts can be selected
		this.getTreeContext().setVisible(true);
		SiteNode siteRoot = (SiteNode)Model.getSingleton().getSession().getSiteTree().getRoot();
		populateContexts((SiteNode)this.contextTree.getRoot());
		if (defaultTarget != null) {
			populateNode(siteRoot, (SiteNode)this.siteTree.getRoot(), defaultTarget.getStartNode());
		} else { 
			populateNode(siteRoot, (SiteNode)this.siteTree.getRoot(), null);
		}
		if (selectedSiteNode != null) {
			// Found the default node, select it
			TreePath path = new TreePath(this.siteTree.getPathToRoot(selectedSiteNode));
			this.getTreeSite().setExpandsSelectedPaths(true);
			this.getTreeSite().setSelectionPath(path);
			this.getTreeSite().scrollPathToVisible(path);
			this.getTreeSite().expandPath(path);
		} else {
			// no default path, just expand the top level
			TreePath path = new TreePath(this.siteTree.getPathToRoot((TreeNode)this.siteTree.getRoot()));
			this.getTreeSite().expandPath(path);
		}
		this.setVisible(true);
		// The dialog is modal so this wont return until the dialog visibility is unset
		if (selectedSiteNode != null) {
			return new Target(selectedSiteNode);
		}
		return selectedTarget;
	}

	private void populateContexts(SiteNode root) {
		// Uncomment to hide contexts tree if there are no valid contexts - 
		// not sure if this is a good idea or not :/ 
		//int contexts = 0;
		int contextsInScope = 0;
		for (Context ctx : Model.getSingleton().getSession().getContexts()) {
			// TODO ignore handle protected mode?
            if (ctx.getIncludeInContextRegexs().size() > 0) {
                SiteNode node = new SiteNode(null, HistoryReference.TYPE_PROXIED, ctx.getName());
                node.setUserObject(new Target(ctx));
    			root.add(node);
    			//contexts ++;
    			if (ctx.isInScope()) {
    				contextsInScope ++;
    			}
            }
		}
		if (contextsInScope > 1) {
			// Allow user to choose everything in scope
            SiteNode node = new SiteNode(null, HistoryReference.TYPE_PROXIED, Constant.messages.getString("context.allInScope"));
            node.setUserObject(new Target(null, null, true, true));
			root.add(node);
		}
		
		//this.getTreeContext().setVisible(contexts > 0);
		this.getTreeContext().expandRow(0);
	}

	private void populateNode(SiteNode src, SiteNode dest, SiteNode defaultNode) {
		if (src.equals(defaultNode)) {
			// Flag this for use later..
			this.selectedSiteNode = dest;
		}
        for (int i=0; i < src.getChildCount(); i++) {
            SiteNode child = (SiteNode) src.getChildAt(i);
            SiteNode copy = new SiteNode(null, HistoryReference.TYPE_PROXIED, child.getNodeName());
            copy.setUserObject(child);
            dest.add(copy);
            this.populateNode(child, copy, defaultNode);
		}
	}
	
	private DefaultTreeModel emptyContextTree() {
        return new DefaultTreeModel(new SiteNode(null, -1, Constant.messages.getString("context.list")));
	}

	private DefaultTreeModel emptySiteTree() {
		// Make a very sparse copy of the site tree
		SiteNode siteRoot = (SiteNode)Model.getSingleton().getSession().getSiteTree().getRoot();
        SiteNode root = new SiteNode(null, -1, Constant.messages.getString("tab.sites"));
        root.setUserObject(siteRoot);
        return new DefaultTreeModel(root);
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanel() {
		if (jPanel == null) {
			GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
			java.awt.GridBagConstraints gridBagConstraints13 = new GridBagConstraints();

			javax.swing.JLabel jLabel2 = new JLabel();

			java.awt.GridBagConstraints gridBagConstraints3 = new GridBagConstraints();

			java.awt.GridBagConstraints gridBagConstraints2 = new GridBagConstraints();

			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			jPanel.setPreferredSize(DisplayUtils.getScaledDimension(400,400));
			jPanel.setMinimumSize(DisplayUtils.getScaledDimension(400,400));
			gridBagConstraints2.gridx = 1;
			gridBagConstraints2.gridy = 5;
			gridBagConstraints2.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints2.anchor = java.awt.GridBagConstraints.EAST;
			gridBagConstraints3.gridx = 2;
			gridBagConstraints3.gridy = 5;
			gridBagConstraints3.insets = new java.awt.Insets(2,2,2,10);
			gridBagConstraints3.anchor = java.awt.GridBagConstraints.EAST;

			gridBagConstraints13.gridx = 0;
			gridBagConstraints13.gridy = 5;
			gridBagConstraints13.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints13.weightx = 1.0D;
			gridBagConstraints13.insets = new java.awt.Insets(2,10,2,5);

			gridBagConstraints15.weightx = 1.0D;
			gridBagConstraints15.weighty = 1.0D;
			gridBagConstraints15.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints15.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints15.gridwidth = 3;
			gridBagConstraints15.gridx = 0;
			gridBagConstraints15.gridy = 2;
			gridBagConstraints15.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints15.ipadx = 0;
			gridBagConstraints15.ipady = 10;

			jPanel.add(new ContextsSitesPanel(getTreeContext(), getTreeSite()), gridBagConstraints15);
			jPanel.add(jLabel2, gridBagConstraints13);
			jPanel.add(getCancelButton(), gridBagConstraints2);
			jPanel.add(getSelectButton(), gridBagConstraints3);
		}
		return jPanel;
	}

	private JTree getTreeSite() {
		if (treeSite == null) {
			this.siteTree = this.emptySiteTree();
			treeSite = new JTree(this.siteTree);
			treeSite.setShowsRootHandles(true);
			treeSite.setName("nodeSelectTree");
			treeSite.setToggleClickCount(1);
			treeSite.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			treeSite.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() { 

				@Override
				public void valueChanged(javax.swing.event.TreeSelectionEvent e) {    
				    SiteNode node = (SiteNode) treeSite.getLastSelectedPathComponent();
				    getSelectButton().setEnabled(node != null && (node.getParent() != null || allowRoot));
				}
			});
			treeSite.addMouseListener(new java.awt.event.MouseAdapter() { 
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
				    if (treeSite.getLastSelectedPathComponent() != null) {
				    	// They selected a site node, deselect any context
				    	getTreeContext().clearSelection();
				    }
				    if (e.getClickCount() > 1) {
				    	// Its a double click - close the dialog to select this node
				    	nodeSelected();
				    }
				}
			});


			treeSite.setCellRenderer(new SiteMapTreeCellRenderer(new ArrayList<SiteMapListener>()));
		}
		return treeSite;
	}
	
	private void nodeSelected() {
	    SiteNode node = (SiteNode) treeSite.getLastSelectedPathComponent();
	    if (node != null && (node.getParent() != null || allowRoot)) {
	    	selectedSiteNode = (SiteNode) node.getUserObject();
	    	selectedTarget = null;
			NodeSelectDialog.this.setVisible(false);
	    }
	}
	
	private void contextSelected() {
	    SiteNode node = (SiteNode) treeContext.getLastSelectedPathComponent();
	    if (node != null && (node.getParent() != null || allowRoot)) {
	    	selectedTarget = (Target) node.getUserObject();
	    	selectedSiteNode = null;
			NodeSelectDialog.this.setVisible(false);
	    }
	}
	
	private JTree getTreeContext() {
		if (treeContext == null) {
			this.contextTree = this.emptyContextTree();
			treeContext = new JTree(this.contextTree);
			treeContext.setShowsRootHandles(true);
			treeContext.setName("nodeContextTree");
			treeContext.setToggleClickCount(1);
			treeContext.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			treeContext.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() { 

				@Override
				public void valueChanged(javax.swing.event.TreeSelectionEvent e) {
				    SiteNode node = (SiteNode) treeContext.getLastSelectedPathComponent();
				    getSelectButton().setEnabled(node != null && (node.getParent() != null || allowRoot));
				}
			});
			treeContext.addMouseListener(new java.awt.event.MouseAdapter() { 
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
				    if (treeContext.getLastSelectedPathComponent() != null) {
				    	// They selected a context node, deselect any site node
				    	getTreeSite().clearSelection();
				    }
				    if (e.getClickCount() > 1) {
				    	// Its a double click - close the dialog to select this node
				    	contextSelected();
				    }
				}
			});

			treeContext.setCellRenderer(new ContextsTreeCellRenderer());
		}
		return treeContext;
	}
	

	/**
	 * This method initializes btnStart	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getSelectButton() {
		if (selectButton == null) {
			selectButton = new JButton();
			selectButton.setText(Constant.messages.getString("siteselect.button.select"));
			selectButton.setEnabled(false);	// Enabled when a node is selected
			selectButton.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (treeSite.getLastSelectedPathComponent() != null) {
						nodeSelected();
					} else if (treeContext.getLastSelectedPathComponent() != null) {
						contextSelected();
					}
				}
			});

		}
		return selectButton;
	}
	/**
	 * This method initializes btnStop	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton();
			cancelButton.setText(Constant.messages.getString("all.button.cancel"));
			cancelButton.setEnabled(true);
			cancelButton.addActionListener(new java.awt.event.ActionListener() { 

				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					selectedSiteNode = null;
					selectedTarget = null;
					NodeSelectDialog.this.setVisible(false);
					//NodeSelectDialog.this.dispose();
				}
			});

		}
		return cancelButton;
	}

	public boolean isAllowRoot() {
		return allowRoot;
	}

	public void setAllowRoot(boolean allowRoot) {
		this.allowRoot = allowRoot;
	}
	
}
