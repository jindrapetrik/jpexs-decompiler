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
import com.jpexs.decompiler.flash.helpers.Helper;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.DrawableTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.tags.text.ParseException;
import com.jpexs.decompiler.flash.tags.text.ParsedSymbol;
import com.jpexs.decompiler.flash.tags.text.TextLexer;
import com.jpexs.decompiler.flash.types.GLYPHENTRY;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.TEXTRECORD;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
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
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 *
 * @author JPEXS
 */
public class DefineTextTag extends CharacterTag implements BoundedTag, TextTag, DrawableTag {

    public int characterID;
    public RECT textBounds;
    public MATRIX textMatrix;
    public int glyphBits;
    public int advanceBits;
    public List<TEXTRECORD> textRecords;
    public static final int ID = 11;

    @Override
    public MATRIX getTextMatrix(){
        return textMatrix;
    }
    @Override
    public RECT getBounds() {
        return textBounds;
    }

    @Override
    public void setBounds(RECT r) {
        textBounds = r;
    }

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
            if (rec.styleFlagsHasXOffset || rec.styleFlagsHasYOffset) {
                if (!ret.equals("")) {
                    ret += "\r\n";
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
        ret += "[\r\nxmin " + textBounds.Xmin + "\r\nymin " + textBounds.Ymin + "\r\nxmax " + textBounds.Xmax + "\r\nymax " + textBounds.Ymax;
        if (textMatrix.translateX != 0) {
            ret += "\r\ntranslatex " + textMatrix.translateX;
        }
        if (textMatrix.translateY != 0) {
            ret += "\r\ntranslatey " + textMatrix.translateY;
        }
        if (textMatrix.hasScale) {
            ret += "\r\nscalex " + textMatrix.scaleX;
            ret += "\r\nscaley " + textMatrix.scaleY;
        }
        if (textMatrix.hasRotate) {
            ret += "\r\nrotateskew0 " + textMatrix.rotateSkew0;
            ret += "\r\nrotateskew1 " + textMatrix.rotateSkew1;
        }
        ret += "\r\n]";
        for (TEXTRECORD rec : textRecords) {
            String params = "";
            if (rec.styleFlagsHasFont) {
                for (Tag t : tags) {
                    if (t instanceof FontTag) {
                        if (((FontTag) t).getFontId() == rec.fontId) {
                            fnt = ((FontTag) t);
                            break;
                        }
                    }
                }
                params += "\r\nfont " + rec.fontId + "\r\nheight " + rec.textHeight;
            }
            if (rec.styleFlagsHasColor) {
                params += "\r\ncolor " + rec.textColor.toHexRGB();
            }
            if (rec.styleFlagsHasXOffset) {
                params += "\r\nx " + rec.xOffset;
            }
            if (rec.styleFlagsHasYOffset) {
                params += "\r\ny " + rec.yOffset;
            }
            if (params.length() > 0) {
                ret += "[" + params + "\r\n]";
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
            List<TEXTRECORD> textRecords = new ArrayList<>();
            RGB color = null;
            int fontId = -1;
            int textHeight = -1;
            FontTag font = null;
            Integer x = null;
            Integer y = null;
            int currentX = 0;
            int currentY = 0;
            int glyphBits = 0;
            int advanceBits = 0;
            int maxX = Integer.MIN_VALUE;
            int minX = Integer.MAX_VALUE;
            MATRIX textMatrix = new MATRIX();
            textMatrix.hasRotate = false;
            textMatrix.hasScale = false;
            RECT textBounds = new RECT();
            while ((s = lexer.yylex()) != null) {
                switch (s.type) {
                    case PARAMETER:
                        String paramName = (String) s.values[0];
                        String paramValue = (String) s.values[1];
                        if (paramName.equals("color")) {
                            Matcher m = Pattern.compile("#([0-9a-f][0-9a-f])([0-9a-f][0-9a-f])([0-9a-f][0-9a-f])").matcher(paramValue);
                            if (m.matches()) {
                                color = new RGB(Integer.parseInt(m.group(1), 16), Integer.parseInt(m.group(2), 16), Integer.parseInt(m.group(3), 16));
                            } else {
                                throw new ParseException("Invalid color. Valid format is #rrggbb.", lexer.yyline());
                            }
                        } else if (paramName.equals("font")) {
                            try {
                                fontId = Integer.parseInt(paramValue);

                                for (Tag t : tags) {
                                    if (t instanceof FontTag) {
                                        if (((FontTag) t).getFontId() == fontId) {
                                            font = (FontTag) t;
                                            break;
                                        }
                                    }
                                }
                                if (font == null) {
                                    throw new ParseException("Font not found", lexer.yyline());
                                }
                            } catch (NumberFormatException nfe) {
                                throw new ParseException("Invalid font id - number expected.", lexer.yyline());
                            }
                        } else if (paramName.equals("height")) {
                            try {
                                textHeight = Integer.parseInt(paramValue);
                            } catch (NumberFormatException nfe) {
                                throw new ParseException("Invalid font height - number expected.", lexer.yyline());
                            }
                        } else if (paramName.equals("x")) {

                            try {
                                x = Integer.parseInt(paramValue);
                                currentX = x;
                            } catch (NumberFormatException nfe) {
                                throw new ParseException("Invalid x position - number expected.", lexer.yyline());
                            }
                        } else if (paramName.equals("y")) {

                            try {
                                y = Integer.parseInt(paramValue);
                                currentY = y;
                            } catch (NumberFormatException nfe) {
                                throw new ParseException("Invalid y position - number expected.", lexer.yyline());
                            }
                        } else if (paramName.equals("xmin")) {
                            try {
                                textBounds.Xmin = Integer.parseInt(paramValue);
                            } catch (NumberFormatException nfe) {
                                throw new ParseException("Invalid xmin position - number expected.", lexer.yyline());
                            }
                        } else if (paramName.equals("xmax")) {
                            try {
                                textBounds.Xmax = Integer.parseInt(paramValue);
                            } catch (NumberFormatException nfe) {
                                throw new ParseException("Invalid xmax position - number expected.", lexer.yyline());
                            }
                        } else if (paramName.equals("ymin")) {
                            try {
                                textBounds.Ymin = Integer.parseInt(paramValue);
                            } catch (NumberFormatException nfe) {
                                throw new ParseException("Invalid ymin position - number expected.", lexer.yyline());
                            }
                        } else if (paramName.equals("ymax")) {
                            try {
                                textBounds.Ymax = Integer.parseInt(paramValue);
                            } catch (NumberFormatException nfe) {
                                throw new ParseException("Invalid ymax position - number expected.", lexer.yyline());
                            }
                        } else if (paramName.equals("scalex")) {
                            try {
                                textMatrix.scaleX = Integer.parseInt(paramValue);
                                textMatrix.hasScale = true;
                            } catch (NumberFormatException nfe) {
                                throw new ParseException("Invalid scalex value - number expected.", lexer.yyline());
                            }
                        } else if (paramName.equals("scaley")) {
                            try {
                                textMatrix.scaleY = Integer.parseInt(paramValue);
                                textMatrix.hasScale = true;
                            } catch (NumberFormatException nfe) {
                                throw new ParseException("Invalid scalex value - number expected.", lexer.yyline());
                            }
                        } else if (paramName.equals("rotateskew0")) {
                            try {
                                textMatrix.rotateSkew0 = Integer.parseInt(paramValue);
                                textMatrix.hasRotate = true;
                            } catch (NumberFormatException nfe) {
                                throw new ParseException("Invalid rotateskew0 value - number expected.", lexer.yyline());
                            }
                        } else if (paramName.equals("rotateskew1")) {
                            try {
                                textMatrix.rotateSkew1 = Integer.parseInt(paramValue);
                                textMatrix.hasRotate = true;
                            } catch (NumberFormatException nfe) {
                                throw new ParseException("Invalid rotateskew1 value - number expected.", lexer.yyline());
                            }
                        } else if (paramName.equals("translatex")) {
                            try {
                                textMatrix.translateX = Integer.parseInt(paramValue);
                            } catch (NumberFormatException nfe) {
                                throw new ParseException("Invalid translatex value - number expected.", lexer.yyline());
                            }
                        } else if (paramName.equals("translatey")) {
                            try {
                                textMatrix.translateY = Integer.parseInt(paramValue);
                            } catch (NumberFormatException nfe) {
                                throw new ParseException("Invalid translatey value - number expected.", lexer.yyline());
                            }
                        }
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
                        if (color != null) {
                            tr.textColor = color;
                            tr.styleFlagsHasColor = true;
                            color = null;
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
                            if (!font.containsChar(tags, c)) {
                                font.addCharacter(tags, c);
                            }
                            tr.glyphEntries[i].glyphIndex = font.charToGlyph(tags, c);



                            /*if (tr.glyphEntries[i].glyphIndex == -1) {
                             throw new ParseException("Font does not contain glyph for character '" + c + "'", lexer.yyline());
                             }*/
                            tr.glyphEntries[i].glyphAdvance = textHeight * font.getGlyphAdvance(tr.glyphEntries[i].glyphIndex) / 1024;

                            int gw = textHeight * font.getGlyphWidth(tr.glyphEntries[i].glyphIndex) / 1024;
                            if (i == 0) {
                                currentX += gw;
                            } else {
                                currentX += tr.glyphEntries[i].glyphAdvance;
                            }
                            if (SWFOutputStream.getNeededBitsU(tr.glyphEntries[i].glyphIndex) > glyphBits) {
                                glyphBits = SWFOutputStream.getNeededBitsU(tr.glyphEntries[i].glyphIndex);
                            }
                            if (SWFOutputStream.getNeededBitsS(tr.glyphEntries[i].glyphAdvance) > advanceBits) {
                                advanceBits = SWFOutputStream.getNeededBitsS(tr.glyphEntries[i].glyphAdvance);
                            }

                        }
                        textRecords.add(tr);
                        if (currentX > maxX) {
                            maxX = currentX;
                        }
                        if (currentX < minX) {
                            minX = currentX;
                        }
                        break;
                }

            }
            this.advanceBits = advanceBits;
            this.glyphBits = glyphBits;
            this.textRecords = textRecords;
            this.textMatrix = textMatrix;
            this.textBounds = textBounds;
            //this.textBounds.Xmin = minX;
            //this.textBounds.Xmax = maxX;
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(DefineTextTag.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
        }
    }

    @Override
    public int getCharacterId() {
        return characterID;
    }

    public DefineTextTag(int characterID, RECT textBounds, MATRIX textMatrix, int glyphBits, int advanceBits, List<TEXTRECORD> textRecords) {
        super(ID, "DefineText", new byte[0], 0);
        this.characterID = characterID;
        this.textBounds = textBounds;
        this.textMatrix = textMatrix;
        this.glyphBits = glyphBits;
        this.advanceBits = advanceBits;
        this.textRecords = textRecords;
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
                sos.writeTEXTRECORD(tr, false, glyphBits, advanceBits);
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
     * @param pos
     * @throws IOException
     */
    public DefineTextTag(byte data[], int version, long pos) throws IOException {
        super(11, "DefineText", data, pos);
        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        characterID = sis.readUI16();
        textBounds = sis.readRECT();
        textMatrix = sis.readMatrix();
        glyphBits = sis.readUI8();
        advanceBits = sis.readUI8();
        textRecords = new ArrayList<>();
        TEXTRECORD tr;
        while ((tr = sis.readTEXTRECORD(false, glyphBits, advanceBits)) != null) {
            textRecords.add(tr);
        }
    }

    @Override
    public RECT getRect(HashMap<Integer, CharacterTag> characters, Stack<Integer> visited) {
        return textBounds;
    }

    @Override
    public Set<Integer> getNeededCharacters() {
        Set<Integer> ret = new HashSet<>();
        for (TEXTRECORD tr : textRecords) {
            if (tr.styleFlagsHasFont) {
                ret.add(tr.fontId);
            }
        }
        return ret;
    }

    @Override
    public BufferedImage toImage(int frame, List<Tag> tags, RECT displayRect, HashMap<Integer, CharacterTag> characters, Stack<Integer> visited) {
        RECT bound = getBounds();
        int fixX = -bound.Xmin;
        int fixY = -bound.Ymin;
        BufferedImage ret = new BufferedImage(bound.getWidth() / 20, bound.getHeight() / 20, BufferedImage.TYPE_INT_ARGB);

        Color textColor = new Color(0, 0, 0);
        FontTag font = null;
        int textHeight = 12;
        int x = bound.Xmin;
        int y = 0;
        Graphics2D g = (Graphics2D) ret.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        List<SHAPE> glyphs = new ArrayList<>();
        for (TEXTRECORD rec : textRecords) {
            if (rec.styleFlagsHasColor) {
                textColor = rec.textColor.toColor();
            }
            if (rec.styleFlagsHasFont) {
                font = (FontTag) characters.get(rec.fontId);
                glyphs = font.getGlyphShapeTable();
                textHeight = rec.textHeight;
            }
            if (rec.styleFlagsHasXOffset) {
                x = rec.xOffset * 1000 / textHeight;
            }
            if (rec.styleFlagsHasYOffset) {
                y = rec.yOffset * 1000 / textHeight;
            }

            for (GLYPHENTRY entry : rec.glyphEntries) {
                RECT rect = SHAPERECORD.getBounds(glyphs.get(entry.glyphIndex).shapeRecords);
                rect.Xmax /= font.getDivider();
                rect.Xmin /= font.getDivider();
                rect.Ymax /= font.getDivider();
                rect.Ymin /= font.getDivider();
                BufferedImage img = SHAPERECORD.shapeToImage(tags, 1, null, null, glyphs.get(entry.glyphIndex).shapeRecords, textColor);
                AffineTransform tr = new AffineTransform();
                tr.setToIdentity();
                float rat = textHeight / 1000f;
                tr.translate(rat * x / 20, rat * (y + rect.Ymin) / 20);
                tr.scale(rat / font.getDivider(), rat / font.getDivider());
                tr.translate(fixX, fixY);
                g.drawImage(img, tr, null);
                x += entry.glyphAdvance * 1000 / textHeight;
            }
        }
        return ret;
    }

    @Override
    public Point getImagePos(int frame, HashMap<Integer, CharacterTag> characters, Stack<Integer> visited) {
        return new Point(textBounds.Xmin / 20, textBounds.Ymin / 20);
    }

    @Override
    public int getNumFrames() {
        return 1;
    }
}
