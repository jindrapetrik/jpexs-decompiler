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
import com.jpexs.decompiler.flash.ApplicationInfo;
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

    public List<File> exportFonts(AbortRetryIgnoreHandler handler, String outdir, List<Tag> tags, final FontExportSettings settings) throws IOException {
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
        for (Tag t : tags) {
            File newfile = null;
            if (t instanceof FontTag) {
                final FontTag st = (FontTag) t;
                final File file = new File(outdir + File.separator + st.getCharacterExportFileName() + ".ttf");
                newfile = file;
                new RetryTask(new RunnableIOEx() {
                    @Override
                    public void run() throws IOException {
                        exportFont(st, settings.mode, file);
                    }
                }, handler).run();

                ret.add(newfile);

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
    
    public void exportFont(final FontTag t, FontExportMode mode, File file) throws IOException {
        List<SHAPE> shapes = t.getGlyphShapeTable();
        Fontastic f = new Fontastic(t.getFontName(), file);
        String cop = t.getCopyright();

        // TODO: WOFF export
        f.getEngine().setCopyrightYear(cop == null ? "" : cop);
        f.setAuthor(ApplicationInfo.shortApplicationVerName);
        f.setVersion("1.0");
        f.setAscender(t.getAscent() / t.getDivider());
        f.setDescender(t.getDescent() / t.getDivider());

        for (int i = 0; i < shapes.size(); i++) {
            SHAPE s = shapes.get(i);
            final List<FPoint[]> contours = new ArrayList<>();
            PathExporter seb = new PathExporter(s, new ColorTransform()) {

                private double transformX(double x) {
                    return Math.ceil((double) (x / t.getDivider()));
                }

                private double transformY(double y) {
                    return -Math.ceil((double) (y / t.getDivider()));
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
            if (c == '.') {
                continue;
            }
            final FGlyph g = f.addGlyph(c);
            double adv = t.getGlyphAdvance(i);
            if (adv != -1) {
                g.setAdvanceWidth((int) adv);
            } else {
                g.setAdvanceWidth(t.getGlyphWidth(i) / t.getDivider() + 100);
            }
            for (FPoint[] cnt : contours) {
                if (cnt.length == 0) {
                    continue;
                }
                g.addContour(cnt);
            }

        }
        f.buildFont();
    }
}
