/*
 *  Copyright (C) 2010-2024 JPEXS
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
import com.jpexs.decompiler.flash.amf.amf3.Amf3Value;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.gui.generictageditors.Amf3ValueEditor;
import com.jpexs.decompiler.flash.gui.generictageditors.BinaryDataEditor;
import com.jpexs.decompiler.flash.gui.generictageditors.BooleanEditor;
import com.jpexs.decompiler.flash.gui.generictageditors.ColorEditor;
import com.jpexs.decompiler.flash.gui.generictageditors.EnumEditor;
import com.jpexs.decompiler.flash.gui.generictageditors.FloatEditor;
import com.jpexs.decompiler.flash.gui.generictageditors.FullSized;
import com.jpexs.decompiler.flash.gui.generictageditors.GenericTagEditor;
import com.jpexs.decompiler.flash.gui.generictageditors.NumberEditor;
import com.jpexs.decompiler.flash.gui.generictageditors.StringEditor;
import com.jpexs.decompiler.flash.gui.generictageditors.UUIDEditor;
import com.jpexs.decompiler.flash.gui.tagtree.AbstractTagTree;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.flash.tags.base.CharacterIdTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.types.ARGB;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.CLIPACTIONRECORD;
import com.jpexs.decompiler.flash.types.CLIPACTIONS;
import com.jpexs.decompiler.flash.types.HasSwfAndTag;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.annotations.Conditional;
import com.jpexs.decompiler.flash.types.annotations.ConditionalType;
import com.jpexs.decompiler.flash.types.annotations.EnumValue;
import com.jpexs.decompiler.flash.types.annotations.EnumValues;
import com.jpexs.decompiler.flash.types.annotations.HideInRawEdit;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.flash.types.annotations.Multiline;
import com.jpexs.decompiler.flash.types.annotations.SWFArray;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.annotations.Table;
import com.jpexs.decompiler.flash.types.annotations.UUID;
import com.jpexs.decompiler.flash.types.annotations.parser.AnnotationParseException;
import com.jpexs.decompiler.flash.types.annotations.parser.ConditionEvaluator;
import com.jpexs.decompiler.flash.types.filters.CONVOLUTIONFILTER;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.ConcreteClasses;
import com.jpexs.helpers.ReflectionTools;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.lang.annotation.Annotation;
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
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.basic.BasicLabelUI;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * @author JPEXS
 */
public class GenericTagTreePanel extends GenericTagPanel {

    private static final Logger logger = Logger.getLogger(GenericTagTreePanel.class.getName());

    private JTree tree;

    private Tag editedTag;

    private static final Map<Class, List<Field>> fieldCache = new HashMap<>();

    private static final int FIELD_INDEX = 0;

    private List<TreeModelListener> modelListeners = new ArrayList<>();

    public void addTreeModelListener(TreeModelListener listener) {
        modelListeners.add(listener);
        ((DefaultTreeModel) tree.getModel()).addTreeModelListener(listener);
    }

    public void addTreeSelectionListener(TreeSelectionListener listener) {
        tree.addTreeSelectionListener(listener);
    }

    public void removeTreeSelectionListener(TreeSelectionListener listener) {
        tree.removeTreeSelectionListener(listener);
    }

    public void removeTreeModelListener(TreeModelListener listener) {
        modelListeners.remove(listener);
        ((DefaultTreeModel) tree.getModel()).removeTreeModelListener(listener);
    }

    private class MyTree extends JTree {

