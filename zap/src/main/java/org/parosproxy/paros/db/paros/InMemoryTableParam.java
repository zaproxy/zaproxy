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
package org.parosproxy.paros.db.paros;

import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.RecordParam;
import org.parosproxy.paros.db.TableParam;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryTableParam extends ParosAbstractTable implements TableParam {

    private record ParamItem(
            String site, String type, String name, int used, String flags, String values) {

        public RecordParam toRecordParam(long paramId) {
            return new RecordParam(paramId, site, type, name, used, flags, values);
        }
    }

    private final AtomicLong nextId = new AtomicLong();
    private final InMemoryDb<Long, ParamItem> db = new InMemoryDb<>();

    public InMemoryTableParam() {}

    @Override
    protected void reconnect(Connection conn) throws DatabaseException {}

    @Override
    public synchronized RecordParam read(long paramId) throws DatabaseException {
        ParamItem item = db.get(paramId);
        return item != null ? item.toRecordParam(paramId) : null;
    }

    @Override
    public List<RecordParam> getAll() throws DatabaseException {
        List<RecordParam> result = new ArrayList<>();
        db.collect((id, item) -> result.add(item.toRecordParam(id)));
        return result;
    }

    @Override
    public synchronized RecordParam insert(
            String site, String type, String name, int used, String flags, String values)
            throws DatabaseException {
        ParamItem item = new ParamItem(site, type, name, used, flags, values);
        long id = nextId.incrementAndGet();
        db.put(id, item);
        return item.toRecordParam(id);
    }

    @Override
    public synchronized void update(long paramId, int used, String flags, String values)
            throws DatabaseException {
        ParamItem oldItem = db.get(paramId);
        if (oldItem == null) {
            return;
        }

        ParamItem newItem =
                new ParamItem(
                        oldItem.site, oldItem.type, oldItem.name, used, flags, values);
        db.put(paramId, newItem);
    }
}
