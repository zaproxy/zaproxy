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
package org.zaproxy.zap.view;

import java.awt.Container;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.action.AbstractActionExt;
import org.jdesktop.swingx.table.ColumnControlButton;
import org.jdesktop.swingx.table.DefaultTableColumnModelExt;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.zaproxy.zap.utils.StickyScrollbarAdjustmentListener;
import org.zaproxy.zap.utils.TableExportAction;

/**
 * An enhanced {@code JXTable}. It has the following enhancements:
 *
 * <ul>
 *   <li>Ensures that the right-clicked row is selected before showing context menu (for installed
 *       JPopupMenu);
 *   <li>Allows to enable auto-scroll on new values when scroll bar is at bottom of the table
 *       (enabled by default);
 *   <li>Has an export action, under the {@link org.jdesktop.swingx.table.ColumnControlPopup
 *       ColumnControlPopup};
 * </ul>
 *
 * @since 2.4.1
 * @see JXTable
 * @see #setComponentPopupMenu(JPopupMenu)
 * @see #setAutoScrollOnNewValues(boolean)
 */
@SuppressWarnings("serial")
public class ZapTable extends JXTable {

    private static final long serialVersionUID = 8303870012122236918L;
    private static final String COLUMN_NAME = "ColumnName";
    private static final String COLUMN_INDEX = "ColumnIndex";
    private static final String COLUMN_MODEL_INDEX = "ColumnModelIndex";
    private static final String COLUMN_CONFIGURATION = "ColumnConfiguration_";
    private static final String COLUMN = "Column";

    private boolean autoScroll;
    private AutoScrollAction autoScrollAction;
    private StickyScrollbarAdjustmentListener autoScrollScrollbarAdjustmentListener;
    private String persistanceIdentifier;
    protected TableColumnConfiguration config = null;

    public ZapTable() {
        super();
        init();
    }

    public ZapTable(TableModel dataModel) {
        super(dataModel);

        init();
    }

    public void persistColumnConfiguration() {
        if (StringUtils.isEmpty(this.persistanceIdentifier)) {
            return;
        }
        config = new TableColumnConfiguration();
        List<TableColumn> columns = ((DefaultTableColumnModelExt) columnModel).getColumns(false);
        for (TableColumn column : columns) {
            if (getColumnExt(column.getIdentifier()).isVisible()) {
                int index = columns.indexOf(column);
                config.addColumn(
                        new TableColumnConfig(
                                column.getIdentifier().toString(), index, column.getModelIndex()));
            }
        }
        config.saveToConfig(this.persistanceIdentifier);
    }

    protected void loadColumnConfiguration(String persistanceIdentifier) {
        TableColumnConfiguration config =
                TableColumnConfiguration.loadFromConfig(persistanceIdentifier);
        this.persistanceIdentifier = persistanceIdentifier;
        this.config = config;
        if (config != null) {
            config.apply(this);
        }
    }

    protected void applyDefaultColumnConfigurations() {}

    private void init() {
        setDoubleBuffered(true);
        setColumnControlVisible(true);

        JComponent columnControl = getColumnControl();
        if (columnControl instanceof ZapColumnControlButton) {
            ZapColumnControlButton zapColumnControl = ((ZapColumnControlButton) columnControl);
            autoScrollAction = createAutoScrollAction();
            if (autoScrollAction != null) {
                zapColumnControl.addAction(autoScrollAction);
            }
            TableExportAction<ZapTable> exportAction = createTableExportAction();
            if (exportAction != null) {
                zapColumnControl.addAction(exportAction);
            }
            zapColumnControl.addAction(new ResetColumnsToDefaultAction(this));
            zapColumnControl.populatePopup();
        }

        setAutoScrollOnNewValues(true);
    }

    /**
     * Creates the action to change the state of auto scroll on new values.
     *
     * <p>Called when customising the {@link ZapColumnControlButton}.
     *
     * @return the action to change the state of the auto scroll on new values, might be {@code
     *     null}.
     * @since 2.7.0
     * @see #setAutoScrollOnNewValues(boolean)
     */
    protected AutoScrollAction createAutoScrollAction() {
        return new AutoScrollAction(this);
    }

