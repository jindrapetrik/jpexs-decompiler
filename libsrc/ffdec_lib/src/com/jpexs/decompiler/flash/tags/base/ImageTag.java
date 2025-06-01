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
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.commonshape.ExportRectangle;
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
import com.jpexs.decompiler.flash.types.LINESTYLE2;
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
 * Base class for image tags.
 *
 * @author JPEXS
 */
public abstract class ImageTag extends DrawableTag {

    /**
     * Character ID
     */
    @SWFType(BasicType.UI16)
    public int characterID;

    /**
     * Cached image
     */
    protected SerializableImage cachedImage;

    /**
     * Constructs new ImageTag
     * @param swf SWF
     * @param id ID
     * @param name Name
     * @param data Data
     */
    public ImageTag(SWF swf, int id, String name, ByteArrayRange data) {
        super(swf, id, name, data);
    }

    /**
     * Gets original image data.
     * @return Original image data
     */
    public abstract InputStream getOriginalImageData();

    /**
     * Gets image.
     * @return Image
     */
    protected abstract SerializableImage getImage();

    /**
     * Gets image dimension.
     * @return Image dimension
     */
    public abstract Dimension getImageDimension();

    /**
     * Sets image.
     * @param data Image data
     * @throws IOException On I/O error
     */
    public abstract void setImage(byte[] data) throws IOException;

    /**
     * Gets original image format.
     * @return Original image format
     */
    public abstract ImageFormat getOriginalImageFormat();

    /**
     * Checks if import is supported.
     * @return True if supported, false otherwise
     */
    public boolean importSupported() {
        return true;
    }

    /**
     * Gets image format.
     * @return Image format
     */
    public abstract ImageFormat getImageFormat();

    /**
     * Gets image format.
     * @param data Data
     * @return Image format
     */
    public static ImageFormat getImageFormat(byte[] data) {
        return getImageFormat(new ByteArrayRange(data));
    }

    /**
     * Gets image format.
     * @param data Data
     * @return Image format
     */
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

    /**
     * Gets image. Gets it from cache when available.
     * @return Image
     */
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

