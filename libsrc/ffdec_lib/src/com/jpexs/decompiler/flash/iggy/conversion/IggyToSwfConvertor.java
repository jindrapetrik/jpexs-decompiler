package com.jpexs.decompiler.flash.iggy.conversion;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFCompression;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.iggy.IggyFile;
import com.jpexs.decompiler.flash.iggy.IggyFlashHeaderInterface;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.UnknownTag;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.helpers.ByteArrayRange;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * TODO: TEST!!!
 *
 * @author JPEXS
 */
public class IggyToSwfConvertor {

    public SWF[] getAllSwfs(IggyFile file) {
        SWF[] ret = new SWF[file.getSwfCount()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = getSwf(file, i);
        }
        return ret;
    }

    public void exportAllSwfsToDir(IggyFile file, File outputDir) throws IOException {
        for (int swfIndex = 0; swfIndex < file.getSwfCount(); swfIndex++) {
            exportSwfToDir(file, swfIndex, outputDir);
        }
    }

    public void exportSwfToDir(IggyFile file, int swfIndex, File outputDir) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(new File(outputDir, file.getSwfName(swfIndex)))) {
            exportSwf(file, swfIndex, fos);
        }
    }

    public void exportSwfToFile(IggyFile file, int swfIndex, File outputFile) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            exportSwf(file, swfIndex, fos);
        }
    }

    public void exportSwf(IggyFile file, int swfIndex, OutputStream output) throws IOException {
        SWF swf = getSwf(file, swfIndex);
        swf.saveTo(output);
    }

    public SWF getSwf(IggyFile file, int swfIndex) {
        SWF swf = new SWF();
        swf.compression = SWFCompression.NONE;
        swf.frameCount = 1; //FIXME!!
        swf.gfx = false;
        swf.displayRect = new RECT(
                (int) (file.getSwfXMin(swfIndex) * SWF.unitDivisor),
                (int) (file.getSwfXMax(swfIndex) * SWF.unitDivisor),
                (int) (file.getSwfYMin(swfIndex) * SWF.unitDivisor),
                (int) (file.getSwfYMax(swfIndex) * SWF.unitDivisor));
        swf.version = 9; //FIXME
        for (int tagIndex = 0; tagIndex < file.getSwfTagCount(swfIndex); tagIndex++) {
            byte[] data = file.getSwfTagData(swfIndex, tagIndex);
            int dataLength = data.length;
            int tagLength = dataLength;
            int tagID = file.getSwfTagId(swfIndex, tagIndex);
            long tagExtra = file.getSwfTagExtraInfo(swfIndex, tagIndex);
            try {
                swf.addTag(new UnknownTag(new SWFInputStream(swf, data), tagID, new ByteArrayRange(data)));
            } catch (IOException ex) {
                //ignored
            }
        }
        return swf;
    }
}
