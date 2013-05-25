/*
 *  Copyright (C) 2010-2013 JPEXS
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
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.types.SHAPE;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 *
 *
 * @author JPEXS
 */
public class DefineFontTag extends CharacterTag implements FontTag {

    public int fontId;
    public int offsetTable[];
    public SHAPE glyphShapeTable[];
    private DefineFontInfoTag fontInfoTag = null;
    private DefineFontInfo2Tag fontInfo2Tag = null;

    @Override
    public boolean isSmall() {
        return false;
    }

    @Override
    public int getGlyphAdvance(int glyphIndex) {
        return getGlyphWidth(glyphIndex);
    }

    @Override
    public int getGlyphWidth(int glyphIndex) {
        return glyphShapeTable[glyphIndex].getBounds().getWidth();
    }

    private void ensureFontInfo(List<Tag> tags) {
        if (fontInfoTag == null) {
            for (Tag t : tags) {
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
    public char glyphToChar(List<Tag> tags, int glyphIndex) {
        ensureFontInfo(tags);
        if (fontInfo2Tag != null) {
            return (char) (int) fontInfo2Tag.codeTable.get(glyphIndex);
        } else if (fontInfoTag != null) {
            return (char) (int) fontInfoTag.codeTable.get(glyphIndex);
        } else {
            return '?';
        }
    }

    @Override
    public int charToGlyph(List<Tag> tags, char c) {
        ensureFontInfo(tags);
        if (fontInfo2Tag != null) {
            return fontInfo2Tag.codeTable.indexOf(c);
        } else if (fontInfoTag != null) {
            return fontInfoTag.codeTable.indexOf(c);
        }
        return -1;

    }

    /**
     * Gets data bytes
     *
     * @param version SWF version
     * @return Bytes of data
     */
    @Override
    public byte[] getData(int version) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream os = baos;
        SWFOutputStream sos = new SWFOutputStream(os, version);
        try {
            sos.writeUI16(fontId);
            for (int offset : offsetTable) {
                sos.writeUI16(offset);
            }
            for (SHAPE shape : glyphShapeTable) {
                sos.writeSHAPE(shape, 1);
            }
        } catch (IOException e) {
        }
        return baos.toByteArray();
    }

    /**
     * Constructor
     *
     * @param data Data bytes
     * @param version SWF version
     * @throws IOException
     */
    public DefineFontTag(byte data[], int version, long pos) throws IOException {
        super(10, "DefineFont", data, pos);
        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        fontId = sis.readUI16();
        int firstOffset = sis.readUI16();
        int nGlyphs = firstOffset / 2;
        offsetTable = new int[nGlyphs];
        glyphShapeTable = new SHAPE[nGlyphs];
        offsetTable[0] = firstOffset;
        for (int i = 1; i < nGlyphs; i++) {
            offsetTable[i] = sis.readUI16();
        }
        for (int i = 0; i < nGlyphs; i++) {
            glyphShapeTable[i] = sis.readSHAPE(1);
        }
    }

    @Override
    public int getFontId() {
        return fontId;
    }

    @Override
    public SHAPE[] getGlyphShapeTable() {
        return glyphShapeTable;
    }

    @Override
    public int getCharacterID() {
        return fontId;
    }

    @Override
    public String getFontName(List<Tag> tags) {
        ensureFontInfo(tags);
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
}
