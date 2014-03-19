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
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.KERNINGRECORD;
import com.jpexs.decompiler.flash.types.LANGCODE;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.annotations.Conditional;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.awt.Font;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

/**
 *
 *
 * @author JPEXS
 */
public class DefineFont2Tag extends FontTag {

    @SWFType(BasicType.UI16)
    public int fontId;

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
    @Internal
    public int numGlyphs;
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

    @SWFType(BasicType.SI16)
    @Conditional("fontFlagsHasLayout")
    public List<Integer> fontAdvanceTable;

    @Conditional("fontFlagsHasLayout")
    public List<RECT> fontBoundsTable;

    public KERNINGRECORD[] fontKerningTable;
    public static final int ID = 48;

    @Override
    public boolean isSmall() {
        return fontFlagsSmallText;
    }

    @Override
    public int getGlyphWidth(int glyphIndex) {
        return glyphShapeTable.get(glyphIndex).getBounds().getWidth();
    }

    @Override
    public double getGlyphAdvance(int glyphIndex) {
        if (fontFlagsHasLayout) {
            return fontAdvanceTable.get(glyphIndex);
        } else {
            return -1;
        }
    }

    /**
     * Gets data bytes
     *
     * @return Bytes of data
     */
    @Override
    public byte[] getData() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SWFOutputStream sos = new SWFOutputStream(baos, getVersion());
        try {
            sos.writeUI16(fontId);
            sos.writeUB(1, fontFlagsHasLayout ? 1 : 0);
            sos.writeUB(1, fontFlagsShiftJIS ? 1 : 0);
            sos.writeUB(1, fontFlagsSmallText ? 1 : 0);
            sos.writeUB(1, fontFlagsANSI ? 1 : 0);
            sos.writeUB(1, fontFlagsWideOffsets ? 1 : 0);
            sos.writeUB(1, fontFlagsWideCodes ? 1 : 0);
            sos.writeUB(1, fontFlagsItalic ? 1 : 0);
            sos.writeUB(1, fontFlagsBold ? 1 : 0);
            sos.writeLANGCODE(languageCode);
            byte[] fontNameBytes = Utf8Helper.getBytes(fontName);
            sos.writeUI8(fontNameBytes.length);
            sos.write(fontNameBytes);
            sos.writeUI16(numGlyphs);

            List<Long> offsetTable = new ArrayList<>();
            ByteArrayOutputStream baosGlyphShapes = new ByteArrayOutputStream();

            SWFOutputStream sos3 = new SWFOutputStream(baosGlyphShapes, getVersion());
            for (int i = 0; i < numGlyphs; i++) {
                offsetTable.add((glyphShapeTable.size() + 1/*CodeTableOffset*/) * (fontFlagsWideOffsets ? 4 : 2) + sos3.getPos());
                sos3.writeSHAPE(glyphShapeTable.get(i), 1);
            }
            byte[] baGlyphShapes = baosGlyphShapes.toByteArray();
            for (Long offset : offsetTable) {
                if (fontFlagsWideOffsets) {
                    sos.writeUI32(offset);
                } else {
                    sos.writeUI16((int) (long) offset);
                }
            }
            if (numGlyphs > 0) {
                long offset = (glyphShapeTable.size() + 1/*CodeTableOffset*/) * (fontFlagsWideOffsets ? 4 : 2) + baGlyphShapes.length;
                if (fontFlagsWideOffsets) {
                    sos.writeUI32(offset);
                } else {
                    sos.writeUI16((int) offset);
                }
                sos.write(baGlyphShapes);

                for (int i = 0; i < numGlyphs; i++) {
                    if (fontFlagsWideCodes) {
                        sos.writeUI16(codeTable.get(i));
                    } else {
                        sos.writeUI8(codeTable.get(i));
                    }
                }
            }
            if (fontFlagsHasLayout) {
                sos.writeSI16(fontAscent);
                sos.writeSI16(fontDescent);
                sos.writeSI16(fontLeading);
                for (int i = 0; i < numGlyphs; i++) {
                    sos.writeSI16(fontAdvanceTable.get(i));
                }
                for (int i = 0; i < numGlyphs; i++) {
                    sos.writeRECT(fontBoundsTable.get(i));
                }
                sos.writeUI16(fontKerningTable.length);
                for (int k = 0; k < fontKerningTable.length; k++) {
                    sos.writeKERNINGRECORD(fontKerningTable[k], fontFlagsWideCodes);
                }
            }

        } catch (IOException e) {
        }
        return baos.toByteArray();
    }

    public DefineFont2Tag(SWF swf) throws IOException {
        super(swf, ID, "DefineFont2", null, 0);
    }

    /**
     * Constructor
     *
     * @param swf
     * @param data Data bytes
     * @param pos
     * @throws IOException
     */
    public DefineFont2Tag(SWF swf, byte[] data, long pos) throws IOException {
        super(swf, ID, "DefineFont2", data, pos);
        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), swf.version);
        fontId = sis.readUI16();
        fontFlagsHasLayout = sis.readUB(1) == 1;
        fontFlagsShiftJIS = sis.readUB(1) == 1;
        fontFlagsSmallText = sis.readUB(1) == 1;
        fontFlagsANSI = sis.readUB(1) == 1;
        fontFlagsWideOffsets = sis.readUB(1) == 1;
        fontFlagsWideCodes = sis.readUB(1) == 1;
        fontFlagsItalic = sis.readUB(1) == 1;
        fontFlagsBold = sis.readUB(1) == 1;
        languageCode = sis.readLANGCODE();
        int fontNameLen = sis.readUI8();
        if (swf.version >= 6) {
            fontName = new String(sis.readBytesEx(fontNameLen), Utf8Helper.charset);
        } else {
            fontName = new String(sis.readBytesEx(fontNameLen));
        }
        numGlyphs = sis.readUI16();
        //offsetTable = new long[numGlyphs];
        for (int i = 0; i < numGlyphs; i++) { //offsetTable
            if (fontFlagsWideOffsets) {
                sis.readUI32();
            } else {
                sis.readUI16();
            }
        }
        if (numGlyphs > 0) { //codeTableOffset
            if (fontFlagsWideOffsets) {
                sis.readUI32();
            } else {
                sis.readUI16();
            }
        }

        glyphShapeTable = new ArrayList<>();
        for (int i = 0; i < numGlyphs; i++) {
            glyphShapeTable.add(sis.readSHAPE(1));
        }

        codeTable = new ArrayList<>(); //[numGlyphs];
        for (int i = 0; i < numGlyphs; i++) {
            if (fontFlagsWideCodes) {
                codeTable.add(sis.readUI16());
            } else {
                codeTable.add(sis.readUI8());
            }
        }
        if (fontFlagsHasLayout) {
            fontAscent = sis.readSI16();
            fontDescent = sis.readSI16();
            fontLeading = sis.readSI16();
            fontAdvanceTable = new ArrayList<>();
            for (int i = 0; i < numGlyphs; i++) {
                fontAdvanceTable.add(sis.readSI16());
            }
            fontBoundsTable = new ArrayList<>();
            for (int i = 0; i < numGlyphs; i++) {
                fontBoundsTable.add(sis.readRECT());
            }
            int kerningCount = sis.readUI16();
            fontKerningTable = new KERNINGRECORD[kerningCount];
            for (int i = 0; i < kerningCount; i++) {
                fontKerningTable[i] = sis.readKERNINGRECORD(fontFlagsWideCodes);
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
    public char glyphToChar(int glyphIndex) {
        return (char) (int) codeTable.get(glyphIndex);
    }

    @Override
    public int charToGlyph(char c) {
        return codeTable.indexOf((Integer) (int) c);
    }

    @Override
    public String getFontName() {
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
    public int getDivider() {
        return 1;
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
    public void addCharacter(char character, String fontName) {
        int fontStyle = getFontStyle();

        SHAPE shp = SHAPERECORD.systemFontCharacterToSHAPE(fontName, fontStyle, getDivider() * 1024, character);

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

        if (fontFlagsHasLayout) {
            Font fnt = new Font(fontName, fontStyle, 1024);
            if (!exists) {
                fontBoundsTable.add(pos, shp.getBounds());
                fontAdvanceTable.add(pos, (int) getDivider() * Math.round(fnt.createGlyphVector((new JPanel()).getFontMetrics(fnt).getFontRenderContext(), "" + character).getGlyphMetrics(0).getAdvanceX()));
            } else {
                fontBoundsTable.set(pos, shp.getBounds());
                fontAdvanceTable.set(pos, (int) getDivider() * Math.round(fnt.createGlyphVector((new JPanel()).getFontMetrics(fnt).getFontRenderContext(), "" + character).getGlyphMetrics(0).getAdvanceX()));
            }
        }
        if (!exists) {
            numGlyphs++;
        }
    }

    @Override
    public String getCharacters(List<Tag> tags) {
        String ret = "";
        for (int i : codeTable) {
            ret += (char) i;
        }
        return ret;
    }

    @Override
    public boolean hasLayout() {
        return fontFlagsHasLayout;
    }

    @Override
    public int getGlyphKerningAdjustment(int glyphIndex, int nextGlyphIndex) {
        char c1 = glyphToChar(glyphIndex);
        char c2 = glyphToChar(nextGlyphIndex);
        int kerningAdjustment = 0;
        for (KERNINGRECORD ker : fontKerningTable) {
            if (ker.fontKerningCode1 == c1 && ker.fontKerningCode2 == c2) {
                kerningAdjustment = ker.fontKerningAdjustment;
                break;
            }
        }
        return kerningAdjustment;
    }
}
