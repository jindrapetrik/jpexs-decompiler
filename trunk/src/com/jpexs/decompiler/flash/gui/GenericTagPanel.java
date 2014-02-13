/*
 *  Copyright (C) 2010-2014 JPEXS
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

import com.jpexs.decompiler.flash.gui.generictageditors.BooleanEditor;
import com.jpexs.decompiler.flash.gui.generictageditors.ChangeListener;
import com.jpexs.decompiler.flash.gui.generictageditors.GenericTagEditor;
import com.jpexs.decompiler.flash.gui.generictageditors.NumberEditor;
import com.jpexs.decompiler.flash.gui.generictageditors.ReflectionTools;
import com.jpexs.decompiler.flash.gui.generictageditors.StringEditor;
import com.jpexs.decompiler.flash.gui.helpers.SpringUtilities;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.types.annotations.Calculated;
import com.jpexs.decompiler.flash.types.annotations.Conditional;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.flash.types.annotations.Optional;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.annotations.parser.ConditionEvaluator;
import com.jpexs.decompiler.flash.types.annotations.parser.ParseException;
import java.awt.BorderLayout;
import java.awt.Component;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;

/**
 *
 * @author JPEXS
 */
public class GenericTagPanel extends JPanel implements ChangeListener {

    private final JEditorPane genericTagPropertiesEditorPane;
    private final JPanel genericTagPropertiesEditPanel;
    private final JScrollPane genericTagPropertiesEditorPaneScrollPanel;
    private final JScrollPane genericTagPropertiesEditPanelScrollPanel;
    private Tag tag;
    private List<String> keys = new ArrayList<>();
    private Map<String, GenericTagEditor> editors = new HashMap<>();
    private Map<String, Component> labels = new HashMap<>();
    private Map<String, Component> types = new HashMap<>();
    private Map<String, List<Field>> fieldPaths = new HashMap<>();
    private Map<String, List<Integer>> fieldIndices = new HashMap<>();
    private HeaderLabel hdr;

    public GenericTagPanel() {
        super(new BorderLayout());

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
        genericTagPropertiesEditPanelScrollPanel = new JScrollPane(genericTagPropertiesEditPanel);
    }

    public void clear() {
        tag = null;
        editors.clear();
        fieldPaths.clear();
        fieldIndices.clear();
        labels.clear();
        types.clear();
        keys.clear();
        genericTagPropertiesEditPanel.removeAll();
        genericTagPropertiesEditPanel.setSize(0, 0);
    }

    public void setEditMode(boolean edit) {
        if (edit) {
            remove(genericTagPropertiesEditorPaneScrollPanel);
            add(genericTagPropertiesEditPanelScrollPanel, BorderLayout.CENTER);
        } else {
            genericTagPropertiesEditPanel.removeAll();
            genericTagPropertiesEditPanel.setSize(0, 0);
            remove(genericTagPropertiesEditPanelScrollPanel);
            add(genericTagPropertiesEditorPaneScrollPanel, BorderLayout.CENTER);
        }
        repaint();
    }

    public void setTagText(Tag tag) {
        clear();
        generateEditControls(tag, true);
        String val = "";
        for (String key : keys) {
            GenericTagEditor ed = editors.get(key);
            if (((Component) ed).isVisible()) {
                val += key + " : " + ed.getChangedValue() + "\r\n";
            }
        }
        genericTagPropertiesEditorPane.setText(val);
        genericTagPropertiesEditorPane.setCaretPosition(0);
        hdr.setText(tag.toString());       
    }

    public void generateEditControls(Tag tag, boolean readonly) {
        clear();
        this.tag = tag;
        generateEditControlsRecursive(tag, "", new ArrayList<Field>(),new ArrayList<Integer>(), readonly);
        change(null);
    }

    private void relayout(int propCount) {
        //Lay out the panel.
        SpringUtilities.makeCompactGrid(genericTagPropertiesEditPanel,
                propCount, 3, //rows, cols
                6, 6, //initX, initY
                6, 6);       //xPad, yPad        
        repaint();
    }