    /**
     * Gets converted image data. Converted means for example DefineBitsJPEG3
     * including alpha channel - PNG images.
     *
     * @return Converted image data
     */
    public InputStream getConvertedImageData() {
        if (getImageFormat() == getOriginalImageFormat()) { //no need to convert
            InputStream is = getOriginalImageData();
            if (is != null) {
                return is;
            }
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageHelper.write(getImage().getBufferedImage(), getImageFormat(), baos);
        return new ByteArrayInputStream(baos.toByteArray());
    }

    /**
     * Gets original image data if available, if not, then converted. Original
     * image data can be for example DefineBitsJPEG3 without transparency.
     *
     * @return Image data
     */
    public InputStream getImageData() {
        InputStream is = getOriginalImageData();
        if (is != null) {
            return is;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageHelper.write(getImage().getBufferedImage(), getImageFormat(), baos);
        return new ByteArrayInputStream(baos.toByteArray());
    }

    /**
     * Checks if data has error header.
     * @param data Data
     * @return True if has error header, false otherwise
     */
    public static boolean hasErrorHeader(byte[] data) {
        return hasErrorHeader(new ByteArrayRange(data));
    }

    /**
     * Checks if data has error header.
     * @param data Data
     * @return True if has error header, false otherwise
     */
    public static boolean hasErrorHeader(ByteArrayRange data) {
        if (data.getLength() > 4) {
            if ((data.get(0) & 0xff) == 0xff && (data.get(1) & 0xff) == 0xd9
                    && (data.get(2) & 0xff) == 0xff && (data.get(3) & 0xff) == 0xd8) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets shape.
     * @param shapeNum Shape number (DefineShape1, DefineShape2, ...)
     * @return Shape
     */
    private SHAPEWITHSTYLE getShape(int shapeNum) {
        RECT rect = getRect();
        return getShape(rect, false, shapeNum);
    }

    /**
     * Gets shape.
     * @param rect Rectangle
     * @param fill Fill
     * @param shapeNum Shape number (DefineShape1, DefineShape2, ...)
     * @return Shape
     */
    public SHAPEWITHSTYLE getShape(RECT rect, boolean fill, int shapeNum) {
        boolean translated = rect.Xmin != 0 || rect.Ymin != 0;
        SHAPEWITHSTYLE shape = new SHAPEWITHSTYLE();
        shape.fillStyles = new FILLSTYLEARRAY();
        shape.fillStyles.fillStyles = new FILLSTYLE[1];
        FILLSTYLE fillStyle = new FILLSTYLE();
        fillStyle.inShape3 = shapeNum >= 3;
        fillStyle.fillStyleType = Configuration.shapeImportUseNonSmoothedFill.get()
                ? FILLSTYLE.NON_SMOOTHED_REPEATING_BITMAP : FILLSTYLE.REPEATING_BITMAP;
        fillStyle.bitmapId = getCharacterId();
        MATRIX matrix = new MATRIX();
        matrix.hasScale = true;
        if (fill) {
            RECT imageRect = getRect();
            matrix.scaleX = (float) (SWF.unitDivisor * rect.getWidth() / imageRect.getWidth());
            matrix.scaleY = (float) (SWF.unitDivisor * rect.getHeight() / imageRect.getHeight());
        } else {
            matrix.scaleX = (float) SWF.unitDivisor;
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
        shape.lineStyles.lineStyles2 = new LINESTYLE2[0];
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
        top.calculateBits();
        StraightEdgeRecord right = new StraightEdgeRecord();
        right.generalLineFlag = true;
        right.deltaY = rect.getHeight();
        right.calculateBits();
        StraightEdgeRecord bottom = new StraightEdgeRecord();
        bottom.generalLineFlag = true;
        bottom.deltaX = -rect.getWidth();
        bottom.calculateBits();
        StraightEdgeRecord left = new StraightEdgeRecord();
        left.generalLineFlag = true;
        left.deltaY = -rect.getHeight();
        left.calculateBits();
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
    public Shape getOutline(boolean fast, int frame, int time, int ratio, RenderContext renderContext, Matrix transformation, boolean stroked, ExportRectangle viewRect, double unzoom) {
        return transformation.toTransform().createTransformedShape(getShape(1).getOutline(fast, 1, swf, stroked));
    }

    @Override
    public void toImage(int frame, int time, int ratio, RenderContext renderContext, SerializableImage image, SerializableImage fullImage, boolean isClip, Matrix transformation, Matrix strokeTransformation, Matrix absoluteTransformation, Matrix fullTransformation, ColorTransform colorTransform, double unzoom, boolean sameImage, ExportRectangle viewRect, ExportRectangle viewRectRaw, boolean scaleStrokes, int drawMode, int blendMode, boolean canUseSmoothing) {
        BitmapExporter.export(ShapeTag.WIND_EVEN_ODD, 1, swf, getShape(1), null, image, unzoom, transformation, strokeTransformation, colorTransform, true, canUseSmoothing);
    }

    @Override
    public void toSVG(SVGExporter exporter, int ratio, ColorTransform colorTransform, int level, Matrix transformation, Matrix strokeTransformation) throws IOException {
        SVGShapeExporter shapeExporter = new SVGShapeExporter(ShapeTag.WIND_EVEN_ODD, 1, swf, getShape(1), getCharacterId(), exporter, null, colorTransform, 1, exporter.getZoom(), strokeTransformation);
        shapeExporter.export();
    }

    @Override
    public void toHtmlCanvas(StringBuilder result, double unitDivisor) {
        CanvasShapeExporter cse = new CanvasShapeExporter(ShapeTag.WIND_EVEN_ODD, 1, null, unitDivisor, swf, getShape(1), null, 0, 0);
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

    @Override
    public RECT getRectWithStrokes() {
        return getRect();
    }

    /**
     * Checks if image is same as other image.
     * @param otherImage Other image
     * @return True if same, false otherwise
     */
    public boolean isSameImage(ImageTag otherImage) {
        SerializableImage imgA = getImageCached();
        SerializableImage imgB = otherImage.getImageCached();
        if (imgA.getWidth() != imgB.getWidth() || imgA.getHeight() != imgB.getHeight()) {
            return false;
        }

        int width = imgA.getWidth();
        int height = imgA.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (imgA.getRGB(x, y) != imgB.getRGB(x, y)) {
                    return false;
                }
            }
        }

        return true;
    }
}
