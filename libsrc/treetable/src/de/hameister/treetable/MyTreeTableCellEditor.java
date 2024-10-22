package de.hameister.treetable;



import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.table.TableCellEditor;

public class MyTreeTableCellEditor extends AbstractCellEditor implements TableCellEditor {

    private JTree tree;
    private JTable table;

    public MyTreeTableCellEditor(JTree tree, JTable table) {
        this.tree = tree;
        this.table = table;
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int r, int c) {
        return tree;
    }

    public boolean isCellEditable(EventObject e) {
        if (e instanceof MouseEvent) {
            int column1 = 0;
            MouseEvent me = (MouseEvent) e;
            int doubleClick = 2;
            MouseEvent newME = new MouseEvent(tree, me.getID(), me.getWhen(), me.getModifiers(), me.getX() - table.getCellRect(0, column1, true).x, me.getY(), doubleClick, me.isPopupTrigger());
            tree.dispatchEvent(newME);
        }
        return false;
    }

    @Override
    public Object getCellEditorValue() {
        return null;
    }

}
