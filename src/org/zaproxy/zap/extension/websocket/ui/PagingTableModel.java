/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
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
package org.zaproxy.zap.extension.websocket.ui;

import java.util.*;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;

/**
 * Asked Brian Cole (bassclar@world.oberlin.edu, http://bitguru.com) if I can
 * base my code for ZAP on his PagingTableModel that I found here:
 * http://www.coderanch.com/t/345383/GUI/java/JTable-Paging
 * <p>
 * This {@link TableModel} has got only MAX_PAGE_SIZE rows paged in at a time,
 * but shows the scrollbar for all entries.
 * <p>
 * Removed simulation code, added type parameter and abstract methods.
 */
public abstract class PagingTableModel<T> extends AbstractTableModel {
	private static final long serialVersionUID = -6353414328926478100L;
	
	private static final Logger logger = Logger.getLogger(PagingTableModel.class);

	private static final int MAX_PAGE_SIZE = 50;

	private int dataOffset = 0;
	private List<T> data = new ArrayList<T>();
	private SortedSet<Segment> pending = new TreeSet<Segment>();

	/**
	 * Return number of all items. Scrollbar will appear accordingly. 
	 * 
	 * @return
	 */
	@Override
	public abstract int getRowCount();

	/**
	 * Called by {@link PagingTableModel#getValueAt(int, int)} when requested
	 * row is already loaded.
	 * 
	 * @param rowObject
	 * @param columnIndex
	 * @return
	 */
	protected abstract Object getColumnValue(T rowObject, int columnIndex);

	/**
	 * Value returned is used to display while loading entry.
	 * 
	 * @param columnIndex
	 * @return
	 */
	protected abstract Object getPlaceholderValueAt(int columnIndex);

	/**
	 * Called by {@link PagingTableModel#load(int, int)} 
	 * @param offset
	 * @param length
	 * @return 
	 */
	protected abstract List<T> loadPage(int offset, int length);

	@Override
	public final Object getValueAt(int rowIndex, int columnIndex) {
		T rowObject = getRowObject(rowIndex);
		if (rowObject == null) {
			// is not loaded yet
			schedule(rowIndex);
			
			// return default value meanwhile
			return getPlaceholderValueAt(columnIndex);
		}
		
		return getColumnValue(rowObject, columnIndex);
	}

	/**
	 * Returns null if object is not in current page.
	 * 
	 * @param rowIndex
	 * @return
	 */
	protected T getRowObject(int rowIndex) {
		int pageIndex = rowIndex - dataOffset;
		if (pageIndex >= 0 && pageIndex < data.size()) {
			return data.get(pageIndex);
		}
		return null;
	}

	/**
	 * Schedule the loading of the neighborhood around offset (if not already
	 * scheduled).
	 * 
	 * @param offset
	 */
	private void schedule(int offset) {
		if (isPending(offset)) {
			return;
		}
		
		int startOffset = Math.max(0, offset - MAX_PAGE_SIZE / 2);
		int length = offset + MAX_PAGE_SIZE / 2 - startOffset;
		
		load(startOffset, length);
	}

	private boolean isPending(int offset) {
		int pendingCount = pending.size();
		
		if (pendingCount == 0) {
			return false;
		}
		
		if (pendingCount == 1) {
			// special case (for speed)
			Segment seg = pending.first();
			return seg.contains(offset);
		}
		
		Segment low = new Segment(offset - MAX_PAGE_SIZE, 0);
		Segment high = new Segment(offset + 1, 0);
		
		// search pending segments that may contain offset
		for (Segment seg : pending.subSet(low, high)) {
			if (seg.contains(offset)) {
				return true;
			}
		}
		
		return false;
	}

	private void load(final int startOffset, final int length) {
		final Segment seg = new Segment(startOffset, length);
		pending.add(seg);
		
		// load entries in another thread
		Runnable loadEntries = new Runnable() {
			public void run() {
				final List<T> page;
				
				try {
					page = loadPage(startOffset, length);
				} catch (Exception e) {
					logger.warn("error retrieving page at " + startOffset + ": aborting", e);
					pending.remove(seg);
					return;
				}
				
				// loading finished, make available on the event dispatch thread
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						setData(startOffset, page);
						pending.remove(seg);
					}
				});
			}
		};
		new Thread(loadEntries).start();
	}

	private void setData(int offset, List<T> page) {
		// This method must be called from the event dispatch thread.
		int lastRow = offset + page.size() - 1;
		dataOffset = offset;
		data = page;
		fireTableRowsUpdated(offset, lastRow);
	}
	
	protected void clear() {
		data.clear();
		pending.clear();
	}

	/**
	 * This class is used to keep track of which rows have been scheduled for
	 * loading, so that rows don't get scheduled twice concurrently. The idea is
	 * to store Segments in a sorted data structure for fast searching.
	 * 
	 * The compareTo() method sorts first by base position, then by length.
	 */
	static final class Segment implements Comparable<Segment> {
		private int base = 0, length = 1;

		public Segment(int base, int length) {
			this.base = base;
			this.length = length;
		}

		public boolean contains(int pos) {
			return (base <= pos && pos < base + length);
		}

		public boolean equals(Object o) {
			boolean isSegment = (o instanceof Segment);
			boolean hasSameBase = (base == ((Segment) o).base);
			boolean hasSameLength = (length == ((Segment) o).length);
			
			return isSegment && hasSameBase && hasSameLength;
		}

		public int compareTo(Segment other) {
			// return negative/zero/positive as this object is
			// less-than/equal-to/greater-than other
			int d = base - other.base;
			if (d != 0) {
				return d;
			}
			return length - other.length;
		}
	}
}
