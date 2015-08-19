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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.DbUtils;
import org.parosproxy.paros.db.RecordAlert;
import org.parosproxy.paros.db.TableAlert;


public class SqlTableAlert extends SqlAbstractTable implements TableAlert {
    
    private static final String TABLE_NAME 	= DbSQL.getSQL("alert.table_name");
    
    private static final String ALERT_INDEX = DbSQL.getSQL("alert.field.alert_index");
	private static final String ALERTID		= DbSQL.getSQL("alert.field.alertid");
	private static final String SCANID		= DbSQL.getSQL("alert.field.scanid");
	private static final String PLUGINID	= DbSQL.getSQL("alert.field.pluginid");
	private static final String ALERT		= DbSQL.getSQL("alert.field.alert");
	private static final String RISK		= DbSQL.getSQL("alert.field.risk");
	private static final String RELIABILITY	= DbSQL.getSQL("alert.field.reliability");
	private static final String DESCRIPTION	= DbSQL.getSQL("alert.field.description");
	private static final String URI			= DbSQL.getSQL("alert.field.uri");
	private static final String PARAM 		= DbSQL.getSQL("alert.field.param");
	private static final String ATTACK 		= DbSQL.getSQL("alert.field.attack");
	private static final String OTHERINFO	= DbSQL.getSQL("alert.field.otherinfo");
	private static final String SOLUTION	= DbSQL.getSQL("alert.field.solution");
	private static final String REFERENCE	= DbSQL.getSQL("alert.field.reference");
	private static final String EVIDENCE	= DbSQL.getSQL("alert.field.evidence");
	private static final String CWEID		= DbSQL.getSQL("alert.field.cweid");
	private static final String WASCID		= DbSQL.getSQL("alert.field.wascid");
	private static final String HISTORYID	= DbSQL.getSQL("alert.field.historyid");
	private static final String SOURCEHISTORYID	= DbSQL.getSQL("alert.field.sourcehistoryid");

    public SqlTableAlert() {
    }
    
    @Override
    protected void reconnect(Connection conn) throws DatabaseException {
        try {
			updateTable(conn);
		} catch (Exception e) {
			throw new DatabaseException(e);
		}
    }
    
