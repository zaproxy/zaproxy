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
package org.zaproxy.zap.extension.websocket.fuzz;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.extension.websocket.WebSocketMessageDTO;
import org.zaproxy.zap.extension.websocket.db.TableWebSocket;
import org.zaproxy.zap.extension.websocket.ui.WebSocketMessagesViewModel;

/**
 * This {@link TableModel} is also backed by the database, but has got some
 * additional columns. Moreover, erroneous entries are stored into an extra
 * {@link List}.
 */
public class WebSocketFuzzMessagesViewModel extends WebSocketMessagesViewModel {
    private static final long serialVersionUID = 5435325545219552543L;

	private static final Logger logger = Logger.getLogger(WebSocketFuzzMessagesViewModel.class);

	/**
	 * Names of new columns.
	 */
	private static final String[] COLUMN_NAMES = {
	        Constant.messages.getString("websocket.table.header.state"),
	        Constant.messages.getString("websocket.table.header.fuzz") };
	
    /**
	 * Number of columns in this table model increased.
	 */
	private static final int COLUMN_COUNT = WebSocketMessagesViewModel.COLUMN_COUNT + COLUMN_NAMES.length;

	/**
	 * This list holds all erroneous messages for this view model.
	 */
	private List<WebSocketMessageDTO> erroneousMessages = new ArrayList<>();

	private Integer currentFuzzId = Integer.valueOf(-1);

	private static final String msgSuccess;
	private static final String msgFail;
	
	static {
		msgSuccess = Constant.messages.getString("websocket.fuzz.success");
		msgFail = Constant.messages.getString("websocket.fuzz.fail");
	}
    
	/**
	 * No arguments required to create an object of this class.
	 */
	public WebSocketFuzzMessagesViewModel(TableWebSocket table) {
		super(table);
	}
	
	/**
	 * @return number of columns
	 */
	@Override
	public int getColumnCount() {
		return COLUMN_COUNT;
	}
	
	/**
	 * @return name of the given column index
	 */
	@Override
	public String getColumnName(int columnIndex) {
		final int totalParent = WebSocketMessagesViewModel.COLUMN_COUNT;
		if (columnIndex < totalParent) {
			return super.getColumnName(columnIndex);
		}
		return COLUMN_NAMES[columnIndex - totalParent];
	}
	
	/**
	 * Return values of new columns.
	 */
	@Override
	public Object getRealValueAt(WebSocketMessageDTO message, int columnIndex) {
		if (message instanceof WebSocketFuzzMessageDTO) {
			WebSocketFuzzMessageDTO fuzzMessage = (WebSocketFuzzMessageDTO) message;
			switch (columnIndex) {
			case 6:
		        String state = "";
		        switch (fuzzMessage.state) {
		        case SUCCESSFUL:
		            state = msgSuccess;
		            break;
		        case ERROR:
		            state = msgFail;
		            break;
		        }
		        return state;
		        
		    case 7:
		        return fuzzMessage.fuzz;
			}
		}
		return super.getRealValueAt(message, columnIndex);
	}

	/**
	 * @return type of column for given column index
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 6:
		case 7:
			return String.class;
		default:
			return super.getColumnClass(columnIndex);
		}
	}

	/**
	 * Row count is determined by size of messages list.
	 */
	@Override
	protected WebSocketMessageDTO getCriterionMessage() {
		WebSocketFuzzMessageDTO fuzzMessage = new WebSocketFuzzMessageDTO();
		fuzzMessage.fuzzId = currentFuzzId;
		return fuzzMessage;
	}

	@Override
	protected List<Integer> getCriterionOpcodes() {
		return new ArrayList<>(0);
	}

	@Override
	protected List<Integer> getCriterianInScope() {
		return null;
	}

	/**
	 * Adds new message, that failed to be sent over channel.
	 * 
	 * @param message
	 */
	public void addErroneousWebSocketMessage(WebSocketFuzzMessageDTO message) {
		if (currentFuzzId != null && currentFuzzId.equals(getFuzzId(message))) {
			erroneousMessages.add(message);
			
			int rowCount = getRowCount();
			fireTableRowsInserted(rowCount, rowCount);
		}
	}
	
	/**
	 * Updates view for successful messages. For erroneous messages,
	 * {@link #addErroneousWebSocketMessage(WebSocketFuzzMessageDTO)} is called.
	 * 
	 * @param message
	 */
	@Override
	public void fireMessageArrived(WebSocketMessageDTO message) {
		Integer fuzzId = getFuzzId(message);
		if (fuzzId != null) {
			if (currentFuzzId .equals(fuzzId)) {
				logger.info("new fuzzed message sent #" + message.channel.id + "." + message.id);
				super.fireMessageArrived(message);
			} else {
				currentFuzzId = fuzzId;
				clear();
				fireTableDataChanged();
			}
		}
	}

	private Integer getFuzzId(WebSocketMessageDTO message) {
		if (message instanceof WebSocketFuzzMessageDTO) {
			return ((WebSocketFuzzMessageDTO) message).fuzzId;
		}
		return null;
	}
	
	@Override
	public int getRowCount() {
		return erroneousMessages.size() + super.getRowCount();
	}
	
	@Override
	protected void clear() {
		super.clear();
		erroneousMessages.clear();
	}
	
	@Override
	protected List<WebSocketMessageDTO> loadPage(int offset, int length) {
		// erroneous messages are put onto the end of list
		int sqlRowCount = super.getRowCount();
		synchronized (erroneousMessages) {
			int erroneousRowCount = erroneousMessages.size();
		
			if (offset >= sqlRowCount) {
				offset = offset - (sqlRowCount - 1);
				return new ArrayList<>(erroneousMessages.subList(offset, Math.min(erroneousRowCount, offset + length)));
			} else if (offset + length >= sqlRowCount) {
				int sqlLength = sqlRowCount - offset;
				List<WebSocketMessageDTO> page = super.loadPage(offset, sqlLength);
				page.addAll(erroneousMessages.subList(0, Math.min(erroneousRowCount, length - sqlLength)));
				return page;
			} else {
				return super.loadPage(offset, length);
			}
		}
	}
}
