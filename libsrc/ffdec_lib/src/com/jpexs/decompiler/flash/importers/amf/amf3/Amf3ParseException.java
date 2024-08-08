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
package com.jpexs.decompiler.flash.importers.amf.amf3;

import com.jpexs.decompiler.flash.ParseException;

/**
 * Exception thrown when AMF3 parsing fails.
 *
 * @author JPEXS
 */
public class Amf3ParseException extends ParseException {

    /**
     * Constructor.
     * @param text Text
     * @param line Line
     */
    public Amf3ParseException(String text, long line) {
        super(text, line);
    }
}
