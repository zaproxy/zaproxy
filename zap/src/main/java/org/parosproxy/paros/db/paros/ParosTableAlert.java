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
// ZAP: 2011/05/09 Support for API
// ZAP: 2011/05/27 Ensure all PreparedStatements and ResultSets closed to prevent leaks
// ZAP: 2012/01/02 Separate param and attack
// ZAP: 2012/04/23 Added @Override annotation to the appropriate method.
// ZAP: 2012/04/25 Changed to use the method Integer.valueOf.
// ZAP: 2012/08/08 Upgrade to HSQLDB 2.x (introduced TABLE_NAME constant + DbUtils)
// ZAP: 2013/07/11 Issue 713: Add CWE and WASC numbers to issues
// ZAP: 2014/03/04 Issue 1042: Add ALERT_INDEX to speed up session load
// ZAP: 2014/03/23 Changed to use try-with-resource statements.
// ZAP: 2014/05/23 Issue 1209: Reliability becomes Confidence and add levels
// ZAP: 2014/06/16 Issue 990: Allow to delete alerts through the API
// ZAP: 2014/08/14 Issue 1283: SQLDataException: data exception: string data,
// right truncation while writing an alert to DB
// ZAP: 2015/02/09 Issue 1525: Introduce a database interface layer to allow for alternative
// implementations
// ZAP: 2016/10/11 Issue 2592: Differentiate the source of alerts
// ZAP: 2018/02/14 Remove unnecessary boxing / unboxing
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2021/04/30 Add input vector to Alert
// ZAP: 2021/08/24 Remove the "(non-Javadoc)" comments.
package org.parosproxy.paros.db.paros;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.DbUtils;
import org.parosproxy.paros.db.RecordAlert;
import org.parosproxy.paros.db.TableAlert;

public class ParosTableAlert extends ParosAbstractTable implements TableAlert {

    // ZAP: introduced class constant
    private static final String TABLE_NAME = "ALERT";

    private static final String ALERT_INDEX = "ALERT_INDEX";

    private static final String ALERTID = "ALERTID";
    private static final String SCANID = "SCANID";
    private static final String PLUGINID = "PLUGINID";
    private static final String ALERT = "ALERT";
    private static final String RISK = "RISK";
    private static final String RELIABILITY = "RELIABILITY";
    private static final String DESCRIPTION = "DESCRIPTION";
    private static final String URI = "URI";
    private static final String PARAM = "PARAM";
    private static final String ATTACK = "ATTACK";
    private static final String OTHERINFO = "OTHERINFO";
    private static final String SOLUTION = "SOLUTION";
    private static final String REFERENCE = "REFERENCE";
    private static final String EVIDENCE = "EVIDENCE";
    private static final String INPUT_VECTOR = "INPUT_VECTOR";
    private static final String CWEID = "CWEID";
    private static final String WASCID = "WASCID";
    private static final String HISTORYID = "HISTORYID";
    private static final String SOURCEHISTORYID = "SOURCEHISTORYID";
    private static final String SOURCEID = "SOURCEID";
    private static final String ALERTREF = "ALERTREF";

    private PreparedStatement psRead = null;
    private PreparedStatement psInsert = null;
    private CallableStatement psGetIdLastInsert = null;

    private PreparedStatement psDeleteAlert = null;
    private PreparedStatement psDeleteAllAlerts = null;
    // private PreparedStatement psDeleteScan = null;

    private PreparedStatement psUpdate = null;
    private PreparedStatement psUpdateHistoryIds = null;

    private PreparedStatement psGetAlertsForHistoryId = null;

    public ParosTableAlert() {}

