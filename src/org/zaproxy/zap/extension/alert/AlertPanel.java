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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
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
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.HttpPanel;
import org.zaproxy.zap.extension.search.SearchMatch;
import org.zaproxy.zap.utils.DisplayUtils;
import org.zaproxy.zap.view.DeselectableButtonGroup;
import org.zaproxy.zap.view.LayoutHelper;
import org.zaproxy.zap.view.ZapToggleButton;
import org.zaproxy.zap.view.messagecontainer.http.DefaultSelectableHistoryReferencesContainer;
import org.zaproxy.zap.view.messagecontainer.http.SelectableHistoryReferencesContainer;

public class AlertPanel extends AbstractPanel {
	
	public static final String ALERT_TREE_PANEL_NAME = "treeAlert";

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(AlertPanel.class);
	
	private ViewDelegate view = null;
	private JTree treeAlert = null;
	
	private JScrollPane paneScroll = null;

	private JPanel panelCommand = null;
	private JToolBar panelToolbar = null;
	private JSplitPane splitPane = null;
	private AlertViewPanel alertViewPanel = null;
	private ZapToggleButton scopeButton = null;

    /**
     * The {@code AlertTreeModel} that holds the alerts of a selected "Sites" tree node when the filter
     * "Link with Sites selection" is enabled, otherwise it will be empty.
     * <p>
     * The tree model is lazily initialised (in the method {@code getLinkWithSitesTreeModel()}).
     * </p>
     * 
     * @see #getLinkWithSitesTreeModel()
     * @see #setLinkWithSitesTreeSelection(boolean)
     */
    private AlertTreeModel linkWithSitesTreeModel;

    /**
     * The tree selection listener that triggers the filter "Link with Sites selection" of the "Alerts" tree based on selected
     * "Sites" tree node.
     * <p>
     * The listener is lazily initialised (in the method {@code getLinkWithSitesTreeSelectionListener()}).
     * </p>
     * 
     * @see #getLinkWithSitesTreeSelectionListener()
     * @see #setLinkWithSitesTreeSelection(boolean)
     */
    private LinkWithSitesTreeSelectionListener linkWithSitesTreeSelectionListener;

    /**
     * The toggle button that enables/disables the "Alerts" tree filtering based on the "Sites" tree selection.
     * <p>
     * The toggle button is lazily initialised (in the method {@code getLinkWithSitesTreeButton()}).
     * </p>
     * 
     * @see #getLinkWithSitesTreeButton()
     */
    private ZapToggleButton linkWithSitesTreeButton;

    /**
     * The button group used to control mutually exclusive "Alerts" tree filtering buttons.
     * <p>
     * Any filtering buttons that are mutually exclusive should be added to this button group.
     * </p>
     */
    private DeselectableButtonGroup alertsTreeFiltersButtonGroup;

