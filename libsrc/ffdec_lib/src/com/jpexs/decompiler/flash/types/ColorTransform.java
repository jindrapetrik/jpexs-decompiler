/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.types;

import com.jpexs.decompiler.flash.types.filters.Filtering;
import com.jpexs.helpers.SerializableImage;
import java.awt.Color;
import java.awt.image.RescaleOp;

/**
 *
 * @author JPEXS
 */
public class ColorTransform implements Cloneable {

    public RescaleOp toRescaleOp() {
        return new RescaleOp(new float[]{getRedMulti() / 256f, getGreenMulti() / 256f, getBlueMulti() / 256f, getAlphaMulti() / 256f},
                new float[]{getRedAdd(), getGreenAdd(), getBlueAdd(), getAlphaAdd()}, null);
    }

    public SerializableImage apply(SerializableImage src) {
        return Filtering.colorEffect(src, getRedAdd(), getGreenAdd(), getBlueAdd(), getAlphaAdd(), getRedMulti(), getGreenMulti(), getBlueMulti(), getAlphaMulti());
    }

    public int apply(int color) {
        return Filtering.colorEffect(color, getRedAdd(), getGreenAdd(), getBlueAdd(), getAlphaAdd(), getRedMulti(), getGreenMulti(), getBlueMulti(), getAlphaMulti());
    }

    public RGB apply(RGB color) {
        if (color == null) {
            return null;
        }
        return new RGBA(Filtering.colorEffect(color.toInt(), getRedAdd(), getGreenAdd(), getBlueAdd(), getAlphaAdd(), getRedMulti(), getGreenMulti(), getBlueMulti(), getAlphaMulti()));
    }

    public RGBA apply(RGBA color) {
        if (color == null) {
            return null;
        }
        return new RGBA(Filtering.colorEffect(color.toInt(), getRedAdd(), getGreenAdd(), getBlueAdd(), getAlphaAdd(), getRedMulti(), getGreenMulti(), getBlueMulti(), getAlphaMulti()));
    }

    public Color apply(Color color) {
        if (color == null) {
            return null;
        }
        return new Color(Filtering.colorEffect(color.getRGB(), getRedAdd(), getGreenAdd(), getBlueAdd(), getAlphaAdd(), getRedMulti(), getGreenMulti(), getBlueMulti(), getAlphaMulti()));
    }

    public GRADRECORD[] apply(GRADRECORD[] gradRecords) {
        GRADRECORD[] ret = new GRADRECORD[gradRecords.length];
        for (int i = 0; i < gradRecords.length; i++) {
            GRADRECORD r = gradRecords[i];
            GRADRECORD r2 = new GRADRECORD();
            r2.inShape3 = r.inShape3;
            r2.ratio = r.ratio;
            r2.color = apply(r.color);
            ret[i] = r2;
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
        return 256;
    }

    public int getGreenMulti() {
        return 256;
    }

    public int getBlueMulti() {
        return 256;
    }

    public int getAlphaMulti() {
        return 256;
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
                return (int) ((float) t.getRedMulti() / 256f * c.getRedMulti());
            }

            @Override
            public int getGreenMulti() {
                return (int) ((float) t.getGreenMulti() / 256f * c.getGreenMulti());
            }

            @Override
            public int getBlueMulti() {
                return (int) ((float) t.getBlueMulti() / 256f * c.getBlueMulti());
            }

            @Override
            public int getAlphaMulti() {
                return (int) ((float) t.getAlphaMulti() / 256f * c.getAlphaMulti());
            }
        };
    }

    @Override
    public String toString() {
        return "[colorTransform redAdd=" + getRedAdd() + ", greenAdd=" + getGreenAdd() + ", blueAdd=" + getBlueAdd() + ", alphaAdd=" + getAlphaAdd()
                + ", redMulti=" + getRedMulti() + ", greenMulti=" + getGreenMulti() + ", blueMulti=" + getBlueMulti() + ", alphaMulti=" + getAlphaMulti() + "]";
    }

    @Override
    public ColorTransform clone() {
        try {
            return (ColorTransform) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException();
        }
    }
}
