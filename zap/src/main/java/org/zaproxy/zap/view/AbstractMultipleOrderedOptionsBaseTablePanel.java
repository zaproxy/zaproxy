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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import org.parosproxy.paros.Constant;
import org.zaproxy.zap.utils.Orderable;

/**
 * An {@code AbstractMultipleOptionsBaseTablePanel} that allows to order the elements.
 *
 * @param <E> the type of elements in this table
 */
public abstract class AbstractMultipleOrderedOptionsBaseTablePanel<E extends Orderable> extends
        AbstractMultipleOptionsBaseTablePanel<E> {

    private static final long serialVersionUID = 8297404899024654579L;

    private JButton moveTopButton;
    private JButton moveUpButton;
    private JButton moveDownButton;
    private JButton moveBottomButton;

    public AbstractMultipleOrderedOptionsBaseTablePanel(AbstractMultipleOrderedOptionsBaseTableModel<E> model) {
        this(model, true);
    }

    protected AbstractMultipleOrderedOptionsBaseTablePanel(
            AbstractMultipleOrderedOptionsBaseTableModel<E> model,
            boolean allowModification) {
        super(model, allowModification);
    }

    protected void addMoveButtons() {
        addButtonSpacer();

        moveTopButton = new JButton(Constant.messages.getString("multiple.options.panel.ordered.move.top.button.label"));
        moveTopButton.setToolTipText(Constant.messages.getString("multiple.options.panel.ordered.move.top.button.tooltip"));
        moveTopButton.setEnabled(false);

        moveTopButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                getMultipleOptionsModel().moveTop(getSelectedRow());
                int finalRow = getTable().convertRowIndexToView(0);
                getTable().getSelectionModel().setSelectionInterval(finalRow, finalRow);
            }
        });
        addButton(moveTopButton);

        moveUpButton = new JButton(Constant.messages.getString("multiple.options.panel.ordered.move.up.button.label"));
        moveUpButton.setToolTipText(Constant.messages.getString("multiple.options.panel.ordered.move.up.button.tooltip"));
        moveUpButton.setEnabled(false);
        moveUpButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int row = getSelectedRow();
                getMultipleOptionsModel().moveUp(row);
                int finalRow = getTable().convertRowIndexToView(row - 1);
                getTable().getSelectionModel().setSelectionInterval(finalRow, finalRow);
            }
        });
        addButton(moveUpButton);

        moveDownButton = new JButton(Constant.messages.getString("multiple.options.panel.ordered.move.down.button.label"));
        moveDownButton.setToolTipText(Constant.messages.getString("multiple.options.panel.ordered.move.down.button.tooltip"));
        moveDownButton.setEnabled(false);
        moveDownButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int row = getSelectedRow();
                getMultipleOptionsModel().moveDown(row);
                int finalRow = getTable().convertRowIndexToView(row + 1);
                getTable().getSelectionModel().setSelectionInterval(finalRow, finalRow);
            }
        });
        addButton(moveDownButton);

        moveBottomButton = new JButton(Constant.messages.getString("multiple.options.panel.ordered.move.bottom.button.label"));
        moveBottomButton.setToolTipText(Constant.messages.getString("multiple.options.panel.ordered.move.bottom.button.tooltip"));
        moveBottomButton.setEnabled(false);
        moveBottomButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                getMultipleOptionsModel().moveBottom(getSelectedRow());
                int finalRow = getTable().convertRowIndexToView(getModel().getRowCount() - 1);
                getTable().getSelectionModel().setSelectionInterval(finalRow, finalRow);
            }
        });
        addButton(moveBottomButton);

        addButtonSpacer();
    }

    @Override
    protected AbstractMultipleOrderedOptionsBaseTableModel<E> getMultipleOptionsModel() {
        return (AbstractMultipleOrderedOptionsBaseTableModel<E>) super.getMultipleOptionsModel();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Overridden to also enable/disable the "move" buttons ("Top", "Up", "Down" and "Bottom").
     */
    @Override
    public void setComponentEnabled(boolean enabled) {
        super.setComponentEnabled(enabled);

        boolean enable = enabled && getTable().getSelectionModel().getMinSelectionIndex() >= 0;
        if (enable) {
            updateMoveButtons();
        } else {
            disableMoveButtons();
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Overridden to update the enabled state of the "move" buttons ("Top", "Up", "Down" and "Bottom").
     */
    @Override
    protected void selectionChanged(boolean entrySelected) {
        super.selectionChanged(entrySelected);

        if (!entrySelected) {
            disableMoveButtons();
        }

        updateMoveButtons();
    }

    protected void updateMoveButtons() {
        int selectedRow = getSelectedRow();
        if (selectedRow == -1 || getModel().getRowCount() == 1) {
            disableMoveButtons();
            return;
        }

        int positionSelected = getMultipleOptionsModel().getElement(selectedRow).getOrder();
        if (positionSelected == 1) {
            moveTopButton.setEnabled(false);
            moveUpButton.setEnabled(false);
            moveDownButton.setEnabled(true);
            moveBottomButton.setEnabled(true);
        } else if (positionSelected == getModel().getRowCount()) {
            moveTopButton.setEnabled(true);
            moveUpButton.setEnabled(true);
            moveDownButton.setEnabled(false);
            moveBottomButton.setEnabled(false);
        } else {
            moveTopButton.setEnabled(true);
            moveUpButton.setEnabled(true);
            moveDownButton.setEnabled(true);
            moveBottomButton.setEnabled(true);
        }
    }

    protected void disableMoveButtons() {
        moveTopButton.setEnabled(false);
        moveUpButton.setEnabled(false);
        moveDownButton.setEnabled(false);
        moveBottomButton.setEnabled(false);
    }

}
