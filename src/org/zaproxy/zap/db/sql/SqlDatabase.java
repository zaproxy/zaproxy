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

import java.util.Vector;

import org.apache.log4j.Logger;
import org.parosproxy.paros.db.Database;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.DatabaseListener;
import org.parosproxy.paros.db.DatabaseServer;
import org.parosproxy.paros.db.DatabaseUnsupportedException;
import org.parosproxy.paros.db.TableAlert;
import org.parosproxy.paros.db.TableContext;
import org.parosproxy.paros.db.TableHistory;
import org.parosproxy.paros.db.TableParam;
import org.parosproxy.paros.db.TableScan;
import org.parosproxy.paros.db.TableSession;
import org.parosproxy.paros.db.TableSessionUrl;
import org.parosproxy.paros.db.TableStructure;
import org.parosproxy.paros.db.TableTag;



public class SqlDatabase implements Database {
	
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
    private static final Logger log = Logger.getLogger(SqlDatabase.class);
	private Vector<DatabaseListener> listenerList = new Vector<>();
	private String type = null;

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

	    addDatabaseListener(DbSQL.getSingleton());
	    addDatabaseListener(tableHistory);
	    addDatabaseListener(tableSession);
	    addDatabaseListener(tableAlert);
	    addDatabaseListener(tableScan);
	    addDatabaseListener(tableTag);
	    addDatabaseListener(tableSessionUrl);
	    addDatabaseListener(tableParam);
	    addDatabaseListener(tableContext);
	    addDatabaseListener(tableStructure);

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
	 * @see org.parosproxy.paros.db.DatabaseIF#addDatabaseListener(org.parosproxy.paros.db.DatabaseListener)
	 */
	@Override
	public void addDatabaseListener(DatabaseListener listener) {
		listenerList.add(listener);
		
	}
	
	// ZAP: Changed parameter's type from SpiderListener to DatabaseListener.
	/* (non-Javadoc)
	 * @see org.parosproxy.paros.db.DatabaseIF#removeDatabaseListener(org.parosproxy.paros.db.DatabaseListener)
	 */
	@Override
	public void removeDatabaseListener(DatabaseListener listener) {
		listenerList.remove(listener);
	}
	
	protected void notifyListenerDatabaseOpen() throws DatabaseException {
	    DatabaseListener listener = null;
	    
	    for (int i=0;i<listenerList.size();i++) {
	        // ZAP: Removed unnecessary cast.
	        listener = listenerList.get(i);
	        try {
				listener.databaseOpen(getDatabaseServer());
			} catch (DatabaseUnsupportedException e) {
				log.error(e.getMessage(), e);
			}
	    }
	}

	/* (non-Javadoc)
	 * @see org.parosproxy.paros.db.DatabaseIF#open(java.lang.String)
	 */
	@Override
	public void open(String path) throws ClassNotFoundException, Exception {
	    // ZAP: Added log statement.
		log.debug("open " + path);
	    setDatabaseServer(new SqlDatabaseServer(path));
	    notifyListenerDatabaseOpen();
	}
	
    /* (non-Javadoc)
	 * @see org.parosproxy.paros.db.DatabaseIF#close(boolean)
	 */
    // ZAP: Added JavaDoc.
    @Override
	public void close(boolean compact) {
        // ZAP: Moved the content of this method to the method close(boolean,
        // boolean) and changed to call that method instead.
        close(compact, true);
    }

    /* (non-Javadoc)
	 * @see org.parosproxy.paros.db.DatabaseIF#deleteSession(java.lang.String)
	 */
    @Override
	public void deleteSession(String sessionName) {
		log.debug("deleteSession " + sessionName);
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
		log.debug("close");
	    if (databaseServer == null) {
	    	return;
	    }
	    
	    try {
	        // ZAP: Added if block.
	        if (cleanup) {
    		    // perform clean up
    	        getTableHistory().deleteTemporary();
	        }
        } catch (Exception e) {
	        // ZAP: Changed to log the exception.
            log.error(e.getMessage(), e);
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
		return type;
	}

}
