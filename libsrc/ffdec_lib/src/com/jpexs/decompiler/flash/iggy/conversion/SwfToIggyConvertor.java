package com.jpexs.decompiler.flash.iggy.conversion;

import com.jpexs.decompiler.flash.iggy.IggyCharAdvances;
import com.jpexs.decompiler.flash.iggy.IggyCharIndices;
import com.jpexs.decompiler.flash.iggy.IggyCharKerning;
import com.jpexs.decompiler.flash.iggy.IggyCharOffset;
import com.jpexs.decompiler.flash.iggy.IggyFont;
import com.jpexs.decompiler.flash.iggy.IggyShape;
import com.jpexs.decompiler.flash.tags.DefineFont2Tag;
import com.jpexs.decompiler.flash.types.KERNINGRECORD;
import com.jpexs.decompiler.flash.types.SHAPE;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class SwfToIggyConvertor {

    private static float normalizeLengths(int val) {
        return (val / 1024f);
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
        /*
        IggyFont iggyFont = new IggyFont(IggyFont.ID, 0, zeroone, fontTag.getCharacterCount(),
                fontTag.getAscent(), fontTag.getDescent(), fontTag.getLeading(), flags,
                fontTag.fontKerningTable.size(), unk_float, 0, what_2, 0, 1,
                xscale, yscale, 0, ssr1, ssr2, fontTag.getCharacterCount(), 0, what_3, zeroes48a, zeroes48b,
                sss1, 1, sss2, 1, sss3, 1, sss4, 1, fontTag.getFontName(),
                charOffsets, glyphs, codePoints, charScales, charKernings);
         */
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
