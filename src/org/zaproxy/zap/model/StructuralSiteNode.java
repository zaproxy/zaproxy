/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright The ZAP Development Team
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.zaproxy.zap.model;

import java.util.Iterator;

import org.apache.commons.httpclient.URI;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.SiteNode;

public class StructuralSiteNode implements StructuralNode {

	private SiteNode node;
	private	StructuralNode parent = null;
	
	public StructuralSiteNode(SiteNode node) {
		this.node = node;
	}

	@Override
	public StructuralNode getParent() throws DatabaseException {
		if (parent == null && ! this.isRoot()) {
			parent = new StructuralSiteNode(node.getParent());
		}
		return parent;
	}

	@Override
	public Iterator<StructuralNode> getChildIterator() {
		return new StructuralSiteNodeIterator(this);
	}

	@Override
	public long getChildNodeCount() {
		return this.node.getChildCount();
	}

	@Override
	public HistoryReference getHistoryReference() {
		return this.node.getHistoryReference();
	}

	@Override
	public URI getURI() {
		return this.getHistoryReference().getURI();
	}

	@Override
	public String getName() {
		return this.getHistoryReference().getURI().toString();
	}

	@Override
	public boolean isRoot() {
		return this.node.isRoot();
	}

	@Override
	public boolean isLeaf() {
		return this.node.isLeaf();
	}

	public SiteNode getSiteNode() {
		return this.node;
	}
	
	@Override
	public boolean isSameAs (StructuralNode node) {
		if (node instanceof StructuralSiteNode) {
			return this.getSiteNode().equals(((StructuralSiteNode)node).getSiteNode());
		}
		return false;
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
		return this.node.isDataDriven();
	}

}
