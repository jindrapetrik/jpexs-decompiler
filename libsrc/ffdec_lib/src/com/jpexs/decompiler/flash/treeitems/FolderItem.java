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

import com.jpexs.decompiler.flash.SWF;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class FolderItem implements TreeItem {

    public SWF swf;

    private final String str;

    private final String name;

    public final List<TreeItem> subItems;

    public FolderItem(String str, String name, SWF swf, List<TreeItem> subItems) {
        this.swf = swf;
        this.str = str;
        this.name = name;
        this.subItems = subItems;
    }

    public String getName() {
        return name;
    }

    @Override
    public SWF getSwf() {
        return swf;
    }

    @Override
    public String toString() {
        return str;
    }

    @Override
    public boolean isModified() {
        if (subItems == null) {
            return false;
        }

        for (TreeItem ti : subItems) {
            if (ti.isModified()) {
                return true;
            }
        }
        return false;
    }
}
