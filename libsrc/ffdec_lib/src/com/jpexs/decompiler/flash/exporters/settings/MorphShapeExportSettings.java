/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
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

import com.jpexs.decompiler.flash.exporters.modes.MorphShapeExportMode;

/**
 * Morph shape export settings.
 *
 * @author JPEXS
 */
public class MorphShapeExportSettings {

    /**
     * Export folder name
     */
    public static final String EXPORT_FOLDER_NAME = "morphshapes";

    /**
     * Mode
     */
    public MorphShapeExportMode mode;

    /**
     * Zoom
     */
    public double zoom;
    
    /**
     * Antialias conflation reducing scale coefficient
     */
    public int aaScale;
    
    /**
     * Duration in seconds
     */
    public Double duration;
    
    /**
     * Number of frames generated
     */
    public Integer numberOfFrames;

    /**
     * Constructor.
     * @param mode Mode
     * @param zoom Zoom
     * @param aaScale Antialias conflation reducing scale coefficient
     * @param duration Duration
     * @param numberOfFrames Number of frames
     */
    public MorphShapeExportSettings(MorphShapeExportMode mode, double zoom, int aaScale, Double duration, Integer numberOfFrames) {
        if (mode.hasFrames() && numberOfFrames == null) {
            throw new IllegalArgumentException("The requested mode requires passing number of frames.");
        }
        if (mode.hasDuration() && duration == null) {
            throw new IllegalArgumentException("The requested mode requires passing duration.");
        }
        this.mode = mode;
        this.zoom = zoom;
        this.aaScale = aaScale;
        this.duration = duration;
        this.numberOfFrames = numberOfFrames;
    }
    
    /**
     * Constructor.
     * @param mode Mode
     * @param zoom Zoom
     * @param aaScale Antialias conflation reducing scale coefficient
     */
    public MorphShapeExportSettings(MorphShapeExportMode mode, double zoom, int aaScale) {
        this(mode, zoom, aaScale, null, null);
    }

    /**
     * Get file extension.
     * @return File extension
     */
    public String getFileExtension() {
        switch (mode) {
            case PNG_START_END:
            case PNG_FRAMES:
                return ".png";
            case BMP_START_END:
            case BMP_FRAMES:
                return ".bmp";
            case WEBP_START_END:
            case WEBP_FRAMES:
            case WEBP:
                return ".webp";
            case SVG:
            case SVG_START_END:
            case SVG_FRAMES:
                return ".svg";
            case CANVAS:
                return ".html";
            case SWF:
                return ".swf";
            case GIF:
                return ".gif";
            case AVI:
                return ".avi";            
            default:
                throw new Error("Unsupported morphshape export mode: " + mode);
        }
    }
}
