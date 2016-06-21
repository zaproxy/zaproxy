/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2014 The ZAP Development Team
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
package org.zaproxy.zap.view.table;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;
import javax.swing.SortOrder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.IconValues;
import org.jdesktop.swingx.renderer.MappedValue;
import org.jdesktop.swingx.renderer.StringValues;
import org.jdesktop.swingx.table.ColumnFactory;
import org.jdesktop.swingx.table.TableColumnExt;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.utils.PagingTableModel;
import org.zaproxy.zap.view.ZapTable;
import org.zaproxy.zap.view.messagecontainer.http.DefaultSelectableHistoryReferencesContainer;
import org.zaproxy.zap.view.messagecontainer.http.SelectableHistoryReferencesContainer;
import org.zaproxy.zap.view.renderer.DateFormatStringValue;
import org.zaproxy.zap.view.renderer.SizeBytesStringValue;
import org.zaproxy.zap.view.renderer.TimeDurationStringValue;
import org.zaproxy.zap.view.table.HistoryReferencesTableModel.Column;
import org.zaproxy.zap.view.table.decorator.AlertRiskTableCellItemIconHighlighter;
import org.zaproxy.zap.view.table.decorator.NoteTableCellItemIconHighlighter;

/**
 * A table specialised in showing data from {@code HistoryReference}s obtained from {@code HistoryReferencesTableModel}s.
 */
public class HistoryReferencesTable extends ZapTable {

    private static final long serialVersionUID = -6988769961088738602L;

    private static final Logger LOGGER = Logger.getLogger(HistoryReferencesTable.class);

    private static final HistoryReferencesTableColumnFactory DEFAULT_COLUMN_FACTORY = new HistoryReferencesTableColumnFactory();

    private static final int MAXIMUM_ROWS_FOR_TABLE_CONFIG = 75;

    private int maximumRowsForTableConfig;

    public HistoryReferencesTable() {
        this(new DefaultHistoryReferencesTableModel());
    }

    public HistoryReferencesTable(final Column[] columns) {
        this(new DefaultHistoryReferencesTableModel(columns));
    }

    public HistoryReferencesTable(HistoryReferencesTableModel<?> model) {
        this(model, true);
    }

    public HistoryReferencesTable(HistoryReferencesTableModel<?> model, boolean useDefaultSelectionListener) {
        super(model);

        maximumRowsForTableConfig = MAXIMUM_ROWS_FOR_TABLE_CONFIG;

        setName("GenericHistoryReferenceTable");

        installColumnFactory();

        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        setSortOrderCycle(SortOrder.ASCENDING, SortOrder.DESCENDING, SortOrder.UNSORTED);

        setColumnSelectionAllowed(false);
        setCellSelectionEnabled(false);
        setRowSelectionAllowed(true);

        if (useDefaultSelectionListener) {
            getSelectionModel().addListSelectionListener(new DisplayMessageOnSelectionValueChange());
        }

        setComponentPopupMenu(new CustomPopupMenu());
    }

    @Override
    protected void createDefaultRenderers() {
        super.createDefaultRenderers();

        setDefaultRenderer(Date.class, new DefaultTableRenderer(new DateFormatStringValue()));
    }

    protected void installColumnFactory() {
        setColumnFactory(DEFAULT_COLUMN_FACTORY);
        createDefaultColumnsFromModel();
        initializeColumnWidths();
    }

    protected void displayMessage(final HttpMessage msg) {
        View.getSingleton().displayMessage(msg);
    }

    public HistoryReference getSelectedHistoryReference() {
        final int selectedRow = getSelectedRow();
        if (selectedRow != -1) {
            return getHistoryReferenceAtViewRow(selectedRow);
        }
        return null;
    }