        public MyTree() {
            if (View.isOceanic()) {
                setBackground(Color.white);
            }
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

    private static SWFType evalSwfType(MyTreeModel mod, String parentPath, SWFType swfType) {
        if (swfType == null) {
            return null;
        }
        if ("".equals(swfType.alternateCondition())) {
            return swfType;
        }
        Conditional cond = new Conditional() {
            @Override
            public String[] value() {
                return new String[]{swfType.alternateCondition()};
            }

            @Override
            public int[] tags() {
                return new int[0];
            }

            @Override
            public int minSwfVersion() {
                return 1;
            }

            @Override
            public int maxSwfVersion() {
                return Integer.MAX_VALUE;
            }

            @Override
            public int[] options() {
                return new int[0];
            }

            @Override
            public boolean revert() {
                return false;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return Conditional.class;
            }
        };
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
            if (!ev.eval(fieldMap, 0)) {
                return swfType;
            }
            return new SWFType() {
                @Override
                public BasicType value() {
                    return swfType.alternateValue();
                }

                @Override
                public BasicType alternateValue() {
                    return BasicType.NONE;
                }

                @Override
                public String alternateCondition() {
                    return "";
                }

                @Override
                public int count() {
                    return swfType.count();
                }

                @Override
                public String countField() {
                    return swfType.countField();
                }

                @Override
                public int countAdd() {
                    return swfType.countAdd();
                }

                @Override
                public Class<? extends Annotation> annotationType() {
                    return SWFType.class;
                }

                @Override
                public boolean canAdd() {
                    return swfType.canAdd();
                }
            };
        } catch (AnnotationParseException | IllegalArgumentException | IllegalAccessException ex) {
            logger.log(Level.SEVERE, null, ex);
            return swfType;
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
            Rectangle cellRect = tree.getPathBounds(tree.getPathForRow(row));
            Rectangle treeVisibleRect = tree.getVisibleRect();
            int scrollBarSize = ((Integer) UIManager.get("ScrollBar.width")).intValue();

            Rectangle cellMaxVisibleRect = new Rectangle(cellRect.x, cellRect.y, treeVisibleRect.width - cellRect.x - tree.getInsets().left - tree.getInsets().right - scrollBarSize, cellRect.height);

            if (value instanceof FieldNode) {
                fnode = (FieldNode) value;
                JPanel panSum = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                panSum.setOpaque(false);
                for (int i = 0; i < fnode.fieldSet.size(); i++) {
                    Field field = fnode.fieldSet.get(i);
                    int index = fnode.index;
                    final Object obj = fnode.obj;
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
                    MyTreeModel model = (MyTreeModel) tree.getModel();

                    SWFArray swfArray = field.getAnnotation(SWFArray.class);
                    boolean isArray = ReflectionTools.needsIndex(field) || swfArray != null;
                    boolean isArrayParent = isArray && index == -1;

                    String thisPath = model.getNodePathName(value);
                    String parentPath = thisPath.substring(0, thisPath.lastIndexOf("."));
                    if (isArray && !isArrayParent) {
                        parentPath = parentPath.substring(0, parentPath.lastIndexOf("."));
                    }

                    swfType = evalSwfType(model, parentPath, swfType);

                    UUID uuid = field.getAnnotation(UUID.class);

                    Multiline multiline = field.getAnnotation(Multiline.class);
                    EnumValues enumValues = field.getAnnotation(EnumValues.class);
                    if (uuid != null) {
                        editor = new UUIDEditor(field.getName(), obj, field, index, type);
                    } else if (enumValues != null && (type.equals(int.class) || type.equals(Integer.class))) {
                        Map<Integer, String> values = new HashMap<>();
                        for (EnumValue enumValue : enumValues.value()) {
                            values.put(enumValue.value(), enumValue.text());
                        }

                        editor = new EnumEditor(field.getName(), obj, field, index, type, swfType, values);
                    } else if (type.equals(double.class) || type.equals(Double.class)
                            || type.equals(float.class) || type.equals(Float.class)) {
                        editor = new FloatEditor(field.getName(), obj, field, index, type);
                    } else if (type.equals(int.class) || type.equals(Integer.class)
                            || type.equals(short.class) || type.equals(Short.class)
                            || type.equals(long.class) || type.equals(Long.class)) {
                        editor = new NumberEditor(field.getName(), obj, field, index, type, swfType);
                    } else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
                        editor = new BooleanEditor(field.getName(), obj, field, index, type);
                    } else if (type.equals(String.class)) {
                        editor = new StringEditor(field.getName(), obj, field, index, type, multiline != null);
                    } else if (type.equals(RGB.class) || type.equals(RGBA.class) || type.equals(ARGB.class)) {
                        editor = new ColorEditor(field.getName(), obj, field, index, type);
                    } else if (type.equals(byte[].class) || type.equals(ByteArrayRange.class)) {
                        editor = new BinaryDataEditor(mainPanel, field.getName(), obj, field, index, type);
                    } else if (type.equals(Amf3Value.class)) {
                        editor = new Amf3ValueEditor(field.getName(), obj, field, index, type);
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
                    JLabel nameLabel = new JLabel(fnode.getNameType(i) + (editor == null ? "" : " = ")) {
                        @Override
                        public BaselineResizeBehavior getBaselineResizeBehavior() {
                            return Component.BaselineResizeBehavior.CONSTANT_ASCENT;
                        }

                        @Override
                        public int getBaseline(int width, int height) {
                            return 0;
                        }
                    };
                    pan.setOpaque(false);
                    nameLabel.setAlignmentY(TOP_ALIGNMENT);
                    pan.add(nameLabel);

                    JComponent editorComponent = (JComponent) editor;
                    if (editorComponent != null) {
                        nameLabel.setSize(nameLabel.getWidth(), editorComponent.getHeight());
                        editorComponent.setAlignmentY(TOP_ALIGNMENT);
                        pan.add(editorComponent);
                        if (editorComponent instanceof FullSized) {
                            editorComponent.setPreferredSize(new Dimension(cellMaxVisibleRect.width - (int) nameLabel.getPreferredSize().getWidth() - 5, editorComponent.getPreferredSize().height));
                        }
                        if (editorComponent instanceof GenericTagEditor) {
                            ((GenericTagEditor) editorComponent).added();
                        }
                        pan.setPreferredSize(new Dimension((int) nameLabel.getPreferredSize().getWidth() + 5 + (int) editorComponent.getPreferredSize().getWidth(), (int) editorComponent.getPreferredSize().getHeight()));
                    } else {
                        pan.setPreferredSize(new Dimension((int) nameLabel.getPreferredSize().getWidth(), (int) nameLabel.getPreferredSize().getHeight()));
                    }
                    panSum.add(pan);
                }
                panSum.setPreferredSize(new Dimension(cellMaxVisibleRect.width, panSum.getPreferredSize().height));
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

            FieldNode fnode = (FieldNode) obj;
            Field field = fnode.fieldSet.get(FIELD_INDEX);

            boolean ret = super.isCellEditable(e)
                    && tree.getModel().isLeaf(obj) && hasEditor(fnode.obj, field, fnode.index);
            return ret;
        }

        @Override
        public boolean stopCellEditing() {
            boolean modified = false;
            if (editors != null) {
                for (GenericTagEditor editor : editors) {
                    try {
                        editor.validateValue();
                        if (editor.save()) {

                            if (editor.getObject() instanceof CONVOLUTIONFILTER) {
                                String fname = editor.getFieldName();
                                if ("matrixX".equals(fname) || "matrixY".equals(fname)) {
                                    CONVOLUTIONFILTER cf = (CONVOLUTIONFILTER) editor.getObject();
                                    if (cf.matrix.length != cf.matrixX * cf.matrixY) {
                                        float[] newmatrix = new float[cf.matrixX * cf.matrixY];
                                        int copycount = cf.matrix.length;
                                        if (copycount > cf.matrixX * cf.matrixY) {
                                            copycount = cf.matrixX * cf.matrixY;
                                        }
                                        System.arraycopy(cf.matrix, 0, newmatrix, 0, copycount);
                                        cf.matrix = newmatrix;
                                    }
                                }
                            }

                            modified = true;
                        }
                    } catch (IllegalArgumentException iex) {
                        return false;
                    }
                }
            }
            super.stopCellEditing();

            editors = null;

            if (modified) {
                TreePath sp = tree.getSelectionPath();
                if (sp != null) {
                    ((MyTreeModel) tree.getModel()).vchanged(sp);
                }
                refreshTree();
            }
            return true;
        }
    }

    @Override
    public boolean tryAutoSave() {
        if (Configuration.autoSaveTagModifications.get()) {
            if (tag == null) {
                return true;
            }
            return save();
        }
        return true;
    }

    public GenericTagTreePanel(MainPanel mainPanel) {
        super(mainPanel);
        setLayout(new BorderLayout());
        tree = new MyTree();
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        add(new FasterScrollPane(tree), BorderLayout.CENTER);
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
            }
        });
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
                        if (SwingUtilities.isRightMouseButton(e)) { //right click
                            Object selObject = selPath.getLastPathComponent();
                            if (selObject instanceof FieldNode) {
                                final FieldNode fnode = (FieldNode) selObject;
                                Field field = fnode.fieldSet.get(FIELD_INDEX);
                                if (ReflectionTools.needsIndex(field)) {
                                    SWFArray swfArray = fnode.fieldSet.get(FIELD_INDEX).getAnnotation(SWFArray.class);
                                    SWFType swfType = fnode.fieldSet.get(FIELD_INDEX).getAnnotation(SWFType.class);

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
                                    if (swfArray != null) {
                                        if (swfArray.count() > 0) {
                                            canAdd = false;
                                        }
                                    }
                                    if (swfType != null) {
                                        if (!swfType.canAdd()) {
                                            canAdd = false;
                                        }
                                    }

                                    JPopupMenu p = new JPopupMenu();
                                    JMenuItem mi;
                                    Class<?> subtype = ReflectionTools.getFieldSubType(fnode.obj, fnode.fieldSet.get(FIELD_INDEX));
                                    if (!canAdd && subtype.isAnnotationPresent(ConcreteClasses.class)) {
                                        Class<?>[] availableClasses = subtype.getAnnotation(ConcreteClasses.class).value();
                                        JMenu mBegin = new JMenu(AppStrings.translate("generictag.array.insertbeginning").replace("%item%", itemStr));
                                        p.add(mBegin);
                                        JMenu mBefore = new JMenu(AppStrings.translate("generictag.array.insertbefore").replace("%item%", itemStr));
                                        if (fnode.index > -1) {
                                            p.add(mBefore);
                                        }
                                        mi = new JMenuItem(AppStrings.translate("generictag.array.remove").replace("%item%", itemStr));
                                        mi.addActionListener(new ActionListener() {
                                            @Override
                                            public void actionPerformed(ActionEvent e) {
                                                TreePath[] tps = tree.getSelectionPaths();
                                                if (tps == null) {
                                                    tps = new TreePath[]{selPath};
                                                }
                                                boolean somethingRemoved = false;
                                                for (int t = tps.length - 1; t >= 0; t--) {
                                                    TreePath tp = tps[t];
                                                    Object selObject = tp.getLastPathComponent();
                                                    if (selObject instanceof FieldNode) {
                                                        final FieldNode fnode = (FieldNode) selObject;
                                                        removeItem(fnode.obj, fnode.fieldSet.get(FIELD_INDEX), fnode.index);
                                                        somethingRemoved = true;
                                                    }
                                                }
                                                if (!somethingRemoved) {
                                                    removeItem(fnode.obj, fnode.fieldSet.get(FIELD_INDEX), fnode.index);
                                                }
                                            }
                                        });

                                        if (fnode.index > -1) {
                                            p.add(mi);
                                        }
                                        JMenu mAfter = new JMenu(AppStrings.translate("generictag.array.insertafter").replace("%item%", itemStr));

                                        if (fnode.index > -1) {
                                            p.add(mAfter);
                                        }

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
                                                    TreePath[] tps = tree.getSelectionPaths();
                                                    if (tps == null) {
                                                        tps = new TreePath[]{selPath};
                                                    }
                                                    boolean someRemoved = false;
                                                    for (int t = tps.length - 1; t >= 0; t--) {
                                                        TreePath tp = tps[t];
                                                        Object selObject = tp.getLastPathComponent();
                                                        if (selObject instanceof FieldNode) {
                                                            final FieldNode fnode = (FieldNode) selObject;
                                                            if (fnode.index > -1) {
                                                                removeItem(fnode.obj, fnode.fieldSet.get(FIELD_INDEX), fnode.index);
                                                                someRemoved = true;
                                                            }
                                                        }
                                                    }
                                                    if (!someRemoved) {
                                                        removeItem(fnode.obj, fnode.fieldSet.get(FIELD_INDEX), fnode.index);
                                                    }
                                                }
                                            });
                                            if (!canAdd) {
                                                mi.setEnabled(false);
                                            }
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
            if (View.isOceanic()) {
                setUI(new BasicLabelUI());
                setOpaque(false);
                setBackgroundNonSelectionColor(Color.white);
            }
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Component component = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            if (component instanceof JLabel) {
                JLabel lab = (JLabel) component;
                if (value == tree.getModel().getRoot()) {
                    //It still does not matter since root is hidden
                    if (editedTag != null) {
                        lab.setIcon(AbstractTagTree.getIconForType(AbstractTagTree.getTreeNodeType(editedTag)));
                    }
                }
            }
            return component;
        }

    }

    @Override
    public void clear() {
        tag = null;
        editedTag = null;
        tree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("root")));
        revalidate();
        repaint();
    }

    private static final class TableFieldNodes extends DefaultMutableTreeNode {

        List<FieldNode> subnodes;

        public TableFieldNodes(List<FieldNode> subnodes) {
            this.subnodes = subnodes;
        }
    }

    public static final class FieldNode extends DefaultMutableTreeNode {

        private Tag tag;

        private Object obj;

        private FieldSet fieldSet;

        private int index;

        private MyTreeModel model;

        private Object parentObject;

        public Object getParentObject() {
            return parentObject;
        }

        public FieldNode(Object parent, MyTreeModel model, Tag tag, Object obj, FieldSet fieldSet, int index) {
            this.tag = tag;
            this.obj = obj;
            this.fieldSet = fieldSet;
            this.index = index;
            this.model = model;
            this.parentObject = parent;

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
                ret.insert(0, "<html>").append("</html>");
                return ret.toString();
            }

            if (fieldSet.size() == 1) {
                ret.append(toString(0));
            } else {
                ret.append(fieldSet.name);
                /*SWFArray a = fieldSet.get(0).getAnnotation(SWFArray.class);
                SWFType t = fieldSet.get(0).getAnnotation(SWFType.class);
                if (t != null && !"".equals(t.countField())) {
                    ret.append(" [").append(t.countField()).append("]");
                } else if (a != null) {
                    ret.append(" [").append(a.countField()).append("]");
                } else {
                    ret.append(" []");
                }*/
            }

            ret.insert(0, "<html>").append("</html>");
            return ret.toString();
        }

        public String toString(int fieldIndex) {
            String valStr = "";
            Field field = fieldSet.get(fieldIndex);

            if (field.getAnnotation(UUID.class) != null) {
                StringBuilder sb = new StringBuilder();
                byte[] val = (byte[]) getValue(fieldIndex);
                for (int i = 0; i < val.length; i++) {
                    String h = Integer.toHexString(val[i] & 0xff);
                    if (h.length() == 1) {
                        h = "0" + h;
                    }
                    sb.append(h);
                    if (i == 3 || i == 5 || i == 7 || i == 9) {
                        sb.append("-");
                    }
                }
                valStr += " = " + sb.toString();
            } else if (ReflectionTools.needsIndex(field) && (index == -1)) {
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

                EnumValues enumValues = field.getAnnotation(EnumValues.class);
                String enumAdd = "";
                if (enumValues != null && val instanceof Integer) {
                    Map<Integer, String> values = new HashMap<>();
                    for (EnumValue enumValue : enumValues.value()) {
                        values.put(enumValue.value(), enumValue.text());
                    }

                    enumAdd = " - " + values.get(val);
                }

                if (val instanceof byte[]) {
                    valStr += " = " + ((byte[]) val).length + " byte";
                } else if (val instanceof ByteArrayRange) {
                    valStr += " = " + ((ByteArrayRange) val).getLength() + " byte";
                } else {
                    valStr += " = " + colorAdd + val.toString() + enumAdd;
                }
            }
            return getNameType(fieldIndex) + valStr;
        }

        public String getType(int fieldIndex) {
            UUID uuid = fieldSet.get(fieldIndex).getAnnotation(UUID.class);
            if (uuid != null) {
                return "UUID";
            }
            SWFArray swfArray = fieldSet.get(fieldIndex).getAnnotation(SWFArray.class);
            Class<?> declaredType = fieldSet.get(fieldIndex).getType();
            boolean isArray = ReflectionTools.needsIndex(fieldSet.get(fieldIndex)) || swfArray != null;
            boolean isArrayParent = isArray && index == -1;

            SWFType swfType = fieldSet.get(fieldIndex).getAnnotation(SWFType.class);
            String thisPath = model.getNodePathName(this);
            String parentPath = thisPath.substring(0, thisPath.lastIndexOf("."));
            if (isArray && !isArrayParent) {
                parentPath = parentPath.substring(0, parentPath.lastIndexOf("."));
            }
            swfType = evalSwfType(model, parentPath, swfType);

            Class<?> declaredSubType = isArray ? ReflectionTools.getFieldSubType(obj, fieldSet.get(fieldIndex)) : null;

            Class<?> type = declaredType;
            if (declaredSubType != null) {
                type = declaredSubType;
            }

            if (isArray && !isArrayParent) {
                //get real value object type
                try {
                    Object val = ReflectionTools.getValue(obj, fieldSet.get(fieldIndex), index);
                    if (val != null) {
                        type = val.getClass();
                    }
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    //ignore
                }
            }

            String typeStr = type.getSimpleName();

            boolean bracketsDetected = false;
            if (swfType != null) {
                if (swfType.value() != BasicType.OTHER) {
                    typeStr = "" + swfType.value();
                }
                if (swfType.count() > 0) {
                    typeStr += "[" + swfType.count();
                    if (swfType.countAdd() > 0) {
                        typeStr += " + " + swfType.countAdd();
                    }
                    typeStr += "]";
                    bracketsDetected = true;
                } else if (!swfType.countField().isEmpty()) {
                    typeStr += "[" + swfType.countField();
                    if (swfType.countAdd() > 0) {
                        typeStr += " + " + swfType.countAdd();
                    }
                    typeStr += "]";
                    bracketsDetected = true;
                }
            }

            String arrayBrackets = "";
            if (isArrayParent && !bracketsDetected) {
                if (swfArray != null) {
                    if (swfArray.count() > 0) {
                        arrayBrackets = "[" + swfArray.count() + "]";
                    } else if (!swfArray.countField().isEmpty()) {
                        arrayBrackets = "[" + swfArray.countField() + "]";
                    } else {
                        arrayBrackets = "[]";
                    }
                } else {
                    arrayBrackets = "[]";
                }
            }
            typeStr += arrayBrackets;

            return typeStr;

        }

        public String getNameType(int fieldIndex) {
            String typeStr = getType(fieldIndex);
            return getName(fieldIndex) + (typeStr != null ? " : " + typeStr : "");
        }

        public String getName(int fieldIndex) {

            SWFArray swfArray = fieldSet.get(fieldIndex).getAnnotation(SWFArray.class);

            boolean isArray = ReflectionTools.needsIndex(fieldSet.get(fieldIndex)) || swfArray != null;
            boolean isArrayParent = isArray && index == -1;

            String name = "";

            if (!isArray || isArrayParent) {
                name = fieldSet.get(fieldIndex).getName();
            } else if (swfArray != null && !isArrayParent) {
                name = swfArray.value();
            }

            if (!isArrayParent && isArray) {
                name += "[" + index + "]";
            }

            return name;
        }

        public Object getValue(int fieldIndex) {
            try {
                if (ReflectionTools.needsIndex(fieldSet.get(fieldIndex)) && (index == -1)) {
                    return ReflectionTools.getValue(obj, fieldSet.get(fieldIndex));
                }
                Object val = ReflectionTools.getValue(obj, fieldSet.get(fieldIndex), index);
                if (val == null) {
                    try {
                        Class type = fieldSet.get(fieldIndex).getType();
                        ConditionalType cond = fieldSet.get(fieldIndex).getAnnotation(ConditionalType.class);
                        if (cond != null) {
                            boolean condEnabled = false;
                            int[] tags = cond.tags();
                            if (tags != null && tags.length > 0) {
                                int tagId = tag.getId();
                                for (int i = 0; i < tags.length; i++) {
                                    if (tags[i] == tagId) {
                                        condEnabled = true;
                                        break;
                                    }
                                }
                            }

                            if (condEnabled) {
                                type = cond.type();
                            }
                        }

                        val = ReflectionTools.newInstanceOf(type);
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
            hash = 11 * hash + System.identityHashCode(this.obj);
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
            if (this.obj != other.obj) {
                return false;
            }
            /*if (!Objects.equals(this.obj, other.obj)) {
                return false;
            }*/
            if (!Objects.equals(this.fieldSet.get(FIELD_INDEX), other.fieldSet.get(FIELD_INDEX))) {
                return false;
            }
            return this.index == other.index;
        }
    }

    public static class MyTreeModel extends DefaultTreeModel {

        private final Tag mtroot;

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
                            String fullPath = parentPath + "." + cname;
                            if (fullPath.equals(dependence)) {
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
                return new FieldNode(null, this, mtroot, mtroot, filterFields(mtroot.getSwf(), this, mtroot.getClass().getSimpleName(), mtroot.getClass(), limited, mtroot.getId()).get(index), -1);
            }
            FieldNode fnode = (FieldNode) parent;
            Field field = fnode.fieldSet.get(FIELD_INDEX);
            if (ReflectionTools.needsIndex(field) && (fnode.index == -1)) { //Arrays ot Lists
                return new FieldNode(parent, this, mtroot, fnode.obj, fnode.fieldSet, index);
            }
            parent = fnode.getValue(FIELD_INDEX);
            return new FieldNode(parent, this, mtroot, parent, filterFields(mtroot.getSwf(), this, getNodePathName(fnode), parent.getClass(), limited, mtroot.getId()).get(index), -1);
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
                return filterFields(mtroot.getSwf(), this, mtroot.getClass().getSimpleName(), mtroot.getClass(), limited, mtroot.getId()).size();
            }

            FieldNode fnode = (FieldNode) parent;

            Field field = fnode.fieldSet.get(FIELD_INDEX);

            boolean isByteArray = field.getType().equals(byte[].class);

            if (hasEditor(fnode.obj, field, fnode.index) || isByteArray) {
                return 0;
            }

            if (ReflectionTools.needsIndex(field) && (fnode.index == -1)) { //Arrays or Lists
                try {
                    if (field.get(fnode.obj) == null) {
                        // todo: instantiate the (Array)List or Array to allow adding items to it
                        return 0;
                    }
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    return 0;
                }

                return ReflectionTools.getFieldSubSize(fnode.obj, field);
            }
            parent = fnode.getValue(FIELD_INDEX);

            return filterFields(mtroot.getSwf(), this, getNodePathName(fnode), parent.getClass(), limited, mtroot.getId()).size();
        }

        @Override
        public boolean isLeaf(Object node) {
            return getChildCount(node) == 0;
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
        if (editedTag == null) {
            return new DefaultTreeModel(new DefaultMutableTreeNode("root"));
        }
        return new MyTreeModel(editedTag);
    }

    @Override
    public void setEditMode(boolean edit, Tag tag) {
        if (tag == null) {
            tag = this.tag;
        }
        this.tag = tag;
        try {
            editedTag = tag == null ? null : tag.cloneTag();
        } catch (InterruptedException ex) {
            //ignore
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        if (!edit && tree.isEditing()) {
            tree.stopEditing();
        }
        tree.setEditable(edit);
        refreshTree();
    }

    @Override
    public boolean save() {
        if (tree.isEditing() && !tree.stopEditing()) {
            return false;
        }

        if (tag == null) {
            return true;
        }
        SWF swf = tag.getSwf();
        assignTag(tag, editedTag);
        tag.setModified(true);
        tag.setSwf(swf);
        if (tag instanceof Timelined) {
            ((Timelined) tag).resetTimeline();
        }
        //For example DefineButton and its DefineButtonCxForm
        if ((tag instanceof CharacterIdTag) && (!(tag instanceof CharacterTag))) {
            CharacterTag parentCharacter = swf.getCharacter(((CharacterIdTag) tag).getCharacterId());
            if (parentCharacter instanceof Timelined) {
                ((Timelined) parentCharacter).resetTimeline();
            }
        }
        swf.computeDependentCharacters();
        swf.computeDependentFrames();
        return true;
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

    /*public static String swfTypeToString(Class<?> type, SWFType swfType, SWFArray swfArray, boolean arrayHeader) {
        S
    }*/
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
        UUID uuid = field.getAnnotation(UUID.class);

        if (uuid != null) {
            return true;
        } else if (type.equals(int.class) || type.equals(Integer.class)
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
        } else if (type.equals(Amf3Value.class)) {
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

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 67 * hash + Objects.hashCode(this.fields);
            hash = 67 * hash + Objects.hashCode(this.name);
            hash = 67 * hash + Objects.hashCode(this.itemName);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final FieldSet other = (FieldSet) obj;
            if (!Objects.equals(this.name, other.name)) {
                return false;
            }
            if (!Objects.equals(this.itemName, other.itemName)) {
                return false;
            }
            return Objects.equals(this.fields, other.fields);
        }
    }

    private static List<FieldSet> filterFields(SWF swf, MyTreeModel mod, String parentPath, Class<?> cls, boolean limited, int parentTagId) {
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
                                //Hack - the field is internal in fonts and thus cannot be accessed via getNodeByPath
                                if ("strippedShapes".equals(sf)) {
                                    fieldMap.put(sf, swf.hasStrippedShapesFromFonts());
                                } else {
                                    fieldMap.put(sf, true);
                                }
                            }
                        }
                        if (!ev.eval(fieldMap, parentTagId)) {
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
            Field[] fields = cls.getFields();
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
        String countFieldName = null;
        if (swfArray != null && !swfArray.countField().isEmpty()) {
            countFieldName = swfArray.countField();
        }

        if (countFieldName != null) { //Fields with same countField must be enlarged too
            Field[] fields = obj.getClass().getDeclaredFields();
            List<Integer> sameFlds = new ArrayList<>();
            for (int f = 0; f < fields.length; f++) {
                SWFArray fieldSwfArray = fields[f].getAnnotation(SWFArray.class);
                String fieldCountFieldName = null;
                if (fieldSwfArray != null && !fieldSwfArray.countField().isEmpty()) {
                    fieldCountFieldName = fieldSwfArray.countField();
                }
                if (fieldCountFieldName != null && fieldCountFieldName.equals(countFieldName)) {
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
                    if (v instanceof HasSwfAndTag) {
                        ((HasSwfAndTag) obj).setSourceTag(editedTag);
                    }
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    //ignore
                }
            }
            try {
                //If countField exists, increment, otherwise do nothing
                Field countField = obj.getClass().getDeclaredField(countFieldName);
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

                //Hack to set CLIPACTIONRECORD parent
                if ((obj instanceof CLIPACTIONS) && (v instanceof CLIPACTIONRECORD)) {
                    ((CLIPACTIONRECORD) v).setParentClipActions((CLIPACTIONS) obj);
                }

                if (obj instanceof HasSwfAndTag) {
                    ((HasSwfAndTag) obj).setSourceTag(editedTag);
                }

            } catch (IllegalArgumentException | IllegalAccessException ex) {
                //ignore
            }
        }
        ((MyTreeModel) tree.getModel()).vchanged(new TreePath(tree.getModel().getRoot()));
        refreshTree();
    }

    public void refreshTree() {
        View.refreshTree(tree, getModel());
        for (TreeModelListener listener : modelListeners) {
            ((DefaultTreeModel) tree.getModel()).addTreeModelListener(listener);
        }
        revalidate();
        repaint();
    }

    private void removeItem(Object obj, Field field, int index) {
        SWFArray swfArray = field.getAnnotation(SWFArray.class);
        String countFieldName = null;
        if (swfArray != null && !swfArray.countField().isEmpty()) {
            countFieldName = swfArray.countField();
        }
        if (countFieldName != null) { //Fields with same countField must be removed from too
            Field[] fields = obj.getClass().getDeclaredFields();
            for (int f = 0; f < fields.length; f++) {
                SWFArray fieldSwfArray = fields[f].getAnnotation(SWFArray.class);
                String fieldCountFieldName = null;
                if (fieldSwfArray != null && !fieldSwfArray.countField().isEmpty()) {
                    fieldCountFieldName = fieldSwfArray.countField();
                }

                if (fieldCountFieldName != null && fieldCountFieldName.equals(countFieldName)) {
                    ReflectionTools.removeFromField(obj, fields[f], index);
                }
            }
            try {
                //If countField exists, decrement, otherwise do nothing
                Field countField = obj.getClass().getDeclaredField(countFieldName);
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
