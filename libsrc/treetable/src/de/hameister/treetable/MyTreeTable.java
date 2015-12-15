package de.hameister.treetable;

import java.awt.Dimension;

import javax.swing.JTable;
import javax.swing.plaf.basic.BasicTableUI;

public class MyTreeTable extends JTable {

    private MyTreeTableCellRenderer tree;
    private MyTreeTableModel treeTableModel;

    public MyTreeTableCellRenderer getTree() {
        return tree;
    }

    public MyTreeTableModel getTreeTableModel() {
        return treeTableModel;
    }

    public void setTreeModel(MyTreeTableModel treeTableModel) {
        // JTree erstellen.
        this.treeTableModel = treeTableModel;
        tree = new MyTreeTableCellRenderer(this, treeTableModel);

        // Modell setzen.
        super.setModel(new MyTreeTableModelAdapter(treeTableModel, tree));

        // Gleichzeitiges Selektieren fuer Tree und Table.
        MyTreeTableSelectionModel selectionModel = new MyTreeTableSelectionModel();
        tree.setSelectionModel(selectionModel); //For the tree
        setSelectionModel(selectionModel.getListSelectionModel()); //For the table

        // Renderer fuer den Tree.
        setDefaultRenderer(MyTreeTableModel.class, tree);
        // Editor fuer die TreeTable
        setDefaultEditor(MyTreeTableModel.class, new MyTreeTableCellEditor(tree, this));

        // Kein Grid anzeigen.
        setShowGrid(false);

        // Keine Abstaende.
        setIntercellSpacing(new Dimension(0, 0));
    }

    public MyTreeTable(MyTreeTableModel treeTableModel) {
        super();
        setUI(new BasicTableUI());
        setTreeModel(treeTableModel);
    }
}
