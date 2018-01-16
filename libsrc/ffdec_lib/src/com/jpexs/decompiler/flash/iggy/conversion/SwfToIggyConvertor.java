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
package com.jpexs.decompiler.flash.iggy.conversion;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.iggy.IggyCharAdvances;
import com.jpexs.decompiler.flash.iggy.IggyCharIndices;
import com.jpexs.decompiler.flash.iggy.IggyCharKerning;
import com.jpexs.decompiler.flash.iggy.IggyCharOffset;
import com.jpexs.decompiler.flash.iggy.IggyFont;
import com.jpexs.decompiler.flash.iggy.IggyShape;
import com.jpexs.decompiler.flash.iggy.IggySwf;
import com.jpexs.decompiler.flash.iggy.IggyText;
import com.jpexs.decompiler.flash.tags.DefineEditTextTag;
import com.jpexs.decompiler.flash.tags.DefineFont2Tag;
import com.jpexs.decompiler.flash.tags.DoABC2Tag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.types.KERNINGRECORD;
import com.jpexs.decompiler.flash.types.SHAPE;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class SwfToIggyConvertor {

    private static float normalizeLengths(int val) {
        return (val / 1024f);
    }

    public static void updateIggy(IggySwf iggySwf, SWF swf) throws IOException {
        List<DefineFont2Tag> fontTags = new ArrayList<>();
        List<DefineEditTextTag> textTags = new ArrayList<>();
        List<DoABC2Tag> abcTags = new ArrayList<>();

        for (Tag t : swf.getTags()) {
            if (t instanceof DefineFont2Tag) {
                fontTags.add((DefineFont2Tag) t);
            }
            if (t instanceof DefineEditTextTag) {
                textTags.add((DefineEditTextTag) t);
            }
            if (t instanceof DoABC2Tag) {
                abcTags.add((DoABC2Tag) t);
            }
        }
        if (abcTags.size() > 1) {
            throw new IOException("Cannot save more than one ABC tag");
        }
        int fontCount = iggySwf.getFonts().size();
        if (fontCount != fontTags.size()) {
            throw new IOException("Font count is different from original iggy file");
        }
        for (int i = 0; i < fontCount; i++) {
            IggyFont iggyFont = iggySwf.getFonts().get(i);
            DefineFont2Tag fontTag = fontTags.get(i);
            SwfToIggyConvertor.updateIggyFont(iggyFont, fontTag);
        }

        int textCount = iggySwf.getTexts().size();
        if (textCount != textTags.size()) {
            throw new IOException("Text count is different from original iggy file");
        }
        for (int i = 0; i < textCount; i++) {
            IggyText iggyText = iggySwf.getTexts().get(i);
            DefineEditTextTag textTag = textTags.get(i);
            SwfToIggyConvertor.updateIggyText(iggyText, textTag);
        }
        if (!abcTags.isEmpty()) {
            DoABC2Tag abcTag = abcTags.get(0);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte abcTagData[] = abcTag.getData();
            byte declData[] = Arrays.copyOfRange(abcTagData, 4/*UI32 flags*/ + 1 /*empty string as name*/ + 3 /*versions, leaving one zero intact*/, abcTagData.length);
            iggySwf.getDeclStrings().setData(declData);
        }
    }

    public static void updateIggyText(IggyText iggyText, DefineEditTextTag textTag) {
        iggyText.setInitialText(textTag.initialText);
    }

    public static void updateIggyFont(IggyFont iggyFont, DefineFont2Tag fontTag) {
        /*byte zeroone[] = new byte[28];
        zeroone[12] = 1;
        long flags = 65795;
        float unk_float[] = new float[]{
            -0.6484375f,
            -1.116211f,
            1.116211f,
            0.6679688f,
            0f
        };
        int xscale = 80;
        int yscale = 28;
        float ssr1 = 0.9f;
        float ssr2 = 0.3f;
        long what_2 = 33188160;
        long what_3 = 33600216;
        byte zeroes48a[] = new byte[48];
        byte zeroes48b[] = new byte[48];
        float sss1 = 1.1728859f;
        float sss2 = 1.1728706f;
        float sss3 = 1.1728821f;
        float sss4 = 1.1729145f;*/
        List<IggyCharOffset> charOffsets = new ArrayList<>();

        List<IggyShape> glyphs = new ArrayList<>();

        for (SHAPE s : fontTag.glyphShapeTable) {
            glyphs.add(SwfShapeToIggyConvertor.convertShape(s));
        }

        List<Character> chars = new ArrayList<>();
        for (int code : fontTag.codeTable) {
            chars.add((char) code);
        }
        IggyCharIndices codePoints = new IggyCharIndices(chars);
        List<Float> scales = new ArrayList<>();
        for (int adv : fontTag.fontAdvanceTable) {
            scales.add(normalizeLengths(adv));
        }
        IggyCharAdvances charScales = new IggyCharAdvances(scales);
        List<Character> charA = new ArrayList<>();
        List<Character> charB = new ArrayList<>();
        List<Short> kernOffs = new ArrayList<>();

        //IggyCharOffset
        for (KERNINGRECORD rec : fontTag.fontKerningTable) {
            charA.add((char) rec.fontKerningCode1);
            charB.add((char) rec.fontKerningCode2);
            kernOffs.add((short) rec.fontKerningAdjustment);
        }
        IggyCharKerning charKernings = new IggyCharKerning(charA, charB, kernOffs);

        for (int i = 0; i < fontTag.getCharacterCount(); i++) {
            charOffsets.add(new IggyCharOffset(1, 0, 80, 19)); //XSCALE, YSCALE???
        }

        iggyFont.setCharCount(fontTag.getCharacterCount());
        iggyFont.setCharCount2(fontTag.getCharacterCount());
        iggyFont.setAscent(fontTag.getAscent());
        iggyFont.setDescent(fontTag.getDescent());
        iggyFont.setLeading(fontTag.getLeading());
        iggyFont.setName(fontTag.getFontName());
        iggyFont.setCharOffsets(charOffsets);
        iggyFont.setGlyphs(glyphs);
        iggyFont.setCodePoints(codePoints);
        iggyFont.setCharScales(charScales);
        iggyFont.setCharKernings(charKernings);
    }

    public static IggyFont createIggyFont(DefineFont2Tag fontTag) {
        byte zeroone[] = new byte[28];
        zeroone[12] = 1;
        long flags = 65795;
        float unk_float[] = new float[]{
            -0.6484375f,
            -1.116211f,
            1.116211f,
            0.6679688f,
            0f
        };
        int xscale = 80;
        int yscale = 28;
        float ssr1 = 0.9f;
        float ssr2 = 0.3f;
        long what_2 = 33188160;
        long what_3 = 33600216;
        byte zeroes48a[] = new byte[48];
        byte zeroes48b[] = new byte[48];
        float sss1 = 1.1728859f;
        float sss2 = 1.1728706f;
        float sss3 = 1.1728821f;
        float sss4 = 1.1729145f;
        List<IggyCharOffset> charOffsets = new ArrayList<>();
        List<IggyShape> glyphs = new ArrayList<>();
        List<Character> chars = new ArrayList<>();
        for (int code : fontTag.codeTable) {
            chars.add((char) code);
        }
        IggyCharIndices codePoints = new IggyCharIndices(chars);
        List<Float> scales = new ArrayList<>();
        for (int adv : fontTag.fontAdvanceTable) {
            scales.add((float) adv);
        }
        IggyCharAdvances charScales = new IggyCharAdvances(scales);
        List<Character> charA = new ArrayList<>();
        List<Character> charB = new ArrayList<>();
        List<Short> kernOffs = new ArrayList<>();

        for (KERNINGRECORD rec : fontTag.fontKerningTable) {
            charA.add((char) rec.fontKerningCode1);
            charB.add((char) rec.fontKerningCode2);
            kernOffs.add((short) rec.fontKerningAdjustment);
        }
        IggyCharKerning charKernings = new IggyCharKerning(charA, charB, kernOffs);

        IggyFont iggyFont = new IggyFont(IggyFont.ID, 0, zeroone, fontTag.getCharacterCount(),
                fontTag.getAscent(), fontTag.getDescent(), fontTag.getLeading(), flags,
                fontTag.fontKerningTable.size(), unk_float, 0, what_2, 0, 1,
                xscale, yscale, 0, ssr1, ssr2, fontTag.getCharacterCount(), 0, what_3, zeroes48a, zeroes48b,
                sss1, 1, sss2, 1, sss3, 1, sss4, 1, fontTag.getFontName(),
                charOffsets, glyphs, codePoints, charScales, charKernings);
        return iggyFont;
    }
}
