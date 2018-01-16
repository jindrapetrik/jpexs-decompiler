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
package com.jpexs.decompiler.flash.abc.types;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.graph.DottedChain;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class ClassInfo {

    public int cinit_index; //MethodInfo - static initializer

    public Traits static_traits;

    @Internal
    public boolean deleted;

    @Internal
    public int lastDispId = -1;

    public ClassInfo() {
        static_traits = new Traits();
    }

    public ClassInfo(Traits traits) {
        static_traits = traits;
    }

    @Override
    public String toString() {
        return "method_index=" + cinit_index + "\r\n" + static_traits.toString();
    }

    public String toString(ABC abc, List<DottedChain> fullyQualifiedNames) {
        return "method_index=" + cinit_index + "\r\n" + static_traits.toString(abc, fullyQualifiedNames);
    }

    public int getNextDispId() {
        if (lastDispId == -1) {
            lastDispId = 0;
            for (Trait trait : static_traits.traits) {
                if (trait instanceof TraitMethodGetterSetter) {
                    int dispId = ((TraitMethodGetterSetter) trait).disp_id;
                    if (dispId > lastDispId) {
                        lastDispId = dispId;
                    }
                }
            }
        }

        lastDispId++;
        return lastDispId;
    }
}
