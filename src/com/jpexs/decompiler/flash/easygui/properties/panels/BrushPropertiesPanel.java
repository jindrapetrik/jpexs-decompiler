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
import com.jpexs.decompiler.flash.easygui.BrushShape;
import com.jpexs.decompiler.flash.easygui.EasyStrings;
import com.jpexs.decompiler.flash.easygui.properties.FloatPropertyField;
import com.jpexs.decompiler.flash.gui.FasterScrollPane;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

/**
 * Persistent drawing settings, independent of the selected shape geometry.
 *
 * @author JPEXS
 */
public class BrushPropertiesPanel extends AbstractPropertiesPanel {

    private final ShapePropertiesPanel.FillEditor fillEditor = new ShapePropertiesPanel.FillEditor(Color.black, false);
    private final JComboBox<BrushShape> shapeEditor = new JComboBox<>(BrushShape.values());
    private final FloatPropertyField sizeEditor = new FloatPropertyField(10, 1, 1000);
    private final boolean eraser;
    private FILLSTYLE lastFill;
    private boolean updating;
    private Runnable changeListener = () -> { };
    private Consumer<FILLSTYLE> fillChangeListener = fill -> { };

    public BrushPropertiesPanel() {
        this(false);
    }

    public BrushPropertiesPanel(boolean eraser) {
        super("shape");
        this.eraser = eraser;
        setLayout(new BorderLayout());
        JPanel cards = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = -1;
        JToggleButton fillTab = eraser ? null : ShapePropertiesPanel.styleTab("fill", "shapetoolpaint16", fillEditor);
        if (!eraser) {
            ButtonGroup group = new ButtonGroup();
            group.add(fillTab);
            fillTab.setSelected(true);
            JPanel fillSection = ShapePropertiesPanel.createStyleSection(fillTab, fillEditor.paintOptions, fillEditor);
            addCard(cards, "fill", null, fillSection, gbc, false);
            setCardOpened("fill", true);
        }
        JPanel brushPanel = new JPanel(new GridBagLayout());
        shapeEditor.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, selected, focus);
                if (value instanceof BrushShape) {
                    BrushShape shape = (BrushShape) value;
                    label.setText(EasyStrings.translate("brush.shape." + shape.name()));
                    BufferedImage image = new BufferedImage(36, 36, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D graphics = image.createGraphics();
                    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    graphics.setColor(Color.black);
                    graphics.fill(shape.tip(new Point2D.Double(18, 18), 28));
                    graphics.dispose();
                    label.setIcon(new ImageIcon(image));
                }
                return label;
            }
        });
        addRow(brushPanel, "brushShape", shapeEditor, 0);
        sizeEditor.setColumns(4);
        JPanel sizePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        sizePanel.add(sizeEditor);
        sizePanel.add(new JLabel(" px"));
        addRow(brushPanel, "brushSize", sizePanel, 1);
        String toolCard = eraser ? "eraser" : "brush";
        addCard(cards, toolCard, null, brushPanel, gbc, false);
        gbc.gridy++;
        gbc.weighty = 1;
        cards.add(new JPanel(), gbc);
        setCardOpened(toolCard, true);
        add(new FasterScrollPane(cards), BorderLayout.CENTER);
        lastFill = getFillStyle(null);
        fillEditor.setChangeListener(property -> {
            if (fillTab != null) {
                fillTab.repaint();
            }
            if (updating) {
                return;
            }
            FILLSTYLE fill = fillEditor.getFillStyle(null);
            if (fill == null || !fillEditor.isFillValid()) {
                updating = true;
                try {
                    fillEditor.setFillStyle(lastFill);
                } finally {
                    updating = false;
                }
            } else {
                lastFill = fill;
            }
            fillChangeListener.accept(getFillStyle(null));
            changeListener.run();
        });
        shapeEditor.addActionListener(event -> changeListener.run());
        sizeEditor.addChangeListener(event -> changeListener.run());
    }

    private void addRow(JPanel panel, String property, Component editor, int row) {
        GridBagLayout layout = (GridBagLayout) panel.getLayout();
        JLabel label = new JLabel(formatPropertyName(property));
        addToGrid(layout, panel, label, 0, row);
        addToGrid(layout, panel, editor, 1, row);
    }

    public void setChangeListener(Runnable listener) {
        changeListener = listener;
    }

    public void setFillChangeListener(Consumer<FILLSTYLE> listener) {
        fillChangeListener = listener;
    }

    public void setFillStyle(FILLSTYLE fill) {
        if (fill == null || eraser) {
            return;
        }
        updating = true;
        try {
            fillEditor.setFillStyle(fill);
            lastFill = fillEditor.getFillStyle(null);
        } finally {
            updating = false;
        }
    }

    public BrushShape getBrushShape() {
        return (BrushShape) shapeEditor.getSelectedItem();
    }

    public double getBrushSize() {
        Float size = sizeEditor.getValue();
        return size == null || !Float.isFinite(size) || size <= 0 ? 10 : size;
    }

    public FILLSTYLE getFillStyle(Rectangle2D bounds) {
        if (eraser) {
            return null;
        }
        FILLSTYLE fill = fillEditor.getFillStyle(bounds);
        return fill == null ? lastFill : fill;
    }

    public void setSwf(SWF swf) {
        if (eraser) {
            return;
        }
        updating = true;
        try {
            fillEditor.setSwf(swf);
            if (!fillEditor.isFillValid()) {
                fillEditor.setSolidColor(Color.black);
            }
            lastFill = fillEditor.getFillStyle(null);
        } finally {
            updating = false;
        }
    }
}
