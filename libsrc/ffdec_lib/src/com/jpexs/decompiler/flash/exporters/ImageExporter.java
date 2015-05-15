/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.EventListener;
import com.jpexs.decompiler.flash.RetryTask;
import com.jpexs.decompiler.flash.exporters.modes.ImageExportMode;
import com.jpexs.decompiler.flash.exporters.settings.ImageExportSettings;
import com.jpexs.decompiler.flash.helpers.BMPFile;
import com.jpexs.decompiler.flash.helpers.ImageHelper;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.Path;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author JPEXS
 */
public class ImageExporter {

    public List<File> exportImages(AbortRetryIgnoreHandler handler, String outdir, List<Tag> tags, ImageExportSettings settings, EventListener evl) throws IOException {
        List<File> ret = new ArrayList<>();
        if (tags.isEmpty()) {
            return ret;
        }

        File foutdir = new File(outdir);
        Path.createDirectorySafe(foutdir);

        int count = 0;
        for (Tag t : tags) {
            if (t instanceof ImageTag) {
                count++;
            }
        }

        if (count == 0) {
            return ret;
        }

        int currentIndex = 1;
        for (Tag t : tags) {
            if (t instanceof ImageTag) {
                if (evl != null) {
                    evl.handleExportingEvent("image", currentIndex, count, t.getName());
                }

                final ImageTag imageTag = (ImageTag) t;

                String fileFormat = imageTag.getImageFormat().toUpperCase(Locale.ENGLISH);
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
                    final File file = new File(outdir + File.separator + Helper.makeFileName(imageTag.getCharacterExportFileName() + "." + fileFormat));
                    final String ffileFormat = fileFormat;

                    new RetryTask(() -> {
                        if (ffileFormat.equals("bmp")) {
                            BMPFile.saveBitmap(imageTag.getImage().getBufferedImage(), file);
                        } else {
                            ImageHelper.write(imageTag.getImage().getBufferedImage(), ffileFormat.toUpperCase(Locale.ENGLISH), file);
                        }
                    }, handler).run();
                    ret.add(file);
                }

                if (evl != null) {
                    evl.handleExportedEvent("image", currentIndex, count, t.getName());
                }

                currentIndex++;
            }
        }

        return ret;
    }
}
