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

/**
 * This interface was extracted from the previous Paros class of the same name. The Paros class that
 * implements this interface has been moved to the 'paros' sub package and prefixed with 'Paros'
 *
 * @author psiinon
 */
import java.util.List;

public interface TableContext extends DatabaseListener {

    RecordContext read(long dataId) throws DatabaseException;

    RecordContext insert(int contextId, int type, String url) throws DatabaseException;

    void delete(int contextId, int type, String data) throws DatabaseException;

    void deleteAllDataForContextAndType(int contextId, int type) throws DatabaseException;

    void deleteAllDataForContext(int contextId) throws DatabaseException;

    List<RecordContext> getAllData() throws DatabaseException;

    List<RecordContext> getDataForContext(int contextId) throws DatabaseException;

    List<RecordContext> getDataForContextAndType(int contextId, int type) throws DatabaseException;

    void setData(int contextId, int type, List<String> dataList) throws DatabaseException;
}
