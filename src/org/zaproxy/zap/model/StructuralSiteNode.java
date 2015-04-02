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
			parent = new StructuralSiteNode((SiteNode) node.getParent());
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
	
	public boolean isSameAs (StructuralNode node) {
		if (node instanceof StructuralSiteNode) {
			return this.getSiteNode().equals(((StructuralSiteNode)node).getSiteNode());
		}
		return false;
	}

}
