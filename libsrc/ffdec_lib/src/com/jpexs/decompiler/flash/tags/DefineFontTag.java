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
import com.jpexs.decompiler.flash.tags.base.CharacterIdTag;
import com.jpexs.decompiler.flash.tags.base.FontInfoTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.types.BasicType;
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
 * DefineFont tag - defines a font.
 *
 * @author JPEXS
 */
@SWFVersion(from = 1)
public class DefineFontTag extends FontTag {

    public static final int ID = 10;

    public static final String NAME = "DefineFont";

    @SWFType(BasicType.UI16)
    public int fontId;

    @Conditional("!strippedShapes")
    public List<SHAPE> glyphShapeTable;

    @Internal
    private FontInfoTag fontInfoTag = null;

    @Internal
    public long unknownGfx;

    @Internal
    public boolean strippedShapes = false;

    /**
     * Constructor
     *
     * @param swf SWF
     */
    public DefineFontTag(SWF swf) {
        super(swf, ID, NAME, null);
        fontId = swf.getNextCharacterId();
        glyphShapeTable = new ArrayList<>();
    }

    /**
     * Constructor
     *
     * @param sis SWF input stream
     * @param data Data
     * @throws IOException On I/O error
     */
    public DefineFontTag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        fontId = sis.readUI16("fontId");
        glyphShapeTable = new ArrayList<>();

        strippedShapes = swf.hasStrippedShapesFromFonts();

        if (!strippedShapes && sis.available() > 0) {
            long pos = sis.getPos();
            int firstOffset = sis.readUI16("firstOffset");
            int nGlyphs = firstOffset / 2;

            long[] offsetTable = new long[nGlyphs];
            offsetTable[0] = firstOffset;
            for (int i = 1; i < nGlyphs; i++) {
                offsetTable[i] = sis.readUI16("offset");
            }
            for (int i = 0; i < nGlyphs; i++) {
                sis.seek(pos + offsetTable[i]);
                glyphShapeTable.add(sis.readSHAPE(1, false, "shape"));
            }
        }
        if (strippedShapes) {
            unknownGfx = sis.readUI32("unknownGfx");
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
        sos.writeUI16(fontId);
        if (!swf.hasStrippedShapesFromFonts()) {
            ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
            List<Integer> offsetTable = new ArrayList<>();
            SWFOutputStream sos2 = new SWFOutputStream(baos2, getVersion(), getCharset());
            for (SHAPE shape : glyphShapeTable) {
                offsetTable.add(glyphShapeTable.size() * 2 + (int) sos2.getPos());
                sos2.writeSHAPE(shape, 1);
            }
            for (int offset : offsetTable) {
                sos.writeUI16(offset);
            }
            sos.write(baos2.toByteArray());
        } else {
            sos.writeUI32(unknownGfx);
        }
    }

    @Override
    public boolean isSmall() {
        return false;
    }

    @Override
    public double getGlyphAdvance(int glyphIndex) {
        return -1;
    }

    @Override
    public void setGlyphAdvance(int glyphIndex, double advanceValue) {
        
    }

