/*
 * ResultsTableModel.java
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
import java.util.List;

import javax.swing.table.DefaultTableModel;

import com.sittinglittleduck.DirBuster.BaseCase;
import com.sittinglittleduck.DirBuster.DirToCheck;
import com.sittinglittleduck.DirBuster.Manager;

public class ResultsTableModel extends DefaultTableModel
{
    List data;
    
    String[] columnNames = { new String("Type"), new String("Found"), new String("Response"), new String("Size"), new String("Include"), new String("Status")};
    
    Manager manager;
    
    /** Creates a new instance of ResultsTableModel */
    public ResultsTableModel(List d)
    {
        this.data = d;
        //this.manager = manager;
    }
    
    public void addRow()
    {
        
    }
    
    public void setColumnName(int index, String name)
    {
        if (index < columnNames.length)
            columnNames[index] = name;
    }
    
    @Override
    public boolean isCellEditable(int row, int col)
    {
        if(col == 4)
        {
            ResultsTableObject tempObj = (ResultsTableObject) data.get(row);
            
            if(tempObj.getFieldStatus().equalsIgnoreCase("Finished") 
                || tempObj.getFieldStatus().equalsIgnoreCase("Scanning")
                || !tempObj.getFieldType().equalsIgnoreCase("dir"))
            {
                return false;
            }
                return true;
        }
        
        return false;
    }
    
    public Class getColumnClass(int c) 
    {
            return getValueAt(0, c).getClass();
    }
    
    public void addRow(ResultsTableObject object)
    {
        //check the item is not already in the table
        if(!data.contains(object))
        {
            data.add(object);
            this.fireTableRowsInserted(data.size() - 1, data.size() - 1);
        }
        
    }
    
    public void updateRow(String dirFinished, String dirStarted)
    {
        ResultsTableObject[] tempArray = new ResultsTableObject[data.size()];
        if(!data.isEmpty())
        {
            tempArray = (ResultsTableObject[]) data.toArray(tempArray);
            for(int a = 0; a < tempArray.length; a++)
            {
                if(tempArray[a].getFieldFound().equals(dirFinished))
                {
                    tempArray[a].setFieldStatus("Finished");
                    
                }
                
                if(tempArray[a].getFieldFound().equals(dirStarted))
                {
                    tempArray[a].setFieldStatus("Scanning");
                }
            }
            
            
            while(!data.isEmpty())
            {
                data.remove(0);
            }
            
            for(int b = 0; b  < tempArray.length; b++)
            {
                ResultsTableObject temp = tempArray[b];
                data.add(temp);
            }
            
            this.fireTableDataChanged();
        }
    }
    
    public void removeRow(int index)
    {
        data.remove(index);
        fireTableRowsDeleted(index, index);
    }
    
    //public Class getColumnClass(int c)
    //{
        //return getValueAt(0, c).getClass();
   // }
    
    public int getRowCount()
    {
        return data==null ? 0 : data.size();
    }
    
    @Override
    public int getColumnCount()
    {
        return columnNames.length;
    }
    
    public String getColumnName(int col)
    {
        return columnNames[col];
    }
    
    
    @Override
    public Object getValueAt(int row, int col)
    {
        if ( row < 0 || row >= data.size() ) return null;
        ResultsTableObject myObj = (ResultsTableObject) data.get(row);
        if (col==0)
            return myObj.getFieldType();
        else if (col==1)
            return myObj.getFieldFound();
        else if (col==2)
            return myObj.getFieldResponceCode();
        else if(col == 3)
        {
            if(myObj.getRawResponce() == null)
            {
                return "";
            }
            return myObj.getRawResponce().length();
        }
        else if (col==4)
            return myObj.isScanFurther();
        else if (col==5)
            return myObj.getFieldStatus();
        else return null;
    }
    
    public String getRowData(int row)
    {
        if ( row < 0 || row >= data.size() ) return null;
        ResultsTableObject temp = (ResultsTableObject) data.get(row);
        return temp.getFieldType() + temp.getFieldFound();
    }
    
    public String getRowResponceCode(int row)
    {
        if ( row < 0 || row >= data.size() ) return null;
        ResultsTableObject temp = (ResultsTableObject) data.get(row);
        return temp.getFieldFound();
    }
    
    public String getRowResponce(int row)
    {
        if ( row < 0 || row >= data.size() ) return null;
        ResultsTableObject temp = (ResultsTableObject) data.get(row);
        //System.out.println("Getting Responce for row: " + row);
        //System.out.println("Responce = " + temp.getResponce());
        return temp.getResponce();
    }
    
    public DirToCheck getDirToCheck(int row)
    {
        if ( row < 0 || row >= data.size() ) return null;
        ResultsTableObject temp = (ResultsTableObject) data.get(row);
        //System.out.println("Getting Responce for row: " + row);
        //System.out.println("Responce = " + temp.getResponce());
        return temp.getDirToCheck();
    }
    
    public String getRowRawResponce(int row)
    {
        if ( row < 0 || row >= data.size() ) return null;
        ResultsTableObject temp = (ResultsTableObject) data.get(row);
        //System.out.println("Getting Responce for row: " + row);
        //System.out.println("Responce = " + temp.getResponce());
        return temp.getRawResponce();
    }
    
    public String getBaseCase(int row)
    {
        if ( row < 0 || row >= data.size() ) return null;
        ResultsTableObject temp = (ResultsTableObject) data.get(row);
        //System.out.println("Getting Responce for row: " + row);
        //System.out.println("Responce = " + temp.getResponce());
        return temp.getBaseCase();
    }
    
    public BaseCase getBaseCaseObj(int row)
    {
        if ( row < 0 || row >= data.size() ) return null;
        ResultsTableObject temp = (ResultsTableObject) data.get(row);
        return temp.getBaseCaseObj();
    }
    
    public String getSelectedURL(int row)
    {
        if ( row < 0 || row >= data.size() ) return null;
        ResultsTableObject temp = (ResultsTableObject) data.get(row);
        return temp.getFullURL();
    }
    public String getSelectedStatus(int row)
    {
        if ( row < 0 || row >= data.size() ) return null;
        ResultsTableObject temp = (ResultsTableObject) data.get(row);
        return temp.getFieldStatus();
    }
    
    public void setValueAt(Object value, int row, int col) 
    {
        //data[row][col] = value;
        //fireTableCellUpdated(row, col);
        
        ResultsTableObject[] tempArray = new ResultsTableObject[data.size()];
        if(!data.isEmpty())
        {
            tempArray = (ResultsTableObject[]) data.toArray(tempArray);
            for(int a = 0; a < tempArray.length; a++)
            {
                if(col == 4)
                {
                    Boolean oldValue = tempArray[row].isScanFurther();
                    tempArray[row].setScanFurther((Boolean) value);
                    
                    /*
                     * Code to add and remove dir to scan if the checkbox changed
                     */
                    
                    if(((Boolean) value).booleanValue())
                    {
                        tempArray[row].setFieldStatus("Waiting");
                        if(!oldValue)
                        {
                            manager.addToDirQueue(tempArray[row].getFieldFound());
                        }
                    }
                    else
                    {
                         tempArray[row].setFieldStatus("Not to be tested");
                         if(oldValue)
                         {
                             manager.removeFromDirQueue(tempArray[row].getFieldFound());
                         }
                    }
                }
            }
            
            
            while(!data.isEmpty())
            {
                data.remove(0);
            }
            
            for(int b = 0; b  < tempArray.length; b++)
            {
                ResultsTableObject temp = tempArray[b];
                data.add(temp);
            }
            
            this.fireTableDataChanged();
        }
    }
    
    public List getList()
    {
        return data;
    }
    
    public void clearData()
    {
        data.clear();
    }

    public void setManager(Manager manager)
    {
        this.manager = manager;
    }
    
    

}
