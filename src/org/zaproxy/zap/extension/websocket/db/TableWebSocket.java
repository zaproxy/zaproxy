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
package org.zaproxy.zap.extension.websocket.db;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.collections.map.LRUMap;
import org.apache.log4j.Logger;
import org.hsqldb.jdbc.JDBCBlob;
import org.hsqldb.jdbc.JDBCClob;
import org.parosproxy.paros.db.AbstractTable;
import org.parosproxy.paros.db.DbUtils;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.zaproxy.zap.extension.websocket.WebSocketChannelDTO;
import org.zaproxy.zap.extension.websocket.WebSocketMessage;
import org.zaproxy.zap.extension.websocket.WebSocketMessageDTO;
import org.zaproxy.zap.extension.websocket.fuzz.WebSocketFuzzMessageDTO;

/**
 * Manages writing and reading WebSocket messages to the database.
 */
public class TableWebSocket extends AbstractTable {
	private static final Logger logger = Logger.getLogger(TableWebSocket.class);
	
	private Set<Integer> channelIds;
	private LRUMap channelCache;
    
    private PreparedStatement psInsertMessage;
    
	private PreparedStatement psSelectChannels;
	
    private PreparedStatement psInsertChannel;
	private PreparedStatement psUpdateChannel;
	
	private PreparedStatement psUpdateHistoryFk;

	private PreparedStatement psDeleteChannel;
	private PreparedStatement psDeleteMessagesByChannelId;

	private PreparedStatement psInsertFuzz;
	
	private PreparedStatement psSelectMessage;
	
	private PreparedStatement psSelectMaxChannelId;

	private Queue<WebSocketMessageDTO> messagesBuffer = new LinkedList<>();
	private Queue<WebSocketChannelDTO> channelsBuffer = new LinkedList<>();
	
