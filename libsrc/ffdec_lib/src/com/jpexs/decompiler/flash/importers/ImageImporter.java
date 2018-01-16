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
package com.jpexs.decompiler.flash.importers;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.helpers.ImageHelper;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG2Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG3Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG4Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsLossless2Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsLosslessTag;
import com.jpexs.decompiler.flash.tags.DefineBitsTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.enums.ImageFormat;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.Helper;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 * @author JPEXS
 */
public class ImageImporter extends TagImporter {

    public Tag importImage(ImageTag it, byte[] newData) throws IOException {
        return importImage(it, newData, 0);
    }

    public Tag importImage(ImageTag it, byte[] newData, int tagType) throws IOException {
        if (newData[0] == 'B' && newData[1] == 'M') {
            BufferedImage b = ImageHelper.read(newData);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageHelper.write(b, ImageFormat.PNG, baos);
            newData = baos.toByteArray();
        }

        if (tagType == 0) {
            if (it instanceof DefineBitsTag) {
                // DefineBits tag shoud be imported as DefineBitsJPEG2 tag
                tagType = DefineBitsJPEG2Tag.ID;
            } else {
                tagType = it.getId();
            }
        }

        if (it.getId() == tagType) {
            it.setImage(newData);
        } else {
            SWF swf = it.getSwf();
            ImageTag imageTag;
            ByteArrayRange range = it.getOriginalRange();
            int characterId = it.getCharacterId();
            switch (tagType) {
                case DefineBitsJPEG2Tag.ID: {
                    imageTag = new DefineBitsJPEG2Tag(swf, range, characterId, newData);
                    break;
                }
                case DefineBitsJPEG3Tag.ID: {
                    imageTag = new DefineBitsJPEG3Tag(swf, range, characterId, newData);
                    break;
                }
                case DefineBitsJPEG4Tag.ID: {
                    imageTag = new DefineBitsJPEG4Tag(swf, range, characterId, newData);
                    break;
                }
                case DefineBitsLosslessTag.ID: {
                    DefineBitsLosslessTag losslessTag = new DefineBitsLosslessTag(swf, range, characterId);
                    losslessTag.setImage(newData);
                    imageTag = losslessTag;
                    break;
                }
                case DefineBitsLossless2Tag.ID: {
                    DefineBitsLossless2Tag lossless2Tag = new DefineBitsLossless2Tag(swf, range, characterId);
                    lossless2Tag.setImage(newData);
                    imageTag = lossless2Tag;
                    break;
                }
                default:
                    throw new Error("Unsupported image type tag.");
            }

            imageTag.setModified(true);
            swf.replaceTag(it, imageTag);
            swf.updateCharacters();
            return imageTag;
        }

        return null;
    }

    public Tag importImageAlpha(ImageTag it, byte[] newData) throws IOException {

        try {
            BufferedImage img = ImageHelper.read(newData);
            int width = img.getWidth();
            int height = img.getHeight();
            byte[] data = new byte[width * height];
            int[] imgData = img.getRGB(0, 0, width, height, null, 0, width);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int alpha = (imgData[y * width + x] >> 24) & 0xff;
                    data[y * width + x] = (byte) alpha;
                }
            }

            newData = data;
        } catch (IOException ex) {
        }

        if (it instanceof DefineBitsJPEG3Tag) {
            ((DefineBitsJPEG3Tag) it).setImageAlpha(newData);
        } else if (it instanceof DefineBitsJPEG4Tag) {
            ((DefineBitsJPEG4Tag) it).setImageAlpha(newData);
        }
        return null;
    }

    public void convertImage(ImageTag it, int tagType) throws IOException {
        importImage(it, Helper.readStream(it.getImageData()), tagType);
    }

    public static int getImageTagType(String format) {
        int res = 0;
        switch (format) {
            case "lossless":
                res = DefineBitsLosslessTag.ID;
                break;
            case "lossless2":
                res = DefineBitsLossless2Tag.ID;
                break;
            case "jpeg2":
                res = DefineBitsJPEG2Tag.ID;
                break;
            case "jpeg3":
                res = DefineBitsJPEG3Tag.ID;
                break;
            case "jpeg4":
                res = DefineBitsJPEG4Tag.ID;
                break;
        }

        return res;
    }
}
