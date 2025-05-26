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
package com.jpexs.decompiler.flash.exporters.modes;

/**
 * Morph shape export mode.
 *
 * @author JPEXS
 */
public enum MorphShapeExportMode {
    //TODO: implement other morphshape export modes

    /**
     * SVG animation - Scalable Vector Graphics
     */
    SVG,
    /**
     * SVG start and end frames - Scalable Vector Graphics
     */
    SVG_START_END,
    /**
     * HTML5 canvas animation
     */
    CANVAS,
    /**
     * PNG start and end frames - Portable Network Graphics
     */
    PNG_START_END,
    /**
     * BMP start and end frames - Windows Bitmap
     */
    BMP_START_END,
    //GIF,
    //AVI,
    /**
     * SWF - Shockwave Flash
     */
    SWF,
}
