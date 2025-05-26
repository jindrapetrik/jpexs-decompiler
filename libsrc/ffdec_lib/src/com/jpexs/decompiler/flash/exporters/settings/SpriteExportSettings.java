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

import com.jpexs.decompiler.flash.exporters.modes.SpriteExportMode;

/**
 * Sprite export settings.
 *
 * @author JPEXS
 */
public class SpriteExportSettings {

    /**
     * Export folder name
     */
    public static final String EXPORT_FOLDER_NAME = "sprites";

    /**
     * Mode
     */
    public SpriteExportMode mode;

    /**
     * Zoom
     */
    public double zoom;

    /**
     * Constructor.
     * @param mode Mode
     * @param zoom Zoom
     */
    public SpriteExportSettings(SpriteExportMode mode, double zoom) {
        this.mode = mode;
        this.zoom = zoom;
    }
}
