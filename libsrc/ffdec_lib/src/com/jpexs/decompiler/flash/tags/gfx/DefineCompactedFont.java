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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.helpers.FontHelper;
import com.jpexs.decompiler.flash.tags.DefineFont2Tag;
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
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.MemoryInputStream;
import java.awt.Font;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public final class DefineCompactedFont extends FontTag {

    public static final int ID = 1005;

    public static final String NAME = "DefineCompactedFont";

    public int fontId;

    public List<FontType> fonts;

    private List<SHAPE> shapeCache;

    /**
     * Gets data bytes
     *
     * @param sos SWF output stream
     * @throws java.io.IOException
     */
    @Override
    public void getData(SWFOutputStream sos) throws IOException {
        sos.writeUI16(fontId);
        for (FontType ft : fonts) {
            ft.write(new GFxOutputStream(sos));
        }
    }

    /**
     * Constructor
     *
     * @param swf
     */
    public DefineCompactedFont(SWF swf) {
        super(swf, ID, NAME, null);
        fontId = swf.getNextCharacterId();

        fonts = new ArrayList<>();
        FontType ft = new FontType();
        fonts.add(ft);

        rebuildShapeCache();
    }

    /**
     * Constructor
     *
     * @param sis
     * @param data
     * @throws IOException
     */
    public DefineCompactedFont(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        fontId = sis.readUI16("fontId");
        fonts = new ArrayList<>();

        MemoryInputStream mis = sis.getBaseStream();
        while (mis.available() > 0) {
            GFxInputStream gis = new GFxInputStream(mis);
            gis.dumpInfo = sis.dumpInfo;
            gis.newDumpLevel("fontType", "FontType");
            fonts.add(new FontType(gis));
            gis.endDumpLevel();
        }
        sis.skipBytes(sis.available());
        if (fonts.size() > 1) {
            Logger.getLogger(DefineCompactedFont.class.getName()).log(Level.WARNING, "Compacted font has more than one FontType inside. This may cause problems while editing.");
        }
        rebuildShapeCache();
    }

    public void rebuildShapeCache() {
        shapeCache = fonts.get(0).getGlyphShapes();
    }

    @Override
    public String getFontNameIntag() {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < fonts.size(); i++) {
            if (i > 0) {
                ret.append(", ");
            }
            ret.append(fonts.get(i).fontName);
        }
        return ret.toString();
    }

    @Override
    public int getCharacterId() {
        return fontId;
    }

    @Override
    public void setCharacterId(int characterId) {
        this.fontId = characterId;
    }

    @Override
    public List<SHAPE> getGlyphShapeTable() {
        return shapeCache;
    }

    @Override
    public void addCharacter(char character, Font cfont) {
        int fontStyle = getFontStyle();
        FontType font = fonts.get(0);

        double d = 1;
        SHAPE shp = SHAPERECORD.fontCharacterToSHAPE(cfont, (int) (font.nominalSize * d), character);

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
            shiftGlyphIndices(fontId, pos, true);
        }

        Font fnt = cfont.deriveFont(fontStyle, Math.round(font.nominalSize * d));
        int advance = (int) Math.round(FontHelper.getFontAdvance(fnt, character));
        if (!exists) {
            font.glyphInfo.add(pos, new GlyphInfoType(code, advance, 0));
            font.glyphs.add(pos, new GlyphType(shp.shapeRecords));
            shapeCache.add(pos, font.glyphs.get(pos).toSHAPE());
        } else {
            font.glyphInfo.set(pos, new GlyphInfoType(code, advance, 0));
            font.glyphs.set(pos, new GlyphType(shp.shapeRecords));
            shapeCache.set(pos, font.glyphs.get(pos).toSHAPE());
        }

        setModified(true);
        getSwf().clearImageCache();
    }

    @Override
    public boolean removeCharacter(char character) {
        FontType font = fonts.get(0);

        int code = (int) character;
        int pos = -1;
        for (int i = 0; i < font.glyphInfo.size(); i++) {
            if (font.glyphInfo.get(i).glyphCode >= code) {
                if (font.glyphInfo.get(i).glyphCode == code) {
                    pos = i;
                    break;
                }

                return false;
            }
        }

        if (pos == -1) {
            return false;
        }

        font.glyphInfo.remove(pos);
        font.glyphs.remove(pos);
        shapeCache.remove(pos);
        shiftGlyphIndices(fontId, pos + 1, false);

        setModified(true);
        getSwf().clearImageCache();
        return true;
    }

    @Override
    public void setAdvanceValues(Font font) {
        throw new UnsupportedOperationException("Setting the advance values for DefineCompactedFont is not supported.");
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
        return resize(fonts.get(0).glyphInfo.get(glyphIndex).advanceX);
    }

    @Override
    public int getGlyphKerningAdjustment(int glyphIndex, int nextGlyphIndex) {
        char c1 = glyphToChar(glyphIndex);
        char c2 = glyphToChar(nextGlyphIndex);
        return getCharKerningAdjustment(c1, c2);
    }

    @Override
    public int getCharKerningAdjustment(char c1, char c2) {
        for (KerningPairType kp : fonts.get(0).kerning) {
            if (kp.char1 == c1 && kp.char2 == c2) {
                return resize(kp.advance);
            }
        }
        return 0;
    }

    @Override
    public int getGlyphWidth(int glyphIndex) {
        return resize(getGlyphShapeTable().get(glyphIndex).getBounds().getWidth());
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
    public double getDivider() {
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
    public int getCharacterCount() {
        FontType ft = fonts.get(0);
        return ft.glyphInfo.size();
    }

    @Override
    public String getCharacters() {
        FontType ft = fonts.get(0);
        StringBuilder ret = new StringBuilder(ft.glyphInfo.size());
        for (GlyphInfoType gi : ft.glyphInfo) {
            ret.append((char) gi.glyphCode);
        }
        return ret.toString();
    }

    @Override
    public RECT getGlyphBounds(int glyphIndex) {
        GlyphType gt = fonts.get(0).glyphs.get(glyphIndex);
        return new RECT(resize(gt.boundingBox[0]), resize(gt.boundingBox[1]), resize(gt.boundingBox[2]), resize(gt.boundingBox[3]));
    }

    public SHAPE resizeShape(SHAPE shp) {
        SHAPE ret = new SHAPE();
        ret.numFillBits = 1;
        ret.numLineBits = 0;
        List<SHAPERECORD> recs = new ArrayList<>();
        for (SHAPERECORD r : shp.shapeRecords) {
            SHAPERECORD c = r.clone();
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

    protected int resize(double val) {
        FontType ft = fonts.get(0);
        return (int) Math.round(val * 1024.0 / ft.nominalSize);
    }

    @Override
    public FontTag toClassicFont() {
        DefineFont2Tag ret = new DefineFont2Tag(swf);
        ret.fontID = getFontId();
        ret.fontFlagsBold = isBold();
        ret.fontFlagsItalic = isItalic();
        ret.fontFlagsWideOffsets = true;
        ret.fontFlagsWideCodes = true;
        ret.fontFlagsHasLayout = true;
        ret.fontAscent = resize(getAscent());
        ret.fontDescent = resize(getDescent());
        ret.fontLeading = resize(getLeading());
        ret.fontAdvanceTable = new ArrayList<>();
        ret.fontBoundsTable = new ArrayList<>();
        ret.codeTable = new ArrayList<>();
        ret.glyphShapeTable = new ArrayList<>();
        List<SHAPE> shp = getGlyphShapeTable();
        for (int g = 0; g < shp.size(); g++) {
            ret.fontAdvanceTable.add((int) getGlyphAdvance(g)); //already resized
            ret.codeTable.add((int) glyphToChar(g));

            SHAPE shpX = resizeShape(shp.get(g));
            ret.glyphShapeTable.add(shpX);
            ret.fontBoundsTable.add(getGlyphBounds(g));
        }
        ret.fontName = getFontNameIntag();
        ret.languageCode = new LANGCODE(1);
        ret.fontKerningTable = new ArrayList<>();

        FontType ft = fonts.get(0);
        for (int i = 0; i < ft.kerning.size(); i++) {
            KERNINGRECORD kr = new KERNINGRECORD();
            kr.fontKerningAdjustment = resize(ft.kerning.get(i).advance);
            kr.fontKerningCode1 = ft.kerning.get(i).char1;
            kr.fontKerningCode2 = ft.kerning.get(i).char2;
            ret.fontKerningTable.add(kr);
        }

        return ret;
    }

    @Override
    public boolean hasLayout() {
        return true;
    }
}
