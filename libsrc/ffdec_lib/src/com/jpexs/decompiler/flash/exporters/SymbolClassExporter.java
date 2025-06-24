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
import com.jpexs.decompiler.flash.exporters.settings.SymbolClassExportSettings;
import com.jpexs.decompiler.flash.tags.ExportAssetsTag;
import com.jpexs.decompiler.flash.tags.SymbolClassTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.Path;
import com.jpexs.helpers.utf8.Utf8OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Symbol class exporter.
 *
 * @author JPEXS
 */
public class SymbolClassExporter {

    public static final String SYMBOL_CLASS_EXPORT_FILENAME = "symbols.csv";

    public List<File> exportNames(AbortRetryIgnoreHandler handler, final String outdir, ReadOnlyTagList tags, SymbolClassExportSettings settings, EventListener evl) throws IOException, InterruptedException {
        List<File> ret = new ArrayList<>();
        if (CancellableWorker.isInterrupted()) {
            return ret;
        }

        int count = 0;
        for (Tag t : tags) {
            if (t instanceof ExportAssetsTag || t instanceof SymbolClassTag) {
                count++;
            }
        }

        if (count == 0) {
            return ret;
        }

        File foutdir = new File(outdir);
        Path.createDirectorySafe(foutdir);

        final File file = new File(outdir + File.separator + SYMBOL_CLASS_EXPORT_FILENAME);
        new RetryTask(() -> {
            try (Writer writer = new BufferedWriter(new Utf8OutputStreamWriter(new FileOutputStream(file)))) {
                for (Tag t : tags) {
                    if (t instanceof ExportAssetsTag) {
                        ExportAssetsTag eat = (ExportAssetsTag) t;
                        for (int i = 0; i < eat.tags.size(); i++) {
                            writer.append(eat.tags.get(i) + ";" + eat.names.get(i) + Helper.newLine);
                        }
                    } else if (t instanceof SymbolClassTag) {
                        SymbolClassTag sct = (SymbolClassTag) t;
                        for (int i = 0; i < sct.tags.size(); i++) {
                            writer.append(sct.tags.get(i) + ";" + sct.names.get(i) + Helper.newLine);
                        }
                    }
                    if (CancellableWorker.isInterrupted()) {
                        break;
                    }
                }
            }
        }, handler).run();

        ret.add(file);
        return ret;
    }
}
