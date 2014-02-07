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
import com.jpexs.decompiler.flash.gui.generictageditors.GenericTagEditor;
import com.jpexs.decompiler.flash.gui.generictageditors.NumberEditor;
import com.jpexs.decompiler.flash.gui.generictageditors.StringEditor;
import com.jpexs.decompiler.flash.gui.helpers.SpringUtilities;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.ZONEDATA;
import com.jpexs.decompiler.flash.types.ZONERECORD;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import java.awt.BorderLayout;
import java.awt.Component;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;

/**
 *
 * @author JPEXS
 */
public class GenericTagPanel extends JPanel {

    private final JEditorPane genericTagPropertiesEditorPane;
    private final JPanel genericTagPropertiesEditPanel;
    private final JScrollPane genericTagPropertiesEditorPaneScrollPanel;
    private final JScrollPane genericTagPropertiesEditPanelScrollPanel;
    private Tag tag;

    public GenericTagPanel() {
        super(new BorderLayout());

        genericTagPropertiesEditorPane = new JEditorPane() {
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return true;
            }
        };
        genericTagPropertiesEditorPane.setEditable(false);
        genericTagPropertiesEditorPaneScrollPanel = new JScrollPane(genericTagPropertiesEditorPane);
        add(genericTagPropertiesEditorPaneScrollPanel);

        genericTagPropertiesEditPanel = new JPanel();
        genericTagPropertiesEditPanel.setLayout(new SpringLayout());
        genericTagPropertiesEditPanelScrollPanel = new JScrollPane(genericTagPropertiesEditPanel);
    }

    public void clear() {
        tag = null;
        genericTagPropertiesEditPanel.removeAll();
    }

    public void setEditMode(boolean edit) {
        if (edit) {
            remove(genericTagPropertiesEditorPaneScrollPanel);
            add(genericTagPropertiesEditPanelScrollPanel);
        } else {
            remove(genericTagPropertiesEditPanelScrollPanel);
            add(genericTagPropertiesEditorPaneScrollPanel);
        }
        repaint();
    }

    public void setTagText(Tag tag) {
        StringBuilder sb = new StringBuilder();
        Field[] fields = tag.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                sb.append(field.getName()).append(": ").append(field.get(tag));
                sb.append(GraphTextWriter.NEW_LINE);
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                Logger.getLogger(GenericTagPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        genericTagPropertiesEditorPane.setText(sb.toString());
        genericTagPropertiesEditorPane.setCaretPosition(0);
    }

    public void generateEditControls(Tag tag) {
        genericTagPropertiesEditPanel.removeAll();
        this.tag = tag;
        int propCount = generateEditControlsRecursive(tag, "");
        //Lay out the panel.
        SpringUtilities.makeCompactGrid(genericTagPropertiesEditPanel,
                propCount, 2, //rows, cols
                6, 6, //initX, initY
                6, 6);       //xPad, yPad        
        repaint();
    }

    private int generateEditControlsRecursive(Object obj, String parent) {
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
                if (value instanceof Iterable) {
                    int i = 0;
                    for (Object obj1 : (Iterable) value) {
                        propCount += generateEditControlsRecursive(obj1, name + "[" + i++ + "]");
                    }
                } else if (value instanceof Object[]) {
                    Object[] objArr = (Object[]) value;
                    for (int i = 0; i < objArr.length; i++) {
                        propCount += generateEditControlsRecursive(objArr[i], name + "[" + i + "]");
                    }
                } else {
                    propCount += addEditor(name, obj, field);
                }
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                Logger.getLogger(GenericTagPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return propCount;
    }

    private int addEditor(String name, Object obj, Field field) throws IllegalArgumentException, IllegalAccessException {
        Component editor;
        Class<?> type = field.getType();
        SWFType swfType = field.getAnnotation(SWFType.class);
        BasicType basicType = swfType == null ? BasicType.NONE : swfType.value();
        if (type.equals(int.class) || type.equals(Integer.class) ||
            type.equals(short.class) || type.equals(Short.class) ||
            type.equals(long.class) || type.equals(Long.class) ||
            type.equals(double.class) || type.equals(Double.class) || 
            type.equals(float.class) || type.equals(Float.class)) {
            editor = new NumberEditor(obj, field, basicType);
        } else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
            editor = new BooleanEditor(obj, field);
        } else if (type.equals(String.class)) {
            editor = new StringEditor(obj, field);
        } else if (type.equals(RECT.class)
                || type.equals(RGB.class)
                || type.equals(ZONERECORD.class)
                || type.equals(ZONEDATA.class)) {
            // todo: add other swf releated classes
            return generateEditControlsRecursive(field.get(obj), field.getName() + ".");
        } else {
            JTextArea textArea = new JTextArea(field.get(obj).toString());
            textArea.setLineWrap(true);
            textArea.setEditable(false);
            editor = textArea;
        }

        JLabel label = new JLabel(name + ":", JLabel.TRAILING);
        genericTagPropertiesEditPanel.add(label);
        label.setLabelFor(editor);
        genericTagPropertiesEditPanel.add(editor);
        return 1;
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
}
