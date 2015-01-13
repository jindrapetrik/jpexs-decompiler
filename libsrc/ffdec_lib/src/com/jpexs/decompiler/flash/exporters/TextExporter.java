/*
 *  Copyright (C) 2014-2015 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.RetryTask;
import com.jpexs.decompiler.flash.RunnableIOEx;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.commonshape.ExportRectangle;
import com.jpexs.decompiler.flash.exporters.commonshape.SVGExporter;
import com.jpexs.decompiler.flash.exporters.modes.TextExportMode;
import com.jpexs.decompiler.flash.exporters.settings.TextExportSettings;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.types.CXFORMWITHALPHA;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.Path;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class TextExporter {

    public static final String TEXT_EXPORT_FOLDER = "texts";
    public static final String TEXT_EXPORT_FILENAME_FORMATTED = "textsformatted.txt";
    public static final String TEXT_EXPORT_FILENAME_PLAIN = "textsplain.txt";

    public List<File> exportTexts(AbortRetryIgnoreHandler handler, String outdir, List<Tag> tags, final TextExportSettings settings) throws IOException {
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

        if (settings.mode == TextExportMode.SVG) {
            for (Tag t : tags) {
                if (t instanceof TextTag) {
                    final TextTag textTag = (TextTag) t;
                    final File file = new File(outdir + File.separator + Helper.makeFileName(textTag.getCharacterExportFileName() + ".svg"));
                    new RetryTask(new RunnableIOEx() {
                        @Override
                        public void run() throws IOException {
                            try (FileOutputStream fos = new FileOutputStream(file)) {
                                ExportRectangle rect = new ExportRectangle(textTag.getRect(new HashSet<BoundedTag>()));
                                SVGExporter exporter = new SVGExporter(rect);
                                textTag.toSVG(exporter, -2, new CXFORMWITHALPHA(), 0, settings.zoom);
                                fos.write(Utf8Helper.getBytes(exporter.getSVG()));
                            }
                        }
                    }, handler).run();
                    ret.add(file);
                }
            }
            return ret;
        }

        if (settings.singleFile) {
            String fileName = Configuration.overrideTextExportFileName.get();
            if (fileName != null && !fileName.isEmpty()) {
                String swfName = Path.getFileNameWithoutExtension(new File(tags.get(0).getSwf().getShortFileName()));
                fileName = fileName.replace("{fileName}", swfName);
            } else {
                fileName = settings.mode == TextExportMode.FORMATTED ? TEXT_EXPORT_FILENAME_FORMATTED : TEXT_EXPORT_FILENAME_PLAIN;
            }
            final File file = new File(outdir + File.separator + fileName);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                for (final Tag t : tags) {
                    if (t instanceof TextTag) {
                        final TextTag textTag = (TextTag) t;
                        new RetryTask(new RunnableIOEx() {
                            @Override
                            public void run() throws IOException {
                                fos.write(Utf8Helper.getBytes("ID: " + textTag.getCharacterId() + Helper.newLine));
                                if (settings.mode == TextExportMode.FORMATTED) {
                                    fos.write(Utf8Helper.getBytes(textTag.getFormattedText()));
                                } else {
                                    String separator = Configuration.textExportSingleFileRecordSeparator.get();
                                    separator = Helper.newLine + separator + Helper.newLine;
                                    List<String> texts = textTag.getTexts();
                                    fos.write(Utf8Helper.getBytes(String.join(separator, texts)));
                                }
                                fos.write(Utf8Helper.getBytes(Helper.newLine + Configuration.textExportSingleFileSeparator.get() + Helper.newLine));
                            }
                        }, handler).run();
                    }
                }
            }
            ret.add(file);
        } else {
            for (Tag t : tags) {
                if (t instanceof TextTag) {
                    final TextTag textTag = (TextTag) t;
                    final File file = new File(outdir + File.separator + Helper.makeFileName(textTag.getCharacterExportFileName() + ".txt"));
                    new RetryTask(new RunnableIOEx() {
                        @Override
                        public void run() throws IOException {
                            try (FileOutputStream fos = new FileOutputStream(file)) {
                                if (settings.mode == TextExportMode.FORMATTED) {
                                    fos.write(Utf8Helper.getBytes(textTag.getFormattedText()));
                                } else {
                                    String separator = Configuration.textExportSingleFileRecordSeparator.get();
                                    separator = Helper.newLine + separator + Helper.newLine;
                                    List<String> texts = textTag.getTexts();
                                    fos.write(Utf8Helper.getBytes(String.join(separator, texts)));
                                }
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
