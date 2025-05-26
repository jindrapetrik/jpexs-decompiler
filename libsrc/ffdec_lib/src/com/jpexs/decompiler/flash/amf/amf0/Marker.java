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
package com.jpexs.decompiler.flash.amf.amf0;

/**
 * AMF0 marker.
 * @author JPEXS
 */
public class Marker {
    /**
     * Number
     */
    public static final int NUMBER = 0x00;
   
    /**
     * Boolean
     */
    public static final int BOOLEAN = 0x01;   
    
    /**
     * String
     */
    public static final int STRING = 0x02;
    
    /**
     * Object
     */
    public static final int OBJECT = 0x03;
    
    /**
     * MovieClip - Reserved, not supported
     */
    public static final int MOVIECLIP = 0x04;
    
    /**
     * Null
     */
    public static final int NULL = 0x05;
    
    /**
     * Undefined
     */
    public static final int UNDEFINED = 0x06;
    
    /**
     * Reference
     */
    public static final int REFERENCE = 0x07;
    
    /**
     * Ecma array
     */
    public static final int ECMA_ARRAY = 0x08;
    
    /**
     * Object end
     */
    public static final int OBJECT_END = 0x09;
    
    /**
     * Strict array
     */
    public static final int STRICT_ARRAY = 0x0A;
    
    /**
     * Date
     */
    public static final int DATE = 0x0B;
    
    /**
     * Long string
     */
    public static final int LONG_STRING = 0x0C;
    
    /**
     * Unsupported
     */
    public static final int UNSUPPORTED = 0x0D;
    
    /**
     * Record set - Reserved, not supported
     */    
    public static final int RECORDSET = 0x0E;
    
    /**
     * XML document
     */
    public static final int XML_DOCUMENT = 0x0F;
    
    /**
     * Typed object
     */
    public static final int TYPED_OBJECT = 0x10;
    
    /**
     * AvmPlus object
     */
    public static final int AVMPLUS_OBJECT = 0x11;
}
