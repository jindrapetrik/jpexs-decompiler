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
package com.jpexs.decompiler.flash.easygui.properties.panels;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.easygui.EasyStrings;
import com.jpexs.decompiler.flash.easygui.EasyTagNameResolver;
import com.jpexs.decompiler.flash.easygui.ShapeSelection;
import com.jpexs.decompiler.flash.easygui.properties.FloatPropertyField;
import com.jpexs.decompiler.flash.easygui.properties.GradientEditor;
import com.jpexs.decompiler.flash.easygui.properties.PropertyEditor;
import com.jpexs.decompiler.flash.gui.ComboBoxItem;
import com.jpexs.decompiler.flash.gui.FasterScrollPane;
import com.jpexs.decompiler.flash.gui.ViewMessages;
import com.jpexs.decompiler.flash.gui.generictageditors.ChangeListener;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.GRADIENT;
import com.jpexs.decompiler.flash.types.GRADRECORD;
import com.jpexs.decompiler.flash.types.ILINESTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.helpers.Helper;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

/**
 * Style editors for the selected shape region. Changes are passed to the
 * editor's undo manager through a listener; styles are never mutated in place.
 *
 * @author JPEXS
 */
public class ShapePropertiesPanel extends AbstractPropertiesPanel {

    private final FillEditor fillEditor = new FillEditor(Color.white);
    private final FillEditor strokeEditor = new FillEditor(Color.black);
    private final FloatPropertyField widthEditor = new FloatPropertyField(1f, 0f, 65535f / (float) SWF.unitDivisor);
    private final JComboBox<String> scaleComboBox = createComboBox("scale", "normal", "horizontal", "vertical", "none");
    private final JComboBox<String> capComboBox = createComboBox("cap", "round", "none", "square");
    private final JComboBox<String> joinComboBox = createComboBox("join", "round", "bevel", "miter");
    private final FloatPropertyField miterEditor = new FloatPropertyField(3f, 1f, 255f);
    private final JPanel fillPanel = new JPanel(new GridBagLayout());
    private final JPanel strokePanel = new JPanel(new GridBagLayout());
    private boolean updating;
    private Boolean selectedStroke;
    private Consumer<Boolean> changeListener;
    private BiConsumer<Boolean, String> propertyChangeListener;
    private LINESTYLE2 originalLine;
    private Rectangle2D selectedBounds = new Rectangle2D.Double(0, 0, 2000, 2000);

    public ShapePropertiesPanel() {
        super("shape");
        setLayout(new BorderLayout());
        JLabel title = new JLabel(EasyStrings.translate("properties.shape"));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        add(title, BorderLayout.NORTH);

        addRow(strokePanel, "type", strokeEditor, 0);
        widthEditor.setColumns(3);
        miterEditor.setColumns(3);
        JPanel widthPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        widthPanel.add(widthEditor);
        widthPanel.add(new JLabel(" px"));
        addRow(strokePanel, "width", widthPanel, 1);
        addRow(strokePanel, "scale", scaleComboBox, 2);
        addRow(strokePanel, "cap", capComboBox, 3);
        addRow(strokePanel, "join", joinComboBox, 4);
        addRow(strokePanel, "miter", miterEditor, 5);

        JPanel cards = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = -1;
        addRow(fillPanel, "type", fillEditor, 0);
        addCard(cards, "fill", null, fillPanel, gbc, false);
        // Keep the final card at its preferred height, with free space below it.
        addCard(cards, "stroke", null, strokePanel, gbc, false);
        gbc.gridy++;
        gbc.weighty = 1;
        cards.add(new JPanel(), gbc);
        setCardOpened("fill", true);
        setCardOpened("stroke", true);
        add(new FasterScrollPane(cards), BorderLayout.CENTER);
        fillEditor.changeListener = property -> fireChange(false, property);
        strokeEditor.changeListener = property -> fireChange(true, property);
        widthEditor.addChangeListener(e -> fireChange(true, "width"));
        miterEditor.addChangeListener(e -> fireChange(true, "miter"));
        scaleComboBox.addActionListener(e -> fireChange(true, "scale"));
        capComboBox.addActionListener(e -> fireChange(true, "cap"));
        joinComboBox.addActionListener(e -> fireChange(true, "join"));
    }

