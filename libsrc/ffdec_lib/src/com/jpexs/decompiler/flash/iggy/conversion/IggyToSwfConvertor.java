package com.jpexs.decompiler.flash.iggy.conversion;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFCompression;
import com.jpexs.decompiler.flash.iggy.IggyChar;
import com.jpexs.decompiler.flash.iggy.IggyFile;
import com.jpexs.decompiler.flash.iggy.IggyFontData;
import com.jpexs.decompiler.flash.iggy.IggyFontInfo;
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

        int fontCount = file.getFontCount(swfIndex);
        for (int fontIndex = 0; fontIndex < fontCount; fontIndex++) {
            IggyFontData iggyFontData = file.getFontData(swfIndex, fontIndex);
            IggyFontInfo fontInfo = file.getFontInfo(swfIndex, fontIndex); //TODO!           
            DefineFont2Tag fontTag = new DefineFont2Tag(swf);
            fontTag.fontID = fontIndex + 1;
            fontTag.codeTable = new ArrayList<>();
            fontTag.fontName = iggyFontData.getName();
            fontTag.glyphShapeTable = new ArrayList<>();
            fontTag.fontAdvanceTable = new ArrayList<>();

            for (int i = 0; i < iggyFontData.getCharacterCount(); i++) {
                int code = iggyFontData.getCharIndices().getChars().get(i);
                IggyChar chr = iggyFontData.getChars().get(i);
                if (chr != null) {
                    fontTag.codeTable.add(code);
                    fontTag.glyphShapeTable.add(IggyCharToShapeConvertor.convertCharToShape(chr));
                    fontTag.fontAdvanceTable.add((int) chr.getUnk());
                }
                //TODO: handle spaces, etc.
            }
            fontTag.setModified(true);
            swf.addTag(fontTag);
        }
        swf.addTag(new EndTag(swf));
        swf.setModified(true);

        return swf;
    }

    public static void main(String[] args) throws IOException {

    }
}
