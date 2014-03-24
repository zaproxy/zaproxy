/*
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
package com.sittinglittleduck.DirBuster.gui.Tree;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

import com.sittinglittleduck.DirBuster.gui.ResultsTableObject;

/**
 *
 * @author james
 */
public class ResultsNode extends DefaultMutableTreeNode
{

    ResultsTableObject result;
    Vector<ResultsNode> children = new Vector<ResultsNode>(100, 10);
    String name;

    public ResultsNode(ResultsTableObject result)
    {
        this.result = result;
    }
    
    public ResultsNode(String name)
    {
        this.result = null;
        this.name = name;
    }

    public void addChild(ResultsNode object)
    {
        children.addElement(object);
    }
    
    public ResultsNode getChild(int index)
    {
        return children.elementAt(index);
    }

    public Object[] getChildren()
    {
        return children.toArray();
    }

    public boolean isLeaf()
    {
        if(result == null)
        {
            return false;
        }
        if (result.getFieldType().equalsIgnoreCase("dir"))
        {
            return false;
        }
        else
        {
            return true;
        }

    }

    public String toString()
    {
        if(result == null)
        {
            return name;
        }
        
        String string = null;
        try
        {

            string = covertData(new URL(result.getFullURL()));
            //return result.getFieldFound() + " | " + result.getFieldResponceCode();
        }
        catch (MalformedURLException ex)
        {
            Logger.getLogger(ResultsNode.class.getName()).log(Level.SEVERE, null, ex);
        }

        return string;
    }

    private String covertData(URL url)
    {

        String item = url.getPath();

        if (item.equals("/"))
        {
            return item;
        }
        if (item.endsWith("/"))
        {
            item = item.substring(0, item.length() - 1);

        }
        //System.out.println("item = " + item);
        int location = item.lastIndexOf("/");
        item = item.substring(location + 1);
        //System.out.println("item = " + item);
        return item;
    }

    public Vector<ResultsNode> getChildrenVector()
    {
        return children;
    }
    
    public void clearData()
    {
        children.removeAllElements();
        result = null;
        name = null;
    }

    public ResultsTableObject getResult()
    {
        return result;
    }
    
    public int getNumberOfChildren()
    {
        return children.size();
    }
    
    public boolean isResultSet()
    {
        if(result == null)
        {
            return false;
        }
        else
        {
            return true;
        }
    }
    
    public void setResult(ResultsTableObject tableObject)
    {
        result = tableObject;
    }
    
    public ResultsNode findChildBasedOnString(String name)
    {
        for(int a = 0; a < children.size(); a++)
        {
            if(children.elementAt(a).toString().equals(name))
            {
                return children.elementAt(a);
            }
        }
        return null;
    }
    
}
