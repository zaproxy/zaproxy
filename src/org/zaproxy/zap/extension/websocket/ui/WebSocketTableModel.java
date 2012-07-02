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

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang.time.FastDateFormat;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.extension.websocket.WebSocketMessage;
import org.zaproxy.zap.extension.websocket.WebSocketMessage.Direction;

/**
 * This model takes an arbitrary number of {@link WebSocketMessage} instances,
 * extracts necessary information for the user interface out of it and stores
 * them in a {@link List}.
 * <p>
 * It manages two lists internally, one that contains all messages and another
 * one, that contains just those messages not blacklisted by the given
 * {@link WebSocketModelFilter}. TODO: Use {@link WebSocketTable}.
 */
public class WebSocketTableModel extends DefaultTableModel {
	
	private static final long serialVersionUID = -5047686640383236512L;
	
	/**
	 * Number of columns in this table model
	 */
	private static final int COLUMN_COUNT = 6;
	
	/**
	 * Names of all columns.
	 */
	private final Vector<String> columnNames;

	/**
	 * This list holds all WebSocket messages for this view model.
	 */
	private List<WebSocketMessageDAO> allMessages = new ArrayList<WebSocketMessageDAO>();

	/**
	 * This list holds all WebSocket messages that are not blacklisted by
	 * {@link WebSocketTableModel#filter}.
	 */
	private List<WebSocketMessageDAO> filteredMessages = new ArrayList<WebSocketMessageDAO>();

	/**
	 * Used to show only specific messages.
	 */
	private WebSocketModelFilter filter;
	
	/**
	 * Add a unique id to each message of one view model.
	 */
	private AtomicInteger messageCounter;

	/**
	 * WebSocket channel id.
	 */
	private int channelId;
	
	/**
	 * Used to display {@link WebSocketMessageDAO#dateTime} in user's locale.
	 */
	private static FastDateFormat dateFormatter;
	
	/**
	 * Use the static initializer for setting up one date formatter for all
	 * instances.
	 */
	static {
		// milliseconds are added later (via usage java.sql.Timestamp.getNanos())
		dateFormatter = FastDateFormat.getDateTimeInstance(
				SimpleDateFormat.SHORT, SimpleDateFormat.MEDIUM,
				Constant.getLocale());
	}
	
	/**
	 * Ctor.
	 * 
	 * @param webSocketFilter
	 */
	public WebSocketTableModel(WebSocketModelFilter webSocketFilter, int channelId) {
		super();
		
		filter = webSocketFilter;
		this.channelId = channelId;
		
		messageCounter = new AtomicInteger(0);

		ResourceBundle msgs = Constant.messages;
		columnNames = new Vector<String>(COLUMN_COUNT);
		columnNames.add(msgs.getString("websocket.table.header.id"));
		columnNames.add(msgs.getString("websocket.table.header.direction"));
		columnNames.add(msgs.getString("websocket.table.header.timestamp"));
		columnNames.add(msgs.getString("websocket.table.header.opcode"));
		columnNames.add(msgs.getString("websocket.table.header.payload_length"));
		columnNames.add(msgs.getString("websocket.table.header.payload"));
	}
	
	/**
	 * Data Access Object used for displaying WebSockets communication.
	 */
	public class WebSocketMessageDAO {

		/**
		 * WebSocket channel id
		 */
		public int channelId;
		
		/**
		 * consecutive number
		 */
		public int id;
		
		/**
		 * Used for sorting, containing time in milliseconds.
		 */
		protected long timestamp;
		
		/**
		 * When the message was finished (it might contain of several frames).
		 */
		public String dateTime;
		
		/**
		 * You can retrieve a textual version of this opcode via:
		 * {@link WebSocketMessage#opcode2string(int)}.
		 */
		public int opcode;
		
		/**
		 * Textual representation of {@link WebSocketMessageDAO#opcode}.
		 */
		public String readableOpcode;
		
		/**
		 * Might be either a string (readable representation for TEXT frames) OR byte[].
		 */
		public String payload;
		
		/**
		 * For close messages, there is always a reason.
		 */
		public int closeCode;
		
		/**
		 * Outgoing or incoming message.
		 */
		public WebSocketMessage.Direction direction;

		/**
		 * Number of the payload bytes.
		 */
		public int payloadLength;
		
		/**
		 * Sets the consecutive number.
		 */
		public WebSocketMessageDAO() {
			id = messageCounter.incrementAndGet();
		}
		
		/**
		 * Useful representation for debugging purposes.
		 */
		public String toString() {
			return "Id=" + id + ";Opcode=" + readableOpcode + ";Bytes=" + payloadLength;
		}
	}
	
	/**
	 * Returns all whitelisted messages for this model, each with a unique id.
	 * Those blacklisted by the {@link WebSocketModelFilter} are excluded.
	 * 
	 * @return
	 */
	public List<WebSocketMessageDAO> getMessages() {
		return filteredMessages;
	}

