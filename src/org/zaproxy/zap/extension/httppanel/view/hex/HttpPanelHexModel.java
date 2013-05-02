/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2010 psiinon@gmail.com
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
package org.zaproxy.zap.extension.httppanel.view.hex;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.zaproxy.zap.utils.ByteBuilder;

public class HttpPanelHexModel extends AbstractTableModel {

    private static final String[] hexSymbols = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };
    private static final int BITS_PER_HEX_DIGIT = 4;
	private static final int COLUMN_COUNT = 34;

	private static final long serialVersionUID = 1L;
    private List<String[]> listRow = new ArrayList<>();
    private boolean editable = false;
    private boolean changed = false;


    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public HttpPanelHexModel() {
        super();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_COUNT;
    }

    @Override
    public int getRowCount() {
        return listRow.size();
    }

    @Override
    public Object getValueAt(int row, int col) {
        return listRow.get(row)[col];
    }

    private boolean isHexColumn (int col) {
    	return col > 0 && col <= 16;
    }

    private boolean isCharColumn (int col) {
    	return col > 17;
    }

    @Override
    public String getColumnName(int col) {
    	if (isHexColumn(col)) {
    		return Integer.toHexString(col-1);
    	} else if (isCharColumn(col)) {
    		return Integer.toHexString(col-18);
    	}
        return "";
    }

    public static String toHexFromByte(final byte b)
    {
        byte leftSymbol = (byte)((b >>> BITS_PER_HEX_DIGIT) & 0x0f);
        byte rightSymbol = (byte)(b & 0x0f);

        return (hexSymbols[leftSymbol] + hexSymbols[rightSymbol]);
    }

    private String [] newRow() {
    	String [] row = new String[COLUMN_COUNT];
    	row[0] = String.format("%08x", (listRow.size() * 16));
    	return row;
    }

    private void setHex(String[] row, int index, byte b) {
		row[index] = toHexFromByte(b);
    	row[index + 17] = String.valueOf((char)b);
    }

    private void setChar(String[] row, int index, char c) {
		row[index - 17] = toHexFromByte((byte)c);
    	row[index] = String.valueOf(c);
    }

    public synchronized void setData(byte[] body) {
    	listRow.clear();

    	if (body.length != 0 || isEditable()) {
	    	int index = 0;
	    	int rowIndex = 0;
	    	String [] row = newRow();
	    	listRow.add(row);
	    	while (index < body.length) {
	    		setHex(row, rowIndex + 1, body[index]);
	        	rowIndex++;
	    		if (rowIndex >= 16) {
	    			row = newRow();
	    	    	listRow.add(row);
	    	    	rowIndex = 0;
	    		}
	    		index++;
	    	}
    	}

    	this.fireTableDataChanged();
    	changed = false;
    }

    public synchronized byte[] getData() {
    	// Need to implement if/when edit supported
    	ByteBuilder bb = new ByteBuilder();
    	for (String[] row : listRow) {
    		for (int i=1; i < 17; i++) {
    			if (row[i] == null || row[i].length() == 0) {
    				break;
    			}
    			bb.append((char)Integer.parseInt(row[i], 16));
    		}
    	}
    	return bb.toByteArray();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
    	if (! isEditable()) {
    		return false;
    	}
    	if (! (isHexColumn(columnIndex) || isCharColumn(columnIndex))) {
    		// Either the index or spacer column
    		return false;
    	}
    	if (rowIndex < listRow.size() - 1) {
    		// Not the last row, so no problem
    		return true;
    	}

        String[] row = listRow.get(rowIndex);
    	if (!isCharColumn(columnIndex)) {
    		// Previous cell is null - can only edit the first null cell
    		return (row[columnIndex-1] != null);
    	} else if (row[columnIndex-1] == null && columnIndex != 18) {
    		// Previous cell is null and it's not the first text cell
    		return false;
    	}

    	return true;
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
    	String val = (String) value;
    	byte byteVal = -1;
    	char charVal = (char)0;
    	if (isHexColumn(col)) {
    		if (val.length() > 2) {
    			return;
    		}
    		try {
				byteVal = (byte)Integer.parseInt(val, 16);
			} catch (NumberFormatException e) {
				return;
			}
    	} else if (isCharColumn(col)) {
    		if (val.length() != 1) {
    			return;
    		}
    		charVal = val.charAt(0);
    	} else {
    		return;
    	}
        String[] cell = listRow.get(row);
    	if (isHexColumn(col)) {
    		setHex(cell, col, byteVal);
    	} else {
       		setChar(cell, col, charVal);
    	}

    	int lastRowChanged = row;

        if ((row == listRow.size() - 1) && cell[16] != null) {
        	// Last cell on last line is used, add a new row
    		listRow.add(newRow());
    		lastRowChanged++;
    	}

        fireTableRowsUpdated(row, lastRowChanged);
        changed = true;
    }

    public boolean hasChanged() {
		return changed;
	}
}