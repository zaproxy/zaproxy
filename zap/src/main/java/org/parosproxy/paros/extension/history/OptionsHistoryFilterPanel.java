/*
 *  Zed Attack Proxy (ZAP) and its related class files.
 *
 *  ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 *  Copyright 2018 The ZAP Development Team
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.parosproxy.paros.extension.history;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SortOrder;
import org.jdesktop.swingx.table.TableColumnExt;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.view.AbstractMultipleOptionsTablePanel;

public class OptionsHistoryFilterPanel extends AbstractParamPanel {

    public static final String PANEL_NAME =
            Constant.messages.getString("history.filter.options.title");
    private static final long serialVersionUID = 1L;

    private HistoryFilterTableModel historyFilterTableModel;
    private HistoryFilterMultipleOptionsTablePanel historyFilterTablePanel;

    public OptionsHistoryFilterPanel() {
        super();
        initialize();
    }

    private void initialize() {
        this.setName(PANEL_NAME);
        this.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.BOTH;

        this.add(new JLabel(Constant.messages.getString("history.filter.options.header")), gbc);

        historyFilterTablePanel =
                new HistoryFilterMultipleOptionsTablePanel(getHistoryFilterTableModel());

        gbc.weighty = 1.0;
        this.add(historyFilterTablePanel, gbc);
    }

    @Override
    public void initParam(Object obj) {
        OptionsParam optionsParam = (OptionsParam) obj;
        HistoryFilterParam param = optionsParam.getParamSet(HistoryFilterParam.class);
        getHistoryFilterTableModel().setFilters(param.getFilters());
        historyFilterTablePanel.setRemoveWithoutConfirmation(!param.isConfirmRemoveToken());
    }

    @Override
    public void validateParam(Object obj) throws Exception {
        List<String> filters =
                getHistoryFilterTableModel().getElements().stream()
                        .map(f -> f.getName())
                        .collect(Collectors.toList());

        if (filters.size() != filters.stream().distinct().count()) {
            throw new IllegalArgumentException(
                    Constant.messages.getString("history.filter.save.dialog.unique"));
        }
    }

    @Override
    public void saveParam(Object obj) throws Exception {
        OptionsParam optionsParam = (OptionsParam) obj;
        HistoryFilterParam param = optionsParam.getParamSet(HistoryFilterParam.class);
        param.setFilters(getHistoryFilterTableModel().getElements());
        param.setConfirmRemoveToken(!historyFilterTablePanel.isRemoveWithoutConfirmation());
    }

    private HistoryFilterTableModel getHistoryFilterTableModel() {
        if (historyFilterTableModel == null) {
            historyFilterTableModel = new HistoryFilterTableModel();
        }
        return historyFilterTableModel;
    }

    private static class HistoryFilterMultipleOptionsTablePanel
            extends AbstractMultipleOptionsTablePanel<HistoryFilter> {

        private static final long serialVersionUID = -115340627058929308L;

        private static final String REMOVE_DIALOG_TITLE =
                Constant.messages.getString("history.filter.options.dialog.remove.title");
        private static final String REMOVE_DIALOG_TEXT =
                Constant.messages.getString("history.filter.options.dialog.remove.text");

        private static final String REMOVE_DIALOG_CONFIRM_BUTTON_LABEL =
                Constant.messages.getString("history.filter.options.dialog.remove.button.confirm");
        private static final String REMOVE_DIALOG_CANCEL_BUTTON_LABEL =
                Constant.messages.getString("history.filter.options.dialog.remove.button.cancel");

        private static final String REMOVE_DIALOG_CHECKBOX_LABEL =
                Constant.messages.getString("history.filter.options.dialog.checkbox.label");

        public HistoryFilterMultipleOptionsTablePanel(HistoryFilterTableModel model) {
            super(model);

            this.model = model;

            getTable().getColumnExt(0).setPreferredWidth(50); // checkbox column should be tiny

            TableColumnExt nameColumn = getTable().getColumnExt(1);
            nameColumn.setPreferredWidth(100);
            nameColumn.setEditable(false);

            TableColumnExt filterDescColumn = getTable().getColumnExt(2);
            filterDescColumn.setPreferredWidth(200);
            filterDescColumn.setEditable(false);

            getTable()
                    .setHorizontalScrollEnabled(
                            true); // FilterDescription could be very wide, so turn on horiz scroll

            getTable().setAutoCreateRowSorter(true);
            getTable().setSortOrder(1, SortOrder.ASCENDING); // default

            addButton.setVisible(false);
            modifyButton.setVisible(false);
        }

        @Override
        public HistoryFilter showAddDialogue() {
            return null;
        }

        @Override
        public HistoryFilter showModifyDialogue(HistoryFilter historyFilter) {
            return null;
        }

        @Override
        public boolean showRemoveDialogue(HistoryFilter e) {
            JCheckBox removeWithoutConfirmationCheckBox =
                    new JCheckBox(REMOVE_DIALOG_CHECKBOX_LABEL);
            Object[] messages = {REMOVE_DIALOG_TEXT, " ", removeWithoutConfirmationCheckBox};
            int option =
                    JOptionPane.showOptionDialog(
                            View.getSingleton().getMainFrame(),
                            messages,
                            REMOVE_DIALOG_TITLE,
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            new String[] {
                                REMOVE_DIALOG_CONFIRM_BUTTON_LABEL,
                                REMOVE_DIALOG_CANCEL_BUTTON_LABEL
                            },
                            null);

            if (option == JOptionPane.OK_OPTION) {
                setRemoveWithoutConfirmation(removeWithoutConfirmationCheckBox.isSelected());

                return true;
            }

            return false;
        }
    }
}
