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
import org.parosproxy.paros.db.RecordSessionUrl;
import org.parosproxy.paros.db.TableSessionUrl;

public class SqlTableSessionUrl extends SqlAbstractTable implements TableSessionUrl {
    
    private static final String TABLE_NAME 	= DbSQL.getSQL("sessionurl.table_name");
    
    private static final String URLID 		= DbSQL.getSQL("sessionurl.field.urlid");
    private static final String TYPE 		= DbSQL.getSQL("sessionurl.field.type");
    private static final String URL 		= DbSQL.getSQL("sessionurl.field.url");

    public SqlTableSessionUrl() {
        
    }
        
    @Override
    protected void reconnect(Connection conn) throws DatabaseException {
        try {
			if (!DbUtils.hasTable(conn, TABLE_NAME)) {
			    // Need to create the table
			    DbUtils.executeAndClose(conn.prepareStatement(DbSQL.getSQL("sessionurl.ps.cretetable")));
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
    }
  
	/* (non-Javadoc)
	 * @see org.parosproxy.paros.db.paros.TableSessionUrl#read(long)
	 */
	@Override
	public synchronized RecordSessionUrl read(long urlId) throws DatabaseException {
    	SqlPreparedStatementWrapper psRead = null;
        try {
        	psRead = DbSQL.getSingleton().getPreparedStatement("sessionurl.ps.read");
			psRead.getPs().setLong(1, urlId);
			
			try (ResultSet rs = psRead.getPs().executeQuery()) {
				return build(rs);
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DbSQL.getSingleton().releasePreparedStatement(psRead);
		}
	}
	
    /* (non-Javadoc)
	 * @see org.parosproxy.paros.db.paros.TableSessionUrl#insert(int, java.lang.String)
	 */
    @Override
	public synchronized RecordSessionUrl insert(int type, String url) throws DatabaseException {
    	SqlPreparedStatementWrapper psInsert = null;
        try {
        	psInsert = DbSQL.getSingleton().getPreparedStatement("sessionurl.ps.insert");
			psInsert.getPs().setInt(1, type);
			psInsert.getPs().setString(2, url);
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
	 * @see org.parosproxy.paros.db.paros.TableSessionUrl#delete(int, java.lang.String)
	 */
    @Override
	public synchronized void delete(int type, String url) throws DatabaseException {
    	SqlPreparedStatementWrapper psDeleteUrls = null;
        try {
        	psDeleteUrls = DbSQL.getSingleton().getPreparedStatement("sessionurl.ps.deleteurls");
			psDeleteUrls.getPs().setInt(1, type);
			psDeleteUrls.getPs().setString(2, url);
			psDeleteUrls.getPs().executeUpdate();
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DbSQL.getSingleton().releasePreparedStatement(psDeleteUrls);
		}
    }
    
    /* (non-Javadoc)
	 * @see org.parosproxy.paros.db.paros.TableSessionUrl#deleteAllUrlsForType(int)
	 */
    @Override
	public synchronized void deleteAllUrlsForType(int type) throws DatabaseException {
    	SqlPreparedStatementWrapper psDeleteAllUrlsForType = null;
        try {
        	psDeleteAllUrlsForType = DbSQL.getSingleton().getPreparedStatement("sessionurl.ps.deleteurlsfortype");
			psDeleteAllUrlsForType.getPs().setInt(1, type);
			psDeleteAllUrlsForType.getPs().executeUpdate();
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DbSQL.getSingleton().releasePreparedStatement(psDeleteAllUrlsForType);
		}
    }
    

    /* (non-Javadoc)
	 * @see org.parosproxy.paros.db.paros.TableSessionUrl#getUrlsForType(int)
	 */
    @Override
	public List<RecordSessionUrl> getUrlsForType (int type) throws DatabaseException {
    	SqlPreparedStatementWrapper psGetAlluRLSForType = null;
        try {
        	psGetAlluRLSForType = DbSQL.getSingleton().getPreparedStatement("sessionurl.ps.geturlsfortype");
			psGetAlluRLSForType.getPs().setInt(1, type);
			try (ResultSet rs = psGetAlluRLSForType.getPs().executeQuery()) {
				List<RecordSessionUrl> result = new ArrayList<>();
				while (rs.next()) {
					result.add(new RecordSessionUrl(rs.getLong(URLID), rs.getInt(TYPE), rs.getString(URL)));
				}
				return result;
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DbSQL.getSingleton().releasePreparedStatement(psGetAlluRLSForType);
		}
    }
                
    private RecordSessionUrl build(ResultSet rs) throws DatabaseException {
        try {
			RecordSessionUrl rt = null;
			if (rs.next()) {
			    rt = new RecordSessionUrl(rs.getLong(URLID), rs.getInt(TYPE), rs.getString(URL));            
			}
			return rt;
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
    }

	/* (non-Javadoc)
	 * @see org.parosproxy.paros.db.paros.TableSessionUrl#setUrls(int, java.util.List)
	 */
	@Override
	public void setUrls(int type, List<String> urls) throws DatabaseException {
		this.deleteAllUrlsForType(type);
		for (String url : urls) {
			this.insert(type, url);
		}
	}    
}
