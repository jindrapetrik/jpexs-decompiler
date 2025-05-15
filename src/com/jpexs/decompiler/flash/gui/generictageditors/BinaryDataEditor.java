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

import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.gui.MainPanel;
import com.jpexs.decompiler.flash.gui.ViewMessages;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.ReflectionTools;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Objects;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * @author JPEXS
 */
public class BinaryDataEditor extends JPanel implements GenericTagEditor {

    private final MainPanel mainPanel;

    private final JButton replaceButton;

    private final JButton exportButton;

    private final Object obj;

    private final Field field;

    private final int index;

    private final Class<?> type;

    private final String fieldName;

    private Object value;

    public BinaryDataEditor(MainPanel mainPanel, String fieldName, Object obj, Field field, int index, Class<?> type) {
        super();
        this.mainPanel = mainPanel;
        this.obj = obj;
        this.field = field;
        this.index = index;
        this.type = type;
        this.fieldName = fieldName;
        exportButton = new JButton(AppStrings.translate("button.export"));
        replaceButton = new JButton(AppStrings.translate("button.replace"));

        setLayout(new FlowLayout());
        add(exportButton);
        add(replaceButton);
        exportButton.addActionListener(this::exportActionPerformed);
        replaceButton.addActionListener(this::replaceActionPerformed);
        reset();

    }

    @Override
    public void validateValue() {
    }

    @Override
    public void reset() {
        try {
            Object val = ReflectionTools.getValue(obj, field, index);
            if (val instanceof byte[]) {
                byte[] ba = (byte[]) val;
                setToolTipText(ba.length + " bytes");
            } else if (val instanceof ByteArrayRange) {
                ByteArrayRange bar = (ByteArrayRange) val;
                setToolTipText(bar.getLength() + " bytes");
            }
            value = val;
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            // ignore
        }
    }

    private void exportActionPerformed(ActionEvent evt) {
        if (value == null) {
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new File(Configuration.lastExportDir.get()));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selfile = Helper.fixDialogFile(fc.getSelectedFile());
            ByteArrayRange br = (ByteArrayRange) value;
            try (FileOutputStream fos = new FileOutputStream(selfile)) {
                fos.write(br.getArray(), br.getPos(), br.getLength());
            } catch (IOException ex) {
                ViewMessages.showMessageDialog(mainPanel, ex.getMessage(), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void replaceActionPerformed(ActionEvent evt) {
        File selectedFile = mainPanel.showImportFileChooser("", false, "importbinarydata");
        if (selectedFile != null) {
            File selfile = Helper.fixDialogFile(selectedFile);
            byte[] data = Helper.readFile(selfile.getAbsolutePath());
            setToolTipText(data.length + " bytes");
            Class type = field.getType();
            if (type.equals(byte[].class)) {
                value = data;
            } else if (type.equals(ByteArrayRange.class)) {
                value = new ByteArrayRange(data);
            }
        }
    }

    @Override
    public boolean save() {
        try {
            Object oldValue = ReflectionTools.getValue(obj, field, index);
            Object newValue = value;
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
        replaceButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                l.change(t);
            }
        });
    }

    @Override
    public Object getChangedValue() {
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

    @Override
    public Object getObject() {
        return obj;
    }
    
    @Override
    public void setValueNormalizer(ValueNormalizer normalizer) {
    
    }  
}
