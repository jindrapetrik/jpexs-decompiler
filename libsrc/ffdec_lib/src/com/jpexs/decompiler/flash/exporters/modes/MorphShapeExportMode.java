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
package com.jpexs.decompiler.flash.exporters.modes;

import com.jpexs.decompiler.flash.tags.enums.ImageFormat;

/**
 * Morph shape export mode.
 *
 * @author JPEXS
 */
public enum MorphShapeExportMode {
    /**
     * SVG animation - Scalable Vector Graphics
     */
    SVG(true, false),
    /**
     * SVG start and end frames - Scalable Vector Graphics
     */
    SVG_START_END(false, false),
    /**
     * SVG individual frames - Scalable Vector Graphics
     */
    SVG_FRAMES(false, true),
    /**
     * HTML5 canvas animation
     */
    CANVAS(true, false),
    /**
     * PNG start and end frames - Portable Network Graphics
     */
    PNG_START_END(false, false),
    /**
     * PNG individual frames - Portable Network Graphics
     */    
    PNG_FRAMES(false, true),
    /**
     * BMP start and end frames - Windows Bitmap
     */
    BMP_START_END(false, false),
    /**
     * BMP individual frames - Windows Bitmap
     */    
    BMP_FRAMES(false, true),
    /**
     * WEBP
     */
    WEBP(true, false),
    /**
     * WEBP start and end frames
     */
    WEBP_START_END(false, false),
    /**
     * WEBP individual frames
     */
    WEBP_FRAMES(false, true),
    /**
     * GIF - Graphics Interchange Format
     */
    GIF(true, false),
    /**
     * AVI - Audio Video Interleave
     */
    AVI(true, false),
    /**
     * SWF - Shockwave Flash
     */
    SWF(true, false);
    
    /**
     * Whether this mode requires total duration in seconds
     */
    private final boolean duration;
    /**
     * Whether this mode requires number of frames
     */
    private final boolean frames;

    private MorphShapeExportMode(boolean duration, boolean frames) {
        this.duration = duration;
        this.frames = frames;
    }
            
    /**
     * Checks whether this mode requires total time (length) in seconds.
     * @return True on required
     */
    public boolean hasDuration() {
        return duration;
    }
    
    /**
     * Checks whether this mode requires number of frames.
     * @return True on required
     */
    public boolean hasFrames() {
        return frames;
    }
    
    /**
     * Checks whether this mode is available on current platform.
     * @return True on available.
     */
    public boolean available() {
        if (this == WEBP_START_END || this == WEBP_FRAMES || this == WEBP) {
            return ImageFormat.WEBP.available();
        }
        return true;
    }
}