    // ZAP: Added the method.
    private void updateTable(Connection connection) throws DatabaseException {
        try {
			// Add the SOURCEHISTORYID column to the db if necessary
			if (!DbUtils.hasColumn(connection, TABLE_NAME, SOURCEHISTORYID)) {
			    DbUtils.executeAndClose(connection.prepareStatement(DbSQL.getSQL("alert.ps.addsourcehistoryid")));
			}
			
			// Add the ATTACK column to the db if necessary
			if (!DbUtils.hasColumn(connection, TABLE_NAME, ATTACK)) {
			    DbUtils.executeAndClose(connection.prepareStatement(DbSQL.getSQL("alert.ps.addattack")));
			}
			
			if (!DbUtils.hasColumn(connection, TABLE_NAME, EVIDENCE)) {
				// Evidence, cweId and wascId all added at the same time
			    DbUtils.executeAndClose(connection.prepareStatement(DbSQL.getSQL("alert.ps.addevidence")));
			    DbUtils.executeAndClose(connection.prepareStatement(DbSQL.getSQL("alert.ps.addcweid")));
			    DbUtils.executeAndClose(connection.prepareStatement(DbSQL.getSQL("alert.ps.addwascid")));
			}
			
			if (!DbUtils.hasIndex(connection, TABLE_NAME, ALERT_INDEX)) {
				// this speads up session loading
				DbUtils.executeAndClose(connection.prepareStatement(DbSQL.getSQL("alert.ps.addalertindex")));
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
        
    }

    /* (non-Javadoc)
	 * @see org.parosproxy.paros.db.paros.TableAlert#read(int)
	 */
    @Override
	public synchronized RecordAlert read(int alertId) throws DatabaseException {
    	SqlPreparedStatementWrapper psRead = null;
        try {
		    psRead = DbSQL.getSingleton().getPreparedStatement("alert.ps.read");
			psRead.getPs().setInt(1, alertId);
			try (ResultSet rs = psRead.getPs().executeQuery()) {
				RecordAlert ra = build(rs);
				return ra;
			}
		} catch (Exception e) {
			throw new DatabaseException(e);
		} finally {
			DbSQL.getSingleton().releasePreparedStatement(psRead);
		}
    }
    

    /* (non-Javadoc)
	 * @see org.parosproxy.paros.db.paros.TableAlert#write(int, int, java.lang.String, int, int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, int, int, int, int)
	 */
    @Override
	public synchronized RecordAlert write(int scanId, int pluginId, String alert, 
            int risk, int confidence, String description, String uri, String param, String attack, 
            String otherInfo, String solution, String reference, String evidence, int cweId, int wascId, int historyId,
            int sourceHistoryId) throws DatabaseException {
        
    	SqlPreparedStatementWrapper psInsert = null;
        try {
        	psInsert = DbSQL.getSingleton().getPreparedStatement("alert.ps.insert");
			psInsert.getPs().setInt(1, scanId);
			psInsert.getPs().setInt(2, pluginId);
			psInsert.getPs().setString(3, alert);
			psInsert.getPs().setInt(4, risk);
			psInsert.getPs().setInt(5, confidence);
			psInsert.getPs().setString(6, description);
			psInsert.getPs().setString(7, uri);
			psInsert.getPs().setString(8, param);
			psInsert.getPs().setString(9, attack);
			psInsert.getPs().setString(10, otherInfo);
			psInsert.getPs().setString(11, solution);
			psInsert.getPs().setString(12, reference);
			psInsert.getPs().setString(13, evidence);
			psInsert.getPs().setInt(14, cweId);
			psInsert.getPs().setInt(15, wascId);
			psInsert.getPs().setInt(16, historyId);
			psInsert.getPs().setInt(17, sourceHistoryId);
			
			psInsert.getPs().executeUpdate();
			
			int id;
			try (ResultSet rs = psInsert.getLastInsertedId()) {
			    rs.next();
			    id = rs.getInt(1);
			}
			return read(id);
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DbSQL.getSingleton().releasePreparedStatement(psInsert);
		}
    }
    
    private RecordAlert build(ResultSet rs) throws DatabaseException {
        try {
			RecordAlert alert = null;
			if (rs.next()) {
			    alert = new RecordAlert(
			            rs.getInt(ALERTID),
			            rs.getInt(SCANID),
			            rs.getInt(PLUGINID),
			            rs.getString(ALERT),
			            rs.getInt(RISK),
			            rs.getInt(RELIABILITY),
			            rs.getString(DESCRIPTION),
			            rs.getString(URI),
			            rs.getString(PARAM),
			            rs.getString(ATTACK),
			            rs.getString(OTHERINFO),
			            rs.getString(SOLUTION),
			            rs.getString(REFERENCE),
			            rs.getString(EVIDENCE),
			            rs.getInt(CWEID),
			            rs.getInt(WASCID),
			            rs.getInt(HISTORYID),
			            rs.getInt(SOURCEHISTORYID)
			    );
			}
			return alert;
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
    }
    
    /* (non-Javadoc)
	 * @see org.parosproxy.paros.db.paros.TableAlert#deleteAlert(int)
	 */
    @Override
	public void deleteAlert(int alertId) throws DatabaseException {
    	SqlPreparedStatementWrapper psDeleteAlert = null;
        try {
        	psDeleteAlert = DbSQL.getSingleton().getPreparedStatement("alert.ps.delete");
			psDeleteAlert.getPs().setInt(1, alertId);
			psDeleteAlert.getPs().execute();
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DbSQL.getSingleton().releasePreparedStatement(psDeleteAlert);
		}
    }
    
    /* (non-Javadoc)
	 * @see org.parosproxy.paros.db.paros.TableAlert#deleteAllAlerts()
	 */
    @Override
	public int deleteAllAlerts() throws DatabaseException {
    	SqlPreparedStatementWrapper psDeleteAllAlerts = null;
        try {
        	psDeleteAllAlerts = DbSQL.getSingleton().getPreparedStatement("alert.ps.deleteall");
			return psDeleteAllAlerts.getPs().executeUpdate();
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DbSQL.getSingleton().releasePreparedStatement(psDeleteAllAlerts);
		}
    }

    /* (non-Javadoc)
	 * @see org.parosproxy.paros.db.paros.TableAlert#update(int, java.lang.String, int, int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, int, int, int)
	 */
    @Override
	public synchronized void update(int alertId, String alert, 
            int risk, int confidence, String description, String uri, 
            String param, String attack, String otherInfo, String solution, String reference,
            String evidence, int cweId, int wascId, int sourceHistoryId) throws DatabaseException {
        
    	SqlPreparedStatementWrapper psUpdate = null;
        try {
        	psUpdate = DbSQL.getSingleton().getPreparedStatement("alert.ps.update");
			psUpdate.getPs().setString(1, alert);
			psUpdate.getPs().setInt(2, risk);
			psUpdate.getPs().setInt(3, confidence);
			psUpdate.getPs().setString(4, description);
			psUpdate.getPs().setString(5, uri);
			psUpdate.getPs().setString(6, param);
			psUpdate.getPs().setString(7, attack);
			psUpdate.getPs().setString(8, otherInfo);
			psUpdate.getPs().setString(9, solution);
			psUpdate.getPs().setString(10, reference);
			psUpdate.getPs().setString(11, evidence);
			psUpdate.getPs().setInt(12, cweId);
			psUpdate.getPs().setInt(13, wascId);
			psUpdate.getPs().setInt(14, sourceHistoryId);
			psUpdate.getPs().setInt(15, alertId);
			psUpdate.getPs().executeUpdate();
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DbSQL.getSingleton().releasePreparedStatement(psUpdate);
		}
    }

    /* (non-Javadoc)
	 * @see org.parosproxy.paros.db.paros.TableAlert#updateHistoryIds(int, int, int)
	 */
    @Override
	public synchronized void updateHistoryIds(int alertId, 
            int historyId, int sourceHistoryId) throws DatabaseException {
        
    	SqlPreparedStatementWrapper psUpdateHistoryIds = null;
        try {
        	psUpdateHistoryIds = DbSQL.getSingleton().getPreparedStatement("alert.ps.updatehistoryid");
			psUpdateHistoryIds.getPs().setInt(1, historyId);
			psUpdateHistoryIds.getPs().setInt(2, sourceHistoryId);
			psUpdateHistoryIds.getPs().setInt(3, alertId);
			psUpdateHistoryIds.getPs().executeUpdate();
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DbSQL.getSingleton().releasePreparedStatement(psUpdateHistoryIds);
		}
    }

    /* (non-Javadoc)
	 * @see org.parosproxy.paros.db.paros.TableAlert#getAlertsBySourceHistoryId(int)
	 */
    @Override
	public List<RecordAlert> getAlertsBySourceHistoryId(int historyId) throws DatabaseException {
    	SqlPreparedStatementWrapper psGetAlertsForHistoryId = null;
        try {
        	psGetAlertsForHistoryId = DbSQL.getSingleton().getPreparedStatement("alert.ps.getalertsforhistoryid");
			List<RecordAlert> result = new ArrayList<>();
			psGetAlertsForHistoryId.getPs().setLong(1, historyId);
			try (ResultSet rs = psGetAlertsForHistoryId.getPs().executeQuery()) {
			    RecordAlert ra = build(rs);
			    while (ra != null) {
			        result.add(ra);
			        ra = build(rs);
			    }
			}
			
			return result;
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DbSQL.getSingleton().releasePreparedStatement(psGetAlertsForHistoryId);
		}
    }

    @Override
	public Vector<Integer> getAlertListBySession(long sessionId) throws DatabaseException {
    	SqlPreparedStatementWrapper psGetAlertsForSession = null;
        try {
        	psGetAlertsForSession = DbSQL.getSingleton().getPreparedStatement("alert.ps.getalertsforsession");
		    Vector<Integer> v = new Vector<>();
			psGetAlertsForSession.getPs().setLong(1, sessionId);
		    try (ResultSet rs = psGetAlertsForSession.getPs().executeQuery()) {
		        while (rs.next()) {
		            int alertId = rs.getInt(ALERTID);
		            v.add(Integer.valueOf(alertId));
		        }
		    }
		    return v;
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DbSQL.getSingleton().releasePreparedStatement(psGetAlertsForSession);
		}
    }

	@Override
	public Vector<Integer> getAlertList() throws DatabaseException {
    	SqlPreparedStatementWrapper psGetAllAlertIds = null;
        try {
        	psGetAllAlertIds = DbSQL.getSingleton().getPreparedStatement("alert.ps.getallalertids");
		    Vector<Integer> v = new Vector<>();
		    try (ResultSet rs = psGetAllAlertIds.getPs().executeQuery()) {
		        while (rs.next()) {
		            v.add(Integer.valueOf(rs.getInt(ALERTID)));
		        }
		    }
		    return v;
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DbSQL.getSingleton().releasePreparedStatement(psGetAllAlertIds);
		}
    }
	
}
