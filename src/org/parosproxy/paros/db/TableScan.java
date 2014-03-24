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
// ZAP: 2011/05/27 Ensure all PreparedStatements and ResultSets closed to prevent leaks 
// ZAP: 2012/04/23 Added @Override annotation to the appropriate method.
// ZAP: 2012/08/08 Upgrade to HSQLDB 2.x (introduced TABLE_NAME constant + DbUtils)
// ZAP: 2014/03/23 Changed to use try-with-resource statements.

package org.parosproxy.paros.db;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TableScan extends AbstractTable {
    
    private static final String TABLE_NAME = "SCAN";
    
    private static final String SCANID	= "SCANID";
    private static final String SESSIONID	= "SESSIONID";
    private static final String SCANNAME	= "SCANNAME";
    private static final String SCANTIME = "SCANTIME";
    
    private PreparedStatement psRead = null;
    private PreparedStatement psInsert = null;
    private CallableStatement psGetIdLastInsert = null;

    //private PreparedStatement psUpdate = null;
    
    public TableScan() {
        
    }
        
    @Override
    protected void reconnect(Connection conn) throws SQLException {
        psRead  = conn.prepareStatement("SELECT * FROM " + TABLE_NAME +" WHERE " + SCANID + " = ?");
        psInsert = conn.prepareStatement("INSERT INTO SCAN (" + SESSIONID + ","+ SCANNAME + ") VALUES (?, ?)");
        psGetIdLastInsert = conn.prepareCall("CALL IDENTITY();");
       
    }
    
    public synchronized RecordScan getLatestScan() throws SQLException {
        try (PreparedStatement psLatest = getConnection().prepareStatement("SELECT * FROM SCAN WHERE SCANID = (SELECT MAX(B.SCANID) FROM SCAN AS B)")) {
			try (ResultSet rs = psLatest.executeQuery()) {
			RecordScan result = build(rs);
			return result;
			}
        }
    }
    
	public synchronized RecordScan read(int scanId) throws SQLException {
		psRead.setInt(1, scanId);
		
		try (ResultSet rs = psRead.executeQuery()) {
			RecordScan result = build(rs);
			return result;
		}
	}
	
    public synchronized RecordScan insert(long sessionId, String scanName) throws SQLException {
        psInsert.setLong(1, sessionId);
        psInsert.setString(2, scanName);
        psInsert.executeUpdate();
        
		int id;
		try (ResultSet rs = psGetIdLastInsert.executeQuery()) {
			rs.next();
			id = rs.getInt(1);
		}
		return read(id);
		
    }
        
    private RecordScan build(ResultSet rs) throws SQLException {
        RecordScan scan = null;
        if (rs.next()) {
            scan = new RecordScan(rs.getInt(SCANID), rs.getString(SCANNAME), rs.getDate(SCANTIME));            
        }
        rs.close();
        return scan;
        
    }    
    
}
