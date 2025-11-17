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
package com.jpexs.decompiler.flash;

import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.tags.DefineEditTextTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.StaticTextTag;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.tags.dynamictext.CharacterWithStyle;
import com.jpexs.decompiler.flash.tags.dynamictext.TextStyle;
import com.jpexs.decompiler.flash.tags.text.xml.XmlException;
import com.jpexs.decompiler.flash.tags.text.xml.XmlLexer;
import com.jpexs.decompiler.flash.tags.text.xml.XmlParsedSymbol;
import com.jpexs.decompiler.flash.tags.text.xml.XmlSymbolType;
import com.jpexs.decompiler.flash.types.GLYPHENTRY;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.TEXTRECORD;
import com.jpexs.decompiler.flash.types.shaperecords.CurvedEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import java.awt.Font;
import java.awt.Point;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * Font size / orientation normalizer. Does following: - shrinks oversized fonts
 * to 1024 em - fixes vertically flipped fonts / texts - fixes zero/1unit spaces
 * font glyph advance - fixes zero last glyph advance in texts
 *
 * @author JPEXS
 */
public class FontNormalizer {

    /**
     * Normalizes fonts in the SWF file in place.
     *
     * @param swf SWF
     */
    public void normalizeFonts(SWF swf) {
        normalizeFonts(swf, true, new LinkedHashMap<>(), new LinkedHashMap<>());
    }

    /**
     * Normalizes fonts in the SWF file creating clones of font/text tags.
     *
     * @param swf SWF file
     * @param outFonts Modified fonts (clone) - fontId to fontTag
     * @param outTexts Modified texts (clone) - textId to textTag
     */
    public void normalizeFonts(SWF swf, Map<Integer, FontTag> outFonts, Map<Integer, TextTag> outTexts) {
        normalizeFonts(swf, false, outFonts, outTexts);
    }

