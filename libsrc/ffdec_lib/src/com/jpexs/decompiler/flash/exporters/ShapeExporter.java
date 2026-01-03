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
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.configuration.Configuration;
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
import com.jpexs.decompiler.flash.timeline.Timeline;
import com.jpexs.decompiler.flash.types.CXFORMWITHALPHA;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.Path;
import com.jpexs.helpers.SerializableImage;
import com.jpexs.helpers.utf8.Utf8Helper;
import dev.matrixlab.webp4j.WebPCodec;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Shape exporter.
 *
 * @author JPEXS
 */
public class ShapeExporter {

    public List<File> exportShapes(AbortRetryIgnoreHandler handler, final String outdir, final SWF swf, ReadOnlyTagList tags, final ShapeExportSettings settings, EventListener evl, double unzoom, int aaScale) throws IOException, InterruptedException {
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
                    Matrix m = Matrix.getScaleInstance(settings.zoom);
                    RECT rect = st.getRect();
                    m.translate(-rect.Xmin, -rect.Ymin);
                    switch (settings.mode) {
                        case SVG:
                            try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(file))) {
                                ExportRectangle rect2 = new ExportRectangle(st.getRect());
                                rect2.xMax *= settings.zoom;
                                rect2.yMax *= settings.zoom;
                                rect2.xMin *= settings.zoom;
                                rect2.yMin *= settings.zoom;
                                SVGExporter exporter = new SVGExporter(rect2, settings.zoom, "shape");
                                st.toSVG(0, 0, exporter, -2, new CXFORMWITHALPHA(), 0, m, m);
                                fos.write(Utf8Helper.getBytes(exporter.getSVG()));
                            }
                            break;
                        case PNG:
                        case BMP:
                        case WEBP:
                            int realAaScale = Configuration.calculateRealAaScale(rect.getWidth(), rect.getHeight(), settings.zoom, aaScale);
                            int newWidth = (int) (rect.getWidth() * settings.zoom * realAaScale / SWF.unitDivisor) + 1;
                            int newHeight = (int) (rect.getHeight() * settings.zoom * realAaScale / SWF.unitDivisor) + 1;
                            SerializableImage img = new SerializableImage(newWidth, newHeight, SerializableImage.TYPE_INT_ARGB_PRE);
                            img.fillTransparent();
                            if (settings.mode == ShapeExportMode.BMP) {
                                RGB backColor = swf.getBackgroundColor().backgroundColor;
                                if (backColor != null) {
                                    Graphics2D g = (Graphics2D) img.getGraphics();
                                    g.setColor(backColor.toColor());
                                    g.fillRect(0, 0, img.getWidth(), img.getHeight());
                                }
                            }                 
                            Matrix m2 = Matrix.getScaleInstance(settings.zoom * realAaScale);
                            m2.translate(-rect.Xmin, -rect.Ymin);
                            
                            st.toImage(0, 0, 0, new RenderContext(), img, img, false, m2, m2, m2, m2, new CXFORMWITHALPHA(), unzoom * realAaScale, false, new ExportRectangle(rect), new ExportRectangle(rect), true, Timeline.DRAW_MODE_ALL, 0, true, realAaScale);
                            
                            BufferedImage bim = img.getBufferedImage();
                            if (realAaScale > 1) {
                                SerializableImage img2 = new SerializableImage(((newWidth - 1) / realAaScale) + 1,
                                        ((newHeight - 1) / realAaScale) + 1, SerializableImage.TYPE_INT_ARGB_PRE);
                                img2.fillTransparent();
                                Graphics g2 = img2.getGraphics();
                                g2.drawImage(img.getBufferedImage().getScaledInstance(img2.getWidth(), img2.getHeight(), Image.SCALE_SMOOTH), 0, 0, null);
                                bim = img2.getBufferedImage();
                            }
                            
                            if (settings.mode == ShapeExportMode.PNG) {
                                ImageHelper.write(bim, ImageFormat.PNG, file);
                            } else if (settings.mode == ShapeExportMode.WEBP) {
                                try (FileOutputStream fos = new FileOutputStream(file)) {
                                    fos.write(WebPCodec.encodeLosslessImage(bim));
                                }
                            } else {
                                BMPFile.saveBitmap(bim, file);
                            }
                            break;
                        case CANVAS:
                            try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(file))) {
                                SHAPE shp = st.getShapes();
                                int deltaX = -shp.getBounds(1).Xmin;
                                int deltaY = -shp.getBounds(1).Ymin;
                                CanvasShapeExporter cse = new CanvasShapeExporter(st.getWindingRule(), st.getShapeNum(), null, SWF.unitDivisor / settings.zoom, ((Tag) st).getSwf(), shp, new CXFORMWITHALPHA(), deltaX, deltaY);
                                cse.export();
                                Set<Integer> needed = new HashSet<>();
                                Set<String> neededClasses = new HashSet<>();
                                needed.add(st.getCharacterId());
                                st.getNeededCharactersDeep(needed, neededClasses);
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                //FIXME!!! Handle Library Classes
                                SWF.libraryToHtmlCanvas(st.getSwf(), needed, baos);
                                fos.write(Utf8Helper.getBytes(cse.getHtml(new String(baos.toByteArray(), Utf8Helper.charset), SWF.getTypePrefix(st) + st.getCharacterId(), st.getRect())));
                            }
                            break;
                        case SWF:
                            try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(file))) {
                                try {
                                    new PreviewExporter().exportSwf(fos, st, null, 0, false);
                                } catch (ActionParseException ex) {
                                    Logger.getLogger(MorphShapeExporter.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }

                            break;
                    }
                }, handler).run();

                Set<String> classNames = st.getClassNames();
                if (Configuration.as3ExportNamesUseClassNamesOnly.get() && !classNames.isEmpty()) {
                    for (String className : classNames) {
                        if (Configuration.autoDeobfuscateIdentifiers.get()) {
                            className = DottedChain.parseNoSuffix(className).toPrintableString(new LinkedHashSet<>(), st.getSwf(), true);
                        }
                        File classFile = new File(outdir + File.separator + Helper.makeFileName(className + settings.getFileExtension()));
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
