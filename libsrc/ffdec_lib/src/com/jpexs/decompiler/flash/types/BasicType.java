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

/**
 * Basic types enum.
 *
 * @author JPEXS
 */
public enum BasicType {

    /**
     * Unsigned 8-bit integer
     */
    UI8,
    /**
     * Unsigned 16-bit integer
     */
    UI16,
    /**
     * Unsigned 32-bit integer
     */
    UI32,
    /**
     * Variable length encoded unsigned 32-bit integer
     */
    EncodedU32,
    /**
     * Signed 8-bit integer
     */
    SI8,
    /**
     * Signed 16-bit integer
     */
    SI16,
    /**
     * Signed 32-bit integer
     */
    SI32,
    /**
     * Unsigned integer bits
     */
    UB,
    /**
     * Signed integer bits
     */
    SB,
    /**
     * Float bits
     */
    FB,
    /**
     * Single-precision (32-bit) floating-point number
     */
    FLOAT,
    /**
     * Half-precision (16-bit) floating-point number
     */
    FLOAT16,
    /**
     * 32-bit 16.16 fixed-point number
     */
    FIXED,
    /**
     * 16-bit 8.8 fixed-point number
     */
    FIXED8,
    /**
     * None
     */
    NONE,
    /**
     * Other
     */
    OTHER
}
