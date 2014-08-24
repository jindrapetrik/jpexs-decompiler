/*
 *  Copyright (C) 2010-2014 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.exporters.morphshape;

import com.jpexs.decompiler.flash.exporters.commonshape.PointInt;

/**
 *
 * @author JPEXS
 */
public interface IMorphEdge {

    public PointInt getFrom();

    public PointInt getTo();

    public PointInt getFromEnd();

    public PointInt getToEnd();

    public int getLineStyleIdx();

    public int getFillStyleIdx();

    public IMorphEdge reverseWithNewFillStyle(int newFillStyleIdx);
}
