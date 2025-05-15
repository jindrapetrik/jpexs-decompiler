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

import com.jpexs.decompiler.flash.easygui.properties.ConvolutionMatrixEditor;
import com.jpexs.decompiler.flash.easygui.properties.GradientEditor;
import com.jpexs.decompiler.flash.easygui.properties.PropertyEditor;
import com.jpexs.decompiler.flash.ecma.EcmaNumberToString;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.gui.generictageditors.BooleanEditor;
import com.jpexs.decompiler.flash.gui.generictageditors.ChangeListener;
import com.jpexs.decompiler.flash.gui.generictageditors.ColorEditor;
import com.jpexs.decompiler.flash.gui.generictageditors.FloatEditor;
import com.jpexs.decompiler.flash.gui.generictageditors.NumberEditor;
import com.jpexs.decompiler.flash.gui.generictageditors.ValueNormalizer;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.annotations.Reserved;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.filters.COLORMATRIXFILTER;
import com.jpexs.decompiler.flash.types.filters.CONVOLUTIONFILTER;
import com.jpexs.decompiler.flash.types.filters.ColorMatrixConvertor;
import com.jpexs.decompiler.flash.types.filters.FILTER;
import de.javagl.treetable.JTreeTable;
import de.javagl.treetable.TreeTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DropMode;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.plaf.basic.BasicTableUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author JPEXS
 */
public class FiltersTreeTable extends JTreeTable {

    private List<ActionListener> filterChangedListeners = new ArrayList<>();
    private boolean linkEnabled = true;
        
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

