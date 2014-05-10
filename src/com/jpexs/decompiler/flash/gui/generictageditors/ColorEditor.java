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

import com.jpexs.decompiler.flash.AppStrings;
import com.jpexs.decompiler.flash.types.ARGB;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.RGBA;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.lang.reflect.Field;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

/**
 *
 * @author JPEXS
 */
public class ColorEditor extends JPanel implements GenericTagEditor, ActionListener {

    private final Object obj;
    private final Field field;
    private final int index;
    private final Class<?> type;
    private final String fieldName;
    private Color color;
    public static final int COLOR_TYPE_RGB = 0;
    public static final int COLOR_TYPE_RGBA = 1;
    public static final int COLOR_TYPE_ARGB = 2;
    private int colorType;
    private JButton buttonChange;

    public int getColorType() {
        return colorType;
    }

    public ColorEditor(String fieldName, Object obj, Field field, int index, Class<?> type) {
        this.obj = obj;
        this.field = field;
        this.index = index;
        this.type = type;
        this.fieldName = fieldName;
        try {
            Object val = ReflectionTools.getValue(obj, field, index);
            if (val instanceof RGBA) {
                colorType = COLOR_TYPE_RGBA;
            } else if (val instanceof RGB) {
                colorType = COLOR_TYPE_RGB;
            } else if (val instanceof ARGB) {
                colorType = COLOR_TYPE_ARGB;
            } else {
                throw new IllegalArgumentException("Invalid value type");
            }
            if (val instanceof RGB) { //Note: Can be RGBA too
                color = ((RGB) val).toColor();
            }
            if (val instanceof ARGB) {
                color = ((ARGB) val).toColor();
            }
            setLayout(new FlowLayout());

            //add(colorPanel);
            buttonChange = new JButton("") {

                @Override
                protected void paintComponent(Graphics g) {
                    g.setColor(getBackground());
                    g.fillRect(0, 0, getWidth(), getHeight());
                    super.paintBorder(g);
                }

            };
            buttonChange.setToolTipText(AppStrings.translate("button.selectcolor.hint"));
            buttonChange.setCursor(new Cursor(Cursor.HAND_CURSOR));
            buttonChange.addActionListener(this);
            buttonChange.setBackground(color);
            buttonChange.setBorderPainted(true);
            buttonChange.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
            Dimension colorDim = new Dimension(16, 16);
            buttonChange.setSize(colorDim);
            buttonChange.setPreferredSize(colorDim);
            add(buttonChange);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            // ignore
        }

    }

    @Override
    public void save() {
        Object val = getChangedValue();
        try {
            ReflectionTools.setValue(obj, field, index, val);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            // ignore
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
        Object val = null;
        switch (colorType) {
            case COLOR_TYPE_RGB:
                val = new RGB(color);
                break;
            case COLOR_TYPE_RGBA:
                val = new RGBA(color);
                break;
            case COLOR_TYPE_ARGB:
                val = new ARGB();
                break;
        }
        return val;
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
    public void actionPerformed(ActionEvent e) {
        Color newColor = JColorChooser.showDialog(null, AppStrings.translate("dialog.selectcolor.title"), color);
        if (newColor != null) {
            color = newColor;
            buttonChange.setBackground(color);
            repaint();
        }
    }

    @Override
    public String getReadOnlyValue() {
        int h = System.identityHashCode(this);
        return "<cite style=\"background-color:rgb(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + ");\">&nbsp;&nbsp;&nbsp;&nbsp;</cite> " + getChangedValue().toString();
    }

}
