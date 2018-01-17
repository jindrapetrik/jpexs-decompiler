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
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.commonshape.ExportRectangle;
import com.jpexs.decompiler.flash.exporters.commonshape.SVGExporter;
import com.jpexs.decompiler.flash.exporters.modes.TextExportMode;
import com.jpexs.decompiler.flash.exporters.settings.TextExportSettings;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.types.CXFORMWITHALPHA;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.Path;
import com.jpexs.helpers.utf8.Utf8Helper;
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
public class TextExporter {

    public static final String TEXT_EXPORT_FILENAME_FORMATTED = "textsformatted.txt";

    public static final String TEXT_EXPORT_FILENAME_PLAIN = "textsplain.txt";

    public List<File> exportTexts(AbortRetryIgnoreHandler handler, String outdir, ReadOnlyTagList tags, final TextExportSettings settings, EventListener evl) throws IOException, InterruptedException {
        List<File> ret = new ArrayList<>();
        if (tags.isEmpty()) {
            return ret;
        }

        File foutdir = new File(outdir);
        Path.createDirectorySafe(foutdir);

        int count = 0;
        for (Tag t : tags) {
            if (t instanceof TextTag) {
                count++;
            }
        }

        if (count == 0) {
            return ret;
        }

        int currentIndex = 1;
        if (settings.mode == TextExportMode.SVG) {
            for (Tag t : tags) {
                if (t instanceof TextTag) {
                    if (evl != null) {
                        evl.handleExportingEvent("text", currentIndex, count, t.getName());
                    }

                    final TextTag textTag = (TextTag) t;
                    final File file = new File(outdir + File.separator + Helper.makeFileName(textTag.getCharacterExportFileName() + ".svg"));
                    new RetryTask(() -> {
                        try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(file))) {
                            ExportRectangle rect = new ExportRectangle(textTag.getRect());
                            SVGExporter exporter = new SVGExporter(rect, settings.zoom);
                            textTag.toSVG(exporter, -2, new CXFORMWITHALPHA(), 0);
                            fos.write(Utf8Helper.getBytes(exporter.getSVG()));
                        }
                    }, handler).run();
                    ret.add(file);

                    if (evl != null) {
                        evl.handleExportedEvent("text", currentIndex, count, t.getName());
                    }

                    currentIndex++;
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
            try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(file))) {
                for (final Tag t : tags) {
                    if (t instanceof TextTag) {
                        final TextTag textTag = (TextTag) t;
                        new RetryTask(() -> {
                            fos.write(Utf8Helper.getBytes("ID: " + textTag.getCharacterId() + Helper.newLine));
                            if (settings.mode == TextExportMode.FORMATTED) {
                                fos.write(Utf8Helper.getBytes(textTag.getFormattedText(false).text));
                            } else {
                                String separator = Configuration.textExportSingleFileRecordSeparator.get();
                                separator = Helper.newLine + separator + Helper.newLine;
                                List<String> texts = textTag.getTexts();
                                fos.write(Utf8Helper.getBytes(String.join(separator, texts)));
                            }
                            fos.write(Utf8Helper.getBytes(Helper.newLine + Configuration.textExportSingleFileSeparator.get() + Helper.newLine));
                        }, handler).run();
                    }
                }
            }
            ret.add(file);
        } else {
            for (Tag t : tags) {
                if (t instanceof TextTag) {
                    if (evl != null) {
                        evl.handleExportingEvent("text", currentIndex, count, t.getName());
                    }

                    final TextTag textTag = (TextTag) t;
                    final File file = new File(outdir + File.separator + Helper.makeFileName(textTag.getCharacterExportFileName() + ".txt"));
                    new RetryTask(() -> {
                        try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(file))) {
                            if (settings.mode == TextExportMode.FORMATTED) {
                                fos.write(Utf8Helper.getBytes(textTag.getFormattedText(false).text));
                            } else {
                                String separator = Configuration.textExportSingleFileRecordSeparator.get();
                                separator = Helper.newLine + separator + Helper.newLine;
                                List<String> texts = textTag.getTexts();
                                fos.write(Utf8Helper.getBytes(String.join(separator, texts)));
                            }
                        }
                    }, handler).run();
                    ret.add(file);

                    if (evl != null) {
                        evl.handleExportedEvent("text", currentIndex, count, t.getName());
                    }

                    currentIndex++;
                }
            }
        }
        return ret;
    }
}
