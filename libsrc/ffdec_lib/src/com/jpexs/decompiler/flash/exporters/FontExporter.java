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

import com.google.typography.font.sfntly.Font;
import com.google.typography.font.sfntly.FontFactory;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.tools.conversion.woff.WoffWriter;
import com.jpexs.decompiler.flash.AbortRetryIgnoreHandler;
import com.jpexs.decompiler.flash.ApplicationInfo;
import com.jpexs.decompiler.flash.EventListener;
import com.jpexs.decompiler.flash.ReadOnlyTagList;
import com.jpexs.decompiler.flash.RetryTask;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.FontExportMode;
import com.jpexs.decompiler.flash.exporters.settings.FontExportSettings;
import com.jpexs.decompiler.flash.exporters.shape.PathExporter;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.Path;
import fontastic.FGlyph;
import fontastic.FPoint;
import fontastic.Fontastic;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class FontExporter {

    public List<File> exportFonts(AbortRetryIgnoreHandler handler, String outdir, ReadOnlyTagList tags, final FontExportSettings settings, EventListener evl) throws IOException, InterruptedException {
        List<File> ret = new ArrayList<>();
        if (tags.isEmpty()) {
            return ret;
        }

        File foutdir = new File(outdir);
        Path.createDirectorySafe(foutdir);

        int count = 0;
        for (Tag t : tags) {
            if (t instanceof FontTag) {
                count++;
            }
        }

        if (count == 0) {
            return ret;
        }

        int currentIndex = 1;
        for (Tag t : tags) {
            if (t instanceof FontTag) {
                if (evl != null) {
                    evl.handleExportingEvent("font", currentIndex, count, t.getName());
                }

                final FontTag st = (FontTag) t;
                String ext = ".ttf";
                if (settings.mode == FontExportMode.WOFF) {
                    ext = ".woff";
                }
                final File file = new File(outdir + File.separator + Helper.makeFileName(st.getCharacterExportFileName() + ext));
                new RetryTask(() -> {
                    exportFont(st, settings.mode, file);
                }, handler).run();

                ret.add(file);

                if (evl != null) {
                    evl.handleExportedEvent("font", currentIndex, count, t.getName());
                }

                currentIndex++;
            }
        }

        return ret;
    }

    public byte[] exportFont(final FontTag t, FontExportMode mode) {
        try {
            String ext = null;
            switch (mode) {
                case TTF:
                    ext = ".ttf";
                    break;
                case WOFF:
                    ext = ".woff";
            }
            File f = File.createTempFile("temp", ext);
            exportFont(t, mode, f);
            return Helper.readFile(f.getPath());
        } catch (IOException ex) {
            Logger.getLogger(FontExporter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return SWFInputStream.BYTE_ARRAY_EMPTY;
    }

    public void exportFont(FontTag ft, FontExportMode mode, File file) throws IOException {
        final FontTag t = ft.toClassicFont();
        List<SHAPE> shapes = t.getGlyphShapeTable();

        final double divider = t.getDivider();

        File ttfFile = file;

        if (mode == FontExportMode.WOFF) {
            ttfFile = File.createTempFile("ffdec_export", ".ttf");
        }

        String fontName = Helper.makeFileName(t.getFontNameIntag());
        if (fontName.length() == 0) {
            fontName = "noname";
        }

        Fontastic f = new Fontastic(fontName, ttfFile);
        String cop = t.getCopyright();

        f.getEngine().setCopyrightYear(cop == null ? "" : cop);
        if (Configuration.setFFDecVersionInExportedFont.get()) {
            f.setAuthor(ApplicationInfo.shortApplicationVerName);
        } else {
            f.setAuthor(ApplicationInfo.SHORT_APPLICATION_NAME);
        }

        f.setVersion("1.0");

        SWF swf = t.getSwf();
        if (swf != null) {
            Date date = swf.getFileModificationDate();
            f.setCreationDate(date);
            f.setModificationDate(date);
        }

        int ascent = t.getAscent();
        if (ascent != -1) {
            float value = Math.round(ascent / divider);
            value = Math.min(value, 1024);
            f.setAscender(value);
        }

        int descent = t.getDescent();
        if (descent != -1) {
            float value = Math.round(descent / divider);
            value = Math.min(value, 1024);
            f.setDescender(value);
        }

        int glyphCount = 0;
        for (int i = 0; i < shapes.size(); i++) {

            //if there are more glyphs for one char (in some weird fonts), use the last glyph
            char c = t.glyphToChar(i);
            while (i + 1 < shapes.size() && t.glyphToChar(i + 1) == c) {
                i++;
            }

            SHAPE s = shapes.get(i);
            final List<FPoint[]> contours = new ArrayList<>();
            PathExporter seb = new PathExporter(swf, s, null) {

                private double transformX(double x) {
                    return Math.ceil((double) (x / divider));
                }

                private double transformY(double y) {
                    return -Math.ceil((double) (y / divider));
                }

                List<FPoint> path = new ArrayList<>();

                @Override
                protected void finalizePath() {
                    FPoint[] points = path.toArray(new FPoint[path.size()]);
                    if (points.length > 0) {
                        contours.add(points);
                    }
                    path.clear();
                }

                @Override
                public void moveTo(double x, double y) {
                    finalizePath();
                    path.add(new FPoint(transformX(x), transformY(y)));
                }

                @Override
                public void lineTo(double x, double y) {
                    path.add(new FPoint(transformX(x), transformY(y)));
                }

                @Override
                public void curveTo(double controlX, double controlY, double anchorX, double anchorY) {
                    path.add(new FPoint(
                            new FPoint(transformX(anchorX), transformY(anchorY)),
                            new FPoint(transformX(controlX), transformY(controlY))
                    ));

                }
            };
            seb.export();

            FGlyph g = f.addGlyph(c);
            glyphCount++;
            double adv = t.getGlyphAdvance(i);
            if (adv != -1) {
                g.setAdvanceWidth((int) Math.round(adv / divider));
            } else {
                g.setAdvanceWidth((int) Math.round(t.getGlyphWidth(i) / divider + 100));
            }
            for (FPoint[] cnt : contours) {
                if (cnt.length == 0) {
                    continue;
                }
                g.addContour(cnt);
            }

        }

        if (glyphCount == 0) {
            return;
        }

        f.buildFont();

        if (mode == FontExportMode.WOFF) {
            FontFactory fontFactory = FontFactory.getInstance();
            byte[] fontBytes;
            try (FileInputStream fis = new FileInputStream(ttfFile)) {
                fontBytes = new byte[(int) ttfFile.length()];
                fis.read(fontBytes);
            }

            Font[] fontArray = null;
            fontArray = fontFactory.loadFonts(fontBytes);

            Font font = fontArray[0];

            try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(file))) {
                WoffWriter w = new WoffWriter();
                WritableFontData woffData = w.convert(font);
                woffData.copyTo(fos);
            }

            ttfFile.delete();
        }
    }
}
