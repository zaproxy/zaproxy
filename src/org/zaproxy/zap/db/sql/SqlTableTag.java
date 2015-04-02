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
import org.parosproxy.paros.db.RecordTag;
import org.parosproxy.paros.db.TableTag;

public class SqlTableTag extends SqlAbstractTable implements TableTag {
    
    private static final String TABLE_NAME 	= DbSQL.getSQL("tag.table_name");
    
    private static final String TAGID 		= DbSQL.getSQL("tag.field.tagid");
    private static final String HISTORYID	= DbSQL.getSQL("tag.field.historyid");
    private static final String TAG 		= DbSQL.getSQL("tag.field.tag");

    public SqlTableTag() {
        
    }
        
    @Override
    protected void reconnect(Connection conn) throws DatabaseException {
        try {
			if (!DbUtils.hasTable(conn, TABLE_NAME)) {
			    // Need to create the table
			    DbUtils.executeAndClose(conn.prepareStatement(DbSQL.getSQL("tag.ps.createtable")));
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
    }
  
	/* (non-Javadoc)
	 * @see org.parosproxy.paros.db.paros.TableTag#read(long)
	 */
	@Override
	public synchronized RecordTag read(long tagId) throws DatabaseException {
    	SqlPreparedStatementWrapper psRead = null;
        try {
        	psRead = DbSQL.getSingleton().getPreparedStatement("tag.ps.read");
			psRead.getPs().setLong(1, tagId);
			
			try (ResultSet rs = psRead.getPs().executeQuery()) {
				RecordTag result = build(rs);
				return result;
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DbSQL.getSingleton().releasePreparedStatement(psRead);
		}
	}
	
    /* (non-Javadoc)
	 * @see org.parosproxy.paros.db.paros.TableTag#insert(long, java.lang.String)
	 */
    @Override
	public synchronized RecordTag insert(long historyId, String tag) throws DatabaseException {
    	SqlPreparedStatementWrapper psInsertTag = null;
        try {
        	psInsertTag = DbSQL.getSingleton().getPreparedStatement("tag.ps.insert");
			psInsertTag.getPs().setLong(1, historyId);
			psInsertTag.getPs().setString(2, tag);
			psInsertTag.getPs().executeUpdate();
			
			try (ResultSet rs = psInsertTag.getLastInsertedId()) {
				rs.next();
				long id = rs.getLong(1);
				return read(id);
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DbSQL.getSingleton().releasePreparedStatement(psInsertTag);
		}
    }
    
    /* (non-Javadoc)
	 * @see org.parosproxy.paros.db.paros.TableTag#delete(long, java.lang.String)
	 */
    @Override
	public synchronized void delete(long historyId, String tag) throws DatabaseException {
    	SqlPreparedStatementWrapper psDeleteTag = null;
        try {
        	psDeleteTag = DbSQL.getSingleton().getPreparedStatement("tag.ps.delete");
			psDeleteTag.getPs().setLong(1, historyId);
			psDeleteTag.getPs().setString(2, tag);
			psDeleteTag.getPs().executeUpdate();
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DbSQL.getSingleton().releasePreparedStatement(psDeleteTag);
		}
    }
    

    /* (non-Javadoc)
	 * @see org.parosproxy.paros.db.paros.TableTag#getTagsForHistoryID(long)
	 */
    @Override
	public List<RecordTag> getTagsForHistoryID (long historyId) throws DatabaseException {
    	SqlPreparedStatementWrapper psGetTagsForHistoryId = null;
        try {
        	psGetTagsForHistoryId = DbSQL.getSingleton().getPreparedStatement("tag.ps.gettagsforhid");
			List<RecordTag> result = new ArrayList<>();
			psGetTagsForHistoryId.getPs().setLong(1, historyId);
			try (ResultSet rs = psGetTagsForHistoryId.getPs().executeQuery()) {
				while (rs.next()) {
					result.add(new RecordTag(rs.getLong(TAGID), rs.getLong(TAGID), rs.getString(TAG)));
				}
			}
			
			return result;
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DbSQL.getSingleton().releasePreparedStatement(psGetTagsForHistoryId);
		}
    }
        
    /* (non-Javadoc)
	 * @see org.parosproxy.paros.db.paros.TableTag#getAllTags()
	 */
    @Override
	public List<String> getAllTags () throws DatabaseException {
    	SqlPreparedStatementWrapper psGetAllTags = null;
        try {
        	psGetAllTags = DbSQL.getSingleton().getPreparedStatement("tag.ps.getalltags");
			List<String> result = new ArrayList<>();
			try (ResultSet rs = psGetAllTags.getPs().executeQuery()) {
				while (rs.next()) {
					result.add(rs.getString(TAG));
				}
			}
			
			return result;
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DbSQL.getSingleton().releasePreparedStatement(psGetAllTags);
		}
    }
        
    /* (non-Javadoc)
	 * @see org.parosproxy.paros.db.paros.TableTag#deleteTagsForHistoryID(long)
	 */
    @Override
	public void deleteTagsForHistoryID (long historyId) throws DatabaseException {
    	SqlPreparedStatementWrapper psDeleteTagsForHistoryId = null;
        try {
        	psDeleteTagsForHistoryId = DbSQL.getSingleton().getPreparedStatement("tag.ps.deletetagsforhid");
			psDeleteTagsForHistoryId.getPs().setLong(1, historyId);
			psDeleteTagsForHistoryId.getPs().execute();
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			DbSQL.getSingleton().releasePreparedStatement(psDeleteTagsForHistoryId);
		}
    }
        
    private RecordTag build(ResultSet rs) throws SQLException {
        RecordTag rt = null;
        if (rs.next()) {
            rt = new RecordTag(rs.getLong(TAGID), rs.getLong(HISTORYID), rs.getString(TAG));            
        }
        return rt;
        
    }    
}
