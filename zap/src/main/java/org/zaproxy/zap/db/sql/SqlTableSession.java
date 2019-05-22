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

import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.RecordSession;
import org.parosproxy.paros.db.TableSession;



public class SqlTableSession extends SqlAbstractTable implements TableSession {

	private static final String SESSIONID		= DbSQL.getSQL("session.field.sessionid");
	private static final String SESSIONNAME		= DbSQL.getSQL("session.field.sessionname");
	private static final String LASTACCESS		= DbSQL.getSQL("session.field.lastaccess");

	public SqlTableSession() {
        
    }
        
    @Override
    protected void reconnect(Connection conn) throws DatabaseException {
    }
    
    /* (non-Javadoc)
	 * @see org.parosproxy.paros.db.paros.TableSession#insert(long, java.lang.String)
	 */
    @Override
	public synchronized void insert(long sessionId, String sessionName) throws DatabaseException {
    	SqlPreparedStatementWrapper psInsert = null;
        try {
        	psInsert = DbSQL.getSingleton().getPreparedStatement("session.ps.insert");
			psInsert.getPs().setLong(1, sessionId);
			psInsert.getPs().setString(2, sessionName);
			psInsert.getPs().executeUpdate();
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DbSQL.getSingleton().releasePreparedStatement(psInsert);
		}
    }
    
    /* (non-Javadoc)
	 * @see org.parosproxy.paros.db.paros.TableSession#update(long, java.lang.String)
	 */
    @Override
	public synchronized void update(long sessionId, String sessionName) throws DatabaseException {
    	SqlPreparedStatementWrapper psUpdate = null;
        try {
        	psUpdate = DbSQL.getSingleton().getPreparedStatement("session.ps.update");
			psUpdate.getPs().setLong(2, sessionId);
			psUpdate.getPs().setString(1, sessionName);
			psUpdate.getPs().executeUpdate();
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DbSQL.getSingleton().releasePreparedStatement(psUpdate);
		}
    }
    
    @Override
    public List<RecordSession> listSessions() throws DatabaseException {
    	SqlPreparedStatementWrapper psList = null;
        try {
        	psList = DbSQL.getSingleton().getPreparedStatement("session.ps.list");
			List<RecordSession> result = new ArrayList<RecordSession>();
			try (ResultSet rs = psList.getPs().executeQuery()) {
				RecordSession ra = build(rs);
			    while (ra != null) {
			        result.add(ra);
			        ra = build(rs);
			    }
			}
			
			return result;
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DbSQL.getSingleton().releasePreparedStatement(psList);
		}

    }
    
    private RecordSession build(ResultSet rs) throws DatabaseException {
        try {
        	RecordSession session = null;
			if (rs.next()) {
			    session = new RecordSession(
			            rs.getLong(SESSIONID),
			            rs.getString(SESSIONNAME),
			            rs.getDate(LASTACCESS)
			    );
			}
			return session;
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
    }

}
