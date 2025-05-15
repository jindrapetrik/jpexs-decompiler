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

import com.jpexs.helpers.ReflectionTools;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

/**
 * @author JPEXS
 */
public class BooleanEditor extends JPanel implements GenericTagEditor {

    private final Object obj;

    private final Field field;

    private final int index;

    private final Class<?> type;

    private final String fieldName;
    
    private final JCheckBox checkBox;

    @Override
    public void added() {

    }

    public BooleanEditor(String fieldName, Object obj, Field field, int index, Class<?> type) {
        super();
        this.obj = obj;
        this.field = field;
        this.index = index;
        this.type = type;
        this.fieldName = fieldName;
        checkBox = new JCheckBox();
        checkBox.setOpaque(false);
        checkBox.setRequestFocusEnabled(false);
        setLayout(new BorderLayout());        
        add(checkBox, BorderLayout.CENTER);
        setOpaque(false);
        reset();        
    }

    @Override
    public void validateValue() {
    }

    @Override
    public void reset() {
        try {
            checkBox.setSelected((boolean) ReflectionTools.getValue(obj, field, index));
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            // ignore
        }
    }

    @Override
    public boolean save() {
        try {
            boolean oldValue = (boolean) ReflectionTools.getValue(obj, field, index);
            boolean newValue = checkBox.isSelected();

            if (oldValue == newValue) {
                return false;
            }

            ReflectionTools.setValue(obj, field, index, checkBox.isSelected());
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            // ignore
        }
        return true;
    }

    @Override
    public void addChangeListener(final ChangeListener l) {
        final GenericTagEditor t = this;
        checkBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                l.change(t);
            }
        });
    }

    @Override
    public Object getChangedValue() {
        return checkBox.isSelected();
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
