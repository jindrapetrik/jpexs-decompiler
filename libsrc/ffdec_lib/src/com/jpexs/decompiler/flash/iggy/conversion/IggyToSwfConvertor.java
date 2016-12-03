package com.jpexs.decompiler.flash.iggy.conversion;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFCompression;
import com.jpexs.decompiler.flash.iggy.IggyShape;
import com.jpexs.decompiler.flash.iggy.IggyCharKerning;
import com.jpexs.decompiler.flash.iggy.IggyShapeNode;
import com.jpexs.decompiler.flash.iggy.IggyCharOffset;
import com.jpexs.decompiler.flash.iggy.IggyCharAdvances;
import com.jpexs.decompiler.flash.iggy.IggyFile;
import com.jpexs.decompiler.flash.iggy.IggyFont;
import com.jpexs.decompiler.flash.iggy.IggyText;
import com.jpexs.decompiler.flash.tags.DefineEditTextTag;
import com.jpexs.decompiler.flash.tags.DefineFont2Tag;
import com.jpexs.decompiler.flash.tags.EndTag;
import com.jpexs.decompiler.flash.tags.FileAttributesTag;
import com.jpexs.decompiler.flash.types.FILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.KERNINGRECORD;
import com.jpexs.decompiler.flash.types.LINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.shaperecords.CurvedEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * WIP
 *
 * @author JPEXS
 */
public class IggyToSwfConvertor {

    public static SWF[] getAllSwfs(IggyFile file) {
        SWF[] ret = new SWF[file.getSwfCount()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = getSwf(file, i);
        }
        return ret;
    }

    public static void exportAllSwfsToDir(IggyFile file, File outputDir) throws IOException {
        for (int swfIndex = 0; swfIndex < file.getSwfCount(); swfIndex++) {
            exportSwfToDir(file, swfIndex, outputDir);
        }
    }

    public static void exportSwfToDir(IggyFile file, int swfIndex, File outputDir) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(new File(outputDir, file.getSwfName(swfIndex)))) {
            exportSwf(file, swfIndex, fos);
        }
    }

    public static void exportSwfToFile(IggyFile file, int swfIndex, File outputFile) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            exportSwf(file, swfIndex, fos);
        }
    }

    public static void exportSwf(IggyFile file, int swfIndex, OutputStream output) throws IOException {
        SWF swf = getSwf(file, swfIndex);
        swf.saveTo(output);
    }

    private static int makeLengthsTwip(double val) {
        return (int) (val * SWF.unitDivisor);
    }

    private static int makeLengthsEm(double val) {
        return (int) (val * 1024.0);
    }

    public static SWF getSwf(IggyFile file, int swfIndex) {
        SWF swf = new SWF();
        swf.compression = SWFCompression.NONE;
        swf.frameCount = 1; //FIXME!!        
        swf.frameRate = file.getSwfFrameRate(swfIndex);
        swf.gfx = false;
        swf.displayRect = new RECT(
                (int) (file.getSwfXMin(swfIndex) * SWF.unitDivisor),
                (int) (file.getSwfXMax(swfIndex) * SWF.unitDivisor),
                (int) (file.getSwfYMin(swfIndex) * SWF.unitDivisor),
                (int) (file.getSwfYMax(swfIndex) * SWF.unitDivisor));
        swf.version = 10; //FIXME

        FileAttributesTag fat = new FileAttributesTag(swf);
        fat.actionScript3 = false;
        fat.hasMetadata = false;
        fat.useNetwork = false;
        swf.addTag(fat);

        //Set<Integer> fontIndices = file.getFontIds(swfIndex);
        int fontCount = file.getFontCount(swfIndex);

        int currentCharId = 0;
        Map<Integer, Integer> fontIndex2CharId = new HashMap<>();

        for (int fontIndex = 0; fontIndex < fontCount; fontIndex++) {
            IggyFont iggyFont = file.getFont(swfIndex, fontIndex);
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
                    fontTag.fontBoundsTable.add(shp.getBounds());
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

        /*       
        //TODO: Texts, they are incomplete
        
        Map<Integer, Integer> textIndex2CharId = new HashMap<>();

        Set<Integer> textIds = file.getTextIds(swfIndex);
        for (int textId : textIds) {
            IggyText iggyText = file.getText(swfIndex, textId);
            DefineEditTextTag textTag = new DefineEditTextTag(swf);
            currentCharId++;
            textIndex2CharId.put(iggyText.getTextIndex(), currentCharId);
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
                    makeLengthsTwip(iggyText.getPar3()),
                    makeLengthsTwip(iggyText.getPar1()),
                    makeLengthsTwip(iggyText.getPar4()),
                    makeLengthsTwip(iggyText.getPar2())
            );

            //textTag.hasFont = true;
            //textTag.fontId = fontIndex2CharId.get(iggyText.getFontIndex());
            textTag.setModified(true);
            swf.addTag(textTag);
        }
         */
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
            System.out.println("All finished sucessfully.");
            System.exit(0);
        } catch (IOException ex) {
            System.out.println("FAIL");
            System.err.println("Error while converting: " + ex.getMessage());
            System.exit(1);
        }
    }
}