	private ExtensionAlert extension = null;
	private ExtensionHistory extHist = null; 

	
    public AlertPanel(ExtensionAlert extension) {
        super();
        this.extension = extension;
        alertsTreeFiltersButtonGroup = new DeselectableButtonGroup();
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
			
		this.setDefaultAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | Event.SHIFT_MASK, false));
		this.setMnemonic(Constant.messages.getChar("alerts.panel.mnemonic"));
		this.setShowByDefault(true);
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
			panelToolbar.setName("AlertToolbar");
			
			panelToolbar.add(getScopeButton(), LayoutHelper.getGBC(0, 0, 1, 0.0D));
			panelToolbar.add(getLinkWithSitesTreeButton(), LayoutHelper.getGBC(1, 0, 1, 0.0D));
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
			scopeButton = new ZapToggleButton();
			scopeButton.setIcon(new ImageIcon(AlertPanel.class.getResource("/resource/icon/fugue/target-grey.png")));
			scopeButton.setToolTipText(Constant.messages.getString("history.scope.button.unselected"));
			scopeButton.setSelectedIcon(new ImageIcon(AlertPanel.class.getResource("/resource/icon/fugue/target.png")));
			scopeButton.setSelectedToolTipText(Constant.messages.getString("history.scope.button.selected"));
			DisplayUtils.scaleIcon(scopeButton);

			scopeButton.addActionListener(new java.awt.event.ActionListener() { 

				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					extension.setShowJustInScope(scopeButton.isSelected());
				}
			});
			alertsTreeFiltersButtonGroup.add(scopeButton);
		}
		return scopeButton;
	}

    /**
     * Returns the toggle button that enables/disables the "Alerts" tree filtering based on the "Sites" tree selection.
     * <p>
     * The instance variable {@code linkWithSitesTreeButton} is initialised on the first call to this method.
     * </p>
     * 
     * @see #linkWithSitesTreeButton
     * @return the toggle button that enables/disables the "Alerts" tree filtering based on the "Sites" tree selection
     */
    private JToggleButton getLinkWithSitesTreeButton() {
        if (linkWithSitesTreeButton == null) {
            linkWithSitesTreeButton = new ZapToggleButton();
            linkWithSitesTreeButton.setIcon(new ImageIcon(AlertPanel.class.getResource("/resource/icon/16/earth-grey.png")));
            linkWithSitesTreeButton.setToolTipText(Constant.messages.getString("alerts.panel.linkWithSitesSelection.unselected.button.tooltip"));
            linkWithSitesTreeButton.setSelectedIcon(new ImageIcon(AlertPanel.class.getResource("/resource/icon/16/094.png")));
            linkWithSitesTreeButton.setSelectedToolTipText(Constant.messages.getString("alerts.panel.linkWithSitesSelection.selected.button.tooltip"));
			DisplayUtils.scaleIcon(linkWithSitesTreeButton);

            linkWithSitesTreeButton.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    setLinkWithSitesTreeSelection(linkWithSitesTreeButton.isSelected());
                }
            });
            alertsTreeFiltersButtonGroup.add(linkWithSitesTreeButton);
        }
        return linkWithSitesTreeButton;
    }

    /**
     * Sets whether the "Alerts" tree is filtered, or not based on a selected "Sites" tree node.
     * <p>
     * If {@code enabled} is {@code true} only the alerts of the selected "Sites" tree node will be shown.
     * </p>
     * <p>
     * Calling this method removes the filter "Just in Scope", if enabled, as they are mutually exclusive.
     * </p>
     * 
     * @param enabled {@code true} if the "Alerts" tree should be filtered based on the "Sites" tree selection, {@code false}
     *            otherwise.
     * @see ExtensionAlert#setShowJustInScope(boolean)
     */
    public void setLinkWithSitesTreeSelection(boolean enabled) {
        linkWithSitesTreeButton.setSelected(enabled);
        if (enabled) {
            extension.setShowJustInScope(false);
            final JTree sitesTree = view.getSiteTreePanel().getTreeSite();
            final TreePath selectionPath = sitesTree.getSelectionPath();
            getTreeAlert().setModel(getLinkWithSitesTreeModel());
            if (selectionPath != null) {
                recreateLinkWithSitesTreeModel((SiteNode) selectionPath.getLastPathComponent());
            }
            sitesTree.addTreeSelectionListener(getLinkWithSitesTreeSelectionListener());
        } else {
            extension.setMainTreeModel();
            ((AlertNode) getLinkWithSitesTreeModel().getRoot()).removeAllChildren();
            view.getSiteTreePanel().getTreeSite().removeTreeSelectionListener(getLinkWithSitesTreeSelectionListener());
        }
    }

    /**
     * Returns the {@code AlertTreeModel} that holds the alerts of the selected "Sites" tree node when the filter
     * "Link with Sites selection" is enabled, otherwise it will be empty.
     * <p>
     * The instance variable {@code linkWithSitesTreeModel} is initialised on the first call to this method.
     * </p>
     * 
     * @see #linkWithSitesTreeModel
     * @return the {@code AlertTreeModel} that holds the alerts of the selected "Sites" tree node when the filter
     *         "Link with Sites selection" is enabled, otherwise it will be empty
     */
    private AlertTreeModel getLinkWithSitesTreeModel() {
        if (linkWithSitesTreeModel == null) {
            linkWithSitesTreeModel = new AlertTreeModel();
        }
        return linkWithSitesTreeModel;
    }

    /**
     * Recreates the {@code linkWithSitesTreeModel} with the alerts of the given {@code siteNode}.
     * <p>
     * If the given {@code siteNode} doesn't contain any alerts the resulting model will only contain the root node, otherwise
     * the model will contain the root node and the alerts returned by the method {@code SiteNode#getAlerts()} although if the
     * node has an HistoryReference only the alerts whose URI is equal to the URI returned by the method
     * {@code HistoryReference#getURI()} will be included.
     * </p>
     * <p>
     * After a call to this method the number of total alerts will be recalculated by calling the method
     * {@code ExtensionAlert#recalcAlerts()}.
     * </p>
     * 
     * @param siteNode the "Sites" tree node that will be used to recreate the alerts tree model.
     * @throws IllegalArgumentException if {@code siteNode} is {@code null}.
     * @see #linkWithSitesTreeModel
     * @see #setLinkWithSitesTreeSelection
     * @see Alert
     * @see ExtensionAlert#recalcAlerts()
     * @see HistoryReference
     * @see SiteNode#getAlerts()
     */
    private void recreateLinkWithSitesTreeModel(SiteNode siteNode) {
        if (siteNode == null) {
            throw new IllegalArgumentException("Parameter siteNode must not be null.");
        }
        ((AlertNode) getLinkWithSitesTreeModel().getRoot()).removeAllChildren();
        if (siteNode.isRoot()) {
            getLinkWithSitesTreeModel().reload();
            extension.recalcAlerts();
            return;
        }
        String uri = null;
        HistoryReference historyReference = siteNode.getHistoryReference();
        if (historyReference != null) {
            uri = historyReference.getURI().toString();
        }
        for (Alert alert : siteNode.getAlerts()) {
            // Just show ones for this node
            if (uri != null && !alert.getUri().equals(uri)) {
                continue;
            }
            getLinkWithSitesTreeModel().addPath(alert);
        }

        getLinkWithSitesTreeModel().reload();
        expandRootChildNodes();
        extension.recalcAlerts();
    }

    /**
     * Returns the tree selection listener that triggers the filter of the "Alerts" tree based on a selected {@code SiteNode}.
     * <p>
     * The instance variable {@code linkWithSitesTreeSelectionListener} is initialised on the first call to this method.
     * </p>
     * 
     * @see #linkWithSitesTreeSelectionListener
     * @return the tree selection listener that triggers the filter of the "Alerts" tree based on a selected {@code SiteNode}.
     */
    private LinkWithSitesTreeSelectionListener getLinkWithSitesTreeSelectionListener() {
        if (linkWithSitesTreeSelectionListener == null) {
            linkWithSitesTreeSelectionListener = new LinkWithSitesTreeSelectionListener();
        }
        return linkWithSitesTreeSelectionListener;
    }

	/**
	 * This method initializes treeAlert	
	 * 	
	 * @return javax.swing.JTree	
	 */    
	JTree getTreeAlert() {
		if (treeAlert == null) {
			treeAlert = new JTree() {

				private static final long serialVersionUID = 1L;

				@Override
				public Point getPopupLocation(final MouseEvent event) {
					if (event != null) {
						// Select item on right click
						TreePath tp = treeAlert.getPathForLocation(event.getX(), event.getY());
						if (tp != null) {
							// Only select a new item if the current item is not
							// already selected - this is to allow multiple items
							// to be selected
							if (!treeAlert.getSelectionModel().isPathSelected(tp)) {
								treeAlert.getSelectionModel().setSelectionPath(tp);
							}
						}
					}
					return super.getPopupLocation(event);
				}
			};
			treeAlert.setName(ALERT_TREE_PANEL_NAME);
			treeAlert.setShowsRootHandles(true);
			treeAlert.setBorder(javax.swing.BorderFactory.createEmptyBorder(0,0,0,0));
			treeAlert.setComponentPopupMenu(new JPopupMenu() {

				private static final long serialVersionUID = 1L;

				@Override
				public void show(Component invoker, int x, int y) {
					final int countSelectedNodes = treeAlert.getSelectionCount();
					final ArrayList<HistoryReference> uniqueHistoryReferences = new ArrayList<>(countSelectedNodes);
					if (countSelectedNodes > 0) {
						SortedSet<Integer> historyReferenceIdsAdded = new TreeSet<>();
						for (TreePath path : treeAlert.getSelectionPaths()) {
							final AlertNode node = (AlertNode) path.getLastPathComponent();
							final Object userObject = node.getUserObject();
							if (userObject instanceof Alert) {
								HistoryReference historyReference = ((Alert) userObject).getHistoryRef();
								if (historyReference != null && !historyReferenceIdsAdded
										.contains(Integer.valueOf(historyReference.getHistoryId()))) {
									historyReferenceIdsAdded.add(Integer.valueOf(historyReference.getHistoryId()));
									uniqueHistoryReferences.add(historyReference);
								}
							}
						}
						uniqueHistoryReferences.trimToSize();
					}
					SelectableHistoryReferencesContainer messageContainer = new DefaultSelectableHistoryReferencesContainer(
							treeAlert.getName(),
							treeAlert,
							Collections.<HistoryReference> emptyList(),
							uniqueHistoryReferences);
					view.getPopupMenu().show(messageContainer, x, y);
				}
			});

			treeAlert.addMouseListener(new java.awt.event.MouseAdapter() { 

				@Override
				public void mouseClicked(java.awt.event.MouseEvent e) {
				    if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() > 1) {
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
						    treeAlert.requestFocusInWindow();
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
		if (EventQueue.isDispatchThread()) {
		    getTreeAlert().expandPath(new TreePath(getTreeAlert().getModel().getRoot()));
		    return;
		}
		try {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
				    expandRoot();
				}
			});
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
    /**
     * Expands all child nodes of the root node.
     * <p>
     * This method should be called only from the EDT (Event Dispatch Thread).
     * </p>
     */
    public void expandRootChildNodes() {
        TreeNode root = (TreeNode) getTreeAlert().getModel().getRoot();
        if (root == null) {
            return;
        }
        TreePath basePath = new TreePath(root);
        for (int i = 0; i < root.getChildCount(); ++i) {
            getTreeAlert().expandPath(basePath.pathByAddingChild(root.getChildAt(i)));
        }
    }

	private void setMessage(HttpMessage msg, String highlight) {
        getView().displayMessage(msg);
        if (msg == null) {
            return;
        }

	    if (!msg.getResponseHeader().isEmpty()) {
	        HttpPanel requestPanel = getView().getRequestPanel();
	        HttpPanel responsePanel = getView().getResponsePanel();

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

    /**
     * The tree selection listener that triggers the filter of the "Alerts" tree by calling the method
     * {@code recreateLinkWithSitesTreeModel(SiteNode)} with the selected {@code SiteNode} as parameter.
     * 
     * @see #recreateLinkWithSitesTreeModel(SiteNode)
     */
    private class LinkWithSitesTreeSelectionListener implements TreeSelectionListener {

        @Override
        public void valueChanged(TreeSelectionEvent e) {
            recreateLinkWithSitesTreeModel((SiteNode) e.getPath().getLastPathComponent());
        }
    }

}