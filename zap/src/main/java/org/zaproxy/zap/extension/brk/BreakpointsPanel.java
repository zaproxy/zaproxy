/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2010 The ZAP Development Team
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
package org.zaproxy.zap.extension.brk;

import java.awt.CardLayout;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdesktop.swingx.JXTable;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.view.View;

@SuppressWarnings("serial")
public class BreakpointsPanel extends AbstractPanel {

    private static final long serialVersionUID = 1L;

    public static final String PANEL_NAME = "breakpoints";

    private ExtensionBreak extension;
    private javax.swing.JPanel panelCommand = null;
    private javax.swing.JLabel jLabel = null;
    private JScrollPane jScrollPane = null;
    private JXTable breakpointsTable = null;
    private BreakpointsTableModel model = new BreakpointsTableModel();

    private static final String BRK_TABLE = "brk.table";
    private static final String PREF_COLUMN_WIDTH = "column.width";
    private final Preferences preferences;
    private final String prefnzPrefix = this.getClass().getSimpleName() + ".";

    private static Logger log = LogManager.getLogger(BreakpointsPanel.class);

    public BreakpointsPanel(ExtensionBreak extension) {
        super();
        this.extension = extension;
        this.preferences = Preferences.userNodeForPackage(getClass());

        initialize();
    }

    private void initialize() {
        this.setLayout(new CardLayout());
        this.setSize(474, 251);
        this.setName(Constant.messages.getString("brk.panel.title"));
        this.setIcon(
                new ImageIcon(
                        BreakpointsPanel.class.getResource(
                                "/resource/icon/16/101.png"))); // 'red X' icon
        this.setDefaultAccelerator(
                extension
                        .getView()
                        .getMenuShortcutKeyStroke(
                                KeyEvent.VK_B,
                                KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK,
                                false));
        this.setMnemonic(Constant.messages.getChar("brk.panel.mnemonic"));
        this.add(getPanelCommand(), getPanelCommand().getName());
    }

    private javax.swing.JPanel getPanelCommand() {
        if (panelCommand == null) {

            panelCommand = new javax.swing.JPanel();
            panelCommand.setLayout(new java.awt.GridBagLayout());
            panelCommand.setName(Constant.messages.getString("brk.panel.title"));

            jLabel = getJLabel();
            GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            GridBagConstraints gridBagConstraints2 = new GridBagConstraints();

            // Better without this?
            // jLabel.setText("Breakpoints:");
            gridBagConstraints1.gridx = 0;
            gridBagConstraints1.gridy = 0;
            gridBagConstraints1.insets = new java.awt.Insets(2, 2, 2, 2);
            gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints1.weightx = 1.0D;
            gridBagConstraints2.gridx = 0;
            gridBagConstraints2.gridy = 1;
            gridBagConstraints2.weightx = 1.0;
            gridBagConstraints2.weighty = 1.0;
            gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints2.insets = new java.awt.Insets(0, 0, 0, 0);
            gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTHWEST;

            // panelCommand.add(jLabel, gridBagConstraints1);
            panelCommand.add(getJScrollPane(), gridBagConstraints2);
        }
        return panelCommand;
    }

