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
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.commonshape.SVGExporter;
import com.jpexs.decompiler.flash.exporters.shape.BitmapExporter;
import com.jpexs.decompiler.flash.exporters.shape.CanvasShapeExporter;
import com.jpexs.decompiler.flash.exporters.shape.SVGShapeExporter;
import com.jpexs.decompiler.flash.helpers.ImageHelper;
import com.jpexs.decompiler.flash.tags.TagInfo;
import com.jpexs.decompiler.flash.tags.enums.ImageFormat;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.FILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.LINESTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.SerializableImage;
import java.awt.Dimension;
import java.awt.Shape;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public abstract class ImageTag extends DrawableTag {

    @SWFType(BasicType.UI16)
    public int characterID;

    protected SerializableImage cachedImage;

    public ImageTag(SWF swf, int id, String name, ByteArrayRange data) {
        super(swf, id, name, data);
    }

    public abstract InputStream getOriginalImageData();

    protected abstract SerializableImage getImage();

    public abstract Dimension getImageDimension();

    public abstract void setImage(byte[] data) throws IOException;

    public abstract ImageFormat getImageFormat();

    public abstract ImageFormat getOriginalImageFormat();

    public boolean importSupported() {
        return true;
    }

    public static ImageFormat getImageFormat(byte[] data) {
        return getImageFormat(new ByteArrayRange(data));
    }

    public static ImageFormat getImageFormat(ByteArrayRange data) {
        if (hasErrorHeader(data)) {
            return ImageFormat.JPEG;
        }

        if (data.getLength() > 2 && ((data.get(0) & 0xff) == 0xff) && ((data.get(1) & 0xff) == 0xd8)) {
            return ImageFormat.JPEG;
        }

        if (data.getLength() > 6 && ((data.get(0) & 0xff) == 0x47) && ((data.get(1) & 0xff) == 0x49) && ((data.get(2) & 0xff) == 0x46) && ((data.get(3) & 0xff) == 0x38) && ((data.get(4) & 0xff) == 0x39) && ((data.get(5) & 0xff) == 0x61)) {
            return ImageFormat.GIF;
        }

        if (data.getLength() > 8 && ((data.get(0) & 0xff) == 0x89) && ((data.get(1) & 0xff) == 0x50) && ((data.get(2) & 0xff) == 0x4e) && ((data.get(3) & 0xff) == 0x47) && ((data.get(4) & 0xff) == 0x0d) && ((data.get(5) & 0xff) == 0x0a) && ((data.get(6) & 0xff) == 0x1a) && ((data.get(7) & 0xff) == 0x0a)) {
            return ImageFormat.PNG;
        }

        return ImageFormat.UNKNOWN;
    }

    public SerializableImage getImageCached() {
        if (cachedImage != null) {
            return cachedImage;
        }

        SerializableImage image = getImage();
        if (Configuration.cacheImages.get()) {
            cachedImage = image;
        }

        return image;
    }

    public InputStream getImageData() {
        InputStream is = getOriginalImageData();
        if (is != null) {
            return is;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageHelper.write(getImage().getBufferedImage(), getImageFormat(), baos);
        return new ByteArrayInputStream(baos.toByteArray());
    }

    public static boolean hasErrorHeader(byte[] data) {
        return hasErrorHeader(new ByteArrayRange(data));
    }

    public static boolean hasErrorHeader(ByteArrayRange data) {
        if (data.getLength() > 4) {
            if ((data.get(0) & 0xff) == 0xff && (data.get(1) & 0xff) == 0xd9
                    && (data.get(2) & 0xff) == 0xff && (data.get(3) & 0xff) == 0xd8) {
                return true;
            }
        }
        return false;
    }

    private SHAPEWITHSTYLE getShape() {
        RECT rect = getRect();
        return getShape(rect, false);
    }

    public SHAPEWITHSTYLE getShape(RECT rect, boolean fill) {
        boolean translated = rect.Xmin != 0 || rect.Ymin != 0;
        SHAPEWITHSTYLE shape = new SHAPEWITHSTYLE();
        shape.fillStyles = new FILLSTYLEARRAY();
        shape.fillStyles.fillStyles = new FILLSTYLE[1];
        FILLSTYLE fillStyle = new FILLSTYLE();
        fillStyle.fillStyleType = Configuration.shapeImportUseNonSmoothedFill.get()
                ? FILLSTYLE.NON_SMOOTHED_REPEATING_BITMAP : FILLSTYLE.REPEATING_BITMAP;
        fillStyle.bitmapId = getCharacterId();
        MATRIX matrix = new MATRIX();
        matrix.hasScale = true;
        if (fill) {
            RECT imageRect = getRect();
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
    public RECT getRect() {
        return getRect(null); // parameter not used
    }

    @Override
    public RECT getRect(Set<BoundedTag> added) {
        Dimension dimension = getImageDimension();
        int widthInTwips = (int) (dimension.getWidth() * SWF.unitDivisor);
        int heightInTwips = (int) (dimension.getHeight() * SWF.unitDivisor);
        return new RECT(0, widthInTwips, 0, heightInTwips);
    }

    @Override
    public int getUsedParameters() {
        return 0;
    }

    @Override
    public Shape getOutline(int frame, int time, int ratio, RenderContext renderContext, Matrix transformation, boolean stroked) {
        return transformation.toTransform().createTransformedShape(getShape().getOutline(swf, stroked));
    }

    @Override
    public void toImage(int frame, int time, int ratio, RenderContext renderContext, SerializableImage image, boolean isClip, Matrix transformation, Matrix strokeTransformation, Matrix absoluteTransformation, ColorTransform colorTransform) {
        BitmapExporter.export(swf, getShape(), null, image, transformation, strokeTransformation, colorTransform);
    }

    @Override
    public void toSVG(SVGExporter exporter, int ratio, ColorTransform colorTransform, int level) throws IOException {
        SVGShapeExporter shapeExporter = new SVGShapeExporter(swf, getShape(), getCharacterId(), exporter, null, colorTransform, 1);
        shapeExporter.export();
    }

    @Override
    public void toHtmlCanvas(StringBuilder result, double unitDivisor) {
        CanvasShapeExporter cse = new CanvasShapeExporter(null, unitDivisor, swf, getShape(), null, 0, 0);
        cse.export();
        result.append(cse.getShapeData());
    }

    @Override
    public int getNumFrames() {
        return 1;
    }

    @Override
    public boolean isSingleFrame() {
        return true;
    }

    public void clearCache() {
        cachedImage = null;
    }

    @Override
    public void getTagInfo(TagInfo tagInfo) {
        super.getTagInfo(tagInfo);
        Dimension dimension = getImageDimension();
        tagInfo.addInfo("general", "width", dimension.getWidth());
        tagInfo.addInfo("general", "height", dimension.getHeight());
    }

    @Override
    public int getCharacterId() {
        return characterID;
    }

    @Override
    public void setCharacterId(int characterId) {
        this.characterID = characterId;
    }
}
