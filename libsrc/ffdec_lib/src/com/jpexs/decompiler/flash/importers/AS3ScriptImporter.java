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
package com.jpexs.decompiler.flash.importers;

import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.exporters.settings.ScriptExportSettings;
import com.jpexs.helpers.Helper;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class AS3ScriptImporter {

    private static final Logger logger = Logger.getLogger(AS3ScriptImporter.class.getName());

    public int importScripts(As3ScriptReplacerInterface scriptReplacer, String scriptsFolder, List<ScriptPack> packs) {
        if (!scriptsFolder.endsWith(File.separator)) {
            scriptsFolder += File.separator;
        }

        int importCount = 0;
        for (ScriptPack pack : packs) {
            try {
                File file = pack.getExportFile(scriptsFolder, new ScriptExportSettings(ScriptExportMode.AS, false));
                if (file.exists()) {
                    String fileName = file.getAbsolutePath();
                    String txt = Helper.readTextFile(fileName);

                    try {
                        pack.abc.replaceScriptPack(scriptReplacer, pack, txt);
                    } catch (As3ScriptReplaceException asre) {
                        for (As3ScriptReplaceExceptionItem item : asre.getExceptionItems()) {
                            logger.log(Level.SEVERE, "%error% on line %line%, column %col%, file: %file%".replace("%error%", item.getMessage()).replace("%line%", Long.toString(item.getLine())).replace("%file%", fileName).replace("%col%", "" + item.getCol()));
                        }
                    } catch (InterruptedException ex) {
                        logger.log(Level.SEVERE, "error during script import, file: %file%".replace("%file%", fileName), ex);
                    }

                    importCount++;
                }
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }

        return importCount;
    }
}
