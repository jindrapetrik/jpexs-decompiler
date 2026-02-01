/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.exporters.modes.MorphShapeExportMode;
import com.jpexs.decompiler.flash.exporters.morphshape.CanvasMorphShapeExporter;
import com.jpexs.decompiler.flash.exporters.settings.MorphShapeExportSettings;
import com.jpexs.decompiler.flash.helpers.BMPFile;
import com.jpexs.decompiler.flash.helpers.ImageHelper;
import com.jpexs.decompiler.flash.tags.DefineMorphShapeTag;
import com.jpexs.decompiler.flash.tags.SetBackgroundColorTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.MorphShapeTag;
import com.jpexs.decompiler.flash.tags.base.RenderContext;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.tags.enums.ImageFormat;
import com.jpexs.decompiler.flash.timeline.Timeline;
import com.jpexs.decompiler.flash.types.CXFORMWITHALPHA;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.ImageResizer;
import com.jpexs.helpers.Path;
import com.jpexs.helpers.SerializableImage;
import com.jpexs.helpers.utf8.Utf8Helper;
import dev.matrixlab.webp4j.WebPCodec;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MorphShape exporter.
 *
 * @author JPEXS
 */
public class MorphShapeExporter {

    public List<File> exportMorphShapes(AbortRetryIgnoreHandler handler, final String outdir, ReadOnlyTagList tags, final MorphShapeExportSettings settings, EventListener evl) throws IOException, InterruptedException {
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
            if (t instanceof MorphShapeTag) {
                count++;
            }
        }

        if (count == 0) {
            return ret;
        }

