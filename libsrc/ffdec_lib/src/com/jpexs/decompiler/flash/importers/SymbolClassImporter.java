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

import com.jpexs.csv.CsvLexer;
import com.jpexs.csv.CsvRow;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.ExportAssetsTag;
import com.jpexs.decompiler.flash.tags.SymbolClassTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.helpers.Helper;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SymbolClass importer.
 *
 * @author JPEXS
 */
public class SymbolClassImporter {

    /**
     * Imports SymbolClasses from a file.
     *
     * @param importFile File to import from
     * @param swf SWF to import to
     */
    public void importSymbolClasses(File importFile, SWF swf) {
        String texts = Helper.readTextFile(importFile.getPath());

        CsvLexer lexer = new CsvLexer(texts);
        Map<Integer, String> nameMap = new HashMap<>();
        try {
            while (true) {
                CsvRow row = lexer.yylex();
                if (row == null) {
                    break;
                }
                if (row.values.size() >= 2) {
                    try {
                        nameMap.put(Integer.parseInt(row.values.get(0)), row.values.get(1));
                    } catch (NumberFormatException nfe) {
                        //ignore
                    }
                }
            }
        } catch (IOException ex) {
            //ignore
        }

        for (Tag tag : swf.getTags()) {
            if (tag instanceof ExportAssetsTag) {
                ExportAssetsTag eat = (ExportAssetsTag) tag;
                for (int i = 0; i < eat.tags.size(); i++) {
                    int id = eat.tags.get(i);
                    if (nameMap.containsKey(id)) {
                        eat.names.set(i, nameMap.get(id));
                        eat.setModified(true);
                    }
                }
            } else if (tag instanceof SymbolClassTag) {
                SymbolClassTag sct = (SymbolClassTag) tag;
                for (int i = 0; i < sct.tags.size(); i++) {
                    int id = sct.tags.get(i);
                    if (nameMap.containsKey(id)) {
                        sct.names.set(i, nameMap.get(id));
                        sct.setModified(true);
                    }
                }
            }
        }
    }
}
