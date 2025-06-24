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
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.settings.BinaryDataExportSettings;
import com.jpexs.decompiler.flash.tags.DefineBinaryDataTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.BinaryDataInterface;
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

/**
 * Binary data exporter.
 *
 * @author JPEXS
 */
public class BinaryDataExporter {

    /**
     * Constructor.
     */
    public BinaryDataExporter() {

    }

    /**
     * Export binary data.
     * @param handler AbortRetryIgnoreHandler
     * @param outdir Output directory
     * @param tags Tags
     * @param settings Binary data export settings
     * @param evl Event listener
     * @return List of exported files
     * @throws IOException On I/O error
     * @throws InterruptedException On interrupt
     */
    public List<File> exportBinaryData(AbortRetryIgnoreHandler handler, String outdir, ReadOnlyTagList tags, BinaryDataExportSettings settings, EventListener evl) throws IOException, InterruptedException {
        List<BinaryDataInterface> binaryDatas = new ArrayList<>();
        for (Tag t : tags) {
            if (t instanceof BinaryDataInterface) {
                binaryDatas.add((BinaryDataInterface) t);
            }
        }
        return exportBinaryData(handler, outdir, binaryDatas, settings, evl);
    }

    /**
     * Export binary data.
     * @param handler AbortRetryIgnoreHandler
     * @param outdir Output directory
     * @param binaryDatas Binary data
     * @param settings Binary data export settings
     * @param evl Event listener
     * @return List of exported files
     * @throws IOException On I/O error
     * @throws InterruptedException On interrupt
     */
    public List<File> exportBinaryData(AbortRetryIgnoreHandler handler, String outdir, List<BinaryDataInterface> binaryDatas, BinaryDataExportSettings settings, EventListener evl) throws IOException, InterruptedException {
        List<File> ret = new ArrayList<>();
        if (CancellableWorker.isInterrupted()) {
            return ret;
        }

        if (binaryDatas.isEmpty()) {
            return ret;
        }

        File foutdir = new File(outdir);
        Path.createDirectorySafe(foutdir);

        int count = binaryDatas.size();

        if (count == 0) {
            return ret;
        }

        int currentIndex = 1;
        for (final BinaryDataInterface t : binaryDatas) {
            if (evl != null) {
                evl.handleExportingEvent("binarydata", currentIndex, count, t.getName());
            }

            String ext = t.getInnerSwf() == null ? ".bin" : ".swf";
            final File file = new File(outdir + File.separator + Helper.makeFileName(t.getCharacterExportFileName() + ext));
            new RetryTask(() -> {
                try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(file))) {
                    fos.write(t.getDataBytes().getRangeData());
                }
            }, handler).run();

            DefineBinaryDataTag bdt = (DefineBinaryDataTag) t.getTopLevelBinaryData();

            Set<String> classNames = bdt.getClassNames();
            if (Configuration.as3ExportNamesUseClassNamesOnly.get() && !classNames.isEmpty()) {
                for (String className : classNames) {
                    File classFile = new File(outdir + File.separator + Helper.makeFileName(t.getClassExportFileName(className) + ext));
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
                evl.handleExportedEvent("binarydata", currentIndex, count, t.getName());
            }

            currentIndex++;
        }

        return ret;
    }
}
