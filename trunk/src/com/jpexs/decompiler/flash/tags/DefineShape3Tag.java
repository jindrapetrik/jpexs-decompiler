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
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.exporters.ExportRectangle;
import com.jpexs.decompiler.flash.exporters.SVGExporter;
import com.jpexs.decompiler.flash.exporters.SVGExporterContext;
import com.jpexs.decompiler.flash.exporters.shape.SVGShapeExporter;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Set;

public class DefineShape3Tag extends ShapeTag {

    @SWFType(BasicType.UI16)
    public int shapeId;
    public RECT shapeBounds;
    public SHAPEWITHSTYLE shapes;
    public static final int ID = 32;

    @Override
    public int getShapeNum() {
        return 3;
    }

    @Override
    public SHAPEWITHSTYLE getShapes() {
        return shapes;
    }

    @Override
    public Set<Integer> getNeededCharacters() {
        return shapes.getNeededCharacters();
    }

    @Override
    public RECT getRect() {
        return shapeBounds;
    }

    @Override
    public String toSVG(SVGExporterContext exporterContext, int ratio, ColorTransform colorTransform, int level) {
        ExportRectangle rect = new ExportRectangle(getRect());
        SVGExporter svgExporter = new SVGExporter(rect, new ColorTransform());
        SVGShapeExporter exporter = new SVGShapeExporter(swf, getShapes(), svgExporter, null, new ColorTransform() /*FIXME?*/);
        exporter.export();
        return svgExporter.getSVG();
    }

    /*    @Override
     public void toImage(int frame, int ratio, java.awt.Point mousePos, int mouseButton, SerializableImage image, Matrix transformation, ColorTransform colorTransform) {
     BitmapExporter.exportTo(swf, getShapes(), null, image, transformation, colorTransform);
     }*/
    @Override
    public int getCharacterId() {
        return shapeId;
    }

    public DefineShape3Tag(SWF swf, byte[] headerData, byte[] data, long pos) throws IOException {
        super(swf, ID, "DefineShape3", headerData, data, pos);
        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), swf.version);
        shapeId = sis.readUI16();
        shapeBounds = sis.readRECT();
        shapes = sis.readSHAPEWITHSTYLE(3);
    }

    @Override
    public int getNumFrames() {
        return 1;
    }

    @Override
    public boolean isSingleFrame() {
        return true;
    }
}
