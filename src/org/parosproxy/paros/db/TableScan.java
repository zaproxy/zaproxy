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

package org.parosproxy.paros.db;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TableScan extends AbstractTable { 
    
    private static final String SCANID	= "SCANID";
    private static final String SESSIONID	= "SESSIONID";
    private static final String SCANNAME	= "SCANNAME";
    private static final String SCANTIME = "SCANTIME";
    
    private PreparedStatement psRead = null;
    private PreparedStatement psInsert1 = null;
    private CallableStatement psInsert2 = null;

    private PreparedStatement psUpdate = null;
    
    public TableScan() {
        
    }
        
    protected void reconnect(Connection conn) throws SQLException {
        psRead	= conn.prepareStatement("SELECT * FROM SCAN WHERE SCANID = ?");
        psInsert1 = conn.prepareStatement("INSERT INTO SCAN ("
                + SESSIONID + ","+ SCANNAME + ") VALUES (?, ?)");
        psInsert2 = conn.prepareCall("CALL IDENTITY();");
       
    }
    
    public synchronized RecordScan getLatestScan() throws SQLException {
        PreparedStatement psLatest	= getConnection().prepareStatement("SELECT * FROM SCAN WHERE SCANID = (SELECT MAX(B.SCANID) FROM SCAN AS B)");
        
        psLatest.execute();		
		ResultSet rs = psLatest.getResultSet();
		RecordScan result = build(rs);
		rs.close();
		psLatest.close();
		return result;
		
    }
    
	public synchronized RecordScan read(int scanId) throws SQLException {
		psRead.setInt(1, scanId);
		psRead.execute();
		
		ResultSet rs = psRead.getResultSet();
		RecordScan result = build(rs);
		rs.close();
		return result;
	}
	
    public synchronized RecordScan insert(long sessionId, String scanName) throws SQLException {
        psInsert1.setLong(1, sessionId);
        psInsert1.setString(2, scanName);
        psInsert1.executeUpdate();
        
        psInsert2.executeQuery();
		ResultSet rs = psInsert2.getResultSet();
		rs.next();
		int id = rs.getInt(1);
		rs.close();
		return read(id);
		
    }
        
    private RecordScan build(ResultSet rs) throws SQLException {
        RecordScan scan = null;
        if (rs.next()) {
            scan = new RecordScan(rs.getInt(SCANID), rs.getString(SCANNAME), rs.getDate(SCANTIME));            
        }
        return scan;
        
    }    
    
}
