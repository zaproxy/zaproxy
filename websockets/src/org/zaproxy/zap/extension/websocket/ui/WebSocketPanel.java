/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
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
package org.zaproxy.zap.extension.websocket.ui;

import java.awt.Adjustable;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.extension.history.LogPanel;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.httppanel.HttpPanel;
import org.zaproxy.zap.extension.websocket.WebSocketChannelDAO;
import org.zaproxy.zap.extension.websocket.WebSocketMessage;
import org.zaproxy.zap.extension.websocket.WebSocketMessageDAO;
import org.zaproxy.zap.extension.websocket.WebSocketObserver;
import org.zaproxy.zap.extension.websocket.WebSocketProxy;
import org.zaproxy.zap.extension.websocket.WebSocketProxy.State;
import org.zaproxy.zap.extension.websocket.brk.WebSocketBreakpointsUiManagerInterface;
import org.zaproxy.zap.extension.websocket.db.TableWebSocket;
import org.zaproxy.zap.extension.websocket.db.WebSocketStorage;

/**
 * Represents the WebSockets tab. It listens to all WebSocket channels and
 * displays messages accordingly. For now it uses a {@link JTable} for this
 * task.
 */
public class WebSocketPanel extends AbstractPanel implements WebSocketObserver, Runnable {

	private static final long serialVersionUID = -2853099315338427006L;

	private static final Logger logger = Logger.getLogger(WebSocketPanel.class);
	
	/**
	 * Observe messages after storage handler was called.
	 */
    public static final int WEBSOCKET_OBSERVING_ORDER = WebSocketStorage.WEBSOCKET_OBSERVING_ORDER + 1;

    /**
	 * Depending on its count, the tab uses either a connected or disconnected
	 * icon.
	 */
	private static Set<Integer> connectedChannelIds;
	
	public static final ImageIcon disconnectIcon;
	public static final ImageIcon connectIcon;
	
	static {
		disconnectIcon = new ImageIcon(WebSocketPanel.class.getResource("/resource/icon/fugue/plug-disconnect.png"));
		connectIcon = new ImageIcon(WebSocketPanel.class.getResource("/resource/icon/fugue/plug-connect.png"));
	};

	private JToolBar panelToolbar = null;

	private JComboBox channelSelect;
	private ComboBoxChannelModel channelSelectModel;

	private JButton handshakeButton;
	private JButton brkButton;
	private JButton filterButton;

	private JLabel filterStatus;
	private WebSocketTableModelFilterDialog filterDialog;
	
//	private JButton optionsButton;

	private JScrollPane scrollPanel;
	private JTable messagesLog;
	private WebSocketTableModel messagesModel;

	private HttpPanel requestPanel;
	private HttpPanel responsePanel;
	private WebSocketBreakpointsUiManagerInterface brkManager;

    private Vector<WebSocketMessageDAO> displayQueue;
    
    private Thread thread = null;

	private TableWebSocket table;

	static {
		connectedChannelIds = new HashSet<Integer>();
	}
	
	/**
	 * Panel is added as tab beside the History tab.
	 * 
	 * @param webSocketTable
	 * @param brkManager 
	 */
	public WebSocketPanel(TableWebSocket webSocketTable, WebSocketBreakpointsUiManagerInterface brkManager) {
		this.brkManager = brkManager;
		
		table = webSocketTable;
		channelSelectModel = new ComboBoxChannelModel();
		displayQueue = new Vector<WebSocketMessageDAO>();

		initializePanel();
	}
	
	/**
	 * Sets up the graphical representation of this tab.
	 */
	private void initializePanel() {
		setName("websocket.panel");
		setLayout(new GridBagLayout());
		
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.NORTHWEST;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(2,2,2,2);
		constraints.weightx = 1.0;
		add(getPanelToolbar(), constraints);

		constraints = new GridBagConstraints();
		constraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		constraints.fill = java.awt.GridBagConstraints.BOTH;
		constraints.gridy = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		add(getWorkPanel(), constraints);
		
		setIcon(WebSocketPanel.disconnectIcon);
		setName(Constant.messages.getString("websocket.panel.title"));
    	
		revalidate();
	}
	
