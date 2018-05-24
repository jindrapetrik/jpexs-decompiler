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
 * License along with this library.
 */
package com.jpexs.decompiler.flash.tags.base;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.configuration.SwfSpecificConfiguration;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.commonshape.SVGExporter;
import com.jpexs.decompiler.flash.exporters.shape.CanvasShapeExporter;
import com.jpexs.decompiler.flash.helpers.FontHelper;
import com.jpexs.decompiler.flash.tags.DefineFontNameTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.GLYPHENTRY;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.TEXTRECORD;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.SerializableImage;
import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;
import java.awt.geom.Area;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public abstract class FontTag extends DrawableTag implements AloneTag {

    public static final int PREVIEWSIZE = 500;

    public FontTag(SWF swf, int id, String name, ByteArrayRange data) {
        super(swf, id, name, data);
    }

    public abstract List<SHAPE> getGlyphShapeTable();

    public abstract void addCharacter(char character, Font font);

    public abstract boolean removeCharacter(char character);

    public abstract void setAdvanceValues(Font font);

    public abstract char glyphToChar(int glyphIndex);

    public abstract int charToGlyph(char c);

    public abstract double getGlyphAdvance(int glyphIndex);

    public abstract int getGlyphKerningAdjustment(int glyphIndex, int nextGlyphIndex);

    public abstract int getCharKerningAdjustment(char c1, char c2);

    public abstract int getGlyphWidth(int glyphIndex);

    public abstract String getFontNameIntag();

    public abstract boolean isSmall();

    public abstract boolean isBold();

    public abstract boolean isItalic();

    public abstract boolean isSmallEditable();

    public abstract boolean isBoldEditable();

    public abstract boolean isItalicEditable();

    public abstract void setSmall(boolean value);

    public abstract void setBold(boolean value);

    public abstract void setItalic(boolean value);

    public abstract double getDivider();

    public abstract int getAscent();

    public abstract int getDescent();

    public abstract int getLeading();

    public String getFontName() {
        DefineFontNameTag fontNameTag = getFontNameTag();
        if (fontNameTag == null) {
            return getFontNameIntag();
        }
        return fontNameTag.fontName;
    }

    public String getFontCopyright() {
        DefineFontNameTag fontNameTag = getFontNameTag();
        if (fontNameTag == null) {
            return "";
        }
        return fontNameTag.fontCopyright;
    }

    private static Map<String, Map<String, Font>> installedFontsByFamily;

    private static Map<String, Font> installedFontsByName;

    private static String defaultFontName;

    private static boolean firstLoaded = false;

    private static void ensureLoaded() {
        if (!firstLoaded) {
            reload();
        }
        firstLoaded = true;
    }

    public int getFontId() {
        return getCharacterId();
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

    public abstract int getCharacterCount();

    public abstract String getCharacters();

    @Override
    public String getName() {
        String nameAppend = "";
        if (exportName != null) {
            nameAppend = ": " + exportName;
        }
        if (className != null) {
            nameAppend = ": " + className;
        }
        String fontName = getFontNameIntag();
        if (fontName != null) {
            nameAppend = ": " + fontName;
        }
        return tagName + " (" + getCharacterId() + nameAppend + ")";
    }

    @Override
    public String getExportFileName() {
        String result = super.getExportFileName();
        String fontName = getFontNameIntag();
        if (fontName != null) {
            fontName = fontName.replace(" ", "_");
        }
        return result + (fontName != null ? "_" + fontName : "");
    }

    public String getSystemFontName() {
        int fontId = getFontId();
        String selectedFont = swf.sourceFontNamesMap.get(fontId);
        if (selectedFont == null) {
            SwfSpecificConfiguration swfConf = Configuration.getSwfSpecificConfiguration(swf.getShortFileName());
            String key = fontId + "_" + getFontNameIntag();
            if (swfConf != null) {
                selectedFont = swfConf.fontPairingMap.get(key);
            }
        }

        if (selectedFont == null) {
            selectedFont = Configuration.getFontToNameMap().get(getFontNameIntag());
        }

        if (selectedFont != null && FontTag.installedFontsByName.containsKey(selectedFont)) {
            return selectedFont;
        }

        // findInstalledFontName always returns an available font name
        return FontTag.findInstalledFontName(getFontName());
    }

    public Font getSystemFont() {
        return FontTag.installedFontsByName.get(getSystemFontName());
    }

    protected void shiftGlyphIndices(int fontId, int startIndex, boolean increment) {
        for (Tag t : swf.getTags()) {
            List<TEXTRECORD> textRecords = null;
            if (t instanceof StaticTextTag) {
                textRecords = ((StaticTextTag) t).textRecords;
            }

            if (textRecords != null) {
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
                            if (increment) {
                                en.glyphIndex++;
                            } else {
                                en.glyphIndex--;
                            }
                        }
                    }

                    t.setModified(true);
                }
            }
        }
    }

    public static float getSystemFontAdvance(String fontName, int fontStyle, int fontSize, Character character, Character nextCharacter) {
        return getSystemFontAdvance(new Font(fontName, fontStyle, fontSize), character, nextCharacter);
    }

    public static float getSystemFontAdvance(Font aFont, Character character, Character nextCharacter) {
        String chars = "" + character + (nextCharacter == null ? "" : nextCharacter);
        GlyphVector gv = aFont.layoutGlyphVector(new FontRenderContext(aFont.getTransform(), true, true), chars.toCharArray(), 0, chars.length(), Font.LAYOUT_LEFT_TO_RIGHT);
        GlyphMetrics gm = gv.getGlyphMetrics(0);
        return gm.getAdvanceX();
    }

    public static String getDefaultFontName() {
        ensureLoaded();
        return defaultFontName;
    }

    public static void reload() {
        installedFontsByFamily = FontHelper.getInstalledFonts();
        installedFontsByName = new HashMap<>();

        for (String fam : installedFontsByFamily.keySet()) {
            for (String nam : installedFontsByFamily.get(fam).keySet()) {
                installedFontsByName.put(nam, installedFontsByFamily.get(fam).get(nam));
            }
        }

        if (installedFontsByFamily.containsKey("Times New Roman")) {
            defaultFontName = "Times New Roman";
        } else if (installedFontsByFamily.containsKey("Arial")) {
            defaultFontName = "Arial";
        } else {
            defaultFontName = installedFontsByFamily.keySet().iterator().next();
        }
    }

    public static String getFontNameWithFallback(String fontName) {
        ensureLoaded();
        if (installedFontsByFamily.containsKey(fontName)) {
            return fontName;
        }
        if (installedFontsByFamily.containsKey("Times New Roman")) {
            return "Times New Roman";
        }
        if (installedFontsByFamily.containsKey("Arial")) {
            return "Arial";
        }

        //First font
        return installedFontsByFamily.keySet().iterator().next();
    }

    public static String isFontFamilyInstalled(String fontFamily) {
        if (installedFontsByFamily.containsKey(fontFamily)) {
            return fontFamily;
        }
        if (fontFamily.contains("_")) {
            String beforeUnderscore = fontFamily.substring(0, fontFamily.indexOf('_'));
            if (installedFontsByFamily.containsKey(beforeUnderscore)) {
                return beforeUnderscore;
            }
        }
        return null;
    }

    public static String findInstalledFontName(String fontName) {
        ensureLoaded();
        if (installedFontsByName.containsKey(fontName)) {
            return fontName;
        }
        if (fontName != null && fontName.contains("_")) {
            String beforeUnderscore = fontName.substring(0, fontName.indexOf('_'));
            if (installedFontsByName.containsKey(beforeUnderscore)) {
                return beforeUnderscore;
            }
        }
        return defaultFontName;
    }

    @Override
    public int getUsedParameters() {
        return PARAMETER_FRAME;
    }

    @Override
    public Shape getOutline(int frame, int time, int ratio, RenderContext renderContext, Matrix transformation, boolean stroked) {
        RECT r = getRect();
        return new Area(new Rectangle(r.Xmin, r.Ymin, r.getWidth(), r.getHeight()));
    }

    @Override
    public void toImage(int frame, int time, int ratio, RenderContext renderContext, SerializableImage image, boolean isClip, Matrix transformation, Matrix strokeTransformation, Matrix absoluteTransformation, ColorTransform colorTransform) {
        SHAPERECORD.shapeListToImage(swf, getGlyphShapeTable(), image, frame, Color.black, colorTransform);
    }

    @Override
    public void toSVG(SVGExporter exporter, int ratio, ColorTransform colorTransform, int level) {
    }

    @Override
    public void toHtmlCanvas(StringBuilder result, double unitDivisor) {
        List<SHAPE> shapes = getGlyphShapeTable();
        result.append("\tdefaultFill = textColor;\r\n");
        result.append("\tswitch(ch){\r\n");
        for (int i = 0; i < shapes.size(); i++) {
            char c = glyphToChar(i);
            String cs = "" + c;
            cs = cs.replace("\\", "\\\\").replace("\"", "\\\"");
            result.append("\t\tcase \"").append(cs).append("\":\r\n");
            CanvasShapeExporter exporter = new CanvasShapeExporter(null, unitDivisor, swf, shapes.get(i), null, 0, 0);
            exporter.export();
            result.append("\t\t").append(exporter.getShapeData().replaceAll("\r\n", "\r\n\t\t"));
            result.append("\tbreak;\r\n");
        }
        result.append("\t}\r\n");
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
    public RECT getRect() {
        return getRect(null); // parameter not used
    }

    @Override
    public RECT getRect(Set<BoundedTag> added) {
        return new RECT(0, (int) (PREVIEWSIZE * SWF.unitDivisor), 0, (int) (PREVIEWSIZE * SWF.unitDivisor));
    }

    @Override
    public String getCharacterExportFileName() {
        return super.getCharacterExportFileName() + "_" + getFontNameIntag();
    }

    public DefineFontNameTag getFontNameTag() {
        for (Tag t : swf.getTags()) {
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

    public RECT getGlyphBounds(int glyphIndex) {
        return getGlyphShapeTable().get(glyphIndex).getBounds();
    }

    public FontTag toClassicFont() {
        return this;
    }

    public static Map<String, Map<String, Font>> getInstalledFontsByFamily() {
        ensureLoaded();
        return installedFontsByFamily;
    }

    public static Map<String, Font> getInstalledFontsByName() {
        ensureLoaded();
        return installedFontsByName;
    }

}
