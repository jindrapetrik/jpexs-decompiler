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
import com.jpexs.decompiler.flash.RetryTask;
import com.jpexs.decompiler.flash.RunnableIOEx;
import com.jpexs.decompiler.flash.exporters.settings.BinaryDataExportSettings;
import com.jpexs.decompiler.flash.tags.DefineBinaryDataTag;
import com.jpexs.decompiler.flash.tags.Tag;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class BinaryDataExporter {

    public List<File> exportBinaryData(AbortRetryIgnoreHandler handler, String outdir, List<Tag> tags, BinaryDataExportSettings settings) throws IOException {
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
            if (t instanceof DefineBinaryDataTag) {
                int characterID = ((DefineBinaryDataTag) t).getCharacterId();
                final File file = new File(outdir + File.separator + characterID + ".bin");
                new RetryTask(new RunnableIOEx() {
                    @Override
                    public void run() throws IOException {
                        try (FileOutputStream fos = new FileOutputStream(file)) {
                            fos.write(((DefineBinaryDataTag) t).binaryData.getRangeData());
                        }
                    }
                }, handler).run();
                ret.add(file);
            }
        }
        return ret;
    }
}
