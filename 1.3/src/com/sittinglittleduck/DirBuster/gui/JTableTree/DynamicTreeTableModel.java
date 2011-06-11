/*
 * Copyright 1997-1999 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer. 
 *   
 * - Redistribution in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials
 *   provided with the distribution. 
 *   
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.  
 * 
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY
 * DAMAGES OR LIABILITIES SUFFERED BY LICENSEE AS A RESULT OF OR
 * RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THIS SOFTWARE OR
 * ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE 
 * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT,   
 * SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER  
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF 
 * THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS 
 * BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 */
package com.sittinglittleduck.DirBuster.gui.JTableTree;

import java.lang.reflect.*;
import javax.swing.tree.*;

/**
 * An implementation of TreeTableModel that uses reflection to answer
 * TableModel methods. This works off a handful
 * of values. A TreeNode is used to answer all the TreeModel related
 * methods (similiar to AbstractTreeTableModel and DefaultTreeModel).
 * The column names are specified in the constructor. The values for
 * the columns are dynamically obtained via reflection, you simply
 * provide the method names. The methods used to set a particular value are
 * also specified as an array of method names, a null method name, or
 * null array indicates the column isn't editable. And the class types,
 * used for the TableModel method getColumnClass are specified in the
 * constructor.
 *
 * @author Scott Violet
 */
public class DynamicTreeTableModel extends AbstractTreeTableModel {
    /** Names of the columns, used for the TableModel getColumnName method. */
    private String[]        columnNames;
    /** Method names used to determine a particular value. Used for the
     * TableModel method getValueAt. */
    private String[]        methodNames;
    /** Setter method names, used to set a particular value. Used for the
     * TableModel method setValueAt. A null entry, or array, indicates the
     * column is not editable.
     */
    private String[]        setterMethodNames;
    /** Column classes, used for the TableModel method getColumnClass. */
    private Class[]         cTypes;


    /**
     * Constructor for creating a DynamicTreeTableModel.
     */
    public DynamicTreeTableModel(TreeNode root, String[] columnNames,
				 String[] getterMethodNames,
				 String[] setterMethodNames,
				 Class[] cTypes) {
	super(root);
	this.columnNames = columnNames;
	this.methodNames = getterMethodNames;
	this.setterMethodNames = setterMethodNames;
	this.cTypes = cTypes;
    }

    //
    // TreeModel interface
    //

    /**
     * TreeModel method to return the number of children of a particular
     * node. Since <code>node</code> is a TreeNode, this can be answered
     * via the TreeNode method <code>getChildCount</code>.
     */
    public int getChildCount(Object node) { 
	return ((TreeNode)node).getChildCount();
    }

    /**
     * TreeModel method to locate a particular child of the specified
     * node. Since <code>node</code> is a TreeNode, this can be answered
     * via the TreeNode method <code>getChild</code>.
     */
    public Object getChild(Object node, int i) {
	return ((TreeNode)node).getChildAt(i);
    }

    /**
     * TreeModel method to determine if a node is a leaf. 
     * Since <code>node</code> is a TreeNode, this can be answered
     * via the TreeNode method <code>isLeaf</code>.
     */
    public boolean isLeaf(Object node) {
	return ((TreeNode)node).isLeaf();
    }

    //
    //  The TreeTable interface. 
    //

    /**
     * Returns the number of column names passed into the constructor.
     */
    public int getColumnCount() {
	return columnNames.length;
    }

    /**
     * Returns the column name passed into the constructor.
     */
    public String getColumnName(int column) {
	if (cTypes == null || column < 0 || column >= cTypes.length) {
	    return null;
	}
	return columnNames[column];
    }

    /**
     * Returns the column class for column <code>column</code>. This
     * is set in the constructor.
     */
    public Class getColumnClass(int column) {
	if (cTypes == null || column < 0 || column >= cTypes.length) {
	    return null;
	}
	return cTypes[column];
    }

    /**
     * Returns the value for the column <code>column</code> and object
     * <code>node</code>. The return value is determined by invoking
     * the method specified in constructor for the passed in column.
     */
    public Object getValueAt(Object node, int column) {
	try {
	    Method method = node.getClass().getMethod(methodNames[column],
						      null);
	    if (method != null) {
		return method.invoke(node, null);
	    }
	}
	catch  (Throwable th) {}
   
	return null; 
    }

