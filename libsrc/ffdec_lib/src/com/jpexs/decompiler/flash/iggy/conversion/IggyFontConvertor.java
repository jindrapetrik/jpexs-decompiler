/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.iggy.conversion;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.iggy.IggyCharAdvances;
import com.jpexs.decompiler.flash.iggy.IggyCharIndices;
import com.jpexs.decompiler.flash.iggy.IggyCharKerning;
import com.jpexs.decompiler.flash.iggy.IggyCharOffset;
import com.jpexs.decompiler.flash.iggy.IggyFlashHeader32;
import com.jpexs.decompiler.flash.iggy.IggyFont;
import com.jpexs.decompiler.flash.iggy.IggyShape;
import com.jpexs.decompiler.flash.iggy.IggySwf;
import com.jpexs.decompiler.flash.tags.DefineFont2Tag;
import com.jpexs.decompiler.flash.tags.DefineFont3Tag;
import com.jpexs.decompiler.flash.tags.DefineFontInfo2Tag;
import com.jpexs.decompiler.flash.tags.DefineFontInfoTag;
import com.jpexs.decompiler.flash.tags.DefineFontTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.FontInfoTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.types.KERNINGRECORD;
import com.jpexs.decompiler.flash.types.LANGCODE;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Native embedded fonts shared by the 32 legacy, 32 and 64 layouts.
 */
public final class IggyFontConvertor {

    private IggyFontConvertor() {
    }

    private static SHAPE emptyShape() {
        SHAPE shape = new SHAPE();
        shape.shapeRecords = new ArrayList<>();
        shape.shapeRecords.add(new EndShapeRecord());
        return shape;
    }

    public static FontTag toSwf(SWF swf, IggyFont font) {
        // Outlines/advances are normalized em values in Iggy. Metrics and kerning
        // retain the source tag's integer units, so export the original font variant.
        int units = font.getFontType() == 3 ? 20480 : 1024;
        List<SHAPE> shapes = new ArrayList<>();
        for (int i = 0; i < font.getCharacterCount(); i++) {
            IggyShape shape = font.getChars().get(i);
            shapes.add(shape == null ? emptyShape() : IggyShapeToSwfConvertor.convertCharToShape(shape, units));
        }
        // A bare DefineFont has no character code table or layout.
        if (font.getCharIndices() == null && !shapes.isEmpty()) {
            DefineFontTag tag = new DefineFontTag(swf);
            tag.fontId = font.getFontId();
            tag.glyphShapeTable = shapes;
            return tag;
        }
        DefineFont2Tag tag = new DefineFont2Tag(swf);
        tag.fontID = font.getFontId();
        tag.fontFlagsHasLayout = font.hasLayout();
        tag.fontFlagsShiftJIS = font.isShiftJIS();
        tag.fontFlagsSmallText = font.isSmallText();
        tag.fontFlagsANSI = font.isAnsi();
        tag.fontFlagsBold = font.isBold();
        tag.fontFlagsItalic = font.isItalic();
        tag.fontFlagsWideCodes = true;
        tag.fontFlagsWideOffsets = true;
        tag.languageCode = new LANGCODE(font.getLanguageCode());
        tag.fontName = font.getName() == null ? "" : font.getName();
        tag.fontAscent = font.getAscent();
        tag.fontDescent = font.getDescent();
        tag.fontLeading = font.getLeading();
        tag.glyphShapeTable = shapes;
        tag.codeTable = new ArrayList<>();
        tag.fontAdvanceTable = new ArrayList<>();
        tag.fontBoundsTable = new ArrayList<>();
        tag.fontKerningTable = new ArrayList<>();
        if (font.getCharIndices() != null) {
            for (char code : font.getCharIndices().getChars()) {
                tag.codeTable.add((int) code);
            }
        }
        if (tag.fontFlagsHasLayout) {
            if ((font.getCharAdvances() == null && !shapes.isEmpty())
                    || (font.getCharAdvances() != null && font.getCharAdvances().getScales().size() != shapes.size())) {
                throw new IllegalArgumentException("Missing Iggy font advance table");
            }
            for (int i = 0; i < shapes.size(); i++) {
                tag.fontAdvanceTable.add((int) Math.round(font.getCharAdvances().getScales().get(i) * (double) units));
                tag.fontBoundsTable.add(shapes.get(i).getBounds(1));
            }
            IggyCharKerning kern = font.getCharKernings();
            if (kern != null) {
                for (int i = 0; i < kern.getKernCount(); i++) {
                    tag.fontKerningTable.add(new KERNINGRECORD(kern.getCharsA().get(i), kern.getCharsB().get(i), kern.getKerningOffsets().get(i)));
                }
            }
        }
        if (font.getFontType() != 3) {
            return tag;
        }
        DefineFont3Tag result = new DefineFont3Tag(swf);
        result.fontID = tag.fontID;
        result.fontFlagsHasLayout = tag.fontFlagsHasLayout;
        result.fontFlagsShiftJIS = tag.fontFlagsShiftJIS;
        result.fontFlagsSmallText = tag.fontFlagsSmallText;
        result.fontFlagsANSI = tag.fontFlagsANSI;
        result.fontFlagsBold = tag.fontFlagsBold;
        result.fontFlagsItalic = tag.fontFlagsItalic;
        result.fontFlagsWideCodes = tag.fontFlagsWideCodes;
        result.fontFlagsWideOffsets = tag.fontFlagsWideOffsets;
        result.languageCode = tag.languageCode;
        result.fontName = tag.fontName;
        result.fontAscent = tag.fontAscent;
        result.fontDescent = tag.fontDescent;
        result.fontLeading = tag.fontLeading;
        result.glyphShapeTable = tag.glyphShapeTable;
        result.codeTable = tag.codeTable;
        result.fontAdvanceTable = tag.fontAdvanceTable;
        result.fontBoundsTable = tag.fontBoundsTable;
        result.fontKerningTable = tag.fontKerningTable;
        return result;
    }

