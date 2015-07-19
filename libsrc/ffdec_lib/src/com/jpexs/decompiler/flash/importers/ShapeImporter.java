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
 * License along with this library.
 */
package com.jpexs.decompiler.flash.importers;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.helpers.ImageHelper;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG2Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsLossless2Tag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.tags.enums.ImageFormat;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 * @author JPEXS
 */
public class ShapeImporter {

    public Tag importImage(ShapeTag st, byte[] newData) throws IOException {
        SWF swf = st.getSwf();

        if (newData[0] == 'B' && newData[1] == 'M') {
            BufferedImage b = ImageHelper.read(newData);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageHelper.write(b, ImageFormat.PNG, baos);
            newData = baos.toByteArray();
        }

        ImageTag imageTag;
        if (ImageTag.getImageFormat(newData) == ImageFormat.JPEG) {
            DefineBitsJPEG2Tag jpeg2Tag = new DefineBitsJPEG2Tag(swf, null, swf.getNextCharacterId(), newData);
            imageTag = jpeg2Tag;
        } else {
            DefineBitsLossless2Tag lossless2Tag = new DefineBitsLossless2Tag(swf);
            lossless2Tag.setImage(newData);
            imageTag = lossless2Tag;
        }

        int idx = swf.tags.indexOf(st);
        if (idx != -1) {
            swf.tags.add(idx, imageTag);
        } else {
            swf.tags.add(imageTag);
        }

        swf.updateCharacters();
        st.setModified(true);
        SHAPEWITHSTYLE shapes = imageTag.getShape(st.getRect(), true);

        st.shapes = shapes;
        return (Tag) st;
    }
}
