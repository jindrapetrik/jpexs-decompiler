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

import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.GRADRECORD;
import com.jpexs.decompiler.flash.types.RGB;

/**
 * Shape exporter interface.
 *
 * @author JPEXS
 */
public interface IShapeExporter {

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
     * @param close Close path
     */
    public void endLines(boolean close);

    /**
     * Begins fill.
     * @param color Color
     */
    public void beginFill(RGB color);

    /**
     * Begins gradient fill.
     * @param type Type
     * @param gradientRecords Gradient records
     * @param matrix Matrix
     * @param spreadMethod Spread method
     * @param interpolationMethod Interpolation method
     * @param focalPointRatio Focal point ratio
     */
    public void beginGradientFill(int type, GRADRECORD[] gradientRecords, Matrix matrix, int spreadMethod, int interpolationMethod, float focalPointRatio);

    /**
     * Begins bitmap fill.
     * @param bitmapId Bitmap ID
     * @param matrix Matrix
     * @param repeat Repeat
     * @param smooth Smooth
     * @param colorTransform Color transform
     */
    public void beginBitmapFill(int bitmapId, Matrix matrix, boolean repeat, boolean smooth, ColorTransform colorTransform);

    /**
     * Ends fill.
     */
    public void endFill();

    /**
     * Sets line style.
     * @param thickness Thickness
     * @param color Color
     * @param pixelHinting Pixel hinting
     * @param scaleMode Scale mode
     * @param startCaps Start caps
     * @param endCaps End caps
     * @param joints Joints
     * @param miterLimit Miter limit
     * @param noClose No close
     */
    public void lineStyle(double thickness, RGB color, boolean pixelHinting, String scaleMode, int startCaps, int endCaps, int joints, float miterLimit, boolean noClose);

    /**
     * Sets line style to gradient.
     * @param type Type
     * @param gradientRecords Gradient records
     * @param matrix Matrix
     * @param spreadMethod Spread method
     * @param interpolationMethod Interpolation method
     * @param focalPointRatio Focal point ratio
     */
    public void lineGradientStyle(int type, GRADRECORD[] gradientRecords, Matrix matrix, int spreadMethod, int interpolationMethod, float focalPointRatio);

    /**
     * Sets line style to bitmap.
     * @param bitmapId Bitmap ID
     * @param matrix Matrix
     * @param repeat Repeat
     * @param smooth Smooth
     * @param colorTransform Color transform
     */
    public void lineBitmapStyle(int bitmapId, Matrix matrix, boolean repeat, boolean smooth, ColorTransform colorTransform);

    /**
     * Moves cursor to.
     * @param x X
     * @param y Y
     */
    public void moveTo(double x, double y);

    /**
     * Draws line to.
     * @param x X
     * @param y Y
     */
    public void lineTo(double x, double y);

    /**
     * Draws curve to.
     * @param controlX Control X
     * @param controlY Control Y
     * @param anchorX Anchor X
     * @param anchorY Anchor Y
     */
    public void curveTo(double controlX, double controlY, double anchorX, double anchorY);
}
