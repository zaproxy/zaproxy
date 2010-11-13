/*
 * TreeTableModelEvent.java
 *
 * Created on 07 December 2004, 07:19
 * 
 * Code taken from the owasp webscrab project which is GPL
 */

package com.sittinglittleduck.DirBuster.gui.JTableTree;

import javax.swing.event.TreeModelEvent;

import javax.swing.tree.TreePath;


/**
 *
 * @author  rogan
 */
public class TreeTableModelEvent extends TreeModelEvent{
    
    protected int column;
    protected int type = 0;
    
    /** Identifies the addtion of new columns. */
    public static final int INSERT =  1;
    /** Identifies a change to existing data. */
    public static final int UPDATE =  0;
    /** Identifies the removal of columns. */
    public static final int DELETE = -1;
    
    /**
     * Used to create an event when the node structure has changed in some way,
     * identifying the path to the root of the modified subtree as a TreePath
     * object. For more information on this event specification, see
     * <code>TreeModelEvent(Object,Object[])</code>.
     *
     * @param source the Object responsible for generating the event (typically
     *               the creator of the event object passes <code>this</code>
     *               for its value)
     * @param path   a TreePath object that identifies the path to the
     *               change. In the DefaultTreeModel,
     *               this object contains an array of user-data objects,
     *               but a subclass of TreePath could use some totally
     *               different mechanism -- for example, a node ID number
     * @param column the column in which the change occurred
     */
    public TreeTableModelEvent(Object source, TreePath path, int column)
    {
	super(source, path);
	this.path = path;
	this.childIndices = new int[0];
        this.column = column;
    }
    
    /**
     * Used to create an event when the node structure has changed in some way,
     * identifying the path to the root of the modified subtree as a TreePath
     * object. For more information on this event specification, see
     * <code>TreeModelEvent(Object,Object[])</code>.
     *
     * @param source the Object responsible for generating the event (typically
     *               the creator of the event object passes <code>this</code>
     *               for its value)
     * @param path   a TreePath object that identifies the path to the
     *               change. In the DefaultTreeModel,
     *               this object contains an array of user-data objects,
     *               but a subclass of TreePath could use some totally
     *               different mechanism -- for example, a node ID number
     * @param column the column in which the change occurred
     */
    public TreeTableModelEvent(Object source, TreePath path, int column, int type)
    {
	this(source, path, column);
        this.type = type;
    }
    
    public int getColumn() {
        return column;
    }
    
    public int getType() {
        return type;
    }
    
}
