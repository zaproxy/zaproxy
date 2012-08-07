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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

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
import javax.swing.event.ListDataListener;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.extension.history.LogPanel;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.httppanel.HttpPanel;
import org.zaproxy.zap.extension.websocket.WebSocketChannelDTO;
import org.zaproxy.zap.extension.websocket.WebSocketException;
import org.zaproxy.zap.extension.websocket.WebSocketMessage;
import org.zaproxy.zap.extension.websocket.WebSocketMessageDTO;
import org.zaproxy.zap.extension.websocket.WebSocketObserver;
import org.zaproxy.zap.extension.websocket.WebSocketProxy;
import org.zaproxy.zap.extension.websocket.WebSocketProxy.State;
import org.zaproxy.zap.extension.websocket.brk.WebSocketBreakpointsUiManagerInterface;
import org.zaproxy.zap.extension.websocket.db.TableWebSocket;
import org.zaproxy.zap.extension.websocket.db.WebSocketStorage;
import org.zaproxy.zap.utils.StickyScrollbarAdjustmentListener;

/**
 * Represents the WebSockets tab. It listens to all WebSocket channels and
 * displays messages accordingly. For now it uses a {@link JTable} for this
 * task.
 */
public class WebSocketPanel extends AbstractPanel implements WebSocketObserver {

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
	static Set<Integer> connectedChannelIds;
	
	public static final ImageIcon disconnectIcon;
	public static final ImageIcon connectIcon;
	
	public static final ImageIcon disconnectTargetIcon;
	public static final ImageIcon connectTargetIcon;
	
	static {
		disconnectIcon = new ImageIcon(WebSocketPanel.class.getResource("/resource/icon/fugue/plug-disconnect.png"));
		connectIcon = new ImageIcon(WebSocketPanel.class.getResource("/resource/icon/fugue/plug-connect.png"));
		
		disconnectTargetIcon = new ImageIcon(WebSocketPanel.class.getResource("/resource/icon/fugue/plug-disconnect-target.png"));
		connectTargetIcon = new ImageIcon(WebSocketPanel.class.getResource("/resource/icon/fugue/plug-connect-target.png"));
	};

	private JToolBar panelToolbar = null;

	private JComboBox channelSelect;
	private ComboBoxChannelModel channelSelectModel;

	private JButton handshakeButton;
	private JButton brkButton;
	private JButton filterButton;

	private JLabel filterStatus;
	private WebSocketMessagesViewFilterDialog filterDialog;
	
	private JButton optionsButton;

	private JScrollPane scrollPanel;
	private WebSocketMessagesView messagesView;
	private WebSocketMessagesViewModel messagesModel;

	private WebSocketBreakpointsUiManagerInterface brkManager;

	private TableWebSocket table;

	private HttpPanel requestPanel;
	private HttpPanel responsePanel;

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
		brkManager.setWebSocketPanel(this);
		
		table = webSocketTable;
		channelSelectModel = new ComboBoxChannelModel();
		
		messagesModel = new WebSocketMessagesViewModel(table, getFilterDialog().getFilter());
		messagesView = new WebSocketMessagesView(messagesModel);

