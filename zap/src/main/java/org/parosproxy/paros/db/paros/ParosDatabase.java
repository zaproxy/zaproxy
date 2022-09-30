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
// ZAP: 2015/02/09 Issue 1525: Introduce a database interface layer to allow for alternative
// implementations
// ZAP: 2015/04/02 Issue 1582: Low memory option
// ZAP: 2016/02/10 Issue 1958: Allow to disable database (HSQLDB) log
// ZAP: 2016/04/22 Issue 2428: Memory leak on session creation/loading
// ZAP: 2016/05/24 Add implementation of Database.discardSession(long)
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2020/11/26 Use getLogger().
// ZAP: 2021/08/24 Remove the "(non-Javadoc)" comments.
// ZAP: 2021/09/27 Added support for Alert Tags.
// ZAP: 2022/09/21 Use format specifiers instead of concatenation when logging.
package org.parosproxy.paros.db.paros;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.parosproxy.paros.db.AbstractDatabase;
import org.parosproxy.paros.db.Database;
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
import org.parosproxy.paros.extension.option.DatabaseParam;
import org.zaproxy.zap.db.TableAlertTag;
import org.zaproxy.zap.db.paros.ParosTableAlertTag;

public class ParosDatabase extends AbstractDatabase {

    private ParosDatabaseServer databaseServer = null;
    private TableHistory tableHistory = null;
    private TableSession tableSession = null;
    private TableAlert tableAlert = null;
    private TableAlertTag tableAlertTag = null;
    private TableScan tableScan = null;
    // ZAP: Added TableTag
    private TableTag tableTag = null;
    // ZAP: Added TableSessionUrl.
    private TableSessionUrl tableSessionUrl = null;
    // ZAP: Added TableParam.
    private TableParam tableParam = null;
    private TableContext tableContext = null;
    private TableStructure tableStructure = null;

    /**
     * {@code DatabaseListener}s added internally when the {@code SqlDatabase} is constructed.
     *
     * <p>These listeners are kept during the lifetime of the database, while dynamically added
     * listeners are removed once the database is closed.
     */
    private List<DatabaseListener> internalDatabaseListeners = new ArrayList<>();

    private DatabaseParam databaseOptions;

    public ParosDatabase() {
        tableHistory = new ParosTableHistory();
        tableSession = new ParosTableSession();
        tableAlert = new ParosTableAlert();
        tableAlertTag = new ParosTableAlertTag();
        tableScan = new ParosTableScan();
        // ZAP: Added statement.
        tableTag = new ParosTableTag();
        // ZAP: Added statement.
        tableSessionUrl = new ParosTableSessionUrl();
        // ZAP: Added statement.
        tableParam = new ParosTableParam();
        tableContext = new ParosTableContext();
        tableStructure = new ParosTableStructure();

        internalDatabaseListeners.add(tableHistory);
        internalDatabaseListeners.add(tableSession);
        internalDatabaseListeners.add(tableAlert);
        internalDatabaseListeners.add(tableAlertTag);
        internalDatabaseListeners.add(tableScan);
        internalDatabaseListeners.add(tableTag);
        internalDatabaseListeners.add(tableSessionUrl);
        internalDatabaseListeners.add(tableParam);
        internalDatabaseListeners.add(tableContext);
        internalDatabaseListeners.add(tableStructure);
    }

    /** @return Returns the databaseServer */
    @Override
    public DatabaseServer getDatabaseServer() {
        return databaseServer;
    }

    /** @param databaseServer The databaseServer to set. */
    private void setDatabaseServer(ParosDatabaseServer databaseServer) {
        this.databaseServer = databaseServer;
    }

    @Override
    public TableHistory getTableHistory() {
        return tableHistory;
    }

    @Override
    public TableSession getTableSession() {
        return tableSession;
    }

    @Override
    public void open(String path) throws ClassNotFoundException, Exception {
        // ZAP: Added log statement.
        getLogger().debug("open {}", path);
        setDatabaseServer(new ParosDatabaseServer(path, databaseOptions));

        notifyListenersDatabaseOpen(internalDatabaseListeners, getDatabaseServer());
        notifyListenersDatabaseOpen(getDatabaseServer());
    }

    @Override
    public void deleteSession(String sessionName) {
        getLogger().debug("deleteSession {}", sessionName);
        if (databaseServer == null) {
            return;
        }
        try {
            databaseServer.shutdown(false);
        } catch (SQLException e) {
            getLogger().error(e.getMessage(), e);
        }

        deleteDbFile(new File(sessionName));
        deleteDbFile(new File(sessionName + ".data"));
        deleteDbFile(new File(sessionName + ".script"));
        deleteDbFile(new File(sessionName + ".properties"));
        deleteDbFile(new File(sessionName + ".backup"));
        deleteDbFile(new File(sessionName + ".lobs"));

        databaseServer = null;
    }

    private void deleteDbFile(File file) {
        getLogger().debug("Deleting {}", file.getAbsolutePath());
        if (file.exists()) {
            if (!file.delete()) {
                getLogger().error("Failed to delete {}", file.getAbsolutePath());
            }
        }
    }

    // ZAP: Added method. Note: any change made to this method must have the
    // ZAP comment as the content was moved from the paros method close(boolean).
    @Override
    public void close(boolean compact, boolean cleanup) {
        // ZAP: Added statement.
        getLogger().debug("close");
        if (databaseServer == null) return;

        super.close(compact, cleanup);

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
            getLogger().error(e.getMessage(), e);
        }
    }

    @Override
    public TableAlert getTableAlert() {
        return tableAlert;
    }

    @Override
    public void setTableAlert(TableAlert tableAlert) {
        this.tableAlert = tableAlert;
    }

    @Override
    public TableAlertTag getTableAlertTag() {
        return tableAlertTag;
    }

    @Override
    public void setTableAlertTag(TableAlertTag tableAlertTag) {
        this.tableAlertTag = tableAlertTag;
    }

    @Override
    public TableScan getTableScan() {
        return tableScan;
    }

    @Override
    public void setTableScan(TableScan tableScan) {
        this.tableScan = tableScan;
    }

    @Override
    public TableTag getTableTag() {
        return tableTag;
    }

    @Override
    public void setTableTag(TableTag tableTag) {
        this.tableTag = tableTag;
    }

    // ZAP: Added method.
    @Override
    public TableSessionUrl getTableSessionUrl() {
        return tableSessionUrl;
    }

    // ZAP: Added method.
    @Override
    public void setTableSessionUrl(TableSessionUrl tableSessionUrl) {
        this.tableSessionUrl = tableSessionUrl;
    }

    // ZAP: Added method.
    @Override
    public TableParam getTableParam() {
        return tableParam;
    }

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
     * @since 2.5.0
     */
    public void setDatabaseParam(DatabaseParam databaseOptions) {
        if (databaseOptions == null) {
            throw new IllegalArgumentException("Parameter databaseOptions must not be null.");
        }
        this.databaseOptions = databaseOptions;
    }

    @Override
    public void discardSession(long sessionId) throws DatabaseException {
        // Do nothing, the database files are going to be deleted anyway.
        // getTableHistory().deleteHistorySession(sessionId);
    }
}
