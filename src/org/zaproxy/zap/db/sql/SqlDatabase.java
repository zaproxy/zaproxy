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

import java.util.ArrayList;
import java.util.List;

import org.parosproxy.paros.db.AbstractDatabase;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.DatabaseListener;
import org.parosproxy.paros.db.DatabaseServer;
import org.parosproxy.paros.db.TableAlert;
import org.parosproxy.paros.db.TableContext;
import org.parosproxy.paros.db.TableHistory;
import org.parosproxy.paros.db.TableParam;
import org.parosproxy.paros.db.TableScan;
import org.parosproxy.paros.db.TableSession;
import org.parosproxy.paros.db.TableSessionUrl;
import org.parosproxy.paros.db.TableStructure;
import org.parosproxy.paros.db.TableTag;



public class SqlDatabase extends AbstractDatabase {
	
	private SqlDatabaseServer databaseServer = null;
	private TableHistory tableHistory = null;
	private TableSession tableSession = null;
	private TableAlert tableAlert = null;
	private TableScan tableScan = null;
	private TableTag tableTag = null;
	private TableSessionUrl tableSessionUrl = null;
	private TableParam tableParam = null;
	private TableContext tableContext = null;
	private TableStructure tableStructure = null;
	/**
	 * {@code DatabaseListener}s added internally when the {@code SqlDatabase} is constructed.
	 * <p>
	 * These listeners are kept during the lifetime of the database, while dynamically added listeners are removed once the
	 * database is closed.
	 */
	private final List<DatabaseListener> internalDatabaseListeners = new ArrayList<>();

	public SqlDatabase() {
		
	    tableAlert = new SqlTableAlert();
	    tableContext = new SqlTableContext();
	    tableHistory = new SqlTableHistory();
	    tableParam = new SqlTableParam();
	    tableScan = new SqlTableScan();
	    tableSession = new SqlTableSession();
	    tableSessionUrl = new SqlTableSessionUrl();
	    tableTag = new SqlTableTag();
	    tableStructure = new SqlTableStructure();

	    internalDatabaseListeners.add(DbSQL.getSingleton());
	    internalDatabaseListeners.add(tableHistory);
	    internalDatabaseListeners.add(tableSession);
	    internalDatabaseListeners.add(tableAlert);
	    internalDatabaseListeners.add(tableScan);
	    internalDatabaseListeners.add(tableTag);
	    internalDatabaseListeners.add(tableSessionUrl);
	    internalDatabaseListeners.add(tableParam);
	    internalDatabaseListeners.add(tableContext);
	    internalDatabaseListeners.add(tableStructure);

	}
	
	/**
	 * @return Returns the databaseServer
	 */
	@Override
	public DatabaseServer getDatabaseServer() {
		return databaseServer;
	}
	
	/**
	 * @param databaseServer The databaseServer to set.
	 */
	protected void setDatabaseServer(SqlDatabaseServer databaseServer) {
		this.databaseServer = databaseServer;
	}
		
	/* (non-Javadoc)
	 * @see org.parosproxy.paros.db.DatabaseIF#getTableHistory()
	 */
	@Override
	public TableHistory getTableHistory() {
		return tableHistory;		
	}

	
    /* (non-Javadoc)
	 * @see org.parosproxy.paros.db.DatabaseIF#getTableSession()
	 */
    @Override
	public TableSession getTableSession() {
        return tableSession;
    }
	
	/* (non-Javadoc)
	 * @see org.parosproxy.paros.db.DatabaseIF#open(java.lang.String)
	 */
	@Override
	public final void open(String path) throws Exception {
	    // ZAP: Added log statement.
		logger.debug("open " + path);
	    setDatabaseServer(createDatabaseServer(path));
	    notifyListenersDatabaseOpen(internalDatabaseListeners, getDatabaseServer());
	    notifyListenersDatabaseOpen(getDatabaseServer());
	}
	
	/**
	 * Creates the {@code SqlDatabaseServer} to be used when opening a database.
	 * <p>
	 * Extending classes can use this method to create a custom {@code SqlDatabaseServer} implementation.
	 *
	 * @param path the location of the database server
	 * @return a {@code SqlDatabaseServer} to be used by this database
	 * @throws Exception if an error occurred while creating the {@code SqlDatabaseServer}.
	 * @since 2.5.0
	 * @see #open(String)
	 */
	protected SqlDatabaseServer createDatabaseServer(String path) throws Exception {
		return new SqlDatabaseServer(path);
	}

