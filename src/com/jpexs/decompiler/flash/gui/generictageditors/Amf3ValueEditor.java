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
import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.importers.amf.amf3.Amf3Importer;
import com.jpexs.decompiler.flash.importers.amf.amf3.Amf3ParseException;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.ReflectionTools;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Timer;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

/**
 *
 * @author JPEXS
 */
public class Amf3ValueEditor extends JPanel implements GenericTagEditor {

    private JEditorPane editor = new JEditorPane() {
        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public Component.BaselineResizeBehavior getBaselineResizeBehavior() {
            return Component.BaselineResizeBehavior.CONSTANT_ASCENT;
        }

        @Override
        public int getBaseline(int width, int height) {
            return 0;
        }
    };

    private final Object obj;

    private final Field field;

    private final int index;

    private final Class<?> type;

    private String fieldName;

    private Amf3Value value;
    private final JLabel errorLabel = new JLabel();
    private Timer hideErrorTimer;
    private final int TIMEOUT = 5000;

    @Override
    public void added() {
        String s = editor.getText();
        editor.setContentType("text/javascript");
        editor.setText(s);
    }

    public Amf3ValueEditor(String fieldName, Object obj, Field field, int index, Class<?> type) {
        this.obj = obj;
        this.field = field;
        this.index = index;
        this.type = type;
        this.fieldName = fieldName;

        setLayout(new BorderLayout());

        Dimension d = new Dimension(500, 330);
        setSize(d);
        setPreferredSize(d);

        add(new JScrollPane(editor), BorderLayout.CENTER);
        add(errorLabel, BorderLayout.SOUTH);
        errorLabel.setBackground(Color.red);
        errorLabel.setForeground(Color.white);
        errorLabel.setOpaque(true);

        reset();
    }

    @Override
    public void reset() {
        try {
            value = (Amf3Value) ReflectionTools.getValue(obj, field, index);
            if (value == null || value.getValue() == null) {
                editor.setText("");
            } else {
                String stringVal = Amf3Exporter.amfToString(value.getValue());
                editor.setText(stringVal);
            }
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            // ignore
        }
    }

    @Override
    public void save() {
        try {
            Object val = getChangedValue();
            ReflectionTools.setValue(obj, field, index, val);
            value = (Amf3Value) val;
        } catch (IllegalAccessException ex) {
            //ignore
        }
    }

    @Override
    public void addChangeListener(final ChangeListener l) {
        final GenericTagEditor t = this;
        editor.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                l.change(t);
            }

        });
    }

    @Override
    public void validateValue() {

        Amf3Importer importer = new Amf3Importer();
        String textVal = editor.getText();
        try {
            if (!textVal.trim().isEmpty()) {
                importer.stringToAmf(textVal);
            }
        } catch (IOException | Amf3ParseException ex) {
            final CaretListener cl = new CaretListener() {
                @Override
                public void caretUpdate(CaretEvent e) {
                    errorLabel.setVisible(false);
                    editor.removeCaretListener(this);
                }
            };
            editor.addCaretListener(cl);
            errorLabel.setText("<html>" + AppStrings.translate("error") + ":" + ex.getMessage() + "</html>");
            errorLabel.setVisible(true);
            throw new IllegalArgumentException("Invalid AMF value", ex);
        }
    }

    @Override
    public Object getChangedValue() {
        Amf3Importer importer = new Amf3Importer();
        String textVal = editor.getText();
        try {
            return textVal.trim().isEmpty() ? null : new Amf3Value(importer.stringToAmf(textVal));
        } catch (IOException | Amf3ParseException ex) {
            return value;
        }
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
