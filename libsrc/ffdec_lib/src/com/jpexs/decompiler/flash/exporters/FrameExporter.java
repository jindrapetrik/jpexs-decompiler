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

import com.jpacker.JPacker;
import com.jpexs.decompiler.flash.AbortRetryIgnoreHandler;
import com.jpexs.decompiler.flash.EventListener;
import com.jpexs.decompiler.flash.RetryTask;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.commonshape.ExportRectangle;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.commonshape.SVGExporter;
import com.jpexs.decompiler.flash.exporters.modes.FontExportMode;
import com.jpexs.decompiler.flash.exporters.modes.FrameExportMode;
import com.jpexs.decompiler.flash.exporters.settings.ButtonExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.FrameExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.SpriteExportSettings;
import com.jpexs.decompiler.flash.exporters.shape.CanvasShapeExporter;
import com.jpexs.decompiler.flash.helpers.BMPFile;
import com.jpexs.decompiler.flash.helpers.ImageHelper;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.SetBackgroundColorTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.RenderContext;
import com.jpexs.decompiler.flash.tags.enums.ImageFormat;
import com.jpexs.decompiler.flash.timeline.DepthState;
import com.jpexs.decompiler.flash.timeline.Frame;
import com.jpexs.decompiler.flash.timeline.Timeline;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.filters.BEVELFILTER;
import com.jpexs.decompiler.flash.types.filters.COLORMATRIXFILTER;
import com.jpexs.decompiler.flash.types.filters.CONVOLUTIONFILTER;
import com.jpexs.decompiler.flash.types.filters.DROPSHADOWFILTER;
import com.jpexs.decompiler.flash.types.filters.FILTER;
import com.jpexs.decompiler.flash.types.filters.GLOWFILTER;
import com.jpexs.decompiler.flash.types.filters.GRADIENTBEVELFILTER;
import com.jpexs.decompiler.flash.types.filters.GRADIENTGLOWFILTER;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.Path;
import com.jpexs.helpers.SerializableImage;
import com.jpexs.helpers.utf8.Utf8Helper;
import gnu.jpdf.PDFGraphics;
import gnu.jpdf.PDFJob;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import net.kroo.elliot.GifSequenceWriter;
import net.weiner.kevin.AnimatedGifEncoder;
import org.monte.media.VideoFormatKeys;
import org.monte.media.avi.AVIWriter;

/**
 * Frame exporter.
 *
 * @author JPEXS
 */
public class FrameExporter {

    private static final Logger logger = Logger.getLogger(FrameExporter.class.getName());

    public List<File> exportButtonFrames(AbortRetryIgnoreHandler handler, String outdir, SWF swf, int containerId, List<Integer> frames, ButtonExportSettings settings, EventListener evl) throws IOException, InterruptedException {
        FrameExportMode fem;
        switch (settings.mode) {
            case BMP:
                fem = FrameExportMode.BMP;
                break;
            case PNG:
                fem = FrameExportMode.PNG;
                break;
            case SVG:
                fem = FrameExportMode.SVG;
                break;
            case SWF:
                fem = FrameExportMode.SWF;
                break;
            default:
                throw new Error("Unsupported button export mode: " + settings.mode);
        }

        if (frames == null) {
            frames = new ArrayList<>();
            frames.add(0); // todo: export all frames
        }

        FrameExportSettings fes = new FrameExportSettings(fem, settings.zoom, true);
        return exportFrames(handler, outdir, swf, containerId, frames, 1, fes, evl);
    }

    public List<File> exportSpriteFrames(AbortRetryIgnoreHandler handler, String outdir, SWF swf, int containerId, List<Integer> frames, int subframesLength, SpriteExportSettings settings, EventListener evl) throws IOException, InterruptedException {
        FrameExportMode fem;
        switch (settings.mode) {
            case PNG:
                fem = FrameExportMode.PNG;
                break;
            case GIF:
                fem = FrameExportMode.GIF;
                break;
            case AVI:
                fem = FrameExportMode.AVI;
                break;
            case SVG:
                fem = FrameExportMode.SVG;
                break;
            case CANVAS:
                fem = FrameExportMode.CANVAS;
                break;
            case PDF:
                fem = FrameExportMode.PDF;
                break;
            case BMP:
                fem = FrameExportMode.BMP;
                break;
            case SWF:
                fem = FrameExportMode.SWF;
                break;
            default:
                throw new Error("Unsupported sprite export mode");
        }

        FrameExportSettings fes = new FrameExportSettings(fem, settings.zoom, true);
        return exportFrames(handler, outdir, swf, containerId, frames, subframesLength, fes, evl);
    }

    private class MyFrameIterator implements Iterator<BufferedImage> {

        private int pos = 0;
        private final Timeline tim;
        private final List<Integer> fframes;
        private final EventListener evl;
        private final boolean usesTransparency;
        private final Color backgroundColor;
        private final FrameExportSettings settings;
        private final int subframeLength;
        private final boolean subFrameMode;

