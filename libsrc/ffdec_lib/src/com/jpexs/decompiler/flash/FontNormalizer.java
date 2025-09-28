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
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.StaticTextTag;
import com.jpexs.decompiler.flash.types.GLYPHENTRY;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.TEXTRECORD;
import com.jpexs.decompiler.flash.types.shaperecords.CurvedEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import java.awt.Point;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Font size / orientation normalizer.
 * Does following:
 *  - shrinks oversized fonts to 1024 em
 *  - fixes vertically flipped fonts / texts
 *  - fixes zero/1unit spaces font glyph advance
 *  - fixes zero last glyph advance in texts
 * 
 * @author JPEXS
 */
public class FontNormalizer {

    /**
     * Normalizes fonts in the SWF file in place.
     * @param swf SWF
     */
    public void normalizeFonts(SWF swf) {
        normalizeFonts(swf, true, new LinkedHashMap<>(), new LinkedHashMap<>());
    }
    
    /**
     * Normalizes fonts in the SWF file creating clones of font/text tags.
     * @param swf SWF file
     * @param outFonts Modified fonts (clone) - fontId to fontTag
     * @param outTexts Modified texts (clone) - textId to textTag
     */
    public void normalizeFonts(SWF swf, Map<Integer, FontTag> outFonts, Map<Integer, StaticTextTag> outTexts) {        
        normalizeFonts(swf, false, outFonts, outTexts);
    }
    
    /**
     * Normalizes fonts in the SWF file.
     * @param swf SWF file
     * @param inPlace Modify tags in SWF file (true) or create clones (false)
     * @param outFonts Modified fonts - fontId to fontTag
     * @param outTexts Modified texts - textId to textTag
     */
    public void normalizeFonts(SWF swf, boolean inPlace, Map<Integer, FontTag> outFonts, Map<Integer, StaticTextTag> outTexts) {
        Map<Integer, CharacterTag> characters = swf.getCharacters(!inPlace);

        Map<Integer, StaticTextTag> texts = new LinkedHashMap<>();        

        for (int characterId : characters.keySet()) {
            CharacterTag character = characters.get(characterId);
            if (character instanceof StaticTextTag) {
                texts.put(characterId, (StaticTextTag) character);
            }
        }

        Set<Integer> invertedFontIds = new LinkedHashSet<>();
        Set<Integer> notInvertedFontIds = new LinkedHashSet<>();
        Set<Integer> fontIds = new LinkedHashSet<>();

        for (StaticTextTag text : texts.values()) {
            boolean inverted = false;
            if (text.textMatrix != null) {
                if (text.textMatrix.scaleY < 0) {
                    inverted = true;
                }
            }
            for (TEXTRECORD rec : text.textRecords) {
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

            int minY = Integer.MAX_VALUE;
            int maxY = Integer.MIN_VALUE;
            for (SHAPE shp : font.getGlyphShapeTable()) {
                RECT b = shp.getBounds(1);
                if (b.Ymin < minY) {
                    minY = b.Ymin;
                }
                if (b.Ymax > maxY) {
                    maxY = b.Ymax;
                }
            }
            int maxH = maxY - minY;

            int originalEmSize = (int) Math.round(maxH / font.getDivider());
            double scale = 1.0;
            boolean willModify = false;
            if (originalEmSize > 1024) {
                scale = 1024.0 / originalEmSize;
                willModify = true;
            }
            if (invertedFontIds.contains(fontId)) {
                willModify = true;
            }
            
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
            StaticTextTag text = texts.get(textId);
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
                        text2.textRecords.get(i).textHeight /= fontNewScale.get(fontId);
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
                        text2.textRecords.get(i).yOffset = - rec.yOffset;
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
        swf.clearShapeCache();
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
