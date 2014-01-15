/*
 *  Copyright (C) 2010-2013 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.treenodes;

import com.jpexs.decompiler.flash.treeitems.TreeItem;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public abstract class TreeNode {
    
    protected TreeItem item;
    public boolean export = false;
    public List<TreeNode> subNodes;

    public TreeNode(TreeItem item) {
        this.item = item;
        this.subNodes = new ArrayList<>();
    }
    
    public TreeItem getItem() {
        return item;
    }

    @Override
    public String toString() {
        return item.toString();
    }

    public List<TreeNode> getAllSubs() {
        List<TreeNode> ret = new ArrayList<>();
        ret.addAll(subNodes);
        for (TreeNode n : subNodes) {
            ret.addAll(n.getAllSubs());
        }
        return ret;
    }
}
