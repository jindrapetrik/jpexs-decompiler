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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class ScriptImporter {

    private static final Logger logger = Logger.getLogger(ScriptImporter.class.getName());

    public void importScripts(String scriptsFolder, Map<String, ASMSource> asms) {

        for (String key : asms.keySet()) {
            String fileName = Path.combine(scriptsFolder, key) + ".as";
            if (new File(fileName).exists()) {
                ASMSource src = asms.get(key);
                String as = Helper.readTextFile(fileName);

                com.jpexs.decompiler.flash.action.parser.script.ActionScriptParser par = new com.jpexs.decompiler.flash.action.parser.script.ActionScriptParser(src.getSwf().version);
                try {
                    src.setActions(par.actionsFromString(as));
                } catch (ActionParseException ex) {
                    logger.log(Level.SEVERE, "%error% on line %line%, file: %file%".replace("%error%", ex.text).replace("%line%", Long.toString(ex.line)).replace("%file%", fileName), ex);
                } catch (CompilationException ex) {
                    logger.log(Level.SEVERE, "%error% on line %line%, file: %file%".replace("%error%", ex.text).replace("%line%", Long.toString(ex.line)).replace("%file%", fileName), ex);
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, "error during script import, file: %file%".replace("%file%", fileName), ex);
                }

                src.setModified();
            }
        }
    }
}
