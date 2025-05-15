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

import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.ReflectionTools;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.lang.reflect.Field;
import java.util.Objects;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.DefaultFormatter;

/**
 * @author JPEXS
 */
public class NumberEditor extends JPanel implements GenericTagEditor {

    private final Object obj;

    private final Field field;

    private final int index;

    private final Class<?> type;

    private final SWFType swfType;

    private String fieldName;

    private ValueNormalizer normalizer;

    private JSpinner spinner;

    @Override
    public BaselineResizeBehavior getBaselineResizeBehavior() {
        return Component.BaselineResizeBehavior.CONSTANT_ASCENT;
    }

    @Override
    public int getBaseline(int width, int height) {
        return 0;
    }

    @Override
    public void added() {

    }

    public NumberEditor(String fieldName, Object obj, Field field, int index, Class<?> type, SWFType swfType) {
        setSize(100, getSize().height);
        setMaximumSize(getSize());
        this.obj = obj;
        this.field = field;
        this.index = index;
        this.type = type;
        this.swfType = swfType;
        this.fieldName = fieldName;
        spinner = new JSpinner();

        setLayout(new BorderLayout());
        add(spinner, BorderLayout.WEST);
        setOpaque(false);

        reset();
        ((JSpinner.NumberEditor) spinner.getEditor()).getFormat().setGroupingUsed(false);
        JFormattedTextField jtf = ((JSpinner.NumberEditor) spinner.getEditor()).getTextField();
        DefaultFormatter formatter = (DefaultFormatter) jtf.getFormatter();
        formatter.setCommitsOnValidEdit(true);

    }

    @Override
    public void reset() {        
        Object value = loadValue();
        if (normalizer != null) {
            value = normalizer.toViewValue(value);
        }
        spinner.setModel(getModel(swfType, value));        
    }

    @Override
    public boolean save() {
        Object oldValue = loadValue();
        Object oldViewValue = oldValue;
        if (normalizer != null) {
            oldViewValue = normalizer.toViewValue(oldValue);
        }
        Object newViewValue = getChangedValue();
        if (newViewValue == null) {
            return false;
        }
        if (Objects.equals(oldViewValue, newViewValue)) {
            return false;
        }
        Object newValue = newViewValue;
        if (normalizer != null) {
            newValue = normalizer.toFieldValue(newViewValue);
        }
        saveValue(newValue);

        return true;
    }

    protected Object loadValue() {
        try {
            return ReflectionTools.getValue(obj, field, index);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            // ignore
        }
        return null;
    }

    protected void saveValue(Object newValue) {
        try {
            ReflectionTools.setValue(obj, field, index, newValue);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            // ignore
        }
    }

    private SpinnerModel getModel(SWFType swfType, Object value) {
        SpinnerNumberModel m = null;
        BasicType basicType = swfType == null ? BasicType.NONE : swfType.value();
        switch (basicType) {
            case UI8:
                m = new SpinnerNumberModel(toInt(value), 0, 0xff, 1);
                break;
            case UI16:
                m = new SpinnerNumberModel(toInt(value), 0, 0xffff, 1);
                break;
            case UB: {
                long max = 1;
                if (swfType.count() > 0) {
                    max <<= swfType.count();
                } else {
                    max <<= 31;
                }
                m = new SpinnerNumberModel((Number) toLong(value), 0L, (long) max - 1, 1L);
            }
            break;
            case UI32:
            case EncodedU32:
            case NONE:
                m = new SpinnerNumberModel((Number) toLong(value), 0L, 0xffffffffL, 1L);
                break;
            case SI8:
                m = new SpinnerNumberModel(toInt(value), -0x80, 0x7f, 1);
                break;
            case SI16:
            case FLOAT16:
                m = new SpinnerNumberModel(toInt(value), -0x8000, 0x7fff, 1);
                break;
            case SB:
                long max = 1;
                if (swfType.count() > 0) {
                    max <<= (swfType.count() - 1);
                } else {
                    max <<= 30;
                }
                m = new SpinnerNumberModel((Number) toLong(value), (long) (-max), (long) max - 1, 1L);
                break;
            case SI32:
                m = new SpinnerNumberModel(toDouble(value), -0x80000000, 0x7fffffff, 1);
                break;
        }
        return m;
    }

    private double toDouble(Object value) {
        if (value instanceof Float) {
            return (double) (Float) value;
        }
        if (value instanceof Double) {
            return (double) (Double) value;
        }
        return 0;
    }

    private int toInt(Object value) {
        if (value instanceof Short) {
            return (int) (Short) value;
        }
        if (value instanceof Integer) {
            return (int) (Integer) value;
        }
        return 0;
    }

    private long toLong(Object value) {
        if (value instanceof Short) {
            return (long) (Short) value;
        }
        if (value instanceof Integer) {
            return (long) (Integer) value;
        }
        if (value instanceof Long) {
            return (long) (Long) value;
        }
        return 0;
    }

    @Override
    public void addChangeListener(final ChangeListener l) {
        final GenericTagEditor t = this;
        addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                l.change(t);
            }

        });
    }

    @Override
    public void validateValue() {
    }

    @Override
    public Object getChangedValue() {
        Object value = null;
        if (type.equals(int.class) || type.equals(Integer.class)) {
            value = Integer.parseInt(spinner.getValue().toString());
        } else if (type.equals(short.class) || type.equals(Short.class)) {
            value = Short.parseShort(spinner.getValue().toString());
        } else if (type.equals(long.class) || type.equals(Long.class)) {
            value = Long.parseLong(spinner.getValue().toString());
        } else if (type.equals(double.class) || type.equals(Double.class)) {
            value = Double.parseDouble(spinner.getValue().toString());
        } else if (type.equals(float.class) || type.equals(Float.class)) {
            value = Float.parseFloat(spinner.getValue().toString());
        }
        return value;
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
        return getChangedValue().toString();
    }

    @Override
    public Object getObject() {
        return obj;
    }

    @Override
    public void setValueNormalizer(ValueNormalizer normalizer) {
        this.normalizer = normalizer;
    }
}