    /**
     * Gets the action to change the state of auto scroll on new values.
     *
     * @return the action to change the state of the auto scroll on new values, might be {@code
     *     null}.
     */
    protected AutoScrollAction getAutoScrollAction() {
        return autoScrollAction;
    }

    /**
     * Creates the action to export the table.
     *
     * <p>Called when customising the {@link ZapColumnControlButton}.
     *
     * @return the action to export the table, might be {@code null}.
     * @since 2.7.0
     * @see #getColumnControl()
     */
    protected TableExportAction<ZapTable> createTableExportAction() {
        return new TableExportAction<>(this);
    }

    /**
     * Sets if the vertical scroll bar of the wrapper {@code JScrollPane} should be automatically
     * scrolled on new values.
     *
     * <p>Default value is to {@code true}.
     *
     * @param autoScroll {@code true} if vertical scroll bar should be automatically scrolled on new
     *     values, {@code false} otherwise.
     */
    public void setAutoScrollOnNewValues(boolean autoScroll) {
        if (this.autoScroll == autoScroll) {
            return;
        }
        if (this.autoScroll) {
            removeAutoScrollScrollbarAdjustmentListener();
        }

        this.autoScroll = autoScroll;

        if (autoScrollAction != null) {
            autoScrollAction.putValue(Action.SELECTED_KEY, autoScroll);
        }

        if (this.autoScroll) {
            addAutoScrollScrollbarAdjustmentListener();
        }
    }

    /**
     * Tells whether or not the vertical scroll bar of the wrapper {@code JScrollPane} is
     * automatically scrolled on new values.
     *
     * @return {@code true} if the vertical scroll bar is automatically scrolled on new values,
     *     {@code false} otherwise.
     * @see #setAutoScrollOnNewValues(boolean)
     */
    public boolean isAutoScrollOnNewValues() {
        return autoScroll;
    }

    private void addAutoScrollScrollbarAdjustmentListener() {
        JScrollPane scrollPane = getEnclosingScrollPane();
        if (scrollPane != null && autoScrollScrollbarAdjustmentListener == null) {
            autoScrollScrollbarAdjustmentListener = new StickyScrollbarAdjustmentListener();
            scrollPane
                    .getVerticalScrollBar()
                    .addAdjustmentListener(autoScrollScrollbarAdjustmentListener);
        }
    }

