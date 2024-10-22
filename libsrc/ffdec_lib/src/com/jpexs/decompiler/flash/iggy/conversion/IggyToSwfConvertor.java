/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.SWFCompression;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.iggy.IggyCharAdvances;
import com.jpexs.decompiler.flash.iggy.IggyCharKerning;
import com.jpexs.decompiler.flash.iggy.IggyDeclStrings;
import com.jpexs.decompiler.flash.iggy.IggyFile;
import com.jpexs.decompiler.flash.iggy.IggyFont;
import com.jpexs.decompiler.flash.iggy.IggyShape;
import com.jpexs.decompiler.flash.iggy.IggySwf;
import com.jpexs.decompiler.flash.iggy.IggyText;
import com.jpexs.decompiler.flash.tags.DefineEditTextTag;
import com.jpexs.decompiler.flash.tags.DefineFont2Tag;
import com.jpexs.decompiler.flash.tags.DoABC2Tag;
import com.jpexs.decompiler.flash.tags.EndTag;
import com.jpexs.decompiler.flash.tags.FileAttributesTag;
import com.jpexs.decompiler.flash.types.KERNINGRECORD;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import com.jpexs.helpers.ByteArrayRange;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Iggy to SWF convertor. WIP
 *
 * @author JPEXS
 */
public class IggyToSwfConvertor {

    public static void exportAllSwfsToDir(IggyFile file, File outputDir) throws IOException {
        exportSwfToDir(file, outputDir);
    }

