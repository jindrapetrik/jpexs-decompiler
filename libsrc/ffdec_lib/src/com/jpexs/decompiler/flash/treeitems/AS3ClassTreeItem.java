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
package com.jpexs.decompiler.flash.treeitems;

import com.jpexs.decompiler.flash.IdentifiersDeobfuscation;
import com.jpexs.decompiler.flash.abc.ClassPath;

/**
 *
 * @author JPEXS
 */
public abstract class AS3ClassTreeItem implements TreeItem {

    private final String name;

    private final ClassPath path;

    private final String namespaceSuffix;

    public AS3ClassTreeItem(String name, String namespaceSuffix, ClassPath path) {
        this.name = name;
        this.path = path;
        this.namespaceSuffix = namespaceSuffix;
    }

    public String getNameWithNamespaceSuffix() {
        String ret = name;
        if (namespaceSuffix != null) {
            ret += namespaceSuffix;
        }
        return ret;
    }

    public String getPath() {
        return path.toString();
    }

    @Override
    public String toString() {
        String ret = IdentifiersDeobfuscation.printIdentifier(true, name);
        if (namespaceSuffix != null) {
            ret += namespaceSuffix;
        }
        return ret;
    }
}
