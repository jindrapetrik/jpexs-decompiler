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

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.TreePath;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

/**
 * This is a wrapper class takes a TreeTableModel and implements the table model
 * interface. The implementation is trivial, with all of the event dispatching
 * support provided by the superclass: the AbstractTableModel.
 */
class TreeTableModelAdapter extends AbstractTableModel
{
    /**
     * Serial UID
     */
    private static final long serialVersionUID = 8489726674002235447L;
    
    /**
     * The tree that backs this adapter. When the tree is expanded or 
     * collapsed, the table model is updated accordingly
     */
    private final JTree tree;
    
    /**
     * The backing {@link TreeTableModel}
     */
    private final TreeTableModel treeTableModel;

    /**
     * Default constructor
     * @param treeTableModel The backing {@link TreeTableModel}
     * @param tree The tree
     */
    TreeTableModelAdapter(TreeTableModel treeTableModel, JTree tree)
    {
        this.tree = tree;
        this.treeTableModel = treeTableModel;

        tree.addTreeExpansionListener(new TreeExpansionListener()
        {
            // Don't use fireTableRowsInserted() here; the selection model
            // would get updated twice.
            @Override
            public void treeExpanded(TreeExpansionEvent event)
            {
                fireTableDataChanged();
            }

            @Override
            public void treeCollapsed(TreeExpansionEvent event)
            {
                fireTableDataChanged();
            }
        });

        // Install a TreeModelListener that can update the table when
        // tree changes. We use delayedFireTableDataChanged as we can
        // not be guaranteed the tree will have finished processing
        // the event before us.
        treeTableModel.addTreeModelListener(new TreeModelListener()
        {
            @Override
            public void treeNodesChanged(TreeModelEvent e)
            {
                delayedFireTableDataChanged();
            }

            @Override
            public void treeNodesInserted(TreeModelEvent e)
            {
                delayedFireTableDataChanged();
            }

            @Override
            public void treeNodesRemoved(TreeModelEvent e)
            {
                delayedFireTableDataChanged();
            }

            @Override
            public void treeStructureChanged(TreeModelEvent e)
            {
                delayedFireTableDataChanged();
            }
        });
    }

    @Override
    public int getColumnCount()
    {
        return treeTableModel.getColumnCount();
    }

    @Override
    public String getColumnName(int column)
    {
        return treeTableModel.getColumnName(column);
    }

    @Override
    public Class<?> getColumnClass(int column)
    {
        return treeTableModel.getColumnClass(column);
    }

    @Override
    public int getRowCount()
    {
        return tree.getRowCount();
    }

    @Override
    public Object getValueAt(int row, int column)
    {
        return treeTableModel.getValueAt(nodeForRow(row), column);
    }

    @Override
    public boolean isCellEditable(int row, int column)
    {
        return treeTableModel.isCellEditable(nodeForRow(row), column);
    }

    @Override
    public void setValueAt(Object value, int row, int column)
    {
        treeTableModel.setValueAt(value, nodeForRow(row), column);
    }

    
    /**
     * Returns the tree node that is located in the specified row
     *  
     * @param row The row
     * @return The tree node
     */
    private Object nodeForRow(int row)
    {
        TreePath treePath = tree.getPathForRow(row);
        return treePath.getLastPathComponent();
    }
    
    /**
     * Invokes fireTableDataChanged after all the pending events have been
     * processed. SwingUtilities.invokeLater is used to handle this.
     */
    private void delayedFireTableDataChanged()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                fireTableDataChanged();
            }
        });
    }
}
