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
package com.jpexs.decompiler.flash.tags.base;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.types.MORPHFILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.MORPHLINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.helpers.ByteArrayRange;
import java.util.HashSet;

/**
 *
 * @author JPEXS
 */
public abstract class MorphShapeTag extends CharacterTag implements DrawableTag {

    public MorphShapeTag(SWF swf, int id, String name, ByteArrayRange data) {
        super(swf, id, name, data);
    }

    public abstract RECT getStartBounds();

    public abstract RECT getEndBounds();

    public abstract MORPHFILLSTYLEARRAY getFillStyles();

    public abstract MORPHLINESTYLEARRAY getLineStyles();

    public abstract SHAPE getStartEdges();

    public abstract SHAPE getEndEdges();

    public abstract int getShapeNum();

    public abstract SHAPEWITHSTYLE getShapeAtRatio(int ratio);

    @Override
    public RECT getRect() {
        return getRect(new HashSet<BoundedTag>());
    }
}
