/*
 * Created on 2004�~7��4��
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
package org.parosproxy.paros.extension.history;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;

import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.HttpPanel;
import org.parosproxy.paros.view.View;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class LogPanel extends AbstractPanel implements Runnable {
	private static final long serialVersionUID = 1L;
	private javax.swing.JScrollPane scrollLog = null;
	private javax.swing.JList listLog = null;
	// ZAP: Added history (filter) toolbar
	private javax.swing.JPanel historyPanel = null;
	private javax.swing.JToolBar panelToolbar = null;
	private JButton filterButton = null;
	private JLabel filterStatus = null;
	
	private HttpPanel requestPanel = null;
	private HttpPanel responsePanel = null;
    private ExtensionHistory extension = null;
	
	/**
	 * This is the default constructor
	 */
	public LogPanel() {
		super();
		initialize();
	}
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private  void initialize() {
		this.setLayout(new BorderLayout());
		this.setSize(600, 200);
		//this.add(getScrollLog(), java.awt.BorderLayout.CENTER);
		this.add(getHistoryPanel(), java.awt.BorderLayout.CENTER);
	}
    
    void setExtension(ExtensionHistory extension) {
        this.extension = extension;
    }
    
	/**

	 * This method initializes scrollLog	

	 * 	

	 * @return javax.swing.JScrollPane	

	 */    
	private javax.swing.JScrollPane getScrollLog() {
		if (scrollLog == null) {
			scrollLog = new javax.swing.JScrollPane();
			scrollLog.setViewportView(getListLog());
			scrollLog.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			scrollLog.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			scrollLog.setPreferredSize(new java.awt.Dimension(800,200));
			scrollLog.setName("scrollLog");
		}
		return scrollLog;
	}

	private javax.swing.JPanel getHistoryPanel() {
		if (historyPanel == null) {

			historyPanel = new javax.swing.JPanel();
			historyPanel.setLayout(new java.awt.GridBagLayout());
			historyPanel.setName("History Panel");
			
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
			
			historyPanel.add(this.getPanelToolbar(), gridBagConstraints1);
			historyPanel.add(getScrollLog(), gridBagConstraints2);

		}
		return historyPanel;
	}
	/**/

	private javax.swing.JToolBar getPanelToolbar() {
		if (panelToolbar == null) {
			
			panelToolbar = new javax.swing.JToolBar();
			panelToolbar.setLayout(new java.awt.GridBagLayout());
			panelToolbar.setEnabled(true);
			panelToolbar.setFloatable(false);
			panelToolbar.setRollover(true);
			panelToolbar.setPreferredSize(new java.awt.Dimension(800,30));
			panelToolbar.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
			panelToolbar.setName("History Toolbar");
			
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			GridBagConstraints gridBagConstraintsX = new GridBagConstraints();

			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.gridy = 0;
			gridBagConstraints1.insets = new java.awt.Insets(0,0,0,0);
			gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
			
			// TODO this shouldnt push the filter button off the lhs
			gridBagConstraints2.gridx = 1;
			gridBagConstraints2.gridy = 0;
			gridBagConstraints2.insets = new java.awt.Insets(0,0,0,0);
			gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;

			gridBagConstraints3.gridx = 2;
			gridBagConstraints3.gridy = 0;
			gridBagConstraints3.insets = new java.awt.Insets(0,0,0,0);
			gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;

			gridBagConstraints4.gridx = 3;
			gridBagConstraints4.gridy = 0;
			gridBagConstraints4.insets = new java.awt.Insets(0,0,0,0);
			gridBagConstraints4.anchor = java.awt.GridBagConstraints.WEST;

			gridBagConstraints5.gridx = 4;
			gridBagConstraints5.gridy = 0;
			gridBagConstraints5.insets = new java.awt.Insets(0,0,0,0);
			gridBagConstraints5.anchor = java.awt.GridBagConstraints.WEST;

			gridBagConstraintsX.gridx = 5;
			gridBagConstraintsX.gridy = 0;
			gridBagConstraintsX.weightx = 1.0;
			gridBagConstraintsX.weighty = 1.0;
			gridBagConstraintsX.insets = new java.awt.Insets(0,0,0,0);
			gridBagConstraintsX.anchor = java.awt.GridBagConstraints.EAST;
			gridBagConstraintsX.fill = java.awt.GridBagConstraints.HORIZONTAL;

			filterStatus = new JLabel("Filter: OFF");
			JLabel t1 = new JLabel();

			panelToolbar.add(getFilterButton(), gridBagConstraints1);
			panelToolbar.add(filterStatus, gridBagConstraints2);
			/*
			panelToolbar.add(getBtnSearch(), gridBagConstraints3);
			panelToolbar.add(getBtnNext(), gridBagConstraints4);
			panelToolbar.add(getBtnPrev(), gridBagConstraints5);
			*/
			panelToolbar.add(t1, gridBagConstraintsX);
		}
		return panelToolbar;
	}

	private JButton getFilterButton() {
		if (filterButton == null) {
			filterButton = new JButton();
			filterButton.setIcon(new ImageIcon(getClass().getResource("/resource/icon/16/054.png")));	// 'filter' icon
			filterButton.setToolTipText("Filter");

			filterButton.addActionListener(new java.awt.event.ActionListener() { 

				public void actionPerformed(java.awt.event.ActionEvent e) {
					extension.showFilterPlusDialog();
				}
			});
		}
		return filterButton;
	}

	/**

	 * This method initializes listLog	

	 * 	

	 * @return javax.swing.JList	

	 */    
	public javax.swing.JList getListLog() {
		if (listLog == null) {
			listLog = new javax.swing.JList();
			listLog.setDoubleBuffered(true);
            listLog.setCellRenderer(getLogPanelCellRenderer());
			listLog.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
			listLog.setName("ListLog");
			listLog.setFont(new java.awt.Font("Default", java.awt.Font.PLAIN, 12));
			listLog.addMouseListener(new java.awt.event.MouseAdapter() { 
				public void mousePressed(java.awt.event.MouseEvent e) {    
				    if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0) {  // right mouse button
				    	
						// ZAP: Select history list item on right click
					    int Idx = listLog.locationToIndex( e.getPoint() );
					    if ( Idx >= 0 ) {
					    	Rectangle Rect = listLog.getCellBounds( Idx, Idx );
					    	Idx = Rect.contains( e.getPoint().x, e.getPoint().y ) ? Idx : -1;
					    }
					    if ( Idx < 0 || !listLog.getSelectionModel().isSelectedIndex( Idx ) ) {
					    	listLog.getSelectionModel().clearSelection();
					    	if ( Idx >= 0 ) {
					    		listLog.getSelectionModel().setSelectionInterval( Idx, Idx );
					    	}
					    }

				        View.getSingleton().getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
				        return;
				    }	
				    
				    if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0 && e.getClickCount() > 1) {  // double click
						requestPanel.setTabFocus();
						return;
				    }
				}
			});
			
			listLog.addListSelectionListener(new javax.swing.event.ListSelectionListener() { 

				public void valueChanged(javax.swing.event.ListSelectionEvent e) {
				    if (listLog.getSelectedValue() == null) {
				        return;
				    }
                    
					final HistoryReference historyRef = (HistoryReference) listLog.getSelectedValue();

                    readAndDisplay(historyRef);

				}


			});

		}
		return listLog;
	}
	
