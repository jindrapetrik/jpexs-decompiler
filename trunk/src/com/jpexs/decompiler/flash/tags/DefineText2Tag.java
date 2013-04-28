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
import com.jpexs.decompiler.flash.gui.MainFrame;
import com.jpexs.decompiler.flash.helpers.Helper;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.tags.text.ParseException;
import com.jpexs.decompiler.flash.tags.text.ParsedSymbol;
import static com.jpexs.decompiler.flash.tags.text.SymbolType.COLOR;
import static com.jpexs.decompiler.flash.tags.text.SymbolType.FONT;
import static com.jpexs.decompiler.flash.tags.text.SymbolType.TEXT;
import static com.jpexs.decompiler.flash.tags.text.SymbolType.X;
import static com.jpexs.decompiler.flash.tags.text.SymbolType.Y;
import com.jpexs.decompiler.flash.tags.text.TextLexer;
import com.jpexs.decompiler.flash.types.GLYPHENTRY;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.TEXTRECORD;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 *
 * @author JPEXS
 */
public class DefineText2Tag extends CharacterTag implements BoundedTag, TextTag {

    public int characterID;
    public RECT textBounds;
    public MATRIX textMatrix;
    public int glyphBits;
    public int advanceBits;
    public List<TEXTRECORD> textRecords;

    @Override
    public String getText(List<Tag> tags) {
        FontTag fnt = null;
        String ret = "";
        for (TEXTRECORD rec : textRecords) {
            if (rec.styleFlagsHasFont) {
                for (Tag t : tags) {
                    if (t instanceof FontTag) {
                        if (((FontTag) t).getFontId() == rec.fontId) {
                            fnt = ((FontTag) t);
                            break;
                        }
                    }
                }
            }
            ret += rec.getText(tags, fnt);
        }
        return ret;
    }

    @Override
    public String getFormattedText(List<Tag> tags) {
        FontTag fnt = null;
        String ret = "";
        for (TEXTRECORD rec : textRecords) {
            if (rec.styleFlagsHasFont) {
                for (Tag t : tags) {
                    if (t instanceof FontTag) {
                        if (((FontTag) t).getFontId() == rec.fontId) {
                            fnt = ((FontTag) t);
                            break;
                        }
                    }
                }
                ret += "[font " + rec.fontId + " height:" + rec.textHeight + "]";
            }
            if (rec.styleFlagsHasColor) {
                ret += "[color " + rec.textColorA.toHexARGB() + "]";
            }
            if (rec.styleFlagsHasXOffset) {
                ret += "[x " + rec.xOffset + "]";
            }
            if (rec.styleFlagsHasYOffset) {
                ret += "[y " + rec.yOffset + "]";
            }
            ret += Helper.escapeString(rec.getText(tags, fnt)).replace("[", "\\[").replace("]", "\\]");
        }
        return ret;
    }

