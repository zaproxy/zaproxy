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
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.security.InvalidParameterException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.db.Database;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.DatabaseListener;
import org.parosproxy.paros.db.DatabaseServer;
import org.parosproxy.paros.db.DatabaseUnsupportedException;
import org.zaproxy.zap.utils.Stats;

public class DbSQL implements DatabaseListener {

    private static Properties dbProperties = null;
    private static Properties sqlProperties = null;
    private static String dbType = null;

    private static DbSQL singleton = null;
    private static SqlDatabaseServer dbServer = null;

    private static final Logger logger = LogManager.getLogger(DbSQL.class);

    private Map<String, StatementPool> stmtPool = new HashMap<>();

    public static DbSQL getSingleton() {
        if (singleton == null) {
            singleton = new DbSQL();
        }
        return singleton;
    }

    protected String getDbUser() {
        if (dbProperties == null) {
            throw new IllegalStateException("Database not initialised");
        }
        return dbProperties.getProperty("db.user");
    }

    protected String getDbPassword() {
        if (dbProperties == null) {
            throw new IllegalStateException("Database not initialised");
        }
        return dbProperties.getProperty("db.password");
    }

    protected String getDbUrl() {
        if (dbProperties == null) {
            throw new IllegalStateException("Database not initialised");
        }
        return dbProperties.getProperty("db.url");
    }

    public static String getDbType() {
        return dbType;
    }

    public synchronized Database initDatabase() throws IllegalStateException, DatabaseException {
        if (dbProperties != null) {
            throw new IllegalStateException("Database already initialised");
        }
        File file = new File(Constant.getZapHome() + File.separator + "db", "db.properties");
        if (!file.exists()) {
            file = new File(Constant.getZapInstall() + File.separator + "db", "db.properties");
        }
        if (!file.exists()) {
            throw new DatabaseException("Missing DB properties file: " + file.getAbsolutePath());
        }

        dbProperties = new Properties();
        try (Reader reader = new FileReader(file)) {
            dbProperties.load(reader);
        } catch (IOException e) {
            throw new DatabaseException("I/O error while reading DB properties file.", e);
        }
        dbType = dbProperties.getProperty("db.type");

        // Load the SQL properties
        sqlProperties = new Properties();
        File sqlFile =
                new File(Constant.getZapInstall() + File.separator + "db", dbType + ".properties");
        try (Reader sqlReader = new FileReader(sqlFile)) {
            sqlProperties.load(sqlReader);
        } catch (Exception e) {
            logger.error("No SQL properties file for db type {}", sqlFile.getAbsolutePath());
            throw new DatabaseException(
                    "Missing SQL properties file: " + sqlFile.getAbsolutePath());
        }

        String className = dbProperties.getProperty("db.class");
        Object dbObj;
        try {
            Class<?> dbClass = Class.forName(className);
            dbObj = dbClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new DatabaseException("Failed to create the instance for: " + className, e);
        }

        if (!(dbObj instanceof Database)) {
            throw new DatabaseException(
                    "db.class is not an instance of Database: "
                            + dbObj.getClass().getCanonicalName());
        }
        return (Database) dbObj;
    }

    public static void addSqlProperties(InputStream inStream) throws IOException {
        sqlProperties.load(inStream);
    }

    public static String getSQL(String key) {
        String str = sqlProperties.getProperty(key);
        if (str != null) {
            // trailing spaces can cause havoc ;)
            str = str.trim();
        }
        return str;
    }

    public static String getSQL(String key, Object... params) {
        String str = MessageFormat.format(getSQL(key), params);
        if (str != null) {
            // trailing spaces can cause havoc ;)
            str = str.trim();
        }
        return str;
    }

    public static void setSetValues(PreparedStatement ps, int startPosition, String... values)
            throws SQLException {
        int i = startPosition;

        while (i < values.length) {
            ps.setString(i, values[i]);
            i++;
        }
    }

