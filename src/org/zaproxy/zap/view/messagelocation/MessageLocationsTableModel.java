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

import java.util.ArrayList;
import java.util.List;

import org.parosproxy.paros.Constant;
import org.zaproxy.zap.view.AbstractMultipleOptionsBaseTableModel;

/**
 * An {@code AbstractMultipleOptionsBaseTableModel} to manage message locations in a table.
 * <p>
 * Used to manage message locations and the corresponding highlights.
 * 
 * @param <T> the type of message location entries
 * @since 2.4.0
 * @see AbstractMultipleOptionsBaseTableModel
 * @see MessageLocationTableEntry
 */
public class MessageLocationsTableModel<T extends MessageLocationTableEntry> extends AbstractMultipleOptionsBaseTableModel<T> {

    private static final long serialVersionUID = 4506544561803715504L;

    private static final String[] COLUMNS = {
            "",
            Constant.messages.getString("messagelocations.table.header.location"),
            Constant.messages.getString("messagelocations.table.header.value") };

    protected static final int BASE_NUMBER_OF_COLUMNS = COLUMNS.length;

    private final List<T> messageLocations;

    public MessageLocationsTableModel() {
        messageLocations = new ArrayList<>();
    }

    public MessageLocationsTableModel(List<T> messageLocations) {
        this.messageLocations = messageLocations;
    }

    @Override
    public int getRowCount() {
        return messageLocations.size();
    }

    @Override
    public int getColumnCount() {
        return BASE_NUMBER_OF_COLUMNS;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        MessageLocationTableEntry messageLocation = getElement(rowIndex);
        if (columnIndex == 0) {
            return messageLocation.getHighlight();
        } else if (columnIndex == 1) {
            return messageLocation.getLocationUI();
        }

        return messageLocation.getLocation().getValue();
    }

    public Class<?> getColumnClass(int row, int columnIndex) {
        if (columnIndex == 0 && getRowCount() != 0) {
            MessageLocationHighlight highlight = getElement(row).getHighlight();
            if (highlight != null) {
                return highlight.getClass();
            }
        }
        return getColumnClass(columnIndex);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0) {
            return MessageLocationHighlight.class;
        } else if (columnIndex == 1) {
            return MessageLocationTableEntry.LocationUI.class;
        }
        return String.class;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex != 0 || !(aValue instanceof MessageLocationHighlight)) {
            return;
        }

        T messageLocation = getElement(rowIndex);
        messageLocation.setHighlight((MessageLocationHighlight) aValue);

        fireHighlightChanged(messageLocation, messageLocation.getHighlightReference());
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return true;
        }
        return false;
    }

    @Override
    public List<T> getElements() {
        return messageLocations;
    }

    public int getRow(MessageLocationTableEntry entry) {
        return messageLocations.indexOf(entry);
    }

    public void addMessageLocationHighlightChangedListener(HighlightChangedListener<T> listener) {
        listenerList.add(HighlightChangedListener.class, listener);
    }

    public void removeMessageLocationHighlightChangedListener(HighlightChangedListener<T> listener) {
        listenerList.remove(HighlightChangedListener.class, listener);
    }

    protected void fireHighlightChanged(T entry, MessageLocationHighlight highlightReference) {
        HighlightChangedEvent<T> event = null;
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == HighlightChangedListener.class) {
                if (event == null) {
                    event = new HighlightChangedEvent<>(this, entry, highlightReference);
                }
                @SuppressWarnings("unchecked")
                HighlightChangedListener<T> listener = ((HighlightChangedListener<T>) listeners[i + 1]);
                listener.highlightChanged(event);
            }
        }
    }

}