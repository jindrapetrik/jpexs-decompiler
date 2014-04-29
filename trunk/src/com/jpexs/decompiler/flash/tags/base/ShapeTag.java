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
package com.jpexs.decompiler.flash.tags.base;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.commonshape.SVGExporter;
import com.jpexs.decompiler.flash.exporters.shape.BitmapExporter;
import com.jpexs.decompiler.flash.exporters.shape.CanvasShapeExporter;
import com.jpexs.decompiler.flash.exporters.shape.SVGShapeExporter;
import com.jpexs.decompiler.flash.timeline.DepthState;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.helpers.SerializableImage;
import java.awt.Shape;
import java.io.IOException;

/**
 *
 * @author JPEXS
 */
public abstract class ShapeTag extends CharacterTag implements BoundedTag, DrawableTag {

    public ShapeTag(SWF swf, int id, String name, byte[] headerData, byte[] data, long pos) {
        super(swf, id, name, headerData, data, pos);
    }

    public abstract SHAPEWITHSTYLE getShapes();

    public abstract int getShapeNum();

    @Override
    public Shape getOutline(int frame, int time, int ratio, DepthState stateUnderCursor, int mouseButton, Matrix transformation) {
        return transformation.toTransform().createTransformedShape(getShapes().getOutline());
    }

    @Override
    public void toImage(int frame, int time, int ratio, DepthState stateUnderCursor, int mouseButton, SerializableImage image, Matrix transformation, ColorTransform colorTransform) {
        BitmapExporter.export(swf, getShapes(), null, image, transformation, colorTransform);
    }

    @Override
    public void toSVG(SVGExporter exporter, int ratio, ColorTransform colorTransform, int level) throws IOException {
        SVGShapeExporter shapeExporter = new SVGShapeExporter(swf, getShapes(), exporter, null, colorTransform);
        shapeExporter.export();
    }

    @Override
    public String toHtmlCanvas(double unitDivisor) {
        CanvasShapeExporter cse = new CanvasShapeExporter(null, unitDivisor, swf, getShapes(), new ColorTransform(), 0, 0);
        cse.export();
        return cse.getShapeData();
    }

}
