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
package com.jpexs.decompiler.flash.amf.amf3;

public class Marker {

    public static final int UNDEFINED = 0x00;
    public static final int NULL = 0x01;
    public static final int FALSE = 0x02;
    public static final int TRUE = 0x03;
    public static final int INTEGER = 0x04;
    public static final int DOUBLE = 0x05;
    public static final int STRING = 0x06;
    public static final int XML_DOC = 0x07;
    public static final int DATE = 0x08;
    public static final int ARRAY = 0x09;
    public static final int OBJECT = 0x0A;
    public static final int XML = 0x0B;
    public static final int BYTE_ARRAY = 0x0C;
    public static final int VECTOR_INT = 0x0D;
    public static final int VECTOR_UINT = 0x0E;
    public static final int VECTOR_DOUBLE = 0x0F;
    public static final int VECTOR_OBJECT = 0x10;
    public static final int DICTIONARY = 0x11;
}
