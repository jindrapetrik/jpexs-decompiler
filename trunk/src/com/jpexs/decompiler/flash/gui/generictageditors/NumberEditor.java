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
package com.jpexs.decompiler.flash.gui.generictageditors;

import com.jpexs.decompiler.flash.types.BasicType;
import java.awt.Color;
import java.lang.reflect.Field;
import javax.swing.JTextArea;

/**
 *
 * @author JPEXS
 */
public class NumberEditor extends JTextArea implements GenericTagEditor {

    private final Object obj;
    private final Field field;
    private final BasicType basicType;

    public NumberEditor(Object obj, Field field, BasicType basicType) {
        setBackground(Color.white);
        setSize(100, getSize().height);
        setMaximumSize(getSize());
        setWrapStyleWord(true);
        this.obj = obj;
        this.field = field;
        this.basicType = basicType;
        try {
            setText(field.get(obj).toString());
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            // ignore
        }
    }

    @Override
    public void save() {
        try {
            Class<?> type = field.getType();
            if (type.equals(int.class) || type.equals(Integer.class)) {
                field.set(obj, Integer.parseInt(getText()));
            } else if (type.equals(short.class) || type.equals(Short.class)) {
                field.set(obj, Short.parseShort(getText()));
            } else if (type.equals(long.class) || type.equals(Long.class)) {
                field.set(obj, Long.parseLong(getText()));
            } else if (type.equals(double.class) || type.equals(Double.class)) {
                field.set(obj, Double.parseDouble(getText()));
            } else if (type.equals(float.class) || type.equals(Float.class)) {
                field.set(obj, Float.parseFloat(getText()));
            }
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            // ignore
        }
    }
}
