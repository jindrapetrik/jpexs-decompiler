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

import com.jpexs.decompiler.flash.action.ConstantPoolTooBigException;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.ASMParser;
import com.jpexs.decompiler.flash.action.parser.script.ActionScript2Parser;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.graph.CompilationException;
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
 *
 * @author JPEXS
 */
public class AS2ScriptImporter {

    private static final Logger logger = Logger.getLogger(AS2ScriptImporter.class.getName());

    public int importScripts(String scriptsFolder, Map<String, ASMSource> asms) {
        if (!scriptsFolder.endsWith(File.separator)) {
            scriptsFolder += File.separator;
        }

        Map<String, List<String>> existingNamesMap = new HashMap<>();

        int importCount = 0;
        for (String key : asms.keySet()) {
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
                String txt = Helper.readTextFile(fileName);

                ActionScript2Parser par = new ActionScript2Parser(asm.getSwf().version);
                try {
                    asm.setActions(par.actionsFromString(txt));
                } catch (ActionParseException ex) {
                    logger.log(Level.SEVERE, "%error% on line %line%, file: %file%".replace("%error%", ex.text).replace("%line%", Long.toString(ex.line)).replace("%file%", fileName), ex);
                } catch (CompilationException ex) {
                    logger.log(Level.SEVERE, "%error% on line %line%, file: %file%".replace("%error%", ex.text).replace("%line%", Long.toString(ex.line)).replace("%file%", fileName), ex);
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, "error during script import, file: %file%".replace("%file%", fileName), ex);
                }

                asm.setModified();
                importCount++;
            }

            fileName = Path.combine(currentOutDir, name) + ".pcode";
            if (new File(fileName).exists()) {
                String txt = Helper.readTextFile(fileName);

                try {
                    asm.setActions(ASMParser.parse(0, true, txt, asm.getSwf().version, false));
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, "error during script import, file: %file%".replace("%file%", fileName), ex);
                } catch (ActionParseException ex) {
                    logger.log(Level.SEVERE, "%error% on line %line%, file: %file%".replace("%error%", ex.text).replace("%line%", Long.toString(ex.line)).replace("%file%", fileName), ex);
                }

                asm.setModified();
                importCount++;
            }

            fileName = Path.combine(currentOutDir, name) + ".hex";
            if (new File(fileName).exists()) {
                String txt = Helper.readTextFile(fileName);

                asm.setActionBytes(Helper.getBytesFromHexaText(txt));
                asm.setModified();
                importCount++;
            }

            fileName = Path.combine(currentOutDir, name) + ".txt";
            if (new File(fileName).exists()) {
                String txt = Helper.readTextFile(fileName);

                try {
                    asm.setConstantPools(Helper.getConstantPoolsFromText(txt));
                } catch (ConstantPoolTooBigException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
                asm.setModified();
                importCount++;
            }
        }

        return importCount;
    }
}
