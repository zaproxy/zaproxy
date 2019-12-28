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
package org.parosproxy.paros.db.paros;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.DbUtils;
import org.parosproxy.paros.db.RecordAlertMetadata;
import org.parosproxy.paros.db.TableAlertMetadata;

public class ParosTableAlertMetadata extends ParosAbstractTable implements TableAlertMetadata {

    private static final String TABLE_NAME = "ALERTMETADATA";

    private static final String ALERTID = "ALERTID";
    private static final String TYPE = "TYPE";
    private static final String DATA = "DATA";

    private PreparedStatement psRead = null;
    private PreparedStatement psGetMetadataForAlertId = null;
    private PreparedStatement psGetMetadataOfTypeForAlertId = null;
    private PreparedStatement psInsert = null;
    private PreparedStatement psDeleteMetadataForAlertId = null;
    private PreparedStatement psDeleteMetadataOfTypeForAlertId = null;
    private CallableStatement psGetIdLastInsert = null;

    public ParosTableAlertMetadata() {}

    @Override
    protected void reconnect(Connection conn) throws DatabaseException {
        try {
            if (!DbUtils.hasTable(conn, TABLE_NAME)) {
                // Need to create the table
                DbUtils.execute(
                        conn,
                        "CREATE cached TABLE ALERT_METADATA (alertid bigint not null, type varchar(255) not null, data varchar(255) default '')");
            }
            psRead =
                    conn.prepareStatement("SELECT * FROM ALERT_METADATA WHERE " + ALERTID + " = ?");
            psGetMetadataOfTypeForAlertId =
                    conn.prepareStatement(
                            "SELECT * FROM ALERT_METADATA WHERE "
                                    + ALERTID
                                    + " = ?"
                                    + " AND "
                                    + TYPE
                                    + " = ?");
            psInsert =
                    conn.prepareStatement(
                            "INSERT INTO ALERT_METADATA ("
                                    + ALERTID
                                    + ","
                                    + TYPE
                                    + ","
                                    + DATA
                                    + ") VALUES (?, ?, ?)");
            psDeleteMetadataForAlertId =
                    conn.prepareStatement("DELETE FROM ALERT_METADATA WHERE " + ALERTID + " = ?");
            psDeleteMetadataOfTypeForAlertId =
                    conn.prepareStatement(
                            "DELETE FROM ALERT_METADATA WHERE "
                                    + ALERTID
                                    + " = ?"
                                    + " AND "
                                    + TYPE
                                    + " = ?");
            psGetIdLastInsert = conn.prepareCall("CALL IDENTITY();");
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
    public synchronized RecordAlertMetadata read(long alertId) throws DatabaseException {
        try {
            psRead.setLong(1, alertId);

            try (ResultSet rs = psRead.executeQuery()) {
                RecordAlertMetadata result = build(rs);
                return result;
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public synchronized List<RecordAlertMetadata> getMetadataForAlertId(long alertId)
            throws DatabaseException {
        try {
            List<RecordAlertMetadata> result = new ArrayList<>();
            psGetMetadataForAlertId.setLong(1, alertId);
            try (ResultSet rs = psGetMetadataForAlertId.executeQuery()) {
                while (rs.next()) {
                    result.add(
                            new RecordAlertMetadata(
                                    rs.getLong(ALERTID), rs.getString(TYPE), rs.getString(DATA)));
                }
            }

            return result;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public synchronized RecordAlertMetadata insert(long alertId, String type, String data)
            throws DatabaseException {
        try {
            psInsert.setLong(1, alertId);
            psInsert.setString(2, type);
            psInsert.setString(3, data);
            psInsert.executeUpdate();

            try (ResultSet rs = psGetIdLastInsert.executeQuery()) {
                rs.next();
                long id = rs.getLong(1);
                return read(id);
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public synchronized void delete(long alertId) throws DatabaseException {
        try {
            psDeleteMetadataForAlertId.setLong(1, alertId);
            psDeleteMetadataForAlertId.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public synchronized void deleteMetadataOfTypeForAlertId(long alertId, String type)
            throws DatabaseException {
        try {
            psDeleteMetadataOfTypeForAlertId.setLong(1, alertId);
            psDeleteMetadataOfTypeForAlertId.setString(2, type);
            psDeleteMetadataOfTypeForAlertId.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public List<RecordAlertMetadata> getMetadataOfTypeForAlertId(long alertId, String type)
            throws DatabaseException {
        try {
            List<RecordAlertMetadata> result = new ArrayList<>();
            psGetMetadataOfTypeForAlertId.setLong(1, alertId);
            psGetMetadataOfTypeForAlertId.setString(2, type);

            try (ResultSet rs = psGetMetadataOfTypeForAlertId.executeQuery()) {
                while (rs.next()) {
                    result.add(
                            new RecordAlertMetadata(
                                    rs.getLong(ALERTID), rs.getString(TYPE), rs.getString(DATA)));
                }
            }

            return result;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }
}