//    private void readAndDisplay(HistoryReference historyRef) {
//
//        HttpMessage msg = null;
//        try {
//            msg = historyRef.getHttpMessage();
//            if (msg.getRequestHeader().isEmpty()) {
//                requestPanel.setMessage(null, true);
//            } else {
//                requestPanel.setMessage(msg, true);
//            }
//            
//            if (msg.getResponseHeader().isEmpty()) {
//                responsePanel.setMessage(null, false);
//            } else {
//                responsePanel.setMessage(msg, false);
//            }
//        } catch (Exception e1) {
//            e1.printStackTrace();
//        }
//        
//    }

    

    
    private Vector displayQueue = new Vector();
    private Thread thread = null;
    private LogPanelCellRenderer logPanelCellRenderer = null;  //  @jve:decl-index=0:visual-constraint="10,304"
    
    private void readAndDisplay(final HistoryReference historyRef) {

        synchronized(displayQueue) {
            if (!ExtensionHistory.isEnableForNativePlatform() || !extension.getBrowserDialog().isVisible()) {
                // truncate queue if browser dialog is displayed to have better response
                if (displayQueue.size()>0) {
                    // replace all display queue because the newest display overrides all previous one
                    // pending to be rendered.
                    displayQueue.clear();
                }
            }
            displayQueue.add(historyRef);

        }
        
        if (thread != null && thread.isAlive()) {
            return;
        }
        
        thread = new Thread(this);

        thread.setPriority(Thread.NORM_PRIORITY);
        thread.start();
    }
    
    
    public void setDisplayPanel(HttpPanel requestPanel, HttpPanel responsePanel) {
        this.requestPanel = requestPanel;
        this.responsePanel = responsePanel;

    }
    
    private void displayMessage(HttpMessage msg) {
        
        if (msg.getRequestHeader().isEmpty()) {
            requestPanel.setMessage(null, true);
        } else {
            requestPanel.setMessage(msg, true);
        }
        
        if (msg.getResponseHeader().isEmpty()) {
            responsePanel.setMessage(null, false);
        } else {
            responsePanel.setMessage(msg, false);
        }
    }

    public void run() {
        HistoryReference ref = null;
        int count = 0;
        
        do {
            synchronized(displayQueue) {
                count = displayQueue.size();
                if (count == 0) {
                    break;
                }
                
                ref = (HistoryReference) displayQueue.get(0);
                displayQueue.remove(0);
            }
            
            try {
                final HistoryReference finalRef = ref;
                final HttpMessage msg = ref.getHttpMessage();
                EventQueue.invokeAndWait(new Runnable() {
                    public void run() {
                        displayMessage(msg);
                        checkAndShowBrowser(finalRef, msg);
                        listLog.requestFocus();

                    }
                });
                
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            
            // wait some time to allow another selection event to be triggered
            try {
                Thread.sleep(200);
            } catch (Exception e) {}
        } while (true);
        
        
    }
    
    private void checkAndShowBrowser(HistoryReference ref, HttpMessage msg) {
        if (!ExtensionHistory.isEnableForNativePlatform() || !extension.getBrowserDialog().isVisible()) {
            return;
        }
        // ZAP: Disabled the platform specific browser
        // extension.browserDisplay(ref, msg);
    }
    /**
     * This method initializes logPanelCellRenderer	
     * 	
     * @return org.parosproxy.paros.extension.history.LogPanelCellRenderer	
     */
    private LogPanelCellRenderer getLogPanelCellRenderer() {
        if (logPanelCellRenderer == null) {
            logPanelCellRenderer = new LogPanelCellRenderer();
            logPanelCellRenderer.setSize(new java.awt.Dimension(328,21));
            logPanelCellRenderer.setBackground(java.awt.Color.white);
            logPanelCellRenderer.setFont(new java.awt.Font("MS Sans Serif", java.awt.Font.PLAIN, 12));
        }
        return logPanelCellRenderer;
    }

    public void setFilterStatus (HistoryFilter filter) {
    	filterStatus.setText(filter.toShortString());
    	filterStatus.setToolTipText(filter.toLongString());
    }
}

