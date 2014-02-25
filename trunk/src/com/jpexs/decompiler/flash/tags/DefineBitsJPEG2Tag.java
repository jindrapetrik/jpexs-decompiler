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
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.tags.base.AloneTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.SerializableImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import javax.imageio.ImageIO;

public class DefineBitsJPEG2Tag extends ImageTag implements AloneTag {

    @SWFType(BasicType.UI16)
    public int characterID;

    @SWFType(BasicType.UI8)
    public byte[] imageData;

    public static final int ID = 21;

    @Override
    public int getCharacterId() {
        return characterID;
    }

    @Override
    public String getImageFormat() {
        return ImageTag.getImageFormat(imageData);
    }

    @Override
    public InputStream getImageData() {
        if (SWF.hasErrorHeader(imageData)) {
            return new ByteArrayInputStream(imageData, 4, imageData.length - 4);
        }
        return new ByteArrayInputStream(imageData);
    }

    @Override
    public SerializableImage getImage(List<Tag> tags) {
        try {
            return new SerializableImage(ImageIO.read(getImageData()));
        } catch (IOException ex) {
        }
        return null;
    }

    public DefineBitsJPEG2Tag(SWF swf, byte[] data, int version, long pos) throws IOException {
        super(swf, ID, "DefineBitsJPEG2", data, pos);
        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        characterID = sis.readUI16();
        imageData = sis.readBytesEx(sis.available());
    }

    public DefineBitsJPEG2Tag(SWF swf, byte[] data, int version, long pos, int characterID, byte[] imageData) throws IOException {
        super(swf, ID, "DefineBitsJPEG2", data, pos);
        this.characterID = characterID;
        this.imageData = imageData;
    }

    @Override
    public void setImage(byte[] data) {
        imageData = data;
    }

    @Override
    public byte[] getData() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream os = baos;
        SWFOutputStream sos = new SWFOutputStream(os, getVersion());
        try {
            sos.writeUI16(characterID);
            sos.write(imageData);
        } catch (IOException e) {
        }
        return baos.toByteArray();
    }
}
