/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.easygui.properties.panels;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.easygui.DrawingShape;
import com.jpexs.decompiler.flash.easygui.EasyStrings;
import com.jpexs.decompiler.flash.easygui.ShapeDrawingGesture;
import com.jpexs.decompiler.flash.easygui.properties.FloatPropertyField;
import com.jpexs.decompiler.flash.easygui.properties.IntegerPropertyField;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

/**
 * Primitive settings and Fill/Stroke editors for the Shapes tool.
 *
 * @author JPEXS
 */
public class DrawingShapesPropertiesPanel extends AbstractPropertiesPanel {

    private final JPanel kindEditor = new JPanel(new BorderLayout(0, 3));
    private final Map<DrawingShape, JToggleButton> kindButtons = new EnumMap<>(DrawingShape.class);
    private final JLabel kindLabel = new JLabel("", SwingConstants.CENTER);
    private DrawingShape kind = DrawingShape.RECTANGLE;
    private final IntegerPropertyField sidesEditor = new IntegerPropertyField(6, 3, 360);
    private final IntegerPropertyField anglesEditor = new IntegerPropertyField(5, 3, 360);
    private final FloatPropertyField radiusEditor = new FloatPropertyField(10, 0, 65535);
    private final JLabel sidesLabel = new JLabel(formatPropertyName("polygonSides"));
    private final JLabel anglesLabel = new JLabel(formatPropertyName("starAngles"));
    private final JLabel radiusLabel = new JLabel(formatPropertyName("cornerRadius"));
    private final JLabel starHint = new JLabel();
    private final ShapePropertiesPanel styles = ShapePropertiesPanel.createDrawingStyles();
    private Runnable changeListener = () -> { };
    private BiConsumer<Boolean, String> styleChangeListener = (stroke, property) -> { };
    private boolean outerRadiusPending;
    private SWF swf;

    public DrawingShapesPropertiesPanel() {
        super("shape");
        setLayout(new BorderLayout());
        JPanel options = new JPanel(new BorderLayout());
        JPanel paletteRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
        JPanel parameters = new JPanel(new GridBagLayout());
        GridBagLayout layout = (GridBagLayout) parameters.getLayout();
        JPanel swatches = new JPanel(new GridLayout(2, 5, 3, 3));
        ButtonGroup group = new ButtonGroup();
        int nameWidth = 0;
        for (DrawingShape shape : DrawingShape.values()) {
            String name = EasyStrings.translate("drawing.shape." + shape.name());
            nameWidth = Math.max(nameWidth, kindLabel.getFontMetrics(kindLabel.getFont()).stringWidth(name));
            JToggleButton button = new JToggleButton(createShapeIcon(shape));
            button.setToolTipText(name);
            button.getAccessibleContext().setAccessibleName(name);
            button.setMargin(new Insets(3, 3, 3, 3));
            button.setPreferredSize(new Dimension(46, 46));
            button.addActionListener(event -> {
                kind = shape;
                changed();
            });
            group.add(button);
            kindButtons.put(shape, button);
            swatches.add(button);
        }
        kindButtons.get(kind).setSelected(true);
        kindLabel.setPreferredSize(new Dimension(nameWidth, kindLabel.getFontMetrics(kindLabel.getFont()).getHeight()));
        kindEditor.add(swatches, BorderLayout.CENTER);
        kindEditor.add(kindLabel, BorderLayout.SOUTH);
        // Keep the palette independent of the changing labels and fields below it.
        paletteRow.add(new JLabel(formatPropertyName("drawingKind")));
        paletteRow.add(kindEditor);
        options.add(paletteRow, BorderLayout.NORTH);
        sidesEditor.setColumns(3);
        anglesEditor.setColumns(3);
        radiusEditor.setColumns(4);
        addToGrid(layout, parameters, sidesLabel, 0, 0);
        addToGrid(layout, parameters, sidesEditor, 1, 0);
        addToGrid(layout, parameters, anglesLabel, 0, 1);
        addToGrid(layout, parameters, anglesEditor, 1, 1);
        addToGrid(layout, parameters, radiusLabel, 0, 2);
        JPanel radiusPanel = new JPanel(new BorderLayout(3, 0));
        radiusPanel.add(radiusEditor, BorderLayout.CENTER);
        radiusPanel.add(new JLabel(" px"), BorderLayout.EAST);
        addToGrid(layout, parameters, radiusPanel, 1, 2);
        JPanel parameterRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        parameterRow.add(parameters);
        JPanel hintRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
        hintRow.add(starHint);
        JPanel parameterSection = new JPanel(new BorderLayout());
        parameterSection.add(parameterRow, BorderLayout.NORTH);
        parameterSection.add(hintRow, BorderLayout.CENTER);
        options.add(parameterSection, BorderLayout.CENTER);
        add(options, BorderLayout.NORTH);
        add(styles, BorderLayout.CENTER);
        sidesEditor.addChangeListener(event -> changed());
        anglesEditor.addChangeListener(event -> changed());
        radiusEditor.addChangeListener(event -> changed());
        styles.setPropertyChangeListener((stroke, property) -> {
            if (!styles.isPropertyChangeValid(stroke, property)) {
                styles.setDrawingSwf(swf);
            }
            styleChangeListener.accept(stroke, property);
            changed();
        });
        updateOptions();
        // Reserve the same columns for every parameter so the centered block
        // stays in place when its visible label or field changes. The wider
        // star hint is centered separately and cannot stretch these columns.
        int labelWidth = Math.max(sidesLabel.getPreferredSize().width,
                Math.max(anglesLabel.getPreferredSize().width, radiusLabel.getPreferredSize().width)) + 6;
        int fieldWidth = Math.max(sidesEditor.getPreferredSize().width,
                Math.max(anglesEditor.getPreferredSize().width, radiusPanel.getPreferredSize().width)) + 6;
        layout.columnWidths = new int[]{labelWidth, fieldWidth};
    }

