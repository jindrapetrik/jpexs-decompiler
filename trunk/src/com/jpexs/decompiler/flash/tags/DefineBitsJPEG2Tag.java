/*
 *  Copyright (C) 2010-2013 JPEXS
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

import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.tags.base.AloneTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;

public class DefineBitsJPEG2Tag extends ImageTag implements AloneTag {

    public int characterID;
    public byte[] imageData;

    @Override
    public int getCharacterID() {
        return characterID;
    }

    @Override
    public String getImageFormat() {
        return ImageTag.getImageFormat(imageData);
    }

    @Override
    public BufferedImage getImage(List<Tag> tags) {
        try {
            return ImageIO.read(new ByteArrayInputStream(imageData));
        } catch (IOException ex) {
        }
        return null;
    }

    public DefineBitsJPEG2Tag(byte[] data, int version, long pos) throws IOException {
        super(21, "DefineBitsJPEG2", data, pos);
        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        characterID = sis.readUI16();
        imageData = sis.readBytes(sis.available());
    }

    @Override
    public void setImage(byte data[]) {
        imageData = data;
    }
}