	/**
	 * Returns the size of currently visible messages.
	 * 
	 * @return
	 */
	@Override
	public int getRowCount() {
		// this method is called by the parent constructor,
		// when filteredMessages is not initialized
		if (filteredMessages == null) {
			return 0;
		}
		
		return filteredMessages.size();
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
			return String.class;
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
	 * Returns the object for given field indexes.
	 * 
	 * @return
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (filteredMessages.size() > rowIndex) {
			WebSocketMessageDAO message = filteredMessages.get(rowIndex);
			
			switch (columnIndex) {
			case 0:
				return "#" + message.channelId + "." + message.id;

			case 1:
				if (message.direction.equals(Direction.OUTGOING)) {
					return "→";
				} else if (message.direction.equals(Direction.INCOMING)) {
					return "←";
				}
				break;

			case 2:
				return message.dateTime;

			case 3:
				return message.opcode + "=" + message.readableOpcode;

			case 4:
				return message.payloadLength;

			case 5:
				return message.payload;
			}
		}

		return null;
	}

	/**
	 * Extracts necessary data from the {@link WebSocketMessage} object and
	 * stores only necessary informations.
	 * @param channelId 
	 * 
	 * @param message
	 * @return 
	 */
	public WebSocketMessageDAO addWebSocketMessage(WebSocketMessage message) {
		WebSocketMessageDAO dao = new WebSocketMessageDAO();
		
		dao.channelId = channelId;
		
		Timestamp ts = message.getTimestamp();
		dao.timestamp = ts.getTime() + (ts.getNanos() / 1000000);
		
		String dateTime = dateFormatter.format(ts);
		String nanos = message.getTimestamp().getNanos() + "";
		dao.dateTime = dateTime.replaceFirst("([0-9]+:[0-9]+:[0-9]+)", "$1." + nanos.replaceAll("0+$", ""));
		
		dao.opcode = message.getOpcode();
		dao.readableOpcode = message.getOpcodeString();
		
		if (message.isText()) {
			dao.payload = message.getReadablePayload();
		} else if (message.isBinary()) {
			dao.payload = byteArrayToHexString(message.getPayload());
		} else if (message.getOpcode() == WebSocketMessage.OPCODE_CLOSE) {
			dao.closeCode = message.getCloseCode();
		}
		
		dao.direction = message.getDirection();
		
		dao.payloadLength = message.getPayloadLength();
		
		addWebSocketMessage(dao);
		
		return dao;
	}

	public void addWebSocketMessage(WebSocketMessageDAO message) {
		allMessages.add(message);
		if (filter.isBlacklisted(message)) {
			// no need for adding the message to the filteredMessages list
		} else {
			int rows = filteredMessages.size();
			filteredMessages.add(message);
			fireTableRowsInserted(rows - 1, rows);
		}
	}

	/**
	 * Fills the list of messages that will be displayed (i.e. that are not
	 * blacklisted by the {@link WebSocketModelFilter}.
	 */
	public void reFilter() {
		synchronized (filteredMessages) {
			filteredMessages.clear();
			for (WebSocketMessageDAO message : allMessages) {
				if (!filter.isBlacklisted(message)) {
					filteredMessages.add(message);
				}
			}
		}
		
		fireTableDataChanged();
	}

//	private byte[] hexStringToByteArray(String hexStr) {
//		int len = hexStr.length() - 1;
//		byte[] data = new byte[(len / 2) + 1];
//		for (int i = 0; i < len; i += 2) {
//			data[i / 2] = (byte) ((Character.digit(hexStr.charAt(i), 16) << 4) + Character
//					.digit(hexStr.charAt(i + 1), 16));
//		}
//		return data;
//	}
	
	private String byteArrayToHexString(byte[] byteArray) {
		StringBuffer hexString = new StringBuffer("");

		for (int i = 0; i < byteArray.length; i++) {
			String hexCharacter = Integer.toHexString(byteArray[i]);

			if (hexCharacter.length() < 2) {
				hexCharacter = "0" + Integer.toHexString(byteArray[i]);
			}
			
			if (i < 10) {
				hexString.append(" " + hexCharacter.toUpperCase());
			} else {
				hexString.append(" " + hexCharacter.toUpperCase() + "\n");
				i = 0;
			}
		}
		
		return hexString.toString();
	}

	/**
	 * Resets the messages list.
	 */
	public void clear() {
		allMessages.clear();
		reFilter();
	}

	/**
	 * Add a bulk of messages with this operation.
	 * 
	 * @param messages
	 */
	public void addMessages(List<WebSocketMessageDAO> messages) {
		allMessages.addAll(messages);
		reFilter();
	}
	
	public String toString() {
		return "Model listing " + getRowCount() + " of " + allMessages.size() + " possible messages!";
	}
}
