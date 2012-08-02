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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hsqldb.jdbc.jdbcBlob;
import org.hsqldb.jdbc.jdbcClob;
import org.parosproxy.paros.db.AbstractTable;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.zaproxy.zap.extension.websocket.WebSocketChannelDAO;
import org.zaproxy.zap.extension.websocket.WebSocketMessage;
import org.zaproxy.zap.extension.websocket.WebSocketMessageDAO;
import org.zaproxy.zap.extension.websocket.fuzz.WebSocketFuzzMessageDAO;

/**
 * Manages writing and reading WebSocket messages to the database.
 */
public class TableWebSocket extends AbstractTable {
	private static Logger logger = Logger.getLogger(TableWebSocket.class);
	
	private Set<Integer> channelIds;
    
    private PreparedStatement psInsertMessage;
    
	private PreparedStatement psSelectChannelIds;
	private PreparedStatement psSelectChannels;
	
    private PreparedStatement psInsertChannel;
	private PreparedStatement psUpdateChannel;
	
	private PreparedStatement psUpdateHistoryFk;

	private PreparedStatement psDeleteChannel;
	private PreparedStatement psDeleteMessagesByChannelId;

	private PreparedStatement psInsertFuzz;
	
	private PreparedStatement psSelectMessage;

//	private PreparedStatement psMergeChannel;

