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

import com.jpexs.decompiler.flash.exporters.modes.FrameExportMode;

/**
 * Frame export settings.
 *
 * @author JPEXS
 */
public class FrameExportSettings {

    /**
     * Export folder name
     */
    public static final String EXPORT_FOLDER_NAME = "frames";

    /**
     * Mode
     */
    public FrameExportMode mode;

    /**
     * Zoom
     */
    public double zoom;

    /**
     * Transparent background
     */
    public boolean transparentBackground;

    /**
     * Constructor.
     * @param mode Mode
     * @param zoom Zoom
     * @param transparentBackground Transparent background
     */
    public FrameExportSettings(FrameExportMode mode, double zoom, boolean transparentBackground) {
        this.mode = mode;
        this.zoom = zoom;
        this.transparentBackground = transparentBackground;
    }
}