    private void removeAutoScrollScrollbarAdjustmentListener() {
        JScrollPane scrollPane = getEnclosingScrollPane();
        if (scrollPane != null && autoScrollScrollbarAdjustmentListener != null) {
            scrollPane
                    .getVerticalScrollBar()
                    .removeAdjustmentListener(autoScrollScrollbarAdjustmentListener);
            autoScrollScrollbarAdjustmentListener = null;
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Overridden to set auto-scroll on new values, if enabled.
     */
    @Override
    protected void configureEnclosingScrollPane() {
        super.configureEnclosingScrollPane();

        if (isAutoScrollOnNewValues()) {
            addAutoScrollScrollbarAdjustmentListener();
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Overridden to unset auto-scroll on new values, if enabled.
     */
    @Override
    protected void unconfigureEnclosingScrollPane() {
        super.unconfigureEnclosingScrollPane();

        if (isAutoScrollOnNewValues()) {
            removeAutoScrollScrollbarAdjustmentListener();
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Overridden to take into account for possible parent {@code JLayer}s.
     *
     * @see javax.swing.JLayer
     */
    // Note: Same implementation as in JXTable#getEnclosingScrollPane() but changed to get the
    // parent and viewport view using
    // the methods SwingUtilities#getUnwrappedParent(Component) and
    // SwingUtilities#getUnwrappedView(JViewport) respectively.
    @Override
    protected JScrollPane getEnclosingScrollPane() {
        Container p = SwingUtilities.getUnwrappedParent(this);
        if (p instanceof JViewport) {
            Container gp = p.getParent();
            if (gp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) gp;
                // Make certain we are the viewPort's view and not, for
                // example, the rowHeaderView of the scrollPane -
                // an implementor of fixed columns might do this.
                JViewport viewport = scrollPane.getViewport();
                if (viewport == null || SwingUtilities.getUnwrappedView(viewport) != this) {
                    return null;
                }
                return scrollPane;
            }
        }
        return null;
    }

    @Override
    public Point getPopupLocation(final MouseEvent event) {
        // Hack to select the row before showing the pop up menu when invoked using the mouse.
        if (event != null) {
            final int row = rowAtPoint(event.getPoint());
            if (row < 0) {
                getSelectionModel().clearSelection();
            } else if (!getSelectionModel().isSelectedIndex(row)) {
                getSelectionModel().setSelectionInterval(row, row);
            }
        }
        return super.getPopupLocation(event);
    }

    @Override
    protected JComponent createDefaultColumnControl() {
        return new ZapColumnControlButton(this);
    }

    @SuppressWarnings("serial")
    protected static class ZapColumnControlButton extends ColumnControlButton {

        private static final long serialVersionUID = -2888568545235496369L;

        private List<Action> customActions;

        public ZapColumnControlButton(JXTable table) {
            super(table);
        }

        public ZapColumnControlButton(JXTable table, Icon icon) {
            super(table, icon);
        }

        @Override
        public void populatePopup() {
            super.populatePopup();

            if (customActions != null && popup instanceof DefaultColumnControlPopup) {
                ((DefaultColumnControlPopup) popup).addAdditionalActionItems(customActions);
            }
        }

        public void addAction(Action action) {
            if (customActions == null) {
                customActions = new ArrayList<>(1);
            }
            customActions.add(action);
        }
    }

    protected static class AutoScrollAction extends AbstractActionExt {

        private static final long serialVersionUID = 5518182106427836717L;

        private final ZapTable table;

        public AutoScrollAction(ZapTable table) {
            super(Constant.messages.getString("view.table.autoscroll.label"));
            putValue(
                    Action.SHORT_DESCRIPTION,
                    Constant.messages.getString("view.table.autoscroll.tooltip"));

            this.table = table;
        }

        public AutoScrollAction(String label, Icon icon, ZapTable table) {
            super(label, icon);

            this.table = table;
        }

        @Override
        public boolean isStateAction() {
            return true;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            table.setAutoScrollOnNewValues(!table.isAutoScrollOnNewValues());
        }
    }

    protected static class ResetColumnsToDefaultAction extends AbstractActionExt {
        private static final long serialVersionUID = 8724735213507882662L;
        private final ZapTable table;

        public ResetColumnsToDefaultAction(ZapTable table) {
            super(Constant.messages.getString("view.table.resetColumns.label"));
            putValue(
                    Action.SHORT_DESCRIPTION,
                    Constant.messages.getString("view.table.resetColumns.tooltip"));

            this.table = table;
        }

        @Override
        public boolean isStateAction() {
            return false;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            table.createDefaultColumnsFromModel();
            table.applyDefaultColumnConfigurations();
        }
    }

    private static class TableColumnConfiguration {
        private List<TableColumnConfig> columnConfigs = new ArrayList<>();

        public List<TableColumnConfig> getColumnConfigs() {
            return columnConfigs;
        }

        public void addColumn(TableColumnConfig column) {
            TableColumnConfig found = this.findColumnByName(column.getName());
            if (found == null) {
                this.columnConfigs.add(column);
            }
        }

        public void apply(ZapTable table) {
            // order matters here!
            this.rearrangeColumns(table);
            this.hideInvisibleColumns(table);
        }

        private void rearrangeColumns(ZapTable table) {
            for (int i = 0; i < this.columnConfigs.size(); i++) {
                TableColumnConfig meta = this.columnConfigs.get(i);
                javax.swing.table.TableColumn column =
                        findColumnByIndex(table, meta.getModelIndex());
                if (column != null) {
                    int columnIndex = table.columnModel.getColumnIndex(column.getIdentifier());
                    table.moveColumn(columnIndex, meta.getIndex());
                }
            }
        }

        private TableColumn findColumnByIndex(ZapTable table, int modelIndex) {
            List<javax.swing.table.TableColumn> columns =
                    ((DefaultTableColumnModelExt) table.columnModel).getColumns(true);
            for (int i = 0; i < columns.size(); i++) {
                if (columns.get(i).getModelIndex() == modelIndex) {
                    return columns.get(i);
                }
            }
            return null;
        }

        private void hideInvisibleColumns(ZapTable table) {
            List<javax.swing.table.TableColumn> columns =
                    ((DefaultTableColumnModelExt) table.columnModel).getColumns(true);
            for (int i = 0; i < columns.size(); i++) {
                javax.swing.table.TableColumn column = columns.get(i);
                TableColumnConfig meta = findColumnConfigByIndex(column.getModelIndex());
                if (meta == null) {
                    table.getColumnExt(column.getIdentifier()).setVisible(false);
                }
            }
        }

        private TableColumnConfig findColumnByName(String name) {
            for (TableColumnConfig t : this.columnConfigs) {
                if (t.getName().equals(name)) {
                    return t;
                }
            }
            return null;
        }

        private TableColumnConfig findColumnConfigByIndex(int modelIndex) {
            for (TableColumnConfig t : this.columnConfigs) {
                if (t.getModelIndex() == modelIndex) {
                    return t;
                }
            }
            return null;
        }

        public static TableColumnConfiguration loadFromConfig(String persistanceIdentifier) {
            FileConfiguration config = Model.getSingleton().getOptionsParam().getConfig();

            TableColumnConfiguration tableColumnConfig = new TableColumnConfiguration();
            int i = 0;
            while (true) {
                String elementBaseKey = getElementBaseKey(persistanceIdentifier, i);
                Object columnName = config.getProperty(elementBaseKey + COLUMN_NAME);
                Object columnIndex = config.getProperty(elementBaseKey + COLUMN_INDEX);
                Object modelIndex = config.getProperty(elementBaseKey + COLUMN_MODEL_INDEX);
                if (StringUtils.isEmpty((String) columnName)) {
                    break;
                }
                tableColumnConfig.addColumn(
                        new TableColumnConfig(
                                columnName.toString(),
                                Integer.parseInt(columnIndex.toString()),
                                Integer.parseInt(modelIndex.toString())));
                i++;
            }
            if (tableColumnConfig.getColumnConfigs().size() == 0) {
                return null;
            }
            return tableColumnConfig;
        }

        private static String getElementBaseKey(String persistanceIdentifier, int index) {
            return COLUMN_CONFIGURATION + persistanceIdentifier + COLUMN + "(" + index + ").";
        }

        private static FileConfiguration getConfig() {
            return Model.getSingleton().getOptionsParam().getConfig();
        }

        public void saveToConfig(String persistanceIdentifier) {

            clearConfigSection(persistanceIdentifier);
            for (int i = 0; i < columnConfigs.size(); i++) {
                TableColumnConfig column = columnConfigs.get(i);
                String elementBaseKey = getElementBaseKey(persistanceIdentifier, i);
                getConfig().setProperty(elementBaseKey + COLUMN_NAME, column.getName());
                getConfig().setProperty(elementBaseKey + COLUMN_INDEX, column.getIndex());
                getConfig()
                        .setProperty(elementBaseKey + COLUMN_MODEL_INDEX, column.getModelIndex());
            }
        }

        private static void clearConfigSection(String persistanceIdentifier) {
            TableColumnConfiguration tableColumnConfig = loadFromConfig(persistanceIdentifier);
            if (tableColumnConfig == null) {
                return;
            }
            HierarchicalConfiguration config = (HierarchicalConfiguration) getConfig();
            config.clearTree(COLUMN_CONFIGURATION + persistanceIdentifier);
        }
    }

    private static class TableColumnConfig {
        private String name;
        private int index;
        private int modelIndex;

        TableColumnConfig(String name, int index, int modelIndex) {
            this.setName(name);
            this.setIndex(index);
            this.setModelIndex(modelIndex);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public int getModelIndex() {
            return modelIndex;
        }

        public void setModelIndex(int modelIndex) {
            this.modelIndex = modelIndex;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            TableColumnConfig that = (TableColumnConfig) o;

            if (index != that.index) {
                return false;
            }
            return name.equals(that.name);
        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + index;
            return result;
        }
    }
}
