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
import com.jpexs.decompiler.flash.easygui.EasyStrings;
import com.jpexs.decompiler.flash.easygui.EasyTagNameResolver;
import com.jpexs.decompiler.flash.easygui.ShapeSelection;
import com.jpexs.decompiler.flash.easygui.properties.GradientEditor;
import com.jpexs.decompiler.flash.gui.FasterScrollPane;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.GRADIENT;
import com.jpexs.decompiler.flash.types.ILINESTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Noninteractive style values displayed by the eyedropper before sampling.
 */
public class ShapeStylePreviewPanel extends AbstractPropertiesPanel {

    private final FillPreview fillPreview = new FillPreview();
    private final FillPreview strokePreview = new FillPreview();
    private final JLabel widthLabel = new JLabel();
    private final JLabel scaleLabel = new JLabel();
    private final JLabel capLabel = new JLabel();
    private final JLabel joinLabel = new JLabel();
    private final JLabel miterLabel = new JLabel();
    private SWF swf;

    public ShapeStylePreviewPanel() {
        super("shape");
        setLayout(new BorderLayout());
        JLabel title = new JLabel(EasyStrings.translate("properties.shape"));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        add(title, BorderLayout.NORTH);
        JPanel fill = new JPanel(new GridBagLayout());
        JPanel stroke = new JPanel(new GridBagLayout());
        addRow(fill, "type", fillPreview, 0);
        addRow(stroke, "type", strokePreview, 0);
        addRow(stroke, "width", widthLabel, 1);
        addRow(stroke, "scale", scaleLabel, 2);
        addRow(stroke, "cap", capLabel, 3);
        addRow(stroke, "join", joinLabel, 4);
        addRow(stroke, "miter", miterLabel, 5);
        JPanel cards = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = -1;
        addCard(cards, "fill", null, fill, gbc, false);
        addCard(cards, "stroke", null, stroke, gbc, false);
        gbc.gridy++;
        gbc.weighty = 1;
        cards.add(new JPanel(), gbc);
        setCardOpened("fill", true);
        setCardOpened("stroke", true);
        add(new FasterScrollPane(cards), BorderLayout.CENTER);
        setSample(null);
    }

    private void addRow(JPanel panel, String property, Component value, int row) {
        GridBagLayout layout = (GridBagLayout) panel.getLayout();
        JLabel label = new JLabel(formatPropertyName(property));
        addToGrid(layout, panel, label, 0, row);
        GridBagConstraints gbc = layout.getConstraints(label);
        gbc.anchor = GridBagConstraints.NORTHEAST;
        layout.setConstraints(label, gbc);
        addToGrid(layout, panel, value, 1, row);
    }

    public void setShape(ShapeTag shape) {
        swf = shape == null ? null : shape.getSwf();
        setSample(null);
    }

    public void setSample(ShapeSelection.Selection sample) {
        boolean showFill = sample != null && !sample.stroke;
        boolean showStroke = sample != null && sample.stroke;
        setCardVisible("fill", showFill);
        setCardOpened("fill", showFill);
        setCardVisible("stroke", showStroke);
        setCardOpened("stroke", showStroke);
        ILINESTYLE line = sample == null || !sample.stroke ? null : sample.line;
        FILLSTYLE paint = null;
        if (line instanceof LINESTYLE2 && ((LINESTYLE2) line).hasFillFlag) {
            paint = ((LINESTYLE2) line).fillType;
        } else if (line != null) {
            paint = new FILLSTYLE();
            paint.color = line.getColor();
        }
        fillPreview.setFill(sample == null || sample.stroke ? null : sample.fill);
        strokePreview.setFill(paint);
        LINESTYLE2 line2 = line instanceof LINESTYLE2 ? (LINESTYLE2) line : null;
        widthLabel.setText(line == null ? "-" : number(line.getWidth() / (float) SWF.unitDivisor) + " px");
        int scale = line2 == null ? 0 : (line2.noHScaleFlag ? 2 : 0) + (line2.noVScaleFlag ? 1 : 0);
        scaleLabel.setText(line == null ? "-" : option("scale", new String[]{"normal", "horizontal", "vertical", "none"}[scale]));
        capLabel.setText(line == null ? "-" : option("cap", new String[]{"round", "none", "square"}[line2 == null ? 0 : line2.startCapStyle]));
        joinLabel.setText(line == null ? "-" : option("join", new String[]{"round", "bevel", "miter"}[line2 == null ? 0 : line2.joinStyle]));
        miterLabel.setText(line2 == null || line2.joinStyle != LINESTYLE2.MITER_JOIN ? "-" : number(line2.miterLimitFactor));
        revalidate();
        repaint();
    }

    private static String option(String property, String value) {
        return EasyStrings.translate("property.shape." + property + "." + value);
    }

    private static String number(float value) {
        String text = Float.toString(value);
        return text.endsWith(".0") ? text.substring(0, text.length() - 2) : text;
    }

    private class FillPreview extends JPanel {

        private final JLabel typeLabel = new JLabel();
        private final JLabel valueLabel = new JLabel();
        private final JPanel details = new JPanel(new BorderLayout(0, 5));

        FillPreview() {
            setLayout(new BorderLayout(0, 5));
            add(typeLabel, BorderLayout.NORTH);
            add(details, BorderLayout.CENTER);
        }

        void setFill(FILLSTYLE fill) {
            details.removeAll();
            valueLabel.setText("");
            valueLabel.setIcon(null);
            if (fill == null) {
                typeLabel.setText(option("fill", "none"));
            } else if (fill.hasGradient()) {
                typeLabel.setText(option("fill", fill.fillStyleType == FILLSTYLE.LINEAR_GRADIENT ? "linearGradient" : "radialGradient"));
                details.add(GradientEditor.createPreview(fill.gradient), BorderLayout.NORTH);
                JPanel options = new JPanel(new GridBagLayout());
                GridBagLayout layout = (GridBagLayout) options.getLayout();
                addToGrid(layout, options, new JLabel(formatPropertyName("flow")), 0, 0);
                int spread = fill.gradient.spreadMode;
                addToGrid(layout, options, new JLabel(spread < 0 || spread > 2 ? "-"
                        : option("flow", new String[]{"pad", "reflect", "repeat"}[spread])), 1, 0);
                addToGrid(layout, options, new JLabel(formatPropertyName("linearRGB") + " "
                        + (fill.gradient.interpolationMode == GRADIENT.INTERPOLATION_LINEAR_RGB_MODE ? "\u2713" : "\u2717")), 0, 1, 2, 1);
                details.add(options, BorderLayout.CENTER);
            } else {
                if (fill.hasBitmap()) {
                    typeLabel.setText(option("fill", "bitmap"));
                    ImageTag image = swf == null ? null : swf.getImage(fill.bitmapId);
                    valueLabel.setText(image == null ? EasyStrings.translate("item.image") + " " + fill.bitmapId
                            : new EasyTagNameResolver().getTagName(image));
                } else {
                    typeLabel.setText(option("fill", "solid"));
                    Color color = fill.color.toColor();
                    valueLabel.setText(String.format("#%02x%02x%02x (%d%%)", color.getRed(), color.getGreen(), color.getBlue(), Math.round(color.getAlpha() * 100f / 255)));
                    valueLabel.setIcon(ShapePropertiesPanel.FillEditor.colorIcon(color));
                }
                details.add(valueLabel, BorderLayout.NORTH);
            }
            details.setVisible(fill != null);
        }
    }
}
