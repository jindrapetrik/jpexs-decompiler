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
package com.jpexs.decompiler.flash.importers;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.helpers.ImageHelper;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG2Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 * @author JPEXS
 */
public class ImageImporter extends TagImporter {

    public Tag importImage(ImageTag it, byte[] newData) throws IOException {

        if (newData[0] == 'B' && newData[1] == 'M') {
            BufferedImage b = ImageHelper.read(new ByteArrayInputStream(newData));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageHelper.write(b, "PNG", baos);
            newData = baos.toByteArray();
        }

        if (it instanceof DefineBitsTag) {
            SWF swf = it.getSwf();
            DefineBitsJPEG2Tag jpeg2Tag = new DefineBitsJPEG2Tag(swf, it.getOriginalRange(), it.getCharacterId(), newData);
            jpeg2Tag.setModified(true);
            swf.tags.set(swf.tags.indexOf(it), jpeg2Tag);
            swf.updateCharacters();
            return jpeg2Tag;
        } else {
            it.setImage(newData);
        }
        return null;
    }
}