    /**
     * Normalizes fonts in the SWF file.
     *
     * @param swf SWF file
     * @param inPlace Modify tags in SWF file (true) or create clones (false)
     * @param outFonts Modified fonts - fontId to fontTag
     * @param outTexts Modified texts - textId to textTag
     */
    public void normalizeFonts(SWF swf, boolean inPlace, Map<Integer, FontTag> outFonts, Map<Integer, TextTag> outTexts) {
        Map<Integer, CharacterTag> characters = swf.getCharacters(!inPlace);

        Map<Integer, TextTag> texts = new LinkedHashMap<>();

        for (int characterId : characters.keySet()) {
            CharacterTag character = characters.get(characterId);
            if (character instanceof TextTag) {
                texts.put(characterId, (TextTag) character);
            }
        }

        Set<Integer> invertedFontIds = new LinkedHashSet<>();
        Set<Integer> notInvertedFontIds = new LinkedHashSet<>();
        Set<Integer> fontIds = new LinkedHashSet<>();

        for (TextTag text : texts.values()) {
            if (text instanceof DefineEditTextTag) {
                DefineEditTextTag detext = (DefineEditTextTag) text;
                fontIds.addAll(getDefineEditTextFonts(detext));
            }

            if (text instanceof StaticTextTag) {
                StaticTextTag stext = (StaticTextTag) text;
                boolean inverted = false;
                if (stext.textMatrix != null) {
                    if (stext.textMatrix.scaleY < 0) {
                        inverted = true;
                    }
                }
                for (TEXTRECORD rec : stext.textRecords) {
                    if (rec.styleFlagsHasFont) {
                        if (inverted) {
                            invertedFontIds.add(rec.fontId);
                        } else {
                            notInvertedFontIds.add(rec.fontId);
                        }
                        fontIds.add(rec.fontId);
                    }
                }
            }
        }

        Map<Integer, Double> fontNewScale = new LinkedHashMap<>();

        for (int fontId : fontIds) {
            if (notInvertedFontIds.contains(fontId)) {
                invertedFontIds.remove(fontId);
            }

            CharacterTag fontCharacter = characters.get(fontId);
            if (fontCharacter == null || !(fontCharacter instanceof FontTag)) {
                continue;
            }
            FontTag font = (FontTag) fontCharacter;

            boolean willModify = false;
            if (invertedFontIds.contains(fontId)) {
                willModify = true;
            }

            double newScale = 1;

            String systemFont = font.getSystemFontName();

            List<SHAPE> shapes1 = font.getGlyphShapeTable();
            Double h = null;
            Double systemH = null;
            for (int i = 0; i < shapes1.size(); i++) {
                RECT b = shapes1.get(i).getBounds(1);
                h = b.getHeight() / font.getDivider();
                if (h <= 0) {
                    continue;
                }
                char c = font.glyphToChar(i);
                Font f = new Font(systemFont, (font.isBold() ? Font.BOLD : 0) | (font.isItalic() ? Font.ITALIC : 0), 1000);
                if (!f.canDisplay(c)) {
                    continue;
                }
                FontRenderContext frc = new FontRenderContext(null, true, true);
                GlyphVector gv = f.createGlyphVector(frc, new char[]{c});
                systemH = gv.getGlyphOutline(0).getBounds2D().getHeight();
                break;
            }

            if (h != null && systemH != null) {
                newScale = systemH / h;
                willModify = true;
            }

            final double scale = newScale;

            int spaceGlyph = font.charToGlyph(' ');
            int nonBreakingSpaceGlyph = font.charToGlyph((char) 0xA0);

            if (spaceGlyph != -1 && font.getGlyphAdvance(spaceGlyph) <= 1) {
                willModify = true;
            }

            if (nonBreakingSpaceGlyph != -1 && font.getGlyphAdvance(nonBreakingSpaceGlyph) <= 1) {
                willModify = true;
            }

            if (!willModify) {
                continue;
            }

            //scale = 1;
            fontNewScale.put(fontId, scale);
            FontTag font2;
            if (inPlace) {
                font2 = font;
            } else {
                try {
                    font2 = (FontTag) font.cloneTag();
                } catch (InterruptedException | IOException ex) {
                    continue;
                }
            }
            outFonts.put(fontId, font2);

            List<SHAPE> shapes = font2.getGlyphShapeTable();

            Matrix matrix = new Matrix();

            matrix = matrix.preConcatenate(Matrix.getScaleInstance(scale, scale));

            if (invertedFontIds.contains(fontId)) {
                matrix = matrix.preConcatenate(Matrix.getScaleInstance(1, -1));
            }

            for (int i = 0; i < shapes.size(); i++) {
                SHAPE shp = shapes.get(i);
                transformSHAPE(matrix, shp);
                if (font2.hasLayout()) {
                    font2.setGlyphAdvance(i, font2.getGlyphAdvance(i) * scale);
                }
            }
            if (font2.hasLayout()) {
                font2.updateBounds();

                font2.setAscent((int) Math.round(font2.getAscent() * scale));
                font2.setDescent((int) Math.round(font2.getDescent() * scale));
                font2.setLeading((int) Math.round(font2.getLeading() * scale));

                if (invertedFontIds.contains(fontId)) {
                    int ascent = font2.getAscent();
                    int descent = font2.getDescent();
                    //switch ascent and descent
                    font2.setAscent(descent);
                    font2.setDescent(ascent);

                    //what to do with leading?                    
                }
            }

            if (spaceGlyph != -1 && font.getGlyphAdvance(spaceGlyph) <= 1) {
                font2.setGlyphAdvance(spaceGlyph, 512);
            }

            if (nonBreakingSpaceGlyph != -1 && font.getGlyphAdvance(nonBreakingSpaceGlyph) <= 1) {
                font2.setGlyphAdvance(nonBreakingSpaceGlyph, 512);
            }

            font2.setModified(true);
        }

        for (int textId : texts.keySet()) {
            int fontId = -1;
            int textHeight = 12 * 20;
            if (texts.get(textId) instanceof DefineEditTextTag) {
                DefineEditTextTag text = (DefineEditTextTag) texts.get(textId);
                scaleDefineEditTextFonts(text, fontNewScale, inPlace, outTexts);
            } else if (texts.get(textId) instanceof StaticTextTag) {
                StaticTextTag text = (StaticTextTag) texts.get(textId);
                StaticTextTag text2 = null;
                for (int i = 0; i < text.textRecords.size(); i++) {
                    TEXTRECORD rec = text.textRecords.get(i);
                    if (rec.styleFlagsHasFont) {
                        fontId = rec.fontId;
                        if (fontNewScale.containsKey(fontId) || invertedFontIds.contains(fontId)) {
                            if (text2 == null) {
                                if (inPlace) {
                                    text2 = text;
                                } else {
                                    try {
                                        text2 = (StaticTextTag) text.cloneTag();
                                    } catch (InterruptedException | IOException ex) {
                                        break;
                                    }
                                }
                                outTexts.put(textId, text2);
                                text2.setModified(true);
                            }
                        }
                        textHeight = text.textRecords.get(i).textHeight;

                        if (fontNewScale.containsKey(fontId)) {
                            text2.textRecords.get(i).textHeight = round20(text2.textRecords.get(i).textHeight / fontNewScale.get(fontId));
                            textHeight = text2.textRecords.get(i).textHeight;
                        }
                        if (invertedFontIds.contains(fontId)) {
                            if (text2.textMatrix != null && text2.textMatrix.scaleY < 0) {
                                text2.textMatrix.scaleY *= -1;
                            }
                        }

                    }

                    if (invertedFontIds.contains(fontId)) {
                        if (rec.styleFlagsHasYOffset) {
                            text2.textRecords.get(i).yOffset = -rec.yOffset;
                        }
                    }
                    if (!rec.glyphEntries.isEmpty() && rec.glyphEntries.get(rec.glyphEntries.size() - 1).glyphAdvance == 0) {
                        FontTag font;
                        if (outFonts.containsKey(fontId)) {
                            font = outFonts.get(fontId);
                        } else {
                            font = swf.getFont(fontId);
                        }
                        if (font != null) {
                            if (text2 == null) {
                                if (inPlace) {
                                    text2 = text;
                                } else {
                                    try {
                                        text2 = (StaticTextTag) text.cloneTag();
                                    } catch (InterruptedException | IOException ex) {
                                        break;
                                    }
                                }
                                outTexts.put(textId, text2);
                                text2.setModified(true);
                            }

                            GLYPHENTRY lastGlyphEntry = text2.textRecords.get(i).glyphEntries.get(rec.glyphEntries.size() - 1);
                            lastGlyphEntry.glyphAdvance = (int) Math.round(font.getGlyphAdvance(lastGlyphEntry.glyphIndex) * textHeight / (1024.0 * font.getDivider()));

                            if (i + 1 < text.textRecords.size()) {
                                TEXTRECORD nextRec = text2.textRecords.get(i + 1);
                                if (!nextRec.styleFlagsHasXOffset && !nextRec.glyphEntries.isEmpty()) {
                                    nextRec.glyphEntries.get(0).glyphAdvance -= lastGlyphEntry.glyphAdvance;
                                }
                            }
                        }
                    }
                }
            }
        }
        swf.clearShapeCache();
    }

