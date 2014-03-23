/*
 *
 * Paros and its related class files.
 * 
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2004 Chinotec Technologies Company
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Clarified Artistic License
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Clarified Artistic License for more details.
 * 
 * You should have received a copy of the Clarified Artistic License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2014/03/23 Issue 503: Change the footer tabs to display the data
// with tables instead of lists
// ZAP: 2014/03/23 Issue 1106: HistoryList's mapping of history ID to list 
// indexes not updated when history entry is deleted
package org.parosproxy.paros.model;


import java.util.Hashtable;
import java.util.Map.Entry;

import javax.swing.DefaultListModel;

/**
 * @deprecated (2.3.0) Replaced with use of {@link org.zaproxy.zap.view.table.HistoryReferencesTableModel}. It will be removed
 *             in a future release.
 */
@Deprecated
public class HistoryList extends DefaultListModel<HistoryReference> {
    
	private static final long serialVersionUID = 1L;
	// ZAP: Added hashtable to allow elements of the list to be accessed via historyid
	private Hashtable<Integer, Integer> historyIdToIndex = new Hashtable<>();

	@Override
	public void addElement(final HistoryReference hRef) {
		int sizeBefore = super.size();
        super.addElement(hRef);
        if (sizeBefore == super.size() -1 ) {
        	historyIdToIndex.put(hRef.getHistoryId(), sizeBefore);
        } else {
        	// Cope with multiple threads adding to the list
        	historyIdToIndex.put(hRef.getHistoryId(), indexOf(hRef));
        }
    }
    
    public synchronized void notifyItemChanged(Object obj) {
        int i = indexOf(obj);
        if (i >= 0) {
            fireContentsChanged(this, i, i);
        }
    }

    public synchronized void notifyItemChanged(int historyId) {
        Integer i = historyIdToIndex.get(historyId);
        if (i != null) {
            fireContentsChanged(this, i, i);
        }
    }
    
    public HistoryReference getHistoryReference (int historyId) {
        Integer i = historyIdToIndex.get(historyId);
        if (i != null) {
        	return this.elementAt(i);
        }
    	return null;
    }
    
    @Override
    public void clear() {
    	super.clear();
    	historyIdToIndex.clear();
    }
    
    @Override
    public void removeAllElements() {
        super.removeAllElements();
        historyIdToIndex.clear();
    }

    @Override
    public HistoryReference remove(int index) {
        HistoryReference hRef = super.remove(index);
        historyIdToIndex.remove(hRef.getHistoryId());
        updateIndexes(index);
        return hRef;
    }

    @Override
    public boolean removeElement(Object obj) {
        if (super.removeElement(obj)) {
            int idx = historyIdToIndex.remove(((HistoryReference) obj).getHistoryId());
            updateIndexes(idx);
            return true;
        }
        return false;
    }

    @Override
    public void removeElementAt(int index) {
        remove(index);
    }

    // Note: implementation copied from base class (DefaultListModel) but changed to call the local method removeElementAt(int)
    @Override
    public void removeRange(int fromIndex, int toIndex) {
        if (fromIndex > toIndex) {
            throw new IllegalArgumentException("fromIndex must be <= toIndex");
        }
        for (int i = toIndex; i >= fromIndex; i--) {
            removeElementAt(i);
        }
        fireIntervalRemoved(this, fromIndex, toIndex);
    }

    private void updateIndexes(int fromIndex) {
        for (Entry<Integer, Integer> entry : historyIdToIndex.entrySet()) {
            if (entry.getValue() > fromIndex) {
                entry.setValue(entry.getValue() - 1);
            }
        }
    }
}