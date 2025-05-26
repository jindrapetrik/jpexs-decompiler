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
package com.jpexs.decompiler.flash.exporters.swf;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ExeExportMode;
import com.jpexs.helpers.Helper;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Exports SWF to EXE.
 *
 * @author JPEXS
 */
public class SwfToExeExporter {

    /**
     * Saves SWF to EXE file.
     * @param swf SWF to save
     * @param exeExportMode EXE export mode
     * @param outFile Target file to save to
     * @throws IOException If an I/O error occurs
     */
    public static void saveFileToExe(SWF swf, ExeExportMode exeExportMode, File outFile) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(outFile); BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            switch (exeExportMode) {
                case WRAPPER:
                    InputStream exeStream = SwfToExeExporter.class.getClassLoader().getResourceAsStream("com/jpexs/helpers/resource/Swf2Exe.bin");
                    Helper.copyStream(exeStream, bos);
                    int width = swf.displayRect.Xmax - swf.displayRect.Xmin;
                    int height = swf.displayRect.Ymax - swf.displayRect.Ymin;
                    bos.write(width & 0xff);
                    bos.write((width >> 8) & 0xff);
                    bos.write((width >> 16) & 0xff);
                    bos.write((width >> 24) & 0xff);
                    bos.write(height & 0xff);
                    bos.write((height >> 8) & 0xff);
                    bos.write((height >> 16) & 0xff);
                    bos.write((height >> 24) & 0xff);
                    bos.write(Configuration.saveAsExeScaleMode.get());
                    break;
                case PROJECTOR_WIN:
                case PROJECTOR_MAC:
                case PROJECTOR_LINUX:
                    File projectorFile = Configuration.getProjectorFile(exeExportMode);
                    if (projectorFile == null) {
                        String message = "Projector not found, please place it to " + Configuration.getProjectorPath();
                        Logger.getLogger(SwfToExeExporter.class.getName()).log(Level.SEVERE, message);
                        throw new IOException(message);
                    }
                    Helper.copyStream(new FileInputStream(projectorFile), bos);
                    bos.flush();
                    break;
            }

            long pos = fos.getChannel().position();
            swf.saveTo(bos);

            switch (exeExportMode) {
                case PROJECTOR_WIN:
                case PROJECTOR_MAC:
                case PROJECTOR_LINUX:
                    bos.flush();
                    int swfSize = (int) (fos.getChannel().position() - pos);

                    // write magic number
                    bos.write(0x56);
                    bos.write(0x34);
                    bos.write(0x12);
                    bos.write(0xfa);

                    bos.write(swfSize & 0xff);
                    bos.write((swfSize >> 8) & 0xff);
                    bos.write((swfSize >> 16) & 0xff);
                    bos.write((swfSize >> 24) & 0xff);
            }
        }
    }
}
