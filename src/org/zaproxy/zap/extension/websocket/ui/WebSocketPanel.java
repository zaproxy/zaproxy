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
package org.zaproxy.zap.extension.websocket.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
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
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.extension.history.LogPanel;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.httppanel.HttpPanel;
import org.zaproxy.zap.extension.websocket.WebSocketMessage;
import org.zaproxy.zap.extension.websocket.WebSocketObserver;
import org.zaproxy.zap.extension.websocket.WebSocketProxy;
import org.zaproxy.zap.extension.websocket.ui.WebSocketUiModel.WebSocketMessageDAO;
import org.zaproxy.zap.utils.SortedComboBoxModel;
import org.zaproxy.zap.view.ScanPanel;

/**
 * Represents the WebSockets tab. It listens to all WebSocket channels and
 * displays messages accordingly.
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

	private WebSocketFilterDialog filterDialog;

	private JComboBox channelSelect;

	private SortedComboBoxModel channelSelectModel;

	private JScrollPane scrollPanel;

	private JList messagesLog;

	private WebSocketPanelCellRenderer cellRenderer;

	private HttpPanel requestPanel;

	private HttpPanel responsePanel;
	
	private Map<Integer, WebSocketUiModel> models;
	
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
		
		models = new HashMap<Integer, WebSocketUiModel>();
		
		// at the beginning all channels are shown, indicated by -1
		currentChannelId = -1;

		initializePanel();
	}
	
	/**
	 * Sets up this tab and installs a change listener.
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
	public JComponent getMessagesLog() {
		if (messagesLog == null) {
			messagesLog = new JList(getJoinedMessagesModel());
			messagesLog.setDoubleBuffered(true);
            messagesLog.setCellRenderer(getCustomCellRenderer());
			messagesLog.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
			messagesLog.setName("WebSocketMessagesLog");
			messagesLog.setFont(new Font("Default", Font.PLAIN, 12));
			
			// significantly speeds up rendering
			messagesLog.setFixedCellHeight(16);
			
			messagesLog.addMouseListener(new MouseAdapter() { 
				@Override
				public void mousePressed(MouseEvent e) {
					mouseClicked(e);
				}
					
				@Override
				public void mouseReleased(MouseEvent e) {
					mouseClicked(e);
				}
				
				@Override
				public void mouseClicked(MouseEvent e) {
					// right mouse button action
					if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0 || e.isPopupTrigger()) {
				    	
						// Select message list item on right click
					    int Idx = messagesLog.locationToIndex( e.getPoint() );
					    
					    if ( Idx >= 0 ) {
					    	Rectangle Rect = messagesLog.getCellBounds( Idx, Idx );
					    	Idx = Rect.contains( e.getPoint().x, e.getPoint().y ) ? Idx : -1;
					    }
					    
					    if ( Idx < 0 || !messagesLog.getSelectionModel().isSelectedIndex( Idx ) ) {
					    	messagesLog.getSelectionModel().clearSelection();
					    	if ( Idx >= 0 ) {
					    		messagesLog.getSelectionModel().setSelectionInterval( Idx, Idx );
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
			
			messagesLog.addListSelectionListener(new ListSelectionListener() {
				
				@Override
				public void valueChanged(ListSelectionEvent e) {
					// Only display messages when there are no more selection changes.
					if (!e.getValueIsAdjusting()) {
					    if (messagesLog.getSelectedValue() == null) {
					        return;
					    }
					    
					    // TODO: display payload in detail
//						final WebSocketMessageDAO message = (WebSocketMessageDAO) messagesLog.getSelectedValue();
//	                    readAndDisplay(message);
					}
				}
			});
		}
		return messagesLog;
	}

	/**
	 * Set this panel up as {@link WebSocketObserver}.
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
		if (channelSelectModel.getIndexOf(item.getAsActive()) < 0 && channelSelectModel.getIndexOf(item.getAsActive()) < 0) {
	 		logger.debug("add channel to panel (" + channelName + ")");
			channelSelectModel.addElement(item.getAsPassive());
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
                final HttpMessage msg = new HttpMessage("", message.payload.getBytes(), "", new byte[0]);
                EventQueue.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                            
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
                        
                        messagesLog.requestFocus();
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
     * This method initializes logPanelCellRenderer	
     * 	
     * @return org.parosproxy.paros.extension.history.LogPanelCellRenderer	
     */
    private WebSocketPanelCellRenderer getCustomCellRenderer() {
        if (cellRenderer == null) {
            cellRenderer = new WebSocketPanelCellRenderer();
            
    	    if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
    	    	cellRenderer.setSize(new java.awt.Dimension(328,21));
    	    }
    	    
            cellRenderer.setBackground(java.awt.Color.white);
            cellRenderer.setFont(new java.awt.Font("MS Sans Serif", java.awt.Font.PLAIN, 12));
        }
        
        return cellRenderer;
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
			
			String base = Constant.messages.getString("websocket.filter.label.filter");
			String status = Constant.messages.getString("websocket.filter.label.off");
			filterStatus = new JLabel(base + status);

			GridBagConstraints constraint = new GridBagConstraints();
			constraint.gridx = x++;
			panelToolbar.add(getChannelSelect(), constraint);

			constraint = new GridBagConstraints();
			constraint.gridx = x++;
			panelToolbar.add(getFilterButton(), constraint);

			constraint = new GridBagConstraints();
			constraint.gridx = x++;
			panelToolbar.add(filterStatus, constraint);

			// stretch pseudo-component to let options button appear on the right
			constraint = new GridBagConstraints();
			constraint.gridx = x++;
			constraint.weightx = 1;
			constraint.fill = GridBagConstraints.HORIZONTAL;
			panelToolbar.add(new JLabel(), constraint);

			constraint = new GridBagConstraints();
			constraint.gridx = x++;
			panelToolbar.add(getOptionsButton(), constraint);
		}

		return panelToolbar;
	}

	protected JComboBox getChannelSelect() {
		if (channelSelect == null) {
			channelSelect = new JComboBox(channelSelectModel);
			channelSelect.addItem(new ComboBoxChannelItem(Constant.messages.getString("websocket.toolbar.channel.select"), -1));
			channelSelect.setSelectedIndex(0);
			channelSelect.setPreferredSize(new Dimension(250, 25));

			channelSelect.addActionListener(new ActionListener() { 

				@Override
				public void actionPerformed(ActionEvent e) {    

				    ComboBoxChannelItem item = (ComboBoxChannelItem) channelSelect.getSelectedItem();
				    int index = channelSelect.getSelectedIndex();
				    if (item != null && index > 0) {
				        channelSelected(item);
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

	private void channelSelected(ComboBoxChannelItem item) {
		if (channelSelectModel.getIndexOf(item.getAsPassive()) < 0) {
			channelSelectModel.setSelectedItem(item.getAsActive());
		} else {
			channelSelectModel.setSelectedItem(item.getAsPassive());
		}
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
		private final int channelId;
		private final String label;
		
		public ComboBoxChannelItem(String name, int channelId) {
			this.label = name;
			this.channelId = channelId;
		}
		
		public int getChannelId() {
			return channelId;
		}

		public String toString() {
			return label;
		}
		
		/**
		 * Show active site labels in bold font.
		 * 
		 * @param label
		 * @return
		 */
		public ComboBoxChannelItem getAsActive() {
			return new ComboBoxChannelItem("<html><b>" + getNakedLabel() + "</b></html>", channelId);
		}

		/**
		 * Show inactive site labels in normal font.
		 * 
		 * @param label
		 * @param channelId
		 * @return
		 */
		public ComboBoxChannelItem getAsPassive() {
			return new ComboBoxChannelItem("<html>" + getNakedLabel() + "</html>", channelId);
		}
		
		/**
		 * Strips out HTML from label string.
		 * 
		 * @return
		 */
		private String getNakedLabel() {
			return label.replaceAll("</?html>", "").replaceAll("</?b>", "");
		}

		@Override
		public int compareTo(Object o) {
			return toString().compareTo(o.toString());
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

	@Override
	public int getObservingOrder() {
		return WEBSOCKET_OBSERVING_ORDER;
	}

	/**
	 * Collects WebSocket messages.
	 */
	@Override
	public boolean onMessageFrame(int channelId, WebSocketMessage message) {
		if (message.isFinished()) {
			getMessagesModel(channelId).addWebSocketMessage(message);
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
		messagesLog.setModel(getMessagesModel(channelId));
	}

	/**
	 * Returns or creates the model for the given channelId.
	 * 
	 * @param channelId
	 * @return
	 */
	private WebSocketUiModel getMessagesModel(int channelId) {
		if (!models.containsKey(channelId)) {
			models.put(channelId, new WebSocketUiModel(getFilterDialog().getFilter()));
		}
		
		currentChannelId = channelId;
		return models.get(channelId);
	}
	
	/**
	 * Get model that contains all messages from all channels.
	 */
	private void useJoinedModel() {
		currentChannelId = -1;
		messagesLog.setModel(getJoinedMessagesModel());
	}
	
	/**
	 * Returns all models combined into a new one.
	 * 
	 * @return
	 */
	private WebSocketUiModel getJoinedMessagesModel() {
		WebSocketUiModel joinedModel = new WebSocketUiModel(getFilterDialog().getFilter());
		
		for (Entry<Integer, WebSocketUiModel> model  : models.entrySet()) {
			joinedModel.addMessages(model.getValue().getMessages());
		}
		
		return joinedModel;
	}
	
	/**
	 * Lazy initializes the filter dialog.
	 * 
	 * @return
	 */
	public WebSocketFilterDialog getFilterDialog() {
		if (filterDialog == null) {
			filterDialog = new WebSocketFilterDialog(mainframe, true);
			
		}
		return filterDialog;
	}
	
	/**
	 * Shows filter dialog
	 * 
	 * @return 1 is returned if applied, -1 when dialog was reseted.
	 */
	protected int showFilterDialog() {
		WebSocketFilterDialog dialog = getFilterDialog();
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
			// no specific channel is currently selected - filter all models
			for (Entry<Integer, WebSocketUiModel> model : models.entrySet()) {
				model.getValue().reFilter();
			}
		} else {
			// filter only model that is currently shown
			models.get(currentChannelId).reFilter();
		}
	}

	/**
	 * Show textual hint for filter status.
	 * 
	 * @param filter
	 */
    private void setFilterStatus() {
    	WebSocketFilter filter = filterDialog.getFilter();
    	
    	filterStatus.setText(filter.toShortString());
    	filterStatus.setToolTipText(filter.toLongString());
    }
}

