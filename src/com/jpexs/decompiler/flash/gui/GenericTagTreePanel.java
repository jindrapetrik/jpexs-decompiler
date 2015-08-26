/*
 *  Copyright (C) 2010-2015 JPEXS
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
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.gui.generictageditors.BinaryDataEditor;
import com.jpexs.decompiler.flash.gui.generictageditors.BooleanEditor;
import com.jpexs.decompiler.flash.gui.generictageditors.ColorEditor;
import com.jpexs.decompiler.flash.gui.generictageditors.GenericTagEditor;
import com.jpexs.decompiler.flash.gui.generictageditors.NumberEditor;
import com.jpexs.decompiler.flash.gui.generictageditors.StringEditor;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.flash.types.ARGB;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.annotations.Conditional;
import com.jpexs.decompiler.flash.types.annotations.HideInRawEdit;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.flash.types.annotations.Multiline;
import com.jpexs.decompiler.flash.types.annotations.SWFArray;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.annotations.Table;
import com.jpexs.decompiler.flash.types.annotations.parser.AnnotationParseException;
import com.jpexs.decompiler.flash.types.annotations.parser.ConditionEvaluator;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.ConcreteClasses;
import com.jpexs.helpers.ReflectionTools;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.plaf.basic.BasicLabelUI;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author JPEXS
 */
public class GenericTagTreePanel extends GenericTagPanel {

    private static final Logger logger = Logger.getLogger(GenericTagTreePanel.class.getName());

    private JTree tree;

    private Tag editedTag;

    private static final Map<Class, List<Field>> fieldCache = new HashMap<>();

    private static final int FIELD_INDEX = 0;

    private class MyTree extends JTree {

        public MyTree() {
            setBackground(Color.white);
            setUI(new BasicTreeUI() {

                @Override
                public void paint(Graphics g, JComponent c) {
                    setHashColor(Color.gray);
                    super.paint(g, c);
                }

            });
            setCellRenderer(new MyTreeCellRenderer());
            setCellEditor(new MyTreeCellEditor(this));
            setInvokesStopCellEditing(true);

        }
    }

    private class MyTreeCellEditor extends AbstractCellEditor implements TreeCellEditor {

        private List<GenericTagEditor> editors = null;

        private final JTree tree;

        private FieldNode fnode;

        public MyTreeCellEditor(JTree tree) {
            this.tree = tree;
        }

