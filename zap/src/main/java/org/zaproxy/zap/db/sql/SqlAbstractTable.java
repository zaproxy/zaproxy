/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2015 The ZAP Development Team
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
package org.zaproxy.zap.db.sql;

import java.sql.Connection;
import java.sql.SQLException;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.DatabaseListener;
import org.parosproxy.paros.db.DatabaseServer;
import org.parosproxy.paros.db.DatabaseUnsupportedException;

public abstract class SqlAbstractTable implements DatabaseListener {

    private Connection connection = null;
    private SqlDatabaseServer sqlServer = null;

    public SqlAbstractTable() {}

    @Override
    public void databaseOpen(DatabaseServer server)
            throws DatabaseException, DatabaseUnsupportedException {
        if (server instanceof SqlDatabaseServer) {
            this.sqlServer = (SqlDatabaseServer) server;
        } else {
            throw new DatabaseUnsupportedException();
        }
        connection = null;
        reconnect(getConnection());
    }

    protected Connection getConnection() throws DatabaseException {
        try {
            if (connection == null) {
                connection = sqlServer.getNewConnection();
            }
            return connection;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    protected abstract void reconnect(Connection connection) throws DatabaseException;
}
