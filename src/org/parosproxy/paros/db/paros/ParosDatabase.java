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
// ZAP: 2011/05/15 Support for exclusions
// ZAP: 2011/10/29 Support for parameters
// ZAP: 2012/03/15 Changed the parameter's type of the method 
// removeDatabaseListener to DatabaseListener instead of SpiderListener. Removed
// unnecessary cast in the method notifyListenerDatabaseOpen.
// ZAP: 2012/05/02 Added the method createSingleton and changed the method
// getSingleton to use it.
// ZAP: 2012/06/11 Added JavaDoc to the method close(boolean), changed the 
// method close(boolean) to call the method close(boolean, boolean), added 
// method close(boolean, boolean).
// ZAP: 2012/07/16 Removed unused setters.
// ZAP: 2012/10/02 Issue 385: Added support for Contexts
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2015/02/05 Issue 1524: New Persist Session dialog
// ZAP: 2015/02/09 Issue 1525: Introduce a database interface layer to allow for alternative implementations
// ZAP: 2015/04/02 Issue 1582: Low memory option
// ZAP: 2016/02/10 Issue 1958: Allow to disable database (HSQLDB) log

package org.parosproxy.paros.db.paros;

import java.io.File;
import java.sql.SQLException;
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
import org.parosproxy.paros.extension.option.DatabaseParam;



public class ParosDatabase implements Database {
	
	private ParosDatabaseServer databaseServer = null;
	private TableHistory tableHistory = null;
	private TableSession tableSession = null;
	private TableAlert tableAlert = null;
	private TableScan tableScan = null;
	// ZAP: Added TableTag
	private TableTag tableTag = null;
	// ZAP: Added TableSessionUrl.
	private TableSessionUrl tableSessionUrl = null;
	// ZAP: Added TableParam.
	private TableParam tableParam = null;
	private TableContext tableContext = null;
	private TableStructure tableStructure = null;
	// ZAP: Added Logger.
    private static final Logger log = Logger.getLogger(ParosDatabase.class);

	// ZAP: Added type arguments.
	private Vector<DatabaseListener> listenerList = new Vector<>();

	private DatabaseParam databaseOptions;

	public ParosDatabase() {
	    tableHistory = new ParosTableHistory();
	    tableSession = new ParosTableSession();
	    tableAlert = new ParosTableAlert();
	    tableScan = new ParosTableScan();
	    // ZAP: Added statement.
	    tableTag = new ParosTableTag();
	    // ZAP: Added statement.
	    tableSessionUrl = new ParosTableSessionUrl();
	    // ZAP: Added statement.
	    tableParam = new ParosTableParam();
	    tableContext = new ParosTableContext();
	    tableStructure = new ParosTableStructure();
	    
	    addDatabaseListener(tableHistory);
	    addDatabaseListener(tableSession);
	    addDatabaseListener(tableAlert);
	    addDatabaseListener(tableScan);
	    // ZAP: Added statement.
	    addDatabaseListener(tableTag);
	    // ZAP: Added statement.
	    addDatabaseListener(tableSessionUrl);
	    // ZAP: Added statement.
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
	private void setDatabaseServer(ParosDatabaseServer databaseServer) {
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
	
	private void notifyListenerDatabaseOpen() throws DatabaseException {
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
	    setDatabaseServer(new ParosDatabaseServer(path, databaseOptions));
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
        try {
			databaseServer.shutdown(false);
		} catch (SQLException e) {
            log.error(e.getMessage(), e);
		}
		
        deleteDbFile(new File(sessionName));
        deleteDbFile(new File(sessionName + ".data"));
        deleteDbFile(new File(sessionName + ".script"));
        deleteDbFile(new File(sessionName + ".properties"));
        deleteDbFile(new File(sessionName + ".backup"));
        deleteDbFile(new File(sessionName + ".lobs"));
        
        databaseServer = null;
    }
    
    private void deleteDbFile (File file) {
    	log.debug("Deleting " + file.getAbsolutePath());
		if (file.exists()) {
			if (! file.delete()) {
	            log.error("Failed to delete " + file.getAbsolutePath());
			}
		}
    }

    /* (non-Javadoc)
	 * @see org.parosproxy.paros.db.DatabaseIF#close(boolean, boolean)
	 */
    // ZAP: Added method. Note: any change made to this method must have the
    // ZAP comment as the content was moved from the paros method close(boolean).
	@Override
	public void close(boolean compact, boolean cleanup) {
		// ZAP: Added statement.
		log.debug("close");
	    if (databaseServer == null) return;
	    
	    try {
	        // ZAP: Added if block.
	        if (cleanup) {
    		    // perform clean up
    	        getTableHistory().deleteTemporary();
	        }

	        // shutdown
	        databaseServer.shutdown(compact);
	        // ZAP: Changed to catch SQLException instead of Exception.
        } catch (Exception e) {
	        // ZAP: Changed to log the exception.
            log.error(e.getMessage(), e);
        }
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
		return Database.DB_TYPE_HSQLDB;
	}

	/**
	 * Sets the object that holds the database options.
	 *
	 * @param databaseOptions the object that holds the database options, must not be {@code null}
	 * @throws IllegalArgumentException if the given parameter is {@code null}.
	 * @since TODO add version
	 */
	public void setDatabaseParam(DatabaseParam databaseOptions) {
		if (databaseOptions == null) {
			throw new IllegalArgumentException("Parameter databaseOptions must not be null.");
		}
		this.databaseOptions = databaseOptions;
	}
}
