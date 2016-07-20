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

import com.jpexs.decompiler.flash.amf.amf3.Amf3Value;
import com.jpexs.decompiler.flash.exporters.amf.amf3.Amf3Exporter;
import com.jpexs.decompiler.flash.importers.amf.amf3.Amf3Importer;
import com.jpexs.decompiler.flash.importers.amf.amf3.Amf3ParseException;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.ReflectionTools;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;
import java.lang.reflect.Field;
import javax.swing.JEditorPane;

/**
 *
 * @author JPEXS
 */
public class Amf3ValueEditor extends JEditorPane implements GenericTagEditor {

    private final Object obj;

    private final Field field;

    private final int index;

    private final Class<?> type;

    private String fieldName;

    @Override
    public void added() {
        String s = getText();
        setContentType("text/javascript");
        setText(s);
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    /*    @Override
    public Dimension getPreferredSize() {
        Dimension ret = super.getPreferredSize();
        ret.width = 300;
        return ret;
    }*/
    @Override
    public Component.BaselineResizeBehavior getBaselineResizeBehavior() {
        return Component.BaselineResizeBehavior.CONSTANT_ASCENT;
    }

    @Override
    public int getBaseline(int width, int height) {
        return 0;
    }

    public Amf3ValueEditor(String fieldName, Object obj, Field field, int index, Class<?> type) {
        this.obj = obj;
        this.field = field;
        this.index = index;
        this.type = type;
        this.fieldName = fieldName;
        try {
            Amf3Value val = (Amf3Value) ReflectionTools.getValue(obj, field, index);
            String stringVal = Amf3Exporter.amfToString(val.getValue());
            setText(stringVal);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            // ignore
        }
    }

    @Override
    public void save() {
        Amf3Importer importer = new Amf3Importer();
        try {
            Object newValue = importer.stringToAmf(getText());
            ReflectionTools.setValue(obj, field, index, new Amf3Value(newValue));
        } catch (IOException | Amf3ParseException | IllegalArgumentException | IllegalAccessException ex) {
            //ignore
        }
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
    public Object getChangedValue() {
        Amf3Importer importer = new Amf3Importer();
        try {
            return new Amf3Value(importer.stringToAmf(getText()));
        } catch (IOException | Amf3ParseException ex) {
            //ignore
        }
        return null;
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

}
