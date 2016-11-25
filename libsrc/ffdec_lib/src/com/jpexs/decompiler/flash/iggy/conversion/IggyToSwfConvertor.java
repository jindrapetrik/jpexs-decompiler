package com.jpexs.decompiler.flash.iggy.conversion;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFCompression;
import com.jpexs.decompiler.flash.iggy.IggyChar;
import com.jpexs.decompiler.flash.iggy.IggyFile;
import com.jpexs.decompiler.flash.iggy.IggyFont;
import com.jpexs.decompiler.flash.iggy.IggyText;
import com.jpexs.decompiler.flash.tags.DefineEditTextTag;
import com.jpexs.decompiler.flash.tags.DefineFont2Tag;
import com.jpexs.decompiler.flash.tags.EndTag;
import com.jpexs.decompiler.flash.tags.FileAttributesTag;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.SHAPE;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
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

        Set<Integer> fontIndices = file.getFontIds(swfIndex);

        int currentCharId = 0;
        Map<Integer, Integer> fontIndex2CharId = new HashMap<>();

        for (int fontIndex : fontIndices) {
            IggyFont iggyFont = file.getFont(swfIndex, fontIndex);
            DefineFont2Tag fontTag = new DefineFont2Tag(swf);
            currentCharId++;
            fontIndex2CharId.put(fontIndex, currentCharId);
            fontTag.fontID = currentCharId;
            fontTag.codeTable = new ArrayList<>();
            fontTag.fontName = iggyFont.getName();
            fontTag.glyphShapeTable = new ArrayList<>();
            fontTag.fontAdvanceTable = new ArrayList<>();
            fontTag.fontBoundsTable = new ArrayList<>();

            for (int i = 0; i < iggyFont.getCharacterCount(); i++) {
                int code = iggyFont.getCharIndices().getChars().get(i);
                IggyChar chr = iggyFont.getChars().get(i);
                if (chr != null) {
                    fontTag.codeTable.add(code);
                    SHAPE shp = IggyCharToShapeConvertor.convertCharToShape(chr);
                    fontTag.glyphShapeTable.add(shp);
                    fontTag.fontAdvanceTable.add((int) chr.getAdvance());
                    fontTag.fontBoundsTable.add(shp.getBounds());

                }
                //FIXME: handle spaces (with no vectors), etc.
            }
            fontTag.setModified(true);
            swf.addTag(fontTag);
        }

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
            //textTag.hasFont = true;
            //textTag.fontId = fontIndex2CharId.get(iggyText.getFontIndex());
            textTag.setModified(true);
            swf.addTag(textTag);
        }

        swf.addTag(new EndTag(swf));
        swf.setModified(true);

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
