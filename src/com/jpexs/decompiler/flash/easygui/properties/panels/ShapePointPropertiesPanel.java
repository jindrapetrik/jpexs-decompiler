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
import com.jpexs.decompiler.flash.easygui.properties.FloatPropertyField;
import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.gui.FasterScrollPane;
import com.jpexs.decompiler.flash.gui.ImagePanel;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.geom.Point2D;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Position properties bound to the selected points in the stage.
 */
public class ShapePointPropertiesPanel extends AbstractPropertiesPanel {

    private final FloatPropertyField xField = new FloatPropertyField(0);
    private final FloatPropertyField yField = new FloatPropertyField(0);
    private final ImagePanel stage;
    private boolean updating;

    public ShapePointPropertiesPanel(ImagePanel stage) {
        super("shape");
        this.stage = stage;
        setLayout(new BorderLayout());
        stage.setPointEditToolbarVisible(false);
        JPanel controls = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        controls.setLayout(layout);
        xField.setColumns(6);
        yField.setColumns(6);
        xField.addValidation(value -> Float.isFinite(value));
        yField.addValidation(value -> Float.isFinite(value));
        addToGrid(layout, controls, new JLabel(AppStrings.translate("edit.points.x")), 0, 0);
        addToGrid(layout, controls, xField, 1, 0);
        addToGrid(layout, controls, new JLabel(AppStrings.translate("edit.points.y")), 0, 1);
        addToGrid(layout, controls, yField, 1, 1);
        JPanel contents = new JPanel(new BorderLayout());
        contents.add(controls, BorderLayout.NORTH);
        JPanel cards = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = -1;
        addCard(cards, "position", null, contents, gbc, false);
        setCardOpened("position", true);
        gbc.gridy++;
        gbc.weighty = 1;
        cards.add(new JPanel(), gbc);
        add(new FasterScrollPane(cards), BorderLayout.CENTER);
        xField.addChangeListener(event -> changePosition(true));
        yField.addChangeListener(event -> changePosition(false));
        stage.addPointSelectionListener(this::updatePosition);
        updatePosition();
    }

    private void updatePosition() {
        Point2D position = stage.getSelectedPointsPosition();
        updating = true;
        try {
            xField.setValue(position == null ? null : (float) (position.getX() / SWF.unitDivisor), true);
            yField.setValue(position == null ? null : (float) (position.getY() / SWF.unitDivisor), true);
            xField.setEnabled(position != null);
            yField.setEnabled(position != null);
        } finally {
            updating = false;
        }
    }

    private void changePosition(boolean x) {
        Point2D position = stage.getSelectedPointsPosition();
        Float value = (x ? xField : yField).getValue();
        if (!updating && position != null && value != null && Float.isFinite(value)) {
            stage.setSelectedPointsPosition(new Point2D.Double(x ? value * SWF.unitDivisor : position.getX(),
                    x ? position.getY() : value * SWF.unitDivisor));
            updatePosition();
        }
    }
}