    public static void exportSwfToDir(IggyFile file, File outputDir) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(new File(outputDir, file.getSwfName()))) {
            exportSwf(file, fos);
        }
    }

    public static void exportSwfToFile(IggyFile file, File outputFile) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            exportSwf(file, fos);
        }
    }

    public static void exportSwf(IggyFile file, OutputStream output) throws IOException {
        SWF swf = getSwf(file);
        swf.saveTo(output);
    }

    private static int makeLengthsEm(double val) {
        return (int) (val * 1024.0);
    }

    private static int makeLengthsTwip(double val) {
        return (int) (val * SWF.unitDivisor);
    }

    public static SWF getSwf(IggyFile file) {
        SWF swf = new SWF();
        swf.compression = SWFCompression.NONE;
        swf.frameCount = 1; //FIXME!!        
        swf.frameRate = file.getSwfFrameRate();
        swf.gfx = false;
        swf.displayRect = new RECT(
                makeLengthsTwip(file.getSwfXMin()),
                makeLengthsTwip(file.getSwfXMax()),
                makeLengthsTwip(file.getSwfYMin()),
                makeLengthsTwip(file.getSwfYMax()));
        swf.version = 10; //FIXME

        FileAttributesTag fat = new FileAttributesTag(swf);
        fat.actionScript3 = true;
        fat.hasMetadata = false;
        fat.useNetwork = false;
        swf.addTag(fat);
        IggySwf iggySwf = file.getSwf();

        int currentCharId = 0;
        Map<Integer, Integer> fontIndex2CharId = new HashMap<>();

        for (int fontIndex = 0; fontIndex < iggySwf.getFonts().size(); fontIndex++) {
            IggyFont iggyFont = iggySwf.getFonts().get(fontIndex);
            DefineFont2Tag fontTag = new DefineFont2Tag(swf);
            currentCharId++;
            fontIndex2CharId.put(fontIndex, currentCharId);
            fontTag.fontID = currentCharId;
            /*System.out.println("===================");
            System.out.println("xscale: " + iggyFont.getXscale());  //80
            System.out.println("yscale: " + iggyFont.getYscale());  //19          

            System.out.println("unk_float1: " + iggyFont.getUnk_float()[0]);
            System.out.println("unk_float2: " + iggyFont.getUnk_float()[1]);
            System.out.println("unk_float3: " + iggyFont.getUnk_float()[2]);
            System.out.println("unk_float4: " + iggyFont.getUnk_float()[3]);
            System.out.println("unk_float5: " + iggyFont.getUnk_float()[4]);
            System.out.println("what_2: " + iggyFont.getWhat_2());
            System.out.println("what_3: " + iggyFont.getWhat_3());*/

            fontTag.fontKerningTable = new ArrayList<>();
            IggyCharKerning ker = iggyFont.getCharKernings();
            if (ker != null) {
                for (int i = 0; i < ker.getKernCount(); i++) {
                    int kerningCode1 = ker.getCharsA().get(i);
                    int kerningCode2 = ker.getCharsA().get(i);
                    int kerningOffset = ker.getKerningOffsets().get(i);
                    fontTag.fontKerningTable.add(new KERNINGRECORD(kerningCode1, kerningCode2, kerningOffset));
                }
            }

            fontTag.fontFlagsWideCodes = true;
            fontTag.fontFlagsWideOffsets = true;
            fontTag.fontAscent = iggyFont.getAscent();
            fontTag.fontDescent = iggyFont.getDescent();
            fontTag.fontLeading = iggyFont.getLeading();
            fontTag.codeTable = new ArrayList<>();
            fontTag.fontName = iggyFont.getName();
            fontTag.glyphShapeTable = new ArrayList<>();
            fontTag.fontBoundsTable = new ArrayList<>();
            fontTag.fontAdvanceTable = new ArrayList<>();
            fontTag.fontFlagsHasLayout = true;
            IggyCharAdvances advanceValues = iggyFont.getCharAdvances();
            for (int i = 0; i < iggyFont.getCharacterCount(); i++) {
                int code = iggyFont.getCharIndices().getChars().get(i);
                fontTag.codeTable.add(code);
                IggyShape glyph = iggyFont.getChars().get(i);
                SHAPE shp;
                if (glyph != null) {
                    shp = IggyShapeToSwfConvertor.convertCharToShape(glyph);
                    fontTag.fontBoundsTable.add(shp.getBounds(1));
                } else {
                    shp = new SHAPE();
                    shp.shapeRecords = new ArrayList<>();
                    shp.shapeRecords.add(new EndShapeRecord());
                    fontTag.fontBoundsTable.add(new RECT()); //??
                }
                fontTag.glyphShapeTable.add(shp);

                fontTag.fontAdvanceTable.add(makeLengthsEm(advanceValues.getScales().get(i)));

            }
            fontTag.setModified(true);
            swf.addTag(fontTag);
        }

        Map<Integer, Integer> textIndex2CharId = new HashMap<>();

        for (int textIndex = 0; textIndex < iggySwf.getTexts().size(); textIndex++) {
            IggyText iggyText = iggySwf.getTexts().get(textIndex);
            DefineEditTextTag textTag = new DefineEditTextTag(swf);
            currentCharId++;
            textIndex2CharId.put(textIndex, currentCharId);
            textTag.characterID = currentCharId;
            textTag.hasText = true;
            textTag.initialText = iggyText.getInitialText();
            textTag.html = true;
            textTag.noSelect = true;
            textTag.wasStatic = true;
            textTag.hasFont = false;
            textTag.hasFontClass = false;
            textTag.hasMaxLength = false;
            //textTag.multiline = true;
            //textTag.wordWrap = true;
            //textTag.hasTextColor = true;
            //textTag.textColor = new RGBA(Color.black);
            //textTag.fontHeight = 40; //??            
            textTag.readOnly = true;
            textTag.bounds = new RECT(
                    makeLengthsTwip(iggyText.getPar1()),
                    makeLengthsTwip(iggyText.getPar3()),
                    makeLengthsTwip(iggyText.getPar2()),
                    makeLengthsTwip(iggyText.getPar4())
            );

            //textTag.hasFont = true;
            //textTag.fontId = fontIndex2CharId.get(iggyText.getFontIndex());
            textTag.setModified(true);
            swf.addTag(textTag);
        }

        IggyDeclStrings declStrings = iggySwf.getDeclStrings();
        if (declStrings != null) {
            byte[] abcData = declStrings.getData();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                baos.write(new byte[]{1, 0, 0, 0, 0, 0x10, 0, 0x2E});
                baos.write(abcData);
            } catch (IOException ex) {
                //should not happen
            }
            byte[] fullAbcTagData = baos.toByteArray();
            try {
                DoABC2Tag nabc = new DoABC2Tag(new SWFInputStream(swf, fullAbcTagData), new ByteArrayRange(fullAbcTagData));
                nabc.setModified(true);
                swf.addTag(nabc);
            } catch (IOException ex) {
                //ignore
            }

        }

        swf.addTag(
                new EndTag(swf));
        swf.setModified(
                true);

        return swf;
    }

    public static void main(String[] args) {

        if (args.length < 2 || (args[0].isEmpty() || args[1].isEmpty())) {
            System.err.println("Invalid arguments");
            System.err.println("Usage: iggy-extract.bat file.iggy d:/outdir/");
            System.exit(1);
        }

        File file = new File(args[0]);
        if (!file.exists()) {
            System.err.println("FAIL: Input file: " + file.getAbsolutePath() + " does not exist.");
            System.exit(1);
        }
        File outDir = new File(args[1]);
        if (!outDir.exists()) {
            if (!outDir.mkdirs()) {
                System.err.println("FAIL: Cannot create output directory");
                System.exit(1);
            }
        }

        try {
            System.out.print("(1/2) Loading file " + args[0] + "...");
            IggyFile iggyFile = new IggyFile(new File(args[0]));
            System.out.println("OK");
            System.out.print("(2/2) Exporting SWF files to " + args[1] + "...");
            exportAllSwfsToDir(iggyFile, new File(args[1]));
            System.out.println("OK");
            System.out.println("All finished successfully.");
            System.exit(0);
        } catch (IOException ex) {
            System.out.println("FAIL");
            System.err.println("Error while converting: " + ex.getMessage());
            System.exit(1);
        }
    }
}
