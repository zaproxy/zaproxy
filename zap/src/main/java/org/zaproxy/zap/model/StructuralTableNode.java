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

import java.security.InvalidParameterException;
import java.util.Iterator;
import java.util.Objects;
import org.apache.commons.httpclient.URI;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.RecordStructure;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;

public class StructuralTableNode implements StructuralNode {

    private RecordStructure rs;
    private StructuralNode parent = null;

    private static ExtensionHistory extHistory = null;

    public StructuralTableNode(RecordStructure rs) {
        if (rs == null) {
            throw new InvalidParameterException("RecordStructure must not be null");
        }
        this.rs = rs;
    }

    @Override
    public StructuralNode getParent() throws DatabaseException {
        if (parent == null && !this.isRoot()) {
            RecordStructure prs =
                    Model.getSingleton()
                            .getDb()
                            .getTableStructure()
                            .read(rs.getSessionId(), rs.getStructureId());
            if (prs == null) {
                throw new InvalidParameterException(
                        "Failed to find parent sessionId="
                                + rs.getSessionId()
                                + " parentId="
                                + rs.getParentId());
            }
            parent = new StructuralTableNode(prs);
        }
        return parent;
    }

    @Override
    public Iterator<StructuralNode> getChildIterator() {
        return new StructuralTableNodeIterator(this);
    }

    @Override
    public long getChildNodeCount() throws DatabaseException {
        return Model.getSingleton()
                .getDb()
                .getTableStructure()
                .getChildCount(rs.getSessionId(), rs.getParentId());
    }

    @Override
    public HistoryReference getHistoryReference() {
        return getExtensionHistory().getHistoryReference(this.rs.getHistoryId());
    }

    @Override
    public URI getURI() {
        try {
            return new URI(this.rs.getUrl(), true);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getName() {
        return this.rs.getName();
    }

    @Override
    public String getMethod() {
        return this.rs.getMethod();
    }

    @Override
    public boolean isRoot() {
        return SessionStructure.ROOT.equals(this.rs.getUrl());
    }

    @Override
    public boolean isLeaf() {
        try {
            return this.getChildNodeCount() == 0;
        } catch (DatabaseException e) {
            return false;
        }
    }

    protected RecordStructure getRecordStructure() {
        return this.rs;
    }

    @Override
    public boolean isSameAs(StructuralNode node) {
        if (node instanceof StructuralTableNode) {
            return this.rs.getStructureId() == ((StructuralTableNode) node).rs.getStructureId();
        }
        return false;
    }

    private static ExtensionHistory getExtensionHistory() {
        if (extHistory == null) {
            extHistory =
                    Control.getSingleton()
                            .getExtensionLoader()
                            .getExtension(ExtensionHistory.class);
        }
        return extHistory;
    }

    @Override
    public String getRegexPattern() throws DatabaseException {
        return this.getRegexPattern(true);
    }

    @Override
    public String getRegexPattern(boolean incChildren) throws DatabaseException {
        return SessionStructure.getRegexPattern(this, incChildren);
    }

    @Override
    public boolean isDataDriven() {
        String name = this.getName();
        int slashIndex = name.lastIndexOf('/');
        if (slashIndex >= 0) {
            name = name.substring(slashIndex + 1);
        }
        return name.startsWith(SessionStructure.DATA_DRIVEN_NODE_PREFIX);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rs);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof StructuralTableNode)) {
            return false;
        }
        StructuralTableNode other = (StructuralTableNode) obj;
        return Objects.equals(rs, other.rs);
    }
}
