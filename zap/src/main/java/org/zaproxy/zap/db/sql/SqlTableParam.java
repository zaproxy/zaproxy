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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.DbUtils;
import org.parosproxy.paros.db.RecordParam;
import org.parosproxy.paros.db.TableParam;

public class SqlTableParam extends SqlAbstractTable implements TableParam {

    private static final String TABLE_NAME = DbSQL.getSQL("param.table_name");

    private static final String PARAMID = DbSQL.getSQL("param.field.paramid");
    private static final String SITE = DbSQL.getSQL("param.field.site");
    private static final String TYPE = DbSQL.getSQL("param.field.type");
    private static final String NAME = DbSQL.getSQL("param.field.name");
    private static final String USED = DbSQL.getSQL("param.field.used");
    private static final String FLAGS = DbSQL.getSQL("param.field.flags");
    private static final String VALUES = DbSQL.getSQL("param.field.vals");

    public SqlTableParam() {}

    @Override
    protected void reconnect(Connection conn) throws DatabaseException {
        try {
            if (!DbUtils.hasTable(conn, TABLE_NAME)) {
                // Need to create the table
                DbUtils.execute(conn, DbSQL.getSQL("param.ps.addtable"));
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public synchronized RecordParam read(long urlId) throws DatabaseException {
        SqlPreparedStatementWrapper psRead = null;
        try {
            psRead = DbSQL.getSingleton().getPreparedStatement("param.ps.read");
            psRead.getPs().setLong(1, urlId);

            try (ResultSet rs = psRead.getPs().executeQuery()) {
                return build(rs);
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbSQL.getSingleton().releasePreparedStatement(psRead);
        }
    }

    @Override
    public List<RecordParam> getAll() throws DatabaseException {
        SqlPreparedStatementWrapper psGetAll = null;
        try {
            psGetAll = DbSQL.getSingleton().getPreparedStatement("param.ps.getall");
            List<RecordParam> result = new ArrayList<>();
            try (ResultSet rs = psGetAll.getPs().executeQuery()) {
                while (rs.next()) {
                    result.add(
                            new RecordParam(
                                    rs.getLong(PARAMID),
                                    rs.getString(SITE),
                                    rs.getString(TYPE),
                                    rs.getString(NAME),
                                    rs.getInt(USED),
                                    rs.getString(FLAGS),
                                    rs.getString(VALUES)));
                }
            }

            return result;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbSQL.getSingleton().releasePreparedStatement(psGetAll);
        }
    }

    @Override
    public synchronized RecordParam insert(
            String site, String type, String name, int used, String flags, String values)
            throws DatabaseException {
        SqlPreparedStatementWrapper psInsert = null;
        try {
            psInsert = DbSQL.getSingleton().getPreparedStatement("param.ps.insert");
            psInsert.getPs().setString(1, site);
            psInsert.getPs().setString(2, type);
            psInsert.getPs().setString(3, name);
            psInsert.getPs().setInt(4, used);
            psInsert.getPs().setString(5, flags);
            psInsert.getPs().setString(6, values);
            psInsert.getPs().executeUpdate();

            long id;
            try (ResultSet rs = psInsert.getLastInsertedId()) {
                rs.next();
                id = rs.getLong(1);
            }
            return read(id);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbSQL.getSingleton().releasePreparedStatement(psInsert);
        }
    }

    @Override
    public synchronized void update(long paramId, int used, String flags, String values)
            throws DatabaseException {
        SqlPreparedStatementWrapper psUpdate = null;
        try {
            psUpdate = DbSQL.getSingleton().getPreparedStatement("param.ps.update");
            psUpdate.getPs().setInt(1, used);
            psUpdate.getPs().setString(2, flags);
            psUpdate.getPs().setString(3, values);
            psUpdate.getPs().setLong(4, paramId);
            psUpdate.getPs().executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbSQL.getSingleton().releasePreparedStatement(psUpdate);
        }
    }

    private RecordParam build(ResultSet rs) throws DatabaseException {
        try {
            RecordParam rt = null;
            if (rs.next()) {
                rt =
                        new RecordParam(
                                rs.getLong(PARAMID),
                                rs.getString(SITE),
                                rs.getString(TYPE),
                                rs.getString(NAME),
                                rs.getInt(USED),
                                rs.getString(FLAGS),
                                rs.getString(VALUES));
            }
            return rt;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }
}