    /**
     * Create tables if not already available
     */
    @Override
    protected void reconnect(Connection conn) throws SQLException {
    	if (!DbUtils.hasTable(conn, "WEBSOCKET_CHANNEL")) {
			// need to create the tables			
			PreparedStatement stmt = conn
					.prepareStatement("CREATE CACHED TABLE websocket_channel ("
							+ "channel_id BIGINT PRIMARY KEY,"
							+ "host VARCHAR(255) NOT NULL,"
							+ "port INTEGER NOT NULL,"
							+ "url VARCHAR(255) NOT NULL,"
							+ "start_timestamp TIMESTAMP NOT NULL,"
							+ "end_timestamp TIMESTAMP NULL,"
							+ "history_id INTEGER NULL,"
							+ "FOREIGN KEY (history_id) REFERENCES HISTORY(HISTORYID) ON DELETE SET NULL ON UPDATE SET NULL"
							+ ")");
			DbUtils.executeAndClose(stmt);
			
			stmt = conn.prepareStatement("CREATE CACHED TABLE websocket_message ("
							+ "message_id BIGINT NOT NULL,"
							+ "channel_id BIGINT NOT NULL,"
							+ "timestamp TIMESTAMP NOT NULL,"
							+ "opcode TINYINT NOT NULL,"
							+ "payload_utf8 CLOB(16M) NULL,"
							+ "payload_bytes BLOB(16M) NULL,"
							+ "payload_length BIGINT NOT NULL,"
							+ "is_outgoing BOOLEAN NOT NULL,"
							+ "PRIMARY KEY (message_id, channel_id),"
							+ "FOREIGN KEY (channel_id) REFERENCES websocket_channel(channel_id)"
							+ ")");
			DbUtils.executeAndClose(stmt);
			
			stmt = conn.prepareStatement("ALTER TABLE websocket_message "
					+ "ADD CONSTRAINT websocket_message_payload "
					+ "CHECK (payload_utf8 IS NOT NULL OR payload_bytes IS NOT NULL)");
			DbUtils.executeAndClose(stmt);
			
			stmt = conn.prepareStatement("CREATE CACHED TABLE websocket_message_fuzz ("
							+ "fuzz_id BIGINT NOT NULL,"
							+ "message_id BIGINT NOT NULL,"
							+ "channel_id BIGINT NOT NULL,"
							+ "state VARCHAR(50) NOT NULL,"
							+ "fuzz LONGVARCHAR NOT NULL,"
							+ "PRIMARY KEY (fuzz_id, message_id, channel_id),"
							+ "FOREIGN KEY (message_id, channel_id) REFERENCES websocket_message(message_id, channel_id) ON DELETE CASCADE"
							+ ")");
			DbUtils.executeAndClose(stmt);
			
			channelIds = new HashSet<>();
		} else {
			channelIds = null;
		}
    	
		channelCache = new LRUMap(20);
        
		// CHANNEL
        psSelectMaxChannelId = conn.prepareStatement("SELECT MAX(c.channel_id) as channel_id "
        		+ "FROM websocket_channel AS c");
        
        psSelectChannels = conn.prepareStatement("SELECT c.* "
        		+ "FROM websocket_channel AS c "
        		+ "ORDER BY c.channel_id");

        // id goes last to be consistent with update query
		psInsertChannel = conn.prepareStatement("INSERT INTO "
				+ "websocket_channel (host, port, url, start_timestamp, end_timestamp, history_id, channel_id) "
				+ "VALUES (?,?,?,?,?,?,?)");

		psUpdateChannel = conn.prepareStatement("UPDATE websocket_channel SET "
				+ "host = ?, port = ?, url = ?, start_timestamp = ?, end_timestamp = ?, history_id = ? "
				+ "WHERE channel_id = ?");
		
		psUpdateHistoryFk = conn.prepareStatement("UPDATE websocket_channel SET "
				+ "history_id = ? "
				+ "WHERE channel_id = ?");
		
		psDeleteChannel = conn.prepareStatement("DELETE FROM websocket_channel "
				+ "WHERE channel_id = ?");
        
		// MESSAGE
        psSelectMessage = conn.prepareStatement("SELECT m.*, f.fuzz_id, f.state, f.fuzz "
        		+ "FROM websocket_message AS m "
				+ "LEFT OUTER JOIN websocket_message_fuzz f "
        		+ "ON m.message_id = f.message_id AND m.channel_id = f.channel_id "
        		+ "WHERE m.message_id = ? AND m.channel_id = ?");
		
		psInsertMessage = conn.prepareStatement("INSERT INTO "
				+ "websocket_message (message_id, channel_id, timestamp, opcode, payload_utf8, payload_bytes, payload_length, is_outgoing) "
				+ "VALUES (?,?,?,?,?,?,?,?)");
		
		psInsertFuzz = conn.prepareStatement("INSERT INTO "
				+ "websocket_message_fuzz (fuzz_id, message_id, channel_id, state, fuzz) "
				+ "VALUES (?,?,?,?,?)");
		
		psDeleteMessagesByChannelId = conn.prepareStatement("DELETE FROM websocket_message "
				+ "WHERE channel_id = ?");
		
		if (channelIds == null) {
			channelIds = new HashSet<>();
			PreparedStatement psSelectChannelIds = conn.prepareStatement("SELECT c.channel_id "
					+ "FROM websocket_channel AS c "
					+ "ORDER BY c.channel_id");
			try {
				psSelectChannelIds.execute();
				
				ResultSet rs = psSelectChannelIds.getResultSet();
				while (rs.next()) {
					channelIds.add(rs.getInt(1));
				}
			} finally {
				try {
					psSelectChannelIds.close();
				} catch (SQLException e) {
					if (logger.isDebugEnabled()) {
						logger.debug(e.getMessage(), e);
					}
				}
			}
		}
    }

    /**
	 * Prepares a {@link PreparedStatement} instance on the fly.
	 * 
	 * @param criteria
	 * @param opcodes Null when all opcodes should be retrieved.
	 * @return number of message that fulfill given template
	 * @throws SQLException
	 */
	public synchronized int getMessageCount(WebSocketMessageDTO criteria, List<Integer> opcodes) throws SQLException {
		return getMessageCount(criteria, opcodes, null);
	}
	
	/**
	 * Prepares a {@link PreparedStatement} instance on the fly.
	 * 
	 * @param criteria
	 * @param opcodes Null when all opcodes should be retrieved.
	 * @param inScopeChannelIds 
	 * @return number of message that fulfill given template
	 * @throws SQLException
	 */
	public synchronized int getMessageCount(WebSocketMessageDTO criteria, List<Integer> opcodes, List<Integer> inScopeChannelIds) throws SQLException {
		String query = "SELECT COUNT(m.message_id) FROM websocket_message AS m "
				+ "LEFT OUTER JOIN websocket_message_fuzz f "
        		+ "ON m.message_id = f.message_id AND m.channel_id = f.channel_id "
				+ "<where> ";
		
		PreparedStatement stmt = buildMessageCriteriaStatement(query, criteria, opcodes, inScopeChannelIds);
		try {
			return executeAndGetSingleIntValue(stmt);
		} finally {
			stmt.close();
		}
	}