    public List<HistoryReference> getSelectedHistoryReferences() {
        final int[] rows = this.getSelectedRows();
        if (rows.length == 0) {
            return Collections.emptyList();
        }

        final List<HistoryReference> hrefList = new ArrayList<>(rows.length);
        for (int row : rows) {
            HistoryReference hRef = getHistoryReferenceAtViewRow(row);
            if (hRef != null) {
                hrefList.add(hRef);
            }
        }
        return hrefList;
    }

    protected HistoryReference getHistoryReferenceAtViewRow(final int row) {
        HistoryReferencesTableEntry entry = getModel().getEntry(convertRowIndexToModel(row));
        if (entry != null) {
            return entry.getHistoryReference();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Overridden to only accept models of type {@code HistoryReferencesTableModel}. If the given model extends from
     * {@code PagingTableModel} the maximum page size will be used to set the maximum rows for table configuration.
     * 
     * @throws IllegalArgumentException if the {@code dataModel} is not a {@code HistoryReferencesTableModel}.
     * @see PagingTableModel
     * @see PagingTableModel#getMaxPageSize()
     * @see #setMaximumRowsForTableConfiguration(int)
     */
    @Override
    public void setModel(final TableModel dataModel) {
        if (!(dataModel instanceof HistoryReferencesTableModel)) {
            throw new IllegalArgumentException("Parameter dataModel must be a subclass of HistoryReferencesTableModel.");
        }

        if (dataModel instanceof PagingTableModel) {
            setMaximumRowsForTableConfiguration(((PagingTableModel<?>) dataModel).getMaxPageSize());
        }

        super.setModel(dataModel);
    }

    @Override
    public HistoryReferencesTableModel<?> getModel() {
        return (HistoryReferencesTableModel<?>) super.getModel();
    }

    /**
     * Sets the maximum rows that should be taken into account when configuring the table (for example, packing the columns).
     * 
     * @param maximumRows the maximum rows that should be taken into account when configuring the table
     * @see #packAll()
     */
    public void setMaximumRowsForTableConfiguration(int maximumRows) {
        this.maximumRowsForTableConfig = maximumRows;
    }

    /**
     * Returns the maximum rows that will be taken into account when configuring the table (for example, packing the columns).
     * 
     * @return the maximum rows that will be taken into account when configuring the table
     */
    public int getMaximumRowsForTableConfiguration() {
        return maximumRowsForTableConfig;
    }

    public void selectHistoryReference(final int historyReferenceId) {
        final int modelRowIndex = getModel().getEntryRowIndex(historyReferenceId);

        if (modelRowIndex > -1) {
            final int viewRowIndex = convertRowIndexToView(modelRowIndex);
            this.getSelectionModel().setSelectionInterval(viewRowIndex, viewRowIndex);
            this.scrollRowToVisible(viewRowIndex);
        }
    }

    protected class DisplayMessageOnSelectionValueChange implements ListSelectionListener {

        @Override
        public void valueChanged(final ListSelectionEvent evt) {
            if (!evt.getValueIsAdjusting()) {
                HistoryReference hRef = getSelectedHistoryReference();
                if (hRef == null) {
                    return;
                }

                boolean focusOwner = isFocusOwner();
                try {
                    displayMessage(hRef.getHttpMessage());
                } catch (HttpMalformedHeaderException | DatabaseException e) {
                    LOGGER.error(e.getMessage(), e);
                } finally {
                    if (focusOwner) {
                        requestFocusInWindow();
                    }
                }
            }
        }
    }

    protected class CustomPopupMenu extends JPopupMenu {

        private static final long serialVersionUID = 1L;

        @Override
        public void show(Component invoker, int x, int y) {
            SelectableHistoryReferencesContainer messageContainer = new DefaultSelectableHistoryReferencesContainer(
                    HistoryReferencesTable.this.getName(),
                    HistoryReferencesTable.this,
                    Collections.<HistoryReference> emptyList(),
                    getSelectedHistoryReferences());
            View.getSingleton().getPopupMenu().show(messageContainer, x, y);
        }

        /**
         * Returns the selected history references.
         * <p>
         * Defaults to call {@code HistoryReferencesTable#getSelectedHistoryReferences()}
         * </p>
         * 
         * @return the selected history references.
         * @see HistoryReferencesTable#getSelectedHistoryReferences()
         */
        protected List<HistoryReference> getSelectedHistoryReferences() {
            return HistoryReferencesTable.this.getSelectedHistoryReferences();
        }
    }

    protected static class HistoryReferencesTableColumnFactory extends ColumnFactory {

        public HistoryReferencesTableColumnFactory() {
        }

        @Override
        protected int getRowCount(final JXTable table) {
            final int rowCount = super.getRowCount(table);
            final int maxRowCount = ((HistoryReferencesTable) table).getMaximumRowsForTableConfiguration();
            if (maxRowCount > 0 && rowCount > maxRowCount) {
                return maxRowCount;
            }
            return rowCount;
        }

        @Override
        public void configureTableColumn(final TableModel model, final TableColumnExt columnExt) {
            super.configureTableColumn(model, columnExt);

            HistoryReferencesTableModel<?> hRefModel = (HistoryReferencesTableModel<?>) model;

            columnExt.setPrototypeValue(hRefModel.getPrototypeValue(columnExt.getModelIndex()));

            final int highestAlertColumnIndex = hRefModel.getColumnIndex(Column.HIGHEST_ALERT);
            if (highestAlertColumnIndex != -1) {
                if (columnExt.getModelIndex() == highestAlertColumnIndex
                        && model.getColumnClass(highestAlertColumnIndex) == AlertRiskTableCellItem.class) {
                    columnExt.setHighlighters(new AlertRiskTableCellItemIconHighlighter(highestAlertColumnIndex));
                }
            }

            final int rttColumnIndex = hRefModel.getColumnIndex(Column.RTT);
            if (rttColumnIndex != -1) {
                if (columnExt.getModelIndex() == rttColumnIndex
                        && TimeDurationStringValue.isTargetClass(model.getColumnClass(rttColumnIndex))) {
                    columnExt.setCellRenderer(new DefaultTableRenderer(new TimeDurationStringValue()));
                }
            }

            final int noteColumnIndex = hRefModel.getColumnIndex(Column.NOTE);
            if (noteColumnIndex != -1) {
                if (columnExt.getModelIndex() == noteColumnIndex && model.getColumnClass(noteColumnIndex) == Boolean.class) {
                    columnExt.setCellRenderer(new DefaultTableRenderer(
                            new MappedValue(StringValues.EMPTY, IconValues.NONE),
                            JLabel.CENTER));
                    columnExt.setHighlighters(new NoteTableCellItemIconHighlighter(noteColumnIndex));
                }
            }
            
            installSizeBytesRenderer(columnExt, hRefModel.getColumnIndex(Column.SIZE_MESSAGE), model);
            installSizeBytesRenderer(columnExt, hRefModel.getColumnIndex(Column.SIZE_REQUEST_HEADER), model);
            installSizeBytesRenderer(columnExt, hRefModel.getColumnIndex(Column.SIZE_REQUEST_BODY), model);
            installSizeBytesRenderer(columnExt, hRefModel.getColumnIndex(Column.SIZE_RESPONSE_HEADER), model);
            installSizeBytesRenderer(columnExt, hRefModel.getColumnIndex(Column.SIZE_RESPONSE_BODY), model);
        }
        
        private void installSizeBytesRenderer(TableColumnExt columnExt, int columnIndex, TableModel model) {
            if (columnIndex != -1) {
                if (columnExt.getModelIndex() == columnIndex
                        && SizeBytesStringValue.isTargetClass(model.getColumnClass(columnIndex))) {
                    columnExt.setCellRenderer(new DefaultTableRenderer(new SizeBytesStringValue()));
                }
            }
        }
    }
}
