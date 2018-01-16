/*
 *  Copyright (C) 2010-2018 JPEXS
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

import com.jpexs.decompiler.flash.gui.ComboBoxItem;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.ReflectionTools;
import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import javax.swing.JComboBox;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

/**
 *
 * @author JPEXS
 */
public class EnumEditor extends JComboBox<ComboBoxItem<Integer>> implements GenericTagEditor {

    private final Object obj;

    private final Field field;

    private final int index;

    private final Class<?> type;

    private final SWFType swfType;

    private String fieldName;

    private Map<Integer, String> values;

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

    public EnumEditor(String fieldName, Object obj, Field field, int index, Class<?> type, SWFType swfType, Map<Integer, String> values) {
        setSize(100, getSize().height);
        setMaximumSize(getSize());
        this.obj = obj;
        this.field = field;
        this.index = index;
        this.type = type;
        this.swfType = swfType;
        this.fieldName = fieldName;
        this.values = values;

        Integer[] valuesArray = new Integer[values.size()];
        values.keySet().toArray(valuesArray);
        Arrays.sort(valuesArray);
        for (int value : valuesArray) {
            addItem(new ComboBoxItem<>(value + " - " + values.get(value), value));
        }

        reset();
    }

    @Override
    public void reset() {
        try {
            int value = (int) (Integer) ReflectionTools.getValue(obj, field, index);
            setSelectedItem(values.get(value));
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            // ignore
        }
    }

    @Override
    public void save() {
        try {
            Object value = getChangedValue();
            if (value != null) {
                ReflectionTools.setValue(obj, field, index, value);
            }
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
            case FB:
            case SB: {
                long max = 1;
                if (swfType.count() > 0) {
                    max <<= (swfType.count() - 1);
                } else {
                    max <<= 30;
                }
                m = new SpinnerNumberModel((Number) toLong(value), (long) (-max), (long) max - 1, 1L);
            }
            break;
            case SI32:
                m = new SpinnerNumberModel(toDouble(value), -0x80000000, 0x7fffffff, 1);
                break;
            case FLOAT:
            case FIXED:
            case FIXED8:
                m = new SpinnerNumberModel(toDouble(value), -0x80000000, 0x7fffffff, 0.01);
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
        @SuppressWarnings("unchecked")
        ComboBoxItem<Integer> item = (ComboBoxItem<Integer>) getSelectedItem();
        int value = item.getValue();
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
}