	private int executeAndGetSingleIntValue(PreparedStatement stmt) throws SQLException {
		stmt.execute();
		ResultSet rs = stmt.getResultSet();
		try {
			if (rs.next()) {
				return rs.getInt(1);
			}
			return 0;
		} finally {
			rs.close();
		}
	}

	public synchronized int getIndexOf(WebSocketMessageDTO criteria, List<Integer> opcodes, List<Integer> inScopeChannelIds) throws SQLException {
		String query = "SELECT COUNT(m.message_id) "
        		+ "FROM websocket_message AS m "
				+ "LEFT OUTER JOIN websocket_message_fuzz f "
        		+ "ON m.message_id = f.message_id AND m.channel_id = f.channel_id "
        		+ "<where> AND m.message_id < ?";
		PreparedStatement stmt = buildMessageCriteriaStatement(query, criteria, opcodes, inScopeChannelIds);
		
		int paramsCount = stmt.getParameterMetaData().getParameterCount();
		stmt.setInt(paramsCount, criteria.id);

		try {
			return executeAndGetSingleIntValue(stmt);
		} finally {
			stmt.close();
		}
	}
	
	public synchronized WebSocketMessageDTO getMessage(int messageId, int channelId) throws SQLException {
		psSelectMessage.setInt(1, messageId);
		psSelectMessage.setInt(2, channelId);
		psSelectMessage.execute();
		
		List<WebSocketMessageDTO> messages = buildMessageDTOs(psSelectMessage.getResultSet(), false);
		if (messages.size() != 1) {
			throw new SQLException("Message not found!");
		}
		return messages.get(0);
	}

	/**
	 * Retrieves list of {@link WebSocketMessageDTO}, but loads only parts of the payload.
	 * 
	 * @param criteria
	 * @param opcodes
	 * @param inScopeChannelIds 
	 * @param offset
	 * @param limit
	 * @param payloadPreviewLength
	 * @return Messages that fulfill given template.
	 * @throws SQLException
	 */
	public synchronized List<WebSocketMessageDTO> getMessages(WebSocketMessageDTO criteria, List<Integer> opcodes, List<Integer> inScopeChannelIds, int offset, int limit, int payloadPreviewLength) throws SQLException {
		String query = "SELECT m.message_id, m.channel_id, m.timestamp, m.opcode, m.payload_length, m.is_outgoing, "
				+ "m.payload_utf8, m.payload_bytes, "
				+ "f.fuzz_id, f.state, f.fuzz "
        		+ "FROM websocket_message AS m "
				+ "LEFT OUTER JOIN websocket_message_fuzz f "
        		+ "ON m.message_id = f.message_id AND m.channel_id = f.channel_id "
        		+ "<where> "
        		+ "ORDER BY m.timestamp, m.channel_id, m.message_id "
        		+ "LIMIT ? "
        		+ "OFFSET ?";

		PreparedStatement stmt;
		try {
			stmt = buildMessageCriteriaStatement(query, criteria, opcodes, inScopeChannelIds);
		} catch (SQLException e) {
			if (getConnection().isClosed()) {
				return new ArrayList<>(0);
			}
			
			throw e;
		}
		
		try {
			int paramsCount = stmt.getParameterMetaData().getParameterCount();
			stmt.setInt(paramsCount - 1, limit);
			stmt.setInt(paramsCount, offset);
			
			stmt.execute();
			
			return buildMessageDTOs(stmt.getResultSet(), true, payloadPreviewLength);
		} finally {
			stmt.close();
		}
	}
	