	/**
	 * Lazy initializes header of this WebSocket tab with a select box and a
	 * filter.
	 * 
	 * @return
	 */
	private Component getPanelToolbar() {
		if (panelToolbar == null) {
			panelToolbar = new JToolBar();
			panelToolbar.setLayout(new GridBagLayout());
			panelToolbar.setEnabled(true);
			panelToolbar.setFloatable(false);
			panelToolbar.setRollover(true);
			panelToolbar.setPreferredSize(new java.awt.Dimension(800,30));
			panelToolbar.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
			panelToolbar.setName("websocket.toolbar");

			int x = 0;

			GridBagConstraints constraints = new GridBagConstraints();
			constraints.gridx = x++;
			panelToolbar.add(new JLabel(Constant.messages.getString("websocket.toolbar.channel.label")), constraints);
			
			constraints = new GridBagConstraints();
			constraints.gridx = x++;
			panelToolbar.add(getChannelSelect(), constraints);
			
			constraints = new GridBagConstraints();
			constraints.gridx = x++;
			panelToolbar.add(getShowHandshakeButton(), constraints);

			if (brkManager != null) {
				// ExtensionBreak is not disabled
				constraints = new GridBagConstraints();
				constraints.gridx = x++;
				panelToolbar.add(getAddBreakpointButton(), constraints);
			}
			
			panelToolbar.addSeparator();
			x++;
			
			constraints = new GridBagConstraints();
			constraints.gridx = x++;
			panelToolbar.add(getFilterButton(), constraints);

			constraints = new GridBagConstraints();
			constraints.gridx = x++;
			panelToolbar.add(getFilterStatus(), constraints);

			// stretch pseudo-component to let options button appear on the right
			constraints = new GridBagConstraints();
			constraints.gridx = x++;
			constraints.weightx = 1;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			panelToolbar.add(new JLabel(), constraints);

			// TODO: add options button to panel toolbar again
//			constraints = new GridBagConstraints();
//			constraints.gridx = x++;
//			panelToolbar.add(getOptionsButton(), constraints);
		}

		return panelToolbar;
	}

	protected JComboBox getChannelSelect() {
		if (channelSelect == null) {			
			channelSelect = new JComboBox(channelSelectModel);
			channelSelect.setRenderer(new ComboBoxChannelRenderer());
			channelSelect.setMaximumRowCount(8);
			channelSelect.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {    

				    WebSocketChannelDAO item = (WebSocketChannelDAO) channelSelect.getSelectedItem();
				    if (item != null && item.channelId != null) {
				    	// has valid element selected + a valid reference
				        useModel(item.channelId);
				    } else {
				        useJoinedModel();
				    }
			        
			        if (item != null && item.historyId != null) {
			        	getShowHandshakeButton().setEnabled(true);
			        } else {
				        getShowHandshakeButton().setEnabled(false);
			        }
			        
				    messagesLog.revalidate();
				}
			});
		}
		return channelSelect;
	}

