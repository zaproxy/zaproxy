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
// ZAP: 2012/04/23 Added @Override annotation to the appropriate method.
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2015/02/09 Issue 1525: Introduce a database interface layer to allow for alternative implementations

package org.parosproxy.paros.db.paros;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.RecordSession;
import org.parosproxy.paros.db.TableSession;
import org.parosproxy.paros.network.HttpMalformedHeaderException;



public class ParosTableSession extends ParosAbstractTable implements TableSession {
    
    private static final String SESSIONID	= "SESSIONID";
    private static final String SESSIONNAME	= "SESSIONNAME";
    private static final String LASTACCESS	= "LASTACCESS";
    
    private PreparedStatement psReadDate = null;
    private PreparedStatement psReadAll = null;
    private PreparedStatement psInsert = null;
    private PreparedStatement psUpdate = null;
        
    public ParosTableSession() {
        
    }
        
    @Override
    protected void reconnect(Connection conn) throws DatabaseException {
        try {
			psReadDate = conn.prepareStatement("SELECT * FROM SESSION WHERE " + LASTACCESS + " < ?");
			psReadAll = conn.prepareStatement("SELECT * FROM SESSION");
			psInsert = conn.prepareStatement("INSERT INTO SESSION ("
			        + SESSIONID + "," + SESSIONNAME
			        + ") VALUES (?, ?)");
			psUpdate = conn.prepareStatement("UPDATE SESSION SET "
			        + SESSIONNAME + " = ?,"
			        + LASTACCESS + " = NOW "
			        + "WHERE " + SESSIONID + " = ?");
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
    }

    /*
    public synchronized RecordSession read(long sessionId) throws HttpMalformedHeaderException, SQLException {
        psRead.setInt(1, historyId);
        psRead.execute();
        ResultSet rs = psRead.getResultSet();
        return build(rs);
    }
    */
    
    /* (non-Javadoc)
	 * @see org.parosproxy.paros.db.paros.TableSession#insert(long, java.lang.String)
	 */
    @Override
	public synchronized void insert(long sessionId, String sessionName) throws DatabaseException {
        try {
			psInsert.setLong(1, sessionId);
			psInsert.setString(2, sessionName);
			psInsert.executeUpdate();
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
    }
    
    /* (non-Javadoc)
	 * @see org.parosproxy.paros.db.paros.TableSession#update(long, java.lang.String)
	 */
    @Override
	public synchronized void update(long sessionId, String sessionName) throws DatabaseException {
        try {
			psUpdate.setLong(2, sessionId);
			psUpdate.setString(1, sessionName);
			psUpdate.executeUpdate();
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
    }
    
    @Override
    public List<RecordSession> listSessions() throws DatabaseException {
    	// Not supported
    	return null;
    }

	private RecordSession build(ResultSet rs) throws HttpMalformedHeaderException, SQLException {
        RecordSession session = null;
        if (rs.next()) {
            session = new RecordSession(
                    rs.getLong(SESSIONID),
                    rs.getString(SESSIONNAME),
                    rs.getDate(LASTACCESS)
            );
            
        }
        return session;
        
    }    
    
}
