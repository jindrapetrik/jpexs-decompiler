/*
 *  Copyright (C) 2014 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.exporters;

import com.jpexs.decompiler.flash.AbortRetryIgnoreHandler;
import com.jpexs.decompiler.flash.RetryTask;
import com.jpexs.decompiler.flash.RunnableIOEx;
import com.jpexs.decompiler.flash.exporters.modes.ImageExportMode;
import com.jpexs.decompiler.flash.exporters.settings.ImageExportSettings;
import com.jpexs.decompiler.flash.helpers.BMPFile;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.helpers.Helper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.imageio.ImageIO;

/**
 *
 * @author JPEXS
 */
public class ImageExporter {

    public List<File> exportImages(AbortRetryIgnoreHandler handler, String outdir, List<Tag> tags, final ImageExportSettings settings) throws IOException {
        List<File> ret = new ArrayList<>();
        if (tags.isEmpty()) {
            return ret;
        }
        File foutdir = new File(outdir);
        if (!foutdir.exists()) {
            if (!foutdir.mkdirs()) {
                if (!foutdir.exists()) {
                    throw new IOException("Cannot create directory " + outdir);
                }
            }
        }
        for (final Tag t : tags) {
            if (t instanceof ImageTag) {

                String fileFormat = ((ImageTag) t).getImageFormat().toUpperCase(Locale.ENGLISH);
                if (settings.mode == ImageExportMode.PNG) {
                    fileFormat = "png";
                }
                if (settings.mode == ImageExportMode.JPEG) {
                    fileFormat = "jpg";
                }

                if (settings.mode == ImageExportMode.BMP) {
                    fileFormat = "bmp";
                }
                {

                    final ImageTag imageTag = (ImageTag) t;
                    final File file = new File(outdir + File.separator + Helper.makeFileName(imageTag.getCharacterExportFileName() + "." + fileFormat));
                    final String ffileFormat = fileFormat;

                    new RetryTask(new RunnableIOEx() {
                        @Override
                        public void run() throws IOException {
                            if (ffileFormat.equals("bmp")) {
                                BMPFile.saveBitmap(imageTag.getImage().getBufferedImage(), file);
                            } else {
                                ImageIO.write(imageTag.getImage().getBufferedImage(), ffileFormat.toUpperCase(Locale.ENGLISH), file);
                            }
                        }
                    }, handler).run();
                    ret.add(file);
                }

            }
        }
        return ret;
    }
}