        @Override
        public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
            if (value instanceof FieldNode) {
                fnode = (FieldNode) value;
                JPanel panSum = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                panSum.setOpaque(false);
                for (int i = 0; i < fnode.fieldSet.size(); i++) {
                    Field field = fnode.fieldSet.get(i);//fnode.fieldSet.get(FIELD_INDEX);
                    int index = fnode.index;
                    Object obj = fnode.obj;
                    Class<?> type;
                    boolean isByteArray = field.getType().equals(byte[].class);
                    try {
                        type = isByteArray ? byte[].class : ReflectionTools.getValue(obj, field, index).getClass();
                    } catch (IllegalArgumentException | IllegalAccessException ex) {
                        logger.log(Level.SEVERE, "Fixing characters order failed, recursion detected.");
                        return null;
                    }
                    GenericTagEditor editor = null;
                    SWFType swfType = field.getAnnotation(SWFType.class);
                    Multiline multiline = field.getAnnotation(Multiline.class);
                    if (type.equals(int.class) || type.equals(Integer.class)
                            || type.equals(short.class) || type.equals(Short.class)
                            || type.equals(long.class) || type.equals(Long.class)
                            || type.equals(double.class) || type.equals(Double.class)
                            || type.equals(float.class) || type.equals(Float.class)) {
                        editor = new NumberEditor(field.getName(), obj, field, index, type, swfType);
                    } else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
                        editor = new BooleanEditor(field.getName(), obj, field, index, type);
                    } else if (type.equals(String.class)) {
                        editor = new StringEditor(field.getName(), obj, field, index, type, multiline != null);
                    } else if (type.equals(RGB.class) || type.equals(RGBA.class) || type.equals(ARGB.class)) {
                        editor = new ColorEditor(field.getName(), obj, field, index, type);
                    } else if (type.equals(byte[].class) || type.equals(ByteArrayRange.class)) {
                        editor = new BinaryDataEditor(mainPanel, field.getName(), obj, field, index, type);
                    }
                    if (editor != null) {
                        if (editors == null) {
                            editors = new ArrayList<>();
                        }
                        editors.add(editor);
                    }
                    JPanel pan = new JPanel();
                    FlowLayout fl = new FlowLayout(FlowLayout.LEFT, 0, 0);
                    fl.setAlignOnBaseline(true);
                    pan.setLayout(fl);
                    JLabel nameLabel = new JLabel(fnode.getNameType(i) + " = ") {

                        @Override
                        public BaselineResizeBehavior getBaselineResizeBehavior() {
                            return Component.BaselineResizeBehavior.CONSTANT_ASCENT;
                        }

                        @Override
                        public int getBaseline(int width, int height) {
                            return 0;
                        }

                    };
                    pan.setBackground(Color.white);
                    nameLabel.setAlignmentY(TOP_ALIGNMENT);
                    pan.add(nameLabel);

                    JComponent editorComponent = (JComponent) editor;
                    if (editorComponent != null) {
                        nameLabel.setSize(nameLabel.getWidth(), editorComponent.getHeight());
                        editorComponent.setAlignmentY(TOP_ALIGNMENT);
                        pan.add(editorComponent);
                        pan.setPreferredSize(new Dimension((int) nameLabel.getPreferredSize().getWidth() + 5 + (int) editorComponent.getPreferredSize().getWidth(), (int) editorComponent.getPreferredSize().getHeight()));
                    } else {
                        pan.setPreferredSize(new Dimension((int) nameLabel.getPreferredSize().getWidth(), (int) nameLabel.getPreferredSize().getHeight()));
                    }
                    panSum.add(pan);
                }
                return panSum;
            }
            return null;

        }

        @Override
        public Object getCellEditorValue() {
            List<Object> ret = new ArrayList<>();
            if (editors != null) {
                for (GenericTagEditor editor : editors) {
                    ret.add(editor.getChangedValue());
                }
            }
            return ret;
        }

        @Override
        public boolean isCellEditable(EventObject e) {
            if (!(e instanceof MouseEvent)) {
                return false;
            }

            MouseEvent me = (MouseEvent) e;
            TreePath path = tree.getPathForLocation(me.getX(), me.getY());

            if (path == null) {
                return false;
            }
            Object obj = path.getLastPathComponent();

            boolean ret = super.isCellEditable(e)
                    && tree.getModel().isLeaf(obj);
            return ret;
        }

        @Override
        public boolean stopCellEditing() {
            super.stopCellEditing();

            /*List<FieldNode> depends = ((MyTreeModel) tree.getModel()).getDependentFields(fnode);
             boolean dep = false;
             if (!depends.isEmpty()) {
             dep = true;
             }     */
            if (editors != null) {
                for (GenericTagEditor editor : editors) {
                    editor.save();
                }
            }

            editors = null;

            TreePath sp = tree.getSelectionPath();
            if (sp != null) {
                ((MyTreeModel) tree.getModel()).vchanged(sp);
            }
            refreshTree();
            return true;
        }
    }

    public GenericTagTreePanel(MainPanel mainPanel) {
        super(mainPanel);
        setLayout(new BorderLayout());
        tree = new MyTree();

        add(new JScrollPane(tree), BorderLayout.CENTER);
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!tree.isEditable()) {
                    return;
                }
                int selRow = tree.getRowForLocation(e.getX(), e.getY());
                TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                if (selRow != -1 && selPath != null) {
                    if (e.getClickCount() == 1) {
                        if (e.getButton() == MouseEvent.BUTTON3) { //right click
                            Object selObject = selPath.getLastPathComponent();
                            if (selObject instanceof FieldNode) {
                                final FieldNode fnode = (FieldNode) selObject;
                                Field field = fnode.fieldSet.get(FIELD_INDEX);
                                if (ReflectionTools.needsIndex(field)) {
                                    SWFArray swfArray = fnode.fieldSet.get(FIELD_INDEX).getAnnotation(SWFArray.class);

                                    String itemStr = "";
                                    if (swfArray != null) {
                                        itemStr = swfArray.value();
                                    }
                                    if (fnode.fieldSet.itemName != null && !fnode.fieldSet.itemName.isEmpty()) {
                                        itemStr = fnode.fieldSet.itemName;
                                    }
                                    if (itemStr.isEmpty()) {
                                        itemStr = AppStrings.translate("generictag.array.item");
                                    }

                                    boolean canAdd = true;
                                    if (!ReflectionTools.canAddToField(fnode.obj, fnode.fieldSet.get(FIELD_INDEX))) {
                                        canAdd = false;
                                    }
                                    JPopupMenu p = new JPopupMenu();
                                    JMenuItem mi;
                                    Class<?> subtype = ReflectionTools.getFieldSubType(fnode.obj, fnode.fieldSet.get(FIELD_INDEX));
                                    if (!canAdd && subtype.isAnnotationPresent(ConcreteClasses.class)) {
                                        Class<?>[] availableClasses = subtype.getAnnotation(ConcreteClasses.class).value();
                                        JMenu mBegin = new JMenu(AppStrings.translate("generictag.array.insertbeginning").replace("%item%", itemStr));
                                        p.add(mBegin);
                                        JMenu mBefore = new JMenu(AppStrings.translate("generictag.array.insertbefore").replace("%item%", itemStr));
                                        p.add(mBefore);
                                        mi = new JMenuItem(AppStrings.translate("generictag.array.remove").replace("%item%", itemStr));
                                        mi.addActionListener((ActionEvent e1) -> {
                                            removeItem(fnode.obj, fnode.fieldSet.get(FIELD_INDEX), fnode.index);
                                        });
                                        p.add(mi);
                                        JMenu mAfter = new JMenu(AppStrings.translate("generictag.array.insertafter").replace("%item%", itemStr));
                                        p.add(mAfter);

                                        JMenu mEnd = new JMenu(AppStrings.translate("generictag.array.insertend").replace("%item%", itemStr));
                                        p.add(mEnd);

                                        for (Class<?> c : availableClasses) {
                                            mi = new JMenuItem(c.getSimpleName());
                                            mi.addActionListener((ActionEvent e1) -> {
                                                addItem(fnode.obj, fnode.fieldSet.get(FIELD_INDEX), 0, c);
                                            });
                                            mBegin.add(mi);

                                            mi = new JMenuItem(c.getSimpleName());
                                            mi.addActionListener((ActionEvent e1) -> {
                                                addItem(fnode.obj, fnode.fieldSet.get(FIELD_INDEX), fnode.index, c);
                                            });
                                            mBefore.add(mi);

                                            mi = new JMenuItem(c.getSimpleName());
                                            mi.addActionListener((ActionEvent e1) -> {
                                                addItem(fnode.obj, fnode.fieldSet.get(FIELD_INDEX), fnode.index + 1, c);
                                            });
                                            mAfter.add(mi);

                                            mi = new JMenuItem(c.getSimpleName());
                                            mi.addActionListener((ActionEvent e1) -> {
                                                addItem(fnode.obj, fnode.fieldSet.get(FIELD_INDEX), ReflectionTools.getFieldSubSize(fnode.obj, fnode.fieldSet.get(FIELD_INDEX)), c);
                                            });
                                            mEnd.add(mi);
                                        }
                                    } else {

                                        mi = new JMenuItem(AppStrings.translate("generictag.array.insertbeginning").replace("%item%", itemStr));
                                        mi.addActionListener(new ActionListener() {

                                            @Override
                                            public void actionPerformed(ActionEvent e) {
                                                addItem(fnode.obj, fnode.fieldSet.get(FIELD_INDEX), 0, null);
                                            }
                                        });
                                        if (!canAdd) {
                                            mi.setEnabled(false);
                                        }
                                        p.add(mi);

                                        if (fnode.index > -1) {
                                            mi = new JMenuItem(AppStrings.translate("generictag.array.insertbefore").replace("%item%", itemStr));
                                            mi.addActionListener(new ActionListener() {

                                                @Override
                                                public void actionPerformed(ActionEvent e) {
                                                    addItem(fnode.obj, fnode.fieldSet.get(FIELD_INDEX), fnode.index, null);
                                                }
                                            });
                                            if (!canAdd) {
                                                mi.setEnabled(false);
                                            }
                                            p.add(mi);

                                            mi = new JMenuItem(AppStrings.translate("generictag.array.remove").replace("%item%", itemStr));
                                            mi.addActionListener(new ActionListener() {

                                                @Override
                                                public void actionPerformed(ActionEvent e) {
                                                    removeItem(fnode.obj, fnode.fieldSet.get(FIELD_INDEX), fnode.index);
                                                }
                                            });
                                            p.add(mi);

                                            mi = new JMenuItem(AppStrings.translate("generictag.array.insertafter").replace("%item%", itemStr));
                                            mi.addActionListener(new ActionListener() {

                                                @Override
                                                public void actionPerformed(ActionEvent e) {
                                                    addItem(fnode.obj, fnode.fieldSet.get(FIELD_INDEX), fnode.index + 1, null);
                                                }
                                            });
                                            if (!canAdd) {
                                                mi.setEnabled(false);
                                            }
                                            p.add(mi);
                                        }

                                        mi = new JMenuItem(AppStrings.translate("generictag.array.insertend").replace("%item%", itemStr));
                                        mi.addActionListener(new ActionListener() {

                                            @Override
                                            public void actionPerformed(ActionEvent e) {
                                                addItem(fnode.obj, fnode.fieldSet.get(FIELD_INDEX), ReflectionTools.getFieldSubSize(fnode.obj, fnode.fieldSet.get(FIELD_INDEX)), null);
                                            }
                                        });
                                        if (!canAdd) {
                                            mi.setEnabled(false);
                                        }
                                        p.add(mi);
                                        //}
                                    }
                                    p.show(tree, e.getX(), e.getY());
                                }
                            }
                        }
                        //} else if (e.getClickCount() == 2) {
                        //    myDoubleClick(selRow, selPath);
                    }
                }
            }
        });
    }

    private Tag tag;

    public class MyTreeCellRenderer extends DefaultTreeCellRenderer {

        public MyTreeCellRenderer() {
            setUI(new BasicLabelUI());
            setOpaque(false);
            setBackgroundNonSelectionColor(Color.white);
        }
    }

    @Override
    public void clear() {

    }

    private static final class TableFieldNodes extends DefaultMutableTreeNode {

        List<FieldNode> subnodes;

        public TableFieldNodes(List<FieldNode> subnodes) {
            this.subnodes = subnodes;
        }
    }

    private static final class FieldNode extends DefaultMutableTreeNode {

        private Object obj;

        private FieldSet fieldSet;

        private int index;

        public FieldNode(Object obj, FieldSet fieldSet, int index) {
            this.obj = obj;
            this.fieldSet = fieldSet;
            this.index = index;

            for (int i = 0; i < fieldSet.size(); i++) {
                if (getValue(i) == null) {
                    try {
                        if (List.class.isAssignableFrom(fieldSet.get(i).getType())) {
                            ReflectionTools.setValue(obj, fieldSet.get(i), new ArrayList<>());
                        } else if (fieldSet.get(i).getType().isArray()) {
                            ReflectionTools.setValue(obj, fieldSet.get(i), Array.newInstance(fieldSet.get(i).getType().getComponentType(), 0));
                        }
                    } catch (IllegalArgumentException | IllegalAccessException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                }
            }
        }

        @Override
        public void setUserObject(Object userObject) {

        }

        /*
         */
        @Override
        public String toString() {
            StringBuilder ret = new StringBuilder();
            if (index > -1) {
                for (int i = 0; i < fieldSet.size(); i++) {
                    if (i > 0) {
                        ret.append(", ");
                    }
                    ret.append(toString(i));
                }
                return ret.toString();
            }

            if (fieldSet.size() == 1) {
                ret.append(toString(0));
            } else {
                ret.append(fieldSet.name);
                SWFArray t = fieldSet.get(0).getAnnotation(SWFArray.class);
                if (t != null) {
                    ret.append(" [").append(t.countField()).append("]");
                } else {
                    ret.append(" []");
                }
            }

            ret.insert(0, "<html>").append("</html>");
            return ret.toString();
        }

        public String toString(int fieldIndex) {
            String valStr = "";
            Field field = fieldSet.get(fieldIndex);
            if (ReflectionTools.needsIndex(field) && (index == -1)) {
                valStr += "";
            } else if (hasEditor(obj, field, index)) {
                Object val = getValue(fieldIndex);
                Color color = null;
                String colorAdd = "";
                if (val instanceof RGB) { //Note: Can be RGBA too
                    color = ((RGB) val).toColor();
                }
                if (val instanceof ARGB) {
                    color = ((ARGB) val).toColor();
                }

                if (color != null) {
                    colorAdd = "<cite style=\"color:rgb(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + ");\">\u25cf</cite> ";
                }

                if (val instanceof byte[]) {
                    valStr += " = " + ((byte[]) val).length + " byte";
                } else if (val instanceof ByteArrayRange) {
                    valStr += " = " + ((ByteArrayRange) val).getLength() + " byte";
                } else {
                    valStr += " = " + colorAdd + val.toString();
                }
            }
            return getNameType(fieldIndex) + valStr;
        }

        public String getType(int fieldIndex) {
            SWFType swfType = fieldSet.get(fieldIndex).getAnnotation(SWFType.class);
            SWFArray swfArray = fieldSet.get(fieldIndex).getAnnotation(SWFArray.class);
            String typeStr = null;
            if ((swfType != null || swfArray != null) && !(ReflectionTools.needsIndex(fieldSet.get(fieldIndex)) && (index > -1))) {
                Class<?> type = fieldSet.get(fieldIndex).getType();
                if (ReflectionTools.needsIndex(fieldSet.get(fieldIndex))) {
                    type = ReflectionTools.getFieldSubType(obj, fieldSet.get(fieldIndex));
                }
                typeStr = swfTypeToString(type, swfType, swfArray);
            }
            return typeStr;
        }

        public String getNameType(int fieldIndex) {
            String typeStr = getType(fieldIndex);
            return getName(fieldIndex) + (typeStr != null ? " : " + typeStr : "");
        }

        public String getName(int fieldIndex) {
            SWFArray swfArray = fieldSet.get(fieldIndex).getAnnotation(SWFArray.class);
            String name = "";
            if (swfArray != null) {
                name = swfArray.value();
            }

            Object val = null;
            try {
                if (index > -1) {
                    val = ReflectionTools.getValue(obj, fieldSet.get(fieldIndex), index);
                }
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                //ignore
            }
            String typeAdd = "";
            if (val != null) {
                typeAdd = " : " + val.getClass().getSimpleName();
            }

            return (index > -1 ? name + "[" + index + "]" + typeAdd : fieldSet.get(fieldIndex).getName());
        }

        public Object getValue(int fieldIndex) {
            try {
                if (ReflectionTools.needsIndex(fieldSet.get(fieldIndex)) && (index == -1)) {
                    return ReflectionTools.getValue(obj, fieldSet.get(fieldIndex));
                }
                Object val = ReflectionTools.getValue(obj, fieldSet.get(fieldIndex), index);
                if (val == null) {
                    try {
                        val = ReflectionTools.newInstanceOf(fieldSet.get(fieldIndex).getType());
                        ReflectionTools.setValue(obj, fieldSet.get(fieldIndex), index, val);
                    } catch (InstantiationException | IllegalAccessException ex) {
                        logger.log(Level.SEVERE, null, ex);
                        return null;
                    }
                }
                return val;
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                return null;
            }
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 11 * hash + Objects.hashCode(this.obj);
            hash = 11 * hash + Objects.hashCode(this.fieldSet.get(FIELD_INDEX));
            hash = 11 * hash + this.index;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final FieldNode other = (FieldNode) obj;
            if (!Objects.equals(this.obj, other.obj)) {
                return false;
            }
            if (!Objects.equals(this.fieldSet.get(FIELD_INDEX), other.fieldSet.get(FIELD_INDEX))) {
                return false;
            }
            return this.index == other.index;
        }
    }

    private static class MyTreeModel extends DefaultTreeModel {

        private final Object mtroot;

        private final List<TreeModelListener> listeners = new ArrayList<>();

        private final Map<String, Object> nodeCache = new HashMap<>();

        // it is much faster to store the reverse mappings, too
        private final Map<Object, String> nodeCacheReverse = new HashMap<>();

        private Object getNodeByPath(String path) {

            if (nodeCache.containsKey(path)) {
                return nodeCache.get(path);
            }
            return null;
        }

        public String getNodePathName(Object find) {

            if (nodeCacheReverse.containsKey(find)) {
                return nodeCacheReverse.get(find);
            }
            return null;
        }

        public List<FieldNode> getDependentFields(FieldNode fnode) {
            List<FieldNode> ret = new ArrayList<>();
            getDependentFields(getNodePathName(fnode), mtroot.getClass().getSimpleName(), mtroot, ret);
            return ret;
        }

        public void getDependentFields(String dependence, String currentPath, Object node, List<FieldNode> ret) {
            if (node instanceof FieldNode) {
                FieldNode fnode = (FieldNode) node;
                Conditional cond = fnode.fieldSet.get(FIELD_INDEX).getAnnotation(Conditional.class);
                if (cond != null) {
                    ConditionEvaluator ev = new ConditionEvaluator(cond);
                    String parentPath = currentPath.indexOf('.') == -1 ? "" : currentPath.substring(0, currentPath.lastIndexOf('.'));
                    try {
                        for (String cname : ev.getFields()) {
                            String fullParh = parentPath + "." + cname;
                            if (fullParh.equals(dependence)) {
                                ret.add(fnode);
                                break;
                            }
                        }
                    } catch (AnnotationParseException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                }
            }
            int count = getChildCount(node);
            for (int i = 0; i < count; i++) {
                FieldNode f = (FieldNode) getChild(node, i);
                getDependentFields(dependence, currentPath + "." + f.getName(FIELD_INDEX), f, ret);
            }
        }

        public MyTreeModel(Tag root) {
            super(new DefaultMutableTreeNode(root));
            this.mtroot = root;
            buildCache(root, "");
        }

        private void buildCache(Object obj, String parentPath) {
            if (!"".equals(parentPath)) {
                parentPath += ".";
            }
            if (obj instanceof FieldNode) {
                FieldNode fn = (FieldNode) obj;
                parentPath += fn.getName(FIELD_INDEX);
            } else {
                parentPath += obj.getClass().getSimpleName();
            }
            nodeCache.put(parentPath, obj);
            nodeCacheReverse.put(obj, parentPath);
            int count = getChildCount(obj, false);
            for (int i = 0; i < count; i++) {
                buildCache(getChild(obj, i, false), parentPath);
            }
        }

        @Override
        public Object getRoot() {
            return mtroot;
        }

        private Object getChild(Object parent, int index, boolean limited) {
            if (parent == mtroot) {
                return new FieldNode(mtroot, filterFields(this, mtroot.getClass().getSimpleName(), mtroot.getClass(), limited).get(index), -1);
            }
            FieldNode fnode = (FieldNode) parent;
            Field field = fnode.fieldSet.get(FIELD_INDEX);
            if (ReflectionTools.needsIndex(field) && (fnode.index == -1)) { //Arrays ot Lists
                return new FieldNode(fnode.obj, fnode.fieldSet, index);
            }
            parent = fnode.getValue(FIELD_INDEX);
            return new FieldNode(parent, filterFields(this, getNodePathName(fnode), parent.getClass(), limited).get(index), -1);
        }

        @Override
        public Object getChild(Object parent, int index) {
            return getChild(parent, index, true);
        }

        @Override
        public int getChildCount(Object parent) {
            return getChildCount(parent, true);
        }

        private int getChildCount(Object parent, boolean limited) {
            if (parent == mtroot) {
                return filterFields(this, mtroot.getClass().getSimpleName(), mtroot.getClass(), limited).size();
            }
            FieldNode fnode = (FieldNode) parent;
            if (isLeaf(fnode)) {
                return 0;
            }
            Field field = fnode.fieldSet.get(FIELD_INDEX);
            if (ReflectionTools.needsIndex(field) && (fnode.index == -1)) { //Arrays or Lists
                try {
                    if (field.get(fnode.obj) == null) {
                        // todo: instanciate the (Array)List or Array to allow adding items to it
                        return 0;
                    }
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    return 0;
                }

                return ReflectionTools.getFieldSubSize(fnode.obj, field);
            }
            parent = fnode.getValue(FIELD_INDEX);

            return filterFields(this, getNodePathName(fnode), parent.getClass(), limited).size();
        }

        @Override
        public boolean isLeaf(Object node) {
            if (node == mtroot) {
                return false;
            }

            FieldNode fnode = (FieldNode) node;
            Field field = fnode.fieldSet.get(FIELD_INDEX);
            boolean isByteArray = field.getType().equals(byte[].class);
            if (!isByteArray && ReflectionTools.needsIndex(field) && fnode.index == -1) {
                return false;
            }
            boolean r = hasEditor(fnode.obj, field, fnode.index);
            return r;
        }

        public void vchanged(TreePath path) {
            fireTreeNodesChanged(this, path.getPath(), null, null);
        }

        @Override
        public int getIndexOfChild(Object parent, Object child) {
            int cnt = getChildCount(parent);
            for (int i = 0; i < cnt; i++) {
                if (getChild(parent, i).equals(child)) {
                    return i;
                }
            }
            return -1;
        }
    }

    private TreeModel getModel() {
        return new MyTreeModel(editedTag);
    }

    @Override
    public void setEditMode(boolean edit, Tag tag) {
        if (tag == null) {
            tag = this.tag;
        }
        this.tag = tag;
        try {
            editedTag = tag.cloneTag();
        } catch (InterruptedException ex) {
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        tree.setEditable(edit);
        if (!edit) {
            tree.stopEditing();
        }
        refreshTree();
    }

    @Override
    public void save() {
        tree.stopEditing();
        SWF swf = tag.getSwf();
        assignTag(tag, editedTag);
        tag.setModified(true);
        tag.setSwf(swf);
    }

    private void assignTag(Tag t, Tag assigned) {
        if (t.getClass() != assigned.getClass()) {
            return;
        }
        for (Field f : getAvailableFields(t.getClass())) {
            try {
                f.set(t, f.get(assigned));
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public Tag getTag() {
        return tag;
    }

    public static String swfArrayToString(SWFArray swfArray) {
        String result = "";
        if (swfArray == null) {
            return result;
        }
        if (swfArray.count() > 0) {
            result += "[" + swfArray.count() + "]";
        } else if (!swfArray.countField().isEmpty()) {
            result += "[" + swfArray.countField() + "]";
        }
        return result;
    }

    public static String swfTypeToString(Class<?> type, SWFType swfType, SWFArray swfArray) {
        String stype = type.getSimpleName();
        if (swfType == null) {
            return stype + swfArrayToString(swfArray);
        }
        String result = swfType.value().toString();
        if (swfType.value() == BasicType.OTHER) {
            result = stype;
        }
        if (swfType.count() > 0) {
            result += "[" + swfType.count();
            if (swfType.countAdd() > 0) {
                result += " + " + swfType.countAdd();
            }
            result += "]";
        } else if (!swfType.countField().isEmpty()) {
            result += "[" + swfType.countField();
            if (swfType.countAdd() > 0) {
                result += " + " + swfType.countAdd();
            }
            result += "]";
        }
        return result + swfArrayToString(swfArray);
    }

    private static boolean hasEditor(Object obj, Field field, int index) {
        boolean isByteArray = field.getType().equals(byte[].class);
        if (!isByteArray && ReflectionTools.needsIndex(field) && index == -1) {
            return false;
        }
        Class<?> type;
        try {
            Object val = ReflectionTools.getValue(obj, field, index);
            if (val == null) {
                return false;
            }
            type = val.getClass();
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            return false;
        }
        SWFType swfType = field.getAnnotation(SWFType.class);
        Multiline multiline = field.getAnnotation(Multiline.class);

        if (type.equals(int.class) || type.equals(Integer.class)
                || type.equals(short.class) || type.equals(Short.class)
                || type.equals(long.class) || type.equals(Long.class)
                || type.equals(double.class) || type.equals(Double.class)
                || type.equals(float.class) || type.equals(Float.class)) {
            return true;
        } else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
            return true;
        } else if (type.equals(String.class)) {
            return true;
        } else if (type.equals(RGB.class) || type.equals(RGBA.class) || type.equals(ARGB.class)) {
            return true;
        } else if (isByteArray || type.equals(ByteArrayRange.class)) {
            return true;
        } else {
            return false;
        }
    }

    private static class FieldSet {

        public List<Field> fields;

        public String name;

        public String itemName;

        public FieldSet(Field field) {
            fields = new ArrayList<>();
            fields.add(field);
            name = field.getName();
        }

        public FieldSet(List<Field> fields, String name, String itemName) {
            this.fields = fields;
            this.name = name;
            this.itemName = itemName;
        }

        public Field get(int index) {
            return fields.get(index);
        }

        public int size() {
            return fields.size();
        }
    }

    private static List<FieldSet> filterFields(MyTreeModel mod, String parentPath, Class<?> cls, boolean limited) {
        List<FieldSet> ret = new ArrayList<>();
        List<Field> fields = getAvailableFields(cls);
        Map<String, List<Field>> tables = new HashMap<>();
        for (Field f : fields) {
            if (limited) {
                Conditional cond = f.getAnnotation(Conditional.class);
                if (cond != null) {
                    ConditionEvaluator ev = new ConditionEvaluator(cond);
                    try {
                        Map<String, Boolean> fieldMap = new HashMap<>();
                        for (String sf : ev.getFields()) {
                            String fulldf = parentPath + "." + sf;
                            FieldNode condnode = (FieldNode) (mod).getNodeByPath(fulldf);

                            if (condnode != null) {
                                Object value = ReflectionTools.getValue(condnode.obj, condnode.fieldSet.get(FIELD_INDEX), condnode.index);
                                if (value instanceof Boolean) {
                                    fieldMap.put(sf, (Boolean) value);
                                } else if (value instanceof Integer) {
                                    int intValue = (Integer) value;
                                    boolean found = false;
                                    for (int i : cond.options()) {
                                        if (i == intValue) {
                                            found = true;
                                        }
                                    }
                                    fieldMap.put(sf, found);
                                }
                            } else {
                                fieldMap.put(sf, true);
                            }
                        }
                        if (!ev.eval(fieldMap)) {
                            continue;
                        }
                    } catch (AnnotationParseException | IllegalArgumentException | IllegalAccessException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                }
            }
            Table t = f.getAnnotation(Table.class);
            List<Field> ret1;
            if (t != null) {
                String tableName = t.value();
                if (!tables.containsKey(tableName)) {
                    ret1 = new ArrayList<>();
                    tables.put(tableName, ret1);
                    ret.add(new FieldSet(ret1, tableName, t.itemName()));
                }
                tables.get(tableName).add(f);
            } else {
                ret.add(new FieldSet(f));
            }
        }
        return ret;
    }

    private static List<Field> getAvailableFields(Class<?> cls) {
        List<Field> ret = fieldCache.get(cls);
        if (ret == null) {
            ret = new ArrayList<>();
            Field fields[] = cls.getFields();
            for (Field f : fields) {
                if (Modifier.isStatic(f.getModifiers())) {
                    continue;
                }
                f.setAccessible(true);
                Internal inter = f.getAnnotation(Internal.class);
                if (inter != null) {
                    continue;
                }
                HideInRawEdit hide = f.getAnnotation(HideInRawEdit.class);
                if (hide != null) {
                    continue;
                }
                ret.add(f);
            }
            fieldCache.put(cls, ret);
        }
        return ret;
    }

    private void addItem(Object obj, Field field, int index, Class<?> cls) {
        SWFArray swfArray = field.getAnnotation(SWFArray.class);
        if (swfArray != null && !swfArray.countField().isEmpty()) { //Fields with same countField must be enlarged too
            Field fields[] = obj.getClass().getDeclaredFields();
            List<Integer> sameFlds = new ArrayList<>();
            for (int f = 0; f < fields.length; f++) {
                SWFArray fieldSwfArray = fields[f].getAnnotation(SWFArray.class);
                if (fieldSwfArray != null && fieldSwfArray.countField().equals(swfArray.countField())) {
                    sameFlds.add(f);
                    if (cls == null && !ReflectionTools.canAddToField(obj, fields[f])) {
                        JOptionPane.showMessageDialog(this, "This field is abstract, cannot be instantiated, sorry."); //TODO!!!
                        return;
                    }

                }
            }
            for (int f : sameFlds) {
                ReflectionTools.addToField(obj, fields[f], index, true, cls);
                try {
                    Object v = ReflectionTools.getValue(obj, fields[f], index);
                    if (v instanceof ASMSource) {
                        ASMSource asv = (ASMSource) v;
                        asv.setSourceTag(editedTag);
                    }
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    //ignore
                }
            }
            try {
                //If countField exists, increment, otherwise do nothing
                Field countField = obj.getClass().getDeclaredField(swfArray.countField());
                int cnt = countField.getInt(obj);
                cnt++;
                countField.setInt(obj, cnt);
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                //ignored
            }
        } else {
            if (cls == null && !ReflectionTools.canAddToField(obj, field)) {
                JOptionPane.showMessageDialog(this, "This field is abstract, cannot be instantiated, sorry."); //TODO!!!
                return;
            }
            ReflectionTools.addToField(obj, field, index, true, cls);
            try {
                Object v = ReflectionTools.getValue(obj, field, index);
                if (v instanceof ASMSource) {
                    ASMSource asv = (ASMSource) v;
                    asv.setSourceTag(editedTag);
                }
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                //ignore
            }
        }
        refreshTree();
    }

    public void refreshTree() {
        View.refreshTree(tree, getModel());
        revalidate();
        repaint();
    }

    private void removeItem(Object obj, Field field, int index) {
        SWFArray swfArray = field.getAnnotation(SWFArray.class);
        if (swfArray != null && !swfArray.countField().isEmpty()) { //Fields with same countField must be removed from too
            Field fields[] = obj.getClass().getDeclaredFields();
            for (int f = 0; f < fields.length; f++) {
                SWFArray fieldSwfArray = fields[f].getAnnotation(SWFArray.class);
                if (fieldSwfArray != null && fieldSwfArray.countField().equals(swfArray.countField())) {
                    ReflectionTools.removeFromField(obj, fields[f], index);
                }
            }
            try {
                //If countField exists, decrement, otherwise do nothing
                Field countField = obj.getClass().getDeclaredField(swfArray.countField());
                int cnt = countField.getInt(obj);
                cnt--;
                countField.setInt(obj, cnt);
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                //ignored
            }
        } else {
            ReflectionTools.removeFromField(obj, field, index);
        }

        refreshTree();
    }
}
