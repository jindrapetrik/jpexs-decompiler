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
package com.jpexs.decompiler.flash.amf.amf3;

/**
 * AMF3 marker constants.
 */
public class Marker {

    /**
     * Undefined
     */
    public static final int UNDEFINED = 0x00;
    /**
     * Null
     */
    public static final int NULL = 0x01;
    /**
     * False
     */
    public static final int FALSE = 0x02;
    /**
     * True
     */
    public static final int TRUE = 0x03;
    /**
     * Integer
     */
    public static final int INTEGER = 0x04;
    /**
     * Double
     */
    public static final int DOUBLE = 0x05;
    /**
     * String
     */
    public static final int STRING = 0x06;
    /**
     * XML document
     */
    public static final int XML_DOC = 0x07;
    /**
     * Date
     */
    public static final int DATE = 0x08;
    /**
     * Array
     */
    public static final int ARRAY = 0x09;
    /**
     * Object
     */
    public static final int OBJECT = 0x0A;
    /**
     * XML
     */
    public static final int XML = 0x0B;
    /**
     * Byte array
     */
    public static final int BYTE_ARRAY = 0x0C;
    /**
     * Vector of integers
     */
    public static final int VECTOR_INT = 0x0D;
    /**
     * Vector of unsigned integers
     */
    public static final int VECTOR_UINT = 0x0E;
    /**
     * Vector of doubles
     */
    public static final int VECTOR_DOUBLE = 0x0F;
    /**
     * Vector of objects
     */
    public static final int VECTOR_OBJECT = 0x10;
    /**
     * Dictionary
     */
    public static final int DICTIONARY = 0x11;
}