    /* (non-Javadoc)
	 * @see org.parosproxy.paros.db.DatabaseIF#deleteSession(java.lang.String)
	 */
    @Override
	public void deleteSession(String sessionName) {
		logger.debug("deleteSession " + sessionName);
	    if (databaseServer == null) {
	    	return;
	    }
        databaseServer = null;
    }
    
    /* (non-Javadoc)
	 * @see org.parosproxy.paros.db.DatabaseIF#close(boolean, boolean)
	 */
	@Override
	public void close(boolean compact, boolean cleanup) {
		// ZAP: Added statement.
		logger.debug("close");
	    if (databaseServer == null) {
	    	return;
	    }

	    super.close(compact, cleanup);
	    
	    try {
	        // ZAP: Added if block.
	        if (cleanup) {
    		    // perform clean up
    	        getTableHistory().deleteTemporary();
	        }
        } catch (Exception e) {
	        // ZAP: Changed to log the exception.
            logger.error(e.getMessage(), e);
        }
	}
	
	public boolean isFileBased () {
		return false;
	}
	
    /* (non-Javadoc)
	 * @see org.parosproxy.paros.db.DatabaseIF#getTableAlert()
	 */
    @Override
	public TableAlert getTableAlert() {
        return tableAlert;
    }
    /* (non-Javadoc)
	 * @see org.parosproxy.paros.db.DatabaseIF#setTableAlert(org.parosproxy.paros.db.TableAlert)
	 */
    @Override
	public void setTableAlert(TableAlert tableAlert) {
        this.tableAlert = tableAlert;
    }
    /* (non-Javadoc)
	 * @see org.parosproxy.paros.db.DatabaseIF#getTableScan()
	 */
    @Override
	public TableScan getTableScan() {
        return tableScan;
    }
    /* (non-Javadoc)
	 * @see org.parosproxy.paros.db.DatabaseIF#setTableScan(org.parosproxy.paros.db.TableScan)
	 */
    @Override
	public void setTableScan(TableScan tableScan) {
        this.tableScan = tableScan;
    }

	/* (non-Javadoc)
	 * @see org.parosproxy.paros.db.DatabaseIF#getTableTag()
	 */
	@Override
	public TableTag getTableTag() {
		return tableTag;
	}

	/* (non-Javadoc)
	 * @see org.parosproxy.paros.db.DatabaseIF#setTableTag(org.parosproxy.paros.db.TableTag)
	 */
	@Override
	public void setTableTag(TableTag tableTag) {
		this.tableTag = tableTag;
	}

	// ZAP: Added method.
	/* (non-Javadoc)
	 * @see org.parosproxy.paros.db.DatabaseIF#getTableSessionUrl()
	 */
	@Override
	public TableSessionUrl getTableSessionUrl() {
		return tableSessionUrl;
	}

	// ZAP: Added method.
	/* (non-Javadoc)
	 * @see org.parosproxy.paros.db.DatabaseIF#setTableSessionUrl(org.parosproxy.paros.db.TableSessionUrl)
	 */
	@Override
	public void setTableSessionUrl(TableSessionUrl tableSessionUrl) {
		this.tableSessionUrl = tableSessionUrl;
	}

	// ZAP: Added method.
	/* (non-Javadoc)
	 * @see org.parosproxy.paros.db.DatabaseIF#getTableParam()
	 */
	@Override
	public TableParam getTableParam() {
		return tableParam;
	}

	/* (non-Javadoc)
	 * @see org.parosproxy.paros.db.DatabaseIF#getTableContext()
	 */
	@Override
	public TableContext getTableContext() {
		return tableContext;
	}

	@Override
	public TableStructure getTableStructure() {
		return tableStructure;
	}
	
	@Override
	public String getType() {
		return DbSQL.getDbType();
	}

	@Override
	public void discardSession(long sessionId) throws DatabaseException {
		if (!isFileBased()) {
			getTableHistory().deleteHistorySession(sessionId);
		}
	}
}