    @Override
    public void setFormattedText(List<Tag> tags, String text) throws ParseException {
        try {
            TextLexer lexer = new TextLexer(new InputStreamReader(new ByteArrayInputStream(text.getBytes("UTF-8")), "UTF-8"));
            ParsedSymbol s = null;
            textRecords = new ArrayList<TEXTRECORD>();
            RGBA colorA = null;
            int fontId = -1;
            int textHeight = -1;
            FontTag font = null;
            Integer x = null;
            Integer y = null;
            glyphBits = 0;
            advanceBits = 0;
            while ((s = lexer.yylex()) != null) {
                switch (s.type) {
                    case COLOR:
                        Matcher m = Pattern.compile("([0-9a-f][0-9a-f])([0-9a-f][0-9a-f])([0-9a-f][0-9a-f])([0-9a-f][0-9a-f])").matcher(s.values[0].toString());
                        if (m.matches()) {
                            colorA = new RGBA(Integer.parseInt(m.group(2), 16), Integer.parseInt(m.group(3), 16), Integer.parseInt(m.group(4), 16), Integer.parseInt(m.group(1), 16));
                        }
                        break;
                    case FONT:
                        fontId = (Integer) s.values[0];
                        textHeight = (Integer) s.values[1];
                        for (Tag t : tags) {
                            if (t instanceof FontTag) {
                                if (((FontTag) t).getFontId() == fontId) {
                                    font = (FontTag) t;
                                    break;
                                }
                            }
                        }
                        break;
                    case X:
                        x = (Integer) s.values[0];
                        break;
                    case Y:
                        y = (Integer) s.values[0];
                        break;
                    case TEXT:
                        if (font == null) {
                            throw new ParseException("Font not defined", lexer.yyline());
                        }
                        TEXTRECORD tr = new TEXTRECORD();
                        if (fontId > -1) {
                            tr.fontId = fontId;
                            tr.textHeight = textHeight;
                            fontId = -1;
                            tr.styleFlagsHasFont = true;
                        }
                        if (colorA != null) {
                            tr.textColorA = colorA;
                            tr.styleFlagsHasColor = true;
                            colorA = null;
                        }
                        if (x != null) {
                            tr.xOffset = x;
                            tr.styleFlagsHasXOffset = true;
                            x = null;
                        }
                        if (y != null) {
                            tr.yOffset = y;
                            tr.styleFlagsHasYOffset = true;
                            y = null;
                        }
                        String txt = (String) s.values[0];
                        tr.glyphEntries = new GLYPHENTRY[txt.length()];
                        for (int i = 0; i < txt.length(); i++) {
                            char c = txt.charAt(i);
                            tr.glyphEntries[i] = new GLYPHENTRY();
                            tr.glyphEntries[i].glyphIndex = font.charToGlyph(tags, c);
                            if (tr.glyphEntries[i].glyphIndex == -1) {
                                throw new ParseException("Font does not contain glyph for character '" + c + "'", lexer.yyline());
                            }
                            tr.glyphEntries[i].glyphAdvance = textHeight * font.getGlyphAdvance(tr.glyphEntries[i].glyphIndex) / 1024;
                            if (SWFOutputStream.getNeededBitsS(tr.glyphEntries[i].glyphIndex) > glyphBits) {
                                glyphBits = SWFOutputStream.getNeededBitsS(tr.glyphEntries[i].glyphIndex);
                            }
                            if (SWFOutputStream.getNeededBitsS(tr.glyphEntries[i].glyphAdvance) > advanceBits) {
                                advanceBits = SWFOutputStream.getNeededBitsS(tr.glyphEntries[i].glyphAdvance);
                            }

                        }
                        textRecords.add(tr);
                        break;
                }

            }
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
        }
    }

    @Override
    public RECT getRect(HashMap<Integer, CharacterTag> characters) {
        return textBounds;
    }

    @Override
    public int getCharacterID() {
        return characterID;
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
            sos.writeUI16(characterID);
            sos.writeRECT(textBounds);
            sos.writeMatrix(textMatrix);
            sos.writeUI8(glyphBits);
            sos.writeUI8(advanceBits);
            for (TEXTRECORD tr : textRecords) {
                sos.writeTEXTRECORD(tr, true, glyphBits, advanceBits);
            }
            sos.writeUI8(0);
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
    public DefineText2Tag(byte data[], int version, long pos) throws IOException {
        super(33, "DefineText2", data, pos);
        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        characterID = sis.readUI16();
        textBounds = sis.readRECT();
        textMatrix = sis.readMatrix();
        glyphBits = sis.readUI8();
        advanceBits = sis.readUI8();
        textRecords = new ArrayList<TEXTRECORD>();
        TEXTRECORD tr;
        while ((tr = sis.readTEXTRECORD(true, glyphBits, advanceBits)) != null) {
            textRecords.add(tr);
        }
    }

    @Override
    public Set<Integer> getNeededCharacters() {
        Set<Integer> ret = new HashSet<Integer>();
        for (TEXTRECORD tr : textRecords) {
            if (tr.styleFlagsHasFont) {
                ret.add(tr.fontId);
            }
        }
        return ret;
    }
}
