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
package com.jpexs.decompiler.flash.tags.base;

import com.jpexs.decompiler.flash.types.RECT;
import java.util.Set;

/**
 * An object which has bounds - rectangle.
 *
 * @author JPEXS
 */
public interface BoundedTag {

    /**
     * Gets rectangle bounds.
     *
     * @return Rectangle bounds
     */
    public RECT getRect();

    /**
     * Gets rectangle bounds with added boundedTags
     *
     * @param added Bounded tags to add
     * @return Rectangle bounds
     */
    public RECT getRect(Set<BoundedTag> added);

    /**
     * Gets rectangle including strokes.
     *
     * @return Rectangle including strokes
     */
    public RECT getRectWithStrokes();
}
