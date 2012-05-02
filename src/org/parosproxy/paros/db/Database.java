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
// removeDatabaseListener to DatabaseListener instead of SpiderListener.
// Removed unnecessary castings in the methods notifyListenerDatabaseOpen.
// ZAP: 2012/05/02 Added the method createSingleton and changed the method
// getSingleton to use it.

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
	private TableSessionUrl tableSessionUrl = null;
	private TableParam tableParam = null;
    private static Logger log = Logger.getLogger(Database.class);

	private Vector<DatabaseListener> listenerList = new Vector<DatabaseListener>();

	public Database() {
	    tableHistory = new TableHistory();
	    tableSession = new TableSession();
	    tableAlert = new TableAlert();
	    tableScan = new TableScan();
	    tableTag = new TableTag();
	    tableSessionUrl = new TableSessionUrl();
	    tableParam = new TableParam();
	    addDatabaseListener(tableHistory);
	    addDatabaseListener(tableSession);
	    addDatabaseListener(tableAlert);
	    addDatabaseListener(tableScan);
	    addDatabaseListener(tableTag);
	    addDatabaseListener(tableSessionUrl);
	    addDatabaseListener(tableParam);

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
	
	/**
	 * @param tableHistory The tableHistory to set.
	 */
	private void setTableHistory(TableHistory tableHistory) {
		this.tableHistory = tableHistory;
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
    
    /**
     * @param tableSession The tableSession to set.
     */
    private void setTableSession(TableSession tableSession) {
        this.tableSession = tableSession;
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
	
	public void removeDatabaseListener(DatabaseListener listener) {
		listenerList.remove(listener);
	}
	
	private void notifyListenerDatabaseOpen() throws SQLException {
	    DatabaseListener listener = null;
	    
	    for (int i=0;i<listenerList.size();i++) {
	        listener = listenerList.get(i);
	        listener.databaseOpen(getDatabaseServer());	        
	    }
	}

	public void open(String path) throws ClassNotFoundException, Exception {
		log.debug("open " + path);
	    setDatabaseServer(new DatabaseServer(path));
	    notifyListenerDatabaseOpen();
	}
	
	public void close(boolean compact) {
		log.debug("close");
	    if (databaseServer == null) return;
	    
	    try {
		    // perform clean up
	        getTableHistory().deleteTemporary();

	        // shutdown
	        getDatabaseServer().shutdown(compact);
        } catch (Exception e) {
            e.printStackTrace();
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

	public TableSessionUrl getTableSessionUrl() {
		return tableSessionUrl;
	}

	public void setTableSessionUrl(TableSessionUrl tableSessionUrl) {
		this.tableSessionUrl = tableSessionUrl;
	}

	public TableParam getTableParam() {
		return tableParam;
	}
	
}
