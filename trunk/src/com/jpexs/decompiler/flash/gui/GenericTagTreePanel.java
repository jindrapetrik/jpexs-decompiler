/*
 * Copyright (C) 2014 JPEXS
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
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.AppStrings;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.gui.generictageditors.BooleanEditor;
import com.jpexs.decompiler.flash.gui.generictageditors.ColorEditor;
import com.jpexs.decompiler.flash.gui.generictageditors.GenericTagEditor;
import com.jpexs.decompiler.flash.gui.generictageditors.NumberEditor;
import com.jpexs.decompiler.flash.gui.generictageditors.ReflectionTools;
import com.jpexs.decompiler.flash.gui.generictageditors.StringEditor;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.types.ARGB;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.flash.types.annotations.Multiline;
import com.jpexs.decompiler.flash.types.annotations.SWFArray;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.Helper;
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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
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

        public MyTreeCellEditor(JTree tree) {
            this.tree = tree;
        }

        @Override
        public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
            if (value instanceof FieldNode) {
                FieldNode fnode = (FieldNode) value;
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
            editor.save();
            ((MyTreeModel)tree.getModel()).vchanged(tree.getSelectionPath());
            return true;
        }

    }

    public GenericTagTreePanel() {
        setLayout(new BorderLayout());
        tree = new MyTree();

        add(tree, BorderLayout.CENTER);
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
                                if(swfArray!=null){
                                    itemStr = swfArray.value();
                                }
                                if(itemStr.equals("")){
                                    itemStr = AppStrings.translate("generictag.array.item");
                                }
                                if (ReflectionTools.needsIndex(fnode.field)) {
                                    JPopupMenu p = new JPopupMenu();
                                    JMenuItem mi;
                                    mi = new JMenuItem(AppStrings.translate("generictag.array.insertbeginning").replace("%item%", itemStr));
                                    mi.addActionListener(new ActionListener() {

                                        @Override
                                        public void actionPerformed(ActionEvent e) {
                                            addItem(fnode.obj, fnode.field, 0);
                                        }
                                    });
                                    p.add(mi);

                                    if (fnode.index > -1) {
                                        mi = new JMenuItem(AppStrings.translate("generictag.array.insertbefore").replace("%item%", itemStr));
                                        mi.addActionListener(new ActionListener() {

                                            @Override
                                            public void actionPerformed(ActionEvent e) {
                                                addItem(fnode.obj, fnode.field, fnode.index);
                                            }
                                        });
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
                                        p.add(mi);
                                    }

                                    mi = new JMenuItem(AppStrings.translate("generictag.array.insertend").replace("%item%", itemStr));
                                    mi.addActionListener(new ActionListener() {

                                        @Override
                                        public void actionPerformed(ActionEvent e) {
                                            addItem(fnode.obj, fnode.field, ReflectionTools.getFieldSubSize(fnode.obj, fnode.field));
                                        }
                                    });
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
                if(val instanceof RGB){ //Note: Can be RGBA too
                    color = ((RGB)val).toColor();
                }
                if(val instanceof ARGB){
                    color = ((ARGB)val).toColor();
                }
                
                if(color!=null){
                    colorAdd = "<cite style=\"color:rgb("+color.getRed()+","+color.getGreen()+","+color.getBlue()+");\">●</cite> ";
                }
                
                valStr += " = " + colorAdd + val.toString();
            }
            return "<html>"+getNameType() + valStr+"</html>";
        }

        public String getType() {
            SWFType swfType = field.getAnnotation(SWFType.class);
            SWFArray swfArray = field.getAnnotation(SWFArray.class);
            String typeStr = null;
            if ((swfType != null || swfArray!=null) && !(ReflectionTools.needsIndex(field) && (index > -1))) {
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
            String name="";
            if(swfArray!=null){
                name = swfArray.value();
            }
            return (index > -1 ? name + "[" + index + "]" : field.getName());
        }

        public Object getValue() {
            try {
                if (ReflectionTools.needsIndex(field) && (index == -1)) {
                    return obj;
                }
                return ReflectionTools.getValue(obj, field, index);
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

        DefaultTreeModel d;
    }

    private static class MyTreeModel extends DefaultTreeModel {

        private final Object root;
        private final List<TreeModelListener> listeners = new ArrayList<>();
        private DefaultMutableTreeNode rootNode;

        public MyTreeModel(Tag root) {
            super(new DefaultMutableTreeNode(root));
            this.root = root;
        }

        @Override
        public Object getRoot() {            
            return root;
        }

        @Override
        public Object getChild(Object parent, int index) {
            if (parent == root) {
                return new FieldNode(root, getAvailableFields(root.getClass()).get(index), -1);
            }
            FieldNode fnode = (FieldNode) parent;
            Field field = fnode.field;
            if (ReflectionTools.needsIndex(field) && (fnode.index == -1)) { //Arrays ot Lists
                return new FieldNode(fnode.obj, field, index);
            }
            parent = fnode.getValue();
            return new FieldNode(parent, getAvailableFields(parent.getClass()).get(index), -1);
        }

        @Override
        public int getChildCount(Object parent) {
            if (parent == root) {
                return getAvailableFields(root.getClass()).size();
            }
            FieldNode fnode = (FieldNode) parent;
            if (isLeaf(fnode)) {
                return 0;
            }
            Field field = fnode.field;
            if (ReflectionTools.needsIndex(field) && (fnode.index == -1)) { //Arrays ot Lists
                return ReflectionTools.getFieldSubSize(fnode.obj, field);
            }
            parent = fnode.getValue();

            return getAvailableFields(parent.getClass()).size();
        }

        @Override
        public boolean isLeaf(Object node) {
            if (node == root) {
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

        public void vchanged(TreePath path){
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
        this.editedTag = (Tag) Helper.deepCopy(tag);
        refreshTree();
        tree.setEditable(edit);
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
                Logger.getLogger(GenericTagPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public Tag getTag() {
        return tag;
    }

    public static String swfArrayToString(SWFArray swfArray){
        String result="";
        if(swfArray==null){
            return result;
        }
        if (swfArray.count() > 0) {
            result += "[" + swfArray.count() + "]";
        } else if (!swfArray.countField().equals("")) {
            result += "[" + swfArray.countField()+ "]";
        }
        return result;        
    }
    
    public static String swfTypeToString(Class<?> type, SWFType swfType, SWFArray swfArray) {
        String stype = type.getSimpleName();
        if (swfType == null) {            
            return stype+swfArrayToString(swfArray);
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
        } else if (!swfType.countField().equals("")) {
            result += "[" + swfType.countField();
            if (swfType.countAdd() > 0) {
                result += " + " + swfType.countAdd();
            }
            result += "]";
        }
        return result+swfArrayToString(swfArray);
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
            ret.add(f);
        }
        return ret;
    }

    private void addItem(Object obj, Field field, int index) {
        SWFArray swfArray = field.getAnnotation(SWFArray.class);
        if (swfArray != null && !swfArray.countField().equals("")) { //Fields with same countField must be enlarged too
            Field fields[] = obj.getClass().getDeclaredFields();
            for (int f = 0; f < fields.length; f++) {
                SWFArray fieldSwfArray = fields[f].getAnnotation(SWFArray.class);
                if (fieldSwfArray != null && fieldSwfArray.countField().equals(swfArray.countField())) {
                    ReflectionTools.addToField(obj, fields[f], index, true);
                }
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
        if (swfArray != null && !swfArray.countField().equals("")) { //Fields with same countField must be removed from too
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
