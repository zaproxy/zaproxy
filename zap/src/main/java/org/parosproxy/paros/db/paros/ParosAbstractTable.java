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
// ZAP: 2012/04/23 Added @Override annotation to the appropriate method.
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2013/05/02 Re-arranged all modifiers into Java coding standard order
// ZAP: 2015/02/09 Issue 1525: Introduce a database interface layer to allow for alternative
// implementations
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
package org.parosproxy.paros.db.paros;

import java.sql.Connection;
import java.sql.SQLException;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.DatabaseListener;
import org.parosproxy.paros.db.DatabaseServer;
import org.parosproxy.paros.db.DatabaseUnsupportedException;

public abstract class ParosAbstractTable implements DatabaseListener {

    private Connection connection = null;
    private ParosDatabaseServer server = null;

    public ParosAbstractTable() {}

    @Override
    public void databaseOpen(DatabaseServer server)
            throws DatabaseException, DatabaseUnsupportedException {
        if (!(server instanceof ParosDatabaseServer)) {
            throw new DatabaseUnsupportedException();
        }
        this.server = (ParosDatabaseServer) server;
        connection = null;
        reconnect(getConnection());
    }

    protected Connection getConnection() throws DatabaseException {
        try {
            if (connection == null) {
                connection = server.getNewConnection();
            }
            return connection;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    protected abstract void reconnect(Connection connection) throws DatabaseException;
}
