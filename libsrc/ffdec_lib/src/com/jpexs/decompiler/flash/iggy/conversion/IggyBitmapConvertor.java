/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.iggy.conversion;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.helpers.ImageHelper;
import com.jpexs.decompiler.flash.iggy.IggyFile;
import com.jpexs.decompiler.flash.iggy.IggyObjectData;
import com.jpexs.decompiler.flash.iggy.IggyTextureDecoder;
import com.jpexs.decompiler.flash.iggy.IggyTextureMetadata;
import com.jpexs.decompiler.flash.iggy.IggyTimeline;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG2Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG3Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsLossless2Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsLosslessTag;
import com.jpexs.decompiler.flash.tags.DefineBitsTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.enums.ImageFormat;
import com.jpexs.decompiler.flash.tags.profiles.iggy.IggyExternalImage;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.Helper;
import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.zip.DeflaterOutputStream;
import javax.imageio.ImageIO;

/**
 * Bitmap definitions: preserved SWF payloads, decoded pixels and external
 * resources.
 */
public final class IggyBitmapConvertor {

    private IggyBitmapConvertor() {
    }

    public static ImageTag toSwf(SWF swf, IggyFile file, IggyTimeline t, long at, int id) throws IOException {
        boolean wide = t.is64();
        int header = wide ? 32 : 16;
        int width = IggyShapeStyles.u16(t, at + header);
        int height = IggyShapeStyles.u16(t, at + header + 2);
        long encoded = t.reference(at + (wide ? 72 : 48));
        long rawField = at + (wide ? 64 : 44);
        long rawValue = wide ? t.bytes(rawField, 8).getLong() : t.bytes(rawField, 4).getInt();
        // This runtime pixel pointer is normally an unrelocated literal zero.
        long raw = rawValue == 0 || rawValue == 1 ? 0 : t.reference(rawField);
        int kind = IggyShapeStyles.u8(t, at + header + 5);
        int format = IggyShapeStyles.u8(t, at + header + 6);
        if (encoded == 0 && raw == 0) {
            IggyExternalImage tag = new IggyExternalImage(swf);
            tag.characterID = id;
            tag.width = width;
            tag.height = height;
            tag.pixelFormat = IggyShapeStyles.u8(t, at + header + 4);
            tag.originalTag = kind;
            tag.originalBitmapFormat = format;
            tag.originalDataLength = t.bytes(at + (wide ? 80 : 52), 4).getInt() & 0xffffffffL;
            tag.scaleX = IggyShapeStyles.f32(t, at + (wide ? 48 : 28));
            tag.scaleY = IggyShapeStyles.f32(t, at + (wide ? 52 : 32));
            tag.offsetX = IggyShapeStyles.f32(t, at + (wide ? 56 : 36));
            tag.offsetY = IggyShapeStyles.f32(t, at + (wide ? 60 : 40));
            if (file != null && file.getOriginalFile() != null) {
                File source = file.getOriginalFile();
                String name = source.getName();
                int dot = name.lastIndexOf('.');
                tag.fileName = (dot < 0 ? name : name.substring(0, dot)) + ".raw.iggytex";
                tag.setSourceDirectory(source.getAbsoluteFile().getParentFile());
                for (IggyTextureMetadata metadata : file.getTextureMetadata()) {
                    for (IggyTextureMetadata.Record record : metadata.getRecords()) {
                        if (record.getCharacterId() == id) {
                            byte[] data = record.getData();
                            java.nio.ByteBuffer b = java.nio.ByteBuffer.wrap(data).order(java.nio.ByteOrder.LITTLE_ENDIAN);
                            tag.resourceType = 8;
                            tag.resourceOffset = b.getInt() & 0xffffffffL;
                            tag.resourceFormat = b.getShort() & 65535;
                            tag.resourceWidth = b.getShort() & 65535;
                            tag.resourceHeight = b.getShort() & 65535;
                        }
                    }
                }
            }
            tag.setModified(true);
            return tag;
        }
        if (encoded == 0) {
            int length = pixelLength(width, height, 4);
            byte[] pixels = new byte[length];
            t.bytes(raw, length).get(pixels);
            BufferedImage image = rgba(pixels, width, height);
            return lossless(swf, id, image);
        }
        int length = IggyShapeStyles.u32(t, at + (wide ? 80 : 52));
        byte[] payload = new byte[length];
        t.bytes(encoded, length).get(payload);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (SWFOutputStream out = new SWFOutputStream(bytes, 10, "UTF-8")) {
            out.writeUI16(id);
            if (kind == 20 || kind == 36) {
                out.writeUI8(format);
                out.writeUI16(width);
                out.writeUI16(height);
            }
            out.write(payload);
        }
        byte[] data = bytes.toByteArray();
        SWFInputStream in = new SWFInputStream(swf, data);
        ByteArrayRange range = new ByteArrayRange(data);
        ImageTag tag;
        switch (kind) {
            case 6:
                throw new IOException("Iggy DefineBits with shared JPEG tables cannot be reconstructed without its tables");
            case 20:
                tag = new DefineBitsLosslessTag(in, range);
                break;
            case 21:
                tag = new DefineBitsJPEG2Tag(in, range);
                break;
            case 35:
                tag = new DefineBitsJPEG3Tag(in, range);
                break;
            case 36:
                tag = new DefineBitsLossless2Tag(in, range);
                break;
            default:
                throw new IOException("Unsupported Iggy embedded bitmap tag " + kind);
        }
        tag.setModified(true);
        return tag;
    }

