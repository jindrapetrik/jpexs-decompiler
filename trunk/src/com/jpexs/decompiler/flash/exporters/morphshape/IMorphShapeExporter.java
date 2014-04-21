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

import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.GRADRECORD;
import com.jpexs.decompiler.flash.types.RGB;

/**
 *
 * @author JPEXS
 */
public interface IMorphShapeExporter {

    public void beginShape();

    public void endShape();

    public void beginFills();

    public void endFills();

    public void beginLines();

    public void endLines();

    public void beginFill(RGB color, RGB colorEnd);

    public void beginGradientFill(int type, GRADRECORD[] gradientRecords, GRADRECORD[] gradientRecordsEnd, Matrix matrix, Matrix matrixEnd, int spreadMethod, int interpolationMethod, float focalPointRatio);

    public void beginBitmapFill(int bitmapId, Matrix matrix, boolean repeat, boolean smooth, ColorTransform colorTransform);

    public void endFill();

    public void lineStyle(double thickness, double thicknessEnd, RGB color, RGB colorEnd, boolean pixelHinting, String scaleMode, int startCaps, int endCaps, int joints, int miterLimit);

    public void lineGradientStyle(int type, GRADRECORD[] gradientRecords, GRADRECORD[] gradientRecordsEnd, Matrix matrix, Matrix matrixEnd, int spreadMethod, int interpolationMethod, float focalPointRatio);

    public void moveTo(double x, double y, double x2, double y2);

    public void lineTo(double x, double y, double x2, double y2);

    public void curveTo(double controlX, double controlY, double anchorX, double anchorY, double controlX2, double controlY2, double anchorX2, double anchorY2);
}
