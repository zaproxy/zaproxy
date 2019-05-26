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
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.zaproxy.zap.view;

import java.util.ArrayList;
import java.util.List;

import org.parosproxy.paros.Constant;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.StructuralNodeModifier;

/**
 * A table model for holding a set of structural node modifiers, for a {@link Context}.
 * @since 2.4.3
 */
public class StructuralNodeModifiersTableModel extends AbstractMultipleOptionsTableModel<StructuralNodeModifier> {

	/** The Constant defining the table column names. */
	private static final String[] COLUMN_NAMES = {
			Constant.messages.getString("context.ddn.table.header.type"),
			Constant.messages.getString("context.ddn.table.header.name"),
			Constant.messages.getString("context.ddn.table.header.regex") };

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 4463944219657112162L;

	/** The users. */
	private List<StructuralNodeModifier> snms = new ArrayList<StructuralNodeModifier>();

	/**
	 * Instantiates a new structural node modifiers table model. An internal copy of the provided list is stored.
	 * 
	 * @param snms the structural node modifiers
	 */
	public StructuralNodeModifiersTableModel(List<StructuralNodeModifier> snms) {
		this.snms = new ArrayList<>(snms);
	}

	/**
	 * Instantiates a new user table model.
	 */
	public StructuralNodeModifiersTableModel() {
		this.snms = new ArrayList<>();
	}

	@Override
	public int getColumnCount() {
		return COLUMN_NAMES.length;
	}

	@Override
	public int getRowCount() {
		return snms.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 0:
			switch (snms.get(rowIndex).getType()) {
			case StructuralParameter:	return Constant.messages.getString("context.ddn.table.type.struct");
			case DataDrivenNode:		return Constant.messages.getString("context.ddn.table.type.data");
			}
			return null;
		case 1:
			return snms.get(rowIndex).getName();
		case 2:
			if (snms.get(rowIndex).getPattern() != null) {
				return snms.get(rowIndex).getPattern().pattern();
			}
			return null;
		default:
			return null;
		}
	}

	@Override
	public List<StructuralNodeModifier> getElements() {
		return snms;
	}

	/**
	 * Gets the internal list of users managed by this model. 
	 * 
	 * @return the users
	 */
	public List<StructuralNodeModifier> getStructuralNodeModifiers() {
		return snms;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	/**
	 * Sets a new list of structural node modifiers for this model. An internal copy of the provided list is stored.
	 * 
	 * @param snms the new structural node modifiers
	 */
	public void setStructuralNodeModifiers(List<StructuralNodeModifier> snms) {
		this.snms = new ArrayList<>(snms);
		this.fireTableDataChanged();
	}
	
	public void addStructuralNodeModifiers(List<StructuralNodeModifier> snms) {
		this.snms.addAll(snms);
		this.fireTableDataChanged();
	}
	
	/**
	 * Removes all the structural node modifiers for this model.
	 */
	public void removeAllStructuralNodeModifiers(){
		this.snms=new ArrayList<>();
		this.fireTableDataChanged();
	}
	
	/**
	 * Adds a new structural node modifiers to this model
	 *
	 * @param snm the structural node modifier
	 */
	public void addStructuralNodeModifier(StructuralNodeModifier snm){
		this.snms.add(snm);
		this.fireTableRowsInserted(this.snms.size()-1, this.snms.size()-1);
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}

	@Override
	public String getColumnName(int column) {
		return COLUMN_NAMES[column];
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
	}

}