    /**
     * Create tables if not already available
     */
    @Override
    protected void reconnect(Connection conn) throws SQLException {
    	ResultSet rs = conn.getMetaData().getTables(null, null, "WEBSOCKET_CHANNEL", null);
    	try {
	        if (!rs.next()) {
				// need to create the tables
				PreparedStatement stmt = conn
						.prepareStatement("CREATE CACHED TABLE websocket_channel ("
								+ "channel_id BIGINT PRIMARY KEY,"
								+ "host VARCHAR(255) NOT NULL,"
								+ "port INTEGER NOT NULL,"
								+ "start_timestamp TIMESTAMP NOT NULL,"
								+ "end_timestamp TIMESTAMP NULL,"
								+ "history_id INTEGER NULL,"
								+ "FOREIGN KEY (history_id) REFERENCES HISTORY(HISTORYID) ON DELETE SET NULL ON UPDATE SET NULL"
								+ ")");
				stmt.execute();
				stmt.close();
				
				stmt = conn.prepareStatement("CREATE CACHED TABLE websocket_message ("
								+ "message_id BIGINT NOT NULL,"
								+ "channel_id BIGINT NOT NULL,"
								+ "timestamp TIMESTAMP NOT NULL,"
								+ "opcode TINYINT NOT NULL,"
								+ "payload_utf8 LONGVARCHAR NULL,"
								+ "payload_bytes LONGVARBINARY NULL,"
								+ "payload_length BIGINT NOT NULL,"
								+ "is_outgoing BOOLEAN NOT NULL,"
								+ "PRIMARY KEY (message_id, channel_id),"
								+ "FOREIGN KEY (channel_id) REFERENCES websocket_channel(channel_id)"
								+ ")");
				stmt.execute();
				stmt.close();
				
				stmt = conn.prepareStatement("ALTER TABLE websocket_message "
						+ "ADD CONSTRAINT websocket_message_payload "
						+ "CHECK (payload_utf8 IS NOT NULL OR payload_bytes IS NOT NULL)");
				stmt.execute();
				stmt.close();
				
				stmt = conn.prepareStatement("CREATE CACHED TABLE websocket_message_fuzz ("
								+ "fuzz_id BIGINT NOT NULL,"
								+ "message_id BIGINT NOT NULL,"
								+ "channel_id BIGINT NOT NULL,"
								+ "state VARCHAR(50) NOT NULL,"
								+ "fuzz LONGVARCHAR NOT NULL,"
								+ "PRIMARY KEY (fuzz_id, message_id, channel_id),"
								+ "FOREIGN KEY (message_id, channel_id) REFERENCES websocket_message(message_id, channel_id) ON DELETE CASCADE"
								+ ")");
				stmt.execute();
				stmt.close();
				
				channelIds = new HashSet<Integer>();
			} else {
				channelIds = null;
			}
    	} finally {
    		rs.close();
    	}

//        psSelectMessages = conn.prepareStatement("SELECT m.* "
//        		+ "FROM websocket_message AS m "
//        		+ "ORDER BY m.timestamp, m.channel_id, m.message_id "
//        		+ "LIMIT ? "
//        		+ "OFFSET ?");
//		
//        psCountMessages = conn.prepareStatement("SELECT COUNT(message_id) "
//				+ "FROM websocket_message");
//
//        psSelectMessagesForChannel = conn.prepareStatement("SELECT m.* "
//        		+ "FROM websocket_message AS m "
//        		+ "WHERE m.channel_id = ?"
//        		+ "ORDER BY m.timestamp, m.channel_id, m.message_id "
//        		+ "LIMIT ? "
//        		+ "OFFSET ?");
        
        psSelectChannelIds = conn.prepareStatement("SELECT c.channel_id "
        		+ "FROM websocket_channel AS c "
        		+ "ORDER BY c.channel_id");
        
        psSelectChannels = conn.prepareStatement("SELECT c.* "
        		+ "FROM websocket_channel AS c "
        		+ "ORDER BY c.channel_id");
        
        psSelectMessage = conn.prepareStatement("SELECT m.*, f.fuzz_id, f.state, f.fuzz "
        		+ "FROM websocket_message AS m "
				+ "LEFT OUTER JOIN websocket_message_fuzz f "
        		+ "ON m.message_id = f.message_id AND m.channel_id = f.channel_id "
        		+ "WHERE m.message_id = ? AND m.channel_id = ?");

        // id goes last to be consistent with update query
		psInsertChannel = conn.prepareStatement("INSERT INTO "
				+ "websocket_channel (host, port, start_timestamp, end_timestamp, history_id, channel_id) "
				+ "VALUES (?,?,?,?,?,?)");
		
		psDeleteChannel = conn.prepareStatement("DELETE FROM websocket_channel "
				+ "WHERE channel_id = ?");
		
		psDeleteMessagesByChannelId = conn.prepareStatement("DELETE FROM websocket_message "
				+ "WHERE channel_id = ?");

		psUpdateChannel = conn.prepareStatement("UPDATE websocket_channel SET "
				+ "host = ?, port = ?, start_timestamp = ?, end_timestamp = ?, history_id = ? "
				+ "WHERE channel_id = ?");
		
		psUpdateHistoryFk = conn.prepareStatement("UPDATE websocket_channel SET "
				+ "history_id = ? "
				+ "WHERE channel_id = ?");
		
//		psMergeChannel = conn.prepareStatement("MERGE INTO websocket_channel AS old "
//				+ "USING (VALUES(?,?,?,?,?,?)) "
//				+ "AS new (channel_id, host, port, start_timestamp, end_timestamp, history_id) "
//				+ "ON old.channel_id = new.channel_id "
//				+ "WHEN MATCHED THEN UPDATE SET old.host = new.host, old.port = new.port, old.start_timestamp = new.start_timestamp, old.end_timestamp = new.end_timestamp, old.history_id = new.history_id " 
//			    + "WHEN NOT MATCHED THEN INSERT VALUES (new.channel_id, new.host, new.port, new.start_timestamp, new.end_timestamp, new.history_id)");

		psInsertMessage = conn.prepareStatement("INSERT INTO "
				+ "websocket_message (message_id, channel_id, timestamp, opcode, payload_utf8, payload_bytes, payload_length, is_outgoing) "
				+ "VALUES (?,?,?,?,?,?,?,?)");
		
		psInsertFuzz = conn.prepareStatement("INSERT INTO "
				+ "websocket_message_fuzz (fuzz_id, message_id, channel_id, state, fuzz) "
				+ "VALUES (?,?,?,?,?)");
		
		if (channelIds == null) {
			channelIds = new HashSet<Integer>();
			psSelectChannelIds.execute();
			
			rs = psSelectChannelIds.getResultSet();
			while (rs.next()) {
				channelIds.add(rs.getInt(1));
			}
		}
    }