	private List<WebSocketMessageDTO> buildMessageDTOs(ResultSet rs, boolean interpretLiteralBytes) throws SQLException {
		return buildMessageDTOs(rs, interpretLiteralBytes, -1);
	}
	/**
	 * 
	 * @param rs
	 * @param interpretLiteralBytes
	 * @param payloadLength 
	 * @return
	 * @throws HttpMalformedHeaderException
	 * @throws SQLException
	 */
	private List<WebSocketMessageDTO> buildMessageDTOs(ResultSet rs, boolean interpretLiteralBytes, int payloadLength) throws SQLException {
		ArrayList<WebSocketMessageDTO> messages = new ArrayList<>();
		try {
			while (rs.next()) {
				WebSocketMessageDTO message;
				
				int channelId = rs.getInt("channel_id");
				WebSocketChannelDTO channel = getChannel(channelId);
				if (rs.getInt("fuzz_id") != 0) {
					WebSocketFuzzMessageDTO fuzzMessage = new WebSocketFuzzMessageDTO(channel);
					fuzzMessage.fuzzId = rs.getInt("fuzz_id");
					fuzzMessage.state = WebSocketFuzzMessageDTO.State.valueOf(rs.getString("state"));
					fuzzMessage.fuzz = rs.getString("fuzz");
					
					message = fuzzMessage;
				} else {
					message = new WebSocketMessageDTO(channel);
				}
				
				message.id = rs.getInt("message_id");
				message.setTime(rs.getTimestamp("timestamp"));
				message.opcode = rs.getInt("opcode");
				message.readableOpcode = WebSocketMessage.opcode2string(message.opcode);
				
				// read payload
				if (message.opcode == WebSocketMessage.OPCODE_BINARY) {
					if (payloadLength == -1) {
						// load all bytes
						message.payload = rs.getBytes("payload_bytes");
					} else {
						Blob blob = rs.getBlob("payload_bytes");
						int length = Math.min(payloadLength, (int) blob.length());
						message.payload = blob.getBytes(1, length);
						blob.free();
					}
					
					if (message.payload == null) {
						message.payload = new byte[0];
					}
				} else {
					if (payloadLength == -1) {
						// load all characters
						message.payload = rs.getString("payload_utf8");
					} else {
						Clob clob = rs.getClob("payload_utf8");
						int length = Math.min(payloadLength, (int) clob.length());
						message.payload = clob.getSubString(1, length);
						clob.free();
					}
					
					if (message.payload == null) {
						message.payload = "";
					}
				}
				
				message.isOutgoing = rs.getBoolean("is_outgoing");
				message.payloadLength = rs.getInt("payload_length");
				
				messages.add(message);
			}
		} finally {
			rs.close();
		}
		
		messages.trimToSize();
		
		return messages;
	}

	private WebSocketChannelDTO getChannel(int channelId) throws SQLException {
		if (!channelCache.containsKey(channelId)) {
			WebSocketChannelDTO criteria = new WebSocketChannelDTO();
			criteria.id = channelId;
			List<WebSocketChannelDTO> channels = getChannels(criteria);
			if (channels.size() == 1) {
				channelCache.put(channelId, channels.get(0));
			} else {
				throw new SQLException("Channel '" + channelId + "' not found!");
			}
		}
		return (WebSocketChannelDTO) channelCache.get(channelId);
	}

	private PreparedStatement buildMessageCriteriaStatement(String query, WebSocketMessageDTO criteria, List<Integer> opcodes, List<Integer> inScopeChannelIds) throws SQLException {
		ArrayList<String> where = new ArrayList<>();
		ArrayList<Object> params = new ArrayList<>();

		if (criteria.channel.id != null) {
			where.add("m.channel_id = ?");
			params.add(criteria.channel.id);
		}
		
		if (criteria.isOutgoing != null) {
			where.add("m.is_outgoing = ?");
			params.add(criteria.isOutgoing);
		}
		
		if (opcodes != null && !opcodes.isEmpty()) {
			StringBuilder opcodeExpr = new StringBuilder("(");
			int opcodesCount = opcodes.size();
			
			for (int i = 0; i < opcodesCount; i++) {
				params.add(opcodes.get(i));
				
				opcodeExpr.append("m.opcode = ?");
				if ((i + 1) < opcodesCount) {
					opcodeExpr.append(" OR ");
				}
			}
			
			opcodeExpr.append(")");
			where.add(opcodeExpr.toString());
		}
		
		if (inScopeChannelIds != null) {
			StringBuilder whereExpr = new StringBuilder("m.channel_id IN (");
			int inScopeChannelCount = inScopeChannelIds.size();
			
			if (inScopeChannelCount > 0) {
				for (int i = 0; i < inScopeChannelCount; i++) {
					params.add(inScopeChannelIds.get(i));
					
					whereExpr.append("?");
					if ((i + 1) < inScopeChannelCount) {
						whereExpr.append(",");
					}
				}
			} else {
				whereExpr.append("null");
			}
			
			whereExpr.append(")");
			where.add(whereExpr.toString());
		}

		if (criteria instanceof WebSocketFuzzMessageDTO) {
			WebSocketFuzzMessageDTO fuzzCriteria = (WebSocketFuzzMessageDTO) criteria;
			if (fuzzCriteria.fuzzId != null) {
				params.add(fuzzCriteria.fuzzId);
				where.add("f.fuzz_id = ?");
			}
		}
		
		where.trimToSize();
		params.trimToSize();
		
		return buildCriteriaStatementHelper(query, where, params);
	}

