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
package com.jpexs.decompiler.flash.tags.base;

import com.jpexs.decompiler.flash.timeline.DepthState;
import com.jpexs.helpers.Cache;
import com.jpexs.helpers.SerializableImage;
import java.awt.Point;
import java.util.List;

/**
 * Rendering context.
 *
 * @author JPEXS
 */
public class RenderContext {

    /**
     * Cursor position.
     */
    public Point cursorPosition;

    /**
     * State under cursor.
     */
    public List<DepthState> stateUnderCursor;

    /**
     * Mouse button.
     */
    public int mouseButton;

    /**
     * Mouse over button.
     */
    public ButtonTag mouseOverButton;

    /**
     * Border image.
     */
    public SerializableImage borderImage;

    /**
     * Display object cache.
     */
    public Cache<DisplayObjectCacheKey, SerializableImage> displayObjectCache;

    /**
     * Enable handling buttons
     */
    public boolean enableButtons = true;
    
    /**
     * Clear display object cache.
     *
     * @param placeObject Place object
     */
    public void clearPlaceObjectCache(PlaceObjectTypeTag placeObject) {
        for (DisplayObjectCacheKey k : displayObjectCache.keys()) {
            if (k.placeObject == placeObject) {
                displayObjectCache.remove(k);
            }
        }
    }
}