    private javax.swing.JLabel getJLabel() {
        if (jLabel == null) {
            jLabel = new javax.swing.JLabel();
            jLabel.setText(" ");
        }
        return jLabel;
    }

    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setViewportView(getBreakpoints());
        }
        return jScrollPane;
    }

    protected JXTable getBreakpoints() {
        if (breakpointsTable == null) {
            breakpointsTable = new JXTable(model);

            breakpointsTable.setColumnSelectionAllowed(false);
            breakpointsTable.setCellSelectionEnabled(false);
            breakpointsTable.setRowSelectionAllowed(true);
            breakpointsTable.setColumnControlVisible(true);

            breakpointsTable
                    .getColumnModel()
                    .getColumn(0)
                    .setPreferredWidth(restoreColumnWidth(BRK_TABLE, 100));
            breakpointsTable
                    .getColumnModel()
                    .getColumn(0)
                    .addPropertyChangeListener(new ColumnResizedListener(BRK_TABLE));
            breakpointsTable.getColumnModel().getColumn(0).setMaxWidth(250);

            breakpointsTable.getTableHeader().setReorderingAllowed(false);

            breakpointsTable.setName(PANEL_NAME);
            breakpointsTable.setDoubleBuffered(true);
            breakpointsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
            breakpointsTable.addMouseListener(
                    new java.awt.event.MouseAdapter() {
                        @Override
                        public void mousePressed(java.awt.event.MouseEvent e) {

                            showPopupMenuIfTriggered(e);
                        }

                        @Override
                        public void mouseReleased(java.awt.event.MouseEvent e) {
                            showPopupMenuIfTriggered(e);
                        }

                        private void showPopupMenuIfTriggered(java.awt.event.MouseEvent e) {
                            if (e.isPopupTrigger()) {

                                // Select table item
                                int row = breakpointsTable.rowAtPoint(e.getPoint());
                                if (row < 0
                                        || !breakpointsTable
                                                .getSelectionModel()
                                                .isSelectedIndex(row)) {
                                    breakpointsTable.getSelectionModel().clearSelection();
                                    if (row >= 0) {
                                        breakpointsTable
                                                .getSelectionModel()
                                                .setSelectionInterval(row, row);
                                    }
                                }

                                View.getSingleton()
                                        .getPopupMenu()
                                        .show(e.getComponent(), e.getX(), e.getY());
                            }
                        }

                        @Override
                        public void mouseClicked(java.awt.event.MouseEvent e) {
                            if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() > 1) {
                                // Its a double click
                                extension.editUiSelectedBreakpoint();
                            }
                        }
                    });
        }
        return breakpointsTable;
    }

    public BreakpointMessageInterface getSelectedBreakpoint() {
        int selectedRow = breakpointsTable.getSelectedRow();
        if (selectedRow != -1) {
            return model.getBreakpointAtRow(breakpointsTable.convertRowIndexToModel(selectedRow));
        }
        return null;
    }

    private void selectRowAndEnsureVisible(int row) {
        if (row != -1) {
            breakpointsTable.getSelectionModel().setSelectionInterval(row, row);
            breakpointsTable.scrollRectToVisible(breakpointsTable.getCellRect(row, 0, true));
        }
    }

    private void addBreakpointModel(BreakpointMessageInterface breakpoint) {
        model.addBreakpoint(breakpoint);
        selectRowAndEnsureVisible(model.getLastAffectedRow());
    }

    void addBreakpoint(final BreakpointMessageInterface breakpoint) {
        if (EventQueue.isDispatchThread()) {
            addBreakpointModel(breakpoint);
            return;
        }
        try {
            EventQueue.invokeAndWait(
                    new Runnable() {
                        @Override
                        public void run() {
                            addBreakpointModel(breakpoint);
                        }
                    });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void editBreakpointModel(
            BreakpointMessageInterface oldBreakpoint, BreakpointMessageInterface newBreakpoint) {
        model.editBreakpoint(oldBreakpoint, newBreakpoint);
        selectRowAndEnsureVisible(model.getLastAffectedRow());
    }

    void editBreakpoint(
            final BreakpointMessageInterface oldBreakpoint,
            final BreakpointMessageInterface newBreakpoint) {
        if (EventQueue.isDispatchThread()) {
            editBreakpointModel(oldBreakpoint, newBreakpoint);
            return;
        }
        try {
            EventQueue.invokeAndWait(
                    new Runnable() {
                        @Override
                        public void run() {
                            editBreakpointModel(oldBreakpoint, newBreakpoint);
                        }
                    });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void removeBreakpointModel(BreakpointMessageInterface breakpoint) {
        model.removeBreakpoint(breakpoint);
    }

    public void removeBreakpoint(final BreakpointMessageInterface breakpoint) {
        if (EventQueue.isDispatchThread()) {
            removeBreakpointModel(breakpoint);
            return;
        }
        try {
            EventQueue.invokeAndWait(
                    new Runnable() {
                        @Override
                        public void run() {
                            removeBreakpointModel(breakpoint);
                        }
                    });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void saveColumnWidth(String prefix, int width) {
        if (width > 0) {
            log.debug(
                    "Saving preference {}{}.{}={}", prefnzPrefix, prefix, PREF_COLUMN_WIDTH, width);
            this.preferences.put(
                    prefnzPrefix + prefix + "." + PREF_COLUMN_WIDTH, Integer.toString(width));
            // immediate flushing
            try {
                this.preferences.flush();
            } catch (final BackingStoreException e) {
                log.error("Error while saving the preferences", e);
            }
        }
    }

    private int restoreColumnWidth(String prefix, int fallback) {
        int result = fallback;
        final String sizestr =
                preferences.get(prefnzPrefix + prefix + "." + PREF_COLUMN_WIDTH, null);
        if (sizestr != null) {
            int width = 0;
            try {
                width = Integer.parseInt(sizestr.trim());
            } catch (final Exception e) {
                // ignoring, cause is prevented by default values;
            }
            if (width > 0) {
                result = width;
                log.debug(
                        "Restoring preference {}{}.{}={}",
                        prefnzPrefix,
                        prefix,
                        PREF_COLUMN_WIDTH,
                        width);
            }
        }
        return result;
    }

    private final class ColumnResizedListener implements PropertyChangeListener {

        private final String prefix;

        public ColumnResizedListener(String prefix) {
            super();
            assert prefix != null;
            this.prefix = prefix;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            TableColumn column = (TableColumn) evt.getSource();
            if (column != null) {
                log.debug("{}{}.{}={}", prefnzPrefix, prefix, PREF_COLUMN_WIDTH, column.getWidth());
                saveColumnWidth(prefix, column.getWidth());
            }
        }
    }
}
