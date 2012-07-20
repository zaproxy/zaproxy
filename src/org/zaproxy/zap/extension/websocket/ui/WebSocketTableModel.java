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

import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.extension.websocket.WebSocketMessage.Direction;
import org.zaproxy.zap.extension.websocket.WebSocketMessageDAO;
import org.zaproxy.zap.extension.websocket.db.TableWebSocket;
import org.zaproxy.zap.extension.websocket.db.WebSocketMessagePrimaryKey;

/**
 * This model uses the {@link TableWebSocket} instance to load only needed
 * entries from database. Moreover it shows only those entries that are not
 * blacklisted by given {@link WebSocketTableModelFilter}.
 */
public class WebSocketTableModel extends PagingTableModel<WebSocketMessageDAO> {
	
	private static final long serialVersionUID = -5047686640383236512L;

	private static final Logger logger = Logger.getLogger(WebSocketTableModel.class);
	
	/**
	 * Number of columns in this table model
	 */
	private static final int COLUMN_COUNT = 6;
	
	/**
	 * Names of all columns.
	 */
	private final Vector<String> columnNames;

	/**
	 * Used to show only specific messages.
	 */
	private WebSocketTableModelFilter filter;

	/**
	 * Interface to database.
	 */
	private TableWebSocket table;

	/**
	 * If null, all messages are shown.
	 */
	private Integer activeChannelId;
	
	/**
	 * Ctor.
	 * 
	 * @param webSocketTable 
	 * @param webSocketFilter 
	 */
	public WebSocketTableModel(TableWebSocket webSocketTable, WebSocketTableModelFilter webSocketFilter) {
		super();

		table = webSocketTable;
		filter = webSocketFilter;

		ResourceBundle msgs = Constant.messages;
		columnNames = new Vector<String>(COLUMN_COUNT);
		columnNames.add(msgs.getString("websocket.table.header.id"));
		columnNames.add(msgs.getString("websocket.table.header.direction"));
		columnNames.add(msgs.getString("websocket.table.header.timestamp"));
		columnNames.add(msgs.getString("websocket.table.header.opcode"));
		columnNames.add(msgs.getString("websocket.table.header.payload_length"));
		columnNames.add(msgs.getString("websocket.table.header.payload"));
	}
	
	public void setActiveChannel(Integer channelId) {
		activeChannelId = channelId;
		clear();
		fireTableDataChanged();
	}

	/**
	 * Returns the size of currently visible messages.
	 * 
	 * @return
	 */
	@Override
	public int getRowCount() {
		try {			
			return table.getMessageCount(getCriterionDao(), filter.getOpcodes());
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			return 0;
		}
	}

	private WebSocketMessageDAO getCriterionDao() {
		WebSocketMessageDAO dao = new WebSocketMessageDAO();
		
		if (activeChannelId != null) {
			dao.channelId = activeChannelId;
		}
		
		if (filter.getDirection() != null) {
			dao.isOutgoing = filter.getDirection().equals(Direction.OUTGOING) ? true : false;
		}
		
		return dao;
	}
	
	@Override
	public Object getColumnValue(WebSocketMessageDAO message, int columnIndex) {
		switch (columnIndex) {
		case 0:
			return new WebSocketMessagePrimaryKey(message.channelId, message.messageId);

		case 1:
			if (message.isOutgoing) {
				return "→";
			} else {
				return "←";
			}

		case 2:
			return message.dateTime;

		case 3:
			return message.opcode + "=" + message.readableOpcode;

		case 4:
			return message.payloadLength;

		case 5:
			return message.payload;
		}

		return null;
	}

	@Override
	protected Object getPlaceholderValueAt(int columnIndex) {
		if (getColumnClass(columnIndex).equals(String.class)) {
			return "..";
		}
		return null;
	}

	@Override
	protected List<WebSocketMessageDAO> loadPage(int offset, int length) {
		try {
			return table.getMessages(getCriterionDao(), filter.getOpcodes(), offset, length);
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Returns the number of columns.
	 * 
	 * @return
	 */
	@Override
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/**
	 * Returns the name of the given column index.
	 * 
	 * @return
	 */
	@Override
	public String getColumnName(int columnIndex) {
		return columnNames.get(columnIndex);
	}
	
	/**
	 * Cells are not editable.
	 * 
	 * @return
	 */
	@Override
	public boolean isCellEditable(int row, int col) {
		return false;
	}

	/**
	 * Returns the type of column for given column index.
	 * 
	 * @return
	 */
	@Override
	public Class<? extends Object> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return WebSocketMessagePrimaryKey.class;
		case 1:
			return String.class;
		case 2:
			return String.class;
		case 3:
			return String.class;
		case 4:
			return Integer.class;
		case 5:
			return String.class;
		}
		return null;
	}

	/**
	 * Might return null. Always check!
	 * 
	 * @param rowIndex
	 * @return
	 */
	public WebSocketMessageDAO getDAO(int rowIndex) {
		return getRowObject(rowIndex);
	}

	/**
	 * Call this method when a new filter is applied on the messages list.
	 */
	public void fireFilterChanged() {
		clear();
		fireTableDataChanged();
	}

	/**
	 * A new message has arrived. Find out if we need to
	 * {@link AbstractTableModel#fireTableDataChanged()}.
	 * 
	 * @param dao
	 */
	public void fireMessageArrived(WebSocketMessageDAO dao) {
		boolean isWhitelistedChannel = (activeChannelId == null) || dao.channelId.equals(activeChannelId);
		if (filter.isBlacklisted(dao) || !isWhitelistedChannel) {
			// no need to fire update, as it isn't active now
		} else {
			// find out where it is inserted and update precisely
			
			// if new row is inserted at the end
			// it suffices to fire inserted row at the end of list
			
			// with enabled row sorter, you'll have to take care about this
			fireTableRowsInserted(getRowCount(), getRowCount());
		}
	}
}