    @Override
    public void updateBounds() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }            
    
    @Override
    public synchronized int getGlyphWidth(int glyphIndex) {
        return glyphShapeTable.get(glyphIndex).getBounds(1).getWidth();
    }

    private void ensureFontInfo() {
        if (fontInfoTag == null) {
            List<CharacterIdTag> characterIdTags = swf.getCharacterIdTags(fontId);
            if (characterIdTags != null) {
                for (CharacterIdTag t : characterIdTags) {
                    if (t instanceof FontInfoTag) {
                        if (((FontInfoTag) t).fontID == fontId) {
                            fontInfoTag = (FontInfoTag) t;
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public synchronized char glyphToChar(int glyphIndex) {
        ensureFontInfo();
        if (fontInfoTag != null) {
            return Utf8Helper.codePointToChar(fontInfoTag.getCodeTable().get(glyphIndex), getCodesCharset());
        } else {
            return '?';
        }
    }

    @Override
    public int charToGlyph(char c) {
        ensureFontInfo();
        if (fontInfoTag != null) {
            return fontInfoTag.getCodeTable().indexOf(Utf8Helper.charToCodePoint(c, getCodesCharset()));
        }
        return -1;

    }

    @Override
    public synchronized List<SHAPE> getGlyphShapeTable() {
        return glyphShapeTable;
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
    public String getFontNameIntag() {
        ensureFontInfo();
        if (fontInfoTag != null) {
            return fontInfoTag.getFontName();
        }
        return null;
    }

    @Override
    public boolean isBold() {
        if (fontInfoTag != null) {
            return fontInfoTag.getFontFlagsBold();
        }
        return false;
    }

    @Override
    public boolean isItalic() {
        if (fontInfoTag != null) {
            return fontInfoTag.getFontFlagsItalic();
        }
        return false;
    }

    @Override
    public boolean isSmallEditable() {
        return false;
    }

    @Override
    public boolean isBoldEditable() {
        return fontInfoTag != null;
    }

    @Override
    public boolean isItalicEditable() {
        return fontInfoTag != null;
    }

    @Override
    public void setSmall(boolean value) {
    }

    @Override
    public void setBold(boolean value) {
        if (fontInfoTag != null) {
            fontInfoTag.setFontFlagsBold(value);
        }
    }

    @Override
    public void setItalic(boolean value) {
        if (fontInfoTag != null) {
            fontInfoTag.setFontFlagsItalic(value);
        }
    }

    @Override
    public int getAscent() {
        return -1;
    }

    @Override
    public int getDescent() {
        return -1;
    }

    @Override
    public int getLeading() {
        return -1;
    }

    @Override
    public double getDivider() {
        return 1;
    }

    @Override
    public String getCodesCharset() {
        if (fontInfoTag != null && fontInfoTag.isShiftJIS()) {
            return "Shift_JIS";
        }
        return getCharset();
    }

    @Override
    public synchronized boolean addCharacter(char character, Font font) {
        SHAPE shp = SHAPERECORD.fontCharacterToSHAPE(font, (int) Math.round(getDivider() * 1024), character);
        ensureFontInfo();
        int code = (int) Utf8Helper.charToCodePoint(character, getCodesCharset());

        if (code == -1) { //Fixme - throw exception, etc.
            code = 0;
        }
        int pos = -1;
        boolean exists = false;
        if (fontInfoTag != null) {
            List<Integer> codeTable = fontInfoTag.getCodeTable();
                        
            pos = codeTable.indexOf(code);
            exists = pos != -1;
            if (!exists) {
                for (int i = 0; i < codeTable.size(); i++) {
                    if (codeTable.get(i) > code) {
                        pos = i;
                        break;
                    }
                }
            }
            if (pos == -1) {
                pos = codeTable.size();
            }            
        } else {
            pos = 0;
        }

        //Check whether offset is not too large
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SWFOutputStream sos2 = new SWFOutputStream(baos, getVersion(), getCharset());
        for (int s = 0; s <= glyphShapeTable.size(); s++) {
            SHAPE shape;
            if (s == glyphShapeTable.size() || pos == s) {
                if (pos != s) {
                    break;
                }
                shape = shp;
            } else {
                shape = glyphShapeTable.get(s);
            }
            int offset = glyphShapeTable.size() * 2 + (int) sos2.getPos();
            if (offset > 0xffff) {
                return false;
            }
            try {
                sos2.writeSHAPE(shape, 1);
            } catch (IOException ex) {
                //should not happen
            }
        }

        if (!exists) {
            shiftGlyphIndices(fontId, pos, true);
            glyphShapeTable.add(pos, shp);
            if (fontInfoTag != null) {
                fontInfoTag.addFontCharacter(pos, code);
            }
        } else {
            glyphShapeTable.set(pos, shp);
        }

        setModified(true);
        getSwf().clearImageCache();
        return true;
    }

    @Override
    public synchronized boolean removeCharacter(char character) {
        ensureFontInfo();
        if (fontInfoTag == null) {
            return false;
        }

        int code = (int) character;
        int pos = -1;
        List<Integer> codeTable = fontInfoTag.getCodeTable();
        pos = codeTable.indexOf(code);
        
        if (pos == -1) {
            return false;
        }

        glyphShapeTable.remove(pos);
        fontInfoTag.removeFontCharacter(pos);

        shiftGlyphIndices(fontId, pos + 1, false);

        setModified(true);
        getSwf().clearImageCache();
        return true;
    }

    @Override
    public void setAdvanceValues(Font font) {
        throw new UnsupportedOperationException("Setting the advance values for DefineFontTag is not supported.");
    }

    @Override
    public synchronized int getCharacterCount() {
        ensureFontInfo();
        if (fontInfoTag != null) {
            List<Integer> codeTable = fontInfoTag.getCodeTable();
            return codeTable.size();
        }
        return 0;
    }

    @Override
    public synchronized String getCharacters() {
        ensureFontInfo();
        if (fontInfoTag != null) {
            List<Integer> codeTable = fontInfoTag.getCodeTable();
            StringBuilder ret = new StringBuilder(codeTable.size());
            for (int i : codeTable) {
                ret.append(Utf8Helper.codePointToChar(i, getCodesCharset()));
            }
            return ret.toString();
        }
        return "";
    }

    @Override
    public int getGlyphKerningAdjustment(int glyphIndex, int nextGlyphIndex) {
        return 0;
    }

    @Override
    public int getCharKerningAdjustment(char c1, char c2) {
        return 0;
    }

    @Override
    public void setAscent(int ascent) {
    }

    @Override
    public void setDescent(int descent) {
    }

    @Override
    public void setLeading(int leading) {
    }

    public void setHasLayout(boolean hasLayout) {

    }

    @Override
    public void setFontNameIntag(String name) {

    }

    @Override
    public boolean isFontNameInTagEditable() {
        return false;
    }

    @Override
    public boolean isAscentEditable() {
        return false;
    }

    @Override
    public boolean isDescentEditable() {
        return false;
    }

    @Override
    public boolean isLeadingEditable() {
        return false;
    }

}
