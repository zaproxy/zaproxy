/*
 * TreeTableModel.java
 * 
 * Copyright 1998 Sun Microsystems, Inc.  All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.sittinglittleduck.DirBuster.gui.JTableTree;

import javax.swing.tree.TreeModel;

/**
 * TreeTableModel is the model used by a JTreeTable. It extends TreeModel
 * to add methods for getting inforamtion about the set of columns each 
 * node in the TreeTableModel may have. Each column, like a column in 
 * a TableModel, has a name and a type associated with it. Each node in 
 * the TreeTableModel can return a value for each of the columns and 
 * set that value if isCellEditable() returns true. 
 *
 * @author Philip Milne 
 * @author Scott Violet
 */
public interface TreeTableModel extends TreeModel
{
    /**
     * Returns the number ofs availible column.
     */
    public int getColumnCount();

    /**
     * Returns the name for column number <code>column</code>.
     */
    public String getColumnName(int column);

    /**
     * Returns the type for column number <code>column</code>.
     */
    public Class getColumnClass(int column);

    /**
     * Returns the value to be displayed for node <code>node</code>, 
     * at column number <code>column</code>.
     */
    public Object getValueAt(Object node, int column);

    /**
     * Indicates whether the the value for node <code>node</code>, 
     * at column number <code>column</code> is editable.
     */
    public boolean isCellEditable(Object node, int column);

    /**
     * Sets the value for node <code>node</code>, 
     * at column number <code>column</code>.
     */
    public void setValueAt(Object aValue, Object node, int column);
    
}
