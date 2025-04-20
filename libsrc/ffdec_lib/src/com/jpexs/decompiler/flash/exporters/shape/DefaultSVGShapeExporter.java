/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.exporters.shape;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.GRADRECORD;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.SHAPE;

/**
 * Default SVG shape exporter.
 *
 * @author JPEXS, Claus Wahlers
 */
public abstract class DefaultSVGShapeExporter extends ShapeExporterBase {

    /**
     * Draw command L
     */
    protected static final String DRAW_COMMAND_L = "L";

    /**
     * Draw command Q
     */
    protected static final String DRAW_COMMAND_Q = "Q";

    /**
     * Current draw command
     */
    protected String currentDrawCommand = "";

    /**
     * Path data
     */
    protected StringBuilder pathData;

    /**
     * Zoom
     */
    protected double zoom;

    /**
     * Aliased fill
     */
    protected boolean aliasedFill;

    /**
     * Constructor.
     * @param windingRule Winding rule
     * @param shapeNum Shape number (1 for DefineShape, 2 for DefineShape2, etc.)
     * @param swf SWF
     * @param shape Shape
     * @param colorTransform Color transform
     * @param zoom Zoom
     */
    public DefaultSVGShapeExporter(int windingRule, int shapeNum, SWF swf, SHAPE shape, ColorTransform colorTransform, double zoom) {
        super(windingRule, shapeNum, swf, shape, colorTransform);
        this.zoom = zoom;
    }

    @Override
    public void beginShape() {
    }

    @Override
    public void endShape() {
    }

    @Override
    public void beginFills() {
        aliasedFill = false;
    }

    @Override
    public void endFills() {
    }

    @Override
    public void beginLines() {
    }

    @Override
    public void endLines(boolean close) {
        if (close) {
            pathData.append("Z");
        }

        finalizePath();
    }

    @Override
    public void beginFill(RGB color) {
        if (aliasedFill) {
            return;
        }
        finalizePath();
    }

    @Override
    public void beginGradientFill(int type, GRADRECORD[] gradientRecords, Matrix matrix, int spreadMethod, int interpolationMethod, float focalPointRatio) {
        if (aliasedFill) {
            return;
        }
        finalizePath();
    }

    @Override
    public void beginBitmapFill(int bitmapId, Matrix matrix, boolean repeat, boolean smooth, ColorTransform colorTransform) {
        if (aliasedFill) {
            return;
        }
        finalizePath();
    }

    @Override
    public void endFill() {
        if (aliasedFill) {
            return;
        }
        finalizePath();
    }

    @Override
    public void lineStyle(double thickness, RGB color, boolean pixelHinting, String scaleMode, int startCaps, int endCaps, int joints, float miterLimit, boolean noClose) {
        finalizePath();
    }

    @Override
    public void lineGradientStyle(int type, GRADRECORD[] gradientRecords, Matrix matrix, int spreadMethod, int interpolationMethod, float focalPointRatio) {
    }

    @Override
    public void moveTo(double x, double y) {
        currentDrawCommand = "";
        pathData.append("M")
                .append(roundPixels20(x * zoom / SWF.unitDivisor)).append(" ")
                .append(roundPixels20(y * zoom / SWF.unitDivisor)).append(" ");
    }

    @Override
    public void lineTo(double x, double y) {
        if (!currentDrawCommand.equals(DRAW_COMMAND_L)) {
            currentDrawCommand = DRAW_COMMAND_L;
            pathData.append("L");
        }

        pathData.append(roundPixels20(x * zoom / SWF.unitDivisor)).append(" ")
                .append(roundPixels20(y * zoom / SWF.unitDivisor)).append(" ");
    }

    @Override
    public void curveTo(double controlX, double controlY, double anchorX, double anchorY) {
        if (!currentDrawCommand.equals(DRAW_COMMAND_Q)) {
            currentDrawCommand = DRAW_COMMAND_Q;
            pathData.append("Q");
        }

        pathData.append(roundPixels20(controlX * zoom / SWF.unitDivisor)).append(" ")
                .append(roundPixels20(controlY * zoom / SWF.unitDivisor)).append(" ")
                .append(roundPixels20(anchorX * zoom / SWF.unitDivisor)).append(" ")
                .append(roundPixels20(anchorY * zoom / SWF.unitDivisor)).append(" ");
    }

    /**
     * Finalizes path.
     */
    protected void finalizePath() {
        pathData = new StringBuilder();
        currentDrawCommand = "";
    }

    /**
     * Rounds pixels to 20.
     * @param pixels Pixels
     * @return Rounded pixels
     */
    protected double roundPixels20(double pixels) {
        return Math.round(pixels * 100) / 100.0;
    }
}
