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
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.extension.history.LogPanel;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.httppanel.HttpPanel;
import org.zaproxy.zap.extension.websocket.WebSocketMessage;
import org.zaproxy.zap.extension.websocket.WebSocketObserver;
import org.zaproxy.zap.extension.websocket.WebSocketProxy;
import org.zaproxy.zap.utils.SortedComboBoxModel;
import org.zaproxy.zap.view.ScanPanel;

/**
 * Represents the WebSockets tab. It listens to all WebSocket channels and
 * displays messages accordingly. For now it uses a {@link JTable} for this
 * task.
 */
public class WebSocketPanel extends AbstractPanel implements WebSocketObserver, Runnable {

	private static final long serialVersionUID = -2853099315338427006L;

	private static final Logger logger = Logger.getLogger(WebSocketPanel.class);
	
	/**
	 * Just observe it somewhere and let others catch them too.
	 */
    public static final int WEBSOCKET_OBSERVING_ORDER = 100;

	private JToolBar panelToolbar = null;

	private JButton filterButton;

	private JLabel filterStatus;

	private WebSocketModelFilterDialog filterDialog;

	private JComboBox channelSelect;

	private SortedComboBoxModel channelSelectModel;

	private JScrollPane scrollPanel;

	private JTable messagesLog;

	private HttpPanel requestPanel;

	private HttpPanel responsePanel;
	
	private Map<Integer, WebSocketTableModel> models;
	
	private int currentChannelId;

	private JButton optionsButton;

	private Frame mainframe;

	/**
	 * Panel is added as tab beside the History tab.
	 * 
	 * @param mainframe Needed for dialog box.
	 */
	public WebSocketPanel(Frame mainframe) {
		this.mainframe = mainframe;
		
		channelSelectModel = new SortedComboBoxModel();
		
		models = new HashMap<Integer, WebSocketTableModel>();

		// at the beginning all channels are shown
		initializePanel();
	}
	
	/**
	 * Sets up the graphical representation of this tab.
	 */
	private void initializePanel() {
		setName("websocket.panel");
		setLayout(new GridBagLayout());
		
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.insets = new Insets(2,2,2,2);
		gridBagConstraints1.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints1.weightx = 1.0;

		GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
		gridBagConstraints2.gridy = 1;
		gridBagConstraints2.weightx = 1.0;
		gridBagConstraints2.weighty = 1.0;
		gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTHWEST;

		add(getPanelToolbar(), gridBagConstraints1);
		add(getWorkPanel(), gridBagConstraints2);
		
		setIcon(new ImageIcon(View.class.getResource("/resource/icon/16/029.png")));
		setName(Constant.messages.getString("http.panel.websocket.title"));
    	
		revalidate();
	}

