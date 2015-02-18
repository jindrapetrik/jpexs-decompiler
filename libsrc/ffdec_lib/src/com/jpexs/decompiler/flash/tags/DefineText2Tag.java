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
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.commonshape.ExportRectangle;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.commonshape.SVGExporter;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.MissingCharacterHandler;
import com.jpexs.decompiler.flash.tags.base.RenderContext;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.tags.text.ParsedSymbol;
import com.jpexs.decompiler.flash.tags.text.TextAlign;
import com.jpexs.decompiler.flash.tags.text.TextLexer;
import com.jpexs.decompiler.flash.tags.text.TextParseException;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.GLYPHENTRY;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.TEXTRECORD;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.SerializableImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 *
 * @author JPEXS
 */
public class DefineText2Tag extends TextTag {

    @SWFType(BasicType.UI16)
    public int characterID;

    private final int glyphBits;

    private final int advanceBits;

    public RECT textBounds;

    public MATRIX textMatrix;

    public List<TEXTRECORD> textRecords;

    public static final int ID = 33;

    @Override
    public RECT getBounds() {
        return textBounds;
    }

    @Override
    public MATRIX getTextMatrix() {
        return textMatrix;
    }

    @Override
    public void setBounds(RECT r) {
        textBounds = r;
    }

    @Override
    public List<String> getTexts() {
        FontTag fnt = null;
        List<String> ret = new ArrayList<>();
        for (TEXTRECORD rec : textRecords) {
            if (rec.styleFlagsHasFont) {
                for (Tag t : swf.tags) {
                    if (t instanceof FontTag) {
                        if (((FontTag) t).getFontId() == rec.fontId) {
                            fnt = ((FontTag) t);
                            break;
                        }
                    }
                }
            }
            if (rec.styleFlagsHasXOffset || rec.styleFlagsHasYOffset) {
                /*if (!ret.isEmpty()) {
                 ret += "\r\n";
                 }*/
            }
            ret.add(rec.getText(fnt));
        }
        return ret;
    }

    @Override
    public List<Integer> getFontIds() {
        List<Integer> ret = new ArrayList<>();
        for (TEXTRECORD rec : textRecords) {
            if (rec.styleFlagsHasFont) {
                ret.add(rec.fontId);
            }
        }
        return ret;
    }

