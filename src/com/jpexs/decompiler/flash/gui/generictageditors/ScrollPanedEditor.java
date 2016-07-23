/*
 * Copyright (C) 2016 Jindra
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.gui.generictageditors;

import java.awt.Component;
import java.awt.Dimension;
import java.lang.reflect.Field;
import javax.swing.JScrollPane;

/**
 *
 * @author JPEXS
 */
public class ScrollPanedEditor extends JScrollPane implements GenericTagEditor {

    private final GenericTagEditor editor;

    public ScrollPanedEditor(GenericTagEditor editor) {
        super((Component) editor);
        this.editor = editor;
        Dimension d = new Dimension(500, 300);
        setSize(d);
        setPreferredSize(d);
    }

    @Override
    public void save() {
        editor.save();
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        editor.addChangeListener(l);
    }

    @Override
    public Object getChangedValue() {
        return editor.getChangedValue();
    }

    @Override
    public String getFieldName() {
        return editor.getFieldName();
    }

    @Override
    public Field getField() {
        return editor.getField();
    }

    @Override
    public String getReadOnlyValue() {
        return editor.getReadOnlyValue();
    }

    @Override
    public void added() {
        editor.added();
    }
}
