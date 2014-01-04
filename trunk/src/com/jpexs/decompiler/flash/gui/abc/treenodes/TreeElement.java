/*
 *  Copyright (C) 2011-2013 Paolo Cancedda, JPEXS
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
package com.jpexs.decompiler.flash.gui.abc.treenodes;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.gui.abc.TreeVisitor;
import com.jpexs.decompiler.flash.treeitems.AS3PackageNodeItem;
import com.jpexs.decompiler.flash.treeitems.TreeElementItem;
import com.jpexs.decompiler.flash.treenodes.TreeNode;
import java.util.*;
import javax.swing.tree.TreePath;

public abstract class TreeElement extends TreeNode {

    private SortedMap<String, TreeElement> branches;
    private SortedMap<String, TreeElement> leafs;
    private String name;
    private String path;
    private TreeElement parent;

    public TreeElement(String name, String path, TreeElementItem item, TreeElement parent) {
        super(item);
        this.name = name;
        this.path = path;
        this.parent = parent;
        branches = new TreeMap<>();
        leafs = new TreeMap<>();
    }

    @Override
    public TreeElementItem getItem() {
        return (TreeElementItem) item;
    }

    public TreeElement getParent() {
        return parent;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public TreePath getTreePath() {
        List<TreeElement> pathList = new ArrayList<>();
        TreeElement temp = this;
        do {
            pathList.add(0, temp);
        } while ((temp = temp.getParent()) != null);
        return new TreePath(pathList.toArray());
    }

    @Override
    public String toString() {
        return name;
    }

    public TreeElement getBranch(String pathElement, SWF swf) {
        TreeElement branch = branches.get(pathElement);
        if (branch == null) {
            branch = new AS3PackageNode(pathElement, path + "." + pathElement, new AS3PackageNodeItem(pathElement, swf), this);
            branches.put(pathElement, branch);
        }
        return branch;
    }

    public void addLeaf(String pathElement, ScriptPack item) {
        ScriptPackNode child = new ScriptPackNode(pathElement, path + "." + pathElement, item, this);
        leafs.put(pathElement, child);
    }

    public TreeElement getChild(int index) {
        Iterator<TreeElement> iter;
        int startingIndex;
        if (index < branches.size()) {
            iter = branches.values().iterator();
            startingIndex = 0;
        } else {
            iter = leafs.values().iterator();
            startingIndex = branches.size();
        }
        int ii = startingIndex;
        TreeElement child = null;
        while (ii <= index && iter.hasNext()) {
            child = iter.next();
            ii++;
        }
        return child;
    }

    public int getChildCount() {
        return branches.size() + leafs.size();
    }

    public boolean isLeaf() {
        return getChildCount() == 0;
    }

    public int getIndexOfChild(TreeElement child) {
        if (getChildCount() < 1) {
            return -1;
        }
        Iterator<TreeElement> iter = branches.values().iterator();
        int ii = 0;
        TreeElement aChild = null;
        while (aChild != child && iter.hasNext()) {
            aChild = iter.next();
            if (aChild == child) {
                return ii;
            }
            ii++;
        }
        iter = leafs.values().iterator();
        while (aChild != child && iter.hasNext()) {
            aChild = iter.next();
            if (aChild == child) {
                return ii;
            }
            ii++;
        }
        return -1;
    }

    public void visitBranches(TreeVisitor visitor) {
        Iterator<TreeElement> iter = branches.values().iterator();
        while (iter.hasNext()) {
            TreeElement branch = iter.next();
            visitor.onBranch(branch);
            branch.visitLeafs(visitor);
            branch.visitBranches(visitor);
        }
    }

    public void visitLeafs(TreeVisitor visitor) {
        Iterator<TreeElement> iter = leafs.values().iterator();
        while (iter.hasNext()) {
            TreeElement leaf = iter.next();
            visitor.onLeaf(leaf);
        }
    }
}
