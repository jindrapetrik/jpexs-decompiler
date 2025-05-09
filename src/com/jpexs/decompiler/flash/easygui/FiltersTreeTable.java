/*
 * Copyright (C) 2024 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.easygui;

import com.jpexs.decompiler.flash.ecma.EcmaNumberToString;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.filters.FILTER;
import de.javagl.treetable.JTreeTable;
import de.javagl.treetable.TreeTableModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelListener;
import javax.swing.plaf.basic.BasicTableUI;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

/**
 *
 * @author JPEXS
 */
public class FiltersTreeTable extends JTreeTable {

    public FiltersTreeTable() {
        super(new FiltersTreeTableModel(null));
        getTree().setCellRenderer(new FiltersTreeCellRenderer());
        getTree().setRootVisible(false);
        getTree().setShowsRootHandles(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int selectedRow = getSelectedRow();
                JTree tree = getTree();

                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    TreePath path = tree.getPathForRow(selectedRow);
                    if (path != null && tree.isExpanded(path)) {
                        tree.collapsePath(path);

                        int parentRow = tree.getRowForPath(path);
                        changeSelection(parentRow, 0, false, false);
                    } else if (path != null) {
                        TreePath parentPath = path.getParentPath();
                        if (parentPath != null) {
                            int parentRow = tree.getRowForPath(parentPath);
                            changeSelection(parentRow, 0, false, false);
                        }
                    }
                    e.consume();
                }

                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    TreePath path = tree.getPathForRow(selectedRow);
                    if (path != null && !tree.isExpanded(path)) {
                        tree.expandPath(path);
                        int parentRow = tree.getRowForPath(path);
                        changeSelection(parentRow, 0, false, false);
                    } else {
                        TreePath childPath = tree.getPathForRow(selectedRow + 1);
                        if (childPath != null) {
                            int childRow = tree.getRowForPath(childPath);
                            changeSelection(childRow, 0, false, false);
                        }
                    }
                    e.consume();
                }
            }
        });
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setUI(new BasicTableUI());

        setRowHeight(18);
        getTree().setRowHeight(18);

        if (View.isOceanic()) {
            setBackground(Color.WHITE);
            getTree().setBackground(Color.WHITE);
        }

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
                    int selectedRow = getSelectedRow();
                    if (selectedRow == -1) {
                        return;
                    }
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) getValueAt(selectedRow, 0);
                    Object obj = node.getUserObject();

                }
            }
        });

        getTableHeader().setReorderingAllowed(false);
    }

    public void setFilters(List<FILTER> filters) {
        setTreeTableModel(new FiltersTreeTableModel(filters));

        getColumnModel().getColumn(0).setMinWidth(200);
        getColumnModel().getColumn(0).setWidth(200);
        getColumnModel().getColumn(0).setPreferredWidth(200);
        getColumnModel().getColumn(1).setPreferredWidth(Integer.MAX_VALUE);
    }

    private static class FiltersTreeCellRenderer extends DefaultTreeCellRenderer {

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            if (value instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                Object object = node.getUserObject();
                label.setText(object.toString());
            }
            if (View.isOceanic()) {
                if (selected) {
                    label.setBackground(getBackgroundSelectionColor());
                } else {
                    label.setBackground(Color.white);
                }
                label.setOpaque(true);
            }
            return label;
        }
    }

    private static class LibraryFolder {

        private String name;

        public LibraryFolder(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return EasyStrings.translate("library.folder." + name);
        }

        public String getName() {
            return name;
        }
    }

    private static class FilterField {

        private final FILTER filter;
        private final Field field;

        public FilterField(FILTER filter, Field property) {
            this.filter = filter;
            this.field = property;
        }

        public FILTER getFilter() {
            return filter;
        }

        public Field getField() {
            return field;
        }

        public Object getValue() {
            try {
                return field.get(filter);
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                return null;
            }
        }

        @Override
        public String toString() {
            return field.getName();
        }

    }

    private static class FiltersTreeTableModel implements TreeTableModel {

        private final DefaultMutableTreeNode root;

        public FiltersTreeTableModel(List<FILTER> filters) {
            root = new DefaultMutableTreeNode("root");

            if (filters == null) {
                DefaultMutableTreeNode indeterminate = new DefaultMutableTreeNode(EasyStrings.translate("properties.instance.filters.indeterminate"));
                root.add(indeterminate);
                return;
            }

            for (FILTER filter : filters) {
                DefaultMutableTreeNode filterNode = new DefaultMutableTreeNode(filter.getClass().getSimpleName());
                root.add(filterNode);
                Field[] fields = filter.getClass().getFields();
                for (Field field : fields) {
                    if ("id".equals(field.getName())) {
                        continue;
                    }
                    FilterField filterField = new FilterField(filter, field);
                    DefaultMutableTreeNode fieldNode = new DefaultMutableTreeNode(filterField);
                    filterNode.add(fieldNode);
                }
            }
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return EasyStrings.translate("properties.instance.filters.header.property");
                case 1:
                    return EasyStrings.translate("properties.instance.filters.header.value");
                default:
                    return null;
            }
        }

        @Override
        public Class<?> getColumnClass(int column) {
            switch (column) {
                case 0:
                    return TreeTableModel.class;
                default:
                    return String.class;
            }

        }

        private String valueToString(String fieldName, Object value) {
            if (value == null) {
                return "null";
            }

            if (value.getClass().isArray()) {
                StringBuilder sb = new StringBuilder();
                sb.append("[");
                int length = Array.getLength(value);
                for (int i = 0; i < length; i++) {
                    Object element = Array.get(value, i);
                    sb.append(valueToString(fieldName, element));
                    if (i < length - 1) {
                        sb.append(", ");
                    }
                }
                sb.append("]");
                return sb.toString();
            }
            if (value.getClass() == RGBA.class) {
                RGBA rgb = (RGBA) value;
                return rgb.toHexARGB();
            }
            if (value.getClass() == RGB.class) {
                RGB rgb = (RGB) value;
                return rgb.toHexRGB();
            }

            if (fieldName.equals("angle")) {
                return "" + EcmaNumberToString.stringFor(Math.round(Math.toDegrees((double) value) * 100) / 100.0);
            }
            if (value.getClass() == Double.class) {
                return EcmaNumberToString.stringFor((Double) value);
            }
            if (value.getClass() == Float.class) {
                return EcmaNumberToString.stringFor((Float) value);
            }
            return "" + value;
        }

        @Override
        public Object getValueAt(Object node, int column) {
            DefaultMutableTreeNode n = (DefaultMutableTreeNode) node;
            Object o = n.getUserObject();
            switch (column) {
                case 0:
                    return node;
                case 1:
                    if (o instanceof FilterField) {
                        FilterField filterField = (FilterField) o;
                        return valueToString(filterField.field.getName(), filterField.getValue());
                    } else {
                        return "";
                    }
                default:
                    return null;
            }
        }

        @Override
        public boolean isCellEditable(Object node, int column) {
            if (column == 0) {
                return true;
            }
            return false;
        }

        @Override
        public void setValueAt(Object value, Object node, int column) {

        }

        @Override
        public Object getRoot() {
            return root;
        }

        @Override
        public Object getChild(Object parent, int index) {
            return ((DefaultMutableTreeNode) parent).getChildAt(index);
        }

        @Override
        public int getChildCount(Object parent) {
            return ((DefaultMutableTreeNode) parent).getChildCount();
        }

        @Override
        public boolean isLeaf(Object node) {
            return ((DefaultMutableTreeNode) node).isLeaf();
        }

        @Override
        public void valueForPathChanged(TreePath path, Object newValue) {

        }

        @Override
        public int getIndexOfChild(Object parent, Object child) {
            return ((DefaultMutableTreeNode) parent).getIndex((DefaultMutableTreeNode) child);
        }

        @Override
        public void addTreeModelListener(TreeModelListener l) {
        }

        @Override
        public void removeTreeModelListener(TreeModelListener l) {
        }

    }
}
