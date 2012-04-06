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
// ZAP: 2011/04/16 i18n
// ZAP: 2011/11/20 Handle dialogs with no children


package org.parosproxy.paros.view;
 
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractDialog;
import org.parosproxy.paros.model.Model;
import org.zaproxy.zap.extension.help.ExtensionHelp;
import org.zaproxy.zap.utils.ZapTextField;

public class AbstractParamDialog extends AbstractDialog {

	private static final long serialVersionUID = -5223178126156052670L;

	private Object paramObject = null;
    private Hashtable<String, AbstractParamPanel> tablePanel = new Hashtable<String, AbstractParamPanel>();
    private int exitResult = JOptionPane.CANCEL_OPTION;
    
	private JPanel jContentPane = null;
	private JButton btnOK = null;
	private JButton btnCancel = null;
	private JButton btnHelp = null;
	private JPanel jPanel = null;
	private JSplitPane jSplitPane = null;
	private JTree treeParam = null;
	private JPanel jPanel1 = null;
	private JPanel panelParam = null;
	private JPanel panelHeadline = null;
	private ZapTextField txtHeadline = null;

	private DefaultTreeModel treeModel = null;  //  @jve:decl-index=0:parse,visual-constraint="14,12"
	private DefaultMutableTreeNode rootNode = null;  //  @jve:decl-index=0:parse,visual-constraint="10,50"
	private JScrollPane jScrollPane = null;
	private JScrollPane jScrollPane1 = null;
	// ZAP: show the last selected panel
	private String nameLastSelectedPanel = null;

	private ShowHelpAction showHelpAction = null;
	
	// ZAP: Added logger
    private static Logger log = Logger.getLogger(AbstractParamDialog.class);

	
	public AbstractParamDialog() {
	    super();
	    initialize();
	}
	
	/**
	 * @param arg0
	 * @throws HeadlessException
	 */
	public AbstractParamDialog(Frame parent, boolean modal, String title, String rootName) throws HeadlessException {
		super(parent, modal);
		initialize();
		this.setTitle(title);
		getRootNode().setUserObject(rootName);
	}
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		// enables the options dialog to be in front, but an modal dialog
		// stays on top of the main application window, but doesn't block childs
		// Examples of childs: help window and client certificate viewer
		this.setModalityType(ModalityType.DOCUMENT_MODAL);
		
