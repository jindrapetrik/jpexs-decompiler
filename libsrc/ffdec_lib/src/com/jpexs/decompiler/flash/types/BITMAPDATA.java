/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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

import java.io.Serializable;

/**
 * Represents 32-bit alpha, red, green and blue value
 *
 * @author JPEXS
 */
public class BITMAPDATA implements Serializable {

    public PIX15[] bitmapPixelDataPix15 = new PIX15[0];

    public PIX24[] bitmapPixelDataPix24 = new PIX24[0];
}
