/*
 *  Copyright (C) 2010-2014 JPEXS
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
    protected String pathData;
    protected String pathDataEnd;

    public DefaultSVGMorphShapeExporter(SHAPE shape, SHAPE endShape, ColorTransform colorTransform) {
        super(shape, endShape, colorTransform);
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
    public void beginGradientFill(int type, GRADRECORD[] gradientRecords, GRADRECORD[] gradientRecordsEnd, Matrix matrix, Matrix matrixEnd, int spreadMethod, int interpolationMethod, float focalPointRatio) {
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
    public void lineStyle(double thickness, double thicknessEnd, RGB color, RGB colorEnd, boolean pixelHinting, String scaleMode, int startCaps, int endCaps, int joints, int miterLimit) {
        finalizePath();
    }

    @Override
    public void lineGradientStyle(int type, GRADRECORD[] gradientRecords, GRADRECORD[] gradientRecordsEnd, Matrix matrix, Matrix matrixEnd, int spreadMethod, int interpolationMethod, float focalPointRatio) {
    }

    @Override
    public void moveTo(double x, double y, double x2, double y2) {
        currentDrawCommand = "";
        pathData += "M"
                + roundPixels20(x / SWF.unitDivisor) + " "
                + roundPixels20(y / SWF.unitDivisor) + " ";
        pathDataEnd += "M"
                + roundPixels20(x2 / SWF.unitDivisor) + " "
                + roundPixels20(y2 / SWF.unitDivisor) + " ";
    }

    @Override
    public void lineTo(double x, double y, double x2, double y2) {
        if (!currentDrawCommand.equals(DRAW_COMMAND_L)) {
            currentDrawCommand = DRAW_COMMAND_L;
            pathData += "L";
            pathDataEnd += "L";
        }
        pathData += roundPixels20(x / SWF.unitDivisor) + " "
                + roundPixels20(y / SWF.unitDivisor) + " ";
        pathDataEnd += roundPixels20(x2 / SWF.unitDivisor) + " "
                + roundPixels20(y2 / SWF.unitDivisor) + " ";
    }

    @Override
    public void curveTo(double controlX, double controlY, double anchorX, double anchorY, double controlX2, double controlY2, double anchorX2, double anchorY2) {
        if (!currentDrawCommand.equals(DRAW_COMMAND_Q)) {
            currentDrawCommand = DRAW_COMMAND_Q;
            pathData += "Q";
            pathDataEnd += "Q";
        }
        pathData += roundPixels20(controlX / SWF.unitDivisor) + " "
                + roundPixels20(controlY / SWF.unitDivisor) + " "
                + roundPixels20(anchorX / SWF.unitDivisor) + " "
                + roundPixels20(anchorY / SWF.unitDivisor) + " ";
        pathDataEnd += roundPixels20(controlX2 / SWF.unitDivisor) + " "
                + roundPixels20(controlY2 / SWF.unitDivisor) + " "
                + roundPixels20(anchorX2 / SWF.unitDivisor) + " "
                + roundPixels20(anchorY2 / SWF.unitDivisor) + " ";
    }

    protected void finalizePath() {
        pathData = "";
        pathDataEnd = "";
        currentDrawCommand = "";
    }

    protected double roundPixels20(double pixels) {
        return Math.round(pixels * 100) / 100.0;
    }

    protected double roundPixels400(double pixels) {
        return Math.round(pixels * 10000) / 10000.0;
    }
}
