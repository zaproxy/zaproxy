/*
 * Copyright (C) 2010, Compass Security AG
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see http://www.gnu.org/copyleft/
 * 
 */

package ch.csnc.extension.ui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import ch.csnc.extension.httpclient.AliasCertificate;
import ch.csnc.extension.httpclient.SSLContextManager;

public class AliasTableModel extends AbstractTableModel {
    
	private static final long serialVersionUID = -4387633069248206563L;
	
	private int _ks = -1;
    private List<AliasCertificate> _aliases = new ArrayList<AliasCertificate>();
	private SSLContextManager _sslcm;
	
	public AliasTableModel(SSLContextManager contextManager){
		_sslcm = contextManager;
	}
    
    public void setKeystore(int ks) {
        _ks = ks;
        _aliases.clear();
        if (_ks > -1) {
        	_aliases = _sslcm.getAliases(_ks);
        }
        fireTableDataChanged();
    }
   
    public void removeKeystore() {
        _ks = -1;
        _aliases.clear();
        fireTableDataChanged();
    }
    
    
    public String getAlias(int row) {
        return (String) _aliases.get(row).getAlias();
    }
    
    public int getColumnCount() {
        return 1;
    }
    
    public int getRowCount() {
        return _aliases.size();
    }
    
    public Object getValueAt(int row, int col) {
        return _aliases.get(row).getName();
    }
    
}
