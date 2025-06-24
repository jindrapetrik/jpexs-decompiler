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
package com.jpexs.decompiler.flash.types;

import com.jpexs.decompiler.flash.types.filters.Filtering;
import com.jpexs.helpers.SerializableImage;
import java.awt.Color;
import java.awt.image.RescaleOp;

/**
 * Base class for color transform.
 *
 * @author JPEXS
 */
public class ColorTransform implements Cloneable {

    private int redAdd;
    private int greenAdd;
    private int blueAdd;
    private int alphaAdd;
    private int redMulti;
    private int greenMulti;
    private int blueMulti;
    private int alphaMulti;

    /**
     * Constructor.
     */
    public ColorTransform() {

    }

    /**
     * Converts this color transform to RescaleOp.
     *
     * @return RescaleOp
     */
    public RescaleOp toRescaleOp() {
        return new RescaleOp(new float[]{getRedMulti() / 256f, getGreenMulti() / 256f, getBlueMulti() / 256f, getAlphaMulti() / 256f},
                new float[]{getRedAdd(), getGreenAdd(), getBlueAdd(), getAlphaAdd()}, null);
    }

    /**
     * Applies this color transform to the given image.
     *
     * @param src Source image
     * @return Transformed image
     */
    public SerializableImage apply(SerializableImage src) {
        return Filtering.colorEffect(src, getRedAdd(), getGreenAdd(), getBlueAdd(), getAlphaAdd(), getRedMulti(), getGreenMulti(), getBlueMulti(), getAlphaMulti());
    }

    /**
     * Applies this color transform to the given color.
     *
     * @param color Color
     * @return Transformed color
     */
    public int apply(int color) {
        return Filtering.colorEffect(color, getRedAdd(), getGreenAdd(), getBlueAdd(), getAlphaAdd(), getRedMulti(), getGreenMulti(), getBlueMulti(), getAlphaMulti());
    }

    /**
     * Applies this color transform to the given color.
     *
     * @param color Color
     * @return Transformed color
     */
    public RGB apply(RGB color) {
        if (color == null) {
            return null;
        }
        return new RGBA(Filtering.colorEffect(color.toInt(), getRedAdd(), getGreenAdd(), getBlueAdd(), getAlphaAdd(), getRedMulti(), getGreenMulti(), getBlueMulti(), getAlphaMulti()));
    }

    /**
     * Applies this color transform to the given color.
     *
     * @param color Color
     * @return Transformed color
     */
    public RGBA apply(RGBA color) {
        if (color == null) {
            return null;
        }
        return new RGBA(Filtering.colorEffect(color.toInt(), getRedAdd(), getGreenAdd(), getBlueAdd(), getAlphaAdd(), getRedMulti(), getGreenMulti(), getBlueMulti(), getAlphaMulti()));
    }

    /**
     * Applies this color transform to the given color.
     *
     * @param color Color
     * @return Transformed color
     */
    public Color apply(Color color) {
        if (color == null) {
            return null;
        }
        return new Color(Filtering.colorEffect(color.getRGB(), getRedAdd(), getGreenAdd(), getBlueAdd(), getAlphaAdd(), getRedMulti(), getGreenMulti(), getBlueMulti(), getAlphaMulti()));
    }

    /**
     * Applies this color transform to gradient records.
     *
     * @param gradRecords Gradient records
     * @return Transformed gradient records
     */
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

    /**
     * Gets red addition.
     *
     * @return Red addition
     */
    public int getRedAdd() {
        return 0;
    }

    /**
     * Gets green addition.
     *
     * @return Green addition
     */
    public int getGreenAdd() {
        return 0;
    }

    /**
     * Gets blue addition.
     *
     * @return Blue addition
     */
    public int getBlueAdd() {
        return 0;
    }

    /**
     * Gets alpha addition.
     *
     * @return Alpha addition
     */
    public int getAlphaAdd() {
        return 0;
    }

    /**
     * Gets red multiplier.
     *
     * @return Red multiplier
     */
    public int getRedMulti() {
        return 256;
    }

    /**
     * Gets green multiplier.
     *
     * @return Green multiplier
     */
    public int getGreenMulti() {
        return 256;
    }

    /**
     * Gets blue multiplier.
     *
     * @return Blue multiplier
     */
    public int getBlueMulti() {
        return 256;
    }

    /**
     * Gets alpha multiplier.
     *
     * @return Alpha multiplier
     */
    public int getAlphaMulti() {
        return 256;
    }

    /**
     * Merges this color transform with another one.
     *
     * @param c Another color transform
     * @return Merged color transform
     */
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

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + this.getRedAdd();
        hash = 97 * hash + this.getGreenAdd();
        hash = 97 * hash + this.getBlueAdd();
        hash = 97 * hash + this.getAlphaAdd();
        hash = 97 * hash + this.getRedMulti();
        hash = 97 * hash + this.getGreenMulti();
        hash = 97 * hash + this.getBlueMulti();
        hash = 97 * hash + this.getAlphaMulti();
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
        if (!(obj instanceof ColorTransform)) {
            return false;
        }
        final ColorTransform other = (ColorTransform) obj;
        if (this.getRedAdd() != other.getRedAdd()) {
            return false;
        }
        if (this.getGreenAdd() != other.getGreenAdd()) {
            return false;
        }
        if (this.getBlueAdd() != other.getBlueAdd()) {
            return false;
        }
        if (this.getAlphaAdd() != other.getAlphaAdd()) {
            return false;
        }
        if (this.getRedMulti() != other.getRedMulti()) {
            return false;
        }
        if (this.getGreenMulti() != other.getGreenMulti()) {
            return false;
        }
        if (this.getBlueMulti() != other.getBlueMulti()) {
            return false;
        }
        return this.getAlphaMulti() == other.getAlphaMulti();
    }

}
