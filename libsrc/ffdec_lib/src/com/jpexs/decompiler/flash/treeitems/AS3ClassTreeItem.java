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
package com.jpexs.decompiler.flash.treeitems;

import com.jpexs.decompiler.flash.IdentifiersDeobfuscation;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ClassPath;
import java.util.LinkedHashSet;

/**
 * ActionScript 3 class TreeItem.
 *
 * @author JPEXS
 */
public abstract class AS3ClassTreeItem implements TreeItem {

    /**
     * Name
     */
    private final String name;

    /**
     * Class path
     */
    private final ClassPath path;

    /**
     * Namespace suffix
     */
    private final String namespaceSuffix;

    /**
     * Constructs AS3ClassTreeItem.
     *
     * @param name Name
     * @param namespaceSuffix Namespace suffix
     * @param path Class path
     */
    public AS3ClassTreeItem(String name, String namespaceSuffix, ClassPath path) {
        this.name = name;
        this.path = path;
        this.namespaceSuffix = namespaceSuffix;
    }

    /**
     * Gets name with namespace suffix.
     *
     * @return Name with namespace suffix
     */
    public String getNameWithNamespaceSuffix() {
        String ret = name;
        if (namespaceSuffix != null) {
            ret += namespaceSuffix;
        }
        return ret;
    }

    /**
     * Gets name with namespace suffix but printable.
     *
     * @return Name with namespace suffix but printable
     */
    public String getPrintableNameWithNamespaceSuffix() {
        String ret = IdentifiersDeobfuscation.printIdentifier(getSwf(), new LinkedHashSet<>(), true, name);
        if (namespaceSuffix != null) {
            ret += namespaceSuffix;
        }
        return ret;
    }

    /**
     * Gets class path as string.
     *
     * @return Class path as string
     */
    public String getPath() {
        return path.toString();
    }

    @Override
    public String toString() {
        return getPrintableNameWithNamespaceSuffix();
    }
    
    protected abstract SWF getSwf();
}
