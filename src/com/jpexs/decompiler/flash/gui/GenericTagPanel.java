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
import com.jpexs.decompiler.flash.gui.generictageditors.ChangeListener;
import com.jpexs.decompiler.flash.gui.generictageditors.ColorEditor;
import com.jpexs.decompiler.flash.gui.generictageditors.GenericTagEditor;
import com.jpexs.decompiler.flash.gui.generictageditors.NumberEditor;
import com.jpexs.decompiler.flash.gui.generictageditors.StringEditor;
import com.jpexs.decompiler.flash.gui.helpers.SpringUtilities;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.types.ARGB;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.annotations.Calculated;
import com.jpexs.decompiler.flash.types.annotations.Conditional;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.flash.types.annotations.Multiline;
import com.jpexs.decompiler.flash.types.annotations.Optional;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.annotations.parser.AnnotationParseException;
import com.jpexs.decompiler.flash.types.annotations.parser.ConditionEvaluator;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.ReflectionTools;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;

/**
 * Old Generic Tag editor
 *
 * @author JPEXS
 */
public class GenericTagPanel extends JPanel implements ChangeListener {

    private static final Logger logger = Logger.getLogger(GenericTagPanel.class.getName());

    protected final MainPanel mainPanel;

    private final JEditorPane genericTagPropertiesEditorPane;

    private final JPanel genericTagPropertiesEditPanel;

    private final JScrollPane genericTagPropertiesEditorPaneScrollPanel;

    private final JScrollPane genericTagPropertiesEditPanelScrollPanel;

    private Tag tag;

    private Tag editedTag;

    private List<String> keys = new ArrayList<>();

    private Map<String, GenericTagEditor> editors = new HashMap<>();

    private Map<String, Component> labels = new HashMap<>();

    private Map<String, Component> types = new HashMap<>();

    private Map<String, List<Field>> fieldPaths = new HashMap<>();

    private Map<String, List<Integer>> fieldIndices = new HashMap<>();

    private HeaderLabel hdr;

    private Set<String> addKeys = new HashSet<>();

    private Map<String, Component> addButtons = new HashMap<>();

    private Map<String, Component> removeButtons = new HashMap<>();

    public GenericTagPanel(MainPanel mainPanel) {
        super(new BorderLayout());

        this.mainPanel = mainPanel;
        hdr = new HeaderLabel("");
        add(hdr, BorderLayout.NORTH);
        genericTagPropertiesEditorPane = new JEditorPane() {
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return true;
            }
        };
        genericTagPropertiesEditorPane.setEditable(false);
        genericTagPropertiesEditorPaneScrollPanel = new JScrollPane(genericTagPropertiesEditorPane);
        add(genericTagPropertiesEditorPaneScrollPanel, BorderLayout.CENTER);