    public static void setSetValues(PreparedStatement ps, int startPosition, int... values)
            throws SQLException {
        int i = 0;

        while (i < values.length) {
            ps.setInt(startPosition + i, values[i]);
            i++;
        }
    }

    @Override
    public void databaseOpen(DatabaseServer dbServer)
            throws DatabaseException, DatabaseUnsupportedException {

        DbSQL.dbServer = (SqlDatabaseServer) dbServer;

        for (Entry<String, StatementPool> entry : stmtPool.entrySet()) {
            entry.getValue().clear();
        }
        Stats.clear("sqldb.");
    }

    public synchronized SqlPreparedStatementWrapper getPreparedStatement(String key, int... params)
            throws SQLException {
        if (params == null || params.length == 0) {
            return getPreparedStatement(key);
        }
        String internalKey = key + Arrays.toString(params);
        return getStatementPool(internalKey)
                .getPreparedStatement(internalKey, createSQL(key, params));
    }

    public synchronized SqlPreparedStatementWrapper getPreparedStatement(String key)
            throws SQLException {
        return getStatementPool(key).getPreparedStatement(key, getSQL(key));
    }

    private StatementPool getStatementPool(String key) {
        Stats.incCounter("sqldb." + key + ".calls");

        StatementPool sp = this.stmtPool.get(key);
        if (sp == null) {
            synchronized (this) {
                if (!this.stmtPool.containsKey(key)) {
                    this.stmtPool.put(key, new StatementPool());
                }
            }
            sp = this.stmtPool.get(key);
        }

        return sp;
    }

    public void releasePreparedStatement(SqlPreparedStatementWrapper psw) {
        if (psw != null) {
            Stats.incCounter("sqldb." + psw.getKey() + ".time", psw.getTimeTaken());
            this.stmtPool.get(psw.getKey()).releasePreparedStatement(psw);
        }
    }

    private class StatementPool {
        private static final int MAX_FREE_POOL_SIZE = 5;

        private Deque<PreparedStatement> inUsePool = new ConcurrentLinkedDeque<>();
        private Deque<PreparedStatement> freePool = new ConcurrentLinkedDeque<>();

        public SqlPreparedStatementWrapper getPreparedStatement(String key, String sql)
                throws SQLException {
            PreparedStatement ps = freePool.pollFirst();
            if (ps == null) {
                ps = dbServer.getNewConnection().prepareStatement(sql);
                Stats.incCounter("sqldb.conn.openned");
            }
            inUsePool.add(ps);
            Stats.setHighwaterMark("sqldb." + key + ".pool", inUsePool.size());
            return new SqlPreparedStatementWrapper(key, ps);
        }

        public void releasePreparedStatement(SqlPreparedStatementWrapper ps) {
            if (inUsePool.remove(ps.getPs())) {
                if (freePool.size() < MAX_FREE_POOL_SIZE) {
                    freePool.add(ps.getPs());
                } else {
                    try {
                        ps.close();
                        Stats.incCounter("sqldb.conn.closed");
                    } catch (SQLException e) {
                        logger.error("Error closing prepared statement", e);
                    }
                }
            } else {
                logger.error(
                        "Releasing prepared statement not in a pool",
                        new InvalidParameterException());
            }
        }

        public void clear() {
            Iterator<PreparedStatement> iter = inUsePool.iterator();
            while (iter.hasNext()) {
                try {
                    iter.next().close();
                } catch (SQLException e) {
                    // Ignore
                }
            }
            inUsePool.clear();
            iter = freePool.iterator();
            while (iter.hasNext()) {
                try {
                    iter.next().close();
                } catch (SQLException e) {
                    // Ignore
                }
            }
            freePool.clear();
        }
    }

    private static String createSQL(String key, int[] numberOfParams) {
        if (numberOfParams == null || numberOfParams.length == 0) {
            return getSQL(key);
        }

        Object[] parameters = new Object[numberOfParams.length];
        for (int i = 0; i < numberOfParams.length; i++) {
            parameters[i] = StringUtils.repeat("?", ", ", numberOfParams[i]);
        }
        return getSQL(key, parameters);
    }
}
