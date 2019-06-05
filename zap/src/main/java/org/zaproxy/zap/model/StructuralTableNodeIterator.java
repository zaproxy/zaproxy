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
package org.zaproxy.zap.model;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.RecordStructure;
import org.parosproxy.paros.model.Model;

public class StructuralTableNodeIterator implements Iterator<StructuralNode> {

    @SuppressWarnings("unused")
    private StructuralNode parent;

    private List<RecordStructure> children;
    private int index = 0;

    public StructuralTableNodeIterator(StructuralTableNode parent) {
        this.parent = parent;
        // TODO handle v large numbers of children?
        try {
            children =
                    Model.getSingleton()
                            .getDb()
                            .getTableStructure()
                            .getChildren(
                                    parent.getRecordStructure().getSessionId(),
                                    parent.getRecordStructure().getStructureId());
        } catch (DatabaseException e) {
            // Ignore - if there are db problems then load of errors will get logged
        }
    }

    @Override
    public boolean hasNext() {
        return children != null && index < children.size();
    }

    @Override
    public StructuralTableNode next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        RecordStructure childRs = children.get(index);
        index++;
        return new StructuralTableNode(childRs);
    }

    @Override
    public void remove() {
        // TODO

    }
}
