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
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.exporters.commonshape.ExportRectangle;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.commonshape.SVGExporter;
import com.jpexs.decompiler.flash.exporters.modes.ShapeExportMode;
import com.jpexs.decompiler.flash.exporters.settings.ShapeExportSettings;
import com.jpexs.decompiler.flash.exporters.shape.CanvasShapeExporter;
import com.jpexs.decompiler.flash.helpers.BMPFile;
import com.jpexs.decompiler.flash.helpers.ImageHelper;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.RenderContext;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.tags.enums.ImageFormat;
import com.jpexs.decompiler.flash.types.CXFORMWITHALPHA;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.Path;
import com.jpexs.helpers.SerializableImage;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class ShapeExporter {

    public List<File> exportShapes(AbortRetryIgnoreHandler handler, final String outdir, ReadOnlyTagList tags, final ShapeExportSettings settings, EventListener evl) throws IOException, InterruptedException {
        List<File> ret = new ArrayList<>();
        if (tags.isEmpty()) {
            return ret;
        }

        File foutdir = new File(outdir);
        Path.createDirectorySafe(foutdir);

        int count = 0;
        for (Tag t : tags) {
            if (t instanceof ShapeTag) {
                count++;
            }
        }

        if (count == 0) {
            return ret;
        }

        int currentIndex = 1;
        for (final Tag t : tags) {
            if (t instanceof ShapeTag) {
                final ShapeTag st = (ShapeTag) t;
                if (evl != null) {
                    evl.handleExportingEvent("shape", currentIndex, count, t.getName());
                }

                final File file = new File(outdir + File.separator + Helper.makeFileName(st.getCharacterExportFileName() + settings.getFileExtension()));
                new RetryTask(() -> {
                    switch (settings.mode) {
                        case SVG:
                            try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(file))) {
                                ExportRectangle rect = new ExportRectangle(st.getRect());
                                rect.xMax *= settings.zoom;
                                rect.yMax *= settings.zoom;
                                rect.xMin *= settings.zoom;
                                rect.yMin *= settings.zoom;
                                SVGExporter exporter = new SVGExporter(rect, settings.zoom);
                                st.toSVG(exporter, -2, new CXFORMWITHALPHA(), 0);
                                fos.write(Utf8Helper.getBytes(exporter.getSVG()));
                            }
                            break;
                        case PNG:
                        case BMP:
                            RECT rect = st.getRect();
                            int newWidth = (int) (rect.getWidth() * settings.zoom / SWF.unitDivisor) + 1;
                            int newHeight = (int) (rect.getHeight() * settings.zoom / SWF.unitDivisor) + 1;
                            SerializableImage img = new SerializableImage(newWidth, newHeight, SerializableImage.TYPE_INT_ARGB_PRE);
                            img.fillTransparent();
                            Matrix m = Matrix.getScaleInstance(settings.zoom);
                            m.translate(-rect.Xmin, -rect.Ymin);
                            st.toImage(0, 0, 0, new RenderContext(), img, false, m, m, m, new CXFORMWITHALPHA());
                            if (settings.mode == ShapeExportMode.PNG) {
                                ImageHelper.write(img.getBufferedImage(), ImageFormat.PNG, file);
                            } else {
                                BMPFile.saveBitmap(img.getBufferedImage(), file);
                            }
                            break;
                        case CANVAS:
                            try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(file))) {
                                SHAPE shp = st.getShapes();
                                int deltaX = -shp.getBounds().Xmin;
                                int deltaY = -shp.getBounds().Ymin;
                                CanvasShapeExporter cse = new CanvasShapeExporter(null, SWF.unitDivisor / settings.zoom, ((Tag) st).getSwf(), shp, new CXFORMWITHALPHA(), deltaX, deltaY);
                                cse.export();
                                Set<Integer> needed = new HashSet<>();
                                needed.add(st.getCharacterId());
                                st.getNeededCharactersDeep(needed);
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                SWF.writeLibrary(st.getSwf(), needed, baos);
                                fos.write(Utf8Helper.getBytes(cse.getHtml(new String(baos.toByteArray(), Utf8Helper.charset), SWF.getTypePrefix(st) + st.getCharacterId(), st.getRect())));
                            }
                            break;
                        case SWF:
                            try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(file))) {
                                try {
                                    new PreviewExporter().exportSwf(fos, st, null, 0);
                                } catch (ActionParseException ex) {
                                    Logger.getLogger(MorphShapeExporter.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }

                            break;
                    }
                }, handler).run();
                ret.add(file);

                if (evl != null) {
                    evl.handleExportedEvent("shape", currentIndex, count, t.getName());
                }

                currentIndex++;
            }
        }
        if (settings.mode == ShapeExportMode.CANVAS) {
            File fcanvas = new File(foutdir + File.separator + "canvas.js");
            Helper.saveStream(SWF.class.getClassLoader().getResourceAsStream("com/jpexs/helpers/resource/canvas.js"), fcanvas);
            ret.add(fcanvas);
        }
        return ret;
    }
}
