/*
*
* Paros and its related class files.
* 
* Paros is an HTTP/HTTPS proxy for assessing web application security.
* Copyright (C) 2003-2004 Chinotec Technologies Company
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
// ZAP: 2011/05/09 Support for API
// ZAP: 2011/05/27 Ensure all PreparedStatements and ResultSets closed to prevent leaks 
// ZAP: 2012/01/02 Separate param and attack
// ZAP: 2012/04/23 Added @Override annotation to the appropriate method.
// ZAP: 2012/04/25 Changed to use the method Integer.valueOf.

package org.parosproxy.paros.db;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;


public class TableAlert extends AbstractTable {

	private static final String	ALERTID		= "ALERTID";
	private static final String SCANID		= "SCANID";
	private static final String PLUGINID	= "PLUGINID";
	private static final String ALERT		= "ALERT";
	private static final String RISK		= "RISK";
	private static final String RELIABILITY	= "RELIABILITY";
	private static final String DESCRIPTION	= "DESCRIPTION";
	private static final String URI			= "URI";
	private static final String PARAM 		= "PARAM";
	private static final String ATTACK 		= "ATTACK";
	private static final String OTHERINFO	= "OTHERINFO";
	private static final String SOLUTION	= "SOLUTION";
	private static final String REFERENCE	= "REFERENCE";
	private static final String HISTORYID	= "HISTORYID";
	private static final String SOURCEHISTORYID	= "SOURCEHISTORYID";

    private PreparedStatement psRead = null;
    private PreparedStatement psInsert1 = null;
    private CallableStatement psInsert2 = null;

    private PreparedStatement psDeleteAlert = null;
    //private PreparedStatement psDeleteScan = null;

    private PreparedStatement psUpdate = null;
    private PreparedStatement psUpdateHistoryIds = null;

    private PreparedStatement psGetAlertsForHistoryId = null;

    public TableAlert() {
    }
    
    @Override
    protected void reconnect(Connection conn) throws SQLException {
        // ZAP: Add the SOURCEHISTORYID column to the db if necessary
        ResultSet rs = conn.getMetaData().getColumns(null, null, "ALERT", SOURCEHISTORYID);
        if (!rs.next()) {
            PreparedStatement stmt = conn.prepareStatement("ALTER TABLE ALERT ADD COLUMN SOURCEHISTORYID INT DEFAULT 0");
            stmt.execute();
        }
        rs.close();
        // ZAP: Add the ATTACK column to the db if necessary
        rs = conn.getMetaData().getColumns(null, null, "ALERT", ATTACK);
        if (!rs.next()) {
            PreparedStatement stmt = conn.prepareStatement("ALTER TABLE ALERT ADD COLUMN ATTACK VARCHAR DEFAULT ''");
            stmt.execute();
        }
        rs.close();

        psRead = conn.prepareStatement("SELECT TOP 1 * FROM ALERT WHERE " + ALERTID + " = ?");
        
        psInsert1 = conn.prepareStatement("INSERT INTO ALERT ("
        		+ SCANID + "," + PLUGINID + "," + ALERT + "," + RISK + "," + RELIABILITY + "," + DESCRIPTION + ","
        		+ URI + "," + PARAM + "," + ATTACK + "," + OTHERINFO + "," + SOLUTION + "," + REFERENCE + "," + HISTORYID        		
        		 + "," + SOURCEHISTORYID + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        psInsert2 = conn.prepareCall("CALL IDENTITY();");
        psDeleteAlert = conn.prepareStatement("DELETE FROM ALERT WHERE " + ALERTID + " = ?");
        //psDeleteScan = conn.prepareStatement("DELETE FROM ALERT WHERE " + SCANID + " = ?");

        // ZAP: New prepared statement for updating an alert
        psUpdate = conn.prepareStatement("UPDATE ALERT SET " +
        		ALERT + " = ?, " + 
        		RISK + " = ?," + 
        		RELIABILITY + " = ?," + 
        		DESCRIPTION + " = ?," +
        		URI + " = ?," + 
        		PARAM + " = ?," + 
        		ATTACK + " = ?," + 
        		OTHERINFO + " = ?," + 
        		SOLUTION + " = ?," + 		
        		REFERENCE + " = ?, " + 		
        		SOURCEHISTORYID + " = ? " + 
        		"WHERE " + ALERTID + " = ?");

        psUpdateHistoryIds = conn.prepareStatement("UPDATE ALERT SET " +
        		HISTORYID + " = ?, " + 
        		SOURCEHISTORYID + " = ? " + 
        		"WHERE " + ALERTID + " = ?");

        psGetAlertsForHistoryId = conn.prepareStatement(
        		"SELECT * FROM ALERT WHERE " + SOURCEHISTORYID + " = ?");

    }
    	
	public synchronized RecordAlert read(int alertId) throws SQLException {
		psRead.setInt(1, alertId);
		ResultSet rs = psRead.executeQuery();
		try {
			RecordAlert ra = build(rs);
			return ra;
		} finally {
			rs.close();
		}
	}
	

	public synchronized RecordAlert write(int scanId, int pluginId, String alert, 
			int risk, int reliability, String description, String uri, String param, String attack, 
			String otherInfo, String solution, String reference, int historyId,
			int sourceHistoryId) throws SQLException {
	    
		psInsert1.setInt(1, scanId);
		psInsert1.setInt(2, pluginId);
		psInsert1.setString(3, alert);
		psInsert1.setInt(4, risk);
		psInsert1.setInt(5, reliability);
		psInsert1.setString(6, description);
		psInsert1.setString(7, uri);
		psInsert1.setString(8, param);
		psInsert1.setString(9, attack);
		psInsert1.setString(10, otherInfo);
		psInsert1.setString(11, solution);
		psInsert1.setString(12, reference);
		psInsert1.setInt(13, historyId);
		psInsert1.setInt(14, sourceHistoryId);
		psInsert1.executeUpdate();
		
		ResultSet rs = psInsert2.executeQuery();
		rs.next();
		int id = rs.getInt(1);
		rs.close();
		return read(id);
	}
	
	private RecordAlert build(ResultSet rs) throws SQLException {
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
					rs.getInt(HISTORYID),
					rs.getInt(SOURCEHISTORYID)
			);
		}
		return alert;
	
	}
	
	public Vector<Integer> getAlertListByScan(int scanId) throws SQLException {
	    PreparedStatement psReadScan = getConnection().prepareStatement("SELECT ALERTID FROM ALERT WHERE " + SCANID + " = ?");
        
	    Vector<Integer> v = new Vector<Integer>();
		psReadScan.setInt(1, scanId);
		ResultSet rs = psReadScan.executeQuery();
		while (rs.next()) {
			// ZAP: Changed to use the method Integer.valueOf.
			v.add(Integer.valueOf(rs.getInt(ALERTID)));
		}
		rs.close();
		psReadScan.close();
		return v;
	}

	public Vector<Integer> getAlertListBySession(long sessionId) throws SQLException {

	    PreparedStatement psReadSession = getConnection().prepareStatement("SELECT ALERTID FROM ALERT INNER JOIN SCAN ON ALERT.SCANID = SCAN.SCANID WHERE SESSIONID = ?");
        
	    Vector<Integer> v = new Vector<Integer>();
		psReadSession.setLong(1, sessionId);
		ResultSet rs = psReadSession.executeQuery();
		while (rs.next()) {
		    int alertId = rs.getInt(ALERTID);
			// ZAP: Changed to use the method Integer.valueOf.
			v.add(Integer.valueOf(alertId));
		}
		rs.close();
		psReadSession.close();
		return v;
	}

	public Vector<Integer> getAlertLists() throws SQLException {

	    PreparedStatement psReadSession = getConnection().prepareStatement("SELECT ALERTID FROM ALERT");
        
	    Vector<Integer> v = new Vector<Integer>();
		ResultSet rs = psReadSession.executeQuery();
		while (rs.next()) {
		    int alertId = rs.getInt(ALERTID);
			// ZAP: Changed to use the method Integer.valueOf.
			v.add(Integer.valueOf(alertId));
		}
		rs.close();
		psReadSession.close();
		return v;
	}

	
	public void deleteAlert(int alertId) throws SQLException {
	    psDeleteAlert.setInt(1, alertId);
	    psDeleteAlert.execute();
	}
	
	public synchronized void update(int alertId, String alert, 
			int risk, int reliability, String description, String uri, 
			String param, String attack, String otherInfo, String solution, String reference,
			int sourceHistoryId) throws SQLException {
	    
		psUpdate.setString(1, alert);
		psUpdate.setInt(2, risk);
		psUpdate.setInt(3, reliability);
		psUpdate.setString(4, description);
		psUpdate.setString(5, uri);
		psUpdate.setString(6, param);
		psUpdate.setString(7, attack);
		psUpdate.setString(8, otherInfo);
		psUpdate.setString(9, solution);
		psUpdate.setString(10, reference);
		psUpdate.setInt(11, sourceHistoryId);
		psUpdate.setInt(12, alertId);
		psUpdate.executeUpdate();
	}

	public synchronized void updateHistoryIds(int alertId, 
			int historyId, int sourceHistoryId) throws SQLException {
	    
		psUpdateHistoryIds.setInt(1, historyId);
		psUpdateHistoryIds.setInt(2, sourceHistoryId);
		psUpdateHistoryIds.setInt(3, alertId);
		psUpdateHistoryIds.executeUpdate();
	}

	public List<RecordAlert> getAlertsBySourceHistoryId(int historyId) throws SQLException {

		List<RecordAlert> result = new ArrayList<RecordAlert>();
    	psGetAlertsForHistoryId.setLong(1, historyId);
    	ResultSet rs = psGetAlertsForHistoryId.executeQuery();
		RecordAlert ra = build(rs);
		while (ra != null) {
			result.add(ra);
			ra = build(rs);
		}
		rs.close();
    	
    	return result;
	}

	// ZAP: Added getAlertList
	public Vector<Integer> getAlertList() throws SQLException {
	    PreparedStatement psReadScan = getConnection().prepareStatement("SELECT ALERTID FROM ALERT");
        
	    Vector<Integer> v = new Vector<Integer>();
		ResultSet rs = psReadScan.executeQuery();
		while (rs.next()) {
			v.add(Integer.valueOf(rs.getInt(ALERTID)));
		}
		rs.close();
		psReadScan.close();
		return v;
	}
	
}
