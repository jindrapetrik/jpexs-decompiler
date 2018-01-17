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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.ExportAssetsTag;
import com.jpexs.decompiler.flash.tags.SymbolClassTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.helpers.Helper;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public class SymbolClassImporter {

    public void importSymbolClasses(File importFile, SWF swf) {
        String texts = Helper.readTextFile(importFile.getPath());
        String[] lines = texts.split(Helper.newLine);
        Map<Integer, String> nameMap = new HashMap<>();
        for (String line : lines) {
            String[] pair = line.split(";");
            int characterId = Integer.parseInt(pair[0]);
            String name = pair[1];
            nameMap.put(characterId, name);
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