	/**
	 * Prepares a {@link PreparedStatement} instance on the fly.
	 * 
	 * @param criteria
	 * @param opcodes Null when all opcodes should be retrieved.
	 * @return
	 * @throws SQLException
	 */
	public synchronized int getMessageCount(WebSocketMessageDAO criteria, List<Integer> opcodes) throws SQLException {
		String query = "SELECT COUNT(m.message_id) FROM websocket_message AS m "
				+ "LEFT OUTER JOIN websocket_message_fuzz f "
        		+ "ON m.message_id = f.message_id AND m.channel_id = f.channel_id "
				+ "<where> ";
		PreparedStatement stmt = buildMessageCriteriaStatement(query, criteria, opcodes);
		return executeAndGetRowCount(stmt);
	}

	private int executeAndGetRowCount(PreparedStatement stmt) throws SQLException {
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

	public synchronized int getIndexOf(WebSocketMessageDAO criteria, List<Integer> opcodes) throws SQLException {
		String query = "SELECT COUNT(m.message_id) "
        		+ "FROM websocket_message AS m "
				+ "LEFT OUTER JOIN websocket_message_fuzz f "
        		+ "ON m.message_id = f.message_id AND m.channel_id = f.channel_id "
        		+ "<where> AND m.message_id < ?";
		PreparedStatement stmt = buildMessageCriteriaStatement(query, criteria, opcodes);
		
		int paramsCount = stmt.getParameterMetaData().getParameterCount();
		stmt.setInt(paramsCount, criteria.messageId);
		
		return executeAndGetRowCount(stmt);
	}
	
	public synchronized WebSocketMessageDAO getMessage(int messageId, int channelId) throws SQLException {
		psSelectMessage.setInt(1, messageId);
		psSelectMessage.setInt(2, channelId);
		psSelectMessage.execute();
		
		List<WebSocketMessageDAO> list = buildMessageDAOs(psSelectMessage.getResultSet(), false);
		if (list.size() != 1) {
			throw new SQLException("Message not found!");
		}
		return list.get(0);
	}

	/**
	 * Retrieves list of {@link WebSocketMessageDAO}, but loads only parts of the payload.
	 * 
	 * @param criteria
	 * @param opcodes
	 * @param offset
	 * @param limit
	 * @param payloadPreviewLength
	 * @return
	 * @throws SQLException
	 */
	public synchronized List<WebSocketMessageDAO> getMessages(WebSocketMessageDAO criteria, List<Integer> opcodes, int offset, int limit, int payloadPreviewLength) throws SQLException {
		// SUBSTRING returns byte values
		payloadPreviewLength *= 2;
		String query = "SELECT m.message_id, m.channel_id, m.timestamp, m.opcode, m.payload_length, m.is_outgoing, "
				+ "SUBSTRING(m.payload_utf8 FROM 0 FOR " + payloadPreviewLength + ") AS payload_utf8, '' AS payload_bytes, "
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
			stmt = buildMessageCriteriaStatement(query, criteria, opcodes);
		} catch (SQLException e) {
			if (getConnection().isClosed()) {
				return new ArrayList<WebSocketMessageDAO>();
			} else {
				throw e;
			}
		}
		
		int paramsCount = stmt.getParameterMetaData().getParameterCount();
		stmt.setInt(paramsCount - 1, limit);
		stmt.setInt(paramsCount, offset);
		
		stmt.execute();
		
		return buildMessageDAOs(stmt.getResultSet(), true);
	}
	
