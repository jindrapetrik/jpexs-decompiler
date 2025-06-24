/*
 *  Copyright (C) 2022-2025 JPEXS
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
package com.jpexs.decompiler.flash.gui.tagtree;

import com.jpexs.decompiler.flash.gui.abc.ClassesListTreeModel;
import com.jpexs.decompiler.flash.treeitems.FolderItem;
import com.jpexs.decompiler.flash.treeitems.HeaderItem;
import com.jpexs.decompiler.flash.treeitems.Openable;
import com.jpexs.decompiler.flash.treeitems.OpenableList;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * @author JPEXS
 */
public class FilteredTreeModel implements TreeModel {

    private String filter;
    
    private List<String> foldersFilter;

    private TreeItem root;
    private Map<TreeItem, List<TreeItem>> subItems = new WeakHashMap<>();

    private final List<TreeModelListener> listeners = new ArrayList<>();

    private final JTree tree;

    public String getFilter() {
        return filter;
    }

    public List<String> getFoldersFilter() {
        return foldersFilter;
    }
        
    public FilteredTreeModel(String filter, List<String> foldersFilter, AbstractTagTreeModel fullModel, JTree tree) {
        this.filter = filter;
        this.foldersFilter = foldersFilter;
        this.tree = tree;

        fullModel.addTreeModelListener(new TreeModelListener() {
            @Override
            public void treeNodesChanged(TreeModelEvent e) {
                rebuildTree(fullModel);
            }

            @Override
            public void treeNodesInserted(TreeModelEvent e) {
                rebuildTree(fullModel);
            }

            @Override
            public void treeNodesRemoved(TreeModelEvent e) {
                rebuildTree(fullModel);
            }

            @Override
            public void treeStructureChanged(TreeModelEvent e) {
                rebuildTree(fullModel);
            }
        });
        rebuildTree(fullModel);
    }

    private void rebuildTree(AbstractTagTreeModel fullModel) {
        subItems.clear();
        this.root = fullModel.getRoot();
        List<String> selectionPathsList = new ArrayList<>();
        TreePath[] selectionPaths = tree.getSelectionPaths();
        if (selectionPaths != null) {
            List<String> currentPathItems = new ArrayList<>();
            for (TreePath tp : selectionPaths) {
                for (int i = 0; i < tp.getPathCount(); i++) {
                    currentPathItems.add(tp.getPathComponent(i).toString());
                }
            }
            selectionPathsList.add(String.join(".", currentPathItems));
        }
        buildTree(fullModel, this.root, "root", "", selectionPathsList);
        fireTreeStructureChanged(new TreeModelEvent(this, new Object[]{root}));
    }

    private boolean isItemSearchable(TreeItem ti) {
        if (ti instanceof Openable) {
            return false;
        }
        if (ti instanceof OpenableList) {
            return false;
        }
        if (ti instanceof FolderItem) {
            return false;
        }
        if (ti instanceof ClassesListTreeModel) {
            return false;
        }
        return true;
    }

    private void buildTree(AbstractTagTreeModel fullModel, TreeItem item, String path, String searchPath, List<String> selectionPaths) {
        List<? extends TreeItem> items = fullModel.getAllChildren(item);
        List<TreeItem> newSubItems = new ArrayList<>();
        if (filter.trim().isEmpty() && foldersFilter.isEmpty()) {
            newSubItems.addAll(items);
        } else {
            for (TreeItem ti : items) {
                String subPath = path + "." + ti.toString();
                String searchSubPath = isItemSearchable(ti) ? searchPath + "." + ti.toString() : searchPath;
                boolean matches = searchSubPath.toLowerCase().contains(filter.toLowerCase());
                
                if (!foldersFilter.isEmpty()) {                
                    if (ti instanceof ClassesListTreeModel) {
                        if (!foldersFilter.contains("scripts")) {
                            continue;
                        }
                    }
                    if (ti instanceof FolderItem) {
                        FolderItem f = (FolderItem) ti;
                        if (!foldersFilter.contains(f.getName())) {
                            continue;
                        }
                    }
                    if (ti instanceof HeaderItem) {
                        HeaderItem h = (HeaderItem) ti;
                        if (!foldersFilter.contains("header")) {
                            continue;
                        }
                    }
                }
                
                if (fullModel.isLeaf(ti)) {
                    if (matches || selectionPaths.contains(subPath)) {
                        newSubItems.add(ti);
                    }
                } else {
                    newSubItems.add(ti);
                }
            }
        }

        for (int i = 0; i < newSubItems.size(); i++) {
            TreeItem ti = newSubItems.get(i);
            String subPath = path + "." + ti.toString();
            String searchSubPath = isItemSearchable(ti) ? searchPath + "." + ti.toString() : searchPath;
            buildTree(fullModel, ti, subPath, searchSubPath, selectionPaths);
            if (!selectionPaths.contains(subPath) && !fullModel.isLeaf(ti) && (!this.subItems.containsKey(ti) || this.subItems.get(ti).isEmpty())) {
                newSubItems.remove(i);
                i--;
            }
        }

        if (!newSubItems.isEmpty()) {
            this.subItems.put(item, newSubItems);
        }
    }

    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public Object getChild(Object parent, int index) {
        if (subItems.containsKey(parent)) {
            return subItems.get(parent).get(index);
        }
        return null;
    }

    @Override
    public int getChildCount(Object parent) {
        if (subItems.containsKey(parent)) {
            return subItems.get(parent).size();
        }
        return 0;
    }

    @Override
    public boolean isLeaf(Object node) {
        if (subItems.containsKey(node)) {
            return subItems.get(node).size() == 0;
        }
        return true;
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {

    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        if (subItems.containsKey(parent)) {
            return subItems.get(parent).indexOf(child);
        }
        return -1;
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
        listeners.add(l);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        listeners.remove(l);
    }

    protected void fireTreeNodesRemoved(TreeModelEvent e) {
        for (TreeModelListener listener : listeners) {
            listener.treeNodesRemoved(e);
        }
    }

    protected void fireTreeNodesInserted(TreeModelEvent e) {
        for (TreeModelListener listener : listeners) {
            listener.treeNodesInserted(e);
        }
    }

    protected void fireTreeStructureChanged(TreeModelEvent e) {
        for (TreeModelListener listener : listeners) {
            listener.treeStructureChanged(e);
        }
    }

}
