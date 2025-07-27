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

import javax.swing.event.*;
import javax.swing.tree.*;

/**
 * An abstract implementation of the TreeTableModel interface, handling the list
 * of listeners.<br>
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
 */
public abstract class AbstractTreeTableModel implements TreeTableModel
{
    /**
     * The root node of the tree
     */
    protected Object root;
    
    /**
     * The TreeModelListeners that will be notified about modifications
     */
    private final EventListenerList listenerList = new EventListenerList();

    /**
     * Default constructor 
     * 
     * @param root The root node of the tree
     */
    protected AbstractTreeTableModel(Object root)
    {
        this.root = root;
    }

    @Override
    public Object getRoot()
    {
        return root;
    }
    
    @Override
    public boolean isLeaf(Object node)
    {
        return getChildCount(node) == 0;
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue)
    {
        // Empty default implementation
    }

    @Override
    public int getIndexOfChild(Object parent, Object child)
    {
        for (int i = 0; i < getChildCount(parent); i++)
        {
            if (getChild(parent, i).equals(child))
            {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void addTreeModelListener(TreeModelListener l)
    {
        listenerList.add(TreeModelListener.class, l);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l)
    {
        listenerList.remove(TreeModelListener.class, l);
    }

    /**
     * Notify all listeners that have registered interest for notification on
     * this event type. The event instance is lazily created using the
     * parameters passed into the fire method.
     * 
     * @param source The source of the event 
     * @param path The tree path
     * @param childIndices The child indices
     * @param children The children
     */
    protected final void fireTreeNodesChanged(
        Object source, Object[] path, int[] childIndices, Object[] children)
    {
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        for (int i = listeners.length - 2; i >= 0; i -= 2)
        {
            if (listeners[i] == TreeModelListener.class)
            {
                if (e == null)
                {
                    e = new TreeModelEvent(
                        source, path, childIndices, children);
                }
                ((TreeModelListener) listeners[i + 1]).treeNodesChanged(e);
            }
        }
    }

    /**
     * Notify all listeners that have registered interest for notification on
     * this event type. The event instance is lazily created using the
     * parameters passed into the fire method.
     * 
     * @param source The source of the event 
     * @param path The tree path
     * @param childIndices The child indices
     * @param children The children
     */
    protected final void fireTreeNodesInserted(
        Object source, Object[] path, int[] childIndices, Object[] children)
    {
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        for (int i = listeners.length - 2; i >= 0; i -= 2)
        {
            if (listeners[i] == TreeModelListener.class)
            {
                if (e == null)
                {
                    e = new TreeModelEvent(
                        source, path, childIndices, children);
                }
                ((TreeModelListener) listeners[i + 1]).treeNodesInserted(e);
            }
        }
    }

    /**
     * Notify all listeners that have registered interest for notification on
     * this event type. The event instance is lazily created using the
     * parameters passed into the fire method.
     * 
     * @param source The source of the event 
     * @param path The tree path
     * @param childIndices The child indices
     * @param children The children
     */
    protected final void fireTreeNodesRemoved(
        Object source, Object[] path, int[] childIndices, Object[] children)
    {
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        for (int i = listeners.length - 2; i >= 0; i -= 2)
        {
            if (listeners[i] == TreeModelListener.class)
            {
                if (e == null)
                {
                    e = new TreeModelEvent(
                        source, path, childIndices, children);
                }
                ((TreeModelListener) listeners[i + 1]).treeNodesRemoved(e);
            }
        }
    }

    /**
     * Notify all listeners that have registered interest for notification on
     * this event type. The event instance is lazily created using the
     * parameters passed into the fire method.
     * 
     * @param source The source of the event 
     * @param path The tree path
     * @param childIndices The child indices
     * @param children The children
     */
    protected final void fireTreeStructureChanged(
        Object source, Object[] path, int[] childIndices, Object[] children)
    {
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        for (int i = listeners.length - 2; i >= 0; i -= 2)
        {
            if (listeners[i] == TreeModelListener.class)
            {
                if (e == null)
                {
                    e = new TreeModelEvent(
                        source, path, childIndices, children);
                }
                ((TreeModelListener) listeners[i + 1]).treeStructureChanged(e);
            }
        }
    }

    @Override
    public boolean isCellEditable(Object node, int column)
    {
        return getColumnClass(column) == TreeTableModel.class;
    }

    @Override
    public void setValueAt(Object aValue, Object node, int column)
    {
        // Empty default implementation
    }

}
