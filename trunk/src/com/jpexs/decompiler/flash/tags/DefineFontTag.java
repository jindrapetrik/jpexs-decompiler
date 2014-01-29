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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 * @author JPEXS
 */
public class DefineFontTag extends FontTag {

    public int fontId;
    public List<SHAPE> glyphShapeTable;
    private DefineFontInfoTag fontInfoTag = null;
    private DefineFontInfo2Tag fontInfo2Tag = null;
    public static final int ID = 10;

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
            ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
            List<Integer> offsetTable = new ArrayList<>();
            SWFOutputStream sos2 = new SWFOutputStream(baos2, version);
            for (SHAPE shape : glyphShapeTable) {
                offsetTable.add(glyphShapeTable.size() * 2 + (int) sos2.getPos());
                sos2.writeSHAPE(shape, 1);
            }
            for (int offset : offsetTable) {
                sos.writeUI16(offset);
            }
            sos.write(baos2.toByteArray());
        } catch (IOException e) {
        }
        return baos.toByteArray();
    }

    /**
     * Constructor
     *
     * @param swf
     * @param data Data bytes
     * @param version SWF version
     * @param pos
     * @throws IOException
     */
    public DefineFontTag(SWF swf, byte[] data, int version, long pos) throws IOException {
        super(swf, ID, "DefineFont", data, pos);
        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        fontId = sis.readUI16();
        int firstOffset = sis.readUI16();
        int nGlyphs = firstOffset / 2;
        glyphShapeTable = new ArrayList<>();

        for (int i = 1; i < nGlyphs; i++) {
            sis.readUI16(); //offset
        }
        for (int i = 0; i < nGlyphs; i++) {
            glyphShapeTable.add(sis.readSHAPE(1));
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
    public String getFontName() {
        ensureFontInfo(swf.tags);
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
    public int getDivider() {
        return 1;
    }

    @Override
    public void addCharacter(List<Tag> tags, char character, String fontName) {
        SHAPE shp = SHAPERECORD.systemFontCharacterToSHAPE(fontName, getFontStyle(), getDivider() * 1024, character);
        List<Integer> codeTable = new ArrayList<>();
        ensureFontInfo(tags);
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
            FontTag.shiftGlyphIndices(fontId, pos, tags);
            glyphShapeTable.add(pos, shp);
            codeTable.add(pos, (int) character);
        } else {
            glyphShapeTable.set(pos, shp);
        }

    }

    @Override
    public String getCharacters(List<Tag> tags) {
        String ret = "";
        ensureFontInfo(tags);
        if (fontInfoTag != null) {
            for (int i : fontInfoTag.codeTable) {
                ret += (char) i;
            }
        }
        if (fontInfo2Tag != null) {
            for (int i : fontInfo2Tag.codeTable) {
                ret += (char) i;
            }
        }
        return ret;
    }

    @Override
    public int getGlyphKerningAdjustment(List<Tag> tags, int glyphIndex, int nextGlyphIndex) {
        return 0;
    }
}