    private static DefineFont2Tag fontData(FontTag font) throws IOException {
        if (font instanceof DefineFont2Tag) {
            return (DefineFont2Tag) font;
        }
        DefineFont2Tag result = new DefineFont2Tag(font.getSwf());
        if (font instanceof DefineFont3Tag) {
            DefineFont3Tag tag = (DefineFont3Tag) font;
            result.fontID = tag.fontID;
            result.fontFlagsHasLayout = tag.fontFlagsHasLayout;
            result.fontFlagsShiftJIS = tag.fontFlagsShiftJIS;
            result.fontFlagsSmallText = tag.fontFlagsSmallText;
            result.fontFlagsANSI = tag.fontFlagsANSI;
            result.fontFlagsBold = tag.fontFlagsBold;
            result.fontFlagsItalic = tag.fontFlagsItalic;
            result.fontFlagsWideCodes = tag.fontFlagsWideCodes;
            result.fontFlagsWideOffsets = tag.fontFlagsWideOffsets;
            result.languageCode = tag.languageCode;
            result.fontName = tag.fontName;
            result.fontAscent = tag.fontAscent;
            result.fontDescent = tag.fontDescent;
            result.fontLeading = tag.fontLeading;
            result.glyphShapeTable = tag.glyphShapeTable;
            result.codeTable = tag.codeTable;
            result.fontAdvanceTable = tag.fontAdvanceTable;
            result.fontBoundsTable = tag.fontBoundsTable;
            result.fontKerningTable = tag.fontKerningTable;
            return result;
        }
        if (!(font instanceof DefineFontTag)) {
            throw new IggyUnsupportedTagException(font, "Unsupported Iggy embedded font tag " + font.getId());
        }
        result.fontID = font.getCharacterId();
        result.glyphShapeTable = font.getGlyphShapeTable();
        result.fontFlagsHasLayout = false;
        result.fontName = "";
        result.codeTable = null;
        FontInfoTag info = null;
        for (Tag tag : IggyDefinitionConvertor.tags(font.getSwf().getTags())) {
            if (tag instanceof FontInfoTag && ((FontInfoTag) tag).fontID == result.fontID) {
                if (info != null) {
                    throw new IOException("Duplicate font info for Iggy font " + result.fontID);
                }
                info = (FontInfoTag) tag;
            }
        }
        if (info != null) {
            result.fontName = info.getFontName();
            result.codeTable = info.getCodeTable();
            result.fontFlagsShiftJIS = info.isShiftJIS();
            result.fontFlagsANSI = info.isAnsi();
            result.fontFlagsBold = info.getFontFlagsBold();
            result.fontFlagsItalic = info.getFontFlagsItalic();
            if (info instanceof DefineFontInfoTag) {
                result.fontFlagsSmallText = ((DefineFontInfoTag) info).fontFlagsSmallText;
            } else if (info instanceof DefineFontInfo2Tag) {
                result.fontFlagsSmallText = ((DefineFontInfo2Tag) info).fontFlagsSmallText;
                result.languageCode = ((DefineFontInfo2Tag) info).languageCode;
            }
        }
        return result;
    }

