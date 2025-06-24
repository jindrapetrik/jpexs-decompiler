/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.exporters.morphshape;

import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.GRADRECORD;
import com.jpexs.decompiler.flash.types.RGB;

/**
 * Interface for morph shape exporters.
 *
 * @author JPEXS
 */
public interface IMorphShapeExporter {

    /**
     * Begins shape.
     */
    public void beginShape();

    /**
     * Ends shape.
     */
    public void endShape();

    /**
     * Begins fills.
     */
    public void beginFills();

    /**
     * Ends fills.
     */
    public void endFills();

    /**
     * Begins lines.
     */
    public void beginLines();

    /**
     * Ends lines.
     *
     * @param close Close shape
     */
    public void endLines(boolean close);

    /**
     * Begins fill.
     *
     * @param color Fill color
     * @param colorEnd End fill color
     */
    public void beginFill(RGB color, RGB colorEnd);

    /**
     * Begins gradient fill.
     *
     * @param type Gradient type
     * @param gradientRecords Gradient records
     * @param gradientRecordsEnd End gradient records
     * @param matrix Matrix
     * @param matrixEnd End matrix
     * @param spreadMethod Spread method
     * @param interpolationMethod Interpolation method
     * @param focalPointRatio Focal point ratio
     * @param focalPointRatioEnd End focal point ratio
     */
    public void beginGradientFill(int type, GRADRECORD[] gradientRecords, GRADRECORD[] gradientRecordsEnd, Matrix matrix, Matrix matrixEnd, int spreadMethod, int interpolationMethod, float focalPointRatio, float focalPointRatioEnd);

    /**
     * Begins bitmap fill.
     *
     * @param bitmapId Bitmap ID
     * @param matrix Matrix
     * @param matrixEnd End matrix
     * @param repeat Repeat
     * @param smooth Smooth
     * @param colorTransform Color transform
     */
    public void beginBitmapFill(int bitmapId, Matrix matrix, Matrix matrixEnd, boolean repeat, boolean smooth, ColorTransform colorTransform);

    /**
     * Ends fill.
     */
    public void endFill();

    /**
     * Begins line style.
     *
     * @param thickness Line thickness
     * @param thicknessEnd End line thickness
     * @param color Line color
     * @param colorEnd End line color
     * @param pixelHinting Pixel hinting
     * @param scaleMode Scale mode
     * @param startCaps Start caps
     * @param endCaps End caps
     * @param joints Joints
     * @param miterLimit Miter limit
     * @param noClose No close
     */
    public void lineStyle(double thickness, double thicknessEnd, RGB color, RGB colorEnd, boolean pixelHinting, String scaleMode, int startCaps, int endCaps, int joints, float miterLimit, boolean noClose);

    /**
     * Begins gradient line style.
     *
     * @param type Gradient type
     * @param gradientRecords Gradient records
     * @param gradientRecordsEnd End gradient records
     * @param matrix Matrix
     * @param matrixEnd End matrix
     * @param spreadMethod Spread method
     * @param interpolationMethod Interpolation method
     * @param focalPointRatio Focal point ratio
     * @param focalPointRatioEnd End focal point ratio
     */
    public void lineGradientStyle(int type, GRADRECORD[] gradientRecords, GRADRECORD[] gradientRecordsEnd, Matrix matrix, Matrix matrixEnd, int spreadMethod, int interpolationMethod, float focalPointRatio, float focalPointRatioEnd);

    /**
     * Begins bitmap line style.
     *
     * @param bitmapId Bitmap ID
     * @param matrix Matrix
     * @param matrixEnd End matrix
     * @param repeat Repeat
     * @param smooth Smooth
     * @param colorTransform Color transform
     */
    public void lineBitmapStyle(int bitmapId, Matrix matrix, Matrix matrixEnd, boolean repeat, boolean smooth, ColorTransform colorTransform);

    /**
     * Moves cursor to.
     * @param x X
     * @param y Y
     * @param x2 X2
     * @param y2 Y2
     */
    public void moveTo(double x, double y, double x2, double y2);

    /**
     * Draws line to.
     * @param x X
     * @param y Y
     * @param x2 X2
     * @param y2 Y2
     */
    public void lineTo(double x, double y, double x2, double y2);

    /**
     * Draws curve to.
     * @param controlX Control X
     * @param controlY Control Y
     * @param anchorX Anchor X
     * @param anchorY Anchor Y
     * @param controlX2 Control X2
     * @param controlY2 Control Y2
     * @param anchorX2 Anchor X2
     * @param anchorY2 Anchor Y2
     */
    public void curveTo(double controlX, double controlY, double anchorX, double anchorY, double controlX2, double controlY2, double anchorX2, double anchorY2);
}
