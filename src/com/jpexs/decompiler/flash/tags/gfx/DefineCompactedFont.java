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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.tags.DefineFont2Tag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.DrawableTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.types.KERNINGRECORD;
import com.jpexs.decompiler.flash.types.LANGCODE;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.gfx.FontType;
import com.jpexs.decompiler.flash.types.gfx.GFxInputStream;
import com.jpexs.decompiler.flash.types.gfx.GFxOutputStream;
import com.jpexs.decompiler.flash.types.gfx.GlyphInfoType;
import com.jpexs.decompiler.flash.types.gfx.GlyphType;
import com.jpexs.decompiler.flash.types.gfx.KerningPairType;
import com.jpexs.decompiler.flash.types.shaperecords.CurvedEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import com.jpexs.helpers.Helper;
import java.awt.Font;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;

/**
 *
 *
 * @author JPEXS
 */
public final class DefineCompactedFont extends FontTag implements DrawableTag {

    public static final int ID = 1005;
    public int fontId;
    public List<FontType> fonts;
    private List<SHAPE> shapeCache;

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
            sos.writeUI16(fontId);
            for (FontType ft : fonts) {
                ft.write(new GFxOutputStream(sos));
            }
        } catch (IOException e) {
        }
        return baos.toByteArray();
    }

    /**
     * Constructor
     *
     * @param sis
     * @param length
     * @param pos
     * @throws IOException
     */
    public DefineCompactedFont(SWFInputStream sis, long pos, int length) throws IOException {
        super(sis.getSwf(), ID, "DefineCompactedFont", pos, length);

        fontId = sis.readUI16("fontId");
        fonts = new ArrayList<>();

        while (sis.available() > 0) {
            fonts.add(new FontType(new GFxInputStream(sis.getBaseStream())));
        }
        rebuildShapeCache();
    }

    public void rebuildShapeCache() {
        shapeCache = fonts.get(0).getGlyphShapes();
    }

    @Override
    public int getNumFrames() {
        return 1;
    }

    @Override
    public String getFontName() {
        String ret = "";
        for (int i = 0; i < fonts.size(); i++) {
            if (i > 0) {
                ret += ", ";
            }
            ret += fonts.get(i).fontName;
        }
        return ret;
    }

    @Override
    public int getCharacterId() {
        return fontId;
    }

    @Override
    public int getFontId() {
        return fontId;
    }

    @Override
    public List<SHAPE> getGlyphShapeTable() {
        return shapeCache;
    }

    @Override
    public void addCharacter(char character, String fontName) {
        int fontStyle = getFontStyle();
        FontType font = fonts.get(0);
        SHAPE shp = SHAPERECORD.systemFontCharacterToSHAPE(fontName, fontStyle, (int) (SWF.unitDivisor * font.nominalSize), character);

        int code = (int) character;
        int pos = -1;
        boolean exists = false;
        for (int i = 0; i < font.glyphInfo.size(); i++) {
            if (font.glyphInfo.get(i).glyphCode >= code) {
                if (font.glyphInfo.get(i).glyphCode == code) {
                    exists = true;
                }
                pos = i;
                break;
            }
        }
        if (pos == -1) {
            pos = font.glyphInfo.size();
        }

        if (!exists) {
            shiftGlyphIndices(fontId, pos);
        }

        Font fnt = new Font(fontName, fontStyle, font.nominalSize);
        int advance = (int) Math.round(fnt.createGlyphVector((new JPanel()).getFontMetrics(fnt).getFontRenderContext(), "" + character).getGlyphMetrics(0).getAdvanceX());
        if (!exists) {
            font.glyphInfo.add(pos, new GlyphInfoType(code, advance, 0));
            font.glyphs.add(pos, new GlyphType(shp.shapeRecords));
            shapeCache.add(pos, font.glyphs.get(pos).toSHAPE());
        } else {
            font.glyphInfo.set(pos, new GlyphInfoType(code, advance, 0));
            font.glyphs.set(pos, new GlyphType(shp.shapeRecords));
            shapeCache.set(pos, font.glyphs.get(pos).toSHAPE());
        }
        SWF.clearImageCache();
    }

    @Override
    public char glyphToChar(int glyphIndex) {
        return (char) fonts.get(0).glyphInfo.get(glyphIndex).glyphCode;
    }

    @Override
    public int charToGlyph(char c) {
        FontType ft = fonts.get(0);
        for (int i = 0; i < ft.glyphInfo.size(); i++) {
            if (ft.glyphInfo.get(i).glyphCode == c) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public double getGlyphAdvance(int glyphIndex) {
        return fonts.get(0).glyphInfo.get(glyphIndex).advanceX;
    }

    @Override
    public int getGlyphKerningAdjustment(int glyphIndex, int nextGlyphIndex) {
        int char1 = glyphToChar(glyphIndex);
        int char2 = glyphToChar(nextGlyphIndex);
        for (KerningPairType kp : fonts.get(0).kerning) {
            if (kp.char1 == char1 && kp.char2 == char2) {
                return kp.advance;
            }
        }
        return 0;
    }

    @Override
    public int getGlyphWidth(int glyphIndex) {
        return getGlyphShapeTable().get(glyphIndex).getBounds().getWidth();
    }

    @Override
    public boolean isSmall() {
        return false;
    }

    @Override
    public boolean isBold() {
        return (fonts.get(0).flags & FontType.FF_Bold) == FontType.FF_Bold;
    }

    @Override
    public boolean isItalic() {
        return (fonts.get(0).flags & FontType.FF_Italic) == FontType.FF_Italic;
    }

    @Override
    public boolean isSmallEditable() {
        return false;
    }

    @Override
    public boolean isBoldEditable() {
        return true;
    }

    @Override
    public boolean isItalicEditable() {
        return true;
    }

    @Override
    public void setSmall(boolean value) {
    }

    @Override
    public void setBold(boolean value) {
        for (FontType font : fonts) {
            font.flags &= FontType.FF_Bold;
            if (!value) {
                font.flags ^= FontType.FF_Bold;
            }
        }
    }

    @Override
    public void setItalic(boolean value) {
        for (FontType font : fonts) {
            font.flags &= FontType.FF_Italic;
            if (!value) {
                font.flags ^= FontType.FF_Italic;
            }
        }
    }

    @Override
    public int getDivider() {
        return 1;
    }

    @Override
    public int getAscent() {
        return fonts.get(0).ascent;
    }

    @Override
    public int getDescent() {
        return fonts.get(0).descent;
    }

    @Override
    public int getLeading() {
        return fonts.get(0).leading;
    }

    @Override
    public String getCharacters(List<Tag> tags) {
        FontType ft = fonts.get(0);
        String ret = "";
        for (GlyphInfoType gi : ft.glyphInfo) {
            ret += (char) gi.glyphCode;
        }
        return ret;
    }

    private int resizemultiplier() {
        FontType ft = fonts.get(0);
        return (int) Math.round(1024.0 / ft.nominalSize);
    }

    private int resize(int val) {
        int ret = val * resizemultiplier();
        return ret;
    }

    private SHAPE resize(SHAPE shp) {
        SHAPE ret = new SHAPE();
        ret.numFillBits = 1;
        ret.numLineBits = 0;
        List<SHAPERECORD> recs = new ArrayList<>();
        for (SHAPERECORD r : shp.shapeRecords) {
            SHAPERECORD c = Helper.deepCopy(r);
            if (c instanceof StyleChangeRecord) {
                StyleChangeRecord scr = (StyleChangeRecord) c;
                scr.moveDeltaX = resize(scr.moveDeltaX);
                scr.moveDeltaY = resize(scr.moveDeltaY);
                scr.calculateBits();
            }
            if (c instanceof CurvedEdgeRecord) {
                CurvedEdgeRecord cer = (CurvedEdgeRecord) c;
                cer.controlDeltaX = resize(cer.controlDeltaX);
                cer.controlDeltaY = resize(cer.controlDeltaY);
                cer.anchorDeltaX = resize(cer.anchorDeltaX);
                cer.anchorDeltaY = resize(cer.anchorDeltaY);
                cer.calculateBits();
            }
            if (c instanceof StraightEdgeRecord) {
                StraightEdgeRecord ser = (StraightEdgeRecord) c;
                ser.deltaX = resize(ser.deltaX);
                ser.deltaY = resize(ser.deltaY);
                ser.calculateBits();
            }
            recs.add(c);
        }
        ret.shapeRecords = recs;
        return ret;
    }

    public FontTag toClassicFont() {
        try {
            DefineFont2Tag ret = new DefineFont2Tag(swf);
            ret.fontId = fontId;
            ret.fontFlagsBold = isBold();
            ret.fontFlagsItalic = isItalic();
            ret.fontFlagsWideOffsets = true;
            ret.fontFlagsWideCodes = true;
            ret.fontFlagsHasLayout = true;
            FontType ft = fonts.get(0);
            ret.fontAscent = resize(ft.ascent);
            ret.fontDescent = resize(ft.descent);
            ret.fontLeading = resize(ft.leading);
            ret.fontAdvanceTable = new ArrayList<>();
            ret.fontBoundsTable = new ArrayList<>();
            ret.codeTable = new ArrayList<>();
            ret.glyphShapeTable = new ArrayList<>();
            ret.numGlyphs = ft.glyphInfo.size();
            SHAPE shpA = SHAPERECORD.systemFontCharacterToSHAPE("Times New Roman", 0, getDivider() * 1024, 'A');
            for (GlyphInfoType gi : ft.glyphInfo) {
                ret.fontAdvanceTable.add(resize(gi.advanceX));
                ret.codeTable.add(gi.glyphCode);
            }

            for (GlyphType gt : ft.glyphs) {
                SHAPE shpX = resize(gt.toSHAPE());
                ret.glyphShapeTable.add(shpX); //
                ret.fontBoundsTable.add(new RECT(resize(gt.boundingBox[0]), resize(gt.boundingBox[1]), resize(gt.boundingBox[2]), resize(gt.boundingBox[3])));
            }
            ret.fontName = ft.fontName;
            ret.languageCode = new LANGCODE(1);
            ret.fontKerningTable = new KERNINGRECORD[0];/*new KERNINGRECORD[ft.kerning.size()];
             for(int i=0;i<ft.kerning.size();i++){
             KERNINGRECORD kr=new KERNINGRECORD();
             kr.fontKerningAdjustment = resize(ft.kerning.get(i).advance);
             kr.fontKerningCode1 = ft.kerning.get(i).char1;
             kr.fontKerningCode2 = ft.kerning.get(i).char2;
             ret.fontKerningTable[i] = kr;
             }*/

            return ret;
        } catch (IOException ex) {
            Logger.getLogger(DefineCompactedFont.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