    @Override
    public String getFormattedText() {
        FontTag fnt = null;
        StringBuilder ret = new StringBuilder();
        ret.append("[\r\nxmin ").append(textBounds.Xmin).
                append("\r\nymin ").append(textBounds.Ymin).
                append("\r\nxmax ").append(textBounds.Xmax).
                append("\r\nymax ").append(textBounds.Ymax);
        if (textMatrix.translateX != 0) {
            ret.append("\r\ntranslatex ").append(textMatrix.translateX);
        }
        if (textMatrix.translateY != 0) {
            ret.append("\r\ntranslatey ").append(textMatrix.translateY);
        }
        if (textMatrix.hasScale) {
            ret.append("\r\nscalex ").append(textMatrix.scaleX);
            ret.append("\r\nscaley ").append(textMatrix.scaleY);
        }
        if (textMatrix.hasRotate) {
            ret.append("\r\nrotateskew0 ").append(textMatrix.rotateSkew0);
            ret.append("\r\nrotateskew1 ").append(textMatrix.rotateSkew1);
        }
        ret.append("\r\n]");
        for (TEXTRECORD rec : textRecords) {
            String params = "";
            if (rec.styleFlagsHasFont) {
                for (Tag t : swf.tags) {
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
                params += "\r\ncolor " + rec.textColorA.toHexARGB();
            }
            if (rec.styleFlagsHasXOffset) {
                params += "\r\nx " + rec.xOffset;
            }
            if (rec.styleFlagsHasYOffset) {
                params += "\r\ny " + rec.yOffset;
            }
            if (params.length() > 0) {
                ret.append("[").append(params).append("\r\n]");
            }
            ret.append(Helper.escapeString(rec.getText(fnt)).replace("[", "\\[").replace("]", "\\]"));
        }
        return ret.toString();
    }

    @Override
    public boolean setFormattedText(MissingCharacterHandler missingCharHandler, String formattedText, String[] texts) throws TextParseException {
        try {
            TextLexer lexer = new TextLexer(new StringReader(formattedText));
            ParsedSymbol s = null;
            List<TEXTRECORD> textRecords = new ArrayList<>();
            RGBA colorA = null;
            int fontId = -1;
            int textHeight = -1;
            FontTag font = null;
            String fontName = null;
            Integer x = null;
            Integer y = null;
            int currentX = 0;
            int currentY = 0;
            int maxX = Integer.MIN_VALUE;
            int minX = Integer.MAX_VALUE;
            MATRIX textMatrix = new MATRIX();
            textMatrix.hasRotate = false;
            textMatrix.hasScale = false;
            RECT textBounds = new RECT();
            int textIdx = 0;
            while ((s = lexer.yylex()) != null) {
                switch (s.type) {
                    case PARAMETER:
                        String paramName = (String) s.values[0];
                        String paramValue = (String) s.values[1];
                        switch (paramName) {
                            case "color":
                                Matcher m = Pattern.compile("#([0-9a-f][0-9a-f])([0-9a-f][0-9a-f])([0-9a-f][0-9a-f])([0-9a-f][0-9a-f])").matcher(paramValue);
                                if (m.matches()) {
                                    colorA = new RGBA(Integer.parseInt(m.group(2), 16), Integer.parseInt(m.group(3), 16), Integer.parseInt(m.group(4), 16), Integer.parseInt(m.group(1), 16));
                                } else {
                                    throw new TextParseException("Invalid color. Valid format is #aarrggbb. Found: " + paramValue, lexer.yyline());
                                }
                                break;
                            case "font":
                                try {
                                    fontId = Integer.parseInt(paramValue);

                                    CharacterTag characterTag = swf.getCharacter(fontId);
                                    if (characterTag == null) {
                                        throw new TextParseException("Font not found.", lexer.yyline());
                                    }

                                    if (!(characterTag instanceof FontTag)) {
                                        throw new TextParseException("Character tag is not a Font tag. CharacterID: " + fontId, lexer.yyline());
                                    }

                                    font = (FontTag) characterTag;
                                    fontName = font.getSystemFontName();
                                } catch (NumberFormatException nfe) {
                                    throw new TextParseException("Invalid font id - number expected. Found: " + paramValue, lexer.yyline());
                                }
                                break;
                            case "height":
                                try {
                                    textHeight = Integer.parseInt(paramValue);
                                } catch (NumberFormatException nfe) {
                                    throw new TextParseException("Invalid font height - number expected. Found: " + paramValue, lexer.yyline());
                                }
                                break;
                            case "x":
                                try {
                                    x = Integer.parseInt(paramValue);
                                    currentX = x;
                                } catch (NumberFormatException nfe) {
                                    throw new TextParseException("Invalid x position - number expected. Found: " + paramValue, lexer.yyline());
                                }
                                break;
                            case "y":
                                try {
                                    y = Integer.parseInt(paramValue);
                                    currentY = y;
                                } catch (NumberFormatException nfe) {
                                    throw new TextParseException("Invalid y position - number expected. Found: " + paramValue, lexer.yyline());
                                }
                                break;
                            case "xmin":
                                try {
                                    textBounds.Xmin = Integer.parseInt(paramValue);
                                } catch (NumberFormatException nfe) {
                                    throw new TextParseException("Invalid xmin position - number expected. Found: " + paramValue, lexer.yyline());
                                }
                                break;
                            case "xmax":
                                try {
                                    textBounds.Xmax = Integer.parseInt(paramValue);
                                } catch (NumberFormatException nfe) {
                                    throw new TextParseException("Invalid xmax position - number expected. Found: " + paramValue, lexer.yyline());
                                }
                                break;
                            case "ymin":
                                try {
                                    textBounds.Ymin = Integer.parseInt(paramValue);
                                } catch (NumberFormatException nfe) {
                                    throw new TextParseException("Invalid ymin position - number expected. Found: " + paramValue, lexer.yyline());
                                }
                                break;
                            case "ymax":
                                try {
                                    textBounds.Ymax = Integer.parseInt(paramValue);
                                } catch (NumberFormatException nfe) {
                                    throw new TextParseException("Invalid ymax position - number expected. Found: " + paramValue, lexer.yyline());
                                }
                                break;
                            case "scalex":
                                try {
                                    textMatrix.scaleX = Integer.parseInt(paramValue);
                                    textMatrix.hasScale = true;
                                } catch (NumberFormatException nfe) {
                                    throw new TextParseException("Invalid scalex value - number expected. Found: " + paramValue, lexer.yyline());
                                }
                                break;
                            case "scaley":
                                try {
                                    textMatrix.scaleY = Integer.parseInt(paramValue);
                                    textMatrix.hasScale = true;
                                } catch (NumberFormatException nfe) {
                                    throw new TextParseException("Invalid scalex value - number expected. Found: " + paramValue, lexer.yyline());
                                }
                                break;
                            case "rotateskew0":
                                try {
                                    textMatrix.rotateSkew0 = Integer.parseInt(paramValue);
                                    textMatrix.hasRotate = true;
                                } catch (NumberFormatException nfe) {
                                    throw new TextParseException("Invalid rotateskew0 value - number expected. Found: " + paramValue, lexer.yyline());
                                }
                                break;
                            case "rotateskew1":
                                try {
                                    textMatrix.rotateSkew1 = Integer.parseInt(paramValue);
                                    textMatrix.hasRotate = true;
                                } catch (NumberFormatException nfe) {
                                    throw new TextParseException("Invalid rotateskew1 value - number expected. Found: " + paramValue, lexer.yyline());
                                }
                                break;
                            case "translatex":
                                try {
                                    textMatrix.translateX = Integer.parseInt(paramValue);
                                } catch (NumberFormatException nfe) {
                                    throw new TextParseException("Invalid translatex value - number expected. Found: " + paramValue, lexer.yyline());
                                }
                                break;
                            case "translatey":
                                try {
                                    textMatrix.translateY = Integer.parseInt(paramValue);
                                } catch (NumberFormatException nfe) {
                                    throw new TextParseException("Invalid translatey value - number expected. Found: " + paramValue, lexer.yyline());
                                }
                                break;
                            default:
                                throw new TextParseException("Unrecognized parameter name: " + paramName, lexer.yyline());
                        }
                        break;
                    case TEXT:
                        String txt = (texts == null || textIdx >= texts.length) ? (String) s.values[0] : texts[textIdx++];
                        if (txt == null || (font == null && txt.isEmpty())) {
                            continue;
                        }

                        if (font == null) {
                            throw new TextParseException("Font not defined", lexer.yyline());
                        }

                        while (txt.charAt(0) == '\r' || txt.charAt(0) == '\n') {
                            txt = txt.substring(1);
                        }

                        while (txt.charAt(txt.length() - 1) == '\r' || txt.charAt(txt.length() - 1) == '\n') {
                            txt = txt.substring(0, txt.length() - 1);
                        }

                        StringBuilder txtSb = new StringBuilder();
                        for (int i = 0; i < txt.length(); i++) {
                            char c = txt.charAt(i);

                            if (!font.containsChar(c)) {
                                if (!missingCharHandler.handle(this, font, c)) {
                                    if (!missingCharHandler.getIgnoreMissingCharacters()) {
                                        return false;
                                    }
                                } else {
                                    return setFormattedText(missingCharHandler, formattedText, texts);
                                }
                            } else {
                                txtSb.append(c);
                            }
                        }

                        txt = txtSb.toString();

                        TEXTRECORD tr = new TEXTRECORD();
                        textRecords.add(tr);
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
                        tr.glyphEntries = new GLYPHENTRY[txt.length()];
                        for (int i = 0; i < txt.length(); i++) {
                            char c = txt.charAt(i);
                            Character nextChar = null;
                            if (i + 1 < txt.length()) {
                                nextChar = txt.charAt(i + 1);
                            }

                            tr.glyphEntries[i] = new GLYPHENTRY();
                            tr.glyphEntries[i].glyphIndex = font.charToGlyph(c);

                            int advance;
                            if (font.hasLayout()) {
                                int kerningAdjustment = 0;
                                if (nextChar != null) {
                                    kerningAdjustment = font.getCharKerningAdjustment(c, nextChar);
                                }
                                advance = (int) Math.round(((double) textHeight * (font.getGlyphAdvance(tr.glyphEntries[i].glyphIndex) + kerningAdjustment)) / (font.getDivider() * 1024.0));
                            } else {
                                advance = (int) Math.round(SWF.unitDivisor * FontTag.getSystemFontAdvance(fontName, font.getFontStyle(), (int) (textHeight / SWF.unitDivisor), c, nextChar));
                            }
                            tr.glyphEntries[i].glyphAdvance = advance;

                            currentX += advance;

                        }
                        if (currentX > maxX) {
                            maxX = currentX;
                        }
                        if (currentX < minX) {
                            minX = currentX;
                        }
                        break;
                }
            }

            setModified(true);
            this.textRecords = textRecords;
            this.textBounds = textBounds;
        } catch (IOException ex) {
            return false;
        } catch (TextParseException ex) {
            throw ex;
        }

        updateTextBounds(textBounds);
        return true;
    }

    @Override
    public boolean alignText(TextAlign textAlign) {
        int maxWidth = 0;
        for (TEXTRECORD tr : textRecords) {
            int width = tr.getTotalAdvance();

            if (width > maxWidth) {
                maxWidth = width;
            }
        }

        for (TEXTRECORD tr : textRecords) {
            int width = tr.getTotalAdvance();
            switch (textAlign) {
                case LEFT:
                    tr.xOffset = 0;
                    tr.styleFlagsHasXOffset = true;
                    break;
                case CENTER:
                    tr.xOffset = (maxWidth - width) / 2;
                    tr.styleFlagsHasXOffset = true;
                    break;
                case RIGHT:
                    tr.xOffset = maxWidth - width;
                    tr.styleFlagsHasXOffset = true;
                    break;
                case JUSTIFY:
                    tr.xOffset = 0;
                    tr.styleFlagsHasXOffset = true;
                    break;
            }
        }
        return true;
    }

    @Override
    public RECT getRect(Set<BoundedTag> added) {
        return textBounds;
    }

    @Override
    public int getCharacterId() {
        return characterID;
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
            sos.writeUI16(characterID);
            sos.writeRECT(textBounds);
            sos.writeMatrix(textMatrix);

            int glyphBits = 0;
            int advanceBits = 0;
            for (TEXTRECORD tr : textRecords) {
                for (GLYPHENTRY ge : tr.glyphEntries) {
                    glyphBits = SWFOutputStream.enlargeBitCountU(glyphBits, ge.glyphIndex);
                    advanceBits = SWFOutputStream.enlargeBitCountS(advanceBits, ge.glyphAdvance);
                }
            }

            if (Configuration.debugCopy.get()) {
                glyphBits = Math.max(glyphBits, this.glyphBits);
                advanceBits = Math.max(advanceBits, this.advanceBits);
            }

            sos.writeUI8(glyphBits);
            sos.writeUI8(advanceBits);
            for (TEXTRECORD tr : textRecords) {
                sos.writeTEXTRECORD(tr, true, glyphBits, advanceBits);
            }
            sos.writeUI8(0);
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
    public DefineText2Tag(SWF swf) {
        super(swf, ID, "DefineText2", null);
        characterID = swf.getNextCharacterId();
        textBounds = new RECT();
        textMatrix = new MATRIX();
        textRecords = new ArrayList<>();
        glyphBits = 0;
        advanceBits = 0;
    }

    /**
     * Constructor
     *
     * @param sis
     * @param data
     * @throws IOException
     */
    public DefineText2Tag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, "DefineText2", data);
        characterID = sis.readUI16("characterID");
        textBounds = sis.readRECT("textBounds");
        textMatrix = sis.readMatrix("textMatrix");
        glyphBits = sis.readUI8("glyphBits");
        advanceBits = sis.readUI8("advanceBits");
        textRecords = new ArrayList<>();
        TEXTRECORD tr;
        while ((tr = sis.readTEXTRECORD(true, glyphBits, advanceBits, "record")) != null) {
            textRecords.add(tr);
        }
    }

