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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class SqlPreparedStatementWrapper {

    private String key;
    private PreparedStatement ps;
    private PreparedStatement psLastInsert;
    private Date created;

    public SqlPreparedStatementWrapper(String key, PreparedStatement ps) {
        super();
        this.key = key;
        this.ps = ps;
        this.created = new Date();
    }

    public String getKey() {
        return key;
    }

    public PreparedStatement getPs() {
        return ps;
    }

    public ResultSet getLastInsertedId() throws SQLException {
        if (psLastInsert == null) {
            psLastInsert = ps.getConnection().prepareStatement(DbSQL.getSQL("table.ps.lastinsert"));
        }
        return psLastInsert.executeQuery();
    }

    public void close() throws SQLException {
        ps.getConnection().close();
    }

    public long getTimeTaken() {
        return (new Date().getTime()) - this.created.getTime();
    }
}
