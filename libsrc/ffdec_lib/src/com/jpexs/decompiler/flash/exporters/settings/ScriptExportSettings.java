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
package com.jpexs.decompiler.flash.exporters.settings;

import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.FileTextWriter;

/**
 * Script export settings.
 *
 * @author JPEXS
 */
public class ScriptExportSettings implements Cloneable {

    /**
     * Export folder name
     */
    public static final String EXPORT_FOLDER_NAME = "scripts";

    /**
     * Mode
     */
    public ScriptExportMode mode;

    /**
     * Single file
     */
    public boolean singleFile;

    /**
     * Single file writer
     */
    public FileTextWriter singleFileWriter;

    /**
     * Ignore frame scripts
     */
    public boolean ignoreFrameScripts;
    
    /**
     * Ignore accessibility
     */
    public boolean ignoreAccessibility;

    /**
     * Export embed
     */
    public boolean exportEmbed;

    /**
     * Export embed fla mode
     */
    public boolean exportEmbedFlaMode;

    /**
     * Resample WAV
     */
    public boolean resampleWav;

    /**
     * Assets directory
     */
    public String assetsDir;

    /**
     * Modify main class to reference all classes
     */
    public boolean includeAllClasses = true;
        
    /**
     * Constructor.
     * @param mode Mode
     * @param singleFile Single file
     * @param ignoreFrameScripts Ignore frame scripts
     * @param exportEmbed Export embed
     * @param exportEmbedFlaMode Export embed fla mode
     * @param resampleWav Resample WAV
     */
    public ScriptExportSettings(
            ScriptExportMode mode,
            boolean singleFile,
            boolean ignoreFrameScripts,
            boolean exportEmbed,
            boolean exportEmbedFlaMode,
            boolean resampleWav
    ) {
        this(mode, singleFile, ignoreFrameScripts, exportEmbed, exportEmbedFlaMode, resampleWav, "/_assets/", false, false);
    }

    public ScriptExportSettings(
            ScriptExportMode mode,
            boolean singleFile,
            boolean ignoreFrameScripts,
            boolean exportEmbed,
            boolean exportEmbedFlaMode,
            boolean resampleWav,
            String assetsDir,
            boolean includeAllClasses,
            boolean ignoreAccessibility
    ) {
        this.mode = mode;
        this.singleFile = singleFile;
        this.ignoreFrameScripts = ignoreFrameScripts;
        this.exportEmbed = exportEmbed;
        this.exportEmbedFlaMode = exportEmbedFlaMode;
        this.resampleWav = resampleWav;
        this.assetsDir = assetsDir;
        this.includeAllClasses = includeAllClasses;
        this.ignoreAccessibility = ignoreAccessibility;
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
    
    @Override
    public ScriptExportSettings clone() {
        try {
            return (ScriptExportSettings) super.clone();
        } catch (CloneNotSupportedException ex) {            
            //ignored
        }
        return null;
    }       
}
