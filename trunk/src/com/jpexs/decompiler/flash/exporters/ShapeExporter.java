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
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.commonshape.SVGExporterContext;
import com.jpexs.decompiler.flash.exporters.modes.ShapeExportMode;
import com.jpexs.decompiler.flash.exporters.settings.ShapeExportSettings;
import com.jpexs.decompiler.flash.exporters.shape.CanvasShapeExporter;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.CXFORMWITHALPHA;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.helpers.SerializableImage;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 *
 * @author JPEXS
 */
public class ShapeExporter {

    public List<File> exportShapes(AbortRetryIgnoreHandler handler, final String outdir, List<Tag> tags, final ShapeExportSettings settings) throws IOException {
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
            if (t instanceof ShapeTag) {
                int characterID = 0;
                if (t instanceof CharacterTag) {
                    characterID = ((CharacterTag) t).getCharacterId();
                }
                String ext = "svg";
                if (settings.mode == ShapeExportMode.PNG) {
                    ext = "png";
                }
                if (settings.mode == ShapeExportMode.CANVAS) {
                    ext = "html";
                }

                final File file = new File(outdir + File.separator + characterID + "." + ext);
                final int fcharacterID = characterID;
                new RetryTask(new RunnableIOEx() {
                    @Override
                    public void run() throws IOException {
                        ShapeTag st = (ShapeTag) t;
                        switch (settings.mode) {
                            case SVG:
                                try (FileOutputStream fos = new FileOutputStream(file)) {
                                    fos.write(Utf8Helper.getBytes(st.toSVG(new SVGExporterContext(outdir, "assets_" + fcharacterID), -2, new CXFORMWITHALPHA(), 0)));
                                }
                                break;
                            case PNG:
                                RECT rect = st.getRect();
                                int newWidth = (int) (rect.getWidth() / SWF.unitDivisor);
                                int newHeight = (int) (rect.getHeight() / SWF.unitDivisor);
                                SerializableImage img = new SerializableImage(newWidth, newHeight, SerializableImage.TYPE_INT_ARGB);
                                img.fillTransparent();
                                Matrix m = new Matrix();
                                m.translate(-rect.Xmin, -rect.Ymin);
                                st.toImage(0, 0, 0, null, 0, img, m, new CXFORMWITHALPHA());
                                ImageIO.write(img.getBufferedImage(), "PNG", new FileOutputStream(file));
                                break;
                            case CANVAS:
                                try (FileOutputStream fos = new FileOutputStream(file)) {
                                    SHAPE shp = st.getShapes();
                                    int deltaX = -shp.getBounds().Xmin;
                                    int deltaY = -shp.getBounds().Ymin;
                                    CanvasShapeExporter cse = new CanvasShapeExporter(null,SWF.unitDivisor,((Tag)st).getSwf(),shp, new CXFORMWITHALPHA(),deltaX,deltaY);
                                    cse.export();                                                                        
                                    fos.write(Utf8Helper.getBytes(cse.getHtml()));
                                }
                                break;
                        }

                    }
                }, handler).run();
                ret.add(file);
            }
        }
        return ret;
    }
}
