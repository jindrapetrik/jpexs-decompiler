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
import java.io.IOException;

/**
 *
 * @author JPEXS
 */
public class FontTextureInfo extends Tag {

    public static final int ID = 1002;

    public static final String NAME = "FontTextureInfo";

    public long textureID;

    public int textureFormat;

    public String fileName;

    public int textureWidth;

    public int textureHeight;

    public int padPixels;

    public int nominalGlyphSz;

    public TEXGLYPH[] texGlyphs;

    public FONTINFO[] fonts;

    public static final int TEXTURE_FORMAT_DEFAULT = 0;

    public static final int TEXTURE_FORMAT_TGA = 1;

    public static final int TEXTURE_FORMAT_DDS = 2;

    /**
     * Gets data bytes
     *
     * @param sos SWF output stream
     * @throws java.io.IOException
     */
    @Override
    public void getData(SWFOutputStream sos) throws IOException {
        sos.writeUI32(textureID);
        sos.writeUI16(textureFormat);
        sos.writeNetString(fileName);
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
    }

    /**
     * Constructor
     *
     * @param sis
     * @param data
     * @throws IOException
     */
    public FontTextureInfo(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        textureID = sis.readUI32("textureID");
        textureFormat = sis.readUI16("textureFormat");
        fileName = sis.readNetString("fileName");
        textureWidth = sis.readUI16("textureWidth");
        textureHeight = sis.readUI16("textureHeight");
        padPixels = sis.readUI8("padPixels");
        nominalGlyphSz = sis.readUI16("nominalGlyphSz");
        int numTexGlyphs = sis.readUI16("numTexGlyphs");
        texGlyphs = new TEXGLYPH[numTexGlyphs];
        MemoryInputStream mis = sis.getBaseStream();
        for (int i = 0; i < numTexGlyphs; i++) {
            GFxInputStream gis = new GFxInputStream(mis);
            gis.dumpInfo = sis.dumpInfo;
            texGlyphs[i] = new TEXGLYPH(gis);
        }
        sis.skipBytes(mis.getPos());
        int numFonts = sis.readUI16("numFonts");
        fonts = new FONTINFO[numFonts];
        mis = sis.getBaseStream();
        for (int i = 0; i < numFonts; i++) {
            GFxInputStream gis = new GFxInputStream(mis);
            gis.dumpInfo = sis.dumpInfo;
            fonts[i] = new FONTINFO(gis);
        }
        sis.skipBytes(mis.getPos());
    }
}
