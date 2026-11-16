/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.shape.BitmapExporter;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.FOCALGRADIENT;
import com.jpexs.decompiler.flash.types.ILINESTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.SHAPE;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.NoninvertibleTransformException;

/**
 * Uses the SWF renderer's stroke geometry and paint for selection-only widening.
 */
final class ShapeStrokeHighlight extends BitmapExporter {

    private static final SHAPE EMPTY_SHAPE = new SHAPE();
    private final double pixelScale;

    private ShapeStrokeHighlight(Graphics2D target, SWF swf, AffineTransform transform, double pixelScale) {
        super(ShapeTag.WIND_NONZERO, 4, swf, EMPTY_SHAPE, Color.BLACK, null);
        graphics = (Graphics2D) target.create();
        graphics.transform(transform);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        AffineTransform placement = AffineTransform.getScaleInstance(1 / pixelScale, 1 / pixelScale);
        placement.concatenate(transform);
        strokeTransformation = new Matrix(placement);
        calculateThicknessScale();
        scaleStrokes = true;
        unzoom = pixelScale * SWF.unitDivisor;
        aaScale = 1;
        defaultStroke = graphics.getStroke();
        this.pixelScale = pixelScale;
    }

    static Shape paint(Graphics2D target, SWF swf, Shape center, ILINESTYLE style, AffineTransform transform, double pixelScale) {
        ShapeStrokeHighlight painter = new ShapeStrokeHighlight(target, swf, transform, pixelScale);
        try {
            return painter.paint(center, style, transform);
        } finally {
            painter.graphics.dispose();
        }
    }

    private Shape paint(Shape center, ILINESTYLE style, AffineTransform transform) {
        LINESTYLE2 line = style instanceof LINESTYLE2 ? (LINESTYLE2) style : ((LINESTYLE) style).toLineStyle2();
        String scaleMode = line.noHScaleFlag ? (line.noVScaleFlag ? "NONE" : "VERTICAL")
                : line.noVScaleFlag ? "HORIZONTAL" : "NORMAL";
        double scale = scaleMode.equals("NONE") ? 1 : scaleMode.equals("VERTICAL") ? thicknessScaleY
                : scaleMode.equals("HORIZONTAL") ? thicknessScaleX : thicknessScale;
        setStyle(line, line.width, scaleMode);
        Area original = lineStroke == null ? new Area() : new Area(lineStroke.createStrokedShape(center));
        if (scale <= 0 || line.width * scale * pixelScale >= 4) {
            return transform.createTransformedShape(original);
        }
        setStyle(line, 4 / (scale * pixelScale), scaleMode);
        Area widened = new Area(lineStroke.createStrokedShape(center));
        Area extension = new Area(widened);
        extension.subtract(original);
        // Paint only the new strip: repainting translucent original ink would darken it.
        // Include the original edge's half-pixel antialias footprint in the exclusion.
        try {
            extension.subtract(new Area(new TransformedStroke(new BasicStroke((float) (1 / pixelScale)),
                    strokeTransformation.toTransform()).createStrokedShape(original)));
        } catch (NoninvertibleTransformException ex) {
            return transform.createTransformedShape(original);
        }
        graphics.clip(extension);
        if (line.hasFillFlag) {
            FILLSTYLE fill = line.fillType;
            if (fill.hasGradient()) {
                lineGradientStyle(fill.fillStyleType, fill.gradient.gradientRecords, new Matrix(fill.gradientMatrix),
                        fill.gradient.spreadMode, fill.gradient.interpolationMode,
                        fill.gradient instanceof FOCALGRADIENT ? ((FOCALGRADIENT) fill.gradient).focalPoint : 0);
            } else if (fill.hasBitmap()) {
                lineBitmapStyle(fill.bitmapId, new Matrix(fill.bitmapMatrix),
                        fill.fillStyleType == FILLSTYLE.REPEATING_BITMAP || fill.fillStyleType == FILLSTYLE.NON_SMOOTHED_REPEATING_BITMAP,
                        fill.fillStyleType == FILLSTYLE.REPEATING_BITMAP || fill.fillStyleType == FILLSTYLE.CLIPPED_BITMAP, null);
            }
        }
        path.append(center, false);
        finalizePath();
        return transform.createTransformedShape(widened);
    }

    private void setStyle(LINESTYLE2 line, double width, String scaleMode) {
        lineStyle(width, line.hasFillFlag && line.fillType.fillStyleType == FILLSTYLE.SOLID ? line.fillType.color : line.color,
                line.pixelHintingFlag, scaleMode, line.startCapStyle, line.endCapStyle, line.joinStyle,
                Math.max(1, line.miterLimitFactor), line.noClose);
    }
}