    private static ImageIcon createShapeIcon(DrawingShape kind) {
        boolean radial = kind == DrawingShape.TRIANGLE || kind == DrawingShape.PENTAGON
                || kind == DrawingShape.POLYGON || kind == DrawingShape.STAR;
        Point2D end = radial ? new Point2D.Double(0, -100)
                : new Point2D.Double(kind == DrawingShape.CIRCLE || kind == DrawingShape.SQUARE ? 100 : 140, 100);
        ShapeDrawingGesture drawing = new ShapeDrawingGesture(kind, new Point2D.Double(), new AffineTransform(), 6, 5, 20);
        if (kind == DrawingShape.STAR) {
            drawing.update(new Point2D.Double(0, -42));
            drawing.release();
        }
        drawing.update(end);
        Shape geometry = drawing.getGeometry();
        Rectangle2D bounds = geometry.getBounds2D();
        double scale = 32 / Math.max(bounds.getWidth(), bounds.getHeight());
        AffineTransform transform = AffineTransform.getTranslateInstance(20, 20);
        transform.scale(scale, scale);
        transform.translate(-bounds.getCenterX(), -bounds.getCenterY());
        Shape preview = transform.createTransformedShape(geometry);
        BufferedImage image = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (kind != DrawingShape.LINE) {
                graphics.setColor(new Color(134, 183, 215, 170));
                graphics.fill(preview);
            }
            graphics.setColor(new Color(30, 45, 60));
            graphics.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            graphics.draw(preview);
            if (kind == DrawingShape.POLYGON || kind == DrawingShape.STAR) {
                Shape letter = new Font(Font.SANS_SERIF, Font.BOLD, 11).createGlyphVector(graphics.getFontRenderContext(), "N").getOutline();
                Rectangle2D letterBounds = letter.getBounds2D();
                Point2D center = transform.transform(new Point2D.Double(), null);
                graphics.fill(AffineTransform.getTranslateInstance(center.getX() - letterBounds.getCenterX(),
                        center.getY() - letterBounds.getCenterY()).createTransformedShape(letter));
            }
        } finally {
            graphics.dispose();
        }
        return new ImageIcon(image);
    }

    private void changed() {
        outerRadiusPending = false;
        updateOptions();
        changeListener.run();
    }

    private void updateOptions() {
        DrawingShape kind = getKind();
        kindLabel.setText(EasyStrings.translate("drawing.shape." + kind.name()));
        sidesLabel.setVisible(kind == DrawingShape.POLYGON);
        sidesEditor.setVisible(kind == DrawingShape.POLYGON);
        anglesLabel.setVisible(kind == DrawingShape.STAR);
        anglesEditor.setVisible(kind == DrawingShape.STAR);
        radiusLabel.setVisible(kind == DrawingShape.ROUND_RECTANGLE);
        radiusEditor.getParent().setVisible(kind == DrawingShape.ROUND_RECTANGLE);
        starHint.setVisible(kind == DrawingShape.STAR);
        starHint.setText("<html><div style='width: 250px;'>"
                + EasyStrings.translate(outerRadiusPending ? "drawing.star.outerHint" : "drawing.star.innerHint") + "</div></html>");
        revalidate();
        repaint();
    }

    public void setOuterRadiusPending(boolean pending) {
        outerRadiusPending = pending;
        updateOptions();
    }

    public void setChangeListener(Runnable listener) {
        changeListener = listener;
    }

    void setStyleChangeListener(BiConsumer<Boolean, String> listener) {
        styleChangeListener = listener;
    }

    ShapePropertiesPanel getStyleSettings() {
        return styles;
    }

    public DrawingShape getKind() {
        return kind;
    }

    public int getSides() {
        return sidesEditor.getValue() == null ? 6 : Math.max(3, Math.min(360, sidesEditor.getValue()));
    }

    public int getAngles() {
        return anglesEditor.getValue() == null ? 5 : Math.max(3, Math.min(360, anglesEditor.getValue()));
    }

    public double getCornerRadius() {
        Float value = radiusEditor.getValue();
        return value == null || !Float.isFinite(value) ? 10 * SWF.unitDivisor : Math.max(0, value) * SWF.unitDivisor;
    }

    public FILLSTYLE getFill(Rectangle2D bounds) {
        return getKind() == DrawingShape.LINE ? null : styles.getFillStyle(bounds);
    }

    public LINESTYLE2 getStroke(Rectangle2D bounds) {
        LINESTYLE2 line = styles.getStrokeStyle(bounds);
        if (line != null) {
            line.noClose = getKind() == DrawingShape.LINE;
        }
        return line;
    }

    public void setSwf(SWF swf) {
        this.swf = swf;
        styles.setDrawingSwf(swf);
    }
}
