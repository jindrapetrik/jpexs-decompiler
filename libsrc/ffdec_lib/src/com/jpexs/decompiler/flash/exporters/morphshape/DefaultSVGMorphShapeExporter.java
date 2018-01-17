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
package com.jpexs.decompiler.flash.exporters.morphshape;

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
public abstract class DefaultSVGMorphShapeExporter extends MorphShapeExporterBase {

    protected static final String DRAW_COMMAND_L = "L";

    protected static final String DRAW_COMMAND_Q = "Q";

    protected String currentDrawCommand = "";

    protected StringBuilder pathData;

    protected StringBuilder pathDataEnd;

    protected double zoom;

    public DefaultSVGMorphShapeExporter(SHAPE shape, SHAPE endShape, ColorTransform colorTransform, double zoom) {
        super(shape, endShape, colorTransform);
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
    public void endLines() {
        finalizePath();
    }

    @Override
    public void beginFill(RGB color, RGB colorEnd) {
        finalizePath();
    }

    @Override
    public void beginGradientFill(int type, GRADRECORD[] gradientRecords, GRADRECORD[] gradientRecordsEnd, Matrix matrix, Matrix matrixEnd, int spreadMethod, int interpolationMethod, float focalPointRatio, float focalPointRatioEnd) {
        finalizePath();
    }

    @Override
    public void beginBitmapFill(int bitmapId, Matrix matrix, Matrix matrixEnd, boolean repeat, boolean smooth, ColorTransform colorTransform) {
        finalizePath();
    }

    @Override
    public void endFill() {
        finalizePath();
    }

    @Override
    public void lineStyle(double thickness, double thicknessEnd, RGB color, RGB colorEnd, boolean pixelHinting, String scaleMode, int startCaps, int endCaps, int joints, float miterLimit) {
        finalizePath();
    }

    @Override
    public void lineGradientStyle(int type, GRADRECORD[] gradientRecords, GRADRECORD[] gradientRecordsEnd, Matrix matrix, Matrix matrixEnd, int spreadMethod, int interpolationMethod, float focalPointRatio, float focalPointRatioEnd) {
    }

    @Override
    public void moveTo(double x, double y, double x2, double y2) {
        currentDrawCommand = "";
        pathData.append("M")
                .append(roundPixels20(x * zoom / SWF.unitDivisor)).append(" ")
                .append(roundPixels20(y * zoom / SWF.unitDivisor)).append(" ");
        pathDataEnd.append("M")
                .append(roundPixels20(x2 * zoom / SWF.unitDivisor)).append(" ")
                .append(roundPixels20(y2 * zoom / SWF.unitDivisor)).append(" ");
    }

    @Override
    public void lineTo(double x, double y, double x2, double y2) {
        if (!currentDrawCommand.equals(DRAW_COMMAND_L)) {
            currentDrawCommand = DRAW_COMMAND_L;
            pathData.append("L");
            pathDataEnd.append("L");
        }
        pathData.append(roundPixels20(x * zoom / SWF.unitDivisor)).append(" ")
                .append(roundPixels20(y * zoom / SWF.unitDivisor)).append(" ");
        pathDataEnd.append(roundPixels20(x2 * zoom / SWF.unitDivisor)).append(" ")
                .append(roundPixels20(y2 * zoom / SWF.unitDivisor)).append(" ");
    }

    @Override
    public void curveTo(double controlX, double controlY, double anchorX, double anchorY, double controlX2, double controlY2, double anchorX2, double anchorY2) {
        if (!currentDrawCommand.equals(DRAW_COMMAND_Q)) {
            currentDrawCommand = DRAW_COMMAND_Q;
            pathData.append("Q");
            pathDataEnd.append("Q");
        }
        pathData.append(roundPixels20(controlX * zoom / SWF.unitDivisor)).append(" ")
                .append(roundPixels20(controlY * zoom / SWF.unitDivisor)).append(" ")
                .append(roundPixels20(anchorX * zoom / SWF.unitDivisor)).append(" ")
                .append(roundPixels20(anchorY * zoom / SWF.unitDivisor)).append(" ");
        pathDataEnd.append(roundPixels20(controlX2 * zoom / SWF.unitDivisor)).append(" ")
                .append(roundPixels20(controlY2 * zoom / SWF.unitDivisor)).append(" ")
                .append(roundPixels20(anchorX2 * zoom / SWF.unitDivisor)).append(" ")
                .append(roundPixels20(anchorY2 * zoom / SWF.unitDivisor)).append(" ");
    }

    protected void finalizePath() {
        pathData = new StringBuilder();
        pathDataEnd = new StringBuilder();
        currentDrawCommand = "";
    }

    protected double roundPixels20(double pixels) {
        return Math.round(pixels * 100) / 100.0;
    }
}
