package org.zaproxy.zap.model;

import java.util.Iterator;

import org.apache.commons.httpclient.URI;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.model.HistoryReference;

public interface StructuralNode {

	StructuralNode getParent() throws DatabaseException;
	
	Iterator<StructuralNode> getChildIterator();
	
	long getChildNodeCount() throws DatabaseException;
	
	HistoryReference getHistoryReference();
	
	String getName();
	
	URI getURI();
	
	boolean isRoot();
	
	boolean isLeaf();
	
	boolean isSameAs (StructuralNode node);
}