    private static int round20(double val) {
        return (int) Math.floor(val / 20.0) * 20;
    }

    private Set<Integer> getDefineEditTextFonts(DefineEditTextTag text) {
        Set<Integer> ret = new LinkedHashSet<>();
        TextStyle style = new TextStyle();
        if (text.fontClass != null) {
            style.font = text.getSwf().getFontByClass(text.fontClass);
        } else {
            style.font = text.getSwf().getFont(text.fontId);
        }
        int fontId = text.getSwf().getCharacterId(style.font);
        ret.add(fontId);

        if (text.html) {
            final Stack<TextStyle> styles = new Stack<>();
            styles.add(style);
            XmlLexer lexer = new XmlLexer(new StringReader(text.initialText));
            try {
                XmlParsedSymbol s = lexer.yylex();
                boolean inOpenTag = false;
                String attributeName = null;
                String tagName = null;
                Map<String, String> attributes = new LinkedHashMap<>();
                loops:
                while (s.type != XmlSymbolType.EOF) {
                    switch (s.type) {
                        case TAG_OPEN:
                            inOpenTag = true;
                            attributeName = null;
                            tagName = (String) s.value;
                            attributes.clear();
                            break;
                        case ATTRIBUTE:
                            attributeName = (String) s.value;
                            break;
                        case ATTRIBUTE_VALUE:
                            if (attributeName == null) {
                                //Error
                                break loops;
                            }
                            attributes.put(attributeName, (String) s.value);
                            break;
                        case TAG_OPEN_END:
                            style = styles.peek();
                            switch (tagName) {
                                case "p":
                                    // todo: parse the following attribute:
                                    // align
                                    break;
                                case "a":
                                    // todo: handle link - href, target attributes
                                    break;
                                case "b":
                                    style = style.clone();
                                    style.bold = true;
                                    styles.add(style);
                                    break;
                                case "i":
                                    style = style.clone();
                                    style.italic = true;
                                    styles.add(style);
                                    break;
                                case "u":
                                    style = style.clone();
                                    style.underlined = true;
                                    styles.add(style);
                                    break;
                                case "font":
                                    style = style.clone();
                                    String face = attributes.get("face");

                                    if (face != null && face.length() > 0) {
                                        style.fontFace = face;
                                    }

                                    String size = attributes.get("size");
                                    if (size != null && size.length() > 0) {

                                        if (style.fontFace != null && text.useOutlines) {
                                            CharacterTag ct = text.getSwf().getCharacterByExportName(style.fontFace);
                                            if (ct != null && (ct instanceof FontTag)) {
                                                style.font = (FontTag) ct;
                                            } else {
                                                style.font = text.getSwf().getFontByNameInTag(style.fontFace, style.bold, style.italic);
                                            }
                                            if (style.font == null) {
                                                style.fontFace = null;
                                            } else {
                                                fontId = text.getSwf().getCharacterId(style.font);
                                                ret.add(fontId);
                                            }
                                        }

                                    }

                                    styles.add(style);
                                    break;
                            }
                            tagName = null;
                            break;
                        case TAG_CLOSE:
                            tagName = (String) s.value;
                            switch (tagName) {
                                case "b":
                                case "i":
                                case "u":
                                case "font":
                                    styles.pop();
                                    break;
                            }
                            tagName = null;
                            break;
                    }
                    s = lexer.yylex();
                }
            } catch (IOException ex) {
                //Logger.getLogger(DefineEditTextTag.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XmlException ex) {
                //Logger.getLogger(DefineEditTextTag.class.getName()).log(Level.SEVERE, null, ex);
                //ex.printStackTrace();
            }
        }
        return ret;
    }

