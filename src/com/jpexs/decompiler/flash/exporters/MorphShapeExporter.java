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
import com.jpexs.decompiler.flash.SWF;
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
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class MorphShapeExporter {

    //TODO: implement morphshape export. How to handle 65536 frames?
    public List<File> exportMorphShapes(AbortRetryIgnoreHandler handler, final String outdir, List<Tag> tags, final MorphShapeExportSettings settings) throws IOException {
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

        for (final Tag t : tags) {
            if (t instanceof MorphShapeTag) {
                int characterID = 0;
                if (t instanceof CharacterTag) {
                    characterID = ((CharacterTag) t).getCharacterId();
                }
                String ext = settings.mode == MorphShapeExportMode.CANVAS ? "html" : "svg";

                final File file = new File(outdir + File.separator + characterID + "." + ext);
                final int fcharacterID = characterID;
                new RetryTask(new RunnableIOEx() {
                    @Override
                    public void run() throws IOException {
                        MorphShapeTag mst = (MorphShapeTag) t;
                        switch (settings.mode) {
                            case SVG:
                                try (FileOutputStream fos = new FileOutputStream(file)) {
                                    ExportRectangle rect = new ExportRectangle(mst.getRect());
                                    SVGExporter exporter = new SVGExporter(rect);
                                    mst.toSVG(exporter, -2, new CXFORMWITHALPHA(), 0);
                                    fos.write(Utf8Helper.getBytes(exporter.getSVG()));
                                }
                                break;
                            case CANVAS:
                                try (FileOutputStream fos = new FileOutputStream(file)) {
                                    int deltaX = -Math.min(mst.getStartBounds().Xmin, mst.getEndBounds().Xmin);
                                    int deltaY = -Math.min(mst.getStartBounds().Ymin, mst.getEndBounds().Ymin);
                                    CanvasMorphShapeExporter cse = new CanvasMorphShapeExporter(((Tag) mst).getSwf(), mst.getShapeAtRatio(0), mst.getShapeAtRatio(DefineMorphShapeTag.MAX_RATIO), new CXFORMWITHALPHA(), SWF.unitDivisor, deltaX, deltaY);
                                    cse.export();
                                    Set<Integer> needed=new HashSet<>();
                                    SWF.getNeededCharacters(((Tag)mst).getSwf(), ((CharacterTag)mst).getCharacterId(), needed);
                                    ByteArrayOutputStream baos=new ByteArrayOutputStream();
                                    SWF.writeLibrary(((Tag)mst).getSwf(), needed, baos);
                                    fos.write(Utf8Helper.getBytes(cse.getHtml(new String(baos.toByteArray(),"UTF-8"))));
                                }
                                break;
                        }

                    }
                }, handler).run();
                ret.add(file);
            }
        }
        
        if(settings.mode == MorphShapeExportMode.CANVAS){
            File fcanvas = new File(foutdir + File.separator + "canvas.js");
            Helper.saveStream(SWF.class.getClassLoader().getResourceAsStream("com/jpexs/helpers/resource/canvas.js"), fcanvas);
            ret.add(fcanvas);
        }
        return ret;
    }
}
