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
package com.jpexs.decompiler.flash.importers;

import com.jpexs.decompiler.flash.ValueTooLargeException;
import com.jpexs.decompiler.flash.action.ConstantPoolTooBigException;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.ASMParser;
import com.jpexs.decompiler.flash.action.parser.script.ActionScript2Parser;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.Path;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ActionScript 1/2 scripts importer.
 *
 * @author JPEXS
 */
public class AS2ScriptImporter {

    private static final Logger logger = Logger.getLogger(AS2ScriptImporter.class.getName());


    /**
     * Constructor.
     */
    public AS2ScriptImporter() {

    }

    /**
     * Imports scripts from given folder.
     * @param scriptsFolder Folder with scripts
     * @param asms Map of ASMSource objects
     * @return Number of imported scripts
     * @throws InterruptedException On I/O error
     */
    public int importScripts(String scriptsFolder, Map<String, ASMSource> asms) throws InterruptedException {
        return importScripts(scriptsFolder, asms, null);
    }

    /**
     * Imports scripts from given folder.
     * @param scriptsFolder Folder with scripts
     * @param asms Map of ASMSource objects
     * @param listener Progress listener
     * @return Number of imported scripts
     * @throws InterruptedException On I/O error
     */
    public int importScripts(String scriptsFolder, Map<String, ASMSource> asms, ScriptImporterProgressListener listener) throws InterruptedException {
        if (!scriptsFolder.endsWith(File.separator)) {
            scriptsFolder += File.separator;
        }

        Map<String, List<String>> existingNamesMap = new HashMap<>();

        int importCount = 0;
        for (String key : asms.keySet()) {
            if (CancellableWorker.isInterrupted()) {
                throw new InterruptedException();
            }
            ASMSource asm = asms.get(key);
            String currentOutDir = scriptsFolder + key + File.separator;
            currentOutDir = new File(currentOutDir).getParentFile().toString() + File.separator;

            List<String> existingNames = existingNamesMap.get(currentOutDir);
            if (existingNames == null) {
                existingNames = new ArrayList<>();
                existingNamesMap.put(currentOutDir, existingNames);
            }

            String name = Helper.makeFileName(asm.getExportFileName());
            int i = 1;
            String baseName = name;
            while (existingNames.contains(name)) {
                i++;
                name = baseName + "_" + i;
            }
            existingNames.add(name);

            String fileName = Path.combine(currentOutDir, name) + ".as";
            if (new File(fileName).exists()) {
                asm.getSwf().informListeners("importing_as", fileName);
                String txt = Helper.readTextFile(fileName);

                ActionScript2Parser par = new ActionScript2Parser(asm.getSwf(), asm);
                boolean errored = false;
                try {
                    asm.setActions(par.actionsFromString(txt, asm.getSwf().getCharset()));
                } catch (ValueTooLargeException ex) {
                    logger.log(Level.SEVERE, "Script or some of its functions are too large, file: {0}", fileName);
                    errored = true;
                } catch (ActionParseException ex) {
                    logger.log(Level.SEVERE, "%error% on line %line%, file: %file%".replace("%error%", ex.text).replace("%line%", Long.toString(ex.line)).replace("%file%", fileName), ex);
                    errored = true;
                } catch (CompilationException ex) {
                    logger.log(Level.SEVERE, "%error% on line %line%, file: %file%".replace("%error%", ex.text).replace("%line%", Long.toString(ex.line)).replace("%file%", fileName), ex);
                    errored = true;
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, "error during script import, file: %file%".replace("%file%", fileName), ex);
                    errored = true;
                } catch (InterruptedException ex) {
                    return importCount;
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "error during script import, file: %file%".replace("%file%", fileName), ex);
                    errored = true;
                }

                if (!errored) {
                    asm.setModified();
                    importCount++;
                    if (listener != null) {
                        listener.scriptImported();
                    }
                } else {
                    if (listener != null) {
                        listener.scriptImportError();
                    }
                }
            }

            fileName = Path.combine(currentOutDir, name) + ".pcode";
            if (new File(fileName).exists()) {
                asm.getSwf().informListeners("importing_as", fileName);
                String txt = Helper.readTextFile(fileName);

                boolean errored = false;
                try {
                    asm.setActions(ASMParser.parse(0, true, txt, asm.getSwf().version, false, asm.getSwf().getCharset()));
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, "error during script import, file: %file%".replace("%file%", fileName), ex);
                    errored = true;
                } catch (ActionParseException ex) {
                    logger.log(Level.SEVERE, "%error% on line %line%, file: %file%".replace("%error%", ex.text).replace("%line%", Long.toString(ex.line)).replace("%file%", fileName), ex);
                    errored = true;
                }

                if (!errored) {
                    asm.setModified();
                    importCount++;
                    if (listener != null) {
                        listener.scriptImported();
                    }
                } else {
                    if (listener != null) {
                        listener.scriptImportError();
                    }
                }
            }

            fileName = Path.combine(currentOutDir, name) + ".hex";
            if (new File(fileName).exists()) {
                asm.getSwf().informListeners("importing_as", fileName);
                String txt = Helper.readTextFile(fileName);

                asm.setActionBytes(Helper.getBytesFromHexaText(txt));
                asm.setModified();
                importCount++;
                if (listener != null) {
                    listener.scriptImported();
                }
            }

            fileName = Path.combine(currentOutDir, name) + ".txt";
            if (new File(fileName).exists()) {
                asm.getSwf().informListeners("importing_as", fileName);
                String txt = Helper.readTextFile(fileName);

                boolean errored = false;
                try {
                    asm.setConstantPools(Helper.getConstantPoolsFromText(txt));
                } catch (ConstantPoolTooBigException ex) {
                    logger.log(Level.SEVERE, null, ex);
                    errored = true;
                }

                if (!errored) {
                    asm.setModified();
                    importCount++;
                    if (listener != null) {
                        listener.scriptImported();
                    }
                } else {
                    if (listener != null) {
                        listener.scriptImportError();
                    }
                }
            }
        }

        return importCount;
    }
}