        genericTagPropertiesEditPanel = new JPanel();
        genericTagPropertiesEditPanel.setLayout(new SpringLayout());
        JPanel edPanel = new JPanel(new BorderLayout());
        edPanel.add(genericTagPropertiesEditPanel, BorderLayout.NORTH);
        genericTagPropertiesEditPanelScrollPanel = new JScrollPane(edPanel);
    }

    public void clear() {
        editors.clear();
        fieldPaths.clear();
        fieldIndices.clear();
        labels.clear();
        types.clear();
        keys.clear();
        addKeys.clear();
        addButtons.clear();
        removeButtons.clear();
        genericTagPropertiesEditPanel.removeAll();
        genericTagPropertiesEditPanel.setSize(0, 0);
    }

    public void setEditMode(boolean edit, Tag tag) {
        if (tag == null) {
            tag = this.tag;
        }

        this.tag = tag;
        this.editedTag = Helper.deepCopy(tag);
        generateEditControls(editedTag, !edit);

        if (edit) {
            remove(genericTagPropertiesEditorPaneScrollPanel);
            add(genericTagPropertiesEditPanelScrollPanel, BorderLayout.CENTER);
        } else {
            genericTagPropertiesEditPanel.removeAll();
            genericTagPropertiesEditPanel.setSize(0, 0);
            remove(genericTagPropertiesEditPanelScrollPanel);
            add(genericTagPropertiesEditorPaneScrollPanel, BorderLayout.CENTER);
            setTagText(this.tag);
        }
        revalidate();
        repaint();
    }

    private void setTagText(Tag tag) {
        clear();
        generateEditControls(tag, true);
        StringBuilder val = new StringBuilder();
        for (String key : keys) {
            GenericTagEditor ed = editors.get(key);
            if (((Component) ed).isVisible()) {
                val.append(key).append(" : ").append(ed.getReadOnlyValue()).append("<br>");
            }
        }
        //HTML for colors:
        val.insert(0, "<html>").append("</html>");
        genericTagPropertiesEditorPane.setContentType("text/html");
        genericTagPropertiesEditorPane.setText(val.toString());
        genericTagPropertiesEditorPane.setCaretPosition(0);
        hdr.setText(tag.toString());
    }

    private void generateEditControls(Tag tag, boolean readonly) {
        clear();
        generateEditControlsRecursive(tag, "", new ArrayList<>(), new ArrayList<>(), readonly);
        change(null);
    }

    private void relayout(int propCount) {
        //Lay out the panel.
        SpringUtilities.makeCompactGrid(genericTagPropertiesEditPanel,
                propCount, 3, //rows, cols
                6, 6, //initX, initY
                6, 6);       //xPad, yPad
        revalidate();
        repaint();
    }

    private int generateEditControlsRecursive(final Object obj, String parent, List<Field> parentFields, List<Integer> parentIndices, boolean readonly) {
        if (obj == null) {
            return 0;
        }
        Field[] fields = obj.getClass().getDeclaredFields();
        int propCount = 0;
        for (final Field field : fields) {
            try {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                field.setAccessible(true);
                String name = parent + field.getName();
                final Object value = field.get(obj);
                if (List.class.isAssignableFrom(field.getType())) {
                    if (value != null) {
                        int i = 0;
                        for (Object obj1 : (Iterable) value) {
                            final String subname = name + "[" + i + "]";
                            propCount += addEditor(subname, obj, field, i, obj1.getClass(), obj1, parentFields, parentIndices, readonly);
                            final int fi = i;
                            i++;
                            JButton removeButton = new JButton(View.getIcon("close16"));
                            removeButton.addActionListener(new ActionListener() {

                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    removeItem(obj, field, fi);
                                }
                            });
                            removeButtons.put(subname, removeButton);
                        }
                    }
                } else if (field.getType().isArray()) {
                    if (value != null) {
                        for (int i = 0; i < Array.getLength(value); i++) {
                            Object item = Array.get(value, i);
                            String subname = name + "[" + i + "]";
                            propCount += addEditor(subname, obj, field, i, item.getClass(), item, parentFields, parentIndices, readonly);
                            final int fi = i;
                            JButton removeButton = new JButton(View.getIcon("close16"));
                            removeButton.addActionListener(new ActionListener() {

                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    removeItem(obj, field, fi);
                                }
                            });
                            removeButtons.put(subname, removeButton);
                        }
                    }
                } else {
                    propCount += addEditor(name, obj, field, 0, field.getType(), value, parentFields, parentIndices, readonly);
                }
                if (ReflectionTools.needsIndex(field) && !readonly && !field.getName().equals("clipActionRecords")) { //No clip actions, sorry
                    JButton addButton = new JButton(View.getIcon("add16"));
                    addButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            addItem(obj, field);
                        }
                    });
                    name += "[]";

                    List<Field> parList = new ArrayList<>(parentFields);
                    parList.add(field);
                    fieldPaths.put(name, parList);

                    List<Integer> parIndices = new ArrayList<>(parentIndices);
                    parIndices.add(0);
                    fieldIndices.put(name, parIndices);

                    addRow(name, addButton, field);
                    addKeys.add(name);
                    addButtons.put(name, addButton);
                }
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
        return propCount;
    }

    private void removeItem(Object obj, Field field, int index) {
        final JScrollBar sb = genericTagPropertiesEditPanelScrollPanel.getVerticalScrollBar();
        final int val = sb.getValue(); //save scroll top
        SWFType swfType = field.getAnnotation(SWFType.class);
        if (swfType != null && !swfType.countField().isEmpty()) { //Fields with same countField must be removed from too
            Field fields[] = obj.getClass().getDeclaredFields();
            for (int f = 0; f < fields.length; f++) {
                SWFType fieldSwfType = fields[f].getAnnotation(SWFType.class);
                if (fieldSwfType != null && fieldSwfType.countField().equals(swfType.countField())) {
                    ReflectionTools.removeFromField(obj, fields[f], index);
                }
            }
            try {
                //If countField exists, decrement, otherwise do nothing
                Field countField = obj.getClass().getDeclaredField(swfType.countField());
                int cnt = countField.getInt(obj);
                cnt--;
                countField.setInt(obj, cnt);
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                //ignored
            }
        } else {
            ReflectionTools.removeFromField(obj, field, index);
        }

        generateEditControls(editedTag, false);

        //Restore scroll top after some time. TODO: Handle this better. I don't know how :-(.
        new Thread() {

            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }

                View.execInEventDispatch(() -> {
                    genericTagPropertiesEditPanelScrollPanel.getVerticalScrollBar().setValue(val);
                });
            }

        }.start();
        revalidate();
        repaint();
    }

    private void addItem(Object obj, Field field) {
        final JScrollBar sb = genericTagPropertiesEditPanelScrollPanel.getVerticalScrollBar();
        final int val = sb.getValue(); //save scroll top
        SWFType swfType = field.getAnnotation(SWFType.class);
        if (swfType != null && !swfType.countField().isEmpty()) { //Fields with same countField must be enlarged too
            Field fields[] = obj.getClass().getDeclaredFields();
            for (int f = 0; f < fields.length; f++) {
                SWFType fieldSwfType = fields[f].getAnnotation(SWFType.class);
                if (fieldSwfType != null && fieldSwfType.countField().equals(swfType.countField())) {
                    ReflectionTools.addToField(obj, fields[f], ReflectionTools.getFieldSubSize(obj, fields[f]), true, null);
                }
            }
            try {
                //If countField exists, increment, otherwise do nothing
                Field countField = obj.getClass().getDeclaredField(swfType.countField());
                int cnt = countField.getInt(obj);
                cnt++;
                countField.setInt(obj, cnt);
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                //ignored
            }
        } else {
            ReflectionTools.addToField(obj, field, ReflectionTools.getFieldSubSize(obj, field), true, null);
        }
        generateEditControls(editedTag, false);

        //Restore scroll top after some time. TODO: Handle this better. I don't know how :-(.
        new Thread() {

            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }

                View.execInEventDispatch(() -> {
                    genericTagPropertiesEditPanelScrollPanel.getVerticalScrollBar().setValue(val);
                });
            }

        }.start();
        revalidate();
        repaint();
    }

    private int addEditor(String name, Object obj, Field field, int index, Class<?> type, Object value, List<Field> parentList, List<Integer> parentIndices, boolean readonly) throws IllegalArgumentException, IllegalAccessException {
        Calculated calculated = field.getAnnotation(Calculated.class);
        if (calculated != null) {
            return 0;
        }
        List<Field> parList = new ArrayList<>(parentList);
        parList.add(field);

        List<Integer> parIndices = new ArrayList<>(parentIndices);
        parIndices.add(index);
        Internal inter = field.getAnnotation(Internal.class);
        if (inter != null) {
            return 0;
        }
        SWFType swfType = field.getAnnotation(SWFType.class);
        Multiline multiline = field.getAnnotation(Multiline.class);
        Component editor;
        if (type.equals(int.class) || type.equals(Integer.class)
                || type.equals(short.class) || type.equals(Short.class)
                || type.equals(long.class) || type.equals(Long.class)
                || type.equals(double.class) || type.equals(Double.class)
                || type.equals(float.class) || type.equals(Float.class)) {
            editor = new NumberEditor(name, obj, field, index, type, swfType);
        } else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
            editor = new BooleanEditor(name, obj, field, index, type);
        } else if (type.equals(String.class)) {
            editor = new StringEditor(name, obj, field, index, type, multiline != null);
        } else if (type.equals(RGB.class) || type.equals(RGBA.class) || type.equals(ARGB.class)) {
            editor = new ColorEditor(name, obj, field, index, type);
        } else if (type.equals(ByteArrayRange.class)) {
            editor = new BinaryDataEditor(mainPanel, name, obj, field, index, type);
        } else {
            if (value == null) {
                if (readonly) {
                    return 0;
                }
                Optional opt = field.getAnnotation(Optional.class);
                if (opt == null) {
                    try {
                        value = ReflectionTools.newInstanceOf(field.getType());
                        field.set(obj, value);
                    } catch (InstantiationException | IllegalAccessException ex) {
                        logger.log(Level.SEVERE, null, ex);
                        return 0;
                    }
                } else {
                    return 0;
                }
            }
            return generateEditControlsRecursive(value, name + ".", parList, parIndices, readonly);
        }
        if (editor instanceof GenericTagEditor) {
            GenericTagEditor ce = (GenericTagEditor) editor;
            ce.addChangeListener(this);
            editors.put(name, ce);
            fieldPaths.put(name, parList);
            fieldIndices.put(name, parIndices);
            addRow(name, editor, field);
        }
        return 1;
    }

    private void addRow(String name, Component editor, Field field) {
        JLabel label = new JLabel(name + ":", JLabel.TRAILING);
        label.setVerticalAlignment(JLabel.TOP);
        genericTagPropertiesEditPanel.add(label);
        label.setLabelFor(editor);
        labels.put(name, label);
        genericTagPropertiesEditPanel.add(editor);
        JLabel typeLabel = new JLabel(swfTypeToString(field.getAnnotation(SWFType.class)), JLabel.TRAILING);
        typeLabel.setVerticalAlignment(JLabel.TOP);
        genericTagPropertiesEditPanel.add(typeLabel);
        types.put(name, typeLabel);
        keys.add(name);
    }

    public String swfTypeToString(SWFType swfType) {
        if (swfType == null) {
            return null;
        }
        String result = swfType.value().toString();
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
        return result;
    }

    private void assignTag(Tag t, Tag assigned) {
        if (t.getClass() != assigned.getClass()) {
            return;
        }
        for (Field f : t.getClass().getDeclaredFields()) {
            if ((f.getModifiers() & Modifier.FINAL) == Modifier.FINAL) {
                continue;
            }
            if ((f.getModifiers() & Modifier.STATIC) == Modifier.STATIC) {
                continue;
            }
            try {
                f.set(t, f.get(assigned));
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
    }

    public void save() {
        for (Object component : genericTagPropertiesEditPanel.getComponents()) {
            if (component instanceof GenericTagEditor) {
                ((GenericTagEditor) component).save();
            }
        }
        SWF swf = tag.getSwf();
        assignTag(tag, editedTag);
        tag.setModified(true);
        tag.setSwf(swf);
        setTagText(tag);
    }

    public Tag getTag() {
        return tag;
    }

    @Override
    public void change(GenericTagEditor ed) {
        for (String key : editors.keySet()) {
            GenericTagEditor dependentEditor = editors.get(key);
            Component dependentLabel = labels.get(key);
            Component dependentTypeLabel = types.get(key);
            List<Field> path = fieldPaths.get(key);
            List<Integer> indices = fieldIndices.get(key);
            String p = "";
            boolean conditionMet = true;
            for (int i = 0; i < path.size(); i++) {
                Field f = path.get(i);
                int index = indices.get(i);
                String par = p;
                if (!p.isEmpty()) {
                    p += ".";
                }
                p += f.getName();
                if (ReflectionTools.needsIndex(f)) {
                    p += "[" + index + "]";
                }
                Conditional cond = f.getAnnotation(Conditional.class);
                if (cond != null) {
                    ConditionEvaluator ev = new ConditionEvaluator(cond);

                    try {
                        Set<String> fieldNames = ev.getFields();
                        Map<String, Boolean> fields = new HashMap<>();
                        for (String fld : fieldNames) {
                            String ckey = "";
                            if (!par.isEmpty()) {
                                ckey = par + ".";
                            }
                            ckey += fld;
                            if (editors.containsKey(ckey)) {
                                GenericTagEditor editor = editors.get(ckey);
                                Object val = editor.getChangedValue();
                                fields.put(fld, true);
                                if (val instanceof Boolean) {
                                    fields.put(fld, (Boolean) val);
                                }
                            }
                        }
                        boolean ok = ev.eval(fields);
                        if (conditionMet) {
                            conditionMet = ok;
                        }
                        ((Component) dependentEditor).setVisible(conditionMet);
                        dependentLabel.setVisible(conditionMet);
                        dependentTypeLabel.setVisible(conditionMet);
                    } catch (AnnotationParseException ex) {
                        logger.log(Level.SEVERE, "Invalid condition", ex);
                    }
                }
                if (!conditionMet) {
                    break;
                }
            }
        }
        genericTagPropertiesEditPanel.removeAll();
        genericTagPropertiesEditPanel.setSize(0, 0);
        int propCount = 0;
        for (String key : keys) {

            Component dependentEditor;
            if (addKeys.contains(key)) {
                dependentEditor = addButtons.get(key);
            } else if (removeButtons.containsKey(key)) { //It's array/list, add remove button
                JPanel editRemPanel = new JPanel(new BorderLayout());
                editRemPanel.add((Component) editors.get(key), BorderLayout.CENTER);
                editRemPanel.add(removeButtons.get(key), BorderLayout.EAST);
                dependentEditor = editRemPanel;
            } else {
                dependentEditor = (Component) editors.get(key);
            }
            Component dependentLabel = labels.get(key);
            Component dependentTypeLabel = types.get(key);
            if (dependentEditor.isVisible()) {
                genericTagPropertiesEditPanel.add(dependentLabel);
                genericTagPropertiesEditPanel.add(((Component) dependentEditor));
                genericTagPropertiesEditPanel.add(dependentTypeLabel);
                propCount++;
            }
        }
        /*genericTagPropertiesEditPanel.add(new JPanel());
         genericTagPropertiesEditPanel.add(new JPanel());
         genericTagPropertiesEditPanel.add(new JPanel());*/
        relayout(propCount /*+ 1*/);
    }
}
