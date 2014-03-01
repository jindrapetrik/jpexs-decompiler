/*
 * Copyright (C) 2014 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.types;

import com.jpexs.decompiler.flash.types.filters.Filtering;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.SerializableImage;
import java.awt.Color;
import java.awt.image.RescaleOp;
import java.io.Serializable;

/**
 *
 * @author JPEXS
 */
public class ColorTransform implements Serializable {

    public RescaleOp toRescaleOp() {
        return new RescaleOp(new float[]{getRedMulti() / 255f, getGreenMulti() / 255f, getBlueMulti() / 255f, getAlphaMulti() / 255f},
                new float[]{getRedAdd(), getGreenAdd(), getBlueAdd(), getAlphaAdd()}, null);
    }

    public SerializableImage apply(SerializableImage src) {
        return Filtering.colorEffect(src, getRedAdd(), getGreenAdd(), getBlueAdd(), getAlphaAdd(), getRedMulti(), getGreenMulti(), getBlueMulti(), getAlphaMulti());
    }

    public Color apply(Color color) {
        return Filtering.colorEffect(color, getRedAdd(), getGreenAdd(), getBlueAdd(), getAlphaAdd(), getRedMulti(), getGreenMulti(), getBlueMulti(), getAlphaMulti());
    }

    public RGB apply(RGB color) {
        if (color == null) {
            return null;
        }
        return new RGBA(Filtering.colorEffect(color.toColor(), getRedAdd(), getGreenAdd(), getBlueAdd(), getAlphaAdd(), getRedMulti(), getGreenMulti(), getBlueMulti(), getAlphaMulti()));
    }

    public RGBA apply(RGBA color) {
        if (color == null) {
            return null;
        }
        return new RGBA(Filtering.colorEffect(color.toColor(), getRedAdd(), getGreenAdd(), getBlueAdd(), getAlphaAdd(), getRedMulti(), getGreenMulti(), getBlueMulti(), getAlphaMulti()));
    }

    public GRADRECORD[] apply(GRADRECORD[] gradRecords) {
        @SuppressWarnings("unchecked")
        GRADRECORD[] ret = (GRADRECORD[]) Helper.deepCopy(gradRecords);
        for (GRADRECORD r : ret) {
            r.color = apply(r.color);
        }
        return ret;
    }

    public int getRedAdd() {
        return 0;
    }

    public int getGreenAdd() {
        return 0;
    }

    public int getBlueAdd() {
        return 0;
    }

    public int getAlphaAdd() {
        return 0;
    }

    public int getRedMulti() {
        return 255;
    }

    public int getGreenMulti() {
        return 255;
    }

    public int getBlueMulti() {
        return 255;
    }

    public int getAlphaMulti() {
        return 255;
    }

    public ColorTransform merge(final ColorTransform c) {
        final ColorTransform t = this;
        return new ColorTransform() {

            @Override
            public int getRedAdd() {
                return (t.getRedAdd() + c.getRedAdd());
            }

            @Override
            public int getGreenAdd() {
                return (t.getGreenAdd() + c.getGreenAdd());
            }

            @Override
            public int getBlueAdd() {
                return (t.getBlueAdd() + c.getBlueAdd());
            }

            @Override
            public int getAlphaAdd() {
                return (t.getAlphaAdd() + c.getAlphaAdd());
            }

            @Override
            public int getRedMulti() {
                return (int) ((float) t.getRedMulti() / 255f * c.getRedMulti());
            }

            @Override
            public int getGreenMulti() {
                return (int) ((float) t.getGreenMulti() / 255f * c.getGreenMulti());
            }

            @Override
            public int getBlueMulti() {
                return (int) ((float) t.getBlueMulti() / 255f * c.getBlueMulti());
            }

            @Override
            public int getAlphaMulti() {
                return (int) ((float) t.getAlphaMulti() / 255f * c.getAlphaMulti());
            }
        };
    }

    @Override
    public String toString() {
        return "[colorTransform redAdd=" + getRedAdd() + ", greenAdd=" + getGreenAdd() + ", blueAdd=" + getBlueAdd() + ", alphaAdd=" + getAlphaAdd()
                + ", redMulti=" + getRedMulti() + ", greenMulti=" + getGreenMulti() + ", blueMulti=" + getBlueMulti() + ", alphaMulti=" + getAlphaMulti() + "]";
    }

}
