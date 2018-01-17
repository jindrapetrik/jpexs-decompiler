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
package com.jpexs.helpers.utf8;

import java.io.OutputStream;
import java.io.PrintWriter;

/**
 *
 * @author JPEXS
 */
public class Utf8PrintWriter extends PrintWriter {

    public Utf8PrintWriter(OutputStream out) {
        super(new Utf8OutputStreamWriter(out));
    }
}
