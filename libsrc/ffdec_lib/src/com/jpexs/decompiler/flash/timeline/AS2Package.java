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
package com.jpexs.decompiler.flash.timeline;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author JPEXS
 */
public class AS2Package implements TreeItem {

    private final SWF swf;

    private final String name;

    private final AS2Package parent;

    public Map<String, AS2Package> subPackages = new TreeMap<>();

    public Map<String, ASMSource> scripts = new TreeMap<>();

    public AS2Package(String name, AS2Package parent, SWF swf) {
        this.name = name;
        this.parent = parent;
        this.swf = swf;
    }

    @Override
    public SWF getSwf() {
        return swf;
    }

    public List<TreeItem> getAllChildren() {
        List<TreeItem> result = new ArrayList<>(getChildCount());
        result.addAll(subPackages.values());
        result.addAll(scripts.values());
        return result;
    }

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

    public int getChildCount() {
        return subPackages.size() + scripts.size();
    }

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
}
