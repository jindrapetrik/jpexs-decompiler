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
import com.jpexs.decompiler.flash.exporters.commonshape.SVGExporterContext;
import com.jpexs.decompiler.flash.exporters.settings.MorphShapeExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.TextExportSettings;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.MorphShapeTag;
import com.jpexs.decompiler.flash.types.CXFORMWITHALPHA;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
                String ext = "svg";

                final File file = new File(outdir + File.separator + characterID + "." + ext);
                final int fcharacterID = characterID;
                new RetryTask(new RunnableIOEx() {
                    @Override
                    public void run() throws IOException {
                        MorphShapeTag mst = (MorphShapeTag) t;
                        switch (settings.mode) {
                            case SVG:
                                try (FileOutputStream fos = new FileOutputStream(file)) {
                                    fos.write(Utf8Helper.getBytes(mst.toSVG(new SVGExporterContext(outdir, "assets_" + fcharacterID), -2, new CXFORMWITHALPHA(), 0)));
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
