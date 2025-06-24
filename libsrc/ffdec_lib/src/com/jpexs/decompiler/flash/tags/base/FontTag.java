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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.configuration.SwfSpecificConfiguration;
import com.jpexs.decompiler.flash.exporters.commonshape.ExportRectangle;
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
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Base class for font tags.
 *
 * @author JPEXS
 */
public abstract class FontTag extends DrawableTag implements AloneTag {

    /**
     * Preview size for font tags
     */
    public static final int PREVIEWSIZE = 500;

    /**
     * Constructor.
     *
     * @param swf SWF
     * @param id Tag ID
     * @param name Tag name
     * @param data Tag data
     */
    public FontTag(SWF swf, int id, String name, ByteArrayRange data) {
        super(swf, id, name, data);
    }

    /**
     * Gets glyph shape table.
     * @return Glyph shape table
     */
    public abstract List<SHAPE> getGlyphShapeTable();

    /**
     * Adds character to font.
     * @param character Character
     * @param font Font
     * @return True if character was added, false otherwise
     */
    public abstract boolean addCharacter(char character, Font font);

    /**
     * Removes character from font.
     * @param character Character
     * @return True if character was removed, false otherwise
     */
    public abstract boolean removeCharacter(char character);

    /**
     * Sets advance values.
     * @param font Font
     */
    public abstract void setAdvanceValues(Font font);

    /**
     * Converts glyph to character.
     * @param glyphIndex Glyph index
     * @return Character
     */
    public abstract char glyphToChar(int glyphIndex);

    /**
     * Converts character to glyph.
     * @param c Character
     * @return Glyph index
     */
    public abstract int charToGlyph(char c);

    /**
     * Gets glyph advance.
     * @param glyphIndex Glyph index
     * @return Glyph advance
     */
    public abstract double getGlyphAdvance(int glyphIndex);

    /**
     * Gets glyph kerning adjustment.
     * @param glyphIndex Glyph index
     * @param nextGlyphIndex Next glyph index
     * @return Kerning adjustment
     */
    public abstract int getGlyphKerningAdjustment(int glyphIndex, int nextGlyphIndex);

    /**
     * Gets character kerning adjustment.
     * @param c1 Character 1
     * @param c2 Character 2
     * @return Kerning adjustment
     */
    public abstract int getCharKerningAdjustment(char c1, char c2);

    /**
     * Gets glyph width.
     * @param glyphIndex Glyph index
     * @return Glyph width
     */
    public abstract int getGlyphWidth(int glyphIndex);

    /**
     * Gets font name in the tag.
     * @return Font name in the tag
     */
    public abstract String getFontNameIntag();

    /**
     * Sets font name in the tag.
     * @param name Font name
     */
    public abstract void setFontNameIntag(String name);

    /**
     * Checks if font is small.
     * @return True if font is small, false otherwise
     */
    public abstract boolean isSmall();

    /**
     * Checks if font is bold.
     * @return True if font is bold, false otherwise
     */
    public abstract boolean isBold();

    /**
     * Checks if font is italic.
     * @return True if font is italic, false otherwise
     */
    public abstract boolean isItalic();

    /**
     * Checks if the font small flag is editable.
     * @return True if the font small flag is editable, false otherwise
     */
    public abstract boolean isSmallEditable();

    /**
     * Checks if the font bold flag is editable.
     * @return True if the font bold flag is editable, false otherwise
     */
    public abstract boolean isBoldEditable();

    /**
     * Checks if the font italic flag is editable.
     * @return True if the font italic flag is editable, false otherwise
     */
    public abstract boolean isItalicEditable();

    /**
     * Checks if the font name in the tag is editable.
     * @return True if the font name in the tag is editable, false otherwise
     */
    public abstract boolean isFontNameInTagEditable();

    /**
     * Checks if ascent is editable.
     * @return True if ascent is editable, false otherwise
     */
    public abstract boolean isAscentEditable();

    /**
     * Checks if descent is editable.
     * @return True if descent is editable, false otherwise
     */
    public abstract boolean isDescentEditable();

    /**
     * Checks if leading is editable.
     * @return True if leading is editable, false otherwise
     */
    public abstract boolean isLeadingEditable();

    /**
     * Sets small flag.
     * @param value Small flag value
     */
    public abstract void setSmall(boolean value);

    /**
     * Sets bold flag.
     * @param value Bold flag value
     */
    public abstract void setBold(boolean value);

    /**
     * Sets italic flag.
     * @param value Italic flag value
     */
    public abstract void setItalic(boolean value);

