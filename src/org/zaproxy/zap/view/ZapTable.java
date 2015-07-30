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
import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.action.AbstractActionExt;
import org.jdesktop.swingx.table.ColumnControlButton;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.utils.StickyScrollbarAdjustmentListener;

/**
 * An enhanced {@code JXTable}. It has the following enhancements:
 * <ul>
 * <li>Ensures that the right-clicked row is selected before showing context menu (for installed JPopupMenu);</li>
 * <li>Allows to enable auto-scroll on new values when scroll bar is at bottom of the table (enabled by default);</li>
 * </ul>
 * 
 * @since 2.4.1
 * @see JXTable
 * @see #setComponentPopupMenu(JPopupMenu)
 * @see #setAutoScrollOnNewValues(boolean)
 */
public class ZapTable extends JXTable {

    private static final long serialVersionUID = 8303870012122236918L;

    private boolean autoScroll;
    private AutoScrollAction autoScrollAction;
    private StickyScrollbarAdjustmentListener autoScrollScrollbarAdjustmentListener;

    public ZapTable() {
        super();
        init();
    }

    public ZapTable(TableModel dataModel) {
        super(dataModel);

        init();
    }

    private void init() {
        setDoubleBuffered(true);
        setColumnControlVisible(true);

        JComponent columnControl = getColumnControl();
        if (columnControl instanceof ZapColumnControlButton) {
            ZapColumnControlButton zapColumnControl = ((ZapColumnControlButton) columnControl);
            zapColumnControl.addAction(getAutoScrollAction());
            zapColumnControl.populatePopup();
        }

        setAutoScrollOnNewValues(true);
    }

    protected AutoScrollAction getAutoScrollAction() {
        if (autoScrollAction == null) {
            autoScrollAction = new AutoScrollAction(this);
        }
        return autoScrollAction;
    }

    /**
     * Sets if the vertical scroll bar of the wrapper {@code JScrollPane} should be automatically scrolled on new values.
     * <p>
     * Default value is to {@code true}.
     * 
     * @param autoScroll {@code true} if vertical scroll bar should be automatically scrolled on new values, {@code false}
     *            otherwise.
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
            autoScrollAction.putValue(Action.SELECTED_KEY, Boolean.valueOf(autoScroll));
        }

        if (this.autoScroll) {
            addAutoScrollScrollbarAdjustmentListener();
        }
    }

    /**
     * Tells whether or not the vertical scroll bar of the wrapper {@code JScrollPane} is automatically scrolled on new values.
     * 
     * @return {@code true} if the vertical scroll bar is automatically scrolled on new values, {@code false} otherwise.
     * @see #setAutoScrollOnNewValues(boolean)
     */
    public boolean isAutoScrollOnNewValues() {
        return autoScroll;
    }

    private void addAutoScrollScrollbarAdjustmentListener() {
        JScrollPane scrollPane = getEnclosingScrollPane();
        if (scrollPane != null && autoScrollScrollbarAdjustmentListener == null) {
            autoScrollScrollbarAdjustmentListener = new StickyScrollbarAdjustmentListener();
            scrollPane.getVerticalScrollBar().addAdjustmentListener(autoScrollScrollbarAdjustmentListener);
        }
    }

    private void removeAutoScrollScrollbarAdjustmentListener() {
        JScrollPane scrollPane = getEnclosingScrollPane();
        if (scrollPane != null && autoScrollScrollbarAdjustmentListener != null) {
            scrollPane.getVerticalScrollBar().removeAdjustmentListener(autoScrollScrollbarAdjustmentListener);
            autoScrollScrollbarAdjustmentListener = null;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Overridden to set auto-scroll on new values, if enabled.
     * </p>
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
     * <p>
     * Overridden to unset auto-scroll on new values, if enabled.
     * </p>
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
     * <p>
     * Overridden to take into account for possible parent {@code JLayer}s.
     * </p>
     * 
     * @see javax.swing.JLayer
     */
    // Note: Same implementation as in JXTable#getEnclosingScrollPane() but changed to get the parent and viewport view using
    // the methods SwingUtilities#getUnwrappedParent(Component) and SwingUtilities#getUnwrappedView(JViewport) respectively.
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
        protected void populatePopup() {
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
            putValue(Action.SHORT_DESCRIPTION, Constant.messages.getString("view.table.autoscroll.tooltip"));

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
}
