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
package org.zaproxy.zap.extension.brk;

import java.awt.CardLayout;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.view.View;
/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class BreakPointsPanel extends AbstractPanel {
	
	private static final long serialVersionUID = 1L;

	public static final String PANEL_NAME = "breakPoints";
	
	private javax.swing.JPanel panelCommand = null;
	private javax.swing.JLabel jLabel = null;
	private JScrollPane jScrollPane = null;
	private javax.swing.JList breakPointList = null;
	private DefaultListModel model = new DefaultListModel();
	private PopupMenuRemove popupMenuDelete = null;

    /**
     * 
     */
    public BreakPointsPanel() {
        super();
 		initialize();
    }

    /**
     * @param isEditable
     */
    /*
    public BreakPointsPanel(boolean isEditable) {
        super(isEditable);
 		initialize();
    }
    */
	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private  void initialize() {
        this.setLayout(new CardLayout());
        this.setSize(474, 251);
        this.setName(Constant.messages.getString("brk.panel.title"));
		this.setIcon(new ImageIcon(getClass().getResource("/resource/icon/16/151.png")));	// 'red circle' icon
        this.add(getPanelCommand(), getPanelCommand().getName());
	}
	/**

	 * This method initializes panelCommand	

	 * 	

	 * @return javax.swing.JPanel	

	 */    
	private javax.swing.JPanel getPanelCommand() {
		if (panelCommand == null) {

			panelCommand = new javax.swing.JPanel();
			panelCommand.setLayout(new java.awt.GridBagLayout());
			panelCommand.setName(Constant.messages.getString("brk.panel.title"));
			
			jLabel = getJLabel();
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();

			// Better without this?
			//jLabel.setText("Break Points:");
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.gridy = 0;
			gridBagConstraints1.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints1.weightx = 1.0D;
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.gridy = 1;
			gridBagConstraints2.weightx = 1.0;
			gridBagConstraints2.weighty = 1.0;
			gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints2.insets = new java.awt.Insets(0,0,0,0);
			gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTHWEST;
			

			//panelCommand.add(jLabel, gridBagConstraints1);
			panelCommand.add(getJScrollPane(), gridBagConstraints2);
			
			//jLabel.addMouseListener(new MouseAdapter() {
			panelCommand.addMouseListener(new MouseAdapter() {
				public void mouseReleased(MouseEvent event){
					if (event.isPopupTrigger())
						getPopupMenuDelete().setVisible(true);
					}
				});

		}
		return panelCommand;
	}


	/**
	 * This method initializes jLabel	
	 * 	
	 * @return javax.swing.JLabel	
	 */    
	private javax.swing.JLabel getJLabel() {
		if (jLabel == null) {
			jLabel = new javax.swing.JLabel();
			jLabel.setText(" ");
		}
		return jLabel;
	}

	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getBreakPoints());
			jScrollPane.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
			jScrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		}
		return jScrollPane;
	}

	protected JList getBreakPoints() {
		if (breakPointList == null) {
			breakPointList = new JList(model);
			breakPointList.setName(PANEL_NAME);
			breakPointList.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
			breakPointList.setDoubleBuffered(true);
			breakPointList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
			breakPointList.addMouseListener(new java.awt.event.MouseAdapter() { 
			    public void mousePressed(java.awt.event.MouseEvent e) {

					if (SwingUtilities.isRightMouseButton(e)) {

						// Select list item
					    int Idx = breakPointList.locationToIndex( e.getPoint() );
					    if ( Idx >= 0 ) {
					    	Rectangle Rect = breakPointList.getCellBounds( Idx, Idx );
					    	Idx = Rect.contains( e.getPoint().x, e.getPoint().y ) ? Idx : -1;
					    }
					    if ( Idx < 0 || !breakPointList.getSelectionModel().isSelectedIndex( Idx ) ) {
					    	breakPointList.getSelectionModel().clearSelection();
					    	if ( Idx >= 0 ) {
					    		breakPointList.getSelectionModel().setSelectionInterval( Idx, Idx );
					    	}
					    }
						
						View.getSingleton().getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
			        }			    	
			    }
			});
		}
		return breakPointList;
	}
	
	private void addToSortedModel(String s) {
		for (int i = 0; i < model.getSize(); i++) {
			int cmp = s.compareTo((String)model.elementAt(i)); 
			if (cmp < 0) {
				model.add(i, s);
				return;
			} else if (cmp == 0) {
				// Already matches, so ignore
				return;
			}
		}
		model.add(model.getSize(), s);
	}
	
	void addBreakPoint(final String s) {
		if (EventQueue.isDispatchThread()) {
			addToSortedModel(s);
			return;
		}
		try {
			EventQueue.invokeAndWait(new Runnable() {
				public void run() {
					addToSortedModel(s);
				}
			});
		} catch (Exception e) {
		}
	    
	}

	public void removeBreakPoint(final String s) {
		if (EventQueue.isDispatchThread()) {
			model.removeElement(s);
			return;
		}
		try {
			EventQueue.invokeAndWait(new Runnable() {
				public void run() {
					model.removeElement(s);
				}
			});
		} catch (Exception e) {
		}
	    
	}

	private PopupMenuRemove getPopupMenuDelete() {
		if (popupMenuDelete == null) {
			popupMenuDelete = new PopupMenuRemove();
		}
		return popupMenuDelete;
	}
}
