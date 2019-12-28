/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2019 The ZAP Development Team
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
import org.parosproxy.paros.db.RecordAlertMetadata;
import org.parosproxy.paros.db.TableAlertMetadata;

public class SqlTableAlertMetadata extends SqlAbstractTable implements TableAlertMetadata {

    private static final String TABLE_NAME = DbSQL.getSQL("alert.metadata.table.name");

    private static final String ALERTID = DbSQL.getSQL("alert.metadata.field.alertid");
    private static final String TYPE = DbSQL.getSQL("alert.metadata.field.type");
    private static final String DATA = DbSQL.getSQL("alert.metadata.field.data");

    public SqlTableAlertMetadata() {}

    @Override
    protected void reconnect(Connection conn) throws DatabaseException {
        try {
            if (!DbUtils.hasTable(conn, TABLE_NAME)) {
                // Need to create the table
                DbUtils.execute(conn, DbSQL.getSQL("alert.metadata.ps.createtable"));
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    private RecordAlertMetadata build(ResultSet rs) throws SQLException {
        RecordAlertMetadata ram = null;
        if (rs.next()) {
            ram =
                    new RecordAlertMetadata(
                            rs.getLong(ALERTID), rs.getString(TYPE), rs.getString(DATA));
        }
        return ram;
    }

    @Override
    public synchronized RecordAlertMetadata insert(long alertId, String type, String data)
            throws DatabaseException {
        SqlPreparedStatementWrapper psInsertAlertMeatadata = null;
        try {
            psInsertAlertMeatadata =
                    DbSQL.getSingleton().getPreparedStatement("alert.metadata.ps.insert");
            psInsertAlertMeatadata.getPs().setLong(1, alertId);
            psInsertAlertMeatadata.getPs().setString(2, type);
            psInsertAlertMeatadata.getPs().setString(3, data);
            psInsertAlertMeatadata.getPs().executeUpdate();

            try (ResultSet rs = psInsertAlertMeatadata.getLastInsertedId()) {
                rs.next();
                long id = rs.getLong(1);
                return read(id);
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbSQL.getSingleton().releasePreparedStatement(psInsertAlertMeatadata);
        }
    }

    @Override
    public synchronized void delete(long alertId) throws DatabaseException {
        SqlPreparedStatementWrapper psDeleteAlertMetadata = null;
        try {
            psDeleteAlertMetadata =
                    DbSQL.getSingleton().getPreparedStatement("alert.metadata.ps.delete");
            psDeleteAlertMetadata.getPs().setLong(1, alertId);
            psDeleteAlertMetadata.getPs().executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbSQL.getSingleton().releasePreparedStatement(psDeleteAlertMetadata);
        }
    }

    @Override
    public synchronized RecordAlertMetadata read(long alertId) throws DatabaseException {
        SqlPreparedStatementWrapper psRead = null;
        try {
            psRead =
                    DbSQL.getSingleton()
                            .getPreparedStatement("alert.metadata.ps.getmetadataforalertid");
            psRead.getPs().setLong(1, alertId);

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
    public synchronized void deleteMetadataOfTypeForAlertId(long alertId, String type)
            throws DatabaseException {
        SqlPreparedStatementWrapper psDeleteMeatadataOfTypeForAlertId = null;
        try {
            psDeleteMeatadataOfTypeForAlertId =
                    DbSQL.getSingleton()
                            .getPreparedStatement(
                                    "alert.metadata.ps.deletemetadataoftypeforalertid");
            psDeleteMeatadataOfTypeForAlertId.getPs().setLong(1, alertId);
            psDeleteMeatadataOfTypeForAlertId.getPs().setString(2, type);
            psDeleteMeatadataOfTypeForAlertId.getPs().execute();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbSQL.getSingleton().releasePreparedStatement(psDeleteMeatadataOfTypeForAlertId);
        }
    }

    @Override
    public synchronized List<RecordAlertMetadata> getMetadataForAlertId(long alertId)
            throws DatabaseException {
        SqlPreparedStatementWrapper psGetMetadataForAlertId = null;
        try {
            psGetMetadataForAlertId =
                    DbSQL.getSingleton()
                            .getPreparedStatement("alert.metadata.ps.getmetadataforalertid");
            List<RecordAlertMetadata> result = new ArrayList<>();
            psGetMetadataForAlertId.getPs().setLong(1, alertId);
            try (ResultSet rs = psGetMetadataForAlertId.getPs().executeQuery()) {
                while (rs.next()) {
                    result.add(
                            new RecordAlertMetadata(
                                    rs.getLong(ALERTID), rs.getString(TYPE), rs.getString(DATA)));
                }
            }

            return result;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbSQL.getSingleton().releasePreparedStatement(psGetMetadataForAlertId);
        }
    }

    @Override
    public List<RecordAlertMetadata> getMetadataOfTypeForAlertId(long alertId, String type)
            throws DatabaseException {
        SqlPreparedStatementWrapper psGetMetadataOfTypeForAlertId = null;
        try {
            psGetMetadataOfTypeForAlertId =
                    DbSQL.getSingleton()
                            .getPreparedStatement("alert.metadata.ps.getmetdataoftypeforalertid");
            List<RecordAlertMetadata> result = new ArrayList<>();
            psGetMetadataOfTypeForAlertId.getPs().setLong(1, alertId);
            psGetMetadataOfTypeForAlertId.getPs().setString(2, type);
            try (ResultSet rs = psGetMetadataOfTypeForAlertId.getPs().executeQuery()) {
                while (rs.next()) {
                    result.add(
                            new RecordAlertMetadata(
                                    rs.getLong(ALERTID), rs.getString(TYPE), rs.getString(DATA)));
                }
            }

            return result;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbSQL.getSingleton().releasePreparedStatement(psGetMetadataOfTypeForAlertId);
        }
    }
}
