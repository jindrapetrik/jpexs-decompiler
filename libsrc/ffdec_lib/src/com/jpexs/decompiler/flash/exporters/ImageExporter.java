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
package com.jpexs.decompiler.flash.exporters;

import com.jpexs.decompiler.flash.AbortRetryIgnoreHandler;
import com.jpexs.decompiler.flash.EventListener;
import com.jpexs.decompiler.flash.ReadOnlyTagList;
import com.jpexs.decompiler.flash.RetryTask;
import com.jpexs.decompiler.flash.exporters.modes.ImageExportMode;
import com.jpexs.decompiler.flash.exporters.settings.ImageExportSettings;
import com.jpexs.decompiler.flash.helpers.BMPFile;
import com.jpexs.decompiler.flash.helpers.ImageHelper;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.enums.ImageFormat;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.Path;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class ImageExporter {

    public List<File> exportImages(AbortRetryIgnoreHandler handler, String outdir, ReadOnlyTagList tags, ImageExportSettings settings, EventListener evl) throws IOException, InterruptedException {
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

                ImageFormat fileFormat = imageTag.getImageFormat();
                ImageFormat originalFormat = fileFormat;
                if (settings.mode == ImageExportMode.PNG) {
                    fileFormat = ImageFormat.PNG;
                }

                if (settings.mode == ImageExportMode.JPEG) {
                    fileFormat = ImageFormat.JPEG;
                }

                if (settings.mode == ImageExportMode.BMP) {
                    fileFormat = ImageFormat.BMP;
                }

                {
                    final File file = new File(outdir + File.separator + Helper.makeFileName(imageTag.getCharacterExportFileName() + "." + ImageHelper.getImageFormatString(fileFormat)));
                    final ImageFormat ffileFormat = fileFormat;

                    new RetryTask(() -> {
                        if (ffileFormat == originalFormat) {
                            try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(file))) {
                                fos.write(Helper.readStream(imageTag.getImageData()));
                            }
                        } else if (ffileFormat == ImageFormat.BMP) {
                            BMPFile.saveBitmap(imageTag.getImageCached().getBufferedImage(), file);
                        } else {
                            ImageHelper.write(imageTag.getImageCached().getBufferedImage(), ffileFormat, file);
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
