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
package com.jpexs.decompiler.flash.tags.gfx;

import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.types.gfx.FONTINFO;
import com.jpexs.decompiler.flash.types.gfx.GFxInputStream;
import com.jpexs.decompiler.flash.types.gfx.GFxOutputStream;
import com.jpexs.decompiler.flash.types.gfx.TEXGLYPH;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.MemoryInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 *
 * @author JPEXS
 */
public class FontTextureInfo extends Tag {

    public static final int ID = 1002;
    public long textureID;
    public int textureFormat;
    public String fileName;
    public int textureWidth;
    public int textureHeight;
    public int padPixels;
    public int nominalGlyphSz;
    public TEXGLYPH texGlyphs[];
    public FONTINFO fonts[];
    public static final int TEXTURE_FORMAT_DEFAULT = 0;
    public static final int TEXTURE_FORMAT_TGA = 1;
    public static final int TEXTURE_FORMAT_DDS = 2;

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
            sos.writeUI32(textureID);
            sos.writeUI16(textureFormat);
            byte fileNameBytes[] = fileName.getBytes();
            sos.writeUI8(fileNameBytes.length);
            sos.write(fileNameBytes);
            sos.writeUI16(textureWidth);
            sos.writeUI16(textureHeight);
            sos.writeUI8(padPixels);
            sos.writeUI16(nominalGlyphSz);
            sos.writeUI16(texGlyphs.length);
            for (int i = 0; i < texGlyphs.length; i++) {
                texGlyphs[i].write(new GFxOutputStream(sos));
            }
            sos.writeUI16(fonts.length);
            for (int i = 0; i < fonts.length; i++) {
                fonts[i].write(new GFxOutputStream(sos));
            }
        } catch (IOException e) {
        }
        return baos.toByteArray();
    }

    /**
     * Constructor
     *
     * @param sis
     * @param data
     * @throws IOException
     */
    public FontTextureInfo(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, "FontTextureInfo", data);
        textureID = sis.readUI32("textureID");
        textureFormat = sis.readUI16("textureFormat");
        int fileNameLen = sis.readUI8("fileNameLen");
        fileName = new String(sis.readBytesEx(fileNameLen, "fileName"));
        textureWidth = sis.readUI16("textureWidth");
        textureHeight = sis.readUI16("textureHeight");
        padPixels = sis.readUI8("padPixels");
        nominalGlyphSz = sis.readUI16("nominalGlyphSz");
        int numTexGlyphs = sis.readUI16("numTexGlyphs");
        texGlyphs = new TEXGLYPH[numTexGlyphs];
        MemoryInputStream mis = sis.getBaseStream();
        for (int i = 0; i < numTexGlyphs; i++) {
            texGlyphs[i] = new TEXGLYPH(new GFxInputStream(mis));
        }
        // todo: honfika: add GFx data to dump view
        sis.skipBytes(mis.getPos());
        int numFonts = sis.readUI16("numFonts");
        fonts = new FONTINFO[numFonts];
        mis = sis.getBaseStream();
        for (int i = 0; i < numFonts; i++) {
            fonts[i] = new FONTINFO(new GFxInputStream(sis.getBaseStream()));
        }
        // todo: honfika: add GFx data to dump view
        sis.skipBytes(mis.getPos());
    }
}