//	private JButton getOptionsButton() {
//		if (optionsButton == null) {
//			optionsButton = new JButton();
//			optionsButton.setToolTipText(Constant.messages.getString("websocket.toolbar.button.options"));
//			optionsButton.setIcon(new ImageIcon(ScanPanel.class.getResource("/resource/icon/16/041.png")));
//			optionsButton.addActionListener(new ActionListener () {
//				@Override
//				public void actionPerformed(ActionEvent e) {
//					Control.getSingleton().getMenuToolsControl().options(
//							Constant.messages.getString("websocket.options.title"));
//				}
//			});
//		}
//		return optionsButton;
//	}

	private Component getFilterButton() {
		if (filterButton == null) {
			filterButton = new JButton();
			filterButton.setIcon(new ImageIcon(LogPanel.class.getResource("/resource/icon/16/054.png")));	// 'filter' icon
			filterButton.setToolTipText(Constant.messages.getString("websocket.filter.button.filter"));

			final WebSocketPanel panel = this;
			filterButton.addActionListener(new ActionListener() { 

				@Override
				public void actionPerformed(ActionEvent e) {
					panel.showFilterDialog();
				}
			});
		}
		return filterButton;
	}
	
	private JLabel getFilterStatus() {
		if (filterStatus == null) {
			String base = Constant.messages.getString("websocket.filter.label.filter");
			String status = Constant.messages.getString("websocket.filter.label.off");
			filterStatus = new JLabel(base + status);
		}
		return filterStatus;
	}

	private Component getShowHandshakeButton() {
		if (handshakeButton == null) {
			handshakeButton = new JButton();
			handshakeButton.setEnabled(false);
			handshakeButton.setIcon(new ImageIcon(LogPanel.class.getResource("/resource/icon/16/handshake.png")));
			handshakeButton.setToolTipText(Constant.messages.getString("websocket.filter.button.handshake"));

			final JComboBox channelSelect = this.channelSelect;
			handshakeButton.addActionListener(new ActionListener() { 

				@Override
				public void actionPerformed(ActionEvent evt) {
					WebSocketChannelDAO item = (WebSocketChannelDAO) channelSelect.getSelectedItem();
					HistoryReference handshakeRef = item.getHandshakeReference();
					if (handshakeRef != null) {
						HttpMessage msg;
						try {
                            msg = handshakeRef.getHttpMessage();
                        } catch (Exception e) {
                        	logger.warn(e.getMessage(), e);
                            return;
                        }
						showHandshakeMessage(msg);
					}
				}
			});
		}
		return handshakeButton;
	}

	private void showHandshakeMessage(HttpMessage msg) {        
        if (msg.getRequestHeader().isEmpty()) {
        	requestPanel.clearView(true);
        } else {
        	requestPanel.setMessage(msg);
        }
        
        if (msg.getResponseHeader().isEmpty()) {
        	responsePanel.clearView(false);
        } else {
        	responsePanel.setMessage(msg, true);
        }
        
    	requestPanel.setTabFocus();
	}

	private Component getAddBreakpointButton() {
		if (brkButton == null) {
			brkButton = new JButton();
			brkButton.setIcon(new ImageIcon(LogPanel.class.getResource("/resource/icon/16/break_add.png")));
			brkButton.setToolTipText(Constant.messages.getString("websocket.filter.button.break_add"));

			final WebSocketBreakpointsUiManagerInterface brkManager = this.brkManager;
			brkButton.addActionListener(new ActionListener() { 

				@Override
				public void actionPerformed(ActionEvent e) {
					brkManager.handleAddBreakpoint(new WebSocketMessageDAO());
				}
			});
		}
		return brkButton;
	}

	/**
	 * Lazy initializes the part of the WebSockets tab that is used to display
	 * the messages.
	 * 
	 * @return
	 */
	private JComponent getWorkPanel() {
		if (scrollPanel == null) {
			// alternatively you can use:
			// scrollPanel = LazyViewport.createLazyScrollPaneFor(getMessagesLog());
			// updates viewport only when scrollbar is released
			
			scrollPanel = new JScrollPane(getMessagesLog());
			scrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			scrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			scrollPanel.setPreferredSize(new Dimension(800,200));
			scrollPanel.setName("WebSocketPanelActions");
			
			scrollPanel.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
				private int previousMaximum;

				public void adjustmentValueChanged(AdjustmentEvent e) {
					Adjustable source = (Adjustable) e.getSource();
					
					if (source.getValue() + source.getVisibleAmount() == previousMaximum
							&& source.getMaximum() > previousMaximum) {
						// scrollbar is at previous position,
						// that was also the former maximum value
						
						// now content was added => scroll down
						source.setValue(source.getMaximum());
					}
					
					previousMaximum = source.getMaximum();
				}
			});
		}
		return scrollPanel;
	}
	
	/**
	 * Lazy initializes a {@link JList} instance, that is fed with different
	 * models (either a joined model or one of {@link WebSocketPanel#models} to
	 * display the WebSocket messages according to the
	 * {@link WebSocketPanel#channelSelect} selection.
	 * 
	 * @return
	 */
	protected JTable getMessagesLog() {
		if (messagesLog == null) {
			messagesModel = new WebSocketTableModel(table, getFilterDialog().getFilter());
			
			messagesLog = new JTable();
			messagesLog.setName("websocket.table");
			messagesLog.setModel(messagesModel);
			messagesLog.setColumnSelectionAllowed(false);
			messagesLog.setCellSelectionEnabled(false);
			messagesLog.setRowSelectionAllowed(true);
			messagesLog.setAutoCreateRowSorter(false);
			
			// prevents columns to loose their width when switching models
			messagesLog.setAutoCreateColumnsFromModel(false);

			// channel + consecutive number
			setColumnWidth(0, 50, 100, 70);

			// direction
			setColumnWidth(1, 25, 100, 25);
			
			// timestamp
			setColumnWidth(2, 160, 200, 160);
			
			// opcode
			setColumnWidth(3, 70, 120, 75);
			
			// payload length
			setColumnWidth(4, 45, 100, 45);
			
			// payload (do not set max & preferred size => stretches to maximum)
			setColumnWidth(5, 100, -1, -1);

			messagesLog.setFont(new Font("Dialog", Font.PLAIN, 12));
			messagesLog.setDoubleBuffered(true);
			messagesLog.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			messagesLog.addMouseListener(new MouseAdapter() { 
			    @Override
			    public void mousePressed(MouseEvent e) {

					if (SwingUtilities.isRightMouseButton(e)) {

						// Select table item
					    int row = messagesLog.rowAtPoint( e.getPoint() );
					    if ( row < 0 || !messagesLog.getSelectionModel().isSelectedIndex( row ) ) {
					    	messagesLog.getSelectionModel().clearSelection();
					    	if ( row >= 0 ) {
					    		messagesLog.getSelectionModel().setSelectionInterval( row, row );
					    	}
					    }
						
						View.getSingleton().getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
			        }
			    }
			});
			
			messagesLog.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				
				@Override
				public void valueChanged(ListSelectionEvent e) {
					// only display messages when there are no more selection changes.
					if (!e.getValueIsAdjusting()) {
						int rowIndex = messagesLog.getSelectedRow();
					    if (rowIndex < 0) {
					    	// selection got filtered away
					        return;
					    }
					    
					    WebSocketTableModel model = (WebSocketTableModel) messagesLog.getModel();
					    
					    // as we use a JTable here, that can be sorted, we have to
					    // transform the row index to the appropriate model row
	                    int modelRow = messagesLog.convertRowIndexToModel(rowIndex);
						final WebSocketMessageDAO message = model.getDAO(modelRow);
	                    readAndDisplay(message);
					}
				}
			});
			
			messagesLog.revalidate();
		}
		return messagesLog;
	}
	
	/**
	 * Helper method for setting the column widths of the
	 * {@link WebSocketPanel#messagesLog}.
	 * 
	 * @param index
	 * @param min
	 * @param max
	 * @param preferred
	 */
	private void setColumnWidth(int index, int min, int max, int preferred) {
		TableColumn column = messagesLog.getColumnModel().getColumn(index);
		
		if (min != -1) {
			column.setMinWidth(min);
		}
		
		if (max != -1) {
			column.setMaxWidth(max);
		}
		
		if (preferred != -1) {
			column.setPreferredWidth(preferred);
		}
	}
	
	/**
	 * Updates icon of this tab.
	 * 
	 * @param icon
	 */
	private synchronized void updateIcon(ImageIcon icon) {
		setIcon(icon);
		
		// workaround to update icon of tab
		Component c = getParent();
	    if (c instanceof JTabbedPane) {
		    JTabbedPane tab = (JTabbedPane) c;
		    int index = tab.indexOfComponent(this);
		    tab.setIconAt(index, icon);
	    }
	}
    
    public void setDisplayPanel(HttpPanel requestPanel, HttpPanel responsePanel) {
        this.requestPanel = requestPanel;
        this.responsePanel = responsePanel;
    }
    
    private void readAndDisplay(final WebSocketMessageDAO message) {
        synchronized(displayQueue) {
            if (displayQueue.size() > 0) {
                displayQueue.clear();
            }
            
            message.tempUserObj = connectedChannelIds.contains(message.channelId);
            displayQueue.add(message);
        }
        
        if (thread != null && thread.isAlive()) {
            return;
        }
        
        thread = new Thread(this);
        thread.setPriority(Thread.NORM_PRIORITY);
        thread.start();
    }
    
    @Override
    public void run() {
        WebSocketMessageDAO message = null;
        int count = 0;
        
        do {
            synchronized(displayQueue) {
                count = displayQueue.size();
                if (count == 0) {
                    break;
                }
                
                message = displayQueue.get(0);
                displayQueue.remove(0);
            }
            
            try {
                final WebSocketMessageDAO msg = message;
                EventQueue.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        if (msg.isOutgoing) {
                            requestPanel.setMessage(msg);
                            responsePanel.clearView(false);
                            requestPanel.setTabFocus();
                        } else {
                            requestPanel.clearView(true);
                            responsePanel.setMessage(msg, true);
                            responsePanel.setTabFocus();
                        }
                    }
                });
                
            } catch (Exception e) {
                // ZAP: Added logging.
                logger.error(e.getMessage(), e);
            }
            
            // wait some time to allow another selection event to be triggered
            try {
                Thread.sleep(200);
            } catch (Exception e) {
            	// safely ignore exception
            }
        } while (true);
    }

	@Override
	public int getObservingOrder() {
		return WEBSOCKET_OBSERVING_ORDER;
	}

	/**
	 * Collects WebSocket messages.
	 */
	@Override
	public synchronized boolean onMessageFrame(final int channelId, WebSocketMessage message) {
		if (message.isFinished()) {
			messagesModel.fireMessageArrived(message.getDAO());
		}
		return true;
	}

	@Override
	public synchronized void onStateChange(State state, WebSocketProxy proxy) {
		WebSocketChannelDAO dao = proxy.getDAO();
		
		boolean isConnectedChannel = connectedChannelIds.contains(dao.channelId);
		boolean isNewChannel = false;

		switch (state){
		case CLOSED:
			if (isConnectedChannel && dao.endTimestamp != null) {
				connectedChannelIds.remove(dao.channelId);
					
				// updates icon
				channelSelectModel.updateElement(dao);
			}
			break;
			
		case EXCLUDED:
			// remove from UI
			connectedChannelIds.remove(dao.channelId);
			channelSelectModel.removeElement(dao);
			
			messagesModel.fireTableDataChanged();
            break;
			
		case OPEN:
			if (!isConnectedChannel && dao.endTimestamp == null) {
				connectedChannelIds.add(dao.channelId);
				channelSelectModel.addElement(dao);
			}
			break;
            
		case INCLUDED:
			// add to UI (probably again)
			connectedChannelIds.add(dao.channelId);
			channelSelectModel.addElement(dao);
			
			messagesModel.fireTableDataChanged();
			isNewChannel = true;
			break;
		}
		
		// change appearance of WebSocket tab header
		int count = connectedChannelIds.size();
		if (count == 0) {
			// change icon, as no WebSocket channel is active
			updateIcon(WebSocketPanel.disconnectIcon);
		} else if (count == 1 && isNewChannel) {
			// change icon, as at least one WebSocket is available
			updateIcon(WebSocketPanel.connectIcon);
		}
	}
	
	/**
	 * Set current displayed channel.
	 * 
	 * @param channelId
	 */
	private void useModel(int channelId) {
		messagesModel.setActiveChannel(channelId);
	}
	
	/**
	 * Get model that contains all messages from all channels.
	 */
	private void useJoinedModel() {
		messagesModel.setActiveChannel(null);
	}
	
	/**
	 * Lazy initializes the filter dialog.
	 * 
	 * @return
	 */
	public WebSocketTableModelFilterDialog getFilterDialog() {
		if (filterDialog == null) {
			filterDialog = new WebSocketTableModelFilterDialog(View.getSingleton().getMainFrame(), true);
			
		}
		return filterDialog;
	}
	
	/**
	 * Shows filter dialog
	 * 
	 * @return 1 is returned if applied, -1 when dialog was reseted.
	 */
	protected int showFilterDialog() {
		WebSocketTableModelFilterDialog dialog = getFilterDialog();
		dialog.setModal(true);
		
		int exit = dialog.showDialog();

		int result = 0;
		switch (exit) {
		case JOptionPane.OK_OPTION:
			// some changes were applied
			result = 1;
			break;
			
		case JOptionPane.NO_OPTION:
			// reset button was pressed
		    result = -1;
		    break;
		    
		case JOptionPane.CANCEL_OPTION:
			// nothing has changed - do not filter again
			return result;
		}
		
	    setFilterStatus();
		applyFilter();
		
		return result;
	}
		
	/**
	 * Apply {@link WebSocketFilter} to visible parts of models.
	 */
	private void applyFilter() {
		messagesModel.fireFilterChanged();
	}
	
	/**
	 * Show textual hint for filter status.
	 * 
	 * @param filter
	 */
    private void setFilterStatus() {
    	WebSocketTableModelFilter filter = getFilterDialog().getFilter();
    	JLabel status = getFilterStatus();
    	
    	status.setText(filter.toLongString());
    	status.setToolTipText(filter.toLongString());
    }
    
    /**
	 * Exposes a cloned model from channel select box.
	 * Can be used at other dialogs.
	 * 
	 * @return
	 */
	public ComboBoxModel getChannelComboBoxModel() {
		return new ClonedComboBoxModel(channelSelectModel);
	}
    
    /**
	 * If a {@link ComboBoxModel} is shared by two different {@link JComboBox}
	 * instances (i.e. set as model on both), then changing the selection of an
	 * item in one {@link JComboBox} causes the same item to be selected in the
	 * other {@link JComboBox} too.
	 * <p>
	 * This model wraps the original model and manages its own selected item. As
	 * a result, the {@link JComboBox} is independent from the other. Moreover
	 * items are always the same.
	 */
    private class ClonedComboBoxModel implements ComboBoxModel {
		private ComboBoxModel wrappedModel;
		private Object selectedObject;
		
		public ClonedComboBoxModel(ComboBoxModel wrappedModel) {
			this.wrappedModel = wrappedModel; 
			this.selectedObject = wrappedModel.getElementAt(0);
		}

		@Override
		public void addListDataListener(ListDataListener l) {
			wrappedModel.addListDataListener(l);
		}

		@Override
		public Object getElementAt(int index) {
			return wrappedModel.getElementAt(index);
		}

		@Override
		public int getSize() {
			return wrappedModel.getSize();
		}

		@Override
		public void removeListDataListener(ListDataListener l) {
			wrappedModel.removeListDataListener(l);
		}

		@Override
		public Object getSelectedItem() {
			return selectedObject;
		}

		@Override
		public void setSelectedItem(Object anItem) {
			selectedObject = anItem;
		}    	
    }
    
    /**
	 * Updates {@link JTable} that contains all messages and the
	 * {@link JComboBox} that is used to filter channels.
	 */
    public void update() {
    	// reset table contents
		messagesModel.fireTableDataChanged();
		
		// reset channel selector's model
		Object selectedItem = channelSelectModel.getSelectedItem();
		channelSelectModel.reset();
		
		try {
			for (WebSocketChannelDAO item : table.getChannelItems()) {
				channelSelectModel.addElement(item);
			}

			int index = channelSelectModel.getIndexOf(selectedItem);
			if (index == -1) {
				index = 0;
			}
			channelSelect.setSelectedIndex(index);
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
    }

    /**
     * Clear control elements, set back to default.
     */
	public void reset() {
		// update contents in views
		update();
		
		// select '-- All Channels --' item
		if (channelSelect.getSelectedIndex() != 0) {
			channelSelect.setSelectedIndex(0);
		}
		
		// reset filter 
		getFilterDialog().getFilter().reset();
	}
}