    public void setChangeListener(Consumer<Boolean> listener) {
        changeListener = listener;
    }

    public void setPropertyChangeListener(BiConsumer<Boolean, String> listener) {
        propertyChangeListener = listener;
    }

    private void fireChange(boolean stroke, String property) {
        if (!updating && (selectedStroke == null || selectedStroke == stroke)) {
            if (changeListener != null) {
                changeListener.accept(stroke);
            }
            if (propertyChangeListener != null) {
                propertyChangeListener.accept(stroke, property);
            }
        }
    }

    public FILLSTYLE applyFillChange(FILLSTYLE original, Rectangle2D bounds, String property) {
        return fillEditor.applyChange(original, bounds, property);
    }

    public boolean isPropertyChangeValid(boolean stroke, String property) {
        FillEditor editor = stroke ? strokeEditor : fillEditor;
        return !property.equals("type") || editor.typeComboBox.getSelectedIndex() != 4 || editor.bitmapComboBox.getItemCount() > 0;
    }

    public LINESTYLE2 applyStrokeChange(ILINESTYLE original, Rectangle2D bounds, String property) {
        LINESTYLE2 line = original == null ? null : lineStyle2(original);
        if (Arrays.asList("type", "color", "gradient", "bitmap").contains(property)) {
            FILLSTYLE paint = strokeEditor.applyChange(lineFill(original), bounds, property);
            if (paint == null) {
                return null;
            }
            if (line == null) {
                line = new LINESTYLE2();
                line.width = 20;
                line.miterLimitFactor = 3;
            }
            line.hasFillFlag = paint.fillStyleType != FILLSTYLE.SOLID;
            line.color = line.hasFillFlag ? null : new RGBA(paint.color);
            line.fillType = line.hasFillFlag ? paint : null;
        } else if (line != null) {
            switch (property) {
                case "width":
                    line.width = Math.round(widthEditor.getValue() * (float) SWF.unitDivisor);
                    break;
                case "scale":
                    int scale = scaleComboBox.getSelectedIndex();
                    line.noHScaleFlag = scale == 2 || scale == 3;
                    line.noVScaleFlag = scale == 1 || scale == 3;
                    break;
                case "cap":
                    line.startCapStyle = capComboBox.getSelectedIndex();
                    line.endCapStyle = line.startCapStyle;
                    break;
                case "join":
                    line.joinStyle = joinComboBox.getSelectedIndex();
                    break;
                case "miter":
                    line.miterLimitFactor = miterEditor.getValue();
                    break;
            }
        }
        return line;
    }

    public FILLSTYLE getFillStyle() {
        return Boolean.TRUE.equals(selectedStroke) ? null : fillEditor.getFillStyle(selectedBounds);
    }

    public LINESTYLE2 getStrokeStyle() {
        if (Boolean.FALSE.equals(selectedStroke)) {
            return null;
        }
        FILLSTYLE fill = strokeEditor.getFillStyle(selectedBounds);
        if (fill == null) {
            return null;
        }
        LINESTYLE2 line = originalLine == null ? new LINESTYLE2() : Helper.deepCopy(originalLine);
        if (widthEditor.getValue() != null) {
            line.width = Math.round(widthEditor.getValue() * (float) SWF.unitDivisor);
        }
        int scale = scaleComboBox.getSelectedIndex();
        if (scale >= 0) {
            line.noHScaleFlag = scale == 2 || scale == 3;
            line.noVScaleFlag = scale == 1 || scale == 3;
        }
        int cap = capComboBox.getSelectedIndex();
        if (cap >= 0 && (originalLine == null || cap != originalLine.startCapStyle)) {
            line.startCapStyle = cap;
            line.endCapStyle = cap;
        }
        if (joinComboBox.getSelectedIndex() >= 0) {
            line.joinStyle = joinComboBox.getSelectedIndex();
        }
        if (miterEditor.getValue() != null && (originalLine == null || line.joinStyle == LINESTYLE2.MITER_JOIN)) {
            line.miterLimitFactor = miterEditor.getValue();
        }
        line.hasFillFlag = fill.fillStyleType != FILLSTYLE.SOLID;
        line.color = line.hasFillFlag ? null : new RGBA(fill.color);
        line.fillType = line.hasFillFlag ? fill : null;
        return line;
    }