	/**
	 * Lazy initializes the part of the WebSockets tab that is used to display
	 * the messages.
	 * 
	 * @return
	 */
	private JComponent getWorkPanel() {
		if (scrollPanel == null) {
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
			messagesLog = new JTable();
			messagesLog.setName("websocket.table");
			
			// set JTable's model - start with all channels
			useJoinedModel();
			
			messagesLog.setColumnSelectionAllowed(false);
			messagesLog.setCellSelectionEnabled(false);
			messagesLog.setRowSelectionAllowed(true);
			messagesLog.setAutoCreateRowSorter(true);
			
			// prevents columns to loose their width when switching models
			messagesLog.setAutoCreateColumnsFromModel(false);

			// channel + consecutive number
			setColumnWidth(0, 50, 100, 70);
			
			// direction
			setColumnWidth(1, 25, 100, 25);
			
			// timestamp
			setColumnWidth(2, 160, 200, 160);
			
			// opcode
			setColumnWidth(3, 70, 120, 70);

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
					    if (messagesLog.getSelectedRow() < 0) {
					    	// selection got filtered away
					        return;
					    }
					    WebSocketTableModel model;
					    if (currentChannelId == -1) {
					        model = getJoinedMessagesModel();
				        } else {
				            model =  models.get(Integer.valueOf(currentChannelId));
				        }
					    
						final WebSocketMessageDAO message = model.getMessages().get(messagesLog.getSelectedRow());
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
	 * Sets this panel up as {@link WebSocketObserver} for the given
	 * {@link WebSocketProxy}.
	 * 
	 * @param ws
	 * @param hostName
	 */
	public void addProxy(WebSocketProxy ws, String hostName) {
		// listen to messages of this proxy
		ws.addObserver(this);
		
		// add to select box
		addChannelToComboBox(hostName + " (#" + ws.getChannelId() + ")", ws.getChannelId());
	}
	
	/**
	 * Show channel in {@link JComboBox}.
	 * 
	 * @param channelName
	 * @param channelId
	 */
	private void addChannelToComboBox(String channelName, int channelId) {
		ComboBoxChannelItem item = new ComboBoxChannelItem(channelName, channelId);
		if (channelSelectModel.getIndexOf(item) < 0) {
			// element is new
	 		logger.debug("add channel to websocket panel (" + channelName + ")");
			channelSelectModel.addElement(item);
		}
	}

    private Vector<WebSocketMessageDAO> displayQueue = new Vector<WebSocketMessageDAO>();
    private Thread thread = null;
    
    private void readAndDisplay(final WebSocketMessageDAO message) {
        synchronized(displayQueue) {
        	/*
        	// ZAP: Disabled the platform specific browser
            if (!ExtensionHistory.isEnableForNativePlatform() || !extension.getBrowserDialog().isVisible()) {
                // truncate queue if browser dialog is displayed to have better response
                if (displayQueue.size()>0) {
                    // replace all display queue because the newest display overrides all previous one
                    // pending to be rendered.
                    displayQueue.clear();
                }
            }
            */
            if (displayQueue.size() > 0) {
                displayQueue.clear();
            }
            
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
                        
                        if (msg.direction == WebSocketMessage.Direction.OUTGOING) {
                            requestPanel.setMessage(msg);
                            responsePanel.clearView(false);
                            requestPanel.setTabFocus();
                        } else {
                            requestPanel.clearView(true);
                            responsePanel.setMessage(msg, true);
                            responsePanel.setTabFocus();
                        }
                        
                        //messagesLog.requestFocus();
                    }
                });
                
            } catch (Exception e) {
                // ZAP: Added logging.
                logger.error(e.getMessage(), e);
            }
            
