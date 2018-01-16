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
package com.jpexs.decompiler.flash.helpers;

import java.awt.Canvas;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextAttribute;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public class FontHelper {

    private static Object getFontManager() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Class<?> clFmFactory = Class.forName("sun.font.FontManagerFactory");
        return clFmFactory.getDeclaredMethod("getInstance").invoke(null);
    }

    /**
     * Gets all available fonts in the system
     *
     * @return Map<FamilyName,Map<FontNAme,Font>>
     */
    public static Map<String, Map<String, Font>> getInstalledFonts() {
        Map<String, Map<String, Font>> ret = new HashMap<>();
        Font[] fonts = null;

        try {

            Object fm = getFontManager();
            Class<?> clFm = Class.forName("sun.font.SunFontManager");

            // Delete cached installed names
            Field inField = clFm.getDeclaredField("installedNames");
            inField.setAccessible(true);
            inField.set(null, null);
            inField.setAccessible(false);

            // Delete cached family names
            Field allFamField = clFm.getDeclaredField("allFamilies");
            allFamField.setAccessible(true);
            allFamField.set(fm, null);
            allFamField.setAccessible(false);

            // Delete cached fonts
            Field allFonField = clFm.getDeclaredField("allFonts");
            allFonField.setAccessible(true);
            allFonField.set(fm, null);
            allFonField.setAccessible(false);

            fonts = (Font[]) clFm.getDeclaredMethod("getAllInstalledFonts").invoke(fm);
        } catch (Throwable ex) {
            // ignore
        }

        if (fonts == null) {
            fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        }

        List<String> javaFonts = Arrays.asList("Dialog", "DialogInput", "Monospaced", "Serif", "SansSerif");
        for (Font f : fonts) {
            String fam = f.getFamily(Locale.ENGLISH);
            // Do not want Java logical fonts
            if (javaFonts.contains(fam)) {
                continue;
            }
            if (!ret.containsKey(fam)) {
                ret.put(fam, new HashMap<>());
            }

            ret.get(fam).put(f.getFontName(Locale.ENGLISH), f);
        }

        return ret;
    }

    public static String fontToString(Font font) {
        int style = font.getStyle();
        String styleString;
        switch (style) {
            case 1:
                styleString = "Bold";
                break;
            case 2:
                styleString = "Italic";
                break;
            case 3:
                styleString = "BoldItalic";
                break;
            default:
                styleString = "Plain";
                break;
        }

        return font.getName() + "-" + styleString + "-" + font.getSize();
    }

    public static Font stringToFont(String fontString) {
        return Font.decode(fontString);
    }

    /**
     * Gets kerning offset for two characters of the font
     *
     * @param font Font
     * @param char1 First character
     * @param char2 Second character
     * @return offset
     */
    public static int getFontCharsKerning(Font font, char char1, char char2) {
        char[] chars = new char[]{char1, char2};
        Map<AttributedCharacterIterator.Attribute, Object> withKerningAttrs = new HashMap<>();

        withKerningAttrs.put(TextAttribute.FONT, font);
        withKerningAttrs.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
        Font withKerningFont = Font.getFont(withKerningAttrs);
        GlyphVector withKerningVector = withKerningFont.layoutGlyphVector(getFontRenderContext(withKerningFont), chars, 0, chars.length, Font.LAYOUT_LEFT_TO_RIGHT);
        int withKerningX = withKerningVector.getGlyphLogicalBounds(1).getBounds().x;

        Map<AttributedCharacterIterator.Attribute, Object> noKerningAttrs = new HashMap<>();
        noKerningAttrs.put(TextAttribute.FONT, font);
        noKerningAttrs.put(TextAttribute.KERNING, 0);
        Font noKerningFont = Font.getFont(noKerningAttrs);
        GlyphVector noKerningVector = noKerningFont.layoutGlyphVector(getFontRenderContext(noKerningFont), chars, 0, chars.length, Font.LAYOUT_LEFT_TO_RIGHT);
        int noKerningX = noKerningVector.getGlyphLogicalBounds(1).getBounds().x;
        return withKerningX - noKerningX;
    }

    /**
     * Gets all kerning pairs of a Font. It is very slow.
     *
     * @param font
     * @param size
     * @return
     */
    public static List<KerningPair> getFontKerningPairs(Font font, int size) {
        File fontFile = getFontFile(font);
        if (fontFile != null && fontFile.getName().toLowerCase().endsWith(".ttf")) {
            KerningLoader k = new KerningLoader();
            try {
                return k.loadFromTTF(fontFile, size);
            } catch (IOException | FontFormatException ex) {
                // ignore
            }
        }
        List<KerningPair> ret = new ArrayList<>();

        List<Character> availableChars = new ArrayList<>();
        for (char c1 = 0; c1 < Character.MAX_VALUE; c1++) {
            if (font.canDisplay((int) c1)) {
                availableChars.add(c1);
            }
        }
        for (char c1 : availableChars) {
            ret.addAll(getFontKerningPairsOneChar(availableChars, font, c1));

        }
        return ret;
    }

    public static float getFontAdvance(Font font, char ch) {
        return createGlyphVector(font, ch).getGlyphMetrics(0).getAdvanceX();
    }

    public static GlyphVector createGlyphVector(Font font, char ch) {
        return font.createGlyphVector(getFontRenderContext(font), new char[]{ch});
    }

    private static FontRenderContext getFontRenderContext(Font font) {
        // Canvas works in headless mode
        return (new Canvas()).getFontMetrics(font).getFontRenderContext();
    }

    private static List<KerningPair> getFontKerningPairsOneChar(List<Character> availableChars, Font font, char firstChar) {
        List<KerningPair> ret = new ArrayList<>();

        char[] chars = new char[availableChars.size() * 2];

        for (int i = 0; i < availableChars.size(); i++) {
            chars[i * 2] = firstChar;
            chars[i * 2 + 1] = availableChars.get(i);
        }

        Map<AttributedCharacterIterator.Attribute, Object> withKerningAttrs = new HashMap<>();

        withKerningAttrs.put(TextAttribute.FONT, font);
        withKerningAttrs.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
        Font withKerningFont = Font.getFont(withKerningAttrs);
        GlyphVector withKerningVector = withKerningFont.layoutGlyphVector(getFontRenderContext(withKerningFont), chars, 0, chars.length, Font.LAYOUT_LEFT_TO_RIGHT);
        int[] withKerningX = new int[availableChars.size()];
        for (int i = 0; i < availableChars.size(); i++) {
            withKerningX[i] = withKerningVector.getGlyphLogicalBounds(i * 2 + 1).getBounds().x;
        }

        Map<AttributedCharacterIterator.Attribute, Object> noKerningAttrs = new HashMap<>();
        noKerningAttrs.put(TextAttribute.FONT, font);
        noKerningAttrs.put(TextAttribute.KERNING, 0);
        Font noKerningFont = Font.getFont(noKerningAttrs);
        GlyphVector noKerningVector = noKerningFont.layoutGlyphVector(getFontRenderContext(noKerningFont), chars, 0, chars.length, Font.LAYOUT_LEFT_TO_RIGHT);
        for (int i = 0; i < availableChars.size(); i++) {
            int noKerningX = noKerningVector.getGlyphLogicalBounds(i * 2 + 1).getBounds().x;
            int kerning = withKerningX[i] - noKerningX;
            if (kerning > 0) {
                ret.add(new KerningPair(firstChar, availableChars.get(i), kerning));
            }
        }
        return ret;
    }

    public static class KerningPair {

        public final char char1;

        public final char char2;

        public int kerning;

        public KerningPair(char char1, char char2, int kerning) {
            this.char1 = char1;
            this.char2 = char2;
            this.kerning = kerning;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 67 * hash + this.char1;
            hash = 67 * hash + this.char2;
            hash = 67 * hash + this.kerning;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final KerningPair other = (KerningPair) obj;
            if (char1 != other.char1) {
                return false;
            }
            if (char2 != other.char2) {
                return false;
            }
            if (kerning != other.kerning) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "'" + char1 + "','" + char2 + "' => " + kerning;
        }
    }

    private static Object getFont2d(Font f) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Object fm = getFontManager();
        return Class.forName("sun.font.FontManager").getDeclaredMethod("findFont2D", String.class, int.class, int.class).invoke(fm, f.getFontName(), f.getStyle(), 2/*LOGICAL_FALLBACK*/);
    }

    public static File getFontFile(Font f) {
        try {
            Class pfClass = Class.forName("sun.font.PhysicalFont");
            Field platName = pfClass.getDeclaredField("platName");
            platName.setAccessible(true);
            String fontPath = (String) platName.get(getFont2d(f));
            platName.setAccessible(false);
            return new File(fontPath);
        } catch (Throwable e) {
            return null;
        }
    }

    private static Map<Integer, Character> getFontGlyphToCharMap(Font f) {
        Map<Integer, Character> ret = new HashMap<>();
        FontRenderContext frc = new FontRenderContext(null, true, false);
        for (char i = 0; i < Character.MAX_VALUE; i++) {
            if (f.canDisplay(i)) {
                GlyphVector gv = f.createGlyphVector(frc, new char[]{i});
                ret.put(gv.getGlyphCode(0), i);
            }
        }
        return ret;
    }

    private static class KerningLoader {

        private int size = -1;

        private float scale;

        private long bytePosition;

        private long headOffset = -1;

        private long kernOffset = -1;

        private Font font;

        private Map<Integer, Character> charmap;

        public List<KerningPair> loadFromTTF(File file, int size) throws IOException, FontFormatException {
            font = Font.createFont(Font.TRUETYPE_FONT, file);
            charmap = getFontGlyphToCharMap(font);
            InputStream input = new FileInputStream(file);
            List<KerningPair> ret = new ArrayList<>();
            this.size = size;
            if (input == null) {
                throw new IllegalArgumentException("input cannot be null.");
            }
            readTableDirectory(input);
            if (headOffset == -1) {
                throw new IOException("HEAD table not found.");
            }
            if (kernOffset == -1) {
                return ret;
            }
            if (headOffset < kernOffset) {
                readHEAD(input);
                readKERN(input, ret);
            } else {
                readKERN(input, ret);
                readHEAD(input);
            }
            input.close();
            for (KerningPair kp : ret) {
                kp.kerning *= scale;
            }
            return ret;
        }

        private void readTableDirectory(InputStream input) throws IOException {
            skip(input, 4);
            int tableCount = readUnsignedShort(input);
            skip(input, 6);

            byte[] tagBytes = new byte[4];
            for (int i = 0; i < tableCount; i++) {
                tagBytes[0] = readByte(input);
                tagBytes[1] = readByte(input);
                tagBytes[2] = readByte(input);
                tagBytes[3] = readByte(input);
                skip(input, 4);
                long offset = readUnsignedLong(input);
                skip(input, 4);

                String tag = new String(tagBytes, "ISO-8859-1");
                if (tag.equals("head")) {
                    headOffset = offset;
                    if (kernOffset != -1) {
                        break;
                    }
                } else if (tag.equals("kern")) {
                    kernOffset = offset;
                    if (headOffset != -1) {
                        break;
                    }
                }
            }
        }

        private void readHEAD(InputStream input) throws IOException {
            seek(input, headOffset + 2 * 4 + 2 * 4 + 2);
            int unitsPerEm = readUnsignedShort(input);
            scale = (float) size / unitsPerEm;
        }

        private void readKERN(InputStream input, List<KerningPair> ret) throws IOException {
            seek(input, kernOffset + 2);
            for (int subTableCount = readUnsignedShort(input); subTableCount > 0; subTableCount--) {
                skip(input, 2 * 2);
                int tupleIndex = readUnsignedShort(input);
                if (!((tupleIndex & 1) != 0) || (tupleIndex & 2) != 0 || (tupleIndex & 4) != 0) {
                    return;
                }
                if (tupleIndex >> 8 != 0) {
                    continue;
                }

                int kerningCount = readUnsignedShort(input);
                skip(input, 3 * 2);
                while (kerningCount-- > 0) {
                    int firstGlyphCode = readUnsignedShort(input);
                    int secondGlyphCode = readUnsignedShort(input);
                    int offset = readShort(input);
                    ret.add(new KerningPair(charmap.get(firstGlyphCode), charmap.get(secondGlyphCode), offset));
                }
            }
        }

        private int readUnsignedByte(InputStream input) throws IOException {
            bytePosition++;
            int b = input.read();
            if (b == -1) {
                throw new EOFException("Unexpected end of file.");
            }
            return b;
        }

        private byte readByte(InputStream input) throws IOException {
            return (byte) readUnsignedByte(input);
        }

        private int readUnsignedShort(InputStream input) throws IOException {
            return (readUnsignedByte(input) << 8) + readUnsignedByte(input);
        }

        private short readShort(InputStream input) throws IOException {
            return (short) readUnsignedShort(input);
        }

        private long readUnsignedLong(InputStream input) throws IOException {
            long value = readUnsignedByte(input);
            value = (value << 8) + readUnsignedByte(input);
            value = (value << 8) + readUnsignedByte(input);
            value = (value << 8) + readUnsignedByte(input);
            return value;
        }

        private void skip(InputStream input, long skip) throws IOException {
            while (skip > 0) {
                long skipped = input.skip(skip);
                if (skipped <= 0) {
                    break;
                }
                bytePosition += skipped;
                skip -= skipped;
            }
        }

        private void seek(InputStream input, long position) throws IOException {
            skip(input, position - bytePosition);
        }
    }
}
