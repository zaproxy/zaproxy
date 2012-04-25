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
// ZAP: 2012/03/15 Changed to use byte[] in the request and response bodies instead of String.
// ZAP: 2012/04/23 Added @Override annotation to the appropriate method.
// ZAP: 2012/04/25 Changed to use the method Integer.valueOf.

package org.parosproxy.paros.db;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpStatusCode;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TableHistory extends AbstractTable {

	private static final String	HISTORYID	= "HISTORYID";
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

    private PreparedStatement psRead = null;
    private PreparedStatement psWrite1 = null;
    private CallableStatement psWrite2 = null;
    private PreparedStatement psDelete = null;
    private PreparedStatement psDeleteTemp = null;
    private PreparedStatement psContainsURI = null;
    //private PreparedStatement psAlterTable = null;
    private PreparedStatement psUpdateTag = null;
    private PreparedStatement psUpdateNote = null;
    private PreparedStatement psLastIndex = null;
    
    private static boolean isExistStatusCode = false;

    // ZAP: Added logger
    private static Logger log = Logger.getLogger(TableHistory.class);

    public TableHistory() {
    }
    
    @Override
    protected void reconnect(Connection conn) throws SQLException {
        psRead = conn.prepareStatement("SELECT TOP 1 * FROM HISTORY WHERE " + HISTORYID + " = ?");
        // updatable recordset does not work in hsqldb jdbc impelementation!
        //psWrite = mConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        psDelete = conn.prepareStatement("DELETE FROM HISTORY WHERE " + HISTORYID + " = ?");
        psDeleteTemp = conn.prepareStatement("DELETE FROM HISTORY WHERE " + HISTTYPE + " = " + HistoryReference.TYPE_TEMPORARY);
        psContainsURI = conn.prepareStatement("SELECT TOP 1 HISTORYID FROM HISTORY WHERE URI = ? AND  METHOD = ? AND REQBODY = ? AND SESSIONID = ? AND HISTTYPE = ?");

        isExistStatusCode = false;
        ResultSet rs = conn.getMetaData().getColumns(null, null, "HISTORY", "STATUSCODE");
        if (rs.next()) {
            isExistStatusCode = true;
        }
        rs.close();
        // ZAP: Added support for the tag when creating a history record
        if (isExistStatusCode) {
            psWrite1= conn.prepareStatement("INSERT INTO HISTORY ("
                    + SESSIONID + "," + HISTTYPE + "," + TIMESENTMILLIS + "," + 
                    TIMEELAPSEDMILLIS + "," + METHOD + "," + URI + "," + REQHEADER + "," + 
                    REQBODY + "," + RESHEADER + "," + RESBODY + "," + TAG + ", " + STATUSCODE
                    + ") VALUES (?, ? ,?, ?, ?, ?, ?, ? ,? , ?, ?, ?)");
        } else {
            psWrite1= conn.prepareStatement("INSERT INTO HISTORY ("
                    + SESSIONID + "," + HISTTYPE + "," + TIMESENTMILLIS + "," + 
                    TIMEELAPSEDMILLIS + "," + METHOD + "," + URI + "," + REQHEADER + "," + 
                    REQBODY + "," + RESHEADER + "," + RESBODY + "," + TAG
                    + ") VALUES (?, ? ,?, ?, ?, ?, ?, ? ,? , ? , ?)");
            
        }
        psWrite2 = conn.prepareCall("CALL IDENTITY();");

        rs = conn.getMetaData().getColumns(null, null, "HISTORY", "TAG");
        if (!rs.next()) {
            PreparedStatement stmt = conn.prepareStatement("ALTER TABLE HISTORY ADD COLUMN TAG VARCHAR DEFAULT ''");
            stmt.execute();
            stmt.close();
        }
        rs.close();
        
        psUpdateTag = conn.prepareStatement("UPDATE HISTORY SET TAG = ? WHERE HISTORYID = ?");

        // ZAP: Add the NOTE column to the db if necessary
        rs = conn.getMetaData().getColumns(null, null, "HISTORY", "NOTE");
        if (!rs.next()) {
            PreparedStatement stmt = conn.prepareStatement("ALTER TABLE HISTORY ADD COLUMN NOTE VARCHAR DEFAULT ''");
            stmt.execute();
            stmt.close();
        }
        rs.close();

       	psUpdateNote = conn.prepareStatement("UPDATE HISTORY SET NOTE = ? WHERE HISTORYID = ?");
       	psLastIndex = conn.prepareStatement("SELECT TOP 1 HISTORYID FROM HISTORY ORDER BY HISTORYID DESC");
    }
    
    
	public synchronized RecordHistory read(int historyId) throws HttpMalformedHeaderException, SQLException {
	    psRead.setInt(1, historyId);
		psRead.execute();
		ResultSet rs = psRead.getResultSet();
		RecordHistory result = null;
		try {
			result = build(rs);
		} finally {
			rs.close();
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
	    return write(sessionId, histType, msg.getTimeSentMillis(), msg.getTimeElapsedMillis(), method, uri, statusCode, reqHeader, reqBody, resHeader, resBody, null);
	    
	}
	
	private synchronized RecordHistory write(long sessionId, int histType, long timeSentMillis, int timeElapsedMillis,
	        String method, String uri, int statusCode,
	        String reqHeader, byte[] reqBody, String resHeader, byte[] resBody, String tag) throws HttpMalformedHeaderException, SQLException {

		psWrite1.setLong(1, sessionId);
		psWrite1.setInt(2, histType);
		psWrite1.setLong(3, timeSentMillis);
		psWrite1.setInt(4, timeElapsedMillis);
		psWrite1.setString(5, method);
		psWrite1.setString(6, uri);        
		psWrite1.setString(7, reqHeader);
		psWrite1.setBytes(8, reqBody);
		psWrite1.setString(9, resHeader);
		psWrite1.setBytes(10, resBody);
		psWrite1.setString(11, tag);

        if (isExistStatusCode) {
            psWrite1.setInt(12, statusCode);
        }
		psWrite1.executeUpdate();
				
		/*
        String sql = "INSERT INTO HISTORY ("
        		+ REQHEADER + "," + REQBODY + "," + RESHEADER + "," + RESBODY +
				") VALUES ('"+ reqHeader + "','" + reqBody + "','" + resHeader + "','" + resBody + "'); CALL IDENTITY();";
		Statement stmt = mConn.createStatement();
		stmt.executeQuery(sql);
		ResultSet rs = stmt.getResultSet();
		*/
		
		ResultSet rs = psWrite2.executeQuery();
		try {
			rs.next();
			int id = rs.getInt(1);
			return read(id);
		} finally {
			rs.close();
		}
	}
	
	private RecordHistory build(ResultSet rs) throws HttpMalformedHeaderException, SQLException {
		RecordHistory history = null;
		try {
			if (rs.next()) {
				history = new RecordHistory(
						rs.getInt(HISTORYID),
						rs.getInt(HISTTYPE),
	                    rs.getLong(SESSIONID),
						rs.getLong(TIMESENTMILLIS),
						rs.getInt(TIMEELAPSEDMILLIS),
						rs.getString(REQHEADER),
						rs.getBytes(REQBODY),
						rs.getString(RESHEADER),
						rs.getBytes(RESBODY),
	                    rs.getString(TAG),
	                    rs.getString(NOTE)			// ZAP: Added note
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
	    Vector<Integer> v = new Vector<Integer>();
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
	    Vector<Integer> v = new Vector<Integer>();
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
		Vector<Integer> v = new Vector<Integer>();
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
        Statement stmt = getConnection().createStatement();
        try {
        	stmt.executeUpdate("DELETE FROM HISTORY WHERE " + SESSIONID + " = " + sessionId);
		} finally {
			stmt.close();
		}
	}
	
	public void deleteHistoryType(long sessionId, int historyType) throws SQLException {
        Statement stmt = getConnection().createStatement();
        try {
        	stmt.executeUpdate("DELETE FROM HISTORY WHERE " + SESSIONID + " = " + sessionId + " AND " + HISTTYPE + " = " + historyType);
		} finally {
			stmt.close();
		}
	}

	public void delete(int historyId) throws SQLException {
		psDelete.setInt(1, historyId);
		psDelete.executeUpdate();
		
	}
	
	public void deleteTemporary() throws SQLException {
	    psDeleteTemp.execute();
	}
	
	public boolean containsURI(long sessionId, int historyType, String method, String uri, byte[] body) throws SQLException {
	    psContainsURI.setString(1, uri);
        psContainsURI.setString(2, method);
	    psContainsURI.setBytes(3, body);
	    psContainsURI.setLong(4, sessionId);
	    psContainsURI.setInt(5, historyType);
	    ResultSet rs = psContainsURI.executeQuery();
		try {
		    if (rs.next()) {
		        return true;
		    }
		} finally {
	    	rs.close();
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
        psReadCache.setBytes(3, reqMsg.getRequestBody().getBytes());

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
        psReadCache.setBytes(3, reqMsg.getRequestBody().getBytes());
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
    
    public int lastIndex () throws SQLException {
    	int lastIndex = -1;
		ResultSet rs = psLastIndex.executeQuery();
		try {
		    if (rs.next()) {
		        lastIndex = rs.getInt(HISTORYID);
		    }
		} finally {
	    	rs.close();
		}
	    return lastIndex;
    }

}
