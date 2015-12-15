package de.hameister.treetable;



import javax.swing.tree.TreeModel;

public interface MyTreeTableModel extends TreeModel {

    /**
     * Returns the number of available columns.
     *
     * @return Number of Columns
     */
    public int getColumnCount();

    /**
     * Returns the column name.
     *
     * @param column Column number
     * @return Column name
     */
    public String getColumnName(int column);

    /**
     * Returns the type (class) of a column.
     *
     * @param column Column number
     * @return Class
     */
    public Class<?> getColumnClass(int column);

    /**
     * Returns the value of a node in a column.
     *
     * @param node Node
     * @param column Column number
     * @return Value of the node in the column
     */
    public Object getValueAt(Object node, int column);

    /**
     * Check if a cell of a node in one column is editable.
     *
     * @param node Node
     * @param column Column number
     * @return true/false
     */
    public boolean isCellEditable(Object node, int column);

    /**
     * Sets a value for a node in one column.
     *
     * @param aValue New value
     * @param node Node
     * @param column Column number
     */
    public void setValueAt(Object aValue, Object node, int column);
}
