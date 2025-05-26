/*
 *  Copyright (C) 2010-2025 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
     * Indicates whether the value for the given node in the
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