        public MyFrameIterator(
                Timeline tim,
                List<Integer> fframes,
                final EventListener evl,
                boolean usesTransparency,
                Color backgroundColor,
                FrameExportSettings settings,
                int subframeLength
        ) {
            this.tim = tim;
            this.fframes = fframes;
            this.evl = evl;
            this.usesTransparency = usesTransparency;
            this.backgroundColor = backgroundColor;
            this.settings = settings;
            this.subframeLength = subframeLength;
            this.subFrameMode = this.subframeLength > 1;
        }

        public void reset() {
            pos = 0;
        }

        @Override
        public boolean hasNext() {
            if (CancellableWorker.isInterrupted()) {
                return false;
            }
            if (subframeLength > 1) {
                return subframeLength > pos;
            }
            return fframes.size() > pos;
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

            Tag parentTag = tim.getParentTag();
            String tagName = parentTag == null ? "" : parentTag.getName();

            if (evl != null) {
                evl.handleExportingEvent("frame", pos + 1, fframes.size(), tagName);
            }
            int max = subFrameMode ? subframeLength : fframes.size();

            int fframe = subFrameMode ? fframes.get(0) : fframes.get(pos++);
            BufferedImage result = SWF.frameToImageGet(tim, fframe, subFrameMode ? pos++ : 0, null, 0, tim.displayRect, new Matrix(), null, backgroundColor == null && !usesTransparency ? Color.white : backgroundColor, settings.zoom, true).getBufferedImage();
            if (CancellableWorker.isInterrupted()) {
                return null;
            }
            if (evl != null) {
                evl.handleExportedEvent("frame", pos, max, tagName);
            }

            return result;
        }
    }

    public List<File> exportFrames(AbortRetryIgnoreHandler handler, String outdir, final SWF swf, int containerId, List<Integer> frames, int subFramesLength, final FrameExportSettings settings, final EventListener evl) throws IOException, InterruptedException {
        final List<File> ret = new ArrayList<>();
        if (CancellableWorker.isInterrupted()) {
            return ret;
        }

        if (swf.getTags().isEmpty()) {
            return ret;
        }
        Timeline tim0;
        List<String> paths = new ArrayList<>();
        String subPath = "";
        if (subFramesLength > 1) {
            subPath = File.separator + (frames.get(0) + 1);
        }

        if (containerId == 0) {
            tim0 = swf.getTimeline();
            paths.add(subPath);
        } else {
            tim0 = ((Timelined) swf.getCharacter(containerId)).getTimeline();

            Set<String> classNames = swf.getCharacter(containerId).getClassNames();
            if (Configuration.as3ExportNamesUseClassNamesOnly.get() && !classNames.isEmpty()) {
                for (String className : classNames) {
                    if (Configuration.autoDeobfuscateIdentifiers.get()) {
                        className = DottedChain.parseNoSuffix(className).toPrintableString(new LinkedHashSet<>(), swf, true);
                    }
                    paths.add(File.separator + Helper.makeFileName(className) + subPath);
                }
            } else {
                paths.add(File.separator + Helper.makeFileName(swf.getCharacter(containerId).getExportFileName()) + subPath);
            }
        }

        final Timeline tim = tim0;

        boolean exportAll = frames == null;
        if (frames == null) {
            frames = new ArrayList<>();
            for (Frame frame : tim.getFrames()) {
                frames.add(frame.frame);
            }
        }

        final List<File> foutdirs = new ArrayList<>();

        for (String path : paths) {
            File foutdir = new File(outdir + path);
            foutdirs.add(foutdir);
            Path.createDirectorySafe(foutdir);
        }

        final List<Integer> fframes = frames;

        Color backgroundColor = null;
        SetBackgroundColorTag setBgColorTag = swf.getBackgroundColor();
        if (!settings.transparentBackground && setBgColorTag != null) {
            backgroundColor = setBgColorTag.backgroundColor.toColor();
        }

        if (settings.mode == FrameExportMode.SVG) {
            int max = frames.size();
            if (subFramesLength > 1) {
                max = subFramesLength;
            }
            for (int i = 0; i < max; i++) {
                if (evl != null) {
                    Tag parentTag = tim.getParentTag();
                    evl.handleExportingEvent("frame", i + 1, max, parentTag == null ? "" : parentTag.getName());
                }

                final int fi = i;
                final Color fbackgroundColor = backgroundColor;
                for (File foutdir : foutdirs) {
                    new RetryTask(() -> {
                        int frame = subFramesLength > 1 ? fframes.get(0) : fframes.get(fi);
                        File f = new File(foutdir + File.separator + ((subFramesLength > 1 ? fi : fframes.get(fi)) + 1) + ".svg");
                        try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(f))) {
                            ExportRectangle rect = new ExportRectangle(tim.displayRect);
                            rect.xMax *= settings.zoom;
                            rect.yMax *= settings.zoom;
                            rect.xMin *= settings.zoom;
                            rect.yMin *= settings.zoom;
                            SVGExporter exporter = new SVGExporter(rect, settings.zoom, "frame", fbackgroundColor);

                            tim.toSVG(frame, subFramesLength > 1 ? fi : 0, null, 0, exporter, null, 0, new Matrix(), new Matrix());
                            fos.write(Utf8Helper.getBytes(exporter.getSVG()));
                        }
                        ret.add(f);
                    }, handler).run();
                }

                if (CancellableWorker.isInterrupted()) {
                    break;
                }

                if (evl != null) {
                    Tag parentTag = tim.getParentTag();
                    evl.handleExportedEvent("frame", i + 1, max, parentTag == null ? "" : parentTag.getName());
                }
            }

