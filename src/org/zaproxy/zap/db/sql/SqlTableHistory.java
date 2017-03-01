/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright The OWASP ZAP Development Team
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

package org.zaproxy.zap.db.sql;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.db.Database;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.DbUtils;
import org.parosproxy.paros.db.RecordHistory;
import org.parosproxy.paros.db.TableHistory;
import org.parosproxy.paros.extension.option.DatabaseParam;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpStatusCode;

public class SqlTableHistory extends SqlAbstractTable implements TableHistory {

    private static final String TABLE_NAME 	= DbSQL.getSQL("history.table_name");
    
    private static final String HISTORYID = DbSQL.getSQL("history.field.historyid");
	private static final String SESSIONID	= DbSQL.getSQL("history.field.sessionid");
	private static final String HISTTYPE	= DbSQL.getSQL("history.field.histtype");
    private static final String STATUSCODE  = DbSQL.getSQL("history.field.statuscode");
	private static final String TIMESENTMILLIS = DbSQL.getSQL("history.field.timesentmillis");
	private static final String TIMEELAPSEDMILLIS = DbSQL.getSQL("history.field.timeelapsedmillis");
	private static final String REQHEADER	= DbSQL.getSQL("history.field.reqheader");
	private static final String REQBODY		= DbSQL.getSQL("history.field.reqbody");
	private static final String RESHEADER	= DbSQL.getSQL("history.field.resheader");
	private static final String RESBODY		= DbSQL.getSQL("history.field.resbody");
    private static final String TAG         = DbSQL.getSQL("history.field.tag");
    private static final String NOTE        = DbSQL.getSQL("history.field.note");
    private static final String RESPONSE_FROM_TARGET_HOST = DbSQL.getSQL("history.field.responsefromtargethost");
    
    private int lastInsertedIndex;
    private static boolean isExistStatusCode = false;

    // ZAP: Added logger
    private static final Logger log = Logger.getLogger(SqlTableHistory.class);

    private boolean bodiesAsBytes; 

