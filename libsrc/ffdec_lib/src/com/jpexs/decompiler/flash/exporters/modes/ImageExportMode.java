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
package com.jpexs.decompiler.flash.exporters.modes;

/**
 * Image export mode.
 *
 * @author JPEXS
 */
public enum ImageExportMode {

    /**
     * PNG, GIF or JPEG, depending on what suits the best
     */
    PNG_GIF_JPEG,
    /**
     * PNG - Portable Network Graphics
     */
    PNG,
    /**
     * JPEG - Joint Photographic Experts Group
     */
    JPEG,
    /**
     * BMP - Windows Bitmap
     */
    BMP,
    /**
     * PNG, GIF or JPEG, depending on what suits the best, plus alpha channel
     */
    PNG_GIF_JPEG_ALPHA
}