    /**
     * Gets divider of font units.
     * @return Divider of font units
     */
    public abstract double getDivider();

    /**
     * Gets ascent.
     * @return Ascent
     */
    public abstract int getAscent();

    /**
     * Gets descent.
     * @return Descent
     */
    public abstract int getDescent();

    /**
     * Gets leading.
     * @return Leading
     */
    public abstract int getLeading();

    /**
     * Sets ascent.
     * @param ascent Ascent
     */
    public abstract void setAscent(int ascent);

    /**
     * Sets descent.
     * @param descent Descent
     */
    public abstract void setDescent(int descent);

    /**
     * Sets leading.
     * @param leading Leading
     */
    public abstract void setLeading(int leading);

    /**
     * Sets has layout flag.
     * @param hasLayout Has layout flag
     */
    public abstract void setHasLayout(boolean hasLayout);

    /**
     * Gets font name.
     * @return Font name
     */
    public String getFontName() {
        DefineFontNameTag fontNameTag = getFontNameTag();
        if (fontNameTag == null) {
            return getFontNameIntag();
        }
        return fontNameTag.fontName;
    }

    /**
     * Gets font copyright.
     * @return Font copyright
     */
    public String getFontCopyright() {
        DefineFontNameTag fontNameTag = getFontNameTag();
        if (fontNameTag == null) {
            return "";
        }
        return fontNameTag.fontCopyright;
    }

    private static Map<String, Map<String, Font>> installedFontsByFamily;

    private static Map<String, Map<String, File>> installedFontFilesByFamily;

    private static Map<String, File> installedFontFilesByName;

    private static Map<String, Font> installedFontsByName;

    private static Map<Font, File> customFontToFile;

    private static String defaultFontName;

    private static boolean firstLoaded = false;

    private static Map<String, Map<String, Map<Integer, List<FontHelper.KerningPair>>>> installedFontKerningPairsByFamily;

    private static Map<Font, Map<Integer, List<FontHelper.KerningPair>>> customFontKerningPairs;

    /**
     * Gets font file from font name.
     * @param fontName Font name
     * @return Font file
     */
    public static File fontNameToFile(String fontName) {
        if (installedFontFilesByName.containsKey(fontName)) {
            return installedFontFilesByName.get(fontName);
        }
        return null;
    }

    private static void ensureLoaded() {
        if (!firstLoaded) {
            reload();
        }
        firstLoaded = true;
    }

    /**
     * Checks if font has layout.
     * @return True if font has layout, false otherwise
     */
    public boolean hasLayout() {
        return false;
    }

    /**
     * Checks if font contains character.
     * @param character Character
     * @return True if font contains character, false otherwise
     */
    public boolean containsChar(char character) {
        return charToGlyph(character) > -1;
    }

    /**
     * Gets font style.
     * @return Font style
     */
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

    /**
     * Gets character count.
     * @return Character count
     */
    public abstract int getCharacterCount();

    /**
     * Gets characters as string.
     * @return Characters as string
     */
    public abstract String getCharacters();