    @Override
    public void getNeededCharacters(Set<Integer> needed) {
        for (TEXTRECORD tr : textRecords) {
            if (tr.styleFlagsHasFont) {
                needed.add(tr.fontId);
            }
        }
    }

    @Override
    public boolean removeCharacter(int characterId) {
        boolean modified = false;
        for (TEXTRECORD tr : textRecords) {
            if (tr.fontId == characterId) {
                tr.styleFlagsHasFont = false;
                tr.fontId = 0;
                modified = true;
            }
        }
        if (modified) {
            setModified(true);
        }
        return modified;
    }

    @Override
    public void toImage(int frame, int time, int ratio, RenderContext renderContext, SerializableImage image, Matrix transformation, ColorTransform colorTransform) {
        staticTextToImage(swf, textRecords, 2, image, getTextMatrix(), transformation, colorTransform);
        /*try {
         DefineText2Tag originalTag = (DefineText2Tag) getOriginalTag();
         if (isModified()) {
         originalTag.toImage(frame, time, ratio, stateUnderCursor, mouseButton, image, transformation, new ConstantColorColorTransform(0xFFC0C0C0));
         }
         staticTextToImage(swf, textRecords, 2, image, getTextMatrix(), transformation, new ConstantColorColorTransform(0xFF000000));
         } catch (InterruptedException | IOException ex) {
         Logger.getLogger(DefineText2Tag.class.getName()).log(Level.SEVERE, null, ex);
         }*/
    }

    @Override
    public void toSVG(SVGExporter exporter, int ratio, ColorTransform colorTransform, int level, double zoom) {
        staticTextToSVG(swf, textRecords, 2, exporter, getRect(), getTextMatrix(), colorTransform, zoom);
    }

    @Override
    public ExportRectangle calculateTextBounds() {
        return calculateTextBounds(swf, textRecords, getTextMatrix());
    }

    @Override
    public int getNumFrames() {
        return 1;
    }

    @Override
    public boolean isSingleFrame() {
        return true;
    }

    @Override
    public String toHtmlCanvas(double unitDivisor) {
        return staticTextToHtmlCanvas(unitDivisor, swf, textRecords, 2, textBounds, textMatrix, new ColorTransform());
    }
}
