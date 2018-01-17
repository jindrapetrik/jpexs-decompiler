/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.exporters.shape;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.GRADRECORD;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.SHAPE;

/**
 *
 * @author JPEXS, Claus Wahlers
 */
public abstract class DefaultSVGShapeExporter extends ShapeExporterBase {

    protected static final String DRAW_COMMAND_L = "L";

    protected static final String DRAW_COMMAND_Q = "Q";

    protected String currentDrawCommand = "";

    protected StringBuilder pathData;

    protected double zoom;

    public DefaultSVGShapeExporter(SWF swf, SHAPE shape, ColorTransform colorTransform, double zoom) {
        super(swf, shape, colorTransform);
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
        finalizePath();
    }

    @Override
    public void beginGradientFill(int type, GRADRECORD[] gradientRecords, Matrix matrix, int spreadMethod, int interpolationMethod, float focalPointRatio) {
        finalizePath();
    }

    @Override
    public void beginBitmapFill(int bitmapId, Matrix matrix, boolean repeat, boolean smooth, ColorTransform colorTransform) {
        finalizePath();
    }

    @Override
    public void endFill() {
        finalizePath();
    }

    @Override
    public void lineStyle(double thickness, RGB color, boolean pixelHinting, String scaleMode, int startCaps, int endCaps, int joints, float miterLimit) {
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

    protected void finalizePath() {
        pathData = new StringBuilder();
        currentDrawCommand = "";
    }

    protected double roundPixels20(double pixels) {
        return Math.round(pixels * 100) / 100.0;
    }
}
