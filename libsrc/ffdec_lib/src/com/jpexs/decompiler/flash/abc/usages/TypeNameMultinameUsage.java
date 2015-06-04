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
package com.jpexs.decompiler.flash.abc.usages;

import com.jpexs.decompiler.flash.abc.ABC;
import java.util.ArrayList;

/**
 *
 * @author JPEXS
 */
public class TypeNameMultinameUsage extends MultinameUsage {

    public int typename_index;

    public TypeNameMultinameUsage(ABC abc, int typename_index) {
        super(abc);
        this.typename_index = typename_index;
    }

    @Override
    public String toString() {
        return "TypeName " + abc.constants.getMultiname(typename_index).toString(abc.constants, new ArrayList<>());
    }
}
