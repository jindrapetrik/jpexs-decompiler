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
package com.jpexs.decompiler.flash.tags.base;

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
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.TEXTRECORD;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.SerializableImage;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author JPEXS
 */
public abstract class StaticTextTag extends TextTag {

    @SWFType(BasicType.UI16)
    public int characterID;

    protected int glyphBits;

    protected int advanceBits;

    public RECT textBounds;

    public MATRIX textMatrix;

    public List<TEXTRECORD> textRecords;

    public abstract int getTextNum();

    public StaticTextTag(SWF swf, int id, String name, ByteArrayRange data) {
        super(swf, id, name, data);
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
        while ((tr = sis.readTEXTRECORD(getTextNum(), glyphBits, advanceBits, "record")) != null) {
            textRecords.add(tr);
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

        if (Configuration._debugCopy.get()) {
            glyphBits = Math.max(glyphBits, this.glyphBits);
            advanceBits = Math.max(advanceBits, this.advanceBits);
        }

        sos.writeUI8(glyphBits);
        sos.writeUI8(advanceBits);
        for (TEXTRECORD tr : textRecords) {
            sos.writeTEXTRECORD(tr, getTextNum(), glyphBits, advanceBits);
        }
        sos.writeUI8(0);
    }

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
                FontTag fnt2 = swf.getFont(rec.fontId);
                if (fnt2 != null) {
                    fnt = fnt2;
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
    public RECT getRect(Set<BoundedTag> added) {
        return textBounds;
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
    public int getCharacterId() {
        return characterID;
    }

    @Override
    public void setCharacterId(int characterId) {
        this.characterID = characterId;
    }

    @Override
    public HighlightedText getFormattedText(boolean ignoreLetterSpacing) {
        FontTag fnt = null;
        HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), true);
        writer.append("[").newLine();
        writer.append("xmin ").append(textBounds.Xmin).newLine();
        writer.append("ymin ").append(textBounds.Ymin).newLine();
        writer.append("xmax ").append(textBounds.Xmax).newLine();
        writer.append("ymax ").append(textBounds.Ymax).newLine();
        if (textMatrix.translateX != 0) {
            writer.append("translatex ").append(textMatrix.translateX).newLine();
        }
        if (textMatrix.translateY != 0) {
            writer.append("translatey ").append(textMatrix.translateY).newLine();
        }
        if (textMatrix.hasScale) {
            writer.append("scalex ").append(textMatrix.scaleX).newLine();
            writer.append("scaley ").append(textMatrix.scaleY).newLine();
        }
        if (textMatrix.hasRotate) {
            writer.append("rotateskew0 ").append(textMatrix.rotateSkew0).newLine();
            writer.append("rotateskew1 ").append(textMatrix.rotateSkew1).newLine();
        }
        writer.append("]");
        int textHeight = 12;
        for (TEXTRECORD rec : textRecords) {
            if (rec.styleFlagsHasFont || rec.styleFlagsHasColor || rec.styleFlagsHasXOffset || rec.styleFlagsHasYOffset) {
                writer.append("[").newLine();
                if (rec.styleFlagsHasFont) {
                    FontTag fnt2 = swf.getFont(rec.fontId);
                    if (fnt2 != null) {
                        fnt = fnt2;
                    }
                    writer.append("font ").append(rec.fontId).newLine();
                    writer.append("height ").append(rec.textHeight).newLine();
                    textHeight = rec.textHeight;
                }
                if (fnt != null && !ignoreLetterSpacing) {
                    int letterSpacing = detectLetterSpacing(rec, fnt, textHeight);
                    if (letterSpacing != 0) {
                        writer.append("letterspacing ").append(letterSpacing).newLine();
                    }
                }
                if (rec.styleFlagsHasColor) {
                    if (getTextNum() == 1) {
                        writer.append("color ").append(rec.textColor.toHexRGB()).newLine();
                    } else {
                        writer.append("color ").append(rec.textColorA.toHexARGB()).newLine();
                    }
                }
                if (rec.styleFlagsHasXOffset) {
                    writer.append("x ").append(rec.xOffset).newLine();
                }
                if (rec.styleFlagsHasYOffset) {
                    writer.append("y ").append(rec.yOffset).newLine();
                }
                writer.append("]");
            }

            if (fnt == null) {
                writer.append(AppResources.translate("fontNotFound").replace("%fontId%", Integer.toString(rec.fontId)));
            } else {
                writer.hilightSpecial(Helper.escapeActionScriptString(rec.getText(fnt)).replace("[", "\\[").replace("]", "\\]"), HighlightSpecialType.TEXT);
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
            RGBA colorA = null;
            int fontId = -1;
            int textHeight = -1;
            int letterSpacing = 0;
            FontTag font = null;
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
                                if (getTextNum() == 1) {
                                    Matcher m = Pattern.compile("#([0-9a-f][0-9a-f])([0-9a-f][0-9a-f])([0-9a-f][0-9a-f])").matcher(paramValue);
                                    if (m.matches()) {
                                        color = new RGB(Integer.parseInt(m.group(1), 16), Integer.parseInt(m.group(2), 16), Integer.parseInt(m.group(3), 16));
                                    } else {
                                        throw new TextParseException("Invalid color. Valid format is #rrggbb. Found: " + paramValue, lexer.yyline());
                                    }
                                } else {
                                    Matcher m = Pattern.compile("#([0-9a-f][0-9a-f])([0-9a-f][0-9a-f])([0-9a-f][0-9a-f])([0-9a-f][0-9a-f])").matcher(paramValue);
                                    if (m.matches()) {
                                        colorA = new RGBA(Integer.parseInt(m.group(2), 16), Integer.parseInt(m.group(3), 16), Integer.parseInt(m.group(4), 16), Integer.parseInt(m.group(1), 16));
                                    } else {
                                        throw new TextParseException("Invalid color. Valid format is #aarrggbb. Found: " + paramValue, lexer.yyline());
                                    }
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
                            case "letterspacing":
                                try {
                                    letterSpacing = Integer.parseInt(paramValue);
                                } catch (NumberFormatException nfe) {
                                    throw new TextParseException("Invalid font letter spacing - number expected. Found: " + paramValue, lexer.yyline());
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
                        if (getTextNum() == 1) {
                            if (color != null) {
                                tr.textColor = color;
                                tr.styleFlagsHasColor = true;
                                color = null;
                            }
                        } else if (colorA != null) {
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
                        tr.glyphEntries = new ArrayList<>(txt.length());
                        for (int i = 0; i < txt.length(); i++) {
                            char c = txt.charAt(i);
                            Character nextChar = null;
                            if (i + 1 < txt.length()) {
                                nextChar = txt.charAt(i + 1);
                            }

                            GLYPHENTRY ge = new GLYPHENTRY();
                            ge.glyphIndex = font.charToGlyph(c);

                            int advance = getAdvance(font, ge.glyphIndex, textHeight, c, nextChar) + letterSpacing;
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

    private int getAdvance(FontTag font, int glyphIndex, int textHeight, char c, Character nextChar) {
        int advance;
        if (font.hasLayout()) {
            int kerningAdjustment = 0;
            if (nextChar != null) {
                kerningAdjustment = font.getCharKerningAdjustment(c, nextChar);
            }
            advance = (int) Math.round(((double) textHeight * (font.getGlyphAdvance(glyphIndex) + kerningAdjustment)) / (font.getDivider() * 1024.0));
        } else {
            String fontName = font.getSystemFontName();
            advance = (int) Math.round(SWF.unitDivisor * FontTag.getSystemFontAdvance(fontName, font.getFontStyle(), (int) (textHeight / SWF.unitDivisor), c, nextChar));
        }

        return advance;
    }

    private int detectLetterSpacing(TEXTRECORD textRecord, FontTag font, int textHeight) {
        int totalLetterSpacing = 0;
        List<GLYPHENTRY> glyphEntries = textRecord.glyphEntries;
        for (int i = 0; i < glyphEntries.size(); i++) {
            GLYPHENTRY glyph = glyphEntries.get(i);
            GLYPHENTRY nextGlyph = null;
            if (i + 1 < glyphEntries.size()) {
                nextGlyph = glyphEntries.get(i + 1);
            }

            char c = font.glyphToChar(glyph.glyphIndex);
            Character nextChar = nextGlyph == null ? null : font.glyphToChar(nextGlyph.glyphIndex);
            int advance = getAdvance(font, glyph.glyphIndex, textHeight, c, nextChar);
            int letterSpacing = glyph.glyphAdvance - advance;
            totalLetterSpacing += letterSpacing;
        }

        return (int) Math.round(totalLetterSpacing / glyphEntries.size());
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
    public boolean replaceCharacter(int oldCharacterId, int newCharacterId) {
        boolean modified = false;
        for (TEXTRECORD tr : textRecords) {
            if (tr.fontId == oldCharacterId) {
                tr.fontId = newCharacterId;
                modified = true;
            }
        }
        if (modified) {
            setModified(true);
        }
        return modified;
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
    public int getUsedParameters() {
        return 0;
    }

    @Override
    public void toImage(int frame, int time, int ratio, RenderContext renderContext, SerializableImage image, boolean isClip, Matrix transformation, Matrix strokeTransformation, Matrix absoluteTransformation, ColorTransform colorTransform) {
        staticTextToImage(swf, textRecords, getTextNum(), image, textMatrix, transformation, colorTransform);
        /*try {
         TextTag originalTag = (TextTag) getOriginalTag();
         if (isModified()) {
         originalTag.toImage(frame, time, ratio, renderContext, image, transformation, new ConstantColorColorTransform(0xFFC0C0C0));
         }
         staticTextToImage(swf, textRecords, getTextNum(), image, getTextMatrix(), transformation, new ConstantColorColorTransform(0xFF000000));
         } catch (InterruptedException | IOException ex) {
         Logger.getLogger(TextTag.class.getName()).log(Level.SEVERE, null, ex);
         }*/
    }

    @Override
    public void toSVG(SVGExporter exporter, int ratio, ColorTransform colorTransform, int level) {
        staticTextToSVG(swf, textRecords, getTextNum(), exporter, getRect(), textMatrix, colorTransform, 1);
    }

    @Override
    public void toHtmlCanvas(StringBuilder result, double unitDivisor) {
        staticTextToHtmlCanvas(unitDivisor, swf, textRecords, getTextNum(), result, textBounds, textMatrix, null);
    }
}
