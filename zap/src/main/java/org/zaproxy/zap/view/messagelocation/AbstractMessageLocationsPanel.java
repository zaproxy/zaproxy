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
package org.zaproxy.zap.view.messagelocation;

import java.awt.Component;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.SortOrder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import org.jdesktop.swingx.JXTable;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.httppanel.ComponentChangedEvent;
import org.zaproxy.zap.extension.httppanel.MessagePanelEventListener;
import org.zaproxy.zap.extension.httppanel.MessageViewSelectedEvent;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelView;
import org.zaproxy.zap.model.MessageLocation;
import org.zaproxy.zap.view.AbstractMultipleOptionsBaseTablePanel;

/**
 * An {@code AbstractMultipleOptionsBaseTablePanel} that allows to manage highlights and message
 * locations.
 *
 * @param <T> the type that contains the details of the highlight and message location
 * @param <S> the type of the table model that contains the elements with the details
 * @since 2.4.0
 * @see MessageLocationTableEntry
 * @see MessageLocationsTableModel
 */
@SuppressWarnings("serial")
public abstract class AbstractMessageLocationsPanel<
                T extends MessageLocationTableEntry, S extends MessageLocationsTableModel<T>>
        extends AbstractMultipleOptionsBaseTablePanel<T> {

    private static final long serialVersionUID = -8990789229815588716L;

    private static final String REMOVE_DIALOG_TITLE =
            Constant.messages.getString("messagelocationspanel.dialog.remove.location.title");
    private static final String REMOVE_DIALOG_TEXT =
            Constant.messages.getString("messagelocationspanel.dialog.remove.location.text");

    private static final String REMOVE_DIALOG_CONFIRM_BUTTON_LABEL =
            Constant.messages.getString(
                    "messagelocationspanel.dialog.remove.location.button.confirm");
    private static final String REMOVE_DIALOG_CANCEL_BUTTON_LABEL =
            Constant.messages.getString(
                    "messagelocationspanel.dialog.remove.location.button.cancel");

    private static final String REMOVE_DIALOG_CHECKBOX_LABEL =
            Constant.messages.getString(
                    "messagelocationspanel.dialog.remove.location.checkbox.label");

    private MessageLocationProducerFocusListener addButtonFocusListenerEnabler;

    private SelectMessageLocationsPanel selectMessageLocationsPanel;

    private final Component parent;

    public AbstractMessageLocationsPanel(
            Component parent, SelectMessageLocationsPanel selectMessageLocationsPanel, S model) {
        this(parent, selectMessageLocationsPanel, model, false);
    }

    public AbstractMessageLocationsPanel(
            Component parent,
            SelectMessageLocationsPanel selectMessageLocationsPanel,
            S model,
            boolean allowModifications) {
        super(model, allowModifications);

        this.parent = parent;

        this.selectMessageLocationsPanel = selectMessageLocationsPanel;
        this.selectMessageLocationsPanel.addMessagePanelEventListener(
                createMessagePanelEventListener());

        getModel().addMessageLocationHighlightChangedListener(createHighlightChangedListener());

        addButton.setEnabled(false);
        addButton.setToolTipText(
                Constant.messages.getString("messagelocationspanel.add.location.tooltip"));
        addButtonFocusListenerEnabler = createFocusListener();

        getTable().setSortOrder(1, SortOrder.ASCENDING);
        getTable()
                .setDefaultRenderer(
                        MessageLocationHighlight.class,
                        new DefaultMessageLocationHighlightRenderer());

        getRemoveWithoutConfirmationCheckBox().setSelected(true);
    }

    protected MessagePanelEventListener createMessagePanelEventListener() {
        return new MessagePanelEventListener() {

            @Override
            public void componentChanged(ComponentChangedEvent event) {
                for (T entry : getModel().getElements()) {
                    MessageLocationHighlight highlight = entry.getHighlight();
                    if (highlight != null) {
                        MessageLocationHighlight highlightReference =
                                selectMessageLocationsPanel.highlight(
                                        entry.getLocation(), highlight);
                        entry.setHighlightReference(highlightReference);
                    }
                }
            }

            @Override
            public void viewSelected(MessageViewSelectedEvent event) {
                HttpPanelView view = event.getCurrentView();
                if (view instanceof MessageLocationHighlighter) {
                    MessageLocationHighlighter highlighter = (MessageLocationHighlighter) view;
                    for (T entry : getModel().getElements()) {
                        MessageLocationHighlight highlight = entry.getHighlight();
                        if (highlight != null) {
                            MessageLocationHighlight highlightReference =
                                    highlighter.highlight(entry.getLocation(), highlight);
                            entry.setHighlightReference(highlightReference);
                        }
                    }
                } else {
                    for (T entry : getModel().getElements()) {
                        entry.setHighlightReference(null);
                    }
                }
            }
        };
    }

    protected HighlightChangedListener<T> createHighlightChangedListener() {
        return new MessageLocationsHighlightChangedListener();
    }

    @Override
    protected JXTable createTable() {
        MessageLocationsTable table = new MessageLocationsTable();
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setColumnControlVisible(true);

        return table;
    }

    public MessageLocationProducerFocusListener getFocusListenerAddButtonEnabler() {
        return addButtonFocusListenerEnabler;
    }

    protected MessageLocationProducerFocusListener createFocusListener() {
        return new MessageLocationsFocusListener();
    }

    @Override
    public T showAddDialogue() {
        addButton.setEnabled(false);
        return addMessageLocationImpl(true, selectMessageLocationsPanel.getSelection());
    }

    public boolean addMessageLocation(MessageLocation messageLocation) {
        if (messageLocation == null) {
            throw new IllegalArgumentException("Parameter messageLocation must not be null.");
        }

        return addMessageLocationImpl(false, messageLocation) != null;
    }

    private T addMessageLocationImpl(boolean buttonAddedLocation, MessageLocation messageLocation) {
        for (T locationUI : getModel().getElements()) {
            if (locationUI.getLocation().overlaps(messageLocation)) {
                View.getSingleton()
                        .showWarningDialog(
                                Constant.messages.getString(
                                        "messagelocationspanel.add.location.warning.locations.overlap"));
                return null;
            }
        }

        MessageLocationHighlight highlight = null;
        MessageLocationHighlight highlightReference = null;
        MessageLocationHighlightsManager highlightsManager = selectMessageLocationsPanel.create();
        if (highlightsManager != null) {
            highlight = highlightsManager.getHighlight(messageLocation);
            highlightReference = selectMessageLocationsPanel.highlight(messageLocation, highlight);
        }

        T entry =
                createMessageLocationTableEntry(
                        buttonAddedLocation, messageLocation, highlight, highlightReference);
        if (entry == null) {
            if (highlightsManager != null) {
                selectMessageLocationsPanel.removeHighlight(messageLocation, highlightReference);
            }
            return null;
        }
        getModel().addElement(entry);

        int row = getTable().convertRowIndexToView(getModel().getRow(entry));
        getTable().setRowSelectionInterval(row, row);

        return null;
    }

    protected abstract T createMessageLocationTableEntry(
            boolean buttonAddedLocation,
            MessageLocation messageLocation,
            MessageLocationHighlight highlight,
            MessageLocationHighlight highlightReference);

    @Override
    public T showModifyDialogue(T e) {
        return null;
    }

    @Override
    public boolean showRemoveDialogue(T e) {
        if (!getRemoveWithoutConfirmationCheckBox().isSelected()) {
            if (!showRemoveDialogueImpl(e)) {
                return false;
            }
        }

        MessageLocationHighlight highlightReference = e.getHighlightReference();
        if (highlightReference != null) {
            selectMessageLocationsPanel.removeHighlight(e.getLocation(), highlightReference);
        }

        return true;
    }

    protected Component getParentOwner() {
        return parent;
    }

    protected boolean showRemoveDialogueImpl(T e) {
        JCheckBox removeWithoutConfirmationCheckBox = new JCheckBox(REMOVE_DIALOG_CHECKBOX_LABEL);
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
                            REMOVE_DIALOG_CONFIRM_BUTTON_LABEL, REMOVE_DIALOG_CANCEL_BUTTON_LABEL
                        },
                        null);

        if (option == JOptionPane.OK_OPTION) {
            getRemoveWithoutConfirmationCheckBox()
                    .setSelected(removeWithoutConfirmationCheckBox.isSelected());
            return true;
        }

        return false;
    }

    @Override
    public boolean isRemoveWithoutConfirmation() {
        // Force base class to call the method showRemoveDialogue(T1) so the
        // state of the panels can be changed before deleting the entries
        return false;
    }

    public void reset() {
        for (T entry : getMultipleOptionsModel().getElements()) {
            MessageLocationHighlight highlightReference = entry.getHighlightReference();
            if (highlightReference != null) {
                selectMessageLocationsPanel.removeHighlight(
                        entry.getLocation(), highlightReference);
            }
        }
        getMultipleOptionsModel().clear();
    }

    @Override
    protected S getModel() {
        @SuppressWarnings("unchecked")
        // Safe cast it's the model that's set in the constructor (which can't be changed)
        S model = (S) super.getMultipleOptionsModel();
        return model;
    }

    private static class DefaultMessageLocationHighlightRenderer extends DefaultTableCellRenderer {

        private static final long serialVersionUID = 427590735065539815L;

        @Override
        protected void setValue(Object value) {}
    }

    protected class MessageLocationsTable extends JXTable {

        private static final long serialVersionUID = -3277532157790764376L;

        @Override
        public TableCellEditor getCellEditor(int row, int column) {

            Class<?> columnClass =
                    AbstractMessageLocationsPanel.this.getModel().getColumnClass(row, column);
            if (columnClass != null
                    && MessageLocationHighlight.class.isAssignableFrom(columnClass)) {

                @SuppressWarnings("unchecked")
                TableCellEditor editor =
                        MessageLocationHighlightRenderersEditors.getInstance()
                                .getEditor((Class<? extends MessageLocationHighlight>) columnClass);
                if (editor != null) {
                    return editor;
                }
            }
            return super.getCellEditor(row, column);
        }

        @Override
        public TableCellRenderer getCellRenderer(int row, int column) {

            Class<?> columnClass =
                    AbstractMessageLocationsPanel.this.getModel().getColumnClass(row, column);
            if (columnClass != null
                    && MessageLocationHighlight.class.isAssignableFrom(columnClass)) {

                @SuppressWarnings("unchecked")
                TableCellRenderer renderer =
                        MessageLocationHighlightRenderersEditors.getInstance()
                                .getRenderer(
                                        (Class<? extends MessageLocationHighlight>) columnClass);
                if (renderer != null) {
                    return renderer;
                }
            }
            return super.getCellRenderer(row, column);
        }
    }

    protected class MessageLocationsHighlightChangedListener
            implements HighlightChangedListener<T> {

        @Override
        public void highlightChanged(HighlightChangedEvent<T> event) {
            T tableEntry = event.getEntry();

            MessageLocationHighlight highlightReference = event.getHighlightReference();
            if (highlightReference != null) {
                selectMessageLocationsPanel.removeHighlight(
                        tableEntry.getLocation(), event.getHighlightReference());
            }

            MessageLocationHighlight highlight = tableEntry.getHighlight();
            if (highlight != null) {
                highlightReference =
                        selectMessageLocationsPanel.highlight(
                                tableEntry.getLocation(), tableEntry.getHighlight());
                tableEntry.setHighlightReference(highlightReference);
            }
        }
    }

    protected class MessageLocationsFocusListener implements MessageLocationProducerFocusListener {

        @Override
        public void focusLost(MessageLocationProducerFocusEvent e) {
            if (e.getFocusEvent().getOppositeComponent() != addButton) {
                addButton.setEnabled(false);
            }
        }

        @Override
        public void focusGained(MessageLocationProducerFocusEvent e) {
            addButton.setEnabled(true);
        }
    }
}