    private static int pixelLength(int width, int height, int bytes) throws IOException {
        long count = (long) width * height * bytes;
        if (width <= 0 || height <= 0 || count > 268435456) {
            throw new IOException("Unsupported Iggy bitmap dimensions");
        }
        return (int) count;
    }

    private static BufferedImage rgba(byte[] data, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = new int[width * height];
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = (data[4 * i + 3] & 255) << 24 | (data[4 * i] & 255) << 16 | (data[4 * i + 1] & 255) << 8 | data[4 * i + 2] & 255;
        }
        image.setRGB(0, 0, width, height, pixels, 0, width);
        return image;
    }

    public static BufferedImage readExternal(IggyExternalImage tag) throws IOException {
        File file = tag.getResourceFile();
        if (file == null || !file.isFile() || tag.resourceOffset < 0 || tag.resourceType != 8) {
            return null;
        }
        int width = tag.resourceWidth;
        int height = tag.resourceHeight;
        pixelLength(width, height, 4);
        int format = tag.resourceFormat;
        int length = IggyTextureDecoder.dataLength(width, height, format);
        byte[] data = new byte[length];
        try (RandomAccessFile in = new RandomAccessFile(file, "r")) {
            if (tag.resourceOffset > in.length() - length) {
                throw new IOException("Iggy texture resource outside companion file");
            }
            in.seek(tag.resourceOffset);
            in.readFully(data);
        }
        int[] pixels = IggyTextureDecoder.decode(width, height, format, data);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, width, height, pixels, 0, width);
        // Resource dimensions may include packing/resizing; the display object keeps the logical dimensions.
        pixelLength(tag.width, tag.height, 4);
        BufferedImage result = new BufferedImage(tag.width, tag.height, BufferedImage.TYPE_INT_ARGB);
        int x = Math.round(tag.offsetX * width);
        int y = Math.round(tag.offsetY * height);
        int right = Math.round((tag.offsetX + tag.scaleX) * width);
        int bottom = Math.round((tag.offsetY + tag.scaleY) * height);
        Graphics2D graphics = result.createGraphics();
        graphics.setComposite(AlphaComposite.Src);
        graphics.drawImage(image, 0, 0, tag.width, tag.height, x, y, right, bottom, null);
        graphics.dispose();
        return result;
    }

    private static DefineBitsLossless2Tag lossless(SWF swf, int id, BufferedImage image) throws IOException {
        DefineBitsLossless2Tag tag = new DefineBitsLossless2Tag(swf);
        tag.characterID = id;
        tag.bitmapFormat = 5;
        tag.bitmapWidth = image.getWidth();
        tag.bitmapHeight = image.getHeight();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DeflaterOutputStream out = new DeflaterOutputStream(bytes)) {
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int pixel = image.getRGB(x, y);
                    int alpha = pixel >>> 24;
                    out.write(alpha);
                    out.write(((pixel >>> 16 & 255) * alpha + 127) / 255);
                    out.write(((pixel >>> 8 & 255) * alpha + 127) / 255);
                    out.write(((pixel & 255) * alpha + 127) / 255);
                }
            }
        }
        tag.zlibBitmapData = new ByteArrayRange(bytes.toByteArray());
        tag.setModified(true);
        return tag;
    }

    private static DefineBitsJPEG2Tag standaloneJpeg(SWF swf, DefineBitsTag input) throws IOException {
        InputStream original = input.getOriginalImageData();
        if (original == null) {
            throw new IggyUnsupportedTagException(input, "DefineBits character " + input.characterID + " requires JPEGTables");
        }
        byte[] jpeg;
        try (InputStream in = original) {
            // DefineBits already joins the shared tables and repairs EOI/SOI
            // boundaries. Keep the compressed JPEG; never encode it again.
            jpeg = Helper.readStream(in);
            // Do not use getImageCached(), which may return an error placeholder.
            if (ImageTag.getImageFormat(new ByteArrayRange(jpeg)) != ImageFormat.JPEG
                    || ImageIO.read(new ByteArrayInputStream(jpeg)) == null) {
                throw new IOException("Invalid JPEG image");
            }
        } catch (IOException | RuntimeException ex) {
            IggyUnsupportedTagException failure = new IggyUnsupportedTagException(input,
                    "Cannot convert DefineBits character " + input.characterID + ": missing or invalid JPEG tables/image data");
            failure.initCause(ex);
            throw failure;
        }
        return new DefineBitsJPEG2Tag(swf, null, input.characterID, jpeg);
    }

    public static IggyObjectData fromSwf(SWF swf, boolean wide, ImageTag input) throws IOException {
        ImageTag tag = input;
        if (input instanceof DefineBitsTag) {
            tag = standaloneJpeg(swf, (DefineBitsTag) input);
        }
        if (input instanceof IggyExternalImage && ((IggyExternalImage) input).replacementImage.getLength() != 0) {
            IggyExternalImage external = (IggyExternalImage) input;
            tag = lossless(swf, input.characterID, ImageHelper.read(external.replacementImage.getRangeData()));
        }
        IggyTimelineBuffer b = new IggyTimelineBuffer(wide);
        int size = wide ? 88 : 56;
        long at = IggyAssetConvertor.header(b, tag.characterID, 4, size);
        int header = wide ? 32 : 16;
        byte[] payload;
        int kind;
        int format = 0;
        int width;
        int height;
        int pixelFormat;
        float scaleX = 1;
        float scaleY = 1;
        float offsetX = 0;
        float offsetY = 0;
        if (tag instanceof IggyExternalImage) {
            IggyExternalImage external = (IggyExternalImage) tag;
            width = external.width;
            height = external.height;
            kind = external.originalTag;
            format = external.originalBitmapFormat;
            pixelFormat = external.pixelFormat;
            payload = new byte[0];
            scaleX = external.scaleX;
            scaleY = external.scaleY;
            offsetX = external.offsetX;
            offsetY = external.offsetY;
            b.u32(at + (wide ? 80 : 52), external.originalDataLength);
        } else {
            kind = tag.getId();
            if (kind != 20 && kind != 21 && kind != 35 && kind != 36
                    || (kind == 21 || kind == 35) && tag.getOriginalImageFormat() != ImageFormat.JPEG) {
                tag = lossless(swf, tag.characterID, tag.getImageCached().getBufferedImage());
                kind = 36;
            }
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (SWFOutputStream out = new SWFOutputStream(bytes, 10, "UTF-8")) {
                tag.getData(out);
            }
            byte[] encoded = bytes.toByteArray();
            Dimension dimensions = tag.getImageDimension();
            width = dimensions.width;
            height = dimensions.height;
            pixelFormat = kind == 20 || kind == 21 ? 3 : 4;
            int skip = 2;
            if (kind == 20 || kind == 36) {
                format = encoded[2] & 255;
                skip = 7;
            }
            payload = java.util.Arrays.copyOfRange(encoded, skip, encoded.length);
            b.u32(at + (wide ? 80 : 52), payload.length);
        }
        if (width < 0 || height < 0 || width > 65535 || height > 65535
                || !(tag instanceof IggyExternalImage) && (width == 0 || height == 0)) {
            throw new IOException("Invalid Iggy bitmap dimensions");
        }
        b.u16(at + header, width);
        b.u16(at + header + 2, height);
        b.u8(at + header + 4, pixelFormat);
        b.u8(at + header + 5, kind);
        b.u8(at + header + 6, format);
        b.value(at + header + 7, 0, 1, 3);
        b.f32(at + (wide ? 48 : 28), scaleX);
        b.f32(at + (wide ? 52 : 32), scaleY);
        b.f32(at + (wide ? 56 : 36), offsetX);
        b.f32(at + (wide ? 60 : 40), offsetY);
        b.pointer(at + (wide ? 64 : 44), -1);
        long body = payload.length == 0 ? -1 : b.allocate(payload.length);
        if (body >= 0) {
            for (int i = 0; i < payload.length; i++) {
                b.u8(body + i, payload[i] & 255);
            }
        }
        b.pointer(at + (wide ? 72 : 48), body);
        return new IggyObjectData(b.data.getAllBytes(), new ArrayList<>(b.fields.values()), b.characters, at, size, 4);
    }
}
