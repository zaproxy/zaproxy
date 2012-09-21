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

package org.parosproxy.paros.view;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HostAuthentication;

public class OptionsAuthenticationTableModel extends AbstractTableModel {

	private static final long serialVersionUID = -7596331927341748685L;

	private static final String[] columnNames = {
				Constant.messages.getString("options.auth.label.host"), 
				Constant.messages.getString("options.auth.label.port"), 
				Constant.messages.getString("options.auth.label.uname"), 
				Constant.messages.getString("options.auth.label.password"), 
				Constant.messages.getString("options.auth.label.realm")};
    
    private Vector<HostAuthentication> listAuth = new Vector<>();
    
    /**
     * 
     */
    public OptionsAuthenticationTableModel() {
        super();
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    @Override
    public int getColumnCount() {
        return 5;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getRowCount()
     */
    @Override
    public int getRowCount() {
        return listAuth.size();
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    @Override
    public Object getValueAt(int row, int col) {
        HostAuthentication auth = listAuth.get(row);
        Object result = null;
        switch (col) {
        	case 0:	result = auth.getHostName();
        			break;
        	case 1: // ZAP: Changed to use the method Integer.valueOf.
        			result = Integer.valueOf(auth.getPort());
        			break;
        	case 2: result = auth.getUserName();
        			break;
        	case 3:	result = auth.getPassword();
        			break;
        	case 4:	result = auth.getRealm();
        			break;
        	default: result = "";
        }
        return result;
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }
    
    @Override
    public void setValueAt(Object value, int row, int col) {
        
        HostAuthentication auth = listAuth.get(row);
        //Object result = null;
        switch (col) {
        	case 0:	auth.setHostName((String) value);
        			break;
        	case 1: auth.setPort(((Integer) value).intValue());
        			break;
        	case 2: auth.setUserName((String) value);
        			break;
        	case 3:	auth.setPassword((String) value);
        			break;
        	case 4:	auth.setRealm((String) value);
        			break;
        }
        checkAndAppendNewRow();
        fireTableCellUpdated(row, col);
    }

    /**
     * @return Returns the listAuth.
     */
    public Vector<HostAuthentication> getListAuth() {
        HostAuthentication auth = null;
        for (int i=0; i<listAuth.size();) {
            auth = listAuth.get(i);
            if (auth.getHostName().equals("")) {
                listAuth.remove(i);
                continue;
            }
            i++;
        }
        
        Vector<HostAuthentication> newList = new Vector<>(listAuth);
        return newList;
    }
    /**
     * @param listAuth The listAuth to set.
     */
    public void setListAuth(Vector<HostAuthentication> listAuth) {
        this.listAuth = new Vector<>(listAuth);
        checkAndAppendNewRow();
  	  	fireTableDataChanged();
    }
    
    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }
    
    private void checkAndAppendNewRow() {
        HostAuthentication auth = null;
        if (listAuth.size() > 0) {
            auth = listAuth.get(listAuth.size()-1);
            if (!auth.getHostName().equals("")) {
                auth = new HostAuthentication();
                listAuth.add(auth);
            }
        } else {
            auth = new HostAuthentication();
            listAuth.add(auth);
        }
    }
    
    @Override
    // ZAP: Added type argument.
    public Class<?> getColumnClass(int c) {
        if (c == 1) {
            return Integer.class;
        }
        return String.class;
        
    }
    
}
