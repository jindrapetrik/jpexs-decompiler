/*
 *  Copyright (C) 2026 JPEXS
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
package com.jpexs.decompiler.flash.easygui.properties;

import com.jpexs.decompiler.flash.easygui.EasyStrings;
import com.jpexs.decompiler.flash.gui.PopupButton;
import com.jpexs.decompiler.flash.gui.View;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Locale;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Inline color controls shared by solid paints and gradient stops.
 */
public final class ShapeColorEditor extends JPanel {

    private static final int SQUARE_SIZE = 112;
    private static final int HUE_WIDTH = 14;
    private static final int FIELD_VERTICAL_INSET = 1;
    private static final int[] HSB_MAXIMA = {360, 100, 100};
    private static final int[] SWATCHES = {
        0x000000, 0x333333, 0x666666, 0x999999, 0xCCCCCC, 0xFFFFFF,
        0xFF0000, 0xFFFF00, 0x00FF00, 0x00FFFF, 0x0000FF, 0xFF00FF,
        0x800000, 0x808000, 0x008000, 0x008080, 0x000080, 0x800080,
        0xFF8000, 0x80FF00, 0x00FF80, 0x0080FF, 0x8000FF, 0xFF0080,
        0xFF8080, 0xFFFF80, 0x80FF80, 0x80FFFF, 0x8080FF, 0xFF80FF
    };

    private final JPopupMenu swatchesPopup = new JPopupMenu();
    private final FloatPropertyField[] channelFields = new FloatPropertyField[3];
    private final FloatPropertyField[] rgbFields = new FloatPropertyField[3];
    private final FloatPropertyField alphaField = colorNumber(100);
    private final TextPropertyField hexField = new TextPropertyField("000000", 9) {
        @Override
        protected String textToValue(String text) {
            return text.trim();
        }
    };
    private final JLabel preview = new JLabel();
    private final ColorPlane plane = new ColorPlane(false);
    private final ColorPlane hueStrip = new ColorPlane(true);
    private final Consumer<Color> changeListener;
    private Color color = Color.black;
    private boolean mixed;
    private float[] planeColor = new float[3];
    private float[] dragColor;

