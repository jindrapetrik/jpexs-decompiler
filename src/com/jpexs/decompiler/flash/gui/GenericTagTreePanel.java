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
import com.jpexs.decompiler.flash.gui.generictageditors.BooleanEditor;
import com.jpexs.decompiler.flash.gui.generictageditors.ColorEditor;
import com.jpexs.decompiler.flash.gui.generictageditors.GenericTagEditor;
import com.jpexs.decompiler.flash.gui.generictageditors.NumberEditor;
import com.jpexs.decompiler.flash.gui.generictageditors.StringEditor;
import com.jpexs.decompiler.flash.tags.Tag;
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
import com.jpexs.decompiler.flash.types.annotations.parser.AnnotationParseException;
import com.jpexs.decompiler.flash.types.annotations.parser.ConditionEvaluator;
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

    private JTree tree;

    private Tag editedTag;

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

        private GenericTagEditor editor = null;

        private final JTree tree;

        private FieldNode fnode;

        public MyTreeCellEditor(JTree tree) {
            this.tree = tree;
        }

        @Override
        public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
            if (value instanceof FieldNode) {
                fnode = (FieldNode) value;
                Field field = fnode.field;
                int index = fnode.index;
                Object obj = fnode.obj;
                Class<?> type;
                try {
                    type = ReflectionTools.getValue(obj, field, index).getClass();
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    ex.printStackTrace();
                    return null;
                }
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
                }
                JPanel pan = new JPanel();
                FlowLayout fl = new FlowLayout(FlowLayout.LEFT, 0, 0);
                fl.setAlignOnBaseline(true);
                pan.setLayout(fl);
                JLabel nameLabel = new JLabel(fnode.getNameType() + " = ") {

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
                nameLabel.setSize(nameLabel.getWidth(), ((Component) editor).getHeight());
                nameLabel.setAlignmentY(TOP_ALIGNMENT);
                ((JComponent) editor).setAlignmentY(TOP_ALIGNMENT);
                pan.add(nameLabel);
                pan.add((Component) editor);
                pan.setPreferredSize(new Dimension((int) nameLabel.getPreferredSize().getWidth() + 5 + (int) ((Component) editor).getPreferredSize().getWidth(), (int) ((Component) editor).getPreferredSize().getHeight()));
                return pan;
            }
            return null;

        }

        @Override
        public Object getCellEditorValue() {
            return editor.getChangedValue();
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
                    && path != null && (tree.getModel().isLeaf(obj));
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
            editor.save();
            ((MyTreeModel) tree.getModel()).vchanged(tree.getSelectionPath());
            refreshTree();
            return true;
        }
    }

    public GenericTagTreePanel() {
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
                                SWFArray swfArray = fnode.field.getAnnotation(SWFArray.class);
                                String itemStr = "";
                                if (swfArray != null) {
                                    itemStr = swfArray.value();
                                }
                                if (itemStr.isEmpty()) {
                                    itemStr = AppStrings.translate("generictag.array.item");
                                }
                                if (ReflectionTools.needsIndex(fnode.field)) {

                                    boolean canAdd = true;
                                    if (!ReflectionTools.canAddToField(fnode.obj, fnode.field)) {
                                        canAdd = false;
                                    }
                                    JPopupMenu p = new JPopupMenu();
                                    JMenuItem mi;
                                    mi = new JMenuItem(AppStrings.translate("generictag.array.insertbeginning").replace("%item%", itemStr));
                                    mi.addActionListener(new ActionListener() {

                                        @Override
                                        public void actionPerformed(ActionEvent e) {
                                            addItem(fnode.obj, fnode.field, 0);
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
                                                addItem(fnode.obj, fnode.field, fnode.index);
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
                                                removeItem(fnode.obj, fnode.field, fnode.index);
                                            }
                                        });
                                        p.add(mi);

                                        mi = new JMenuItem(AppStrings.translate("generictag.array.insertafter").replace("%item%", itemStr));
                                        mi.addActionListener(new ActionListener() {

                                            @Override
                                            public void actionPerformed(ActionEvent e) {
                                                addItem(fnode.obj, fnode.field, fnode.index + 1);
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
                                            addItem(fnode.obj, fnode.field, ReflectionTools.getFieldSubSize(fnode.obj, fnode.field));
                                        }
                                    });
                                    if (!canAdd) {
                                        mi.setEnabled(false);
                                    }
                                    p.add(mi);
                                    //}
                                    p.show(tree, e.getX(), e.getY());
                                }
                            }
                        }
                    } else if (e.getClickCount() == 2) {
                        //myDoubleClick(selRow, selPath);
                    }
                }
            }
        });
    }

    private Tag tag;

    public class MyTreeCellRenderer extends DefaultTreeCellRenderer {

        @Override
        public Component getTreeCellRendererComponent(
                JTree tree,
                Object value,
                boolean sel,
                boolean expanded,
                boolean leaf,
                int row,
                boolean hasFocus) {

            super.getTreeCellRendererComponent(
                    tree, value, sel,
                    expanded, leaf, row,
                    hasFocus);

            setUI(new BasicLabelUI());
            setOpaque(false);
            setBackgroundNonSelectionColor(Color.white);
            return this;
        }
    }

    @Override
    public void clear() {

    }

    private static class FieldNode extends DefaultMutableTreeNode {

        private Object obj;

        private Field field;

        private int index;

        public FieldNode(Object obj, Field field, int index) {
            this.obj = obj;
            this.field = field;
            this.index = index;
        }

        @Override
        public void setUserObject(Object userObject) {

        }

        /*
         */
        @Override
        public String toString() {

            String valStr = "";
            if (ReflectionTools.needsIndex(field) && (index == -1)) {
                valStr += "";
            } else if (hasEditor(obj, field, index)) {
                Object val = getValue();
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

                valStr += " = " + colorAdd + val.toString();
            }
            return "<html>" + getNameType() + valStr + "</html>";
        }

        public String getType() {
            SWFType swfType = field.getAnnotation(SWFType.class);
            SWFArray swfArray = field.getAnnotation(SWFArray.class);
            String typeStr = null;
            if ((swfType != null || swfArray != null) && !(ReflectionTools.needsIndex(field) && (index > -1))) {
                Class<?> type = field.getType();
                if (ReflectionTools.needsIndex(field)) {
                    type = ReflectionTools.getFieldSubType(obj, field);
                }
                typeStr = swfTypeToString(type, swfType, swfArray);
            }
            return typeStr;
        }

        public String getNameType() {
            String typeStr = getType();
            return getName() + (typeStr != null ? " : " + typeStr : "");
        }

        public String getName() {
            SWFArray swfArray = field.getAnnotation(SWFArray.class);
            String name = "";
            if (swfArray != null) {
                name = swfArray.value();
            }
            return (index > -1 ? name + "[" + index + "]" : field.getName());
        }

        public Object getValue() {
            try {
                if (ReflectionTools.needsIndex(field) && (index == -1)) {
                    return obj;
                }
                Object val = ReflectionTools.getValue(obj, field, index);
                if (val == null) {
                    try {
                        val = ReflectionTools.newInstanceOf(field.getType());
                        ReflectionTools.setValue(obj, field, index, val);
                    } catch (InstantiationException | IllegalAccessException ex) {
                        Logger.getLogger(GenericTagTreePanel.class.getName()).log(Level.SEVERE, null, ex);
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
            hash = 11 * hash + Objects.hashCode(this.field);
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
            if (!Objects.equals(this.field, other.field)) {
                return false;
            }
            if (this.index != other.index) {
                return false;
            }
            return true;
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
                Conditional cond = fnode.field.getAnnotation(Conditional.class);
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
                        Logger.getLogger(GenericTagTreePanel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            int count = getChildCount(node);
            for (int i = 0; i < count; i++) {
                FieldNode f = (FieldNode) getChild(node, i);
                getDependentFields(dependence, currentPath + "." + f.getName(), f, ret);
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
                parentPath += fn.getName();
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
            Field field = fnode.field;
            if (ReflectionTools.needsIndex(field) && (fnode.index == -1)) { //Arrays ot Lists
                return new FieldNode(fnode.obj, field, index);
            }
            parent = fnode.getValue();
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
            Field field = fnode.field;
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
            parent = fnode.getValue();

            return filterFields(this, getNodePathName(fnode), parent.getClass(), limited).size();
        }

        @Override
        public boolean isLeaf(Object node) {
            if (node == mtroot) {
                return false;
            }

            FieldNode fnode = (FieldNode) node;
            Field field = fnode.field;
            if (ReflectionTools.needsIndex(field) && fnode.index == -1) {
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
        SWF swf = tag.getSwf();
        try {
            editedTag = tag.cloneTag();
        } catch (InterruptedException ex) {
        } catch (IOException ex) {
            Logger.getLogger(GenericTagTreePanel.class.getName()).log(Level.SEVERE, null, ex);
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
                Logger.getLogger(GenericTagTreePanel.class.getName()).log(Level.SEVERE, null, ex);
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
        if (ReflectionTools.needsIndex(field) && index == -1) {
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
        } else {
            return false;
        }
    }

    private static List<Field> filterFields(MyTreeModel mod, String parentPath, Class<?> cls, boolean limited) {
        List<Field> ret = new ArrayList<>();
        List<Field> fields = getAvailableFields(cls);
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
                                Object value = ReflectionTools.getValue(condnode.obj, condnode.field, condnode.index);
                                if (value instanceof Boolean) {
                                    fieldMap.put(sf, (Boolean) value);
                                } else if (value instanceof Integer) {
                                    int intValue = (int) value;
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
                        Logger.getLogger(GenericTagTreePanel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            ret.add(f);
        }
        return ret;
    }

    private static List<Field> getAvailableFields(Class<?> cls) {
        List<Field> ret = new ArrayList<>();
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
        return ret;
    }

    private void addItem(Object obj, Field field, int index) {
        SWFArray swfArray = field.getAnnotation(SWFArray.class);
        if (swfArray != null && !swfArray.countField().isEmpty()) { //Fields with same countField must be enlarged too
            Field fields[] = obj.getClass().getDeclaredFields();
            List<Integer> sameFlds = new ArrayList<>();
            for (int f = 0; f < fields.length; f++) {
                SWFArray fieldSwfArray = fields[f].getAnnotation(SWFArray.class);
                if (fieldSwfArray != null && fieldSwfArray.countField().equals(swfArray.countField())) {
                    sameFlds.add(f);
                    if (!ReflectionTools.canAddToField(obj, fields[f])) {
                        JOptionPane.showMessageDialog(this, "This field is abstract, cannot be instantiated, sorry."); //TODO!!!
                        return;
                    }

                }
            }
            for (int f : sameFlds) {
                ReflectionTools.addToField(obj, fields[f], index, true);
            }
            try {
                //If countField exists, increment, otherwise do nothing
                Field countField = obj.getClass().getDeclaredField(swfArray.countField());
                if (countField != null) {
                    int cnt = countField.getInt(obj);
                    cnt++;
                    countField.setInt(obj, cnt);
                }
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                //ignored
            }
        } else {
            if (!ReflectionTools.canAddToField(obj, field)) {
                JOptionPane.showMessageDialog(this, "This field is abstract, cannot be instantiated, sorry."); //TODO!!!
                return;
            }
            ReflectionTools.addToField(obj, field, index, true);
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
                if (countField != null) {
                    int cnt = countField.getInt(obj);
                    cnt--;
                    countField.setInt(obj, cnt);
                }
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                //ignored
            }
        } else {
            ReflectionTools.removeFromField(obj, field, index);
        }

        refreshTree();
    }
}
