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
package com.jpexs.decompiler.flash.abc.types.traits;

/**
 *
 * @author JPEXS
 */
public enum TraitType {
    METHOD,
    VAR,
    CONST,
    INITIALIZER,
    SCRIPT_INITIALIZER;

    public static TraitType getTypeForTrait(Trait t) {
        if (t instanceof TraitMethodGetterSetter) {
            return METHOD;
        }
        if (t instanceof TraitSlotConst) {
            if (((TraitSlotConst) t).isConst()) {
                return CONST;
            } else {
                return VAR;
            }
        }
        return null;
    }
}
