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
package com.jpexs.decompiler.flash.tags.base;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.Matrix;
import com.jpexs.decompiler.flash.exporters.Point;
import com.jpexs.decompiler.flash.exporters.SVGExporterContext;
import com.jpexs.decompiler.flash.helpers.FontHelper;
import com.jpexs.decompiler.flash.tags.DefineFontNameTag;
import com.jpexs.decompiler.flash.tags.DefineText2Tag;
import com.jpexs.decompiler.flash.tags.DefineTextTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.timeline.DepthState;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.GLYPHENTRY;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.TEXTRECORD;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.helpers.SerializableImage;
import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public abstract class FontTag extends CharacterTag implements AloneTag, DrawableTag {

    protected final int previewSize = 500;

    public FontTag(SWF swf, int id, String name, byte[] headerData, byte[] data, long pos) {
        super(swf, id, name, headerData, data, pos);
    }

    public abstract int getFontId();

    public abstract List<SHAPE> getGlyphShapeTable();

    public abstract void addCharacter(char character, String fontName);

    public abstract char glyphToChar(int glyphIndex);

    public abstract int charToGlyph(char c);

    public abstract double getGlyphAdvance(int glyphIndex);

    public abstract int getGlyphKerningAdjustment(int glyphIndex, int nextGlyphIndex);

    public abstract int getGlyphWidth(int glyphIndex);

    public abstract String getFontName();

    public abstract boolean isSmall();

    public abstract boolean isBold();

    public abstract boolean isItalic();

    public abstract boolean isSmallEditable();

    public abstract boolean isBoldEditable();

    public abstract boolean isItalicEditable();

    public abstract void setSmall(boolean value);

    public abstract void setBold(boolean value);

    public abstract void setItalic(boolean value);

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

    public boolean containsChar(char character) {
        return charToGlyph(character) > -1;
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
    public String getName() {
        String nameAppend = "";
        if (exportName != null) {
            nameAppend = ": " + exportName;
        }
        if (className != null) {
            nameAppend = ": " + className;
        }
        String fontName = getFontName();
        if (fontName != null) {
            nameAppend = ": " + fontName;
        }
        return tagName + " (" + getCharacterId() + nameAppend + ")";
    }

    public String getSystemFontName() {
        Map<String, String> fontPairs = Configuration.getFontPairs();
        String key = swf.getShortFileName() + "_" + getFontId() + "_" + getFontName();
        if (fontPairs.containsKey(key)) {
            return fontPairs.get(key);
        }
        key = getFontName();
        if (fontPairs.containsKey(key)) {
            return fontPairs.get(key);
        }
        return defaultFontName;
    }

    public void shiftGlyphIndices(int fontId, int startIndex) {
        List<TEXTRECORD> textRecords = new ArrayList<>();
        for (Tag t : swf.tags) {
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

    public static String getFontNameWithFallback(String fontName) {
        if (fontNames.contains(fontName)) {
            return fontName;
        }
        if (fontNames.contains("Times New Roman")) {
            return "Times New Roman";
        }
        if (fontNames.contains("Arial")) {
            return "Arial";
        }
        //Fallback to DIALOG
        return "Dialog";
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
    public void toImage(int frame, int time, int ratio, DepthState stateUnderCursor, int mouseButton, SerializableImage image, Matrix transformation, ColorTransform colorTransform) {
        SHAPERECORD.shapeListToImage(swf, getGlyphShapeTable(), image, frame, Color.black, colorTransform);
    }

    @Override
    public String toSVG(SVGExporterContext exporterContext, int ratio, int level) {
        return "";
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Point getImagePos(int frame) {
        return new Point(0, 0);
    }

    @Override
    public int getNumFrames() {
        int frameCount = (getGlyphShapeTable().size() - 1) / SHAPERECORD.MAX_CHARACTERS_IN_FONT_PREVIEW + 1;
        if (frameCount < 1) {
            frameCount = 1;
        }
        return frameCount;
    }

    @Override
    public boolean isSingleFrame() {
        return true;
    }

    @Override
    public Shape getOutline(int frame, int time, int ratio, DepthState stateUnderCursor, int mouseButton, Matrix transformation) {
        RECT r = getRect();
        return new Area(new Rectangle(r.Xmin, r.Ymin, r.getWidth(), r.getHeight()));
    }

    @Override
    public RECT getRect() {
        return new RECT(0, (int) (previewSize * SWF.unitDivisor), 0, (int) (previewSize * SWF.unitDivisor));
    }

    @Override
    public String getCharacterExportFileName() {
        return super.getCharacterExportFileName() + "_" + getFontName();
    }

    public DefineFontNameTag getFontNameTag() {
        for (Tag t : swf.tags) {
            if (t instanceof DefineFontNameTag) {
                DefineFontNameTag dfn = (DefineFontNameTag) t;
                if (dfn.fontId == getFontId()) {
                    return dfn;
                }
            }
        }
        return null;
    }

    public String getCopyright() {
        DefineFontNameTag dfn = getFontNameTag();
        if (dfn == null) {
            return null;
        }
        return dfn.fontCopyright;
    }
}
