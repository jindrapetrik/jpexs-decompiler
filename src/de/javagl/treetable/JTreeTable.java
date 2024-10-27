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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * A JTreeTable is a table that uses a JTree as a renderer (and editor) for
 * the cells in a particular column. It is backed by a {@link TreeTableModel}
 * that provides the tree structure as well as the table data.
 */
public class JTreeTable extends JTable
{
    /**
     * Serial UID
     */
    private static final long serialVersionUID = 6063660027266471485L;
    /** 
     * A special JTree that is used as a TableCellRenderer
     */
    private final TreeTableCellRenderer tree;

    /**
     * Creates a new JTreeTable that is backed by the given 
     * {@link TreeTableModel}
     * 
     * @param treeTableModel The {@link TreeTableModel}
     */
    public JTreeTable(TreeTableModel treeTableModel)
    {
        // Create the tree. It will be used as a renderer and editor.
        tree = new TreeTableCellRenderer(treeTableModel);

        // Install a tableModel representing the visible rows in the tree.
        super.setModel(new TreeTableModelAdapter(treeTableModel, tree));

        // Force the JTable and JTree to share their row selection models.
        ListToTreeSelectionModelWrapper selectionWrapper =
            new ListToTreeSelectionModelWrapper();
        tree.setSelectionModel(selectionWrapper);
        setSelectionModel(selectionWrapper.getListSelectionModel());

        // Install the tree editor renderer and editor.
        setDefaultRenderer(TreeTableModel.class, tree);
        setDefaultEditor(TreeTableModel.class, new TreeTableCellEditor());

        // No grid.
        setShowGrid(false);

        // No inter cell spacing
        setIntercellSpacing(new Dimension(0, 0));

        // And update the height of the trees row to match that of
        // the table.
        if (tree.getRowHeight() < 1)
        {
            // Metal looks better like this.
            setRowHeight(18);
        }
    }

    /**
     * Overridden to message super and forward the method to the tree. Since the
     * tree is not actually in the component hierarchy it will never receive
     * this unless we forward it in this manner.
     */
    @Override
    public void updateUI()
    {
        super.updateUI();
        if (tree != null)
        {
            tree.updateUI();
        }
        // Use the tree's default foreground and background colors in the
        // table.
        LookAndFeel.installColorsAndFont(this, 
            "Tree.background", "Tree.foreground", "Tree.font");
    }

    /*
     * Workaround for BasicTableUI anomaly. Make sure the UI never tries to
     * paint the editor. The UI currently uses different techniques to paint the
     * renderers and editors and overriding setBounds() below is not the right
     * thing to do for an editor. Returning -1 for the editing row in this case,
     * ensures the editor is never painted.
     */
    @Override
    public int getEditingRow()
    {
        return (getColumnClass(editingColumn) == TreeTableModel.class) ? 
            -1 : editingRow;
    }

    /**
     * Overridden to pass the new rowHeight to the tree.
     */
    @Override
    public void setRowHeight(int rowHeight)
    {
        super.setRowHeight(rowHeight);
        if (tree != null && tree.getRowHeight() != rowHeight)
        {
            tree.setRowHeight(getRowHeight());
        }
    }

    /**
     * Returns the tree that is being shared between the model.
     * 
     * @return The tree 
     */
    public JTree getTree()
    {
        return tree;
    }

    /**
     * A TreeCellRenderer that displays a JTree.
     */
    public class TreeTableCellRenderer extends JTree implements
        TableCellRenderer
    {
        /**
         * Serial UID
         */
        private static final long serialVersionUID = -3458046584741784015L;
        
        /** 
         * Last table/tree row asked to renderer. 
         */
        private int visibleRow;

        /**
         * Creates a new TreeTableCellRenderer that displays the JTree
         * according to the given tree model
         * 
         * @param model The tree model
         */
        TreeTableCellRenderer(TreeModel model)
        {
            super(model);
        }

        /**
         * updateUI is overridden to set the colors of the Tree's renderer to
         * match that of the table.
         */
        @Override
        public void updateUI()
        {
            super.updateUI();
            // Make the tree's cell renderer use the table's cell selection
            // colors.
            TreeCellRenderer tcr = getCellRenderer();
            if (tcr instanceof DefaultTreeCellRenderer)
            {
                DefaultTreeCellRenderer dtcr = ((DefaultTreeCellRenderer) tcr);
                // For 1.1 uncomment this, 1.2 has a bug that will cause an
                // exception to be thrown if the border selection color is
                // null.
                // dtcr.setBorderSelectionColor(null);
                dtcr.setTextSelectionColor(UIManager
                    .getColor("Table.selectionForeground"));
                dtcr.setBackgroundSelectionColor(UIManager
                    .getColor("Table.selectionBackground"));
            }
        }

        /**
         * Sets the row height of the tree, and forwards the row height to the
         * table.
         */
        @Override
        public void setRowHeight(int rowHeight)
        {
            if (rowHeight > 0)
            {
                super.setRowHeight(rowHeight);
                if (JTreeTable.this.getRowHeight() != rowHeight)
                {
                    JTreeTable.this.setRowHeight(getRowHeight());
                }
            }
        }

        /**
         * This is overridden to set the height to match that of the JTable.
         */
        @Override
        public void setBounds(int x, int y, int w, int h)
        {
            super.setBounds(x, 0, w, JTreeTable.this.getHeight());
        }

        /**
         * Sublcassed to translate the graphics such that the last visible row
         * will be drawn at 0,0.
         */
        @Override
        public void paint(Graphics g)
        {
            g.translate(0, -visibleRow * getRowHeight());
            super.paint(g);
        }

        /**
         * TreeCellRenderer method. Overridden to update the visible row.
         */
        @Override
        public Component getTableCellRendererComponent(JTable table,
            Object value, boolean isSelected, boolean hasFocus, int row,
            int column)
        {
            if (isSelected)
                setBackground(table.getSelectionBackground());
            else
                setBackground(table.getBackground());

            visibleRow = row;
            return this;
        }
    }

