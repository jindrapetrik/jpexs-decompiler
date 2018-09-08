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
 * License along with this library.
 */
package com.jpexs.decompiler.flash.exporters.settings;

import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.FileTextWriter;

/**
 *
 * @author JPEXS
 */
public class ScriptExportSettings {

    public static final String EXPORT_FOLDER_NAME = "scripts";

    public ScriptExportMode mode;

    public boolean singleFile;

    public FileTextWriter singleFileWriter;

    public ScriptExportSettings(ScriptExportMode mode, boolean singleFile) {
        this.mode = mode;
        this.singleFile = singleFile;
    }

    public String getFileExtension() {
        switch (mode) {
            case AS:
            case AS_METHOD_STUBS:
                return ".as";
            case PCODE_GRAPHVIZ:
                return ".gv";
            case PCODE:
            case PCODE_HEX:
                return ".pcode";
            case HEX:
                return ".hex";
            case CONSTANTS:
                return ".txt";
            default:
                throw new Error("Unsupported script export mode: " + mode);
        }
    }
}