    @Override
    public Map<String, String> getNameProperties() {
        Map<String, String> ret = super.getNameProperties();
        ret.put("chid", "" + getCharacterId());
        String fontName = getFontNameIntag();
        if (fontName != null) {
            ret.put("fn", fontName);
        }
        return ret;
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

    /**
     * Gets system font name.
     * @return System font name
     */
    public String getSystemFontName() {
        int fontId = getCharacterId();
        String selectedFont = swf.sourceFontNamesMap.get(fontId);
        if (selectedFont == null) {
            SwfSpecificConfiguration swfConf = Configuration.getSwfSpecificConfiguration(swf.getShortPathTitle());
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

    /**
     * Gets system font.
     * @return System font
     */
    public Font getSystemFont() {
        return FontTag.installedFontsByName.get(getSystemFontName());
    }

    /**
     * Shifts glyph indices.
     * @param fontId Font ID
     * @param startIndex Start index
     * @param increment Increment
     */
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

    /**
     * Gets system font advance.
     * @param fontName Font name
     * @param fontStyle Font style
     * @param fontSize Font size
     * @param character Character
     * @param nextCharacter Next character
     * @return System font advance
     */
    public static float getSystemFontAdvance(String fontName, int fontStyle, int fontSize, Character character, Character nextCharacter) {
        return getSystemFontAdvance(new Font(fontName, fontStyle, fontSize), character, nextCharacter);
    }

    /**
     * Gets system font advance.
     * @param aFont Font
     * @param character Character
     * @param nextCharacter Next character
     * @return System font advance
     */
    public static float getSystemFontAdvance(Font aFont, Character character, Character nextCharacter) {
        String chars = "" + character + (nextCharacter == null ? "" : nextCharacter);
        GlyphVector gv = aFont.layoutGlyphVector(new FontRenderContext(aFont.getTransform(), true, true), chars.toCharArray(), 0, chars.length(), Font.LAYOUT_LEFT_TO_RIGHT);
        GlyphMetrics gm = gv.getGlyphMetrics(0);
        return gm.getAdvanceX();
    }

    /**
     * Gets default font name.
     * @return Default font name
     */
    public static String getDefaultFontName() {
        ensureLoaded();
        return defaultFontName;
    }

    /**
     * Gets font kerning pairs.
     * @param font Font
     * @param size Size
     * @return Font kerning pairs
     */
    protected static List<FontHelper.KerningPair> getFontKerningPairs(Font font, int size) {
        if (customFontToFile.containsKey(font)) {
            if (!customFontKerningPairs.containsKey(font) || !customFontKerningPairs.get(font).containsKey(size)) {
                if (!customFontKerningPairs.containsKey(font)) {
                    customFontKerningPairs.put(font, new HashMap<>());
                }
                customFontKerningPairs.get(font).put(size, FontHelper.getFontKerningPairs(customFontToFile.get(font), size));
            }
            return customFontKerningPairs.get(font).get(size);
        }
        if (installedFontKerningPairsByFamily.containsKey(font.getFamily(Locale.ENGLISH))
                && installedFontKerningPairsByFamily.get(font.getFamily()).containsKey(font.getFontName(Locale.ENGLISH))
                && installedFontKerningPairsByFamily.get(font.getFamily()).get(font.getFontName(Locale.ENGLISH)).containsKey(size)) {
            return installedFontKerningPairsByFamily.get(font.getFamily()).get(font.getFontName(Locale.ENGLISH)).get(size);
        }

        if (installedFontFilesByFamily.containsKey(font.getFamily(Locale.ENGLISH)) && installedFontFilesByFamily.get(font.getFamily()).containsKey(font.getFontName(Locale.ENGLISH))) {
            File file = installedFontFilesByFamily.get(font.getFamily(Locale.ENGLISH)).get(font.getFontName(Locale.ENGLISH));
            if (!installedFontKerningPairsByFamily.containsKey(font.getFamily(Locale.ENGLISH))) {
                installedFontKerningPairsByFamily.put(font.getFamily(Locale.ENGLISH), new HashMap<>());
            }
            if (!installedFontKerningPairsByFamily.get(font.getFamily(Locale.ENGLISH)).containsKey(font.getFontName(Locale.ENGLISH))) {
                installedFontKerningPairsByFamily.get(font.getFamily(Locale.ENGLISH)).put(font.getFontName(Locale.ENGLISH), new HashMap<>());
            }

            installedFontKerningPairsByFamily.get(font.getFamily(Locale.ENGLISH)).get(font.getFontName(Locale.ENGLISH)).put(size, FontHelper.getFontKerningPairs(file, size));
        }
        if (installedFontKerningPairsByFamily.containsKey(font.getFamily(Locale.ENGLISH))
                && installedFontKerningPairsByFamily.get(font.getFamily()).containsKey(font.getFontName(Locale.ENGLISH))
                && installedFontKerningPairsByFamily.get(font.getFamily()).get(font.getFontName(Locale.ENGLISH)).containsKey(size)) {
            return installedFontKerningPairsByFamily.get(font.getFamily()).get(font.getFontName(Locale.ENGLISH)).get(size);
        }
        return new ArrayList<>();
    }

    /**
     * Adds custom font.
     * @param font Font
     * @param file File
     */
    public static void addCustomFont(Font font, File file) {
        customFontToFile.put(font, file);
    }

    /**
     * Reloads fonts.
     */
    public static void reload() {
        installedFontKerningPairsByFamily = new HashMap<>();
        installedFontsByFamily = FontHelper.getInstalledFonts();
        installedFontFilesByFamily = FontHelper.getInstalledFontFiles();
        installedFontsByName = new HashMap<>();
        installedFontFilesByName = new HashMap<>();
        customFontToFile = new HashMap<>();
        customFontKerningPairs = new HashMap<>();

        for (String fam : installedFontsByFamily.keySet()) {
            for (String nam : installedFontsByFamily.get(fam).keySet()) {
                installedFontsByName.put(nam, installedFontsByFamily.get(fam).get(nam));
            }
        }

        for (String fam : installedFontFilesByFamily.keySet()) {
            for (String nam : installedFontFilesByFamily.get(fam).keySet()) {
                installedFontFilesByName.put(nam, installedFontFilesByFamily.get(fam).get(nam));
            }
        }

        if (installedFontsByFamily.containsKey("Times New Roman")) {
            defaultFontName = installedFontsByFamily.get("Times New Roman").keySet().iterator().next();
        } else if (installedFontsByFamily.containsKey("Arial")) {
            defaultFontName = installedFontsByFamily.get("Arial").keySet().iterator().next();
        } else {
            defaultFontName = installedFontsByFamily.get(installedFontsByFamily.keySet().iterator().next()).keySet().iterator().next();
        }
    }

    /**
     * Gets font name with fallback.
     * @param fontName Font name
     * @return Font name with fallback
     */
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

        return installedFontsByFamily.keySet().iterator().next();
    }

    public static boolean isFontNameInstalled(String fontName) {
        ensureLoaded();
        return installedFontsByName.containsKey(fontName);
    }
    
    /**
     * Checks if font family is installed.
     * @param fontFamily Font family
     * @return Installed font family
     */
    public static String isFontFamilyInstalled(String fontFamily) {
        ensureLoaded();
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

    /**
     * Finds installed font name.
     * @param fontName Font name
     * @return Installed font name
     */
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
    public Shape getOutline(boolean fast, int frame, int time, int ratio, RenderContext renderContext, Matrix transformation, boolean stroked, ExportRectangle viewRect, double unzoom) {
        RECT r = getRect();
        return new Area(new Rectangle(r.Xmin, r.Ymin, r.getWidth(), r.getHeight()));
    }

    @Override
    public synchronized void toImage(int frame, int time, int ratio, RenderContext renderContext, SerializableImage image, SerializableImage fullImage, boolean isClip, Matrix transformation, Matrix strokeTransformation, Matrix absoluteTransformation, Matrix fullTransformation, ColorTransform colorTransform, double unzoom, boolean sameImage, ExportRectangle viewRect, ExportRectangle viewRectRaw, boolean scaleStrokes, int drawMode, int blendMode, boolean canUseSmoothing) {
        SHAPERECORD.shapeListToImage(ShapeTag.WIND_EVEN_ODD, 1, swf, getGlyphShapeTable(), image, frame, Color.black, colorTransform);
    }

    @Override
    public void toSVG(SVGExporter exporter, int ratio, ColorTransform colorTransform, int level, Matrix transformation, Matrix strokeTransformation) {

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
            CanvasShapeExporter exporter = new CanvasShapeExporter(ShapeTag.WIND_EVEN_ODD, 1, null, unitDivisor, swf, shapes.get(i), null, 0, 0);
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
    public RECT getRectWithStrokes() {
        return getRect();
    }

    @Override
    public String getCharacterExportFileName() {
        return super.getCharacterExportFileName() + "_" + getFontNameIntag();
    }

    /**
     * Gets DefineFontName tag.
     * @return DefineFontNameTag tag
     */
    public DefineFontNameTag getFontNameTag() {
        if (swf == null) {
            return null;
        }
        for (Tag t : swf.getTags()) {
            if (t instanceof DefineFontNameTag) {
                DefineFontNameTag dfn = (DefineFontNameTag) t;
                if (dfn.fontId == getCharacterId()) {
                    return dfn;
                }
            }
        }
        return null;
    }

    /**
     * Gets copyright.
     * @return Copyright
     */
    public String getCopyright() {
        DefineFontNameTag dfn = getFontNameTag();
        if (dfn == null) {
            return null;
        }
        return dfn.fontCopyright;
    }

    /**
     * Gets glyph bounds.
     * @param glyphIndex Glyph index
     * @return Glyph bounds
     */
    public RECT getGlyphBounds(int glyphIndex) {
        return getGlyphShapeTable().get(glyphIndex).getBounds(1);
    }

    /**
     * Converts font to classic font tag. (= not GFX and such)
     * @return Classic font tag
     */
    public FontTag toClassicFont() {
        return this;
    }

    /**
     * Gets installed fonts by family.
     * @return Installed fonts by family
     */
    public static Map<String, Map<String, Font>> getInstalledFontsByFamily() {
        ensureLoaded();
        return installedFontsByFamily;
    }

    /**
     * Gets installed fonts by family.
     * @return Installed fonts by family
     */
    public static Map<String, Font> getInstalledFontsByName() {
        ensureLoaded();
        return installedFontsByName;
    }

    /**
     * Gets codes charset.
     * @return Codes charset
     */
    public abstract String getCodesCharset();

}
