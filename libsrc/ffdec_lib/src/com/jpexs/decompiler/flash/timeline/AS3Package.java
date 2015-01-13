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
 * License along with this library. */
package com.jpexs.decompiler.flash.timeline;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.treeitems.AS3ClassTreeItem;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author JPEXS
 */
public class AS3Package extends AS3ClassTreeItem {

    private final SWF swf;
    public String packageName;
    public Map<String, AS3Package> subPackages = new TreeMap<>();
    public Map<String, ScriptPack> scripts = new TreeMap<>();

    public AS3Package(String packageName, SWF swf) {
        super(packageName, null);
        this.swf = swf;
        this.packageName = packageName;
    }

    @Override
    public SWF getSwf() {
        return swf;
    }

    public AS3ClassTreeItem getChild(int index) {
        if (index < subPackages.size()) {
            for (AS3Package subPackage : subPackages.values()) {
                if (index == 0) {
                    return subPackage;
                }

                index--;
            }
        }

        index -= subPackages.size();

        for (ScriptPack pack : scripts.values()) {
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

    public int getIndexOfChild(AS3ClassTreeItem child) {
        int res = 0;
        if (child instanceof AS3Package) {
            for (AS3Package pkg : subPackages.values()) {
                if (pkg.packageName.equals(((AS3Package) child).packageName)) {
                    break;
                }
                res++;
            }
            return res;
        }

        res = subPackages.size();
        for (ScriptPack pack : scripts.values()) {
            if (pack.equals(child)) {
                break;
            }
            res++;
        }

        return res;
    }

    @Override
    public String toString() {
        return packageName;
    }
}
