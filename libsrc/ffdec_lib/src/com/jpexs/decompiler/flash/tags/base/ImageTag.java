/*
 *  Copyright (C) 2010-2014 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.commonshape.SVGExporter;
import com.jpexs.decompiler.flash.exporters.shape.BitmapExporter;
import com.jpexs.decompiler.flash.exporters.shape.CanvasShapeExporter;
import com.jpexs.decompiler.flash.exporters.shape.SVGShapeExporter;
import com.jpexs.decompiler.flash.timeline.DepthState;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.FILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.LINESTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.SerializableImage;
import java.awt.Color;
import java.awt.Shape;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public abstract class ImageTag extends CharacterTag implements DrawableTag {

    protected SerializableImage cachedImage;

    public ImageTag(SWF swf, int id, String name, ByteArrayRange data) {
        super(swf, id, name, data);
    }

    public abstract InputStream getImageData();

    public abstract SerializableImage getImage();

    public abstract void setImage(byte[] data) throws IOException;

    public abstract String getImageFormat();

    public boolean importSupported() {
        return true;
    }

    public static String getImageFormat(byte[] data) {
        return getImageFormat(new ByteArrayRange(data));
    }

    public static String getImageFormat(ByteArrayRange data) {
        if (SWF.hasErrorHeader(data)) {
            return "jpg";
        }
        if (data.getLength() > 2 && ((data.get(0) & 0xff) == 0xff) && ((data.get(1) & 0xff) == 0xd8)) {
            return "jpg";
        }
        if (data.getLength() > 6 && ((data.get(0) & 0xff) == 0x47) && ((data.get(1) & 0xff) == 0x49) && ((data.get(2) & 0xff) == 0x46) && ((data.get(3) & 0xff) == 0x38) && ((data.get(4) & 0xff) == 0x39) && ((data.get(5) & 0xff) == 0x61)) {
            return "gif";
        }

        if (data.getLength() > 8 && ((data.get(0) & 0xff) == 0x89) && ((data.get(1) & 0xff) == 0x50) && ((data.get(2) & 0xff) == 0x4e) && ((data.get(3) & 0xff) == 0x47) && ((data.get(4) & 0xff) == 0x0d) && ((data.get(5) & 0xff) == 0x0a) && ((data.get(6) & 0xff) == 0x1a) && ((data.get(7) & 0xff) == 0x0a)) {
            return "png";
        }

        return "unk";
    }

    protected static int max255(float val) {
        if (val > 255) {
            return 255;
        }
        return (int) val;
    }

    protected static Color intToColor(int val) {
        return new Color(val & 0xff, (val >> 8) & 0xff, (val >> 16) & 0xff, (val >> 24) & 0xff);
    }

    protected static int colorToInt(Color c) {
        return (c.getAlpha() << 24) | (c.getBlue() << 16) | (c.getGreen() << 8) | c.getRed();
    }

    protected static Color multiplyAlpha(Color c) {
        float multiplier = c.getAlpha() == 0 ? 0 : 255.0f / c.getAlpha();
        return new Color(max255(c.getRed() * multiplier), max255(c.getGreen() * multiplier), max255(c.getBlue() * multiplier), c.getAlpha());
    }

    private SHAPEWITHSTYLE getShape() {
        RECT rect = getRect(new HashSet<BoundedTag>());
        return getShape(rect, false);
    }
    
    public SHAPEWITHSTYLE getShape(RECT rect, boolean fill) {
        boolean translated = rect.Xmin != 0 || rect.Ymin != 0;
        SHAPEWITHSTYLE shape = new SHAPEWITHSTYLE();
        shape.fillStyles = new FILLSTYLEARRAY();
        shape.fillStyles.fillStyles = new FILLSTYLE[1];
        FILLSTYLE fillStyle = new FILLSTYLE();
        fillStyle.fillStyleType = FILLSTYLE.REPEATING_BITMAP;
        fillStyle.bitmapId = getCharacterId();
        MATRIX matrix = new MATRIX();
        matrix.hasScale = true;
        if (fill) { 
            RECT imageRect = getRect(new HashSet<BoundedTag>());
            matrix.scaleX = (int) ((((long) SWF.unitDivisor) << 16) * rect.getWidth() / imageRect.getWidth());
            matrix.scaleY = (int) ((((long) SWF.unitDivisor) << 16) * rect.getHeight() / imageRect.getHeight());
        } else {
            matrix.scaleX = ((int) SWF.unitDivisor) << 16;
            matrix.scaleY = matrix.scaleX;
        }
        if (translated) {
            matrix.translateX = rect.Xmin;
            matrix.translateY = rect.Ymin;
        }
        fillStyle.bitmapMatrix = matrix;
        shape.fillStyles.fillStyles[0] = fillStyle;

        shape.lineStyles = new LINESTYLEARRAY();
        shape.lineStyles.lineStyles = new LINESTYLE[0];
        shape.shapeRecords = new ArrayList<>();
        StyleChangeRecord style = new StyleChangeRecord();
        style.stateFillStyle0 = true;
        style.fillStyle0 = 1;
        style.stateMoveTo = true;
        if (translated) {
            style.moveDeltaX = rect.Xmin;
            style.moveDeltaY = rect.Ymin;
        }
        shape.shapeRecords.add(style);
        StraightEdgeRecord top = new StraightEdgeRecord();
        top.generalLineFlag = true;
        top.deltaX = rect.getWidth();
        StraightEdgeRecord right = new StraightEdgeRecord();
        right.generalLineFlag = true;
        right.deltaY = rect.getHeight();
        StraightEdgeRecord bottom = new StraightEdgeRecord();
        bottom.generalLineFlag = true;
        bottom.deltaX = -rect.getWidth();
        StraightEdgeRecord left = new StraightEdgeRecord();
        left.generalLineFlag = true;
        left.deltaY = -rect.getHeight();
        shape.shapeRecords.add(top);
        shape.shapeRecords.add(right);
        shape.shapeRecords.add(bottom);
        shape.shapeRecords.add(left);
        shape.shapeRecords.add(new EndShapeRecord());
        return shape;
    }

    @Override
    public RECT getRect(Set<BoundedTag> added) {
        SerializableImage image = getImage();
        int widthInTwips = (int) (image.getWidth() * SWF.unitDivisor);
        int heightInTwips = (int) (image.getHeight() * SWF.unitDivisor);
        return new RECT(0, widthInTwips, 0, heightInTwips);
    }

    @Override
    public void toImage(int frame, int time, int ratio, DepthState stateUnderCursor, int mouseButton, SerializableImage image, Matrix transformation, ColorTransform colorTransform) {
        BitmapExporter.export(swf, getShape(), null, image, transformation, colorTransform);
    }

    @Override
    public void toSVG(SVGExporter exporter, int ratio, ColorTransform colorTransform, int level, double zoom) throws IOException {
        SVGShapeExporter shapeExporter = new SVGShapeExporter(swf, getShape(), exporter, null, colorTransform, zoom);
        shapeExporter.export();
    }

    @Override
    public String toHtmlCanvas(double unitDivisor) {
        CanvasShapeExporter cse = new CanvasShapeExporter(null, unitDivisor, swf, getShape(), new ColorTransform(), 0, 0);
        cse.export();
        return cse.getShapeData();
    }

    @Override
    public int getNumFrames() {
        return 1;
    }

    @Override
    public boolean isSingleFrame() {
        return true;
    }

    @Override
    public Shape getOutline(int frame, int time, int ratio, DepthState stateUnderCursor, int mouseButton, Matrix transformation) {
        return transformation.toTransform().createTransformedShape(getShape().getOutline());
    }
    
    public void clearCache() {
        cachedImage = null;
    }
}