		initializePanel();
	}
    
    public void setDisplayPanel(HttpPanel requestPanel, HttpPanel responsePanel) {
        this.requestPanel = requestPanel;
        this.responsePanel = responsePanel;
        
        messagesView.setDisplayPanel(requestPanel, responsePanel);
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

			constraints = new GridBagConstraints();
			constraints.gridx = x++;
			panelToolbar.add(getOptionsButton(), constraints);
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

				    WebSocketChannelDTO channel = (WebSocketChannelDTO) channelSelect.getSelectedItem();
				    if (channel != null && channel.id != null) {
				    	// has valid element selected + a valid reference
				        useModel(channel.id);
				    } else {
				        useJoinedModel();
				    }
			        
			        if (channel != null && channel.historyId != null) {
			        	getShowHandshakeButton().setEnabled(true);
			        } else {
				        getShowHandshakeButton().setEnabled(false);
			        }
			        
			        messagesView.revalidate();
				}
			});
		}
		return channelSelect;
	}

	private JButton getOptionsButton() {
		if (optionsButton == null) {
			optionsButton = new JButton();
			optionsButton.setToolTipText(Constant.messages.getString("websocket.toolbar.button.options"));
			optionsButton.setIcon(new ImageIcon(WebSocketPanel.class.getResource("/resource/icon/16/041.png")));
			optionsButton.addActionListener(new ActionListener () {
				@Override
				public void actionPerformed(ActionEvent e) {
					Control.getSingleton().getMenuToolsControl().options(
							Constant.messages.getString("websocket.panel.title"));
				}
			});
		}
		return optionsButton;
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
					WebSocketChannelDTO channel = (WebSocketChannelDTO) channelSelect.getSelectedItem();
					HistoryReference handshakeRef = channel.getHandshakeReference();
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
					brkManager.handleAddBreakpoint(new WebSocketMessageDTO());
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
			
			scrollPanel = new JScrollPane(messagesView.getViewComponent());
			scrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			scrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			scrollPanel.setPreferredSize(new Dimension(800,200));
			scrollPanel.setName("WebSocketPanelActions");
			
			scrollPanel.getVerticalScrollBar().addAdjustmentListener(new StickyScrollbarAdjustmentListener());
		}
		return scrollPanel;
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
			messagesModel.fireMessageArrived(message.getDTO());
		}
		return true;
	}

	@Override
	public void onStateChange(State state, WebSocketProxy proxy) {
		WebSocketChannelDTO channel = proxy.getDTO();
		
		int connectedChannelsCount = 0;
		boolean isNewChannel = false;
		
		synchronized (connectedChannelIds) {
			boolean isConnectedChannel = connectedChannelIds.contains(channel.id);
	
			switch (state){
			case CLOSED:
				if (isConnectedChannel && channel.endTimestamp != null) {
					connectedChannelIds.remove(channel.id);
						
					// updates icon
					channelSelectModel.updateElement(channel);
				}
				break;
				
			case EXCLUDED:
				// remove from UI
				connectedChannelIds.remove(channel.id);
				channelSelectModel.removeElement(channel);
				
				messagesModel.fireTableDataChanged();
	            break;
				
			case OPEN:
				if (!isConnectedChannel && channel.endTimestamp == null) {
					connectedChannelIds.add(channel.id);
					channelSelectModel.addElement(channel);
					isNewChannel = true;
				}
				break;
	            
			case INCLUDED:
				// add to UI (probably again)
				connectedChannelIds.add(channel.id);
				channelSelectModel.addElement(channel);
				
				messagesModel.fireTableDataChanged();
				isNewChannel = true;
				break;
			}
			
			// change appearance of WebSocket tab header
			connectedChannelsCount = connectedChannelIds.size();
		}
		
		if (connectedChannelsCount == 0) {
			// change icon, as no WebSocket channel is active
			updateIcon(WebSocketPanel.disconnectIcon);
		} else if (connectedChannelsCount > 0 && isNewChannel) {
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
	public WebSocketMessagesViewFilterDialog getFilterDialog() {
		if (filterDialog == null) {
			filterDialog = new WebSocketMessagesViewFilterDialog(View.getSingleton().getMainFrame(), true);
			
		}
		return filterDialog;
	}
	
	/**
	 * Shows filter dialog
	 * 
	 * @return 1 is returned if applied, -1 when dialog was reseted.
	 */
	protected int showFilterDialog() {
		WebSocketMessagesViewFilterDialog dialog = getFilterDialog();
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
    	WebSocketMessagesViewFilter filter = getFilterDialog().getFilter();
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

		synchronized (channelSelectModel) {
			// reset channel selector's model
			Object selectedItem = channelSelectModel.getSelectedItem();
				
			channelSelectModel.reset();
			
			try {
				for (WebSocketChannelDTO channel : table.getChannelItems()) {
					channelSelectModel.addElement(channel);
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

	public void showMessage(WebSocketMessageDTO message) throws WebSocketException {
		setTabFocus();

		// show channel if not already active
		Integer activeChannelId = messagesModel.getActiveChannelId();
		if (message.channel.id != null && !message.channel.id.equals(activeChannelId)) {
			messagesModel.setActiveChannel(message.channel.id);
			channelSelectModel.setSelectedChannelId(message.channel.id);
		}
		
		// check if message is filtered out
		WebSocketMessagesViewFilter filter = getFilterDialog().getFilter();
		if (filter.isBlacklisted(message)) {
			// make it visible by resetting filter
			filter.reset();
		    setFilterStatus();
			applyFilter();
		}
		
		// select message and scroll there
		messagesView.selectAndShowItem(message);
	}
}