    private static JComboBox<String> createComboBox(String property, String... values) {
        JComboBox<String> comboBox = new JComboBox<>();
        comboBox.setRenderer(indeterminateRenderer());
        for (String value : values) {
            comboBox.addItem(EasyStrings.translate("property.shape." + property + "." + value));
        }
        return comboBox;
    }

    private static DefaultListCellRenderer indeterminateRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focus) {
                return super.getListCellRendererComponent(list, value == null ? "-" : value, index, selected, focus);
            }
        };
    }

    private static LINESTYLE2 lineStyle2(ILINESTYLE line) {
        return line instanceof LINESTYLE2 ? Helper.deepCopy((LINESTYLE2) line) : ((LINESTYLE) line).toLineStyle2();
    }

    private static FILLSTYLE lineFill(ILINESTYLE line) {
        if (line == null) {
            return null;
        }
        if (line instanceof LINESTYLE2 && ((LINESTYLE2) line).hasFillFlag) {
            return ((LINESTYLE2) line).fillType;
        }
        FILLSTYLE fill = new FILLSTYLE();
        fill.color = new RGBA(line.getColor());
        return fill;
    }

    private static <T, V> V common(List<T> values, Function<T, V> getter) {
        if (values.isEmpty()) {
            return null;
        }
        V first = getter.apply(values.get(0));
        for (T value : values) {
            if (!Objects.equals(first, getter.apply(value))) {
                return null;
            }
        }
        return first;
    }

    public void setSelections(List<ShapeSelection.Selection> selections) {
        if (selections.isEmpty()) {
            setSelectedFill(null);
            return;
        }
        List<FILLSTYLE> fills = new ArrayList<>();
        List<ILINESTYLE> lines = new ArrayList<>();
        Rectangle2D bounds = null;
        for (ShapeSelection.Selection item : selections) {
            if (item.stroke) {
                lines.add(item.line);
            } else {
                fills.add(item.fill);
            }
            Rectangle2D current = item.geometry.getBounds2D();
            bounds = bounds == null ? current : bounds.createUnion(current);
        }
        boolean wasUpdating = updating;
        updating = true;
        try {
            Boolean kind = fills.isEmpty() ? Boolean.TRUE : lines.isEmpty() ? Boolean.FALSE : null;
            setSelectedStyles(fills.isEmpty() ? null : fills.get(0), lines.isEmpty() ? null : lines.get(0),
                    bounds, kind);
            fillEditor.setFillStyles(fills);
            List<FILLSTYLE> paints = new ArrayList<>();
            for (ILINESTYLE line : lines) {
                paints.add(lineFill(line));
            }
            strokeEditor.setFillStyles(paints);
            if (!lines.isEmpty()) {
                HashSet<Float> widths = new HashSet<>();
                HashSet<Float> miters = new HashSet<>();
                for (ILINESTYLE line : lines) {
                    widths.add(line == null ? 1f : line.getWidth() / (float) SWF.unitDivisor);
                    miters.add(line instanceof LINESTYLE2 && ((LINESTYLE2) line).joinStyle == LINESTYLE2.MITER_JOIN
                            ? ((LINESTYLE2) line).miterLimitFactor : 3f);
                }
                widthEditor.setValue(widths, true);
                miterEditor.setValue(miters, true);
                Integer scale = common(lines, line -> line instanceof LINESTYLE2
                        ? (((LINESTYLE2) line).noHScaleFlag ? 2 : 0) + (((LINESTYLE2) line).noVScaleFlag ? 1 : 0) : 0);
                Integer cap = common(lines, line -> line instanceof LINESTYLE2 ? ((LINESTYLE2) line).startCapStyle : 0);
                Integer join = common(lines, line -> line instanceof LINESTYLE2 ? ((LINESTYLE2) line).joinStyle : 0);
                scaleComboBox.setSelectedIndex(scale == null ? -1 : scale);
                capComboBox.setSelectedIndex(cap == null ? -1 : cap);
                joinComboBox.setSelectedIndex(join == null ? -1 : join);
            }
        } finally {
            updating = wasUpdating;
        }
    }

    private void addRow(JPanel panel, String property, Component editor, int row) {
        GridBagLayout layout = (GridBagLayout) panel.getLayout();
        addToGrid(layout, panel, new JLabel(formatPropertyName(property)), 0, row);
        addToGrid(layout, panel, editor, 1, row);
    }

    public void setShape(ShapeTag shape) {
        updating = true;
        try {
            SWF swf = shape == null ? null : shape.getSwf();
            fillEditor.setSwf(swf);
            strokeEditor.setSwf(swf);
            setSelectedFill(null);
        } finally {
            updating = false;
        }
    }

    public void setSelectedFill(FILLSTYLE fill) {
        setSelectedStyles(fill, null, null);
    }

    public void setSelectedStroke(ILINESTYLE line) {
        setSelectedStyles(null, line, null);
    }

    public void setSelectedStyles(FILLSTYLE fill, ILINESTYLE line, Shape geometry) {
        setSelectedStyles(fill, line, geometry, line != null ? Boolean.TRUE : fill != null ? Boolean.FALSE : null);
    }

    public void setSelectedStyles(FILLSTYLE fill, ILINESTYLE line, Shape geometry, boolean stroke) {
        setSelectedStyles(fill, line, geometry, Boolean.valueOf(stroke));
    }

    private void setSelectedStyles(FILLSTYLE fill, ILINESTYLE line, Shape geometry, Boolean stroke) {
        boolean wasUpdating = updating;
        updating = true;
        try {
            selectedStroke = stroke;
            if (geometry != null) {
                selectedBounds = geometry.getBounds2D();
            }
            fillEditor.setFillStyle(Boolean.TRUE.equals(stroke) ? null : fill);
            setLineStyle(Boolean.FALSE.equals(stroke) ? null : line);
            boolean fillEnabled = !Boolean.TRUE.equals(stroke);
            boolean strokeEnabled = !Boolean.FALSE.equals(stroke);
            setEnabledRecursively(fillPanel, fillEnabled);
            setEnabledRecursively(strokePanel, strokeEnabled);
            fillEditor.bitmapComboBox.setEnabled(fillEnabled && fillEditor.bitmapComboBox.getItemCount() > 0);
            strokeEditor.bitmapComboBox.setEnabled(strokeEnabled && strokeEditor.bitmapComboBox.getItemCount() > 0);
        } finally {
            updating = wasUpdating;
        }
    }

    private static void setEnabledRecursively(Component component, boolean enabled) {
        component.setEnabled(enabled);
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                setEnabledRecursively(child, enabled);
            }
        }
    }

    private void setLineStyle(ILINESTYLE line) {
        originalLine = line == null ? null : line instanceof LINESTYLE2 ? Helper.deepCopy((LINESTYLE2) line) : ((LINESTYLE) line).toLineStyle2();
        strokeEditor.setFillStyle(line instanceof LINESTYLE2 && ((LINESTYLE2) line).hasFillFlag ? ((LINESTYLE2) line).fillType : null);
        if (line != null && line.getColor() != null) {
            FILLSTYLE solid = new FILLSTYLE();
            solid.color = new RGBA(line.getColor());
            strokeEditor.setFillStyle(solid);
        }
        widthEditor.setValue(line == null ? 1f : line.getWidth() / (float) SWF.unitDivisor);
        LINESTYLE2 line2 = line instanceof LINESTYLE2 ? (LINESTYLE2) line : null;
        scaleComboBox.setSelectedIndex(line2 == null ? 0 : (line2.noHScaleFlag ? 2 : 0) + (line2.noVScaleFlag ? 1 : 0));
        capComboBox.setSelectedIndex(line2 == null ? 0 : line2.startCapStyle);
        joinComboBox.setSelectedIndex(line2 == null ? 0 : line2.joinStyle);
        miterEditor.setValue(line2 == null || line2.joinStyle != LINESTYLE2.MITER_JOIN ? 3f : line2.miterLimitFactor);
    }

    private static class FillEditor extends JPanel {

        private final JComboBox<String> typeComboBox = createComboBox("fill", "none", "solid", "linearGradient", "radialGradient", "bitmap");
        private final JPanel valuePanel = new JPanel(new CardLayout());
        private final JButton colorButton = new JButton();
        private final JPanel gradientPanel = new JPanel(new BorderLayout());
        private final JComboBox<ComboBoxItem<Integer>> bitmapComboBox = new JComboBox<>();
        private final Color defaultColor;
        private Color color;
        private FILLSTYLE originalStyle;
        private GRADIENT gradient;
        private GradientEditor gradientEditor;
        private Consumer<String> changeListener = property -> { };

        public FillEditor(Color defaultColor) {
            this.defaultColor = defaultColor;
            setLayout(new BorderLayout(0, 5));
            add(typeComboBox, BorderLayout.NORTH);
            valuePanel.add(new JPanel(), "none");
            valuePanel.add(colorButton, "solid");
            valuePanel.add(gradientPanel, "gradient");
            valuePanel.add(bitmapComboBox, "bitmap");
            valuePanel.add(new JLabel("-"), "indeterminate");
            add(valuePanel, BorderLayout.CENTER);
            bitmapComboBox.setPrototypeDisplayValue(new ComboBoxItem<>("Bitmap (65535)", -1));
            bitmapComboBox.setRenderer(indeterminateRenderer());
            typeComboBox.addActionListener(e -> {
                int index = typeComboBox.getSelectedIndex();
                String card = index < 0 ? "indeterminate" : index == 0 ? "none" : index == 1 ? "solid" : index == 4 ? "bitmap" : "gradient";
                ((CardLayout) valuePanel.getLayout()).show(valuePanel, card);
                valuePanel.setVisible(index != 0);
                revalidate();
                repaint();
                changeListener.accept("type");
            });
            bitmapComboBox.addActionListener(e -> changeListener.accept("bitmap"));
            colorButton.addActionListener(e -> {
                Color selected = ViewMessages.showColorDialog(this, color, true);
                if (selected != null) {
                    updateColor(selected);
                    changeListener.accept("color");
                }
            });
            setFillStyle(null);
        }

        private void updateColor(Color color) {
            this.color = color;
            colorButton.setText(String.format("#%02x%02x%02x (%d%%)", color.getRed(), color.getGreen(), color.getBlue(), Math.round(color.getAlpha() * 100f / 255)));
            // Paint the swatch independently of the look and feel's button colors.
            BufferedImage swatch = new BufferedImage(18, 18, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = swatch.createGraphics();
            try {
                for (int y = 1; y < 17; y += 4) {
                    for (int x = 1; x < 17; x += 4) {
                        graphics.setColor(((x / 4 + y / 4) % 2) == 0 ? Color.white : Color.lightGray);
                        graphics.fillRect(x, y, 4, 4);
                    }
                }
                graphics.setColor(color);
                graphics.fillRect(1, 1, 16, 16);
                graphics.setColor(Color.gray);
                graphics.drawRect(0, 0, 17, 17);
            } finally {
                graphics.dispose();
            }
            colorButton.setIcon(new ImageIcon(swatch));
        }

        public void setSolidColor(Color color) {
            updateColor(color);
            typeComboBox.setSelectedIndex(1);
        }

        public void setFillStyle(FILLSTYLE style) {
            originalStyle = style == null ? null : style.toShapeNum(4);
            updateColor(style != null && style.color != null ? style.color.toColor() : defaultColor);
            // The gradient editor only receives a detached copy of the style.
            gradient = style != null && style.gradient != null ? Helper.deepCopy(style.gradient) : new GRADIENT();
            gradientEditor = new GradientEditor(gradient);
            gradientEditor.addChangeListener(new ChangeListener() {
                @Override
                public void change(PropertyEditor editor) {
                    editor.save();
                    changeListener.accept("gradient");
                }

                @Override
                public void linkChanged(boolean newValue) {
                }
            });
            gradientPanel.removeAll();
            gradientPanel.add(gradientEditor, BorderLayout.CENTER);
            int type = 0;
            if (style != null) {
                switch (style.fillStyleType) {
                    case FILLSTYLE.SOLID:
                        type = 1;
                        break;
                    case FILLSTYLE.LINEAR_GRADIENT:
                        type = 2;
                        break;
                    case FILLSTYLE.RADIAL_GRADIENT:
                    case FILLSTYLE.FOCAL_RADIAL_GRADIENT:
                        type = 3;
                        break;
                    default:
                        if (style.hasBitmap()) {
                            type = 4;
                            for (int i = 0; i < bitmapComboBox.getItemCount(); i++) {
                                if (bitmapComboBox.getItemAt(i).getValue() == style.bitmapId) {
                                    bitmapComboBox.setSelectedIndex(i);
                                    break;
                                }
                            }
                        }
                        break;
                }
            }
            typeComboBox.setSelectedIndex(type);
        }

        public FILLSTYLE getFillStyle(Rectangle2D bounds) {
            int type = typeComboBox.getSelectedIndex();
            if (type < 0) {
                return Helper.deepCopy(originalStyle);
            }
            if (type == 0) {
                return null;
            }
            if (type == 4 && bitmapComboBox.getSelectedIndex() < 0) {
                return Helper.deepCopy(originalStyle);
            }
            FILLSTYLE style = originalStyle == null ? new FILLSTYLE() : Helper.deepCopy(originalStyle);
            style.inShape3 = true;
            if (type == 1) {
                style.fillStyleType = FILLSTYLE.SOLID;
                style.color = new RGBA(color);
            } else if (type == 2 || type == 3) {
                style.fillStyleType = type == 2 ? FILLSTYLE.LINEAR_GRADIENT
                        : originalStyle != null && originalStyle.fillStyleType == FILLSTYLE.FOCAL_RADIAL_GRADIENT
                                ? FILLSTYLE.FOCAL_RADIAL_GRADIENT : FILLSTYLE.RADIAL_GRADIENT;
                gradientEditor.save();
                style.gradient = Helper.deepCopy(gradient);
                if (style.gradientMatrix == null) {
                    MATRIX matrix = new MATRIX();
                    matrix.hasScale = true;
                    matrix.scaleX = (float) (Math.max(20, bounds.getWidth()) / 32768);
                    matrix.scaleY = (float) (Math.max(20, bounds.getHeight()) / 32768);
                    matrix.translateX = (int) Math.round(bounds.getCenterX());
                    matrix.translateY = (int) Math.round(bounds.getCenterY());
                    style.gradientMatrix = matrix;
                }
            } else {
                style.fillStyleType = originalStyle != null && originalStyle.hasBitmap() ? originalStyle.fillStyleType : FILLSTYLE.REPEATING_BITMAP;
                style.bitmapId = bitmapComboBox.getItemAt(bitmapComboBox.getSelectedIndex()).getValue();
                if (style.bitmapMatrix == null) {
                    MATRIX matrix = new MATRIX();
                    matrix.hasScale = true;
                    matrix.scaleX = (float) SWF.unitDivisor;
                    matrix.scaleY = (float) SWF.unitDivisor;
                    style.bitmapMatrix = matrix;
                }
            }
            return style;
        }

        private static int styleType(FILLSTYLE style) {
            if (style == null) {
                return 0;
            }
            if (style.hasGradient()) {
                return style.fillStyleType == FILLSTYLE.LINEAR_GRADIENT ? 2 : 3;
            }
            return style.hasBitmap() ? 4 : 1;
        }

        private static List<Object> gradientStops(FILLSTYLE style) {
            List<Object> stops = new ArrayList<>();
            if (style != null && style.gradient != null) {
                for (GRADRECORD stop : style.gradient.gradientRecords) {
                    stops.add(stop.ratio);
                    stops.add(stop.color.toColor());
                }
            }
            return stops;
        }

        public void setFillStyles(List<FILLSTYLE> styles) {
            setFillStyle(styles.isEmpty() ? null : styles.get(0));
            if (styles.isEmpty()) {
                return;
            }
            Integer type = common(styles, FillEditor::styleType);
            typeComboBox.setSelectedIndex(type == null ? -1 : type);
            if (type != null && type == 1 && common(styles, style -> style.color.toColor()) == null) {
                colorButton.setText("-");
                colorButton.setIcon(null);
            }
            if (type != null && (type == 2 || type == 3)) {
                gradientEditor.setIndeterminate(common(styles, FillEditor::gradientStops) == null);
            }
            if (type != null && type == 4) {
                Integer bitmap = common(styles, style -> style.bitmapId);
                bitmapComboBox.setSelectedIndex(-1);
                if (bitmap != null) {
                    for (int i = 0; i < bitmapComboBox.getItemCount(); i++) {
                        if (bitmapComboBox.getItemAt(i).getValue().equals(bitmap)) {
                            bitmapComboBox.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            }
        }

        public FILLSTYLE applyChange(FILLSTYLE original, Rectangle2D bounds, String property) {
            FILLSTYLE base = original == null ? null : original.toShapeNum(4);
            if (property.equals("color")) {
                if (base != null && base.fillStyleType == FILLSTYLE.SOLID) {
                    base.color = new RGBA(color);
                }
            } else if (property.equals("gradient")) {
                if (base != null && base.hasGradient()) {
                    gradientEditor.save();
                    base.gradient.gradientRecords = Helper.deepCopy(gradient.gradientRecords);
                }
            } else if (property.equals("bitmap")) {
                if (base != null && base.hasBitmap() && bitmapComboBox.getSelectedIndex() >= 0) {
                    base.bitmapId = bitmapComboBox.getItemAt(bitmapComboBox.getSelectedIndex()).getValue();
                }
            } else if (property.equals("type")) {
                FILLSTYLE template = getFillStyle(bounds);
                if (template == null) {
                    return null;
                }
                FILLSTYLE result = base == null ? new FILLSTYLE() : base;
                result.inShape3 = true;
                int type = typeComboBox.getSelectedIndex();
                if (type == 1) {
                    result.color = original != null && original.fillStyleType == FILLSTYLE.SOLID
                            ? new RGBA(original.color) : new RGBA(template.color);
                    result.fillStyleType = FILLSTYLE.SOLID;
                } else if (type == 2 || type == 3) {
                    result.fillStyleType = type == 2 ? FILLSTYLE.LINEAR_GRADIENT
                            : original != null && original.fillStyleType == FILLSTYLE.FOCAL_RADIAL_GRADIENT
                                    ? FILLSTYLE.FOCAL_RADIAL_GRADIENT : FILLSTYLE.RADIAL_GRADIENT;
                    result.gradient = original != null && original.gradient != null ? Helper.deepCopy(original.gradient) : template.gradient;
                    result.gradientMatrix = original != null && original.gradientMatrix != null ? Helper.deepCopy(original.gradientMatrix) : template.gradientMatrix;
                } else if (type == 4) {
                    result.fillStyleType = original != null && original.hasBitmap() ? original.fillStyleType : template.fillStyleType;
                    result.bitmapId = original != null && original.hasBitmap() ? original.bitmapId : template.bitmapId;
                    result.bitmapMatrix = original != null && original.bitmapMatrix != null ? Helper.deepCopy(original.bitmapMatrix) : template.bitmapMatrix;
                }
                return result;
            }
            return base;
        }

        public void setSwf(SWF swf) {
            bitmapComboBox.removeAllItems();
            if (swf != null) {
                List<CharacterTag> images = new ArrayList<>();
                for (CharacterTag character : swf.getCharacters(false).values()) {
                    if (character instanceof ImageTag) {
                        images.add(character);
                    }
                }
                images.sort(Comparator.comparingInt(CharacterTag::getCharacterId));
                EasyTagNameResolver resolver = new EasyTagNameResolver();
                for (CharacterTag image : images) {
                    bitmapComboBox.addItem(new ComboBoxItem<>(resolver.getTagName(image), image.getCharacterId()));
                }
            }
            bitmapComboBox.setEnabled(bitmapComboBox.getItemCount() > 0);
        }
    }
}
