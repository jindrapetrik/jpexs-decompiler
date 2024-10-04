/*
 * www.javagl.de - JTreeTable
 *
 * Copyright (c) 2016 Marco Hutter - http://www.javagl.de
 * 
 * This library is based on the code from the article "Creating TreeTables"
 * by Sun Microsystems (now known as Oracle). 
 * 
 * The original copyright header:
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
package de.javagl.treetable;

import javax.swing.tree.TreeModel;

/**
 * TreeTableModel is the model used by a JTreeTable. It extends TreeModel
 * to add methods for getting information about the set of columns each 
 * node in the TreeTableModel may have. Each column, like a column in 
 * a TableModel, has a name and a type associated with it. Each node in 
 * the TreeTableModel can return a value for each of the columns and 
 * set that value if isCellEditable() returns true. <br>
 * <br>
 * <b>Note:</b> In order to actually display the JTree in one column of the 
 * JTreeTable, implementors of this class have to return the 
 * <code>TreeTableModel.class</code> as the respective column class
 * in their implementation of the {@link #getColumnClass(int)} method:
 * <pre><code>
 * public Class&lt;?&gt; getColumnClass(int column)
 * {
 *     if (column == columnThatShouldContainTheTree)
 *     {
 *         return TreeTableModel.class;
 *     }
 *     // Return other types as desired:
 *     return Object.class;
 * }
 * </code></pre> 
 * 
 */
public interface TreeTableModel extends TreeModel
{
    /**
     * Returns the number of available columns.
     * 
     * @return The number of columns
     */
    int getColumnCount();

    /**
     * Returns the name for the specified column
     * 
     * @param column The column
     * @return The column name
     */
    String getColumnName(int column);

    /**
     * Returns the type for the specified column.<br> 
     * <br>
     * <b>Note:</b> For the column that should display the JTree, this 
     * method must return <code>TreeTableModel.class</code>.
     * 
     * @param column The column
     * @return The column name
     */
    Class<?> getColumnClass(int column);

    /**
     * Returns the value to be displayed for the given node in the 
     * specified column
     * 
     * @param node The node
     * @param column The column
     * @return The object that should be displayed
     */
    Object getValueAt(Object node, int column);

    /**
     * Indicates whether the the value for the given node in the
     * specified column is editable
     * 
     * @param node The node
     * @param column The column
     * @return Whether the specified value is editable
     */
    boolean isCellEditable(Object node, int column);

    /**
     * Sets the value for the given node in the specified column
     * 
     * @param value The value to set
     * @param node The node
     * @param column The column
     */
    void setValueAt(Object value, Object node, int column);
}
