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

import com.google.typography.font.sfntly.Font;
import com.google.typography.font.sfntly.FontFactory;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.tools.conversion.woff.WoffWriter;
import com.jpexs.decompiler.flash.AbortRetryIgnoreHandler;
import com.jpexs.decompiler.flash.ApplicationInfo;
import com.jpexs.decompiler.flash.EventListener;
import com.jpexs.decompiler.flash.RetryTask;
import com.jpexs.decompiler.flash.RunnableIOEx;
import com.jpexs.decompiler.flash.exporters.modes.FontExportMode;
import com.jpexs.decompiler.flash.exporters.settings.FontExportSettings;
import com.jpexs.decompiler.flash.exporters.shape.PathExporter;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.helpers.Helper;
import fontastic.FGlyph;
import fontastic.FPoint;
import fontastic.Fontastic;
import fontastic.PVector;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class FontExporter {

    public List<File> exportFonts(AbortRetryIgnoreHandler handler, String outdir, List<Tag> tags, final FontExportSettings settings, EventListener evl) throws IOException {
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

        int count = 0;
        for (Tag t : tags) {
            if (t instanceof FontTag) {
                count++;
            }
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
                new RetryTask(new RunnableIOEx() {
                    @Override
                    public void run() throws IOException {
                        exportFont(st, settings.mode, file);
                    }
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
        return new byte[0];
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
        f.setAuthor(ApplicationInfo.shortApplicationVerName);
        f.setVersion("1.0");

        f.setAscender(Math.round(t.getAscent() / divider));
        f.setDescender(Math.round(t.getDescent() / divider));

        int glyphCount = 0;
        for (int i = 0; i < shapes.size(); i++) {
            SHAPE s = shapes.get(i);
            final List<FPoint[]> contours = new ArrayList<>();
            PathExporter seb = new PathExporter(s, new ColorTransform()) {

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
                    path.add(new FPoint(new PVector(transformX(x), transformY(y))));
                }

                @Override
                public void lineTo(double x, double y) {
                    path.add(new FPoint(new PVector(transformX(x), transformY(y))));
                }

                @Override
                public void curveTo(double controlX, double controlY, double anchorX, double anchorY) {
                    path.add(new FPoint(
                            new PVector(transformX(anchorX), transformY(anchorY)),
                            new PVector(transformX(controlX), transformY(controlY))
                    ));

                }
            };
            seb.export();
            char c = t.glyphToChar(i);
            if (contours.isEmpty()) {
                continue;
            }

            boolean hasContour = false;
            for (FPoint[] cnt : contours) {
                if (cnt.length > 0) {
                    hasContour = true;
                    break;
                }
            }

            if (!hasContour) {
                continue;
            }

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
            byte[] fontBytes = new byte[0];
            try (FileInputStream fis = new FileInputStream(ttfFile)) {
                fontBytes = new byte[(int) ttfFile.length()];
                fis.read(fontBytes);
            }

            Font[] fontArray = null;
            fontArray = fontFactory.loadFonts(fontBytes);

            Font font = fontArray[0];

            try (FileOutputStream fos = new FileOutputStream(file)) {
                WoffWriter w = new WoffWriter();
                WritableFontData woffData = w.convert(font);
                woffData.copyTo(fos);
            }
            ttfFile.delete();
        }
    }
}
