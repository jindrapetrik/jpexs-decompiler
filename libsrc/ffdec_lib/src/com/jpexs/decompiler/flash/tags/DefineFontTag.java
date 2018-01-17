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
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.tags.base.CharacterIdTag;
import com.jpexs.decompiler.flash.tags.base.FontInfoTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.helpers.ByteArrayRange;
import java.awt.Font;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
@SWFVersion(from = 1)
public class DefineFontTag extends FontTag {

    public static final int ID = 10;

    public static final String NAME = "DefineFont";

    @SWFType(BasicType.UI16)
    public int fontId;

    public List<SHAPE> glyphShapeTable;

    @Internal
    private FontInfoTag fontInfoTag = null;

    /**
     * Constructor
     *
     * @param swf
     */
    public DefineFontTag(SWF swf) {
        super(swf, ID, NAME, null);
        fontId = swf.getNextCharacterId();
        glyphShapeTable = new ArrayList<>();
    }

    /**
     * Constructor
     *
     * @param sis
     * @param data
     * @throws IOException
     */
    public DefineFontTag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        fontId = sis.readUI16("fontId");
        glyphShapeTable = new ArrayList<>();

        if (sis.available() > 0) {
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
    }

    /**
     * Gets data bytes
     *
     * @param sos SWF output stream
     * @throws java.io.IOException
     */
    @Override
    public void getData(SWFOutputStream sos) throws IOException {
        sos.writeUI16(fontId);
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        List<Integer> offsetTable = new ArrayList<>();
        SWFOutputStream sos2 = new SWFOutputStream(baos2, getVersion());
        for (SHAPE shape : glyphShapeTable) {
            offsetTable.add(glyphShapeTable.size() * 2 + (int) sos2.getPos());
            sos2.writeSHAPE(shape, 1);
        }
        for (int offset : offsetTable) {
            sos.writeUI16(offset);
        }
        sos.write(baos2.toByteArray());
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
    public int getGlyphWidth(int glyphIndex) {
        return glyphShapeTable.get(glyphIndex).getBounds().getWidth();
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
    public char glyphToChar(int glyphIndex) {
        ensureFontInfo();
        if (fontInfoTag != null) {
            return (char) (int) fontInfoTag.getCodeTable().get(glyphIndex);
        } else {
            return '?';
        }
    }

    @Override
    public int charToGlyph(char c) {
        ensureFontInfo();
        if (fontInfoTag != null) {
            return fontInfoTag.getCodeTable().indexOf((int) c);
        }
        return -1;

    }

    @Override
    public List<SHAPE> getGlyphShapeTable() {
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
    public void addCharacter(char character, Font font) {
        SHAPE shp = SHAPERECORD.fontCharacterToSHAPE(font, (int) Math.round(getDivider() * 1024), character);
        ensureFontInfo();
        int code = (int) character;
        int pos = -1;
        boolean exists = false;
        if (fontInfoTag != null) {
            List<Integer> codeTable = fontInfoTag.getCodeTable();
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
        } else {
            pos = 0;
        }

        if (!exists) {
            shiftGlyphIndices(fontId, pos, true);
            glyphShapeTable.add(pos, shp);
            if (fontInfoTag != null) {
                fontInfoTag.addFontCharacter(pos, (int) character);
            }
        } else {
            glyphShapeTable.set(pos, shp);
        }

        setModified(true);
        getSwf().clearImageCache();
    }

    @Override
    public boolean removeCharacter(char character) {
        ensureFontInfo();
        if (fontInfoTag == null) {
            return false;
        }

        int code = (int) character;
        int pos = -1;
        List<Integer> codeTable = fontInfoTag.getCodeTable();
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
    public int getCharacterCount() {
        ensureFontInfo();
        if (fontInfoTag != null) {
            List<Integer> codeTable = fontInfoTag.getCodeTable();
            return codeTable.size();
        }
        return 0;
    }

    @Override
    public String getCharacters() {
        ensureFontInfo();
        if (fontInfoTag != null) {
            List<Integer> codeTable = fontInfoTag.getCodeTable();
            StringBuilder ret = new StringBuilder(codeTable.size());
            for (int i : codeTable) {
                ret.append((char) i);
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
}
