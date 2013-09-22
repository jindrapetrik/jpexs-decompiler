/*
 * Copyright (C) 2013 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.types.gfx;

import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.types.SHAPE;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class FontType {

    public static final int FF_Italic = 0x0001;
    public static final int FF_Bold = 0x0002;
    public String fontName;
    public int flags;
    public int nominalSize;
    public int ascent;
    public int descent;
    public int leading;
    public GlyphType[] glyphs;
    public GlyphInfoType[] glyphInfo;
    public KerningPairType[] kerning;

    public FontType(GFxInputStream sis) throws IOException {
        sis = new GFxInputStream(sis);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //fontId = sis.readUI16();
        int val;
        while ((val = sis.readUI8()) > 0) {
            baos.write(val);
        }
        fontName = new String(baos.toByteArray());
        flags = sis.readUI16();
        nominalSize = sis.readUI16();
        ascent = sis.readSI16();
        descent = sis.readSI16();
        leading = sis.readSI16();
        long numGlyphs = sis.readUI32();
        long glyphBytesLen = sis.readUI32();
        byte glyphBytes[] = new byte[(int) glyphBytesLen];
        sis.read(glyphBytes);
        glyphInfo = new GlyphInfoType[(int) numGlyphs];
        for (int i = 0; i < numGlyphs; i++) {
            glyphInfo[i] = new GlyphInfoType(sis);
        }

        long kerningTableSize = sis.readUI30();
        kerning = new KerningPairType[((int) kerningTableSize)];
        for (int i = 0; i < kerningTableSize; i++) {
            kerning[i] = new KerningPairType(sis);
        }

        glyphs = new GlyphType[glyphInfo.length];
        for (int i = 0; i < glyphInfo.length; i++) {
            sis.setPos(glyphInfo[i].globalOffset);
            glyphs[i] = new GlyphType(sis);
        }
    }

    public List<SHAPE> getGlyphShapes() {
        List<SHAPE> ret = new ArrayList<>();
        for (GlyphType g : glyphs) {
            ret.add(g.toSHAPE());
        }
        return ret;
    }

    public void write(SWFOutputStream sos) throws IOException {
    }
}