    public SqlTableHistory() {
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

			if (DbSQL.getDbType().equals(Database.DB_TYPE_HSQLDB)) {
				updateTable(conn);
			}
			
			isExistStatusCode = DbUtils.hasColumn(conn, TABLE_NAME, STATUSCODE);
			int currentIndex = 0;
			PreparedStatement stmt = null;
			try {
			    stmt = conn.prepareStatement(DbSQL.getSQL("history.ps.lastindex"));
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
			    DbUtils.executeAndClose(connection.prepareStatement(DbSQL.getSQL("history.ps.addtag")));
			}

			// Add the NOTE column to the db if necessary
			if (!DbUtils.hasColumn(connection, TABLE_NAME, NOTE)) {
			    DbUtils.executeAndClose(connection.prepareStatement(DbSQL.getSQL("history.ps.addnote")));
			}
			
			/* TODO how to handle HSQLDB dependancy?? Need to parameterize somehow.. vvvvvvvvvvvv */ 
			if (DbUtils.getColumnType(connection, TABLE_NAME, REQBODY) != 61 /*Types.SQL_VARBINARY*/) {
			    bodiesAsBytes = false;
			} else {
				// Databases created with ZAP<1.4.0.1 used VARCHAR for the REQBODY/RESBODY
				// HSQLDB 1.8.x converted from VARCHAR to bytes without problems
				// (through the method ResultSet.getBytes)
				// but the new version doesn't, it throws the following exception:
				// incompatible data type in conversion: from SQL type VARCHAR
			}

			if (!DbUtils.hasColumn(connection, TABLE_NAME, RESPONSE_FROM_TARGET_HOST)) {
			    DbUtils.executeAndClose(connection.prepareStatement(DbSQL.getSQL("history.ps.addrespfromtarget")));
			    DbUtils.executeUpdateAndClose(connection.prepareStatement(DbSQL.getSQL("history.ps.setrespfromtarget")));
			}
			
			int requestbodysizeindb = DbUtils.getColumnSize(connection, TABLE_NAME, REQBODY);
			int responsebodysizeindb = DbUtils.getColumnSize(connection, TABLE_NAME, RESBODY);
			try {	        
			    if (requestbodysizeindb != this.configuredrequestbodysize && this.configuredrequestbodysize > 0) {
			    	PreparedStatement stmt = connection.prepareStatement(DbSQL.getSQL("history.ps.changereqsize"));
			    	stmt.setInt(1, this.configuredrequestbodysize);
			    	DbUtils.executeAndClose(stmt);
			    }
			    
			    if (responsebodysizeindb != this.configuredresponsebodysize && this.configuredresponsebodysize > 0) {
			    	PreparedStatement stmt = connection.prepareStatement(DbSQL.getSQL("history.ps.changerespsize"));
			    	stmt.setInt(1, this.configuredresponsebodysize);
			    	DbUtils.executeAndClose(stmt);
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
    
	/* (non-Javadoc)
	 * @see org.parosproxy.paros.db.TbleHistoryIf#read(int)
	 */
	@Override
	public RecordHistory read(int historyId) throws HttpMalformedHeaderException, DatabaseException {
	    SqlPreparedStatementWrapper psRead = null;
	    try {
		    psRead = DbSQL.getSingleton().getPreparedStatement( "history.ps.read");
		    
			psRead.getPs().setInt(1, historyId);
			psRead.getPs().execute();
			RecordHistory result = null;
			try (ResultSet rs = psRead.getPs().getResultSet()) {
				result = build(rs);
			}

			return result;
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DbSQL.getSingleton().releasePreparedStatement(psRead);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.parosproxy.paros.db.TbleHistoryIf#write(long, int, org.parosproxy.paros.network.HttpMessage)
	 */
	@Override
	public RecordHistory write(long sessionId, int histType, HttpMessage msg) throws HttpMalformedHeaderException, DatabaseException {
	    
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
	
	private RecordHistory write(long sessionId, int histType, long timeSentMillis, int timeElapsedMillis,
	        String method, String uri, int statusCode,
	        String reqHeader, byte[] reqBody, String resHeader, byte[] resBody, String tag, String note, boolean responseFromTargetHost) throws HttpMalformedHeaderException, DatabaseException {

		//ZAP: Allow the request and response body sizes to be user-specifiable as far as possible
		if (reqBody.length > this.configuredrequestbodysize) {
			throw new DatabaseException("The actual Request Body length "+ reqBody.length + " is greater than the configured request body length "+ this.configuredrequestbodysize);
		}
		if (resBody.length > this.configuredresponsebodysize) {
			throw new DatabaseException("The actual Response Body length "+ resBody.length + " is greater than the configured response body length "+ this.configuredresponsebodysize);
		}

		SqlPreparedStatementWrapper psInsert = null;
	    try {
		    psInsert = DbSQL.getSingleton().getPreparedStatement( "history.ps.insertstd");
			psInsert.getPs().setLong(1, sessionId);
			psInsert.getPs().setInt(2, histType);
			psInsert.getPs().setLong(3, timeSentMillis);
			psInsert.getPs().setInt(4, timeElapsedMillis);
			psInsert.getPs().setString(5, method);
			psInsert.getPs().setString(6, uri);        
			psInsert.getPs().setString(7, reqHeader);
			if (bodiesAsBytes) {
			    psInsert.getPs().setBytes(8, reqBody);
			} else {
			    psInsert.getPs().setString(8, new String(reqBody, StandardCharsets.US_ASCII));
			}
			psInsert.getPs().setString(9, resHeader);
			if (bodiesAsBytes) {
			    psInsert.getPs().setBytes(10, resBody);
			} else {
			    psInsert.getPs().setString(10, new String(resBody, StandardCharsets.US_ASCII));
			}
			psInsert.getPs().setString(11, tag);

			// ZAP: Added the statement.
			int currentIdx = 12;
			
			if (isExistStatusCode) {
			    psInsert.getPs().setInt(currentIdx, statusCode);
			    // ZAP: Added the statement.
			    ++currentIdx;
			}
			
			// ZAP: Added the statement.
			psInsert.getPs().setString(currentIdx, note);
			++currentIdx;
			
			psInsert.getPs().setBoolean(currentIdx, responseFromTargetHost);
			
			psInsert.getPs().executeUpdate();
					
			try (ResultSet rs = psInsert.getLastInsertedId()) {
				rs.next();
				int id = rs.getInt(1);
			    lastInsertedIndex = id;
				return read(id);
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DbSQL.getSingleton().releasePreparedStatement(psInsert);
		}
	}
	
	private RecordHistory build(ResultSet rs) throws HttpMalformedHeaderException, DatabaseException {
		try {
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
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	
	}
	
    /* (non-Javadoc)
	 * @see org.parosproxy.paros.db.TbleHistoryIf#getHistoryIds(long)
	 */
    @Override
	public List<Integer> getHistoryIds(long sessionId) throws DatabaseException {
	    SqlPreparedStatementWrapper psGetAllHistoryIds = null;
        try {
		    psGetAllHistoryIds = DbSQL.getSingleton().getPreparedStatement( "history.ps.gethistoryids");
		    List<Integer> v = new ArrayList<>();
		    psGetAllHistoryIds.getPs().setLong(1, sessionId);
		    try (ResultSet rs = psGetAllHistoryIds.getPs().executeQuery()) {
		        while (rs.next()) {
		            v.add(Integer.valueOf(rs.getInt(HISTORYID)));
		        }
		    }
		    return v;
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DbSQL.getSingleton().releasePreparedStatement(psGetAllHistoryIds);
		}
        //return getHistoryIdsOfHistType(sessionId, null);
    }

    /* (non-Javadoc)
	 * @see org.parosproxy.paros.db.TbleHistoryIf#getHistoryIdsOfHistType(long, int)
	 */
    @Override
	public List<Integer> getHistoryIdsOfHistType(long sessionId, int... histTypes) throws DatabaseException {
    	if (histTypes == null || histTypes.length == 0) {
    		return getHistoryIds(sessionId);
    	}
    	
	    SqlPreparedStatementWrapper psGetAllHistoryIdsIncTypes = null;
        try {
		    psGetAllHistoryIdsIncTypes = DbSQL.getSingleton().getPreparedStatement( "history.ps.gethistoryidsinctypes");
		    List<Integer> v = new ArrayList<>();
		    psGetAllHistoryIdsIncTypes.getPs().setLong(1, sessionId);
		    DbSQL.setSetValues(psGetAllHistoryIdsIncTypes.getPs(), 2, histTypes);
		    try (ResultSet rs = psGetAllHistoryIdsIncTypes.getPs().executeQuery()) {
		        while (rs.next()) {
		            v.add(Integer.valueOf(rs.getInt(HISTORYID)));
		        }
		    }
		    return v;
		    
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DbSQL.getSingleton().releasePreparedStatement(psGetAllHistoryIdsIncTypes);
		}
    }

    /* (non-Javadoc)
	 * @see org.parosproxy.paros.db.TbleHistoryIf#getHistoryIdsExceptOfHistType(long, int)
	 */
    @Override
	public List<Integer> getHistoryIdsExceptOfHistType(long sessionId, int... histTypes) throws DatabaseException {
    	if (histTypes == null || histTypes.length == 0) {
    		return getHistoryIds(sessionId);
    	}
    	
	    SqlPreparedStatementWrapper psGetAllHistoryIdsExcTypes = null;
        try {
		    List<Integer> v = new ArrayList<>();
		    psGetAllHistoryIdsExcTypes = DbSQL.getSingleton().getPreparedStatement( "history.ps.gethistoryidsnottypes");
		    psGetAllHistoryIdsExcTypes.getPs().setLong(1, sessionId);
		    DbSQL.setSetValues(psGetAllHistoryIdsExcTypes.getPs(), 2, histTypes);
		    try (ResultSet rs = psGetAllHistoryIdsExcTypes.getPs().executeQuery()) {
		        while (rs.next()) {
		            v.add(Integer.valueOf(rs.getInt(HISTORYID)));
		        }
		    }
		    return v;
		    
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DbSQL.getSingleton().releasePreparedStatement(psGetAllHistoryIdsExcTypes);
		}
    }

	/* (non-Javadoc)
	 * @see org.parosproxy.paros.db.TbleHistoryIf#getHistoryList(long, int, java.lang.String, boolean)
	 */
	@Override
	public List<Integer> getHistoryList(long sessionId, int histType, String filter, boolean isRequest) throws DatabaseException {
        try { // TODO
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
	
	/* (non-Javadoc)
	 * @see org.parosproxy.paros.db.TbleHistoryIf#deleteHistorySession(long)
	 */
	@Override
	public void deleteHistorySession(long sessionId) throws DatabaseException {
	    SqlPreparedStatementWrapper psDeleteSession = null;
        try {
		    psDeleteSession = DbSQL.getSingleton().getPreparedStatement( "history.ps.deletesession");
        	psDeleteSession.getPs().setLong(1, sessionId);
			psDeleteSession.getPs().executeUpdate();
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DbSQL.getSingleton().releasePreparedStatement(psDeleteSession);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.parosproxy.paros.db.TbleHistoryIf#deleteHistoryType(long, int)
	 */
	@Override
	public void deleteHistoryType(long sessionId, int historyType) throws DatabaseException {
	    SqlPreparedStatementWrapper psDeleteType = null;
        try {
		    psDeleteType = DbSQL.getSingleton().getPreparedStatement( "history.ps.deletetype");
        	psDeleteType.getPs().setLong(1, sessionId);
        	psDeleteType.getPs().setInt(2, historyType);
			psDeleteType.getPs().executeUpdate();
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DbSQL.getSingleton().releasePreparedStatement(psDeleteType);
		}
	}

	/* (non-Javadoc)
	 * @see org.parosproxy.paros.db.TbleHistoryIf#delete(int)
	 */
	@Override
	public void delete(int historyId) throws DatabaseException {
	    SqlPreparedStatementWrapper psDelete = null;
		try {
		    psDelete = DbSQL.getSingleton().getPreparedStatement( "history.ps.delete");
			psDelete.getPs().setInt(1, historyId);
			psDelete.getPs().executeUpdate();
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DbSQL.getSingleton().releasePreparedStatement(psDelete);
		}
		
	}
	
    /* (non-Javadoc)
	 * @see org.parosproxy.paros.db.TbleHistoryIf#delete(java.util.List)
	 */
	// ZAP: Added method.
    @Override
	public void delete(List<Integer> ids) throws DatabaseException {
        delete(ids, 1000);
    }

    /* (non-Javadoc)
	 * @see org.parosproxy.paros.db.TbleHistoryIf#delete(java.util.List, int)
	 */
    @Override
	public void delete(List<Integer> ids, int batchSize) throws DatabaseException {
        if (ids == null) {
            throw new IllegalArgumentException("Parameter ids must not be null.");
        }
        if (batchSize <= 0) {
            throw new IllegalArgumentException("Parameter batchSize must be greater than zero.");
        }

	    SqlPreparedStatementWrapper psDelete = null;
        try {
		    psDelete = DbSQL.getSingleton().getPreparedStatement( "history.ps.delete");
			int count = 0;
			for (Integer id : ids) {
			    psDelete.getPs().setInt(1, id.intValue());
			    psDelete.getPs().addBatch();
			    count++;

			    if (count % batchSize == 0) {
			        psDelete.getPs().executeBatch();
			        count = 0;
			    }
			}
			if (count % batchSize != 0) {
			    psDelete.getPs().executeBatch();
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DbSQL.getSingleton().releasePreparedStatement(psDelete);
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

    /* (non-Javadoc)
	 * @see org.parosproxy.paros.db.TbleHistoryIf#deleteTemporary()
	 */
    @Override
	public void deleteTemporary() throws DatabaseException {
	    SqlPreparedStatementWrapper psDeleteTemp = null;
        try {
		    psDeleteTemp = DbSQL.getSingleton().getPreparedStatement( "history.ps.deletetemp");
			for (Integer type : HistoryReference.getTemporaryTypes()) {
				psDeleteTemp.getPs().setInt(1, type);
				psDeleteTemp.getPs().execute();
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DbSQL.getSingleton().releasePreparedStatement(psDeleteTemp);
		}
    }
	
	/* (non-Javadoc)
	 * @see org.parosproxy.paros.db.TbleHistoryIf#containsURI(long, int, java.lang.String, java.lang.String, byte[])
	 */
	@Override
	public boolean containsURI(long sessionId, int historyType, String method, String uri, byte[] body) throws DatabaseException {
	    SqlPreparedStatementWrapper psContainsURI = null;
	    try {
		    psContainsURI = DbSQL.getSingleton().getPreparedStatement( "history.ps.containsuri");
			psContainsURI.getPs().setString(1, uri);
			psContainsURI.getPs().setString(2, method);
			
			if (bodiesAsBytes) {
			    psContainsURI.getPs().setBytes(3, body);
			} else {
			    psContainsURI.getPs().setString(3, new String(body));
			}
			
			psContainsURI.getPs().setLong(4, sessionId);
			psContainsURI.getPs().setInt(5, historyType);
			try (ResultSet rs = psContainsURI.getPs().executeQuery()) {
			    if (rs.next()) {
			        return true;
			    }
			}
			return false;
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DbSQL.getSingleton().releasePreparedStatement(psContainsURI);
		}
	    
	}
    
    /* (non-Javadoc)
	 * @see org.parosproxy.paros.db.TbleHistoryIf#getHistoryCache(org.parosproxy.paros.model.HistoryReference, org.parosproxy.paros.network.HttpMessage)
	 */
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
			
			// TODO
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

			// TODO
			if (isExistStatusCode) {
			    psReadCache = getConnection().prepareStatement("SELECT TOP 1 * FROM HISTORY WHERE URI = ? AND METHOD = ? AND REQBODY = ? AND SESSIONID = ? AND STATUSCODE != 304");
			} else {
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
    
    /* (non-Javadoc)
	 * @see org.parosproxy.paros.db.TbleHistoryIf#updateNote(int, java.lang.String)
	 */
    @Override
	public void updateNote(int historyId, String note) throws DatabaseException {
	    SqlPreparedStatementWrapper psUpdateNote = null;
        try {
		    psUpdateNote = DbSQL.getSingleton().getPreparedStatement( "history.ps.updatenote");
			psUpdateNote.getPs().setString(1, note);
			psUpdateNote.getPs().setInt(2, historyId);
			psUpdateNote.getPs().execute();
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DbSQL.getSingleton().releasePreparedStatement(psUpdateNote);
		}
    }

    /* (non-Javadoc)
	 * @see org.parosproxy.paros.db.TbleHistoryIf#lastIndex()
	 */
    @Override
	public int lastIndex () {
        return lastInsertedIndex;
    }

}
