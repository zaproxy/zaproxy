/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The ZAP Development Team
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
package org.zaproxy.zap.utils;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A paginated {@code TableModel}. The model will have at most, by default, {@value
 * #DEFAULT_MAX_PAGE_SIZE} rows loaded in memory at any given time. The advertised row count will be
 * of all the entries (as if they were all loaded in memory).
 *
 * <p>If a {@code JTable}, using this model, is wrapped in a {@code JScrollPane} the vertical scroll
 * bar will be shown as if all the entries were loaded.
 *
 * <p>Rows (page segments) will be loaded in a separate thread, on demand.
 *
 * <p>Implementation based on PagingTableModel located in {@literal
 * http://www.coderanch.com/t/345383/GUI/java/JTable-Paging}, with permission from the author, Brian
 * Cole (bassclar@world.oberlin.edu, http://bitguru.com).<br>
 * Contains the following changes:
 *
 * <ul>
 *   <li>Removed simulation code;
 *   <li>Added type parameter;
 *   <li>Added abstract methods.
 * </ul>
 *
 * @param <T> the type of elements in this table model
 * @see #loadPage(int, int)
 * @see #setMaxPageSize(int)
 * @see javax.swing.table.TableModel
 * @see javax.swing.JTable
 * @see javax.swing.JScrollPane
 */
@SuppressWarnings("serial")
public abstract class PagingTableModel<T> extends AbstractTableModel {
    private static final long serialVersionUID = -6353414328926478100L;

    private static final Logger logger = LogManager.getLogger(PagingTableModel.class);

    /** Default segment loader thread name. */
    public static final String DEFAULT_SEGMENT_LOADER_THREAD_NAME =
            "ZAP-PagingTableModel-SegmentLoaderThread";

    /** Default maximum page size. */
    public static final int DEFAULT_MAX_PAGE_SIZE = 50;

    /** The maximum size of the page. */
    private int maxPageSize;

    private int dataOffset = 0;
    private List<T> data = Collections.emptyList();
    private SortedSet<Segment> pending = new TreeSet<>();

    private final String segmentLoaderThreadName;

    /**
     * Constructs a {@code PagingTableModel} with default default segment loader thread name (
     * {@value #DEFAULT_SEGMENT_LOADER_THREAD_NAME}) and default maximum page size ({@value
     * #DEFAULT_MAX_PAGE_SIZE}).
     */
    public PagingTableModel() {
        this(DEFAULT_SEGMENT_LOADER_THREAD_NAME, DEFAULT_MAX_PAGE_SIZE);
    }

    /**
     * Constructs a {@code PagingTableModel} with the given segment loader thread name and default
     * maximum page size ({@value #DEFAULT_MAX_PAGE_SIZE}).
     *
     * @param segmentLoaderThreadName the name for segment loader thread
     * @throws IllegalArgumentException if {@code maxPageSize} is negative or zero.
     */
    public PagingTableModel(String segmentLoaderThreadName) {
        this(segmentLoaderThreadName, DEFAULT_MAX_PAGE_SIZE);
    }

    /**
     * Constructs a {@code PagingTableModel} with the given maximum page size and default segment
     * loader thread name ( {@value #DEFAULT_SEGMENT_LOADER_THREAD_NAME}).
     *
     * @param maxPageSize the maximum page size
     * @throws IllegalArgumentException if {@code maxPageSize} is negative or zero.
     */
    public PagingTableModel(final int maxPageSize) {
        this(DEFAULT_SEGMENT_LOADER_THREAD_NAME, maxPageSize);
    }

    /**
     * Constructs a {@code PagingTableModel} with the given segment loader thread name and given
     * maximum page size.
     *
     * @param segmentLoaderThreadName the name for segment loader thread
     * @param maxPageSize the maximum page size
     * @throws IllegalArgumentException if {@code maxPageSize} is negative or zero.
     */
    public PagingTableModel(String segmentLoaderThreadName, int maxPageSize) {
        this.segmentLoaderThreadName = segmentLoaderThreadName;

        setMaxPageSizeWithoutPageChanges(maxPageSize);
    }

    /**
     * Returns the maximum page size.
     *
     * @return the maximum page size.
     */
    public int getMaxPageSize() {
        return maxPageSize;
    }

    /**
     * Sets the maximum size of the page.
     *
     * <p>If the given maximum size is greater than the current maximum size a new page will be
     * loaded, otherwise the current page will be shrunk to meet the given maximum size. In both
     * cases the {@code TableModelListener} will be notified of the change.
     *
     * <p>The call to this method has no effect if the given maximum size is equal to the current
     * maximum size.
     *
     * @param maxPageSize the new maximum page size
     * @throws IllegalArgumentException if {@code maxPageSize} is negative or zero.
     * @see #setMaxPageSizeWithoutPageChanges(int)
     * @see TableModelListener
     */
    public void setMaxPageSize(final int maxPageSize) {
        if (maxPageSize <= 0) {
            throw new IllegalArgumentException("Parameter maxPageSize must be greater than zero.");
        }
        if (this.maxPageSize == maxPageSize) {
            return;
        }
        int oldMaxPageSize = this.maxPageSize;
        setMaxPageSizeWithoutPageChanges(maxPageSize);

        int rowCount = getRowCount();
        if (rowCount > 0) {
            if (maxPageSize > oldMaxPageSize) {
                schedule(dataOffset);
            } else if (data.size() > maxPageSize) {
                final List<T> shrunkData = data.subList(0, maxPageSize);

                EventQueue.invokeLater(
                        new Runnable() {

                            @Override
                            public void run() {
                                setData(dataOffset, new ArrayList<>(shrunkData));
                            }
                        });
            }
        }
    }

    /**
     * Sets the maximum size of the page.
     *
     * <p>As opposed to method {@code #setMaxPageSize(int)} no changes will be made to the current
     * page.
     *
     * @param maxPageSize the new maximum page size
     * @throws IllegalArgumentException if {@code maxPageSize} is negative or zero.
     * @see #setMaxPageSize(int)
     */
    public void setMaxPageSizeWithoutPageChanges(final int maxPageSize) {
        if (maxPageSize <= 0) {
            throw new IllegalArgumentException("Parameter maxPageSize must be greater than zero.");
        }
        this.maxPageSize = maxPageSize;
    }

    @Override
    public void fireTableDataChanged() {
        // clear cached data
        clear();
        super.fireTableDataChanged();
    }

    /**
     * Returns the number of all items.
     *
     * @return number of items
     */
    @Override
    public abstract int getRowCount();

    /**
     * Called by {@link PagingTableModel#getValueAt(int, int)} when requested row is already loaded.
     *
     * @param rowObject the row object
     * @param columnIndex the column index
     * @return value from requested column
     */
    protected abstract Object getRealValueAt(T rowObject, int columnIndex);

    /**
     * Gets the placeholder value that should be shown for the given column, until the actual values
     * are ready to be shown.
     *
     * @param columnIndex the column index
     * @return Value is used to display while loading entry
     */
    protected abstract Object getPlaceholderValueAt(int columnIndex);

    /**
     * Called when a new page is required.
     *
     * <p>The returned {@code List} should support fast (preferably constant time) random access.
     *
     * @param offset the start offset of the page
     * @param length the length of the page
     * @return an excerpt of whole list
     * @see List#get(int)
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

        return getRealValueAt(rowObject, columnIndex);
    }

    /**
     * Gets the object at the given row.
     *
     * @param rowIndex the index of the row
     * @return {@code null} if object is not in the current page
     */
    protected T getRowObject(int rowIndex) {
        int pageIndex = rowIndex - dataOffset;
        if (pageIndex >= 0 && pageIndex < data.size()) {
            return data.get(pageIndex);
        }
        return null;
    }

    /**
     * Schedule the loading of the neighborhood around offset (if not already scheduled).
     *
     * @param offset the offset row
     */
    private void schedule(int offset) {
        if (isPending(offset)) {
            return;
        }

        int startOffset = Math.max(0, offset - maxPageSize / 2);
        int length = offset + maxPageSize / 2 - startOffset;

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

        Segment low = new Segment(offset - maxPageSize, 0);
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
        Segment seg = new Segment(startOffset, length);
        pending.add(seg);

        SegmentLoaderThread segmentLoader = new SegmentLoaderThread(seg, segmentLoaderThreadName);
        segmentLoader.start();
    }

    /**
     * Sets the given {@code page} as the currently loaded data and notifies the table model
     * listeners of the rows updated.
     *
     * <p><strong>Note:</strong> This method must be call on the EDT, failing to do so might result
     * in GUI state inconsistencies.
     *
     * @param offset the start offset of the given {@code page}
     * @param page the new data
     * @see EventQueue#invokeLater(Runnable)
     * @see TableModelListener
     */
    private void setData(int offset, List<T> page) {
        int lastRow = offset + page.size() - 1;
        dataOffset = offset;
        data = page;
        fireTableRowsUpdated(offset, lastRow);
    }

    protected void clear() {
        data.clear();
        data = Collections.emptyList();
        pending.clear();
    }

    /**
     * This class is used to keep track of which rows have been scheduled for loading, so that rows
     * don't get scheduled twice concurrently. The idea is to store Segments in a sorted data
     * structure for fast searching.
     *
     * <p>The compareTo() method sorts first by base position, then by length.
     */
    static final class Segment implements Comparable<Segment> {
        private final int base;
        private final int length;

        public Segment(int base, int length) {
            this.base = base;
            this.length = length;
        }

        public int getBase() {
            return base;
        }

        public int getLength() {
            return length;
        }

        public boolean contains(int pos) {
            return (base <= pos && pos < base + length);
        }

        @Override
        public boolean equals(Object o) {
            if (o != null && o instanceof Segment) {
                Segment s = (Segment) o;

                boolean hasSameBase = (base == s.base);
                boolean hasSameLength = (length == s.length);

                return hasSameBase && hasSameLength;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return (41 * (41 + base) + length);
        }

        @Override
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

    private class SegmentLoaderThread extends Thread {

        private final Segment segment;

        public SegmentLoaderThread(Segment segment, String name) {
            super(name);

            this.segment = segment;
        }

        @Override
        public void run() {
            final List<T> page;

            try {
                page = loadPage(segment.getBase(), segment.getLength());
            } catch (Exception e) {
                logger.warn("error retrieving page at {}: aborting", segment.getBase(), e);
                pending.remove(segment);
                return;
            }

            EventQueue.invokeLater(
                    new Runnable() {

                        @Override
                        public void run() {
                            setData(segment.getBase(), page);
                            pending.remove(segment);
                        }
                    });
        }
    }
}
