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
package org.parosproxy.paros.db;

import java.util.List;

/**
 * A table that represents the SiteMap when the low-memory option is set
 *
 * @author psiinon
 */
public interface TableStructure extends DatabaseListener {

    RecordStructure read(long sessionId, long structureId) throws DatabaseException;

    RecordStructure find(long sessionId, String name, String method) throws DatabaseException;

    List<RecordStructure> getChildren(long sessionId, long parentId) throws DatabaseException;

    long getChildCount(long sessionId, long parentId) throws DatabaseException;

    RecordStructure insert(
            long sessionId, long parentId, int historyId, String name, String url, String method)
            throws DatabaseException;

    void deleteLeaf(long sessionId, long structureId) throws DatabaseException;

    void deleteSubtree(long sessionId, long structureId) throws DatabaseException;
}
