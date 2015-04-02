package org.zaproxy.zap.model;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.parosproxy.paros.model.SiteNode;

public class StructuralSiteNodeIterator implements Iterator<StructuralNode> {

	private Enumeration<SiteNode> children;
	
	public StructuralSiteNodeIterator(StructuralSiteNode parent){
		children = parent.getSiteNode().children();
	}
	
	@Override
	public boolean hasNext() {
		return children.hasMoreElements();
	}

	@Override
	public StructuralSiteNode next() {
		if (! hasNext()) {
			throw new NoSuchElementException();
		}
		return new StructuralSiteNode((SiteNode)children.nextElement());
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub
	}
}
