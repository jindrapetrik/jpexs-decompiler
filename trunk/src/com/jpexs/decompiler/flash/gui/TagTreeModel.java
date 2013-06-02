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
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.TagNode;
import com.jpexs.decompiler.flash.abc.gui.ClassesListTreeModel;
import com.jpexs.decompiler.flash.abc.gui.TreeElement;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class TagTreeModel implements TreeModel {

    private Object root;
    private List<TagNode> list = new ArrayList<TagNode>();

    public TagTreeModel(List<TagNode> list, Object rootName) {
        this.root = rootName;
        this.list = list;
    }

    private List<Object> searchTag(Object obj, Object parent, List<Object> path) {
        List<Object> ret = null;
        int cnt = getChildCount(parent);
        for (int i = 0; i < cnt; i++) {
            Object n = getChild(parent, i);
            List<Object> newPath = new ArrayList<Object>();
            newPath.addAll(path);
            newPath.add(n);

            if (n instanceof TreeElement) {
                TreeElement te = (TreeElement) n;
                Object it = te.getItem();
                if (obj == it) {
                    return newPath;
                }
            }
            if (n instanceof TagNode) {
                TagNode nd = (TagNode) n;
                if (nd.tag == obj) {
                    return newPath;
                }
            }
            ret = searchTag(obj, n, newPath);
            if (ret != null) {
                return ret;
            }
        }
        return ret;
    }

    public TreePath getTagPath(Object obj) {
        List<Object> path = new ArrayList<Object>();
        path.add(getRoot());
        path = searchTag(obj, getRoot(), path);
        TreePath tp = new TreePath(path.toArray(new Object[path.size()]));
        return tp;
    }

    public List<TagNode> getNodeList() {
        return list;
    }

    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public Object getChild(Object parent, int index) {
        if (parent instanceof TagNode) {
            if (((TagNode) parent).tag instanceof ClassesListTreeModel) {
                ClassesListTreeModel clt = (ClassesListTreeModel) ((TagNode) parent).tag;
                return clt.getChild(clt.getRoot(), index);
            }
        } else if (parent instanceof TreeElement) {
            return ((TreeElement) parent).getChild(index);
        }
        if (parent == root) {
            return list.get(index);
        } else {
            return ((TagNode) parent).subItems.get(index);
        }
    }

    @Override
    public int getChildCount(Object parent) {
        if (parent == root) {
            return list.size();
        } else {
            if (parent instanceof TagNode) {
                if (((TagNode) parent).tag instanceof ClassesListTreeModel) {
                    ClassesListTreeModel clt = (ClassesListTreeModel) ((TagNode) parent).tag;
                    return clt.getChildCount(clt.getRoot());
                }
                return ((TagNode) parent).subItems.size();
            } else if (parent instanceof TreeElement) {
                return ((TreeElement) parent).getChildCount();
            }
            return 0;

        }
    }

    @Override
    public boolean isLeaf(Object node) {
        return (getChildCount(node) == 0);
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        if (parent instanceof TagNode) {
            if (((TagNode) parent).tag instanceof ClassesListTreeModel) {
                ClassesListTreeModel clt = (ClassesListTreeModel) ((TagNode) parent).tag;
                return clt.getIndexOfChild(clt.getRoot(), child);
            }
        }
        if (parent == root) {
            for (int t = 0; t < list.size(); t++) {
                if (list.get(t) == child) {
                    return t;
                }
            }
            return -1;
        } else {
            if (parent instanceof TagNode) {
                List<TagNode> subTags = ((TagNode) parent).subItems;
                for (int t = 0; t < subTags.size(); t++) {
                    if (subTags.get(t) == child) {
                        return t;
                    }
                }
            }
            if (parent instanceof TreeElement) {
                return ((TreeElement) parent).getIndexOfChild((TreeElement) child);
            }

            return -1;
        }
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
    }
}