            // wait some time to allow another selection event to be triggered
            try {
                Thread.sleep(200);
            } catch (Exception e) {}
        } while (true);
    }
    
    
    public void setDisplayPanel(HttpPanel requestPanel, HttpPanel responsePanel) {
        this.requestPanel = requestPanel;
        this.responsePanel = responsePanel;
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

			constraints = new GridBagConstraints();
			constraints.gridx = x++;
			panelToolbar.add(getOptionsButton(), constraints);
		}

		return panelToolbar;
	}

	protected JComboBox getChannelSelect() {
		if (channelSelect == null) {
			channelSelect = new JComboBox(channelSelectModel);
			channelSelect.addItem(new ComboBoxChannelItem(Constant.messages.getString("websocket.toolbar.channel.select"), -1));
			channelSelect.setSelectedIndex(0);
			channelSelect.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {    

				    ComboBoxChannelItem item = (ComboBoxChannelItem) channelSelect.getSelectedItem();
				    int index = channelSelect.getSelectedIndex();
				    if (item != null && index > 0) {
				        useModel(item.getChannelId());
				    } else if (index == 0) {
				        useJoinedModel();
				    }
				    
				    messagesLog.revalidate();
				}
			});
		}
		return channelSelect;
	}

	private JButton getOptionsButton() {
		if (optionsButton == null) {
			optionsButton = new JButton();
			optionsButton.setToolTipText(Constant.messages.getString("websocket.toolbar.button.options"));
			optionsButton.setIcon(new ImageIcon(ScanPanel.class.getResource("/resource/icon/16/041.png")));
			optionsButton.addActionListener(new ActionListener () {
				@Override
				public void actionPerformed(ActionEvent e) {
					Control.getSingleton().getMenuToolsControl().options(
							Constant.messages.getString("websocket.options.title"));
				}
			});
		}
		return optionsButton;
	}
	
	protected class ComboBoxChannelItem implements Comparable<Object> {
		private final Integer channelId;
		private final String label;
		
		public ComboBoxChannelItem(String name, int channelId) {
			this.label = name;
			this.channelId = channelId;
		}
		
		public Integer getChannelId() {
			return channelId;
		}

		public String toString() {
			return label;
		}

		@Override
		public int compareTo(Object o) {
			String otherString = o.toString().replaceAll("#[0-9]+", "");
			String thisString = toString().replaceAll("#[0-9]+", "");
			int result = thisString.compareTo(otherString);
			
			if (result == 0) {
				ComboBoxChannelItem otherItem = (ComboBoxChannelItem) o;
				return getChannelId().compareTo(otherItem.getChannelId());
			}
			
			return result;
		}
	}

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

	@Override
	public int getObservingOrder() {
		return WEBSOCKET_OBSERVING_ORDER;
	}

	/**
	 * Collects WebSocket messages.
	 */
	@Override
	public boolean onMessageFrame(int channelId, WebSocketMessage message) {
		synchronized (models) {
			if (message.isFinished()) {
				WebSocketMessageDAO dao = getMessagesModel(channelId).addWebSocketMessage(message);
				if (currentChannelId == -1) {
					// joined model is used -> add message also there
					((WebSocketTableModel) messagesLog.getModel()).addWebSocketMessage(dao);
				}
			}
		}
		return true;
	}
	
	/**
	 * Set current displayed channel.
	 * 
	 * @param channelId
	 */
	private void useModel(int channelId) {
		currentChannelId = channelId;
		
		WebSocketTableModel model = getMessagesModel(channelId);
		model.reFilter();
		
		messagesLog.setModel(model);
	}

	/**
	 * Returns or creates the model for the given channelId.
	 * 
	 * @param channelId
	 * @return
	 */
	private WebSocketTableModel getMessagesModel(int channelId) {
		if (!models.containsKey(channelId)) {
			models.put(channelId, new WebSocketTableModel(getFilterDialog().getFilter(), channelId));
		}

		return models.get(channelId);
	}
	
	/**
	 * Get model that contains all messages from all channels.
	 */
	private void useJoinedModel() {
		currentChannelId = -1;
		reFilterJoinedModel();
		messagesLog.setModel(getJoinedMessagesModel());
	}
	
	/**
	 * Returns all models combined into a new one.
	 * 
	 * @return
	 */
	private WebSocketTableModel getJoinedMessagesModel() {
		WebSocketTableModel joinedModel = new WebSocketTableModel(getFilterDialog().getFilter(), -1);
		
		for (Entry<Integer, WebSocketTableModel> model  : models.entrySet()) {
			joinedModel.addMessages(model.getValue().getMessages());
		}
		
		return joinedModel;
	}

	/**
	 * Filter all models according to selected filter.
	 */
	private void reFilterJoinedModel() {
		for (Entry<Integer, WebSocketTableModel> model : models.entrySet()) {
			model.getValue().reFilter();
		}
	}
	
	/**
	 * Lazy initializes the filter dialog.
	 * 
	 * @return
	 */
	public WebSocketModelFilterDialog getFilterDialog() {
		if (filterDialog == null) {
			filterDialog = new WebSocketModelFilterDialog(mainframe, true);
			
		}
		return filterDialog;
	}
	
	/**
	 * Shows filter dialog
	 * 
	 * @return 1 is returned if applied, -1 when dialog was reseted.
	 */
	protected int showFilterDialog() {
		WebSocketModelFilterDialog dialog = getFilterDialog();
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
		if (currentChannelId == -1) {
			// as no specific model is chosen, I've to refilter all models
			useJoinedModel();
		} else {
			// filtering the model that is currently shown suffices
			models.get(currentChannelId).reFilter();
		}
	}
	
	/**
	 * Show textual hint for filter status.
	 * 
	 * @param filter
	 */
    private void setFilterStatus() {
    	WebSocketModelFilter filter = getFilterDialog().getFilter();
    	JLabel status = getFilterStatus();
    	
    	status.setText(filter.toShortString());
    	status.setToolTipText(filter.toLongString());
    }
}