        this.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
	    if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
	    	this.setSize(500, 375);
	    }
        this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        this.setContentPane(getJContentPane());
	}
	/**

	 * This method initializes jContentPane	

	 * 	

	 * @return javax.swing.JPanel	

	 */    
	private javax.swing.JPanel getJContentPane() {
		if (jContentPane == null) {
			java.awt.GridBagConstraints gridBagConstraints1 = new GridBagConstraints();

			java.awt.GridBagConstraints gridBagConstraints14 = new GridBagConstraints();

			java.awt.GridBagConstraints gridBagConstraints13 = new GridBagConstraints();

			java.awt.GridBagConstraints gridBagConstraints12 = new GridBagConstraints();

			javax.swing.JLabel jLabel = new JLabel();

			jContentPane = new javax.swing.JPanel();
			jContentPane.setLayout(new GridBagLayout());
			jLabel.setName("jLabel");
			jLabel.setText("");
			gridBagConstraints12.gridx = 0;
			gridBagConstraints12.gridy = 1;
			gridBagConstraints12.ipadx = 0;
			gridBagConstraints12.ipady = 0;
			gridBagConstraints12.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints12.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints12.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints12.weightx = 1.0D;
			gridBagConstraints13.gridx = 1;
			gridBagConstraints13.gridy = 1;
			gridBagConstraints13.ipadx = 0;
			gridBagConstraints13.ipady = 0;
			gridBagConstraints13.fill = java.awt.GridBagConstraints.NONE;
			gridBagConstraints13.anchor = java.awt.GridBagConstraints.EAST;
			gridBagConstraints13.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints14.gridx = 2;
			gridBagConstraints14.gridy = 1;
			gridBagConstraints14.ipadx = 0;
			gridBagConstraints14.ipady = 0;
			gridBagConstraints14.anchor = java.awt.GridBagConstraints.EAST;
			gridBagConstraints14.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints1.weightx = 1.0;
			gridBagConstraints1.weighty = 1.0;
			gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints1.gridwidth = 3;
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.gridy = 0;
			jContentPane.add(getJSplitPane(), gridBagConstraints1);
			jContentPane.add(jLabel, gridBagConstraints12);
			jContentPane.add(getBtnOK(), gridBagConstraints13);
			jContentPane.add(getBtnCancel(), gridBagConstraints14);
		}
		return jContentPane;
	}
	

	/**
	 * This method initializes btnOK	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getBtnOK() {
		if (btnOK == null) {
			btnOK = new JButton();
			btnOK.setName("btnOK");
			btnOK.setText(Constant.messages.getString("all.button.ok"));
			btnOK.addActionListener(new java.awt.event.ActionListener() { 

				public void actionPerformed(java.awt.event.ActionEvent e) {    

					try {
					    validateParam();
					    saveParam();
					    exitResult = JOptionPane.OK_OPTION;
					    
					    AbstractParamDialog.this.setVisible(false);
					    
						
					} catch (Exception ex) {
					    View.getSingleton().showWarningDialog(ex.getMessage());
					}
					
				}
			});

		}
		return btnOK;
	}
	/**
	 * This method initializes btnCancel	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	protected JButton getBtnCancel() {
		if (btnCancel == null) {
			btnCancel = new JButton();
			btnCancel.setName("btnCancel");
			btnCancel.setText(Constant.messages.getString("all.button.cancel"));
			btnCancel.addActionListener(new java.awt.event.ActionListener() { 

				public void actionPerformed(java.awt.event.ActionEvent e) {    

				   exitResult = JOptionPane.CANCEL_OPTION;
				   AbstractParamDialog.this.setVisible(false);
				}
			});

		}
		return btnCancel;
	}
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanel() {
		if (jPanel == null) {
			java.awt.GridBagConstraints gridBagConstraints7 = new GridBagConstraints();

			java.awt.GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.gridwidth = 2;

			java.awt.GridBagConstraints gridBagConstraints2 = new GridBagConstraints();

			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			jPanel.setName("jPanel");
			gridBagConstraints2.weightx = 1.0;
			gridBagConstraints2.weighty = 1.0;
			gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints5.gridx = 0;
			gridBagConstraints5.gridy = 1;
			gridBagConstraints5.ipadx = 0;
			gridBagConstraints5.ipady = 0;
			gridBagConstraints5.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints5.weightx = 1.0D;
			gridBagConstraints5.weighty = 1.0D;
			gridBagConstraints5.insets = new Insets(2, 5, 5, 0);
			gridBagConstraints5.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints7.weightx = 1.0;
			gridBagConstraints7.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints7.gridx = 0;
			gridBagConstraints7.gridy = 0;
			gridBagConstraints7.insets = new Insets(2, 5, 5, 0);
			jPanel.add(getPanelHeadline(), gridBagConstraints7);
			jPanel.add(getPanelParam(), gridBagConstraints5);
			GridBagConstraints gbc_button = new GridBagConstraints();
			gbc_button.insets = new Insets(0, 5, 0, 5);
			gbc_button.gridx = 1;
			gbc_button.gridy = 0;
			jPanel.add(getHelpButton(), gbc_button);
		}
		return jPanel;
	}

	/**
	 * This method initializes jSplitPane	
	 * 	
	 * @return javax.swing.JSplitPane	
	 */    
	private JSplitPane getJSplitPane() {
		if (jSplitPane == null) {
			jSplitPane = new JSplitPane();
			jSplitPane.setContinuousLayout(true);
			jSplitPane.setVisible(true);
			jSplitPane.setRightComponent(getJPanel1());
			jSplitPane.setDividerLocation(175);
			jSplitPane.setDividerSize(3);
			jSplitPane.setResizeWeight(0.3D);
			jSplitPane.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED));
			jSplitPane.setLeftComponent(getJScrollPane());
		}
		return jSplitPane;
	}
	/**
	 * This method initializes treeParam	
	 * 	
	 * @return javax.swing.JTree	
	 */    
	private JTree getTreeParam() {
		if (treeParam == null) {
			treeParam = new JTree();
			treeParam.setModel(getTreeModel());
			treeParam.setShowsRootHandles(true);
			treeParam.setRootVisible(true);
			treeParam.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() { 

				public void valueChanged(javax.swing.event.TreeSelectionEvent e) {    
					
			        DefaultMutableTreeNode node = (DefaultMutableTreeNode) getTreeParam().getLastSelectedPathComponent();
			        if (node == null) return;
			        String name = (String) node.getUserObject();
			        showParamPanel(name);
				}
			});
			DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
			renderer.setLeafIcon(null);
			renderer.setOpenIcon(null);
			renderer.setClosedIcon(null);
			treeParam.setCellRenderer(renderer);

			treeParam.setRowHeight(18);
		}
		return treeParam;
	}
	/**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			jPanel1 = new JPanel();
			jPanel1.setLayout(new CardLayout());
			jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0,0,0,0));
			jPanel1.add(getJScrollPane1(), getJScrollPane1().getName());
		}
		return jPanel1;
	}
	/**
	 * This method initializes panelParam	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	protected JPanel getPanelParam() {
		if (panelParam == null) {
			panelParam = new JPanel();
			panelParam.setLayout(new CardLayout());
			panelParam.setPreferredSize(new java.awt.Dimension(300,300));
			panelParam.setBorder(javax.swing.BorderFactory.createEmptyBorder(0,0,0,0));
		}
		return panelParam;
	}
	/**
	 * @return
	 */
	private JPanel getPanelHeadline() {
		if (panelHeadline == null) {
			panelHeadline = new JPanel();
			panelHeadline.setLayout(new BorderLayout(0, 0));
			
			txtHeadline = getTxtHeadline();
			panelHeadline.add(txtHeadline, BorderLayout.CENTER);
			
			JButton button = getHelpButton();
			panelHeadline.add(button, BorderLayout.EAST);
		}
		return panelHeadline;
	}
	
	/**
	 * This method initializes txtHeadline	
	 * 	
	 * @return javax.swing.ZapTextField	
	 */    
	private ZapTextField getTxtHeadline() {
		if (txtHeadline == null) {
			txtHeadline = new ZapTextField();
			txtHeadline.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
			txtHeadline.setEditable(false);
			txtHeadline.setEnabled(false);
			txtHeadline.setBackground(java.awt.Color.white);
			txtHeadline.setFont(new java.awt.Font("Default", java.awt.Font.BOLD, 12));
		}
		return txtHeadline;
	}
	

	/**
	 * This method initializes treeModel	
	 * 	
	 * @return javax.swing.tree.DefaultTreeModel	
	 */    
	private DefaultTreeModel getTreeModel() {
		if (treeModel == null) {
			treeModel = new DefaultTreeModel(getRootNode());
			treeModel.setRoot(getRootNode());
		}
		return treeModel;
	}
	/**
	 * This method initializes rootNode	
	 * 	
	 * @return javax.swing.tree.DefaultMutableTreeNode	
	 */    
	protected DefaultMutableTreeNode getRootNode() {
		if (rootNode == null) {
			rootNode = new DefaultMutableTreeNode("Root");
		}
		return rootNode;
	}
	
	private DefaultMutableTreeNode addParamNode(String[] paramSeq) {
	    String param = null;
	    DefaultMutableTreeNode parent = getRootNode();
	    DefaultMutableTreeNode child = null;
	    DefaultMutableTreeNode result = null;
	    
	    for (int i=0; i<paramSeq.length; i++) {
	        param = paramSeq[i];
	        result = null;
	        for (int j=0; j<parent.getChildCount(); j++) {
	            child = (DefaultMutableTreeNode) parent.getChildAt(j);
	            if (child.toString().equalsIgnoreCase(param)) {
	                result = child;
	                break;
	            }
	        }
	        
	        if (result == null) {
	            result = new DefaultMutableTreeNode(param);
	            parent.add(result);
	        }

	        parent = result;
	    }
	    
	    return parent;
	        
	 
	}
	
	/**
	 * If multiple name use the same panel
	 * @param parentParams
	 * @param name
	 * @param panel
	 */
	// ZAP: Added sort option
	public void addParamPanel(String[] parentParams, String name, AbstractParamPanel panel, boolean sort) {
	    if (parentParams != null) {
	        DefaultMutableTreeNode parent = addParamNode(parentParams);
	        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(name);

	        boolean added = false;
	        if (sort) {
		        for (int i=0; i < parent.getChildCount(); i++) {
		        	if (name.compareToIgnoreCase(parent.getChildAt(i).toString()) < 0) {
		    	        parent.insert(newNode, i);
		        		added = true;
		        		break;
		        	}
		        }
	        }
	        if (! added) {
	        	parent.add(newNode);
	        }
	    } else {
	        // No need to create node.  This is the root panel.
	    }
	    panel.setName(name);
        getPanelParam().add(panel, name);
        tablePanel.put(name, panel);
	    
	}
	
	public void addParamPanel(String[] parentParams, AbstractParamPanel panel, boolean sort) {
	    addParamPanel(parentParams, panel.getName(), panel, sort);
	}
	
	// ZAP: Made public so that other classes can specify which panel is displayed
	public void showParamPanel(String name) {
	    if (name == null || name.equals("")) return;

	    // exit if panel name not found. 
	    AbstractParamPanel panel = tablePanel.get(name);
	    if (panel == null) return;

        // ZAP: show the last selected panel
        nameLastSelectedPanel = name;

        getPanelHeadline();
        getTxtHeadline().setText(name);
        getHelpButton().setVisible(panel.getHelpIndex() != null);
        getShowHelpAction().setHelpIndex(panel.getHelpIndex());

        CardLayout card = (CardLayout) getPanelParam().getLayout();
        card.show(getPanelParam(), name);
	}

	public void initParam(Object obj) {
	    paramObject = obj;
	    Enumeration<AbstractParamPanel> en = tablePanel.elements();
	    AbstractParamPanel panel = null;
	    while(en.hasMoreElements()) {
	        panel = en.nextElement();
	        panel.initParam(obj);
	    }
	    
	}

	/**
	 * This method is to be overrided by subclass.
	 *
	 */
	public void validateParam() throws Exception {
	    Enumeration<AbstractParamPanel> en = tablePanel.elements();
	    AbstractParamPanel panel = null;
	    while(en.hasMoreElements()) {
	        panel = en.nextElement();
	        panel.validateParam(paramObject);
	    }
	}

	
	/**
	 * This method is to be overrided by subclass.
	 *
	 */
	public void saveParam() throws Exception {
	    Enumeration<AbstractParamPanel> en = tablePanel.elements();
	    AbstractParamPanel panel = null;
	    while(en.hasMoreElements()) {
	        panel = en.nextElement();
	        panel.saveParam(paramObject);
	    }
	}
	
	
	protected void expandRoot() {
	    getTreeParam().expandPath(new TreePath(getRootNode()));
	}
	
	public int showDialog(boolean showRoot) {
		return showDialog(showRoot, null);
	}
	
	// ZAP: Added option to specify panel - note this only supports one level at the moment
	// ZAP: show the last selected panel
	public int showDialog(boolean showRoot, String panel) {
        expandRoot();
        try {
            DefaultMutableTreeNode node = null;
            if (panel != null) {
            	node = getTreeNodeFromPanelName(panel);
            }
            if (node == null) {
            	if (nameLastSelectedPanel != null) {
            		node = getTreeNodeFromPanelName(nameLastSelectedPanel);
            	} else if (showRoot) {
	                node = (DefaultMutableTreeNode) getTreeModel().getRoot();
	            } else if (((DefaultMutableTreeNode) getTreeModel().getRoot()).getChildCount() > 0){
	                node = (DefaultMutableTreeNode) ((DefaultMutableTreeNode) getTreeModel().getRoot()).getChildAt(0);
	            }
            }
            if (node != null) {
            	showParamPanel(node.toString());
            	getTreeParam().setSelectionPath(new TreePath(node.getPath()));
            }
        } catch (Exception e) {
        	// ZAP: log errors
        	log.error(e.getMessage(), e);
        }
        
        this.setVisible(true);
	    return exitResult;
	
	}

	// ZAP: show the last selected panel
	private DefaultMutableTreeNode getTreeNodeFromPanelName(String panel) {
		DefaultMutableTreeNode node = null;
		
		Enumeration<DefaultMutableTreeNode> children = ((DefaultMutableTreeNode) getTreeModel().getRoot()).children();
		while (children.hasMoreElements()) {
			DefaultMutableTreeNode child = children.nextElement();
			if (panel.equals(child.toString())) {
				node = child;
				break;
			}
		}
		return node;
	}

	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */    
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getTreeParam());
			jScrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0,0,0,0));
		}
		return jScrollPane;
	}
	/**
	 * This method initializes jScrollPane1	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */    
	private JScrollPane getJScrollPane1() {
		if (jScrollPane1 == null) {
			jScrollPane1 = new JScrollPane();
			jScrollPane1.setName("jScrollPane1");
			jScrollPane1.setViewportView(getJPanel());
			jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			jScrollPane1.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		}
		return jScrollPane1;
	}
	/**
	 * This method initializes the help button, if any button can be applied
	 * @return
	 */
	private JButton getHelpButton() {
		if (btnHelp == null) {
			btnHelp = new JButton();
			btnHelp.setBorder(null);
			btnHelp.setIcon(new ImageIcon(getClass().getResource("/resource/icon/16/201.png"))); // help icon
			btnHelp.addActionListener(getShowHelpAction());
			btnHelp.setToolTipText(Constant.messages.getString("menu.help"));
		}
		return btnHelp;
	}
	
	private ShowHelpAction getShowHelpAction() {
		if (showHelpAction == null) {
			showHelpAction  = new ShowHelpAction();
		}
		return showHelpAction;
	}
	
	/**
	 * Displays the current help by index ...
	 */
	private static final class ShowHelpAction implements ActionListener {

		private String helpIndex = null;

		@Override
		public void actionPerformed(ActionEvent e) {
			if (helpIndex != null) {
				ExtensionHelp.showHelp(helpIndex);
			}
		}

		public void setHelpIndex(String helpIndex) {
			this.helpIndex = helpIndex;
		}

	}
}  //  @jve:decl-index=0:visual-constraint="73,11"