    public ShapeColorEditor(Consumer<Color> changeListener) {
        super(new BorderLayout(0, 4));
        this.changeListener = changeListener;

        JPanel components = new JPanel(new GridBagLayout());
        JPanel picker = new JPanel(new GridBagLayout());
        GridBagConstraints pickerConstraints = new GridBagConstraints();
        pickerConstraints.anchor = GridBagConstraints.NORTHWEST;
        pickerConstraints.weighty = 1;
        JPanel planes = new JPanel(new BorderLayout(4, 0)) {
            @Override
            public Dimension getPreferredSize() {
                Dimension size = super.getPreferredSize();
                int height = components.getPreferredSize().height - FIELD_VERTICAL_INSET;
                size.width += height - size.height;
                size.height = height;
                return size;
            }
        };
        planes.add(plane, BorderLayout.CENTER);
        planes.add(hueStrip, BorderLayout.EAST);
        picker.add(planes, pickerConstraints);
        pickerConstraints.weightx = 1;
        picker.add(components, pickerConstraints);
        for (int i = 0; i < channelFields.length; i++) {
            final int index = i;
            channelFields[i] = colorNumber(HSB_MAXIMA[i]);
            addField(components, i, new JLabel("HSB".substring(i, i + 1)), channelFields[i],
                    new JLabel(i == 0 ? "\u00b0" : "%"));
            channelFields[i].addChangeListener(e -> editChannel(index, channelFields[index].getValue()));
        }
        for (int i = 0; i < rgbFields.length; i++) {
            final int index = i;
            rgbFields[i] = colorNumber(255);
            addField(components, channelFields.length + i, new JLabel("RGB".substring(i, i + 1)), rgbFields[i], null);
            rgbFields[i].addChangeListener(e -> editRgbChannel(index, rgbFields[index].getValue()));
        }
        add(picker, BorderLayout.CENTER);

        swatchesPopup.setLayout(new GridLayout(0, 6, 2, 2));
        for (int rgb : SWATCHES) {
            Color swatch = new Color(rgb);
            ImageIcon icon = createSwatchIcon(swatch);
            JMenuItem item = new JMenuItem(icon);
            item.setBorder(BorderFactory.createEmptyBorder());
            item.setHorizontalAlignment(SwingConstants.CENTER);
            item.setHorizontalTextPosition(SwingConstants.CENTER);
            item.setPreferredSize(new Dimension(icon.getIconWidth() + 2, icon.getIconHeight() + 2));
            item.setToolTipText(hex(swatch));
            item.getAccessibleContext().setAccessibleName(hex(swatch));
            item.addActionListener(e -> chooseColor(withAlpha(swatch, color.getAlpha())));
            swatchesPopup.add(item);
        }
        PopupButton swatchesButton = new PopupButton(View.getIcon("swatch16")) {
            @Override
            protected JPopupMenu getPopupMenu() {
                return swatchesPopup;
            }
        };
        String swatchesLabel = EasyStrings.translate("property.shape.color.swatches");
        swatchesButton.setToolTipText(swatchesLabel);
        swatchesButton.getAccessibleContext().setAccessibleName(swatchesLabel);
        swatchesButton.setMargin(new Insets(2, 3, 2, 3));

        JPanel textFields = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        preview.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));
        textFields.add(preview);
        JLabel hexLabel = new JLabel("#");
        hexLabel.setLabelFor(hexField);
        hexField.setColumns(7);
        setFieldHint(hexField, "property.shape.color.hex");
        textFields.add(hexLabel);
        textFields.add(hexField);
        JPanel alpha = new JPanel(new GridBagLayout());
        addField(alpha, 0, new JLabel("A"), alphaField, new JLabel("%"));
        setFieldHint(alphaField, "property.shape.color.alpha");
        textFields.add(alpha);
        JPanel footer = new JPanel(new BorderLayout(6, 0));
        footer.add(textFields, BorderLayout.CENTER);
        footer.add(swatchesButton, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);

        hexField.addValidation(text -> text.matches("#?[0-9a-fA-F]{6}([0-9a-fA-F]{2})?"));
        hexField.addChangeListener(e -> editHex(hexField.getValue()));
        alphaField.addChangeListener(e -> commit(withAlpha(color, Math.round(alphaField.getValue() * 255 / 100)), planeColor));
        refresh();
    }

    public void setColor(Color newColor) {
        if (!newColor.equals(color)) {
            color = newColor;
            planeColor = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        }
        mixed = false;
        refresh();
    }

    public void setMixed() {
        mixed = true;
        refresh();
    }

    public boolean isMixed() {
        return mixed;
    }

    void finishEditing() {
        Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        AbstractPropertyField<?> field = (AbstractPropertyField<?>) SwingUtilities.getAncestorOfClass(AbstractPropertyField.class, focusOwner);
        if (field != null && SwingUtilities.isDescendingFrom(field, this)) {
            field.finishEdit();
        }
    }

    private static FloatPropertyField colorNumber(int max) {
        FloatPropertyField field = new FloatPropertyField(0, 0, max) {
            @Override
            protected Float textToValue(String text) {
                return super.textToValue(text.replace(',', '.'));
            }
        };
        field.setColumns(4);
        return field;
    }

    private static void setFieldHint(AbstractPropertyField<?> field, String key) {
        String hint = EasyStrings.translate(key);
        field.getAccessibleContext().setAccessibleName(hint);
        field.readLabel.setToolTipText(hint);
        field.writeField.setToolTipText(hint);
    }

    private static void addField(JPanel panel, int row, JLabel label, AbstractPropertyField<?> field, JLabel unit) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = row;
        constraints.insets = new Insets(FIELD_VERTICAL_INSET, 3, FIELD_VERTICAL_INSET, 0);
        constraints.anchor = GridBagConstraints.WEST;
        label.setLabelFor(field);
        panel.add(label, constraints);
        panel.add(field, constraints);
        if (unit != null) {
            panel.add(unit, constraints);
        }
    }

    private void editChannel(int index, float value) {
        float[] next = planeColor.clone();
        next[index] = value / HSB_MAXIMA[index];
        Color rgb = new Color(Color.HSBtoRGB(next[0], next[1], next[2]));
        commit(withAlpha(rgb, color.getAlpha()), next);
    }

    private void editRgbChannel(int index, float value) {
        int[] rgb = rgbChannels();
        rgb[index] = Math.round(value);
        chooseColor(new Color(rgb[0], rgb[1], rgb[2], color.getAlpha()));
    }

    private int[] rgbChannels() {
        return new int[]{color.getRed(), color.getGreen(), color.getBlue()};
    }

    private void editHex(String text) {
        String digits = text.startsWith("#") ? text.substring(1) : text;
        Color rgb = new Color(Integer.parseInt(digits.substring(0, 6), 16));
        int alpha = digits.length() == 8 ? Integer.parseInt(digits.substring(6), 16) : color.getAlpha();
        chooseColor(withAlpha(rgb, alpha));
    }

    private void chooseColor(Color newColor) {
        commit(newColor, Color.RGBtoHSB(newColor.getRed(), newColor.getGreen(), newColor.getBlue(), null));
    }

    private void commit(Color newColor, float[] newPlane) {
        boolean changed = mixed || !newColor.equals(color);
        color = newColor;
        mixed = false;
        planeColor = newPlane;
        refresh();
        if (changed) {
            changeListener.accept(newColor);
            // An edit can synchronously reload the properties from the shape.
            if (newColor.equals(color) && !mixed) {
                planeColor = newPlane;
                refresh();
            }
        }
    }

    private void refresh() {
        for (int i = 0; i < channelFields.length; i++) {
            channelFields[i].setValue(mixed ? null : (float) Math.round(planeColor[i] * HSB_MAXIMA[i]), true);
        }
        int[] rgb = rgbChannels();
        for (int i = 0; i < rgbFields.length; i++) {
            rgbFields[i].setValue(mixed ? null : (float) rgb[i], true);
        }
        hexField.setValue(mixed ? null : hex(color).substring(1), true);
        alphaField.setValue(mixed ? null : Math.round(color.getAlpha() * 1000f / 255) / 10f, true);
        preview.setText(mixed ? "-" : "");
        preview.setIcon(mixed ? null : createSwatchIcon(color));
        plane.repaint();
        hueStrip.repaint();
    }

    private static Color withAlpha(Color rgb, int alpha) {
        return new Color(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), alpha);
    }

    private static String hex(Color color) {
        return String.format(Locale.ROOT, "#%06X", color.getRGB() & 0xFFFFFF);
    }

    public static ImageIcon createSwatchIcon(Color color) {
        BufferedImage image = new BufferedImage(18, 18, BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = image.getGraphics();
        try {
            for (int y = 0; y < 4; y++) {
                for (int x = 0; x < 4; x++) {
                    graphics.setColor((x + y) % 2 == 0 ? Color.white : Color.lightGray);
                    graphics.fillRect(1 + x * 4, 1 + y * 4, 4, 4);
                }
            }
            graphics.setColor(color);
            graphics.fillRect(1, 1, 16, 16);
            graphics.setColor(Color.gray);
            graphics.drawRect(0, 0, 17, 17);
        } finally {
            graphics.dispose();
        }
        return new ImageIcon(image);
    }

    private final class ColorPlane extends JComponent {

        private final boolean hueOnly;
        private BufferedImage image;
        private float renderedHue = -1;

        ColorPlane(boolean hueOnly) {
            this.hueOnly = hueOnly;
            setPreferredSize(new Dimension(hueOnly ? HUE_WIDTH : SQUARE_SIZE, SQUARE_SIZE));
            setBorder(BorderFactory.createLineBorder(Color.gray));
            MouseAdapter mouse = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (isEnabled() && SwingUtilities.isLeftMouseButton(e)) {
                        dragColor = planeColor.clone();
                        updateDrag(e);
                    }
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (dragColor != null) {
                        updateDrag(e);
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (dragColor == null || !SwingUtilities.isLeftMouseButton(e)) {
                        return;
                    }
                    updateDrag(e);
                    float[] picked = dragColor;
                    dragColor = null;
                    Color rgb = new Color(Color.HSBtoRGB(picked[0], picked[1], picked[2]));
                    commit(withAlpha(rgb, color.getAlpha()), picked);
                }
            };
            addMouseListener(mouse);
            addMouseMotionListener(mouse);
        }

        private void updateDrag(MouseEvent e) {
            float y = fraction(e.getY(), getHeight());
            if (hueOnly) {
                dragColor[0] = 1 - y;
            } else {
                dragColor[1] = fraction(e.getX(), getWidth());
                dragColor[2] = 1 - y;
            }
            plane.repaint();
            hueStrip.repaint();
        }

        private float fraction(int position, int size) {
            return Math.max(0, Math.min(1, (position - 1f) / Math.max(1, size - 3)));
        }

        @Override
        protected void paintComponent(Graphics g) {
            int width = getWidth() - 2;
            int height = getHeight() - 2;
            if (width < 2 || height < 2) {
                return;
            }
            float[] shown = dragColor == null ? planeColor : dragColor;
            if (image == null || image.getWidth() != width || image.getHeight() != height
                    || (!hueOnly && renderedHue != shown[0])) {
                image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                for (int y = 0; y < height; y++) {
                    float vertical = 1 - y / (float) (height - 1);
                    for (int x = 0; x < width; x++) {
                        int rgb = hueOnly ? Color.HSBtoRGB(vertical, 1, 1)
                                : Color.HSBtoRGB(shown[0], x / (float) (width - 1), vertical);
                        image.setRGB(x, y, rgb);
                    }
                }
                renderedHue = shown[0];
            }
            g.drawImage(image, 1, 1, null);
            if (!mixed || dragColor != null) {
                int y = 1 + Math.round((1 - shown[hueOnly ? 0 : 2]) * (height - 1));
                g.setColor(Color.black);
                if (hueOnly) {
                    g.drawRect(0, y - 2, getWidth() - 1, 4);
                    g.setColor(Color.white);
                    g.drawLine(1, y, width, y);
                } else {
                    int x = 1 + Math.round(shown[1] * (width - 1));
                    g.drawOval(x - 4, y - 4, 8, 8);
                    g.setColor(Color.white);
                    g.drawOval(x - 3, y - 3, 6, 6);
                }
            }
        }
    }
}
