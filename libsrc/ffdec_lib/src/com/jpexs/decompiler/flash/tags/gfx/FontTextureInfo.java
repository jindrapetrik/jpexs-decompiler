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
package com.jpexs.decompiler.flash.tags.gfx;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.TagInfo;
import com.jpexs.decompiler.flash.tags.gfx.enums.FileFormatType;
import com.jpexs.decompiler.flash.tags.gfx.enums.IdType;
import com.jpexs.decompiler.flash.types.gfx.FONTINFO;
import com.jpexs.decompiler.flash.types.gfx.GFxInputStream;
import com.jpexs.decompiler.flash.types.gfx.GFxOutputStream;
import com.jpexs.decompiler.flash.types.gfx.TEXGLYPH;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.MemoryInputStream;
import java.io.IOException;
import java.util.Map;

/**
 * FontTextureInfo tag - font texture info.
 *
 * @author JPEXS
 */
public class FontTextureInfo extends Tag {

    public static final int ID = 1002;

    public static final String NAME = "FontTextureInfo";

    public int textureID;

    public int idType;

    public int textureFormat;

    public String fileName;

    public int textureWidth;

    public int textureHeight;

    public int padPixels;

    public int nominalGlyphSz;

    public TEXGLYPH[] texGlyphs;

    public FONTINFO[] fonts;

    /**
     * Gets data bytes
     *
     * @param sos SWF output stream
     * @throws IOException On I/O error
     */
    @Override
    public void getData(SWFOutputStream sos) throws IOException {
        sos.writeUI16(textureID);
        sos.writeUI16(idType);
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
     * @param sis SWF input stream
     * @param data Data
     * @throws IOException On I/O error
     */
    public FontTextureInfo(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    public FontTextureInfo(SWF swf) {
        super(swf, ID, NAME, null);
        fileName = "";
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        textureID = sis.readUI16("textureID");
        idType = sis.readUI16("idType");
        textureFormat = sis.readUI16("textureFormat");
        fileName = sis.readNetString("fileName");
        textureWidth = sis.readUI16("textureWidth");
        textureHeight = sis.readUI16("textureHeight");
        padPixels = sis.readUI8("padPixels");
        nominalGlyphSz = sis.readUI16("nominalGlyphSz");
        int numTexGlyphs = sis.readUI16("numTexGlyphs");
        texGlyphs = new TEXGLYPH[numTexGlyphs];
        MemoryInputStream mis = sis.getBaseStream();
        long misStartPos;
        misStartPos = mis.getPos();
        for (int i = 0; i < numTexGlyphs; i++) {
            GFxInputStream gis = new GFxInputStream(mis);
            gis.dumpInfo = sis.dumpInfo;
            texGlyphs[i] = new TEXGLYPH(gis);
        }
        sis.skipBytes(mis.getPos() - misStartPos);
        int numFonts = sis.readUI16("numFonts");
        fonts = new FONTINFO[numFonts];
        mis = sis.getBaseStream();
        misStartPos = mis.getPos();
        for (int i = 0; i < numFonts; i++) {
            GFxInputStream gis = new GFxInputStream(mis);
            gis.dumpInfo = sis.dumpInfo;
            fonts[i] = new FONTINFO(gis);
        }
        sis.skipBytes(mis.getPos() - misStartPos);
    }

    @Override
    public Map<String, String> getNameProperties() {
        Map<String, String> ret = super.getNameProperties();
        ret.put("chid", "" + getUniqueId());
        return ret;
    }

    @Override
    public String getUniqueId() {
        if (idType == IdType.IDTYPE_NONE) {
            return super.getUniqueId();
        }
        return "ft" + textureID;
    }

    @Override
    public void getTagInfo(TagInfo tagInfo) {
        super.getTagInfo(tagInfo);

        tagInfo.addInfo("general", "textureId", textureID);
        tagInfo.addInfo("general", "idType", IdType.idTypeToString(idType) + " (" + idType + ")");
        String textureFormatStr = "0x" + Integer.toHexString(textureFormat);
        String fileFormatStr = FileFormatType.fileFormatToString(textureFormat);
        if (fileFormatStr != null) {
            textureFormatStr = fileFormatStr + " (" + textureFormat + ")";
        }
        tagInfo.addInfo("general", "textureFormat", textureFormatStr);
        tagInfo.addInfo("general", "fileName", fileName);
        tagInfo.addInfo("general", "width", textureWidth);
        tagInfo.addInfo("general", "height", textureHeight);
        tagInfo.addInfo("general", "padPixels", padPixels);
        tagInfo.addInfo("general", "nominalGlyphSz", nominalGlyphSz);
        tagInfo.addInfo("general", "glyphCount", texGlyphs.length);
        tagInfo.addInfo("general", "fontCount", fonts.length);
    }
}
