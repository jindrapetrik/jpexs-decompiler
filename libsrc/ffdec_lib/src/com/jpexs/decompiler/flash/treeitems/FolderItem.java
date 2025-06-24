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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.Tag;
import java.util.List;

/**
 * A folder TreeItem - container for items.
 *
 * @author JPEXS
 */
public class FolderItem implements TreeItem {

    /**
     * SWF.
     */
    public SWF swf;

    /**
     * ToString name.
     */
    private final String str;

    /**
     * Name.
     */
    private final String name;

    /**
     * Sub items.
     */
    public final List<TreeItem> subItems;

    /**
     * Constructs FolderItem
     *
     * @param str ToString name
     * @param name Name
     * @param swf SWF
     * @param subItems Sub items
     */
    public FolderItem(String str, String name, SWF swf, List<TreeItem> subItems) {
        this.swf = swf;
        this.str = str;
        this.name = name;
        this.subItems = subItems;
    }

    /**
     * Gets name.
     *
     * @return Name
     */
    public String getName() {
        return name;
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


    @Override
    public String toString() {
        return str;
    }

    /**
     * Gets modified flag.
     *
     * @return Modified flag
     */
    @Override
    public boolean isModified() {
        if (subItems == null) {
            return false;
        }

        for (TreeItem ti : subItems) {
            if ((ti instanceof Tag) && (((Tag) ti).isReadOnly())) {
                continue;
            }
            if (ti.isModified()) {
                return true;
            }
        }
        return false;
    }
}
