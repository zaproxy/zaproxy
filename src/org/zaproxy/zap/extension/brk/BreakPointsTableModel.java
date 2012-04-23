/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
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
package org.zaproxy.zap.extension.brk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.parosproxy.paros.Constant;

public class BreakPointsTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
	private static final int COLUMN_COUNT = 2;
	
	private final String[] columnNames;
	private List<BreakPoint> breakPoints;

	private int lastAddedRow;
	private int lastEditedRow;
	
	public BreakPointsTableModel() {
		super();
		columnNames = new String[COLUMN_COUNT];
		columnNames[0] = Constant.messages.getString("brk.table.header.enabled");
		columnNames[1] = Constant.messages.getString("brk.table.header.url");

		breakPoints = Collections.synchronizedList(new ArrayList<BreakPoint>());
		
		lastAddedRow = -1;
		lastEditedRow = -1;
	}

	List<BreakPoint> getBreakPointsList() {
		return breakPoints;
	}
	
	@Override
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	@Override
	public int getRowCount() {
		return breakPoints.size();
	}

	@Override
	public String getColumnName(int col) {
		return columnNames[col];
	}

	@Override
	public Object getValueAt(int row, int col) {
		Object obj = null;
		BreakPoint breakPoint = breakPoints.get(row);
		if (col == 0) {
			obj = Boolean.valueOf(breakPoint.isEnabled());
		} else if (col == 1) {
			obj = breakPoint.getUrl();
		}
		return obj;
	}

	public BreakPoint getBreakPointAtRow(int row) {
		return breakPoints.get(row);
	}

	public void addBreakPoint(String url) {
		lastAddedRow = -1;
		
		BreakPoint breakPoint = new BreakPoint(url);
		for (int i = 0; i < breakPoints.size(); i++) {
			int cmp = breakPoint.compareTo(breakPoints.get(i));
			if (cmp < 0) {
				breakPoints.add(i, breakPoint);
				this.fireTableRowsInserted(i, i);

				lastAddedRow = i;
				return;
			} else if (cmp == 0) {
				// Already matches, so ignore
				lastAddedRow = i;
				return;
			}
		}

		breakPoints.add(breakPoint);
		this.fireTableRowsInserted(breakPoints.size()-1, breakPoints.size()-1);
		
		lastAddedRow = breakPoints.size()-1;
	}
	
	public void editBreakPointAtRow(int row, String url) {
		lastEditedRow = -1;
		
		BreakPoint brk = breakPoints.get(row);
		BreakPoint newBrkPt = new BreakPoint(url);
		if(!brk.equals(newBrkPt)) {
			int i = 0;
			for (; i < breakPoints.size(); i++) {
				int cmp = newBrkPt.compareTo(breakPoints.get(i));
				if (cmp < 0) {
					if(i == (row+1) || (i == row)) {
						brk.setUrl(url);
						this.fireTableRowsUpdated(row, row);
						
						lastEditedRow = row;
						return;
					}
					
					newBrkPt.setEnabled(brk.isEnabled());
					breakPoints.add(i, newBrkPt);
					this.fireTableRowsInserted(i, i);
					
					int r = i<row?row+1:row;
					breakPoints.remove(r);
					this.fireTableRowsDeleted(r, r);
					
					lastEditedRow = i>row?i-1:i;
					return;
				} else if (cmp == 0) {
					breakPoints.remove(row);
					this.fireTableRowsDeleted(row, row);
					
					lastEditedRow = i>row?i-1:i;
					return;
				}
			}

			if(i == (row+1)) {
				brk.setUrl(url);
				this.fireTableRowsUpdated(row, row);

				lastEditedRow = row;
			} else {
				breakPoints.remove(row);
				this.fireTableRowsDeleted(row, row);
				
				newBrkPt.setEnabled(brk.isEnabled());
				breakPoints.add(breakPoints.size(), newBrkPt);
				this.fireTableRowsInserted(breakPoints.size()-1, breakPoints.size()-1);

				lastEditedRow = breakPoints.size()-1;
			}
		}
	}
	
	public void removeBreakPointAtRow(int row) {
		breakPoints.remove(row);
		this.fireTableRowsDeleted(row, row);
	}

	public int getLastAddedRow() {
		return lastAddedRow;
	}
	
	public int getLastEditedRow() {
		return lastEditedRow;
	}
	
	@Override
	public boolean isCellEditable(int row, int col) {
		return (col == 0);
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		if (col == 0) {
			if (value instanceof Boolean) {
				breakPoints.get(row).setEnabled(((Boolean)value).booleanValue());
				this.fireTableCellUpdated(row, col);
			}
		}
	}

	@Override
	public Class<?> getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}

}
