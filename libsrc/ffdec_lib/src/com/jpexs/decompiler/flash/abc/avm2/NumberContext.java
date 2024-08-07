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
package com.jpexs.decompiler.flash.abc.avm2;

/**
 * Number context.
 */
public class NumberContext {

    //Rounding modes
    public static final int ROUND_CEILING = 0;
    public static final int ROUND_UP = 1;
    public static final int ROUND_HALF_UP = 2;
    public static final int ROUND_HALF_EVEN = 3;
    public static final int ROUND_HALF_DOWN = 4;
    public static final int ROUND_DOWN = 5;
    public static final int ROUND_FLOOR = 6;

    //Usage modes
    public static final int USE_NUMBER = 0;
    public static final int USE_DECIMAL = 1;
    public static final int USE_DOUBLE = 2;
    public static final int USE_INT = 3;
    public static final int USE_UINT = 4;

    /**
     * Usage of the number.
     */
    private int usage = USE_NUMBER;
    /**
     * Precision of the number.
     */
    private int precision = 34;
    /**
     * Rounding of the number.
     */
    private int rounding = ROUND_HALF_EVEN;

    /**
     * Creates a new number context.
     *
     * @param usage Usage of the number.
     * @param precision Precision of the number.
     * @param rounding Rounding of the number.
     */
    public NumberContext(int usage, int precision, int rounding) {
        this.usage = usage;
        this.precision = precision;
        this.rounding = rounding;
    }

    /**
     * Creates a new number context.
     *
     * @param param Parameter.
     */
    public NumberContext(int param) {
        this.usage = param & 7;
        this.rounding = (param >> 3) & 7;
        this.precision = param >> 6;
    }

    /**
     * Sets the usage of the number.
     *
     * @param usage
     */
    public void setUsage(int usage) {
        if (usage > 6 || usage < 0) {
            throw new IllegalArgumentException("Invalid usage value :" + usage);
        }
        this.usage = usage;
    }

    /**
     * Gets the usage of the number.
     *
     * @return Usage of the number.
     */
    public int getUsage() {
        return usage;
    }

    /**
     * Gets the precision of the number.
     *
     * @return Precision of the number.
     */
    public int getPrecision() {
        return precision;
    }

    /**
     * Sets the precision of the number.
     *
     * @param precision Precision of the number.
     */
    public void setPrecision(int precision) {
        if (precision > 34) {
            throw new IllegalArgumentException("Maximum value of precision is 34");
        }
        this.precision = precision;
    }

    /**
     * Converts the number context to a parameter.
     *
     * @return Parameter.
     */
    public int toParam() {
        int ret = usage;
        if (usage == USE_NUMBER || usage == USE_DECIMAL) {
            ret |= (rounding << 3);
            if (precision < 34) {
                ret |= (precision << 6);
            }
        }
        return ret;
    }
}
