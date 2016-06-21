/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2016 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.parosproxy.paros.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * An abstract implementation of {@link Database}, it allows to manage {@link DatabaseListener DatabaseListeners}.
 * 
 * @since 2.5.0
 */
public abstract class AbstractDatabase implements Database {

    protected final Logger logger = Logger.getLogger(getClass());

    private final List<DatabaseListener> databaseListeners;

    public AbstractDatabase() {
        databaseListeners = new ArrayList<>();
    }

    /**
     * Gets the database listeners added.
     *
     * @return the database listeners
     * @see #addDatabaseListener(DatabaseListener)
     */
    protected List<DatabaseListener> getDatabaseListeners() {
        return databaseListeners;
    }

    @Override
    public void addDatabaseListener(DatabaseListener listener) {
        databaseListeners.add(listener);
    }

    @Override
    public void removeDatabaseListener(DatabaseListener listener) {
        databaseListeners.remove(listener);
    }

    @Override
    public void close(boolean compact) {
        close(compact, true);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Also, removes the database listeners added previously.
     */
    @Override
    public void close(boolean compact, boolean cleanup) {
        removeDatabaseListeners();
    }

    /**
     * Removes all database listeners added.
     * 
     * @see #removeDatabaseListener(DatabaseListener)
     */
    protected void removeDatabaseListeners() {
        databaseListeners.clear();
    }

    /**
     * Notifies the given listeners that the given database server was opened.
     *
     * @param listeners the listeners that will be notified
     * @param databaseServer the database server that was opened
     * @throws DatabaseException if an error occurred while notifying the database listeners.
     */
    protected void notifyListenersDatabaseOpen(Collection<DatabaseListener> listeners, DatabaseServer databaseServer)
            throws DatabaseException {
        for (DatabaseListener databaseListener : listeners) {
            try {
                databaseListener.databaseOpen(databaseServer);
            } catch (DatabaseUnsupportedException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Notifies the database listeners added that the given database server was opened.
     *
     * @param databaseServer the database server that was opened
     * @throws DatabaseException if an error occurred while notifying the database listeners.
     */
    protected void notifyListenersDatabaseOpen(DatabaseServer databaseServer) throws DatabaseException {
        notifyListenersDatabaseOpen(databaseListeners, databaseServer);
    }
}
