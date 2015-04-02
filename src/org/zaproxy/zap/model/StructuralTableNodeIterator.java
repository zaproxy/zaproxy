package org.zaproxy.zap.model;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.RecordStructure;
import org.parosproxy.paros.model.Model;

public class StructuralTableNodeIterator implements Iterator<StructuralNode> {

	private StructuralNode parent;
	private List<RecordStructure> children;
	private int index = 0;
	
	public StructuralTableNodeIterator(StructuralTableNode parent){
		this.parent = parent;
		// TODO handle v large numbers of children?
		try {
			System.out.println("SBSB StructuralTableNodeIterator for parent: " + parent);
			System.out.println("SBSB StructuralTableNodeIterator for parent name: " + parent.getName());
			
			children = Model.getSingleton().getDb().getTableStructure().getChildren(
					parent.getRecordStructure().getSessionId(), parent.getRecordStructure().getStructureId());
			System.out.println("SBSB StructuralTableNodeIterator children: " + children);
			System.out.println("SBSB StructuralTableNodeIterator children size: " + children.size());
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean hasNext() {
		return children != null && index < children.size();
	}

	@Override
	public StructuralTableNode next() {
		if (! hasNext()) {
			throw new NoSuchElementException();
		}
		RecordStructure childRs = children.get(index);
		index++;
		return new StructuralTableNode(childRs);
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub
		
	}


}
