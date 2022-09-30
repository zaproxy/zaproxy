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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HsqldbDatabaseServer extends SqlDatabaseServer {

    public static final int DEFAULT_SERVER_PORT = 9001;

    private static final Logger logger = LogManager.getLogger(HsqldbDatabaseServer.class);

    public HsqldbDatabaseServer(String dbname) throws ClassNotFoundException, Exception {
        super(dbname);

        // hsqldb only accept '/' as path;
        dbname = dbname.replaceAll("\\\\", "/");

        // ZAP: Check if old database should be compacted
        boolean doCompact = false;
        File propsFile = new File(dbname + ".properties");
        if (propsFile.exists()) {
            Properties dbProps = new Properties();
            InputStream propsStream = null;
            try {
                propsStream = new FileInputStream(propsFile);
                dbProps.load(propsStream);
            } finally {
                try {
                    if (propsStream != null) {
                        propsStream.close();
                    }
                } catch (IOException e) {
                    logger.debug(e.getMessage(), e);
                }
            }
            String version = (String) dbProps.get("version");
            if (version.charAt(0) < '2') {
                // got version < 2.0.0
                // => SHUTDOWN COMPACT database
                // and reconnect again
                doCompact = true;
            }
        }

        this.setDbUrl("jdbc:hsqldb:file:" + dbname);
        this.setDbUser(DbSQL.getSingleton().getDbUser());
        this.setDbPassword(DbSQL.getSingleton().getDbPassword());

        // ZAP: If old database is in load => shutdown & reconnect
        if (doCompact) {
            shutdown(true);
        }
    }

    @Override
    void shutdown(boolean compact) throws SQLException {
        super.shutdown(compact);
        try (Connection conn = getNewConnection()) {
            String statement;

            if (compact) {
                // db is not new and useful for future.  Compact it.
                statement = "SHUTDOWN COMPACT";

            } else {
                // new need to compact database.  just shutdown.
                statement = "SHUTDOWN";
            }

            try (CallableStatement psCompact = conn.prepareCall(statement)) {
                psCompact.execute();
            }
        }
    }
}
