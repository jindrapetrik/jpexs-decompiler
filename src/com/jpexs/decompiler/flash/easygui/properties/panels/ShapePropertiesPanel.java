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
import com.jpexs.decompiler.flash.easygui.properties.JTriStateCheckBox;
import com.jpexs.decompiler.flash.easygui.properties.PropertyEditor;
import com.jpexs.decompiler.flash.easygui.properties.ShapeColorEditor;
import com.jpexs.decompiler.flash.gui.ComboBoxItem;
import com.jpexs.decompiler.flash.gui.FasterScrollPane;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.gui.generictageditors.ChangeListener;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.FOCALGRADIENT;
import com.jpexs.decompiler.flash.types.GRADIENT;
import com.jpexs.decompiler.flash.types.GRADRECORD;
import com.jpexs.decompiler.flash.types.ILINESTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.SerializableImage;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GrayFilter;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

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
    private final JPanel styleCards = new JPanel(new VisibleCardLayout());
    private final JPanel optionCards = new JPanel(new VisibleCardLayout());
    private final JToggleButton fillTab = styleTab("fill", "shapetoolpaint16", fillEditor);
    private final JToggleButton strokeTab = styleTab("stroke", "shapetoolpencil16", strokeEditor);
    private boolean updating;
    private Boolean selectedStroke;
    private Consumer<Boolean> changeListener;
    private BiConsumer<Boolean, String> propertyChangeListener;
    private LINESTYLE2 originalLine;
    private Rectangle2D selectedBounds = new Rectangle2D.Double(0, 0, 2000, 2000);

    public ShapePropertiesPanel() {
        this(false);
    }

    /**
     * Creates either the selection editor or persistent Pencil stroke settings.
     */
    public ShapePropertiesPanel(boolean strokeOnly) {
        this(strokeOnly, false, strokeOnly);
    }

    private ShapePropertiesPanel(boolean strokeOnly, boolean fillOnly, boolean drawing) {
        super("shape");
        setLayout(new BorderLayout());
        if (!drawing) {
            JLabel title = new JLabel(EasyStrings.translate("properties.shape"));
            title.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
            add(title, BorderLayout.NORTH);
        }

        addToGrid((GridBagLayout) strokePanel.getLayout(), strokePanel, strokeEditor, 0, 0, 2, 1);
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

        JPanel tabs = new JPanel(new GridLayout(0, 1, 0, 3));
        ButtonGroup group = new ButtonGroup();
        if (!strokeOnly) {
            addToGrid((GridBagLayout) fillPanel.getLayout(), fillPanel, fillEditor, 0, 0, 2, 1);
            styleCards.add(fillPanel, "fill");
            optionCards.add(fillEditor.paintOptions, "fill");
            group.add(fillTab);
            fillTab.addActionListener(e -> showStyleTab(false));
            tabs.add(fillTab);
        }
        if (!fillOnly) {
            styleCards.add(strokePanel, "stroke");
            optionCards.add(strokeEditor.paintOptions, "stroke");
            group.add(strokeTab);
            strokeTab.addActionListener(e -> showStyleTab(true));
            tabs.add(strokeTab);
        }
        JPanel styleSection = createStyleSection(tabs, optionCards, styleCards);
        showStyleTab(strokeOnly);
        JPanel contents = new JPanel(new GridBagLayout());
        GridBagConstraints sectionConstraints = new GridBagConstraints();
        sectionConstraints.weighty = 1;
        sectionConstraints.anchor = GridBagConstraints.NORTH;
        sectionConstraints.insets = new Insets(3, 3, 3, 3);
        contents.add(styleSection, sectionConstraints);
        add(new FasterScrollPane(contents), BorderLayout.CENTER);
        fillEditor.changeListener = property -> fireChange(false, property);
        strokeEditor.changeListener = property -> fireChange(true, property);
        widthEditor.addChangeListener(e -> fireChange(true, "width"));
        miterEditor.addChangeListener(e -> fireChange(true, "miter"));
        scaleComboBox.addActionListener(e -> fireChange(true, "scale"));
        capComboBox.addActionListener(e -> fireChange(true, "cap"));
        joinComboBox.addActionListener(e -> fireChange(true, "join"));
        if (drawing) {
            LINESTYLE2 line = new LINESTYLE2();
            line.color = new RGBA(Color.black);
            line.width = (int) SWF.unitDivisor;
            line.miterLimitFactor = 3;
            line.noClose = strokeOnly;
            if (strokeOnly) {
                setSelectedStroke(line);
            } else if (fillOnly) {
                FILLSTYLE fill = new FILLSTYLE();
                fill.color = new RGBA(Color.black);
                setSelectedFill(fill);
            } else {
                FILLSTYLE fill = new FILLSTYLE();
                fill.color = new RGBA(Color.white);
                setSelectedStyles(fill, line, null, (Boolean) null);
            }
        }
    }

    static JPanel createStyleSection(Component tabs, JPanel options, JPanel body) {
        JPanel tabColumn = new JPanel(new BorderLayout());
        tabColumn.add(tabs, BorderLayout.NORTH);
        JPanel styleHeader = new JPanel(new BorderLayout(4, 0));
        styleHeader.add(tabColumn, BorderLayout.WEST);
        styleHeader.add(options, BorderLayout.CENTER);
        JPanel styleSection = new JPanel(new BorderLayout(0, 5));
        styleSection.add(styleHeader, BorderLayout.NORTH);
        JPanel insetBody = new JPanel(new BorderLayout());
        insetBody.setBorder(BorderFactory.createEmptyBorder(0, tabs.getPreferredSize().width / 2, 0, 0));
        insetBody.add(body, BorderLayout.CENTER);
        styleSection.add(insetBody, BorderLayout.EAST);
        return styleSection;
    }

    static JToggleButton styleTab(String name, String icon, FillEditor editor) {
        String label = EasyStrings.translate("properties.shape.header." + name);
        ImageIcon tool = View.getIcon(icon, 16);
        ImageIcon disabledTool = new ImageIcon(GrayFilter.createDisabledImage(tool.getImage()));
        int swatchWidth = 32;
        int swatchHeight = 16;
        int swatchOffset = tool.getIconWidth() + 5;
        Icon tabIcon = new Icon() {
            @Override
            public int getIconWidth() {
                return swatchOffset + swatchWidth;
            }

            @Override
            public int getIconHeight() {
                return swatchHeight;
            }

            @Override
            public void paintIcon(Component component, Graphics graphics, int x, int y) {
                (component.isEnabled() ? tool : disabledTool).paintIcon(component, graphics, x, y);
            }
        };
        JToggleButton tab = new JToggleButton(tabIcon) {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                // Keep the live swatch out of the look and feel's icon effects and cache.
                Insets insets = getInsets();
                int x = insets.left + (getWidth() - insets.left - insets.right - tabIcon.getIconWidth()) / 2;
                int y = insets.top + (getHeight() - insets.top - insets.bottom - tabIcon.getIconHeight()) / 2;
                Graphics2D swatch = (Graphics2D) graphics.create(x + swatchOffset, y, swatchWidth, swatchHeight);
                try {
                    editor.paintSwatch(swatch, swatchWidth, swatchHeight);
                } finally {
                    swatch.dispose();
                }
            }
        };
        tab.setDisabledIcon(tabIcon);
        tab.setMargin(new Insets(4, 9, 4, 9));
        tab.setToolTipText(label);
        tab.getAccessibleContext().setAccessibleName(label);
        return tab;
    }

    private void showStyleTab(boolean stroke) {
        (stroke ? strokeTab : fillTab).setSelected(true);
        String card = stroke ? "stroke" : "fill";
        ((CardLayout) optionCards.getLayout()).show(optionCards, card);
        ((CardLayout) styleCards.getLayout()).show(styleCards, card);
    }

    private static class VisibleCardLayout extends CardLayout {

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            for (Component card : parent.getComponents()) {
                if (card.isVisible()) {
                    Dimension preferred = card.getPreferredSize();
                    Insets insets = parent.getInsets();
                    return new Dimension(preferred.width + insets.left + insets.right,
                            preferred.height + insets.top + insets.bottom);
                }
            }
            return super.preferredLayoutSize(parent);
        }
    }

    public static ShapePropertiesPanel createDrawingStyles() {
        return new ShapePropertiesPanel(false, false, true);
    }

    public static ShapePropertiesPanel createFillSettings() {
        return new ShapePropertiesPanel(false, true, true);
    }

    public void setChangeListener(Consumer<Boolean> listener) {
        changeListener = listener;
    }

    public void setPropertyChangeListener(BiConsumer<Boolean, String> listener) {
        propertyChangeListener = listener;
    }

    private void fireChange(boolean stroke, String property) {
        (stroke ? strokeTab : fillTab).repaint();
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
        if (Arrays.asList("type", "color", "gradient", "flow", "linearRGB", "focalPoint", "bitmap", "smoothed", "repeat").contains(property)) {
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
        return getFillStyle(selectedBounds);
    }

    public FILLSTYLE getFillStyle(Rectangle2D bounds) {
        return Boolean.TRUE.equals(selectedStroke) ? null : fillEditor.getFillStyle(bounds);
    }

    public LINESTYLE2 getStrokeStyle() {
        return getStrokeStyle(selectedBounds);
    }

    public LINESTYLE2 getStrokeStyle(Rectangle2D bounds) {
        return getStrokeStyle(bounds, false);
    }

    private LINESTYLE2 getStrokeStyle(Rectangle2D bounds, boolean allowNone) {
        if (Boolean.FALSE.equals(selectedStroke) && !allowNone) {
            return null;
        }
        FILLSTYLE fill = strokeEditor.getFillStyle(bounds);
        if (fill == null && !allowNone) {
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
        line.hasFillFlag = fill != null && fill.fillStyleType != FILLSTYLE.SOLID;
        line.color = fill == null || line.hasFillFlag ? null : new RGBA(fill.color);
        line.fillType = line.hasFillFlag ? fill : null;
        return line;
    }

    /**
     * Captures drawing settings without generating a geometry-dependent matrix.
     * A stroke with no paint still retains its width, caps and other settings.
     */
    public LINESTYLE2 getDrawingStrokeStyle() {
        LINESTYLE2 line = getStrokeStyle(null, true);
        if (miterEditor.getValue() != null) {
            line.miterLimitFactor = miterEditor.getValue();
        }
        return line;
    }

    public void setDrawingFillStyle(FILLSTYLE fill) {
        boolean wasUpdating = updating;
        updating = true;
        try {
            fillEditor.setFillStyle(fill);
        } finally {
            updating = wasUpdating;
        }
    }

    public void setDrawingStrokeStyle(LINESTYLE2 line) {
        boolean wasUpdating = updating;
        updating = true;
        try {
            setLineStyle(line);
            miterEditor.setValue(line.miterLimitFactor);
        } finally {
            updating = wasUpdating;
        }
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
            updateStyleEnabled();
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

    /**
     * Refreshes available bitmaps without resetting drawing style settings.
     */
    public void setDrawingSwf(SWF swf) {
        boolean wasUpdating = updating;
        updating = true;
        try {
            fillEditor.setSwf(swf);
            if (!fillEditor.isFillValid()) {
                fillEditor.setSolidColor(Color.white);
            }
            strokeEditor.setSwf(swf);
            if (!strokeEditor.isFillValid()) {
                strokeEditor.setSolidColor(Color.black);
            }
        } finally {
            updating = wasUpdating;
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
            updateStyleEnabled();
        } finally {
            updating = wasUpdating;
        }
    }

    private void updateStyleEnabled() {
        boolean fillEnabled = !Boolean.TRUE.equals(selectedStroke);
        boolean strokeEnabled = !Boolean.FALSE.equals(selectedStroke);
        setEnabledRecursively(fillPanel, fillEnabled);
        setEnabledRecursively(strokePanel, strokeEnabled);
        setEnabledRecursively(fillEditor.paintOptions, fillEnabled);
        setEnabledRecursively(strokeEditor.paintOptions, strokeEnabled);
        fillTab.setEnabled(fillEnabled);
        strokeTab.setEnabled(strokeEnabled);
        fillTab.repaint();
        strokeTab.repaint();
        if (selectedStroke != null) {
            showStyleTab(selectedStroke);
        }
        fillEditor.bitmapComboBox.setEnabled(fillEnabled && fillEditor.bitmapComboBox.getItemCount() > 0);
        strokeEditor.bitmapComboBox.setEnabled(strokeEnabled && strokeEditor.bitmapComboBox.getItemCount() > 0);
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

    static class FillEditor extends JPanel {

        private static final int BITMAP_PREVIEW_SIZE = 32;

        private final JComboBox<String> typeComboBox = createComboBox("fill", "none", "solid", "linearGradient", "radialGradient", "bitmap");
        final JPanel paintOptions = new JPanel(new BorderLayout(0, 5));
        private final JPanel gradientOptions = new JPanel(new GridBagLayout());
        private final JPanel valuePanel = new JPanel(new VisibleCardLayout());
        private final ShapeColorEditor colorEditor = new ShapeColorEditor(this::changeColor);
        private final JPanel gradientPanel = new JPanel(new BorderLayout());
        private final ButtonGroup flowGroup = new ButtonGroup();
        private final JToggleButton[] flowButtons = new JToggleButton[3];
        private final JTriStateCheckBox linearRGBCheckBox = new JTriStateCheckBox(EasyStrings.translate("property.shape.linearRGB"));
        private final JTriStateCheckBox focalPointCheckBox = new JTriStateCheckBox(EasyStrings.translate("property.shape.focalPoint"));
        private final JComboBox<ComboBoxItem<Integer>> bitmapComboBox = new JComboBox<>();
        private final Map<Integer, ImageIcon> bitmapIcons = new HashMap<>();
        private SWF bitmapSwf;
        private final JTriStateCheckBox smoothedCheckBox = new JTriStateCheckBox(EasyStrings.translate("property.shape.smoothed"));
        private final JTriStateCheckBox repeatCheckBox = new JTriStateCheckBox(EasyStrings.translate("property.shape.repeat"));
        private final Color defaultColor;
        private final boolean allowNone;
        private Color color;
        private FILLSTYLE originalStyle;
        private GRADIENT gradient = new GRADIENT();
        private final GradientEditor gradientEditor = GradientEditor.createInline(gradient);
        private Consumer<String> changeListener = property -> { };

        public FillEditor(Color defaultColor) {
            this(defaultColor, true);
        }

        FillEditor(Color defaultColor, boolean allowNone) {
            this.defaultColor = defaultColor;
            this.allowNone = allowNone;
            if (!allowNone) {
                typeComboBox.removeItemAt(0);
            }
            setLayout(new BorderLayout(0, 5));
            typeComboBox.getAccessibleContext().setAccessibleName(EasyStrings.translate("property.shape.type"));
            paintOptions.add(typeComboBox, BorderLayout.NORTH);
            add(paintOptions, BorderLayout.NORTH);
            valuePanel.add(new JPanel(), "none");
            valuePanel.add(colorEditor, "solid");
            valuePanel.add(gradientPanel, "gradient");
            JPanel bitmapPanel = new JPanel(new BorderLayout(0, 5));
            bitmapPanel.add(bitmapComboBox, BorderLayout.NORTH);
            JPanel bitmapOptions = new JPanel(new BorderLayout());
            bitmapOptions.add(smoothedCheckBox, BorderLayout.NORTH);
            bitmapOptions.add(repeatCheckBox, BorderLayout.SOUTH);
            bitmapPanel.add(bitmapOptions, BorderLayout.SOUTH);
            valuePanel.add(bitmapPanel, "bitmap");
            valuePanel.add(new JLabel("-"), "indeterminate");
            add(valuePanel, BorderLayout.CENTER);
            GridBagConstraints optionConstraints = new GridBagConstraints();
            optionConstraints.gridx = 0;
            optionConstraints.gridy = 0;
            optionConstraints.anchor = GridBagConstraints.WEST;
            gradientOptions.add(new JLabel(EasyStrings.translate("property.shape.flow") + ": "), optionConstraints);
            optionConstraints.gridx = 1;
            JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
            String[] flowNames = {"pad", "reflect", "repeat"};
            CycleMethod[] flowCycles = {CycleMethod.NO_CYCLE, CycleMethod.REFLECT, CycleMethod.REPEAT};
            for (int i = 0; i < flowButtons.length; i++) {
                JToggleButton button = new JToggleButton(flowIcon(flowCycles[i]));
                String name = EasyStrings.translate("property.shape.flow." + flowNames[i]);
                button.setToolTipText(name);
                button.getAccessibleContext().setAccessibleName(name);
                button.setMargin(new Insets(3, 4, 3, 4));
                button.addActionListener(e -> changeListener.accept("flow"));
                flowButtons[i] = button;
                flowGroup.add(button);
                flowPanel.add(button);
            }
            optionConstraints.weightx = 1;
            gradientOptions.add(flowPanel, optionConstraints);
            optionConstraints.gridx = 0;
            optionConstraints.gridy = 1;
            optionConstraints.gridwidth = 2;
            gradientOptions.add(linearRGBCheckBox, optionConstraints);
            optionConstraints.gridy = 2;
            gradientOptions.add(focalPointCheckBox, optionConstraints);
            paintOptions.add(gradientOptions, BorderLayout.CENTER);
            gradientPanel.add(gradientEditor, BorderLayout.CENTER);
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
            linearRGBCheckBox.addActionListener(e -> {
                gradientEditor.setLinearRGB(linearRGBCheckBox.isSelected());
                changeListener.accept("linearRGB");
            });
            focalPointCheckBox.addActionListener(e -> changeListener.accept("focalPoint"));
            smoothedCheckBox.addActionListener(e -> changeListener.accept("smoothed"));
            repeatCheckBox.addActionListener(e -> changeListener.accept("repeat"));
            bitmapComboBox.setPrototypeDisplayValue(new ComboBoxItem<>("Bitmap (65535)", -1));
            bitmapComboBox.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focus) {
                    JLabel label = (JLabel) super.getListCellRendererComponent(list, value == null ? "-" : value, index, selected, focus);
                    label.setIcon(value instanceof ComboBoxItem ? bitmapIcon((Integer) ((ComboBoxItem<?>) value).getValue()) : null);
                    label.setBorder(BorderFactory.createCompoundBorder(label.getBorder(), BorderFactory.createEmptyBorder(3, 2, 3, 2)));
                    return label;
                }
            });
            typeComboBox.addActionListener(e -> {
                int index = getType();
                String card = index < 0 ? "indeterminate" : index == 0 ? "none" : index == 1 ? "solid" : index == 4 ? "bitmap" : "gradient";
                ((CardLayout) valuePanel.getLayout()).show(valuePanel, card);
                valuePanel.setVisible(index != 0);
                gradientOptions.setVisible(index == 2 || index == 3);
                focalPointCheckBox.setVisible(index == 3);
                revalidate();
                repaint();
                changeListener.accept("type");
            });
            bitmapComboBox.addActionListener(e -> changeListener.accept("bitmap"));
            setFillStyle(null);
        }

        private static ImageIcon flowIcon(CycleMethod cycle) {
            int width = 28;
            int height = 14;
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            try {
                graphics.setPaint(new LinearGradientPaint(9, 0, 18, 0,
                        new float[]{0, 1}, new Color[]{Color.black, Color.white}, cycle));
                graphics.fillRect(0, 0, width, height);
                graphics.setColor(Color.gray);
                graphics.drawRect(0, 0, width - 1, height - 1);
            } finally {
                graphics.dispose();
            }
            return new ImageIcon(image);
        }

        private void setFlow(int flow) {
            if (flow >= 0 && flow < flowButtons.length) {
                flowButtons[flow].setSelected(true);
            } else {
                flowGroup.clearSelection();
            }
        }

        private int getFlow() {
            for (int i = 0; i < flowButtons.length; i++) {
                if (flowButtons[i].isSelected()) {
                    return i;
                }
            }
            return -1;
        }

        private int getType() {
            int index = typeComboBox.getSelectedIndex();
            return index < 0 ? -1 : index + (allowNone ? 0 : 1);
        }

        private void setType(int type) {
            typeComboBox.setSelectedIndex(type < 0 ? -1 : allowNone ? type : Math.max(1, type) - 1);
        }

        private void updateColor(Color color) {
            this.color = color;
            colorEditor.setColor(color);
        }

        private void changeColor(Color color) {
            this.color = color;
            changeListener.accept("color");
        }

        private void paintSwatch(Graphics2D graphics, int width, int height) {
            int type = getType();
            graphics.setColor(Color.white);
            graphics.fillRect(0, 0, width, height);
            if (type == 0) {
                graphics.setColor(Color.red);
                graphics.drawLine(1, height - 2, width - 2, 1);
            } else if (type < 0 || (type == 1 && colorEditor.isMixed())
                    || ((type == 2 || type == 3) && (gradientEditor.isIndeterminate() || linearRGBCheckBox.getSelectionState() == 1))
                    || (type == 4 && bitmapComboBox.getSelectedIndex() < 0)) {
                graphics.setColor(Color.gray);
                graphics.drawLine(width / 2 - 3, height / 2, width / 2 + 3, height / 2);
            } else {
                paintCheckerboard(graphics, width - 2, height - 2);
                if (type == 1) {
                    graphics.setColor(color);
                    graphics.fillRect(1, 1, width - 2, height - 2);
                } else if (type == 4) {
                    int id = bitmapComboBox.getItemAt(bitmapComboBox.getSelectedIndex()).getValue();
                    int left = (width - height) / 2 + 1;
                    graphics.drawImage(bitmapIcon(id).getImage(), left, 1, left + height - 2, height - 1,
                            1, 1, BITMAP_PREVIEW_SIZE + 1, BITMAP_PREVIEW_SIZE + 1, null);
                } else {
                    float focus = focalPointCheckBox.isSelected() && gradient instanceof FOCALGRADIENT
                            ? ((FOCALGRADIENT) gradient).focalPoint : 0;
                    gradientEditor.paintSwatch(graphics, new Rectangle(1, 1, width - 2, height - 2), type == 3, focus);
                }
            }
            graphics.setColor(Color.gray);
            graphics.drawRect(0, 0, width - 1, height - 1);
        }

        static ImageIcon colorIcon(Color color) {
            return ShapeColorEditor.createSwatchIcon(color);
        }

        void setChangeListener(Consumer<String> listener) {
            changeListener = listener;
        }

        boolean isFillValid() {
            return getType() != 4 || bitmapComboBox.getSelectedIndex() >= 0;
        }

        public void setSolidColor(Color color) {
            updateColor(color);
            setType(1);
        }

        public void setFillStyle(FILLSTYLE style) {
            originalStyle = style == null ? null : style.toShapeNum(4);
            updateColor(style != null && style.color != null ? style.color.toColor() : defaultColor);
            // The gradient editor only receives a detached copy of the style.
            gradient = style != null && style.gradient != null ? Helper.deepCopy(style.gradient) : new GRADIENT();
            gradientEditor.setGradient(gradient);
            setFlow(gradient.spreadMode);
            linearRGBCheckBox.setSelected(gradient.interpolationMode == GRADIENT.INTERPOLATION_LINEAR_RGB_MODE);
            focalPointCheckBox.setSelected(style != null && style.fillStyleType == FILLSTYLE.FOCAL_RADIAL_GRADIENT);
            smoothedCheckBox.setSelected(style == null || isSmoothed(style));
            repeatCheckBox.setSelected(style == null || isRepeating(style));
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
            setType(type);
        }

        public FILLSTYLE getFillStyle(Rectangle2D bounds) {
            int type = getType();
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
                gradientEditor.save();
                style.gradient = Helper.deepCopy(gradient);
                setGradientType(style, type);
                if (getFlow() >= 0) {
                    style.gradient.spreadMode = getFlow();
                }
                if (linearRGBCheckBox.getSelectionState() != 1) {
                    style.gradient.interpolationMode = linearRGBCheckBox.isSelected()
                            ? GRADIENT.INTERPOLATION_LINEAR_RGB_MODE : GRADIENT.INTERPOLATION_RGB_MODE;
                }
                if (style.gradientMatrix == null && bounds != null) {
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
                setBitmapSmoothing(style);
                setBitmapRepeating(style);
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

        private void setGradientType(FILLSTYLE style, int type) {
            boolean focal = type == 3 && (focalPointCheckBox.getSelectionState() == 1
                    ? style.fillStyleType == FILLSTYLE.FOCAL_RADIAL_GRADIENT : focalPointCheckBox.isSelected());
            style.fillStyleType = type == 2 ? FILLSTYLE.LINEAR_GRADIENT
                    : focal ? FILLSTYLE.FOCAL_RADIAL_GRADIENT : FILLSTYLE.RADIAL_GRADIENT;
            style.fieldChanged(null);
        }

        private static boolean isSmoothed(FILLSTYLE style) {
            return style.fillStyleType != FILLSTYLE.NON_SMOOTHED_REPEATING_BITMAP
                    && style.fillStyleType != FILLSTYLE.NON_SMOOTHED_CLIPPED_BITMAP;
        }

        private void setBitmapSmoothing(FILLSTYLE style) {
            if (smoothedCheckBox.getSelectionState() == 1) {
                return;
            }
            boolean clipped = style.fillStyleType == FILLSTYLE.CLIPPED_BITMAP
                    || style.fillStyleType == FILLSTYLE.NON_SMOOTHED_CLIPPED_BITMAP;
            style.fillStyleType = smoothedCheckBox.isSelected()
                    ? clipped ? FILLSTYLE.CLIPPED_BITMAP : FILLSTYLE.REPEATING_BITMAP
                    : clipped ? FILLSTYLE.NON_SMOOTHED_CLIPPED_BITMAP : FILLSTYLE.NON_SMOOTHED_REPEATING_BITMAP;
        }

        private static boolean isRepeating(FILLSTYLE style) {
            return style.fillStyleType != FILLSTYLE.CLIPPED_BITMAP
                    && style.fillStyleType != FILLSTYLE.NON_SMOOTHED_CLIPPED_BITMAP;
        }

        private void setBitmapRepeating(FILLSTYLE style) {
            if (repeatCheckBox.getSelectionState() == 1) {
                return;
            }
            style.fillStyleType = repeatCheckBox.isSelected()
                    ? isSmoothed(style) ? FILLSTYLE.REPEATING_BITMAP : FILLSTYLE.NON_SMOOTHED_REPEATING_BITMAP
                    : isSmoothed(style) ? FILLSTYLE.CLIPPED_BITMAP : FILLSTYLE.NON_SMOOTHED_CLIPPED_BITMAP;
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
            setType(type == null ? -1 : type);
            if (type != null && type == 1 && common(styles, style -> style.color.toColor()) == null) {
                colorEditor.setMixed();
            }
            if (styles.stream().allMatch(style -> style != null && style.hasGradient())) {
                ((CardLayout) valuePanel.getLayout()).show(valuePanel, "gradient");
                gradientOptions.setVisible(true);
                gradientEditor.setIndeterminate(common(styles, FillEditor::gradientStops) == null);
                Integer flow = common(styles, style -> style.gradient.spreadMode);
                setFlow(flow == null ? -1 : flow);
                Integer interpolation = common(styles, style -> style.gradient.interpolationMode);
                linearRGBCheckBox.setSelectionState(interpolation == null ? 1
                        : interpolation == GRADIENT.INTERPOLATION_LINEAR_RGB_MODE ? 2 : 0);
                Boolean focal = common(styles, style -> style.fillStyleType == FILLSTYLE.FOCAL_RADIAL_GRADIENT);
                focalPointCheckBox.setSelectionState(focal == null ? 1 : focal ? 2 : 0);
            }
            if (type != null && type == 4) {
                Boolean smoothed = common(styles, FillEditor::isSmoothed);
                smoothedCheckBox.setSelectionState(smoothed == null ? 1 : smoothed ? 2 : 0);
                Boolean repeating = common(styles, FillEditor::isRepeating);
                repeatCheckBox.setSelectionState(repeating == null ? 1 : repeating ? 2 : 0);
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
            } else if (property.equals("flow")) {
                if (base != null && base.hasGradient() && getFlow() >= 0) {
                    base.gradient.spreadMode = getFlow();
                }
            } else if (property.equals("linearRGB")) {
                if (base != null && base.hasGradient() && linearRGBCheckBox.getSelectionState() != 1) {
                    base.gradient.interpolationMode = linearRGBCheckBox.isSelected()
                            ? GRADIENT.INTERPOLATION_LINEAR_RGB_MODE : GRADIENT.INTERPOLATION_RGB_MODE;
                }
            } else if (property.equals("focalPoint")) {
                if (base != null && styleType(base) == 3 && focalPointCheckBox.getSelectionState() != 1) {
                    setGradientType(base, 3);
                }
            } else if (property.equals("smoothed")) {
                if (base != null && base.hasBitmap()) {
                    setBitmapSmoothing(base);
                }
            } else if (property.equals("repeat")) {
                if (base != null && base.hasBitmap()) {
                    setBitmapRepeating(base);
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
                int type = getType();
                if (type == 1) {
                    result.color = original != null && original.fillStyleType == FILLSTYLE.SOLID
                            ? new RGBA(original.color) : new RGBA(template.color);
                    result.fillStyleType = FILLSTYLE.SOLID;
                } else if (type == 2 || type == 3) {
                    result.gradient = original != null && original.gradient != null ? Helper.deepCopy(original.gradient) : template.gradient;
                    setGradientType(result, type);
                    result.gradientMatrix = original != null && original.gradientMatrix != null ? Helper.deepCopy(original.gradientMatrix) : template.gradientMatrix;
                } else if (type == 4) {
                    result.fillStyleType = original != null && original.hasBitmap() ? original.fillStyleType : template.fillStyleType;
                    setBitmapSmoothing(result);
                    setBitmapRepeating(result);
                    result.bitmapId = original != null && original.hasBitmap() ? original.bitmapId : template.bitmapId;
                    result.bitmapMatrix = original != null && original.bitmapMatrix != null ? Helper.deepCopy(original.bitmapMatrix) : template.bitmapMatrix;
                }
                return result;
            }
            return base;
        }

        private ImageIcon bitmapIcon(int id) {
            return bitmapIcons.computeIfAbsent(id, key -> bitmapIcon(bitmapSwf == null ? null : bitmapSwf.getImage(key)));
        }

        private static void paintCheckerboard(Graphics2D graphics, int width, int height) {
            for (int y = 0; y < height; y += 4) {
                for (int x = 0; x < width; x += 4) {
                    graphics.setColor((x / 4 + y / 4) % 2 == 0 ? Color.white : Color.lightGray);
                    graphics.fillRect(1 + x, 1 + y, Math.min(4, width - x), Math.min(4, height - y));
                }
            }
        }

        static ImageIcon bitmapIcon(ImageTag image) {
            int iconSize = BITMAP_PREVIEW_SIZE + 2;
            BufferedImage preview = new BufferedImage(iconSize, iconSize, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = preview.createGraphics();
            try {
                paintCheckerboard(graphics, BITMAP_PREVIEW_SIZE, BITMAP_PREVIEW_SIZE);
                Dimension size = image == null ? null : image.getImageDimension();
                if (size != null && size.width > 0 && size.height > 0) {
                    SerializableImage source = image.getImageCached();
                    if (source != null) {
                        double scale = Math.min((double) BITMAP_PREVIEW_SIZE / source.getWidth(), (double) BITMAP_PREVIEW_SIZE / source.getHeight());
                        int width = Math.max(1, (int) Math.round(source.getWidth() * scale));
                        int height = Math.max(1, (int) Math.round(source.getHeight() * scale));
                        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                        graphics.drawImage(source.getBufferedImage(), (iconSize - width) / 2, (iconSize - height) / 2, width, height, null);
                    }
                }
                graphics.setColor(Color.gray);
                graphics.drawRect(0, 0, iconSize - 1, iconSize - 1);
            } finally {
                graphics.dispose();
            }
            return new ImageIcon(preview);
        }

        public void setSwf(SWF swf) {
            Integer selectedId = bitmapComboBox.getSelectedIndex() < 0 ? null
                    : bitmapComboBox.getItemAt(bitmapComboBox.getSelectedIndex()).getValue();
            bitmapSwf = swf;
            bitmapIcons.clear();
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
            if (selectedId != null) {
                for (int i = 0; i < bitmapComboBox.getItemCount(); i++) {
                    if (selectedId.equals(bitmapComboBox.getItemAt(i).getValue())) {
                        bitmapComboBox.setSelectedIndex(i);
                        break;
                    }
                }
            }
            bitmapComboBox.setEnabled(bitmapComboBox.getItemCount() > 0);
        }
    }
}
