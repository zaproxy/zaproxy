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

import com.sittinglittleduck.DirBuster.gui.ResultsTableObject;
import com.sittinglittleduck.DirBuster.gui.JTableTree.AbstractTreeTableModel;
import com.sittinglittleduck.DirBuster.gui.JTableTree.TreeTableModel;

/**
 *
 * @author james
 */
public class ResultsTableTreeModel extends AbstractTreeTableModel
        implements TreeTableModel
{
    protected static String[] cNames = {new String("Directory Stucture"), new String("Responce Code"), new String("Responce Size")};
    // Types of the columns.
    protected static Class[]  cTypes = {TreeTableModel.class, String.class, String.class};
    private ResultsNode rootNode;

    public ResultsTableTreeModel(ResultsNode rootNode)
    {
        super(rootNode);
        this.rootNode = rootNode;
    }
    
    @Override
    public Object getRoot()
    {
        return rootNode;
    }
    
    public void cleartable()
    {
        rootNode.clearData();
        this.fireTreeStructureChanged(rootNode, getPathToRoot(rootNode), null, rootNode.getChildren());
    }
    
    public Object getChild(Object parent, int index)
    {
        return ((ResultsNode) parent).getChild(index);
        
    }

    public int getChildCount(Object parent)
    {
        return ((ResultsNode) parent).getNumberOfChildren();
    }

    @Override
    public boolean isLeaf(Object node)
    {
        return ((ResultsNode) node).isLeaf();
    }

    
    @Override
    public int getIndexOfChild(Object parent, Object child)
    {
        //TODO complete;
        return 0;
    }
    

    public int getColumnCount()
    {
        return cNames.length;
    }

    public String getColumnName(int column)
    {
        return cNames[column];
    }

    public Object getValueAt(Object node, int col)
    {
        ResultsNode resultsNode = ((ResultsNode) node);
        if (col == 0)
        {
            return resultsNode.toString();
        }
        else if (col == 1)
        {
            if(resultsNode.result == null)
            {
                return "???";
            }
            else
            {
                return resultsNode.result.getFieldResponceCode();
            }
        }
        else if (col == 2)
        {
            if(resultsNode.result == null)
            {
                return "???";
            }
            else
            {
                return resultsNode.result.getRawResponce().length();
            }
        }
        else
        {
            return null;
        }
    }
    
    public Class getColumnClass(int column) 
    {
        //super.
	return cTypes[column];
    }
    
    public void addRow(ResultsTableObject result)
    {
        ResultsNode rootTemp = rootNode;
        try
        {
            /*
             * Split the url up into a vector of items
             * eg test test1 test.asp = /test/test1/test.asp
             */
            URL url = new URL(result.getFullURL());
            String fullItem = url.getPath();
            
            /*
             * different case if it is the root node!
             */
            if(fullItem.equals("/"))
            {
                //ResultsNode root = (ResultsNode) gui.jPanelRunning.jTableTreeResults.getTree().getModel().getRoot();
                if(!rootTemp.isResultSet())
                {
                    rootTemp.setResult(result);
                    //this.fireTreeNodesChanged(rootTemp, getPathToRoot(rootTemp), null, rootTemp.getChildren());
                }
                
                return;
            }

            Vector<String> items = new Vector<String>(10, 1);

            int index = 0;
            while ((index = fullItem.indexOf("/")) != -1)
            {
                String realitem = fullItem.substring(0, index);
                String rest = fullItem.substring(index + 1);
                if (realitem.length() > 0)
                {
                    items.addElement(realitem);
                }
                fullItem = rest;
            }

            if (!fullItem.contains("/") && fullItem.length() > 0)
            {
                items.addElement(fullItem);
            }

            /*
             * starting the root node search it's children and see if the item is there.
             */
            //ResultsNode root = (ResultsNode) gui.jPanelRunning.jTableTreeResults.getTree().getModel().getRoot();

            for (int a = 0; a < items.size(); a++)
            {
                ResultsNode node = rootTemp.findChildBasedOnString(items.elementAt(a));

                if (node == null)
                {
                    /*
                     * if it is the last one
                     */
                    if (a == items.size() - 1)
                    {
                        rootTemp.addChild(new ResultsNode(result));
                        
                        
                        
                        fireTreeNodesInserted(rootTemp, getPathToRoot(rootTemp), new int[]{rootTemp.getChildren().length -1}, rootTemp.getChildren());
                        
                    }
                    else
                    {
                        /*
                         * create the new node as this does exist
                         */
                        
                        rootTemp.addChild(new ResultsNode(items.elementAt(a)));
                        fireTreeNodesInserted(rootTemp, getPathToRoot(rootTemp), new int[]{rootTemp.getChildren().length -1}, rootTemp.getChildren());

                        /*
                         * find the node we have just added
                         */
                        node = rootTemp.findChildBasedOnString(items.elementAt(a));
                    }
                }
                /*
                 * if the last item is there but has not had it details set
                 */
                else if(!rootTemp.isResultSet() && a == (items.size() - 1))
                {
                    rootTemp.setResult(result);
                    /*
                     * last item so we exit!
                     */
                    //gui.jPanelRunning.jTableTreeResults.updateUI();
                    this.fireTreeNodesChanged(rootTemp, getPathToRoot(rootTemp), new int[]{rootTemp.getChildren().length -1}, rootTemp.getChildren());
                    return;
                }

                rootTemp = node;
            }

            
            //TODO need to inprove this section of code!
            //gui.jPanelRunning.jTableTreeResults.

        }
        catch (MalformedURLException ex)
        {
            
            //Logger.getLogger(Manager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