	/**
	 * 
	 * @param rs
	 * @param interpretLiteralBytes
	 * @return
	 * @throws HttpMalformedHeaderException
	 * @throws SQLException
	 */
	private List<WebSocketMessageDAO> buildMessageDAOs(ResultSet rs, boolean interpretLiteralBytes) throws SQLException {
		List<WebSocketMessageDAO> messages = new ArrayList<WebSocketMessageDAO>();
		try {
			while (rs.next()) {
				WebSocketMessageDAO dao;
				
				if (rs.getInt("fuzz_id") != 0) {
					WebSocketFuzzMessageDAO fuzzDao = new WebSocketFuzzMessageDAO();
					fuzzDao.fuzzId = rs.getInt("fuzz_id");
					fuzzDao.state = WebSocketFuzzMessageDAO.State.valueOf(rs.getString("state"));
					fuzzDao.fuzz = rs.getString("fuzz");
					
					dao = fuzzDao;
				} else {
					dao = new WebSocketMessageDAO();
				}
				
				dao.channelId = rs.getInt("channel_id");
				dao.messageId = rs.getInt("message_id");
				dao.setTime(rs.getTimestamp("timestamp"));
				dao.opcode = rs.getInt("opcode");
				dao.readableOpcode = WebSocketMessage.opcode2string(dao.opcode);
				
				// read payload
				if (dao.opcode == WebSocketMessage.OPCODE_BINARY) {
					dao.payload = rs.getBytes("payload_bytes");
					if (dao.payload == null) {
						dao.payload = new byte[0];
					}
				} else {
					dao.payload = rs.getString("payload_utf8");
					if (dao.payload == null) {
						dao.payload = "";
					}
				}
				
				dao.isOutgoing = rs.getBoolean("is_outgoing");
				dao.payloadLength = rs.getInt("payload_length");
				
				messages.add(dao);
			}
		} finally {
			rs.close();
		}
		
		return messages;
	}

