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
package com.jpexs.decompiler.flash.exporters;

import com.jpexs.decompiler.flash.AbortRetryIgnoreHandler;
import com.jpexs.decompiler.flash.EventListener;
import com.jpexs.decompiler.flash.ReadOnlyTagList;
import com.jpexs.decompiler.flash.RetryTask;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.Font4ExportMode;
import com.jpexs.decompiler.flash.exporters.settings.Font4ExportSettings;
import com.jpexs.decompiler.flash.tags.DefineFont4Tag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.Path;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DefineFont4 exporter.
 *
 * @author JPEXS
 */
public class Font4Exporter {

    public List<File> exportFonts(AbortRetryIgnoreHandler handler, String outdir, ReadOnlyTagList tags, final Font4ExportSettings settings, EventListener evl) throws IOException, InterruptedException {
        List<File> ret = new ArrayList<>();
        if (CancellableWorker.isInterrupted()) {
            return ret;
        }

        if (tags.isEmpty()) {
            return ret;
        }

        File foutdir = new File(outdir);
        Path.createDirectorySafe(foutdir);

        int count = 0;
        for (Tag t : tags) {
            if (t instanceof DefineFont4Tag) {
                count++;
            }
        }

        if (count == 0) {
            return ret;
        }

        int currentIndex = 1;
        for (Tag t : tags) {
            if (t instanceof DefineFont4Tag) {
                if (evl != null) {
                    evl.handleExportingEvent("font", currentIndex, count, t.getName());
                }

                final DefineFont4Tag st = (DefineFont4Tag) t;
                if (!st.fontFlagsHasFontData) {
                    continue;
                }
                String ext = ".cff";
                final File file = new File(outdir + File.separator + Helper.makeFileName(st.getCharacterExportFileName() + ext));
                new RetryTask(() -> {
                    exportFont(st, settings.mode, file);
                }, handler).run();

                Set<String> classNames = st.getClassNames();
                if (Configuration.as3ExportNamesUseClassNamesOnly.get() && !classNames.isEmpty()) {
                    for (String className : classNames) {
                        File classFile = new File(outdir + File.separator + Helper.makeFileName(className + ext));
                        new RetryTask(() -> {
                            Files.copy(file.toPath(), classFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        }, handler).run();
                        ret.add(classFile);
                    }
                    file.delete();
                } else {
                    ret.add(file);
                }

                if (CancellableWorker.isInterrupted()) {
                    break;
                }

                if (evl != null) {
                    evl.handleExportedEvent("font", currentIndex, count, t.getName());
                }

                currentIndex++;
            }
        }

        return ret;
    }

    public byte[] exportFont(final DefineFont4Tag t, Font4ExportMode mode) {
        try {
            String ext = ".cff";

            File f = File.createTempFile("temp", ext);
            exportFont(t, mode, f);
            return Helper.readFile(f.getPath());
        } catch (IOException ex) {
            Logger.getLogger(Font4Exporter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return SWFInputStream.BYTE_ARRAY_EMPTY;
    }

    public void exportFont(DefineFont4Tag ft, Font4ExportMode mode, File file) throws IOException {
        try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(file))) {
            fos.write(ft.fontData.getArray(), ft.fontData.getPos(), ft.fontData.getLength());
        }
    }
}