    /**
     * TreeTableCellEditor implementation. Component returned is the JTree.
     */
    public class TreeTableCellEditor extends AbstractCellEditor implements
        TableCellEditor
    {
        @Override
        public Component getTableCellEditorComponent(JTable table,
            Object value, boolean isSelected, int r, int c)
        {
            return tree;
        }

        /**
         * Overridden to return false, and if the event is a mouse event it is
         * forwarded to the tree.
         * <p>
         * The behavior for this is debatable, and should really be offered as a
         * property. By returning false, all keyboard actions are implemented in
         * terms of the table. By returning true, the tree would get a chance to
         * do something with the keyboard events. For the most part this is ok.
         * But for certain keys, such as left/right, the tree will
         * expand/collapse where as the table focus should really move to a
         * different column. Page up/down should also be implemented in terms of
         * the table. By returning false this also has the added benefit that
         * clicking outside of the bounds of the tree node, but still in the
         * tree column will select the row, whereas if this returned true that
         * wouldn't be the case.
         * <p>
         * By returning false we are also enforcing the policy that the tree
         * will never be editable (at least by a key sequence).
         */
        @Override
        public boolean isCellEditable(EventObject e)
        {
            if (!(e instanceof MouseEvent))
            {
                return false;
            }
            MouseEvent me = (MouseEvent) e;
            for (int counter = getColumnCount() - 1; counter >= 0; counter--)
            {
                if (getColumnClass(counter) == TreeTableModel.class)
                {
                    MouseEvent newME =
                        new MouseEvent(tree, me.getID(), me.getWhen(),
                            me.getModifiersEx(), me.getX()
                            - getCellRect(0, counter, true).x,
                            me.getY(), me.getClickCount(),
                            me.isPopupTrigger(), me.getButton());
                    tree.dispatchEvent(newME);
                    break;
                }
            }
            return false;
        }
    }

    /**
     * ListToTreeSelectionModelWrapper extends DefaultTreeSelectionModel to
     * listen for changes in the ListSelectionModel it maintains. Once a change
     * in the ListSelectionModel happens, the paths are updated in the
     * DefaultTreeSelectionModel.
     */
    class ListToTreeSelectionModelWrapper extends DefaultTreeSelectionModel
    {
        /**
         * Serial UID
         */
        private static final long serialVersionUID = -3150534152238745922L;

        /** 
         * Set to true when we are updating the ListSelectionModel. 
         */
        protected boolean updatingListSelectionModel;

        /**
         * Default constructor
         */
        ListToTreeSelectionModelWrapper()
        {
            getListSelectionModel().addListSelectionListener(
                new ListSelectionListener()
            {
                @Override
                public void valueChanged(ListSelectionEvent e)
                {
                    updateSelectedPathsFromSelectedRows();
                }
            });
        }

        /**
         * Returns the list selection model. ListToTreeSelectionModelWrapper
         * listens for changes to this model and updates the selected paths
         * accordingly.
         * 
         * @return The list selection model
         */
        ListSelectionModel getListSelectionModel()
        {
            return listSelectionModel;
        }

        /**
         * This is overridden to set <code>updatingListSelectionModel</code> and
         * message super. This is the only place DefaultTreeSelectionModel
         * alters the ListSelectionModel.
         */
        @Override
        public void resetRowSelection()
        {
            if (!updatingListSelectionModel)
            {
                updatingListSelectionModel = true;
                try
                {
                    super.resetRowSelection();
                }
                finally
                {
                    updatingListSelectionModel = false;
                }
            }
            // Notice how we don't message super if
            // updatingListSelectionModel is true. If
            // updatingListSelectionModel is true, it implies the
            // ListSelectionModel has already been updated and the
            // paths are the only thing that needs to be updated.
        }

        /**
         * If <code>updatingListSelectionModel</code> is false, this will reset
         * the selected paths from the selected rows in the list selection
         * model.
         */
        protected void updateSelectedPathsFromSelectedRows()
        {
            if (!updatingListSelectionModel)
            {
                updatingListSelectionModel = true;
                try
                {
                    // This is way expensive, ListSelectionModel needs an
                    // enumerator for iterating.
                    int min = listSelectionModel.getMinSelectionIndex();
                    int max = listSelectionModel.getMaxSelectionIndex();

                    clearSelection();
                    if (min != -1 && max != -1)
                    {
                        for (int counter = min; counter <= max; counter++)
                        {
                            if (listSelectionModel.isSelectedIndex(counter))
                            {
                                TreePath selPath = tree.getPathForRow(counter);

                                if (selPath != null)
                                {
                                    addSelectionPath(selPath);
                                }
                            }
                        }
                    }
                }
                finally
                {
                    updatingListSelectionModel = false;
                }
            }
        }

    }
    
    //JPEXS
    public void setTreeTableModel(TreeTableModel model) {
        tree.setModel(model);
        setModel(new TreeTableModelAdapter(model, tree));
        repaint();
    }
}