    private int generateEditControlsRecursive(Object obj, String parent, List<Field> parentFields, List<Integer> parentIndices, boolean readonly) {
        if (obj == null) {
            return 0;
        }
        Field[] fields = obj.getClass().getDeclaredFields();
        int propCount = 0;
        for (Field field : fields) {
            try {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                field.setAccessible(true);
                String name = parent + field.getName();
                Object value = field.get(obj);
                if (List.class.isAssignableFrom(field.getType())) {
                    if (value != null) {
                        int i = 0;
                        for (Object obj1 : (Iterable) value) {
                            propCount += addEditor(name + "[" + i + "]", obj, field, i, obj1.getClass(), obj1, parentFields,parentIndices, readonly);
                            i++;
                        }
                    }
                } else if (field.getType().isArray()) {
                    if (value != null) {
                        for (int i = 0; i < Array.getLength(value); i++) {
                            Object item = Array.get(value, i);
                            propCount += addEditor(name + "[" + i + "]", obj, field, i, item.getClass(), item, parentFields,parentIndices, readonly);
                        }
                    }
                } else {
                    propCount += addEditor(name, obj, field, 0, field.getType(), value, parentFields,parentIndices, readonly);
                }
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                Logger.getLogger(GenericTagPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return propCount;
    }

    private int addEditor(String name, Object obj, Field field, int index, Class<?> type, Object value, List<Field> parentList,List<Integer> parentIndices, boolean readonly) throws IllegalArgumentException, IllegalAccessException {
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
            editor = new StringEditor(name, obj, field, index, type);
        } else {
            if (value == null) {
                if (readonly) {
                    return 0;
                }
                Optional opt = field.getAnnotation(Optional.class);
                if (opt == null) {
                    try {
                        value = field.getType().newInstance();
                        field.set(obj, value);
                    } catch (InstantiationException | IllegalAccessException ex) {
                        Logger.getLogger(GenericTagPanel.class.getName()).log(Level.SEVERE, null, ex);
                        return 0;
                    }
                } else {
                    return 0;
                }
            }
            return generateEditControlsRecursive(value, name + ".", parList,parIndices, readonly);
        }
        if (editor instanceof GenericTagEditor) {
            GenericTagEditor ce = (GenericTagEditor) editor;
            ce.addChangeListener(this);
            editors.put(name, ce);
            fieldPaths.put(name, parList);
            fieldIndices.put(name,parIndices);
        }

        JLabel label = new JLabel(name + ":", JLabel.TRAILING);
        genericTagPropertiesEditPanel.add(label);
        label.setLabelFor(editor);
        labels.put(name, label);
        genericTagPropertiesEditPanel.add(editor);
        JLabel typeLabel = new JLabel(swfTypeToString(swfType), JLabel.TRAILING);
        genericTagPropertiesEditPanel.add(typeLabel);
        types.put(name, typeLabel);
        keys.add(name);

        return 1;
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
        } else if (!swfType.countField().equals("")) {
            result += "[" + swfType.countField();
            if (swfType.countAdd() > 0) {
                result += " + " + swfType.countAdd();
            }
            result += "]";
        }
        return result;
    }

    public void save() {
        for (Object component : genericTagPropertiesEditPanel.getComponents()) {
            if (component instanceof GenericTagEditor) {
                ((GenericTagEditor) component).save();
            }
        }
        tag.setModified(true);
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
            for (int i=0;i<path.size();i++) {
                Field f = path.get(i);
                int index=indices.get(i);
                String par = p;
                if (!p.equals("")) {
                    p += ".";
                }
                p += f.getName();
                if(ReflectionTools.needsIndex(f)){
                    p+="["+index+"]";
                }
                Conditional cond = f.getAnnotation(Conditional.class);
                if (cond != null) {
                    ConditionEvaluator ev = new ConditionEvaluator(cond);

                    try {
                        Set<String> fieldNames = ev.getFields();
                        Map<String, Boolean> fields = new HashMap<>();
                        for (String fld : fieldNames) {
                            String ckey = "";
                            if (!par.equals("")) {
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
                    } catch (ParseException ex) {
                        Logger.getLogger(GenericTagPanel.class.getName()).log(Level.SEVERE, "Invalid condition", ex);
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
            Component dependentEditor = (Component) editors.get(key);
            Component dependentLabel = labels.get(key);
            Component dependentTypeLabel = types.get(key);
            if (dependentEditor.isVisible()) {
                genericTagPropertiesEditPanel.add(dependentLabel);
                genericTagPropertiesEditPanel.add(((Component) dependentEditor));
                genericTagPropertiesEditPanel.add(dependentTypeLabel);
                propCount++;
            }
        }
        relayout(propCount);
    }
}
