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
package org.zaproxy.zap.db;

import java.util.List;
import java.util.Map;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.DatabaseListener;

public interface TableAlertTag extends DatabaseListener {

    RecordAlertTag read(long tagId) throws DatabaseException;

    RecordAlertTag read(long alertId, String key) throws DatabaseException;

    RecordAlertTag insertOrUpdate(long alertId, String key, String value) throws DatabaseException;

    Map<String, String> getTagsByAlertId(long alertId) throws DatabaseException;

    Map<String, String> getAllTags() throws DatabaseException;

    List<RecordAlertTag> getAllRecords() throws DatabaseException;

    void delete(long tagId) throws DatabaseException;

    void delete(long alertId, String key) throws DatabaseException;

    void deleteAllTagsForAlert(long alertId) throws DatabaseException;

    int deleteAllTags() throws DatabaseException;
}
