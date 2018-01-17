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
import com.jpexs.decompiler.flash.exporters.commonshape.SVGExporter;
import com.jpexs.decompiler.flash.exporters.modes.MorphShapeExportMode;
import com.jpexs.decompiler.flash.exporters.morphshape.CanvasMorphShapeExporter;
import com.jpexs.decompiler.flash.exporters.settings.MorphShapeExportSettings;
import com.jpexs.decompiler.flash.tags.DefineMorphShapeTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.MorphShapeTag;
import com.jpexs.decompiler.flash.types.CXFORMWITHALPHA;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.Path;
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
public class MorphShapeExporter {

    //TODO: implement morphshape export. How to handle 65536 frames?
    public List<File> exportMorphShapes(AbortRetryIgnoreHandler handler, final String outdir, ReadOnlyTagList tags, final MorphShapeExportSettings settings, EventListener evl) throws IOException, InterruptedException {
        List<File> ret = new ArrayList<>();
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

                final File file = new File(outdir + File.separator + characterID + settings.getFileExtension());
                new RetryTask(() -> {
                    MorphShapeTag mst = (MorphShapeTag) t;
                    switch (settings.mode) {
                        case SVG:
                            try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(file))) {
                                ExportRectangle rect = new ExportRectangle(mst.getRect());
                                rect.xMax *= settings.zoom;
                                rect.yMax *= settings.zoom;
                                rect.xMin *= settings.zoom;
                                rect.yMin *= settings.zoom;
                                SVGExporter exporter = new SVGExporter(rect, settings.zoom);
                                mst.toSVG(exporter, -2, new CXFORMWITHALPHA(), 0);
                                fos.write(Utf8Helper.getBytes(exporter.getSVG()));
                            }
                            break;
                        case CANVAS:
                            try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(file))) {
                                int deltaX = -Math.min(mst.getStartBounds().Xmin, mst.getEndBounds().Xmin);
                                int deltaY = -Math.min(mst.getStartBounds().Ymin, mst.getEndBounds().Ymin);
                                CanvasMorphShapeExporter cse = new CanvasMorphShapeExporter(((Tag) mst).getSwf(), mst.getShapeAtRatio(0), mst.getShapeAtRatio(DefineMorphShapeTag.MAX_RATIO), new CXFORMWITHALPHA(), SWF.unitDivisor, deltaX, deltaY);
                                cse.export();
                                Set<Integer> needed = new HashSet<>();
                                CharacterTag ct = ((CharacterTag) mst);
                                needed.add(ct.getCharacterId());
                                ct.getNeededCharactersDeep(needed);
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                SWF.writeLibrary(ct.getSwf(), needed, baos);
                                fos.write(Utf8Helper.getBytes(cse.getHtml(new String(baos.toByteArray(), Utf8Helper.charset), SWF.getTypePrefix(mst) + mst.getCharacterId(), mst.getRect())));
                            }
                            break;
                        case SWF:
                            try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(file))) {
                                try {
                                    new PreviewExporter().exportSwf(fos, mst, null, 0);
                                } catch (ActionParseException ex) {
                                    Logger.getLogger(MorphShapeExporter.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }

                            break;
                    }
                }, handler).run();
                ret.add(file);

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
}
