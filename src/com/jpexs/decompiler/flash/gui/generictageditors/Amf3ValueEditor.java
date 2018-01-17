/*
 *  Copyright (C) 2016-2018 JPEXS
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

import com.jpexs.decompiler.flash.amf.amf3.Amf3Value;
import com.jpexs.decompiler.flash.exporters.amf.amf3.Amf3Exporter;
import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.gui.editor.LineMarkedEditorPane;
import com.jpexs.decompiler.flash.importers.amf.amf3.Amf3Importer;
import com.jpexs.decompiler.flash.importers.amf.amf3.Amf3ParseException;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.ReflectionTools;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Timer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

/**
 *
 * @author JPEXS
 */
public class Amf3ValueEditor extends JPanel implements GenericTagEditor, FullSized {

    private LineMarkedEditorPane editor = new LineMarkedEditorPane();

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
        editor.setCaretPosition(0);
        //Dimension csize = getPreferredSize();
        //System.out.println("max=" + getMaximumSize());

        //csize.height = 300;
        //Dimension editorSize = editor.getPreferredSize();
        //setPreferredSize(csize);
    }

    @Override
    public BaselineResizeBehavior getBaselineResizeBehavior() {
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
        setPreferredSize(new Dimension(800, 200));
        setLayout(new BorderLayout());

        JPanel titlePanel = new JPanel(new BorderLayout()) {
            @Override
            public Insets getInsets() {
                Insets is = super.getInsets();
                is.left = 5;
                return is;
            }

        };
        JLabel titleLabel = new JLabel(AppStrings.translate("generic.editor.amf3.title"));
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        JButton helpButton = new JButton(View.getIcon("about16"));
        helpButton.setFocusable(false);
        JTextArea txthelp = new JTextArea();
        txthelp.setFont(new Font("monospaced", Font.PLAIN, 12));

        final String SCALAR_SAMPLES = " * undefined, null, false, true\n"
                + " * integer : -5, 5, 29\n"
                + " * double: 5.5, 1.27, -187.568\n"
                + " * String: \"hello\", \"escape new\\nline\"\n";
        final String NONSCALAR_SAMPLES = " * XMLDocument: {\"type\":\"XMLDocument\", value: \"<foo></foo>\"}\n"
                + " * Date: {\"type\":\"Date\", \"value\": \"2016-07-17 14:51:42.584\"}\n"
                + " * Array: {\"type\":\"Array\", \"denseValues\": [27,58,99], \"associativeValues\": {\"key1\":5, \"key2\": -4.6 }}\n"
                + " * Object: {\"type\":\"Object\",\"className\":\"\",\"dynamic\":true,\n"
                + "            \"sealedMembers\": {\"smemberA\": \"abc\", \"smemberB\":987.5},\n"
                + "            \"dynamicMembers\": {\"member1\": 5, \"member2\": \"aaa\"}\n"
                + "            }\n"
                + " * XML: {\"type\":\"XML\", value: \"<foo></foo>\"}\n"
                + " * ByteArray: {\"type\":\"ByteArray\", \"value\":\"B0312F\"}\n"
                + " * Vector: {\"type\": \"Vector\", \"fixed\":false, \"subtype\":\"int\", \"values\": [8, 4, 6]}\n"
                + " * Dictionary: {\"type\": \"Dictionary\", \"weakKeys\":false, \"entries\": { \"dkey1\" : \"val1\", \"dkey2\": 56 }}\n";
        final String REFERENCE_SAMPLE = "  {\"type\": \"Vector\", \"fixed\":false, \"subtype\":\"\",\n"
                + "   \"values\": [\n"
                + "       {\"type\":\"Date\", id=\"mydate\",\"value\": \"2016-07-17 14:51:42.584\"}\n"
                + "       #mydate,\n"
                + "       #mydate,\n"
                + "       {\"type\":\"Date\", \"value\": \"2016-07-26 18:12:22.188\"}\n"
                + "    ]}";

        txthelp.setText(AppStrings.translate("generic.editor.amf3.help").replace("%scalar_samples%", SCALAR_SAMPLES).replace("%nonscalar_samples%", NONSCALAR_SAMPLES).replace("%reference_sample%", REFERENCE_SAMPLE));
        txthelp.setEditable(false);
        helpButton.addActionListener((ActionEvent e) -> {
            View.showMessageDialog(null, txthelp);
        });
        titlePanel.add(helpButton, BorderLayout.EAST);
        add(titlePanel, BorderLayout.NORTH);

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

            if (ex instanceof Amf3ParseException) {
                Amf3ParseException ape = (Amf3ParseException) ex;
                if (ape.line > 0) {
                    editor.gotoLine((int) ape.line);
                }
            }
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
