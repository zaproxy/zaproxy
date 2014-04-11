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

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.commons.httpclient.URI;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractDialog;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.SiteNode;

/**
 * A modal dialog for selecting a node in the SitesTree. 
 * @author psiinon
 *
 */
public class NodeSelectDialog extends AbstractDialog {

	private static final long serialVersionUID = 1L;
	private JPanel jPanel = null;
	private JTree treeSite = null;

	private JButton selectButton = null;
	private JButton cancelButton = null;
	
	private DefaultTreeModel siteTree = null;
	
	private JScrollPane jScrollPane = null;
	private SiteNode selectedNode = null;
	private SiteNode defaultSelected = null;
	private boolean allowRoot = false;
	
    /**
     * @param arg0
     * @param arg1
     * @throws HeadlessException
     */
    public NodeSelectDialog(Frame arg0) throws HeadlessException {
        super(arg0, true);	// Modal, ie always on top
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
		SiteNode siteRoot = (SiteNode)Model.getSingleton().getSession().getSiteTree().getRoot();
		populateNode(siteRoot, (SiteNode)this.siteTree.getRoot(), defaultNode);
		if (defaultSelected != null) {
			// Found the default node, select it
			TreePath path = new TreePath(this.siteTree.getPathToRoot(defaultSelected));
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
		return selectedNode;
	}
	
	private void populateNode(SiteNode src, SiteNode dest, SiteNode defaultNode) {
		if (src.equals(defaultNode)) {
			// Flag this for use later..
			this.defaultSelected = dest;
		}
        for (int i=0; i < src.getChildCount(); i++) {
            SiteNode child = (SiteNode) src.getChildAt(i);
            SiteNode copy = new SiteNode(null, HistoryReference.TYPE_PROXIED, child.getNodeName());
            copy.setUserObject(child);
            dest.add(copy);
            this.populateNode(child, copy, defaultNode);
		}
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
			jPanel.setPreferredSize(new java.awt.Dimension(400,400));
			jPanel.setMinimumSize(new java.awt.Dimension(400,400));
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

			jPanel.add(getJScrollPane(), gridBagConstraints15);
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
	    	selectedNode = (SiteNode) node.getUserObject();
			NodeSelectDialog.this.setVisible(false);
	    }
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
			selectButton.setMinimumSize(new java.awt.Dimension(75,30));
			selectButton.setPreferredSize(new java.awt.Dimension(75,30));
			selectButton.setMaximumSize(new java.awt.Dimension(100,40));
			selectButton.setEnabled(false);	// Enabled when a node is selected
			selectButton.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
			    	nodeSelected();
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
			cancelButton.setMaximumSize(new java.awt.Dimension(100,40));
			cancelButton.setMinimumSize(new java.awt.Dimension(70,30));
			cancelButton.setPreferredSize(new java.awt.Dimension(70,30));
			cancelButton.setEnabled(true);
			cancelButton.addActionListener(new java.awt.event.ActionListener() { 

				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					selectedNode = null;
					NodeSelectDialog.this.setVisible(false);
					//NodeSelectDialog.this.dispose();
				}
			});

		}
		return cancelButton;
	}
	
	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */    
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			jScrollPane.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			jScrollPane.setViewportView(getTreeSite());
		}
		return jScrollPane;
	}

	public boolean isAllowRoot() {
		return allowRoot;
	}

	public void setAllowRoot(boolean allowRoot) {
		this.allowRoot = allowRoot;
	}
	
}

