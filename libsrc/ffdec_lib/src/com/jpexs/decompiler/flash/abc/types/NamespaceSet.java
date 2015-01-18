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
package com.jpexs.decompiler.flash.abc.types;

import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;

public class NamespaceSet {

    public boolean deleted;

    public int[] namespaces;

    public NamespaceSet() {
    }

    public NamespaceSet(int[] namespaces) {
        this.namespaces = namespaces;
    }

    public String toString(AVM2ConstantPool constants) {
        String s = "";
        for (int i = 0; i < this.namespaces.length; i++) {
            if (i > 0) {
                s += ", ";
            }
            s += constants.getNamespace(namespaces[i]).getNameWithKind(constants);
        }
        return s;
    }
}
