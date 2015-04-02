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
import org.parosproxy.paros.db.DbUtils;
import org.parosproxy.paros.db.RecordContext;
import org.parosproxy.paros.db.TableContext;

public class SqlTableContext extends SqlAbstractTable implements TableContext {
    
    private static final String TABLE_NAME 	= DbSQL.getSQL("context.table_name");
    private static final String DATAID = DbSQL.getSQL("context.field.dataid");
    private static final String CONTEXTID = DbSQL.getSQL("context.field.contextid");
    private static final String TYPE = DbSQL.getSQL("context.field.type");
    private static final String DATA = DbSQL.getSQL("context.field.data");

    public SqlTableContext() {
        
    }
        
    @Override
    protected void reconnect(Connection conn) throws DatabaseException {
        try {
			if (!DbUtils.hasTable(conn, TABLE_NAME)) {
			    // Need to create the table
			    DbUtils.executeAndClose(conn.prepareStatement(DbSQL.getSQL("context.ps.createtable")));
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
    }
  
	/* (non-Javadoc)
	 * @see org.parosproxy.paros.db.paros.TableContext#read(long)
	 */
	@Override
	public synchronized RecordContext read(long dataId) throws DatabaseException {
    	SqlPreparedStatementWrapper psRead = null;
		try {
        	psRead = DbSQL.getSingleton().getPreparedStatement("context.ps.read");
			psRead.getPs().setLong(1, dataId);
			
			try (ResultSet rs = psRead.getPs().executeQuery()) {
				RecordContext result = build(rs);
				return result;
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DbSQL.getSingleton().releasePreparedStatement(psRead);
		}
	}
	
    /* (non-Javadoc)
	 * @see org.parosproxy.paros.db.paros.TableContext#insert(int, int, java.lang.String)
	 */
    @Override
	public synchronized RecordContext insert(int contextId, int type, String url) throws DatabaseException {
    	SqlPreparedStatementWrapper psInsert = null;
        try {
        	psInsert = DbSQL.getSingleton().getPreparedStatement("context.ps.insert");
			psInsert.getPs().setInt(1, contextId);
			psInsert.getPs().setInt(2, type);
			psInsert.getPs().setString(3, url);
			psInsert.getPs().executeUpdate();
			
			long id;
			try (ResultSet rs = psInsert.getLastInsertedId()) {
				rs.next();
				id = rs.getLong(1);
			}
			return read(id);
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DbSQL.getSingleton().releasePreparedStatement(psInsert);
		}
		
    }
    
    /* (non-Javadoc)
	 * @see org.parosproxy.paros.db.paros.TableContext#delete(int, int, java.lang.String)
	 */
    @Override
	public synchronized void delete(int contextId, int type, String data) throws DatabaseException {
    	SqlPreparedStatementWrapper psDeleteData = null;
    	try {
        	psDeleteData = DbSQL.getSingleton().getPreparedStatement("context.ps.delete");
			psDeleteData.getPs().setInt(1, contextId);
			psDeleteData.getPs().setInt(2, type);
			psDeleteData.getPs().setString(3, data);
			psDeleteData.getPs().executeUpdate();
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DbSQL.getSingleton().releasePreparedStatement(psDeleteData);
		}
    }
    
    /* (non-Javadoc)
	 * @see org.parosproxy.paros.db.paros.TableContext#deleteAllDataForContextAndType(int, int)
	 */
    @Override
	public synchronized void deleteAllDataForContextAndType(int contextId, int type) throws DatabaseException {
    	SqlPreparedStatementWrapper psDeleteAllDataForContextAndType = null;
    	try {
        	psDeleteAllDataForContextAndType = DbSQL.getSingleton().getPreparedStatement("context.ps.alldataforcontexttype");
			psDeleteAllDataForContextAndType.getPs().setInt(1, contextId);
			psDeleteAllDataForContextAndType.getPs().setInt(2, type);
			psDeleteAllDataForContextAndType.getPs().executeUpdate();
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DbSQL.getSingleton().releasePreparedStatement(psDeleteAllDataForContextAndType);
		}
    }
    
    /* (non-Javadoc)
	 * @see org.parosproxy.paros.db.paros.TableContext#deleteAllDataForContext(int)
	 */
    @Override
	public synchronized void deleteAllDataForContext(int contextId) throws DatabaseException {
    	SqlPreparedStatementWrapper psDeleteAllDataForContext = null;
    	try {
        	psDeleteAllDataForContext = DbSQL.getSingleton().getPreparedStatement("context.ps.alldataforcontext");
			psDeleteAllDataForContext.getPs().setInt(1, contextId);
			psDeleteAllDataForContext.getPs().executeUpdate();
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DbSQL.getSingleton().releasePreparedStatement(psDeleteAllDataForContext);
		}
    }

    /* (non-Javadoc)
	 * @see org.parosproxy.paros.db.paros.TableContext#getAllData()
	 */
    @Override
	public List<RecordContext> getAllData () throws DatabaseException {
    	SqlPreparedStatementWrapper psGetAllData = null;
    	try {
        	psGetAllData = DbSQL.getSingleton().getPreparedStatement("context.ps.alldata");
			List<RecordContext> result = new ArrayList<>();
			try (ResultSet rs = psGetAllData.getPs().executeQuery()) {
				while (rs.next()) {
					result.add(new RecordContext(rs.getLong(DATAID), rs.getInt(CONTEXTID), rs.getInt(TYPE), rs.getString(DATA)));
				}
			}
			
			return result;
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DbSQL.getSingleton().releasePreparedStatement(psGetAllData);
		}
    }
                
    /* (non-Javadoc)
	 * @see org.parosproxy.paros.db.paros.TableContext#getDataForContext(int)
	 */
    @Override
	public List<RecordContext> getDataForContext (int contextId) throws DatabaseException {
    	SqlPreparedStatementWrapper psGetAllDataForContext = null;
    	try {
        	psGetAllDataForContext = DbSQL.getSingleton().getPreparedStatement("context.ps.alldataforcontext");
			List<RecordContext> result = new ArrayList<>();
			psGetAllDataForContext.getPs().setInt(1, contextId);
			try (ResultSet rs = psGetAllDataForContext.getPs().executeQuery()) {
				while (rs.next()) {
					result.add(new RecordContext(rs.getLong(DATAID), rs.getInt(CONTEXTID), rs.getInt(TYPE), rs.getString(DATA)));
				}
			}
			
			return result;
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DbSQL.getSingleton().releasePreparedStatement(psGetAllDataForContext);
		}
    }
                
    /* (non-Javadoc)
	 * @see org.parosproxy.paros.db.paros.TableContext#getDataForContextAndType(int, int)
	 */
    @Override
	public List<RecordContext> getDataForContextAndType (int contextId, int type) throws DatabaseException {
    	SqlPreparedStatementWrapper psGetAllDataForContextAndType = null;
    	try {
        	psGetAllDataForContextAndType = DbSQL.getSingleton().getPreparedStatement("context.ps.alldataforcontexttype");
			List<RecordContext> result = new ArrayList<>();
			psGetAllDataForContextAndType.getPs().setInt(1, contextId);
			psGetAllDataForContextAndType.getPs().setInt(2, type);
			try (ResultSet rs = psGetAllDataForContextAndType.getPs().executeQuery()) {
				while (rs.next()) {
					result.add(new RecordContext(rs.getLong(DATAID), rs.getInt(CONTEXTID), rs.getInt(TYPE), rs.getString(DATA)));
				}
			}
			
			return result;
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DbSQL.getSingleton().releasePreparedStatement(psGetAllDataForContextAndType);
		}
    }
                
    private RecordContext build(ResultSet rs) throws DatabaseException {
        try {
			RecordContext rt = null;
			if (rs.next()) {
			    rt = new RecordContext(rs.getLong(DATAID), rs.getInt(CONTEXTID), rs.getInt(TYPE), rs.getString(DATA));            
			}
			return rt;
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
    }

	/* (non-Javadoc)
	 * @see org.parosproxy.paros.db.paros.TableContext#setData(int, int, java.util.List)
	 */
	@Override
	public void setData(int contextId, int type, List<String> dataList) throws DatabaseException {
		this.deleteAllDataForContextAndType(contextId, type);
		for (String data : dataList) {
			this.insert(contextId, type, data);
		}
	}    
}
