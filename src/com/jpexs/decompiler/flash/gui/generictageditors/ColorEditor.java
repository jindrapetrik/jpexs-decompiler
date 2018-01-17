/*
 *  Copyright (C) 2010-2018 JPEXS
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

import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.types.ARGB;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.helpers.ReflectionTools;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.border.BevelBorder;
import javax.swing.colorchooser.AbstractColorChooserPanel;

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

    private final JButton buttonChange;

    public int getColorType() {
        return colorType;
    }

    @Override
    public void added() {

    }

    public ColorEditor(String fieldName, Object obj, Field field, int index, Class<?> type) {
        this.obj = obj;
        this.field = field;
        this.index = index;
        this.type = type;
        this.fieldName = fieldName;

        setLayout(new FlowLayout());

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

        buttonChange.setBorderPainted(true);
        buttonChange.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        Dimension colorDim = new Dimension(16, 16);
        buttonChange.setSize(colorDim);
        buttonChange.setPreferredSize(colorDim);
        add(buttonChange);
        reset();
    }

    @Override
    public void validateValue() {
    }

    @Override
    public void reset() {
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

            buttonChange.setBackground(color);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            // ignore
        }
    }

    @Override
    public void save() {
        Object val = getChangedValue();
        try {
            ReflectionTools.setValue(obj, field, index, val);
        } catch (IllegalAccessException ex) {
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

    private static Color noTransparencyColorChooser(Component component, String title, Color initialColor) throws Exception {
        final JColorChooser pane = new JColorChooser(initialColor != null
                ? initialColor : Color.white);

        AbstractColorChooserPanel[] colorPanels = pane.getChooserPanels();
        for (int i = 1; i < colorPanels.length; i++) {
            AbstractColorChooserPanel cp = colorPanels[i];

            Field f = cp.getClass().getDeclaredField("panel");
            f.setAccessible(true);

            Object colorPanel = f.get(cp);
            Field f2 = colorPanel.getClass().getDeclaredField("spinners");
            f2.setAccessible(true);
            Object spinners = f2.get(colorPanel);

            Object transpSlispinner = Array.get(spinners, 3);
            if (i == colorPanels.length - 1) {
                transpSlispinner = Array.get(spinners, 4);
            }
            Field f3 = transpSlispinner.getClass().getDeclaredField("slider");
            f3.setAccessible(true);
            JSlider slider = (JSlider) f3.get(transpSlispinner);
            slider.setEnabled(false);
            Field f4 = transpSlispinner.getClass().getDeclaredField("spinner");
            f4.setAccessible(true);
            JSpinner spinner = (JSpinner) f4.get(transpSlispinner);
            spinner.setEnabled(false);
        }
        final Color[] col = new Color[]{initialColor};

        JDialog dialog = JColorChooser.createDialog(component, title, true, pane, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                col[0] = pane.getColor();
            }
        }, null);

        dialog.setVisible(true);
        return col[0];

    }

    @Override
    public void actionPerformed(ActionEvent e) {

        Color newColor;
        if (colorType == COLOR_TYPE_RGB) {
            try {
                newColor = noTransparencyColorChooser(null, AppStrings.translate("dialog.selectcolor.title"), color);
            } catch (Exception ex) {
                newColor = JColorChooser.showDialog(null, AppStrings.translate("dialog.selectcolor.title"), color);
            }
        } else {
            newColor = JColorChooser.showDialog(null, AppStrings.translate("dialog.selectcolor.title"), color);
        }
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
