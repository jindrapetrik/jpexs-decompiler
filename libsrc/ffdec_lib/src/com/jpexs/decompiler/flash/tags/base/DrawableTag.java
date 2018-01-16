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
package com.jpexs.decompiler.flash.tags.base;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.commonshape.SVGExporter;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.SerializableImage;
import java.awt.Shape;
import java.io.IOException;

/**
 *
 * @author JPEXS
 */
public abstract class DrawableTag extends CharacterTag implements BoundedTag {

    public static final int PARAMETER_FRAME = 1;

    public static final int PARAMETER_TIME = 2;

    public static final int PARAMETER_RATIO = 4;

    public DrawableTag(SWF swf, int id, String name, ByteArrayRange data) {
        super(swf, id, name, data);
    }

    public abstract int getUsedParameters();

    public abstract Shape getOutline(int frame, int time, int ratio, RenderContext renderContext, Matrix transformation, boolean stroked);

    public abstract void toImage(int frame, int time, int ratio, RenderContext renderContext, SerializableImage image, boolean isClip, Matrix transformation, Matrix prevTransformation, Matrix absoluteTransformation, ColorTransform colorTransform);

    public abstract void toSVG(SVGExporter exporter, int ratio, ColorTransform colorTransform, int level) throws IOException;

    public abstract void toHtmlCanvas(StringBuilder result, double unitDivisor);

    public abstract int getNumFrames();

    public abstract boolean isSingleFrame();
}
