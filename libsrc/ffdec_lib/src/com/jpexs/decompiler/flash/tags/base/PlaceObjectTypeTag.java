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
package com.jpexs.decompiler.flash.tags.base;

import com.jpexs.decompiler.flash.types.CLIPACTIONS;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.filters.FILTER;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public interface PlaceObjectTypeTag {

    public int getCharacterId();

    public int getDepth();

    public MATRIX getMatrix();

    public String getInstanceName();

    public void setInstanceName(String name);

    public void setClassName(String className);

    public ColorTransform getColorTransform();

    public int getBlendMode();

    public List<FILTER> getFilters();

    public int getClipDepth();

    public String getClassName();

    public boolean cacheAsBitmap();

    public boolean isVisible();

    public RGBA getBackgroundColor();

    public boolean flagMove();

    public int getRatio();

    public CLIPACTIONS getClipActions();
}
