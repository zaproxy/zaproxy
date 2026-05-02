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
import org.parosproxy.paros.db.RecordTag;
import org.parosproxy.paros.db.TableTag;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryTableTag extends ParosAbstractTable implements TableTag {

    private final AtomicLong nextId = new AtomicLong();
    private final InMemoryDb<Long, TagItem> db = new InMemoryDb<>();

    private record TagItem(long historyId, String tag) {
        public RecordTag toRecordTag(long tagId) {
            return new RecordTag(tagId, historyId, tag);
        }
    }

    @Override
    protected void reconnect(Connection conn) throws DatabaseException {

    }

    @Override
    public synchronized RecordTag read(long tagId) throws DatabaseException {
        return db.get(tagId).toRecordTag(tagId);
    }

    @Override
    public synchronized RecordTag insert(long historyId, String tag) throws DatabaseException {
        TagItem item = new TagItem(historyId, tag);
        long id = nextId.incrementAndGet();
        db.put(id, item);
        return item.toRecordTag(id);
    }

    @Override
    public void delete(long historyId, String tag) throws DatabaseException {
        db.remove((item) -> item.historyId == historyId && item.tag.equals(tag));
    }

    @Override
    public List<RecordTag> getTagsForHistoryID(long historyId) {

        var result = new ArrayList<RecordTag>();

        db.collect((id, item) -> {
            if (item.historyId == historyId) {
                result.add(item.toRecordTag(id));
            }
        });

        return result;
    }

    @Override
    public List<String> getAllTags() {

        var result = new HashSet<String>();

        db.collect((id, item) -> result.add(item.tag));

        return result.stream().sorted().toList();
    }

    @Override
    public void deleteTagsForHistoryID(long historyId) {
        db.remove((it) -> it.historyId == historyId);
    }
}