	private PreparedStatement buildMessageCriteriaStatement(String query, WebSocketMessageDAO criteria, List<Integer> opcodes) throws SQLException {
		List<String> where = new ArrayList<String>();
		List<Object> params = new ArrayList<Object>();

		if (criteria.channelId != null) {
			where.add("m.channel_id = ?");
			params.add(criteria.channelId);
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

		if (criteria instanceof WebSocketFuzzMessageDAO) {
			WebSocketFuzzMessageDAO fuzzCriteria = (WebSocketFuzzMessageDAO) criteria;
			if (fuzzCriteria.fuzzId != null) {
				params.add(fuzzCriteria.fuzzId);
				where.add("f.fuzz_id = ?");
			}
		}
		
		return buildCriteriaStatementHelper(query, where, params);
	}

	public WebSocketMessagePrimaryKey getMessagePrimaryKey(WebSocketMessageDAO dao) {
		return new WebSocketMessagePrimaryKey(dao.channelId, dao.messageId);
	}

	public List<WebSocketChannelDAO> getChannelItems() throws SQLException {
		psSelectChannels.execute();
		ResultSet rs = psSelectChannels.getResultSet();
		
		return buildChannelDAOs(rs);
	}

	private List<WebSocketChannelDAO> buildChannelDAOs(ResultSet rs) throws SQLException {
		List<WebSocketChannelDAO> channels = new ArrayList<WebSocketChannelDAO>();
		try {
			while (rs.next()) {
				WebSocketChannelDAO dao = new WebSocketChannelDAO();
				dao.channelId = rs.getInt("channel_id");
				dao.host = rs.getString("host");
				dao.port = rs.getInt("port");
				dao.startTimestamp = rs.getTimestamp("start_timestamp").getTime();
				
				Time endTs = rs.getTime("end_timestamp");
				dao.endTimestamp = (endTs != null) ? endTs.getTime() : null;
				
				dao.historyId = rs.getInt("history_id");
				
				channels.add(dao);
			}
		} finally {
			rs.close();
		}
		
		return channels;
	}
	
	public void insertOrUpdateChannel(WebSocketChannelDAO dao) throws SQLException {
		synchronized (this) {
			PreparedStatement stmt;
			boolean addIdOnSuccess = false;
			
			// first, find out if already inserted
			if (channelIds.contains(dao.channelId)) {
				// proceed with update
				stmt = psUpdateChannel;
			} else {
				// proceed with insert
				stmt = psInsertChannel;
				addIdOnSuccess = true;
				logger.info("insert channel: " + dao.toString());
			}
	
			stmt.setString(1, dao.host);
			stmt.setInt(2, dao.port);
			stmt.setTimestamp(3, (dao.startTimestamp != null) ? new Timestamp(dao.startTimestamp) : null);
			stmt.setTimestamp(4, (dao.endTimestamp != null) ? new Timestamp(dao.endTimestamp) : null);
			stmt.setNull(5, Types.INTEGER);
			stmt.setInt(6, dao.channelId);
			
			try {
				stmt.execute();
				if (addIdOnSuccess) {
					channelIds.add(dao.channelId);
				}
			} catch (SQLException e) {
				throw e;
			}
			
			if (dao.historyId != null) {
				psUpdateHistoryFk.setInt(1, dao.historyId);
				psUpdateHistoryFk.setInt(2, dao.channelId);
				try {
					psUpdateHistoryFk.execute();
				} catch (SQLException e) {
					// safely ignore this exception
					// on shutdown, the history table is cleaned before
					// WebSocket channels are closed and updated
				}
			}
			
			// with newer version of HSQLDB I could have used the MERGE command
	//		psMergeChannel.setInt(1, dao.channelId);
	//		psMergeChannel.setString(2, dao.host);
	//		psMergeChannel.setInt(3, dao.port);
	//		psMergeChannel.setTimestamp(4, new Timestamp(dao.startTimestamp));
	//		psMergeChannel.setTimestamp(5, new Timestamp(dao.endTimestamp));
	//		psMergeChannel.setInt(6, dao.historyId);
	//		psMergeChannel.execute();	
		}
	}

	public void insertMessage(WebSocketMessageDAO dao) throws SQLException {

		// synchronize on whole object to avoid race conditions with insertOrUpdateChannel()
		synchronized (this) {
			if (!channelIds.contains(dao.channelId)) {
				throw new SQLException("channel not inserted: " + dao.channelId);
			}
			
			logger.info("insert message: " + dao.toString());
			try {
				psInsertMessage.setInt(1, dao.messageId);
				psInsertMessage.setInt(2, dao.channelId);
				psInsertMessage.setTimestamp(3, new Timestamp(dao.timestamp));
				psInsertMessage.setInt(4, dao.opcode);

				// write payload
				if (dao.payload instanceof String) {
					psInsertMessage.setClob(5, new jdbcClob((String) dao.payload));
					psInsertMessage.setNull(6, Types.BLOB);
				} else if (dao.payload instanceof byte[]) {
					psInsertMessage.setNull(5, Types.CLOB);
					psInsertMessage.setBlob(6, new jdbcBlob((byte[]) dao.payload));
				} else {
					throw new SQLException("Attribute 'payload' of class WebSocketMessageDAO has got wrong type!");
				}
				
				psInsertMessage.setInt(7, dao.payloadLength);
				psInsertMessage.setBoolean(8, dao.isOutgoing);
				psInsertMessage.execute();
				
				if (dao instanceof WebSocketFuzzMessageDAO) {
					WebSocketFuzzMessageDAO fuzzDao = (WebSocketFuzzMessageDAO) dao;
					psInsertFuzz.setInt(1, fuzzDao.fuzzId);
					psInsertFuzz.setInt(2, fuzzDao.messageId);
					psInsertFuzz.setInt(3, fuzzDao.channelId);
					psInsertFuzz.setString(4, fuzzDao.state.toString());
					psInsertFuzz.setString(5, fuzzDao.fuzz);
					psInsertFuzz.execute();
				}
			} catch (SQLException e) {
				throw e;
			}
		}
	}

	public List<WebSocketChannelDAO> getChannels(WebSocketChannelDAO criteria) throws SQLException {
		String query = "SELECT c.* "
        		+ "FROM websocket_channel AS c "
        		+ "<where> "
        		+ "ORDER BY c.start_timestamp, c.channel_id";

		PreparedStatement stmt;
		try {
			stmt = buildMessageCriteriaStatement(query, criteria);
		} catch (SQLException e) {
			if (getConnection().isClosed()) {
				return new ArrayList<WebSocketChannelDAO>();
			} else {
				throw e;
			}
		}
		
		stmt.execute();
		
		return buildChannelDAOs(stmt.getResultSet());
	}
	
	private PreparedStatement buildMessageCriteriaStatement(String query, WebSocketChannelDAO criteria) throws SQLException {
		List<String> where = new ArrayList<String>();
		List<Object> params = new ArrayList<Object>();
	
		if (criteria.channelId != null) {
			where.add("c.channel_id = ?");
			params.add(criteria.channelId);
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

		PreparedStatement stmt = getConnection().prepareStatement(query.toString());
		int i = 1;
		for (Object param : params) {
			stmt.setObject(i++, param);
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
}
