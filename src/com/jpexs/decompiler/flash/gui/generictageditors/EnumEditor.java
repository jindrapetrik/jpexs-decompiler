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

import com.jpexs.decompiler.flash.gui.ComboBoxItem;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.ReflectionTools;
import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import javax.swing.JComboBox;

/**
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
            for (int i = 0; i < getItemCount(); i++) {
                ComboBoxItem<Integer> item = getItemAt(i);
                if (item.getValue() == value) {
                    setSelectedItem(item);
                    break;
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            // ignore
        }
    }

    @Override
    public boolean save() {
        try {
            Integer oldValue = (Integer) ReflectionTools.getValue(obj, field, index);
            Integer newValue = (Integer) getChangedValue();
            if (newValue == null) {
                return false;
            }
            if (Objects.equals(oldValue, newValue)) {
                return false;
            }
            ReflectionTools.setValue(obj, field, index, newValue);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            // ignore
        }
        return true;
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

    @Override
    public Object getObject() {
        return obj;
    }
    
    @Override
    public void setValueNormalizer(ValueNormalizer normalizer) {
    
    }  
}
