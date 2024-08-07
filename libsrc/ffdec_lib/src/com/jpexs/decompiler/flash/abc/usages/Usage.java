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
package com.jpexs.decompiler.flash.abc.usages;

import com.jpexs.decompiler.flash.abc.ABC;

/**
 * Usage interface.
 *
 * @author JPEXS
 */
public interface Usage {

    /**
     * Returns the ABC file.
     *
     * @return ABC file
     */
    public ABC getAbc();

    /**
     * Returns the index of the usage in the constant pool.
     *
     * @return index of the usage in the constant pool
     */
    public int getIndex();

    /**
     * Returns the kind of the usage.
     *
     * @return kind of the usage
     */
    public String getKind();
}