	public WebSocketMessagePrimaryKey getMessagePrimaryKey(WebSocketMessageDTO message) {
		return new WebSocketMessagePrimaryKey(message.channel.id, message.id);
	}

	public List<WebSocketChannelDTO> getChannelItems() throws SQLException {
		psSelectChannels.execute();
		ResultSet rs = psSelectChannels.getResultSet();
		
		return buildChannelDTOs(rs);
	}

	private List<WebSocketChannelDTO> buildChannelDTOs(ResultSet rs) throws SQLException {
		ArrayList<WebSocketChannelDTO> channels = new ArrayList<>();
		try {
			while (rs.next()) {
				WebSocketChannelDTO channel = new WebSocketChannelDTO();
				channel.id = rs.getInt("channel_id");
				channel.host = rs.getString("host");
				channel.port = rs.getInt("port");
				channel.url = rs.getString("url");
				channel.startTimestamp = rs.getTimestamp("start_timestamp").getTime();
				
				Time endTs = rs.getTime("end_timestamp");
				channel.endTimestamp = (endTs != null) ? endTs.getTime() : null;
				
				channel.historyId = rs.getInt("history_id");
				
				channels.add(channel);
			}
		} finally {
			rs.close();
		}
		
		channels.trimToSize();
		
		return channels;
	}
	
	public void insertOrUpdateChannel(WebSocketChannelDTO channel) throws SQLException {
		synchronized (this) {
			if (getConnection().isClosed()) {
				// temporarily buffer channels and insert/update later
				channelsBuffer.offer(channel);
				return;
			}
			
			do {
				PreparedStatement stmt;
				boolean addIdOnSuccess = false;
				
				// first, find out if already inserted
				if (channelIds.contains(channel.id)) {
					// proceed with update
					stmt = psUpdateChannel;
				} else {
					// proceed with insert
					stmt = psInsertChannel;
					addIdOnSuccess = true;
					logger.info("insert channel: " + channel.toString());
				}
		
				stmt.setString(1, channel.host);
				stmt.setInt(2, channel.port);
				stmt.setString(3, channel.url);
				stmt.setTimestamp(4, (channel.startTimestamp != null) ? new Timestamp(channel.startTimestamp) : null);
				stmt.setTimestamp(5, (channel.endTimestamp != null) ? new Timestamp(channel.endTimestamp) : null);
				stmt.setNull(6, Types.INTEGER);
				stmt.setInt(7, channel.id);
				
				stmt.execute();
				if (addIdOnSuccess) {
					channelIds.add(channel.id);
				}
				
				if (channel.historyId != null) {
					psUpdateHistoryFk.setInt(1, channel.historyId);
					psUpdateHistoryFk.setInt(2, channel.id);
					try {
						psUpdateHistoryFk.execute();
					} catch (SQLException e) {
						// safely ignore this exception
						// on shutdown, the history table is cleaned before
						// WebSocket channels are closed and updated
						if (logger.isDebugEnabled()) {
							logger.debug(e.getMessage(), e);
						}
					}
				}
				
				channel = channelsBuffer.poll();
			} while (channel != null);
		}
	}

