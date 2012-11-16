/*
 *
 * Paros and its related class files.
 * 
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2004 Chinotec Technologies Company
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Clarified Artistic License
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Clarified Artistic License for more details.
 * 
 * You should have received a copy of the Clarified Artistic License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
// ZAP: 2011/04/26 i18n
// ZAP: 2012/04/23 Added @Override annotation to all appropriate methods and
// removed unnecessary casts.
// ZAP: 2012/04/25 Added type argument to generic type and changed to use the
// method Integer.valueOf.
// ZAP: 2012/11/15 Issue 416: Normalise how multiple related options are managed
// throughout ZAP and enhance the usability of some options.

package org.parosproxy.paros.view;

import java.util.ArrayList;
import java.util.List;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HostAuthentication;
import org.zaproxy.zap.view.AbstractMultipleOptionsTableModel;

public class OptionsAuthenticationTableModel extends AbstractMultipleOptionsTableModel<HostAuthentication> {

	private static final long serialVersionUID = -7596331927341748685L;

	private static final String[] COLUMN_NAMES = {
		Constant.messages.getString("options.auth.table.header.enabled"),
		Constant.messages.getString("options.auth.table.header.name"),
		Constant.messages.getString("options.auth.table.header.host"),
		Constant.messages.getString("options.auth.table.header.port"),
		Constant.messages.getString("options.auth.table.header.uname"),
		Constant.messages.getString("options.auth.table.header.password"),
		Constant.messages.getString("options.auth.table.header.realm")};
	
	private static final int COLUMN_COUNT = COLUMN_NAMES.length;
    
    private List<HostAuthentication> listAuth = new ArrayList<>(5);
    
    /**
     * 
     */
    public OptionsAuthenticationTableModel() {
        super();
    }
    
    @Override
    public String getColumnName(int col) {
        return COLUMN_NAMES[col];
    }

    @Override
    public int getColumnCount() {
        return COLUMN_COUNT;
    }

    @Override
    public int getRowCount() {
        return listAuth.size();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return (columnIndex == 0);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch(columnIndex) {
        case 0:
            return Boolean.valueOf(getElement(rowIndex).isEnabled());
        case 1:
            return getElement(rowIndex).getName();
        case 2:
            return getElement(rowIndex).getHostName();
        case 3:
            return Integer.valueOf(getElement(rowIndex).getPort());
        case 4:
            return getElement(rowIndex).getUserName();
        case 5:
            return getElement(rowIndex).getPassword();
        case 6:
            return getElement(rowIndex).getRealm();
        }
        return null;
    }
    
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            if (aValue instanceof Boolean) {
                listAuth.get(rowIndex).setEnabled(((Boolean) aValue).booleanValue());
            }
        }
    }
    
    @Override
    // ZAP: Added type argument.
    public Class<?> getColumnClass(int c) {
        if (c == 0) {
            return Boolean.class;
        } else if (c == 3) {
            return Integer.class;
        }
        return String.class;
    }

    /**
     * @return Returns the listAuth.
     */
    public List<HostAuthentication> getListAuth() {
        HostAuthentication auth = null;
        for (int i=0; i<listAuth.size();) {
            auth = listAuth.get(i);
            if (auth.getHostName().equals("")) {
                listAuth.remove(i);
                continue;
            }
            i++;
        }
        
        List<HostAuthentication> newList = new ArrayList<>(listAuth);
        return newList;
    }
    
    /**
     * @param listAuth The listAuth to set.
     */
    public void setListAuth(List<HostAuthentication> listAuth) {
        this.listAuth = new ArrayList<>(listAuth.size());
        
        for (HostAuthentication auth : listAuth) {
            this.listAuth.add(new HostAuthentication(auth));
        }
        
        fireTableDataChanged();
    }

    @Override
    public List<HostAuthentication> getElements() {
        return listAuth;
    }
}