    private void scaleDefineEditTextFonts(DefineEditTextTag text, Map<Integer, Double> fontNewScale, boolean inPlace, Map<Integer, TextTag> outTexts) {
        String str = "";
        TextStyle style = new TextStyle();
        if (text.fontClass != null) {
            style.font = text.getSwf().getFontByClass(text.fontClass);
        } else {
            style.font = text.getSwf().getFont(text.fontId);
        }
        int textId = text.getSwf().getCharacterId(text);
        int fontId = text.getSwf().getCharacterId(style.font);
        DefineEditTextTag text2 = null;
        if (fontNewScale.containsKey(fontId)) {
            if (inPlace) {
                text2 = text;
            } else {
                try {
                    text2 = (DefineEditTextTag) text.cloneTag();
                } catch (InterruptedException | IOException ex) {
                    return;
                }
            }
            text2.fontHeight = round20(text2.fontHeight / fontNewScale.get(fontId));
            outTexts.put(textId, text2);
            text2.setModified(true);
        }

        style.fontHeight = text.fontHeight;
        style.fontLeading = text.leading;
        if (text.hasTextColor) {
            style.textColor = text.textColor;
        }
        if (text.hasText) {
            str = text.initialText;
        }
        style.leftMargin = text.leftMargin;
        final List<CharacterWithStyle> ret = new ArrayList<>();
        if (text.html) {
            StringBuilder sb = new StringBuilder();
            //SAXParserFactory factory = SAXParserFactory.newInstance();
            //SAXParser saxParser;
            final Stack<TextStyle> styles = new Stack<>();
            styles.add(style);
            XmlLexer lexer = new XmlLexer(new StringReader(text.initialText));
            try {
                XmlParsedSymbol s = lexer.yylex();
                boolean inOpenTag = false;
                String attributeName = null;
                String tagName = null;
                Map<String, String> attributes = new LinkedHashMap<>();
                loops:
                while (s.type != XmlSymbolType.EOF) {
                    switch (s.type) {
                        case TAG_OPEN:
                            inOpenTag = true;
                            attributeName = null;
                            tagName = (String) s.value;
                            attributes.clear();
                            break;
                        case ATTRIBUTE:
                            attributeName = (String) s.value;
                            break;
                        case ATTRIBUTE_VALUE:
                            if (attributeName == null) {
                                //Error
                                break loops;
                            }
                            attributes.put(attributeName, (String) s.value);
                            break;
                        case TAG_OPEN_END:
                            style = styles.peek();
                            switch (tagName) {
                                case "p":
                                    // todo: parse the following attribute:
                                    // align
                                    break;
                                case "a":
                                    // todo: handle link - href, target attributes
                                    break;
                                case "b":
                                    style = style.clone();
                                    style.bold = true;
                                    styles.add(style);
                                    break;
                                case "i":
                                    style = style.clone();
                                    style.italic = true;
                                    styles.add(style);
                                    break;
                                case "u":
                                    style = style.clone();
                                    style.underlined = true;
                                    styles.add(style);
                                    break;
                                case "font":
                                    style = style.clone();
                                    String face = attributes.get("face");

                                    if (face != null && face.length() > 0) {
                                        style.fontFace = face;
                                    }

                                    String size = attributes.get("size");
                                    if (size != null && size.length() > 0) {

                                        if (style.fontFace != null && text.useOutlines) {
                                            CharacterTag ct = text.getSwf().getCharacterByExportName(style.fontFace);
                                            if (ct != null && (ct instanceof FontTag)) {
                                                style.font = (FontTag) ct;
                                            } else {
                                                style.font = text.getSwf().getFontByNameInTag(style.fontFace, style.bold, style.italic);
                                            }
                                            if (style.font == null) {
                                                style.fontFace = null;
                                            } else {
                                                fontId = text.getSwf().getCharacterId(style.font);

                                                if (fontNewScale.containsKey(fontId)) {
                                                    if (text2 == null) {
                                                        if (inPlace) {
                                                            text2 = text;
                                                        } else {
                                                            try {
                                                                text2 = (DefineEditTextTag) text.cloneTag();
                                                            } catch (InterruptedException | IOException ex) {
                                                                return;
                                                            }
                                                        }
                                                        outTexts.put(textId, text2);
                                                        text2.setModified(true);
                                                    }

                                                    try {
                                                        char firstChar = size.charAt(0);
                                                        if (firstChar != '+' && firstChar != '-') {
                                                            int fontSize = Integer.parseInt(size);
                                                            //style.fontHeight = (int) Math.round(fontSize * SWF.unitDivisor);
                                                            attributes.put("size", "" + Math.round(fontSize / fontNewScale.get(fontId)));
                                                        } else {
                                                            int fontSizeDelta = (int) Math.round(Integer.parseInt(size.substring(1)) * SWF.unitDivisor);
                                                            attributes.put("size", "" + firstChar + Math.round(fontSizeDelta / fontNewScale.get(fontId)));
                                                            /*if (firstChar == '+') {
                                                                style.fontHeight = style.fontHeight + fontSizeDelta;
                                                            } else {
                                                                style.fontHeight = style.fontHeight - fontSizeDelta;
                                                            }*/
                                                        }
                                                        style.fontLeading = text.leading;
                                                    } catch (NumberFormatException nfe) {
                                                        //do not change fontHeight or leading
                                                    }
                                                }
                                            }

                                        }

                                    }

                                    styles.add(style);
                                    break;
                            }
                            sb.append("<").append(tagName);
                            for (String key : attributes.keySet()) {
                                sb.append(" ").append(key).append("=").append("\"").append(attributes.get(key)).append("\"");
                            }
                            sb.append(">");
                            tagName = null;
                            break;
                        case TAG_CLOSE:
                            tagName = (String) s.value;
                            switch (tagName) {
                                case "b":
                                case "i":
                                case "u":
                                case "font":
                                    styles.pop();
                                    break;
                            }
                            sb.append("</").append(tagName).append(">");
                            tagName = null;
                            break;

                        case ENTITY:
                            sb.append("&").append(s.value).append(";");
                            break;
                        case CHARACTER:
                            sb.append(s.value);
                            break;
                    }
                    s = lexer.yylex();
                }
                if (text2 != null) {
                    text2.initialText = sb.toString();
                }
            } catch (IOException ex) {
                //Logger.getLogger(DefineEditTextTag.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XmlException ex) {
                //Logger.getLogger(DefineEditTextTag.class.getName()).log(Level.SEVERE, null, ex);
                //ex.printStackTrace();
            }
        }
    }