            return ret;
        }

        if (settings.mode == FrameExportMode.CANVAS) {
            if (evl != null) {
                Tag parentTag = tim.getParentTag();
                evl.handleExportingEvent("canvas", 1, 1, parentTag == null ? "" : parentTag.getName());
            }

            final Timeline ftim = tim;
            final Color fbackgroundColor = null;
            final SWF fswf = swf;
            for (File foutdir : foutdirs) {
                new RetryTask(() -> {
                    File fcanvas = new File(foutdir + File.separator + "canvas.js");
                    Helper.saveStream(SWF.class.getClassLoader().getResourceAsStream("com/jpexs/helpers/resource/canvas.js"), fcanvas);
                    ret.add(fcanvas);

                    File f = new File(foutdir + File.separator + "frames.js");
                    File fmin = new File(foutdir + File.separator + "frames.min.js");
                    int width = (int) (ftim.displayRect.getWidth() * settings.zoom / SWF.unitDivisor);
                    int height = (int) (ftim.displayRect.getHeight() * settings.zoom / SWF.unitDivisor);
                    try (final OutputStream fos = new BufferedOutputStream(new FileOutputStream(f))) {
                        fos.write(Utf8Helper.getBytes("\r\n"));
                        fos.write(Utf8Helper.getBytes("var scalingGrids = {};\r\nvar boundRects = {};\r\n"));
                        Set<Integer> library = new HashSet<>();
                        ftim.getNeededCharacters(fframes, library);

                        SWF.libraryToHtmlCanvas(fswf, library, fos);

                        String currentName = ftim.id == 0 ? "main" : SWF.getTypePrefix(fswf.getCharacter(ftim.id)) + ftim.id;

                        StringBuilder sb = new StringBuilder();
                        sb.append("function ").append(currentName).append("(ctx,ctrans,frame,ratio,time){\r\n");
                        sb.append("\tctx.save();\r\n");
                        sb.append("\tctx.transform(1,0,0,1,").append(-ftim.displayRect.Xmin * settings.zoom / SWF.unitDivisor).append(",").append(-ftim.displayRect.Ymin * settings.zoom / SWF.unitDivisor).append(");\r\n");
                        framesToHtmlCanvas(sb, SWF.unitDivisor / settings.zoom, ftim, fframes, 0, null, 0, ftim.displayRect, null, fbackgroundColor);
                        sb.append("\tctx.restore();\r\n");
                        sb.append("}\r\n\r\n");

                        sb.append("var frame = -1;\r\n");
                        sb.append("var time = 0;\r\n");
                        sb.append("var frames = [];\r\n");
                        for (int i : fframes) {
                            sb.append("frames.push(").append(i).append(");\r\n");
                        }
                        sb.append("\r\n");
                        RGB backgroundColor1 = new RGB(255, 255, 255);
                        if (!settings.transparentBackground && setBgColorTag != null) {
                            backgroundColor1 = setBgColorTag.backgroundColor;
                        }

                        sb.append("var backgroundColor = \"").append(backgroundColor1.toHexRGB()).append("\";\r\n");
                        sb.append("var originalWidth = ").append(width).append(";\r\n");
                        sb.append("var originalHeight= ").append(height).append(";\r\n");
                        sb.append("function nextFrame(ctx,ctrans){\r\n");
                        sb.append("\tvar oldframe = frame;\r\n");
                        sb.append("\tframe = (frame+1)%frames.length;\r\n");
                        sb.append("\tif(frame==oldframe){time++;}else{time=0;};\r\n");
                        sb.append("\tdrawFrame();\r\n");
                        sb.append("}\r\n\r\n");

                        sb.append("function drawFrame(){\r\n");
                        sb.append("\tctx.fillStyle = backgroundColor;\r\n");
                        sb.append("\tctx.fillRect(0,0,canvas.width,canvas.height);\r\n");
                        sb.append("\tctx.save();\r\n");
                        sb.append("\tctx.transform(canvas.width/originalWidth,0,0,canvas.height/originalHeight,0,0);\r\n");
                        sb.append("\t").append(currentName).append("(ctx,ctrans,frames[frame],0,time);\r\n");
                        sb.append("\tctx.restore();\r\n");
                        sb.append("}\r\n\r\n");
                        if (ftim.swf.frameRate > 0) {
                            sb.append("window.setInterval(function(){nextFrame(ctx,ctrans);},").append((int) (1000.0 / ftim.swf.frameRate)).append(");\r\n");
                        }
                        sb.append("nextFrame(ctx,ctrans);\r\n");
                        fos.write(Utf8Helper.getBytes(sb.toString()));
                    }

                    boolean packed = false;
                    if (Configuration.packJavaScripts.get()) {
                        try {
                            JPacker.main(new String[]{"-q", "-b", "62", "-o", fmin.getAbsolutePath(), f.getAbsolutePath()});
                            f.delete();
                            packed = true;
                        } catch (Exception | Error e) { // Something wrong in the packer
                            logger.log(Level.WARNING, "JPacker: Cannot minimize script");
                            f.renameTo(fmin);
                        }
                    } else {
                        f.renameTo(fmin);
                    }

                    File fh = new File(foutdir + File.separator + "frames.html");
                    try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(fh)); FileInputStream fis = new FileInputStream(fmin)) {
                        fos.write(Utf8Helper.getBytes(CanvasShapeExporter.getHtmlPrefix(width, height)));
                        fos.write(Utf8Helper.getBytes(CanvasShapeExporter.getJsPrefix()));
                        byte[] buf = new byte[1000];
                        int cnt;
                        while ((cnt = fis.read(buf)) > 0) {
                            fos.write(buf, 0, cnt);
                        }
                        if (packed) {
                            fos.write(Utf8Helper.getBytes(";"));
                        }
                        fos.write(Utf8Helper.getBytes(CanvasShapeExporter.getJsSuffix()));
                        fos.write(Utf8Helper.getBytes(CanvasShapeExporter.getHtmlSuffix()));
                    }

                    fmin.delete();

                    ret.add(f);
                }, handler).run();
            }

            if (evl != null) {
                Tag parentTag = tim.getParentTag();
                evl.handleExportedEvent("canvas", 1, 1, parentTag == null ? "" : parentTag.getName());
            }
            return ret;
        }

        if (settings.mode == FrameExportMode.SWF) {
            Color fBackgroundColor = backgroundColor;
            if (exportAll) {
                for (File foutdir : foutdirs) {
                    new RetryTask(() -> {
                        File f = new File(foutdir + File.separator + "frames.swf");

                        try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(f))) {
                            try {
                                new PreviewExporter().exportSwf(fos, swf.getCharacter(containerId), fBackgroundColor, 0, false);
                            } catch (ActionParseException ex) {
                                Logger.getLogger(FrameExporter.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }

                        ret.add(f);
                    }, handler).run();
                }
            } else {
                for (Integer frame : fframes) {
                    for (File foutdir : foutdirs) {
                        new RetryTask(() -> {
                            File f = new File(foutdir + File.separator + (frame + 1) + ".swf");
                            Frame fn = (Frame) tim.getFrame(frame);

                            try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(f))) {
                                try {
                                    new PreviewExporter().exportSwf(fos, fn, fBackgroundColor, 0, false);
                                } catch (ActionParseException ex) {
                                    Logger.getLogger(FrameExporter.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }

                            ret.add(f);
                        }, handler).run();
                    }
                }
            }
        }

        final Color fbackgroundColor = backgroundColor;
        final boolean usesTransparency = settings.mode == FrameExportMode.PNG || settings.mode == FrameExportMode.GIF;
        final MyFrameIterator frameImages = new MyFrameIterator(tim, fframes, evl, usesTransparency, backgroundColor, settings, subFramesLength);

        switch (settings.mode) {
            case GIF:
                for (File foutdir : foutdirs) {
                    frameImages.reset();
                    new RetryTask(() -> {
                        File f = new File(foutdir + File.separator + "frames.gif");
                        makeGIF(frameImages, swf.frameRate, f, evl);
                        ret.add(f);
                    }, handler).run();
                }
                break;
            case BMP:
                for (File foutdir : foutdirs) {
                    frameImages.reset();
                    for (int i = 0; frameImages.hasNext(); i++) {
                        final int fi = i;
                        new RetryTask(() -> {
                            int fileNum = subFramesLength > 1 ? fi + 1 : (fframes.get(fi) + 1);

                            File f = new File(foutdir + File.separator + fileNum + ".bmp");
                            BufferedImage img = frameImages.next();
                            if (img != null) {
                                BMPFile.saveBitmap(img, f);
                            }
                            ret.add(f);
                        }, handler).run();
                    }
                }
                break;
            case PNG:
                for (File foutdir : foutdirs) {
                    frameImages.reset();
                    for (int i = 0; frameImages.hasNext(); i++) {
                        final int fi = i;
                        new RetryTask(() -> {
                            int fileNum = subFramesLength > 1 ? fi + 1 : (fframes.get(fi) + 1);
                            File file = new File(foutdir + File.separator + fileNum + ".png");
                            BufferedImage img = frameImages.next();
                            if (img != null) {
                                ImageHelper.write(img, ImageFormat.PNG, file);
                                ret.add(file);
                            }
                        }, handler).run();
                    }
                }
                break;
            case PDF:
                if (frameImages.hasNext()) {
                    for (File foutdir : foutdirs) {
                        new RetryTask(() -> {
                            File f = new File(foutdir + File.separator + "frames.pdf");
                            PDFJob job = new PDFJob(new BufferedOutputStream(new FileOutputStream(f)));
                            PageFormat pf = new PageFormat();
                            pf.setOrientation(PageFormat.PORTRAIT);
                            Paper p = new Paper();
                            /*BufferedImage img = frameImages.next();
                            p.setSize(img.getWidth() + 10, img.getHeight() + 10);*/

                            int pos = 0;
                            RECT rect = tim.displayRect;

                            double w = (rect.getWidth() * settings.zoom / SWF.unitDivisor);
                            double h = (rect.getHeight() * settings.zoom / SWF.unitDivisor);
                            p.setSize(w, h);
                            pf.setPaper(p);
                            double zoom = settings.zoom;
                            Matrix m = new Matrix();
                            m.translate(-rect.Xmin * zoom, -rect.Ymin * zoom);
                            m.scale(zoom);
                            Matrix transformation = m;
                            Map<Integer, Font> existingFonts = new HashMap<>();

                            int maxPos = fframes.size();
                            if (subFramesLength > 1) {
                                maxPos = subFramesLength;
                            }

                            while (pos < maxPos) {

                                if (evl != null) {
                                    Tag parentTag = tim.getParentTag();
                                    String tagName = parentTag == null ? "" : parentTag.getName();
                                    evl.handleExportingEvent("frame", pos + 1, maxPos, tagName);
                                }
                                int fframe = subFramesLength > 1 ? fframes.get(0) : fframes.get(pos);
                                final Graphics2D g = (Graphics2D) job.getGraphics(pf);
                                //g.drawImage(img, 5, 5, img.getWidth(), img.getHeight(), null);

                                SerializableImage image = new SerializableImage((int) w + 1, (int) h + 1, SerializableImage.TYPE_INT_ARGB_PRE) {

                                    private Graphics2D compositeGraphics;

                                    @Override
                                    public Graphics getGraphics() {
                                        if (compositeGraphics != null) {
                                            return compositeGraphics;
                                        }
                                        final Graphics2D parentGraphics = (Graphics2D) super.getGraphics();
                                        compositeGraphics = new DualPdfGraphics2D(parentGraphics, (PDFGraphics) g, existingFonts);
                                        return compositeGraphics;
                                    }

                                    @Override
                                    public void fillTransparent() {

                                    }

                                };
                                //if (backGroundColor == null) {
                                //    image.fillTransparent();
                                int imgWidth = (int) (rect.getWidth() * zoom / SWF.unitDivisor) + 1;
                                int imgHeight = (int) (rect.getHeight() * zoom / SWF.unitDivisor) + 1;
                                if (!usesTransparency && fbackgroundColor != null) {
                                    g.setComposite(AlphaComposite.Src);
                                    g.setColor(fbackgroundColor);
                                    g.fill(new Rectangle(imgWidth, imgHeight));
                                }

                                RenderContext renderContext = new RenderContext();
                                renderContext.cursorPosition = new Point(-1, -1);
                                renderContext.mouseButton = 0;
                                renderContext.stateUnderCursor = new ArrayList<>();

                                try {
                                    tim.toImage(fframe, subFramesLength > 1 ? pos : 0, renderContext, image, image, false, m, new Matrix(), m, null, zoom, true, new ExportRectangle(rect), new ExportRectangle(rect), m, true, Timeline.DRAW_MODE_ALL, 0, true, new ArrayList<>());
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                                g.dispose();

                                if (CancellableWorker.isInterrupted()) {
                                    return;
                                }
                                pos++;
                            }

                            job.end();
                            ret.add(f);
                        }, handler).run();
                    }
                }
                break;
            case AVI:
                for (File foutdir : foutdirs) {
                    frameImages.reset();
                    new RetryTask(() -> {
                        File f = new File(foutdir + File.separator + "frames.avi");
                        makeAVI(frameImages, swf.frameRate, f, evl);
                        ret.add(f);
                    }, handler).run();
                }
                break;
        }

        return ret;
    }

    private static void drawText(SWF swf, float x, float y, Matrix trans, int textColor, Map<Integer, Font> existingFonts, FontTag font, String text, int textHeight, Graphics g) {
        int fontId = swf.getCharacterId(font);
        PDFGraphics g2 = (PDFGraphics) g;
        if (existingFonts.containsKey(fontId)) {
            g2.setExistingTtfFont(existingFonts.get(fontId).deriveFont((float) textHeight));
        } else {
            if (font.getCharacterCount() < 1) {
                String fontName = font.getFontName();
                File fontFile = FontTag.fontNameToFile(fontName);
                if (fontFile == null) {
                    fontFile = FontTag.fontNameToFile("Times New Roman");
                }
                if (fontFile == null) {
                    fontFile = FontTag.fontNameToFile("Arial");
                }
                if (fontFile == null) {
                    throw new RuntimeException("Font " + fontName + " not found in your system");
                }
                Font f = new Font("/MYFONT" + fontId, font.getFontStyle(), textHeight);
                existingFonts.put(fontId, f);
                try {
                    g2.setTtfFont(f, fontFile);
                } catch (IOException ex) {
                    Logger.getLogger(FrameExporter.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                FontExporter fe = new FontExporter();
                File tempFile = null;
                try {
                    tempFile = File.createTempFile("ffdec_font_export_", ".ttf");
                    fe.exportFont(font, FontExportMode.TTF, tempFile);
                    Font f = new Font("/MYFONT" + fontId, font.getFontStyle(), textHeight);
                    existingFonts.put(fontId, f);
                    g2.setTtfFont(f, tempFile);
                } catch (IOException ex) {
                    Logger.getLogger(FrameExporter.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (tempFile != null && tempFile.exists()) {
                    tempFile.delete();
                }
            }
        }

        g2.setTransform(trans.toTransform());
        Color textColor2 = new Color(textColor, true);
        g2.setColor(textColor2);
        g2.drawTransparentString(text, (float) x, (float) y);
    }

    private static String jsArrColor(RGB rgb) {
        return "[" + rgb.red + "," + rgb.green + "," + rgb.blue + "," + ((rgb instanceof RGBA) ? ((RGBA) rgb).getAlphaFloat() : 1) + "]";
    }

    public static void makeAVI(Iterator<BufferedImage> images, float frameRate, File file, EventListener evl) throws IOException {
        if (!images.hasNext()) {
            return;
        }

        AVIWriter out = new AVIWriter(file);
        BufferedImage img0 = images.next();
        out.addVideoTrack(VideoFormatKeys.ENCODING_AVI_PNG, 1, (int) frameRate, img0.getWidth(), img0.getHeight(), 0, 0);
        try {
            out.write(0, img0, 1);
            while (images.hasNext()) {
                BufferedImage img = images.next();
                if (img == null) {
                    break;
                }
                out.write(0, img, 1);
            }
        } finally {
            out.close();
        }
    }

    public static void makeGIF(Iterator<BufferedImage> images, float frameRate, File file, EventListener evl) throws IOException {
        if (!images.hasNext()) {
            return;
        }

        AnimatedGifEncoder encoder = new AnimatedGifEncoder();
        encoder.setRepeat(0); // repeat forever
        encoder.start(file.getAbsolutePath());
        encoder.setDelay((int) (1000.0 / frameRate));
        while (images.hasNext()) {
            BufferedImage img = images.next();
            if (img == null) {
                break;
            }
            encoder.addFrame(img);
        }

        encoder.finish();
    }

    public static void makeGIFOld(Iterator<BufferedImage> images, float frameRate, File file, EventListener evl) throws IOException {
        if (!images.hasNext()) {
            return;
        }

        try (ImageOutputStream output = new FileImageOutputStream(file)) {
            BufferedImage img0 = images.next();
            GifSequenceWriter writer = new GifSequenceWriter(output, img0.getType(), (int) (1000.0 / frameRate), true);
            writer.writeToSequence(img0);

            while (images.hasNext()) {
                BufferedImage img = images.next();
                if (img == null) {
                    break;
                }
                writer.writeToSequence(img);
            }

            writer.close();
        }
    }

    public static void framesToHtmlCanvas(StringBuilder result, double unitDivisor, Timeline timeline, List<Integer> frames, int time, DepthState stateUnderCursor, int mouseButton, RECT displayRect, ColorTransform colorTransform, Color backGroundColor) {
        if (frames == null) {
            frames = new ArrayList<>();
            for (int i = 0; i < timeline.getFrameCount(); i++) {
                frames.add(i);
            }
        }

        result.append("\tvar clips = [];\r\n");
        result.append("\tvar frame_cnt = ").append(timeline.getFrameCount()).append(";\r\n");
        result.append("\tframe = frame % frame_cnt;\r\n");
        result.append("\tswitch(frame){\r\n");
        int maxDepth = timeline.getMaxDepth();
        Stack<Integer> clipDepths = new Stack<>();
        for (int frame : frames) {

            if (CancellableWorker.isInterrupted()) {
                break;
            }
            result.append("\t\tcase ").append(frame).append(":\r\n");
            Frame frameObj = timeline.getFrame(frame);
            for (int i = 1; i <= maxDepth + 1; i++) {
                while (!clipDepths.isEmpty() && clipDepths.peek() <= i) {
                    clipDepths.pop();
                    result.append("\t\t\tvar o = clips.pop();\r\n");
                    result.append("\t\t\tctx.globalCompositeOperation = \"destination-in\";\r\n");
                    result.append("\t\t\tctx.setTransform(1,0,0,1,0,0);\r\n");
                    result.append("\t\t\tctx.drawImage(o.clipCanvas,0,0);\r\n");
                    result.append("\t\t\tvar ms=o.ctx._matrix;\r\n");
                    result.append("\t\t\to.ctx.setTransform(1,0,0,1,0,0);\r\n");
                    result.append("\t\t\to.ctx.globalCompositeOperation = \"source-over\";\r\n");
                    result.append("\t\t\to.ctx.drawImage(canvas,0,0);\r\n");
                    result.append("\t\t\to.ctx.applyTransforms(ms);\r\n");
                    result.append("\t\t\tctx = o.ctx;\r\n");
                    result.append("\t\t\tcanvas = o.canvas;\r\n");
                }
                if (!frameObj.layers.containsKey(i)) {
                    continue;
                }
                DepthState layer = frameObj.layers.get(i);
                if (!layer.isVisible) {
                    continue;
                }

                CharacterTag character = layer.getCharacter();
                if (character == null) {
                    continue;
                }

                Matrix placeMatrix = new Matrix(layer.matrix);
                placeMatrix.scaleX /= unitDivisor;
                placeMatrix.scaleY /= unitDivisor;
                placeMatrix.rotateSkew0 /= unitDivisor;
                placeMatrix.rotateSkew1 /= unitDivisor;
                placeMatrix.translateX /= unitDivisor;
                placeMatrix.translateY /= unitDivisor;

                int f = 0;
                String fstr = "0";
                if (character instanceof DefineSpriteTag) {
                    DefineSpriteTag sp = (DefineSpriteTag) character;
                    Timeline tim = sp.getTimeline();
                    if (tim.getFrameCount() > 0) {
                        f = layer.time % tim.getFrameCount();
                        fstr = "(" + f + "+time)%" + tim.getFrameCount();
                    }
                }

                if (layer.clipDepth != -1) {
                    clipDepths.push(layer.clipDepth);
                    result.append("\t\t\tclips.push({ctx:ctx,canvas:canvas});\r\n");
                    result.append("\t\t\tvar ccanvas = createCanvas(canvas.width,canvas.height);\r\n");
                    result.append("\t\t\tvar cctx = ccanvas.getContext(\"2d\");\r\n");
                    result.append("\t\t\tenhanceContext(cctx);\r\n");
                    result.append("\t\t\tcctx.applyTransforms(ctx._matrix);\r\n");
                    result.append("\t\t\tcanvas = ccanvas;\r\n");
                    result.append("\t\t\tctx = cctx;\r\n");
                }

                if (layer.filters != null && layer.filters.size() > 0) {
                    result.append("\t\t\tvar oldctx = ctx;\r\n");
                    result.append("\t\t\tvar fcanvas = createCanvas(canvas.width,canvas.height);");
                    result.append("\t\t\tvar fctx = fcanvas.getContext(\"2d\");\r\n");
                    result.append("\t\t\tenhanceContext(fctx);\r\n");
                    result.append("\t\t\tfctx.applyTransforms(ctx._matrix);\r\n");
                    result.append("\t\t\tctx = fctx;\r\n");
                }

                ColorTransform ctrans = layer.colorTransForm; // todo: colorTransform from parameter is not used? why?
                String ctrans_str = "ctrans";
                if (ctrans != null) {
                    ctrans_str = "ctrans.merge(new cxform("
                            + ctrans.getRedAdd() + "," + ctrans.getGreenAdd() + "," + ctrans.getBlueAdd() + "," + ctrans.getAlphaAdd() + ","
                            + ctrans.getRedMulti() + "," + ctrans.getGreenMulti() + "," + ctrans.getBlueMulti() + "," + ctrans.getAlphaMulti()
                            + "))";
                }
                result.append("\t\t\tplace(\"").append(SWF.getTypePrefix(character)).append(layer.getCharacter().getCharacterId()).append("\",canvas,ctx,[").append(placeMatrix.scaleX).append(",")
                        .append(placeMatrix.rotateSkew0).append(",")
                        .append(placeMatrix.rotateSkew1).append(",")
                        .append(placeMatrix.scaleY).append(",")
                        .append(placeMatrix.translateX).append(",")
                        .append(placeMatrix.translateY).append("],").append(ctrans_str).append(",").append("").append(layer.blendMode < 1 ? 1 : layer.blendMode).append(",").append(fstr).append(",").append(layer.ratio < 0 ? 0 : layer.ratio).append(",time").append(");\r\n");

                if (layer.filters != null && layer.filters.size() > 0) {
                    for (FILTER filter : layer.filters) {
                        if (filter instanceof COLORMATRIXFILTER) {
                            COLORMATRIXFILTER cmf = (COLORMATRIXFILTER) filter;
                            result.append("\t\t\tfcanvas = Filters.colorMatrix(fcanvas,fcanvas.getContext(\"2d\"),[");
                            for (int k = 0; k < cmf.matrix.length; k++) {
                                if (k > 0) {
                                    result.append(",");
                                }
                                result.append(cmf.matrix[k]);
                            }
                            result.append("]");
                            result.append(");\r\n");
                        }

                        if (filter instanceof CONVOLUTIONFILTER) {
                            CONVOLUTIONFILTER cf = (CONVOLUTIONFILTER) filter;
                            float[] matrix2 = new float[cf.matrixX * cf.matrixY];
                            for (int y = 0; y < cf.matrixY; y++) {
                                for (int x = 0; x < cf.matrixX; x++) {
                                    matrix2[y * cf.matrixX + x] = cf.matrix[y * cf.matrixX + x] / cf.divisor + cf.bias;
                                }
                            }
                            String mat = "[";
                            for (int k = 0; k < matrix2.length; k++) {
                                if (k > 0) {
                                    mat += ",";
                                }
                                mat += matrix2[k];
                            }
                            mat += "]";
                            result.append("\t\t\tfcanvas = Filters.convolution(fcanvas,fcanvas.getContext(\"2d\"),").append(mat).append(",false);\r\n");
                        }

                        if (filter instanceof GLOWFILTER) {
                            GLOWFILTER gf = (GLOWFILTER) filter;
                            result.append("\t\t\tfcanvas = Filters.glow(fcanvas,fcanvas.getContext(\"2d\"),").append(gf.blurX).append(",").append(gf.blurY).append(",").append(gf.strength).append(",").append(jsArrColor(gf.glowColor)).append(",").append(gf.innerGlow ? "true" : "false").append(",").append(gf.knockout ? "true" : "false").append(",").append(gf.passes).append(");\r\n");
                        }

                        if (filter instanceof DROPSHADOWFILTER) {
                            DROPSHADOWFILTER ds = (DROPSHADOWFILTER) filter;
                            result.append("\t\t\tfcanvas = Filters.dropShadow(fcanvas,fcanvas.getContext(\"2d\"),").append(ds.blurX).append(",").append(ds.blurY).append(",").append((int) (ds.angle * 180 / Math.PI)).append(",").append(ds.distance).append(",").append(jsArrColor(ds.dropShadowColor)).append(",").append(ds.innerShadow ? "true" : "false").append(",").append(ds.passes).append(",").append(ds.strength).append(",").append(ds.knockout ? "true" : "false").append(");\r\n");
                        }
                        if (filter instanceof BEVELFILTER) {
                            BEVELFILTER bv = (BEVELFILTER) filter;
                            String type = "Filters.INNER";
                            if (bv.onTop && !bv.innerShadow) {
                                type = "Filters.FULL";
                            } else if (!bv.innerShadow) {
                                type = "Filters.OUTER";
                            }
                            result.append("\t\t\tfcanvas = Filters.bevel(fcanvas,fcanvas.getContext(\"2d\"),").append(bv.blurX).append(",").append(bv.blurY).append(",").append(bv.strength).append(",").append(type).append(",").append(jsArrColor(bv.highlightColor)).append(",").append(jsArrColor(bv.shadowColor)).append(",").append((int) (bv.angle * 180 / Math.PI)).append(",").append(bv.distance).append(",").append(bv.knockout ? "true" : "false").append(",").append(bv.passes).append(");\r\n");
                        }

                        if (filter instanceof GRADIENTBEVELFILTER) {
                            GRADIENTBEVELFILTER gbf = (GRADIENTBEVELFILTER) filter;
                            String colArr = "[";
                            String ratArr = "[";
                            for (int k = 0; k < gbf.gradientColors.length; k++) {
                                if (k > 0) {
                                    colArr += ",";
                                    ratArr += ",";
                                }
                                colArr += jsArrColor(gbf.gradientColors[k]);
                                ratArr += gbf.gradientRatio[k] / 255f;
                            }
                            colArr += "]";
                            ratArr += "]";
                            String type = "Filters.INNER";
                            if (gbf.onTop && !gbf.innerShadow) {
                                type = "Filters.FULL";
                            } else if (!gbf.innerShadow) {
                                type = "Filters.OUTER";
                            }

                            result.append("\t\t\tfcanvas = Filters.gradientBevel(fcanvas,fcanvas.getContext(\"2d\"),").append(colArr).append(",").append(ratArr).append(",").append(gbf.blurX).append(",").append(gbf.blurY).append(",").append(gbf.strength).append(",").append(type).append(",").append((int) (gbf.angle * 180 / Math.PI)).append(",").append(gbf.distance).append(",").append(gbf.knockout ? "true" : "false").append(",").append(gbf.passes).append(");\r\n");
                        }

                        if (filter instanceof GRADIENTGLOWFILTER) {
                            GRADIENTGLOWFILTER ggf = (GRADIENTGLOWFILTER) filter;
                            String colArr = "[";
                            String ratArr = "[";
                            for (int k = 0; k < ggf.gradientColors.length; k++) {
                                if (k > 0) {
                                    colArr += ",";
                                    ratArr += ",";
                                }
                                colArr += jsArrColor(ggf.gradientColors[k]);
                                ratArr += ggf.gradientRatio[k] / 255f;
                            }
                            colArr += "]";
                            ratArr += "]";
                            String type = "Filters.INNER";
                            if (ggf.onTop && !ggf.innerShadow) {
                                type = "Filters.FULL";
                            } else if (!ggf.innerShadow) {
                                type = "Filters.OUTER";
                            }

                            result.append("\t\t\tfcanvas = Filters.gradientGlow(fcanvas,fcanvas.getContext(\"2d\"),").append(ggf.blurX).append(",").append(ggf.blurY).append(",").append((int) (ggf.angle * 180 / Math.PI)).append(",").append(ggf.distance).append(",").append(colArr).append(",").append(ratArr).append(",").append(type).append(",").append(ggf.passes).append(",").append(ggf.strength).append(",").append(ggf.knockout ? "true" : "false").append(");\r\n");
                        }
                    }
                    result.append("\t\t\tctx = oldctx;\r\n");
                    result.append("\t\t\tvar ms=ctx._matrix;\r\n");
                    result.append("\t\t\tctx.setTransform(1,0,0,1,0,0);\r\n");
                    result.append("\t\t\tctx.drawImage(fcanvas,0,0);\r\n");
                    result.append("\t\t\tctx.applyTransforms(ms);\r\n");
                }

                if (layer.clipDepth != -1) {
                    result.append("\t\t\tclips[clips.length-1].clipCanvas = canvas;\r\n");
                    result.append("\t\t\tcanvas = createCanvas(canvas.width,canvas.height);\r\n");
                    result.append("\t\t\tvar nctx = canvas.getContext(\"2d\");\r\n");
                    result.append("\t\t\tenhanceContext(nctx);\r\n");
                    result.append("\t\t\tnctx.applyTransforms(ctx._matrix);\r\n");
                    result.append("\t\t\tctx = nctx;\r\n");
                }
            }
            result.append("\t\t\tbreak;\r\n");
        }
        result.append("\t}\r\n");
    }
}
