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
package com.jpexs.decompiler.flash.tags.base;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.exporters.commonshape.ExportRectangle;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.commonshape.SVGExporter;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.SerializableImage;
import java.awt.Shape;
import java.io.IOException;

/**
 * A character that can be drawn on the screen.
 *
 * @author JPEXS
 */
public abstract class DrawableTag extends CharacterTag implements BoundedTag {

    /**
     * Parameter frame
     */
    public static final int PARAMETER_FRAME = 1;

    /**
     * Parameter time
     */
    public static final int PARAMETER_TIME = 2;

    /**
     * Parameter ratio
     */
    public static final int PARAMETER_RATIO = 4;

    /**
     * Constructs new DrawableTag
     * @param swf SWF
     * @param id ID
     * @param name Name
     * @param data Data
     */
    public DrawableTag(SWF swf, int id, String name, ByteArrayRange data) {
        super(swf, id, name, data);
    }

    /**
     * Gets used parameters.
     * @return Used parameters - See PARAMETER_* constants
     */
    public abstract int getUsedParameters();

    /**
     * Calculates drawable outline.
     *
     * @param fast When the structure is large, can approximate to rectangles
     * instead of being slow.
     * @param frame Frame
     * @param time Time
     * @param ratio Ratio
     * @param renderContext Render context
     * @param transformation Transformation
     * @param stroked Stroked
     * @param viewRect View rectangle
     * @param unzoom Unzoom
     * @return Outline
     */
    public abstract Shape getOutline(boolean fast, int frame, int time, int ratio, RenderContext renderContext, Matrix transformation, boolean stroked, ExportRectangle viewRect, double unzoom);

    /**
     * Converts the drawable to image.
     * @param frame Frame
     * @param time Time
     * @param ratio Ratio
     * @param renderContext Render context
     * @param image Image
     * @param fullImage Full image
     * @param isClip Is clip
     * @param transformation Transformation
     * @param prevTransformation Previous transformation
     * @param absoluteTransformation Absolute transformation
     * @param fullTransformation Full transformation
     * @param colorTransform Color transform
     * @param unzoom Unzoom
     * @param sameImage Same image
     * @param viewRect View rectangle
     * @param viewRectRaw View rectangle raw
     * @param scaleStrokes Scale strokes
     * @param drawMode Draw mode
     * @param blendMode Blend mode
     * @param canUseSmoothing Can use smoothing
     */
    public abstract void toImage(int frame, int time, int ratio, RenderContext renderContext, SerializableImage image, SerializableImage fullImage, boolean isClip, Matrix transformation, Matrix prevTransformation, Matrix absoluteTransformation, Matrix fullTransformation, ColorTransform colorTransform, double unzoom, boolean sameImage, ExportRectangle viewRect, ExportRectangle viewRectRaw, boolean scaleStrokes, int drawMode, int blendMode, boolean canUseSmoothing);

    /**
     * Converts the drawable to SVG.
     * @param exporter SVG exporter
     * @param ratio Ratio
     * @param colorTransform Color transform
     * @param level Level
     * @param transformation Transformation
     * @param strokeTransformation Stroke transformation
     * @throws IOException On I/O error
     */
    public abstract void toSVG(SVGExporter exporter, int ratio, ColorTransform colorTransform, int level, Matrix transformation, Matrix strokeTransformation) throws IOException;

    /**
     * Converts the drawable to HTML canvas.
     * @param result Result
     * @param unitDivisor Unit divisor
     */
    public abstract void toHtmlCanvas(StringBuilder result, double unitDivisor);

    /**
     * Gets number of frames.
     * @return Number of frames
     */
    public abstract int getNumFrames();

    /**
     * Checks if the drawable is single frame.
     * @return True if single frame, false if not
     */
    public abstract boolean isSingleFrame();
}
