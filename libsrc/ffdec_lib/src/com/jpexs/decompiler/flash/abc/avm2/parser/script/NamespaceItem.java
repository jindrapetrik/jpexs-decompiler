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
package com.jpexs.decompiler.flash.abc.avm2.parser.script;

import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.graph.DottedChain;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author JPEXS
 */
public class NamespaceItem {

    public DottedChain name;
    public int kind;

    public NamespaceItem(DottedChain name, int kind) {
        this.name = name;
        this.kind = kind;
    }

    public NamespaceItem(String name, int kind) {
        this.name = DottedChain.parse(name);
        this.kind = kind;
    }

    @Override
    public String toString() {
        return Namespace.kindToStr(kind) + " " + name.toRawString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NamespaceItem other = (NamespaceItem) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return (this.kind == other.kind);
    }

    public int getCpoolIndex(AVM2ConstantPool cpool) {
        return cpool.getNamespaceId(kind, name, 0, true);
    }

    public static int getCpoolSetIndex(AVM2ConstantPool cpool, List<NamespaceItem> namespaces) {
        int[] nssa = new int[namespaces.size()];
        for (int i = 0; i < nssa.length; i++) {
            nssa[i] = namespaces.get(i).getCpoolIndex(cpool);
        }

        return cpool.getNamespaceSetId(nssa, true);
    }

}
