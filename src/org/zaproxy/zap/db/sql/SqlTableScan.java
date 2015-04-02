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

import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.RecordScan;
import org.parosproxy.paros.db.TableScan;

public class SqlTableScan extends SqlAbstractTable implements TableScan {
    
    private static final String SCANID 		= DbSQL.getSQL("scan.field.scanid");
    private static final String SCANNAME	= DbSQL.getSQL("scan.field.scanname");
    private static final String SCANTIME 	= DbSQL.getSQL("scan.field.scantime");

    public SqlTableScan() {
    }
        
    @Override
    protected void reconnect(Connection conn) throws DatabaseException {
    }
    
    /* (non-Javadoc)
	 * @see org.parosproxy.paros.db.paros.TableScan#getLatestScan()
	 */
    @Override
	public synchronized RecordScan getLatestScan() throws DatabaseException {
    	SqlPreparedStatementWrapper psGetLatestScan = null;
        try {
        	psGetLatestScan = DbSQL.getSingleton().getPreparedStatement("scan.ps.getlatestscan");
			try (ResultSet rs = psGetLatestScan.getPs().executeQuery()) {
				RecordScan result = build(rs);
				return result;
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DbSQL.getSingleton().releasePreparedStatement(psGetLatestScan);
		}
    }
    
	/* (non-Javadoc)
	 * @see org.parosproxy.paros.db.paros.TableScan#read(int)
	 */
	@Override
	public synchronized RecordScan read(int scanId) throws DatabaseException {
    	SqlPreparedStatementWrapper psRead = null;
        try {
        	psRead = DbSQL.getSingleton().getPreparedStatement("scan.ps.read");
			psRead.getPs().setInt(1, scanId);
			
			try (ResultSet rs = psRead.getPs().executeQuery()) {
				RecordScan result = build(rs);
				return result;
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DbSQL.getSingleton().releasePreparedStatement(psRead);
		}
	}
	
    /* (non-Javadoc)
	 * @see org.parosproxy.paros.db.paros.TableScan#insert(long, java.lang.String)
	 */
    @Override
	public synchronized RecordScan insert(long sessionId, String scanName) throws DatabaseException {
    	SqlPreparedStatementWrapper psInsert = null;
        try {
        	psInsert = DbSQL.getSingleton().getPreparedStatement("scan.ps.insert");
			psInsert.getPs().setLong(1, sessionId);
			psInsert.getPs().setString(2, scanName);
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
        
    private RecordScan build(ResultSet rs) throws DatabaseException {
        try {
			RecordScan scan = null;
			if (rs.next()) {
			    scan = new RecordScan(rs.getInt(SCANID), rs.getString(SCANNAME), rs.getDate(SCANTIME));            
			}
			rs.close();
			return scan;
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
    }    
    
}