    /**
     * Returns true if there is a setter method name for column
     * <code>column</code>. This is set in the constructor.
     */
    public boolean isCellEditable(Object node, int column) { 
         return (setterMethodNames != null &&
	         setterMethodNames[column] != null);
    }

    /**
     * Sets the value to <code>aValue</code> for the object
     * <code>node</code> in column <code>column</code>. This is done
     * by using the setter method name, and coercing the passed in
     * value to the specified type.
     */
    // Note: This looks up the methods each time! This is rather inefficient;
    // it should really be changed to cache matching methods/constructors
    // based on <code>node</code>'s class, and <code>aValue</code>'s class.
    public void setValueAt(Object aValue, Object node, int column) {
	boolean found = false;
	try {
	    // We have to search through all the methods since the
	    // types may not match up.
	    Method[] methods = node.getClass().getMethods();

	    for (int counter = methods.length - 1; counter >= 0; counter--) {
		if (methods[counter].getName().equals
		       (setterMethodNames[column]) && methods[counter].
		       getParameterTypes() != null && methods[counter].
		       getParameterTypes().length == 1) {
		    // We found a matching method
		    Class param = methods[counter].getParameterTypes()[0];
		    if (!param.isInstance(aValue)) {
			// Yes, we can use the value passed in directly,
			// no coercision is necessary!
			if (aValue instanceof String &&
			    ((String)aValue).length() == 0) {
			    // Assume an empty string is null, this is
			    // probably bogus for here.
			    aValue = null;
			}
			else {
			    // Have to attempt some sort of coercision.
			    // See if the expected parameter type has
			    // a constructor that takes a String.
			    Constructor cs = param.getConstructor
			                 (new Class[] { String.class });
			    if (cs != null) {
				aValue = cs.newInstance(new Object[]
							{ aValue });
			    }
			    else {
				aValue = null;
			    }
			}
		    }
		    // null either means it was an empty string, or there
		    // was no translation. Could potentially deal with these
		    // differently.
		    methods[counter].invoke(node, new Object[] { aValue });
		    found = true;
		    break;
		}
	    }
	} catch (Throwable th) {
	    System.out.println("exception: " + th);
	}
	if (found) {
	    // The value changed, fire an event to notify listeners.
	    TreeNode parent = ((TreeNode)node).getParent();
	    fireTreeNodesChanged(this, getPathToRoot(parent),
				 new int[] { getIndexOfChild(parent, node) },
				 new Object[] { node });
	}
    }

    /**
     * Builds the parents of the node up to and including the root node,
     * where the original node is the last element in the returned array.
     * The length of the returned array gives the node's depth in the
     * tree.
     * 
     * @param aNode the TreeNode to get the path for
     * @param an array of TreeNodes giving the path from the root to the
     *        specified node. 
     */
    public TreeNode[] getPathToRoot(TreeNode aNode) {
        return getPathToRoot(aNode, 0);
    }

    /**
     * Builds the parents of the node up to and including the root node,
     * where the original node is the last element in the returned array.
     * The length of the returned array gives the node's depth in the
     * tree.
     * 
     * @param aNode  the TreeNode to get the path for
     * @param depth  an int giving the number of steps already taken towards
     *        the root (on recursive calls), used to size the returned array
     * @return an array of TreeNodes giving the path from the root to the
     *         specified node 
     */
    private TreeNode[] getPathToRoot(TreeNode aNode, int depth) {
        TreeNode[]              retNodes;
	// This method recurses, traversing towards the root in order
	// size the array. On the way back, it fills in the nodes,
	// starting from the root and working back to the original node.

        /* Check for null, in case someone passed in a null node, or
           they passed in an element that isn't rooted at root. */
        if(aNode == null) {
            if(depth == 0)
                return null;
            else
                retNodes = new TreeNode[depth];
        }
        else {
            depth++;
            if(aNode == root)
                retNodes = new TreeNode[depth];
            else
                retNodes = getPathToRoot(aNode.getParent(), depth);
            retNodes[retNodes.length - depth] = aNode;
        }
        return retNodes;
    }
}

