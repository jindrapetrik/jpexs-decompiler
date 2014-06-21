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
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.SerializableImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.imageio.ImageIO;

public class DefineBitsTag extends ImageTag {

    @SWFType(BasicType.UI16)
    public int characterID;

    @SWFType(BasicType.UI8)
    public byte[] jpegData;

    @Internal
    private JPEGTablesTag jtt = null;
    public static final int ID = 6;

    @Override
    public void setImage(byte[] data) {
        throw new UnsupportedOperationException("Set image is not supported for DefineBits");
    }

    @Override
    public boolean importSupported() {
        // importing a new image will replace the current DefineBitsTag with a new DefineBitsJPEG2Tag
        return true;
    }

    public DefineBitsTag(SWFInputStream sis, long pos, int length) throws IOException {
        super(sis.getSwf(), ID, "DefineBits", pos, length);
        characterID = sis.readUI16();
        jpegData = sis.readBytesEx(sis.available());
    }

    private void getJPEGTables() {
        if (jtt == null) {
            for (Tag t : swf.tags) {
                if (t instanceof JPEGTablesTag) {
                    jtt = (JPEGTablesTag) t;
                    break;
                }
            }
        }
    }

    @Override
    public InputStream getImageData() {
        return null;
    }

    @Override
    public SerializableImage getImage() {
        getJPEGTables();
        if ((jtt != null)) {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                byte[] jttdata = jtt.jpegData;
                if (jttdata.length != 0) {
                    baos.write(jttdata, SWF.hasErrorHeader(jttdata) ? 4 : 0, jttdata.length - (SWF.hasErrorHeader(jttdata) ? 6 : 2));
                    baos.write(jpegData, SWF.hasErrorHeader(jpegData) ? 6 : 2, jpegData.length - (SWF.hasErrorHeader(jttdata) ? 6 : 2));
                } else {
                    baos.write(jpegData, 0, jpegData.length);
                }
                return new SerializableImage(ImageIO.read(new ByteArrayInputStream(baos.toByteArray())));
            } catch (IOException ex) {
                return null;
            }
        }
        return null;
    }

    /**
     * Gets data bytes
     *
     * @return Bytes of data
     */
    @Override
    public byte[] getData() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream os = baos;
        SWFOutputStream sos = new SWFOutputStream(os, getVersion());
        try {
            sos.writeUI16(characterID);
            sos.write(jpegData);
        } catch (IOException e) {
        }
        return baos.toByteArray();
    }

    @Override
    public int getCharacterId() {
        return characterID;
    }

    @Override
    public String getImageFormat() {
        return "jpg";
    }
}
