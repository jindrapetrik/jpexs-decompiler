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

import com.jpexs.decompiler.flash.AppResources;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.commonshape.ExportRectangle;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.commonshape.SVGExporter;
import com.jpexs.decompiler.flash.helpers.HighlightedText;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightSpecialType;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
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
import com.jpexs.decompiler.flash.types.RGB;
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
public class DefineTextTag extends TextTag {

    @SWFType(BasicType.UI16)
    public int characterID;

    private int glyphBits;

    private int advanceBits;

    public RECT textBounds;

    public MATRIX textMatrix;

    public List<TEXTRECORD> textRecords;

    public static final int ID = 11;

    @Override
    public MATRIX getTextMatrix() {
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

            if (fnt == null) {
                ret.add(AppResources.translate("fontNotFound").replace("%fontId%", Integer.toString(rec.fontId)));
            } else {
                ret.add(rec.getText(fnt));
            }
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
    public HighlightedText getFormattedText() {
        FontTag fnt = null;
        HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), true);
        writer.append("[").newLine();
        writer.append("xmin " + textBounds.Xmin).newLine();
        writer.append("ymin " + textBounds.Ymin).newLine();
        writer.append("xmax " + textBounds.Xmax).newLine();
        writer.append("ymax " + textBounds.Ymax).newLine();
        if (textMatrix.translateX != 0) {
            writer.append("translatex " + textMatrix.translateX).newLine();
        }
        if (textMatrix.translateY != 0) {
            writer.append("translatey " + textMatrix.translateY).newLine();
        }
        if (textMatrix.hasScale) {
            writer.append("scalex " + textMatrix.scaleX).newLine();
            writer.append("scaley " + textMatrix.scaleY).newLine();
        }
        if (textMatrix.hasRotate) {
            writer.append("rotateskew0 " + textMatrix.rotateSkew0).newLine();
            writer.append("rotateskew1 " + textMatrix.rotateSkew1).newLine();
        }
        writer.append("]");
        for (TEXTRECORD rec : textRecords) {
            if (rec.styleFlagsHasFont || rec.styleFlagsHasColor || rec.styleFlagsHasXOffset || rec.styleFlagsHasYOffset) {
                writer.append("[").newLine();
                if (rec.styleFlagsHasFont) {
                    for (Tag t : swf.tags) {
                        if (t instanceof FontTag) {
                            if (((FontTag) t).getFontId() == rec.fontId) {
                                fnt = ((FontTag) t);
                                break;
                            }
                        }
                    }
                    writer.append("font " + rec.fontId).newLine();
                    writer.append("height " + rec.textHeight).newLine();
                }
                if (rec.styleFlagsHasColor) {
                    writer.append("color " + rec.textColor.toHexRGB()).newLine();
                }
                if (rec.styleFlagsHasXOffset) {
                    writer.append("x " + rec.xOffset).newLine();
                }
                if (rec.styleFlagsHasYOffset) {
                    writer.append("y " + rec.yOffset).newLine();
                }
                writer.append("]");
            }

            if (fnt == null) {
                writer.append(AppResources.translate("fontNotFound").replace("%fontId%", Integer.toString(rec.fontId)));
            } else {
                writer.hilightSpecial(Helper.escapeString(rec.getText(fnt)).replace("[", "\\[").replace("]", "\\]"), HighlightSpecialType.TEXT);
            }
        }
        return new HighlightedText(writer);
    }

    @Override
    public boolean setFormattedText(MissingCharacterHandler missingCharHandler, String formattedText, String[] texts) throws TextParseException {
        try {
            TextLexer lexer = new TextLexer(new StringReader(formattedText));
            ParsedSymbol s = null;
            List<TEXTRECORD> textRecords = new ArrayList<>();
            RGB color = null;
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
                                Matcher m = Pattern.compile("#([0-9a-f][0-9a-f])([0-9a-f][0-9a-f])([0-9a-f][0-9a-f])").matcher(paramValue);
                                if (m.matches()) {
                                    color = new RGB(Integer.parseInt(m.group(1), 16), Integer.parseInt(m.group(2), 16), Integer.parseInt(m.group(3), 16));
                                } else {
                                    throw new TextParseException("Invalid color. Valid format is #rrggbb. Found: " + paramValue, lexer.yyline());
                                }
                                break;
                            case "font":
                                try {
                                    fontId = Integer.parseInt(paramValue);

                                    FontTag ft = swf.getFont(fontId);
                                    if (ft == null) {
                                        throw new TextParseException("Font not found.", lexer.yyline());
                                    }

                                    font = (FontTag) ft;
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
                        tr.glyphEntries = new ArrayList<>(txt.length());
                        for (int i = 0; i < txt.length(); i++) {
                            char c = txt.charAt(i);
                            Character nextChar = null;
                            if (i + 1 < txt.length()) {
                                nextChar = txt.charAt(i + 1);
                            }

                            GLYPHENTRY ge = new GLYPHENTRY();
                            ge.glyphIndex = font.charToGlyph(c);

                            int advance;
                            if (font.hasLayout()) {
                                int kerningAdjustment = 0;
                                if (nextChar != null) {
                                    kerningAdjustment = font.getCharKerningAdjustment(c, nextChar);
                                }
                                advance = (int) Math.round(((double) textHeight * (font.getGlyphAdvance(ge.glyphIndex) + kerningAdjustment)) / (font.getDivider() * 1024.0));
                            } else {
                                advance = (int) Math.round(SWF.unitDivisor * FontTag.getSystemFontAdvance(fontName, font.getFontStyle(), (int) (textHeight / SWF.unitDivisor), c, nextChar));
                            }

                            ge.glyphAdvance = advance;
                            tr.glyphEntries.add(ge);

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
            this.textMatrix = textMatrix;
            this.textBounds = textBounds;
        } catch (IOException ex) {
            return false;
        } catch (TextParseException ex) {
            throw ex;
        }

        updateTextBounds();
        return true;
    }

    @Override
    public void updateTextBounds() {
        updateTextBounds(textBounds);
    }

    @Override
    public boolean alignText(TextAlign textAlign) {
        alignText(swf, textRecords, textAlign);
        setModified(true);
        return true;
    }

    @Override
    public boolean translateText(int diff) {
        textMatrix.translateX += diff;
        updateTextBounds();
        setModified(true);
        return true;
    }

    @Override
    public int getCharacterId() {
        return characterID;
    }

    /**
     * Constructor
     *
     * @param swf
     */
    public DefineTextTag(SWF swf) {
        super(swf, ID, "DefineText", null);
        characterID = swf.getNextCharacterId();
        textBounds = new RECT();
        textMatrix = new MATRIX();
        textRecords = new ArrayList<>();
        glyphBits = 0;
        advanceBits = 0;
    }

    public DefineTextTag(SWF swf, int characterID, RECT textBounds, MATRIX textMatrix, List<TEXTRECORD> textRecords) {
        super(swf, ID, "DefineText", null);
        this.characterID = characterID;
        this.textBounds = textBounds;
        this.textMatrix = textMatrix;
        this.textRecords = textRecords;
        this.glyphBits = 0;
        this.advanceBits = 0;
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
                sos.writeTEXTRECORD(tr, false, glyphBits, advanceBits);
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
     * @param sis
     * @param data
     * @throws IOException
     */
    public DefineTextTag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, "DefineText", data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        characterID = sis.readUI16("characterID");
        textBounds = sis.readRECT("textBounds");
        textMatrix = sis.readMatrix("textMatrix");
        glyphBits = sis.readUI8("glyphBits");
        advanceBits = sis.readUI8("advanceBits");
        textRecords = new ArrayList<>();
        TEXTRECORD tr;
        while ((tr = sis.readTEXTRECORD(false, glyphBits, advanceBits, "record")) != null) {
            textRecords.add(tr);
        }
    }

    @Override
    public RECT getRect(Set<BoundedTag> added) {
        return textBounds;
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
        staticTextToImage(swf, textRecords, 1, image, getTextMatrix(), transformation, colorTransform);
        /*try {
         DefineTextTag originalTag = (DefineTextTag) getOriginalTag();
         if (isModified()) {
         originalTag.toImage(frame, time, ratio, stateUnderCursor, mouseButton, image, transformation, new ConstantColorColorTransform(0xFFC0C0C0));
         }
         staticTextToImage(swf, textRecords, 1, image, getTextMatrix(), transformation, new ConstantColorColorTransform(0xFF000000));
         } catch (InterruptedException | IOException ex) {
         Logger.getLogger(DefineTextTag.class.getName()).log(Level.SEVERE, null, ex);
         }*/
    }

    @Override
    public void toSVG(SVGExporter exporter, int ratio, ColorTransform colorTransform, int level, double zoom) {
        staticTextToSVG(swf, textRecords, 1, exporter, getRect(), getTextMatrix(), colorTransform, zoom);
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
        return staticTextToHtmlCanvas(unitDivisor, swf, textRecords, 1, textBounds, textMatrix, new ColorTransform());
    }
}
