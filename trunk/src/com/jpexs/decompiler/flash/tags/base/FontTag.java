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
package com.jpexs.decompiler.flash.tags.base;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.helpers.FontHelper;
import com.jpexs.decompiler.flash.tags.DefineText2Tag;
import com.jpexs.decompiler.flash.tags.DefineTextTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.types.GLYPHENTRY;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.TEXTRECORD;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 *
 * @author JPEXS
 */
public abstract class FontTag extends CharacterTag implements AloneTag, DrawableTag {

    public FontTag(SWF swf, int id, String name, byte[] data, long pos) {
        super(swf, id, name, data, pos);
    }

    public abstract int getFontId();

    public abstract List<SHAPE> getGlyphShapeTable();

    public abstract void addCharacter(List<Tag> tags, char character, String fontName);

    public abstract char glyphToChar(List<Tag> tags, int glyphIndex);

    public abstract int charToGlyph(List<Tag> tags, char c);

    public abstract int getGlyphAdvance(int glyphIndex);

    public abstract int getGlyphKerningAdjustment(List<Tag> tags, int glyphIndex, int nextGlyphIndex);

    public abstract int getGlyphWidth(int glyphIndex);

    public abstract String getFontName(List<Tag> tags);

    public abstract boolean isSmall();

    public abstract boolean isBold();

    public abstract boolean isItalic();

    public abstract int getDivider();

    public abstract int getAscent();

    public abstract int getDescent();

    public abstract int getLeading();

    public static String[] fontNamesArray;

    public static List<String> fontNames;
    
    public static String defaultFontName;
    
    static {
        reload();
    }
    
    public boolean hasLayout() {
        return false;
    }

    public boolean containsChar(List<Tag> tags, char character) {
        return charToGlyph(tags, character) > -1;
    }

    public int getFontStyle() {
        int fontStyle = 0;
        if (isBold()) {
            fontStyle |= Font.BOLD;
        }
        if (isItalic()) {
            fontStyle |= Font.ITALIC;
        }
        return fontStyle;
    }

    public abstract String getCharacters(List<Tag> tags);

    @Override
    public String getName(List<Tag> tags) {
        String nameAppend = "";
        if (exportName != null) {
            nameAppend = ": " + exportName;
        }
        if (className != null) {
            nameAppend = ": " + className;
        }
        String fontName = getFontName(tags);
        if (fontName != null) {
            nameAppend = ": " + fontName;
        }
        return name + " (" + getCharacterId() + nameAppend + ")";
    }

    public String getSystemFontName(List<Tag> tags) {
        Map<String, String> fontPairs = Configuration.getFontPairs();
        String name = getFontName(tags);
        if (fontPairs.containsKey(name)) {
            return fontPairs.get(name);
        }
        return defaultFontName;
    }
    
    public static void shiftGlyphIndices(int fontId, int startIndex, List<Tag> tags) {
        List<TEXTRECORD> textRecords = new ArrayList<>();
        for (Tag t : tags) {
            if (t instanceof DefineTextTag) {
                textRecords.addAll(((DefineTextTag) t).textRecords);
            }
            if (t instanceof DefineText2Tag) {
                textRecords.addAll(((DefineText2Tag) t).textRecords);
            }
        }
        int curFontId = 0;
        for (TEXTRECORD tr : textRecords) {
            if (tr.styleFlagsHasFont) {
                curFontId = tr.fontId;
            }
            if (curFontId != fontId) {
                continue;
            }
            for (GLYPHENTRY en : tr.glyphEntries) {
                if (en == null) { //Currently edited
                    continue;
                }
                if (en.glyphIndex >= startIndex) {
                    en.glyphIndex++;
                }
            }
        }
    }

    public static float getSystemFontAdvance(String fontName, int fontStyle, int fontSize, Character character, Character nextCharacter) {
        return getSystemFontAdvance(new Font(fontName, fontStyle, fontSize), character, nextCharacter);
    }

    public static float getSystemFontAdvance(Font aFont, Character character, Character nextCharacter) {
        GlyphVector gv = aFont.createGlyphVector(new FontRenderContext(aFont.getTransform(), true, true), "" + character + (nextCharacter == null ? "" : nextCharacter));
        GlyphMetrics gm = gv.getGlyphMetrics(0);
        return gm.getAdvanceX();
    }

    public static void reload() {
        fontNamesArray = FontHelper.getInstalledFontFamilyNames();
        fontNames = Arrays.asList(fontNamesArray);
        if (fontNames.contains("Times New Roman")) {
            defaultFontName = "Times New Roman";
        } else if (fontNames.contains("Arial")) {
            defaultFontName = "Arial";
        } else {
            defaultFontName = fontNames.get(0);
        }
    }

    public static String isFontInstalled(String fontName) {
        if (fontNames.contains(fontName)) {
            return fontName;
        }
        if (fontName.contains("_")) {
            String beforeUnderscore = fontName.substring(0, fontName.indexOf('_'));
            if (fontNames.contains(beforeUnderscore)) {
                return beforeUnderscore;
            }
        }
        return null;
    }

    public static String findInstalledFontName(String fontName) {
        if (fontNames.contains(fontName)) {
            return fontName;
        }
        if (fontName.contains("_")) {
            String beforeUnderscore = fontName.substring(0, fontName.indexOf('_'));
            if (fontNames.contains(beforeUnderscore)) {
                return beforeUnderscore;
            }
        }
        return defaultFontName;
    }

    @Override
    public BufferedImage toImage(int frame, List<Tag> tags, RECT displayRect, HashMap<Integer, CharacterTag> characters, Stack<Integer> visited) {
        return SHAPERECORD.shapeListToImage(getGlyphShapeTable(), 500, 500, Color.black);
    }

    @Override
    public Point getImagePos(int frame, HashMap<Integer, CharacterTag> characters, Stack<Integer> visited) {
        return new Point(0, 0);
    }

    @Override
    public int getNumFrames() {
        return 1;
    }
}
