/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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

import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.commonshape.SVGExporter;
import com.jpexs.decompiler.flash.timeline.DepthState;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.helpers.SerializableImage;
import java.awt.Shape;
import java.io.IOException;

/**
 *
 * @author JPEXS
 */
public interface DrawableTag extends BoundedTag {

    public void toImage(int frame, int time, int ratio, DepthState stateUnderCursor, int mouseButton, SerializableImage image, Matrix transformation, ColorTransform colorTransform);

    public void toSVG(SVGExporter exporter, int ratio, ColorTransform colorTransform, int level, double zoom) throws IOException;

    public String toHtmlCanvas(double unitDivisor);

    public int getNumFrames();

    public boolean isSingleFrame();

    public Shape getOutline(int frame, int time, int ratio, DepthState stateUnderCursor, int mouseButton, Matrix transformation);
}
