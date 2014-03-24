/*
 * HTTPHeaderTableModel.java
 *
 * Copyright 2007 James Fisher
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA
 */

package com.sittinglittleduck.DirBuster.gui;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import com.sittinglittleduck.DirBuster.HTMLelementToParse;

/**
 *
 * @author james
 */
public class HTMLParseTableModel extends AbstractTableModel
{
    String[] columnNames = {new String("HTML Tag"), new String("Attribute")};


    private Vector tableData;
    Object[][] data;

    /** Creates a new instance of HTTPHeaderTableModel */
    public HTMLParseTableModel(Vector d)
    {
        data = null;

        this.tableData = d;

        data = new Object[tableData.size()][2];

        for(int a = 0; a < tableData.size(); a++)
        {
            HTMLelementToParse element = (HTMLelementToParse) tableData.elementAt(a);

            data[a][0] = element.getTag();
            data[a][1] = element.getAttr();

        }
    }

    public void setColumnName(int index, String name)
    {
        if (index < columnNames.length)
            columnNames[index] = name;
    }

    public boolean isCellEditable(int row, int col)
    {
        return false;
    }


    public int getRowCount()
    {
        return data==null ? 0 : data.length;
    }

    public int getColumnCount()
    {
        return columnNames.length;
    }

    public String getColumnName(int col)
    {
        return columnNames[col];
    }


    public Object getValueAt(int row, int col)
    {
        if ( row < 0 || row >= data.length ) return null;

        return data[row][col];

    }

    public void setValueAt(Object value, int row, int col)
    {
        data[row][col] = value;
        fireTableCellUpdated(row, col);
    }

    public void clearData()
    {
        data = null;
    }

    public Vector getVector()
    {
        return tableData;
    }

}
