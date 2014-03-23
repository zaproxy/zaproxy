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

package org.parosproxy.paros.db;

import java.nio.charset.Charset;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.hsqldb.types.Types;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpStatusCode;

public class TableHistory extends AbstractTable {

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
    private static final Logger log = Logger.getLogger(TableHistory.class);

    private boolean bodiesAsBytes; 

    public TableHistory() {
    }
    
    @Override
    protected void reconnect(Connection conn) throws SQLException {
        bodiesAsBytes = true;

        updateTable(conn);
        
        isExistStatusCode = DbUtils.hasColumn(conn, TABLE_NAME, STATUSCODE);
        
        psRead = conn.prepareStatement("SELECT TOP 1 * FROM HISTORY WHERE " + HISTORYID + " = ?");
        // updatable recordset does not work in hsqldb jdbc impelementation!
        //psWrite = mConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        psDelete = conn.prepareStatement("DELETE FROM HISTORY WHERE " + HISTORYID + " = ?");
        psDeleteTemp = conn.prepareStatement("DELETE FROM HISTORY WHERE " + HISTTYPE + " = " + HistoryReference.TYPE_TEMPORARY);
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
    }
    
    // ZAP: Added the method.
    private void updateTable(Connection connection) throws SQLException {
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
    }
    
	public synchronized RecordHistory read(int historyId) throws HttpMalformedHeaderException, SQLException {
	    psRead.setInt(1, historyId);
		psRead.execute();
		RecordHistory result = null;
		try (ResultSet rs = psRead.getResultSet()) {
			result = build(rs);
		}

		return result;
	}
	
	public synchronized RecordHistory write(long sessionId, int histType, HttpMessage msg) throws HttpMalformedHeaderException, SQLException {
	    
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
	    
	}
	
	private synchronized RecordHistory write(long sessionId, int histType, long timeSentMillis, int timeElapsedMillis,
	        String method, String uri, int statusCode,
	        String reqHeader, byte[] reqBody, String resHeader, byte[] resBody, String tag, String note, boolean responseFromTargetHost) throws HttpMalformedHeaderException, SQLException {

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
            psInsert.setString(8, new String(reqBody, Charset.forName("US-ASCII")));
        }
        psInsert.setString(9, resHeader);
        if (bodiesAsBytes) {
            psInsert.setBytes(10, resBody);
        } else {
            psInsert.setString(10, new String(resBody, Charset.forName("US-ASCII")));
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
	
	public Vector<Integer> getHistoryList(long sessionId, int histType) throws SQLException {
	    PreparedStatement psReadSession = getConnection().prepareStatement("SELECT " + HISTORYID + " FROM HISTORY WHERE " + SESSIONID + " = ? AND " + HISTTYPE + " = ? ORDER BY " + HISTORYID);
	    ResultSet rs = null;
	    Vector<Integer> v = new Vector<>();
	    try {
        
		    psReadSession.setLong(1, sessionId);
		    psReadSession.setInt(2, histType);
		    rs = psReadSession.executeQuery();
	    
		    while (rs.next()) {
		        int last = rs.getInt(HISTORYID);
		        // ZAP: Changed to use the method Integer.valueOf.
		        v.add(Integer.valueOf(last));
		    }
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (Exception e) {
					// Ignore
				}
			}
		    psReadSession.close();
		}

		return v;
	}

	public Vector<Integer> getHistoryList(long sessionId) throws SQLException {
	    PreparedStatement psReadSession = getConnection().prepareStatement("SELECT " + HISTORYID + " FROM HISTORY WHERE " + SESSIONID + " = ? ORDER BY " + HISTORYID);
	    ResultSet rs = null;
	    Vector<Integer> v = new Vector<>();
	    try {
		    psReadSession.setLong(1, sessionId);
		    rs = psReadSession.executeQuery();
		    
		    while (rs.next()) {
		        int last = rs.getInt(HISTORYID);
		        // ZAP: Changed to use the method Integer.valueOf.
		        v.add(Integer.valueOf(last));
		    }
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (Exception e) {
					// Ignore
				}
			}
		    psReadSession.close();
		}

		return v;
	}

	public List<Integer> getHistoryList(long sessionId, int histType, String filter, boolean isRequest) throws SQLException {
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
	}
	
	public void deleteHistorySession(long sessionId) throws SQLException {
        try (Statement stmt = getConnection().createStatement()) {
        	stmt.executeUpdate("DELETE FROM HISTORY WHERE " + SESSIONID + " = " + sessionId);
		}
	}
	
	public void deleteHistoryType(long sessionId, int historyType) throws SQLException {
        try (Statement stmt = getConnection().createStatement()) {
        	stmt.executeUpdate("DELETE FROM HISTORY WHERE " + SESSIONID + " = " + sessionId + " AND " + HISTTYPE + " = " + historyType);
		}
	}

	public void delete(int historyId) throws SQLException {
		psDelete.setInt(1, historyId);
		psDelete.executeUpdate();
		
	}
	
    /**
     * Deletes from the database all the {@code HistoryReference}s whose id is
     * in the list {@code ids}.
     * 
     * @param ids
     *            a {@code List} containing all the ids of the
     *            {@code HistoryReference}s to be deleted
     * @throws SQLException
     *             if an error occurred while deleting the
     *             {@code HistoryReference}s
     */
	// ZAP: Added method.
    public void delete(List<Integer> ids) throws SQLException {
        for (Iterator<Integer> it = ids.iterator(); it.hasNext();) {
            psDelete.setInt(1, it.next().intValue());
            psDelete.executeUpdate();
        }
    }

	public void deleteTemporary() throws SQLException {
	    psDeleteTemp.execute();
	}
	
	public boolean containsURI(long sessionId, int historyType, String method, String uri, byte[] body) throws SQLException {
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
	    
	}
    
    public RecordHistory getHistoryCache(HistoryReference ref, HttpMessage reqMsg) throws SQLException , HttpMalformedHeaderException {

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
    }
    
    public void updateNote(int historyId, String note) throws SQLException {
        psUpdateNote.setString(1, note);
        psUpdateNote.setInt(2, historyId);
        psUpdateNote.execute();
    }

    public int lastIndex () {
        return lastInsertedIndex;
    }

}
