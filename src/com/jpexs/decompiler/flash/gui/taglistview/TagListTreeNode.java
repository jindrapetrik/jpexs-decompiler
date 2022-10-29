/*
 * Copyright (C) 2022 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.gui.taglistview;

import com.jpexs.decompiler.flash.tags.DoInitActionTag;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import javax.swing.tree.TreeNode;

/**
 *
 * @author JPEXS
 */
public class TagListTreeNode implements TreeNode {

    private final List<TreeNode> children = new ArrayList<>();
    private TreeNode parent;
    private Object data;

    @Override
    public String toString() {
        //hack
        if (data instanceof DoInitActionTag) {
            DoInitActionTag doinit = (DoInitActionTag) data;
            String exportName = doinit.getSwf().getExportName(doinit.spriteId);
            if (exportName != null && !exportName.isEmpty()) {
                return DoInitActionTag.NAME + " (" + doinit.spriteId + ") : " + exportName;
            }
        }
        return data.toString();
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Object getData() {
        return data;
    }

    public void addChild(TreeNode node) {
        children.add(node);
    }

    public void setParent(TreeNode parent) {
        this.parent = parent;
    }

    @Override
    public TreeNode getChildAt(int childIndex) {
        return children.get(childIndex);
    }

    @Override
    public int getChildCount() {
        return children.size();
    }

    @Override
    public TreeNode getParent() {
        return parent;
    }

    @Override
    public int getIndex(TreeNode node) {
        return children.indexOf(node);
    }

    @Override
    public boolean getAllowsChildren() {
        return true;
    }

    @Override
    public boolean isLeaf() {
        return children.isEmpty();
    }

    @Override
    public Enumeration<? extends TreeNode> children() {
        return Collections.enumeration(children);
    }
    
    public void removeChild(int index) {
        children.remove(index);
    }
}
