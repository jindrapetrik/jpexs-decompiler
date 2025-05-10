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
package com.jpexs.decompiler.flash.gui.generictageditors;

import com.jpexs.decompiler.flash.easygui.properties.PropertyEditor;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.ReflectionTools;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.lang.reflect.Field;
import java.util.Objects;
import javax.swing.JTextField;

/**
 * @author JPEXS
 */
public class FloatEditor extends JTextField implements GenericTagEditor {

    private final Object obj;

    private final Field field;

    private final int index;

    private final Class<?> type;

    private String fieldName;
    
    private ValueNormalizer normalizer;
    
    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

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
        reset();
    }

    @Override
    public void reset() {
        try {
            Object val = ReflectionTools.getValue(obj, field, index);
            if (normalizer != null) {
                val = normalizer.toViewValue(val);
            }
            setText(val == null ? "" : EcmaScript.toString(val));            
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
            String newValue = getText();
            if (Objects.equals(oldValue, newValue)) {
                return false;
            }
            Object val;
            if (type.equals(double.class) || type.equals(Double.class)) {
                val = Double.valueOf(getText());
            } else {
                val = Float.valueOf(getText());
            }
            if (normalizer != null) {
                val = normalizer.toFieldValue(val);
            }
            ReflectionTools.setValue(obj, field, index, val);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            // ignore
        }
        return true;
    }

    @Override
    public void addChangeListener(final ChangeListener l) {
        final PropertyEditor t = this;
        addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                l.change(t);
            }

        });
    }

    @Override
    public Object getChangedValue() {
        return getText();
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
}
