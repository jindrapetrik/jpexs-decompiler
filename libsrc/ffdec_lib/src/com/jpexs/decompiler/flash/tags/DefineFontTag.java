/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.helpers.ByteArrayRange;
import java.awt.Font;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class DefineFontTag extends FontTag {

    public static final int ID = 10;

    public static final String NAME = "DefineFont";

    @SWFType(BasicType.UI16)
    public int fontId;

    public List<SHAPE> glyphShapeTable;

    @Internal
    private DefineFontInfoTag fontInfoTag = null;

    @Internal
    private DefineFontInfo2Tag fontInfo2Tag = null;

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
            for (Tag t : swf.tags) {
                if (t instanceof DefineFontInfoTag) {
                    if (((DefineFontInfoTag) t).fontId == fontId) {
                        fontInfoTag = (DefineFontInfoTag) t;
                        break;
                    }
                }
                if (t instanceof DefineFontInfo2Tag) {
                    if (((DefineFontInfo2Tag) t).fontID == fontId) {
                        fontInfo2Tag = (DefineFontInfo2Tag) t;
                        break;
                    }
                }
            }
        }
    }

    @Override
    public char glyphToChar(int glyphIndex) {
        ensureFontInfo();
        if (fontInfo2Tag != null) {
            return (char) (int) fontInfo2Tag.codeTable.get(glyphIndex);
        } else if (fontInfoTag != null) {
            return (char) (int) fontInfoTag.codeTable.get(glyphIndex);
        } else {
            return '?';
        }
    }

    @Override
    public int charToGlyph(char c) {
        ensureFontInfo();
        if (fontInfo2Tag != null) {
            return fontInfo2Tag.codeTable.indexOf((int) c);
        } else if (fontInfoTag != null) {
            return fontInfoTag.codeTable.indexOf((int) c);
        }
        return -1;

    }

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
        } catch (IOException e) {
            throw new Error("This should never happen.", e);
        }
        return baos.toByteArray();
    }

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

    @Override
    public int getFontId() {
        return fontId;
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
        if (fontInfo2Tag != null) {
            return fontInfo2Tag.fontName;
        }
        if (fontInfoTag != null) {
            return fontInfoTag.fontName;
        }
        return null;
    }

    @Override
    public boolean isBold() {
        if (fontInfo2Tag != null) {
            return fontInfo2Tag.fontFlagsBold;
        }
        if (fontInfoTag != null) {
            return fontInfoTag.fontFlagsBold;
        }
        return false;
    }

    @Override
    public boolean isItalic() {
        if (fontInfo2Tag != null) {
            return fontInfo2Tag.fontFlagsItalic;
        }
        if (fontInfoTag != null) {
            return fontInfoTag.fontFlagsItalic;
        }
        return false;
    }

    @Override
    public boolean isSmallEditable() {
        return false;
    }

    @Override
    public boolean isBoldEditable() {
        return fontInfo2Tag != null || fontInfoTag != null;
    }

    @Override
    public boolean isItalicEditable() {
        return fontInfo2Tag != null || fontInfoTag != null;
    }

    @Override
    public void setSmall(boolean value) {
    }

    @Override
    public void setBold(boolean value) {
        if (fontInfo2Tag != null) {
            fontInfo2Tag.fontFlagsBold = value;
        }
        if (fontInfoTag != null) {
            fontInfoTag.fontFlagsBold = value;
        }
    }

    @Override
    public void setItalic(boolean value) {
        if (fontInfo2Tag != null) {
            fontInfo2Tag.fontFlagsItalic = value;
        }
        if (fontInfoTag != null) {
            fontInfoTag.fontFlagsItalic = value;
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
        List<Integer> codeTable = new ArrayList<>();
        ensureFontInfo();
        if (fontInfoTag != null) {
            codeTable = fontInfoTag.codeTable;
        }
        if (fontInfo2Tag != null) {
            codeTable = fontInfo2Tag.codeTable;
        }
        int code = (int) character;
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
            shiftGlyphIndices(fontId, pos);
            glyphShapeTable.add(pos, shp);
            codeTable.add(pos, (int) character);
        } else {
            glyphShapeTable.set(pos, shp);
        }

        setModified(true);
    }

    @Override
    public void setAdvanceValues(Font font) {
        throw new UnsupportedOperationException("Setting the advance values for DefineFontTag is not supported.");
    }

    @Override
    public String getCharacters(List<Tag> tags) {
        StringBuilder ret = new StringBuilder();
        ensureFontInfo();
        if (fontInfoTag != null) {
            for (int i : fontInfoTag.codeTable) {
                ret.append((char) i);
            }
        }
        if (fontInfo2Tag != null) {
            for (int i : fontInfo2Tag.codeTable) {
                ret.append((char) i);
            }
        }
        return ret.toString();
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