	public void insertMessage(WebSocketMessageDTO message) throws SQLException {
		// synchronize on whole object to avoid race conditions with insertOrUpdateChannel()
		synchronized (this) {
			if (getConnection().isClosed()) {
				// temporarily buffer messages and write them the next time
				messagesBuffer.offer(message);
				return;
			}
			
			do {
				if (!channelIds.contains(message.channel.id)) {
					// maybe channel is buffered
					if (channelsBuffer.size() > 0) {
						insertOrUpdateChannel(channelsBuffer.poll());
					}
					throw new SQLException("channel not inserted: " + message.channel.id);
				}
				
				logger.info("insert message: " + message.toString());
	
				psInsertMessage.setInt(1, message.id);
				psInsertMessage.setInt(2, message.channel.id);
				psInsertMessage.setTimestamp(3, new Timestamp(message.timestamp));
				psInsertMessage.setInt(4, message.opcode);
	
				// write payload
				if (message.payload instanceof String) {
					psInsertMessage.setClob(5, new JDBCClob((String) message.payload));
					psInsertMessage.setNull(6, Types.BLOB);
				} else if (message.payload instanceof byte[]) {
					psInsertMessage.setNull(5, Types.CLOB);
					psInsertMessage.setBlob(6, new JDBCBlob((byte[]) message.payload));
				} else {
					throw new SQLException("Attribute 'payload' of class WebSocketMessageDTO has got wrong type!");
				}
				
				psInsertMessage.setInt(7, message.payloadLength);
				psInsertMessage.setBoolean(8, message.isOutgoing);
				psInsertMessage.execute();
				
				if (message instanceof WebSocketFuzzMessageDTO) {
					WebSocketFuzzMessageDTO fuzzMessage = (WebSocketFuzzMessageDTO) message;
					psInsertFuzz.setInt(1, fuzzMessage.fuzzId);
					psInsertFuzz.setInt(2, fuzzMessage.id);
					psInsertFuzz.setInt(3, fuzzMessage.channel.id);
					psInsertFuzz.setString(4, fuzzMessage.state.toString());
					psInsertFuzz.setString(5, fuzzMessage.fuzz);
					psInsertFuzz.execute();
				}
				
				message = messagesBuffer.poll();
			} while (message != null);
		}
	}

	public List<WebSocketChannelDTO> getChannels(WebSocketChannelDTO criteria) throws SQLException {
		String query = "SELECT c.* "
        		+ "FROM websocket_channel AS c "
        		+ "<where> "
        		+ "ORDER BY c.start_timestamp, c.channel_id";

		PreparedStatement stmt;
		try {
			stmt = buildMessageCriteriaStatement(query, criteria);
		} catch (SQLException e) {
			if (getConnection().isClosed()) {
				return new ArrayList<>(0);
			}
			
			throw e;
		}
		
		stmt.execute();
		
		return buildChannelDTOs(stmt.getResultSet());
	}
	
	private PreparedStatement buildMessageCriteriaStatement(String query, WebSocketChannelDTO criteria) throws SQLException {
		List<String> where = new ArrayList<>();
		List<Object> params = new ArrayList<>();
	
		if (criteria.id != null) {
			where.add("c.channel_id = ?");
			params.add(criteria.id);
		}
		
		return buildCriteriaStatementHelper(query, where, params);
	}

	private PreparedStatement buildCriteriaStatementHelper(String query, List<String> where, List<Object> params) throws SQLException {
		int conditionsCount = where.size();
		if (conditionsCount > 0) {
			StringBuilder whereExpr = new StringBuilder();
		    int i = 0;
			for (String condition : where) {
				whereExpr.append(condition);
				
				i++;
				if (i < conditionsCount) {
					// one more will be appended
					whereExpr.append(" AND ");
				}
			}
			query = query.replace("<where>", "WHERE " + whereExpr.toString());
		} else {
			query = query.replace("<where> AND", "WHERE ");
			query = query.replace("<where> ", "");
		}

		PreparedStatement stmt = getConnection().prepareStatement(query);
		try {
			int i = 1;
			for (Object param : params) {
				stmt.setObject(i++, param);
			}
		} catch (SQLException e) {
			stmt.close();
			throw e;
		}
		
		return stmt;
	}

	/**
	 * Deletes all entries from given channelId from database.
	 * 
	 * @param channelId
	 * @throws SQLException 
	 */
	public void purgeChannel(Integer channelId) throws SQLException {
		synchronized (this) {
			if (channelIds.contains(channelId)) {
				psDeleteMessagesByChannelId.setInt(1, channelId);
				psDeleteMessagesByChannelId.execute();
				
				psDeleteChannel.setInt(1, channelId);
				psDeleteChannel.execute();
				
				channelIds.remove(channelId);
			}
		}
	}

	/**
	 * @return current maximum value of the channel column
	 * @throws SQLException 
	 */
	public int getMaxChannelId() throws SQLException {
		synchronized (this) {
			return executeAndGetSingleIntValue(psSelectMaxChannelId);
		}
	}
}
