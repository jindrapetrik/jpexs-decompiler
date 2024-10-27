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

import javax.swing.tree.*;
import javax.swing.event.*;

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
