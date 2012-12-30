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
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.apache.log4j.Logger;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.SiteMap;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.view.SiteMapPanel;

/**
 * Custom renderer for {@link SiteMapPanel} to set custom icons
 * and tooltips. If you want tooltips you have to enable them via:
 * <code>ToolTipManager.sharedInstance().registerComponent(tree);</code>
 */
public class SiteMapTreeCellRenderer extends DefaultTreeCellRenderer {
	
	private static final ImageIcon ROOT_ICON = new ImageIcon(DefaultTreeCellRenderer.class.getResource("/resource/icon/16/094.png"));
	private static final ImageIcon LEAF_IN_SCOPE_ICON = new ImageIcon(DefaultTreeCellRenderer.class.getResource("/resource/icon/fugue/document-target.png"));
	private static final ImageIcon LEAF_ICON = new ImageIcon(DefaultTreeCellRenderer.class.getResource("/resource/icon/fugue/document.png"));
	private static final ImageIcon FOLDER_OPEN_IN_SCOPE_ICON = new ImageIcon(DefaultTreeCellRenderer.class.getResource("/resource/icon/fugue/folder-horizontal-open-target.png"));
	private static final ImageIcon FOLDER_OPEN_ICON = new ImageIcon(DefaultTreeCellRenderer.class.getResource("/resource/icon/fugue/folder-horizontal-open.png"));
	private static final ImageIcon FOLDER_CLOSED_IN_SCOPE_ICON = new ImageIcon(DefaultTreeCellRenderer.class.getResource("/resource/icon/fugue/folder-horizontal-target.png"));
	private static final ImageIcon FOLDER_CLOSED_ICON = new ImageIcon(DefaultTreeCellRenderer.class.getResource("/resource/icon/fugue/folder-horizontal.png"));

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

		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		
		SiteNode node = null;
		if (value instanceof SiteNode) {
			node = (SiteNode) value;
		}
		
		if (node != null) {
			// folder / file icons with scope 'target' if relevant
			if (node.isRoot()) {
				setIcon(ROOT_ICON);	// 'World' icon
			} else if (leaf) {
				if (node.isIncludedInScope() && ! node.isExcludedFromScope()) {
					setIcon(LEAF_IN_SCOPE_ICON);
				} else {
					setIcon(LEAF_ICON);
				}
			} else {
				if  (expanded) {
					if (node.isIncludedInScope() && ! node.isExcludedFromScope()) {
						setIcon(FOLDER_OPEN_IN_SCOPE_ICON);
					} else {
						setIcon(FOLDER_OPEN_ICON);
					}
				} else {
					if (node.isIncludedInScope() && ! node.isExcludedFromScope()) {
						setIcon(FOLDER_CLOSED_IN_SCOPE_ICON);
					} else {
						setIcon(FOLDER_CLOSED_ICON);
					}
				}
			}

			// ZAP: Call SiteMapListeners
	        for (SiteMapListener listener : listeners) {
	        	listener.onReturnNodeRendererComponent(this, leaf, node);
	        }
		}

		return this;
	}
	
	/**
	 * Extract HttpMessage out of {@link SiteMap} node.
	 * 
	 * @param value
	 * @return
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
