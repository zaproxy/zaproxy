/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2021 The ZAP Development Team
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.DbUtils;
import org.zaproxy.zap.db.RecordAlertTag;
import org.zaproxy.zap.db.TableAlertTag;

public class SqlTableAlertTag extends SqlAbstractTable implements TableAlertTag {

    private static final String TABLE_NAME = DbSQL.getSQL("alerttag.table_name");

    private static final String TAG_ID = DbSQL.getSQL("alerttag.field.tagid");
    private static final String ALERT_ID = DbSQL.getSQL("alerttag.field.alertid");
    private static final String KEY = DbSQL.getSQL("alerttag.field.key");
    private static final String VALUE = DbSQL.getSQL("alerttag.field.value");

    @Override
    protected void reconnect(Connection conn) throws DatabaseException {
        try {
            if (!DbUtils.hasTable(conn, TABLE_NAME)) {
                DbUtils.execute(conn, DbSQL.getSQL("alerttag.ps.createtable"));
            }
            if (!DbUtils.hasIndex(conn, "ALERT_TAG", "ALERT_ID_INDEX")) {
                DbUtils.execute(conn, DbSQL.getSQL("alerttag.ps.indexalertid"));
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public RecordAlertTag read(long tagId) throws DatabaseException {
        SqlPreparedStatementWrapper psRead = null;
        try {
            psRead = DbSQL.getSingleton().getPreparedStatement("alerttag.ps.readbytagid");
            psRead.getPs().setLong(1, tagId);

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
    public RecordAlertTag read(long alertId, String key) throws DatabaseException {
        SqlPreparedStatementWrapper psRead = null;
        try {
            psRead = DbSQL.getSingleton().getPreparedStatement("alerttag.ps.readbyalertidtagkey");
            psRead.getPs().setLong(1, alertId);
            psRead.getPs().setString(2, key);

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
    public RecordAlertTag insertOrUpdate(long alertId, String key, String value)
            throws DatabaseException {
        SqlPreparedStatementWrapper psInsertOrUpdate = null;
        try {
            psInsertOrUpdate =
                    DbSQL.getSingleton().getPreparedStatement("alerttag.ps.insertorupdate");
            psInsertOrUpdate.getPs().setLong(1, alertId);
            psInsertOrUpdate.getPs().setString(2, key);
            psInsertOrUpdate.getPs().setString(3, value);
            psInsertOrUpdate.getPs().executeUpdate();
            return read(alertId, key);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbSQL.getSingleton().releasePreparedStatement(psInsertOrUpdate);
        }
    }

    @Override
    public Map<String, String> getAllTags() throws DatabaseException {
        SqlPreparedStatementWrapper psGetAllTags = null;
        try {
            psGetAllTags = DbSQL.getSingleton().getPreparedStatement("alerttag.ps.getalltags");
            Map<String, String> result = new HashMap<>();
            try (ResultSet rs = psGetAllTags.getPs().executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString(KEY), rs.getString(VALUE));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbSQL.getSingleton().releasePreparedStatement(psGetAllTags);
        }
    }

    @Override
    public List<RecordAlertTag> getAllRecords() throws DatabaseException {
        SqlPreparedStatementWrapper psGetAllRecords = null;
        try {
            psGetAllRecords =
                    DbSQL.getSingleton().getPreparedStatement("alerttag.ps.getallrecords");
            List<RecordAlertTag> result = new ArrayList<>();
            try (ResultSet rs = psGetAllRecords.getPs().executeQuery()) {
                RecordAlertTag rat;
                while ((rat = build(rs)) != null) {
                    result.add(rat);
                }
            }
            return result;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbSQL.getSingleton().releasePreparedStatement(psGetAllRecords);
        }
    }

    @Override
    public Map<String, String> getTagsByAlertId(long alertId) throws DatabaseException {
        SqlPreparedStatementWrapper psGetTagsByAlertId = null;
        try {
            psGetTagsByAlertId =
                    DbSQL.getSingleton().getPreparedStatement("alerttag.ps.gettagsbyalertid");
            Map<String, String> result = new HashMap<>();
            psGetTagsByAlertId.getPs().setLong(1, alertId);
            try (ResultSet rs = psGetTagsByAlertId.getPs().executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString("key"), rs.getString("value"));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbSQL.getSingleton().releasePreparedStatement(psGetTagsByAlertId);
        }
    }

    @Override
    public void delete(long alertId, String key) throws DatabaseException {
        SqlPreparedStatementWrapper psDelete = null;
        try {
            psDelete =
                    DbSQL.getSingleton().getPreparedStatement("alerttag.ps.deletebyalertidtagkey");
            psDelete.getPs().setLong(1, alertId);
            psDelete.getPs().setString(2, key);
            psDelete.getPs().executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbSQL.getSingleton().releasePreparedStatement(psDelete);
        }
    }

    @Override
    public void delete(long tagId) throws DatabaseException {
        SqlPreparedStatementWrapper psDelete = null;
        try {
            psDelete = DbSQL.getSingleton().getPreparedStatement("alerttag.ps.deletebytagid");
            psDelete.getPs().setLong(1, tagId);
            psDelete.getPs().executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbSQL.getSingleton().releasePreparedStatement(psDelete);
        }
    }

    @Override
    public void deleteAllTagsForAlert(long alertId) throws DatabaseException {
        SqlPreparedStatementWrapper psDelete = null;
        try {
            psDelete =
                    DbSQL.getSingleton().getPreparedStatement("alerttag.ps.deletealltagsforalert");
            psDelete.getPs().setLong(1, alertId);
            psDelete.getPs().executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbSQL.getSingleton().releasePreparedStatement(psDelete);
        }
    }

    @Override
    public int deleteAllTags() throws DatabaseException {
        SqlPreparedStatementWrapper psDelete = null;
        try {
            psDelete = DbSQL.getSingleton().getPreparedStatement("alerttag.ps.deletealltags");
            return psDelete.getPs().executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbSQL.getSingleton().releasePreparedStatement(psDelete);
        }
    }

    private RecordAlertTag build(ResultSet rs) throws SQLException {
        RecordAlertTag rat = null;
        if (rs.next()) {
            rat =
                    new RecordAlertTag(
                            rs.getLong(TAG_ID),
                            rs.getLong(ALERT_ID),
                            rs.getString(KEY),
                            rs.getString(VALUE));
        }
        return rat;
    }
}