    // Percentile helper (p in <0,100>)
    private static double percentile(List<Double> values, double p) {
        if (values.isEmpty()) {
            return 0.0;
        }
        Collections.sort(values);
        double rank = (p / 100.0) * (values.size() - 1);
        int lo = (int) Math.floor(rank);
        int hi = (int) Math.ceil(rank);
        if (lo == hi) {
            return values.get(lo);
        }
        double w = rank - lo;
        return values.get(lo) * (1.0 - w) + values.get(hi) * w;
    }

    private void transformSHAPE(Matrix matrix, SHAPE shape) {
        int x = 0;
        int y = 0;
        StyleChangeRecord lastStyleChangeRecord = null;
        boolean wasMoveTo = false;
        for (SHAPERECORD rec : shape.shapeRecords) {
            if (rec instanceof StyleChangeRecord) {
                StyleChangeRecord scr = (StyleChangeRecord) rec;
                lastStyleChangeRecord = scr;
                if (scr.stateNewStyles) {
                    //transformStyles(matrix, scr.fillStyles, scr.lineStyles, shapeNum);
                }
                if (scr.stateMoveTo) {
                    Point nextPoint = new Point(scr.moveDeltaX, scr.moveDeltaY);
                    x = scr.changeX(x);
                    y = scr.changeY(y);
                    Point nextPoint2 = matrix.transform(nextPoint);
                    scr.moveDeltaX = nextPoint2.x;
                    scr.moveDeltaY = nextPoint2.y;
                    scr.calculateBits();
                    wasMoveTo = true;
                }
            }

            if (((rec instanceof StraightEdgeRecord) || (rec instanceof CurvedEdgeRecord)) && !wasMoveTo) {
                if (lastStyleChangeRecord != null) {
                    Point nextPoint2 = matrix.transform(new Point(x, y));
                    if (nextPoint2.x != 0 || nextPoint2.y != 0) {
                        lastStyleChangeRecord.stateMoveTo = true;
                        lastStyleChangeRecord.moveDeltaX = nextPoint2.x;
                        lastStyleChangeRecord.moveDeltaY = nextPoint2.y;
                        lastStyleChangeRecord.calculateBits();
                        wasMoveTo = true;
                    }
                }
            }
            if (rec instanceof StraightEdgeRecord) {
                StraightEdgeRecord ser = (StraightEdgeRecord) rec;
                ser.generalLineFlag = true;
                ser.vertLineFlag = false;
                Point currentPoint = new Point(x, y);
                Point nextPoint = new Point(x + ser.deltaX, y + ser.deltaY);
                x = ser.changeX(x);
                y = ser.changeY(y);
                Point currentPoint2 = matrix.transform(currentPoint);
                Point nextPoint2 = matrix.transform(nextPoint);
                ser.deltaX = nextPoint2.x - currentPoint2.x;
                ser.deltaY = nextPoint2.y - currentPoint2.y;
                ser.simplify();
            }
            if (rec instanceof CurvedEdgeRecord) {
                CurvedEdgeRecord cer = (CurvedEdgeRecord) rec;
                Point currentPoint = new Point(x, y);
                Point controlPoint = new Point(x + cer.controlDeltaX, y + cer.controlDeltaY);
                Point anchorPoint = new Point(x + cer.controlDeltaX + cer.anchorDeltaX, y + cer.controlDeltaY + cer.anchorDeltaY);
                x = cer.changeX(x);
                y = cer.changeY(y);

                Point currentPoint2 = matrix.transform(currentPoint);
                Point controlPoint2 = matrix.transform(controlPoint);
                Point anchorPoint2 = matrix.transform(anchorPoint);

                cer.controlDeltaX = controlPoint2.x - currentPoint2.x;
                cer.controlDeltaY = controlPoint2.y - currentPoint2.y;
                cer.anchorDeltaX = anchorPoint2.x - controlPoint2.x;
                cer.anchorDeltaY = anchorPoint2.y - controlPoint2.y;
                cer.calculateBits();
            }
        }
    }
}
