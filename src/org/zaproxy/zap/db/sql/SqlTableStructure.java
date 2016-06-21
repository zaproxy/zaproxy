/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
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
import org.parosproxy.paros.db.DbUtils;
import org.parosproxy.paros.db.RecordStructure;
import org.parosproxy.paros.db.TableStructure;

public class SqlTableStructure extends SqlAbstractTable implements TableStructure {
    
    private static final String TABLE_NAME = "STRUCTURE";
    
    private static final String STRUCTUREID	= "STRUCTUREID";
    private static final String SESSIONID	= "SESSIONID";
    private static final String PARENTID	= "PARENTID";
    private static final String HISTORYID	= "HISTORYID";
    private static final String NAME		= "NAME";
    private static final String URL	= "URL";
    private static final String METHOD	= "METHOD";
    
    public SqlTableStructure() {
        
    }
    
    @Override
    protected void reconnect(Connection conn) throws DatabaseException {
        try {
			if (!DbUtils.hasTable(conn, TABLE_NAME)) {
			    // Need to create the table
			    DbUtils.executeAndClose(conn.prepareStatement(DbSQL.getSQL("structure.ps.createtable")));
			}

		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
    }
  
	/* (non-Javadoc)
	 * @see org.parosproxy.paros.db.paros.TableParam#read(long)
	 */
	@Override
	public synchronized RecordStructure read(long sessionId, long urlId) throws DatabaseException {
	    SqlPreparedStatementWrapper psRead = null;
		try {
		    psRead = DbSQL.getSingleton().getPreparedStatement("structure.ps.read");

			psRead.getPs().setLong(1, sessionId);
			psRead.getPs().setLong(2, urlId);
			
			try (ResultSet rs = psRead.getPs().executeQuery()) {
				RecordStructure result = null;
				if (rs.next()) {
					result = build(rs);
				}
				return result;
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DbSQL.getSingleton().releasePreparedStatement(psRead);
		}
	}

	@Override
	public RecordStructure insert(long sessionId, long parentId, int historyId, String name, String url, String method) throws DatabaseException {
		SqlPreparedStatementWrapper psInsert = null;
		try {
        	psInsert = DbSQL.getSingleton().getPreparedStatement("structure.ps.insert");
			psInsert.getPs().setLong(1, sessionId);
			psInsert.getPs().setLong(2, parentId);
			psInsert.getPs().setInt(3, historyId);
			psInsert.getPs().setString(4, name);
			psInsert.getPs().setLong(5, name.hashCode());
			psInsert.getPs().setString(6, url);
			psInsert.getPs().setString(7, method);
			psInsert.getPs().executeUpdate();
			
			long id;
			try (ResultSet rs = psInsert.getLastInsertedId()) {
				rs.next();
				id = rs.getLong(1);
			}
			return read(sessionId, id);
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DbSQL.getSingleton().releasePreparedStatement(psInsert);
		}
    }
    

	@Override
	public RecordStructure find(long sessionId, String name, String method) throws DatabaseException {
		SqlPreparedStatementWrapper psFind = null;
		try {
			psFind = DbSQL.getSingleton().getPreparedStatement("structure.ps.find");
			psFind.getPs().setLong(1, sessionId);
			psFind.getPs().setLong(2, name.hashCode());
			psFind.getPs().setString(3, method);
			try (ResultSet rs = psFind.getPs().executeQuery()) {
				while (rs.next()) {
					// We can get multiple records back due to hash collisions,
					// so double check the actual URL
					if (name.equals(rs.getString(NAME))) {
						return build(rs);
					}
				}
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DbSQL.getSingleton().releasePreparedStatement(psFind);
		}
		return null;
	}

	@Override
	public List<RecordStructure> getChildren(long sessionId, long parentId)
			throws DatabaseException {
		SqlPreparedStatementWrapper psGetChildren = null;
    	try {
    		psGetChildren = DbSQL.getSingleton().getPreparedStatement("structure.ps.getchildren");
    		psGetChildren.getPs().setLong(1, sessionId);
    		psGetChildren.getPs().setLong(2, parentId);
			List<RecordStructure> result = new ArrayList<>();
			try (ResultSet rs = psGetChildren.getPs().executeQuery()) {
				while (rs.next()) {
					result.add(build(rs));
				}
			}
			
			return result;
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DbSQL.getSingleton().releasePreparedStatement(psGetChildren);
		}
	}

	@Override
	public long getChildCount(long sessionId, long parentId) throws DatabaseException {
		SqlPreparedStatementWrapper psGetChildCount = null;
    	try {
    		psGetChildCount = DbSQL.getSingleton().getPreparedStatement("structure.ps.getchildcount");
    		psGetChildCount.getPs().setLong(1, sessionId);
    		psGetChildCount.getPs().setLong(2, parentId);
			try (ResultSet rs = psGetChildCount.getPs().executeQuery()) {
				if (rs.next()) {
					return rs.getLong(1);
				}
			}
			return 0;
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DbSQL.getSingleton().releasePreparedStatement(psGetChildCount);
		}
	}

	@Override
	public void deleteLeaf(long sessionId, long structureId) throws DatabaseException {
		// TODO Implement
		
	}

	@Override
	public void deleteSubtree(long sessionId, long structureId) throws DatabaseException {
		// TODO Implement
		
	}

    private RecordStructure build(ResultSet rs) throws DatabaseException {
        try {
		    return new RecordStructure(rs.getLong(SESSIONID), rs.getLong(STRUCTUREID), rs.getLong(PARENTID), rs.getInt(HISTORYID), 
		    		rs.getString(NAME), rs.getString(URL), rs.getString(METHOD));
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
    }

}
