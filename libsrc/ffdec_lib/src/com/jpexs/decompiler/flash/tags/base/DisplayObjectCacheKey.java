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

import com.jpexs.decompiler.flash.exporters.commonshape.ExportRectangle;
import java.util.Objects;

/**
 * Display object cache key.
 *
 * @author JPEXS
 */
public class DisplayObjectCacheKey {

    /**
     * Place object
     */
    public PlaceObjectTypeTag placeObject;
    /**
     * Zoom
     */
    public double zoom;
    /**
     * View rectangle
     */
    public ExportRectangle viewRect;

    /**
     * Constructor.
     *
     * @param placeObject Place object
     * @param zoom Zoom
     * @param viewRect View rectangle
     */
    public DisplayObjectCacheKey(PlaceObjectTypeTag placeObject, double zoom, ExportRectangle viewRect) {
        this.placeObject = placeObject;
        this.zoom = zoom;
        this.viewRect = viewRect;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + Objects.hashCode(this.placeObject);
        hash = 79 * hash + (int) (Double.doubleToLongBits(this.zoom) ^ (Double.doubleToLongBits(this.zoom) >>> 32));
        hash = 79 * hash + Objects.hashCode(this.viewRect);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DisplayObjectCacheKey other = (DisplayObjectCacheKey) obj;
        if (Double.doubleToLongBits(this.zoom) != Double.doubleToLongBits(other.zoom)) {
            return false;
        }
        if (!Objects.equals(this.placeObject, other.placeObject)) {
            return false;
        }
        if (!Objects.equals(this.viewRect, other.viewRect)) {
            return false;
        }
        return true;
    }

}
