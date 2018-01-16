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
package com.jpexs.decompiler.flash.types.gfx;

import com.jpexs.decompiler.flash.types.SHAPE;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class FontType implements Serializable {

    public static final int FF_Italic = 0x0001;

    public static final int FF_Bold = 0x0002;

    public String fontName;

    public int flags;

    public int nominalSize;

    public int ascent;

    public int descent;

    public int leading;

    public List<GlyphType> glyphs;

    public List<GlyphInfoType> glyphInfo;

    public List<KerningPairType> kerning;

    public FontType() {
        fontName = "New font";
        glyphInfo = new ArrayList<>();
        kerning = new ArrayList<>();
        glyphs = new ArrayList<>();
    }

    public FontType(GFxInputStream sis) throws IOException {
        long offset = sis.getPos();
        fontName = sis.readString("fontName");
        flags = sis.readUI16("flags");
        nominalSize = sis.readUI16("nominalSize");
        ascent = sis.readSI16("ascent");
        descent = sis.readSI16("descent");
        leading = sis.readSI16("leading");
        long numGlyphs = sis.readUI32("numGlyphs");
        long glyphBytesLen = sis.readUI32("glyphBytesLen");
        byte[] glyphBytes = new byte[(int) glyphBytesLen];
        sis.read(glyphBytes);
        glyphInfo = new ArrayList<>();
        for (int i = 0; i < numGlyphs; i++) {
            sis.newDumpLevel("glyphInfoType", "GlyphInfoType");
            glyphInfo.add(new GlyphInfoType(sis));
            sis.endDumpLevel();
        }

        long kerningTableSize = sis.readUI30("kerningTableSize");
        kerning = new ArrayList<>();
        for (int i = 0; i < kerningTableSize; i++) {
            sis.newDumpLevel("kerningPairType", "KerningPairType");
            kerning.add(new KerningPairType(sis));
            sis.endDumpLevel();
        }

        long pos = sis.getPos();

        glyphs = new ArrayList<>();
        for (int i = 0; i < glyphInfo.size(); i++) {
            sis.setPos(glyphInfo.get(i).globalOffset + offset);
            sis.newDumpLevel("glyphType", "GlyphType");
            glyphs.add(new GlyphType(sis, offset));
            sis.endDumpLevel();
        }

        sis.setPos(pos);
    }

    public List<SHAPE> getGlyphShapes() {
        List<SHAPE> ret = new ArrayList<>();
        for (GlyphType g : glyphs) {
            ret.add(g.toSHAPE());
        }
        return ret;
    }

    public void write(GFxOutputStream sos) throws IOException {
        sos = new GFxOutputStream(sos);
        sos.write(fontName.getBytes());
        sos.writeUI8(0);
        sos.writeUI16(flags);
        sos.writeUI16(nominalSize);
        sos.writeUI16(ascent);
        sos.writeUI16(descent);
        sos.writeUI16(leading);
        sos.writeUI32(glyphInfo.size()); //numGlyphs
        long headerLen = sos.getPos() + 4;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GFxOutputStream sos2 = new GFxOutputStream(baos);
        for (int i = 0; i < glyphs.size(); i++) {
            glyphInfo.get(i).globalOffset = headerLen + sos2.getPos();
            glyphs.get(i).write(sos2);
        }
        byte[] glyphBytes = baos.toByteArray();
        sos.writeUI32(glyphBytes.length);
        sos.write(glyphBytes);
        for (int i = 0; i < glyphInfo.size(); i++) {
            glyphInfo.get(i).write(sos);
        }
        sos.writeUI30(kerning.size());
        for (KerningPairType kp : kerning) {
            kp.write(sos);
        }
    }
}
