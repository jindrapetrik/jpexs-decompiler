/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.tags.base;

import com.jpexs.decompiler.flash.AppResources;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.RequiresNormalizedFonts;
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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base class for static text tags.
 *
 * @author JPEXS
 */
public abstract class StaticTextTag extends TextTag {

    /**
     * Character ID
     */
    @SWFType(BasicType.UI16)
    public int characterID;

    /**
     * Glyph bits
     */
    protected int glyphBits;

    /**
     * Advance bits
     */
    protected int advanceBits;

    /**
     * Text bounds
     */
    public RECT textBounds;

    /**
     * Text matrix
     */
    public MATRIX textMatrix;

    /**
     * Text records
     */
    public List<TEXTRECORD> textRecords;

    /**
     * Gets text number. DefineText = 1, DefineText2 = 2
     *
     * @return Text num
     */
    public abstract int getTextNum();

    /**
     * Constructor.
     *
     * @param swf SWF
     * @param id ID
     * @param name Name
     * @param data Data
     */
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

    @Override
    public void insertCharacterGlyph(int glyphPos, char character) {
    }

    @Override
    public void removeCharacterGlyph(int glyphPos) {
    }    
    
    /**
     * Gets data bytes
     *
     * @param sos SWF output stream
     * @throws IOException On I/O error
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
            writer.append("scalexf ").append(textMatrix.scaleX).newLine();
            writer.append("scaleyf ").append(textMatrix.scaleY).newLine();
        }
        if (textMatrix.hasRotate) {
            writer.append("rotateskew0f ").append(textMatrix.rotateSkew0).newLine();
            writer.append("rotateskew1f ").append(textMatrix.rotateSkew1).newLine();
        }
        writer.append("]");
        int textHeight = 12;
        int prevLetterSpacing = 0;
        for (TEXTRECORD rec : textRecords) {
            int letterSpacing = 0;
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
                    letterSpacing = detectLetterSpacing(rec, fnt, textHeight);
                    if (letterSpacing != prevLetterSpacing) {
                        writer.append("letterspacing ").append(letterSpacing).newLine();
                    }
                    prevLetterSpacing = letterSpacing;
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
            }

            if (fnt == null) {
                writer.append(AppResources.translate("fontNotFound").replace("%fontId%", Integer.toString(rec.fontId)));
            } else {

                boolean first = true;
                Map<String, Integer> spacing = new LinkedHashMap<>();
                Map<String, Integer> spacingCount = new LinkedHashMap<>();
                Set<String> ignoredSpacings = new LinkedHashSet<>();
                Map<Character, Set<Integer>> charToSpacing = new LinkedHashMap<>();
                Set<Character> noSpacing = new LinkedHashSet<>();
                Map<Character, Integer> charCount = new LinkedHashMap<>();
                if (rec.glyphEntries.size() > 1) {
                    for (int i = 0; i < rec.glyphEntries.size(); i++) {
                        GLYPHENTRY ge = rec.glyphEntries.get(i);
                        char c = fnt.glyphToChar(ge.glyphIndex);
                        Character nextChar = null;
                        if (i + 1 < rec.glyphEntries.size()) {
                            GLYPHENTRY nge = rec.glyphEntries.get(i + 1);
                            nextChar = fnt.glyphToChar(nge.glyphIndex);
                        }
                        int advance = getAdvance(fnt, ge.glyphIndex, textHeight, c, nextChar);
                        int delta = ge.glyphAdvance - advance;
                        String spacingKey = "" + c + (nextChar == null ? "" : nextChar);
                        int spacingVal = delta - letterSpacing;

                        if (!charToSpacing.containsKey(c)) {
                            charToSpacing.put(c, new LinkedHashSet<>());
                        }
                        charToSpacing.get(c).add(spacingVal);

                        if (!charCount.containsKey(c)) {
                            charCount.put(c, 0);
                        }
                        charCount.put(c, charCount.get(c) + 1);

                        
                        if (delta != letterSpacing && !ignoreLetterSpacing) {
                            if (ignoredSpacings.contains(spacingKey)) {
                                continue;
                            }
                            if (spacing.containsKey(spacingKey) && spacing.get(spacingKey) != spacingVal) {
                                spacing.remove(spacingKey);
                                spacingCount.remove(spacingKey);
                                ignoredSpacings.add(spacingKey);
                                continue;
                            }
                            spacing.put(spacingKey, spacingVal);
                            if (!spacingCount.containsKey(spacingKey)) {
                                spacingCount.put(spacingKey, 0);
                            }
                            spacingCount.put(spacingKey, spacingCount.get(spacingKey) + 1);
                        } else {
                            noSpacing.add(c);
                        }
                    }
                }
                List<String> spacingKeys = new ArrayList<>(spacing.keySet());
                Map<Character, Integer> simpleSpacing = new LinkedHashMap<>();
                for (String spacingKey : spacingKeys) {
                    Character c = spacingKey.charAt(0);
                    if (charToSpacing.get(c).size() == 1 && charCount.get(c) > 1) {
                        spacing.remove(spacingKey);
                        simpleSpacing.put(c, charToSpacing.get(c).iterator().next());
                    }
                }

                if (!(rec.styleFlagsHasFont || rec.styleFlagsHasColor || rec.styleFlagsHasXOffset || rec.styleFlagsHasYOffset)
                        && (!simpleSpacing.isEmpty() || !spacing.isEmpty())) {
                    writer.append("[").newLine().append("resetspacing").newLine();
                }

                if (!simpleSpacing.isEmpty()) {
                    //writer.append("[").newLine();
                    for (Character c : simpleSpacing.keySet()) {
                        writer.append("spacing \"").append(Helper.escapeString("" + c)).append("\"");
                        writer.append(" ");
                        writer.append(simpleSpacing.get(c));
                        writer.newLine();
                    }
                    //writer.append("]");
                }

                if (!spacing.isEmpty()) {
                    //writer.append("[").newLine();

                    for (String spacingKey : spacing.keySet()) {
                        writer.append("spacingpair \"").append(Helper.escapeString("" + spacingKey.charAt(0))).append("\"");
                        writer.append(" ");
                        if (spacingKey.length() > 1) {
                            writer.append("\"").append(spacingKey.charAt(1)).append("\"");
                        } else {
                            writer.append("\"\"");
                        }
                        writer.append(" ");
                        writer.append(spacing.get(spacingKey));
                        writer.newLine();
                    }

                    //writer.append("]");
                }

                if (rec.styleFlagsHasFont || rec.styleFlagsHasColor || rec.styleFlagsHasXOffset || rec.styleFlagsHasYOffset
                        || !simpleSpacing.isEmpty() || !spacing.isEmpty()) {
                    writer.append("]");
                }

                for (int i = 0; i < rec.glyphEntries.size(); i++) {
                    GLYPHENTRY ge = rec.glyphEntries.get(i);
                    char c = fnt.glyphToChar(ge.glyphIndex);
                    String sc = ("" + c).replace("[", "\\[").replace("]", "\\]");
                    writer.hilightSpecial(sc, HighlightSpecialType.TEXT);

                    if (!ignoreLetterSpacing) {
                        Character nextChar = null;
                        if (i + 1 < rec.glyphEntries.size()) {
                            GLYPHENTRY nge = rec.glyphEntries.get(i + 1);
                            nextChar = fnt.glyphToChar(nge.glyphIndex);
                        }

                        String spacingKey = "" + c + (nextChar == null ? "" : nextChar);
                        if (!spacing.containsKey(spacingKey) && !simpleSpacing.containsKey(c)) {
                            int advance = getAdvance(fnt, ge.glyphIndex, textHeight, c, nextChar);
                            int delta = ge.glyphAdvance - advance;
                            if (delta != letterSpacing && !ignoreLetterSpacing) {
                                writer.append("[space " + (delta - letterSpacing) + "]");
                            }
                        }
                    }
                }
            }
        }
        writer.finishHilights();
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
            Map<Character, Integer> simpleSpacing = new LinkedHashMap<>();
            Map<String, Integer> pairSpacing = new LinkedHashMap<>();
            boolean append = false;
            while ((s = lexer.yylex()) != null) {
                switch (s.type) {
                    case PARAMETER_IDENTIFIER:
                        String paramName = (String) s.value;

                        switch (paramName) {
                            case "font":
                            case "height":
                            case "letterspacing":
                            case "color":
                            case "x":
                            case "y":
                            case "resetspacing":
                                simpleSpacing.clear();
                                pairSpacing.clear();
                        }

                        if (!paramName.equals("space")) {
                            append = false;
                        }

                        String paramValue;
                        switch (paramName) {
                            case "resetspacing":
                                break;
                            case "color":
                                s = lexer.yylex();
                                paramValue = (String) s.value;
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
                                s = lexer.yylex();
                                paramValue = (String) s.value;
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
                                s = lexer.yylex();
                                paramValue = (String) s.value;
                                try {
                                    textHeight = Integer.parseInt(paramValue);
                                } catch (NumberFormatException nfe) {
                                    throw new TextParseException("Invalid font height - number expected. Found: " + paramValue, lexer.yyline());
                                }
                                break;
                            case "letterspacing":
                                s = lexer.yylex();
                                paramValue = (String) s.value;
                                try {
                                    letterSpacing = Integer.parseInt(paramValue);
                                } catch (NumberFormatException nfe) {
                                    throw new TextParseException("Invalid font letter spacing - number expected. Found: " + paramValue, lexer.yyline());
                                }
                                break;
                            case "x":
                                s = lexer.yylex();
                                paramValue = (String) s.value;
                                try {
                                    x = Integer.parseInt(paramValue);
                                    currentX = x;
                                } catch (NumberFormatException nfe) {
                                    throw new TextParseException("Invalid x position - number expected. Found: " + paramValue, lexer.yyline());
                                }
                                break;
                            case "y":
                                s = lexer.yylex();
                                paramValue = (String) s.value;
                                try {
                                    y = Integer.parseInt(paramValue);
                                    currentY = y;
                                } catch (NumberFormatException nfe) {
                                    throw new TextParseException("Invalid y position - number expected. Found: " + paramValue, lexer.yyline());
                                }
                                break;
                            case "xmin":
                                s = lexer.yylex();
                                paramValue = (String) s.value;
                                try {
                                    textBounds.Xmin = Integer.parseInt(paramValue);
                                } catch (NumberFormatException nfe) {
                                    throw new TextParseException("Invalid xmin position - number expected. Found: " + paramValue, lexer.yyline());
                                }
                                break;
                            case "xmax":
                                s = lexer.yylex();
                                paramValue = (String) s.value;
                                try {
                                    textBounds.Xmax = Integer.parseInt(paramValue);
                                } catch (NumberFormatException nfe) {
                                    throw new TextParseException("Invalid xmax position - number expected. Found: " + paramValue, lexer.yyline());
                                }
                                break;
                            case "ymin":
                                s = lexer.yylex();
                                paramValue = (String) s.value;
                                try {
                                    textBounds.Ymin = Integer.parseInt(paramValue);
                                } catch (NumberFormatException nfe) {
                                    throw new TextParseException("Invalid ymin position - number expected. Found: " + paramValue, lexer.yyline());
                                }
                                break;
                            case "ymax":
                                s = lexer.yylex();
                                paramValue = (String) s.value;
                                try {
                                    textBounds.Ymax = Integer.parseInt(paramValue);
                                } catch (NumberFormatException nfe) {
                                    throw new TextParseException("Invalid ymax position - number expected. Found: " + paramValue, lexer.yyline());
                                }
                                break;
                            case "scalex":
                                s = lexer.yylex();
                                paramValue = (String) s.value;
                                try {
                                    textMatrix.scaleX = MATRIX.toFloat(Integer.parseInt(paramValue));
                                    textMatrix.hasScale = true;
                                } catch (NumberFormatException nfe) {
                                    throw new TextParseException("Invalid scalex value - number expected. Found: " + paramValue, lexer.yyline());
                                }
                                break;
                            case "scaley":
                                s = lexer.yylex();
                                paramValue = (String) s.value;
                                try {
                                    textMatrix.scaleY = MATRIX.toFloat(Integer.parseInt(paramValue));
                                    textMatrix.hasScale = true;
                                } catch (NumberFormatException nfe) {
                                    throw new TextParseException("Invalid scaley value - number expected. Found: " + paramValue, lexer.yyline());
                                }
                                break;

                            case "scalexf":
                                s = lexer.yylex();
                                paramValue = (String) s.value;
                                try {
                                    textMatrix.scaleX = Float.parseFloat(paramValue);
                                    textMatrix.hasScale = true;
                                } catch (NumberFormatException nfe) {
                                    throw new TextParseException("Invalid scalexf value - float number expected. Found: " + paramValue, lexer.yyline());
                                }
                                break;
                            case "scaleyf":
                                s = lexer.yylex();
                                paramValue = (String) s.value;
                                try {
                                    textMatrix.scaleY = Float.parseFloat(paramValue);
                                    textMatrix.hasScale = true;
                                } catch (NumberFormatException nfe) {
                                    throw new TextParseException("Invalid scaleyf value - float number expected. Found: " + paramValue, lexer.yyline());
                                }
                                break;

                            case "rotateskew0":
                                s = lexer.yylex();
                                paramValue = (String) s.value;
                                try {
                                    textMatrix.rotateSkew0 = MATRIX.toFloat(Integer.parseInt(paramValue));
                                    textMatrix.hasRotate = true;
                                } catch (NumberFormatException nfe) {
                                    throw new TextParseException("Invalid rotateskew0 value - number expected. Found: " + paramValue, lexer.yyline());
                                }
                                break;
                            case "rotateskew1":
                                s = lexer.yylex();
                                paramValue = (String) s.value;
                                try {
                                    textMatrix.rotateSkew1 = MATRIX.toFloat(Integer.parseInt(paramValue));
                                    textMatrix.hasRotate = true;
                                } catch (NumberFormatException nfe) {
                                    throw new TextParseException("Invalid rotateskew1 value - number expected. Found: " + paramValue, lexer.yyline());
                                }
                                break;
                            case "rotateskew0f":
                                s = lexer.yylex();
                                paramValue = (String) s.value;
                                try {
                                    textMatrix.rotateSkew0 = Float.parseFloat(paramValue);
                                    textMatrix.hasRotate = true;
                                } catch (NumberFormatException nfe) {
                                    throw new TextParseException("Invalid rotateskew0 value - float number expected. Found: " + paramValue, lexer.yyline());
                                }
                                break;
                            case "rotateskew1f":
                                s = lexer.yylex();
                                paramValue = (String) s.value;
                                try {
                                    textMatrix.rotateSkew1 = Float.parseFloat(paramValue);
                                    textMatrix.hasRotate = true;
                                } catch (NumberFormatException nfe) {
                                    throw new TextParseException("Invalid rotateskew1 value - float number expected. Found: " + paramValue, lexer.yyline());
                                }
                                break;
                            case "translatex":
                                s = lexer.yylex();
                                paramValue = (String) s.value;
                                try {
                                    textMatrix.translateX = Integer.parseInt(paramValue);
                                } catch (NumberFormatException nfe) {
                                    throw new TextParseException("Invalid translatex value - number expected. Found: " + paramValue, lexer.yyline());
                                }
                                break;
                            case "translatey":
                                s = lexer.yylex();
                                paramValue = (String) s.value;
                                try {
                                    textMatrix.translateY = Integer.parseInt(paramValue);
                                } catch (NumberFormatException nfe) {
                                    throw new TextParseException("Invalid translatey value - number expected. Found: " + paramValue, lexer.yyline());
                                }
                                break;
                            case "space":
                                s = lexer.yylex();
                                paramValue = (String) s.value;
                                try {
                                    int space = Integer.parseInt(paramValue);
                                    if (textRecords.isEmpty()) {
                                        throw new TextParseException("space parameter must be placed after some text", lexer.yyline());
                                    }
                                    TEXTRECORD lastRecord = textRecords.get(textRecords.size() - 1);
                                    if (!lastRecord.glyphEntries.isEmpty()) {
                                        lastRecord.glyphEntries.get(lastRecord.glyphEntries.size() - 1).glyphAdvance += space;
                                    }
                                } catch (NumberFormatException nfe) {
                                    throw new TextParseException("Invalid space value - number expected. Found: " + paramValue, lexer.yyline());
                                }
                                break;
                            case "spacing":
                                s = lexer.yylex();
                                String spacingChar = (String) s.value;
                                if (spacingChar.length() != 1) {
                                    throw new TextParseException("Invalid spacing character - single character expected. Found: " + (String) s.value, lexer.yyline());
                                }
                                s = lexer.yylex();
                                try {
                                    int space = Integer.parseInt((String) s.value);
                                    simpleSpacing.put(spacingChar.charAt(0), space);
                                } catch (NumberFormatException nfe) {
                                    throw new TextParseException("Invalid spacing value - number expected. Found: " + (String) s.value, lexer.yyline());
                                }
                                break;
                            case "spacingpair":
                                s = lexer.yylex();
                                String spacingChar1 = (String) s.value;
                                if (spacingChar1.length() != 1) {
                                    throw new TextParseException("Invalid spacing character1 - single character expected. Found: " + (String) s.value, lexer.yyline());
                                }
                                s = lexer.yylex();
                                String spacingChar2 = (String) s.value;
                                if (spacingChar2.length() > 1) {
                                    throw new TextParseException("Invalid spacing character2 - single character expected. Found: " + (String) s.value, lexer.yyline());
                                }
                                s = lexer.yylex();
                                try {
                                    int space = Integer.parseInt((String) s.value);
                                    pairSpacing.put(spacingChar1 + spacingChar2, space);
                                } catch (NumberFormatException nfe) {
                                    throw new TextParseException("Invalid spacing value - number expected. Found: " + (String) s.value, lexer.yyline());
                                }
                                break;
                            default:
                                throw new TextParseException("Unrecognized parameter name: " + paramName, lexer.yyline());
                        }
                        break;
                    case TEXT:
                        String txt = (texts == null || textIdx >= texts.length) ? (String) s.value : texts[textIdx++];
                        if (txt == null || (font == null && txt.isEmpty())) {
                            continue;
                        }

                        if (font == null) {
                            throw new TextParseException("Font not defined", lexer.yyline());
                        }

                        while (!txt.isEmpty() && (txt.charAt(0) == '\r' || txt.charAt(0) == '\n')) {
                            txt = txt.substring(1);
                        }

                        while (!txt.isEmpty() && (txt.charAt(txt.length() - 1) == '\r' || txt.charAt(txt.length() - 1) == '\n')) {
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

                        TEXTRECORD tr;

                        if (append) {
                            tr = textRecords.get(textRecords.size() - 1);
                        } else {
                            tr = new TEXTRECORD();
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
                        }
                        for (int i = 0; i < txt.length(); i++) {
                            char c = txt.charAt(i);
                            Character nextChar = null;
                            if (i + 1 < txt.length()) {
                                nextChar = txt.charAt(i + 1);
                            }

                            GLYPHENTRY ge = new GLYPHENTRY();
                            ge.glyphIndex = font.charToGlyph(c);

                            int advance = getAdvance(font, ge.glyphIndex, textHeight, c, nextChar);

                            advance += letterSpacing;
                            if (simpleSpacing.containsKey(c)) {
                                advance += simpleSpacing.get(c) - letterSpacing;
                            }
                            String pairKey = "" + c + (nextChar == null ? "" : nextChar);
                            if (pairSpacing.containsKey(pairKey)) {
                                advance += pairSpacing.get(pairKey) - letterSpacing;
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
                        append = true;
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

    /**
     * Gets advance.
     *
     * @param font Font
     * @param glyphIndex Glyph index
     * @param textHeight Text height
     * @param c Character
     * @param nextChar Next character
     * @return Advance
     */
    public static int getAdvance(FontTag font, int glyphIndex, int textHeight, char c, Character nextChar) {
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

    /**
     * Detects letter spacing.
     *
     * @param textRecord Text record
     * @param font Font
     * @param textHeight Text height
     * @return Letter spacing
     */
    public static int detectLetterSpacing(TEXTRECORD textRecord, FontTag font, int textHeight) {
        int minLetterSpacing = Integer.MAX_VALUE;
        int numNegatives = 0;
        List<GLYPHENTRY> glyphEntries = textRecord.glyphEntries;

        if (glyphEntries.size() < 2) {
            return 0;
        }

        int numMin = 0;
        for (int i = 0; i < glyphEntries.size() - 1; i++) {
            GLYPHENTRY glyph = glyphEntries.get(i);
            /*GLYPHENTRY nextGlyph = null;
            if (i + 1 < glyphEntries.size()) {
                nextGlyph = glyphEntries.get(i + 1);
            }*/
            GLYPHENTRY nextGlyph = glyphEntries.get(i + 1);
            char c = font.glyphToChar(glyph.glyphIndex);
            //Character nextChar = nextGlyph == null ? null : font.glyphToChar(nextGlyph.glyphIndex);
            Character nextChar = font.glyphToChar(nextGlyph.glyphIndex);
            int advance = getAdvance(font, glyph.glyphIndex, textHeight, c, nextChar);
            int letterSpacing = glyph.glyphAdvance - advance;
            //System.err.println("advance between char "+c+" and "+nextChar+": advance = " + advance + " glyphAdvance="+glyph.glyphAdvance+" delta:"+letterSpacing);
            if (letterSpacing < 0) {
                numNegatives++;
            }
            if (letterSpacing == minLetterSpacing) {
                numMin++;
            }
            if (letterSpacing < minLetterSpacing) {
                minLetterSpacing = letterSpacing;
                numMin = 1;
            }
        }
        if (minLetterSpacing < 0 && numNegatives < glyphEntries.size() / 2) { //a hack, use negative letterspacing only when 50% letters use it
            minLetterSpacing = 0;
        }
        if (numMin == 1) {
            return 0;
        }

        return minLetterSpacing;
    }

    @Override
    public void getNeededCharacters(Set<Integer> needed, SWF swf) {
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
    public void toImage(int frame, int time, int ratio, RenderContext renderContext, SerializableImage image, SerializableImage fullImage, boolean isClip, Matrix transformation, Matrix strokeTransformation, Matrix absoluteTransformation, Matrix fullTransformation, ColorTransform colorTransform, double unzoom, boolean sameImage, ExportRectangle viewRect, ExportRectangle viewRectRaw, boolean scaleStrokes, int drawMode, int blendMode, boolean canUseSmoothing) {
        if (image.getGraphics() instanceof RequiresNormalizedFonts) {
            RequiresNormalizedFonts g = (RequiresNormalizedFonts) image.getGraphics();
            Map<Integer, StaticTextTag> normalizedTexts = g.getNormalizedTexts();
            int realTextId = getSwf().getCharacterId(this);
            if (normalizedTexts.containsKey(realTextId)) {
                StaticTextTag normalizedText = normalizedTexts.get(realTextId);
                staticTextToImage(swf, normalizedText.textRecords, getTextNum(), image, normalizedText.textMatrix, transformation, colorTransform, renderContext.selectionText == this ? renderContext.selectionStart : 0, renderContext.selectionText == this ? renderContext.selectionEnd : 0);
                return;
            }
        }
        staticTextToImage(swf, textRecords, getTextNum(), image, textMatrix, transformation, colorTransform, renderContext.selectionText == this ? renderContext.selectionStart : 0, renderContext.selectionText == this ? renderContext.selectionEnd : 0);
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
    public void toSVG(SVGExporter exporter, int ratio, ColorTransform colorTransform, int level, Matrix transformation, Matrix strokeTransformation) {
        int realTextId = getSwf().getCharacterId(this);
        if (exporter.getNormalizedTexts().containsKey(realTextId)) {
            StaticTextTag normalizedText = exporter.getNormalizedTexts().get(realTextId);
            staticTextToSVG(swf, normalizedText.textRecords, getTextNum(), exporter, getRect(), normalizedText.textMatrix, colorTransform, exporter.getZoom(), transformation);
            return;
        }
        staticTextToSVG(swf, textRecords, getTextNum(), exporter, getRect(), textMatrix, colorTransform, exporter.getZoom(), transformation);
    }

    @Override
    public void toHtmlCanvas(StringBuilder result, double unitDivisor) {
        staticTextToHtmlCanvas(unitDivisor, swf, textRecords, getTextNum(), result, textBounds, textMatrix, null);
    }
}
