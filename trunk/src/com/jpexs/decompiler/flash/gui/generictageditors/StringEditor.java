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

import java.awt.Color;
import java.lang.reflect.Field;
import javax.swing.JTextArea;

/**
 *
 * @author JPEXS
 */
public class StringEditor extends JTextArea implements GenericTagEditor {

    private final Object tag;
    private final Field field;

    public StringEditor(Object obj, Field field) {
        setBackground(Color.white);
        setLineWrap(true);
        this.tag = obj;
        this.field = field;
        try {
            setText((String) field.get(obj));
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            // ignore
        }
    }

    @Override
    public void save() {
        try {
            field.set(tag, getText());
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            // ignore
        }
    }
}
