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

package org.parosproxy.paros.db;

import java.sql.SQLException;
import java.util.Vector;

import org.apache.log4j.Logger;



/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Database {
	

	
	private static Database database = null;
	
	private DatabaseServer databaseServer = null;
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
	// ZAP: Added TableWebSocket.
	private TableContext tableContext = null;
	// ZAP: Added Logger.
    private static final Logger log = Logger.getLogger(Database.class);

	// ZAP: Added type arguments.
	private Vector<DatabaseListener> listenerList = new Vector<>();

	public Database() {
	    tableHistory = new TableHistory();
	    tableSession = new TableSession();
	    tableAlert = new TableAlert();
	    tableScan = new TableScan();
	    // ZAP: Added statement.
	    tableTag = new TableTag();
	    // ZAP: Added statement.
	    tableSessionUrl = new TableSessionUrl();
	    // ZAP: Added statement.
	    tableParam = new TableParam();
	    tableContext = new TableContext();
	    
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

	}
	
	/**
	 * @return Returns the databaseServer.
	 */
	public DatabaseServer getDatabaseServer() {
		return databaseServer;
	}
	
	/**
	 * @param databaseServer The databaseServer to set.
	 */
	private void setDatabaseServer(DatabaseServer databaseServer) {
		this.databaseServer = databaseServer;
	}
		
	public TableHistory getTableHistory() {
		return tableHistory;		
	}

	
    /**
     * @return Returns the tableSession.
     */
    public TableSession getTableSession() {
        return tableSession;
    }
    
    public static Database getSingleton() {
        if (database == null) {
            // ZAP: Changed to use the method createSingleton().
            createSingleton();
        }
        
        return database;
    }
    
    // ZAP: Added method.
    private static synchronized void createSingleton() {
        if (database == null) {
            database = new Database();
        }
    }
    
	public void addDatabaseListener(DatabaseListener listener) {
		listenerList.add(listener);
		
	}
	
	// ZAP: Changed parameter's type from SpiderListener to DatabaseListener.
	public void removeDatabaseListener(DatabaseListener listener) {
		listenerList.remove(listener);
	}
	
	private void notifyListenerDatabaseOpen() throws SQLException {
	    DatabaseListener listener = null;
	    
	    for (int i=0;i<listenerList.size();i++) {
	        // ZAP: Removed unnecessary cast.
	        listener = listenerList.get(i);
	        listener.databaseOpen(getDatabaseServer());
	    }
	}

	public void open(String path) throws ClassNotFoundException, Exception {
	    // ZAP: Added log statement.
		log.debug("open " + path);
	    setDatabaseServer(new DatabaseServer(path));
	    notifyListenerDatabaseOpen();
	}
	
    /**
     * Closes the database. If the parameter {@code compact} is {@code true},
     * the database will be compacted, compacting the database ensures a minimal
     * space disk usage but it will also take longer to close. Any necessary
     * cleanups are performed prior to closing the database (the cleanup removes
     * the temporary {@code HistoryRefernece}s).
     * 
     * <p>
     * <b>Note:</b> Calling this method has the same effects as calling the
     * method {@link #close(boolean, boolean)} with the parameter
     * {@code cleanup} as {@code true}.
     * </p>
     * 
     * @param compact
     *            {@code true} if the database should be compacted,
     *            {@code false} otherwise.
     * @see org.parosproxy.paros.model.HistoryReference
     */
    // ZAP: Added JavaDoc.
    public void close(boolean compact) {
        // ZAP: Moved the content of this method to the method close(boolean,
        // boolean) and changed to call that method instead.
        close(compact, true);
    }

    /**
     * Closes the database. If the parameter {@code compact} is {@code true},
     * the database will be compacted, compacting the database ensures a minimal
     * space disk usage but it will also take longer to close. If the parameter
     * {@code cleanup} is {@code true} any necessary cleanups are performed
     * prior to closing the database (the cleanup removes the temporary
     * {@code HistoryRefernece}s.)
     * 
     * @param compact
     *            {@code true} if the database should be compacted,
     *            {@code false} otherwise.
     * @param cleanup
     *            {@code true} if any necessary cleanups should be performed,
     *            {@code false} otherwise.
     * @see org.parosproxy.paros.model.HistoryReference
     */
    // ZAP: Added method. Note: any change made to this method must have the
    // ZAP comment as the content was moved from the paros method close(boolean).
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
	        getDatabaseServer().shutdown(compact);
	        // ZAP: Changed to catch SQLException instead of Exception.
        } catch (SQLException e) {
	        // ZAP: Changed to log the exception.
            log.error(e.getMessage(), e);
        }
	}
	
    
    /**
     * @return Returns the tableAlert.
     */
    public TableAlert getTableAlert() {
        return tableAlert;
    }
    /**
     * @param tableAlert The tableAlert to set.
     */
    public void setTableAlert(TableAlert tableAlert) {
        this.tableAlert = tableAlert;
    }
    /**
     * @return Returns the tableScan.
     */
    public TableScan getTableScan() {
        return tableScan;
    }
    /**
     * @param tableScan The tableScan to set.
     */
    public void setTableScan(TableScan tableScan) {
        this.tableScan = tableScan;
    }

	public TableTag getTableTag() {
		return tableTag;
	}

	public void setTableTag(TableTag tableTag) {
		this.tableTag = tableTag;
	}

	// ZAP: Added method.
	public TableSessionUrl getTableSessionUrl() {
		return tableSessionUrl;
	}

	// ZAP: Added method.
	public void setTableSessionUrl(TableSessionUrl tableSessionUrl) {
		this.tableSessionUrl = tableSessionUrl;
	}

	// ZAP: Added method.
	public TableParam getTableParam() {
		return tableParam;
	}

	public TableContext getTableContext() {
		return tableContext;
	}
	
	
	
}
