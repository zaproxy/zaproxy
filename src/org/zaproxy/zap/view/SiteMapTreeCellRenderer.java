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

import java.awt.Component;
import java.awt.Dimension;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.apache.log4j.Logger;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.SiteMap;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.view.SiteMapPanel;
import org.zaproxy.zap.utils.DisplayUtils;

/**
 * Custom renderer for {@link SiteMapPanel} to set custom icons
 * and tooltips. If you want tooltips you have to enable them via:
 * <code>ToolTipManager.sharedInstance().registerComponent(tree);</code>
 */
public class SiteMapTreeCellRenderer extends DefaultTreeCellRenderer {
	
	private static final ImageIcon ROOT_ICON = new ImageIcon(SiteMapTreeCellRenderer.class.getResource("/resource/icon/16/094.png"));
	private static final ImageIcon LEAF_IN_SCOPE_ICON = new ImageIcon(SiteMapTreeCellRenderer.class.getResource("/resource/icon/fugue/document-target.png"));
	private static final ImageIcon LEAF_ICON = new ImageIcon(SiteMapTreeCellRenderer.class.getResource("/resource/icon/fugue/document.png"));
	private static final ImageIcon FOLDER_OPEN_IN_SCOPE_ICON = new ImageIcon(SiteMapTreeCellRenderer.class.getResource("/resource/icon/fugue/folder-horizontal-open-target.png"));
	private static final ImageIcon FOLDER_OPEN_ICON = new ImageIcon(SiteMapTreeCellRenderer.class.getResource("/resource/icon/fugue/folder-horizontal-open.png"));
	private static final ImageIcon FOLDER_CLOSED_IN_SCOPE_ICON = new ImageIcon(SiteMapTreeCellRenderer.class.getResource("/resource/icon/fugue/folder-horizontal-target.png"));
	private static final ImageIcon FOLDER_CLOSED_ICON = new ImageIcon(SiteMapTreeCellRenderer.class.getResource("/resource/icon/fugue/folder-horizontal.png"));
	private static final ImageIcon DATA_DRIVEN_ICON = new ImageIcon(SiteMapTreeCellRenderer.class.getResource("/resource/icon/fugue/database.png"));
	private static final ImageIcon DATA_DRIVEN_IN_SCOPE_ICON = new ImageIcon(SiteMapTreeCellRenderer.class.getResource("/resource/icon/fugue/database-target.png"));

	private static final ImageIcon LOCK_OVERLAY_ICON = new ImageIcon(SiteMapTreeCellRenderer.class.getResource("/resource/icon/fugue/lock-overlay.png"));

	private static final long serialVersionUID = -4278691012245035225L;

	private static Logger log = Logger.getLogger(SiteMapPanel.class);

	private List<SiteMapListener> listeners;

	public SiteMapTreeCellRenderer(List<SiteMapListener> listeners) {
		this.listeners = listeners;
	}

	/**
	 * Sets custom tree node logos.
	 */
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {

		SiteNode node = null;
		if (value instanceof SiteNode) {
			node = (SiteNode) value;
		}
		
		if (node != null) {
	        if( node.isFiltered()) {
	        	// Hide the node
	            setPreferredSize( new Dimension(0, 0) );
	        } else {
	            setPreferredSize(null);	// clears the prefered size, making the node visible
	            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
	        }

			// folder / file icons with scope 'target' if relevant
			if (node.isRoot()) {
				setIcon(DisplayUtils.getScaledIcon(ROOT_ICON));	// 'World' icon
			} else {
				OverlayIcon icon;
		        if (node.isDataDriven()) {
					if (node.isIncludedInScope() && ! node.isExcludedFromScope()) {
						icon = new OverlayIcon(DATA_DRIVEN_IN_SCOPE_ICON);
					} else {
						icon = new OverlayIcon(DATA_DRIVEN_ICON);
					}
		        } else if (leaf) {
					if (node.isIncludedInScope() && ! node.isExcludedFromScope()) {
						icon = new OverlayIcon(LEAF_IN_SCOPE_ICON);
					} else {
						icon = new OverlayIcon(LEAF_ICON);
					}
				} else {
					if  (expanded) {
						if (node.isIncludedInScope() && ! node.isExcludedFromScope()) {
							icon = new OverlayIcon(FOLDER_OPEN_IN_SCOPE_ICON);
						} else {
							icon = new OverlayIcon(FOLDER_OPEN_ICON);
						}
					} else {
						if (node.isIncludedInScope() && ! node.isExcludedFromScope()) {
							icon = new OverlayIcon(FOLDER_CLOSED_IN_SCOPE_ICON);
						} else {
							icon = new OverlayIcon(FOLDER_CLOSED_ICON);
						}
					}
				}
				if (node.getParent().isRoot() && node.getNodeName().startsWith("https://")) {
					// Add lock icon to site nodes with https
					icon.add(LOCK_OVERLAY_ICON);
				}
				setIcon(DisplayUtils.getScaledIcon(icon));
			}

	        for (SiteMapListener listener : listeners) {
	        	listener.onReturnNodeRendererComponent(this, leaf, node);
	        }
		}

		return this;
	}
	
	/**
	 * Extracts a HistoryReference out of {@link SiteMap} node.
	 * 
	 * @param value the node
	 * @return the {@code HistoryReference}, or {@code null} if it has none
	 */
	public HistoryReference getHistoryReferenceFromNode(Object value) {
		SiteNode node = null;
		if (value instanceof SiteNode) {
			node = (SiteNode) value;
	
			if (node.getHistoryReference() != null) {
				try {
					return node.getHistoryReference();
				} catch (Exception e) {
					log.warn(e.getMessage(), e);
				}
			}
		}
		return null;
	}
}
