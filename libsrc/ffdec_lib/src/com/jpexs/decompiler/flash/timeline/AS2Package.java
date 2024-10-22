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
package com.jpexs.decompiler.flash.timeline;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.flash.treeitems.Openable;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * ActionScript 2 package TreeItem.
 *
 * @author JPEXS
 */
public class AS2Package implements TreeItem {

    /**
     * SWF this package resides in
     */
    private final SWF swf;

    /**
     * Name
     */
    private final String name;

    /**
     * Parent package
     */
    private final AS2Package parent;

    /**
     * Subpackages
     */
    public Map<String, AS2Package> subPackages = new TreeMap<>();

    /**
     * Scripts in this package
     */
    public Map<String, ASMSource> scripts = new TreeMap<>();

    /**
     * Whether the package is flat = in the format "mypkg.sub1.sub2" instead of
     * "sub1"
     */
    private final boolean flat;

    /**
     * Whether this is default package
     */
    private final boolean defaultPackage;

    /**
     * Constructs AS2Package.
     *
     * @param name Name
     * @param parent Parent package
     * @param swf SWF this package resides in
     * @param flat Whether the package is flat = in the format "mypkg.sub1.sub2"
     * instead of "sub1"
     * @param defaultPackage Default package
     */
    public AS2Package(String name, AS2Package parent, SWF swf, boolean flat, boolean defaultPackage) {
        this.name = name;
        this.parent = parent;
        this.swf = swf;
        this.flat = flat;
        this.defaultPackage = defaultPackage;
    }

    /**
     * Checks whether it is default package.
     *
     * @return Whether it is default package
     */
    public boolean isDefaultPackage() {
        return defaultPackage;
    }

    /**
     * Gets openable.
     *
     * @return Openable
     */
    @Override
    public Openable getOpenable() {
        return swf;
    }

    /**
     * Gets all subpackages and subscripts.
     *
     * @return All subpackages and subscripts
     */
    public List<TreeItem> getAllChildren() {
        List<TreeItem> result = new ArrayList<>(getChildCount());
        result.addAll(subPackages.values());
        result.addAll(scripts.values());
        return result;
    }

    /**
     * Gets child at index.
     *
     * @param index Index
     * @return Child at index
     */
    public TreeItem getChild(int index) {
        if (index < subPackages.size()) {
            for (AS2Package subPackage : subPackages.values()) {
                if (index == 0) {
                    return subPackage;
                }

                index--;
            }
        }

        index -= subPackages.size();

        for (ASMSource pack : scripts.values()) {
            if (index == 0) {
                return pack;
            }

            index--;
        }

        return null;
    }

    /**
     * Gets child count.
     *
     * @return Child count
     */
    public int getChildCount() {
        return subPackages.size() + scripts.size();
    }

    /**
     * Gets index of child.
     *
     * @param child Child
     * @return Index of child
     */
    public int getIndexOfChild(TreeItem child) {
        int res = 0;
        if (child instanceof AS2Package) {
            for (AS2Package pkg : subPackages.values()) {
                if (pkg.equals(child)) {
                    break;
                }
                res++;
            }
            return res;
        }

        res = subPackages.size();
        for (ASMSource pack : scripts.values()) {
            if (pack.equals(child)) {
                break;
            }
            res++;
        }

        return res;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Gets package name.
     *
     * @return Package name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets modified flag.
     *
     * @return Modified flag
     */
    @Override
    public boolean isModified() {
        for (ASMSource s : scripts.values()) {
            if (s.isModified()) {
                return true;
            }
        }
        for (AS2Package p : subPackages.values()) {
            if (p.isModified()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether the package is flat. Flat = in the format
     * "mypkg.sub1.sub2" instead of "sub1".
     *
     * @return Whether the package is flat
     */
    public boolean isFlat() {
        return flat;
    }
}
