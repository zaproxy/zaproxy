/*
*
* Paros and its related class files.
* 
* Paros is an HTTP/HTTPS proxy for assessing web application security.
* Copyright (C) 2003-2006 Chinotec Technologies Company
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
// ZAP: 2011/05/27 Ensure all PreparedStatements and ResultSets closed to prevent leaks 
// ZAP: 2012/03/15 Changed to use byte[] in the request and response bodies
// instead of String.
// ZAP: 2012/04/23 Added @Override annotation to the appropriate method.
// ZAP: 2012/04/25 Changed to use the method Integer.valueOf.
// ZAP: 2012/06/11 Added method delete(List<Integer>).
// ZAP: 2012/08/08 Upgrade to HSQLDB 2.x (Added updateTable() and refactored names)
// ZAP: 2013/09/26 Issue 716: ZAP flags its own HTTP responses
// ZAP: 2014/03/23 Changed to use try-with-resource statements.
// ZAP: 2014/03/23 Issue 999: History loaded in wrong order
// ZAP: 2014/03/23 Issue 1075: Change TableHistory to delete records in batches
// ZAP: 2014/03/23 Issue 1091: CoreAPI - Do not get the IDs of temporary history records
// ZAP: 2014/03/27 Issue 1072: Allow the request and response body sizes to be user-specifiable as far as possible
// ZAP: 2014/08/14 Issue 1310: Allow to set history types as temporary
// ZAP: 2014/12/11 Replaced calls to Charset.forName(String) with StandardCharsets
// ZAP: 2015/02/09 Issue 1525: Introduce a database interface layer to allow for alternative implementations
// ZAP: 2016/05/26 Delete temporary history types sequentially
// ZAP: 2016/05/27 Change to use HistoryReference to obtain the temporary types
// ZAP: 2016/08/30 Issue 2836: Change to delete temporary history types in batches to prevent out-of-memory-exception(s)

package org.parosproxy.paros.db.paros;

import java.nio.charset.StandardCharsets;
import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.hsqldb.types.Types;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.DbUtils;
import org.parosproxy.paros.db.RecordHistory;
import org.parosproxy.paros.db.TableHistory;
import org.parosproxy.paros.extension.option.DatabaseParam;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpStatusCode;

public class ParosTableHistory extends ParosAbstractTable implements TableHistory {

    private static final String TABLE_NAME = "HISTORY";
    
    private static final String HISTORYID	= "HISTORYID";
	private static final String SESSIONID	= "SESSIONID";
	private static final String HISTTYPE	= "HISTTYPE";
	private static final String METHOD 		= "METHOD";
	private static final String URI			= "URI";
    private static final String STATUSCODE  = "STATUSCODE";
	private static final String TIMESENTMILLIS = "TIMESENTMILLIS";
	private static final String TIMEELAPSEDMILLIS = "TIMEELAPSEDMILLIS";
	private static final String REQHEADER	= "REQHEADER";
	private static final String REQBODY		= "REQBODY";
	private static final String RESHEADER	= "RESHEADER";
	private static final String RESBODY		= "RESBODY";
    private static final String TAG         = "TAG";
    // ZAP: Added NOTE field to history table
    private static final String NOTE        = "NOTE";
    private static final String RESPONSE_FROM_TARGET_HOST = "RESPONSEFROMTARGETHOST";

    private PreparedStatement psRead = null;
    private PreparedStatement psInsert = null;
    private CallableStatement psGetIdLastInsert = null;
    private PreparedStatement psDelete = null;
    private PreparedStatement psDeleteTemp = null;
    private PreparedStatement psContainsURI = null;
    //private PreparedStatement psAlterTable = null;
//    private PreparedStatement psUpdateTag = null;
    private PreparedStatement psUpdateNote = null;
    
    private int lastInsertedIndex;
    
    private static boolean isExistStatusCode = false;

    // ZAP: Added logger
    private static final Logger log = Logger.getLogger(ParosTableHistory.class);

    private boolean bodiesAsBytes; 

    public ParosTableHistory() {
    }
    
	//ZAP: Allow the request and response body sizes to be user-specifiable as far as possible
    int configuredrequestbodysize = -1;    
    int configuredresponsebodysize = -1;

    @Override
    protected void reconnect(Connection conn) throws DatabaseException {
    	try {
			//ZAP: Allow the request and response body sizes to be user-specifiable as far as possible
			//re-load the configuration data from file, to get the configured length of the request and response bodies
			//this will later be compared to the actual lengths of these fields in the database (in updateTable(Connection c))
			DatabaseParam dbparams = new DatabaseParam ();
			dbparams.load(Constant.getInstance().FILE_CONFIG);
			this.configuredrequestbodysize = dbparams.getRequestBodySize();
			this.configuredresponsebodysize = dbparams.getResponseBodySize();
			    	
			bodiesAsBytes = true;

			updateTable(conn);
			
			isExistStatusCode = DbUtils.hasColumn(conn, TABLE_NAME, STATUSCODE);
			
			psRead = conn.prepareStatement("SELECT TOP 1 * FROM HISTORY WHERE " + HISTORYID + " = ?");
			// updatable recordset does not work in hsqldb jdbc impelementation!
			//psWrite = mConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			psDelete = conn.prepareStatement("DELETE FROM HISTORY WHERE " + HISTORYID + " = ?");
			psDeleteTemp = conn.prepareStatement("DELETE FROM HISTORY WHERE " + HISTTYPE + " IN (?) LIMIT 1000");
			psContainsURI = conn.prepareStatement("SELECT TOP 1 HISTORYID FROM HISTORY WHERE URI = ? AND  METHOD = ? AND REQBODY = ? AND SESSIONID = ? AND HISTTYPE = ?");

			
			// ZAP: Added support for the tag when creating a history record
			if (isExistStatusCode) {
			    psInsert = conn.prepareStatement("INSERT INTO HISTORY ("
			            + SESSIONID + "," + HISTTYPE + "," + TIMESENTMILLIS + "," + 
			            TIMEELAPSEDMILLIS + "," + METHOD + "," + URI + "," + REQHEADER + "," + 
			            REQBODY + "," + RESHEADER + "," + RESBODY + "," + TAG + ", " + STATUSCODE + "," + NOTE + ", " +
			            RESPONSE_FROM_TARGET_HOST
			            + ") VALUES (?, ? ,?, ?, ?, ?, ?, ? ,? , ?, ?, ?, ?, ?)");
			} else {
			    psInsert = conn.prepareStatement("INSERT INTO HISTORY ("
			            + SESSIONID + "," + HISTTYPE + "," + TIMESENTMILLIS + "," + 
			            TIMEELAPSEDMILLIS + "," + METHOD + "," + URI + "," + REQHEADER + "," + 
			            REQBODY + "," + RESHEADER + "," + RESBODY + "," + TAG + "," + NOTE + ", " +
			            RESPONSE_FROM_TARGET_HOST
			            + ") VALUES (?, ? ,?, ?, ?, ?, ?, ? ,? , ? , ?, ?, ?)");
			    
			}
			psGetIdLastInsert = conn.prepareCall("CALL IDENTITY();");
			
//        psUpdateTag = conn.prepareStatement("UPDATE HISTORY SET TAG = ? WHERE HISTORYID = ?");

			psUpdateNote = conn.prepareStatement("UPDATE HISTORY SET NOTE = ? WHERE HISTORYID = ?");
			
			int currentIndex = 0;
			PreparedStatement stmt = null;
			try {
			    stmt = conn.prepareStatement("SELECT TOP 1 HISTORYID FROM HISTORY ORDER BY HISTORYID DESC");
			    try (ResultSet rs = stmt.executeQuery()) {
			        if (rs.next()) {
			            currentIndex = rs.getInt(1);
			        }
			    }
			} finally {
			    if (stmt != null) {
			        try {
			            stmt.close();
			        } catch(SQLException e) {
			            if (log.isDebugEnabled()) {
			                log.debug(e.getMessage(), e);
			            }
			        }
			    }
			}
			lastInsertedIndex = currentIndex;
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
    }
    
    // ZAP: Added the method.
    private void updateTable(Connection connection) throws DatabaseException {
        try {
			if (!DbUtils.hasColumn(connection, TABLE_NAME, TAG)) {
			    DbUtils.executeAndClose(connection.prepareStatement("ALTER TABLE "+TABLE_NAME+" ADD COLUMN "+TAG+" VARCHAR(32768) DEFAULT ''"));
			}

			// Add the NOTE column to the db if necessary
			if (!DbUtils.hasColumn(connection, TABLE_NAME, NOTE)) {
			    DbUtils.executeAndClose(connection.prepareStatement("ALTER TABLE "+TABLE_NAME+" ADD COLUMN "+NOTE+" VARCHAR(1048576) DEFAULT ''"));
			}
			
			if (DbUtils.getColumnType(connection, TABLE_NAME, REQBODY) != Types.SQL_VARBINARY) {
			    bodiesAsBytes = false;
			} else {
				// Databases created with ZAP<1.4.0.1 used VARCHAR for the REQBODY/RESBODY
				// HSQLDB 1.8.x converted from VARCHAR to bytes without problems
				// (through the method ResultSet.getBytes)
				// but the new version doesn't, it throws the following exception:
				// incompatible data type in conversion: from SQL type VARCHAR
			}

			if (!DbUtils.hasColumn(connection, TABLE_NAME, RESPONSE_FROM_TARGET_HOST)) {
			    DbUtils.executeAndClose(connection.prepareStatement("ALTER TABLE " + TABLE_NAME + " ADD COLUMN "
			            + RESPONSE_FROM_TARGET_HOST + " BOOLEAN DEFAULT FALSE"));
			    DbUtils.executeUpdateAndClose(connection.prepareStatement("UPDATE " + TABLE_NAME + " SET " + RESPONSE_FROM_TARGET_HOST
			            + " = TRUE "));
			}
			
			int requestbodysizeindb = DbUtils.getColumnSize(connection, TABLE_NAME, REQBODY);
			int responsebodysizeindb = DbUtils.getColumnSize(connection, TABLE_NAME, RESBODY);
			try {	        
			    if (requestbodysizeindb != this.configuredrequestbodysize && this.configuredrequestbodysize > 0) {
			    	if (log.isDebugEnabled()) log.debug("Extending table "+ TABLE_NAME + " request body length from "+ requestbodysizeindb + " to " + this.configuredrequestbodysize);
			    	DbUtils.executeAndClose(connection.prepareStatement("ALTER TABLE " + TABLE_NAME + " ALTER COLUMN "
			            + REQBODY + " VARBINARY("+this.configuredrequestbodysize+")"));
			    	if (log.isDebugEnabled()) log.debug("Completed extending table "+ TABLE_NAME + " request body length from "+ requestbodysizeindb + " to " + this.configuredrequestbodysize);
			    }
			    
			    if (responsebodysizeindb != this.configuredresponsebodysize && this.configuredresponsebodysize > 0) {
			    	if (log.isDebugEnabled()) log.debug("Extending table "+ TABLE_NAME + " response body length from "+ responsebodysizeindb + " to " + this.configuredresponsebodysize);
			    	DbUtils.executeAndClose(connection.prepareStatement("ALTER TABLE " + TABLE_NAME + " ALTER COLUMN "
			            + RESBODY + " VARBINARY("+this.configuredresponsebodysize+")"));
			    	if (log.isDebugEnabled()) log.debug("Completed extending table "+ TABLE_NAME + " response body length from "+ responsebodysizeindb + " to " + this.configuredresponsebodysize);
			    }
			}
			catch (SQLException e) {
				log.error("An error occurred while modifying a column length on "+ TABLE_NAME);
				log.error("The 'Maximum Request Body Size' value in the Database Options needs to be set to at least " + requestbodysizeindb + " to avoid this error" );
				log.error("The 'Maximum Response Body Size' value in the Database Options needs to be set to at least " + responsebodysizeindb + " to avoid this error" );
				log.error("The SQL Exception was:", e);
				throw e;
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
    }
    
	@Override
	public synchronized RecordHistory read(int historyId) throws HttpMalformedHeaderException, DatabaseException {
	    try {
			psRead.setInt(1, historyId);
			psRead.execute();
			RecordHistory result = null;
			try (ResultSet rs = psRead.getResultSet()) {
				result = build(rs);
			}

			return result;
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}
	
	@Override
	public synchronized RecordHistory write(long sessionId, int histType, HttpMessage msg) throws HttpMalformedHeaderException, DatabaseException {
	    
	    try {
			String reqHeader = "";
			byte[] reqBody = new byte[0];
			String resHeader = "";
			byte[] resBody = reqBody;
			String method = "";
			String uri = "";
			int statusCode = 0;
			String note = msg.getNote();
			
			if (!msg.getRequestHeader().isEmpty()) {
			    reqHeader = msg.getRequestHeader().toString();
			    reqBody = msg.getRequestBody().getBytes();
			    method = msg.getRequestHeader().getMethod();
			    uri = msg.getRequestHeader().getURI().toString();
			}

			if (!msg.getResponseHeader().isEmpty()) {
			    resHeader = msg.getResponseHeader().toString();
			    resBody = msg.getResponseBody().getBytes();
			    statusCode = msg.getResponseHeader().getStatusCode();
			}
			
			//return write(sessionId, histType, msg.getTimeSentMillis(), msg.getTimeElapsedMillis(), method, uri, statusCode, reqHeader, reqBody, resHeader, resBody, msg.getTag());
			return write(sessionId, histType, msg.getTimeSentMillis(), msg.getTimeElapsedMillis(), method, uri, statusCode, reqHeader, reqBody, resHeader, resBody, null, note, msg.isResponseFromTargetHost());
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	    
	}
	
	private synchronized RecordHistory write(long sessionId, int histType, long timeSentMillis, int timeElapsedMillis,
	        String method, String uri, int statusCode,
	        String reqHeader, byte[] reqBody, String resHeader, byte[] resBody, String tag, String note, boolean responseFromTargetHost) 
	        		throws HttpMalformedHeaderException, SQLException, DatabaseException {

		//ZAP: Allow the request and response body sizes to be user-specifiable as far as possible
		if (reqBody.length > this.configuredrequestbodysize) {
			throw new SQLException("The actual Request Body length "+ reqBody.length + " is greater than the configured request body length "+ this.configuredrequestbodysize);
		}
		if (resBody.length > this.configuredresponsebodysize) {
			throw new SQLException("The actual Response Body length "+ resBody.length + " is greater than the configured response body length "+ this.configuredresponsebodysize);
		}

	    psInsert.setLong(1, sessionId);
	    psInsert.setInt(2, histType);
	    psInsert.setLong(3, timeSentMillis);
	    psInsert.setInt(4, timeElapsedMillis);
	    psInsert.setString(5, method);
	    psInsert.setString(6, uri);        
	    psInsert.setString(7, reqHeader);
        if (bodiesAsBytes) {
            psInsert.setBytes(8, reqBody);
        } else {
            psInsert.setString(8, new String(reqBody, StandardCharsets.US_ASCII));
        }
        psInsert.setString(9, resHeader);
        if (bodiesAsBytes) {
            psInsert.setBytes(10, resBody);
        } else {
            psInsert.setString(10, new String(resBody, StandardCharsets.US_ASCII));
        }
	    psInsert.setString(11, tag);

	    // ZAP: Added the statement.
	    int currentIdx = 12;
	    
        if (isExistStatusCode) {
            psInsert.setInt(currentIdx, statusCode);
            // ZAP: Added the statement.
            ++currentIdx;
        }
        
        // ZAP: Added the statement.
        psInsert.setString(currentIdx, note);
        ++currentIdx;
        
        psInsert.setBoolean(currentIdx, responseFromTargetHost);
        
        psInsert.executeUpdate();
				
		/*
        String sql = "INSERT INTO HISTORY ("
        		+ REQHEADER + "," + REQBODY + "," + RESHEADER + "," + RESBODY +
				") VALUES ('"+ reqHeader + "','" + reqBody + "','" + resHeader + "','" + resBody + "'); CALL IDENTITY();";
		Statement stmt = mConn.createStatement();
		stmt.executeQuery(sql);
		ResultSet rs = stmt.getResultSet();
		*/
		
		try (ResultSet rs = psGetIdLastInsert.executeQuery()) {
			rs.next();
			int id = rs.getInt(1);
            lastInsertedIndex = id;
			return read(id);
		}
	}
	
	private RecordHistory build(ResultSet rs) throws HttpMalformedHeaderException, SQLException {
		RecordHistory history = null;
		try {
			if (rs.next()) {
                byte[] reqBody;
                byte[] resBody;
                
                if (bodiesAsBytes) {
                    reqBody = rs.getBytes(REQBODY);
                    resBody = rs.getBytes(RESBODY);
                } else {
                    reqBody = rs.getString(REQBODY).getBytes();
                    resBody = rs.getString(RESBODY).getBytes();
                }
                
				history = new RecordHistory(
						rs.getInt(HISTORYID),
						rs.getInt(HISTTYPE),
	                    rs.getLong(SESSIONID),
						rs.getLong(TIMESENTMILLIS),
						rs.getInt(TIMEELAPSEDMILLIS),
						rs.getString(REQHEADER),
						reqBody,
						rs.getString(RESHEADER),
						resBody,
	                    rs.getString(TAG),
	                    rs.getString(NOTE),			// ZAP: Added note
                        rs.getBoolean(RESPONSE_FROM_TARGET_HOST)
				);
			}
		} finally {
			rs.close();
		}
		return history;
	
	}
	
    /**
     * Gets all the history record IDs of the given session.
     *
     * @param sessionId the ID of session of the history records to be returned
     * @return a {@code List} with all the history IDs of the given session, never {@code null}
     * @throws DatabaseException if an error occurred while getting the history IDs
     * @since 2.3.0
     * @see #getHistoryIdsOfHistType(long, int...)
     */
    @Override
    public List<Integer> getHistoryIds(long sessionId) throws DatabaseException {
        return getHistoryIdsOfHistType(sessionId, null);
    }

    /**
     * Gets all the history record IDs of the given session and with the given history types.
     *
     * @param sessionId the ID of session of the history records
     * @param histTypes the history types of the history records that should be returned
     * @return a {@code List} with all the history IDs of the given session and history types, never {@code null}
     * @throws DatabaseException if an error occurred while getting the history IDs
     * @since 2.3.0
     * @see #getHistoryIds(long)
     * @see #getHistoryIdsExceptOfHistType(long, int...)
     */
    @Override
    public List<Integer> getHistoryIdsOfHistType(long sessionId, int... histTypes) throws DatabaseException {
        try {
			boolean hasHistTypes = histTypes != null && histTypes.length > 0;
			int strLength = hasHistTypes ? 97 : 68;
			StringBuilder strBuilder = new StringBuilder(strLength);
			strBuilder.append("SELECT ").append(HISTORYID);
			strBuilder.append(" FROM ").append(TABLE_NAME).append(" WHERE ").append(SESSIONID).append(" = ?");
			if (hasHistTypes) {
			    strBuilder.append(" AND ").append(HISTTYPE).append(" IN ( UNNEST(?) )");
			}
			strBuilder.append(" ORDER BY ").append(HISTORYID);

			try (PreparedStatement psReadSession = getConnection().prepareStatement(strBuilder.toString())) {

			    psReadSession.setLong(1, sessionId);
			    if (hasHistTypes) {
			        Array arrayHistTypes = getConnection().createArrayOf("INTEGER", ArrayUtils.toObject(histTypes));
			        psReadSession.setArray(2, arrayHistTypes);
			    }
			    try (ResultSet rs = psReadSession.executeQuery()) {
			        ArrayList<Integer> ids = new ArrayList<>();
			        while (rs.next()) {
			            ids.add(Integer.valueOf(rs.getInt(HISTORYID)));
			        }
			        ids.trimToSize();

			        return ids;
			    }
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
    }

    /**
     * Returns all the history record IDs of the given session except the ones with the given history types.
     ** 
     * @param sessionId the ID of session of the history records
     * @param histTypes the history types of the history records that should be excluded
     * @return a {@code List} with all the history IDs of the given session and history types, never {@code null}
     * @throws DatabaseException if an error occurred while getting the history IDs
     * @since 2.3.0
     * @see #getHistoryIdsOfHistType(long, int...)
     */
    @Override
    public List<Integer> getHistoryIdsExceptOfHistType(long sessionId, int... histTypes) throws DatabaseException {
        try {
			boolean hasHistTypes = histTypes != null && histTypes.length > 0;
			int strLength = hasHistTypes ? 102 : 68;
			StringBuilder sb = new StringBuilder(strLength);
			sb.append("SELECT ").append(HISTORYID);
			sb.append(" FROM ").append(TABLE_NAME).append(" WHERE ").append(SESSIONID).append(" = ?");
			if (hasHistTypes) {
			    sb.append(" AND ").append(HISTTYPE).append(" NOT IN ( UNNEST(?) )");
			}
			sb.append(" ORDER BY ").append(HISTORYID);

			try (PreparedStatement psReadSession = getConnection().prepareStatement(sb.toString())) {

			    psReadSession.setLong(1, sessionId);
			    if (hasHistTypes) {
			        Array arrayHistTypes = getConnection().createArrayOf("INTEGER", ArrayUtils.toObject(histTypes));
			        psReadSession.setArray(2, arrayHistTypes);
			    }
			    try (ResultSet rs = psReadSession.executeQuery()) {
			        ArrayList<Integer> ids = new ArrayList<>();
			        while (rs.next()) {
			            ids.add(Integer.valueOf(rs.getInt(HISTORYID)));
			        }
			        ids.trimToSize();

			        return ids;
			    }
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
    }

	/**
	 * @deprecated (2.3.0) Use {@link #getHistoryIdsOfHistType(long, int...)} instead. If the thread-safety provided by the
	 *             class {@code Vector} is really required "wrap" the returned List with
	 *             {@link Collections#synchronizedList(List)} instead.
	 */
	@Deprecated
	@SuppressWarnings("javadoc")
	public Vector<Integer> getHistoryList(long sessionId, int histType) throws DatabaseException {
		return new Vector<>(getHistoryIdsOfHistType(sessionId, histType));
	}

	/**
	 * @deprecated (2.3.0) Use {@link #getHistoryIds(long)} instead. If the thread-safety provided by the class {@code Vector}
	 *             is really required "wrap" the returned List with {@link Collections#synchronizedList(List)} instead.
	 */
	@Deprecated
	@SuppressWarnings("javadoc")
	public Vector<Integer> getHistoryList(long sessionId) throws DatabaseException {
		return new Vector<>(getHistoryIds(sessionId));
	}

	@Override
	public List<Integer> getHistoryList(long sessionId, int histType, String filter, boolean isRequest) throws DatabaseException {
        try {
			PreparedStatement psReadSearch = getConnection().prepareStatement("SELECT * FROM HISTORY WHERE " + SESSIONID + " = ? AND " + HISTTYPE + " = ? ORDER BY " + HISTORYID);
			ResultSet rs = null;
			Vector<Integer> v = new Vector<>();
			try {

			    Pattern pattern = Pattern.compile(filter, Pattern.MULTILINE| Pattern.CASE_INSENSITIVE);
				Matcher matcher = null;

				psReadSearch.setLong(1, sessionId);
				psReadSearch.setInt(2, histType);
				rs = psReadSearch.executeQuery();
				while (rs.next()) {
				    if (isRequest) {
				        matcher = pattern.matcher(rs.getString(REQHEADER));
				        if (matcher.find()) {
				            // ZAP: Changed to use the method Integer.valueOf.
				            v.add(Integer.valueOf(rs.getInt(HISTORYID)));
				            continue;
				        }
				        matcher = pattern.matcher(rs.getString(REQBODY));
				        if (matcher.find()) {
				            // ZAP: Changed to use the method Integer.valueOf.
				            v.add(Integer.valueOf(rs.getInt(HISTORYID)));
				            continue;
				        }
				    } else {
				        matcher = pattern.matcher(rs.getString(RESHEADER));
				        if (matcher.find()) {
				            // ZAP: Changed to use the method Integer.valueOf.
				            v.add(Integer.valueOf(rs.getInt(HISTORYID)));
				            continue;
				        }
				        matcher = pattern.matcher(rs.getString(RESBODY));
				        if (matcher.find()) {
				            // ZAP: Changed to use the method Integer.valueOf.
				            v.add(Integer.valueOf(rs.getInt(HISTORYID)));
				            continue;
				        }
				    }
				    
				}
			} finally {
				if (rs != null) {
					try {
						rs.close();
					} catch (Exception e) {
						// Ignore
					}
				}
			    psReadSearch.close();
			}

			return v;
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}
	
	@Override
	public void deleteHistorySession(long sessionId) throws DatabaseException {
        try {
			try (Statement stmt = getConnection().createStatement()) {
				stmt.executeUpdate("DELETE FROM HISTORY WHERE " + SESSIONID + " = " + sessionId);
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}
	
	@Override
	public void deleteHistoryType(long sessionId, int historyType) throws DatabaseException {
        try {
			try (Statement stmt = getConnection().createStatement()) {
				stmt.executeUpdate("DELETE FROM HISTORY WHERE " + SESSIONID + " = " + sessionId + " AND " + HISTTYPE + " = " + historyType);
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}

	@Override
	public synchronized void delete(int historyId) throws DatabaseException {
		try {
			psDelete.setInt(1, historyId);
			psDelete.executeUpdate();
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}
	
    /**
     * Deletes from the database all the history records whose ID is
     * in the list {@code ids}, in batches of 1000 records.
     * 
     * @param ids
     *            a {@code List} containing all the IDs of the
     *            history records to be deleted
     * @throws IllegalArgumentException if {@code ids} is null
     * @throws DatabaseException
     *             if an error occurred while deleting the
     *             history records
     * @since 2.0.0
     * @see #delete(List, int)
     */
	// ZAP: Added method.
    @Override
    public void delete(List<Integer> ids) throws DatabaseException {
        delete(ids, 1000);
    }

    /**
     * Deletes from the database all the history records whose ID is in the list {@code ids}, in batches of given
     * {@code batchSize}.
     * 
     * @param ids a {@code List} containing all the IDs of the history records to be deleted
     * @param batchSize the maximum size of records to delete in a single batch
     * @throws IllegalArgumentException if {@code ids} is null
     * @throws IllegalArgumentException if {@code batchSize} is not greater than zero
     * @throws DatabaseException if an error occurred while deleting the history records
     * @since 2.3.0
     */
    @Override
    public synchronized void delete(List<Integer> ids, int batchSize) throws DatabaseException {
        try {
			if (ids == null) {
			    throw new IllegalArgumentException("Parameter ids must not be null.");
			}
			if (batchSize <= 0) {
			    throw new IllegalArgumentException("Parameter batchSize must be greater than zero.");
			}

			int count = 0;
			for (Integer id : ids) {
			    psDelete.setInt(1, id.intValue());
			    psDelete.addBatch();
			    count++;

			    if (count % batchSize == 0) {
			        psDelete.executeBatch();
			        count = 0;
			    }
			}
			if (count % batchSize != 0) {
			    psDelete.executeBatch();
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
    }

    /**
     * @deprecated (2.5.0) Use {@link HistoryReference#addTemporaryType(int)} instead.
     * @since 2.4
     * @param historyType the history type that will be set as temporary
     * @see #deleteTemporary()
     */
    @Deprecated
    public static void setHistoryTypeAsTemporary(int historyType) {
        HistoryReference.addTemporaryType(historyType);
    }

    /**
     * @deprecated (2.5.0) Use {@link HistoryReference#removeTemporaryType(int)} instead.
     * @since 2.4
     * @param historyType the history type that will be marked as temporary
     * @see #deleteTemporary()
     */
    @Deprecated
    public static void unsetHistoryTypeAsTemporary(int historyType) {
        HistoryReference.removeTemporaryType(historyType);
    }

    /**
     * Deletes all records whose history type was marked as temporary (by calling {@code setHistoryTypeTemporary(int)}).
     * <p>
     * By default the only temporary history types are {@code HistoryReference#TYPE_TEMPORARY} and
     * {@code HistoryReference#TYPE_SCANNER_TEMPORARY}.
     * </p>
     *
     * @throws DatabaseException if an error occurred while deleting the temporary history records
     * @see HistoryReference#getTemporaryTypes()
     */
    @Override
    public void deleteTemporary() throws DatabaseException {
        try {
            for (Integer type : HistoryReference.getTemporaryTypes()) {
                while (true) {
                    psDeleteTemp.setInt(1, type);
                    int result = psDeleteTemp.executeUpdate();
                    if (result == 0) {
                        break;
                    }
                }
            }
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
    }
	
	@Override
	public synchronized boolean containsURI(long sessionId, int historyType, String method, String uri, byte[] body) throws DatabaseException {
	    try {
			psContainsURI.setString(1, uri);
			psContainsURI.setString(2, method);
			
			if (bodiesAsBytes) {
			    psContainsURI.setBytes(3, body);
			} else {
			    psContainsURI.setString(3, new String(body));
			}
			
			psContainsURI.setLong(4, sessionId);
			psContainsURI.setInt(5, historyType);
			try (ResultSet rs = psContainsURI.executeQuery()) {
			    if (rs.next()) {
			        return true;
			    }
			}
			return false;
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}
    
    @Override
    public RecordHistory getHistoryCache(HistoryReference ref, HttpMessage reqMsg) throws DatabaseException , HttpMalformedHeaderException {
        try {
			//  get the cache from provided reference.
			//  naturally, the obtained cache should be AFTER AND NEARBY to the given reference.
			//  - historyId up to historyId+200
			//  - match sessionId
			//  - history type can be MANUEL or hidden (hidden is used by images not explicitly stored in history)
			//  - match URI
			PreparedStatement psReadCache = null;
			
			if (isExistStatusCode) {
//          psReadCache = getConnection().prepareStatement("SELECT TOP 1 * FROM HISTORY WHERE URI = ? AND METHOD = ? AND REQBODY = ? AND " + HISTORYID + " >= ? AND " + HISTORYID + " <= ? AND SESSIONID = ? AND (HISTTYPE = " + HistoryReference.TYPE_MANUAL + " OR HISTTYPE = " + HistoryReference.TYPE_HIDDEN + ") AND STATUSCODE != 304");
			    psReadCache = getConnection().prepareStatement("SELECT TOP 1 * FROM HISTORY WHERE URI = ? AND METHOD = ? AND REQBODY = ? AND " + HISTORYID + " >= ? AND " + HISTORYID + " <= ? AND SESSIONID = ? AND STATUSCODE != 304");

			} else {
//          psReadCache = getConnection().prepareStatement("SELECT * FROM HISTORY WHERE URI = ? AND METHOD = ? AND REQBODY = ? AND " + HISTORYID + " >= ? AND " + HISTORYID + " <= ? AND SESSIONID = ? AND (HISTTYPE = " + HistoryReference.TYPE_MANUAL + " OR HISTTYPE = " + HistoryReference.TYPE_HIDDEN + ")");
			    psReadCache = getConnection().prepareStatement("SELECT * FROM HISTORY WHERE URI = ? AND METHOD = ? AND REQBODY = ? AND " + HISTORYID + " >= ? AND " + HISTORYID + " <= ? AND SESSIONID = ?)");            
			    
			}
			psReadCache.setString(1, reqMsg.getRequestHeader().getURI().toString());
			psReadCache.setString(2, reqMsg.getRequestHeader().getMethod());
			
			if (bodiesAsBytes) {
			    psReadCache.setBytes(3, reqMsg.getRequestBody().getBytes());
			} else {
			    psReadCache.setString(3, new String(reqMsg.getRequestBody().getBytes()));
			}

			psReadCache.setInt(4, ref.getHistoryId());        
			psReadCache.setInt(5, ref.getHistoryId()+200);
			psReadCache.setLong(6, ref.getSessionId());
			
			ResultSet rs = psReadCache.executeQuery();
			RecordHistory rec = null;
      
			try {
			    do {
			        rec = build(rs);
			        // for retrieval from cache, the message requests nature must be the same.
			        // and the result should NOT be NOT_MODIFIED for rendering by browser
			        if (rec != null && rec.getHttpMessage().equals(reqMsg) && 
			        		rec.getHttpMessage().getResponseHeader().getStatusCode() != HttpStatusCode.NOT_MODIFIED) {
			            return rec;
			        }

			    } while (rec != null);
			    
			} finally {
			    try {
			        rs.close();
			        psReadCache.close();
			    } catch (Exception e) {
			    	// ZAP: Log exceptions
			    	log.warn(e.getMessage(), e);
			    }
			}

			// if cache not exist, probably due to NOT_MODIFIED,
			// lookup from cache BEFORE the given reference

			if (isExistStatusCode) {
//            psReadCache = getConnection().prepareStatement("SELECT TOP 1 * FROM HISTORY WHERE URI = ? AND METHOD = ? AND REQBODY = ? AND SESSIONID = ? AND STATUSCODE != 304 AND (HISTTYPE = " + HistoryReference.TYPE_MANUAL + " OR HISTTYPE = " + HistoryReference.TYPE_HIDDEN  + ")");
			    psReadCache = getConnection().prepareStatement("SELECT TOP 1 * FROM HISTORY WHERE URI = ? AND METHOD = ? AND REQBODY = ? AND SESSIONID = ? AND STATUSCODE != 304");

			} else {
//            psReadCache = getConnection().prepareStatement("SELECT * FROM HISTORY WHERE URI = ? AND METHOD = ? AND REQBODY = ? AND SESSIONID = ? AND (HISTTYPE = " + HistoryReference.TYPE_MANUAL + " OR HISTTYPE = " + HistoryReference.TYPE_HIDDEN  + ")");
			    psReadCache = getConnection().prepareStatement("SELECT * FROM HISTORY WHERE URI = ? AND METHOD = ? AND REQBODY = ? AND SESSIONID = ?");

			}
			psReadCache.setString(1, reqMsg.getRequestHeader().getURI().toString());
			psReadCache.setString(2, reqMsg.getRequestHeader().getMethod());
			
			if (bodiesAsBytes) {
			    psReadCache.setBytes(3, reqMsg.getRequestBody().getBytes());
			} else {
			    psReadCache.setString(3, new String(reqMsg.getRequestBody().getBytes()));
			}
			
			psReadCache.setLong(4, ref.getSessionId());
			
			rs = psReadCache.executeQuery();
			rec = null;
      
			try {
			    do {
			        rec = build(rs);
			        if (rec != null && rec.getHttpMessage().equals(reqMsg) && rec.getHttpMessage().getResponseHeader().getStatusCode() != HttpStatusCode.NOT_MODIFIED) {
			            return rec;
			        }

			    } while (rec != null);
			    
			} finally {
			    try {
			        rs.close();
			        psReadCache.close();
			    } catch (Exception e) {
			    	// ZAP: Log exceptions
			    	log.warn(e.getMessage(), e);
			    }
			    
			}
			
			return null;
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
    }
    
    @Override
    public synchronized void updateNote(int historyId, String note) throws DatabaseException {
        try {
			psUpdateNote.setString(1, note);
			psUpdateNote.setInt(2, historyId);
			psUpdateNote.execute();
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
    }

    @Override
    public int lastIndex () {
        return lastInsertedIndex;
    }

}