    @Override
    protected void reconnect(Connection conn) throws DatabaseException {
        try {
            // ZAP: Changed to call the method updateTable(Connection).
            updateTable(conn);

            psRead =
                    conn.prepareStatement(
                            "SELECT TOP 1 * FROM " + TABLE_NAME + " WHERE " + ALERTID + " = ?");

            psInsert =
                    conn.prepareStatement(
                            "INSERT INTO "
                                    + TABLE_NAME
                                    + " ("
                                    + SCANID
                                    + ","
                                    + PLUGINID
                                    + ","
                                    + ALERT
                                    + ","
                                    + RISK
                                    + ","
                                    + RELIABILITY
                                    + ","
                                    + DESCRIPTION
                                    + ","
                                    + URI
                                    + ","
                                    + PARAM
                                    + ","
                                    + ATTACK
                                    + ","
                                    + OTHERINFO
                                    + ","
                                    + SOLUTION
                                    + ","
                                    + REFERENCE
                                    + ","
                                    + EVIDENCE
                                    + ","
                                    + CWEID
                                    + ","
                                    + WASCID
                                    + ","
                                    + HISTORYID
                                    + ","
                                    + SOURCEHISTORYID
                                    + ","
                                    + SOURCEID
                                    + ","
                                    + ALERTREF
                                    + ","
                                    + INPUT_VECTOR
                                    + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            psGetIdLastInsert = conn.prepareCall("CALL IDENTITY();");
            psDeleteAlert =
                    conn.prepareStatement(
                            "DELETE FROM " + TABLE_NAME + " WHERE " + ALERTID + " = ?");
            psDeleteAllAlerts = conn.prepareStatement("DELETE FROM " + TABLE_NAME);
            // psDeleteScan = conn.prepareStatement("DELETE FROM ALERT WHERE " + SCANID + " = ?");

            // ZAP: New prepared statement for updating an alert
            psUpdate =
                    conn.prepareStatement(
                            "UPDATE "
                                    + TABLE_NAME
                                    + " SET "
                                    + ALERT
                                    + " = ?, "
                                    + RISK
                                    + " = ?,"
                                    + RELIABILITY
                                    + " = ?,"
                                    + DESCRIPTION
                                    + " = ?,"
                                    + URI
                                    + " = ?,"
                                    + PARAM
                                    + " = ?,"
                                    + ATTACK
                                    + " = ?,"
                                    + OTHERINFO
                                    + " = ?,"
                                    + SOLUTION
                                    + " = ?,"
                                    + REFERENCE
                                    + " = ?, "
                                    + EVIDENCE
                                    + " = ?, "
                                    + CWEID
                                    + " = ?, "
                                    + WASCID
                                    + " = ?, "
                                    + SOURCEHISTORYID
                                    + " = ?, "
                                    + INPUT_VECTOR
                                    + " = ? "
                                    + "WHERE "
                                    + ALERTID
                                    + " = ?");

            psUpdateHistoryIds =
                    conn.prepareStatement(
                            "UPDATE "
                                    + TABLE_NAME
                                    + " SET "
                                    + HISTORYID
                                    + " = ?, "
                                    + SOURCEHISTORYID
                                    + " = ? "
                                    + "WHERE "
                                    + ALERTID
                                    + " = ?");

            psGetAlertsForHistoryId =
                    conn.prepareStatement(
                            "SELECT * FROM " + TABLE_NAME + " WHERE " + SOURCEHISTORYID + " = ?");
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    // ZAP: Added the method.
    private void updateTable(Connection connection) throws DatabaseException {
        try {
            // Add the SOURCEHISTORYID column to the db if necessary
            if (!DbUtils.hasColumn(connection, TABLE_NAME, SOURCEHISTORYID)) {
                DbUtils.execute(
                        connection,
                        "ALTER TABLE "
                                + TABLE_NAME
                                + " ADD COLUMN "
                                + SOURCEHISTORYID
                                + " INT DEFAULT 0");
            }

            // Add the ATTACK column to the db if necessary
            if (!DbUtils.hasColumn(connection, TABLE_NAME, ATTACK)) {
                DbUtils.execute(
                        connection,
                        "ALTER TABLE "
                                + TABLE_NAME
                                + " ADD COLUMN "
                                + ATTACK
                                + " VARCHAR(32768) DEFAULT ''");
            }

            if (!DbUtils.hasColumn(connection, TABLE_NAME, EVIDENCE)) {
                // Evidence, cweId and wascId all added at the same time
                DbUtils.execute(
                        connection,
                        "ALTER TABLE "
                                + TABLE_NAME
                                + " ADD COLUMN "
                                + EVIDENCE
                                + " VARCHAR(16777216) DEFAULT ''");
                DbUtils.execute(
                        connection,
                        "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + CWEID + " INT DEFAULT -1");
                DbUtils.execute(
                        connection,
                        "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + WASCID + " INT DEFAULT -1");
            }

            if (!DbUtils.hasColumn(connection, TABLE_NAME, INPUT_VECTOR)) {
                DbUtils.execute(
                        connection,
                        "ALTER TABLE "
                                + TABLE_NAME
                                + " ADD COLUMN "
                                + INPUT_VECTOR
                                + " VARCHAR(256) DEFAULT ''");
            }

            if (!DbUtils.hasIndex(connection, TABLE_NAME, ALERT_INDEX)) {
                // this speeds up session loading
                DbUtils.execute(
                        connection,
                        "CREATE INDEX "
                                + ALERT_INDEX
                                + " ON "
                                + TABLE_NAME
                                + " ("
                                + SOURCEHISTORYID
                                + ")");
            }

            if (!DbUtils.hasColumn(connection, TABLE_NAME, SOURCEID)) {
                DbUtils.execute(
                        connection,
                        "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + SOURCEID + " INT DEFAULT 0");
                DbUtils.execute(
                        connection,
                        "CREATE INDEX INDEX_ALERT_SOURCEID ON "
                                + TABLE_NAME
                                + " ("
                                + SOURCEID
                                + ")");
            }
            if (!DbUtils.hasColumn(connection, TABLE_NAME, ALERTREF)) {
                DbUtils.execute(
                        connection,
                        "ALTER TABLE "
                                + TABLE_NAME
                                + " ADD COLUMN "
                                + ALERTREF
                                + " VARCHAR(256) DEFAULT ''");
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public synchronized RecordAlert read(int alertId) throws DatabaseException {
        try {
            psRead.setInt(1, alertId);
            try (ResultSet rs = psRead.executeQuery()) {
                RecordAlert ra = build(rs);
                return ra;
            }
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public synchronized RecordAlert write(
            int scanId,
            int pluginId,
            String alert,
            int risk,
            int confidence,
            String description,
            String uri,
            String param,
            String attack,
            String otherInfo,
            String solution,
            String reference,
            String evidence,
            int cweId,
            int wascId,
            int historyId,
            int sourceHistoryId,
            int sourceId,
            String alertRef,
            String inputVector)
            throws DatabaseException {

        try {
            psInsert.setInt(1, scanId);
            psInsert.setInt(2, pluginId);
            psInsert.setString(3, alert);
            psInsert.setInt(4, risk);
            psInsert.setInt(5, confidence);
            psInsert.setString(6, description);
            psInsert.setString(7, uri);
            psInsert.setString(8, param);
            psInsert.setString(9, attack);
            psInsert.setString(10, otherInfo);
            psInsert.setString(11, solution);
            psInsert.setString(12, reference);
            psInsert.setString(13, evidence);
            psInsert.setInt(14, cweId);
            psInsert.setInt(15, wascId);
            psInsert.setInt(16, historyId);
            psInsert.setInt(17, sourceHistoryId);
            psInsert.setInt(18, sourceId);
            psInsert.setString(19, alertRef);
            psInsert.setString(20, inputVector);
            psInsert.executeUpdate();

            int id;
            try (ResultSet rs = psGetIdLastInsert.executeQuery()) {
                rs.next();
                id = rs.getInt(1);
            }
            return read(id);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    private RecordAlert build(ResultSet rs) throws DatabaseException {
        try {
            RecordAlert alert = null;
            if (rs.next()) {
                alert =
                        new RecordAlert(
                                rs.getInt(ALERTID),
                                rs.getInt(SCANID),
                                rs.getInt(PLUGINID),
                                rs.getString(ALERT),
                                rs.getInt(RISK),
                                rs.getInt(RELIABILITY),
                                rs.getString(DESCRIPTION),
                                rs.getString(URI),
                                rs.getString(PARAM),
                                rs.getString(ATTACK),
                                rs.getString(OTHERINFO),
                                rs.getString(SOLUTION),
                                rs.getString(REFERENCE),
                                rs.getString(EVIDENCE),
                                rs.getInt(CWEID),
                                rs.getInt(WASCID),
                                rs.getInt(HISTORYID),
                                rs.getInt(SOURCEHISTORYID),
                                rs.getInt(SOURCEID),
                                rs.getString(ALERTREF),
                                rs.getString(INPUT_VECTOR));
            }
            return alert;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public Vector<Integer> getAlertListByScan(int scanId) throws DatabaseException {
        try {
            try (PreparedStatement psReadScan =
                    getConnection()
                            .prepareStatement(
                                    "SELECT ALERTID FROM "
                                            + TABLE_NAME
                                            + " WHERE "
                                            + SCANID
                                            + " = ?")) {

                Vector<Integer> v = new Vector<>();
                psReadScan.setInt(1, scanId);
                try (ResultSet rs = psReadScan.executeQuery()) {
                    while (rs.next()) {
                        // ZAP: Changed to use the method Integer.valueOf.
                        v.add(rs.getInt(ALERTID));
                    }
                }
                return v;
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public Vector<Integer> getAlertListBySession(long sessionId) throws DatabaseException {
        try {
            try (PreparedStatement psReadSession =
                    getConnection()
                            .prepareStatement(
                                    "SELECT ALERTID FROM "
                                            + TABLE_NAME
                                            + " INNER JOIN SCAN ON ALERT.SCANID = SCAN.SCANID WHERE SESSIONID = ?")) {

                Vector<Integer> v = new Vector<>();
                psReadSession.setLong(1, sessionId);
                try (ResultSet rs = psReadSession.executeQuery()) {
                    while (rs.next()) {
                        int alertId = rs.getInt(ALERTID);
                        // ZAP: Changed to use the method Integer.valueOf.
                        v.add(alertId);
                    }
                }
                return v;
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public synchronized void deleteAlert(int alertId) throws DatabaseException {
        try {
            psDeleteAlert.setInt(1, alertId);
            psDeleteAlert.execute();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public int deleteAllAlerts() throws DatabaseException {
        try {
            return psDeleteAllAlerts.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public synchronized void update(
            int alertId,
            String alert,
            int risk,
            int confidence,
            String description,
            String uri,
            String param,
            String attack,
            String otherInfo,
            String solution,
            String reference,
            String evidence,
            int cweId,
            int wascId,
            int sourceHistoryId,
            String inputVector)
            throws DatabaseException {

        try {
            psUpdate.setString(1, alert);
            psUpdate.setInt(2, risk);
            psUpdate.setInt(3, confidence);
            psUpdate.setString(4, description);
            psUpdate.setString(5, uri);
            psUpdate.setString(6, param);
            psUpdate.setString(7, attack);
            psUpdate.setString(8, otherInfo);
            psUpdate.setString(9, solution);
            psUpdate.setString(10, reference);
            psUpdate.setString(11, evidence);
            psUpdate.setInt(12, cweId);
            psUpdate.setInt(13, wascId);
            psUpdate.setInt(14, sourceHistoryId);
            psUpdate.setString(15, inputVector);
            psUpdate.setInt(16, alertId);
            psUpdate.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public synchronized void updateHistoryIds(int alertId, int historyId, int sourceHistoryId)
            throws DatabaseException {

        try {
            psUpdateHistoryIds.setInt(1, historyId);
            psUpdateHistoryIds.setInt(2, sourceHistoryId);
            psUpdateHistoryIds.setInt(3, alertId);
            psUpdateHistoryIds.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public synchronized List<RecordAlert> getAlertsBySourceHistoryId(int historyId)
            throws DatabaseException {
        try {
            List<RecordAlert> result = new ArrayList<>();
            psGetAlertsForHistoryId.setLong(1, historyId);
            try (ResultSet rs = psGetAlertsForHistoryId.executeQuery()) {
                RecordAlert ra = build(rs);
                while (ra != null) {
                    result.add(ra);
                    ra = build(rs);
                }
            }

            return result;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public Vector<Integer> getAlertList() throws DatabaseException {
        try {
            try (PreparedStatement psReadScan =
                    getConnection().prepareStatement("SELECT " + ALERTID + " FROM " + TABLE_NAME)) {

                Vector<Integer> v = new Vector<>();
                try (ResultSet rs = psReadScan.executeQuery()) {
                    while (rs.next()) {
                        v.add(rs.getInt(ALERTID));
                    }
                }
                return v;
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }
}
