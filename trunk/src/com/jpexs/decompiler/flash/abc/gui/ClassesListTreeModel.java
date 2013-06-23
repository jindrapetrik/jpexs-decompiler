/*
 *  Copyright (C) 2010-2013 JPEXS, Paolo Cancedda
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
package com.jpexs.decompiler.flash.abc.gui;

import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import java.util.HashMap;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

class ClassIndexVisitor implements TreeVisitor {

    private TreeElement found = null;
    private int classIndex = 0;

    public ClassIndexVisitor(int classIndex) {
        this.classIndex = classIndex;
    }

    @Override
    public void onBranch(TreeElement branch) {
        Object o = branch.getItem();
        if (o == null) {
            return;
        }
        ScriptPack sc = (ScriptPack) o;
        for (Trait t : sc.abc.script_info[sc.scriptIndex].traits.traits) {
            if (t instanceof TraitClass) {
                if (((TraitClass) t).class_info == classIndex) {
                    found = branch;
                    return;
                }
            }
        }
    }

    @Override
    public void onLeaf(TreeElement leaf) {
        Object o = leaf.getItem();
        if (o == null) {
            return;
        }
        ScriptPack sc = (ScriptPack) o;
        for (Trait t : sc.abc.script_info[sc.scriptIndex].traits.traits) {
            if (t instanceof TraitClass) {
                if (((TraitClass) t).class_info == classIndex) {
                    found = leaf;
                    return;
                }
            }
        }
    }

    public TreeElement getFound() {
        return found;
    }
}

public class ClassesListTreeModel implements TreeModel {

    private Tree classTree = new Tree();
    private HashMap<String, ScriptPack> list;

    public HashMap<String, ScriptPack> getList() {
        return list;
    }

    public ClassesListTreeModel(HashMap<String, ScriptPack> list) {
        this(list, null);
    }

    public ClassesListTreeModel(HashMap<String, ScriptPack> list, String filter) {
        for (String path : list.keySet()) {
            if (filter != null) {
                if (!filter.equals("")) {
                    if (!path.contains(filter)) {
                        continue;
                    }
                }
            }
            String nsName = path.contains(".") ? path.substring(path.lastIndexOf(".") + 1) : path;
            String packageName = path.contains(".") ? path.substring(0, path.lastIndexOf(".")) : "";
            classTree.add(nsName, packageName, list.get(path));
        }
        this.list = list;

    }

    public Object getItemByPath(String fullPath) {
        TreeElement elem = classTree.get(fullPath);
        if (elem == null) {
            return -1;
        }
        return elem.getItem();
    }

    public TreeElement getElementByClassIndex(int classIndex) {
        ClassIndexVisitor civ = new ClassIndexVisitor(classIndex);
        classTree.visit(civ);
        return civ.getFound();
    }

    @Override
    public Object getRoot() {
        return classTree.getRoot();
    }

    @Override
    public Object getChild(Object parent, int index) {
        TreeElement pte = (TreeElement) parent;
        TreeElement te = pte.getChild(index);
        return te;
    }

    @Override
    public int getChildCount(Object parent) {
        TreeElement te = (TreeElement) parent;
        return te.getChildCount();
    }

    @Override
    public boolean isLeaf(Object node) {
        TreeElement te = (TreeElement) node;
        return te.isLeaf();
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        TreeElement te1 = (TreeElement) parent;
        TreeElement te2 = (TreeElement) child;
        return te1.getIndexOfChild(te2);
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
    }

    @Override
    public String toString() {
        return "scripts";
    }
}
