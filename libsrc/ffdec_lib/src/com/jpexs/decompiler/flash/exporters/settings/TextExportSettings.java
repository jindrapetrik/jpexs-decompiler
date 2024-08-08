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
package com.jpexs.decompiler.flash.exporters.settings;

import com.jpexs.decompiler.flash.exporters.modes.TextExportMode;

/**
 * Text export settings.
 *
 * @author JPEXS
 */
public class TextExportSettings {

    /**
     * Export folder name
     */
    public static final String EXPORT_FOLDER_NAME = "texts";

    /**
     * Mode
     */
    public TextExportMode mode;

    /**
     * Single file
     */
    public boolean singleFile;

    /**
     * Zoom
     */
    public double zoom;

    /**
     * Constructor.
     * @param mode Mode
     * @param singleFile Single file
     * @param zoom Zoom
     */
    public TextExportSettings(TextExportMode mode, boolean singleFile, double zoom) {
        this.mode = mode;
        this.singleFile = singleFile;
        this.zoom = zoom;
    }
}
