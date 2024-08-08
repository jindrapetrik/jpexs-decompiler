/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.importers;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.exporters.settings.ScriptExportSettings;
import com.jpexs.decompiler.flash.treeitems.Openable;
import com.jpexs.helpers.Helper;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ActionScript 3 scripts importer.
 *
 * @author JPEXS
 */
public class AS3ScriptImporter {

    private static final Logger logger = Logger.getLogger(AS3ScriptImporter.class.getName());

    /**
     * Constructor.
     */
    public AS3ScriptImporter() {

    }

    /**
     * Imports scripts from a folder.
     * @param scriptReplacer Replacer for the scripts
     * @param scriptsFolder Folder with scripts
     * @param packs List of script packs
     * @param dependencies List of dependencies
     * @return Number of imported scripts
     * @throws InterruptedException On interrupt
     */
    public int importScripts(As3ScriptReplacerInterface scriptReplacer, String scriptsFolder, List<ScriptPack> packs, List<SWF> dependencies) throws InterruptedException {
        return importScripts(scriptReplacer, scriptsFolder, packs, null, dependencies);
    }

    /**
     * Imports scripts from a folder.
     * @param scriptReplacer Replacer for the scripts
     * @param scriptsFolder Folder with scripts
     * @param packs List of script packs
     * @param listener Listener for progress
     * @param dependencies List of dependencies
     * @return Number of imported scripts
     * @throws InterruptedException On interrupt
     */
    public int importScripts(As3ScriptReplacerInterface scriptReplacer, String scriptsFolder, List<ScriptPack> packs, ScriptImporterProgressListener listener, List<SWF> dependencies) throws InterruptedException {
        if (!scriptsFolder.endsWith(File.separator)) {
            scriptsFolder += File.separator;
        }

        int importCount = 0;
        for (ScriptPack pack : packs) {
            if (Thread.currentThread().isInterrupted()) {
                return importCount;
            }
            if (!pack.isSimple) {
                continue;
            }
            try {
                File file = pack.getExportFile(scriptsFolder, new ScriptExportSettings(ScriptExportMode.AS, false, false, false, false, true));
                if (file.exists()) {
                    Openable openable = pack.getOpenable();
                    SWF swf = (openable instanceof SWF) ? (SWF) openable : ((ABC) openable).getSwf();
                    swf.informListeners("importing_as", file.getAbsolutePath());
                    String fileName = file.getAbsolutePath();
                    String txt = Helper.readTextFile(fileName);

                    try {
                        pack.abc.replaceScriptPack(scriptReplacer, pack, txt, dependencies);
                    } catch (As3ScriptReplaceException asre) {
                        for (As3ScriptReplaceExceptionItem item : asre.getExceptionItems()) {
                            logger.log(Level.SEVERE, "%error% on line %line%, column %col%, file: %file%".replace("%error%", item.getMessage()).replace("%line%", Long.toString(item.getLine())).replace("%file%", fileName).replace("%col%", "" + item.getCol()));
                        }
                        if (listener != null) {
                            listener.scriptImportError();
                        }
                    } catch (InterruptedException ex) {
                        return importCount;
                    }

                    importCount++;
                    if (listener != null) {
                        listener.scriptImported();
                    }
                }
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }

        return importCount;
    }
}
