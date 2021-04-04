package com.jpexs.decompiler.flash.tags.base;

import com.jpexs.decompiler.flash.exporters.commonshape.ExportRectangle;
import java.util.Objects;

/**
 *
 * @author JPEXS
 */
public class DisplayObjectCacheKey {
    public PlaceObjectTypeTag placeObject;
    public double zoom;
    public ExportRectangle viewRect;

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
