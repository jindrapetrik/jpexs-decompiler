/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.abc.types;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.graph.DottedChain;
import java.util.List;

/**
 * Class info.
 *
 * @author JPEXS
 */
public class ClassInfo {

    /**
     * Static class initializer method info index.
     */
    public int cinit_index;

    /**
     * Static traits.
     */
    public Traits static_traits;

    /**
     * True if class is deleted.
     */
    @Internal
    public boolean deleted;

    /**
     * Last disp_id.
     */
    @Internal
    public int lastDispId = -1;

    /**
     * Constructs a new ClassInfo.
     */
    public ClassInfo() {
        static_traits = new Traits();
    }

    /**
     * Constructs a new ClassInfo.
     *
     * @param traits Static traits
     */
    public ClassInfo(Traits traits) {
        static_traits = traits;
    }

    /**
     * To string.
     *
     * @return String
     */
    @Override
    public String toString() {
        return "method_index=" + cinit_index + "\r\n" + static_traits.toString();
    }

    /**
     * To string.
     *
     * @param abc ABC
     * @param fullyQualifiedNames Fully qualified names
     * @return String
     */
    public String toString(ABC abc, List<DottedChain> fullyQualifiedNames) {
        return "method_index=" + cinit_index + "\r\n" + static_traits.toString(abc, fullyQualifiedNames);
    }

    /**
     * Gets next disp_id.
     *
     * @return Next disp_id
     */
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
