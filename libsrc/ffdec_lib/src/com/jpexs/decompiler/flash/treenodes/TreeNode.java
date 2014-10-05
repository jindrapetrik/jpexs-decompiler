/*
 *  Copyright (C) 2010-2014 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.treenodes;

import com.jpexs.decompiler.flash.treeitems.TreeItem;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public abstract class TreeNode {

    protected TreeItem item;
    public List<TreeNode> subNodes;

    public TreeNode(TreeItem item) {
        if (item == null) {
            throw new Error("TreeItem should not be null.");
        }
        this.item = item;
        this.subNodes = new ArrayList<>();
    }

    public TreeItem getItem() {
        return item;
    }

    @Override
    public String toString() {
        if (item == null) {
            Logger.getLogger(TreeNode.class.getName()).log(Level.FINE, "Tree item is null");
            return null;
        }
        return item.toString();
    }
}
