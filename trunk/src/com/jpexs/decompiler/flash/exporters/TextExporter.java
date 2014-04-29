/*
 * Copyright (C) 2014 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.types.CXFORMWITHALPHA;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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
                    final File file = new File(outdir + File.separator + textTag.getCharacterId() + ".svg");
                    new RetryTask(new RunnableIOEx() {
                        @Override
                        public void run() throws IOException {
                            try (FileOutputStream fos = new FileOutputStream(file)) {
                                ExportRectangle rect = new ExportRectangle(textTag.getRect());
                                SVGExporter exporter = new SVGExporter(rect);
                                textTag.toSVG(exporter, -2, new CXFORMWITHALPHA(), 0);
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
            final File file = new File(outdir + File.separator
                    + (settings.mode == TextExportMode.FORMATTED ? TEXT_EXPORT_FILENAME_FORMATTED : TEXT_EXPORT_FILENAME_PLAIN));
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
                                    fos.write(Utf8Helper.getBytes(textTag.getText(Configuration.textExportSingleFileRecordSeparator.get())));
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
                    final File file = new File(outdir + File.separator + textTag.getCharacterId() + ".txt");
                    new RetryTask(new RunnableIOEx() {
                        @Override
                        public void run() throws IOException {
                            try (FileOutputStream fos = new FileOutputStream(file)) {
                                if (settings.mode == TextExportMode.FORMATTED) {
                                    fos.write(Utf8Helper.getBytes(textTag.getFormattedText()));
                                } else {
                                    fos.write(Utf8Helper.getBytes(textTag.getText(Configuration.textExportSingleFileRecordSeparator.get())));
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
