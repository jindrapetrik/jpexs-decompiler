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
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.helpers.FontHelper;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.KERNINGRECORD;
import com.jpexs.decompiler.flash.types.LANGCODE;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.annotations.Conditional;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.awt.Font;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * DefineFont3 tag - defines a font. Extends DefineFont2 functionality.
 *
 * @author JPEXS
 */
@SWFVersion(from = 8)
public class DefineFont3Tag extends FontTag {

    public static final int ID = 75;

    public static final String NAME = "DefineFont3";

    @SWFType(BasicType.UI16)
    public int fontID;

    public boolean fontFlagsHasLayout;

    public boolean fontFlagsShiftJIS;

    public boolean fontFlagsSmallText;

    public boolean fontFlagsANSI;

    public boolean fontFlagsWideOffsets;

    public boolean fontFlagsWideCodes;

    public boolean fontFlagsItalic;

    public boolean fontFlagsBold;

    public LANGCODE languageCode;

    public String fontName;

    @Conditional("!strippedShapes")
    public List<SHAPE> glyphShapeTable;

    @SWFType(value = BasicType.UI8, alternateValue = BasicType.UI16, alternateCondition = "fontFlagsWideCodes")
    public List<Integer> codeTable;

    @SWFType(BasicType.UI16)
    @Conditional("fontFlagsHasLayout")
    public int fontAscent;

    @SWFType(BasicType.UI16)
    @Conditional("fontFlagsHasLayout")
    public int fontDescent;

    @SWFType(BasicType.SI16)
    @Conditional("fontFlagsHasLayout")
    public int fontLeading;

    //Docs Says SI16, but Flash handles this as unsigned. There's probably not such thing as negative advance
    @SWFType(BasicType.UI16)
    @Conditional("fontFlagsHasLayout")
    public List<Integer> fontAdvanceTable;

    @Conditional("fontFlagsHasLayout")
    public List<RECT> fontBoundsTable;

    @Conditional("fontFlagsHasLayout")
    public List<KERNINGRECORD> fontKerningTable;

    @Internal
    public long unknownGfx;

    @Internal
    public boolean strippedShapes = false;

    /**
     * Constructor
     *
     * @param swf SWF
     */
    public DefineFont3Tag(SWF swf) {
        super(swf, ID, NAME, null);
        fontID = swf.getNextCharacterId();
        languageCode = new LANGCODE();
        fontName = "New font";
        glyphShapeTable = new ArrayList<>();
        codeTable = new ArrayList<>();
    }

    public DefineFont3Tag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        fontID = sis.readUI16("fontId");
        fontFlagsHasLayout = sis.readUB(1, "fontFlagsHasLayout") == 1;
        fontFlagsShiftJIS = sis.readUB(1, "fontFlagsShiftJIS") == 1;
        fontFlagsSmallText = sis.readUB(1, "fontFlagsSmallText") == 1;
        fontFlagsANSI = sis.readUB(1, "fontFlagsANSI") == 1;
        fontFlagsWideOffsets = sis.readUB(1, "fontFlagsWideOffsets") == 1;
        fontFlagsWideCodes = sis.readUB(1, "fontFlagsWideCodes") == 1;
        fontFlagsItalic = sis.readUB(1, "fontFlagsItalic") == 1;
        fontFlagsBold = sis.readUB(1, "fontFlagsBold") == 1;
        languageCode = sis.readLANGCODE("languageCode");
        fontName = sis.readNetString("fontName");
        int numGlyphs = sis.readUI16("numGlyphs");
        strippedShapes = swf.hasStrippedShapesFromFonts();

