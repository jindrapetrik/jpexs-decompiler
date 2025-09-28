/*
 *  Copyright (C) 2010-2025 JPEXS
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
package com.jpexs.decompiler.flash.gui.generictageditors;

import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.ReflectionTools;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * @author JPEXS
 */
public class FloatEditor extends JPanel implements GenericTagEditor {

    private final Object obj;

    private final Field field;

    private final int index;

    private final Class<?> type;

    private String fieldName;

    private ValueNormalizer normalizer;

    private Field linkedField;

    private boolean linkEnabled;

    private List<ChangeListener> listeners = new ArrayList<>();

    private JTextField textField;
    private JLabel linkLabel;

    @Override
    public Dimension getPreferredSize() {
        Dimension ret = super.getPreferredSize();
        ret.width = 300;
        return ret;
    }

    @Override
    public BaselineResizeBehavior getBaselineResizeBehavior() {
        return Component.BaselineResizeBehavior.CONSTANT_ASCENT;
    }

    @Override
    public int getBaseline(int width, int height) {
        return 0;
    }

    public FloatEditor(String fieldName, Object obj, Field field, int index, Class<?> type) {
        this.obj = obj;
        this.field = field;
        this.index = index;
        this.type = type;
        this.fieldName = fieldName;

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                for (ChangeListener l : listeners) {
                    l.change(FloatEditor.this);
                }
            }
        });

        textField = new JTextField() {
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return true;
            }
        };

        textField.setBorder(BorderFactory.createEmptyBorder());
        
        setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        textField.setPreferredSize(new Dimension(50, textField.getPreferredSize().height));
        textField.setMaximumSize(textField.getPreferredSize());
                        
        add(textField);
        linkLabel = new JLabel(View.getIcon("link16"));
        add(linkLabel);
        
        linkLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                boolean newValue = !linkEnabled;
                setLinkEnabled(newValue);
                for (ChangeListener l : listeners) {
                    l.linkChanged(newValue);
                }           
                repaint();
            }            
        });
        
        linkLabel.setVisible(false);

        reset();        
    }

    @Override
    public void reset() {
        try {
            Object val = ReflectionTools.getValue(obj, field, index);
            if (normalizer != null) {
                val = normalizer.toViewValue(val);
            }
            textField.setText(val == null ? "" : EcmaScript.toString(val));
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            // ignore
        }
    }

    @Override
    public boolean save() {
        try {
            Object oldFieldValue = ReflectionTools.getValue(obj, field, index);
            if (normalizer != null) {
                oldFieldValue = normalizer.toViewValue(oldFieldValue);
            }
            String oldValue = (String) EcmaScript.toString(oldFieldValue);
            String newValue = textField.getText();
            if (Objects.equals(oldValue, newValue)) {
                return false;
            }
            Object val;
            if (type.equals(double.class) || type.equals(Double.class)) {
                val = Double.valueOf(textField.getText());
            } else {
                val = Float.valueOf(textField.getText());
            }
            if (normalizer != null) {
                val = normalizer.toFieldValue(val);
            }

            ReflectionTools.setValue(obj, field, index, val);

            if (linkedField != null && linkEnabled) {
                Object linkedFieldValue = ReflectionTools.getValue(obj, linkedField);
                Object newLinkedFieldValue = null;
                if (oldFieldValue instanceof Double) {
                    Double v = (Double) oldFieldValue;
                    Double v2 = (Double) val;
                    Double vL = (Double) linkedFieldValue;
                    newLinkedFieldValue = v == 0.0 ? v2 : v2 * vL / v;
                } else if (oldFieldValue instanceof Float) {
                    Float v = (Float) oldFieldValue;
                    Float v2 = (Float) val;
                    Float vL = (Float) linkedFieldValue;
                    newLinkedFieldValue = v == 0f ? v2 : v2 * vL / v;
                }
                ReflectionTools.setValue(obj, linkedField, -1, newLinkedFieldValue);
            }
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            // ignore
        }
        return true;
    }

    @Override
    public void addChangeListener(final ChangeListener l) {
        listeners.add(l);
    }

    @Override
    public Object getChangedValue() {
        return textField.getText();
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public Field getField() {
        return field;
    }

    @Override
    public String getReadOnlyValue() {
        return Helper.escapeHTML(getChangedValue().toString());
    }

    @Override
    public void added() {

    }

    @Override
    public void validateValue() {
    }

    @Override
    public Object getObject() {
        return obj;
    }

    @Override
    public void setValueNormalizer(ValueNormalizer normalizer) {
        this.normalizer = normalizer;
        reset();
    }

    public void setLinkedField(Field linkedField) {
        this.linkedField = linkedField;
        linkLabel.setVisible(true);
    }

    public void setLinkEnabled(boolean enabled) {
        this.linkEnabled = enabled;
        linkLabel.setIcon(View.getIcon(enabled ? "link16" : "linkbreak16"));
    }

    public boolean isLinkEnabled() {
        return linkEnabled;
    }
}