        /*addMouseListener(new MouseAdapter() {
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
        });*/
        getTableHeader().setReorderingAllowed(false);
        /*getTree().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isEditing()) {
                    getCellEditor().stopCellEditing();
                }
            }            
        });*/           
        setDragEnabled(true);
        setTransferHandler(new TreeTransferHandler());
        setDropMode(DropMode.INSERT_ROWS);
    }

    private static class TreeTransferHandler extends TransferHandler {

        private DataFlavor nodesFlavor;
        private DefaultMutableTreeNode[] nodesToRemove;

        public TreeTransferHandler() {
            try {
                String mimeType = DataFlavor.javaJVMLocalObjectMimeType
                        + ";class=\"" + DefaultMutableTreeNode[].class.getName() + "\"";
                nodesFlavor = new DataFlavor(mimeType);
            } catch (ClassNotFoundException e) {
                //ignore
            }
        }

        @Override
        public boolean canImport(TransferHandler.TransferSupport support) {
            return support.isDrop() && support.isDataFlavorSupported(nodesFlavor);
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            //JTree tree = (JTree) c;
            JTree tree = ((FiltersTreeTable) c).getTree();
            TreePath[] paths = tree.getSelectionPaths();
            if (paths == null) {
                return null;
            }

            DefaultMutableTreeNode[] nodes = new DefaultMutableTreeNode[paths.length];
            for (int i = 0; i < paths.length; i++) {
                nodes[i] = (DefaultMutableTreeNode) paths[i].getLastPathComponent();
            }
            nodesToRemove = nodes;
            return new NodesTransferable(nodes);
        }

        @Override
        public int getSourceActions(JComponent c) {
            return MOVE;
        }

        @Override
        public boolean importData(TransferHandler.TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }
            try {
                JTable.DropLocation dl = (JTable.DropLocation) support.getDropLocation();
                int rowNum = dl.getRow();
                FiltersTreeTable filterTable = (FiltersTreeTable) support.getComponent();

                TreePath path = filterTable.getTree().getPathForRow(rowNum);
                DefaultMutableTreeNode rowNode = rowNum == -1 || path == null ? null : (DefaultMutableTreeNode) path.getLastPathComponent();
                int childIndex = -1;
                if (rowNum > -1) {
                    if (rowNode != null) {
                        childIndex = rowNode.getParent().getIndex(rowNode);
                    }
                }

                FiltersTreeTableModel model = (FiltersTreeTableModel) filterTable.getTree().getModel();
                DefaultMutableTreeNode parent = rowNode == null ? null : (DefaultMutableTreeNode) rowNode.getParent();
                if (parent != null && parent != model.getRoot()) {
                    return false;
                }
                parent = (DefaultMutableTreeNode) filterTable.getTree().getModel().getRoot();

                DefaultMutableTreeNode[] nodes = (DefaultMutableTreeNode[]) support.getTransferable().getTransferData(nodesFlavor);

                for (DefaultMutableTreeNode node : nodes) {
                    if (!(node.getUserObject() instanceof FilterName)) {
                        return false;
                    }
                }

                for (DefaultMutableTreeNode node : nodes) {
                    boolean expanded = filterTable.getTree().isExpanded(new TreePath(new Object[]{parent, node}));
                    FILTER filter = ((FilterName) node.getUserObject()).filter;
                    int newChildIndex = (childIndex == -1) ? parent.getChildCount() : childIndex++;
                    model.addFilter(newChildIndex, filter);
                    if (expanded) {
                        filterTable.getTree().expandPath(new TreePath(new Object[]{parent, parent.getChildAt(newChildIndex)}));
                    }
                }
                return true;
            } catch (UnsupportedFlavorException | IOException e) {
                //ignore
            }

            return false;
        }

        @Override
        protected void exportDone(JComponent source, Transferable data, int action) {
            if (action == MOVE) {
                FiltersTreeTable filtersTable = (FiltersTreeTable) source;
                FiltersTreeTableModel model = (FiltersTreeTableModel) filtersTable.getTree().getModel();
                for (DefaultMutableTreeNode node : nodesToRemove) {
                    model.removeFilter(node.getParent().getIndex(node));
                }
                filtersTable.fireFilterChanged();
            }
        }

        public class NodesTransferable implements Transferable {

            DefaultMutableTreeNode[] nodes;

            public NodesTransferable(DefaultMutableTreeNode[] nodes) {
                this.nodes = nodes;
            }

            @Override
            public Object getTransferData(DataFlavor flavor) {
                if (!isDataFlavorSupported(flavor)) {
                    return null;
                }
                return nodes;
            }

            @Override
            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[]{nodesFlavor};
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return nodesFlavor.equals(flavor);
            }
        }
    }

    public void addFilterChangedListener(ActionListener l) {
        filterChangedListeners.add(l);
    }

    public void removeFilterChangedListener(ActionListener l) {
        filterChangedListeners.remove(l);
    }

    public void fireFilterChanged() {
        List<ActionListener> listeners2 = new ArrayList<>(filterChangedListeners);
        for (ActionListener l : listeners2) {
            l.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "filterChanged"));
        }
    }

    public List<FILTER> getFilters() {
        TreeModel model = getTree().getModel();
        if (model instanceof FiltersTreeTableModel) {
            FiltersTreeTableModel treeTableModel = (FiltersTreeTableModel) model;
            return treeTableModel.filters;
        }
        return new ArrayList<>();
    }

    public void addFilter(FILTER filter) {
        TreeModel model = getTree().getModel();
        if (model instanceof FiltersTreeTableModel) {
            FiltersTreeTableModel treeTableModel = (FiltersTreeTableModel) model;
            treeTableModel.addFilter(filter);
            DefaultMutableTreeNode root = ((DefaultMutableTreeNode) treeTableModel.getRoot());
            getTree().expandPath(new TreePath(new Object[]{root, root.getChildAt(root.getChildCount() - 1)}));
            updateColumns();
            fireFilterChanged();
        }
    }

    public void clearFilters() {
        TreeModel model = getTree().getModel();
        if (model instanceof FiltersTreeTableModel) {
            setTreeTableModel(new FiltersTreeTableModel(new ArrayList<>()));
            fireFilterChanged();
        }
    }

    public boolean isFilterSelected() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) getTree().getLastSelectedPathComponent();
        if (node == null) {
            return false;
        }
        return node.getUserObject() instanceof FilterName;
    }
    
    public FILTER getSelectedFilter() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) getTree().getLastSelectedPathComponent();
        if (node == null) {
            return null;
        }
        if (!(node.getUserObject() instanceof FilterName)) {
            return null;
        }
        
        FilterName filterName = (FilterName) node.getUserObject();
        return filterName.filter;                
    }
    
    public void removeSelectedFilter() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) getTree().getLastSelectedPathComponent();
        if (node == null) {
            return;
        }
        Object object = node.getUserObject();
        if (object instanceof FilterName) {
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) getTree().getModel().getRoot();
            int filterIndex = root.getIndex(node);
            FiltersTreeTableModel model = (FiltersTreeTableModel) getTree().getModel();
            model.removeFilter(filterIndex);

            //Select previous filter
            filterIndex--;
            if (filterIndex < 0) {
                filterIndex = 0;
            }
            fireFilterChanged();
            if (filterIndex < root.getChildCount()) {
                Timer timer = new Timer();
                final int fFilterIndex = filterIndex;
                //Add some delay, otherwise it won't work. WTF?
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (root.getChildCount() > fFilterIndex) {
                            getTree().setSelectionPath(new TreePath(new Object[]{root, root.getChildAt(fFilterIndex)}));
                        }
                    }
                }, 50);
            }
        }
    }

    private void updateColumns() {
        getColumnModel().getColumn(0).setMinWidth(200);
        getColumnModel().getColumn(0).setWidth(200);
        getColumnModel().getColumn(0).setPreferredWidth(200);
        getColumnModel().getColumn(1).setPreferredWidth(Integer.MAX_VALUE);

        getColumnModel().getColumn(1).setCellEditor(new FiltersValueCellEditor(this));
        getColumnModel().getColumn(1).setCellRenderer(new FiltersTableCellRenderer());
    }

    public void setFilters(List<FILTER> filters) {
        if (Objects.equals(getFilters(), filters)) {
            return;
        }
        setTreeTableModel(new FiltersTreeTableModel(filters));

        TreeModel ttm = getTree().getModel();
        Object root = ttm.getRoot();
        int childCount = ttm.getChildCount(root);
        for (int i = 0; i < childCount; i++) {
            getTree().expandPath(new TreePath(new Object[]{root, ttm.getChild(root, i)}));
        }
        updateColumns();
    }

    private static class FiltersValueCellEditor implements TableCellEditor {

        private Object value;
        private PropertyEditor editor;
        private List<CellEditorListener> listeners = new ArrayList<>();
        private final FiltersTreeTable filtersTable;

        public FiltersValueCellEditor(FiltersTreeTable filtersTable) {
            this.filtersTable = filtersTable;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            if (value == null) {
                return null;
            }

            if (!(value instanceof FilterValue)) {
                return null;
            }

            FilterValue filterValue = (FilterValue) value;
            Object realValue = filterValue.getValue();
            this.value = value;

            FilterField filterField = filterValue.filterField;

            editor = null;
            if (filterField.special > 0) {
                editor = new NumberEditor(filterField.toString(), filterField.filter, filterField.field, -1, int.class, new SWFType() {
                    @Override
                    public BasicType value() {
                        return BasicType.SI16;
                    }

                    @Override
                    public BasicType alternateValue() {
                        return null;
                    }

                    @Override
                    public String alternateCondition() {
                        return "";
                    }

                    @Override
                    public int count() {
                        return 0;
                    }

                    @Override
                    public String countField() {
                        return "";
                    }

                    @Override
                    public int countAdd() {
                        return 0;
                    }

                    @Override
                    public boolean canAdd() {
                        return false;
                    }

                    @Override
                    public Class<? extends Annotation> annotationType() {
                        return SWFType.class;
                    }

                }) {
                    @Override
                    protected void saveValue(Object newValue) {
                        float[] matrix = (float[]) super.loadValue();
                        ColorMatrixConvertor convertor = new ColorMatrixConvertor(matrix);
                        switch (filterField.special) {
                            case FilterField.SPECIAL_BRIGHTNESS:
                                convertor.setBrightness((int) newValue);
                                break;
                            case FilterField.SPECIAL_CONTRAST:
                                convertor.setContrast((int) newValue);
                                break;
                            case FilterField.SPECIAL_SATURATION:
                                convertor.setSaturation((int) newValue);
                                break;
                            case FilterField.SPECIAL_HUE:
                                convertor.setHue((int) newValue);
                                break;
                        }
                        matrix = convertor.getMatrix();
                        super.saveValue(matrix);
                    }

                    @Override
                    protected Object loadValue() {
                        float[] matrix = (float[]) super.loadValue();
                        ColorMatrixConvertor convertor = new ColorMatrixConvertor(matrix);
                        switch (filterField.special) {
                            case FilterField.SPECIAL_BRIGHTNESS:
                                return convertor.getBrightness();
                            case FilterField.SPECIAL_CONTRAST:
                                return convertor.getContrast();
                            case FilterField.SPECIAL_SATURATION:
                                return convertor.getSaturation();
                            case FilterField.SPECIAL_HUE:
                                return convertor.getHue();
                        }
                        return 0;
                    }
                };
            } else if ("matrix".equals(filterField.field.getName()) && filterField.filter instanceof CONVOLUTIONFILTER) {
                editor = new ConvolutionMatrixEditor((CONVOLUTIONFILTER) filterField.filter);
            } else if ("gradientColors".equals(filterField.field.getName())) {
                editor = new GradientEditor(filterField.filter);
            } else if (realValue.getClass() == Boolean.class) {
                editor = new BooleanEditor(filterField.toString(), filterField.filter, filterField.field, -1, Boolean.class);
                editor.addChangeListener(new ChangeListener() {
                    @Override
                    public void change(PropertyEditor editor) {
                        stopCellEditing();
                    }
                    
                    @Override
                    public void linkChanged(boolean newValue) {
                    }                                        
                });
            } else if (realValue.getClass() == Double.class || realValue.getClass() == Float.class) {
                editor = new FloatEditor(filterField.toString(), filterField.filter, filterField.field, -1, realValue.getClass());
                if ("angle".equals(filterField.field.getName())) {
                    ((FloatEditor) editor).setValueNormalizer(new ValueNormalizer() {
                        @Override
                        public Object toFieldValue(Object viewValue) {
                            return Math.toRadians((double) viewValue);
                        }

                        @Override
                        public Object toViewValue(Object fieldValue) {
                            return Math.round(Math.toDegrees((double) fieldValue) * 100) / 100.0;
                        }
                    });
                }
                if ("blurX".equals(filterField.field.getName())) {
                    try {
                        ((FloatEditor) editor).setLinkedField(filterField.filter.getClass().getField("blurY"));
                        ((FloatEditor) editor).setLinkEnabled(filtersTable.linkEnabled);
                    } catch (NoSuchFieldException | SecurityException ex) {
                        //ignore
                    }
                }
                if ("blurY".equals(filterField.field.getName())) {
                    try {
                        ((FloatEditor) editor).setLinkedField(filterField.filter.getClass().getField("blurX"));
                        ((FloatEditor) editor).setLinkEnabled(filtersTable.linkEnabled);
                    } catch (NoSuchFieldException | SecurityException ex) {
                        //ignore
                    }
                }
            } else if (realValue.getClass() == int.class || realValue.getClass() == Integer.class) {
                editor = new NumberEditor(filterField.toString(), filterField.filter, filterField.field, -1, realValue.getClass(), filterField.field.getAnnotation(SWFType.class));
            } else if (realValue.getClass() == RGBA.class) {
                editor = new ColorEditor(filterField.toString(), filterField.filter, filterField.field, -1, RGBA.class);
            }

            if (editor != null) {

                editor.addChangeListener(new ChangeListener() {
                    @Override
                    public void change(PropertyEditor editor) {
                    }

                    @Override
                    public void linkChanged(boolean newValue) {
                        filtersTable.linkEnabled = newValue;
                        stopCellEditing();
                        filtersTable.repaint();
                    }                    
                });
                if (table instanceof JTreeTable) {
                    JTreeTable treeTable = (JTreeTable) table;
                    if (treeTable.isRowSelected(row)) {
                        ((JComponent) editor).setForeground(new Color(UIManager.getColor("Table.selectionForeground").getRGB(), true));
                        ((JComponent) editor).setBackground(new Color(UIManager.getColor("Table.selectionBackground").getRGB(), true));
                        ((JComponent) editor).setOpaque(true);
                    } else {
                        ((JComponent) editor).setOpaque(false);
                    }
                }
            }

            return (Component) editor;
        }

        @Override
        public Object getCellEditorValue() {
            return value;
        }

        @Override
        public boolean isCellEditable(EventObject anEvent) {
            return true;
        }

        @Override
        public boolean shouldSelectCell(EventObject anEvent) {
            if (value == null) {
                return true;
            }

            if (!(value instanceof FilterValue)) {
                return false;
            }

            FilterValue filterValue = (FilterValue) value;

            Object realValue = filterValue.getValue();

            if (realValue.getClass() == Double.class || realValue.getClass() == Float.class) {
                return true;
            }

            if (filterValue.filterField.special > 0) {
                return true;
            }
            if (realValue.getClass() == Boolean.class) {
                return false;
            }
            return false;
        }

        @Override
        public boolean stopCellEditing() {
            editor.save();
            filtersTable.fireFilterChanged();
            List<CellEditorListener> listeners2 = new ArrayList<>(listeners);
            for (CellEditorListener l : listeners2) {
                l.editingStopped(new ChangeEvent(editor));
            }
            filtersTable.repaint();
            return true;
        }

        @Override
        public void cancelCellEditing() {
            List<CellEditorListener> listeners2 = new ArrayList<>(listeners);
            for (CellEditorListener l : listeners2) {
                l.editingCanceled(new ChangeEvent(editor));
            }
            editor.reset();
        }

        @Override
        public void addCellEditorListener(CellEditorListener l) {
            listeners.add(l);
        }

        @Override
        public void removeCellEditorListener(CellEditorListener l) {
            listeners.remove(l);
        }

    }

    private static class FiltersTableCellRenderer extends DefaultTableCellRenderer {

        JLabel label = new JLabel();
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            label.setText(value.toString());

            if (table instanceof JTreeTable) {
                JTreeTable treeTable = (JTreeTable) table;
                isSelected = treeTable.isRowSelected(row);
            }

            JComponent component = label;
            if (value instanceof FilterValue) {
                FilterValue filterValue = (FilterValue) value;
                String units = "";
                switch (filterValue.filterField.field.getName()) {
                    case "angle":
                        units = " \u00B0"; //degrees
                        break;
                    case "blurX":
                    case "blurY":
                    case "distance":
                        units = " px";
                }

                Object fieldValue = filterValue.getValue();

                if (filterValue.filterField.special > 0) {
                    float[] matrix = (float[]) fieldValue;
                    ColorMatrixConvertor convertor = new ColorMatrixConvertor(matrix);
                    switch (filterValue.filterField.special) {
                        case FilterField.SPECIAL_BRIGHTNESS:
                            label.setText("" + convertor.getBrightness());
                            break;
                        case FilterField.SPECIAL_CONTRAST:
                            label.setText("" + convertor.getContrast());
                            break;
                        case FilterField.SPECIAL_SATURATION:
                            label.setText("" + convertor.getSaturation());
                            break;
                        case FilterField.SPECIAL_HUE:
                            label.setText("" + convertor.getHue());
                            break;
                    }
                } else {
                    label.setText(value.toString() + units);
                }
                if (fieldValue != null) {                    
                    if ("matrix".equals(filterValue.filterField.field.getName()) && filterValue.filterField.filter instanceof CONVOLUTIONFILTER) {
                        component = new ConvolutionMatrixEditor((CONVOLUTIONFILTER) filterValue.filterField.filter);
                    }
                    
                    if ("gradientColors".equals(filterValue.filterField.field.getName())) {
                        component = new GradientEditor(filterValue.filterField.filter);
                    }
                    if (fieldValue.getClass() == Boolean.class) {
                        JPanel panel = new JPanel(new BorderLayout());
                        JCheckBox checkBox = new JCheckBox();
                        //checkBox.setHorizontalAlignment(CENTER);
                        checkBox.setSelected((Boolean) fieldValue);
                        checkBox.setOpaque(false);
                        panel.add(checkBox, BorderLayout.CENTER);
                        panel.setOpaque(false);
                        component = panel;
                    }
                    if (fieldValue.getClass() == RGBA.class) {                       
                        component = new ColorEditor(filterValue.filterField.toString(), filterValue.filterField.filter, filterValue.filterField.field, -1, RGBA.class);
                        component.setToolTipText(AppStrings.translate("button.selectcolor.hint"));
                    }
                    if ("blurX".equals(filterValue.filterField.field.getName())
                            || "blurY".equals(filterValue.filterField.field.getName())) {
                        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                        JLabel blurLabel = new JLabel(label.getText());
                        panel.add(blurLabel);
                        blurLabel.setPreferredSize(new Dimension(50, blurLabel.getPreferredSize().height));
                        blurLabel.setMaximumSize(blurLabel.getPreferredSize());
                        JLabel linkLabel = new JLabel(View.getIcon(((FiltersTreeTable) table).linkEnabled ? "link16" : "linkbreak16"));
                        panel.add(linkLabel);
                        component = panel;
                    }
                }
            }

            if (isSelected) {
                component.setForeground(new Color(UIManager.getColor("Table.selectionForeground").getRGB(), true));
                component.setBackground(new Color(UIManager.getColor("Table.selectionBackground").getRGB(), true));
                component.setOpaque(true);
            } else {
                component.setOpaque(false);
            }
            return component;
        }
    }

    private static class FiltersTreeCellRenderer extends DefaultTreeCellRenderer {

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            if (value instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                Object object = node.getUserObject();
                label.setText(object.toString());
                if (object instanceof FilterName) {
                    FilterName filterName = (FilterName) object;
                    if (!filterName.filter.enabled) {
                        label.setIcon(View.getIcon("cross16"));                        
                    }
                }
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

    private static class FilterField {

        private final FILTER filter;
        private final Field field;
        private final int special;

        public static final int SPECIAL_BRIGHTNESS = 1;
        public static final int SPECIAL_CONTRAST = 2;
        public static final int SPECIAL_SATURATION = 3;
        public static final int SPECIAL_HUE = 4;

        public FilterField(FILTER filter, Field property) {
            this(filter, property, 0);
        }

        public FilterField(FILTER filter, Field property, int special) {
            this.filter = filter;
            this.field = property;
            this.special = special;
        }

        public int getSpecial() {
            return special;
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

        public String getIdentifier() {            
            switch (special) {
                case SPECIAL_BRIGHTNESS:
                    return "brightness";
                case SPECIAL_CONTRAST:
                    return "contrast";
                case SPECIAL_SATURATION:
                    return "saturation";
                case SPECIAL_HUE:
                    return "hue";
            }
            if ("gradientColors".equals(field.getName())) {
                return "gradient";
            }
            return field.getName();
        }
        
        @Override
        public String toString() {
            return EasyStrings.translate("property.instance.filters." + getIdentifier());
        }

    }

    private static class FilterValue {

        private final FilterField filterField;

        public FilterValue(FilterField filterField) {
            this.filterField = filterField;
        }

        public Object getValue() {
            return filterField.getValue();
        }

        @Override
        public String toString() {
            return valueToString(filterField.getField().getName(), filterField.getValue());
        }
    }

    private static String valueToString(String fieldName, Object value) {
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
        if (value.getClass() == Double.class
                || value.getClass() == Float.class
                || value.getClass() == float.class
                || value.getClass() == double.class) {
            return EcmaScript.toString(value);
        }
        return "" + value;
    }

    private static class FilterName {

        private final FILTER filter;

        public FilterName(FILTER filter) {
            this.filter = filter;
        }

        @Override
        public String toString() {
            String filterName = filter.getClass().getSimpleName();
            filterName = filterName.substring(0, filterName.length() - "FILTER".length());
            return EasyStrings.translate("filter." + filterName.toLowerCase());
        }
    }

    private static class FiltersTreeTableModel implements TreeTableModel {

        private final DefaultMutableTreeNode root;

        private List<FILTER> filters;

        private List<TreeModelListener> listeners = new ArrayList<>();

        public FiltersTreeTableModel(List<FILTER> filters) {
            root = new DefaultMutableTreeNode("root");

            if (filters == null) {
                DefaultMutableTreeNode indeterminate = new DefaultMutableTreeNode(EasyStrings.translate("property.instance.filters.indeterminate"));
                root.add(indeterminate);
                this.filters = null;
                return;
            }

            this.filters = new ArrayList<>();

            for (FILTER filter : filters) {
                addFilter(filter);
            }
        }

        public void addFilter(FILTER filter) {
            addFilter(filters.size(), filter);
        }

        public void addFilter(int index, FILTER filter) {
            if (this.filters == null) { //indeterminate
                DefaultMutableTreeNode indeterminate = (DefaultMutableTreeNode) root.getChildAt(0);
                root.remove(0);
                this.filters = new ArrayList<>();
                for (TreeModelListener l : listeners) {
                    l.treeNodesRemoved(new TreeModelEvent(this, new Object[]{root}, new int[]{0}, new Object[]{indeterminate}));
                }
            }

            DefaultMutableTreeNode filterNode = new DefaultMutableTreeNode(new FilterName(filter));
            root.insert(filterNode, index);
            Field[] fields = filter.getClass().getFields();
            LinkedDefaultMutableTreeNode lastLinkedNode = null;
            for (Field field : fields) {
                if ("id".equals(field.getName())) {
                    continue;
                }
                if ("enabled".equals(field.getName())) {
                    continue;
                }
                if ("gradientRatio".equals(field.getName())) {
                    continue;
                }
                if (filter instanceof CONVOLUTIONFILTER) {
                    if ("matrixX".equals(field.getName())
                            || "matrixY".equals(field.getName())) {
                        continue;
                    }
                }
                if (filter instanceof COLORMATRIXFILTER) {
                    if ("matrix".equals(field.getName())) {

                        DefaultMutableTreeNode fieldNode;
                        FilterField filterField;

                        filterField = new FilterField(filter, field, FilterField.SPECIAL_BRIGHTNESS);
                        fieldNode = new DefaultMutableTreeNode(filterField);
                        filterNode.add(fieldNode);

                        filterField = new FilterField(filter, field, FilterField.SPECIAL_CONTRAST);
                        fieldNode = new DefaultMutableTreeNode(filterField);
                        filterNode.add(fieldNode);

                        filterField = new FilterField(filter, field, FilterField.SPECIAL_SATURATION);
                        fieldNode = new DefaultMutableTreeNode(filterField);
                        filterNode.add(fieldNode);

                        filterField = new FilterField(filter, field, FilterField.SPECIAL_HUE);
                        fieldNode = new DefaultMutableTreeNode(filterField);
                        filterNode.add(fieldNode);

                        continue;
                    }
                }
                Reserved reserved = field.getAnnotation(Reserved.class);
                if (reserved != null) {
                    continue;
                }
                FilterField filterField = new FilterField(filter, field);
                DefaultMutableTreeNode fieldNode;
                if ("blurX".equals(field.getName())) {
                    LinkedDefaultMutableTreeNode linkedNode = new LinkedDefaultMutableTreeNode(filterField);
                    fieldNode = linkedNode;
                    lastLinkedNode = linkedNode;                    
                } else if ("blurY".equals(field.getName())) {
                    LinkedDefaultMutableTreeNode linkedNode = new LinkedDefaultMutableTreeNode(filterField);                    
                    fieldNode = linkedNode;
                    linkedNode.setLinkedNode(lastLinkedNode);
                    lastLinkedNode.setLinkedNode(linkedNode);
                } else {
                    fieldNode = new DefaultMutableTreeNode(filterField);
                }
                filterNode.add(fieldNode);
            }
            this.filters.add(index, filter);

            for (TreeModelListener l : listeners) {
                l.treeNodesInserted(new TreeModelEvent(this, new Object[]{root}, new int[]{index}, new Object[]{filterNode}));
            }
        }

        public void removeFilter(int index) {
            filters.remove(index);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) root.getChildAt(index);
            root.remove(node);
            for (TreeModelListener l : listeners) {
                l.treeNodesRemoved(new TreeModelEvent(this, new Object[]{root}, new int[]{index}, new Object[]{node}));
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
                    return EasyStrings.translate("property.instance.filters.header.property");
                case 1:
                    return EasyStrings.translate("property.instance.filters.header.value");
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
                        return new FilterValue(filterField);
                    } else if (o instanceof FilterName) {
                        FilterName filterName = (FilterName) o;
                        if (filterName.filter instanceof CONVOLUTIONFILTER) {
                            ConvolutionPreset preset = ConvolutionPreset.getPresetOfFilter((CONVOLUTIONFILTER) filterName.filter);
                            if (preset == null) {
                                return "";
                            } else {
                                return preset;
                            }
                        } else {
                            return "";
                        }
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
            DefaultMutableTreeNode n = (DefaultMutableTreeNode) node;
            if (column == 1 && n.getUserObject() instanceof FilterField) {
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
            listeners.add(l);
        }

        @Override
        public void removeTreeModelListener(TreeModelListener l) {
            listeners.remove(l);
        }
    }
    
    private static class LinkedDefaultMutableTreeNode extends DefaultMutableTreeNode {
        private LinkedDefaultMutableTreeNode linkedNode;

        public LinkedDefaultMutableTreeNode(Object userObject) {
            super(userObject);
        }

        
        
        public void setLinkedNode(LinkedDefaultMutableTreeNode linkedNode) {
            this.linkedNode = linkedNode;
        }

        public LinkedDefaultMutableTreeNode getLinkedNode() {
            return linkedNode;
        }                
    }
}
