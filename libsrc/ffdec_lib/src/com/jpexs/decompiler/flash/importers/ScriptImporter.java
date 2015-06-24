/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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

import com.jpexs.decompiler.flash.action.parser.ActionParseException;
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
public class ScriptImporter {

    private static final Logger logger = Logger.getLogger(ScriptImporter.class.getName());

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
                String as = Helper.readTextFile(fileName);

                com.jpexs.decompiler.flash.action.parser.script.ActionScriptParser par = new com.jpexs.decompiler.flash.action.parser.script.ActionScriptParser(asm.getSwf().version);
                try {
                    asm.setActions(par.actionsFromString(as));
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
        }

        return importCount;
    }
}