    private static byte[] bytes(Tag tag) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (SWFOutputStream out = new SWFOutputStream(bytes, tag.getVersion(), tag.getCharset())) {
            tag.getData(out);
        }
        return bytes.toByteArray();
    }

    private static byte[] bytes(SHAPE shape) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (SWFOutputStream out = new SWFOutputStream(bytes, 10, "UTF-8")) {
            out.writeSHAPE(shape, 1);
        }
        return bytes.toByteArray();
    }

    public static void updateFonts(IggySwf movie, SWF swf) throws IOException {
        Map<Integer, FontTag> fonts = new HashMap<>();
        for (Tag tag : IggyDefinitionConvertor.tags(swf.getTags())) {
            if (tag instanceof FontTag) {
                FontTag font = (FontTag) tag;
                if (fonts.put(font.getCharacterId(), font) != null) {
                    throw new IOException("Duplicate Iggy font ID " + font.getCharacterId());
                }
            }
        }
        Map<Integer, IggyFont> originals = new HashMap<>();
        for (IggyFont font : movie.getFonts()) {
            originals.put(font.getFontId(), font);
        }
        for (FontTag tag : fonts.values()) {
            IggyFont font = originals.get(tag.getCharacterId());
            boolean added = font == null;
            if (added) {
                font = IggyFont.create(movie.is64(), movie.getHdr() instanceof IggyFlashHeader32, tag.getCharacterId());
            }
            updateFont(font, tag);
            if (added) {
                movie.replaceDefinition(font);
            }
        }
    }

    public static void updateFont(IggyFont font, FontTag source) throws IOException {
        DefineFont2Tag tag = fontData(source);
        int count = tag.glyphShapeTable.size();
        boolean legacy = source instanceof DefineFontTag;
        boolean layout = !legacy && tag.fontFlagsHasLayout;
        int type = source instanceof DefineFont3Tag ? 3 : legacy ? 1 : 2;
        int units = type == 3 ? 20480 : 1024;
        if (count > 65535 || (tag.codeTable != null && tag.codeTable.size() != count)
                || (!legacy && tag.codeTable == null)) {
            throw new IOException("Invalid Iggy font code table size");
        }
        if (layout && (tag.fontAdvanceTable == null || tag.fontAdvanceTable.size() != count
                || tag.fontKerningTable == null || tag.fontKerningTable.size() > 65535)) {
            throw new IOException("Invalid Iggy font layout table size");
        }
        if (layout) {
            if (tag.fontAscent < 0 || tag.fontAscent > 65535 || tag.fontDescent < 0 || tag.fontDescent > 65535
                    || tag.fontLeading < -32768 || tag.fontLeading > 32767) {
                throw new IOException("Iggy font metrics outside 16-bit range");
            }
            for (int value : tag.fontAdvanceTable) {
                if (value < 0 || value > 65535) {
                    throw new IOException("Iggy font advance outside UI16 range");
                }
            }
            for (KERNINGRECORD kern : tag.fontKerningTable) {
                if (kern.fontKerningAdjustment < -32768 || kern.fontKerningAdjustment > 32767
                        || kern.fontKerningCode1 < 0 || kern.fontKerningCode1 > 65535
                        || kern.fontKerningCode2 < 0 || kern.fontKerningCode2 > 65535) {
                    throw new IOException("Iggy font kerning outside 16-bit range");
                }
            }
        }
        if (tag.codeTable != null) {
            for (int code : tag.codeTable) {
                if (code < 0 || code > 65535) {
                    throw new IOException("Iggy font character code outside UI16 range");
                }
            }
        }
        // Keep outlines, cached descriptors and metrics exactly as loaded when no edit was made.
        if (font.getChars() != null) {
            FontTag original = toSwf(source.getSwf(), font);
            if (!legacy && original.getId() == source.getId() && Arrays.equals(bytes(original), bytes(source))) {
                return;
            }
        }
        List<IggyShape> glyphs = new ArrayList<>();
        List<IggyCharOffset> offsets = new ArrayList<>();
        float[] bounds = new float[5];
        boolean haveBounds = false;
        for (int i = 0; i < count; i++) {
            SHAPE shape = tag.glyphShapeTable.get(i);
            IggyShape old = font.getChars() != null && i < font.getChars().size() ? font.getChars().get(i) : null;
            SHAPE oldShape = old == null ? emptyShape() : IggyShapeToSwfConvertor.convertCharToShape(old, units);
            boolean same = Arrays.equals(bytes(oldShape), bytes(shape));
            IggyShape glyph = same ? old : SwfShapeToIggyConvertor.convertShape(1, shape, units);
            if (glyph != null && glyph.getNodes().isEmpty()) {
                glyph = null;
            }
            glyphs.add(glyph);
            offsets.add(same && font.getCharOffsets() != null && i < font.getCharOffsets().size()
                    ? font.getCharOffsets().get(i) : new IggyCharOffset(glyph == null ? 0 : 1, glyph != null && glyph.getNodes().stream()
                    .anyMatch(node -> node.getNodeType() == 3) ? 1 : 0, 0, 0));
            if (glyph != null) {
                if (!haveBounds) {
                    bounds[0] = glyph.getMinx();
                    bounds[1] = glyph.getMiny();
                    bounds[2] = glyph.getMaxx();
                    bounds[3] = glyph.getMaxy();
                    haveBounds = true;
                } else {
                    bounds[0] = Math.min(bounds[0], glyph.getMinx());
                    bounds[1] = Math.min(bounds[1], glyph.getMiny());
                    bounds[2] = Math.max(bounds[2], glyph.getMaxx());
                    bounds[3] = Math.max(bounds[3], glyph.getMaxy());
                }
            }
        }
        List<Character> codes = new ArrayList<>();
        if (tag.codeTable != null) {
            for (int code : tag.codeTable) {
                codes.add((char) code);
            }
        }
        List<Float> advances = new ArrayList<>();
        List<Character> left = new ArrayList<>();
        List<Character> right = new ArrayList<>();
        List<Short> adjustments = new ArrayList<>();
        if (layout) {
            for (int advance : tag.fontAdvanceTable) {
                advances.add(advance / (float) units);
            }
            for (KERNINGRECORD kern : tag.fontKerningTable) {
                left.add((char) kern.fontKerningCode1);
                right.add((char) kern.fontKerningCode2);
                adjustments.add((short) kern.fontKerningAdjustment);
            }
        }
        font.setFontFlags(type, tag.languageCode.languageCode, layout, tag.fontFlagsShiftJIS,
                tag.fontFlagsSmallText, tag.fontFlagsANSI, tag.fontFlagsBold, tag.fontFlagsItalic);
        font.setCharCount(count);
        font.setCharCount2(count);
        font.setAscent(layout ? tag.fontAscent : 0);
        font.setDescent(layout ? tag.fontDescent : 0);
        font.setLeading(layout ? tag.fontLeading : 0);
        font.setName(tag.fontName);
        font.setUnkFloat(bounds);
        font.setGlyphs(glyphs);
        font.setCharOffsets(offsets);
        font.setCodePoints(tag.codeTable == null ? null : new IggyCharIndices(codes));
        font.setCharScales(layout ? new IggyCharAdvances(advances) : null);
        font.setCharKernings(layout && !left.isEmpty() ? new IggyCharKerning(left, right, adjustments) : null);
    }
}
