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
package org.zaproxy.zap.extension.params;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import org.parosproxy.paros.Constant;

public class ParamsTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
	private static final int COLUMN_COUNT = 7;
	
	private final Vector<String> columnNames;
	private List<HtmlParameterStats> paramStats;

	private int lastAddedRow;
	private int lastEditedRow;
	
	public ParamsTableModel() {
		super();
		columnNames = new Vector<>(COLUMN_COUNT);
		columnNames.add(Constant.messages.getString("params.table.header.type"));
		columnNames.add(Constant.messages.getString("params.table.header.name"));
		columnNames.add(Constant.messages.getString("params.table.header.used"));
		columnNames.add(Constant.messages.getString("params.table.header.numvals"));
		columnNames.add(Constant.messages.getString("params.table.header.pcchange"));
		columnNames.add(Constant.messages.getString("params.table.header.flags"));
		columnNames.add(Constant.messages.getString("params.table.header.values"));

		paramStats = Collections.synchronizedList(new ArrayList<HtmlParameterStats>());
		
		lastAddedRow = -1;
		lastEditedRow = -1;
	}

	@Override
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	@Override
	public int getRowCount() {
		return paramStats.size();
	}

	@Override
	public String getColumnName(int col) {
		return columnNames.get(col);
	}

	@Override
	public Object getValueAt(int row, int col) {
		Object obj = null;
		if (row >= paramStats.size()) {
			return null;
		}
		// TODO this doesnt work if resorted??
		HtmlParameterStats param = paramStats.get(row);
		switch (col) {
		case 0: obj = Constant.messages.getString("params.type." + param.getType().name()); break;
		case 1:	obj = param.getName(); break;
		case 2:	obj = param.getTimesUsed(); break;
		case 3:	obj = param.getValues().size(); break;
		case 4:	obj = getPercentChange(param); break;
		case 5: obj = param.getAllFlags(); break;
		case 6: obj = param.getValuesSummary(); break;
		}
		return obj;
	}
	
	private int getPercentChange(HtmlParameterStats param) {
		if (param.getValues().size() == 1) {
			return 0;
		}
		return (param.getValues().size() * 100 / param.getTimesUsed());
	}

	public HtmlParameterStats getHtmlParameterStatsAtRow(int row) {
		// TODO this doesnt work if resorted??
		return paramStats.get(row);
	}

	public void addHtmlParameterStats(HtmlParameterStats param) {
		lastAddedRow = -1;
		
		for (int i = 0; i < paramStats.size(); i++) {
			int cmp = param.compareTo(paramStats.get(i));
			if (cmp < 0) {
				paramStats.add(i, param);
				this.fireTableRowsInserted(i, i);

				lastAddedRow = i;
				return;
			} else if (cmp == 0) {
				// Already matches, so ignore
				lastAddedRow = i;
				return;
			}
		}

		paramStats.add(param);
		this.fireTableRowsInserted(paramStats.size()-1, paramStats.size()-1);
		
		lastAddedRow = paramStats.size()-1;
	}
	
	public int getLastAddedRow() {
		return lastAddedRow;
	}
	
	public int getLastEditedRow() {
		return lastEditedRow;
	}
	
	@Override
	public boolean isCellEditable(int row, int col) {
		return false;
	}

	@Override
	public Class<? extends Object> getColumnClass(int c) {
		switch (c) {
		case 0: return String.class;
		case 1:	return String.class;
		case 2:	return Integer.class;
		case 3:	return Integer.class;
		case 4:	return Integer.class;
		case 5: return String.class;
		case 6: return String.class;
		}
		return null;
	}

	public void removeAllElements() {
		paramStats.clear();
	}

}