        int currentIndex = 1;
        for (final Tag t : tags) {
            if (t instanceof MorphShapeTag) {
                if (evl != null) {
                    evl.handleExportingEvent("morphshape", currentIndex, count, t.getName());
                }

                int characterID = 0;
                if (t instanceof CharacterTag) {
                    characterID = ((CharacterTag) t).getCharacterId();
                }

                final int fCharacterID = characterID;
                
                final File file = new File(outdir + File.separator + characterID + settings.getFileExtension());
                final File fileStart = new File(outdir + File.separator + characterID + ".start" + settings.getFileExtension());
                final File fileEnd = new File(outdir + File.separator + characterID + ".end" + settings.getFileExtension());
                MorphShapeTag mst = (MorphShapeTag) t;

                new RetryTask(() -> {
                    ShapeTag st = mst.getStartShapeTag();
                    Matrix m;
                    RECT rect = st.getRect();
                    m = Matrix.getScaleInstance(settings.zoom);
                    m.translate(-rect.Xmin, -rect.Ymin);

                    switch (settings.mode) {
                        case SVG_START_END:
                            try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(fileStart))) {
                                ExportRectangle rect2 = new ExportRectangle(mst.getStartBounds());
                                rect2.xMax *= settings.zoom;
                                rect2.yMax *= settings.zoom;
                                rect2.xMin *= settings.zoom;
                                rect2.yMin *= settings.zoom;
                                SVGExporter exporter = new SVGExporter(rect2, settings.zoom, "shape");
                                mst.getStartShapeTag().toSVG(0, 0, exporter, -2, new CXFORMWITHALPHA(), 0, m, m);
                                fos.write(Utf8Helper.getBytes(exporter.getSVG()));
                            }
                            try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(fileEnd))) {
                                ExportRectangle rect2 = new ExportRectangle(mst.getStartBounds());
                                rect2.xMax *= settings.zoom;
                                rect2.yMax *= settings.zoom;
                                rect2.xMin *= settings.zoom;
                                rect2.yMin *= settings.zoom;
                                SVGExporter exporter = new SVGExporter(rect2, settings.zoom, "shape");
                                mst.getEndShapeTag().toSVG(0, 0, exporter, -2, new CXFORMWITHALPHA(), 0, m, m);
                                fos.write(Utf8Helper.getBytes(exporter.getSVG()));
                            }
                            break;
                        case SVG:
                            try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(file))) {
                                ExportRectangle rect2 = new ExportRectangle(mst.getRect());
                                rect2.xMax *= settings.zoom;
                                rect2.yMax *= settings.zoom;
                                rect2.xMin *= settings.zoom;
                                rect2.yMin *= settings.zoom;
                                SVGExporter exporter = new SVGExporter(rect2, settings.zoom, "morphshape");
                                mst.toSVG(exporter, new CXFORMWITHALPHA(), settings.duration);
                                fos.write(Utf8Helper.getBytes(exporter.getSVG()));
                            }
                            break;
                        case PNG_START_END:
                        case BMP_START_END:
                        case WEBP_START_END:
                            double unzoom = settings.zoom;
                            st = mst.getStartShapeTag();
                            rect = st.getRect();

                            int realAaScale = Configuration.calculateRealAaScale(rect.getWidth(), rect.getHeight(), settings.zoom, settings.aaScale);

                            int newWidth = (int) (rect.getWidth() * settings.zoom * realAaScale / SWF.unitDivisor) + 1;
                            int newHeight = (int) (rect.getHeight() * settings.zoom * realAaScale / SWF.unitDivisor) + 1;
                            SerializableImage img = new SerializableImage(newWidth, newHeight, SerializableImage.TYPE_INT_ARGB_PRE);
                            img.fillTransparent();
                            if (settings.mode == MorphShapeExportMode.BMP_START_END) {
                                RGB backColor = t.getSwf().getBackgroundColor().backgroundColor;
                                if (backColor != null) {
                                    Graphics2D g = (Graphics2D) img.getGraphics();
                                    g.setColor(backColor.toColor());
                                    g.fillRect(0, 0, img.getWidth(), img.getHeight());
                                }
                            }
                            m = Matrix.getScaleInstance(settings.zoom * realAaScale);
                            m.translate(-rect.Xmin, -rect.Ymin);
                            st.toImage(0, 0, 0, new RenderContext(), img, img, false, m, m, m, m, new CXFORMWITHALPHA(), unzoom * realAaScale, false, new ExportRectangle(rect), new ExportRectangle(rect), true, Timeline.DRAW_MODE_ALL, 0, true, realAaScale);

                            BufferedImage bim = img.getBufferedImage();

                            if (realAaScale > 1) {
                                bim = ImageResizer.resizeImage(bim, ((newWidth - 1) / realAaScale) + 1,
                                        ((newHeight - 1) / realAaScale) + 1, RenderingHints.VALUE_INTERPOLATION_BICUBIC, true);                                                            
                            }

                            if (settings.mode == MorphShapeExportMode.PNG_START_END) {
                                ImageHelper.write(bim, ImageFormat.PNG, fileStart);
                            } else if (settings.mode == MorphShapeExportMode.WEBP_START_END) {
                                try (FileOutputStream fos = new FileOutputStream(fileStart)) {
                                    fos.write(WebPCodec.encodeImage(bim, 100f));
                                }
                            } else {
                                BMPFile.saveBitmap(bim, fileStart);
                            }

                            st = mst.getEndShapeTag();
                            rect = st.getRect();
                            realAaScale = Configuration.calculateRealAaScale(rect.getWidth(), rect.getHeight(), settings.zoom, settings.aaScale);

                            newWidth = (int) (rect.getWidth() * settings.zoom * realAaScale / SWF.unitDivisor) + 1;
                            newHeight = (int) (rect.getHeight() * settings.zoom * realAaScale / SWF.unitDivisor) + 1;
                            img = new SerializableImage(newWidth, newHeight, SerializableImage.TYPE_INT_ARGB_PRE);
                            img.fillTransparent();
                            if (settings.mode == MorphShapeExportMode.BMP_START_END) {
                                RGB backColor = t.getSwf().getBackgroundColor().backgroundColor;
                                if (backColor != null) {
                                    Graphics2D g = (Graphics2D) img.getGraphics();
                                    g.setColor(backColor.toColor());
                                    g.fillRect(0, 0, img.getWidth(), img.getHeight());
                                }
                            }
                            m = Matrix.getScaleInstance(settings.zoom * realAaScale);
                            m.translate(-rect.Xmin, -rect.Ymin);
                            st.toImage(0, 0, 0, new RenderContext(), img, img, false, m, m, m, m, new CXFORMWITHALPHA(), unzoom, false, new ExportRectangle(rect), new ExportRectangle(rect), true, Timeline.DRAW_MODE_ALL, 0, true, settings.aaScale);

                            bim = img.getBufferedImage();

                            if (realAaScale > 1) {
                                bim = ImageResizer.resizeImage(bim, ((newWidth - 1) / realAaScale) + 1,
                                    ((newHeight - 1) / realAaScale) + 1, RenderingHints.VALUE_INTERPOLATION_BICUBIC, true);                                                            
                            }

                            if (settings.mode == MorphShapeExportMode.PNG_START_END) {
                                ImageHelper.write(bim, ImageFormat.PNG, fileEnd);
                            } else if (settings.mode == MorphShapeExportMode.WEBP_START_END) {
                                try (FileOutputStream fos = new FileOutputStream(fileEnd)) {
                                    fos.write(WebPCodec.encodeLosslessImage(bim));
                                }
                            } else {
                                BMPFile.saveBitmap(bim, fileEnd);
                            }
                            break;
                        case CANVAS:
                            try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(file))) {
                                int deltaX = -Math.min(mst.getStartBounds().Xmin, mst.getEndBounds().Xmin);
                                int deltaY = -Math.min(mst.getStartBounds().Ymin, mst.getEndBounds().Ymin);
                                CanvasMorphShapeExporter cse = new CanvasMorphShapeExporter(mst.getShapeNum(), ((Tag) mst).getSwf(), mst.getShapeAtRatio(0), mst.getShapeAtRatio(DefineMorphShapeTag.MAX_RATIO), new CXFORMWITHALPHA(), SWF.unitDivisor, deltaX, deltaY, settings.duration);
                                cse.export();
                                Set<Integer> needed = new HashSet<>();
                                Set<String> neededClasses = new HashSet<>();
                                CharacterTag ct = ((CharacterTag) mst);
                                needed.add(ct.getCharacterId());
                                ct.getNeededCharactersDeep(needed, neededClasses);
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                //FIXME!!! Handle Library Classes
                                baos.write(Utf8Helper.getBytes("var scalingGrids = {};\r\nvar boundRects = {};\r\n"));                        
                                SWF.libraryToHtmlCanvas(ct.getSwf(), needed, baos);
                                fos.write(Utf8Helper.getBytes(cse.getHtml(new String(baos.toByteArray(), Utf8Helper.charset), SWF.getTypePrefix(mst) + mst.getCharacterId(), mst.getRect())));
                            }
                            break;
                        case SWF:
                            try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(file))) {
                                try {
                                    new PreviewExporter().exportSwf(fos, mst, null, 0, false, settings.duration);
                                } catch (ActionParseException ex) {
                                    Logger.getLogger(MorphShapeExporter.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            break;                                              
                    }
                }, handler).run();

                
                SetBackgroundColorTag bkgTag = mst.getSwf().getBackgroundColor();
                Color backgroundColor = null;
                if (bkgTag != null) {
                    backgroundColor = bkgTag.backgroundColor.toColor();
                }
                
                int iteratorNumFrames = 2;
                if (settings.mode.hasDuration()) {
                    iteratorNumFrames = (int) Math.round(settings.duration * PreviewExporter.MORPH_SHAPE_ANIMATION_FRAME_RATE);
                } else if (settings.mode.hasFrames()) {
                    iteratorNumFrames = settings.numberOfFrames;
                }

                final boolean usesTransparency = settings.mode == MorphShapeExportMode.PNG_FRAMES 
                    || settings.mode == MorphShapeExportMode.GIF
                    || settings.mode == MorphShapeExportMode.WEBP_FRAMES;
                        
                
                MyFrameIterator frameImages = new MyFrameIterator(mst, iteratorNumFrames, evl, usesTransparency, backgroundColor, settings);

                
                File dir;
                switch (settings.mode) {
                    case SVG_FRAMES:
                        Matrix m;
                        RECT rect = mst.getRect();
                        m = Matrix.getScaleInstance(settings.zoom);
                        m.translate(-rect.Xmin, -rect.Ymin);
                    
                        dir = new File(outdir + File.separator + fCharacterID);
                        Path.createDirectorySafe(dir);

                        ExportRectangle rect2 = new ExportRectangle(mst.getRect());
                        rect2.xMax *= settings.zoom;
                        rect2.yMax *= settings.zoom;
                        rect2.xMin *= settings.zoom;
                        rect2.yMin *= settings.zoom;


                        for (int f = 0; f < settings.numberOfFrames; f++) {
                            int ratio = (int) Math.round(f * 65535.0 / (settings.numberOfFrames - 1));
                            File frameFile = new File(outdir + File.separator + fCharacterID + File.separator + (f + 1) + "_of_" + settings.numberOfFrames + settings.getFileExtension()); 

                            new RetryTask(() -> {   
                                try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(frameFile))) {
                                    SVGExporter exporter = new SVGExporter(rect2, settings.zoom, "shape");
                                    mst.getShapeTagAtRatio(ratio).toSVG(0, 0, exporter, -2, new CXFORMWITHALPHA(), 0, m, m);
                                    fos.write(Utf8Helper.getBytes(exporter.getSVG()));
                                }
                            }, handler).run();
                        }
                        break; 
                        
                    case GIF:
                        frameImages.reset();
                        new RetryTask(() -> {                            
                            FrameExporter.makeGIF(frameImages, PreviewExporter.MORPH_SHAPE_ANIMATION_FRAME_RATE, file, evl);
                            ret.add(file);
                        }, handler).run();
                        break;
                        
                    case AVI:
                        frameImages.reset();
                        new RetryTask(() -> {
                            FrameExporter.makeAVI(frameImages, PreviewExporter.MORPH_SHAPE_ANIMATION_FRAME_RATE, file, evl);
                            ret.add(file);
                        }, handler).run();
                        break;
                    case WEBP:
                        frameImages.reset();
                        new RetryTask(() -> {
                            FrameExporter.makeAnimatedWebP(frameImages, PreviewExporter.MORPH_SHAPE_ANIMATION_FRAME_RATE, file, evl);
                            ret.add(file);
                        }, handler).run();
                        break;
                    case PNG_FRAMES:
                    case BMP_FRAMES:                        
                    case WEBP_FRAMES:                                
                        dir = new File(outdir + File.separator + fCharacterID);
                        Path.createDirectorySafe(dir);

                        
                        frameImages.reset();
                        for (int i = 0; frameImages.hasNext(); i++) {
                            final int fi = i;
                            new RetryTask(() -> {                                
                                File frameFile = new File(outdir + File.separator + fCharacterID + File.separator + (fi + 1) + "_of_" + settings.numberOfFrames + settings.getFileExtension()); 
                                BufferedImage img = frameImages.next();
                                if (img != null) {
                                    if (settings.mode == MorphShapeExportMode.PNG_FRAMES) {
                                        ImageHelper.write(img, ImageFormat.PNG, frameFile);
                                    } else if (settings.mode == MorphShapeExportMode.WEBP_FRAMES) {
                                        try (FileOutputStream fos = new FileOutputStream(frameFile)) {
                                            fos.write(WebPCodec.encodeLosslessImage(img));
                                        }
                                    } else {
                                        BMPFile.saveBitmap(img, frameFile);
                                    }
                                    ret.add(frameFile);
                                }                                
                            }, handler).run();
                        }                                                
                        break;
                }
                
                Set<String> classNames = mst.getClassNames();
                if (Configuration.as3ExportNamesUseClassNamesOnly.get() && !classNames.isEmpty()) {
                    for (String className : classNames) {
                        if (Configuration.autoDeobfuscateIdentifiers.get()) {
                            className = DottedChain.parseNoSuffix(className).toPrintableString(new LinkedHashSet<>(), mst.getSwf(), true);
                        }
                        File classFile = new File(outdir + File.separator + Helper.makeFileName(className + settings.getFileExtension()));
                        File classFileStart = new File(outdir + File.separator + Helper.makeFileName(className + ".start" + settings.getFileExtension()));
                        File classFileEnd = new File(outdir + File.separator + Helper.makeFileName(className + ".end" + settings.getFileExtension()));
                        new RetryTask(() -> {
                            Files.copy(file.toPath(), classFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        }, handler).run();
                        ret.add(classFile);

                        if (fileStart.exists()) {
                            new RetryTask(() -> {
                                Files.copy(fileStart.toPath(), classFileStart.toPath(), StandardCopyOption.REPLACE_EXISTING);
                            }, handler).run();
                        }

                        if (fileEnd.exists()) {
                            new RetryTask(() -> {
                                Files.copy(fileEnd.toPath(), classFileEnd.toPath(), StandardCopyOption.REPLACE_EXISTING);
                            }, handler).run();
                        }
                    }
                    file.delete();
                    if (fileStart.exists()) {
                        fileStart.delete();
                    }
                    if (fileEnd.exists()) {
                        fileEnd.delete();
                    }
                } else {
                    ret.add(file);
                }

                if (CancellableWorker.isInterrupted()) {
                    break;
                }
                if (evl != null) {
                    evl.handleExportedEvent("morphshape", currentIndex, count, t.getName());
                }

                currentIndex++;
            }
        }

        if (settings.mode == MorphShapeExportMode.CANVAS) {
            File fcanvas = new File(foutdir + File.separator + "canvas.js");
            Helper.saveStream(SWF.class.getClassLoader().getResourceAsStream("com/jpexs/helpers/resource/canvas.js"), fcanvas);
            ret.add(fcanvas);
        }
        return ret;
    }
    
    private class MyFrameIterator implements Iterator<BufferedImage> {

        private int pos = 0;
        private final MorphShapeTag mst;
        private final int numFrames;
        private final EventListener evl;
        private final boolean usesTransparency;
        private final Color backgroundColor;
        private final MorphShapeExportSettings settings;
        private final RECT rect;
        private final int realAaScale;
        private final int newWidth;
        private final int newHeight;
        
        public MyFrameIterator(
                MorphShapeTag mst,
                int numFrames,
                final EventListener evl,
                boolean usesTransparency,
                Color backgroundColor,
                MorphShapeExportSettings settings
        ) {
            this.mst = mst;
            this.numFrames = numFrames;
            this.evl = evl;
            this.usesTransparency = usesTransparency;
            this.backgroundColor = backgroundColor;
            this.settings = settings;
            
            
            rect = mst.getRect();
            realAaScale = Configuration.calculateRealAaScale(rect.getWidth(), rect.getHeight(), settings.zoom, settings.aaScale);
            newWidth = (int) (rect.getWidth() * settings.zoom * realAaScale / SWF.unitDivisor) + 1;
            newHeight = (int) (rect.getHeight() * settings.zoom * realAaScale / SWF.unitDivisor) + 1;
        }

        public void reset() {
            pos = 0;
        }

        @Override
        public boolean hasNext() {
            if (CancellableWorker.isInterrupted()) {
                return false;
            }            
            return numFrames > pos;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public BufferedImage next() {
            if (!hasNext()) {
                return null;
            }

            String tagName = mst.getName();

            if (evl != null) {
                evl.handleExportingEvent("frame", pos + 1, numFrames, tagName);
            }
            
            int ratio = (int) Math.round(pos * 65535.0 / (numFrames - 1));
                                
            ShapeTag st = mst.getShapeTagAtRatio(ratio);

            SerializableImage img = new SerializableImage(newWidth, newHeight, SerializableImage.TYPE_INT_ARGB_PRE);
            img.fillTransparent();
            if (!usesTransparency) {
                Graphics2D g = (Graphics2D) img.getGraphics();
                if (backgroundColor == null) {
                    g.setColor(Color.white);
                } else {
                    g.setColor(backgroundColor);
                }
                g.fillRect(0, 0, img.getWidth(), img.getHeight());
            }
            Matrix m = Matrix.getScaleInstance(settings.zoom * realAaScale);
            m.translate(-rect.Xmin, -rect.Ymin);
            st.toImage(0, 0, 0, new RenderContext(), img, img, false, m, m, m, m, new CXFORMWITHALPHA(), settings.zoom * realAaScale, false, new ExportRectangle(rect), new ExportRectangle(rect), true, Timeline.DRAW_MODE_ALL, 0, true, realAaScale);

            BufferedImage result = img.getBufferedImage();

            if (realAaScale > 1) {
                result = ImageResizer.resizeImage(result, ((newWidth - 1) / realAaScale) + 1,
                        ((newHeight - 1) / realAaScale) + 1, RenderingHints.VALUE_INTERPOLATION_BICUBIC, true);                                                            
            }
            
            
            if (CancellableWorker.isInterrupted()) {
                return null;
            }
            pos++;
            if (evl != null) {
                evl.handleExportedEvent("frame", pos, numFrames, tagName);
            }

            return result;
        }
    }
}