        if (!strippedShapes) {
            long[] offsetTable = new long[numGlyphs];
            long pos = sis.getPos();
            for (int i = 0; i < numGlyphs; i++) { //offsetTable
                if (fontFlagsWideOffsets) {
                    offsetTable[i] = sis.readUI32("offset");
                } else {
                    offsetTable[i] = sis.readUI16("offset");
                }
            }
            if (numGlyphs > 0 || fontFlagsHasLayout) {
                if (fontFlagsWideOffsets) {
                    sis.readUI32("codeTableOffset");
                } else {
                    sis.readUI16("codeTableOffset");
                }
            }
            glyphShapeTable = new ArrayList<>();
            for (int i = 0; i < numGlyphs; i++) {
                sis.seek(pos + offsetTable[i]);
                glyphShapeTable.add(sis.readSHAPE(1, false, "shape"));
            }
        } else {
            if (numGlyphs > 0 || fontFlagsHasLayout) {
                unknownGfx = sis.readUI32("unknownGfx");
            }
            glyphShapeTable = new ArrayList<>();
            for (int i = 0; i < numGlyphs; i++) {
                glyphShapeTable.add(new SHAPE());
            }
        }
        codeTable = new ArrayList<>();
        for (int i = 0; i < numGlyphs; i++) {
            if (fontFlagsWideCodes) {
                codeTable.add(sis.readUI16("code"));
            } else {
                codeTable.add(sis.readUI8("code"));
            }
        }
        if (fontFlagsHasLayout) {
            fontAscent = sis.readUI16("fontAscent");
            fontDescent = sis.readUI16("fontDescent");
            fontLeading = sis.readSI16("fontLeading");
            fontAdvanceTable = new ArrayList<>();
            for (int i = 0; i < numGlyphs; i++) {
                fontAdvanceTable.add(sis.readUI16("fontAdvance"));
            }
            fontBoundsTable = new ArrayList<>();
            for (int i = 0; i < numGlyphs; i++) {
                fontBoundsTable.add(sis.readRECT("rect"));
            }
            int kerningCount = sis.readUI16("kerningCount");
            fontKerningTable = new ArrayList<>();
            for (int i = 0; i < kerningCount; i++) {
                fontKerningTable.add(sis.readKERNINGRECORD(fontFlagsWideCodes, "record"));
            }
        }
    }

    private void checkWideParameters() {
        int numGlyphs = glyphShapeTable.size();

        if (!fontFlagsWideOffsets) {
            ByteArrayOutputStream baosGlyphShapes = new ByteArrayOutputStream();
            SWFOutputStream sos3 = new SWFOutputStream(baosGlyphShapes, getVersion(), getCharset());
            for (int i = 0; i < numGlyphs; i++) {
                long offset = (glyphShapeTable.size()) * 2 + sos3.getPos();
                if (offset > 0xffff) {
                    fontFlagsWideOffsets = true;
                    checkWideParameters();
                    return;
                }
                try {
                    sos3.writeSHAPE(glyphShapeTable.get(i), 1);
                } catch (IOException ex) {
                    //should not happen
                    return;
                }
            }
            byte[] baGlyphShapes = baosGlyphShapes.toByteArray();

            if (numGlyphs > 0) {
                long maxOffset = (glyphShapeTable.size() + 1/*CodeTableOffset*/) * 2 + baGlyphShapes.length;
                if (maxOffset > 0xffff) {
                    fontFlagsWideOffsets = true;
                    checkWideParameters();
                    return;
                }
            }
        }

        if (!fontFlagsWideCodes) {
            for (int i = 0; i < numGlyphs; i++) {
                long code = codeTable.get(i);
                if (code > 0xff) {
                    fontFlagsWideCodes = true;
                }
            }
        }
    }

    /**
     * Gets data bytes
     *
     * @param sos SWF output stream
     * @throws IOException On I/O error
     */
    @Override
    public synchronized void getData(SWFOutputStream sos) throws IOException {
        checkWideParameters();
        List<Long> offsetTable = new ArrayList<>();
        ByteArrayOutputStream baosGlyphShapes = new ByteArrayOutputStream();
        SWFOutputStream sos3 = new SWFOutputStream(baosGlyphShapes, getVersion(), getCharset());
        int numGlyphs = glyphShapeTable.size();
        byte[] baGlyphShapes = null;
        if (!swf.hasStrippedShapesFromFonts()) {
            for (int i = 0; i < numGlyphs; i++) {
                offsetTable.add(sos3.getPos());
                sos3.writeSHAPE(glyphShapeTable.get(i), 1);
            }
            baGlyphShapes = baosGlyphShapes.toByteArray();
        }

        sos.writeUI16(fontID);
        sos.writeUB(1, fontFlagsHasLayout ? 1 : 0);
        sos.writeUB(1, fontFlagsShiftJIS ? 1 : 0);
        sos.writeUB(1, fontFlagsSmallText ? 1 : 0);
        sos.writeUB(1, fontFlagsANSI ? 1 : 0);
        sos.writeUB(1, fontFlagsWideOffsets ? 1 : 0);
        sos.writeUB(1, fontFlagsWideCodes ? 1 : 0);
        sos.writeUB(1, fontFlagsItalic ? 1 : 0);
        sos.writeUB(1, fontFlagsBold ? 1 : 0);
        sos.writeLANGCODE(languageCode);
        sos.writeNetString(fontName);
        sos.writeUI16(numGlyphs);

        if (!swf.hasStrippedShapesFromFonts()) {
            for (long offset : offsetTable) {
                long offset2 = (glyphShapeTable.size() + 1/*CodeTableOffset*/) * (fontFlagsWideOffsets ? 4 : 2) + offset;
                if (fontFlagsWideOffsets) {
                    sos.writeUI32(offset2);
                } else {
                    sos.writeUI16((int) offset2);
                }
            }
            if (numGlyphs > 0 || fontFlagsHasLayout) {
                long offset = (glyphShapeTable.size() + 1/*CodeTableOffset*/) * (fontFlagsWideOffsets ? 4 : 2) + baGlyphShapes.length;
                if (fontFlagsWideOffsets) {
                    sos.writeUI32(offset);
                } else {
                    sos.writeUI16((int) offset);
                }
            }
            if (numGlyphs > 0) {
                sos.write(baGlyphShapes);
            }
        } else {
            sos.writeUI32(unknownGfx);
        }

        for (int i = 0; i < numGlyphs; i++) {
            if (fontFlagsWideCodes) {
                sos.writeUI16(codeTable.get(i));
            } else {
                sos.writeUI8(codeTable.get(i));
            }
        }

        if (fontFlagsHasLayout) {
            sos.writeUI16(fontAscent);
            sos.writeUI16(fontDescent);
            sos.writeSI16(fontLeading);
            for (int i = 0; i < numGlyphs; i++) {
                sos.writeUI16(fontAdvanceTable.get(i));
            }
            for (int i = 0; i < numGlyphs; i++) {
                sos.writeRECT(fontBoundsTable.get(i));
            }
            sos.writeUI16(fontKerningTable.size());
            for (int k = 0; k < fontKerningTable.size(); k++) {
                sos.writeKERNINGRECORD(fontKerningTable.get(k), fontFlagsWideCodes);
            }
        }
    }

    @Override
    public boolean isSmall() {
        return fontFlagsSmallText;
    }

    @Override
    public synchronized int getGlyphWidth(int glyphIndex) {
        return glyphShapeTable.get(glyphIndex).getBounds(1).getWidth();
    }

    @Override
    public synchronized double getGlyphAdvance(int glyphIndex) {
        if (fontFlagsHasLayout && glyphIndex != -1) {
            return fontAdvanceTable.get(glyphIndex);
        } else {
            return -1;
        }
    }

    @Override
    public synchronized char glyphToChar(int glyphIndex) {
        return Utf8Helper.codePointToChar(codeTable.get(glyphIndex), getCodesCharset());
    }

    @Override
    public synchronized int charToGlyph(char c) {
        return codeTable.indexOf(Utf8Helper.charToCodePoint(c, getCodesCharset()));
    }

    @Override
    public synchronized List<SHAPE> getGlyphShapeTable() {
        return glyphShapeTable;
    }

    @Override
    public int getCharacterId() {
        return fontID;
    }

    @Override
    public void setCharacterId(int characterId) {
        this.fontID = characterId;
    }

    @Override
    public String getFontNameIntag() {
        String ret = fontName;
        if (ret.contains("" + (char) 0)) {
            ret = ret.substring(0, ret.indexOf(0));
        }
        return ret;
    }

    @Override
    public boolean isBold() {
        return fontFlagsBold;
    }

    @Override
    public boolean isItalic() {
        return fontFlagsItalic;
    }

    @Override
    public boolean isSmallEditable() {
        return true;
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
        fontFlagsSmallText = value;
    }

    @Override
    public void setBold(boolean value) {
        fontFlagsBold = value;
    }

    @Override
    public void setItalic(boolean value) {
        fontFlagsItalic = value;
    }

    @Override
    public double getDivider() {
        return 20;
    }

    @Override
    public int getAscent() {
        if (fontFlagsHasLayout) {
            return fontAscent;
        }
        return -1;
    }

    @Override
    public int getDescent() {
        if (fontFlagsHasLayout) {
            return fontDescent;
        }
        return -1;
    }

    @Override
    public int getLeading() {
        if (fontFlagsHasLayout) {
            return fontLeading;
        }
        return -1;
    }

    @Override
    public synchronized boolean addCharacter(char character, Font font) {

        //Font Align Zones will be removed as adding new character zones is not supported:-(
        for (int i = 0; i < swf.getTags().size(); i++) {
            Tag t = swf.getTags().get(i);
            if (t instanceof DefineFontAlignZonesTag) {
                DefineFontAlignZonesTag fa = (DefineFontAlignZonesTag) t;
                if (fa.fontID == fontID) {
                    swf.removeTag(t);
                    i--;
                }
            }
        }
        int fontStyle = getFontStyle();
        SHAPE shp = SHAPERECORD.fontCharacterToSHAPE(font, (int) Math.round(getDivider() * 1024), character);
        int code = (int) Utf8Helper.charToCodePoint(character, getCodesCharset());

        if (code == -1) { //Fixme - throw exception, etc.
            code = 0;
        }
        int pos = -1;
        boolean exists = false;
        for (int i = 0; i < codeTable.size(); i++) {
            if (codeTable.get(i) >= code) {
                if (codeTable.get(i) == code) {
                    exists = true;
                }
                pos = i;
                break;
            }
        }
        if (pos == -1) {
            pos = codeTable.size();
        }

        if (!exists) {
            shiftGlyphIndices(fontID, pos, true);
            glyphShapeTable.add(pos, shp);
            codeTable.add(pos, code);
        } else {
            glyphShapeTable.set(pos, shp);
        }
        if (fontFlagsHasLayout) {

            Font advanceFont = font.deriveFont(fontStyle, 1024); // Not multiplied with divider as it causes problems to create font with height around 20k
            if (!exists) {
                fontBoundsTable.add(pos, shp.getBounds(1));
                fontAdvanceTable.add(pos, (int) getDivider() * Math.round(FontHelper.getFontAdvance(advanceFont, character)));
            } else {
                fontBoundsTable.set(pos, shp.getBounds(1));
                fontAdvanceTable.set(pos, (int) getDivider() * Math.round(FontHelper.getFontAdvance(advanceFont, character)));
            }

            for (int k = 0; k < fontKerningTable.size(); k++) {
                if (fontKerningTable.get(k).fontKerningCode1 == code
                        || fontKerningTable.get(k).fontKerningCode2 == code) {
                    fontKerningTable.remove(k);
                    k--;
                }
            }
            List<FontHelper.KerningPair> kerning = getFontKerningPairs(font, (int) (getDivider() * 1024));
            for (FontHelper.KerningPair pair : kerning) {
                if (pair.char1 != code && pair.char2 != code) {
                    continue;
                }
                int glyph1 = charToGlyph(pair.char1);
                if (pair.char1 == code) {
                    //empty
                } else if (glyph1 == -1) {
                    continue;
                }
                int glyph2 = charToGlyph(pair.char2);
                if (pair.char2 == code) {
                    //empty
                } else if (glyph2 == -1) {
                    continue;
                }
                fontKerningTable.add(new KERNINGRECORD(pair.char1, pair.char2, pair.kerning));
            }
        }

        checkWideParameters();
        setModified(true);
        getSwf().clearImageCache();
        return true;
    }

    @Override
    public synchronized boolean removeCharacter(char character) {

        //Font Align Zones will be removed as removing character zones is not supported:-(
        for (int i = 0; i < swf.getTags().size(); i++) {
            Tag t = swf.getTags().get(i);
            if (t instanceof DefineFontAlignZonesTag) {
                DefineFontAlignZonesTag fa = (DefineFontAlignZonesTag) t;
                if (fa.fontID == fontID) {
                    swf.removeTag(t);
                    i--;
                }
            }
        }

        int code = (int) character;
        int pos = -1;
        for (int i = 0; i < codeTable.size(); i++) {
            if (codeTable.get(i) >= code) {
                if (codeTable.get(i) == code) {
                    pos = i;
                    break;
                }

                return false;
            }
        }
        if (pos == -1) {
            return false;
        }

        glyphShapeTable.remove(pos);
        codeTable.remove(pos);

        if (fontFlagsHasLayout) {
            fontBoundsTable.remove(pos);
            fontAdvanceTable.remove(pos);

            for (int i = 0; i < fontKerningTable.size(); i++) {
                if (fontKerningTable.get(i).fontKerningCode1 == character
                        || fontKerningTable.get(i).fontKerningCode2 == character) {
                    fontKerningTable.remove(i);
                    i--;
                }
            }
        }

        shiftGlyphIndices(fontID, pos + 1, false);

        checkWideParameters();
        setModified(true);
        getSwf().clearImageCache();
        return true;
    }

    @Override
    public synchronized void setAdvanceValues(Font font) {
        List<RECT> newFontBoundsTable = new ArrayList<>();
        List<Integer> newFontAdvanceTable = new ArrayList<>();
        for (int i = 0; i < codeTable.size(); i++) {
            Integer character = codeTable.get(i);
            char ch = (char) (int) character;
            if (!font.canDisplay(ch) && fontFlagsHasLayout) { //cannot display, leave old if exist
                newFontAdvanceTable.add(fontAdvanceTable.get(i));
                newFontBoundsTable.add(fontBoundsTable.get(i));
                continue;
            }
            SHAPE shp = SHAPERECORD.fontCharacterToSHAPE(font, (int) Math.round(getDivider() * 1024), ch);
            newFontBoundsTable.add(shp.getBounds(1));
            int fontStyle = getFontStyle();
            Font advanceFont = font.deriveFont(fontStyle, 1024); // Not multiplied with divider as it causes problems to create font with height around 20k
            newFontAdvanceTable.add((int) getDivider() * Math.round(FontHelper.getFontAdvance(advanceFont, ch)));
        }
        fontAdvanceTable = newFontAdvanceTable;
        fontBoundsTable = newFontBoundsTable;
        fontKerningTable = new ArrayList<>();
        fontFlagsHasLayout = true;
    }

    @Override
    public synchronized int getCharacterCount() {
        return codeTable.size();
    }

    @Override
    public synchronized String getCharacters() {
        StringBuilder ret = new StringBuilder(codeTable.size());
        for (int i : codeTable) {
            Character c = Utf8Helper.codePointToChar(i, getCodesCharset());
            ret.append(c == null ? "?" : c);
        }
        return ret.toString();
    }

    @Override
    public boolean hasLayout() {
        return fontFlagsHasLayout;
    }

    @Override
    public synchronized RECT getGlyphBounds(int glyphIndex) {
        if (fontFlagsHasLayout) {
            return fontBoundsTable.get(glyphIndex);
        }
        return super.getGlyphBounds(glyphIndex);
    }

    @Override
    public synchronized int getGlyphKerningAdjustment(int glyphIndex, int nextGlyphIndex) {
        if (glyphIndex == -1 || nextGlyphIndex == -1) {
            return 0;
        }
        char c1 = glyphToChar(glyphIndex);
        char c2 = glyphToChar(nextGlyphIndex);
        return getCharKerningAdjustment(c1, c2);
    }

    @Override
    public synchronized int getCharKerningAdjustment(char c1, char c2) {
        int c1Code = Utf8Helper.charToCodePoint(c1, getCodesCharset());
        int c2Code = Utf8Helper.charToCodePoint(c2, getCodesCharset());

        int kerningAdjustment = 0;
        for (KERNINGRECORD ker : fontKerningTable) {
            if (ker.fontKerningCode1 == c1Code && ker.fontKerningCode2 == c2Code) {
                kerningAdjustment = ker.fontKerningAdjustment;
                break;
            }
        }
        return kerningAdjustment;
    }

    @Override
    public void setAscent(int ascent) {
        if (fontFlagsHasLayout) {
            fontAscent = ascent;
        }
    }

    @Override
    public void setDescent(int descent) {
        if (fontFlagsHasLayout) {
            fontDescent = descent;
        }
    }

    @Override
    public void setLeading(int leading) {
        if (fontFlagsHasLayout) {
            fontLeading = leading;
        }
    }

    @Override
    public void setHasLayout(boolean hasLayout) {
        fontFlagsHasLayout = hasLayout;

        if (hasLayout) {
            if (fontAdvanceTable == null) {
                fontAdvanceTable = new ArrayList<>();
            }
            if (fontBoundsTable == null) {
                fontBoundsTable = new ArrayList<>();
            }
            if (fontKerningTable == null) {
                fontKerningTable = new ArrayList<>();
            }
        }
    }

    @Override
    public void setFontNameIntag(String name) {
        fontName = name;
    }

    @Override
    public boolean isFontNameInTagEditable() {
        return true;
    }

    @Override
    public boolean isAscentEditable() {
        return hasLayout();
    }

    @Override
    public boolean isDescentEditable() {
        return hasLayout();
    }

    @Override
    public boolean isLeadingEditable() {
        return hasLayout();
    }

    @Override
    public String getCodesCharset() {
        if (fontFlagsShiftJIS) {
            return "Shift_JIS";
        }
        return getCharset();
    }
}
