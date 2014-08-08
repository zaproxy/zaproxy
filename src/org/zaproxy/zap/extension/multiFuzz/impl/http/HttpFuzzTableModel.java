/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package org.zaproxy.zap.extension.multiFuzz.impl.http;

import org.jdesktop.swingx.treetable.AbstractTreeTableModel;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.utils.Pair;

import java.util.LinkedList;
import java.util.List;

import javax.swing.tree.TreePath;

public class HttpFuzzTableModel extends AbstractTreeTableModel {

	private static final String[] COLUMN_NAMES = { 
			Constant.messages.getString("fuzz.http.table.header.name"),
			Constant.messages.getString("fuzz.http.table.header.method"),
			Constant.messages.getString("fuzz.http.table.header.uri"),
			Constant.messages.getString("fuzz.http.table.header.rtt"),
			Constant.messages.getString("fuzz.http.table.header.size"),
			Constant.messages.getString("fuzz.http.table.header.status"),
			Constant.messages.getString("fuzz.http.table.header.reason"),
			Constant.messages.getString("fuzz.http.table.header.state"),
			Constant.messages.getString("fuzz.http.table.header.payloads"),
			Constant.messages.getString("fuzz.http.table.header.include")};

	private List<HttpFuzzRecord> data = new LinkedList<>();
	
	public HttpFuzzTableModel() {
		super(new Object());
	}

	@Override
	public int getColumnCount() {
		return COLUMN_NAMES.length;
	}

	@Override
	public String getColumnName(int column) {
		return COLUMN_NAMES[column];
	}

	@Override
	public boolean isCellEditable(Object node, int column) {
		return true;
	}

	@Override
	public boolean isLeaf(Object node) {
		return (node instanceof HttpFuzzRequestRecord);
	}

	@Override
	public Object getValueAt(Object node, int column) {
		if (node instanceof HttpFuzzRecord) {
			HttpFuzzRecord result = (HttpFuzzRecord) node;
			switch (column) {
			case 0:
				return result.getName();
			case 1:
				return result.getMethod();
			case 2:
				return result.getURI();
			case 3:
				return result.getRTT();
			case 4:
				return result.getSize();
			case 5:
				return result.getState();
			case 6:
				return result.getReason();
			case 7:
				return result.getResult();
			case 8:
				StringBuilder pay = new StringBuilder();
				for (int i = 0; i < result.getPayloads().size(); i++) {
					pay.append(i);
					pay.append(". gap -> ");
					pay.append(result.getPayloads().get(i));
					pay.append("   \t");
				}
				return pay.toString();
			case 9:
				return result.isIncluded();
			default:
				return "";
			}
		}
		return null;
	}
	@Override
	public void setValueAt(Object inValue, Object row, int col)  {
		if(col == 0 && (inValue instanceof String)){
			((HttpFuzzRecord) row).setName((String) inValue);
		}
		else if (col == 9 && (inValue instanceof Boolean)){
        	((HttpFuzzRecord) row).setIncluded((Boolean)inValue);
        }
        return;
	}
	@Override
	public Object getChild(Object parent, int index) {
		if (parent instanceof HttpFuzzRecordGroup) {
			HttpFuzzRecordGroup group = (HttpFuzzRecordGroup) parent;
			return group.getMembers().get(index);
		}
		return data.get(index);
	}

	@Override
	public int getChildCount(Object parent) {
		if (parent instanceof HttpFuzzRecordGroup) {
			HttpFuzzRecordGroup group = (HttpFuzzRecordGroup) parent;
			return group.getMembers().size();
		}
		return data.size();
	}

	@Override
	public int getIndexOfChild(Object parent, Object child) {
		if (parent instanceof HttpFuzzRecordGroup
				&& child instanceof HttpFuzzRequestRecord) {
			HttpFuzzRecordGroup group = (HttpFuzzRecordGroup) parent;
			HttpFuzzRequestRecord rec = (HttpFuzzRequestRecord) child;
			return group.getMembers().indexOf(rec);
		}
		return data.indexOf(child);
	}

	public void addFuzzRecord(HttpFuzzRecord httpFuzzRecord) {
		data.add(httpFuzzRecord);
		modelSupport.fireChildAdded(new TreePath(httpFuzzRecord), data.size() - 1, httpFuzzRecord);
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 3:
			return Integer.class;
		case 4:
			return Integer.class;
		case 5:
			return Integer.class;
		case 7:
			return Pair.class;
		case 9:
			return Boolean.class;
		default:
			return String.class;
		}
	}

	public List<HttpFuzzRecord> getEntries() {
		if (data == null) {
			data = new LinkedList<>();
		}
		return data;
	}

	public List<HttpFuzzRequestRecord> getHistoryReferences() {
		List<HttpFuzzRequestRecord> res = new LinkedList<>();
		for (HttpFuzzRecord rec : data) {
			if (rec instanceof HttpFuzzRequestRecord) {
				res.add((HttpFuzzRequestRecord) rec);
			}
		}
		return res;
	}

	public void removeFuzzRecord(HttpFuzzRecord entry) {
		if (!data.remove(entry)) {
			for (HttpFuzzRecord r : data) {
				if (r instanceof HttpFuzzRecordGroup) {
					if(((HttpFuzzRecordGroup) r).getMembers().contains(entry)){
						((HttpFuzzRecordGroup) r).getMembers().remove(entry);
						modelSupport.fireChildRemoved(new TreePath(r), ((HttpFuzzRecordGroup) r).getMembers().indexOf(entry), entry);
					}
				}
			}
		}
	}
	public void removeAll() {
		data.clear();
		modelSupport.fireNewRoot();
	}
}